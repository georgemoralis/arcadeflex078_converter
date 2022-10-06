/*************************************************************************

	Atari Battle Zone hardware

*************************************************************************/

/*----------- defined in drivers/bzone.c -----------*/



/*----------- defined in sndhrdw/bzone.c -----------*/


int bzone_sh_start(const struct MachineSound *msound);
void bzone_sh_stop(void);
void bzone_sh_update(void);


/*----------- defined in sndhrdw/redbaron.c -----------*/


int redbaron_sh_start(const struct MachineSound *msound);
void redbaron_sh_stop(void);
void redbaron_sh_update(void);
