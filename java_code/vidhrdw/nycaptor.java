/***************************************************************************

  vidhrdw.c

  Functions to emulate the video hardware of the machine.

***************************************************************************/
/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class nycaptor
{
	
	#define nycaptor_spot (nycaptor_sharedram[0x299]?nycaptor_sharedram[0x298]:0)
	/*
	 298 (e298) - spot (0-3) , 299 (e299) - lives
	 spot number isn't set to 0 in main menu ; lives - yes
	 sprites in main menu req priority 'type' 0
	*/
	
	
	#ifdef MAME_DEBUG
	 int nycaptor_mask=0;
	#endif
	
	static struct tilemap *tilemap;
	static int char_bank,palette_bank,gfxctrl;
	
	UINT8 *nycaptor_scrlram;
	
	UINT8 *nycaptor_spriteram;
	public static WriteHandlerPtr nycaptor_spriteram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		nycaptor_spriteram[offset]=data;
	} };
	
	public static ReadHandlerPtr nycaptor_spriteram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return nycaptor_spriteram[offset];
	} };
	
	public static GetTileInfoHandlerPtr get_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) 
	{
		int flags,pal;
		tile_info.priority = (videoram.read(tile_index*2 + 1)& 0x30)>>4;
		pal=videoram.read(tile_index*2+1)&0x0f;
	  flags=TILE_SPLIT(0);
	  if((!nycaptor_spot)&&(pal==6))flags=TILE_SPLIT(1);
		if(((nycaptor_spot==3)&&(pal==8))||((nycaptor_spot==1)&&(pal==0xc)))flags=TILE_SPLIT(2);
		if((nycaptor_spot==1)&&(tile_info.priority==2))flags=TILE_SPLIT(3);
	#ifdef MAME_DEBUG
	  if(nycaptor_mask&(1<<tile_info.priority))
	  {
	    if(nycaptor_spot)pal=0xe;else pal=4;
	  }
	#endif
	
		SET_TILE_INFO(
				0,
				videoram.read(tile_index*2)+ ((videoram.read(tile_index*2+1)& 0xc0) << 2) +0x400 * char_bank,
				pal,flags;
				)
	} };
	
	
	public static VideoStartHandlerPtr video_start_nycaptor  = new VideoStartHandlerPtr() { public int handler(){
	  nycaptor_spriteram = auto_malloc (160);
	  tilemap = tilemap_create( get_tile_info,tilemap_scan_rows,TILEMAP_SPLIT,8,8,32,32 );
	
	  tilemap_set_transmask(tilemap,0,0xf800,0x7ff); //split 0
	  tilemap_set_transmask(tilemap,1,0xfe00,0x01ff);//split 1
	  tilemap_set_transmask(tilemap,2,0xfffc,0x0003);//split 2
	  tilemap_set_transmask(tilemap,3,0xfff0,0x000f);//split 3
	
		paletteram = auto_malloc(0x200);
		paletteram_2 = auto_malloc(0x200);
		tilemap_set_scroll_cols(tilemap,32);
		return video_start_generic.handler();
	} };
	
	public static WriteHandlerPtr nycaptor_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		videoram.write(offset,data);
		tilemap_mark_tile_dirty(tilemap,offset>>1);
	} };
	
	public static ReadHandlerPtr nycaptor_videoram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return videoram.read(offset);
	} };
	
	public static WriteHandlerPtr nycaptor_palette_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (offset & 0x100)
			paletteram_xxxxBBBBGGGGRRRR_split2_w((offset & 0xff) + (palette_bank << 8),data);
		else
			paletteram_xxxxBBBBGGGGRRRR_split1_w((offset & 0xff) + (palette_bank << 8),data);
	} };
	
	public static ReadHandlerPtr nycaptor_palette_r  = new ReadHandlerPtr() { public int handler(int offset){
		if (offset & 0x100)
			return paletteram_2.read( (offset & 0xff) + (palette_bank << 8) );
		else
			return paletteram  [ (offset & 0xff) + (palette_bank << 8) ];
	} };
	
	public static WriteHandlerPtr nycaptor_gfxctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (gfxctrl == data)
			return;
		gfxctrl = data;
	
		if(char_bank != ((data & 0x18) >> 3))
		{
			char_bank=((data & 0x18) >> 3);
			tilemap_mark_all_tiles_dirty( tilemap );
		}
		palette_bank = (data & 0x20) >> 5;
	
	} };
	
	public static ReadHandlerPtr nycaptor_gfxctrl_r  = new ReadHandlerPtr() { public int handler(int offset){
			return 	gfxctrl;
	} };
	
	public static ReadHandlerPtr nycaptor_scrlram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return nycaptor_scrlram[offset];
	} };
	
	public static WriteHandlerPtr nycaptor_scrlram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		nycaptor_scrlram[offset] = data;
		tilemap_set_scrolly(tilemap, offset, data );
	} };
	
	void nycaptor_draw_sprites(struct mame_bitmap *bitmap, const struct rectangle *cliprect,int pri)
	{
		int i;
		for (i=0;i<0x20;i++)
		{
			int pr = nycaptor_spriteram[0x9f-i];
			int offs = (pr & 0x1f) * 4;
			{
				int code,sx,sy,flipx,flipy,pal,priori;
				code = nycaptor_spriteram[offs+2] + ((nycaptor_spriteram[offs+1] & 0x10) << 4);//1 bit wolny = 0x20
				pal=nycaptor_spriteram[offs+1] & 0x0f;
				sx = nycaptor_spriteram[offs+3];
				sy = 240-nycaptor_spriteram[offs+0];
				priori=(pr&0xe0)>>5;
	      if(priori==pri)
	      {
	#ifdef MAME_DEBUG
	      if(nycaptor_mask&(1<<(pri+4)))pal=0xd;
	#endif
				flipx = ((nycaptor_spriteram[offs+1]&0x40)>>6);
				flipy = ((nycaptor_spriteram[offs+1]&0x80)>>7);
	
				drawgfx(bitmap,Machine->gfx[1],
						code,
						pal,
						flipx,flipy,
						sx,sy,
						cliprect,TRANSPARENCY_PEN,15);
	
				if(nycaptor_spriteram[offs+3]>240)
				{
					sx = (nycaptor_spriteram[offs+3]-256);
					drawgfx(bitmap,Machine->gfx[1],
	        				code,
					        pal,
					        flipx,flipy,
						      sx,sy,
						      cliprect,TRANSPARENCY_PEN,15);
						}
					}
			}
		}
	}
	
	
	
	
	
	#ifdef MAME_DEBUG
	/*
	 Keys :
	   q/w/e/r - bg priority display select
	   a/s/d/f/g/h/j/k - sprite priority display select
	   z - clear
	   x - no bg/sprite pri.
	*/
	
	#define mKEY_MASK(x,y) if (keyboard_pressed_memory(x)){nycaptor_mask|=y;tilemap_mark_all_tiles_dirty( tilemap );}
	
	void nycaptor_setmask(void)
	{
	  mKEY_MASK(KEYCODE_Q,1); /* bg */
	  mKEY_MASK(KEYCODE_W,2);
	  mKEY_MASK(KEYCODE_E,4);
	  mKEY_MASK(KEYCODE_R,8);
	
	  mKEY_MASK(KEYCODE_A,0x10); /* sprites */
	  mKEY_MASK(KEYCODE_S,0x20);
	  mKEY_MASK(KEYCODE_D,0x40);
	  mKEY_MASK(KEYCODE_F,0x80);
	  mKEY_MASK(KEYCODE_G,0x100);
	  mKEY_MASK(KEYCODE_H,0x200);
	  mKEY_MASK(KEYCODE_J,0x400);
	  mKEY_MASK(KEYCODE_K,0x800);
	
	  if (keyboard_pressed_memory(KEYCODE_Z)){nycaptor_mask=0;tilemap_mark_all_tiles_dirty( tilemap );} /* disable */
	  if (keyboard_pressed_memory(KEYCODE_X)){nycaptor_mask|=0x1000;tilemap_mark_all_tiles_dirty( tilemap );} /* no layers */
	}
	#endif
	
	public static VideoUpdateHandlerPtr video_update_nycaptor  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
	#ifdef MAME_DEBUG
	  nycaptor_setmask();
	  if(nycaptor_mask&0x1000)
	  {
	     	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|3,0);
	     	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|3,0);
	     	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|2,0);
	     	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|2,0);
	     	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|1,0);
	     	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|1,0);
	     	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|0,0);
	     	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|0,0);
	     	nycaptor_draw_sprites(bitmap,cliprect,0);
	     	nycaptor_draw_sprites(bitmap,cliprect,1);
	     	nycaptor_draw_sprites(bitmap,cliprect,2);
	     	nycaptor_draw_sprites(bitmap,cliprect,3);
	     	nycaptor_draw_sprites(bitmap,cliprect,4);
	     	nycaptor_draw_sprites(bitmap,cliprect,5);
	     	nycaptor_draw_sprites(bitmap,cliprect,6);
	     	nycaptor_draw_sprites(bitmap,cliprect,7);
	  }
	 else
	#endif
	 switch (nycaptor_spot&3)
	 {
	  case 0:
	  	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|3,0);
	    nycaptor_draw_sprites(bitmap,cliprect,6);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|3,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|2,0);
		  tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|2,0);
	   	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|1,0);
	    nycaptor_draw_sprites(bitmap,cliprect,3);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|1,0);
	    nycaptor_draw_sprites(bitmap,cliprect,0);
	    nycaptor_draw_sprites(bitmap,cliprect,2);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|0,0);
	    nycaptor_draw_sprites(bitmap,cliprect,1);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|0,0);
	  break;
	
	  case 1:
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|3,0);
	    nycaptor_draw_sprites(bitmap,cliprect,3);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|3,0);
	    nycaptor_draw_sprites(bitmap,cliprect,2);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|2,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|1,0);
	    nycaptor_draw_sprites(bitmap,cliprect,1);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|1,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|2,0);
	    nycaptor_draw_sprites(bitmap,cliprect,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|0,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|0,0);
	  break;
	
	  case 2:
	   	tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|3,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|3,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|1,0);
	    nycaptor_draw_sprites(bitmap,cliprect,1);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|1,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|2,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|2,0);
	    nycaptor_draw_sprites(bitmap,cliprect,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|0,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|0,0);
	  break;
	
	  case 3:
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|1,0);
	    nycaptor_draw_sprites(bitmap,cliprect,1);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|1,0);
	    nycaptor_draw_sprites(bitmap,cliprect,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_BACK|0,0);
	    tilemap_draw(bitmap,cliprect,tilemap,TILEMAP_FRONT|0,0);
	  break;
	 }
	 	draw_crosshair(bitmap,readinputport(5),readinputport(6),cliprect);
	} };
	
}
