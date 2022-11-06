#ifndef _system1_H_
#define _system1_H_

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class system1H
{
	
	#define SPR_Y_TOP		0
	#define SPR_Y_BOTTOM	1
	#define SPR_X_LO		2
	#define SPR_X_HI		3
	#define SPR_SKIP_LO		4
	#define SPR_SKIP_HI		5
	#define SPR_GFXOFS_LO	6
	#define SPR_GFXOFS_HI	7
	
	#define system1_BACKGROUND_MEMORY_SINGLE 0
	#define system1_BACKGROUND_MEMORY_BANKED 1
	
	
	
	void system1_define_background_memory(int Mode);
	
	
	
	#endif
}
