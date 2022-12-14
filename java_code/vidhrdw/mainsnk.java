/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class mainsnk
{
	
	static unsigned char bg_color,  old_bg_color;
	#define mainsnk_offset 8
	static struct tilemap *me_fg_tilemap;
	static struct tilemap *me_bg_tilemap;
	data8_t *me_fgram;
	data8_t *me_bgram;
	static int me_gfx_ctrl;
	
	public static WriteHandlerPtr me_c600_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		bg_color = data&0xf;
		me_gfx_ctrl=data;
	} };
	
	public static GetTileInfoHandlerPtr get_me_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = (me_fgram[tile_index]);
	
		SET_TILE_INFO(
				0,
				code,
				0x10,
				0)
	} };
	
	static void stuff_palette( int source_index, int dest_index, int num_colors )
	{
	
	
	
		unsigned char *color_prom = memory_region(REGION_PROMS) + source_index;
		int i;
		for( i=0; i<num_colors; i++ )
		{
			int bit0=0,bit1,bit2,bit3;
			int red, green, blue;
	
			bit0 = (color_prom.read(0x1000)>> 2) & 0x01; // ?
			bit1 = (color_prom.read(0x000)>> 1) & 0x01;
			bit2 = (color_prom.read(0x000)>> 2) & 0x01;
			bit3 = (color_prom.read(0x000)>> 3) & 0x01;
			red = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(0x1000)>> 1) & 0x01; // ?
			bit1 = (color_prom.read(0x800)>> 2) & 0x01;
			bit2 = (color_prom.read(0x800)>> 3) & 0x01;
			bit3 = (color_prom.read(0x000)>> 0) & 0x01;
			green = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			bit0 = (color_prom.read(0x1000)>> 0) & 0x01; // ?
			bit1 = (color_prom.read(0x1000)>> 3) & 0x01; // ?
			bit2 = (color_prom.read(0x800)>> 0) & 0x01;
			bit3 = (color_prom.read(0x800)>> 1) & 0x01;
			blue = 0x0e * bit0 + 0x1f * bit1 + 0x43 * bit2 + 0x8f * bit3;
	
			palette_set_color( dest_index++, red, green, blue );
			color_prom++;
		}
	
	}
	
	static void update_palette( int type )
	{
		if( bg_color!=old_bg_color )
		{
			stuff_palette( 256+16*(bg_color&0x7), (0x11-type)*16, 16 );
			old_bg_color = bg_color;
		}
	}
	
	
	public static ReadHandlerPtr me_fgram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return me_fgram[offset];
	} };
	
	
	public static WriteHandlerPtr me_fgram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		me_fgram[offset] = data;
		tilemap_mark_tile_dirty(me_fg_tilemap,offset);
	} };
	
	
	public static GetTileInfoHandlerPtr get_me_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int code = (me_bgram[tile_index]);
	
		SET_TILE_INFO(
				0,
				code  + ((me_gfx_ctrl<<4)&0x300),
				0x10,
				0)
	} };
	
	
	public static ReadHandlerPtr me_bgram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return me_bgram[offset];
	
	} };
	
	public static WriteHandlerPtr me_bgram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		me_bgram[offset] = data;
		tilemap_mark_tile_dirty(me_bg_tilemap,offset);
	} };
	
	
	public static VideoStartHandlerPtr video_start_mainsnk  = new VideoStartHandlerPtr() { public int handler(){
		old_bg_color = -1;
		stuff_palette( 0, 0, 16*8 );
		stuff_palette( 16*8*3, 16*8, 16*8 );
		me_fg_tilemap = tilemap_create(get_me_fg_tile_info,tilemap_scan_cols,TILEMAP_TRANSPARENT,8,8,32, 32);
		tilemap_set_transparent_pen(me_fg_tilemap,15);
		me_bg_tilemap = tilemap_create(get_me_bg_tile_info,tilemap_scan_cols,TILEMAP_OPAQUE,8,8,32, 32);
		tilemap_set_scrollx( me_fg_tilemap, 0, -mainsnk_offset );
		tilemap_set_scrollx( me_bg_tilemap, 0, -mainsnk_offset );
		return 0;
	} };
	
	static void draw_sprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect, int scrollx, int scrolly )
	{
		const struct GfxElement *gfx = Machine->gfx[1];
		const unsigned char *source, *finish;
		source =  memory_region(REGION_CPU1)+0xe800;
		finish =  source + 0x64;
	
		while( source<finish )
		{
			int attributes = source[3];
			int tile_number = source[1];
			int sy = source[0];
			int sx = source[2];
			int color = attributes&0xf;
			if( sy>240 ) sy -= 256;
	
			tile_number |= attributes<<4 & 0x300;
	
			drawgfx( bitmap,gfx,
				tile_number,
				color,
				0,0,
				256-sx+mainsnk_offset,sy,
				cliprect,TRANSPARENCY_PEN,7);
	
			source+=4;
		}
	}
	
	
	static void draw_status( struct mame_bitmap *bitmap, const struct rectangle *cliprect,int dx,int off )
	{
		const unsigned char *base = memory_region(REGION_CPU1)+0xf000+off;
		const struct GfxElement *gfx = Machine->gfx[0];
		int row;
		for( row=0; row<4; row++ )
		{
			int sy,sx = (row&1)*8;
			const unsigned char *source = base + (row&1)*32;
			if( row>1 )
			{
				sx+=256+16;
			}
			else
			{
				source+=30*32;
			}
	
			for( sy=0; sy<256; sy+=8 )
			{
				int tile_number = *source++;
				drawgfx( bitmap, gfx,
				    tile_number, tile_number>>5,
				    0,0,
				    sx+dx,sy,
				    cliprect,
				    TRANSPARENCY_NONE, 0xf );
			}
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_mainsnk  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		struct rectangle myclip;
		myclip.min_x = cliprect.min_x+8;
		myclip.max_x = cliprect.max_x-8;
		myclip.min_y = cliprect.min_y;
		myclip.max_y = cliprect.max_y;
		tilemap_draw(bitmap,&myclip,me_bg_tilemap,0,0);
		draw_sprites( bitmap,&myclip, 0,0 );
		tilemap_draw(bitmap,&myclip,me_fg_tilemap,0,0);
		draw_status( bitmap,cliprect,0,0x400 );
		draw_status( bitmap,cliprect,32*8,0x40 );
		update_palette(1);
	} };
	
}
