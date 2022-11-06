/***************************************************************************

	mcr.c

	Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
	I/O ports)

	Tapper machine started by Chris Kirmse

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.machine;

public class mcrH
{
	
	
	
	
	
	/************ Generic MCR routines ***************/
	
	
	
	
	
	WRITE16_HANDLER( mcr68_6840_upper_w );
	WRITE16_HANDLER( mcr68_6840_lower_w );
	READ16_HANDLER( mcr68_6840_upper_r );
	READ16_HANDLER( mcr68_6840_lower_r );
	
	
	
	/************ Generic character and sprite definition ***************/
	
	}
