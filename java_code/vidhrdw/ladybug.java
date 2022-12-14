/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class ladybug
{
	
	static struct tilemap *bg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Lady Bug has a 32 bytes palette PROM and a 32 bytes sprite color lookup
	  table PROM.
	  The palette PROM is connected to the RGB output this way:
	
	  bit 7 -- inverter -- 220 ohm resistor  -- BLUE
	        -- inverter -- 220 ohm resistor  -- GREEN
	        -- inverter -- 220 ohm resistor  -- RED
	        -- inverter -- 470 ohm resistor  -- BLUE
	        -- unused
	        -- inverter -- 470 ohm resistor  -- GREEN
	        -- unused
	  bit 0 -- inverter -- 470 ohm resistor  -- RED
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_ladybug  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		for (i = 0;i < 32;i++)
		{
			int bit1,bit2,r,g,b;
	
	
			bit1 = (~color_prom.read(i)>> 0) & 0x01;
			bit2 = (~color_prom.read(i)>> 5) & 0x01;
			r = 0x47 * bit1 + 0x97 * bit2;
			bit1 = (~color_prom.read(i)>> 2) & 0x01;
			bit2 = (~color_prom.read(i)>> 6) & 0x01;
			g = 0x47 * bit1 + 0x97 * bit2;
			bit1 = (~color_prom.read(i)>> 4) & 0x01;
			bit2 = (~color_prom.read(i)>> 7) & 0x01;
			b = 0x47 * bit1 + 0x97 * bit2;
			palette_set_color(i,r,g,b);
		}
	
		/* characters */
		for (i = 0;i < 8;i++)
		{
			colortable[4 * i] = 0;
			colortable[4 * i + 1] = i + 0x08;
			colortable[4 * i + 2] = i + 0x10;
			colortable[4 * i + 3] = i + 0x18;
		}
	
		/* sprites */
		for (i = 0;i < 4 * 8;i++)
		{
			int bit0,bit1,bit2,bit3;
	
	
			/* low 4 bits are for sprite n */
			bit0 = (color_prom.read(i + 32)>> 3) & 0x01;
			bit1 = (color_prom.read(i + 32)>> 2) & 0x01;
			bit2 = (color_prom.read(i + 32)>> 1) & 0x01;
			bit3 = (color_prom.read(i + 32)>> 0) & 0x01;
			colortable[i + 4 * 8] = 1 * bit0 + 2 * bit1 + 4 * bit2 + 8 * bit3;
	
			/* high 4 bits are for sprite n + 8 */
			bit0 = (color_prom.read(i + 32)>> 7) & 0x01;
			bit1 = (color_prom.read(i + 32)>> 6) & 0x01;
			bit2 = (color_prom.read(i + 32)>> 5) & 0x01;
			bit3 = (color_prom.read(i + 32)>> 4) & 0x01;
			colortable[i + 4 * 16] = 1 * bit0 + 2 * bit1 + 4 * bit2 + 8 * bit3;
		}
	} };
	
	public static WriteHandlerPtr ladybug_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr ladybug_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
	
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr ladybug_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (flip_screen() != (data & 0x01))
		{
			flip_screen_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index)+ 32 * (colorram.read(tile_index)& 0x08);
		int color = colorram.read(tile_index)& 0x07;
	
		SET_TILE_INFO(0, code, color, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_ladybug  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, 
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		tilemap_set_scroll_rows(bg_tilemap, 32);
	
		return 0;
	} };
	
	static void ladybug_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 2*0x40;offs >= 2*0x40;offs -= 0x40)
		{
			int i = 0;
	
			while (i < 0x40 && spriteram.read(offs + i)!= 0)
				i += 4;
	
			while (i > 0)
			{
	/*
	 abccdddd eeeeeeee fffghhhh iiiiiiii
	
	 a enable?
	 b size (0 = 8x8, 1 = 16x16)
	 cc flip
	 dddd y offset
	 eeeeeeee sprite code (shift right 2 bits for 16x16 sprites)
	 fff unknown
	 g sprite bank
	 hhhh color
	 iiiiiiii x position
	*/
				i -= 4;
	
				if (spriteram.read(offs + i)& 0x80)
				{
					if (spriteram.read(offs + i)& 0x40)	/* 16x16 */
						drawgfx(bitmap,Machine->gfx[1],
								(spriteram.read(offs + i + 1)>> 2) + 4 * (spriteram.read(offs + i + 2)& 0x10),
								spriteram.read(offs + i + 2)& 0x0f,
								spriteram.read(offs + i)& 0x20,spriteram.read(offs + i)& 0x10,
								spriteram.read(offs + i + 3),
								offs / 4 - 8 + (spriteram.read(offs + i)& 0x0f),
								Machine->visible_area,TRANSPARENCY_PEN,0);
					else	/* 8x8 */
						drawgfx(bitmap,Machine->gfx[2],
								spriteram.read(offs + i + 1)+ 4 * (spriteram.read(offs + i + 2)& 0x10),
								spriteram.read(offs + i + 2)& 0x0f,
								spriteram.read(offs + i)& 0x20,spriteram.read(offs + i)& 0x10,
								spriteram.read(offs + i + 3),
								offs / 4 + (spriteram.read(offs + i)& 0x0f),
								Machine->visible_area,TRANSPARENCY_PEN,0);
				}
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_ladybug  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs;
	
		for (offs = 0; offs < 32; offs++)
		{
			int sx = offs % 4;
			int sy = offs / 4;
	
			if (flip_screen())
				tilemap_set_scrollx(bg_tilemap, offs, -videoram.read(32 * sx + sy));
			else
				tilemap_set_scrollx(bg_tilemap, offs, videoram.read(32 * sx + sy));
		}
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		ladybug_draw_sprites(bitmap);
	} };
}
