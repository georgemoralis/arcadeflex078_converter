/***************************************************************************

	Battle Lane Vol. 5

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 
package arcadeflex.v078.vidhrdw;

public class battlane
{
	
	UINT8 *battlane_spriteram;
	UINT8 *battlane_tileram;
	
	static struct tilemap *bg_tilemap;
	
	static int battlane_video_ctrl;
	
	static struct mame_bitmap *screen_bitmap;
	
	/*
	    Video control register
	
	        0x80    = low bit of blue component (taken when writing to palette)
	        0x0e    = Bitmap plane (bank?) select  (0-7)
	        0x01    = Scroll MSB
	*/
	
	public static WriteHandlerPtr battlane_palette_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int r, g, b;
		int bit0, bit1, bit2;
	
		/* red component */
	
		bit0 = (~data >> 0) & 0x01;
		bit1 = (~data >> 1) & 0x01;
		bit2 = (~data >> 2) & 0x01;
		r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
		/* green component */
	
		bit0 = (~data >> 3) & 0x01;
		bit1 = (~data >> 4) & 0x01;
		bit2 = (~data >> 5) & 0x01;
		g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
		/* blue component */
	
		bit0 = (~battlane_video_ctrl >> 7) & 0x01;
		bit1 = (~data >> 6) & 0x01;
		bit2 = (~data >> 7) & 0x01;
		b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
		palette_set_color(offset, r, g, b);
	} };
	
	public static WriteHandlerPtr battlane_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrollx(bg_tilemap, 0, ((battlane_video_ctrl & 0x01) << 8) + data);
	} };
	
	public static WriteHandlerPtr battlane_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tilemap_set_scrolly(bg_tilemap, 0, ((battlane_cpu_control & 0x01) << 8) + data);
	} };
	
	public static WriteHandlerPtr battlane_tileram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (battlane_tileram[offset] != data)
		{
		    battlane_tileram[offset] = data;
			//tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr battlane_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	    battlane_spriteram[offset] = data;
	} };
	
	public static WriteHandlerPtr battlane_bitmap_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i, orval;
	
	    orval = (~battlane_video_ctrl >> 1) & 0x07;
	
		if (orval == 0)
			orval = 7;
	
		for (i = 0; i < 8; i++)
		{
			if (data & 1 << i)
			{
				((UINT8 *)screen_bitmap.line[offset % 0x100])[(offset / 0x100) * 8 + i] |= orval;
			}
			else
			{
				((UINT8 *)screen_bitmap.line[offset % 0x100])[(offset / 0x100) * 8 + i] &= ~orval;
			}
		}
	} };
	
	public static WriteHandlerPtr battlane_video_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		battlane_video_ctrl = data;
	} };
	
	static void get_tile_info_bg(int tile_index)
	{
		int code = battlane_tileram[tile_index];
		int attr = battlane_tileram[tile_index + 0x400];
		int gfxn = (attr & 0x01) + 1;
		int color = (attr >> 1) & 0x03;
	
		SET_TILE_INFO(gfxn, code, color, 0)
	}
	
	static UINT32 battlane_tilemap_scan_rows_2x2( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows )
	{
		/* 
				Tilemap Memory Organization
	
			 0              15 16            31
			+-----------------+----------------+
			|0				15|256			   |0
			|				  |				   |
			|     screen 0    |    screen 1    |
			|                 |                |
			|240           255|             511|15
			+-----------------+----------------+
			|512              |768             |16
			|                 |                |
			|     screen 2    |    screen 3    |
			|                 |                |
			|              767|            1023|31
			+-----------------+-----------------
	
		*/
		
		return (row & 0xf) * 16 + (col & 0xf) + (row & 0x10) * 32 + (col & 0x10) * 16;
	}
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	VIDEO_START( battlane )
	{
		bg_tilemap = tilemap_create(get_tile_info_bg, battlane_tilemap_scan_rows_2x2,
			TILEMAP_OPAQUE, 16, 16, 32, 32);
		
		if (bg_tilemap == 0)
			return 1;
	
		screen_bitmap = auto_bitmap_alloc(32 * 8, 32 * 8);
	
		if (screen_bitmap == 0)
			return 1;
	
		return 0;
	}
	
	static void battlane_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs, attr, code, color, sx, sy, flipx, flipy, dy;
	
		for (offs = 0; offs < 0x100; offs += 4)
		{
			/*
				0x80 = Bank 2
				0x40 =
				0x20 = Bank 1
				0x10 = Y Double
				0x08 = Color
				0x04 = X Flip
				0x02 = Y Flip
				0x01 = Sprite Enable
			*/
	
			attr = battlane_spriteram[offs + 1];
			code = battlane_spriteram[offs + 3];
	
			code += 256 * ((attr >> 6) & 0x02);
			code += 256 * ((attr >> 5) & 0x01);
	
			if ((attr & 0x01) != 0)
			{
				color = (attr >> 3) & 0x01;
	
				sx = battlane_spriteram[offs + 2];
				sy = battlane_spriteram[offs];
	
				flipx = attr & 0x04;
				flipy = attr & 0x02;
	
				if (flip_screen == 0)
	            {
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = !flipx;
					flipy = !flipy;
				}
	
				drawgfx(bitmap,Machine.gfx[0],
					code,
					color,
					flipx, flipy,
					sx, sy,
					Machine.visible_area,
					TRANSPARENCY_PEN, 0);
	
				if ((attr & 0x10) != 0)  /* Double Y direction */
				{
					dy = flipy ? 16 : -16;
	
					drawgfx(bitmap,Machine.gfx[0],
						code + 1,
						color,
						flipx, flipy,
						sx, sy + dy,
						Machine.visible_area,
						TRANSPARENCY_PEN, 0);
				}
			}
		}
	}
	
	static void battlane_draw_fg_bitmap( struct mame_bitmap *bitmap )
	{
		int x, y, data;
	
		for (y = 0; y < 32 * 8; y++)
		{
			for (x = 0; x < 32 * 8; x++)
			{
				data = ((UINT8 *)screen_bitmap.line[y])[x];
	
				if (data != 0)
				{
					if (flip_screen != 0)
					{
						plot_pixel(bitmap, 255 - x, 255 - y, Machine.pens[data]);
					}
					else
					{
						plot_pixel(bitmap, x, y, Machine.pens[data]);
					}
				}
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_battlane  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		tilemap_mark_all_tiles_dirty(bg_tilemap); // HACK
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		battlane_draw_sprites(bitmap);
		battlane_draw_fg_bitmap(bitmap);
	} };
}
