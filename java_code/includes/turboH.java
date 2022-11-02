/*************************************************************************

	Sega Z80-3D system

*************************************************************************/

/*----------- defined in machine/turbo.c -----------*/




MACHINE_INIT( turbo );
MACHINE_INIT( subroc3d );
MACHINE_INIT( buckrog );



void turbo_rom_decode(void);

void turbo_update_tachometer(void);
void turbo_update_segments(void);



/*----------- defined in vidhrdw/turbo.c -----------*/


VIDEO_START( turbo );
VIDEO_EOF( turbo );

VIDEO_START( subroc3d );

VIDEO_START( buckrog );

