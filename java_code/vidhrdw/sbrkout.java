/*************************************************************************

	Atari Super Breakout hardware

*************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class sbrkout
{
	
	UINT8 *sbrkout_horiz_ram;
	UINT8 *sbrkout_vert_ram;
	
	static struct tilemap *bg_tilemap;
	
	public static WriteHandlerPtr sbrkout_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = (videoram.read(tile_index)& 0x80) ? videoram.read(tile_index): 0;
	
		SET_TILE_INFO(0, code, 0, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_sbrkout  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows,
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		return 0;
	} };
	
	static void sbrkout_draw_balls( struct mame_bitmap *bitmap )
	{
		int ball;
	
		for (ball=2; ball>=0; ball--)
		{
			int code = ((sbrkout_vert_ram[ball * 2 + 1] & 0x80) >> 7);
			int sx = 31 * 8 - sbrkout_horiz_ram[ball * 2];
			int sy = 30 * 8 - sbrkout_vert_ram[ball * 2];
	
			drawgfx(bitmap, Machine->gfx[1],
				code, 0,
				0, 0,
				sx, sy,
				Machine->visible_area,
				TRANSPARENCY_PEN, 0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_sbrkout  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		sbrkout_draw_balls(bitmap);
	} };
}
