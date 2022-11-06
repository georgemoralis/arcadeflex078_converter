/******************************************************************************

Kikiippatsu Mayumi-chan (c) 1988 Victory L.L.C.

Video hardware
	driver by Uki

******************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class mayumi
{
	
	static data8_t *mayumi_videoram;
	static struct tilemap *mayumi_tilemap;
	
	public static GetTileInfoHandlerPtr get_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = mayumi_videoram[tile_index] + (mayumi_videoram[tile_index+0x800] & 0x1f)*0x100 ;
		int col = (mayumi_videoram[tile_index+0x1000] >> 3) & 0x1f;
	
		SET_TILE_INFO(0, code, col, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_mayumi  = new VideoStartHandlerPtr() { public int handler(){
		mayumi_videoram = auto_malloc(0x1800);
	
		mayumi_tilemap = tilemap_create( get_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,64,32 );
	
		if (!mayumi_videoram || !mayumi_tilemap )
			return 1;
	
		return 0;
	} };
	
	public static WriteHandlerPtr mayumi_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		mayumi_videoram[offset] = data;
	
		tilemap_mark_tile_dirty(mayumi_tilemap, offset & 0x7ff );
	} };
	
	public static ReadHandlerPtr mayumi_videoram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return mayumi_videoram[offset];
	} };
	
	public static VideoUpdateHandlerPtr video_update_mayumi  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, cliprect, mayumi_tilemap, 0, 0);
	} };
	
}
