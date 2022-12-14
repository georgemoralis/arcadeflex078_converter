/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class mosaic
{
	
	
	data8_t *mosaic_fgvideoram;
	data8_t *mosaic_bgvideoram;
	
	static struct tilemap *bg_tilemap,*fg_tilemap;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		tile_index *= 2;
		SET_TILE_INFO(
				0,
				mosaic_fgvideoram[tile_index] + (mosaic_fgvideoram[tile_index+1] << 8),
				0,
				0)
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		tile_index *= 2;
		SET_TILE_INFO(
				1,
				mosaic_bgvideoram[tile_index] + (mosaic_bgvideoram[tile_index+1] << 8),
				0,
				0)
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_mosaic  = new VideoStartHandlerPtr() { public int handler(){
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,64,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,     8,8,64,32);
	
		if (!fg_tilemap || !bg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,0xff);
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr mosaic_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		mosaic_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset/2);
	} };
	
	public static WriteHandlerPtr mosaic_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		mosaic_bgvideoram[offset] = data;
		tilemap_mark_tile_dirty(bg_tilemap,offset/2);
	} };
	
	
	
	public static VideoUpdateHandlerPtr video_update_mosaic  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,bg_tilemap,0,0);
		tilemap_draw(bitmap,cliprect,fg_tilemap,0,0);
	} };
}
