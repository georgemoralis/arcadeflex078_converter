/***************************************************************************

	Taito Qix hardware

	driver by John Butler, Ed Mueller, Aaron Giles

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 

public class qixH
{
	
	
	/*----------- defined in machine/qix.c -----------*/
	
	
	MACHINE_INIT( qix );
	MACHINE_INIT( qixmcu );
	MACHINE_INIT( slither );
	
	
	
	
	
	
	
	
	/*----------- defined in vidhrdw/qix.c -----------*/
	
	
	
	void qix_scanline_callback(int scanline);
	
	
	}
