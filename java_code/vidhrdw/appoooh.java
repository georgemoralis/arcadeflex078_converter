/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class appoooh
{
	
	unsigned char *appoooh_fg_videoram,*appoooh_fg_colorram;
	unsigned char *appoooh_bg_videoram,*appoooh_bg_colorram;
	
	#define CHR1_OFST 0x00  /* palette page of char set #1 */
	#define CHR2_OFST 0x10  /* palette page of char set #2 */
	
	static struct tilemap *fg_tilemap,*bg_tilemap;
	
	static int scroll_x;
	static int priority;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Palette information of appoooh is not known.
	
	  The palette decoder of Bank Panic was used for this driver.
	  Because these hardware is similar.
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_appoooh  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
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
			COLOR(0,i) = (*(color_prom++) & 0x0f)|CHR1_OFST;
	
		/* charset #2 lookup table */
		for (i = 0;i < TOTAL_COLORS(1);i++)
			COLOR(1,i) = (*(color_prom++) & 0x0f)|CHR2_OFST;
	
		/* TODO: the driver currently uses only 16 of the 32 color codes. */
		/* 16-31 might be unused, but there might be a palette bank selector */
		/* to use them somewhere in the game. */
	} };
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = appoooh_fg_videoram[tile_index] + 256 * ((appoooh_fg_colorram[tile_index]>>5) & 7);
	
		SET_TILE_INFO(
				0,
				code,
				appoooh_fg_colorram[tile_index]&0x0f,
				(appoooh_fg_colorram[tile_index] & 0x10 ) ? TILEMAP_FLIPX : 0
		);
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = appoooh_bg_videoram[tile_index] + 256 * ((appoooh_bg_colorram[tile_index]>>5) & 7);
	
		SET_TILE_INFO(
				1,
				code,
				appoooh_bg_colorram[tile_index]&0x0f,
				(appoooh_bg_colorram[tile_index] & 0x10 ) ? TILEMAP_FLIPX : 0
		);
	} };
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	public static VideoStartHandlerPtr video_start_appoooh  = new VideoStartHandlerPtr() { public int handler(){
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,     8,8,32,32);
	
		if (!bg_tilemap || !fg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,0);
		tilemap_set_scrolldy(fg_tilemap,8,8);
		tilemap_set_scrolldy(bg_tilemap,8,8);
	
		return 0;
	} };
	
	public static WriteHandlerPtr appoooh_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		scroll_x = data;
	} };
	
	
	public static WriteHandlerPtr appoooh_fg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (appoooh_fg_videoram[offset] != data)
		{
			appoooh_fg_videoram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr appoooh_fg_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (appoooh_fg_colorram[offset] != data)
		{
			appoooh_fg_colorram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr appoooh_bg_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (appoooh_bg_videoram[offset] != data)
		{
			appoooh_bg_videoram[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr appoooh_bg_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (appoooh_bg_colorram[offset] != data)
		{
			appoooh_bg_colorram[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap,offset);
		}
	} };
	
	public static WriteHandlerPtr appoooh_out_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bit 0 controls NMI */
		interrupt_enable_w(0,data & 0x01);
	
		/* bit 1 flip screen */
		flip_screen_set(data & 0x02);
	
		/* bits 2-3 unknown */
	
		/* bits 4-5 are playfield/sprite priority */
		/* TODO: understand how this works, currently the only thing I do is draw */
		/* the front layer behind sprites when priority == 0, and invert the sprite */
		/* order when priority == 1 */
		priority = (data & 0x30) >> 4;
	
		/* bit 6 ROM bank select */
		{
			unsigned char *RAM = memory_region(REGION_CPU1);
	
			cpu_setbank(1,&RAM[data&0x40 ? 0x10000 : 0x0a000]);
		}
	
		/* bit 7 unknown (used) */
	} };
	
	static void appoooh_draw_sprites(struct mame_bitmap *dest_bmp,
			const struct rectangle *cliprect,
	        const struct GfxElement *gfx,
	        unsigned char *sprite)
	{
		int offs;
	
		for (offs = 0x20 - 4;offs >= 0;offs -= 4)
		{
			int sy    = 240 - sprite[offs+0];
			int code  = (sprite[offs+1]>>2) + ((sprite[offs+2]>>5) & 0x07)*0x40;
			int color = sprite[offs+2]&0x0f;	/* TODO: bit 4 toggles continuously, what is it? */
			int sx    = sprite[offs+3];
			int flipx = sprite[offs+1]&0x01;
	
			if(sx>=248) sx -= 256;
	
			if (flip_screen())
			{
				sx = 239 - sx;
				sy = 239 - sy;
				flipx = NOT(flipx);
			}
			drawgfx( dest_bmp, gfx,
					code,
					color,
					flipx,flip_screen(),
					sx, sy,
					cliprect,
					TRANSPARENCY_PEN , 0);
		 }
	}
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	public static VideoUpdateHandlerPtr video_update_appoooh  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,bg_tilemap,0,0);
	
		if (priority == 0)	/* fg behind sprites */
			tilemap_draw(bitmap,cliprect,fg_tilemap,0,0);
	
		/* draw sprites */
		if (priority == 1)
		{
			/* sprite set #1 */
			appoooh_draw_sprites( bitmap, cliprect, Machine.gfx[2],spriteram);
			/* sprite set #2 */
			appoooh_draw_sprites( bitmap, cliprect, Machine.gfx[3],spriteram_2);
		}
		else
		{
			/* sprite set #2 */
			appoooh_draw_sprites( bitmap, cliprect, Machine.gfx[3],spriteram_2);
			/* sprite set #1 */
			appoooh_draw_sprites( bitmap, cliprect, Machine.gfx[2],spriteram);
		}
	
		if (priority != 0)	/* fg in front of sprites */
			tilemap_draw(bitmap,cliprect,fg_tilemap,0,0);
	} };
}
