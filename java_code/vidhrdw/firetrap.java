/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class firetrap
{
	
	
	
	unsigned char *firetrap_bg1videoram;
	unsigned char *firetrap_bg2videoram;
	unsigned char *firetrap_fgvideoram;
	
	static struct tilemap *fg_tilemap, *bg1_tilemap, *bg2_tilemap;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Fire Trap has one 256x8 and one 256x4 palette PROMs.
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
	
	public static PaletteInitHandlerPtr palette_init_firetrap  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
	
			bit0 = (color_prom.read(i)>> 0) & 0x01;
			bit1 = (color_prom.read(i)>> 1) & 0x01;
			bit2 = (color_prom.read(i)>> 2) & 0x01;
			bit3 = (color_prom.read(i)>> 3) & 0x01;
			r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(i)>> 4) & 0x01;
			bit1 = (color_prom.read(i)>> 5) & 0x01;
			bit2 = (color_prom.read(i)>> 6) & 0x01;
			bit3 = (color_prom.read(i)>> 7) & 0x01;
			g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(i + Machine.drv.total_colors)>> 0) & 0x01;
			bit1 = (color_prom.read(i + Machine.drv.total_colors)>> 1) & 0x01;
			bit2 = (color_prom.read(i + Machine.drv.total_colors)>> 2) & 0x01;
			bit3 = (color_prom.read(i + Machine.drv.total_colors)>> 3) & 0x01;
			b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			palette_set_color(i,r,g,b);
		}
	} };
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	UINT32 get_fg_memory_offset( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows )
	{
		return (row ^ 0x1f) + (col << 5);
	}
	
	UINT32 get_bg_memory_offset( UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows )
	{
		return ((row & 0x0f) ^ 0x0f) | ((col & 0x0f) << 4) |
				/* hole at bit 8 */
				((row & 0x10) << 5) | ((col & 0x10) << 6);
	}
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code, color;
	
		code = firetrap_fgvideoram[tile_index];
		color = firetrap_fgvideoram[tile_index + 0x400];
		SET_TILE_INFO(
				0,
				code | ((color & 0x01) << 8),
				color >> 4,
				0)
	} };
	
	INLINE void get_bg_tile_info(int tile_index, unsigned char *bgvideoram, int gfx_region)
	{
		int code, color;
	
		code = bgvideoram[tile_index];
		color = bgvideoram[tile_index + 0x100];
		SET_TILE_INFO(
				gfx_region,
				code + ((color & 0x03) << 8),
				(color & 0x30) >> 4,
				TILE_FLIPXY((color & 0x0c) >> 2))
	}
	
	public static GetTileInfoHandlerPtr get_bg1_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		get_bg_tile_info(tile_index, firetrap_bg1videoram, 1);
	} };
	
	public static GetTileInfoHandlerPtr get_bg2_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		get_bg_tile_info(tile_index, firetrap_bg2videoram, 2);
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_firetrap  = new VideoStartHandlerPtr() { public int handler(){
		fg_tilemap  = tilemap_create(get_fg_tile_info, get_fg_memory_offset,TILEMAP_TRANSPARENT, 8, 8,32,32);
		bg1_tilemap = tilemap_create(get_bg1_tile_info,get_bg_memory_offset,TILEMAP_TRANSPARENT,16,16,32,32);
		bg2_tilemap = tilemap_create(get_bg2_tile_info,get_bg_memory_offset,TILEMAP_OPAQUE,     16,16,32,32);
	
		if (!fg_tilemap || !bg1_tilemap || !bg2_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,0);
		tilemap_set_transparent_pen(bg1_tilemap,0);
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr firetrap_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		firetrap_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr firetrap_bg1videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		firetrap_bg1videoram[offset] = data;
		tilemap_mark_tile_dirty(bg1_tilemap,offset & 0x6ff);
	} };
	
	public static WriteHandlerPtr firetrap_bg2videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		firetrap_bg2videoram[offset] = data;
		tilemap_mark_tile_dirty(bg2_tilemap,offset & 0x6ff);
	} };
	
	
	public static WriteHandlerPtr firetrap_bg1_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static unsigned char scroll[2];
	
		scroll[offset] = data;
		tilemap_set_scrollx(bg1_tilemap,0,scroll[0] | (scroll[1] << 8));
	} };
	
	public static WriteHandlerPtr firetrap_bg1_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static unsigned char scroll[2];
	
		scroll[offset] = data;
		tilemap_set_scrolly(bg1_tilemap,0,-(scroll[0] | (scroll[1] << 8)));
	} };
	
	public static WriteHandlerPtr firetrap_bg2_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static unsigned char scroll[2];
	
		scroll[offset] = data;
		tilemap_set_scrollx(bg2_tilemap,0,scroll[0] | (scroll[1] << 8));
	} };
	
	public static WriteHandlerPtr firetrap_bg2_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static unsigned char scroll[2];
	
		scroll[offset] = data;
		tilemap_set_scrolly(bg2_tilemap,0,-(scroll[0] | (scroll[1] << 8)));
	} };
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect)
	{
		int offs;
	
	
		for (offs = 0;offs < spriteram_size; offs += 4)
		{
			int sx,sy,flipx,flipy,code,color;
	
	
			/* the meaning of bit 3 of [offs] is unknown */
	
			sy = spriteram.read(offs);
			sx = spriteram.read(offs + 2);
			code = spriteram.read(offs + 3)+ 4 * (spriteram.read(offs + 1)& 0xc0);
			color = ((spriteram.read(offs + 1)& 0x08) >> 2) | (spriteram.read(offs + 1)& 0x01);
			flipx = spriteram.read(offs + 1)& 0x04;
			flipy = spriteram.read(offs + 1)& 0x02;
			if (flip_screen())
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = NOT(flipx);
				flipy = NOT(flipy);
			}
	
			if (spriteram.read(offs + 1)& 0x10)	/* double width */
			{
				if (flip_screen()) sy -= 16;
	
				drawgfx(bitmap,Machine->gfx[3],
						code & ~1,
						color,
						flipx,flipy,
						sx,flipy ? sy : sy + 16,
						cliprect,TRANSPARENCY_PEN,0);
				drawgfx(bitmap,Machine->gfx[3],
						code | 1,
						color,
						flipx,flipy,
						sx,flipy ? sy + 16 : sy,
						cliprect,TRANSPARENCY_PEN,0);
	
				/* redraw with wraparound */
				drawgfx(bitmap,Machine->gfx[3],
						code & ~1,
						color,
						flipx,flipy,
						sx - 256,flipy ? sy : sy + 16,
						cliprect,TRANSPARENCY_PEN,0);
				drawgfx(bitmap,Machine->gfx[3],
						code | 1,
						color,
						flipx,flipy,
						sx - 256,flipy ? sy + 16 : sy,
						cliprect,TRANSPARENCY_PEN,0);
			}
			else
			{
				drawgfx(bitmap,Machine->gfx[3],
						code,
						color,
						flipx,flipy,
						sx,sy,
						cliprect,TRANSPARENCY_PEN,0);
	
				/* redraw with wraparound */
				drawgfx(bitmap,Machine->gfx[3],
						code,
						color,
						flipx,flipy,
						sx - 256,sy,
						cliprect,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_firetrap  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,bg2_tilemap,0,0);
		tilemap_draw(bitmap,cliprect,bg1_tilemap,0,0);
		draw_sprites(bitmap,cliprect);
		tilemap_draw(bitmap,cliprect,fg_tilemap,0,0);
	} };
}
