//============================================================
//
//	video.h - Win32 implementation of MAME video routines
//
//============================================================

#ifndef __WIN_VIDEO__
#define __WIN_VIDEO__


//============================================================
//	PARAMETERS
//============================================================

// maximum video size
#define MAX_VIDEO_WIDTH			1600
#define MAX_VIDEO_HEIGHT		1200



//============================================================
//	GLOBAL VARIABLES
//============================================================

// current frameskip/autoframeskip settings

// speed throttling

// palette lookups

// rotation



//============================================================
//	PROTOTYPES
//============================================================

void win_orient_rect(struct rectangle *rect);
void win_disorient_rect(struct rectangle *rect);

#endif
