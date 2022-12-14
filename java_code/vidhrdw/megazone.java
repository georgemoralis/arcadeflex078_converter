/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class megazone
{
	
	unsigned char *megazone_scrollx;
	unsigned char *megazone_scrolly;
	static int flipscreen;
	
	unsigned char *megazone_videoram2;
	unsigned char *megazone_colorram2;
	size_t megazone_videoram2_size;
	
	/***************************************************************************
	Based on driver from MAME 0.55
	Changes by Martin M. (pfloyd@gmx.net) 14.10.2001:
	
	 - Added support for screen flip in cocktail mode (tricky!) */
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Megazone has one 32x8 palette PROM and two 256x4 lookup table PROMs
	  (one for characters, one for sprites).
	  The palette PROM is connected to the RGB output this way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_megazone  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
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
	
		/* color_prom now points to the beginning of the lookup table */
	
		/* sprites */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			COLOR(1,i) = *(color_prom++) & 0x0f;
	
		/* characters */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			COLOR(0,i) = (*(color_prom++) & 0x0f) + 0x10;
	} };
	
	public static WriteHandlerPtr megazone_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	public static VideoStartHandlerPtr video_start_megazone  = new VideoStartHandlerPtr() { public int handler(){
		dirtybuffer = 0;
		tmpbitmap = 0;
	
		if ((dirtybuffer = auto_malloc(videoram_size[0])) == 0)
			return 1;
		memset(dirtybuffer,1,videoram_size[0]);
	
		if ((tmpbitmap = auto_bitmap_alloc(256,256)) == 0)
			return 1;
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VideoUpdateHandlerPtr video_update_megazone  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs;
		int x,y;
	
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			if (dirtybuffer[offs])
			{
				int sx,sy,flipx,flipy;
	
				dirtybuffer[offs] = 0;
	
				sx = offs % 32;
				sy = offs / 32;
				flipx = colorram.read(offs)& (1<<6);
				flipy = colorram.read(offs)& (1<<5);
				if (flipscreen)
				{
					sx = 31 - sx;
					sy = 31 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						((int)videoram.read(offs)) + ((colorram.read(offs)& (1<<7) ? 256 : 0) ),
						(colorram.read(offs)& 0x0f) + 0x10,
						flipx,flipy,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
			}
		}
	
		/* copy the temporary bitmap to the screen */
		{
			int scrollx;
			int scrolly;
	
			if (flipscreen)
			{
				scrollx = *megazone_scrolly;
				scrolly = *megazone_scrollx;
			}
			else
			{
				scrollx = -*megazone_scrolly+4*8; // leave space for credit&score overlay
				scrolly = -*megazone_scrollx;
			}
	
	
			copyscrollbitmap(bitmap,tmpbitmap,1,&scrollx,1,&scrolly,Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	
	
		/* Draw the sprites. */
		{
			for (offs = spriteram_size[0]-4; offs >= 0;offs -= 4)
			{
				int sx,sy,flipx,flipy;
	
	
				sx = spriteram.read(offs + 3);
				if (flipscreen) sx-=11; else sx+=4*8;   	  // Sprite y-position correction depending on screen flip
				sy = 255-((spriteram.read(offs + 1)+16)&0xff);
				if (flipscreen) sy+=2; 			  	  // Sprite x-position correction depending on screen flip
	
				flipx = ~spriteram.read(offs+0)& 0x40;
				flipy = spriteram.read(offs+0)& 0x80;
	
				drawgfx(bitmap,Machine.gfx[1],
						spriteram.read(offs + 2),
						spriteram.read(offs + 0)& 0x0f,
						flipx,flipy,
						sx,sy,
						Machine.visible_area,TRANSPARENCY_COLOR,0);
			}
		}
	
		for (y = 0; y < 32;y++)
		{
			offs = y*32;
			for (x = 0; x < 6; x++)
			{
				int sx,sy,flipx,flipy;
	
				sx = x;
				sy = y;
	
				flipx = megazone_colorram2[offs] & (1<<6);
				flipy = megazone_colorram2[offs] & (1<<5);
	
				if (flipscreen)
				{
					sx = 35 - sx;
					sy = 31 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
	
	
	
				drawgfx(bitmap,Machine.gfx[0],
						((int)megazone_videoram2[offs]) + ((megazone_colorram2[offs] & (1<<7) ? 256 : 0) ),
						(megazone_colorram2[offs] & 0x0f) + 0x10,
						flipx,flipy,
						8*sx,8*sy,
						0,TRANSPARENCY_NONE,0);
				offs++;
			}
		}
	} };
}
