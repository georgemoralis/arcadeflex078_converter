/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.02
 */ 
package arcadeflex.v078.vidhrdw;

public class zodiack
{
	
	UINT8 *zodiack_videoram2;
	UINT8 *zodiack_attributesram;
	UINT8 *zodiack_bulletsram;
	size_t zodiack_bulletsram_size;
	
	
	static struct tilemap *bg_tilemap, *fg_tilemap;
	
	public static WriteHandlerPtr zodiack_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr zodiack_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (zodiack_videoram2[offset] != data)
		{
			zodiack_videoram2[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr zodiack_attributes_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if ((offset & 1) && zodiack_attributesram[offset] != data)
		{
			int i;
	
			for (i = offset / 2;i < videoram_size; i += 32)
			{
				tilemap_mark_tile_dirty(bg_tilemap, i);
				tilemap_mark_tile_dirty(fg_tilemap, i);
			}
		}
	
		zodiack_attributesram[offset] = data;
	} };
	
	public static WriteHandlerPtr zodiack_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (flip_screen != (~data & 0x01))
		{
			flip_screen_set(~data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static PaletteInitHandlerPtr palette_init_zodiack  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom)
	{
		int i;
	
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		/* first, the character/sprite palette */
		for (i = 0;i < Machine.drv.total_colors-1; i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
			/* red component */
	
			bit0 = (*color_prom >> 0) & 0x01;
			bit1 = (*color_prom >> 1) & 0x01;
			bit2 = (*color_prom >> 2) & 0x01;
	
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			/* green component */
	
			bit0 = (*color_prom >> 3) & 0x01;
			bit1 = (*color_prom >> 4) & 0x01;
			bit2 = (*color_prom >> 5) & 0x01;
	
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			/* blue component */
	
			bit0 = 0;
			bit1 = (*color_prom >> 6) & 0x01;
			bit2 = (*color_prom >> 7) & 0x01;
	
			b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			palette_set_color(i,r,g,b);
	
			color_prom++;
		}
	
		/* white for bullets */
	
		palette_set_color(Machine.drv.total_colors-1,0xff,0xff,0xff);
	
		for (i = 0;i < TOTAL_COLORS(0);i+=2)
		{
			COLOR(0,i  ) = (32 + (i / 2));
			COLOR(0,i+1) = (40 + (i / 2));
		}
	
		for (i = 0;i < TOTAL_COLORS(3);i++)
		{
			if ((i & 3) == 0)  COLOR(3,i) = 0;
		}
	
		/* bullet */
		COLOR(2, 0) = 0;
		COLOR(2, 1) = 48;
	} };
	
	static void get_bg_tile_info(int tile_index)
	{
		int code = zodiack_videoram2[tile_index];
		int color = (zodiack_attributesram[2 * (tile_index % 32) + 1] >> 4) & 0x07;
	
		SET_TILE_INFO(0, code, color, 0)
	}
	
	static void get_fg_tile_info(int tile_index)
	{
		int code = videoram.read(tile_index);
		int color = zodiack_attributesram[2 * (tile_index % 32) + 1] & 0x07;
	
		SET_TILE_INFO(3, code, color, 0)
	}
	
	VIDEO_START( zodiack )
	{
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows,
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if (bg_tilemap == 0)
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows,
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if (fg_tilemap == 0)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
		tilemap_set_scroll_cols(fg_tilemap, 32);
	
		flip_screen = 0;
	
		return 0;
	}
	
	static void zodiack_draw_bullets( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = 0; offs < zodiack_bulletsram_size; offs += 4)
		{
			int x, y;
	
			x = zodiack_bulletsram[offs + 3] + Machine.drv.gfxdecodeinfo[2].gfxlayout.width;
			y = 255 - zodiack_bulletsram[offs + 1];
	
			if (flip_screen && percuss_hardware)
			{
				y = 255 - y;
			}
	
			drawgfx(
				bitmap,
				Machine.gfx[2],
				0,	/* this is just a dot, generated by the hardware */
				0,
				0,0,
				x,y,
				Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	static void zodiack_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 4; offs >= 0; offs -= 4)
		{
			int flipx, flipy, sx, sy, spritecode;
	
			sx = 240 - spriteram.read(offs + 3);
			sy = 240 - spriteram.read(offs);
			flipx = !(spriteram.read(offs + 1)& 0x40);
			flipy = spriteram.read(offs + 1)& 0x80;
			spritecode = spriteram.read(offs + 1)& 0x3f;
	
			if (flip_screen && percuss_hardware)
			{
				sy = 240 - sy;
				flipy = !flipy;
			}
	
			drawgfx(bitmap, Machine.gfx[1],
				spritecode,
				spriteram.read(offs + 2)& 0x07,
				flipx, flipy,
				sx, sy,
				//flip_screen[0] ? &spritevisibleareaflipx : &spritevisiblearea,TRANSPARENCY_PEN,0);
				//&spritevisiblearea,TRANSPARENCY_PEN,0);
				Machine.visible_area, TRANSPARENCY_PEN, 0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_zodiack  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		int i;
	
		for (i = 0; i < 32; i++)
		{
			tilemap_set_scrolly(fg_tilemap, i, zodiack_attributesram[i * 2]);
		}
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		tilemap_draw(bitmap, Machine.visible_area, fg_tilemap, 0, 0);
		zodiack_draw_bullets(bitmap);
		zodiack_draw_sprites(bitmap);
	} };
}
