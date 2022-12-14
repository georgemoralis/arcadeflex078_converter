/***************************************************************************
  Goindol

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class goindol
{
	
	UINT8 *goindol_bg_videoram;
	UINT8 *goindol_fg_videoram;
	UINT8 *goindol_fg_scrollx;
	UINT8 *goindol_fg_scrolly;
	
	size_t goindol_fg_videoram_size;
	size_t goindol_bg_videoram_size;
	int goindol_char_bank;
	
	static struct tilemap *bg_tilemap,*fg_tilemap;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = goindol_fg_videoram[2*tile_index+1];
		int attr = goindol_fg_videoram[2*tile_index];
		SET_TILE_INFO(
				0,
				code | ((attr & 0x7) << 8) | (goindol_char_bank << 11),
				(attr & 0xf8) >> 3,
				0)
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = goindol_bg_videoram[2*tile_index+1];
		int attr = goindol_bg_videoram[2*tile_index];
		SET_TILE_INFO(
				1,
				code | ((attr & 0x7) << 8) | (goindol_char_bank << 11),
				(attr & 0xf8) >> 3,
				0)
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_goindol  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_SPLIT,      8,8,32,32);
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!fg_tilemap || !bg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,0);
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr goindol_fg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (goindol_fg_videoram[offset] != data)
		{
			goindol_fg_videoram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset / 2);
		}
	} };
	
	public static WriteHandlerPtr goindol_bg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (goindol_bg_videoram[offset] != data)
		{
			goindol_bg_videoram[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap,offset / 2);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect, int gfxbank, unsigned char *sprite_ram)
	{
		int offs,sx,sy,tile,palette;
	
		for (offs = 0 ;offs < spriteram_size; offs+=4)
		{
			sx = sprite_ram[offs];
			sy = 240-sprite_ram[offs+1];
	
			if (flip_screen())
			{
				sx = 248 - sx;
				sy = 248 - sy;
			}
	
			if ((sprite_ram[offs+1] >> 3) && (sx < 248))
			{
				tile	 = ((sprite_ram[offs+3])+((sprite_ram[offs+2] & 7) << 8));
				tile	+= tile;
				palette	 = sprite_ram[offs+2] >> 3;
	
				drawgfx(bitmap,Machine->gfx[gfxbank],
							tile,
							palette,
							flip_screen(),flip_screen(),
							sx,sy,
							cliprect,
							TRANSPARENCY_PEN, 0);
				drawgfx(bitmap,Machine->gfx[gfxbank],
							tile+1,
							palette,
							flip_screen(),flip_screen(),
							sx,sy + (flip_screen() ? -8 : 8),
							cliprect,
							TRANSPARENCY_PEN, 0);
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_goindol  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_set_scrollx(fg_tilemap,0,*goindol_fg_scrollx);
		tilemap_set_scrolly(fg_tilemap,0,*goindol_fg_scrolly);
	
		tilemap_draw(bitmap,cliprect,bg_tilemap,0,0);
		tilemap_draw(bitmap,cliprect,fg_tilemap,0,0);
		draw_sprites(bitmap,cliprect,1,spriteram);
		draw_sprites(bitmap,cliprect,0,spriteram_2);
	} };
}
