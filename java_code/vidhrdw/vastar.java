/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class vastar
{
	
	
	
	data8_t *vastar_bg1videoram,*vastar_bg2videoram,*vastar_fgvideoram;
	data8_t *vastar_bg1_scroll,*vastar_bg2_scroll;
	data8_t *vastar_sprite_priority;
	
	static struct tilemap *fg_tilemap, *bg1_tilemap, *bg2_tilemap;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = vastar_fgvideoram[tile_index + 0x800] | (vastar_fgvideoram[tile_index + 0x400] << 8);
		color = vastar_fgvideoram[tile_index];
		SET_TILE_INFO(
				0,
				code,
				color & 0x3f,
				0)
	} };
	
	public static GetTileInfoHandlerPtr get_bg1_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = vastar_bg1videoram[tile_index + 0x800] | (vastar_bg1videoram[tile_index] << 8);
		color = vastar_bg1videoram[tile_index + 0xc00];
		SET_TILE_INFO(
				4,
				code,
				color & 0x3f,
				0)
	} };
	
	public static GetTileInfoHandlerPtr get_bg2_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = vastar_bg2videoram[tile_index + 0x800] | (vastar_bg2videoram[tile_index] << 8);
		color = vastar_bg2videoram[tile_index + 0xc00];
		SET_TILE_INFO(
				3,
				code,
				color & 0x3f,
				0)
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_vastar  = new VideoStartHandlerPtr() { public int handler(){
		fg_tilemap  = tilemap_create(get_fg_tile_info, tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		bg1_tilemap = tilemap_create(get_bg1_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		bg2_tilemap = tilemap_create(get_bg2_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!fg_tilemap || !bg1_tilemap || !bg2_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,0);
		tilemap_set_transparent_pen(bg1_tilemap,0);
		tilemap_set_transparent_pen(bg2_tilemap,0);
	
		tilemap_set_scroll_cols(bg1_tilemap, 32);
		tilemap_set_scroll_cols(bg2_tilemap, 32);
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr vastar_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		vastar_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr vastar_bg1videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		vastar_bg1videoram[offset] = data;
		tilemap_mark_tile_dirty(bg1_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr vastar_bg2videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		vastar_bg2videoram[offset] = data;
		tilemap_mark_tile_dirty(bg2_tilemap,offset & 0x3ff);
	} };
	
	
	public static ReadHandlerPtr vastar_bg1videoram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return vastar_bg1videoram[offset];
	} };
	
	public static ReadHandlerPtr vastar_bg2videoram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return vastar_bg2videoram[offset];
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap,const struct rectangle *cliprect)
	{
		int offs;
	
	
		for (offs = 0; offs < spriteram_size; offs += 2)
		{
			int code, sx, sy, color, flipx, flipy;
	
	
			code = ((spriteram_3.read(offs)& 0xfc) >> 2) + ((spriteram_2.read(offs)& 0x01) << 6)
					+ ((offs & 0x20) << 2);
	
			sx = spriteram_3.read(offs + 1);
			sy = spriteram.read(offs);
			color = spriteram.read(offs + 1)& 0x3f;
			flipx = spriteram_3.read(offs)& 0x02;
			flipy = spriteram_3.read(offs)& 0x01;
	
			if (flip_screen())
			{
				flipx = NOT(flipx);
				flipy = NOT(flipy);
			}
	
			if (spriteram_2.read(offs)& 0x08)	/* double width */
			{
				if (!flip_screen())
					sy = 224 - sy;
	
				drawgfx(bitmap,Machine->gfx[2],
						code/2,
						color,
						flipx,flipy,
						sx,sy,
						cliprect,TRANSPARENCY_PEN,0);
				/* redraw with wraparound */
				drawgfx(bitmap,Machine->gfx[2],
						code/2,
						color,
						flipx,flipy,
						sx,sy+256,
						cliprect,TRANSPARENCY_PEN,0);
			}
			else
			{
				if (!flip_screen())
					sy = 240 - sy;
	
				drawgfx(bitmap,Machine->gfx[1],
						code,
						color,
						flipx,flipy,
						sx,sy,
						cliprect,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_vastar  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int i;
	
	
		for (i = 0;i < 32;i++)
		{
			tilemap_set_scrolly(bg1_tilemap,i,vastar_bg1_scroll[i]);
			tilemap_set_scrolly(bg2_tilemap,i,vastar_bg2_scroll[i]);
		}
	
		switch (*vastar_sprite_priority)
		{
		case 0:
			tilemap_draw(bitmap,cliprect, bg1_tilemap, TILEMAP_IGNORE_TRANSPARENCY,0);
			draw_sprites(bitmap,cliprect);
			tilemap_draw(bitmap,cliprect, bg2_tilemap, 0,0);
			tilemap_draw(bitmap,cliprect, fg_tilemap, 0,0);
			break;
	
		case 2:
			tilemap_draw(bitmap,cliprect, bg1_tilemap, TILEMAP_IGNORE_TRANSPARENCY,0);
			draw_sprites(bitmap,cliprect);
			tilemap_draw(bitmap,cliprect, bg1_tilemap, 0,0);
			tilemap_draw(bitmap,cliprect, bg2_tilemap, 0,0);
			tilemap_draw(bitmap,cliprect, fg_tilemap, 0,0);
			break;
	
		case 3:
			tilemap_draw(bitmap,cliprect, bg1_tilemap, TILEMAP_IGNORE_TRANSPARENCY,0);
			tilemap_draw(bitmap,cliprect, bg2_tilemap, 0,0);
			tilemap_draw(bitmap,cliprect, fg_tilemap, 0,0);
			draw_sprites(bitmap,cliprect);
			break;
	
		default:
			logerror("Unimplemented priority %X\n", *vastar_sprite_priority);
			break;
		}
	} };
}
