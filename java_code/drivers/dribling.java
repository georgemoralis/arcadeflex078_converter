/***************************************************************************

	Model Racing Dribbling hardware

    driver by Aaron Giles

	Games supported:
		* Dribbling

****************************************************************************

	Memory map

****************************************************************************

	========================================================================
	CPU #1
	========================================================================
	tbd
	========================================================================
	Interrupts:
		NMI not connected
		IRQ generated by VBLANK
	========================================================================

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class dribling
{
	
	
	
	/*************************************
	 *
	 *	Global variables
	 *
	 *************************************/
	
	/* referenced by the video hardware */
	data8_t dribling_abca;
	
	static data8_t dr, ds, sh;
	static data8_t input_mux;
	static data8_t di;
	
	
	
	/*************************************
	 *
	 *	Interrupt generation
	 *
	 *************************************/
	
	public static InterruptHandlerPtr dribling_irq_gen = new InterruptHandlerPtr() {public void handler(){
		if (di)
			cpu_set_irq_line(0, 0, ASSERT_LINE);
	} };
	
	
	
	/*************************************
	 *
	 *	PPI inputs
	 *
	 *************************************/
	
	public static ReadHandlerPtr dsr_r  = new ReadHandlerPtr() { public int handler(int offset){
		/* return DSR0-7 */
		return (ds << sh) | (dr >> (8 - sh));
	} };
	
	
	public static ReadHandlerPtr input_mux0_r  = new ReadHandlerPtr() { public int handler(int offset){
		/* low value in the given bit selects */
		if (!(input_mux & 0x01))
			return readinputport(0);
		else if (!(input_mux & 0x02))
			return readinputport(1);
		else if (!(input_mux & 0x04))
			return readinputport(2);
		return 0xff;
	} };
	
	
	
	/*************************************
	 *
	 *	PPI outputs
	 *
	 *************************************/
	
	public static WriteHandlerPtr misc_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bit 7 = di */
		di = (data >> 7) & 1;
		if (!di)
			cpu_set_irq_line(0, 0, CLEAR_LINE);
	
		/* bit 6 = parata */
	
		/* bit 5 = ab. campo */
		dribling_abca = (data >> 5) & 1;
	
		/* bit 4 = ab. a.b.f. */
		/* bit 3 = n/c */
	
		/* bit 2 = (9) = PC2 */
		/* bit 1 = (10) = PC1 */
		/* bit 0 = (32) = PC0 */
		input_mux = data & 7;
		logerror("%04X:misc_w(%02X)\n", activecpu_get_previouspc(), data);
	} };
	
	
	public static WriteHandlerPtr sound_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bit 7 = stop palla */
		/* bit 6 = contrasto */
		/* bit 5 = calgio a */
		/* bit 4 = fischio */
		/* bit 3 = calcio b */
		/* bit 2 = folla a */
		/* bit 1 = folla m */
		/* bit 0 = folla b */
		logerror("%04X:sound_w(%02X)\n", activecpu_get_previouspc(), data);
	} };
	
	
	public static WriteHandlerPtr pb_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* write PB0-7 */
		logerror("%04X:pb_w(%02X)\n", activecpu_get_previouspc(), data);
	} };
	
	
	public static WriteHandlerPtr shr_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		/* bit 3 = watchdog */
		if (data & 0x08)
			watchdog_reset_w(0, 0);
	
		/* bit 2-0 = SH0-2 */
		sh = data & 0x07;
	} };
	
	
	
	/*************************************
	 *
	 *	PPI accessors
	 *
	 *************************************/
	
	public static ReadHandlerPtr ioread  = new ReadHandlerPtr() { public int handler(int offset){
		if (offset & 0x08)
			return ppi8255_0_r(offset & 3);
		else if (offset & 0x10)
			return ppi8255_1_r(offset & 3);
		return 0xff;
	} };
	
	
	public static WriteHandlerPtr iowrite = new WriteHandlerPtr() {public void handler(int offset, int data){
		if (offset & 0x08)
			ppi8255_0_w(offset & 3, data);
		else if (offset & 0x10)
			ppi8255_1_w(offset & 3, data);
		else if (offset & 0x40)
		{
			dr = ds;
			ds = data;
		}
	} };
	
	
	
	/*************************************
	 *
	 *	Machine init
	 *
	 *************************************/
	
	static ppi8255_interface ppi8255_intf =
	{
		2,
		{ dsr_r,        NULL           },
		{ input_mux0_r, NULL           },
		{ NULL,         input_port_3_r },
		{ NULL,         sound_w        },
		{ NULL,         pb_w           },
		{ misc_w,       shr_w          }
	};
	
	public static MachineInitHandlerPtr machine_init_dribling  = new MachineInitHandlerPtr() { public void handler(){
		ppi8255_init(&ppi8255_intf);
	} };
	
	
	
	/*************************************
	 *
	 *	Main CPU memory handlers
	 *
	 *************************************/
	
	public static Memory_ReadAddress readmem[]={
		new Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_ReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new Memory_ReadAddress( 0x2000, 0x3fff, MRA_RAM ),
		new Memory_ReadAddress( 0x4000, 0x7fff, MRA_ROM ),
		new Memory_ReadAddress( 0xc000, 0xdfff, MRA_RAM ),
		new Memory_ReadAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static Memory_WriteAddress writemem[]={
		new Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),
		new Memory_WriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new Memory_WriteAddress( 0x2000, 0x3fff, MWA_RAM, videoram ),
		new Memory_WriteAddress( 0x4000, 0x7fff, MWA_ROM ),
		new Memory_WriteAddress( 0xc000, 0xdfff, dribling_colorram_w, colorram ),
		new Memory_WriteAddress(MEMPORT_MARKER, 0)
	};
	
	
	public static IO_ReadPort readport[]={
		new IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_ReadPort( 0x00, 0xff, ioread ),
	MEMORY_END
	
	
	public static IO_WritePort writeport[]={
		new IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),
		new IO_WritePort( 0x00, 0xff, iowrite ),
	MEMORY_END
	
	
	
	/*************************************
	 *
	 *	Port definitions
	 *
	 *************************************/
	
	static InputPortPtr input_ports_dribling = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( dribling )
		PORT_START(); 	/* IN0 (mux 0) */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT  | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER1 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN  | IPF_PLAYER1 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP    | IPF_PLAYER1 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT   | IPF_PLAYER1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT  | IPF_PLAYER1 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN   | IPF_PLAYER1 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP     | IPF_PLAYER1 );
	
		PORT_START(); 	/* IN0 (mux 1) */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_LEFT  | IPF_PLAYER2 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_RIGHT | IPF_PLAYER2 );
		PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_DOWN  | IPF_PLAYER2 );
		PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_JOYSTICKRIGHT_UP    | IPF_PLAYER2 );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_LEFT   | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_RIGHT  | IPF_PLAYER2 );
		PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_DOWN   | IPF_PLAYER2 );
		PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICKLEFT_UP     | IPF_PLAYER2 );
	
		PORT_START(); 	/* IN0 (mux 2) */
		PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON1             | IPF_PLAYER1 );
		PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2             | IPF_PLAYER1 );
		PORT_BIT( 0x0c, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_BUTTON2             | IPF_PLAYER2 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_BUTTON1             | IPF_PLAYER2 );
		PORT_BIT( 0xc0, IP_ACTIVE_LOW, IPT_UNUSED );
	
		PORT_START(); 	/* IN0 */
		PORT_BIT( 0x0f, IP_ACTIVE_LOW, IPT_UNUSED );
		PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_COIN1 );
		PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START1 );
		PORT_DIPNAME( 0x40, 0x40, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Unknown") );
		PORT_DIPSETTING(    0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(    0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Machine driver
	 *
	 *************************************/
	
	static MACHINE_DRIVER_START( dribling )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(Z80, 5000000)
		MDRV_CPU_MEMORY(readmem,writemem)
		MDRV_CPU_PORTS(readport,writeport)
		MDRV_CPU_VBLANK_INT(dribling_irq_gen,1)
	
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(DEFAULT_REAL_60HZ_VBLANK_DURATION)
	
		MDRV_MACHINE_INIT(dribling)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_UPDATE_BEFORE_VBLANK)
		MDRV_SCREEN_SIZE(256, 256)
		MDRV_VISIBLE_AREA(0, 255, 40, 255)
		MDRV_PALETTE_LENGTH(256)
	
		MDRV_PALETTE_INIT(dribling)
		MDRV_VIDEO_UPDATE(dribling)
	
		/* sound hardware */
	MACHINE_DRIVER_END
	
	
	
	/*************************************
	 *
	 *	ROM definitions
	 *
	 *************************************/
	
	static RomLoadPtr rom_dribling = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "5p.bin",  0x0000, 0x1000, CRC(0e791947) SHA1(57bc4f4e9e1fe3fbac1017601c9c75029b2601a4) )
		ROM_LOAD( "5n.bin",  0x1000, 0x1000, CRC(bd0f223a) SHA1(f9fbc5670a8723c091d61012e545774d315eb18f) ) //
		ROM_LOAD( "5l.bin",  0x4000, 0x1000, CRC(1fccfc85) SHA1(c0365ad54144414218f52209173b858b927c9626) )
		ROM_LOAD( "5k.bin",  0x5000, 0x1000, CRC(737628c4) SHA1(301fda413388c26da5b5150aec2cefc971801749) ) //
		ROM_LOAD( "5h.bin",  0x6000, 0x1000, CRC(30d0957f) SHA1(52135e12094ee1c8828a48c355bdd565aa5895de) ) //
	
		ROM_REGION( 0x2000, REGION_GFX1, 0 )
		ROM_LOAD( "3p.bin",  0x0000, 0x1000, CRC(208971b8) SHA1(f91f3ea04d75beb58a61c844472b4dba53d84c0f) )
		ROM_LOAD( "3n.bin",  0x1000, 0x1000, CRC(356c9803) SHA1(8e2ce52f32b33886f4747dadf3aeb78148538173) )
	
		ROM_REGION( 0x600, REGION_PROMS, 0 )
		ROM_LOAD( "prom_3c.bin", 0x0000, 0x0400, CRC(25f068de) SHA1(ea4c56c47fe8153069acb9df80df0b099f3b81f1) )
		ROM_LOAD( "prom_3e.bin", 0x0400, 0x0100, CRC(73eba798) SHA1(7be0e253624df53092e26c28eb18afdcf71434aa) )
		ROM_LOAD( "prom_2d.bin", 0x0500, 0x0100, CRC(5d8c57c6) SHA1(abfb54812d66a36e797be47653dadda4843e8a90) )
	ROM_END(); }}; 
	
	
	static RomLoadPtr rom_driblino = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 )
		ROM_LOAD( "5p.bin",       0x0000, 0x1000, CRC(0e791947) SHA1(57bc4f4e9e1fe3fbac1017601c9c75029b2601a4) )
		ROM_LOAD( "dribblng.5n",  0x1000, 0x1000, CRC(5271e620) SHA1(ebed8e31057bb8492840a6e3b8bc453f7cb67243) )
		ROM_LOAD( "5l.bin",       0x4000, 0x1000, CRC(1fccfc85) SHA1(c0365ad54144414218f52209173b858b927c9626) )
		ROM_LOAD( "dribblng.5j",  0x5000, 0x1000, CRC(e535ac5b) SHA1(ba13298378f1e5b2b40634874097ad29c402fdea) )
		ROM_LOAD( "dribblng.5h",  0x6000, 0x1000, CRC(e6af7264) SHA1(a015120d85461e599c4bb9626ebea296386a31bb) )
	
		ROM_REGION( 0x2000, REGION_GFX1, 0 )
		ROM_LOAD( "3p.bin",  0x0000, 0x1000, CRC(208971b8) SHA1(f91f3ea04d75beb58a61c844472b4dba53d84c0f) )
		ROM_LOAD( "3n.bin",  0x1000, 0x1000, CRC(356c9803) SHA1(8e2ce52f32b33886f4747dadf3aeb78148538173) )
	
		ROM_REGION( 0x600, REGION_PROMS, 0 )
		ROM_LOAD( "prom_3c.bin", 0x0000, 0x0400, CRC(25f068de) SHA1(ea4c56c47fe8153069acb9df80df0b099f3b81f1) )
		ROM_LOAD( "prom_3e.bin", 0x0400, 0x0100, CRC(73eba798) SHA1(7be0e253624df53092e26c28eb18afdcf71434aa) )
		ROM_LOAD( "prom_2d.bin", 0x0500, 0x0100, CRC(5d8c57c6) SHA1(abfb54812d66a36e797be47653dadda4843e8a90) )
	ROM_END(); }}; 
	
	
	
	/*************************************
	 *
	 *	Game drivers
	 *
	 *************************************/
	
	GAMEX( 1983, dribling, 0,        dribling, dribling, 0, ROT0, "Model Racing", "Dribbling", GAME_NO_SOUND )
	GAMEX( 1983, driblino, dribling, dribling, dribling, 0, ROT0, "Model Racing (Olympia license)", "Dribbling (Olympia)", GAME_NO_SOUND )
}
