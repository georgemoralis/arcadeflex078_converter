/***************************************************************************

Functions to emulate the video hardware of the machine.

 Video hardware of this hardware is almost similar with "mexico86". So,
 most routines are derived from mexico86 driver.

***************************************************************************/


/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class exzisus
{
	
	
	UINT8 *exzisus_videoram0;
	UINT8 *exzisus_videoram1;
	UINT8 *exzisus_objectram0;
	UINT8 *exzisus_objectram1;
	size_t  exzisus_objectram_size0;
	size_t  exzisus_objectram_size1;
	
	
	
	/***************************************************************************
	  Memory handlers
	***************************************************************************/
	
	public static ReadHandlerPtr exzisus_videoram_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return exzisus_videoram0[offset];
	} };
	
	
	public static ReadHandlerPtr exzisus_videoram_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return exzisus_videoram1[offset];
	} };
	
	
	public static ReadHandlerPtr exzisus_objectram_0_r  = new ReadHandlerPtr() { public int handler(int offset){
		return exzisus_objectram0[offset];
	} };
	
	
	public static ReadHandlerPtr exzisus_objectram_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return exzisus_objectram1[offset];
	} };
	
	
	public static WriteHandlerPtr exzisus_videoram_0_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		exzisus_videoram0[offset] = data;
	} };
	
	
	public static WriteHandlerPtr exzisus_videoram_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		exzisus_videoram1[offset] = data;
	} };
	
	
	public static WriteHandlerPtr exzisus_objectram_0_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		exzisus_objectram0[offset] = data;
	} };
	
	
	public static WriteHandlerPtr exzisus_objectram_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		exzisus_objectram1[offset] = data;
	} };
	
	
	/***************************************************************************
	  Screen refresh
	***************************************************************************/
	
	public static VideoUpdateHandlerPtr video_update_exzisus  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs;
		int sx, sy, xc, yc;
		int gfx_num, gfx_attr, gfx_offs;
	
		/* Is this correct ? */
		fillbitmap(bitmap, Machine.pens[1023], Machine.visible_area);
	
		/* ---------- 1st TC0010VCU ---------- */
		sx = 0;
		for (offs = 0 ; offs < exzisus_objectram_size0 ; offs += 4)
	    {
			int height;
	
			/* Skip empty sprites. */
			if ( !(*(UINT32 *)(&exzisus_objectram0[offs])) )
			{
				continue;
			}
	
			gfx_num = exzisus_objectram0[offs + 1];
			gfx_attr = exzisus_objectram0[offs + 3];
	
			if ((gfx_num & 0x80) == 0)	/* 16x16 sprites */
			{
				gfx_offs = ((gfx_num & 0x7f) << 3);
				height = 2;
	
				sx = exzisus_objectram0[offs + 2];
				sx |= (gfx_attr & 0x40) << 2;
			}
			else	/* tilemaps (each sprite is a 16x256 column) */
			{
				gfx_offs = ((gfx_num & 0x3f) << 7) + 0x0400;
				height = 32;
	
				if (gfx_num & 0x40)			/* Next column */
				{
					sx += 16;
				}
				else
				{
					sx = exzisus_objectram0[offs + 2];
					sx |= (gfx_attr & 0x40) << 2;
				}
			}
	
			sy = 256 - (height << 3) - (exzisus_objectram0[offs]);
	
			for (xc = 0 ; xc < 2 ; xc++)
			{
				int goffs = gfx_offs;
				for (yc = 0 ; yc < height ; yc++)
				{
					int code, color, x, y;
	
					code  = (exzisus_videoram0[goffs + 1] << 8) | exzisus_videoram0[goffs];
					color = (exzisus_videoram0[goffs + 1] >> 6) | (gfx_attr & 0x0f);
					x = (sx + (xc << 3)) & 0xff;
					y = (sy + (yc << 3)) & 0xff;
	
					if (flip_screen())
					{
						x = 248 - x;
						y = 248 - y;
					}
	
					drawgfx(bitmap, Machine.gfx[0],
							code & 0x3fff,
							color,
							flip_screen(), flip_screen(),
							x, y,
							Machine.visible_area, TRANSPARENCY_PEN, 15);
					goffs += 2;
				}
				gfx_offs += height << 1;
			}
		}
	
		/* ---------- 2nd TC0010VCU ---------- */
		sx = 0;
		for (offs = 0 ; offs < exzisus_objectram_size1 ; offs += 4)
	    {
			int height;
	
			/* Skip empty sprites. */
			if ( !(*(UINT32 *)(&exzisus_objectram1[offs])) )
			{
				continue;
			}
	
			gfx_num = exzisus_objectram1[offs + 1];
			gfx_attr = exzisus_objectram1[offs + 3];
	
			if ((gfx_num & 0x80) == 0)	/* 16x16 sprites */
			{
				gfx_offs = ((gfx_num & 0x7f) << 3);
				height = 2;
	
				sx = exzisus_objectram1[offs + 2];
				sx |= (gfx_attr & 0x40) << 2;
			}
			else	/* tilemaps (each sprite is a 16x256 column) */
			{
				gfx_offs = ((gfx_num & 0x3f) << 7) + 0x0400;	///
				height = 32;
	
				if (gfx_num & 0x40)			/* Next column */
				{
					sx += 16;
				}
				else
				{
					sx = exzisus_objectram1[offs + 2];
					sx |= (gfx_attr & 0x40) << 2;
				}
			}
			sy = 256 - (height << 3) - (exzisus_objectram1[offs]);
	
			for (xc = 0 ; xc < 2 ; xc++)
			{
				int goffs = gfx_offs;
				for (yc = 0 ; yc < height ; yc++)
				{
					int code, color, x, y;
	
					code  = (exzisus_videoram1[goffs + 1] << 8) | exzisus_videoram1[goffs];
					color = (exzisus_videoram1[goffs + 1] >> 6) | (gfx_attr & 0x0f);
					x = (sx + (xc << 3)) & 0xff;
					y = (sy + (yc << 3)) & 0xff;
	
					if (flip_screen())
					{
						x = 248 - x;
						y = 248 - y;
					}
	
					drawgfx(bitmap, Machine.gfx[1],
							code & 0x3fff,
							color,
							flip_screen(), flip_screen(),
							x, y,
							Machine.visible_area, TRANSPARENCY_PEN, 15);
					goffs += 2;
				}
				gfx_offs += height << 1;
			}
		}
	} };
}
