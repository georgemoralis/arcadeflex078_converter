/***************************************************************************

	Midway MCR-III system

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class mcr3
{
	
	
	
	/*************************************
	 *
	 *	Global variables
	 *
	 *************************************/
	
	/* Spy Hunter hardware extras */
	UINT8 spyhunt_sprite_color_mask;
	INT16 spyhunt_scrollx, spyhunt_scrolly;
	INT16 spyhunt_scroll_offset;
	
	UINT8 *spyhunt_alpharam;
	size_t spyhunt_alpharam_size;
	
	
	
	/*************************************
	 *
	 *	Local variables
	 *
	 *************************************/
	
	static struct tilemap *bg_tilemap;
	static struct tilemap *alpha_tilemap;
	
	
	
	/*************************************
	 *
	 *	Tilemap callbacks
	 *
	 *************************************/
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int data = videoram.read(tile_index * 2)| (videoram.read(tile_index * 2 + 1)<< 8);
		int code = (data & 0x3ff) | ((data >> 4) & 0x400);
		int color = (data >> 12) & 3;
		SET_TILE_INFO(0, code, color, TILE_FLIPYX((data >> 10) & 3));
	} };
	
	
	public static GetTileInfoHandlerPtr mcrmono_get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int data = videoram.read(tile_index * 2)| (videoram.read(tile_index * 2 + 1)<< 8);
		int code = (data & 0x3ff) | ((data >> 4) & 0x400);
		int color = ((data >> 12) & 3) ^ 3;
		SET_TILE_INFO(0, code, color, TILE_FLIPYX((data >> 10) & 3));
	} };
	
	
	static UINT32 spyhunt_bg_scan(UINT32 col, UINT32 row, UINT32 num_cols, UINT32 num_rows)
	{
		/* logical (col,row) -> memory offset */
		return (row & 0x0f) | ((col & 0x3f) << 4) | ((row & 0x10) << 6);
	}
	
	
	public static GetTileInfoHandlerPtr spyhunt_get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int data = videoram.read(tile_index);
		int code = (data & 0x3f) | ((data >> 1) & 0x40);
		SET_TILE_INFO(0, code, 0, (data & 0x40) ? TILE_FLIPY : 0);
	} };
	
	
	public static GetTileInfoHandlerPtr spyhunt_get_alpha_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		SET_TILE_INFO(2, spyhunt_alpharam[tile_index], 0, 0);
	} };
	
	
	
	/*************************************
	 *
	 *	Video startup
	 *
	 *************************************/
	
	public static VideoStartHandlerPtr video_start_mcr3  = new VideoStartHandlerPtr() { public int handler(){
		/* initialize the background tilemap */
		bg_tilemap = tilemap_create(get_bg_tile_info, tilemap_scan_rows, TILEMAP_OPAQUE, 16,16, 32,30);
		if (!bg_tilemap)
			return 1;
		return 0;
	} };
	
	
	public static VideoStartHandlerPtr video_start_mcrmono  = new VideoStartHandlerPtr() { public int handler(){
		/* initialize the background tilemap */
		bg_tilemap = tilemap_create(mcrmono_get_bg_tile_info, tilemap_scan_rows, TILEMAP_OPAQUE, 16,16, 32,30);
		if (!bg_tilemap)
			return 1;
		return 0;
	} };
	
	
	public static VideoStartHandlerPtr video_start_spyhunt  = new VideoStartHandlerPtr() { public int handler(){
		/* initialize the background tilemap */
		bg_tilemap = tilemap_create(spyhunt_get_bg_tile_info, spyhunt_bg_scan, TILEMAP_OPAQUE, 64,32, 64,32);
		if (!bg_tilemap)
			return 1;
	
		/* initialize the text tilemap */
		alpha_tilemap = tilemap_create(spyhunt_get_alpha_tile_info, tilemap_scan_cols, TILEMAP_TRANSPARENT, 16,16, 32,32);
		if (!alpha_tilemap)
			return 1;
		tilemap_set_transparent_pen(alpha_tilemap, 0);
		tilemap_set_scrollx(alpha_tilemap, 0, 16);
		return 0;
	} };
	
	
	
	/*************************************
	 *
	 *	Palette RAM writes
	 *
	 *************************************/
	
	public static WriteHandlerPtr mcr3_paletteram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		int r, g, b;
	
		paletteram.write(offset,data);
		offset &= 0x7f;
	
		/* high bit of red comes from low bit of address */
		r = ((offset & 1) << 2) + (data >> 6);
		g = (data >> 0) & 7;
		b = (data >> 3) & 7;
	
		/* up to 8 bits */
		r = (r << 5) | (r << 2) | (r >> 1);
		g = (g << 5) | (g << 2) | (g >> 1);
		b = (b << 5) | (b << 2) | (b >> 1);
	
		palette_set_color(offset / 2, r, g, b);
	} };
	
	
	
	/*************************************
	 *
	 *	Video RAM writes
	 *
	 *************************************/
	
	public static WriteHandlerPtr mcr3_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		videoram.write(offset,data);
		tilemap_mark_tile_dirty(bg_tilemap, offset / 2);
	} };
	
	
	public static WriteHandlerPtr spyhunt_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		videoram.write(offset,data);
		tilemap_mark_tile_dirty(bg_tilemap, offset);
	} };
	
	
	public static WriteHandlerPtr spyhunt_alpharam_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		spyhunt_alpharam[offset] = data;
		tilemap_mark_tile_dirty(alpha_tilemap, offset);
	} };
	
	
	
	/*************************************
	 *
	 *	Sprite update
	 *
	 *************************************/
	
	void mcr3_update_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect, int color_mask, int code_xor, int dx, int dy)
	{
		int offs;
	
		fillbitmap(priority_bitmap, 1, cliprect);
	
		/* loop over sprite RAM */
		for (offs = spriteram_size - 4; offs >= 0; offs -= 4)
		{
			int code, color, flipx, flipy, sx, sy, flags;
	
			/* skip if zero */
			if (spriteram[offs] == 0)
				continue;
	
			/* extract the bits of information */
			flags = spriteram.read(offs + 1);
			code = spriteram.read(offs + 2)+ 256 * ((flags >> 3) & 0x01);
			color = ~flags & color_mask;
			flipx = flags & 0x10;
			flipy = flags & 0x20;
			sx = (spriteram.read(offs + 3)- 3) * 2;
			sy = (241 - spriteram.read(offs)) * 2;
	
			code ^= code_xor;
	
			sx += dx;
			sy += dy;
	
			/* sprites use color 0 for background pen and 8 for the 'under tile' pen.
				The color 8 is used to cover over other sprites. */
			if (!mcr_cocktail_flip)
			{
				/* first draw the sprite, visible */
				pdrawgfx(bitmap, Machine->gfx[1], code, color, flipx, flipy, sx, sy,
						cliprect, TRANSPARENCY_PENS, 0x0101, 0x00);
	
				/* then draw the mask, behind the background but obscuring following sprites */
				pdrawgfx(bitmap, Machine->gfx[1], code, color, flipx, flipy, sx, sy,
						cliprect, TRANSPARENCY_PENS, 0xfeff, 0x02);
			}
			else
			{
				/* first draw the sprite, visible */
				pdrawgfx(bitmap, Machine->gfx[1], code, color, NOT(flipx), NOT(flipy), 480 - sx, 452 - sy,
						cliprect, TRANSPARENCY_PENS, 0x0101, 0x00);
	
				/* then draw the mask, behind the background but obscuring following sprites */
				pdrawgfx(bitmap, Machine->gfx[1], code, color, NOT(flipx), NOT(flipy), 480 - sx, 452 - sy,
						cliprect, TRANSPARENCY_PENS, 0xfeff, 0x02);
			}
		}
	}
	
	
	
	/*************************************
	 *
	 *	Generic MCR3 redraw
	 *
	 *************************************/
	
	public static VideoUpdateHandlerPtr video_update_mcr3  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		/* update the flip state */
		tilemap_set_flip(bg_tilemap, mcr_cocktail_flip ? (TILEMAP_FLIPX | TILEMAP_FLIPY) : 0);
	
		/* draw the background */
		tilemap_draw(bitmap, cliprect, bg_tilemap, 0, 0);
	
		/* draw the sprites */
		mcr3_update_sprites(bitmap, cliprect, 0x03, 0, 0, 0);
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_spyhunt  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		/* for every character in the Video RAM, check if it has been modified */
		/* since last time and update it accordingly. */
		tilemap_set_scrollx(bg_tilemap, 0, spyhunt_scrollx * 2 + spyhunt_scroll_offset);
		tilemap_set_scrolly(bg_tilemap, 0, spyhunt_scrolly * 2);
		tilemap_draw(bitmap, cliprect, bg_tilemap, 0, 0);
	
		/* draw the sprites */
		mcr3_update_sprites(bitmap, cliprect, spyhunt_sprite_color_mask, 0x80, -12, 0);
	
		/* render any characters on top */
		tilemap_draw(bitmap, cliprect, alpha_tilemap, 0, 0);
	} };
	
	
	
	/*************************************
	 *
	 *	Spy Hunter-specific color PROM decoder
	 *
	 *************************************/
	
	public static PaletteInitHandlerPtr palette_init_spyhunt  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		/* add some colors for the alpha RAM */
		palette_set_color(4*16+0,0x00,0x00,0x00);
		palette_set_color(4*16+1,0x00,0xff,0x00);
		palette_set_color(4*16+2,0x00,0x00,0xff);
		palette_set_color(4*16+3,0xff,0xff,0xff);
	} };
}
