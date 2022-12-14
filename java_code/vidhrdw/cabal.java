/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class cabal
{
	
	static struct tilemap *background_layer,*text_layer;
	
	
	public static GetTileInfoHandlerPtr get_back_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tile = videoram16[tile_index];
		int color = (tile>>12)&0xf;
	
		tile &= 0xfff;
	
		SET_TILE_INFO(
				1,
				tile,
				color,
				0)
	} };
	
	public static GetTileInfoHandlerPtr get_text_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tile = colorram16[tile_index];
		int color = (tile>>10);
	
		tile &= 0x3ff;
	
		SET_TILE_INFO(
				0,
				tile,
				color,
				0)
	} };
	
	
	public static VideoStartHandlerPtr video_start_cabal  = new VideoStartHandlerPtr() { public int handler(){
		background_layer = tilemap_create(get_back_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,16,16);
		text_layer       = tilemap_create(get_text_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,  8,8,32,32);
	
		if (!text_layer || !background_layer ) return 1;
	
		tilemap_set_transparent_pen(text_layer,3);
		tilemap_set_transparent_pen(background_layer,15);
	
		return 0;
	} };
	
	
	/**************************************************************************/
	
	WRITE16_HANDLER( cabal_flipscreen_w )
	{
		if (ACCESSING_LSB)
		{
			int flip = (data & 0x20) ? (TILEMAP_FLIPX | TILEMAP_FLIPY) : 0;
			tilemap_set_flip(background_layer,flip);
			tilemap_set_flip(text_layer,flip);
	
			flip_screen_set(data & 0x20);
		}
	}
	
	WRITE16_HANDLER( cabal_background_videoram16_w )
	{
		int oldword = videoram16[offset];
		COMBINE_DATA(&videoram16[offset]);
		if (oldword != videoram16[offset])
			tilemap_mark_tile_dirty(background_layer,offset);
	}
	
	WRITE16_HANDLER( cabal_text_videoram16_w )
	{
		int oldword = colorram16[offset];
		COMBINE_DATA(&colorram16[offset]);
		if (oldword != colorram16[offset])
			tilemap_mark_tile_dirty(text_layer,offset);
	}
	
	
	/********************************************************************
	
		Cabal Spriteram
		---------------
	
		+0   .......x ........  Sprite enable bit
		+0   ........ xxxxxxxx  Sprite Y coordinate
		+1   ..??.... ........  ??? unknown ???
		+1   ....xxxx xxxxxxxx  Sprite tile number
	 	+2   .xxxx... ........  Sprite color bank
		+2   .....x.. ........  Sprite flip x
		+2   .......x xxxxxxxx  Sprite X coordinate
		+3   (unused)
	
	            -------E YYYYYYYY
	            ----BBTT TTTTTTTT
	            -CCCCF-X XXXXXXXX
	            -------- --------
	
	********************************************************************/
	
	static void cabal_draw_sprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect )
	{
		int offs,data0,data1,data2;
	
		for( offs = spriteram_size/2 - 4; offs >= 0; offs -= 4 )
		{
			data0 = spriteram16[offs];
			data1 = spriteram16[offs+1];
			data2 = spriteram16[offs+2];
	
			if( data0 & 0x100 )
			{
				int tile_number = data1 & 0xfff;
				int color   = ( data2 & 0x7800 ) >> 11;
				int sy = ( data0 & 0xff );
				int sx = ( data2 & 0x1ff );
				int flipx = ( data2 & 0x0400 );
				int flipy = 0;
	
				if ( sx>256 )   sx -= 512;
	
				if (flip_screen())
				{
					sx = 240 - sx;
					sy = 240 - sy;
					flipx = NOT(flipx);
					flipy = NOT(flipy);
				}
	
				drawgfx( bitmap,Machine->gfx[2],
					tile_number,
					color,
					flipx,flipy,
					sx,sy,
					cliprect,TRANSPARENCY_PEN,0xf );
			}
		}
	}
	
	
	public static VideoUpdateHandlerPtr video_update_cabal  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_draw(bitmap,cliprect,background_layer,TILEMAP_IGNORE_TRANSPARENCY,0);
		cabal_draw_sprites(bitmap,cliprect);
		tilemap_draw(bitmap,cliprect,text_layer,0,0);
	} };
	
	
}
