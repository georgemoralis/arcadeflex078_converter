/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class dkong
{
	
	
	static int gfx_bank, palette_bank;
	static int grid_on;
	static const UINT8 *color_codes;
	
	static struct tilemap *bg_tilemap;
	
	public static WriteHandlerPtr dkong_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Donkey Kong has two 256x4 palette PROMs and one 256x4 PROM which contains
	  the color codes to use for characters on a per row/column basis (groups of
	  of 4 characters in the same column - actually row, since the display is
	  rotated)
	  The palette PROMs are connected to the RGB output this way:
	
	  bit 3 -- 220 ohm resistor -- inverter  -- RED
	        -- 470 ohm resistor -- inverter  -- RED
	        -- 1  kohm resistor -- inverter  -- RED
	  bit 0 -- 220 ohm resistor -- inverter  -- GREEN
	  bit 3 -- 470 ohm resistor -- inverter  -- GREEN
	        -- 1  kohm resistor -- inverter  -- GREEN
	        -- 220 ohm resistor -- inverter  -- BLUE
	  bit 0 -- 470 ohm resistor -- inverter  -- BLUE
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_dkong  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom)
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < 256;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
	
			/* red component */
			bit0 = (color_prom[256] >> 1) & 1;
			bit1 = (color_prom[256] >> 2) & 1;
			bit2 = (color_prom[256] >> 3) & 1;
			r = 255 - (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
			/* green component */
			bit0 = (color_prom[0] >> 2) & 1;
			bit1 = (color_prom[0] >> 3) & 1;
			bit2 = (color_prom[256] >> 0) & 1;
			g = 255 - (0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2);
			/* blue component */
			bit0 = (color_prom[0] >> 0) & 1;
			bit1 = (color_prom[0] >> 1) & 1;
			b = 255 - (0x55 * bit0 + 0xaa * bit1);
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
		color_prom += 256;
		/* color_prom now points to the beginning of the character color codes */
		color_codes = color_prom;	/* we'll need it later */
	} };
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Donkey Kong 3 has two 512x8 palette PROMs and one 256x4 PROM which contains
	  the color codes to use for characters on a per row/column basis (groups of
	  of 4 characters in the same column - actually row, since the display is
	  rotated)
	  Interstingly, bytes 0-255 of the palette PROMs contain an inverted palette,
	  as other Nintendo games like Donkey Kong, while bytes 256-511 contain a non
	  inverted palette. This was probably done to allow connection to both the
	  special Nintendo and a standard monitor.
	  I don't know the exact values of the resistors between the PROMs and the
	  RGB output, but they are probably the usual:
	
	  bit 7 -- 220 ohm resistor -- inverter  -- RED
	        -- 470 ohm resistor -- inverter  -- RED
	        -- 1  kohm resistor -- inverter  -- RED
	        -- 2.2kohm resistor -- inverter  -- RED
	        -- 220 ohm resistor -- inverter  -- GREEN
	        -- 470 ohm resistor -- inverter  -- GREEN
	        -- 1  kohm resistor -- inverter  -- GREEN
	  bit 0 -- 2.2kohm resistor -- inverter  -- GREEN
	
	  bit 3 -- 220 ohm resistor -- inverter  -- BLUE
	        -- 470 ohm resistor -- inverter  -- BLUE
	        -- 1  kohm resistor -- inverter  -- BLUE
	  bit 0 -- 2.2kohm resistor -- inverter  -- BLUE
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_dkong3  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom)
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < 256;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
	
			/* red component */
			bit0 = (color_prom[0] >> 4) & 0x01;
			bit1 = (color_prom[0] >> 5) & 0x01;
			bit2 = (color_prom[0] >> 6) & 0x01;
			bit3 = (color_prom[0] >> 7) & 0x01;
			r = 255 - (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			/* green component */
			bit0 = (color_prom[0] >> 0) & 0x01;
			bit1 = (color_prom[0] >> 1) & 0x01;
			bit2 = (color_prom[0] >> 2) & 0x01;
			bit3 = (color_prom[0] >> 3) & 0x01;
			g = 255 - (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
			/* blue component */
			bit0 = (color_prom[256] >> 0) & 0x01;
			bit1 = (color_prom[256] >> 1) & 0x01;
			bit2 = (color_prom[256] >> 2) & 0x01;
			bit3 = (color_prom[256] >> 3) & 0x01;
			b = 255 - (0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3);
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
		color_prom += 256;
		/* color_prom now points to the beginning of the character color codes */
		color_codes = color_prom;	/* we'll need it later */
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int code = videoram.read(tile_index)+ 256 * gfx_bank;
		int color = (color_codes[tile_index % 32 + 32 * (tile_index / 32 / 4)] & 0x0f) + 0x10 * palette_bank;
	
		SET_TILE_INFO(0, code, color, 0)
	}
	
	public static VideoUpdateHandlerPtr video_update_dkong  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		gfx_bank = 0;
		palette_bank = 0;
	
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		return 0;
	} };
	
	
	public static WriteHandlerPtr dkongjr_gfxbank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (gfx_bank != (data & 0x01))
		{
			gfx_bank = data & 0x01;
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	} };
	
	public static WriteHandlerPtr dkong3_gfxbank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (gfx_bank != (~data & 0x01))
		{
			gfx_bank = ~data & 0x01;
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	} };
	
	public static WriteHandlerPtr dkong_palettebank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int newbank;
	
		newbank = palette_bank;
	
		if ((data & 1) != 0)
			newbank |= 1 << offset;
		else
			newbank &= ~(1 << offset);
	
		if (palette_bank != newbank)
		{
			palette_bank = newbank;
			tilemap_mark_all_tiles_dirty(bg_tilemap);
		}
	} };
	
	public static WriteHandlerPtr radarscp_grid_enable_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		grid_on = data & 0x01;
	} };
	
	public static WriteHandlerPtr radarscp_grid_color_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int r,g,b;
	
		r = ((~data >> 0) & 0x01) * 0xff;
		g = ((~data >> 1) & 0x01) * 0xff;
		b = ((~data >> 2) & 0x01) * 0xff;
	//	palette_set_color(257,r,g,b);
		palette_set_color(257,0x00,0x00,0xff);
	} };
	
	public static WriteHandlerPtr dkong_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_set(~data & 0x01);
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap, unsigned int mask_bank, unsigned int shift_bits)
	{
		int offs;
	
		/* Draw the sprites. */
		for (offs = 0;offs < spriteram_size;offs += 4)
		{
			if (spriteram.read(offs))
			{
				/* spriteram.read(offs + 2)& 0x40 is used by Donkey Kong 3 only */
				/* spriteram.read(offs + 2)& 0x30 don't seem to be used (they are */
				/* probably not part of the color code, since Mario Bros, which */
				/* has similar hardware, uses a memory mapped port to change */
				/* palette bank, so it's limited to 16 color codes) */
	
				int x,y;
	
				x = spriteram.read(offs + 3)- 8;
				y = 240 - spriteram.read(offs)+ 7;
	
				if (flip_screen != 0)
				{
					x = 240 - x;
					y = 240 - y;
	
					drawgfx(bitmap,Machine.gfx[1],
							(spriteram.read(offs + 1)& 0x7f) + ((spriteram.read(offs + 2)& mask_bank) << shift_bits),
							(spriteram.read(offs + 2)& 0x0f) + 16 * palette_bank,
							!(spriteram.read(offs + 2)& 0x80),!(spriteram.read(offs + 1)& 0x80),
							x,y,
							Machine.visible_area,TRANSPARENCY_PEN,0);
	
					/* draw with wrap around - this fixes the 'beheading' bug */
					drawgfx(bitmap,Machine.gfx[1],
							(spriteram.read(offs + 1)& 0x7f) + ((spriteram.read(offs + 2)& mask_bank) << shift_bits),
							(spriteram.read(offs + 2)& 0x0f) + 16 * palette_bank,
							(spriteram.read(offs + 2)& 0x80),(spriteram.read(offs + 1)& 0x80),
							x-256,y,
							Machine.visible_area,TRANSPARENCY_PEN,0);
				}
				else
				{
					drawgfx(bitmap,Machine.gfx[1],
							(spriteram.read(offs + 1)& 0x7f) + ((spriteram.read(offs + 2)& mask_bank) << shift_bits),
							(spriteram.read(offs + 2)& 0x0f) + 16 * palette_bank,
							(spriteram.read(offs + 2)& 0x80),(spriteram.read(offs + 1)& 0x80),
							x,y,
							Machine.visible_area,TRANSPARENCY_PEN,0);
	
					/* draw with wrap around - this fixes the 'beheading' bug */
					drawgfx(bitmap,Machine.gfx[1],
							(spriteram.read(offs + 1)& 0x7f) + ((spriteram.read(offs + 2)& mask_bank) << shift_bits),
							(spriteram.read(offs + 2)& 0x0f) + 16 * palette_bank,
							(spriteram.read(offs + 2)& 0x80),(spriteram.read(offs + 1)& 0x80),
							x+256,y,
							Machine.visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	}
	
	static void draw_grid(struct mame_bitmap *bitmap)
	{
		const UINT8 *table = memory_region(REGION_GFX3);
		int x,y,counter;
	
		counter = flip_screen ? 0x000 : 0x400;
	
		x = Machine.visible_area.min_x;
		y = Machine.visible_area.min_y;
		while (y <= Machine.visible_area.max_y)
		{
			x = 4 * (table[counter] & 0x7f);
			if (x >= Machine.visible_area.min_x &&
					x <= Machine.visible_area.max_x)
			{
				if (table[counter] & 0x80)	/* star */
				{
					if (rand() & 1)	/* noise coming from sound board */
						plot_pixel(bitmap,x,y,Machine.pens[256]);
				}
				else if (grid_on != 0)			/* radar */
					plot_pixel(bitmap,x,y,Machine.pens[257]);
			}
	
			counter++;
	
			if (x >= 4 * (table[counter] & 0x7f))
				y++;
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_radarscp  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		palette_set_color(256,0xff,0x00,0x00);	/* stars */
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		draw_grid(bitmap);
		draw_sprites(bitmap, 0x40, 1);
	} };
	
	public static VideoUpdateHandlerPtr video_update_dkong  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		draw_sprites(bitmap, 0x40, 1);
	} };
	
	public static VideoUpdateHandlerPtr video_update_pestplce  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		int offs;
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
	
		/* Draw the sprites. */
		for (offs = 0;offs < spriteram_size;offs += 4)
		{
			if (spriteram.read(offs))
			{
				drawgfx(bitmap,Machine.gfx[1],
						spriteram.read(offs + 2),
						(spriteram.read(offs + 1)& 0x0f) + 16 * palette_bank,
						spriteram.read(offs + 1)& 0x80,spriteram.read(offs + 1)& 0x40,
						spriteram.read(offs + 3)- 8,240 - spriteram.read(offs)+ 8,
						Machine.visible_area,TRANSPARENCY_PEN,0);
			}
		}
	} };
	
	public static VideoUpdateHandlerPtr video_update_spclforc  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
	
		/* it uses spriteram.read(offs + 2)& 0x10 for sprite bank */
		draw_sprites(bitmap, 0x10, 3);
	} };
}
