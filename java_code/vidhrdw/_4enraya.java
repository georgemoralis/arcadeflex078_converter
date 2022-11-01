/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class _4enraya
{
	
	static struct tilemap *tilemap;
	
	public static WriteHandlerPtr fenraya_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		videoram[(offset&0x3ff)*2]=data;
		videoram[(offset&0x3ff)*2+1]=(offset&0xc00)>>10;
		tilemap_mark_tile_dirty(tilemap,offset&0x3ff);
	} };
	
	static void get_tile_info(int tile_index)
	{
		int code = videoram[tile_index*2]+(videoram[tile_index*2+1]<<8);
		SET_TILE_INFO(
			0,
			code,
			0,
			0)
	}
	
	public static VideoUpdateHandlerPtr 4enraya  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		tilemap = tilemap_create( get_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32 );
		return video_start_generic();
	} };
	
	public static VideoUpdateHandlerPtr 4enraya  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		tilemap_draw(bitmap,cliprect,tilemap, 0,0);
	} };
}
