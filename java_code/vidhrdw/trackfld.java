/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class trackfld
{
	
	UINT8 *trackfld_scroll;
	UINT8 *trackfld_scroll2;
	
	static struct tilemap *bg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Track 'n Field has one 32x8 palette PROM and two 256x4 lookup table PROMs
	  (one for characters, one for sprites).
	  The palette PROM is connected to the RGB output this way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_trackfld  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
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
	
		/* color_prom now points to the beginning of the lookup table */
	
	
		/* sprites */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			COLOR(1,i) = *(color_prom++) & 0x0f;
	
		/* characters */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			COLOR(0,i) = (*(color_prom++) & 0x0f) + 0x10;
	} };
	
	public static WriteHandlerPtr trackfld_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr trackfld_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr trackfld_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (flip_screen() != data)
		{
			flip_screen_set(data);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int attr = colorram.read(tile_index);
		int code = videoram.read(tile_index)+ 4 * (attr & 0xc0);
		int color = attr & 0x0f;
		int flags = ((attr & 0x10) ? TILE_FLIPX : 0) | ((attr & 0x20) ? TILE_FLIPY : 0);
	
		SET_TILE_INFO(0, code, color, flags)
	} };
	
	public static VideoStartHandlerPtr video_start_trackfld  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 64, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		tilemap_set_scroll_rows(bg_tilemap, 32);
	
		return 0;
	} };
	
	static void trackfld_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 2; offs >= 0; offs -= 2)
		{
			int attr = spriteram_2.read(offs);
			int code = spriteram.read(offs + 1);
			int color = attr & 0x0f;
			int flipx = ~attr & 0x40;
			int flipy = attr & 0x80;
			int sx = spriteram.read(offs)- 1;
			int sy = 240 - spriteram_2.read(offs + 1);
	
			if (flip_screen())
			{
				sy = 240 - sy;
				flipy = NOT(flipy);
			}
	
			/* Note that this adjustement must be done AFTER handling flip screen, thus */
			/* proving that this is a hardware related "feature" */
			sy += 1;
	
			drawgfx(bitmap, Machine->gfx[1],
				code, color,
				flipx, flipy,
				sx, sy,
				Machine->visible_area,
				TRANSPARENCY_COLOR, 0);
	
			/* redraw with wraparound */
			drawgfx(bitmap,Machine->gfx[1],
				code, color,
				flipx, flipy,
				sx - 256, sy,
				Machine->visible_area,
				TRANSPARENCY_COLOR, 0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_trackfld  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int row, scrollx;
	
		for (row = 0; row < 32; row++)
		{
			scrollx = trackfld_scroll[row] + 256 * (trackfld_scroll2[row] & 0x01);
			if (flip_screen()) scrollx = -scrollx;
			tilemap_set_scrollx(bg_tilemap, row, scrollx);
		}
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		trackfld_draw_sprites(bitmap);
	} };
}
