/***************************************************************************

	Mustache Boy
	(c)1987 March Electronics

***************************************************************************/


/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class mustache
{
	
	static struct tilemap *bg_tilemap;
	static int control_byte;
	
	public static PaletteInitHandlerPtr palette_init_mustache  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
	  int i;
	
	  for (i = 0;i < 256;i++)
	  {
		int bit0,bit1,bit2,bit3,r,g,b;
	
		/* red component */
		bit0 = (color_prom.read(i)>> 0) & 0x01;
		bit1 = (color_prom.read(i)>> 1) & 0x01;
		bit2 = (color_prom.read(i)>> 2) & 0x01;
		bit3 = (color_prom.read(i)>> 3) & 0x01;
		r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		/* green component */
		bit0 = (color_prom.read(i + 256)>> 0) & 0x01;
		bit1 = (color_prom.read(i + 256)>> 1) & 0x01;
		bit2 = (color_prom.read(i + 256)>> 2) & 0x01;
		bit3 = (color_prom.read(i + 256)>> 3) & 0x01;
		g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		/* blue component */
		bit0 = (color_prom.read(i + 512)>> 0) & 0x01;
		bit1 = (color_prom.read(i + 512)>> 1) & 0x01;
		bit2 = (color_prom.read(i + 512)>> 2) & 0x01;
		bit3 = (color_prom.read(i + 512)>> 3) & 0x01;
		b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
		palette_set_color(i,r,g,b);
	  }
	} };
	
	public static WriteHandlerPtr mustache_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset / 2);
		}
	} };
	
	public static WriteHandlerPtr mustache_video_control_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (flip_screen() != (data & 0x01))
		{
			flip_screen_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		/* tile bank */
	
		if ((control_byte ^ data) & 0x08)
		{
			control_byte = data;
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static WriteHandlerPtr mustache_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		tilemap_set_scrollx(bg_tilemap, 0, 0x100 - data);
		tilemap_set_scrollx(bg_tilemap, 1, 0x100 - data);
		tilemap_set_scrollx(bg_tilemap, 2, 0x100 - data);
		tilemap_set_scrollx(bg_tilemap, 3, 0x100);
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int attr = videoram.read(2 * tile_index + 1);
		int code = videoram.read(2 * tile_index)+ ((attr & 0xe0) << 3) + ((control_byte & 0x08) << 7);
		int color = attr & 0x0f;
	
		SET_TILE_INFO(0, code, color, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_mustache  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows_flip_x,
			TILEMAP_OPAQUE, 8, 8, 64, 32);
	
		tilemap_set_scroll_rows(bg_tilemap, 4);
	
		return 0;
	} };
	
	static void mustache_draw_sprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect )
	{
		struct rectangle clip = *cliprect;
		const struct GfxElement *gfx = Machine->gfx[1];
		int offs;
	
		for (offs = 0;offs < spriteram_size;offs += 4)
		{
			int sy = 240-spriteram.read(offs);
			int sx = 240-spriteram.read(offs+3);
			int code = spriteram.read(offs+2);
			int attr = spriteram.read(offs+1);
			int color = (attr & 0xe0)>>5;
	
			if (sy == 240) continue;
	
			code+=(attr&0x0c)<<6;
	
			if ((control_byte & 0xa))
				clip.max_y = Machine->visible_area.max_y;
			else
				if (flip_screen())
					clip.min_y = Machine->visible_area.min_y + 56;
				else
					clip.max_y = Machine->visible_area.max_y - 56;
	
			if (flip_screen())
			{
				sx = 240 - sx;
				sy = 240 - sy;
			}
	
			drawgfx(bitmap,gfx,
					code,
					color,
					flip_screen(),flip_screen(),
					sx,sy,
					&clip,TRANSPARENCY_PEN,0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_mustache  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, cliprect, bg_tilemap, 0, 0);
		mustache_draw_sprites(bitmap, cliprect);
	} };
}
