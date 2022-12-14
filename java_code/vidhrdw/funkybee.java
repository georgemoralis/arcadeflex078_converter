/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class funkybee
{
	
	static int gfx_bank;
	static struct tilemap *bg_tilemap;
	
	public static PaletteInitHandlerPtr palette_init_funkybee  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
	
		/* first, the character/sprite palette */
		for (i = 0;i < 32;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
			/* red component */
			bit0 = (color_prom.read()>> 0) & 0x01;
			bit1 = (color_prom.read()>> 1) & 0x01;
			bit2 = (color_prom.read()>> 2) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (color_prom.read()>> 3) & 0x01;
			bit1 = (color_prom.read()>> 4) & 0x01;
			bit2 = (color_prom.read()>> 5) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (color_prom.read()>> 6) & 0x01;
			bit2 = (color_prom.read()>> 7) & 0x01;
			b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	} };
	
	public static WriteHandlerPtr funkybee_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr funkybee_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr funkybee_gfx_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (gfx_bank != (data & 0x01))
		{
			gfx_bank = data & 0x01;
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static WriteHandlerPtr funkybee_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		tilemap_set_scrollx(bg_tilemap, 0, flip_screen() ? -data : data);
	} };
	
	public static WriteHandlerPtr funkybee_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (flip_screen() != (data & 0x01))
		{
			flip_screen_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index);
		int color = colorram.read(tile_index)& 0x03;
	
		SET_TILE_INFO(gfx_bank, code, color, 0)
	} };
	
	static UINT32 funkybee_tilemap_scan( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows )
	{
		/* logical (col,row) -> memory offset */
		return 256 * row + col;
	}
	
	public static VideoStartHandlerPtr video_start_funkybee  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, funkybee_tilemap_scan,
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		return 0;
	} };
	
	static void funkybee_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = 0x0f; offs >= 0; offs--)
		{
			int offs2 = offs + 0x1e00;
			int attr = videoram.read(offs2);
			int code = (attr >> 2) | ((attr & 2) << 5);
			int color = colorram.read(offs2 + 0x10);
			int flipx = 0;
			int flipy = attr & 0x01;
			int sx = videoram.read(offs2 + 0x10);
			int sy = 224 - colorram.read(offs2);
	
			if (flip_screen())
			{
				sy += 32;
				flipx = NOT(flipx);
			}
	
			drawgfx(bitmap,Machine->gfx[2+gfx_bank],
				code, color,
				flipx, flipy,
				sx, sy,
				Machine->visible_area,
				TRANSPARENCY_PEN, 0);
		}
	}
	
	static void funkybee_draw_columns( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = 0x1f;offs >= 0;offs--)
		{
			int code = videoram.read(0x1c00 + offs);
			int color = colorram.read(0x1f10)& 0x03;
			int sx = videoram.read(0x1f10);
			int sy = offs * 8;
	
			if (flip_screen())
			{
				sx = 248 - sx;
				sy = 248 - sy;
			}
	
			drawgfx(bitmap,Machine->gfx[gfx_bank],
					code, color,
					flip_screen(), flip_screen(),
					sx, sy,
					0,TRANSPARENCY_PEN,0);
	
			code = videoram.read(0x1d00 + offs);
			color = colorram.read(0x1f11)& 0x03;
			sx = videoram.read(0x1f11);
			sy = offs * 8;
	
			if (flip_screen())
			{
				sx = 248 - sx;
				sy = 248 - sy;
			}
	
			drawgfx(bitmap,Machine->gfx[gfx_bank],
					code, color,
					flip_screen(), flip_screen(),
					sx, sy,
					0,TRANSPARENCY_PEN,0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_funkybee  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		funkybee_draw_sprites(bitmap);
		funkybee_draw_columns(bitmap);
	} };
}
