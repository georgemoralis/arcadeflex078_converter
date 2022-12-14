#ifndef _WINDOW_H_
#define	_WINDOW_H_

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.mame;

public class windowH
{
	
	#ifdef  GNU
	#define ARGFMT  __attribute__((format(printf,2,3)))
	#else
	#define ARGFMT
	#endif
	
	#ifndef DECL_SPEC
	#define DECL_SPEC
	#endif
	
	#ifndef TRUE
	#define TRUE    1
	#endif
	
	#ifndef FALSE
	#define FALSE   0
	#endif
	
	#ifndef INVALID
	#define INVALID 0xffffffff
	#endif
	
	#ifndef WIN_EMPTY
	#define WIN_EMPTY   176 /* checkered pattern */
	#endif
	#ifndef CAPTION_L
	#define CAPTION_L   174 /* >> */
	#endif
	#ifndef CAPTION_R
	#define CAPTION_R   175 /* << */
	#endif
	#ifndef FRAME_TL
	#define FRAME_TL    218 /* top, left border */
	#endif
	#ifndef FRAME_BL
	#define FRAME_BL    192 /* bottom, left border */
	#endif
	#ifndef FRAME_TR
	#define FRAME_TR    191 /* top, right border */
	#endif
	#ifndef FRAME_BR
	#define FRAME_BR    217 /* bottom, right border */
	#endif
	#ifndef FRAME_V
	#define FRAME_V     179 /* vertical line */
	#endif
	#ifndef FRAME_H
	#define FRAME_H     196 /* horizontal line */
	#endif
	
	/* This is our window structure */
	
	struct sWindow
	{
		UINT8 filler;		/* Character */
		UINT8 prio; 		/* This window's priority */
		UINT32 x;			/* X Position (in characters) of our window */
		UINT32 y;			/* Y Position (in characters) of our window */
		UINT32 w;			/* X Size of our window (in characters) */
		UINT32 h;			/* Y Size (lines) of our window (in character lengths) */
		UINT32 cx;			/* Current cursor's X position */
		UINT32 cy;			/* Current cursor's Y position */
		UINT32 flags;		/* Window's attributes (below) */
		UINT8 co_text;		/* Default color */
		UINT8 co_frame; 	/* Frame color */
		UINT8 co_title; 	/* Title color */
		UINT8 saved_text;	/* Character under the cursor position */
		UINT8 saved_attr;	/* Attribute under the cursor position */
	
		/* Stuff that needs to be saved off differently */
	
		char	*title; /* Window title (if any) */
		UINT8	*text;	/* Pointer to video data - characters */
		UINT8	*attr;	/* Pointer to video data - attributes */
	
		/* These are the callbacks when certain things happen. All fields have been
		 * updated BEFORE the call. Return FALSE if the moves, resizes, closes,
		 * refocus aren't accepted.
		 */
	
		UINT32 (*Resize)(UINT32 idx, struct sWindow *);
		UINT32 (*Close)(UINT32 idx, struct sWindow *);
		UINT32 (*Move)(UINT32 idx, struct sWindow *);
		UINT32 (*Refocus)(UINT32 idx, struct sWindow *);  /* Bring it to the front */
	};
	
	/* These defines are for various aspects of the window */
	
	#define BORDER_LEFT 		0x01	/* Border on left side of window */
	#define BORDER_RIGHT		0x02	/* Border on right side of window */
	#define BORDER_TOP			0x04	/* Border on top side of window */
	#define BORDER_BOTTOM		0x08	/* Border on bottom side of window */
	#define HIDDEN				0x10	/* Is it hidden currently? */
	#define CURSOR_ON			0x20	/* Is the cursor on? */
	#define NO_WRAP 			0x40	/* Do we actually wrap at the right side? */
	#define NO_SCROLL			0x80	/* Do we actually scroll it? */
	#define SHADOW				0x100	/* Do we cast a shadow? */
	#define MOVEABLE			0x200	/* Is this Window moveable? */
	#define RESIZEABLE			0x400	/* IS this Window resiable? */
	
	#define MAX_WINDOWS 		32		/* Up to 32 windows active at once */
	#define TAB_STOP			8		/* 8 Spaces for a tab stop! */
	
	#define AUTO_FIX_XYWH		TRUE
	#define NEWLINE_ERASE_EOL	TRUE	/* Shall newline also erase to end of line? */
	
	/* Special characters */
	
	#define CHAR_CURSORON		219 /* Cursor on character */
	#define CHAR_CURSOROFF		32	/* Cursor off character */
	
	/* Standard color set for IBM character set. DO NOT ALTER! */
	
	#define WIN_BLACK			DBG_BLACK
	#define WIN_BLUE			DBG_BLUE
	#define WIN_GREEN			DBG_GREEN
	#define WIN_CYAN			DBG_CYAN
	#define WIN_RED 			DBG_RED
	#define WIN_MAGENTA 		DBG_MAGENTA
	#define WIN_BROWN			DBG_BROWN
	#define WIN_WHITE			DBG_LIGHTGRAY
	#define WIN_GRAY			DBG_GRAY
	#define WIN_LIGHT_BLUE		DBG_LIGHTBLUE
	#define WIN_LIGHT_GREEN 	DBG_LIGHTGREEN
	#define WIN_LIGHT_CYAN		DBG_LIGHTCYAN
	#define WIN_LIGHT_RED		DBG_LIGHTRED
	#define WIN_LIGHT_MAGENTA	DBG_LIGHTMAGENTA
	#define WIN_YELLOW			DBG_YELLOW
	#define WIN_BRIGHT_WHITE	DBG_WHITE
	
	#define	WIN_BRIGHT	0x08
	
	/* Externs! */
	
	
	
	#endif
}
