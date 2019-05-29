#include "mapreduce.h"
#include <unistd.h>

#include <assert.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/**
 * this is all a wrapper around the stdlib allocation
 * functions so we can atomically track objects
 */
static inline int fetch_and_add(int *variable, int value) {
  __asm__ volatile("lock; xaddl %0, %1"
                   : "+r"(value), "+m"(*variable)  // input+output
                   :                               // No input-only
                   : "memory");
  return value;
}

static int live_objs = 0;
static void *MR_alloc(size_t sz) {
  fetch_and_add(&live_objs, 1);
  return calloc(sz, 1);
}

static void MR_free(void *ptr) {
  fetch_and_add(&live_objs, -1);
  free(ptr);
}

static char *MR_strdup(char *s) {
  char *ns = MR_alloc(strlen(s));
  strcpy(ns, s);
  return ns;
}

/**
 * linked list of values
 */
typedef struct _valuenode {
  char *val;
  struct _valuenode *next;
} valuenode_t;

typedef struct _keynode {
  char *key;
  valuenode_t *todo;
  valuenode_t *done;
  // a keynode is a node on a binary tree
  struct _keynode *left;
  struct _keynode *right;
} keynode_t;

typedef struct {
  keynode_t *tree;
  pthread_mutex_t lock;
} partition_t;

static void init_partition(partition_t *);
static void free_partition(partition_t *);
static void free_keynode(keynode_t *);
static void free_valuenode(valuenode_t *);

/**
 * unfortunately, I can't figure out how to not have global state,
 * so this is all the global state of the program
 */
int argc;
char **argv;
int argi;  // the current argument that the mappers will atomically add to
int mapper_count = 0;
int partition_count = 0;
Partitioner main_partitioner = NULL;
Mapper map;
Reducer reduce;
partition_t **parts = NULL;

static void init_partition(partition_t *p) {
  assert(p != NULL);
  // the tree should start as NULL
  p->tree = NULL;
  // initialize the mutex
  pthread_mutex_init(&p->lock, NULL);
}

static void dump_keynode(keynode_t *kn, int d) {
  for (int i = 0; i < d; i++) printf(".   ");
  if (kn == NULL) {
    printf("# nil\n");
    return;
  }
  printf("# %s ", kn->key);
  int t = 0;
  valuenode_t *vn = kn->todo;
  while (vn != NULL) {
    vn = vn->next;
    t++;
  }
  int n = 0;
  vn = kn->done;
  while (vn != NULL) {
    vn = vn->next;
    n++;
  }
  printf("%d:%d", t, n);
  printf("\n");
  dump_keynode(kn->left, d + 1);
  dump_keynode(kn->right, d + 1);
}

static void free_partition(partition_t *p) {
  if (p == NULL) return;
  free_keynode(p->tree);
  MR_free(p);
}

/**
 * free a keynode correctly, recursively
 */
static void free_keynode(keynode_t *kn) {
  // instead of checking nullity of left and right, check
  // once for the current node's nullity
  if (kn == NULL) return;
  free_valuenode(kn->todo);
  free_valuenode(kn->done);
  // unconditionally free the left and right
  free_keynode(kn->left);
  free_keynode(kn->right);
  MR_free(kn->key);
  MR_free(kn);
}

/**
 * free a valuenode_t list
 */
void free_valuenode(valuenode_t *vn) {
  if (vn == NULL) return;
  free_valuenode(vn->next);
  MR_free(vn->val);
  MR_free(vn);
}

static keynode_t *new_keynode(char *key) {
  keynode_t *kn = MR_alloc(sizeof(keynode_t));

  char *nkey = MR_strdup(key);
  kn->key = nkey;
  kn->left = NULL;
  kn->right = NULL;
  kn->todo = NULL;
  kn->done = NULL;
  return kn;
}

/**
 * find a keynode in a partition table, creating if it needs to
 */
static keynode_t *find_keynode(keynode_t *kn, char *key) {
  if (key == kn->key) return kn;
  int dst = strcmp(kn->key, key);

  if (dst > 0) {
    if (kn->left == NULL) {
      kn->left = new_keynode(key);
    }
    return find_keynode(kn->left, key);
  } else if (dst < 0) {
    if (kn->right == NULL) {
      kn->right = new_keynode(key);
    }
    return find_keynode(kn->right, key);
  }
  return kn;
}

