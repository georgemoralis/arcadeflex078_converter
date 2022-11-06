/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class kopunch
{
	
	UINT8 *kopunch_videoram2;
	
	static INT8 scroll[2]; // REMOVE
	static int gfxbank, gfxflip;
	
	static struct tilemap *bg_tilemap, *fg_tilemap;
	
	public static PaletteInitHandlerPtr palette_init_kopunch  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		color_prom+=24;	/* first 24 colors are black */
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
	} };
	
	public static WriteHandlerPtr kopunch_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr kopunch_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (kopunch_videoram2[offset] != data)
		{
			kopunch_videoram2[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr kopunch_scroll_x_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		scroll[0] = data; // REMOVE
		tilemap_set_scrollx(fg_tilemap, 0, data);
	} };
	
	public static WriteHandlerPtr kopunch_scroll_y_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		scroll[1] = data; // REMOVE
		tilemap_set_scrolly(fg_tilemap, 0, data);
	} };
	
	public static WriteHandlerPtr kopunch_gfxbank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (gfxbank != (data & 0x07))
		{
			gfxbank = data & 0x07;
			tilemap_mark_all_tiles_dirty(fg_tilemap);
		}
	
		gfxflip = data & 0x08; // REMOVE
	
		tilemap_set_flip(fg_tilemap, (data & 0x08) ? TILEMAP_FLIPY : 0);
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index);
	
		SET_TILE_INFO(0, code, 0, 0)
	} };
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = (kopunch_videoram2[tile_index] & 0x7f) + 128 * gfxbank;
	
		SET_TILE_INFO(1, code, 0, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_kopunch  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT, 8, 8, 16, 16);
	
		if ( !fg_tilemap )
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_kopunch  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs;
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		//tilemap_draw(bitmap, Machine.visible_area, fg_tilemap, 0, 0);
	
		for (offs = 1023;offs >= 0;offs--)
		{
			int sx,sy;
	
			sx = offs % 16;
			sy = offs / 16;
	
			drawgfx(bitmap,Machine.gfx[1],
					(kopunch_videoram2[offs] & 0x7f) + 128 * gfxbank,
					0,
					0,gfxflip,
					8*(sx+8)+scroll[0],8*(8+(gfxflip ? 15-sy : sy))+scroll[1],
					Machine.visible_area,TRANSPARENCY_PEN,0);
		}
	} };
}
