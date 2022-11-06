
/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class hitme
{
	
	
	
	/* Palette Init */
	
	public static PaletteInitHandlerPtr palette_init_hitme  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		palette_set_color(0,0x00,0x00,0x00);	/* black */
		palette_set_color(1,0xff,0xff,0xff);	/* white */
	} };
	
	/* Tilemap */
	
	static struct tilemap *hitme_tilemap;
	
	public static GetTileInfoHandlerPtr get_hitme_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = hitme_vidram[tile_index] & 0x3f;
		SET_TILE_INFO(
				0,
				code,
				0,
				0)
	} };
	
	/* the old vidhrdw/hitme.c had some invert thing, need to change it to work with this I guess .. */
	
	/*
	
		for (offs = 0; offs < videoram_size; offs++)
		{
			charcode = videoram.read(offs)& 0x3F;
	
			if (videoram.read(offs)& 0x80) hitme_invert_count = 4;
			if (hitme_invert_count) {
				invert = 1;
				hitme_invert_count--;
			}
			else {
				invert = 0;
			}
			sx = 10 * (offs % 40);
			sy = 11 * (offs / 40);
			drawgfx(tmpbitmap,Machine->gfx[0],
	        charcode, invert,
			0,0,sx,sy,
			Machine->drv->visible_area,TRANSPARENCY_NONE,0);
		}
	
	*/
	
	public static WriteHandlerPtr hitme_vidram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		hitme_vidram[offset] = data;
		tilemap_mark_tile_dirty(hitme_tilemap,offset);
	} };
	
	/* Video Start / Update */
	
	public static VideoStartHandlerPtr video_start_hitme  = new VideoStartHandlerPtr() { public int handler(){
		hitme_tilemap = tilemap_create(get_hitme_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,40, 19);
	
		return 0;
	} };
	
	public static VideoStartHandlerPtr video_start_brickyrd  = new VideoStartHandlerPtr() { public int handler(){
		hitme_tilemap = tilemap_create(get_hitme_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32, 24);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_hitme  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,hitme_tilemap,0,0);
	} };
}
