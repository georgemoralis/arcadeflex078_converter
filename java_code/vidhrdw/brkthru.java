/***************************************************************************

	breakthru:vidhrdw.c

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class brkthru
{
	
	
	unsigned char *brkthru_scroll;
	unsigned char *brkthru_videoram;
	size_t brkthru_videoram_size;
	static int bgscroll;
	static int bgbasecolor;
	static int flipscreen;
	
	static struct tilemap *fg_tilemap;
	static struct tilemap *bg_tilemap;
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Break Thru has one 256x8 and one 256x4 palette PROMs.
	  I don't know for sure how the palette PROMs are connected to the RGB
	  output, but it's probably the usual:
	
	  bit 7 -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 2.2kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	        -- 1  kohm resistor  -- RED
	  bit 0 -- 2.2kohm resistor  -- RED
	
	  bit 3 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 1  kohm resistor  -- BLUE
	  bit 0 -- 2.2kohm resistor  -- BLUE
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_brkthru  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
	
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(0)>> 4) & 0x01;
			bit1 = (color_prom.read(0)>> 5) & 0x01;
			bit2 = (color_prom.read(0)>> 6) & 0x01;
			bit3 = (color_prom.read(0)>> 7) & 0x01;
			g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine.drv.total_colors)>> 3) & 0x01;
			b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			palette_set_color(i,r,g,b);
	
			color_prom++;
		}
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		/* BG RAM format
			0         1
			---- -c-- ---- ---- = Color
			---- --xx xxxx xxxx = Code
		*/
	
		int code = (videoram.read(tile_index*2)| ((videoram.read(tile_index*2+1)) << 8)) & 0x3ff;
		int region = 1 + (code >> 7);
		int colour = bgbasecolor + ((videoram.read(tile_index*2+1)& 0x04) >> 2);
	
		SET_TILE_INFO(region, code & 0x7f,colour,0)
	} };
	
	public static WriteHandlerPtr brkthru_bgram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap,offset/2);
		}
	} };
	
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		data8_t code = brkthru_videoram[tile_index];
		SET_TILE_INFO(0, code, 0, 0)
	} };
	
	public static WriteHandlerPtr brkthru_fgram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (brkthru_videoram[offset] != data)
		{
			brkthru_videoram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset);
		}
	} };
	
	public static VideoStartHandlerPtr video_start_brkthru  = new VideoStartHandlerPtr() { public int handler(){
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_cols,TILEMAP_TRANSPARENT,16,16,32,16);
	
		if (!fg_tilemap)
			return 1;
	
		if (!bg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen( fg_tilemap, 0 );
		tilemap_set_transparent_pen( bg_tilemap, 0 );
	
		return 0;
	} };
	
	
	
	public static WriteHandlerPtr brkthru_1800_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (offset == 0)	/* low 8 bits of scroll */
			bgscroll = (bgscroll & 0x100) | data;
		else if (offset == 1)
		{
			int bankaddress;
			unsigned char *RAM = memory_region(REGION_CPU1);
	
	
			/* bit 0-2 = ROM bank select */
			bankaddress = 0x10000 + (data & 0x07) * 0x2000;
			cpu_setbank(1,&RAM[bankaddress]);
	
			/* bit 3-5 = background tiles color code */
			if (((data & 0x38) >> 2) != bgbasecolor)
			{
				bgbasecolor = (data & 0x38) >> 2;
				tilemap_mark_all_tiles_dirty (bg_tilemap);
			}
	
			/* bit 6 = screen flip */
			if (flipscreen != (data & 0x40))
			{
				flipscreen = data & 0x40;
				tilemap_set_flip(bg_tilemap,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
				tilemap_set_flip(fg_tilemap,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	
			}
	
			/* bit 7 = high bit of scroll */
			bgscroll = (bgscroll & 0xff) | ((data & 0x80) << 1);
		}
	} };
	
	
	#if 0
	static void show_register( struct mame_bitmap *bitmap, int x, int y, unsigned long data )
	{
		int n;
	
		for( n=0; n<4; n++ ){
			drawgfx( bitmap, Machine->uifont,
				"0123456789abcdef"[(data>>(12-4*n))&0xf],
				0,
				1,0,
				y, x + n*8,
				0,TRANSPARENCY_NONE,0);
		}
	}
	#endif
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	static void brkthru_drawsprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect, int prio )
		{
		int offs;
		/* Draw the sprites. Note that it is important to draw them exactly in this */
		/* order, to have the correct priorities. */
	
		/* Sprite RAM format
			0         1         2         3
			ccc- ---- ---- ---- ---- ---- ---- ---- = Color
			---d ---- ---- ---- ---- ---- ---- ---- = Double Size
			---- p--- ---- ---- ---- ---- ---- ---- = Priority
			---- -bb- ---- ---- ---- ---- ---- ---- = Bank
			---- ---e ---- ---- ---- ---- ---- ---- = Enable/Disable
			---- ---- ssss ssss ---- ---- ---- ---- = Sprite code
			---- ---- ---- ---- yyyy yyyy ---- ---- = Y position
			---- ---- ---- ---- ---- ---- xxxx xxxx = X position
		*/
	
		for (offs = 0;offs < spriteram_size; offs += 4)
		{
			if ((spriteram.read(offs)& 0x09) == prio)	/* Enable && Low Priority */
			{
				int sx,sy,code,color;
	
				sx = 240 - spriteram.read(offs+3);
				if (sx < -7) sx += 256;
				sy = 240 - spriteram.read(offs+2);
				code = spriteram.read(offs+1)+ 128 * (spriteram.read(offs)& 0x06);
				color = (spriteram.read(offs)& 0xe0) >> 5;
				if (flipscreen)
				{
					sx = 240 - sx;
					sy = 240 - sy;
				}
	
				if (spriteram.read(offs)& 0x10)	/* double height */
				{
					drawgfx(bitmap,Machine->gfx[9],
							code & ~1,
							color,
							flipscreen,flipscreen,
							sx,flipscreen? sy + 16 : sy - 16,
							Machine->visible_area,TRANSPARENCY_PEN,0);
					drawgfx(bitmap,Machine->gfx[9],
							code | 1,
							color,
							flipscreen,flipscreen,
							sx,sy,
							Machine->visible_area,TRANSPARENCY_PEN,0);
	
					/* redraw with wraparound */
					drawgfx(bitmap,Machine->gfx[9],
							code & ~1,
							color,
							flipscreen,flipscreen,
							sx,(flipscreen? sy + 16 : sy - 16) + 256,
							Machine->visible_area,TRANSPARENCY_PEN,0);
					drawgfx(bitmap,Machine->gfx[9],
							code | 1,
							color,
							flipscreen,flipscreen,
							sx,sy + 256,
							Machine->visible_area,TRANSPARENCY_PEN,0);
	
				}
				else
				{
					drawgfx(bitmap,Machine->gfx[9],
							code,
							color,
							flipscreen,flipscreen,
							sx,sy,
							Machine->visible_area,TRANSPARENCY_PEN,0);
	
					/* redraw with wraparound */
					drawgfx(bitmap,Machine->gfx[9],
							code,
							color,
							flipscreen,flipscreen,
							sx,sy + 256,
							Machine->visible_area,TRANSPARENCY_PEN,0);
	
				}
				}
			}
		}
	
	public static VideoUpdateHandlerPtr video_update_brkthru  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)	{
		tilemap_set_scrollx(bg_tilemap,0, bgscroll);
		tilemap_draw(bitmap,cliprect,bg_tilemap,TILEMAP_IGNORE_TRANSPARENCY,0);
	
		/* low priority sprites */
		brkthru_drawsprites(bitmap, cliprect, 0x01 );
	
		/* draw background over low priority sprites */
		tilemap_draw(bitmap,cliprect,bg_tilemap,0,0);
	
		/* high priority sprites */
		brkthru_drawsprites(bitmap, cliprect, 0x09 );
	
		/* fg layer */
		tilemap_draw(bitmap,cliprect,fg_tilemap,0,0);
	
	/*	show_register(bitmap,8,8,(unsigned long)flipscreen); */
	
	} };
}
