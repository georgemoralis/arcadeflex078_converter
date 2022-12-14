#ifndef __2612INTF_H__
#define __2612INTF_H__

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.sound;

public class _2612intfH
{
	#ifdef BUILD_YM2612
	  void YM2612UpdateRequest(int chip);
	#endif
	
	#define   MAX_2612    (2)
	
	#define YM2612interface AY8910interface
	
	int  YM2612_sh_start(const struct MachineSound *msound);
	void YM2612_sh_stop(void);
	void YM2612_sh_reset(void);
	
	/************************************************/
	/* Chip 0 functions								*/
	/************************************************/
	
	/************************************************/
	/* Chip 1 functions								*/
	/************************************************/
	
	/**************************************************/
	/*   YM2612 left/right position change (TAITO)    */
	/**************************************************/
	
	#endif
	/**************** end of file ****************/
}
