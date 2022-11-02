/***************************************************************************

  vidhrdw/balsente.c

  Functions to emulate the video hardware of the machine.

****************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 
package arcadeflex.v078.vidhrdw;

public class balsente
{
	
	
	/*************************************
	 *
	 *	Statics
	 *
	 *************************************/
	
	static UINT8 *local_videoram;
	static UINT8 *scanline_dirty;
	static UINT8 *scanline_palette;
	static UINT8 *sprite_data;
	static UINT32 sprite_mask;
	
	static UINT8 last_scanline_palette;
	static UINT8 screen_refresh_counter;
	static UINT8 palettebank_vis;
	
	
	
	/*************************************
	 *
	 *	Video system start
	 *
	 *************************************/
	
	VIDEO_START( balsente )
	{
		/* reset the system */
		palettebank_vis = 0;
		
		/* allocate a bitmap */
		tmpbitmap = auto_bitmap_alloc(Machine.drv.screen_width, Machine.drv.screen_height);
		if (tmpbitmap == 0)
			return 1;
	
		/* allocate a local copy of video RAM */
		local_videoram = auto_malloc(256 * 256);
		if (local_videoram == 0)
			return 1;
	
		/* allocate a scanline dirty array */
		scanline_dirty = auto_malloc(256);
		if (scanline_dirty == 0)
			return 1;
	
		/* allocate a scanline palette array */
		scanline_palette = auto_malloc(256);
		if (scanline_palette == 0)
			return 1;
	
		/* mark everything dirty to start */
		memset(scanline_dirty, 1, 256);
	
		/* reset the scanline palette */
		memset(scanline_palette, 0, 256);
		last_scanline_palette = 0;
	
		/* determine sprite size */
		sprite_data = memory_region(REGION_GFX1);
		sprite_mask = memory_region_length(REGION_GFX1) - 1;
	
		return 0;
	}
	
	
	
	/*************************************
	 *
	 *	Video RAM write
	 *
	 *************************************/
	
	public static WriteHandlerPtr balsente_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		videoram.write(offset,data);
	
		/* expand the two pixel values into two bytes */
		local_videoram[offset * 2 + 0] = data >> 4;
		local_videoram[offset * 2 + 1] = data & 15;
	
		/* mark the scanline dirty */
		scanline_dirty[offset / 128] = 1;
	} };
	
	
	
	/*************************************
	 *
	 *	Palette banking
	 *
	 *************************************/
	
	static void update_palette(void)
	{
		int scanline = cpu_getscanline(), i;
		if (scanline > 255) scanline = 0;
	
		/* special case: the scanline is the same as last time, but a screen refresh has occurred */
		if (scanline == last_scanline_palette && screen_refresh_counter)
		{
			for (i = 0; i < 256; i++)
			{
				/* mark the scanline dirty if it was a different palette */
				if (scanline_palette[i] != palettebank_vis)
					scanline_dirty[i] = 1;
				scanline_palette[i] = palettebank_vis;
			}
		}
	
		/* fill in the scanlines up till now */
		else
		{
			for (i = last_scanline_palette; i != scanline; i = (i + 1) & 255)
			{
				/* mark the scanline dirty if it was a different palette */
				if (scanline_palette[i] != palettebank_vis)
					scanline_dirty[i] = 1;
				scanline_palette[i] = palettebank_vis;
			}
	
			/* remember where we left off */
			last_scanline_palette = scanline;
		}
	
		/* reset the screen refresh counter */
		screen_refresh_counter = 0;
	}
	
	
	public static WriteHandlerPtr balsente_palette_select_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* only update if changed */
		if (palettebank_vis != (data & 3))
		{
			/* update the scanline palette */
			update_palette();
			palettebank_vis = data & 3;
		}
	
		logerror("balsente_palette_select_w(%d) scanline=%d\n", data & 3, cpu_getscanline());
	} };
	
	
	
	/*************************************
	 *
	 *	Palette RAM write
	 *
	 *************************************/
	
	public static WriteHandlerPtr balsente_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int r, g, b;
	
		paletteram[offset] = data & 0x0f;
	
		r = paletteram[(offset & ~3) + 0];
		g = paletteram[(offset & ~3) + 1];
		b = paletteram[(offset & ~3) + 2];
		palette_set_color(offset / 4, (r << 4) | r, (g << 4) | g, (b << 4) | b);
	} };
	
	
	
	/*************************************
	 *
	 *	Sprite drawing
	 *
	 *************************************/
	
	static void draw_one_sprite(struct mame_bitmap *bitmap, const struct rectangle *cliprect, UINT8 *sprite)
	{
		struct rectangle finalclip = *cliprect;
		int flags = sprite[0];
		int image = sprite[1] | ((flags & 7) << 8);
		int ypos = sprite[2] + 17;
		int xpos = sprite[3];
		UINT8 *src;
		int x, y;
		
		if (finalclip.min_y < 16)
			finalclip.min_y = 16;
		if (finalclip.max_y > 240)
			finalclip.max_y = 240;
	
		/* get a pointer to the source image */
		src = &sprite_data[(64 * image) & sprite_mask];
		if ((flags & 0x80) != 0) src += 4 * 15;
	
		/* loop over y */
		for (y = 0; y < 16; y++, ypos = (ypos + 1) & 255)
		{
			if (ypos >= finalclip.min_y && ypos <= finalclip.max_y)
			{
				UINT32 *pens = Machine.pens[scanline_palette[y] * 256];
				UINT8 *old = &local_videoram[ypos * 256 + xpos];
				int currx = xpos;
	
				/* standard case */
				if (!(flags & 0x40))
				{
					/* loop over x */
					for (x = 0; x < 4; x++, old += 2)
					{
						int ipixel = *src++;
						int left = ipixel & 0xf0;
						int right = (ipixel << 4) & 0xf0;
	
						/* left pixel, combine with the background */
						if (left && currx >= finalclip.min_x && currx <= finalclip.max_x)
							plot_pixel(bitmap, currx, ypos, pens[left | old[0]]);
						currx++;
	
						/* right pixel, combine with the background */
						if (right && currx >= finalclip.min_x && currx <= finalclip.max_x)
							plot_pixel(bitmap, currx, ypos, pens[right | old[1]]);
						currx++;
					}
				}
	
				/* hflip case */
				else
				{
					src += 4;
	
					/* loop over x */
					for (x = 0; x < 4; x++, old += 2)
					{
						int ipixel = *--src;
						int left = (ipixel << 4) & 0xf0;
						int right = ipixel & 0xf0;
	
						/* left pixel, combine with the background */
						if (left && currx >= finalclip.min_x && currx <= finalclip.max_x)
							plot_pixel(bitmap, currx, ypos, pens[left | old[0]]);
						currx++;
	
						/* right pixel, combine with the background */
						if (right && currx >= finalclip.min_x && currx <= finalclip.max_x)
							plot_pixel(bitmap, currx, ypos, pens[right | old[1]]);
						currx++;
					}
					src += 4;
				}
			}
			else
				src += 4;
			if ((flags & 0x80) != 0) src -= 2 * 4;
		}
	}
	
	
	
	/*************************************
	 *
	 *	Main screen refresh
	 *
	 *************************************/
	
	public static VideoUpdateHandlerPtr video_update_balsente  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		int update_all = get_vh_global_attribute_changed();
		int y, i;
	
		/* update the remaining scanlines */
		screen_refresh_counter++;
		update_palette();
	
		/* make sure color 1024 is white for our crosshair */
		palette_set_color(1024, 0xff, 0xff, 0xff);
	
		/* draw any dirty scanlines from the VRAM directly */
		for (y = 0; y < 240; y++)
			if (scanline_dirty[y] || update_all)
			{
				pen_t *pens = Machine.pens[scanline_palette[y] * 256];
				draw_scanline8(tmpbitmap, 0, y, 256, &local_videoram[y * 256], pens, -1);
				scanline_dirty[y] = 0;
			}
		copybitmap(bitmap,tmpbitmap,0,0,0,0,cliprect,TRANSPARENCY_NONE,0);
	
		/* draw the sprite images */
		for (i = 0; i < 40; i++)
			draw_one_sprite(bitmap, cliprect, &spriteram.read((0xe0 + i * 4) & 0xff));
	
		/* draw a crosshair */
		if (balsente_shooter != 0)
		{
			int beamx = balsente_shooter_x;
			int beamy = balsente_shooter_y - 10;
	
			draw_crosshair(bitmap,beamx,beamy,cliprect);
		}
	} };
}
