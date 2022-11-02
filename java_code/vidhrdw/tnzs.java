/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/
/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class tnzs
{
	
	
	
	
	
	static int tnzs_screenflip;
	
	/***************************************************************************
	
	  The New Zealand Story doesn't have a color PROM. It uses 1024 bytes of RAM
	  to dynamically create the palette. Each couple of bytes defines one
	  color (15 bits per pixel; the top bit of the second byte is unused).
	  Since the graphics use 4 bitplanes, hence 16 colors, this makes for 32
	  different color codes.
	
	***************************************************************************/
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Arkanoid has a two 512x8 palette PROMs. The two bytes joined together
	  form 512 xRRRRRGGGGGBBBBB color values.
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_arknoid2  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom)
	{
		int i,col,r,g,b;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			col = (color_prom.read(i)<<8)+color_prom.read(i+512);
			r =  (col & 0x7c00)>>7;	/* Red */
			g =  (col & 0x03e0)>>2;	/* Green */
			b =  (col & 0x001f)<<3;	/* Blue */
			palette_set_color(i,r,g,b);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	void tnzs_vh_draw_background(struct mame_bitmap *bitmap,unsigned char *m)
	{
		int x,y,column,tot;
		int scrollx, scrolly;
		unsigned int upperbits;
	
	
		/* If the byte at f301 has bit 0 clear, then don't draw the
		   background tiles -WRONG- */
	
		/* The byte at f200 is the y-scroll value for the first column.
		   The byte at f204 is the LSB of x-scroll value for the first column.
	
		   The other columns follow at 16-byte intervals.
	
		   The 9th bit of each x-scroll value is combined into 2 bytes
		   at f302-f303 */
	
		/* f301 seems to control how many columns are drawn but it's not clear how. */
		/* Arkanoid 2 also uses f381, which TNZS always leaves at 00. */
		/* Maybe it's a background / foreground thing? In Arkanoid 2, f381 contains */
		/* the value we expect for the background stars (2E vs. 2A), while f301 the */
		/* one we expect at the beginning of a level (2C vs. 2A). */
		x = tnzs_scrollram[0x101] & 0xf;
		if (x == 1) x = 16;
		y = tnzs_scrollram[0x181] & 0xf;
		if (y == 1) y = 16;
		/* let's just pick the larger value... */
		tot = x;
		if (y > tot) tot = y;
	
		upperbits = tnzs_scrollram[0x102] + tnzs_scrollram[0x103] * 256;
		/* again, it's not clear why there are two areas, but Arkanoid 2 uses these */
		/* for the end of game animation */
		upperbits |= tnzs_scrollram[0x182] + tnzs_scrollram[0x183] * 256;
	
		for (column = 0;column < tot;column++)
		{
			scrollx = tnzs_scrollram[column*16+4] - ((upperbits & 0x01) * 256);
			if (tnzs_screenflip != 0)
				scrolly = tnzs_scrollram[column*16] + 1 - 256;
			else
				scrolly = -tnzs_scrollram[column*16] + 1;
	
			for (y=0;y<16;y++)
			{
				for (x=0;x<2;x++)
				{
					int code,color,flipx,flipy,sx,sy;
					int i = 32*(column^8) + 2*y + x;
	
	
					code = m[i] + ((m[i + 0x1000] & 0x1f) << 8);
					color = (m[i + 0x1200] & 0xf8) >> 3; /* colours at d600-d7ff */
					sx = x*16;
					sy = y*16;
					flipx = m[i + 0x1000] & 0x80;
					flipy = m[i + 0x1000] & 0x40;
					if (tnzs_screenflip != 0)
					{
						sy = 240 - sy;
						flipx = !flipx;
						flipy = !flipy;
					}
	
					drawgfx(bitmap,Machine.gfx[0],
							code,
							color,
							flipx,flipy,
							sx + scrollx,(sy + scrolly) & 0xff,
							0,TRANSPARENCY_PEN,0);
				}
			}
	
	
			upperbits >>= 1;
		}
	}
	
	void tnzs_vh_draw_foreground(struct mame_bitmap *bitmap,
								 unsigned char *char_pointer,
								 unsigned char *x_pointer,
								 unsigned char *y_pointer,
								 unsigned char *ctrl_pointer,
								 unsigned char *color_pointer)
	{
		int i;
	
	
		/* Draw all 512 sprites */
		for (i=0x1ff;i >= 0;i--)
		{
			int code,color,sx,sy,flipx,flipy;
	
			code = char_pointer[i] + ((ctrl_pointer[i] & 0x1f) << 8);
			color = (color_pointer[i] & 0xf8) >> 3;
			sx = x_pointer[i] - ((color_pointer[i] & 1) << 8);
			sy = 240 - y_pointer[i];
			flipx = ctrl_pointer[i] & 0x80;
			flipy = ctrl_pointer[i] & 0x40;
			if (tnzs_screenflip != 0)
			{
				sy = 240 - sy;
				flipx = !flipx;
				flipy = !flipy;
				/* hack to hide Chuka Taisens grey line, top left corner */
				if ((sy == 0) && (code == 0)) sy += 240;
			}
	
			drawgfx(bitmap,Machine.gfx[0],
					code,
					color,
					flipx,flipy,
					sx,sy+2,
					Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_tnzs  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		/* If the byte at f300 has bit 6 set, flip the screen
		   (I'm not 100% sure about this) */
		tnzs_screenflip = (tnzs_scrollram[0x100] & 0x40) >> 6;
	
	
		/* Blank the background */
		fillbitmap(bitmap, Machine.pens[0], Machine.visible_area);
	
		/* Redraw the background tiles (c400-c5ff) */
		tnzs_vh_draw_background(bitmap, tnzs_objram + 0x400);
	
		/* Draw the sprites on top */
		tnzs_vh_draw_foreground(bitmap,
								tnzs_objram + 0x0000, /*  chars : c000 */
								tnzs_objram + 0x0200, /*	  x : c200 */
								tnzs_vdcram + 0x0000, /*	  y : f000 */
								tnzs_objram + 0x1000, /*   ctrl : d000 */
								tnzs_objram + 0x1200); /* color : d200 */
	} };
}
