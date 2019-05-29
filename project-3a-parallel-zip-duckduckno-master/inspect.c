#include <stdio.h>

typedef struct run {
	int len;
	char c;
} __attribute__((packed)) run_t;

int main() {
	run_t run;
	while (fread(&run, sizeof(run_t), 1, stdin)) {
		printf("%i '%c'\n", run.len, run.c);
	}
	return 0;
}
