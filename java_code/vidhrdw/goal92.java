/***************************************************************************

	Goal '92 video hardware

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class goal92
{
	
	data16_t *goal92_back_data,*goal92_fore_data,*goal92_textram,*goal92_scrollram16;
	
	static struct tilemap *background_layer,*foreground_layer,*text_layer;
	
	static data16_t fg_bank = 0;
	
	READ16_HANDLER( goal92_fg_bank_r )
	{
		return fg_bank;
	}
	
	WRITE16_HANDLER( goal92_fg_bank_w )
	{
		COMBINE_DATA(&fg_bank);
	
		if(ACCESSING_LSB)
		{
			tilemap_mark_all_tiles_dirty(foreground_layer);
		}
	}
	
	WRITE16_HANDLER( goal92_text_w )
	{
		int oldword = goal92_textram[offset];
		COMBINE_DATA(&goal92_textram[offset]);
		if (oldword != goal92_textram[offset])
			tilemap_mark_tile_dirty(text_layer,offset);
	}
	
	WRITE16_HANDLER( goal92_background_w )
	{
		int oldword = goal92_back_data[offset];
		COMBINE_DATA(&goal92_back_data[offset]);
		if (oldword != goal92_back_data[offset])
			tilemap_mark_tile_dirty(background_layer,offset);
	}
	
	WRITE16_HANDLER( goal92_foreground_w )
	{
		int oldword = goal92_fore_data[offset];
		COMBINE_DATA(&goal92_fore_data[offset]);
		if (oldword != goal92_fore_data[offset])
			tilemap_mark_tile_dirty(foreground_layer,offset);
	}
	
	public static GetTileInfoHandlerPtr get_text_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tile = goal92_textram[tile_index];
		int color=(tile>>12)&0xf;
	
		tile &= 0xfff;
	
		tile |= 0xc000;
	
		SET_TILE_INFO(1,tile,color,0)
	} };
	
	public static GetTileInfoHandlerPtr get_back_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tile=goal92_back_data[tile_index];
		int color=(tile>>12)&0xf;
	
		tile &= 0xfff;
	
		SET_TILE_INFO(2,tile,color,0)
	} };
	
	public static GetTileInfoHandlerPtr get_fore_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int tile=goal92_fore_data[tile_index];
		int color=(tile>>12)&0xf;
		int region;
	
		tile &= 0xfff;
	
		if(fg_bank & 0xff)
		{
			region = 3;
			tile |= 0x1000;
		}
		else
		{
			region = 4;
			tile |= 0x2000;
		}
	
		SET_TILE_INFO(region,tile,color,0)
	} };
	
	static void draw_sprites(struct mame_bitmap *bitmap,const struct rectangle *cliprect,int pri)
	{
		int offs,fx,fy,x,y,color,sprite;
	
		for (offs = 3;offs <= 0x400-5;offs += 4)
		{
			UINT16 data = spriteram16[offs+2];
	
			y = spriteram16[offs+0];
	
			if (y & 0x8000)
				break;
	
			if (!(data & 0x8000))
				continue;
	
			sprite = spriteram16[offs+1];
	
			if ((sprite>>14)!=pri)
				continue;
	
			x = spriteram16[offs+3];
	
			sprite &= 0x1fff;
	
			x &= 0x1ff;
			y &= 0x1ff;
	
			color = (data & 0x3f) + 0x40;
			fx = (data & 0x4000) >> 14;
			fy = 0;
	
			x -= 320/4-16-1;
	
			y = 256-(y+7);
	
			drawgfx(bitmap,Machine->gfx[0],
					sprite,
					color,fx,fy,x,y,
					cliprect,TRANSPARENCY_PEN,15);
		}
	}
	
	
	public static VideoStartHandlerPtr video_start_goal92  = new VideoStartHandlerPtr() { public int handler(){
		background_layer = tilemap_create(get_back_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,32,32);
		foreground_layer = tilemap_create(get_fore_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,32,32);
		text_layer =       tilemap_create(get_text_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,  8,8,64,32);
	
		if (!background_layer || !foreground_layer || !text_layer)
			return 1;
	
		tilemap_set_transparent_pen(background_layer,15);
		tilemap_set_transparent_pen(foreground_layer,15);
		tilemap_set_transparent_pen(text_layer,15);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_goal92  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		tilemap_set_scrollx( background_layer, 0, goal92_scrollram16[0] + 60);
		tilemap_set_scrolly( background_layer, 0, goal92_scrollram16[1] + 8);
	
		if(fg_bank & 0xff)
		{
			tilemap_set_scrollx( foreground_layer, 0, goal92_scrollram16[0] + 60);
			tilemap_set_scrolly( foreground_layer, 0, goal92_scrollram16[1] + 8);
		}
		else
		{
			tilemap_set_scrollx( foreground_layer, 0, goal92_scrollram16[2] + 60);
			tilemap_set_scrolly( foreground_layer, 0, goal92_scrollram16[3] + 8);
		}
	
		fillbitmap(bitmap,get_black_pen(),cliprect);
	
		tilemap_draw(bitmap,cliprect,background_layer,0,0);
		draw_sprites(bitmap,cliprect,2);
		
		if(!(fg_bank & 0xff))
			draw_sprites(bitmap,cliprect,1);
	
		tilemap_draw(bitmap,cliprect,foreground_layer,0,0);
	
		if(fg_bank & 0xff)
			draw_sprites(bitmap,cliprect,1);
	
		draw_sprites(bitmap,cliprect,0);
		draw_sprites(bitmap,cliprect,3);
		tilemap_draw(bitmap,cliprect,text_layer,0,0);
	} };
}
