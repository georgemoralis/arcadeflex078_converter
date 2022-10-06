/*************************************************************************

	Atari I, Robot hardware

*************************************************************************/

/*----------- defined in machine/irobot.c -----------*/


DRIVER_INIT( irobot );
MACHINE_INIT( irobot );



/*----------- defined in vidhrdw/irobot.c -----------*/

PALETTE_INIT( irobot );
VIDEO_START( irobot );
VIDEO_UPDATE( irobot );


void irobot_poly_clear(void);
void irobot_run_video(void);
