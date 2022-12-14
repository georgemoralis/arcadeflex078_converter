/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of early Toaplan hardware.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class slapfght
{
	
	unsigned char *slapfight_videoram;
	unsigned char *slapfight_colorram;
	size_t slapfight_videoram_size;
	unsigned char *slapfight_scrollx_lo,*slapfight_scrollx_hi,*slapfight_scrolly;
	static int flipscreen;
	
	static struct tilemap *pf1_tilemap,*fix_tilemap;
	
	
	
	
	/***************************************************************************
	
	  Callbacks for the TileMap code
	
	***************************************************************************/
	
	public static GetTileInfoHandlerPtr get_pf_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 	/* For Performan only */
	{
		int tile,color;
	
		tile=videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x03) << 8);
		color=(colorram.read(tile_index)>> 3) & 0x0f;
		SET_TILE_INFO(
				0,
				tile,
				color,
				0)
	} };
	
	public static GetTileInfoHandlerPtr get_pf1_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tile,color;
	
		tile=videoram.read(tile_index)+ ((colorram.read(tile_index)& 0x0f) << 8);
		color=(colorram.read(tile_index)& 0xf0) >> 4;
	
		SET_TILE_INFO(
				1,
				tile,
				color,
				0)
	} };
	
	public static GetTileInfoHandlerPtr get_fix_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tile,color;
	
		tile=slapfight_videoram[tile_index] + ((slapfight_colorram[tile_index] & 0x03) << 8);
		color=(slapfight_colorram[tile_index] & 0xfc) >> 2;
	
		SET_TILE_INFO(
				0,
				tile,
				color,
				0)
	} };
	
	
	/***************************************************************************
	
	  Start the video hardware emulation.
	
	***************************************************************************/
	
	public static VideoStartHandlerPtr video_start_perfrman  = new VideoStartHandlerPtr() { public int handler(){
		pf1_tilemap = tilemap_create(get_pf_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,64,32);
	
		if (!pf1_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(pf1_tilemap,0);
	
		return 0;
	} };
	
	public static VideoStartHandlerPtr video_start_slapfight  = new VideoStartHandlerPtr() { public int handler(){
		pf1_tilemap = tilemap_create(get_pf1_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,64,32);
		fix_tilemap = tilemap_create(get_fix_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,64,32);
	
		if (!pf1_tilemap || !fix_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(fix_tilemap,0);
	
		return 0;
	} };
	
	
	/***************************************************************************
	
	  Memory handlers
	
	***************************************************************************/
	
	public static WriteHandlerPtr slapfight_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		videoram.write(offset,data);
		tilemap_mark_tile_dirty(pf1_tilemap,offset);
	} };
	
	public static WriteHandlerPtr slapfight_colorram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		colorram.write(offset,data);
		tilemap_mark_tile_dirty(pf1_tilemap,offset);
	} };
	
	public static WriteHandlerPtr slapfight_fixram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		slapfight_videoram[offset]=data;
		tilemap_mark_tile_dirty(fix_tilemap,offset);
	} };
	
	public static WriteHandlerPtr slapfight_fixcol_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		slapfight_colorram[offset]=data;
		tilemap_mark_tile_dirty(fix_tilemap,offset);
	} };
	
	public static WriteHandlerPtr slapfight_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		logerror("Writing %02x to flipscreen\n",offset);
		if (offset==0) flipscreen=1; /* Port 0x2 is flipscreen */
		else flipscreen=0; /* Port 0x3 is normal */
	} };
	
	#ifdef MAME_DEBUG
	void slapfght_log_vram(void)
	{
		if ( keyboard_pressed_memory(KEYCODE_B) )
		{
			int i;
			for (i=0; i<0x800; i++)
			{
				logerror("Offset:%03x   TileRAM:%02x   AttribRAM:%02x   SpriteRAM:%02x\n",i, videoram.read(i),colorram.read(i),spriteram.read(i));
			}
		}
	}
	#endif
	
	/***************************************************************************
	
	  Render the Sprites
	
	***************************************************************************/
	static void perfrman_draw_sprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect, int priority_to_display )
	{
		int offs;
	
		for (offs = 0;offs < spriteram_size;offs += 4)
		{
			int sx, sy;
	
			if ((buffered_spriteram[offs+2] & 0x80) == priority_to_display)
			{
				if (flipscreen)
				{
					sx = 265 - buffered_spriteram[offs+1];
					sy = 239 - buffered_spriteram[offs+3];
					sy &= 0xff;
				}
				else
				{
					sx = buffered_spriteram[offs+1] + 3;
					sy = buffered_spriteram[offs+3] - 1;
				}
				drawgfx(bitmap,Machine->gfx[1],
					buffered_spriteram[offs],
					((buffered_spriteram[offs+2] >> 1) & 3)
						+ ((buffered_spriteram[offs+2] << 2) & 4)
	//					+ ((buffered_spriteram[offs+2] >> 2) & 8)
					,
					flipscreen, flipscreen,
					sx, sy,
					cliprect,TRANSPARENCY_PEN,0);
			}
		}
	}
	
	/***************************************************************************
	
	  Draw the game screen in the given mame_bitmap.
	  Do NOT call osd_update_display() from this function, it will be called by
	  the main emulation engine.
	
	***************************************************************************/
	
	public static VideoUpdateHandlerPtr video_update_perfrman  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_set_flip( pf1_tilemap, flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		tilemap_set_scrolly( pf1_tilemap ,0 , 0 );
		if (flipscreen) {
			tilemap_set_scrollx( pf1_tilemap ,0 , 264 );
		}
		else {
			tilemap_set_scrollx( pf1_tilemap ,0 , -16 );
		}
	
		fillbitmap(bitmap,Machine.pens[0],cliprect);
	
		perfrman_draw_sprites(bitmap,cliprect,0);
		tilemap_draw(bitmap,cliprect,pf1_tilemap,0,0);
		perfrman_draw_sprites(bitmap,cliprect,0x80);
	
	#ifdef MAME_DEBUG
		slapfght_log_vram();
	#endif
	} };
	
	
	public static VideoUpdateHandlerPtr video_update_slapfight  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs;
	
		tilemap_set_flip(ALL_TILEMAPS,flipscreen ? (TILEMAP_FLIPY | TILEMAP_FLIPX) : 0);
		if (flipscreen) {
			tilemap_set_scrollx( fix_tilemap,0,296);
			tilemap_set_scrollx( pf1_tilemap,0,(*slapfight_scrollx_lo + 256 * *slapfight_scrollx_hi)+296 );
			tilemap_set_scrolly( pf1_tilemap,0, (*slapfight_scrolly)+15 );
			tilemap_set_scrolly( fix_tilemap,0, -1 ); /* Glitch in Tiger Heli otherwise */
		}
		else {
			tilemap_set_scrollx( fix_tilemap,0,0);
			tilemap_set_scrollx( pf1_tilemap,0,(*slapfight_scrollx_lo + 256 * *slapfight_scrollx_hi) );
			tilemap_set_scrolly( pf1_tilemap,0, (*slapfight_scrolly)-1 );
			tilemap_set_scrolly( fix_tilemap,0, -1 ); /* Glitch in Tiger Heli otherwise */
		}
	
		tilemap_draw(bitmap,cliprect,pf1_tilemap,0,0);
	
		/* Draw the sprites */
		for (offs = 0;offs < spriteram_size[0];offs += 4)
		{
			if (flipscreen)
				drawgfx(bitmap,Machine.gfx[2],
					buffered_spriteram[offs] + ((buffered_spriteram[offs+2] & 0xc0) << 2),
					(buffered_spriteram[offs+2] & 0x1e) >> 1,
					1,1,
					288-(buffered_spriteram[offs+1] + ((buffered_spriteram[offs+2] & 0x01) << 8)) +18,240-buffered_spriteram[offs+3],
					cliprect,TRANSPARENCY_PEN,0);
			else
				drawgfx(bitmap,Machine.gfx[2],
					buffered_spriteram[offs] + ((buffered_spriteram[offs+2] & 0xc0) << 2),
					(buffered_spriteram[offs+2] & 0x1e) >> 1,
					0,0,
					(buffered_spriteram[offs+1] + ((buffered_spriteram[offs+2] & 0x01) << 8)) - 13,buffered_spriteram[offs+3],
					cliprect,TRANSPARENCY_PEN,0);
		}
	
		tilemap_draw(bitmap,cliprect,fix_tilemap,0,0);
	
	#ifdef MAME_DEBUG
		slapfght_log_vram();
	#endif
	} };
}
