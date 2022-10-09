/*************************************************************************

	Exidy 6502 hardware

*************************************************************************/

/*----------- defined in sndhrdw/exidy.c -----------*/

int exidy_sh_start(const struct MachineSound *msound);




/*----------- defined in sndhrdw/targ.c -----------*/


int targ_sh_start(const struct MachineSound *msound);
void targ_sh_stop(void);



/*----------- defined in vidhrdw/exidy.c -----------*/

#define PALETTE_LEN 8
#define COLORTABLE_LEN 20







