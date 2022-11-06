/***************************************************************************

Atari Poolshark video emulation

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class poolshrk
{
	
	UINT8* poolshrk_playfield_ram;
	UINT8* poolshrk_hpos_ram;
	UINT8* poolshrk_vpos_ram;
	
	static struct tilemap* tilemap;
	
	
	static UINT32 get_memory_offset(UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows)
	{
		return num_cols * row + col;
	}
	
	
	public static GetTileInfoHandlerPtr get_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		SET_TILE_INFO(1, poolshrk_playfield_ram[tile_index] & 0x3f, 0, 0)
	} };
	
	
	public static VideoStartHandlerPtr video_start_poolshrk  = new VideoStartHandlerPtr() { public int handler(){
		tilemap = tilemap_create(get_tile_info, get_memory_offset,
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if (tilemap == NULL)
			return 1;
	
		tilemap_set_transparent_pen(tilemap, 0);
	
		return 0;
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_poolshrk  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int i;
	
		tilemap_mark_all_tiles_dirty(tilemap);
	
		fillbitmap(bitmap, Machine.pens[0], cliprect);
	
		/* draw sprites */
	
		for (i = 0; i < 16; i++)
		{
			int hpos = poolshrk_hpos_ram[i];
			int vpos = poolshrk_vpos_ram[i];
	
			drawgfx(bitmap, Machine.gfx[0], i, (i == 0) ? 0 : 1, 0, 0,
				248 - hpos, vpos - 15, cliprect, TRANSPARENCY_PEN, 0);
		}
	
		/* draw playfield */
	
		tilemap_draw(bitmap, cliprect, tilemap, 0, 0);
	} };
}
