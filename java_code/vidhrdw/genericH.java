/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.vidhrdw;

public class genericH
{
	
	#ifdef __cplusplus
	#endif
	
	
	
	void video_stop_generic(void);
	void video_stop_generic_bitmapped(void);
	
	READ16_HANDLER( spriteram16_r );
	WRITE16_HANDLER( spriteram16_w );
	WRITE16_HANDLER( buffer_spriteram16_w );
	WRITE32_HANDLER( buffer_spriteram32_w );
	WRITE16_HANDLER( buffer_spriteram16_2_w );
	WRITE32_HANDLER( buffer_spriteram32_2_w );
	void buffer_spriteram(unsigned char *ptr,int length);
	void buffer_spriteram_2(unsigned char *ptr,int length);
	
	/* screen flipping */
	void flip_screen_set(int on);
	void flip_screen_x_set(int on);
	void flip_screen_y_set(int on);
	#define flip_screen() flip_screen_x
	
	/* sets a variable and schedules a full screen refresh if it changed */
	void set_vh_global_attribute(int *addr, int data);
	int get_vh_global_attribute_changed(void);
	
	
	#ifdef __cplusplus
	}
	#endif
}
