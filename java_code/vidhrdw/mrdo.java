/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class mrdo
{
	
	
	unsigned char *mrdo_bgvideoram,*mrdo_fgvideoram;
	static struct tilemap *bg_tilemap,*fg_tilemap;
	static int flipscreen;
	
	
	
	/***************************************************************************
	
	  Convert the color PROMs into a more useable format.
	
	  Mr. Do! has two 32 bytes palette PROM and a 32 bytes sprite color lookup
	  table PROM.
	  The palette PROMs are connected to the RGB output this way:
	
	  U2:
	  bit 7 -- unused
	        -- unused
	        -- 100 ohm resistor  -diode- BLUE
	        --  75 ohm resistor  -diode- BLUE
	        -- 100 ohm resistor  -diode- GREEN
	        --  75 ohm resistor  -diode- GREEN
	        -- 100 ohm resistor  -diode- RED
	  bit 0 --  75 ohm resistor  -diode- RED
	
	  T2:
	  bit 7 -- unused
	        -- unused
	        -- 150 ohm resistor  -diode- BLUE
	        -- 120 ohm resistor  -diode- BLUE
	        -- 150 ohm resistor  -diode- GREEN
	        -- 120 ohm resistor  -diode- GREEN
	        -- 150 ohm resistor  -diode- RED
	  bit 0 -- 120 ohm resistor  -diode- RED
	
	  200 ohm pulldown on all three components
	
	***************************************************************************/
	public static PaletteInitHandlerPtr palette_init_mrdo  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
		#define TOTAL_COLORS(gfxn) (Machine.gfx[gfxn].total_colors * Machine.gfx[gfxn].color_granularity)
		#define COLOR(gfxn,offs) (colortable[Machine.drv.gfxdecodeinfo[gfxn].color_codes_start + offs])
	
		const int R1 = 150;
		const int R2 = 120;
		const int R3 = 100;
		const int R4 = 75;
		const int pull = 200;
		float pot[16];
		int weight[16];
		const float potadjust = 0.2;	/* diode voltage drop */
	
		for (i = 15;i >= 0;i--)
		{
			float par = 0;
	
			if (i & 1) par += 1.0/R1;
			if (i & 2) par += 1.0/R2;
			if (i & 4) par += 1.0/R3;
			if (i & 8) par += 1.0/R4;
			if (par)
			{
				par = 1/par;
				pot[i] = pull/(pull+par) - potadjust;
			}
			else pot[i] = 0;
	
			weight[i] = 255 * pot[i] / pot[15];
		}
	
		for (i = 0;i < 256;i++)
		{
			int a1,a2;
			int bits0,bits2,r,g,b;
	
			a1 = ((i >> 3) & 0x1c) + (i & 0x03) + 32;
			a2 = ((i >> 0) & 0x1c) + (i & 0x03);
	
			bits0 = (color_prom.read(a1)>> 0) & 0x03;
			bits2 = (color_prom.read(a2)>> 0) & 0x03;
			r = weight[bits0 + (bits2 << 2)];
			bits0 = (color_prom.read(a1)>> 2) & 0x03;
			bits2 = (color_prom.read(a2)>> 2) & 0x03;
			g = weight[bits0 + (bits2 << 2)];
			bits0 = (color_prom.read(a1)>> 4) & 0x03;
			bits2 = (color_prom.read(a2)>> 4) & 0x03;
			b = weight[bits0 + (bits2 << 2)];
			palette_set_color(i,r,g,b);
		}
	
		color_prom += 64;
	
		/* sprites */
		for (i = 0;i < TOTAL_COLORS(2);i++)
		{
			int bits;
	
			if (i < 32)
				bits = color_prom.read(i)& 0x0f;		/* low 4 bits are for sprite color n */
			else
				bits = color_prom.read(i & 0x1f)>> 4;	/* high 4 bits are for sprite color n + 8 */
	
			COLOR(2,i) = bits + ((bits & 0x0c) << 3);
		}
	} };
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		unsigned char attr = mrdo_bgvideoram[tile_index];
		SET_TILE_INFO(
				1,
				mrdo_bgvideoram[tile_index+0x400] + ((attr & 0x80) << 1),
				attr & 0x3f,
				(attr & 0x40) ? TILE_IGNORE_TRANSPARENCY : 0)
	} };
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		unsigned char attr = mrdo_fgvideoram[tile_index];
		SET_TILE_INFO(
				0,
				mrdo_fgvideoram[tile_index+0x400] + ((attr & 0x80) << 1),
				attr & 0x3f,
				(attr & 0x40) ? TILE_IGNORE_TRANSPARENCY : 0)
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_mrdo  = new VideoStartHandlerPtr() { public int handler(){
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
	
		if (!bg_tilemap || !fg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(bg_tilemap,0);
		tilemap_set_transparent_pen(fg_tilemap,0);
	
		return 0;
	} };
	
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr mrdo_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (mrdo_bgvideoram[offset] != data)
		{
			mrdo_bgvideoram[offset] = data;
			tilemap_mark_tile_dirty(bg_tilemap,offset & 0x3ff);
		}
	} };
	
	public static WriteHandlerPtr mrdo_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (mrdo_fgvideoram[offset] != data)
		{
			mrdo_fgvideoram[offset] = data;
			tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
		}
	} };
	
	
	public static WriteHandlerPtr mrdo_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		tilemap_set_scrollx(bg_tilemap,0,data);
	} };
	
	public static WriteHandlerPtr mrdo_scrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* This is NOT affected by flipscreen (so stop it happening) */
	
		if (flipscreen) tilemap_set_scrolly(bg_tilemap,0,((256-data) & 0xff));
		else tilemap_set_scrolly(bg_tilemap,0,data);
	} };
	
	
	public static WriteHandlerPtr mrdo_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bits 1-3 control the playfield priority, but they are not used by */
		/* Mr. Do! so we don't emulate them */
	
		flipscreen = data & 0x01;
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap,const struct rectangle *cliprect)
	{
		int offs;
	
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			if (spriteram.read(offs + 1)!= 0)
			{
				drawgfx(bitmap,Machine->gfx[2],
						spriteram.read(offs),spriteram.read(offs + 2)& 0x0f,
						spriteram.read(offs + 2)& 0x10,spriteram.read(offs + 2)& 0x20,
						spriteram.read(offs + 3),256 - spriteram.read(offs + 1),
						cliprect,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_mrdo  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		fillbitmap(bitmap,Machine.pens[0],cliprect);
		tilemap_draw(bitmap,cliprect,bg_tilemap,0,0);
		tilemap_draw(bitmap,cliprect,fg_tilemap,0,0);
		draw_sprites(bitmap,cliprect);
	} };
}
