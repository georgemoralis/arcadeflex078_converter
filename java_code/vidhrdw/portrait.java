/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.


***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class portrait
{
	data8_t *portrait_bgvideoram, *portrait_fgvideoram, *portrait_spriteram;
	static struct tilemap *foreground, *background;
	
	public static WriteHandlerPtr portrait_bgvideo_write = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (portrait_bgvideoram[offset] != data)
		{
			tilemap_mark_tile_dirty(background,offset/2);
			portrait_bgvideoram[offset] = data;
		}
	} };
	
	public static WriteHandlerPtr portrait_fgvideo_write = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (portrait_fgvideoram[offset] != data)
		{
			tilemap_mark_tile_dirty(foreground,offset/2);
			portrait_fgvideoram[offset] = data;
		}
	} };
	
	static void get_tile_info( const data8_t *source, int tile_index )
	{
		int attr    = source[tile_index*2+0];
		int tilenum = source[tile_index*2+1];
		int flags = 0;
		int color   = 0;
	
		if( attr & 0x20 ) flags |= TILE_FLIPY;
	
		attr &= 0x07;
		if(attr == 1) tilenum+=0x200; // 001
		if(attr == 3) tilenum+=0x300; // 011
		if(attr == 5) tilenum+=0x100; // 101
	
		SET_TILE_INFO( 0, tilenum, color, flags )
	}
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		get_tile_info( portrait_bgvideoram, tile_index );
	} };
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		get_tile_info( portrait_fgvideoram, tile_index );
	} };
	
	public static VideoStartHandlerPtr video_start_portrait  = new VideoStartHandlerPtr() { public int handler(){
		background = tilemap_create( get_bg_tile_info, tilemap_scan_rows, TILEMAP_OPAQUE,      16, 16, 32, 32 );
		foreground = tilemap_create( get_fg_tile_info, tilemap_scan_rows, TILEMAP_TRANSPARENT, 16, 16, 32, 32 );
		if( background && foreground )
		{
			tilemap_set_transparent_pen( foreground, 0 );
			return 0;
		}
		return -1;
	} };
	
	
	public static PaletteInitHandlerPtr palette_init_portrait  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
	} };
	
	static void draw_sprites( struct mame_bitmap *bitmap )
	{
		const data8_t *source = portrait_spriteram;
		const data8_t *finish = source + 0x200;
		while( source<finish )
		{
			int sy      = source[0];
			int sx      = source[1];
			int attr    = source[2];
				/* xx------
				 * --x----- flip
				 * ----x--- msb source[0]
				 * -----x-- msb source[1]
				 */
			int tilenum = source[3];
			int color = 0;
			int flip = attr&0x20;
			if( attr&0x04 ) sx |= 0x100;
			if( attr&0x08 ) sy |= 0x100;
	
			sx += (source-portrait_spriteram)-8;
			sx &= 0x1ff;
	
			sy = (512-64)-sy;
			//sy += portrait_scrollx_hi;
	
			drawgfx(bitmap,Machine->gfx[0],
				tilenum,color,
				0,flip,
				sx,sy,
				NULL,TRANSPARENCY_PEN,0 );
	
			source+=0x10;
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_portrait  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_set_scrolly( background, 0, portrait_scrollx_hi );
		tilemap_set_scrolly( foreground, 0, portrait_scrollx_hi );
	
		tilemap_draw( bitmap, cliprect, background, 0, 0 );
		draw_sprites(bitmap);
		tilemap_draw( bitmap, cliprect, foreground, 0, 0 );
	} };
}
