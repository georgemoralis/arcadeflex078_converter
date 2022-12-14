/***************************************************************************

	Star Fire/Fire One system

    driver by Daniel Boris, Olivier Galibert, Aaron Giles

****************************************************************************

	Memory map

****************************************************************************

	========================================================================
	MAIN CPU
	========================================================================
	0000-7FFF   R     xxxxxxxx   Program ROM
	8000-9FFF   R/W   xxxxxxxx   Scratch RAM, actually mapped into low VRAM
	9000          W   xxxxxxxx   VRAM write control register
	              W   xxx-----      (VRAM shift amount 1)
	              W   ---x----      (VRAM write mirror 1)
	              W   ----xxx-      (VRAM shift amount 2)
	              W   -------x      (VRAM write mirror 2)
	9001          W   xxxxxxxx   Video control register
	              W   x-------      (Color RAM source select)
	              W   -x------      (Palette RAM write enable)
	              W   --x-----      (Video RAM write enable)
	              W   ---x----      (Right side mask select)
	              W   ----xxxx      (Video RAM ALU operation)
	9800-9807   R     xxxxxxxx   Input ports
	A000-BFFF   R/W   xxxxxxxx   Color RAM
	C000-DFFF   R/W   xxxxxxxx   Video RAM, using shift/mirror 1 and color
	E000-FFFF   R/W   xxxxxxxx   Video RAM, using shift/mirror 2
	========================================================================
	Interrupts:
	   NMI generated once/frame
	========================================================================

***************************************************************************

Notes:

starfira has one less rom in total than starfire but everything passes as
 ok in the rom test so its probably just an earlier revision or something

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class starfire
{
	
	
	/* In vidhrdw/starfire.c */
	
	
	
	
	UINT8 *starfire_videoram;
	UINT8 *starfire_colorram;
	
	static UINT8 fireone_select;
	static mem_read_handler input_read;
	
	
	
	/*************************************
	 *
	 *	Video updates
	 *
	 *************************************/
	
	#define SCANLINE_UPDATE_CHUNK	8
	
	static void update_callback(int scanline)
	{
		/* update the previous chunk of scanlines */
		starfire_video_update(scanline, SCANLINE_UPDATE_CHUNK);
		scanline += SCANLINE_UPDATE_CHUNK;
		if (scanline >= Machine->drv->screen_height)
			scanline = 32;
		timer_set(cpu_getscanlinetime(scanline + SCANLINE_UPDATE_CHUNK - 1), scanline, update_callback);
	}
	
	
	public static MachineInitHandlerPtr machine_init_starfire  = new MachineInitHandlerPtr() { public void handler(){
		timer_set(cpu_getscanlinetime(32 + SCANLINE_UPDATE_CHUNK - 1), 32, update_callback);
	} };
	
	
	
	/*************************************
	 *
	 *	Scratch RAM, mapped into video RAM
	 *
	 *************************************/
	
	public static WriteHandlerPtr starfire_scratch_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* A12 and A3 select video control registers */
		if ((offset & 0x1008) == 0x1000)
		{
			switch (offset & 7)
			{
				case 0:	starfire_vidctrl_w(0, data); break;
				case 1: starfire_vidctrl1_w(0, data); break;
				case 2:
					/* Sounds */
					fireone_select = (data & 0x8) ? 0 : 1;
					break;
			}
		}
	
		/* convert to a videoram offset */
		offset = (offset & 0x31f) | ((offset & 0xe0) << 5);
	    starfire_videoram[offset] = data;
	} };
	
	
	public static ReadHandlerPtr starfire_scratch_r  = new ReadHandlerPtr() { public int handler(int offset){
		/* A11 selects input ports */
		if (offset & 0x800)
			return (*input_read)(offset);
	
		/* convert to a videoram offset */
		offset = (offset & 0x31f) | ((offset & 0xe0) << 5);
	    return starfire_videoram[offset];
	} };
	
	
	
	/*************************************
	 *
	 *	Game-specific input handlers
	 *
	 *************************************/
	
	public static ReadHandlerPtr starfire_input_r  = new ReadHandlerPtr() { public int handler(int offset){
		switch (offset & 15)
		{
			case 0:	return input_port_0_r.handler(0);
			case 1:	return input_port_1_r.handler(0);	/* Note: need to loopback sounds lengths on that one */
			case 5: return input_port_4_r.handler(0);
			case 6:	return input_port_2_r.handler(0);
			case 7:	return input_port_3_r.handler(0);
			default: return 0xff;
		}
	} };
	
	
	public static ReadHandlerPtr fireone_input_r  = new ReadHandlerPtr() { public int handler(int offset){
		static const UINT8 fireone_paddle_map[64] =
		{
			0x00,0x01,0x03,0x02,0x06,0x07,0x05,0x04,
			0x0c,0x0d,0x0f,0x0e,0x0a,0x0b,0x09,0x08,
			0x18,0x19,0x1b,0x1a,0x1e,0x1f,0x1d,0x1c,
			0x14,0x15,0x17,0x16,0x12,0x13,0x11,0x10,
			0x30,0x31,0x33,0x32,0x36,0x37,0x35,0x34,
			0x3c,0x3d,0x3f,0x3e,0x3a,0x3b,0x39,0x38,
			0x28,0x29,0x2b,0x2a,0x2e,0x2f,0x2d,0x2c,
			0x24,0x25,0x27,0x26,0x22,0x23,0x21,0x20
		};
		int temp;
	
		switch (offset & 15)
		{
			case 0:	return input_port_0_r.handler(0);
			case 1: return input_port_1_r.handler(0);
			case 2:
				temp = fireone_select ? input_port_2_r.handler(0) : input_port_3_r.handler(0);
				temp = (temp & 0xc0) | fireone_paddle_map[temp & 0x3f];
				return temp;
			default: return 0xff;
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0x8000, 0x9fff, starfire_scratch_r ),
		new Memory_ReadAddress( 0xa000, 0xbfff, starfire_colorram_r ),
		new Memory_ReadAddress( 0xc000, 0xffff, starfire_videoram_r ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0x8000, 0x9fff, starfire_scratch_w ),
		new Memory_WriteAddress( 0xa000, 0xbfff, starfire_colorram_w, starfire_colorram ),
		new Memory_WriteAddress( 0xc000, 0xffff, starfire_videoram_w, starfire_videoram ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_starfire = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( starfire )
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, "Time" );
		PORT_DIPSETTING(    0x00, "90 Sec" );
		PORT_DIPSETTING(    0x01, "80 Sec" );
		PORT_DIPSETTING(    0x02, "70 Sec" );
		PORT_DIPSETTING(    0x03, "60 Sec" );
		PORT_DIPNAME( 0x04, 0x00, "Coin(s);to Start" )
		PORT_DIPSETTING(    0x00, "1" );
		PORT_DIPSETTING(    0x04, "2" );
		PORT_DIPNAME( 0x08, 0x00, "Fuel per Coin" );
		PORT_DIPSETTING(    0x00, "300" );
		PORT_DIPSETTING(    0x08, "600" );
		PORT_DIPNAME( 0x30, 0x00, "Bonus" );
		PORT_DIPSETTING(    0x00, "300 points" );
		PORT_DIPSETTING(    0x10, "500 points" );
		PORT_DIPSETTING(    0x20, "700 points" );
		PORT_DIPSETTING(    0x30, "None" );
		PORT_DIPNAME( 0x40, 0x00, "Score Table Hold" );
		PORT_DIPSETTING(    0x00, "fixed length" );
		PORT_DIPSETTING(    0x40, "fixed length+fire" );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x04, IP_ACTIVE_HIGH, IPT_BUTTON1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );
	
		PORT_START();   /* IN2 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_X, 100, 10, 0, 255 );
	
		PORT_START();   /* IN3 */
		PORT_ANALOG( 0xff, 0x80, IPT_AD_STICK_Y | IPF_REVERSE, 100, 10, 0, 255 );
	
		PORT_START();  /* Throttle (IN4) [not really player 2, but we need to separate it out] */
		PORT_ANALOG( 0xff, 0x80, IPT_PADDLE_V | IPF_REVERSE | IPF_PLAYER2, 100, 10, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	
	static InputPortPtr input_ports_fireone = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( fireone )
		PORT_START();       /* DSW0 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(    0x03, "2 Coins/1 Player" );
		PORT_DIPSETTING(    0x02, "2 Coins/1 or 2 Players" );
		PORT_DIPSETTING(    0x00, "1 Coin/1 Player" );
		PORT_DIPSETTING(    0x01, "1 Coin/1 or 2 Players" );
		PORT_DIPNAME( 0x0c, 0x0c, "Time" );
		PORT_DIPSETTING(    0x00, "75 Sec" );
		PORT_DIPSETTING(    0x04, "90 Sec" );
		PORT_DIPSETTING(    0x08, "105 Sec" );
		PORT_DIPSETTING(    0x0c, "120 Sec" );
		PORT_DIPNAME( 0x30, 0x00, "Bonus difficulty" );
		PORT_DIPSETTING(    0x00, "Easy" );
		PORT_DIPSETTING(    0x10, "Normal" );
		PORT_DIPSETTING(    0x20, "Hard" );
		PORT_DIPSETTING(    0x30, "Very hard" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(    0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x00, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		PORT_START();       /* IN1 */
		PORT_BIT( 0x01, IP_ACTIVE_HIGH, IPT_START1 );
		PORT_BIT( 0x02, IP_ACTIVE_HIGH, IPT_START2 );
		PORT_BIT_IMPULSE( 0x04, IP_ACTIVE_HIGH, IPT_COIN1, 1 );
		PORT_BIT( 0x08, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT( 0x10, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x20, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();   /* IN2 */
		PORT_ANALOG( 0x3f, 0x20, IPT_PADDLE | IPF_PLAYER1, 50, 1, 0, 63 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );
	
		PORT_START();   /* IN3 */
		PORT_ANALOG( 0x3f, 0x20, IPT_PADDLE | IPF_PLAYER2, 50, 1, 0, 63 );
		PORT_BIT( 0x40, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( starfire )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 2500000)
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_VBLANK_INT(nmi_line_pulse,1)
	
		MDRV_FRAMES_PER_SECOND(57)
		MDRV_VBLANK_DURATION(DEFAULT_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(starfire)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0, 256-1, 32, 256-1)
		MDRV_PALETTE_LENGTH(64)
	
		MDRV_VIDEO_START(starfire)
		MDRV_VIDEO_UPDATE(starfire)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	static RomLoadPtr rom_starfire = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )     /* 64k for code */
		ROM_LOAD( "sfire.1a",     0x0000, 0x0800, CRC(9990af64) SHA1(05eccf1084ace55be9d6cf0fccddcaa18fa5487a) )
		ROM_LOAD( "sfire.2a",     0x0800, 0x0800, CRC(6e17ba33) SHA1(59433696f56018a7b253491b1db3ff45546dcd46) )
		ROM_LOAD( "sfire.1b",     0x1000, 0x0800, CRC(946175d0) SHA1(6a55d9f6031b96e9e05d61d59a23d4fc6df724bf) )
		ROM_LOAD( "sfire.2b",     0x1800, 0x0800, CRC(67be4275) SHA1(dd6232e034030e0c2b4d866fda36cbe22d8518f7) )
		ROM_LOAD( "sfire.1c",     0x2000, 0x0800, CRC(c56b4e07) SHA1(e55ae84c484a78372180783df37750cdad8b04a2) )
		ROM_LOAD( "sfire.2c",     0x2800, 0x0800, CRC(b4b9d3a7) SHA1(8f3e0d67d1e94f6b1c41a78e59ac81f021aa827a) )
		ROM_LOAD( "sfire.1d",     0x3000, 0x0800, CRC(fd52ffb5) SHA1(c1ba2ffb7de0301a962cca2e693bfbbd9838b852) )
		ROM_LOAD( "sfire.2d",     0x3800, 0x0800, CRC(51c69fe3) SHA1(33159cb3ea5029d395fc20916899aa05139c2d51) )
		ROM_LOAD( "sfire.1e",     0x4000, 0x0800, CRC(01994ec8) SHA1(db694f922a98bb0fc585cad83bee8a88d72fca8f) )
		ROM_LOAD( "sfire.2e",     0x4800, 0x0800, CRC(ef3d1b71) SHA1(ca427209194f519b1ac5b94d29c2789445303dc1) )
		ROM_LOAD( "sfire.1f",     0x5000, 0x0800, CRC(af31dc39) SHA1(0dfeff6973fd03e85b08e70c77d212f0bb60121d) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_starfira = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )     /* 64k for code */
		ROM_LOAD( "starfire.1a",  0x0000, 0x0800, CRC(6adcd7e7) SHA1(a931fb80e48db3050ce3bc39f455961c0c7c56ce) )
		ROM_LOAD( "starfire.2a",  0x0800, 0x0800, CRC(835c70ea) SHA1(36828735aa48de5e3e973ca1f42ef08537e1c6ce) )
		ROM_LOAD( "starfire.1b",  0x1000, 0x0800, CRC(377afbef) SHA1(97cb5a20aeb8c70670d6db8f41b2abcb181755c6) )
		ROM_LOAD( "starfire.2b",  0x1800, 0x0800, CRC(f3a833cb) SHA1(d2e01806ead71b0946347fd9668fd3f24524734e) )
		ROM_LOAD( "starfire.1c",  0x2000, 0x0800, CRC(db625c1d) SHA1(5d0307258a73b4b82fbe7b10634076412f4ab3c7) )
		ROM_LOAD( "starfire.2c",  0x2800, 0x0800, CRC(68fa2ce6) SHA1(2b32df960bc4ec38f50f0d23ab96becb68bc4034) )
		ROM_LOAD( "starfire.1d",  0x3000, 0x0800, CRC(c6b5f1d1) SHA1(85a3f7ce7a51597609c762c9a809b84922f8a6e5) )
		ROM_LOAD( "starfire.2d",  0x3800, 0x0800, CRC(ab2a36a5) SHA1(debd9503246b4d27c8136bfb60cdffd9107ad95e) )
		ROM_LOAD( "starfire.1e",  0x4000, 0x0800, CRC(1ac8ba8c) SHA1(90c5a8a943edad74141b15e1f145598abce8cb75) )
		ROM_LOAD( "starfire.2e",  0x4800, 0x0800, CRC(ba8434c5) SHA1(1831b291dfe3e4b081e66caa909b8c727bfffa7b) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_fireone = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )     /* 64k for code */
		ROM_LOAD( "fo-ic13.7b",     0x0000, 0x0800, CRC(f927f086) SHA1(509db84d781dd2d5aaefd561539738f0db7c4ca5) )
		ROM_LOAD( "fo-ic24.7c",     0x0800, 0x0800, CRC(0d2d8723) SHA1(e9bb2092ce7786016f15e42916ad48ef12735e9c) )
		ROM_LOAD( "fo-ic12.6b",     0x1000, 0x0800, CRC(ac7783d9) SHA1(8bcfcc5d3126382f4ec8904e0435de0931abc41e) )
		ROM_LOAD( "fo-ic23.6c",     0x1800, 0x0800, CRC(15c74ee7) SHA1(0adb87c2471ecbbd18d10579043765ce877dbde7) )
		ROM_LOAD( "fo-ic11.5b",     0x2000, 0x0800, CRC(721930a1) SHA1(826245ffbd399056a74ccd14cd2bd4acd2fb2d24) )
		ROM_LOAD( "fo-ic22.5c",     0x2800, 0x0800, CRC(f0c965b4) SHA1(ffe96e636720325d9a40b729128730446b74435b) )
		ROM_LOAD( "fo-ic10.4b",     0x3000, 0x0800, CRC(27a7b2c0) SHA1(7a8c70e565bdcb6e085e4d283f41c92758640055) )
		ROM_LOAD( "fo-ic21.4c",     0x3800, 0x0800, CRC(b142c857) SHA1(609fbd0c0b5833807fd606284c26ad7cb7e4d742) )
		ROM_LOAD( "fo-ic09.3b",     0x4000, 0x0800, CRC(1c076b1b) SHA1(874c09c81e90e1be869902057b7359e71f77db52) )
		ROM_LOAD( "fo-ic20.3c",     0x4800, 0x0800, CRC(b4ac6e71) SHA1(4731dd6865929b8c9c33cbe4cf1dde23046d6914) )
		ROM_LOAD( "fo-ic08.2b",     0x5000, 0x0800, CRC(5839e2ff) SHA1(9d8a17c5b64cdf5bf222f4dbca48f0210b18e403) )
		ROM_LOAD( "fo-ic19.2c",     0x5800, 0x0800, CRC(9fd85e11) SHA1(f8264357a63f757bc58f3703e60e219d67d0d081) )
		ROM_LOAD( "fo-ic07.1b",     0x6000, 0x0800, CRC(b90baae1) SHA1(c7dedf38e5a1977234f1f745a7aa443f6bf7db52) )
		ROM_LOAD( "fo-ic18.1c",     0x6800, 0x0800, CRC(771ee5ba) SHA1(6577e219386de594dbde8a54d5f5f9657419061a) )
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Driver init
	 *
	 *************************************/
	
	public static DriverInitHandlerPtr init_starfire  = new DriverInitHandlerPtr() { public void handler(){
		input_read = starfire_input_r;
	} };
	
	public static DriverInitHandlerPtr init_fireone  = new DriverInitHandlerPtr() { public void handler(){
		input_read = fireone_input_r;
	} };
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	GAMEX( 1979, starfire, 0,        starfire, starfire, starfire, ROT0, "Exidy", "Star Fire (set 1)", GAME_NO_SOUND )
	GAMEX( 1979, starfira, starfire, starfire, starfire, starfire, ROT0, "Exidy", "Star Fire (set 2)", GAME_NO_SOUND )
	GAMEX( 1979, fireone,  0,        starfire, fireone,  fireone,  ROT0, "Exidy", "Fire One", GAME_NO_SOUND )
}
