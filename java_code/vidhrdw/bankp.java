/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class bankp
{
	
	UINT8 *bankp_videoram2;
	UINT8 *bankp_colorram2;
	
	static int scroll_x;
	static int priority;
	
	static struct tilemap *bg_tilemap, *fg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Bank Panic has a 32x8 palette PROM (I'm not sure whether the second 16
	  bytes are used - they contain the same colors as the first 16 with only
	  one different) and two 256x4 lookup table PROMs (one for charset #1, one
	  for charset #2 - only the first 128 nibbles seem to be used).
	
	  I don't know for sure how the palette PROM is connected to the RGB output,
	  but it's probably the usual:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_bankp  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
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
	
		/* charset #1 lookup table */
		for (i = 0;i < TOTAL_COLORS(0);i++)
			COLOR(0,i) = *(color_prom++) & 0x0f;
	
		color_prom += 128;	/* skip the bottom half of the PROM - seems to be not used */
	
		/* charset #2 lookup table */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			COLOR(1,i) = *(color_prom++) & 0x0f;
	
		/* the bottom half of the PROM seems to be not used */
	} };
	
	public static WriteHandlerPtr bankp_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		scroll_x = data;
	} };
	
	public static WriteHandlerPtr bankp_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr bankp_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr bankp_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (bankp_videoram2[offset] != data)
		{
			bankp_videoram2[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr bankp_colorram2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (bankp_colorram2[offset] != data)
		{
			bankp_colorram2[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr bankp_out_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bits 0-1 are playfield priority */
		/* TODO: understand how this works */
		priority = data & 0x03;
	
		/* bits 2-3 unknown (2 is used) */
	
		/* bit 4 controls NMI */
		interrupt_enable_w(0,(data & 0x10)>>4);
	
		/* bit 5 controls screen flip */
		if (flip_screen() != (data & 0x20))
		{
			flip_screen_set(data & 0x20);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		/* bits 6-7 unknown */
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = bankp_videoram2[tile_index] + 256 * (bankp_colorram2[tile_index] & 0x07);
		int color = bankp_colorram2[tile_index] >> 4;
		int flags = (bankp_colorram2[tile_index] & 0x08) ? TILE_FLIPX : 0;
	
		SET_TILE_INFO(1, code, color, flags)
	} };
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index)+ 256 * ((colorram.read(tile_index)& 3) >> 0);
		int color = colorram.read(tile_index)>> 3;
		int flags = (colorram.read(tile_index)& 0x04) ? TILE_FLIPX : 0;
	
		SET_TILE_INFO(0, code, color, flags)
	} };
	
	public static VideoStartHandlerPtr video_start_bankp  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT_COLOR, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT_COLOR, 8, 8, 32, 32);
	
		if ( !fg_tilemap )
			return 1;
	
		tilemap_set_transparent_pen(bg_tilemap, 0);
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_bankp  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		/* The tilemap has to be shifted to the left in flip screen mode
			because the visible tilemap data is not centered in video memory */
	
		if (flip_screen())
		{
			tilemap_set_scrollx(fg_tilemap, 0, -scroll_x - 16);
			tilemap_set_scrollx(bg_tilemap, 0, -16);
		} 
		else
		{
			tilemap_set_scrollx(fg_tilemap, 0, scroll_x);
			tilemap_set_scrollx(bg_tilemap, 0, 0);
		}	
	
		switch (priority)
		{
		case 0:
			tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, TILEMAP_IGNORE_TRANSPARENCY, 0); // just a guess
			break;
		case 1:
			tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, TILEMAP_IGNORE_TRANSPARENCY, 0);
			tilemap_draw(bitmap, Machine.visible_area, fg_tilemap, 0, 0);
			break;
		case 2:
			tilemap_draw(bitmap, Machine.visible_area, fg_tilemap, TILEMAP_IGNORE_TRANSPARENCY, 0);
			tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
			break;
		case 3:
			tilemap_draw(bitmap, Machine.visible_area, fg_tilemap, TILEMAP_IGNORE_TRANSPARENCY, 0); // just a guess
			break;
		}
	} };
}
