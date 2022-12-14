/* Macross Plus - Vidhrdw
  see DRIVER file for notes */

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class macrossp
{
	
	
	data32_t *macrossp_scra_videoram, *macrossp_scra_videoregs;
	data32_t *macrossp_scrb_videoram, *macrossp_scrb_videoregs;
	data32_t *macrossp_scrc_videoram, *macrossp_scrc_videoregs;
	data32_t *macrossp_text_videoram, *macrossp_text_videoregs;
	data32_t *macrossp_spriteram;
	
	static data32_t *spriteram_old,*spriteram_old2;
	
	static struct tilemap  *macrossp_scra_tilemap, *macrossp_scrb_tilemap,*macrossp_scrc_tilemap, *macrossp_text_tilemap;
	
	
	/*** SCR A LAYER ***/
	
	WRITE32_HANDLER( macrossp_scra_videoram_w )
	{
		COMBINE_DATA(&macrossp_scra_videoram[offset]);
	
		tilemap_mark_tile_dirty(macrossp_scra_tilemap,offset);
	}
	
	
	public static GetTileInfoHandlerPtr get_macrossp_scra_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		UINT32 attr,tileno,color;
	
		attr = macrossp_scra_videoram[tile_index];
		tileno = attr & 0x0000ffff;
	
		switch (macrossp_scra_videoregs[0] & 0x00000c00)
		{
			case 0x00000800:
				color = (attr & 0x000e0000) >> 15;
				break;
	
			case 0x00000400:
				color = (attr & 0x003e0000) >> 17;
				break;
	
			default:
				color = rand() & 7;
				break;
		}
	
		SET_TILE_INFO(1,tileno,color,TILE_FLIPYX((attr & 0xc0000000) >> 30))
	} };
	
	/*** SCR B LAYER ***/
	
	WRITE32_HANDLER( macrossp_scrb_videoram_w )
	{
		COMBINE_DATA(&macrossp_scrb_videoram[offset]);
	
		tilemap_mark_tile_dirty(macrossp_scrb_tilemap,offset);
	}
	
	
	public static GetTileInfoHandlerPtr get_macrossp_scrb_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		UINT32 attr,tileno,color;
	
		attr = macrossp_scrb_videoram[tile_index];
		tileno = attr & 0x0000ffff;
	
		switch (macrossp_scrb_videoregs[0] & 0x00000c00)
		{
			case 0x00000800:
				color = (attr & 0x000e0000) >> 15;
				break;
	
			case 0x00000400:
				color = (attr & 0x003e0000) >> 17;
				break;
	
			default:
				color = rand() & 7;
				break;
		}
	
		SET_TILE_INFO(2,tileno,color,TILE_FLIPYX((attr & 0xc0000000) >> 30))
	} };
	
	/*** SCR C LAYER ***/
	
	WRITE32_HANDLER( macrossp_scrc_videoram_w )
	{
		COMBINE_DATA(&macrossp_scrc_videoram[offset]);
	
		tilemap_mark_tile_dirty(macrossp_scrc_tilemap,offset);
	}
	
	
	public static GetTileInfoHandlerPtr get_macrossp_scrc_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		UINT32 attr,tileno,color;
	
		attr = macrossp_scrc_videoram[tile_index];
		tileno = attr & 0x0000ffff;
	
		switch (macrossp_scrc_videoregs[0] & 0x00000c00)
		{
			case 0x00000800:
				color = (attr & 0x000e0000) >> 15;
				break;
	
			case 0x00000400:
				color = (attr & 0x003e0000) >> 17;
				break;
	
			default:
				color = rand() & 7;
				break;
		}
	
		SET_TILE_INFO(3,tileno,color,TILE_FLIPYX((attr & 0xc0000000) >> 30))
	} };
	
	/*** TEXT LAYER ***/
	
	WRITE32_HANDLER( macrossp_text_videoram_w )
	{
		COMBINE_DATA(&macrossp_text_videoram[offset]);
	
		tilemap_mark_tile_dirty(macrossp_text_tilemap,offset);
	}
	
	
	public static GetTileInfoHandlerPtr get_macrossp_text_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		UINT32 tileno, colour;
	
		tileno = macrossp_text_videoram[tile_index] & 0x0000ffff;
		colour = (macrossp_text_videoram[tile_index] & 0x00fe0000) >> 17;
	
		SET_TILE_INFO(4,tileno,colour,0)
	} };
	
	
	
	/*** VIDEO START / UPDATE ***/
	
	public static VideoStartHandlerPtr video_start_macrossp  = new VideoStartHandlerPtr() { public int handler(){
		spriteram_old = auto_malloc(spriteram_size[0]);
		spriteram_old2 = auto_malloc(spriteram_size[0]);
	
		if (!spriteram_old || !spriteram_old2)
			return 1;
	
		memset(spriteram_old,0,spriteram_size[0]);
		memset(spriteram_old2,0,spriteram_size[0]);
	
		macrossp_text_tilemap = tilemap_create(get_macrossp_text_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,64,64);
		macrossp_scra_tilemap = tilemap_create(get_macrossp_scra_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,64,64);
		macrossp_scrb_tilemap = tilemap_create(get_macrossp_scrb_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,64,64);
		macrossp_scrc_tilemap = tilemap_create(get_macrossp_scrc_tile_info,tilemap_scan_rows,TILEMAP_TRANSPARENT,16,16,64,64);
	
		if (!macrossp_text_tilemap || !macrossp_scra_tilemap || !macrossp_scrb_tilemap || !macrossp_scrc_tilemap)
			return 1;
	
		tilemap_set_transparent_pen(macrossp_text_tilemap,0);
		tilemap_set_transparent_pen(macrossp_scra_tilemap,0);
		tilemap_set_transparent_pen(macrossp_scrb_tilemap,0);
		tilemap_set_transparent_pen(macrossp_scrc_tilemap,0);
	
		Machine.gfx[0].color_granularity=64;
		Machine.gfx[1].color_granularity=64;
		Machine.gfx[2].color_granularity=64;
		Machine.gfx[3].color_granularity=64;
	
		alpha_set_level(0x80); /* guess */
	
		return 0;
	} };
	
	
	
	static void macrossp_drawsprites( struct mame_bitmap *bitmap, const struct rectangle *cliprect, int priority )
	{
		const struct GfxElement *gfx = Machine->gfx[0];
	//	data32_t *source = macrossp_spriteram;
		data32_t *source = spriteram_old2; /* buffers by two frames */
		data32_t *finish = source + spriteram_size/4;
	
	
		while( source<finish )
		{
	
			/*
	
			 --hh hhyy yyyy yyyy   CCww wwxx xxxx xxxx
	
			 ---- --zz zzzz zzzz   ---- --ZZ ZZZZ ZZZZ
	
			 fFa- pp-- cccc c---   tttt tttt tttt tttt
	
			 */
	
	
			int wide = (source[0] & 0x00003c00) >> 10;
			int high = (source[0] & 0x3c000000) >> 26;
	
			int xpos = (source[0] & 0x000003ff) >> 0;
			int ypos = (source[0] & 0x03ff0000) >> 16;
	
			int xzoom = (source[1] & 0x000003ff) >> 0; /* 0x100 is zoom factor of 1.0 */
			int yzoom = (source[1] & 0x03ff0000) >> 16;
	
			int col;
			int tileno = (source[2] & 0x0000ffff) >> 0;
	
			int flipx = (source[2] & 0x40000000) >> 30;
			int flipy = (source[2] & 0x80000000) >> 31;
	
			int trans = (source[2] & 0x20000000)?TRANSPARENCY_ALPHA:TRANSPARENCY_PEN; /* alpha blending enable? */
	
			int loopno = 0;
	
			int xcnt,ycnt;
			int xoffset,yoffset;
	
			int pri = (source[2] & 0x0c000000) >> 26;
	
			if (pri == priority)
			{
				switch (source[0] & 0x0000c000)
				{
					case 0x00008000:
						col = (source[2] & 0x00380000) >> 17;
						break;
	
					case 0x00004000:
						col = (source[2] & 0x00f80000) >> 19;
						break;
	
					default:
						col = rand();
						break;
				}
	
	
				if (xpos > 0x1ff) xpos -=0x400;
				if (ypos > 0x1ff) ypos -=0x400;
	
				if (NOT(flipx)) {
					if (NOT(flipy)) { /* noxflip, noyflip */
						yoffset = 0; /* I'm doing this so rounding errors are cumulative, still looks a touch crappy when multiple sprites used together */
						for (ycnt = 0; ycnt <= high; ycnt++) {
							xoffset = 0;
							for (xcnt = 0; xcnt <= wide; xcnt++) {
								drawgfxzoom(bitmap,gfx,tileno+loopno,col,flipx,flipy,xpos+xoffset,ypos+yoffset,cliprect,trans,0,xzoom*0x100,yzoom*0x100);
	
								xoffset += ((xzoom*16 + (1<<7)) >> 8);
								loopno++;
							}
							yoffset += ((yzoom*16 + (1<<7)) >> 8);
						}
					}else{ /* noxflip, flipy */
						yoffset = ((high*yzoom*16) >> 8);
						for (ycnt = high; ycnt >= 0; ycnt--) {
							xoffset = 0;
							for (xcnt = 0; xcnt <= wide; xcnt++) {
								drawgfxzoom(bitmap,gfx,tileno+loopno,col,flipx,flipy,xpos+xoffset,ypos+yoffset,cliprect,trans,0,xzoom*0x100,yzoom*0x100);
	
								xoffset += ((xzoom*16 + (1<<7)) >> 8);
								loopno++;
							}
							yoffset -= ((yzoom*16 + (1<<7)) >> 8);
						}
					}
				}else{
					if (NOT(flipy)) { /* xflip, noyflip */
						yoffset = 0;
						for (ycnt = 0; ycnt <= high; ycnt++) {
							xoffset = ((wide*xzoom*16) >> 8);
							for (xcnt = wide; xcnt >= 0; xcnt--) {
								drawgfxzoom(bitmap,gfx,tileno+loopno,col,flipx,flipy,xpos+xoffset,ypos+yoffset,cliprect,trans,0,xzoom*0x100,yzoom*0x100);
	
								xoffset -= ((xzoom*16 + (1<<7)) >> 8);
								loopno++;
							}
							yoffset += ((yzoom*16 + (1<<7)) >> 8);
						}
					}else{ /* xflip, yflip */
						yoffset = ((high*yzoom*16) >> 8);
						for (ycnt = high; ycnt >= 0; ycnt--) {
							xoffset = ((wide*xzoom*16) >> 8);
							for (xcnt = wide; xcnt >=0 ; xcnt--) {
								drawgfxzoom(bitmap,gfx,tileno+loopno,col,flipx,flipy,xpos+xoffset,ypos+yoffset,cliprect,trans,0,xzoom*0x100,yzoom*0x100);
	
								xoffset -= ((xzoom*16 + (1<<7)) >> 8);
								loopno++;
							}
							yoffset -= ((yzoom*16 + (1<<7)) >> 8);
						}
					}
				}
			}
			source += 3;
		}
	}
	
	
	static void draw_layer(struct mame_bitmap *bitmap, const struct rectangle *cliprect, int layer)
	{
		struct tilemap *tm;
		data32_t *vr;
	
		switch (layer)
		{
			case 0:
			default:
				tm = macrossp_scra_tilemap;
				vr = macrossp_scra_videoregs;
				break;
	
			case 1:
				tm = macrossp_scrb_tilemap;
				vr = macrossp_scrb_videoregs;
				break;
	
			case 2:
				tm = macrossp_scrc_tilemap;
				vr = macrossp_scrc_videoregs;
				break;
		}
	
		if ((vr[2] & 0xf0000000) == 0xe0000000)	/* zoom enable (guess, surely wrong) */
		{
			int startx,starty,inc;
	
			startx = (vr[1] & 0x0000ffff) << 16;
			starty = (vr[1] & 0xffff0000) >> 0;
			inc = (vr[2] & 0x00ff0000) >> 6,
	
			/* WRONG! */
			/* scroll register contain position relative to the center of the screen, so adjust */
			startx -= (368/2) * inc;
			starty -= (240/2) * inc;
	
			tilemap_draw_roz(bitmap,cliprect,tm,
					startx,starty,inc,0,0,inc,
					1,	/* wraparound */
					0,0);
		}
		else
		{
			tilemap_set_scrollx( tm, 0, ((vr[0] & 0x000003ff) >> 0 ) );
			tilemap_set_scrolly( tm, 0, ((vr[0] & 0x03ff0000) >> 16) );
			tilemap_draw(bitmap,cliprect,tm,0,0);
		}
	}
	
	/* useful function to sort the three tile layers by priority order */
	static void sortlayers(int *layer,int *pri)
	{
	#define SWAP(a,b) \
		if (pri[a] >= pri[b]) \
		{ \
			int t; \
			t = pri[a]; pri[a] = pri[b]; pri[b] = t; \
			t = layer[a]; layer[a] = layer[b]; layer[b] = t; \
		}
	
		SWAP(0,1)
		SWAP(0,2)
		SWAP(1,2)
	}
	
	public static VideoUpdateHandlerPtr video_update_macrossp  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int layers[3],layerpri[3];
	
	
		fillbitmap(bitmap,get_black_pen(),cliprect);
	
		layers[0] = 0;
		layerpri[0] = (macrossp_scra_videoregs[0] & 0x0000c000) >> 14;
		layers[1] = 1;
		layerpri[1] = (macrossp_scrb_videoregs[0] & 0x0000c000) >> 14;
		layers[2] = 2;
		layerpri[2] = (macrossp_scrc_videoregs[0] & 0x0000c000) >> 14;
	
		sortlayers(layers, layerpri);
	
		draw_layer(bitmap,cliprect,layers[0]);
		macrossp_drawsprites(bitmap,cliprect,0);
		draw_layer(bitmap,cliprect,layers[1]);
		macrossp_drawsprites(bitmap,cliprect,1);
		draw_layer(bitmap,cliprect,layers[2]);
		macrossp_drawsprites(bitmap,cliprect,2);
		macrossp_drawsprites(bitmap,cliprect,3);
		tilemap_draw(bitmap,cliprect,macrossp_text_tilemap,0,0);
	
	#if 0
	usrintf_showmessage	("scra - %08x %08x %08x\nscrb - %08x %08x %08x\nscrc - %08x %08x %08x",
	macrossp_scra_videoregs[0]&0xffff33ff, // yyyyxxxx
	macrossp_scra_videoregs[1], // ??? more scrolling?
	macrossp_scra_videoregs[2], // 08 - 0b
	
	macrossp_scrb_videoregs[0]&0xffff33ff, // 00 - 03
	macrossp_scrb_videoregs[1], // 04 - 07
	macrossp_scrb_videoregs[2], // 08 - 0b
	
	macrossp_scrc_videoregs[0]&0xffff33ff, // 00 - 03
	macrossp_scrc_videoregs[1], // 04 - 07
	macrossp_scrc_videoregs[2]);// 08 - 0b
	#endif
	} };
	
	public static VideoEofHandlerPtr video_eof_macrossp  = new VideoEofHandlerPtr() { public void handler(){
		/* looks like sprites are *two* frames ahead, like nmk16 */
		memcpy(spriteram_old2,spriteram_old,spriteram_size[0]);
		memcpy(spriteram_old,macrossp_spriteram,spriteram_size[0]);
	} };
}
