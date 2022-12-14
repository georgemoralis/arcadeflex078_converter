/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class rockola
{
	
	UINT8 *rockola_videoram2;
	UINT8 *rockola_charram;
	
	static int charbank;
	static int backcolor;
	
	static struct tilemap *bg_tilemap, *fg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Zarzon has a different PROM layout from the others.
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_rockola  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		for (i = 0; i < Machine.drv.total_colors; i++)
		{
			int bit0, bit1, bit2, r, g, b;
	
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
	
			palette_set_color(i, r, g, b);
	
			color_prom++;
		}
	
		backcolor = 0;	/* background color can be changed by the game */
	
		for (i = 0; i < TOTAL_COLORS(0); i++)
			COLOR(0, i) = i;
	
		for (i = 0; i < TOTAL_COLORS(1); i++)
		{
			if (i % 4 == 0)
				COLOR(1, i) = 4 * backcolor + 0x20;
			else
				COLOR(1, i) = i + 0x20;
		}
	} };
	
	public static WriteHandlerPtr rockola_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr rockola_videoram2_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (rockola_videoram2[offset] != data)
		{
			rockola_videoram2[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr rockola_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr rockola_charram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (rockola_charram[offset] != data)
		{
			rockola_charram[offset] = data;
			tilemap_mark_all_tiles_dirty(fg_tilemap);
		}
	} };
	
	public static WriteHandlerPtr rockola_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		int bank;
	
		/* bits 0-2 select background color */
	
		if (backcolor != (data & 7))
		{
			int i;
	
			backcolor = data & 7;
	
			for (i = 0;i < 32;i += 4)
				Machine->gfx[1]->colortable[i] = Machine->pens[4 * backcolor + 0x20];
	
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		/* bit 3 selects char bank */
	
		bank = (~data & 0x08) >> 3;
	
		if (charbank != bank)
		{
			charbank = bank;
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		/* bit 7 flips screen */
	
		if (flip_screen() != (data & 0x80))
		{
			flip_screen_set(data & 0x80);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static WriteHandlerPtr rockola_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		tilemap_set_scrollx(bg_tilemap, 0, data);
	} };
	
	public static WriteHandlerPtr rockola_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		tilemap_set_scrolly(bg_tilemap, 0, data);
	} };
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index)+ 256 * charbank;
		int color = (colorram.read(tile_index)& 0x38) >> 3;
	
		SET_TILE_INFO(1, code, color, 0)
	} };
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = rockola_videoram2[tile_index];
		int color = colorram.read(tile_index)& 0x07;
	
		decodechar(Machine->gfx[0], code, rockola_charram, 
			Machine->drv->gfxdecodeinfo[0].gfxlayout);
	
		SET_TILE_INFO(0, code, color, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_rockola  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if ( !fg_tilemap )
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_rockola  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		tilemap_draw(bitmap, Machine.visible_area, fg_tilemap, 0, 0);
	} };
	
	/* Satan of Saturn */
	
	public static PaletteInitHandlerPtr palette_init_satansat  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		for (i = 0; i < Machine.drv.total_colors; i++)
		{
			int bit0, bit1, bit2, r, g, b;
	
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
	
			palette_set_color(i, r, g, b);
	
			color_prom++;
		}
	
		backcolor = 0;	/* background color can be changed by the game */
	
		for (i = 0; i < TOTAL_COLORS(0); i++)
			COLOR(0, i) = 4 * (i % 4) + (i / 4);
	
		for (i = 0; i < TOTAL_COLORS(1); i++)
		{
			if (i % 4 == 0)
				COLOR(1, i) = backcolor + 0x10;
			else
				COLOR(1, i) = 4 * (i % 4) + (i / 4) + 0x10;
		}
	} };
	
	public static WriteHandlerPtr satansat_b002_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bit 0 flips screen */
	
		if (flip_screen() != (data & 0x01))
		{
			flip_screen_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		/* bit 1 enables interrups */
		/* it controls only IRQs, not NMIs. Here I am affecting both, which */
		/* is wrong. */
	
		interrupt_enable_w(0,data & 0x02);
	
		/* other bits unused */
	} };
	
	public static WriteHandlerPtr satansat_backcolor_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bits 0-1 select background color. Other bits unused. */
	
		if (backcolor != (data & 0x03))
		{
			int i;
	
			backcolor = data & 0x03;
	
			for (i = 0; i < 16; i += 4)
				Machine->gfx[1]->colortable[i] = Machine->pens[backcolor + 0x10];
	
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static GetTileInfoHandlerPtr satansat_get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index);
		int color = (colorram.read(tile_index)& 0x0c) >> 2;
	
		SET_TILE_INFO(1, code, color, 0)
	} };
	
	public static GetTileInfoHandlerPtr satansat_get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = rockola_videoram2[tile_index];
		int color = colorram.read(tile_index)& 0x03;
	
		decodechar(Machine->gfx[0], code, rockola_charram, 
			Machine->drv->gfxdecodeinfo[0].gfxlayout);
	
		SET_TILE_INFO(0, code, color, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_satansat  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(satansat_get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		fg_tilemap = tilemap_create(satansat_get_fg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if ( !fg_tilemap )
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	} };
}
