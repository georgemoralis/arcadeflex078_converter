/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class yiear
{
	
	int nmi_enable;
	
	static struct tilemap *bg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Yie Ar Kung-Fu has one 32x8 palette PROM, connected to the RGB output this
	  way:
	
	  bit 7 -- 220 ohm resistor  -- BLUE
	        -- 470 ohm resistor  -- BLUE
	        -- 220 ohm resistor  -- GREEN
	        -- 470 ohm resistor  -- GREEN
	        -- 1  kohm resistor  -- GREEN
	        -- 220 ohm resistor  -- RED
	        -- 470 ohm resistor  -- RED
	  bit 0 -- 1  kohm resistor  -- RED
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_yiear  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
	
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
	} };
	
	public static WriteHandlerPtr yiear_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset / 2);
		}
	} };
	
	public static WriteHandlerPtr yiear_control_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bit 0 flips screen */
	
		if (flip_screen() != (data & 0x01))
		{
			flip_screen_set(data & 0x01);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		/* bit 1 is NMI enable */
	
		nmi_enable = data & 0x02;
	
		/* bit 2 is IRQ enable */
	
		interrupt_enable_w(0, data & 0x04);
	
		/* bits 3 and 4 are coin counters */
	
		coin_counter_w(0, data & 0x08);
		coin_counter_w(1, data & 0x10);
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int offs = tile_index * 2;
		int attr = videoram.read(offs);
		int code = videoram.read(offs + 1)| ((attr & 0x10) << 4);
	//	int color = (attr & 0xf0) >> 4;
		int flags = ((attr & 0x80) ? TILE_FLIPX : 0) | ((attr & 0x40) ? TILE_FLIPY : 0);
	
		SET_TILE_INFO(0, code, 0, flags)
	} };
	
	public static VideoStartHandlerPtr video_start_yiear  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows,
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		return 0;
	} };
	
	static void yiear_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = spriteram_size - 2;offs >= 0;offs -= 2)
		{
			int attr = spriteram.read(offs);
			int code = spriteram_2.read(offs + 1)+ 256 * (attr & 0x01);
			int color = 0;
			int flipx = ~attr & 0x40;
			int flipy = attr & 0x80;
			int sy = 240 - spriteram.read(offs + 1);
			int sx = spriteram_2.read(offs);
	
			if (flip_screen())
			{
				sy = 240 - sy;
				flipy = NOT(flipy);
			}
	
			if (offs < 0x26)
			{
				sy++;	/* fix title screen & garbage at the bottom of the screen */
			}
	
			drawgfx(bitmap, Machine->gfx[1],
				code, color,
				flipx, flipy,
				sx, sy,
				Machine->visible_area,
				TRANSPARENCY_PEN, 0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_yiear  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		yiear_draw_sprites(bitmap);
	} };
}
