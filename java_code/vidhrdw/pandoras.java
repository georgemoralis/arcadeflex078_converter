/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class pandoras
{
	
	static int flipscreen;
	static struct tilemap *layer0;
	
	/***********************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Pandora's Palace has one 32x8 palette PROM and two 256x4 lookup table
	  PROMs (one for characters, one for sprites).
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
	public static PaletteInitHandlerPtr palette_init_pandoras  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
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
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_tile_info0 = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		unsigned char attr = colorram.read(tile_index);
		SET_TILE_INFO(
				0,
				videoram.read(tile_index)+ ((attr & 0x10) << 4),
				attr & 0x0f,
				TILE_FLIPYX((attr & 0xc0) >> 6))
		tile_info.priority = (attr & 0x20) >> 5;
	} };
	
	/***************************************************************************
	
		Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_pandoras  = new VideoStartHandlerPtr() { public int handler(){
		layer0 = tilemap_create(get_tile_info0,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
	
		if (!layer0)
			return 1;
	
		return 0;
	} };
	
	/***************************************************************************
	
	  Memory Handlers
	
	***************************************************************************/
	
	public static ReadHandlerPtr pandoras_vram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return videoram.read(offset);
	} };
	
	public static ReadHandlerPtr pandoras_cram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return colorram.read(offset);
	} };
	
	public static WriteHandlerPtr pandoras_vram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			tilemap_mark_tile_dirty(layer0,offset);
			videoram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr pandoras_cram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (colorram.read(offset)!= data)
		{
			tilemap_mark_tile_dirty(layer0,offset);
			colorram.write(offset,data);
		}
	} };
	
	public static WriteHandlerPtr pandoras_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		tilemap_set_scrolly(layer0,0,data);
	} };
	
	public static WriteHandlerPtr pandoras_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		flipscreen = data;
		tilemap_set_flip(ALL_TILEMAPS, flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	} };
	
	/***************************************************************************
	
	  Screen Refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect, unsigned char* sr)
	{
		int offs;
	
		for (offs = 0; offs < 0x100; offs += 4)
		{
			int sx = sr[offs + 1];
			int sy = 240 - sr[offs];
			int nflipx = sr[offs + 3] & 0x40;
			int nflipy = sr[offs + 3] & 0x80;
	
			drawgfx(bitmap,Machine->gfx[1],
				sr[offs + 2],
				sr[offs + 3] & 0x0f,
				!nflipx,!nflipy,
				sx,sy,
				cliprect,TRANSPARENCY_COLOR,0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_pandoras  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw( bitmap,cliprect, layer0, 1 ,0);
		draw_sprites( bitmap,cliprect, &pandoras_sharedram[0x800] );
		tilemap_draw( bitmap,cliprect, layer0, 0 ,0);
	} };
}