void partition_add(partition_t *p, char *key, char *val) {
  // unfortunately, we need to lock the entire partition table.
  // This could probably be done with atomic operations at the keynode level
  // TODO(atomic) do this.
  pthread_mutex_lock(&p->lock);

  keynode_t *kn = NULL;
  if (p->tree == NULL) {
    kn = new_keynode(key);
    p->tree = kn;
  } else {
    kn = find_keynode(p->tree, key);
  }

  // allocate a val
  valuenode_t *vn = MR_alloc(sizeof(valuenode_t));
  vn->val = MR_strdup(val);
  vn->next = kn->todo;
  kn->todo = vn;
  pthread_mutex_unlock(&p->lock);
}

static void *mapper_thread(void *arg) {
  while (1) {
    int i = fetch_and_add(&argi, 1);
    if (i >= argc) break;
    char *file = argv[i];
    map(file);
  }
  return NULL;
}

static char *part_get_func(char *key, int part_number) {
  partition_t *p = parts[part_number];

  keynode_t *kn = NULL;
  if (p->tree == NULL) {
    kn = new_keynode(key);
    p->tree = kn;
  } else {
    kn = find_keynode(p->tree, key);
  }


  valuenode_t *vn = kn->todo;
  if (vn == NULL) {
    return NULL;
  }

  kn->todo = vn->next;
  vn->next = kn->done;
  kn->done = vn;
  return vn->val;
}

static void keynode_reduce_walk(keynode_t *kn) {
  if (kn == NULL) return;
  // walk the left
  keynode_reduce_walk(kn->left);

  // reduce the middle
  unsigned long pn = main_partitioner(kn->key, partition_count);
  reduce(kn->key, part_get_func, pn);

  // walk the right
  keynode_reduce_walk(kn->right);
}

static void *reducer_thread(void *arg) {
  partition_t *p = arg;
  keynode_reduce_walk(p->tree);
  return NULL;
}

/**
 * MAIN API
 */

/**
 * emit is the only way to add to the partition trees via the API
 */
void MR_Emit(char *key, char *val) {
  unsigned long i = main_partitioner(key, partition_count);
  partition_t *p = parts[i];
  partition_add(p, key, val);
}

/**
 * The default partition function. it decides which partition
 * a key should go into
 */
unsigned long MR_DefaultHashPartition(char *key, int num_partitions) {
  unsigned long hash = 5381;
  int c;
  while ((c = *key++) != '\0') hash = hash * 33 + c;
  return hash % num_partitions;
}

/**
 * MR_Run is the main entrypoint to the mapreduce lib. It's how a user
 * initializes a mapreduce with a mapper and reducer function
 */
void MR_Run(int _argc, char *_argv[], Mapper _map, int mc, Reducer _reduce,
            int rc, Partitioner partition) {
  argi = 0;
  argc = _argc;
  argv = _argv;
  parts = MR_alloc(sizeof(partition_t *) * rc);

  main_partitioner = partition;

  pthread_t mappers[mc];
  pthread_t reducers[rc];

  map = _map;
  reduce = _reduce;

  // Loop through and initialize all the partitions in the global namespace
  // TODO(global var) maybe don't do global variables, somehow? I don't think
  //                  we can without breaking the API
  for (int i = 0; i < rc; i++) {
    parts[i] = MR_alloc(sizeof(partition_t));
    init_partition(parts[i]);
  }
  partition_count = rc;

  for (int i = 0; i < mc; i++) {
    pthread_create(&mappers[i], NULL, mapper_thread, NULL);
  }
  for (int i = 0; i < mc; i++) {
    pthread_join(mappers[i], NULL);
  }

  for (int i = 0; i < rc; i++) {
    pthread_create(&reducers[i], NULL, reducer_thread, parts[i]);
  }

  for (int i = 0; i < rc; i++) {
    pthread_join(reducers[i], NULL);
  }

  for (int i = 0; i < rc; i++) {
    if (0 && parts[i]->tree != NULL) {
      dump_keynode(parts[i]->tree, 0);
    }
    free_partition(parts[i]);
  }

  MR_free(parts);
  printf("%d live objs\n", live_objs);
}
