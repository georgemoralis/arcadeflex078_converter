/* Speed Spin Vidhrdw, see driver file for notes */

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class speedspn
{
	
	data8_t *speedspn_attram;
	
	static struct tilemap *speedspn_tilemap;
	static UINT8 speedspn_display_disable = 0;
	static int speedspn_bank_vidram = 0;
	static data8_t* speedspn_vidram;
	
	
	public static GetTileInfoHandlerPtr get_speedspn_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = speedspn_vidram[tile_index*2+1] | (speedspn_vidram[tile_index*2] << 8);
		int attr = speedspn_attram[tile_index^0x400];
	
		SET_TILE_INFO(0,code,attr & 0x3f,(attr & 0x80) ? TILE_FLIPX : 0)
	} };
	
	public static VideoStartHandlerPtr video_start_speedspn  = new VideoStartHandlerPtr() { public int handler(){
		speedspn_vidram = auto_malloc(0x1000 * 2);
		speedspn_tilemap = tilemap_create(get_speedspn_tile_info,tilemap_scan_cols,TILEMAP_OPAQUE, 8, 8,64,32);
		return 0;
	} };
	
	public static WriteHandlerPtr speedspn_vidram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		speedspn_vidram[offset + speedspn_bank_vidram] = data;
	
		if (speedspn_bank_vidram == 0)
			tilemap_mark_tile_dirty(speedspn_tilemap,offset/2);
	} };
	
	public static WriteHandlerPtr speedspn_attram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		speedspn_attram[offset] = data;
	
		tilemap_mark_tile_dirty(speedspn_tilemap,offset^0x400);
	} };
	
	public static ReadHandlerPtr speedspn_vidram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return speedspn_vidram[offset + speedspn_bank_vidram];
	} };
	
	public static WriteHandlerPtr speedspn_banked_vidram_change = new WriteHandlerPtr() {public void handler(int offset, int data){
	//	logerror("VidRam Bank: %04x\n", data);
		speedspn_bank_vidram = data & 1;
		speedspn_bank_vidram *= 0x1000;
	} };
	
	public static WriteHandlerPtr speedspn_global_display_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	//	logerror("Global display: %u\n", data);
		speedspn_display_disable = data & 1;
	} };
	
	
	static void speedspn_drawsprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect )
	{
		const struct GfxElement *gfx = Machine->gfx[1];
		data8_t *source = speedspn_vidram+ 0x1000;
		data8_t *finish = source + 0x1000;
	
		while( source<finish )
		{
			int xpos = source[0];
			int tileno = source[1];
			int attr = source[2];
			int ypos = source[3];
			int color;
	
			if (attr&0x10) xpos +=0x100;
	
			xpos = 0x1f8-xpos;
			tileno += ((attr & 0xe0) >> 5) * 0x100;
			color = attr & 0x0f;
	
			drawgfx(bitmap,gfx,
					tileno,
					color,
					0,0,
					xpos,ypos,
					cliprect,TRANSPARENCY_PEN,15);
	
			source +=4;
		}
	}
	
	
	public static VideoUpdateHandlerPtr video_update_speedspn  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		if (speedspn_display_disable)
		{
			fillbitmap(bitmap,get_black_pen(),cliprect);
			return;
		}
	
	#if 0
		{
			FILE* f;
			f = fopen("vidram.bin","wb");
			fwrite(speedspn_vidram, 1, 0x1000 * 2, f);
			fclose(f);
		}
	#endif
		tilemap_set_scrollx(speedspn_tilemap,0, 0x100); // verify
		tilemap_draw(bitmap,cliprect,speedspn_tilemap,0,0);
		speedspn_drawsprites(bitmap,cliprect);
	} };
}
