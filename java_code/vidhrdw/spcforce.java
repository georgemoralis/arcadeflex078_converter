/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.04
 */ 
package arcadeflex.v078.vidhrdw;

public class spcforce
{
	
	
	unsigned char *spcforce_scrollram;
	
	
	public static WriteHandlerPtr spcforce_flip_screen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		flip_screen_set(~data & 0x01);
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VideoUpdateHandlerPtr video_update_spcforce  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs;
	
	
		/* draw the characters as sprites because they could be overlapping */
	
		fillbitmap(bitmap,Machine.pens[0],Machine.visible_area);
	
	
		for (offs = 0; offs < videoram_size[0]; offs++)
		{
			int code,sx,sy,col;
	
	
			sy = 8 * (offs / 32) -  (spcforce_scrollram[offs]       & 0x0f);
			sx = 8 * (offs % 32) + ((spcforce_scrollram[offs] >> 4) & 0x0f);
	
			code = videoram.read(offs)+ ((colorram.read(offs)& 0x01) << 8);
			col  = (~colorram.read(offs)>> 4) & 0x07;
	
			if (flip_screen())
			{
				sx = 248 - sx;
				sy = 248 - sy;
			}
	
			drawgfx(bitmap,Machine.gfx[0],
					code, col,
					flip_screen(), flip_screen(),
					sx, sy,
					Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
