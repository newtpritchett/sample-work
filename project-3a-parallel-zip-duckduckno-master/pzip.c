#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>
#include <sys/mman.h>
#include <sys/sysinfo.h>

#define CHUNKSZ 4096

// The data structure corresponding to a task's input
typedef struct input {
	int len;        // amount of characters to be processed
	int index;      // intended position in the output array
	char *contents; // the aforementioned characters
} input_t;

// A single run in the run-length encoding schema
typedef struct run {
	int len; // length of the run
	char c;  // the character to be repeated
} __attribute__((packed)) run_t;

// The data structure corresponding to a task's output
typedef struct output {
	int len;         // amount of runs to be output
	run_t *contents; // the aforementioned runs
} output_t;

typedef struct node {
	input_t val;
	struct node *next;
} node_t;

typedef struct queue {
	struct node *start;
	struct node *end;
	pthread_mutex_t lock;
} queue_t;

void queue_append(queue_t *d, input_t val) {
	node_t *n = malloc(sizeof(node_t));
	n->val = val;
	n->next = NULL;
	pthread_mutex_lock(&d->lock);
	if (d->start == NULL) {
		d->start = n;
		d->end = n;
		pthread_mutex_unlock(&d->lock);
		return;
	}
	d->end->next = n;
	d->end = n;
	pthread_mutex_unlock(&d->lock);
}

input_t queue_take(queue_t *d, int *empty) {
	pthread_mutex_lock(&d->lock);
	node_t *n = d->start;
	if (n == NULL) {
		*empty = 1;
		pthread_mutex_unlock(&d->lock);
		return (input_t) {};
	}
	d->start = n->next;
	pthread_mutex_unlock(&d->lock);
	input_t val = n->val;
	free(n);
	*empty = 0;
	return val;
}

// If A ends with the same character with which B starts,
//     the correct behavior is to merge the two runs.
// This function does so, if appropriate,
//     by truncating A and extending B[0].
void merge_outputs(output_t *a, output_t *b) {
	if (a == NULL || b == NULL || a->len == 0 || b->len == 0) {
		// The null checks are mostly sentinel values
		// i.e. "this is the (first|last) chunk"
		return;
	}
	run_t *end_of_a = a->contents + a->len - 1;
	run_t *start_of_b = b->contents;
	if (end_of_a->c == start_of_b->c) {
		start_of_b->len += end_of_a->len;
		a->len--;
	}
}

// Perform run-length encoding on a chunk of input.
output_t process(input_t in) {
	int run_len = 0;
	char run_ch;
	output_t out;
	out.len = 0;
	if (in.len == 0 /* the "wat" special case */) {
		out.contents = NULL;
		return out;
	}
	out.contents = calloc(CHUNKSZ, sizeof(run_t));
	run_ch = in.contents[0];
	for (int i = 0; i < in.len; i++) {
		char ch = in.contents[i];
		if (ch == run_ch) {
			run_len++;
		} else {
			out.contents[out.len++] = (run_t) {.len = run_len, .c = run_ch};
			run_len = 1;
			run_ch = ch;
		}
	}
	out.contents[out.len++] = (run_t) {.len = run_len, .c = run_ch};
	return out;
}

// Split a big string, given its size, into however many input_t objects
input_t *chop(char *buf, int size, int *r_nchunks) {
	int nchunks = (size / CHUNKSZ) + (size % CHUNKSZ != 0); // ceil division
	input_t *chunks = calloc(nchunks, sizeof(input_t));
	for (int i = 0; i < nchunks; i++) {
		if (i == nchunks - 1 && size % CHUNKSZ != 0) {
			// This is the last block, and it's smaller than usual
			chunks[i].len = size % CHUNKSZ;
		} else {
			// This is an ordinary block, or the last one is the right size
			chunks[i].len = CHUNKSZ;
		}
		// contents should just point to somewhere within buf
		chunks[i].contents = buf + (i * CHUNKSZ);
	}
	*r_nchunks = nchunks;
	return chunks;
}

output_t *out_chunks;

void *do_work(void *x) {
	queue_t *main_queue = x;
	while (1) {
		int empty;
		input_t in_chunk = queue_take(main_queue, &empty);
		if (empty) break;
		out_chunks[in_chunk.index] = process(in_chunk);
		munmap(in_chunk.contents, in_chunk.len);
	}
	return NULL;
}

int main (int argc, char *argv[]) {
	queue_t main_queue;
	main_queue.start = NULL;
	main_queue.end = NULL;
	pthread_mutex_init(&main_queue.lock, NULL);
	int qlen = 0;

	for (int i = 1; i < argc; i++) {
		// Open the file
		FILE *input_file = fopen(argv[i], "r");
		if (input_file == NULL) {
			printf("invalid file: %s\n", argv[i]);
			exit(1);
		}

		// Get the file's descriptor and length
		int fd = fileno(input_file);
		fseek(input_file, 0L, SEEK_END);
		int sz = ftell(input_file);
		rewind(input_file);

		// Map the file to memory
		char *buf = mmap(NULL, sz, PROT_READ, MAP_PRIVATE, fd, 0);
		if (buf == NULL) {
			printf("couldn't map file: %s\n", argv[i]);
			exit(1);
		}

		// Split the mmap'd file into chunks of <= CHUNKSZ bytes
		int num_chunks;
		input_t *chunk_list = chop(buf, sz, &num_chunks);
		// Put each chunk into a queue of all the chunks
		for (int j = 0; j < num_chunks; j++) {
			chunk_list[j].index = qlen++;
			queue_append(&main_queue, chunk_list[j]);
		}

		free(chunk_list);
		fclose(input_file);
	}

	out_chunks = calloc(qlen, sizeof(output_t));

	int nprocs = get_nprocs();
	pthread_t *threads = calloc(nprocs, sizeof(pthread_t));
	for (int i = 0; i < nprocs; i++) {
		pthread_create(&threads[i], NULL, do_work, &main_queue);
	}
	for (int i = 0; i < nprocs; i++) {
		pthread_join(threads[i], NULL);
	}

	for (int i = 0; i < qlen; i++) {
		if (i+1 < qlen) merge_outputs(&out_chunks[i], &out_chunks[i+1]);
		fwrite(out_chunks[i].contents, sizeof(run_t), out_chunks[i].len, stdout);
		free(out_chunks[i].contents);
	}

	free(out_chunks);
}
