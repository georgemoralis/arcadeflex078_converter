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


PALETTE_INIT( turbo );
VIDEO_START( turbo );
VIDEO_EOF( turbo );
VIDEO_UPDATE( turbo );

PALETTE_INIT( subroc3d );
VIDEO_START( subroc3d );
VIDEO_UPDATE( subroc3d );

PALETTE_INIT( buckrog );
VIDEO_START( buckrog );
VIDEO_UPDATE( buckrog );

