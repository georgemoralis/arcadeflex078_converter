/***************************************************************************

	Taito Qix hardware

	driver by John Butler, Ed Mueller, Aaron Giles

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package includes;

public class qixH
{
	
	
	/*----------- defined in machine/qix.c -----------*/
	
	
	MACHINE_INIT( qix );
	MACHINE_INIT( qixmcu );
	MACHINE_INIT( slither );
	
	
	
	
	
	
	
	
	/*----------- defined in vidhrdw/qix.c -----------*/
	
	
	
	INTERRUPT_GEN( qix_vblank_start );
	void qix_scanline_callback(int scanline);
	
	
	}
