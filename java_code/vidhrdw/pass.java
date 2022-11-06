/* vidhrdw/pass.c - see drivers/pass.c for more info */

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class pass
{
	
	static struct tilemap *pass_bg_tilemap;
	static struct tilemap *pass_fg_tilemap;
	
	/* in drivers/pass.c */
	/* end in drivers/pass.c */
	
	/* background tilemap stuff */
	
	public static GetTileInfoHandlerPtr get_pass_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tileno,fx;
	
		tileno = pass_bg_videoram[tile_index] & 0x1fff;
		fx = (pass_bg_videoram[tile_index] & 0xc000) >> 14;
		SET_TILE_INFO(1,tileno,0,TILE_FLIPYX(fx))
	
	} };
	
	WRITE16_HANDLER( pass_bg_videoram_w )
	{
		if (pass_bg_videoram[offset] != data)
		{
			pass_bg_videoram[offset] = data;
			tilemap_mark_tile_dirty(pass_bg_tilemap,offset);
		}
	}
	
	/* foreground 'sprites' tilemap stuff */
	
	public static GetTileInfoHandlerPtr get_pass_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tileno, flip;
	
		tileno = pass_fg_videoram[tile_index] & 0x3fff;
		flip = (pass_fg_videoram[tile_index] & 0xc000) >>14;
	
		SET_TILE_INFO(0,tileno,0,TILE_FLIPYX(flip));
	
	} };
	
	WRITE16_HANDLER( pass_fg_videoram_w )
	{
		if (pass_fg_videoram[offset] != data)
		{
			pass_fg_videoram[offset] = data;
			tilemap_mark_tile_dirty(pass_fg_tilemap,offset);
		}
	}
	
	/* video update / start */
	
	public static VideoUpdateHandlerPtr video_update_pass  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,pass_bg_tilemap,0,0);
		tilemap_draw(bitmap,cliprect,pass_fg_tilemap,0,0);
	
	} };
	
	public static VideoStartHandlerPtr video_start_pass  = new VideoStartHandlerPtr() { public int handler(){
	
		pass_bg_tilemap = tilemap_create(get_pass_bg_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE, 8, 8,64,32);
		pass_fg_tilemap = tilemap_create(get_pass_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT, 4, 4,128,64);
	
		if (!pass_bg_tilemap || !pass_fg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(pass_fg_tilemap,255);
	
		return 0;
	} };
}
