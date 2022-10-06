/*###################################################################################################
**
**	TMS34010: Portable Texas Instruments TMS34010 emulator
**
**	Copyright (C) Alex Pasadyn/Zsolt Vasvari 1998
**	 Parts based on code by Aaron Giles
**
**#################################################################################################*/

#ifndef RECURSIVE_INCLUDE

#if LOG_GRAPHICS_OPS
#define LOGGFX(x) do { if (keyboard_pressed(KEYCODE_L)) logerror x ; } while (0)
#else
#define LOGGFX(x)
#endif


/* Graphics Instructions */

static void line(void)
{
	if (!P_FLAG)
	{
		if (state.window_checking != 0 && state.window_checking != 3)
			logerror("LINE XY  %08X - Window Checking Mode %d not supported\n", PC, state.window_checking);

		P_FLAG = 1;
		TEMP = (state.op & 0x80) ? 1 : 0;  /* boundary value depends on the algorithm */
		LOGGFX(("%08X(%3d):LINE (%d,%d)-(%d,%d)\n", PC, cpu_getscanline(), DADDR_X, DADDR_Y, DADDR_X + DYDX_X, DADDR_Y + DYDX_Y));
	}

	if (COUNT > 0)
	{
		INT16 x1,y1;

		COUNT--;
		if (state.window_checking != 3 ||
			(DADDR_X >= WSTART_X && DADDR_X <= WEND_X &&
			 DADDR_Y >= WSTART_Y && DADDR_Y <= WEND_Y))
			WPIXEL(DXYTOL(DADDR_XY),COLOR1);

		if (SADDR >= TEMP)
		{
			SADDR += DYDX_Y*2 - DYDX_X*2;
			x1 = INC1_X;
			y1 = INC1_Y;
		}
		else
		{
			SADDR += DYDX_Y*2;
			x1 = INC2_X;
			y1 = INC2_Y;
		}
		DADDR_X += x1;
		DADDR_Y += y1;

		COUNT_UNKNOWN_CYCLES(2);
		PC -= 0x10;  /* not done yet, check for interrupts and restart instruction */
		return;
	}
	P_FLAG = 0;
}


/*
cases:
* window modes (0,1,2,3)
* boolean/arithmetic ops (16+6)
* transparency (on/off)
* plane masking
* directions (left->right/right->left, top->bottom/bottom->top)
*/

static int apply_window(const char *inst_name,int srcbpp, UINT32 *srcaddr, XY *dst, int *dx, int *dy)
{
	/* apply the window */
	if (state.window_checking == 0)
		return 0;
	else
	{
		int sx = dst->x;
		int sy = dst->y;
		int ex = sx + *dx - 1;
		int ey = sy + *dy - 1;
		int diff, cycles = 3;

		if (state.window_checking == 1 || state.window_checking == 2)
			logerror("%08x: %s apply_window window mode %d not supported!\n", activecpu_get_pc(), inst_name, state.window_checking);

		if (state.window_checking == 1)
			V_FLAG = 1;
		else
			CLR_V;	/* clear the V flag by default */

		/* clip X */
		diff = WSTART_X - sx;
		if (diff > 0)
		{
			if (srcaddr)
				*srcaddr += diff * srcbpp;
			sx += diff;
			V_FLAG = 1;
		}
		diff = ex - WEND_X;
		if (diff > 0)
		{
			ex -= diff;
			V_FLAG = 1;
		}

		/* clip Y */
		diff = WSTART_Y - sy;
		if (diff > 0)
		{
			if (srcaddr)
				*srcaddr += diff * SPTCH;
			sy += diff;
			V_FLAG = 1;
		}
		diff = ey - WEND_Y;
		if (diff > 0)
		{
			ey -= diff;
			V_FLAG = 1;
		}

		/* compute cycles */
		if (*dx != ex - sx + 1 || *dy != ey - sy + 1)
		{
			if (dst->x != sx || dst->y != sy)
				cycles += 11;
			else
				cycles += 3;
		}
		else if (dst->x != sx || dst->y != sy)
			cycles += 7;

		/* update the values */
		dst->x = sx;
		dst->y = sy;
		*dx = ex - sx + 1;
		*dy = ey - sy + 1;
		return cycles;
	}
}


/*******************************************************************

	About the timing of gfx operations:

	The 34010 manual lists a fairly intricate and accurate way of
	computing cycle timings for graphics ops. However, there are
	enough typos and misleading statements to make the reliability
	of the timing info questionable.

	So, to address this, here is a simplified approximate version
	of the timing.

		timing = setup + (srcwords * 2 + dstwords * gfxop) * rows

	Each read access takes 2 cycles. Each gfx operation has
	its own timing as specified in the 34010 manual. So, it's 2
	cycles per read plus gfxop cycles per operation. Pretty
	simple, no?

*******************************************************************/

int compute_fill_cycles(int left_partials, int right_partials, int full_words, int rows, int op_timing)
{
	int dstwords;

	if (left_partials) full_words += 1;
	if (right_partials) full_words += 1;
	dstwords = full_words;

	return (dstwords * op_timing) * rows + 2;
}

int compute_pixblt_cycles(int left_partials, int right_partials, int full_words, int op_timing)
{
	int srcwords, dstwords;

	if (left_partials) full_words += 1;
	if (right_partials) full_words += 1;
	srcwords = full_words;
	dstwords = full_words;

	return (dstwords * op_timing + srcwords * 2) + 2;
}

int compute_pixblt_b_cycles(int left_partials, int right_partials, int full_words, int rows, int op_timing, int bpp)
{
	int srcwords, dstwords;

	if (left_partials) full_words += 1;
	if (right_partials) full_words += 1;
	srcwords = full_words * bpp / 16;
	dstwords = full_words;

	return (dstwords * op_timing + srcwords * 2) * rows + 2;
}


/* Shift register handling */
static void shiftreg_w(offs_t offset,data16_t data)
{
	if (state.config->from_shiftreg)
		(*state.config->from_shiftreg)((UINT32)(offset << 3) & ~15, &state.shiftreg[0]);
	else
		logerror("From ShiftReg function not set. PC = %08X\n", PC);
}

static data16_t shiftreg_r(offs_t offset)
{
	if (state.config->to_shiftreg)
		(*state.config->to_shiftreg)((UINT32)(offset << 3) & ~15, &state.shiftreg[0]);
	else
		logerror("To ShiftReg function not set. PC = %08X\n", PC);
	return state.shiftreg[0];
}

static data16_t dummy_shiftreg_r(offs_t offset)
{
	return state.shiftreg[0];
}



