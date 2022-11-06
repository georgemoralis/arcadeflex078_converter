/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class _4enraya
{
	
	static struct tilemap *tilemap;
	
	public static WriteHandlerPtr fenraya_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		videoram.write((offset&0x3ff)*2,data);
		videoram.write((offset&0x3ff)*2+1,(offset&0xc00)>>10);
		tilemap_mark_tile_dirty(tilemap,offset&0x3ff);
	} };
	
	public static GetTileInfoHandlerPtr get_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index*2)+(videoram.read(tile_index*2+1)<<8);
		SET_TILE_INFO(
			0,
			code,
			0,
			0)
	} };
	
	public static VideoStartHandlerPtr video_start_4enraya  = new VideoStartHandlerPtr() { public int handler(){
		tilemap = tilemap_create( get_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32 );
		return video_start_generic.handler();
	} };
	
	public static VideoUpdateHandlerPtr video_update_4enraya  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,tilemap, 0,0);
	} };
}
