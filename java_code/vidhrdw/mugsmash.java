/* vidhrdw/mugsmash.c - see drivers/mugsmash.c for more info */

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class mugsmash
{
	
	static struct tilemap *mugsmash_tilemap1,  *mugsmash_tilemap2;
	
	
	static void draw_sprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect )
	{
	
		/* Each Sprite takes 16 bytes, 5 used? */
	
		/* ---- ----  xxxx xxxx  ---- ----  aaaa aaaa  ---- ----  NNNN NNNN  ---- ----  nnnn nnnn  ---- ----  yyyy yyyy (rest unused?) */
	
		/* x = xpos LSB
		   y = ypos LSB
		   N = tile number MSB
		   n = tile number LSB
		   a = attribute / extra
		   		f?XY cccc
	
		   	f = x-flip
		   	? = unknown, probably y-flip
		   	X = xpos MSB
		   	y = ypos MSB
		   	c = colour
	
		*/
	
		const UINT16 *source = mugs_spriteram;
		const UINT16 *finish = source+0x2000;
		const struct GfxElement *gfx = Machine->gfx[0];
	
		while( source<finish )
		{
			int xpos = source[0] & 0x00ff;
			int ypos = source[4] & 0x00ff;
			int num = (source[3] & 0x00ff) | ((source[2] & 0x00ff) << 8);
			int attr = source[1];
			int flipx = (attr & 0x0080)>>7;
			int colour = (attr & 0x000f);
	
	
			xpos += ((attr & 0x0020) >> 5) * 0x100;
			ypos += ((attr & 0x0010) >> 4) * 0x100;
	
			xpos -= 28;
			ypos -= 16;
	
			drawgfx(
					bitmap,
					gfx,
					num,
					colour,
					flipx,0,
					xpos,ypos,
					cliprect,
					TRANSPARENCY_PEN,0
					);
	
			source += 0x8;
		}
	}
	
	public static GetTileInfoHandlerPtr get_mugsmash_tile_info1 = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
	
		/* fF-- cccc  nnnn nnnn */
	
		/* c = colour?
		   n = number?
		   F = flip-X
		   f = flip-Y
		*/
	
		int tileno,colour,fx;
	
		tileno = mugsmash_videoram1[tile_index *2 +1];
		colour = mugsmash_videoram1[tile_index *2] & 0x000f;
		fx = (mugsmash_videoram1[tile_index *2] & 0xc0) >>6;
	
		SET_TILE_INFO(1,tileno,colour,TILE_FLIPYX(fx))
	} };
	
	WRITE16_HANDLER( mugsmash_videoram1_w )
	{
		if (mugsmash_videoram1[offset] != data)
		{
			mugsmash_videoram1[offset] = data;
			tilemap_mark_tile_dirty(mugsmash_tilemap1,offset/2);
		}
	}
	
	public static GetTileInfoHandlerPtr get_mugsmash_tile_info2 = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
	
		/* fF-- cccc  nnnn nnnn */
	
		/* c = colour?
		   n = number?
		   F = flip-X
		   f = flip-Y
		*/
	
		int tileno,colour,fx;
	
		tileno = mugsmash_videoram2[tile_index *2 +1];
		colour = mugsmash_videoram2[tile_index *2] & 0x000f;
		fx = (mugsmash_videoram2[tile_index *2] & 0xc0) >>6;
	
		SET_TILE_INFO(1,tileno,16+colour,TILE_FLIPYX(fx))
	} };
	
	WRITE16_HANDLER( mugsmash_videoram2_w )
	{
		if (mugsmash_videoram2[offset] != data)
		{
			mugsmash_videoram2[offset] = data;
			tilemap_mark_tile_dirty(mugsmash_tilemap2,offset/2);
		}
	}
	
	WRITE16_HANDLER (mugsmash_reg_w)
	{
		mugsmash_regs1[offset] = data;
	//	usrintf_showmessage	("Regs %04x, %04x, %04x, %04x", mugsmash_regs1[0], mugsmash_regs1[1],mugsmash_regs1[2], mugsmash_regs1[3]);
	
		switch (offset)
		{
		case 0:
			tilemap_set_scrollx(mugsmash_tilemap2,0, mugsmash_regs1[2]+4); // verify
			break;
		case 1:
			tilemap_set_scrolly(mugsmash_tilemap2,0, mugsmash_regs1[3]+4);
			break;
		case 2:
			tilemap_set_scrollx(mugsmash_tilemap1,0, mugsmash_regs1[0]+4); // verify
			break;
		case 3:
			tilemap_set_scrolly(mugsmash_tilemap1,0, mugsmash_regs1[1]+4);
			break;
		}
	}
	
	public static VideoStartHandlerPtr video_start_mugsmash  = new VideoStartHandlerPtr() { public int handler(){
	
		mugsmash_tilemap1 = tilemap_create(get_mugsmash_tile_info1,tilemap_scan_rows,TILEMAP_TRANSPARENT, 16, 16,32,32);
		tilemap_set_transparent_pen(mugsmash_tilemap1,0);
	
		mugsmash_tilemap2 = tilemap_create(get_mugsmash_tile_info2,tilemap_scan_rows,TILEMAP_OPAQUE, 16, 16,32,32);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_mugsmash  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,mugsmash_tilemap2,0,0);
		tilemap_draw(bitmap,cliprect,mugsmash_tilemap1,0,0);
		draw_sprites(bitmap,cliprect);
	} };
}
