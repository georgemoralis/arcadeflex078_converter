/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class seicross
{
	
	UINT8 *seicross_row_scroll;
	
	static struct tilemap *bg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Seicross has two 32x8 palette PROMs, connected to the RGB output this way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_seicross  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
			/* red component */
			bit0 = (color_prom.read(i)>> 0) & 0x01;
			bit1 = (color_prom.read(i)>> 1) & 0x01;
			bit2 = (color_prom.read(i)>> 2) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (color_prom.read(i)>> 3) & 0x01;
			bit1 = (color_prom.read(i)>> 4) & 0x01;
			bit2 = (color_prom.read(i)>> 5) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = (color_prom.read(i)>> 6) & 0x01;
			bit1 = (color_prom.read(i)>> 7) & 0x01;
			b = 0x4f * bit0 + 0xa8 * bit1;
	
			palette_set_color(i,r,g,b);
		}
	} };
	
	public static WriteHandlerPtr seicross_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr seicross_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			/* bit 5 of the address is not used for color memory. There is just */
			/* 512k of memory; every two consecutive rows share the same memory */
			/* region. */
			offset &= 0xffdf;
	
			colorram.write(offset,data);
			colorram.write(offset + 0x20,data);
	
			tilemap_mark_tile_dirty(bg_tilemap, offset);
			tilemap_mark_tile_dirty(bg_tilemap, offset + 0x20);
		}
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x10) << 4);
		int color = colorram.read(tile_index)& 0x0f;
		int flags = ((colorram.read(tile_index)& 0x40) ? TILE_FLIPX : 0) | ((colorram.read(tile_index)& 0x80) ? TILE_FLIPY : 0);
	
		SET_TILE_INFO(0, code, color, flags)
	} };
	
	public static VideoStartHandlerPtr video_start_seicross  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		tilemap_set_scroll_cols(bg_tilemap, 32);
	
		return 0;
	} };
	
	static void seicross_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			int x = spriteram.read(offs + 3);
			drawgfx(bitmap,Machine->gfx[1],
					(spriteram.read(offs)& 0x3f) + ((spriteram.read(offs + 1)& 0x10) << 2) + 128,
					spriteram.read(offs + 1)& 0x0f,
					spriteram.read(offs)& 0x40,spriteram.read(offs)& 0x80,
					x,240-spriteram.read(offs + 2),
					Machine->visible_area,TRANSPARENCY_PEN,0);
			if(x>0xf0)
				drawgfx(bitmap,Machine->gfx[1],
						(spriteram.read(offs)& 0x3f) + ((spriteram.read(offs + 1)& 0x10) << 2) + 128,
						spriteram.read(offs + 1)& 0x0f,
						spriteram.read(offs)& 0x40,spriteram.read(offs)& 0x80,
						x-256,240-spriteram.read(offs + 2),
						Machine->visible_area,TRANSPARENCY_PEN,0);
		}
	
		for (offs = spriteram_2_size - 4;offs >= 0;offs -= 4)
		{
			int x = spriteram_2.read(offs + 3);
			drawgfx(bitmap,Machine->gfx[1],
					(spriteram_2.read(offs)& 0x3f) + ((spriteram_2.read(offs + 1)& 0x10) << 2),
					spriteram_2.read(offs + 1)& 0x0f,
					spriteram_2.read(offs)& 0x40,spriteram_2.read(offs)& 0x80,
					x,240-spriteram_2.read(offs + 2),
					Machine->visible_area,TRANSPARENCY_PEN,0);
			if(x>0xf0)
				drawgfx(bitmap,Machine->gfx[1],
						(spriteram_2.read(offs)& 0x3f) + ((spriteram_2.read(offs + 1)& 0x10) << 2),
						spriteram_2.read(offs + 1)& 0x0f,
						spriteram_2.read(offs)& 0x40,spriteram_2.read(offs)& 0x80,
						x-256,240-spriteram_2.read(offs + 2),
						Machine->visible_area,TRANSPARENCY_PEN,0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_seicross  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int col;
	
		for (col = 0; col < 32; col++)
		{
			tilemap_set_scrolly(bg_tilemap, col, seicross_row_scroll[col]);
		}
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		seicross_draw_sprites(bitmap);
	} };
}
