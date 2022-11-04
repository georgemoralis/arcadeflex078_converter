/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.03
 */ 
package arcadeflex.v078.vidhrdw;

public class sbasketb
{
	
	UINT8 *sbasketb_scroll;
	UINT8 *sbasketb_palettebank;
	UINT8 *sbasketb_spriteram_select;
	
	static struct tilemap *bg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Super Basketball has three 256x4 palette PROMs (one per gun) and two 256x4
	  lookup table PROMs (one for characters, one for sprites).
	  I don't know for sure how the palette PROMs are connected to the RGB
	  output, but it's probably the usual:
	
	  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
	        -- 470 ohm resistor  -- RED/GREEN/BLUE
	        -- 1  kohm resistor  -- RED/GREEN/BLUE
	  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_sbasketb  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom)
	{
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
	
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine.drv.total_colors)>> 3) & 0x01;
			g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(2*Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine.drv.total_colors)>> 3) & 0x01;
			b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
		color_prom += 2*Machine.drv.total_colors;
		/* color_prom now points to the beginning of the character lookup table */
	
	
		/* characters use colors 240-255 */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			COLOR(0,i) = (*(color_prom++) & 0x0f) + 240;
	
		/* sprites use colors 0-256 (?) in 16 banks */
		for (i = 0;i < TOTAL_COLORS(1)/16;i++)
		{
			int j;
	
	
			for (j = 0;j < 16;j++)
				COLOR(1,i + j * TOTAL_COLORS(1)/16) = (color_prom.read()& 0x0f) + 16 * j;
	
			color_prom++;
		}
	} };
	
	public static WriteHandlerPtr sbasketb_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr sbasketb_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr sbasketb_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flip_screen() != data)
		{
			flip_screen_set(data);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static WriteHandlerPtr sbasketb_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int col;
	
		for (col = 6; col < 32; col++)
		{
			tilemap_set_scrolly(bg_tilemap, col, data);
		}
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int code = videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x20) << 3);
		int color = colorram.read(tile_index)& 0x0f;
		int flags = ((colorram.read(tile_index)& 0x40) ? TILE_FLIPX : 0) | ((colorram.read(tile_index)& 0x80) ? TILE_FLIPY : 0);
	
		SET_TILE_INFO(0, code, color, flags)
	}
	
	public static VideoStartHandlerPtr video_start_sbasketb  = new VideoStartHandlerPtr() { public int handler()
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if (bg_tilemap == 0)
			return 1;
	
		tilemap_set_scroll_cols(bg_tilemap, 32);
	
		return 0;
	} };
	
	static void sbasketb_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs = (*sbasketb_spriteram_select & 0x01) * 0x100;
		int i;
	
		for (i = 0; i < 64; i++, offs += 4)
		{
			int sx = spriteram.read(offs + 2);
			int sy = spriteram.read(offs + 3);
	
			if (sx || sy)
			{
				int code  =  spriteram.read(offs + 0)| ((spriteram.read(offs + 1)& 0x20) << 3);
				int color = (spriteram.read(offs + 1)& 0x0f) + 16 * *sbasketb_palettebank;
				int flipx =  spriteram.read(offs + 1)& 0x40;
				int flipy =  spriteram.read(offs + 1)& 0x80;
	
				if (flip_screen != 0)
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
				drawgfx(bitmap,Machine.gfx[1],
					code, color,
					flipx, flipy,
					sx, sy,
					Machine.visible_area,
					TRANSPARENCY_PEN, 0);
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_sbasketb  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		sbasketb_draw_sprites(bitmap);
	} };
}
