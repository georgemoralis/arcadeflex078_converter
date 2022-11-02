/*************************************************************************

	Meadows S2650 hardware

*************************************************************************/

/*----------- defined in sndhrdw/meadows.c -----------*/

int meadows_sh_start(const struct MachineSound *msound);
void meadows_sh_stop(void);
void meadows_sh_dac_w(int data);
void meadows_sh_update(void);


/*----------- defined in vidhrdw/meadows.c -----------*/


