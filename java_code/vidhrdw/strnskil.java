/******************************************************************************

Strength & Skill (c) 1984 Sun Electronics

Video hardware driver by Uki

	19/Jun/2001 -

******************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class strnskil
{
	
	static UINT8 strnskil_scrl_ctrl;
	static UINT8 strnskil_xscroll[2];
	
	static struct tilemap *bg_tilemap;
	
	public static PaletteInitHandlerPtr palette_init_strnskil  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int r = color_prom.read(0)*0x11;
			int g = color_prom.read(Machine.drv.total_colors)*0x11;
			int b = color_prom.read(2*Machine.drv.total_colors)*0x11;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
		color_prom += 2*Machine.drv.total_colors;
	
		/* color_prom now points to the beginning of the lookup table */
	
		/* sprites lookup table */
		for (i=0; i<512; i++)
			*(colortable++) = *(color_prom++);
	
		/* bg lookup table */
		for (i=0; i<512; i++)
			*(colortable++) = *(color_prom++);
	
	} };
	
	public static WriteHandlerPtr strnskil_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset / 2);
		}
	} };
	
	public static WriteHandlerPtr strnskil_scroll_x_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		strnskil_xscroll[offset] = data;
	} };
	
	public static WriteHandlerPtr strnskil_scrl_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		strnskil_scrl_ctrl = data >> 5;
	
		if (flip_screen() != (data & 0x08))
		{
			flip_screen_set(data & 0x08);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int attr = videoram.read(tile_index * 2);
		int code = videoram.read((tile_index * 2) + 1)+ ((attr & 0x60) << 3);
		int color = (attr & 0x1f) | ((attr & 0x80) >> 2);
	
		SET_TILE_INFO(0, code, color, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_strnskil  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_cols, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		tilemap_set_scroll_rows(bg_tilemap, 32);
	
		return 0;
	} };
	
	static void strnskil_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = 0x60; offs < 0x100; offs += 4)
		{
			int code = spriteram.read(offs + 1);
			int color = spriteram.read(offs + 2)& 0x3f;
			int flipx = flip_screen_x;
			int flipy = flip_screen_y;
	
			int sx = spriteram.read(offs + 3);
			int sy = spriteram.read(offs);
			int px, py;
	
			if (flip_screen())
			{
				px = 240 - sx + 0; /* +2 or +0 ? */
				py = sy;
			}
			else
			{
				px = sx - 2;
				py = 240 - sy;
			}
	
			sx = sx & 0xff;
	
			if (sx > 248)
				sx = sx - 256;
	
			drawgfx(bitmap, Machine->gfx[1],
				code, color,
				flipx, flipy,
				px, py,
				Machine->visible_area,
				TRANSPARENCY_COLOR, 0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_strnskil  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int row;
	
		for (row = 0; row < 32; row++)
		{
			if (strnskil_scrl_ctrl != 0x07)
			{
				switch (memory_region(REGION_USER1)[strnskil_scrl_ctrl * 32 + row])
				{
				case 2:
					tilemap_set_scrollx(bg_tilemap, row, -~strnskil_xscroll[1]);
					break;
				case 4:
					tilemap_set_scrollx(bg_tilemap, row, -~strnskil_xscroll[0]);
					break;
				}
			}
		}
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		strnskil_draw_sprites(bitmap);
	} };
}
