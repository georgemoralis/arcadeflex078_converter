/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class ironhors
{
	
	UINT8 *ironhors_scroll;
	static int palettebank, charbank, spriterambank;
	
	static struct tilemap *bg_tilemap;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Iron Horse has three 256x4 palette PROMs (one per gun) and two 256x4
	  lookup table PROMs (one for characters, one for sprites).
	  I don't know for sure how the palette PROMs are connected to the RGB
	  output, but it's probably the usual:
	
	  bit 3 -- 220 ohm resistor  -- RED/GREEN/BLUE
	        -- 470 ohm resistor  -- RED/GREEN/BLUE
	        -- 1  kohm resistor  -- RED/GREEN/BLUE
	  bit 0 -- 2.2kohm resistor  -- RED/GREEN/BLUE
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_ironhors  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
	
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(Machine.drv.total_colors)>> 3) & 0x01;
			g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(2*Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(2*Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(2*Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(2*Machine.drv.total_colors)>> 3) & 0x01;
			b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
		color_prom += 2*Machine.drv.total_colors;
		/* color_prom now points to the beginning of the character lookup table */
	
	
		/* there are eight 32 colors palette banks; sprites use colors 0-15 and */
		/* characters 16-31 of each bank. */
		for (i = 0;i < TOTAL_COLORS(0)/8;i++)
		{
			int j;
	
	
			for (j = 0;j < 8;j++)
				COLOR(0,i + j * TOTAL_COLORS(0)/8) = (color_prom.read()& 0x0f) + 32 * j + 16;
	
			color_prom++;
		}
	
		for (i = 0;i < TOTAL_COLORS(1)/8;i++)
		{
			int j;
	
	
			for (j = 0;j < 8;j++)
				COLOR(1,i + j * TOTAL_COLORS(1)/8) = (color_prom.read()& 0x0f) + 32 * j;
	
			color_prom++;
		}
	} };
	
	public static WriteHandlerPtr ironhors_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr ironhors_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			colorram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap, offset);
		}
	} };
	
	public static WriteHandlerPtr ironhors_charbank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (charbank != (data & 0x03))
		{
			charbank = data & 0x03;
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		spriterambank = data & 0x08;
	
		/* other bits unknown */
	} };
	
	public static WriteHandlerPtr ironhors_palettebank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (palettebank != (data & 0x07))
		{
			palettebank = data & 0x07;
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		coin_counter_w(0, data & 0x10);
		coin_counter_w(1, data & 0x20);
	
		/* bit 6 unknown - set after game over */
	
		if (data & 0x88) usrintf_showmessage("ironhors_palettebank_w %02x",data);
	} };
	
	public static WriteHandlerPtr ironhors_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (flip_screen() != (~data & 0x08))
		{
			flip_screen_set(~data & 0x08);
			tilemap_mark_all_tiles_dirty(ALL_TILEMAPS);
		}
	
		/* other bits are used too, but unknown */
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x40) << 2) +
			((colorram.read(tile_index)& 0x20) << 4) + (charbank << 10);
		int color = (colorram.read(tile_index)& 0x0f) + 16 * palettebank;
		int flags = ((colorram.read(tile_index)& 0x10) ? TILE_FLIPX : 0) |
			((colorram.read(tile_index)& 0x20) ? TILE_FLIPY : 0);
	
		SET_TILE_INFO(0, code, color, flags)
	} };
	
	public static VideoStartHandlerPtr video_start_ironhors  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows,
			TILEMAP_OPAQUE, 8, 8, 32, 32);
	
		if ( !bg_tilemap )
			return 1;
	
		tilemap_set_scroll_rows(bg_tilemap, 32);
	
		return 0;
	} };
	
	static void ironhors_draw_sprites( struct mame_bitmap *bitmap )
	{
		int offs;
		UINT8 *sr;
	
		if (spriterambank != 0)
			sr = spriteram;
		else
			sr = spriteram_2;
	
		for (offs = 0;offs < spriteram_size;offs += 5)
		{
			int sx = sr[offs+3];
			int sy = sr[offs+2];
			int flipx = sr[offs+4] & 0x20;
			int flipy = sr[offs+4] & 0x40;
			int code = (sr[offs] << 2) + ((sr[offs+1] & 0x03) << 10) + ((sr[offs+1] & 0x0c) >> 2);
			int color = ((sr[offs+1] & 0xf0)>>4) + 16 * palettebank;
		//	int mod = flip_screen() ? -8 : 8;
	
			if (flip_screen())
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = NOT(flipx);
				flipy = NOT(flipy);
			}
	
			switch (sr[offs+4] & 0x0c)
			{
				case 0x00:	/* 16x16 */
					drawgfx(bitmap,Machine->gfx[1],
							code/4,
							color,
							flipx,flipy,
							sx,sy,
							Machine->visible_area,TRANSPARENCY_PEN,0);
					break;
	
				case 0x04:	/* 16x8 */
					{
						if (flip_screen()) sy += 8; // this fixes the train wheels' position
	
						drawgfx(bitmap,Machine->gfx[2],
								code & ~1,
								color,
								flipx,flipy,
								flipx?sx+8:sx,sy,
								Machine->visible_area,TRANSPARENCY_PEN,0);
						drawgfx(bitmap,Machine->gfx[2],
								code | 1,
								color,
								flipx,flipy,
								flipx?sx:sx+8,sy,
								Machine->visible_area,TRANSPARENCY_PEN,0);
					}
					break;
	
				case 0x08:	/* 8x16 */
					{
						drawgfx(bitmap,Machine->gfx[2],
								code & ~2,
								color,
								flipx,flipy,
								sx,flipy?sy+8:sy,
								Machine->visible_area,TRANSPARENCY_PEN,0);
						drawgfx(bitmap,Machine->gfx[2],
								code | 2,
								color,
								flipx,flipy,
								sx,flipy?sy:sy+8,
								Machine->visible_area,TRANSPARENCY_PEN,0);
					}
					break;
	
				case 0x0c:	/* 8x8 */
					{
						drawgfx(bitmap,Machine->gfx[2],
								code,
								color,
								flipx,flipy,
								sx,sy,
								Machine->visible_area,TRANSPARENCY_PEN,0);
					}
					break;
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_ironhors  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int row;
	
		for (row = 0; row < 32; row++)
			tilemap_set_scrollx(bg_tilemap, row, ironhors_scroll[row]);
	
		tilemap_draw(bitmap, Machine.visible_area, bg_tilemap, 0, 0);
		ironhors_draw_sprites(bitmap);
	} };
}
