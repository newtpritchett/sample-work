
void main(void){
	char *addr = (char*)0xb8000;
	addr[0] = 'O';
	addr[1] = 0x0F;
	addr[2] = 'K';
	addr[3] = 0x0F;
	while(1){};
	return;
}
