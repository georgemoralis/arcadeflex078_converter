/*************************************************************************

	8080bw.h

*************************************************************************/

/*----------- defined in machine/8080bw.c -----------*/








/*----------- defined in sndhrdw/8080bw.c -----------*/






/*----------- defined in vidhrdw/8080bw.c -----------*/

DRIVER_INIT( 8080bw );
DRIVER_INIT( invaders );
DRIVER_INIT( invadpt2 );
DRIVER_INIT( cosmo );
DRIVER_INIT( sstrngr2 );
DRIVER_INIT( invaddlx );
DRIVER_INIT( invrvnge );
DRIVER_INIT( invad2ct );
DRIVER_INIT( schaser );
DRIVER_INIT( rollingc );
DRIVER_INIT( polaris );
DRIVER_INIT( lupin3 );
DRIVER_INIT( seawolf );
DRIVER_INIT( blueshrk );
DRIVER_INIT( desertgu );
DRIVER_INIT( helifire );
DRIVER_INIT( phantom2 );
DRIVER_INIT( bowler );
DRIVER_INIT( gunfight );
DRIVER_INIT( bandido );

void c8080bw_flip_screen_w(int data);
void c8080bw_screen_red_w(int data);
void c8080bw_helifire_colors_change_w(int data);




VIDEO_EOF (helifire);


