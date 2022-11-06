/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class blockade
{
	
	static struct tilemap *bg_tilemap;
	
	public static WriteHandlerPtr blockade_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	
		if (input_port_3_r(0) & 0x80)
		{
			logerror("blockade_videoram_w: scanline %d\n", cpu_getscanline());
			cpu_spinuntil_int();
		}
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index);
	
		SET_TILE_INFO(0, code, 0, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_blockade  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows,
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_blockade  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
	} };
}
