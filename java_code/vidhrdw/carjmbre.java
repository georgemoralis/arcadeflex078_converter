/*
Car Jamboree
Omori Electric CAD (OEC) 1981
*/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class carjmbre
{
	
	static struct tilemap *carjmbre_tilemap;
	
	static int carjmbre_flipscreen, carjmbre_bgcolor;
	
	public static PaletteInitHandlerPtr palette_init_carjmbre  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom){
		int i,bit0,bit1,bit2,r,g,b;
	
		for (i = 0;i < Machine.drv.total_colors; i++)
		{
			/* red component */
			bit0 = (color_prom.read()>> 0) & 0x01;
			bit1 = (color_prom.read()>> 1) & 0x01;
			bit2 = (color_prom.read()>> 2) & 0x01;
			r = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* green component */
			bit0 = (color_prom.read()>> 3) & 0x01;
			bit1 = (color_prom.read()>> 4) & 0x01;
			bit2 = (color_prom.read()>> 5) & 0x01;
			g = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
			/* blue component */
			bit0 = 0;
			bit1 = (color_prom.read()>> 6) & 0x01;
			bit2 = (color_prom.read()>> 7) & 0x01;
			b = 0x21 * bit0 + 0x47 * bit1 + 0x97 * bit2;
	
			palette_set_color(i,r,g,b);
			color_prom++;
		}
	} };
	
	public static WriteHandlerPtr carjmbre_flipscreen_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		carjmbre_flipscreen = data?(TILEMAP_FLIPX|TILEMAP_FLIPY):0;
		tilemap_set_flip( ALL_TILEMAPS,carjmbre_flipscreen );
	} };
	
	public static WriteHandlerPtr carjmbre_bgcolor_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		int oldbg,i;
	
		oldbg=carjmbre_bgcolor;
	
		carjmbre_bgcolor&=0xff00>>(offset*8);
		carjmbre_bgcolor|=((~data)&0xff)<<(offset*8);
	
		if(oldbg!=carjmbre_bgcolor)
		{
			memset(dirtybuffer,1,videoram_size[0]);
	
			for (i=0;i<64;i+=4)
				palette_set_color(i, (carjmbre_bgcolor&0xff)*0x50, (carjmbre_bgcolor&0xff)*0x50, (carjmbre_bgcolor&0xff)!=0?0x50:0);
		}
	} };
	
	public static GetTileInfoHandlerPtr get_carjmbre_tile_info = new GetTileInfoHandlerPtr() { public void handler(int tile_index) {
		unsigned int tile_number = videoram.read(tile_index)& 0xFF;
		unsigned char attr  = videoram.read(tile_index+0x400);
		tile_number += (attr & 0x80) << 1; /* bank */
		SET_TILE_INFO(
				0,
				tile_number,
				(attr&0x7),
				0)
	} };
	
	public static WriteHandlerPtr carjmbre_videoram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
		videoram.write(offset,data);
		tilemap_mark_tile_dirty(carjmbre_tilemap,offset&0x3ff);
	}
	
	
	
	public static VideoStartHandlerPtr video_start_carjmbre  = new VideoStartHandlerPtr() { public int handler(){
	
		carjmbre_tilemap = tilemap_create( get_carjmbre_tile_info,tilemap_scan_rows,TILEMAP_OPAQUE,8,8,32,32 );
	
		state_save_register_int ("video", 0, "flipscreen", &carjmbre_flipscreen);
		state_save_register_int ("video", 0, "bgcolor",    &carjmbre_bgcolor);
	
		return 0;
	} };
	
	public static VideoUpdateHandlerPtr video_update_carjmbre  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect){
		int offs,troffs,sx,sy,flipx,flipy;
	
		//colorram
		//76543210
		//x------- graphic bank
		//-xxx---- unused
		//----x--- ?? probably colour, only used for ramp and pond
		//-----xxx colour
	
		tilemap_draw( bitmap,cliprect,carjmbre_tilemap,0,0 );
	
		//spriteram.read(offs)
		//+0       y pos
		//+1       sprite number
		//+2
		//76543210
		//x------- flipy
		//-x------ flipx
		//--xx---- unused
		//----x--- ?? probably colour
		//-----xxx colour
		//+3       x pos
		for (offs = spriteram_size[0]-4; offs >= 0; offs-=4)
		{
			//before copying the sprites to spriteram the game reorders the first
			//sprite to last, sprite ordering is incorrect if this isn't undone
			troffs=(offs-4+spriteram_size[0])%spriteram_size[0];
	
			//unused sprites are marked with ypos <= 0x02 (or >= 0xfd if screen flipped)
			if (spriteram.read(troffs)> 0x02 && spriteram.read(troffs)< 0xfd)
			{
				{
					sx = spriteram.read(troffs+3)-7;
					sy = 241-spriteram.read(troffs);
					flipx = (spriteram.read(troffs+2)&0x40)>>6;
					flipy = (spriteram.read(troffs+2)&0x80)>>7;
	
					if (carjmbre_flipscreen)
					{
						sx = (256+(226-sx))%256;
						sy = 242-sy;
						flipx = NOT(flipx);
						flipy = NOT(flipy);
					}
	
					drawgfx(bitmap,Machine.gfx[1],
							spriteram.read(troffs+1),
							spriteram.read(troffs+2)&0x07,
							flipx,flipy,
							sx,sy,
							cliprect,TRANSPARENCY_PEN,0);
				}
			}
		}
	} };
}
