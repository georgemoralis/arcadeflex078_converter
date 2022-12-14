/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class gyruss
{
	
	
	static int flipscreen;
	
	/*
	sprites are multiplexed, so we have to buffer the spriteram
	scanline by scanline.
	*/
	static unsigned char *sprite_mux_buffer;
	static int scanline;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Gyruss has one 32x8 palette PROM and two 256x4 lookup table PROMs
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
	public static PaletteInitHandlerPtr palette_init_gyruss  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
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
	
		/* color_prom now points to the beginning of the sprite lookup table */
	
		/* sprites */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			COLOR(1,i) = *(color_prom++) & 0x0f;
	
		/* characters */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			COLOR(0,i) = (*(color_prom++) & 0x0f) + 0x10;
	} };
	
	
	
	public static VideoStartHandlerPtr video_start_gyruss  = new VideoStartHandlerPtr() { public int handler(){
		sprite_mux_buffer = auto_malloc(256 * spriteram_size[0]);
	
		if (!sprite_mux_buffer)
			return 1;
	
		return video_start_generic.handler();
	} };
	
	
	
	public static WriteHandlerPtr gyruss_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (flipscreen != (data & 1))
		{
			flipscreen = data & 1;
			memset(dirtybuffer,1,videoram_size[0]);
		}
	} };
	
	
	
	/* Return the current video scan line */
	public static ReadHandlerPtr gyruss_scanline_r  = new ReadHandlerPtr() { public int handler(int offset){
		return scanline;
	} };
	
	
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap)
	{
		struct rectangle clip = Machine->visible_area;
		int offs;
		int line;
	
	
		for (line = 0;line < 256;line++)
		{
			if (line >= Machine->visible_area.min_y && line <= Machine->visible_area.max_y)
			{
				unsigned char *sr;
	
				sr = sprite_mux_buffer + line * spriteram_size;
				clip.min_y = clip.max_y = line;
	
				for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
				{
					int sx,sy;
	
					sx = sr[offs];
					sy = 241 - sr[offs + 3];
					if (sy > line-16 && sy <= line)
					{
						drawgfx(bitmap,Machine->gfx[1 + (sr[offs + 1] & 1)],
								sr[offs + 1]/2 + 4*(sr[offs + 2] & 0x20),
								sr[offs + 2] & 0x0f,
								!(sr[offs + 2] & 0x40),sr[offs + 2] & 0x80,
								sx,sy,
								&clip,TRANSPARENCY_PEN,0);
					}
				}
			}
		}
	}
	
	
	public static VideoUpdateHandlerPtr video_update_gyruss  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs;
	
	
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
				flipx = colorram.read(offs)& 0x40;
				flipy = colorram.read(offs)& 0x80;
				if (flipscreen)
				{
					sx = 31 - sx;
					sy = 31 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
				drawgfx(tmpbitmap,Machine.gfx[0],
						videoram.read(offs)+ 8 * (colorram.read(offs)& 0x20),
						colorram.read(offs)& 0x0f,
						flipx,flipy,
						8*sx,8*sy,
						Machine.visible_area,TRANSPARENCY_NONE,0);
			}
		}
	
	
		/* copy the character mapped graphics */
		copybitmap(bitmap,tmpbitmap,0,0,0,0,Machine.visible_area,TRANSPARENCY_NONE,0);
	
	
		draw_sprites(bitmap);
	
	
		/* redraw the characters which have priority over sprites */
		for (offs = videoram_size[0] - 1;offs >= 0;offs--)
		{
			int sx,sy,flipx,flipy;
	
	
			sx = offs % 32;
			sy = offs / 32;
			flipx = colorram.read(offs)& 0x40;
			flipy = colorram.read(offs)& 0x80;
			if (flipscreen)
			{
				sx = 31 - sx;
				sy = 31 - sy;
				flipx = NOT(flipx);
				flipy = NOT(flipy);
			}
	
			if ((colorram.read(offs)& 0x10) != 0)
				drawgfx(bitmap,Machine.gfx[0],
						videoram.read(offs)+ 8 * (colorram.read(offs)& 0x20),
						colorram.read(offs)& 0x0f,
						flipx,flipy,
						8*sx,8*sy,
						Machine.visible_area,TRANSPARENCY_NONE,0);
		}
	} };
	
	
	public static InterruptHandlerPtr gyruss_6809_interrupt = new InterruptHandlerPtr() {public void handler(){
		scanline = 255 - cpu_getiloops();
	
		memcpy(sprite_mux_buffer + scanline * spriteram_size,spriteram,spriteram_size);
	
		if (scanline == 255)
			irq0_line_hold();
	} };
}
