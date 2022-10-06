/*************************************************************************

	Sega G-80 raster hardware

*************************************************************************/

/*----------- defined in machine/segar.c -----------*/


void sega_security(int chip);


/*----------- defined in sndhrdw/segar.c -----------*/

 
   
/* temporary speech handling through samples */
int astrob_speech_sh_start(const struct MachineSound *msound);
void astrob_speech_sh_update(void);

/* sample names */


/*----------- defined in vidhrdw/segar.c -----------*/




PALETTE_INIT( segar );
VIDEO_START( segar );
VIDEO_UPDATE( segar );


VIDEO_START( monsterb );
VIDEO_UPDATE( monsterb );


VIDEO_START( spaceod );
VIDEO_UPDATE( spaceod );



VIDEO_UPDATE( sindbadm );
