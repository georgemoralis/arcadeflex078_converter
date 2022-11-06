//============================================================
//
//	config.h - Win32 configuration routines
//
//============================================================

#ifndef _WIN_CONFIG__
#define _WIN_CONFIG__

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.windows;

public class configH
{
	
	// Initializes and exits the configuration system
	int  cli_frontend_init (int argc, char **argv);
	void cli_frontend_exit (void);
	
	// Creates an RC object
	struct rc_struct *cli_rc_create(void);
	
	#endif // _WIN_CONFIG__
}
