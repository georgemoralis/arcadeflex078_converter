/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class gng
{
	
	
	unsigned char *gng_fgvideoram;
	unsigned char *gng_bgvideoram;
	
	static struct tilemap *bg_tilemap,*fg_tilemap;
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		unsigned char attr = gng_fgvideoram[tile_index + 0x400];
		SET_TILE_INFO(
				0,
				gng_fgvideoram[tile_index] + ((attr & 0xc0) << 2),
				attr & 0x0f,
				TILE_FLIPYX((attr & 0x30) >> 4))
	} };
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		unsigned char attr = gng_bgvideoram[tile_index + 0x400];
		SET_TILE_INFO(
				1,
				gng_bgvideoram[tile_index] + ((attr & 0xc0) << 2),
				attr & 0x07,
				TILE_FLIPYX((attr & 0x30) >> 4) | TILE_SPLIT((attr & 0x08) >> 3))
	} };
	
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_gng  = new VideoStartHandlerPtr() { public int handler(){
		fg_tilemap = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		bg_tilemap = tilemap_create(get_bg_tile_info,tilemap_scan_cols,TILEMAP_SPLIT,    16,16,32,32);
	
		if (!fg_tilemap || !bg_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fg_tilemap,3);
	
		tilemap_set_transmask(bg_tilemap,0,0xff,0x00); /* split type 0 is totally transparent in front half */
		tilemap_set_transmask(bg_tilemap,1,0x41,0xbe); /* split type 1 has pens 0 and 6 transparent in front half */
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr gng_fgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		gng_fgvideoram[offset] = data;
		tilemap_mark_tile_dirty(fg_tilemap,offset & 0x3ff);
	} };
	
	public static WriteHandlerPtr gng_bgvideoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		gng_bgvideoram[offset] = data;
		tilemap_mark_tile_dirty(bg_tilemap,offset & 0x3ff);
	} };
	
	
	public static WriteHandlerPtr gng_bgscrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static unsigned char scrollx[2];
		scrollx[offset] = data;
		tilemap_set_scrollx( bg_tilemap, 0, scrollx[0] + 256 * scrollx[1] );
	} };
	
	public static WriteHandlerPtr gng_bgscrolly_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		static unsigned char scrolly[2];
		scrolly[offset] = data;
		tilemap_set_scrolly( bg_tilemap, 0, scrolly[0] + 256 * scrolly[1] );
	} };
	
	
	public static WriteHandlerPtr gng_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		flip_screen_set(~data & 1);
	} };
	
	
	
	/***************************************************************************
	
	  Display refresh
	
	***************************************************************************/
	
	static void draw_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect)
	{
		const struct GfxElement *gfx = Machine->gfx[2];
		int offs;
	
	
		for (offs = spriteram_size - 4;offs >= 0;offs -= 4)
		{
			unsigned char attributes = buffered_spriteram[offs+1];
			int sx = buffered_spriteram[offs + 3] - 0x100 * (attributes & 0x01);
			int sy = buffered_spriteram[offs + 2];
			int flipx = attributes & 0x04;
			int flipy = attributes & 0x08;
	
			if (flip_screen())
			{
				sx = 240 - sx;
				sy = 240 - sy;
				flipx = NOT(flipx);
				flipy = NOT(flipy);
			}
	
			drawgfx(bitmap,gfx,
					buffered_spriteram[offs] + ((attributes<<2) & 0x300),
					(attributes >> 4) & 3,
					flipx,flipy,
					sx,sy,
					cliprect,TRANSPARENCY_PEN,15);
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_gng  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,bg_tilemap,TILEMAP_BACK,0);
		draw_sprites(bitmap,cliprect);
		tilemap_draw(bitmap,cliprect,bg_tilemap,TILEMAP_FRONT,0);
		tilemap_draw(bitmap,cliprect,fg_tilemap,0,0);
	} };
	
	public static VideoEofHandlerPtr video_eof_gng  = new VideoEofHandlerPtr() { public void handler(){
		buffer_spriteram_w(0,0);
	} };
}
