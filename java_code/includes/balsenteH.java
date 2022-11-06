/***************************************************************************

	Bally/Sente SAC-1 system

    driver by Aaron Giles

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.includes;

public class balsenteH
{
	
	
	/*----------- defined in machine/balsente.c -----------*/
	
	
	
	void balsente_noise_gen(int chip, int count, short *buffer);
	
	
	
	
	
	
	
	
	
	
	
	READ16_HANDLER( shrike_shared_68k_r );
	WRITE16_HANDLER( shrike_shared_68k_w );
	
	
	/*----------- defined in vidhrdw/balsente.c -----------*/
	
	
	}
