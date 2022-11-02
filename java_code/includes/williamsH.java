/*************************************************************************

	Driver for early Williams games

**************************************************************************/


/*----------- defined in machine/wmsyunit.c -----------*/

/* Generic old-Williams PIA interfaces */

/* Game-specific old-Williams PIA interfaces */

/* Generic later-Williams PIA interfaces */

/* Game-specific later-Williams PIA interfaces */

/* banking variables */

/* switches controlled by $c900 */

/* initialization */
MACHINE_INIT( defender );
MACHINE_INIT( williams );
MACHINE_INIT( williams2 );
MACHINE_INIT( joust2 );

/* banking */

/* misc */

/* Mayday protection */


/*----------- defined in vidhrdw/wmsyunit.c -----------*/


/* blitter variables */

/* tilemap variables */

/* later-Williams video control variables */

/* Blaster extra variables */



VIDEO_START( williams );

VIDEO_START( blaster );
VIDEO_START( williams2 );

