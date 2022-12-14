/***************************************************************************

Knuckle Joe - (c) 1985 Taito Corporation

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class kncljoe
{
	
	static struct tilemap *bg_tilemap;
	static int tile_bank,sprite_bank;
	static int flipscreen;
	
	UINT8 *kncljoe_scrollregs;
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	***************************************************************************/
	
	public static PaletteInitHandlerPtr palette_init_kncljoe  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
	
		for (i = 0;i < 128;i++)
		{
			int bit0,bit1,bit2,bit3,r,g,b;
	
			bit0 = (color_prom.read(0)>> 0) & 0x01;
			bit1 = (color_prom.read(0)>> 1) & 0x01;
			bit2 = (color_prom.read(0)>> 2) & 0x01;
			bit3 = (color_prom.read(0)>> 3) & 0x01;
			r = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(0x100)>> 0) & 0x01;
			bit1 = (color_prom.read(0x100)>> 1) & 0x01;
			bit2 = (color_prom.read(0x100)>> 2) & 0x01;
			bit3 = (color_prom.read(0x100)>> 3) & 0x01;
			g = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
			bit0 = (color_prom.read(0x200)>> 0) & 0x01;
			bit1 = (color_prom.read(0x200)>> 1) & 0x01;
			bit2 = (color_prom.read(0x200)>> 2) & 0x01;
			bit3 = (color_prom.read(0x200)>> 3) & 0x01;
			b = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	
		color_prom += 2*256 + 128;	/* bottom half is not used */
	
		for (i = 0;i < 16;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
			/* red component */
			bit0 = 0;
			bit1 = (color_prom.read()>> 6) & 0x01;
			bit2 = (color_prom.read()>> 7) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (color_prom.read()>> 3) & 0x01;
			bit1 = (color_prom.read()>> 4) & 0x01;
			bit2 = (color_prom.read()>> 5) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = (color_prom.read()>> 0) & 0x01;
			bit1 = (color_prom.read()>> 1) & 0x01;
			bit2 = (color_prom.read()>> 2) & 0x01;
			b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			palette_set_color(i+128,r,g,b);
			color_prom ++;
		}
	
		color_prom += 16;	/* bottom half is not used */
	
		/* sprite lookup table */
		for (i = 0;i < 128;i++)
		{
			COLOR(1,i) = 128 + (*(color_prom++) & 0x0f);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int attr = videoram.read(2*tile_index+1);
		int code = videoram.read(2*tile_index)+ ((attr & 0xc0) << 2) + (tile_bank << 10);
	
		SET_TILE_INFO(
				0,
				code,
				attr & 0xf,
				TILE_FLIPXY((attr & 0x30) >> 4))
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_kncljoe  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,64,32);
	
		if (!bg_tilemap)
			return 1;
	
		tilemap_set_scroll_rows(bg_tilemap,4);
	
		tile_bank = sprite_bank = flipscreen = 0;
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr kncljoe_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (videoram.read(offset)!= data)
		{
			videoram.write(offset,data);
			tilemap_mark_tile_dirty(bg_tilemap,offset/2);
		}
	} };
	
	public static WriteHandlerPtr kncljoe_control_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		int i;
	
		switch(offset)
		{
			/*
				0x01	screen flip
				0x02	coin counter#1
				0x04	sprite bank
				0x10	character bank
				0x20	coin counter#2
	
				reset when IN0 - Coin 1 goes low (active)
				set after IN0 - Coin 1 goes high AND the credit has been added
			*/
			case 0:
				flipscreen = data & 0x01;
				tilemap_set_flip(ALL_TILEMAPS,flipscreen ? TILEMAP_FLIPX : TILEMAP_FLIPY);
	
				coin_counter_w(0,data & 0x02);
				coin_counter_w(1,data & 0x20);
	
				i = (data & 0x10) >> 4;
				if (tile_bank != i)
				{
					tile_bank = i;
					tilemap_mark_all_tiles_dirty(bg_tilemap);
				}
	
				i = (data & 0x04) >> 2;
				if (sprite_bank != i)
				{
					sprite_bank = i;
					memset(memory_region(REGION_CPU1)+0xf100, 0, 0x180);
				}
			break;
			case 1:
				// ???
			break;
			case 2:
				// ???
			break;
		}
	} };
	
	public static WriteHandlerPtr kncljoe_scroll_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		int scrollx;
	
		kncljoe_scrollregs[offset] = data;
		scrollx = kncljoe_scrollregs[0] | kncljoe_scrollregs[1]<<8;
		tilemap_set_scrollx(bg_tilemap,0,scrollx);
		tilemap_set_scrollx(bg_tilemap,1,scrollx);
		tilemap_set_scrollx(bg_tilemap,2,scrollx);
		tilemap_set_scrollx(bg_tilemap,3,0);
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect )
	{
		struct rectangle clip = *cliprect;
		const struct GfxElement *gfx = Machine->gfx[1 + sprite_bank];
		int i, j, pribase[4]={0x0180, 0x0080, 0x0100, 0x0000};
	
		/* score covers sprites */
		if (flipscreen)
		{
			if (clip.max_y > Machine->visible_area.max_y - 64)
				clip.max_y = Machine->visible_area.max_y - 64;
		}
		else
		{
			if (clip.min_y < Machine->visible_area.min_y + 64)
				clip.min_y = Machine->visible_area.min_y + 64;
		}
	
		for (i=0; i<4; i++)
		for (j=0x7c; j>=0; j-=4)
		{
			int offs = pribase[i] + j;
			int sy = spriteram.read(offs);
			int sx = spriteram.read(offs+3);
			int code = spriteram.read(offs+2);
			int attr = spriteram.read(offs+1);
			int flipx = attr & 0x40;
			int flipy = !(attr & 0x80);
			int color = attr & 0x0f;
	
			if (attr & 0x10) code += 512;
			if (attr & 0x20) code += 256;
	
			if (flipscreen)
			{
				flipx = NOT(flipx);
				flipy = NOT(flipy);
				sx = 240 - sx;
				sy = 240 - sy;
			}
	
			if (sx >= 256-8) sx -= 256;
	
			drawgfx(bitmap,gfx,
					code,
					color,
					flipx,flipy,
					sx,sy,
					&clip,TRANSPARENCY_PEN,0);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_kncljoe  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,bg_tilemap,0,0);
		draw_sprites(bitmap,cliprect);
	} };
}
