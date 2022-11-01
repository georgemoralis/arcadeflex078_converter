/***************************************************************************

							  -= Paradise =-

					driver by	Luca Elia (l.elia@tin.it)


Note:	if MAME_DEBUG is defined, pressing Z with:

		Q 		shows the background layer
		W 		shows the midground layer
		E 		shows the foreground layer
		R 		shows the pixmap layer
		A 		shows sprites

		There are 4 Fixed 256 x 256 Layers.

		Background tiles are 8x8x4 with a register selecting which
		color code to use.

		midground and foreground tiles are 8x8x8 with no color code.
		Then there's a 16 color pixel layer.

		Bog standard 16x16x8 sprites, apparently with no color code nor flipping.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package vidhrdw;

public class paradise
{
	
	/* Variables that driver has access to: */
	
	data8_t *paradise_vram_0,*paradise_vram_1,*paradise_vram_2;
	
	/* Variables only used here */
	
	static data8_t paradise_palbank, paradise_priority;
	
	public static WriteHandlerPtr paradise_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		flip_screen_set(data ? 0 : 1);
	} };
	
	/* 800 bytes for red, followed by 800 bytes for green & 800 bytes for blue */
	public static WriteHandlerPtr paradise_palette_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		paletteram[offset] = data;
		offset %= 0x800;
		palette_set_color(offset,	paletteram[offset + 0x800 * 0],
									paletteram[offset + 0x800 * 1],
									paletteram[offset + 0x800 * 2]	);
	} };
	
	/***************************************************************************
	
										Tilemaps
	
		Offset:
	
		$000.b		Code (Low  Bits)
		$400.b		Code (High Bits)
	
	***************************************************************************/
	
	static struct tilemap *tilemap_0,*tilemap_1,*tilemap_2;
	
	/* Background */
	public static WriteHandlerPtr paradise_vram_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (paradise_vram_0[offset] != data)
		{
			paradise_vram_0[offset] = data;
			tilemap_mark_tile_dirty(tilemap_0, offset % 0x400);
		}
	} };
	
	/* 16 color tiles with paradise_palbank as color code */
	public static WriteHandlerPtr paradise_palbank_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int i;
		int bank1 = (data & 0x0e) | 1;
		int bank2 = (data & 0xf0);
	
		for (i = 0; i < 15; i++)
			palette_set_color(0x800+i,	paletteram[0x200 + bank2 + i + 0x800 * 0],
										paletteram[0x200 + bank2 + i + 0x800 * 1],
										paletteram[0x200 + bank2 + i + 0x800 * 2]	);
		if (paradise_palbank != bank1)
		{
			paradise_palbank = bank1;
			tilemap_mark_all_tiles_dirty(tilemap_0);
		}
	} };
	
	static void get_tile_info_0( int tile_index )
	{
		int code = paradise_vram_0[tile_index] + (paradise_vram_0[tile_index + 0x400] << 8);
		SET_TILE_INFO(1, code, paradise_palbank, 0);
	}
	
	
	/* Midground */
	public static WriteHandlerPtr paradise_vram_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (paradise_vram_1[offset] != data)
		{
			paradise_vram_1[offset] = data;
			tilemap_mark_tile_dirty(tilemap_1, offset % 0x400);
		}
	} };
	
	static void get_tile_info_1( int tile_index )
	{
		int code = paradise_vram_1[tile_index] + (paradise_vram_1[tile_index + 0x400] << 8);
		SET_TILE_INFO(2, code, 0, 0);
	}
	
	
	/* Foreground */
	public static WriteHandlerPtr paradise_vram_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (paradise_vram_2[offset] != data)
		{
			paradise_vram_2[offset] = data;
			tilemap_mark_tile_dirty(tilemap_2, offset % 0x400);
		}
	} };
	
	static void get_tile_info_2( int tile_index )
	{
		int code = paradise_vram_2[tile_index] + (paradise_vram_2[tile_index + 0x400] << 8);
		SET_TILE_INFO(3, code, 0, 0);
	}
	
	/* 256 x 256 bitmap. 4 bits per pixel so every byte encodes 2 pixels */
	
	public static WriteHandlerPtr paradise_pixmap_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int x,y;
	
		videoram[offset] = data;
	
		x = (offset & 0x7f) << 1;
		y = (offset >> 7);
	
		plot_pixel(tmpbitmap, x+0,y, 0x80f - (data >> 4));
		plot_pixel(tmpbitmap, x+1,y, 0x80f - (data & 0x0f));
	} };
	
	
	/***************************************************************************
	
								Vide Hardware Init
	
	***************************************************************************/
	
	public static VideoUpdateHandlerPtr paradise  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		tilemap_0 = tilemap_create(	get_tile_info_0, tilemap_scan_rows,
									TILEMAP_TRANSPARENT, 8,8, 0x20,0x20 );
	
		tilemap_1 = tilemap_create(	get_tile_info_1, tilemap_scan_rows,
									TILEMAP_TRANSPARENT, 8,8, 0x20,0x20 );
	
		tilemap_2 = tilemap_create(	get_tile_info_2, tilemap_scan_rows,
									TILEMAP_TRANSPARENT, 8,8, 0x20,0x20 );
	
		/* pixmap */
		tmpbitmap = auto_bitmap_alloc(Machine->drv->screen_width,Machine->drv->screen_height);
	
		/* paletteram and videoram (pixmap) are accessed through CPU ports, that don't
		   get memory automatically allocated for them */
		paletteram	=	auto_malloc(0x1800);
		videoram	=	auto_malloc(0x8000);
	
		if (!tilemap_0 || !tilemap_1 || !tilemap_2 || !tmpbitmap || !paletteram || !videoram)
			return 1;
	
		tilemap_set_transparent_pen(tilemap_0,0x0f);
		tilemap_set_transparent_pen(tilemap_1,0xff);
		tilemap_set_transparent_pen(tilemap_2,0xff);
		return 0;
	} };
	
	
	/***************************************************************************
	
								Sprites Drawing
	
	***************************************************************************/
	
	/* Sprites / Layers priority */
	public static WriteHandlerPtr paradise_priority_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		paradise_priority = data;
	} };
	
	static void draw_sprites(struct mame_bitmap *bitmap,const struct rectangle *cliprect)
	{
		int i;
		for (i = 0; i < spriteram_size ; i += 32)
		{
			int code	=	spriteram[i+0];	// Only 4 bytes out of 32 used?
			int x		=	spriteram[i+1];
			int y		=	spriteram[i+2];
			int attr	=	spriteram[i+3];
	
			int flipx	=	0;	// ?
			int flipy	=	0;
	
			if (flip_screen)	{	x = 0xf0 - x;	flipx = !flipx;
									y = 0xf0 - y;	flipy = !flipy;	}
	
			drawgfx(bitmap,Machine->gfx[0],
					code + (attr << 8),
					0,
					flipx, flipy,
					x,y,
					cliprect,TRANSPARENCY_PEN, 0xff );
		}
	}
	
	
	/***************************************************************************
	
									Screen Drawing
	
	***************************************************************************/
	
	public static VideoUpdateHandlerPtr paradise  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)
	{
		int layers_ctrl = -1;
	
	#ifdef MAME_DEBUG
	if (keyboard_pressed(KEYCODE_Z))
	{
		int mask = 0;
		if (keyboard_pressed(KEYCODE_Q))	mask |= 1;
		if (keyboard_pressed(KEYCODE_W))	mask |= 2;
		if (keyboard_pressed(KEYCODE_E))	mask |= 4;
		if (keyboard_pressed(KEYCODE_R))	mask |= 8;
		if (keyboard_pressed(KEYCODE_A))	mask |= 16;
		if (mask != 0) layers_ctrl &= mask;
	}
	#endif
	
		fillbitmap(bitmap,get_black_pen(),cliprect);
	
		if (!(paradise_priority & 4))	/* Screen blanking */
			return;
	
		if (paradise_priority & 1)
			if (layers_ctrl&16)	draw_sprites(bitmap,cliprect);
	
		if (layers_ctrl&1)	tilemap_draw(bitmap,cliprect, tilemap_0, 0,0);
		if (layers_ctrl&2)	tilemap_draw(bitmap,cliprect, tilemap_1, 0,0);
		if (layers_ctrl&4)	copybitmap(bitmap,tmpbitmap,flip_screen,flip_screen,0,0,cliprect,TRANSPARENCY_PEN, 0x80f);
	
		if (paradise_priority & 2)
		{
			if (!(paradise_priority & 1))
				if (layers_ctrl&16)	draw_sprites(bitmap,cliprect);
			if (layers_ctrl&8)	tilemap_draw(bitmap,cliprect, tilemap_2, 0,0);
		}
		else
		{
			if (layers_ctrl&8)	tilemap_draw(bitmap,cliprect, tilemap_2, 0,0);
			if (!(paradise_priority & 1))
				if (layers_ctrl&16)	draw_sprites(bitmap,cliprect);
		}
	} };
}
