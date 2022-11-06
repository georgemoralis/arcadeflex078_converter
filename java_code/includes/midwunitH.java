/*************************************************************************

	Driver for Midway Wolf-unit games.

**************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.includes;

public class midwunitH
{
	
	/*----------- defined in machine/midwolfu.c -----------*/
	
	
	WRITE16_HANDLER( midwunit_cmos_enable_w );
	WRITE16_HANDLER( midwunit_cmos_w );
	WRITE16_HANDLER( midxunit_cmos_w );
	READ16_HANDLER( midwunit_cmos_r );
	
	WRITE16_HANDLER( midwunit_io_w );
	WRITE16_HANDLER( midxunit_io_w );
	WRITE16_HANDLER( midxunit_unknown_w );
	
	READ16_HANDLER( midwunit_io_r );
	READ16_HANDLER( midxunit_io_r );
	READ16_HANDLER( midxunit_analog_r );
	WRITE16_HANDLER( midxunit_analog_select_w );
	READ16_HANDLER( midxunit_status_r );
	
	READ16_HANDLER( midxunit_uart_r );
	WRITE16_HANDLER( midxunit_uart_w );
	
	
	
	
	READ16_HANDLER( midwunit_security_r );
	WRITE16_HANDLER( midwunit_security_w );
	WRITE16_HANDLER( midxunit_security_w );
	WRITE16_HANDLER( midxunit_security_clock_w );
	
	READ16_HANDLER( midwunit_sound_r );
	WRITE16_HANDLER( midwunit_sound_w );
}
