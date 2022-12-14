/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class redclash
{
	
	static int star_speed;
	static int gfxbank;
	
	static struct tilemap *fg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  I'm using the same palette conversion as Lady Bug, but the Zero Hour
	  schematics show a different resistor network.
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_redclash  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		for (i = 0;i < 32;i++)
		{
			int bit1,bit2,r,g,b;
	
	
			bit1 = (color_prom.read(i)>> 0) & 0x01;
			bit2 = (color_prom.read(i)>> 5) & 0x01;
			r = 0x47 * bit1 + 0x97 * bit2;
			bit1 = (color_prom.read(i)>> 2) & 0x01;
			bit2 = (color_prom.read(i)>> 6) & 0x01;
			g = 0x47 * bit1 + 0x97 * bit2;
			bit1 = (color_prom.read(i)>> 4) & 0x01;
			bit2 = (color_prom.read(i)>> 7) & 0x01;
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
	
	public static WriteHandlerPtr redclash_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(fg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr redclash_gfxbank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (gfxbank != (data & 0x01))
		{
			gfxbank = data & 0x01;
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	} };
	
	public static WriteHandlerPtr redclash_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		flip_screen_set(data & 0x01);
	} };
	
	/*
	star_speed:
	0 = unused
	1 = unused
	2 = forward fast
	3 = forward medium
	4 = forward slow
	5 = backwards slow
	6 = backwards medium
	7 = backwards fast
	*/
	public static WriteHandlerPtr redclash_star0_w = new WriteHandlerPtr() {public void handler(int offset, int data) star_speed = (star_speed & ~1) | ((data & 1) << 0); }
	public static WriteHandlerPtr redclash_star1_w = new WriteHandlerPtr() {public void handler(int offset, int data) star_speed = (star_speed & ~2) | ((data & 1) << 1); }
	public static WriteHandlerPtr redclash_star2_w = new WriteHandlerPtr() {public void handler(int offset, int data) star_speed = (star_speed & ~4) | ((data & 1) << 2); }
	public static WriteHandlerPtr redclash_star_reset_w = new WriteHandlerPtr() {public void handler(int offset, int data) }
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index);
		int color = (videoram.read(tile_index)& 0x70) >> 4; // ??
	
		SET_TILE_INFO(0, code, color, 0)
	} };
	
	public static VideoStartHandlerPtr video_start_redclash  = new VideoStartHandlerPtr() { public int handler(){
		fg_tilemap = tilemap_create(get_fg_tile_info, tilemap_scan_rows, 
			TILEMAP_TRANSPARENT, 8, 8, 32, 32);
	
		if ( !fg_tilemap )
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap, 0);
	
		return 0;
	} };
	
	static void redclash_draw_sprites( struct mame_bitmap *bitmap )
	{
		int i, offs;
	
		for (offs = spriteram_size - 0x20;offs >= 0;offs -= 0x20)
		{
			i = 0;
			while (i < 0x20 && spriteram.read(offs + i)!= 0)
				i += 4;
	
			while (i > 0)
			{
				i -= 4;
	
				if (spriteram.read(offs + i)& 0x80)
				{
					int color = spriteram.read(offs + i + 2)& 0x0f;
					int sx = spriteram.read(offs + i + 3);
					int sy = offs / 4 + (spriteram.read(offs + i)& 0x07);
	
	
					switch ((spriteram.read(offs + i)& 0x18) >> 3)
					{
						case 3:	/* 24x24 */
						{
							int code = ((spriteram.read(offs + i + 1)& 0xf0) >> 4) + ((gfxbank & 1) << 4);
	
							drawgfx(bitmap,Machine->gfx[3],
									code,
									color,
									0,0,
									sx,sy - 16,
									Machine->visible_area,TRANSPARENCY_PEN,0);
							/* wraparound */
							drawgfx(bitmap,Machine->gfx[3],
									code,
									color,
									0,0,
									sx - 256,sy - 16,
									Machine->visible_area,TRANSPARENCY_PEN,0);
							break;
						}
	
						case 2:	/* 16x16 */
							if (spriteram.read(offs + i)& 0x20)	/* zero hour spaceships */
							{
								int code = ((spriteram.read(offs + i + 1)& 0xf8) >> 3) + ((gfxbank & 1) << 5);
								int bank = (spriteram.read(offs + i + 1)& 0x02) >> 1;
	
								drawgfx(bitmap,Machine->gfx[4+bank],
										code,
										color,
										0,0,
										sx,sy - 16,
										Machine->visible_area,TRANSPARENCY_PEN,0);
							}
							else
							{
								int code = ((spriteram.read(offs + i + 1)& 0xf0) >> 4) + ((gfxbank & 1) << 4);
	
								drawgfx(bitmap,Machine->gfx[2],
										code,
										color,
										0,0,
										sx,sy - 16,
										Machine->visible_area,TRANSPARENCY_PEN,0);
							}
							break;
	
						case 1:	/* 8x8 */
							drawgfx(bitmap,Machine->gfx[1],
									spriteram.read(offs + i + 1),// + 4 * (spriteram.read(offs + i + 2)& 0x10),
									color,
									0,0,
									sx,sy - 16,
									Machine->visible_area,TRANSPARENCY_PEN,0);
							break;
	
						case 0:
	usrintf_showmessage("unknown sprite size 0");
							break;
					}
				}
			}
		}
	}
	
	static void redclash_draw_bullets( struct mame_bitmap *bitmap )
	{
		int offs;
	
		for (offs = 0; offs < 0x20; offs++)
		{
	//		sx = videoram.read(offs);
			int sx = 8 * offs + (videoram.read(offs)& 0x07);	/* ?? */
			int sy = 0xff - videoram.read(offs + 0x20);
	
			if (flip_screen())
			{
				sx = 240 - sx;
			}
	
			if (sx >= Machine->visible_area.min_x && sx <= Machine->visible_area.max_x &&
					sy >= Machine->visible_area.min_y && sy <= Machine->visible_area.max_y)
				plot_pixel(bitmap, sx, sy, Machine->pens[0x0e]);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_redclash  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		fillbitmap(bitmap, get_black_pen(), Machine.visible_area);
		redclash_draw_sprites(bitmap);
		redclash_draw_bullets(bitmap);
		tilemap_draw(bitmap, Machine.visible_area, fg_tilemap, 0, 0);
	} };
}
