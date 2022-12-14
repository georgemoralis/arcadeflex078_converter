/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class mermaid
{
	
	
	
	unsigned char* mermaid_background_videoram;
	unsigned char* mermaid_foreground_videoram;
	unsigned char* mermaid_foreground_colorram;
	unsigned char* mermaid_background_scrollram;
	unsigned char* mermaid_foreground_scrollram;
	
	
	static struct rectangle spritevisiblearea =
	{
		0*8, 26*8-1,
		2*8, 30*8-1
	};
	
	static struct rectangle flip_spritevisiblearea =
	{
		6*8, 31*8-1,
		2*8, 30*8-1
	};
	
	
	public static WriteHandlerPtr mermaid_flip_screen_x_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		flip_screen_x_set(data & 0x01);
	} };
	
	public static WriteHandlerPtr mermaid_flip_screen_y_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		flip_screen_y_set(data & 0x01);
	} };
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  I'm not sure about the resistor value, I'm using the Galaxian ones.
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_mermaid  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		int i;
	
		/* first, the char acter/sprite palette */
		for (i = 0;i < TOTAL_COLORS(0); i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
			/* red component */
			bit0 = (color_prom.read()>> 0) & 0x01;
			bit1 = (color_prom.read()>> 1) & 0x01;
			bit2 = (color_prom.read()>> 2) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (color_prom.read()>> 3) & 0x01;
			bit1 = (color_prom.read()>> 4) & 0x01;
			bit2 = (color_prom.read()>> 5) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (color_prom.read()>> 6) & 0x01;
			bit2 = (color_prom.read()>> 7) & 0x01;
			b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
		/* blue background */
		palette_set_color(TOTAL_COLORS(0),0,0,0xff);
	
		/* set up background palette */
	    COLOR(2,0) = 32;
	    COLOR(2,1) = 33;
	
	    COLOR(2,2) = 64;
	    COLOR(2,3) = 33;
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VideoUpdateHandlerPtr video_update_mermaid  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs;
	
	
		/* for every character in the backround RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = 0; offs < videoram_size[0]; offs ++)
		{
			int code,sx,sy;
	
	
			sy = 8 * (offs / 32);
			sx = 8 * (offs % 32);
	
			code = mermaid_background_videoram[offs];
	
			if (flip_screen_x)
				sx = 248 - sx;
	
			if (flip_screen_y)
				sy = 248 - sy;
	
			drawgfx(tmpbitmap,Machine.gfx[2],
					code,
					(flip_screen_x ?
					    ((sx <= 5*8) ? 0 : 1) :
					    ((sx >= 26*8) ? 0 : 1)),
					flip_screen_x,flip_screen_y,
					sx,sy,
					0,TRANSPARENCY_NONE,0);
		}
	
	
		/* copy the temporary bitmap to the screen */
		{
			int i, scroll[32];
	
	
			for (i = 0;i < 32;i++)
			{
				scroll[i] = (flip_screen_x ?
				    mermaid_background_scrollram[31 - i] :
				    -mermaid_background_scrollram[i]);
			}
	
	
			copyscrollbitmap(bitmap,tmpbitmap,0,0,32,scroll,Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* draw the front layer. They are characters, but draw them as sprites */
		for (offs = 0; offs < videoram_size[0]; offs ++)
		{
			int code,sx,sy,flipx,flipy;
	
	
			sy = 8 * (offs / 32);
			sx =     (offs % 32);
	
			sy = (sy - mermaid_foreground_scrollram[sx]) & 0xff;
	
			code = mermaid_foreground_videoram[offs] | ((mermaid_foreground_colorram[offs] & 0x30) << 4);
	
			flipx = mermaid_foreground_colorram[offs] & 0x40;
			flipy = mermaid_foreground_colorram[offs] & 0x80;
	
			if (flip_screen_x)
			{
				sx = 31 - sx;
				flipx = NOT(flipx);
			}
	
			if (flip_screen_y)
			{
				sy = 248 - sy;
				flipy = NOT(flipy);
			}
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					mermaid_foreground_colorram[offs] & 0x0f,
					flipx,flipy,
					8*sx,sy,
					Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	
	
		/* draw the sprites */
		for (offs = spriteram_size[0] - 4;offs >= 0;offs -= 4)
		{
	#ifdef MAME_DEBUG
			#endif
			UINT8 flipx,flipy,sx,sy,code,bank = 0;
	
	
			sx = spriteram.read(offs + 3)+ 1;
			sy = 240 - spriteram.read(offs + 1);
			flipx = spriteram.read(offs + 0)& 0x40;
			flipy = spriteram.read(offs + 0)& 0x80;
	
			/* this doesn't look correct. Oh really? Maybe there is a PROM. */
			switch (spriteram.read(offs + 2)& 0xf0)
			{
			case 0x00:  bank = 2; break;
			case 0x10:  bank = 1; break;
			case 0x20:  bank = 2; break;
			case 0x30:  bank = 3; break;
			case 0x50:  bank = 1; break;
			case 0x60:  bank = 2; break;
			case 0x80:  bank = 0; break;
			case 0x90:  bank = 3; break;
			case 0xa0:  bank = 2; break;
			case 0xb0:  bank = 3; break;
	#ifdef MAME_DEBUG
			default:  debug_key_pressed = 1; break;
	#endif
			}
	
			code = (spriteram.read(offs + 0)& 0x3f) | (bank << 6);
	
			if (flip_screen_x) {
				flipx = NOT(flipx);
				sx = 240 - sx;
			}
	
			if (flip_screen_y) {
				flipy = NOT(flipy);
				sy = spriteram.read(offs + 1);
			}
	
			drawgfx(bitmap,Machine.gfx[1],
					code,
					spriteram.read(offs + 2)& 0x0f,
					flipx, flipy,
					sx, sy,
					(flip_screen_x ? &flip_spritevisiblearea :
					    spritevisiblearea),
					TRANSPARENCY_PEN,0);
		}
	} };
}
