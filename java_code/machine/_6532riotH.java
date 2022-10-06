/***************************************************************************

  RIOT 6532 emulation

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package machine;

public class _6532riotH
{
	
	
	struct R6532interface
	{
		mem_read_handler portA_r;
		mem_read_handler portB_r;
	
		mem_write_handler portA_w;
		mem_write_handler portB_w;
	};
	
	
	extern void r6532_init(int n, const struct R6532interface* RI);
	
	extern extern 
	extern extern }
