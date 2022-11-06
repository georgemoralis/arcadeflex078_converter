/***************************************************************************

  vidhrdw/jack.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class jack
{
	
	static struct tilemap *bg_tilemap;
	
	public static WriteHandlerPtr jack_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr jack_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr jack_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* RGB output is inverted */
		paletteram_BBGGGRRR_w(offset,~data);
	} };
	
	public static ReadHandlerPtr jack_flipscreen_r  = new ReadHandlerPtr() { public int handler(int offset){
		flip_screen_set(offset);
		return 0;
	} };
	
	public static WriteHandlerPtr jack_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		flip_screen_set(offset);
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x18) << 5);
		int color = colorram.read(tile_index)& 0x07;
	
		SET_TILE_INFO(0, code, color, 0)
	} };
	
	static UINT32 tilemap_scan_cols_flipy( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows )
	{
		/* logical (col,row) -> memory offset */
		return (col * num_rows) + (num_rows - 1 - row);
	}
	
	public static VideoStartHandlerPtr video_start_jack  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_cols_flipy, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		return 0;
	} };
	
	static void jack_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int sx,sy,num, color,flipx,flipy;
	
			sx    = spriteram.read(offs + 1);
			sy    = spriteram.read(offs);
			num   = spriteram.read(offs + 2)+ ((spriteram.read(offs + 3)& 0x08) << 5);
			color = spriteram.read(offs + 3)& 0x07;
			flipx = (spriteram.read(offs + 3)& 0x80);
			flipy = (spriteram.read(offs + 3)& 0x40);
	
			if (flip_screen())
			{
				sx = 248 - sx;
				sy = 248 - sy;
				flipx = NOT(flipx);
				flipy = NOT(flipy);
			}
	
			drawgfx(bitmap,Machine->gfx[0],
					num,
					color,
					flipx,flipy,
					sx,sy,
					Machine->visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_jack  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		jack_draw_sprites(bitmap);
	} };
}
