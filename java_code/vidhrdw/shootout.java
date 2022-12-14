/*
	Video Hardware for Shoot Out
	prom GB09.K6 may be related to background tile-sprite priority
*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class shootout
{
	
	static struct tilemap *background, *foreground;
	
	
	public static PaletteInitHandlerPtr palette_init_shootout  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
	
		for (i = 0;i < Machine.drv.total_colors;i++)
		{
			int bit0,bit1,bit2,r,g,b;
	
			/* red component */
			bit0 = (color_prom.read(i)>> 0) & 0x01;
			bit1 = (color_prom.read(i)>> 1) & 0x01;
			bit2 = (color_prom.read(i)>> 2) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (color_prom.read(i)>> 3) & 0x01;
			bit1 = (color_prom.read(i)>> 4) & 0x01;
			bit2 = (color_prom.read(i)>> 5) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (color_prom.read(i)>> 6) & 0x01;
			bit2 = (color_prom.read(i)>> 7) & 0x01;
			b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			palette_set_color(i,r,g,b);
		}
	} };
	
	
	
	public static GetTileInfoHandlerPtr get_bg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) {
		int attributes = videoram.read(tile_index+0x400); /* CCCC -TTT */
		int tile_number = videoram.read(tile_index)+ 256*(attributes&7);
		int color = attributes>>4;
		SET_TILE_INFO(
				2,
				tile_number,
				color,
				0)
	} };
	
	public static GetTileInfoHandlerPtr get_fg_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) {
		int attributes = shootout_textram[tile_index+0x400]; /* CCCC --TT */
		int tile_number = shootout_textram[tile_index] + 256*(attributes&0x3);
		int color = attributes>>4;
		SET_TILE_INFO(
				0,
				tile_number,
				color,
				0)
	} };
	
	public static WriteHandlerPtr shootout_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
		if( videoram.read(offset)!=data ){
			videoram.write(offset,data);
			tilemap_mark_tile_dirty( background, offset&0x3ff );
		} };
	}
	public static WriteHandlerPtr shootout_textram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
		if( shootout_textram[offset]!=data ){
			shootout_textram[offset] = data;
			tilemap_mark_tile_dirty( foreground, offset&0x3ff );
		} };
	}
	
	public static VideoStartHandlerPtr video_start_shootout  = new VideoStartHandlerPtr() { public int handler()
		background = tilemap_create(get_bg_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32);
		foreground = tilemap_create(get_fg_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,8,8,32,32);
		if( background && foreground ){
			tilemap_set_transparent_pen( foreground, 0 );
			return 0;
		} };
		return 1; /* error */
	}
	
	static void draw_sprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect, int bank_bits ){
		static int bFlicker;
		const struct GfxElement *gfx = Machine->gfx[1];
		const UINT8 *source = spriteram+127*4;
		int count;
	
		bFlicker = !bFlicker;
	
		for( count=0; count<128; count++ ){
			int attributes = source[1];
			/*
			    76543210
				xxx-----	bank
				---x----	vertical size
				----x---	priority
				-----x--	horizontal flip
				------x-	flicker
				-------x	enable
			*/
			if ( attributes & 0x01 ){ /* visible */
				if( bFlicker || (attributes&0x02)==0 ){
					int priority_mask = (attributes&0x08)?0x2:0;
					int sx = (240 - source[2])&0xff;
					int sy = (240 - source[0])&0xff;
					int vx, vy;
					int number = source[3] | ((attributes<<bank_bits)&0x700);
					int flipx = (attributes & 0x04);
					int flipy = 0;
	
					if (flip_screen()) {
						flipx = NOT(flipx);
						flipy = NOT(flipy);
					}
	
					if( attributes & 0x10 ){ /* double height */
						number = number&(~1);
						sy -= 16;
	
						vx = sx;
						vy = sy;
						if (flip_screen()) {
							vx = 240 - vx;
							vy = 240 - vy;
						}
	
						pdrawgfx(bitmap,gfx,
							number,
							0 /*color*/,
							flipx,flipy,
							vx,vy,
							cliprect,TRANSPARENCY_PEN,0,
							priority_mask);
	
						number++;
						sy += 16;
					}
	
					vx = sx;
					vy = sy;
					if (flip_screen()) {
						vx = 240 - vx;
						vy = 240 - vy;
					}
	
					pdrawgfx(bitmap,gfx,
							number,
							0 /*color*/,
							flipx,flipy,
							vx,vy,
							cliprect,TRANSPARENCY_PEN,0,
							priority_mask);
					}
			}
			source -= 4;
		}
	}
	
	public static VideoUpdateHandlerPtr video_update_shootout  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		fillbitmap(priority_bitmap,0,cliprect);
	
		tilemap_draw(bitmap,cliprect,background,0,0);
		tilemap_draw(bitmap,cliprect,foreground,0,1);
		draw_sprites(bitmap,cliprect,3/*bank bits */);
	} };
	
	public static VideoUpdateHandlerPtr video_update_shootouj  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		fillbitmap(priority_bitmap,0,cliprect);
	
		tilemap_draw(bitmap,cliprect,background,0,0);
		tilemap_draw(bitmap,cliprect,foreground,0,1);
		draw_sprites(bitmap,cliprect,2/*bank bits*/);
	} };
}
