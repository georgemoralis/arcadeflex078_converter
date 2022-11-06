#ifndef __GSTRIKER_H
#define __GSTRIKER_H

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.includes;

public class gstrikerH
{
	
	
	/*** VS920A **********************************************/
	
	#define MAX_VS920A	2
	
	typedef struct
	{
		struct tilemap* tmap;
		data16_t* vram;
		UINT16 pal_base;
		UINT8 gfx_region;
	} sVS920A;
	
	
	#define VS920A_0_vram	(VS920A[0].vram)
	#define VS920A_1_vram	(VS920A[1].vram)
	
	
	
	
	/*** MB60553 **********************************************/
	
	#define MAX_MB60553 2
	
	typedef struct
	{
		struct tilemap* tmap;
		data16_t* vram;
		data16_t regs[8];
		UINT8 bank[8];
		UINT16 pal_base;
		UINT8 gfx_region;
	
	} tMB60553;
	
	
	#define MB60553_0_vram	(MB60553[0].vram)
	#define MB60553_1_vram	(MB60553[1].vram)
	
	
	
	
	/*** CG10103 **********************************************/
	
	#define MAX_CG10103 2
	
	typedef struct
	{
		data16_t* vram;
		UINT16 pal_base;
		UINT8 gfx_region;
	
	} tCG10103;
	
	
	#define CG10103_0_vram	(CG10103[0].vram)
	#define CG10103_1_vram	(CG10103[1].vram)
	
	#endif
}