/* Pixel operations */
static UINT16 pixel_op00(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return srcpix; }
static UINT16 pixel_op01(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return srcpix & dstpix; }
static UINT16 pixel_op02(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return srcpix & ~dstpix; }
static UINT16 pixel_op03(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return 0; }
static UINT16 pixel_op04(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return (srcpix | ~dstpix) & mask; }
static UINT16 pixel_op05(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return ~(srcpix ^ dstpix) & mask; }
static UINT16 pixel_op06(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return ~dstpix & mask; }
static UINT16 pixel_op07(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return ~(srcpix | dstpix) & mask; }
static UINT16 pixel_op08(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return (srcpix | dstpix) & mask; }
static UINT16 pixel_op09(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return dstpix & mask; }
static UINT16 pixel_op10(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return (srcpix ^ dstpix) & mask; }
static UINT16 pixel_op11(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return (~srcpix & dstpix) & mask; }
static UINT16 pixel_op12(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return mask; }
static UINT16 pixel_op13(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return (~srcpix & dstpix) & mask; }
static UINT16 pixel_op14(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return ~(srcpix & dstpix) & mask; }
static UINT16 pixel_op15(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return srcpix ^ mask; }
static UINT16 pixel_op16(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return (srcpix + dstpix) & mask; }
static UINT16 pixel_op17(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { INT32 tmp = srcpix + (dstpix & mask); return (tmp > mask) ? mask : tmp; }
static UINT16 pixel_op18(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { return (dstpix - srcpix) & mask; }
static UINT16 pixel_op19(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { INT32 tmp = srcpix - (dstpix & mask); return (tmp < 0) ? 0 : tmp; }
static UINT16 pixel_op20(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { dstpix &= mask; return (srcpix > dstpix) ? srcpix : dstpix; }
static UINT16 pixel_op21(UINT16 dstpix, UINT16 mask, UINT16 srcpix) { dstpix &= mask; return (srcpix < dstpix) ? srcpix : dstpix; }

static UINT16 (*pixel_op_table[])(UINT16, UINT16, UINT16) =
{
	pixel_op00,	pixel_op01,	pixel_op02,	pixel_op03,	pixel_op04,	pixel_op05,	pixel_op06,	pixel_op07,
	pixel_op08,	pixel_op09,	pixel_op10,	pixel_op11,	pixel_op12,	pixel_op13,	pixel_op14,	pixel_op15,
	pixel_op16,	pixel_op17,	pixel_op18,	pixel_op19,	pixel_op20,	pixel_op21,	pixel_op00,	pixel_op00,
	pixel_op00,	pixel_op00,	pixel_op00,	pixel_op00,	pixel_op00,	pixel_op00,	pixel_op00,	pixel_op00
};
static UINT8 pixel_op_timing_table[] =
{
	2,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,5,5,6,5,5,2,2,2,2,2,2,2,2,2,2,2
};
static UINT16 (*pixel_op)(UINT16, UINT16, UINT16);
static UINT32 pixel_op_timing;


/* Blitters/fillers */
static void pixblt_1_op0(int src_is_linear, int dst_is_linear);
static void pixblt_2_op0(int src_is_linear, int dst_is_linear);
static void pixblt_4_op0(int src_is_linear, int dst_is_linear);
static void pixblt_8_op0(int src_is_linear, int dst_is_linear);
static void pixblt_16_op0(int src_is_linear, int dst_is_linear);
static void pixblt_r_1_op0(int src_is_linear, int dst_is_linear);
static void pixblt_r_2_op0(int src_is_linear, int dst_is_linear);
static void pixblt_r_4_op0(int src_is_linear, int dst_is_linear);
static void pixblt_r_8_op0(int src_is_linear, int dst_is_linear);
static void pixblt_r_16_op0(int src_is_linear, int dst_is_linear);
static void pixblt_b_1_op0(int dst_is_linear);
static void pixblt_b_2_op0(int dst_is_linear);
static void pixblt_b_4_op0(int dst_is_linear);
static void pixblt_b_8_op0(int dst_is_linear);
static void pixblt_b_16_op0(int dst_is_linear);
static void fill_1_op0(int dst_is_linear);
static void fill_2_op0(int dst_is_linear);
static void fill_4_op0(int dst_is_linear);
static void fill_8_op0(int dst_is_linear);
static void fill_16_op0(int dst_is_linear);

static void pixblt_1_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_2_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_4_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_8_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_16_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_1_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_2_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_4_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_8_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_16_op0_trans(int src_is_linear, int dst_is_linear);
static void pixblt_b_1_op0_trans(int dst_is_linear);
static void pixblt_b_2_op0_trans(int dst_is_linear);
static void pixblt_b_4_op0_trans(int dst_is_linear);
static void pixblt_b_8_op0_trans(int dst_is_linear);
static void pixblt_b_16_op0_trans(int dst_is_linear);
static void fill_1_op0_trans(int dst_is_linear);
static void fill_2_op0_trans(int dst_is_linear);
static void fill_4_op0_trans(int dst_is_linear);
static void fill_8_op0_trans(int dst_is_linear);
static void fill_16_op0_trans(int dst_is_linear);

static void pixblt_1_opx(int src_is_linear, int dst_is_linear);
static void pixblt_2_opx(int src_is_linear, int dst_is_linear);
static void pixblt_4_opx(int src_is_linear, int dst_is_linear);
static void pixblt_8_opx(int src_is_linear, int dst_is_linear);
static void pixblt_16_opx(int src_is_linear, int dst_is_linear);
static void pixblt_r_1_opx(int src_is_linear, int dst_is_linear);
static void pixblt_r_2_opx(int src_is_linear, int dst_is_linear);
static void pixblt_r_4_opx(int src_is_linear, int dst_is_linear);
static void pixblt_r_8_opx(int src_is_linear, int dst_is_linear);
static void pixblt_r_16_opx(int src_is_linear, int dst_is_linear);
static void pixblt_b_1_opx(int dst_is_linear);
static void pixblt_b_2_opx(int dst_is_linear);
static void pixblt_b_4_opx(int dst_is_linear);
static void pixblt_b_8_opx(int dst_is_linear);
static void pixblt_b_16_opx(int dst_is_linear);
static void fill_1_opx(int dst_is_linear);
static void fill_2_opx(int dst_is_linear);
static void fill_4_opx(int dst_is_linear);
static void fill_8_opx(int dst_is_linear);
static void fill_16_opx(int dst_is_linear);

static void pixblt_1_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_2_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_4_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_8_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_16_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_1_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_2_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_4_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_8_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_r_16_opx_trans(int src_is_linear, int dst_is_linear);
static void pixblt_b_1_opx_trans(int dst_is_linear);
static void pixblt_b_2_opx_trans(int dst_is_linear);
static void pixblt_b_4_opx_trans(int dst_is_linear);
static void pixblt_b_8_opx_trans(int dst_is_linear);
static void pixblt_b_16_opx_trans(int dst_is_linear);
static void fill_1_opx_trans(int dst_is_linear);
static void fill_2_opx_trans(int dst_is_linear);
static void fill_4_opx_trans(int dst_is_linear);
static void fill_8_opx_trans(int dst_is_linear);
static void fill_16_opx_trans(int dst_is_linear);


/* tables */
static void (*pixblt_op_table[])(int, int) =
{
	pixblt_1_op0,	pixblt_1_op0_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,
	pixblt_1_opx,	pixblt_1_opx_trans,		pixblt_1_opx,	pixblt_1_opx_trans,

	pixblt_2_op0,	pixblt_2_op0_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,
	pixblt_2_opx,	pixblt_2_opx_trans,		pixblt_2_opx,	pixblt_2_opx_trans,

	pixblt_4_op0,	pixblt_4_op0_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,
	pixblt_4_opx,	pixblt_4_opx_trans,		pixblt_4_opx,	pixblt_4_opx_trans,

	pixblt_8_op0,	pixblt_8_op0_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,
	pixblt_8_opx,	pixblt_8_opx_trans,		pixblt_8_opx,	pixblt_8_opx_trans,

	pixblt_16_op0,	pixblt_16_op0_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans,
	pixblt_16_opx,	pixblt_16_opx_trans,	pixblt_16_opx,	pixblt_16_opx_trans
};

static void (*pixblt_r_op_table[])(int, int) =
{
	pixblt_r_1_op0,	pixblt_r_1_op0_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,
	pixblt_r_1_opx,	pixblt_r_1_opx_trans,	pixblt_r_1_opx,	pixblt_r_1_opx_trans,

	pixblt_r_2_op0,	pixblt_r_2_op0_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,
	pixblt_r_2_opx,	pixblt_r_2_opx_trans,	pixblt_r_2_opx,	pixblt_r_2_opx_trans,

	pixblt_r_4_op0,	pixblt_r_4_op0_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,
	pixblt_r_4_opx,	pixblt_r_4_opx_trans,	pixblt_r_4_opx,	pixblt_r_4_opx_trans,

	pixblt_r_8_op0,	pixblt_r_8_op0_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,
	pixblt_r_8_opx,	pixblt_r_8_opx_trans,	pixblt_r_8_opx,	pixblt_r_8_opx_trans,

	pixblt_r_16_op0,pixblt_r_16_op0_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans,
	pixblt_r_16_opx,pixblt_r_16_opx_trans,	pixblt_r_16_opx,pixblt_r_16_opx_trans
};

static void (*pixblt_b_op_table[])(int) =
{
	pixblt_b_1_op0,	pixblt_b_1_op0_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,
	pixblt_b_1_opx,	pixblt_b_1_opx_trans,	pixblt_b_1_opx,	pixblt_b_1_opx_trans,

	pixblt_b_2_op0,	pixblt_b_2_op0_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,
	pixblt_b_2_opx,	pixblt_b_2_opx_trans,	pixblt_b_2_opx,	pixblt_b_2_opx_trans,

	pixblt_b_4_op0,	pixblt_b_4_op0_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,
	pixblt_b_4_opx,	pixblt_b_4_opx_trans,	pixblt_b_4_opx,	pixblt_b_4_opx_trans,

	pixblt_b_8_op0,	pixblt_b_8_op0_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,
	pixblt_b_8_opx,	pixblt_b_8_opx_trans,	pixblt_b_8_opx,	pixblt_b_8_opx_trans,

	pixblt_b_16_op0,pixblt_b_16_op0_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans,
	pixblt_b_16_opx,pixblt_b_16_opx_trans,	pixblt_b_16_opx,pixblt_b_16_opx_trans
};

static void (*fill_op_table[])(int) =
{
	fill_1_op0,		fill_1_op0_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,
	fill_1_opx,		fill_1_opx_trans,		fill_1_opx,		fill_1_opx_trans,

	fill_2_op0,		fill_2_op0_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,
	fill_2_opx,		fill_2_opx_trans,		fill_2_opx,		fill_2_opx_trans,

	fill_4_op0,		fill_4_op0_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,
	fill_4_opx,		fill_4_opx_trans,		fill_4_opx,		fill_4_opx_trans,

	fill_8_op0,		fill_8_op0_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,
	fill_8_opx,		fill_8_opx_trans,		fill_8_opx,		fill_8_opx_trans,

	fill_16_op0,	fill_16_op0_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans,
	fill_16_opx,	fill_16_opx_trans,		fill_16_opx,	fill_16_opx_trans
};


#define RECURSIVE_INCLUDE

/* non-transparent replace ops */
#define PIXEL_OP(src, mask, pixel) 		pixel = pixel
#define PIXEL_OP_TIMING			 		2
#define PIXEL_OP_REQUIRES_SOURCE 		0
#define TRANSPARENCY					0

	/* 1bpp cases */
	#define BITS_PER_PIXEL					1
	#define FUNCTION_NAME(base)				base##_1_op0
	/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package cpu.tms34010;

public class _34010gfx
{
		#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 2bpp cases */
		#define BITS_PER_PIXEL					2
		#define FUNCTION_NAME(base)				base##_2_op0
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 4bpp cases */
		#define BITS_PER_PIXEL					4
		#define FUNCTION_NAME(base)				base##_4_op0
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 8bpp cases */
		#define BITS_PER_PIXEL					8
		#define FUNCTION_NAME(base)				base##_8_op0
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 16bpp cases */
		#define BITS_PER_PIXEL					16
		#define FUNCTION_NAME(base)				base##_16_op0
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
	#undef TRANSPARENCY
	#undef PIXEL_OP_REQUIRES_SOURCE
	#undef PIXEL_OP_TIMING
	#undef PIXEL_OP
	
	
	#define PIXEL_OP(src, mask, pixel) 		pixel = (*pixel_op)(src, mask, pixel)
	#define PIXEL_OP_TIMING			 		pixel_op_timing
	#define PIXEL_OP_REQUIRES_SOURCE 		1
	#define TRANSPARENCY					0
	
		/* 1bpp cases */
		#define BITS_PER_PIXEL					1
		#define FUNCTION_NAME(base)				base##_1_opx
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 2bpp cases */
		#define BITS_PER_PIXEL					2
		#define FUNCTION_NAME(base)				base##_2_opx
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 4bpp cases */
		#define BITS_PER_PIXEL					4
		#define FUNCTION_NAME(base)				base##_4_opx
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 8bpp cases */
		#define BITS_PER_PIXEL					8
		#define FUNCTION_NAME(base)				base##_8_opx
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 16bpp cases */
		#define BITS_PER_PIXEL					16
		#define FUNCTION_NAME(base)				base##_16_opx
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
	#undef TRANSPARENCY
	#undef PIXEL_OP_REQUIRES_SOURCE
	#undef PIXEL_OP_TIMING
	#undef PIXEL_OP
	
	
	/* transparent replace ops */
	#define PIXEL_OP(src, mask, pixel) 		pixel = pixel
	#define PIXEL_OP_REQUIRES_SOURCE 		0
	#define PIXEL_OP_TIMING			 		4
	#define TRANSPARENCY					1
	
		/* 1bpp cases */
		#define BITS_PER_PIXEL					1
		#define FUNCTION_NAME(base)				base##_1_op0_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 2bpp cases */
		#define BITS_PER_PIXEL					2
		#define FUNCTION_NAME(base)				base##_2_op0_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 4bpp cases */
		#define BITS_PER_PIXEL					4
		#define FUNCTION_NAME(base)				base##_4_op0_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 8bpp cases */
		#define BITS_PER_PIXEL					8
		#define FUNCTION_NAME(base)				base##_8_op0_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 16bpp cases */
		#define BITS_PER_PIXEL					16
		#define FUNCTION_NAME(base)				base##_16_op0_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
	#undef TRANSPARENCY
	#undef PIXEL_OP_REQUIRES_SOURCE
	#undef PIXEL_OP_TIMING
	#undef PIXEL_OP
	
	
	#define PIXEL_OP(src, mask, pixel) 		pixel = (*pixel_op)(src, mask, pixel)
	#define PIXEL_OP_REQUIRES_SOURCE 		1
	#define PIXEL_OP_TIMING					(2+pixel_op_timing)
	#define TRANSPARENCY					1
	
		/* 1bpp cases */
		#define BITS_PER_PIXEL					1
		#define FUNCTION_NAME(base)				base##_1_opx_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 2bpp cases */
		#define BITS_PER_PIXEL					2
		#define FUNCTION_NAME(base)				base##_2_opx_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 4bpp cases */
		#define BITS_PER_PIXEL					4
		#define FUNCTION_NAME(base)				base##_4_opx_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 8bpp cases */
		#define BITS_PER_PIXEL					8
		#define FUNCTION_NAME(base)				base##_8_opx_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
		/* 16bpp cases */
		#define BITS_PER_PIXEL					16
		#define FUNCTION_NAME(base)				base##_16_opx_trans
			#undef FUNCTION_NAME
		#undef BITS_PER_PIXEL
	
	#undef TRANSPARENCY
	#undef PIXEL_OP_REQUIRES_SOURCE
	#undef PIXEL_OP_TIMING
	#undef PIXEL_OP
	
	static UINT8 pixelsize_lookup[32] =
	{
		0,0,1,1,2,2,2,2,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4
	};
	
	
	static void pixblt_b_l(void)
	{
		int psize = pixelsize_lookup[IOREG(REG_PSIZE) & 0x1f];
		int trans = (IOREG(REG_CONTROL) & 0x20) >> 5;
		int rop = (IOREG(REG_CONTROL) >> 10) & 0x1f;
		int ix = trans | (rop << 1) | (psize << 6);
		if (!P_FLAG) LOGGFX(("%08X(%3d):PIXBLT B,L (%dx%d) depth=%d\n", PC, cpu_getscanline(), DYDX_X, DYDX_Y, IOREG(REG_PSIZE) ? IOREG(REG_PSIZE) : 32));
		pixel_op = pixel_op_table[rop];
		pixel_op_timing = pixel_op_timing_table[rop];
		(*pixblt_b_op_table[ix])(1);
	}
	
	static void pixblt_b_xy(void)
	{
		int psize = pixelsize_lookup[IOREG(REG_PSIZE) & 0x1f];
		int trans = (IOREG(REG_CONTROL) & 0x20) >> 5;
		int rop = (IOREG(REG_CONTROL) >> 10) & 0x1f;
		int ix = trans | (rop << 1) | (psize << 6);
		if (!P_FLAG) LOGGFX(("%08X(%3d):PIXBLT B,XY (%d,%d) (%dx%d) depth=%d\n", PC, cpu_getscanline(), DADDR_X, DADDR_Y, DYDX_X, DYDX_Y, IOREG(REG_PSIZE) ? IOREG(REG_PSIZE) : 32));
		pixel_op = pixel_op_table[rop];
		pixel_op_timing = pixel_op_timing_table[rop];
		(*pixblt_b_op_table[ix])(0);
	}
	
	static void pixblt_l_l(void)
	{
		int psize = pixelsize_lookup[IOREG(REG_PSIZE) & 0x1f];
		int trans = (IOREG(REG_CONTROL) & 0x20) >> 5;
		int rop = (IOREG(REG_CONTROL) >> 10) & 0x1f;
		int pbh = (IOREG(REG_CONTROL) >> 8) & 1;
		int ix = trans | (rop << 1) | (psize << 6);
		if (!P_FLAG) LOGGFX(("%08X(%3d):PIXBLT L,L (%dx%d) depth=%d\n", PC, cpu_getscanline(), DYDX_X, DYDX_Y, IOREG(REG_PSIZE) ? IOREG(REG_PSIZE) : 32));
		pixel_op = pixel_op_table[rop];
		pixel_op_timing = pixel_op_timing_table[rop];
		if (!pbh)
			(*pixblt_op_table[ix])(1, 1);
		else
			(*pixblt_r_op_table[ix])(1, 1);
	}
	
	static void pixblt_l_xy(void)
	{
		int psize = pixelsize_lookup[IOREG(REG_PSIZE) & 0x1f];
		int trans = (IOREG(REG_CONTROL) & 0x20) >> 5;
		int rop = (IOREG(REG_CONTROL) >> 10) & 0x1f;
		int pbh = (IOREG(REG_CONTROL) >> 8) & 1;
		int ix = trans | (rop << 1) | (psize << 6);
		if (!P_FLAG) LOGGFX(("%08X(%3d):PIXBLT L,XY (%d,%d) (%dx%d) depth=%d\n", PC, cpu_getscanline(), DADDR_X, DADDR_Y, DYDX_X, DYDX_Y, IOREG(REG_PSIZE) ? IOREG(REG_PSIZE) : 32));
		pixel_op = pixel_op_table[rop];
		pixel_op_timing = pixel_op_timing_table[rop];
		if (!pbh)
			(*pixblt_op_table[ix])(1, 0);
		else
			(*pixblt_r_op_table[ix])(1, 0);
	}
	
	static void pixblt_xy_l(void)
	{
		int psize = pixelsize_lookup[IOREG(REG_PSIZE) & 0x1f];
		int trans = (IOREG(REG_CONTROL) & 0x20) >> 5;
		int rop = (IOREG(REG_CONTROL) >> 10) & 0x1f;
		int pbh = (IOREG(REG_CONTROL) >> 8) & 1;
		int ix = trans | (rop << 1) | (psize << 6);
		if (!P_FLAG) LOGGFX(("%08X(%3d):PIXBLT XY,L (%dx%d) depth=%d\n", PC, cpu_getscanline(), DYDX_X, DYDX_Y, IOREG(REG_PSIZE) ? IOREG(REG_PSIZE) : 32));
		pixel_op = pixel_op_table[rop];
		pixel_op_timing = pixel_op_timing_table[rop];
		if (!pbh)
			(*pixblt_op_table[ix])(0, 1);
		else
			(*pixblt_r_op_table[ix])(0, 1);
	}
	
	static void pixblt_xy_xy(void)
	{
		int psize = pixelsize_lookup[IOREG(REG_PSIZE) & 0x1f];
		int trans = (IOREG(REG_CONTROL) & 0x20) >> 5;
		int rop = (IOREG(REG_CONTROL) >> 10) & 0x1f;
		int pbh = (IOREG(REG_CONTROL) >> 8) & 1;
		int ix = trans | (rop << 1) | (psize << 6);
		if (!P_FLAG) LOGGFX(("%08X(%3d):PIXBLT XY,XY (%dx%d) depth=%d\n", PC, cpu_getscanline(), DYDX_X, DYDX_Y, IOREG(REG_PSIZE) ? IOREG(REG_PSIZE) : 32));
		pixel_op = pixel_op_table[rop];
		pixel_op_timing = pixel_op_timing_table[rop];
		if (!pbh)
			(*pixblt_op_table[ix])(0, 0);
		else
			(*pixblt_r_op_table[ix])(0, 0);
	}
	
	static void fill_l(void)
	{
		int psize = pixelsize_lookup[IOREG(REG_PSIZE) & 0x1f];
		int trans = (IOREG(REG_CONTROL) & 0x20) >> 5;
		int rop = (IOREG(REG_CONTROL) >> 10) & 0x1f;
		int ix = trans | (rop << 1) | (psize << 6);
		if (!P_FLAG) LOGGFX(("%08X(%3d):FILL L (%dx%d) depth=%d\n", PC, cpu_getscanline(), DYDX_X, DYDX_Y, IOREG(REG_PSIZE) ? IOREG(REG_PSIZE) : 32));
		pixel_op = pixel_op_table[rop];
		pixel_op_timing = pixel_op_timing_table[rop];
		(*fill_op_table[ix])(1);
	}
	
	static void fill_xy(void)
	{
		int psize = pixelsize_lookup[IOREG(REG_PSIZE) & 0x1f];
		int trans = (IOREG(REG_CONTROL) & 0x20) >> 5;
		int rop = (IOREG(REG_CONTROL) >> 10) & 0x1f;
		int ix = trans | (rop << 1) | (psize << 6);
		if (!P_FLAG) LOGGFX(("%08X(%3d):FILL XY (%d,%d) (%dx%d) depth=%d\n", PC, cpu_getscanline(), DADDR_X, DADDR_Y, DYDX_X, DYDX_Y, IOREG(REG_PSIZE) ? IOREG(REG_PSIZE) : 32));
		pixel_op = pixel_op_table[rop];
		pixel_op_timing = pixel_op_timing_table[rop];
		(*fill_op_table[ix])(0);
	}
	
	
	#else
	
	
	#undef PIXELS_PER_WORD
	#undef PIXEL_MASK
	
	#define PIXELS_PER_WORD (16 / BITS_PER_PIXEL)
	#define PIXEL_MASK ((1 << BITS_PER_PIXEL) - 1)
	
	#ifdef macintosh
	#pragma optimization_level 1
	#endif
	
	static void FUNCTION_NAME(pixblt)(int src_is_linear, int dst_is_linear)
	{
		/* if this is the first time through, perform the operation */
		if (!P_FLAG)
		{
			int dx, dy, x, y, words, yreverse;
			void (*word_write)(offs_t address,data16_t data);
			data16_t (*word_read)(offs_t address);
			UINT32 saddr, daddr;
	
			/* determine read/write functions */
			if (IOREG(REG_DPYCTL) & 0x0800)
			{
				word_write = shiftreg_w;
				word_read = shiftreg_r;
			}
			else
			{
				word_write = cpu_writemem29lew_word;
				word_read = cpu_readmem29lew_word;
			}
	
			/* compute the starting addresses */
			saddr = src_is_linear ? SADDR : SXYTOL(SADDR_XY);
			saddr &= ~(BITS_PER_PIXEL - 1);
	
			/* compute the bounds of the operation */
			dx = (INT16)DYDX_X;
			dy = (INT16)DYDX_Y;
	
			/* apply the window for non-linear destinations */
			state.gfxcycles = 7 + (src_is_linear ? 0 : 2);
			if (!dst_is_linear)
			{
				XY temp = DADDR_XY;
				state.gfxcycles += 2 + (!src_is_linear) + apply_window("PIXBLT", BITS_PER_PIXEL, &saddr, &temp, &dx, &dy);
				daddr = DXYTOL(temp);
			}
			else
				daddr = DADDR;
			daddr &= ~(BITS_PER_PIXEL - 1);
			LOGGFX(("  saddr=%08X daddr=%08X sptch=%08X dptch=%08X\n", saddr, daddr, SPTCH, DPTCH));
	
			/* bail if we're clipped */
			if (dx <= 0 || dy <= 0)
				return;
	
			/* handle flipping the addresses */
			yreverse = (IOREG(REG_CONTROL) >> 9) & 1;
			if (!src_is_linear || !dst_is_linear)
				if (yreverse)
				{
					saddr += (dy - 1) * SPTCH;
					daddr += (dy - 1) * DPTCH;
				}
	
			P_FLAG = 1;
	
			/* loop over rows */
			for (y = 0; y < dy; y++)
			{
				int left_partials, right_partials, full_words, bitshift, bitshift_alt;
				UINT16 srcword, srcmask, dstword, dstmask, pixel;
				UINT32 swordaddr, dwordaddr;
	
				/* determine the bit shift to get from source to dest */
				bitshift = ((daddr & 15) - (saddr & 15)) & 15;
				bitshift_alt = (16 - bitshift) & 15;
	
				/* how many left and right partial pixels do we have? */
				left_partials = (PIXELS_PER_WORD - ((daddr & 15) / BITS_PER_PIXEL)) & (PIXELS_PER_WORD - 1);
				right_partials = ((daddr + dx * BITS_PER_PIXEL) & 15) / BITS_PER_PIXEL;
				full_words = dx - left_partials - right_partials;
				if (full_words < 0)
					left_partials = dx, right_partials = full_words = 0;
				else
					full_words /= PIXELS_PER_WORD;
	
				/* compute cycles */
				state.gfxcycles += compute_pixblt_cycles(left_partials, right_partials, full_words, PIXEL_OP_TIMING);
	
				/* use word addresses each row */
				swordaddr = saddr >> 4;
				dwordaddr = daddr >> 4;
	
				/* fetch the initial source word */
				srcword = (*word_read)(swordaddr++ << 1);
				srcmask = PIXEL_MASK << (saddr & 15);
	
				/* handle the left partial word */
				if (left_partials != 0)
				{
					/* fetch the destination word */
					dstword = (*word_read)(dwordaddr << 1);
					dstmask = PIXEL_MASK << (daddr & 15);
	
					/* loop over partials */
					for (x = 0; x < left_partials; x++)
					{
						/* fetch another word if necessary */
						if (srcmask == 0)
						{
							srcword = (*word_read)(swordaddr++ << 1);
							srcmask = PIXEL_MASK;
						}
	
						/* process the pixel */
						pixel = srcword & srcmask;
						if (dstmask > srcmask)
							pixel <<= bitshift;
						else
							pixel >>= bitshift_alt;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask <<= BITS_PER_PIXEL;
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* loop over full words */
				for (words = 0; words < full_words; words++)
				{
					/* fetch the destination word (if necessary) */
					if (PIXEL_OP_REQUIRES_SOURCE || TRANSPARENCY)
						dstword = (*word_read)(dwordaddr << 1);
					else
						dstword = 0;
					dstmask = PIXEL_MASK;
	
					/* loop over partials */
					for (x = 0; x < PIXELS_PER_WORD; x++)
					{
						/* fetch another word if necessary */
						if (srcmask == 0)
						{
							srcword = (*word_read)(swordaddr++ << 1);
							srcmask = PIXEL_MASK;
						}
	
						/* process the pixel */
						pixel = srcword & srcmask;
						if (dstmask > srcmask)
							pixel <<= bitshift;
						else
							pixel >>= bitshift_alt;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask <<= BITS_PER_PIXEL;
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* handle the right partial word */
				if (right_partials != 0)
				{
					/* fetch the destination word */
					dstword = (*word_read)(dwordaddr << 1);
					dstmask = PIXEL_MASK;
	
					/* loop over partials */
					for (x = 0; x < right_partials; x++)
					{
						/* fetch another word if necessary */
						if (srcmask == 0)
						{
							srcword = (*word_read)(swordaddr++ << 1);
							srcmask = PIXEL_MASK;
						}
	
						/* process the pixel */
						pixel = srcword & srcmask;
						if (dstmask > srcmask)
							pixel <<= bitshift;
						else
							pixel >>= bitshift_alt;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask <<= BITS_PER_PIXEL;
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* update for next row */
				if (!yreverse)
				{
					saddr += SPTCH;
					daddr += DPTCH;
				}
				else
				{
					saddr -= SPTCH;
					daddr -= DPTCH;
				}
			}
			LOGGFX(("  (%d cycles)\n", state.gfxcycles));
		}
	
		/* eat cycles */
		if (state.gfxcycles > tms34010_ICount)
		{
			state.gfxcycles -= tms34010_ICount;
			tms34010_ICount = 0;
			PC -= 0x10;
		}
		else
		{
			tms34010_ICount -= state.gfxcycles;
			P_FLAG = 0;
			if (src_is_linear && dst_is_linear)
				SADDR += DYDX_Y * SPTCH;
			else if (src_is_linear)
				SADDR += DYDX_Y * SPTCH;
			else
				SADDR_Y += DYDX_Y;
			if (dst_is_linear)
				DADDR += DYDX_Y * DPTCH;
			else
				DADDR_Y += DYDX_Y;
		}
	}
	
	static void FUNCTION_NAME(pixblt_r)(int src_is_linear, int dst_is_linear)
	{
		/* if this is the first time through, perform the operation */
		if (!P_FLAG)
		{
			int dx, dy, x, y, words, yreverse;
			void (*word_write)(offs_t address,data16_t data);
			data16_t (*word_read)(offs_t address);
			UINT32 saddr, daddr;
	
			/* determine read/write functions */
			if (IOREG(REG_DPYCTL) & 0x0800)
			{
				word_write = shiftreg_w;
				word_read = shiftreg_r;
			}
			else
			{
				word_write = cpu_writemem29lew_word;
				word_read = cpu_readmem29lew_word;
			}
	
			/* compute the starting addresses */
			saddr = src_is_linear ? SADDR : SXYTOL(SADDR_XY);
			saddr &= ~(BITS_PER_PIXEL - 1);
	
			/* compute the bounds of the operation */
			dx = (INT16)DYDX_X;
			dy = (INT16)DYDX_Y;
	
			/* apply the window for non-linear destinations */
			state.gfxcycles = 7 + (src_is_linear ? 0 : 2);
			if (!dst_is_linear)
			{
				XY temp = DADDR_XY;
				state.gfxcycles += 2 + (!src_is_linear) + apply_window("PIXBLT R", BITS_PER_PIXEL, &saddr, &temp, &dx, &dy);
				daddr = DXYTOL(temp);
			}
			else
				daddr = DADDR;
			daddr &= ~(BITS_PER_PIXEL - 1);
			LOGGFX(("  saddr=%08X daddr=%08X sptch=%08X dptch=%08X\n", saddr, daddr, SPTCH, DPTCH));
	
			/* bail if we're clipped */
			if (dx <= 0 || dy <= 0)
				return;
	
			/* handle flipping the addresses */
			yreverse = (IOREG(REG_CONTROL) >> 9) & 1;
			if (!src_is_linear || !dst_is_linear)
			{
				saddr += dx * BITS_PER_PIXEL;
				daddr += dx * BITS_PER_PIXEL;
				if (yreverse)
				{
					saddr += (dy - 1) * SPTCH;
					daddr += (dy - 1) * DPTCH;
				}
			}
	
			P_FLAG = 1;
	
			/* loop over rows */
			for (y = 0; y < dy; y++)
			{
				int left_partials, right_partials, full_words, bitshift, bitshift_alt;
				UINT16 srcword, srcmask, dstword, dstmask, pixel;
				UINT32 swordaddr, dwordaddr;
	
				/* determine the bit shift to get from source to dest */
				bitshift = ((daddr & 15) - (saddr & 15)) & 15;
				bitshift_alt = (16 - bitshift) & 15;
	
				/* how many left and right partial pixels do we have? */
				left_partials = (PIXELS_PER_WORD - (((daddr - dx * BITS_PER_PIXEL) & 15) / BITS_PER_PIXEL)) & (PIXELS_PER_WORD - 1);
				right_partials = (daddr & 15) / BITS_PER_PIXEL;
				full_words = dx - left_partials - right_partials;
				if (full_words < 0)
					right_partials = dx, left_partials = full_words = 0;
				else
					full_words /= PIXELS_PER_WORD;
	
				/* compute cycles */
				state.gfxcycles += compute_pixblt_cycles(left_partials, right_partials, full_words, PIXEL_OP_TIMING);
	
				/* use word addresses each row */
				swordaddr = (saddr + 15) >> 4;
				dwordaddr = (daddr + 15) >> 4;
	
				/* fetch the initial source word */
				srcword = (*word_read)(--swordaddr << 1);
				srcmask = PIXEL_MASK << ((saddr - BITS_PER_PIXEL) & 15);
	
				/* handle the right partial word */
				if (right_partials != 0)
				{
					/* fetch the destination word */
					dstword = (*word_read)(--dwordaddr << 1);
					dstmask = PIXEL_MASK << ((daddr - BITS_PER_PIXEL) & 15);
	
					/* loop over partials */
					for (x = 0; x < right_partials; x++)
					{
						/* process the pixel */
						pixel = srcword & srcmask;
						if (dstmask > srcmask)
							pixel <<= bitshift;
						else
							pixel >>= bitshift_alt;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask >>= BITS_PER_PIXEL;
						if (srcmask == 0)
						{
							srcword = (*word_read)(--swordaddr << 1);
							srcmask = PIXEL_MASK << (16 - BITS_PER_PIXEL);
						}
	
						/* update the destination */
						dstmask >>= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr << 1, dstword);
				}
	
				/* loop over full words */
				for (words = 0; words < full_words; words++)
				{
					/* fetch the destination word (if necessary) */
					dwordaddr--;
					if (PIXEL_OP_REQUIRES_SOURCE || TRANSPARENCY)
						dstword = (*word_read)(dwordaddr << 1);
					else
						dstword = 0;
					dstmask = PIXEL_MASK << (16 - BITS_PER_PIXEL);
	
					/* loop over partials */
					for (x = 0; x < PIXELS_PER_WORD; x++)
					{
						/* process the pixel */
						pixel = srcword & srcmask;
						if (dstmask > srcmask)
							pixel <<= bitshift;
						else
							pixel >>= bitshift_alt;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask >>= BITS_PER_PIXEL;
						if (srcmask == 0)
						{
							srcword = (*word_read)(--swordaddr << 1);
							srcmask = PIXEL_MASK << (16 - BITS_PER_PIXEL);
						}
	
						/* update the destination */
						dstmask >>= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr << 1, dstword);
				}
	
				/* handle the left partial word */
				if (left_partials != 0)
				{
					/* fetch the destination word */
					dstword = (*word_read)(--dwordaddr << 1);
					dstmask = PIXEL_MASK << (16 - BITS_PER_PIXEL);
	
					/* loop over partials */
					for (x = 0; x < left_partials; x++)
					{
						/* process the pixel */
						pixel = srcword & srcmask;
						if (dstmask > srcmask)
							pixel <<= bitshift;
						else
							pixel >>= bitshift_alt;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask >>= BITS_PER_PIXEL;
						if (srcmask == 0)
						{
							srcword = (*word_read)(--swordaddr << 1);
							srcmask = PIXEL_MASK << (16 - BITS_PER_PIXEL);
						}
	
						/* update the destination */
						dstmask >>= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr << 1, dstword);
				}
	
				/* update for next row */
				if (!yreverse)
				{
					saddr += SPTCH;
					daddr += DPTCH;
				}
				else
				{
					saddr -= SPTCH;
					daddr -= DPTCH;
				}
			}
			LOGGFX(("  (%d cycles)\n", state.gfxcycles));
		}
	
		/* eat cycles */
		if (state.gfxcycles > tms34010_ICount)
		{
			state.gfxcycles -= tms34010_ICount;
			tms34010_ICount = 0;
			PC -= 0x10;
		}
		else
		{
			tms34010_ICount -= state.gfxcycles;
			P_FLAG = 0;
			if (src_is_linear && dst_is_linear)
				SADDR += DYDX_Y * SPTCH;
			else if (src_is_linear)
				SADDR += DYDX_Y * SPTCH;
			else
				SADDR_Y += DYDX_Y;
			if (dst_is_linear)
				DADDR += DYDX_Y * DPTCH;
			else
				DADDR_Y += DYDX_Y;
		}
	}
	
	#ifdef macintosh
	#pragma optimization_level reset
	#endif
	
	static void FUNCTION_NAME(pixblt_b)(int dst_is_linear)
	{
		/* if this is the first time through, perform the operation */
		if (!P_FLAG)
		{
			int dx, dy, x, y, words, left_partials, right_partials, full_words;
			void (*word_write)(offs_t address,data16_t data);
			data16_t (*word_read)(offs_t address);
			UINT32 saddr, daddr;
	
			/* determine read/write functions */
			if (IOREG(REG_DPYCTL) & 0x0800)
			{
				word_write = shiftreg_w;
				word_read = shiftreg_r;
			}
			else
			{
				word_write = cpu_writemem29lew_word;
				word_read = cpu_readmem29lew_word;
			}
	
			/* compute the starting addresses */
			saddr = SADDR;
	
			/* compute the bounds of the operation */
			dx = (INT16)DYDX_X;
			dy = (INT16)DYDX_Y;
	
			/* apply the window for non-linear destinations */
			state.gfxcycles = 4;
			if (!dst_is_linear)
			{
				XY temp = DADDR_XY;
				state.gfxcycles += 2 + apply_window("PIXBLT B", 1, &saddr, &temp, &dx, &dy);
				daddr = DXYTOL(temp);
			}
			else
				daddr = DADDR;
			daddr &= ~(BITS_PER_PIXEL - 1);
			LOGGFX(("  saddr=%08X daddr=%08X sptch=%08X dptch=%08X\n", saddr, daddr, SPTCH, DPTCH));
	
			/* bail if we're clipped */
			if (dx <= 0 || dy <= 0)
				return;
	
			/* how many left and right partial pixels do we have? */
			left_partials = (PIXELS_PER_WORD - ((daddr & 15) / BITS_PER_PIXEL)) & (PIXELS_PER_WORD - 1);
			right_partials = ((daddr + dx * BITS_PER_PIXEL) & 15) / BITS_PER_PIXEL;
			full_words = dx - left_partials - right_partials;
			if (full_words < 0)
				left_partials = dx, right_partials = full_words = 0;
			else
				full_words /= PIXELS_PER_WORD;
	
			/* compute cycles */
			state.gfxcycles += compute_pixblt_b_cycles(left_partials, right_partials, full_words, dy, PIXEL_OP_TIMING, BITS_PER_PIXEL);
			P_FLAG = 1;
	
			/* loop over rows */
			for (y = 0; y < dy; y++)
			{
				UINT16 srcword, srcmask, dstword, dstmask, pixel;
				UINT32 swordaddr, dwordaddr;
	
				/* use byte addresses each row */
				swordaddr = saddr >> 4;
				dwordaddr = daddr >> 4;
	
				/* fetch the initial source word */
				srcword = (*word_read)(swordaddr++ << 1);
				srcmask = 1 << (saddr & 15);
	
				/* handle the left partial word */
				if (left_partials != 0)
				{
					/* fetch the destination word */
					dstword = (*word_read)(dwordaddr << 1);
					dstmask = PIXEL_MASK << (daddr & 15);
	
					/* loop over partials */
					for (x = 0; x < left_partials; x++)
					{
						/* process the pixel */
						pixel = (srcword & srcmask) ? COLOR1 : COLOR0;
						pixel &= dstmask;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask <<= 1;
						if (srcmask == 0)
						{
							srcword = (*word_read)(swordaddr++ << 1);
							srcmask = 0x0001;
						}
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* loop over full words */
				for (words = 0; words < full_words; words++)
				{
					/* fetch the destination word (if necessary) */
					if (PIXEL_OP_REQUIRES_SOURCE || TRANSPARENCY)
						dstword = (*word_read)(dwordaddr << 1);
					else
						dstword = 0;
					dstmask = PIXEL_MASK;
	
					/* loop over partials */
					for (x = 0; x < PIXELS_PER_WORD; x++)
					{
						/* process the pixel */
						pixel = (srcword & srcmask) ? COLOR1 : COLOR0;
						pixel &= dstmask;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask <<= 1;
						if (srcmask == 0)
						{
							srcword = (*word_read)(swordaddr++ << 1);
							srcmask = 0x0001;
						}
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* handle the right partial word */
				if (right_partials != 0)
				{
					/* fetch the destination word */
					dstword = (*word_read)(dwordaddr << 1);
					dstmask = PIXEL_MASK;
	
					/* loop over partials */
					for (x = 0; x < right_partials; x++)
					{
						/* process the pixel */
						pixel = (srcword & srcmask) ? COLOR1 : COLOR0;
						pixel &= dstmask;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
							dstword = (dstword & ~dstmask) | pixel;
	
						/* update the source */
						srcmask <<= 1;
						if (srcmask == 0)
						{
							srcword = (*word_read)(swordaddr++ << 1);
							srcmask = 0x0001;
						}
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* update for next row */
				saddr += SPTCH;
				daddr += DPTCH;
			}
			LOGGFX(("  (%d cycles)\n", state.gfxcycles));
		}
	
		/* eat cycles */
		if (state.gfxcycles > tms34010_ICount)
		{
			state.gfxcycles -= tms34010_ICount;
			tms34010_ICount = 0;
			PC -= 0x10;
		}
		else
		{
			tms34010_ICount -= state.gfxcycles;
			P_FLAG = 0;
			SADDR += DYDX_Y * SPTCH;
			if (dst_is_linear)
				DADDR += DYDX_Y * DPTCH;
			else
				DADDR_Y += DYDX_Y;
		}
	}
	
	static void FUNCTION_NAME(fill)(int dst_is_linear)
	{
		/* if this is the first time through, perform the operation */
		if (!P_FLAG)
		{
			int dx, dy, x, y, words, left_partials, right_partials, full_words;
			void (*word_write)(offs_t address,data16_t data);
			data16_t (*word_read)(offs_t address);
			UINT32 daddr;
	
			/* determine read/write functions */
			if (IOREG(REG_DPYCTL) & 0x0800)
			{
				word_write = shiftreg_w;
				word_read = dummy_shiftreg_r;
			}
			else
			{
				word_write = cpu_writemem29lew_word;
				word_read = cpu_readmem29lew_word;
			}
	
			/* compute the bounds of the operation */
			dx = (INT16)DYDX_X;
			dy = (INT16)DYDX_Y;
	
			/* apply the window for non-linear destinations */
			state.gfxcycles = 4;
			if (!dst_is_linear)
			{
				XY temp = DADDR_XY;
				state.gfxcycles += 2 + apply_window("FILL", 0, NULL, &temp, &dx, &dy);
				daddr = DXYTOL(temp);
			}
			else
				daddr = DADDR;
			daddr &= ~(BITS_PER_PIXEL - 1);
			LOGGFX(("  daddr=%08X\n", daddr));
	
			/* bail if we're clipped */
			if (dx <= 0 || dy <= 0)
				return;
	
			/* how many left and right partial pixels do we have? */
			left_partials = (PIXELS_PER_WORD - ((daddr & 15) / BITS_PER_PIXEL)) & (PIXELS_PER_WORD - 1);
			right_partials = ((daddr + dx * BITS_PER_PIXEL) & 15) / BITS_PER_PIXEL;
			full_words = dx - left_partials - right_partials;
			if (full_words < 0)
				left_partials = dx, right_partials = full_words = 0;
			else
				full_words /= PIXELS_PER_WORD;
	
			/* compute cycles */
			/* TODO: when state.window_checking == 1, we should count only the time to the first pixel hit */
			state.gfxcycles += compute_fill_cycles(left_partials, right_partials, full_words, dy, PIXEL_OP_TIMING);
			P_FLAG = 1;
	
			/* loop over rows */
			for (y = 0; y < dy; y++)
			{
				UINT16 dstword, dstmask, pixel;
				UINT32 dwordaddr;
	
				/* use byte addresses each row */
				dwordaddr = daddr >> 4;
	
				/* handle the left partial word */
				if (left_partials != 0)
				{
					/* fetch the destination word */
					dstword = (*word_read)(dwordaddr << 1);
					dstmask = PIXEL_MASK << (daddr & 15);
	
					/* loop over partials */
					for (x = 0; x < left_partials; x++)
					{
						/* process the pixel */
						pixel = COLOR1 & dstmask;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
						{
							if (state.window_checking == 1 && !dst_is_linear)
							{
								CLR_V;
								goto bailout;
							}
							else
								dstword = (dstword & ~dstmask) | pixel;
						}
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* loop over full words */
				for (words = 0; words < full_words; words++)
				{
					/* fetch the destination word (if necessary) */
					if (PIXEL_OP_REQUIRES_SOURCE || TRANSPARENCY)
						dstword = (*word_read)(dwordaddr << 1);
					else
						dstword = 0;
					dstmask = PIXEL_MASK;
	
					/* loop over partials */
					for (x = 0; x < PIXELS_PER_WORD; x++)
					{
						/* process the pixel */
						pixel = COLOR1 & dstmask;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
						{
							if (state.window_checking == 1 && !dst_is_linear)
							{
								CLR_V;
								goto bailout;
							}
							else
								dstword = (dstword & ~dstmask) | pixel;
						}
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* handle the right partial word */
				if (right_partials != 0)
				{
					/* fetch the destination word */
					dstword = (*word_read)(dwordaddr << 1);
					dstmask = PIXEL_MASK;
	
					/* loop over partials */
					for (x = 0; x < right_partials; x++)
					{
						/* process the pixel */
						pixel = COLOR1 & dstmask;
						PIXEL_OP(dstword, dstmask, pixel);
						if (!TRANSPARENCY || pixel != 0)
						{
							if (state.window_checking == 1 && !dst_is_linear)
							{
								CLR_V;
								goto bailout;
							}
							else
							dstword = (dstword & ~dstmask) | pixel;
						}
	
						/* update the destination */
						dstmask <<= BITS_PER_PIXEL;
					}
	
					/* write the result */
					(*word_write)(dwordaddr++ << 1, dstword);
				}
	
				/* update for next row */
				daddr += DPTCH;
			}
	bailout:
			LOGGFX(("  (%d cycles)\n", state.gfxcycles));
		}
	
		/* eat cycles */
		if (state.gfxcycles > tms34010_ICount)
		{
			state.gfxcycles -= tms34010_ICount;
			tms34010_ICount = 0;
			PC -= 0x10;
		}
		else
		{
			tms34010_ICount -= state.gfxcycles;
			P_FLAG = 0;
			if (dst_is_linear)
				DADDR += DYDX_Y * DPTCH;
			else
			{
				if (state.window_checking == 1)
				{
					int dx = (INT16)DYDX_X;
					int dy = (INT16)DYDX_Y;
					int v = V_FLAG;
	
					apply_window("FILL clip", 0, NULL, &DADDR_XY, &dx, &dy);
					DYDX_X = dx;
					DYDX_Y = dy;
	
					V_FLAG = v;	/* thrashed by apply_window */
	
					if (v == 0)
					{
						IOREG(REG_INTPEND) |= TMS34010_WV;
						check_interrupt();
					}
				}
				else
					DADDR_Y += DYDX_Y;
			}
		}
	}
	
	#endif
	
}
