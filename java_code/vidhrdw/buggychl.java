/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class buggychl
{
	
	
	
	unsigned char *buggychl_scrollv,*buggychl_scrollh;
	unsigned char buggychl_sprite_lookup[0x2000];
	unsigned char *buggychl_character_ram;
	
	static int *dirtychar;
	
	static struct mame_bitmap *tmpbitmap1,*tmpbitmap2;
	static int sl_bank,bg_on,sky_on,sprite_color_base,bg_scrollx;
	
	
	
	public static PaletteInitHandlerPtr palette_init_buggychl  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i;
	
		/* arbitrary blue shading for the sky */
		for (i = 0;i < 128;i++)
			palette_set_color(i+128,0,i,2*i);
	} };
	
	
	
	
	public static VideoStartHandlerPtr video_start_buggychl  = new VideoStartHandlerPtr() { public int handler(){
		dirtybuffer = auto_malloc(videoram_size[0]);
		dirtychar = auto_malloc(256 * sizeof(*dirtychar));
		tmpbitmap1 = auto_bitmap_alloc(256,256);
		tmpbitmap2 = auto_bitmap_alloc(256,256);
	
		if (!dirtybuffer || !dirtychar || !tmpbitmap1 || !tmpbitmap2)
			return 1;
	
		memset(dirtybuffer,1,videoram_size[0]);
		memset(dirtychar,0xff,256 * sizeof(*dirtychar));
	
		return 0;
	} };
	
	
	
	public static WriteHandlerPtr buggychl_chargen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (buggychl_character_ram[offset] != data)
		{
			buggychl_character_ram[offset] = data;
	
			dirtychar[(offset / 8) & 0xff] = 1;
		}
	} };
	
	public static WriteHandlerPtr buggychl_sprite_lookup_bank_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		sl_bank = (data & 0x10) << 8;
	} };
	
	public static WriteHandlerPtr buggychl_sprite_lookup_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		buggychl_sprite_lookup[offset + sl_bank] = data;
	} };
	
	public static WriteHandlerPtr buggychl_ctrl_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	/*
		bit7 = lamp
		bit6 = lockout
		bit4 = OJMODE
		bit3 = SKY OFF
		bit2 = /SN3OFF
		bit1 = HINV
		bit0 = VINV
	*/
	
		flip_screen_y_set(data & 0x01);
		flip_screen_x_set(data & 0x02);
	
		bg_on = data & 0x04;
		sky_on = data & 0x08;
	
		sprite_color_base = (data & 0x10) ? 1*16 : 3*16;
	
		coin_lockout_global_w((~data & 0x40) >> 6);
		set_led_status(0,~data & 0x80);
	} };
	
	public static WriteHandlerPtr buggychl_bg_scrollx_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		bg_scrollx = -(data - 0x12);
	} };
	
	
	
	
	static void draw_sky(struct mame_bitmap *bitmap)
	{
		int x,y;
	
		for (y = 0;y < 256;y++)
		{
			for (x = 0;x < 256;x++)
			{
				plot_pixel(bitmap,x,y,Machine->pens[128 + x/2]);
			}
		}
	}
	
	
	static void draw_bg(struct mame_bitmap *bitmap)
	{
		int offs;
		int scroll[256];
	
		for (offs = 0;offs < 0x400;offs++)
		{
			int code = videoram.read(0x400+offs);
	
			if (dirtybuffer[0x400+offs] || dirtychar[code])
			{
				int sx = offs % 32;
				int sy = offs / 32;
	
				dirtybuffer[0x400+offs] = 0;
	
				if (flip_screen_x) sx = 31 - sx;
				if (flip_screen_y) sy = 31 - sy;
	
				drawgfx(tmpbitmap1,Machine->gfx[0],
						code,
						2,
						flip_screen_x,flip_screen_y,
						8*sx,8*sy,
						NULL,TRANSPARENCY_NONE,0);
			}
		}
	
		/* first copy to a temp bitmap doing column scroll */
		for (offs = 0;offs < 256;offs++)
			scroll[offs] = -buggychl_scrollv[offs/8];
	
		copyscrollbitmap(tmpbitmap2,tmpbitmap1,1,&bg_scrollx,256,scroll,NULL,TRANSPARENCY_NONE,0);
	
		/* then copy to the screen doing row scroll */
		for (offs = 0;offs < 256;offs++)
			scroll[offs] = -buggychl_scrollh[offs];
	
		copyscrollbitmap(bitmap,tmpbitmap2,256,scroll,0,0,Machine->visible_area,TRANSPARENCY_COLOR,32);
	}
	
	
	static void draw_fg(struct mame_bitmap *bitmap)
	{
		int offs;
	
	
		for (offs = 0;offs < 0x400;offs++)
		{
			int sx = offs % 32;
			int sy = offs / 32;
			/* the following line is most likely wrong */
			int transp = (bg_on && sx >= 22) ? TRANSPARENCY_NONE : TRANSPARENCY_PEN;
	
			int code = videoram.read(offs);
	
			if (flip_screen_x) sx = 31 - sx;
			if (flip_screen_y) sy = 31 - sy;
	
			drawgfx(bitmap,Machine->gfx[0],
					code,
					0,
					flip_screen_x,flip_screen_y,
					8*sx,8*sy,
					Machine->visible_area,transp,0);
		}
	}
	
	
	static void draw_sprites(struct mame_bitmap *bitmap)
	{
		int offs;
	
		profiler_mark(PROFILER_USER1);
	
		for (offs = 0;offs < spriteram_size;offs += 4)
		{
			int sx,sy,flipy,zoom,ch,x,px,y;
			const unsigned char *lookup;
			const unsigned char *zoomx_rom,*zoomy_rom;
	
	
			sx = spriteram.read(offs+3)- ((spriteram.read(offs+2)& 0x80) << 1);
			sy = 256-64 - spriteram.read(offs)+ ((spriteram.read(offs+1)& 0x80) << 1);
			flipy = spriteram.read(offs+1)& 0x40;
			zoom = spriteram.read(offs+1)& 0x3f;
			zoomy_rom = memory_region(REGION_GFX2) + (zoom << 6);
			zoomx_rom = memory_region(REGION_GFX2) + 0x2000 + (zoom << 3);
	
			lookup = buggychl_sprite_lookup + ((spriteram.read(offs+2)& 0x7f) << 6);
	
			for (y = 0;y < 64;y++)
			{
				int dy = flip_screen_y ? (255 - sy - y) : (sy + y);
	
				if ((dy & ~0xff) == 0)
				{
					int charline,base_pos;
	
					charline = zoomy_rom[y] & 0x07;
					base_pos = zoomy_rom[y] & 0x38;
					if (flipy) base_pos ^= 0x38;
	
					px = 0;
					for (ch = 0;ch < 4;ch++)
					{
						int pos,code,realflipy;
						const UINT8 *pendata;
	
						pos = base_pos + 2*ch;
						code = 8 * (lookup[pos] | ((lookup[pos+1] & 0x07) << 8));
						realflipy = (lookup[pos+1] & 0x80) ? NOT(flipy) : flipy;
						code += (realflipy ? (charline ^ 7) : charline);
						pendata = Machine->gfx[1]->gfxdata + code*16;
	
						for (x = 0;x < 16;x++)
						{
							int col;
	
							col = pendata[x];
							if (col)
							{
								int dx = flip_screen_x ? (255 - sx - px) : (sx + px);
								if ((dx & ~0xff) == 0)
									plot_pixel(bitmap,dx,dy,Machine->pens[sprite_color_base+col]);
							}
	
							/* the following line is almost certainly wrong */
							if (zoomx_rom[7-(2*ch+x/8)] & (1 << (x & 7)))
								px++;
						}
					}
				}
			}
		}
	
		profiler_mark(PROFILER_END);
	}
	
	
	public static VideoUpdateHandlerPtr video_update_buggychl  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int code;
	
	
		if (sky_on)
			draw_sky(bitmap);
		else
			fillbitmap(bitmap,Machine.pens[0],Machine.visible_area);
	
		/* decode modified characters */
		for (code = 0;code < 256;code++)
		{
			if (dirtychar[code])
				decodechar(Machine.gfx[0],code,buggychl_character_ram,Machine.drv.gfxdecodeinfo[0].gfxlayout);
		}
	
		if (bg_on)
			draw_bg(bitmap);
	
		draw_sprites(bitmap);
	
		draw_fg(bitmap);
	
		for (code = 0;code < 256;code++)
			dirtychar[code] = 0;
	} };
}
