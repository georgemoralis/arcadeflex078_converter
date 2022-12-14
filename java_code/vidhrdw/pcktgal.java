/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class pcktgal
{
	
	static struct tilemap *bg_tilemap;
	
	public static PaletteInitHandlerPtr palette_init_pcktgal  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
			bit0 = (color_prom.read(i)>> 0) & 0x01;
			bit1 = (color_prom.read(i)>> 1) & 0x01;
			bit2 = (color_prom.read(i)>> 2) & 0x01;
			bit3 = (color_prom.read(i)>> 3) & 0x01;
			r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(i)>> 4) & 0x01;
			bit1 = (color_prom.read(i)>> 5) & 0x01;
			bit2 = (color_prom.read(i)>> 6) & 0x01;
			bit3 = (color_prom.read(i)>> 7) & 0x01;
			g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(i + Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(i + Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(i + Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(i + Machine.drv.total_colors)>> 3) & 0x01;
			b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			palette_set_color(i,r,g,b);
		}
	} };
	
	public static WriteHandlerPtr pcktgal_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset / 2);
		}
	} };
	
	public static WriteHandlerPtr pcktgal_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (flip_screen() != (data & 0x80))
		{
			flip_screen_set(data & 0x80);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index*2+1)+ ((videoram.read(tile_index*2)& 0x0f) << 8);
		int color = videoram.read(tile_index*2)>> 4;
	
		SET_TILE_INFO(0, code, color, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_pcktgal  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		return 0;
	} };
	
	static void pcktgal_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = 0;offs < spriteram_size;offs += 4)
		{
			if (spriteram.read(offs)!= 0xf8)
			{
				int sx,sy,flipx,flipy;
	
	
				sx = 240 - spriteram.read(offs+2);
				sy = 240 - spriteram.read(offs);
	
				flipx = spriteram.read(offs+1)& 0x04;
				flipy = spriteram.read(offs+1)& 0x02;
				if (flip_screen()) {
					sx=240-sx;
					sy=240-sy;
					if (flipx) flipx=0; else flipx=1;
					if (flipy) flipy=0; else flipy=1;
				}
	
				drawgfx(bitmap,Machine->gfx[1],
						spriteram.read(offs+3)+ ((spriteram.read(offs+1)& 1) << 8),
						(spriteram.read(offs+1)& 0x70) >> 4,
						flipx,flipy,
						sx,sy,
						Machine->visible_area,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_pcktgal  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		pcktgal_draw_sprites(bitmap);
	} };
}
