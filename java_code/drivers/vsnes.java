/***************************************************************************

Nintendo VS UniSystem and DualSystem - (c) 1984 Nintendo of America

	Portions of this code are heavily based on
	Brad Oliver's MESS implementation of the NES.

RP2C04-001:
- Baseball
- Freedom Force
- Gradius
- Hogan's Alley
- Mach Rider (Japan, Fighting Course)
- Pinball
- Platoon
- Super Xevious

RP2C04-002:
- Castlevania
- Ladies golf
- Mach Rider (Endurance Course)
- Raid on Bungeling Bay (Japan)
- Slalom
- Stroke N' Match Golf
- Wrecking Crew

RP2C04-003:
- Dr mario
- Excite Bike
- Goonies
- Soccer
- TKO Boxing

RP2c05-004:
- Clu Clu Land
- Excite Bike (Japan)
- Ice Climber
- Ice Climber Dual (Japan)
- Super Mario Bros.

Rcp2c03b:
- Battle City
- Duck Hunt
- Mahjang
- Pinball (Japan)
- Rbi Baseball
- Star Luster
- Stroke and Match Golf (Japan)
- Super Skykid
- Tennis
- Tetris

RC2C05-01:
- Ninja Jajamaru Kun (Japan)

RC2C05-02:
- Mighty Bomb Jack (Japan)

RC2C05-03:
- Gumshoe

RC2C05-04:
- Top Gun

Graphic hack games:
- Skate Kids								(by Two-Bit Score, 1988; hack of Vs. Super Mario Bros.)

Needed roms:
- Babel no Tou								(by Namco, 198?)
- Family Boxing								(by Namco/Woodplace, 198?)
- Family Stadium '87						(by Namco, 1987)
- Family Stadium '88						(by Namco, 1988)
- Family Tennis								(by Namco, 198?)
- Head to Head Baseball						(ever finished/released?, by Nintendo, 1986)
- Japanese version of Vs. Tennis			(1984)
- Japanese version of Vs. Soccer			(1985)
- Japanese version of Vs. Super Mario Bros. (1986)
- Lionex									(prototype by Sunsoft, 1987)
- Madura no Tsubasa							(prototype by Sunsoft, 1987)
- Predators									(prototype by Williams, 84)
- Pro Yakyuu Family Stadium					(by Namco, 1986?)
- Quest of Ki								(by Namco/Game Studio, 198?)
- Super Chinese								(by Namco/Culture Brain, 1988)
- Toukaidou 53tsugi							(prototype by Sunsoft, 1985)
- Trojan									(by Capcom, 1987)
- Urban Champion							(1984)
- Volleyball								(1986)
- Walkure no Bouken							(by Namco, 198?)
- Wild Gunman								(1984, light gun game)

TO DO:
	- Check others bits in coin counter
	- Check other values in bnglngby irq
	- Top Gun: cpu #0 (PC=00008016): unmapped memory byte read from 00007FFF ???

Changes:

  16/10/2003 Pierpaolo Prazzoli

  - Added
		- Vs. Freedom Force
		- Vs. Super Xevious

  24/12/2002 Pierpaolo Prazzoli

  - Added
		- Vs. Mighty Bomb Jack (Japan)
		- Vs. Ninja Jajamaru Kun (Japan)
		- Vs. Raid on Bungeling Bay (Japan)
		- Vs. Top Gun
		- Vs. Mach Rider (Japan, Fighting Course Version)
		- Vs. Ice Climber (Japan)
		- Vs. Gumshoe (partially working)
		- Vs. Freedom Force (not working)
		- Vs. Stroke and Match Golf (Men set 2) (not working)
		- Vs. BaseBall (Japan set 3) (not working)
  - Added coin counter
  - Added Extra Ram in vstetris
  - Added Demo Sound in vsmahjng
  - Fixed vsskykid inputs
  - Fixed protection in Vs. Super Xevious
  - Corrected or checked dip-switches in Castlevania, Duck Hunt, Excitebike,
	Gradius, Hogan's Alley, Ice Climber, R.B.I. Baseball, Slalom, Soccer,
	Super Mario Bros., Top Gun, BaseBall, Tennis, Stroke and Match Golf

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.0.5
 */ 
package arcadeflex.v078.drivers;

public class vsnes
{
	
	/* clock frequency */
	#define N2A03_DEFAULTCLOCK ( 21477272.724 / 12 )
	
	#define DUAL_RBI 1
	
	/* from vidhrdw */
	
	/* from machine */
	
	
	/******************************************************************************/
	
	/* local stuff */
	static UINT8 *work_ram, *work_ram_1;
	static int coin;
	
	public static ReadHandlerPtr mirror_ram_r  = new ReadHandlerPtr() { public int handler(int offset){
		return work_ram[ offset & 0x7ff ];
	} };
	
	public static ReadHandlerPtr mirror_ram_1_r  = new ReadHandlerPtr() { public int handler(int offset){
		return work_ram[ offset & 0x7ff ];
	} };
	
	public static WriteHandlerPtr mirror_ram_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		work_ram[ offset & 0x7ff ] = data;
	} };
	
	public static WriteHandlerPtr mirror_ram_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		work_ram[ offset & 0x7ff ] = data;
	} };
	
	public static WriteHandlerPtr sprite_dma_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		int source = ( data & 7 ) * 0x100;
	
		ppu2c03b_spriteram_dma( 0, &work_ram[source] );
	} };
	
	public static WriteHandlerPtr sprite_dma_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		int source = ( data & 7 ) * 0x100;
	
		ppu2c03b_spriteram_dma( 1, &work_ram_1[source] );
	} };
	
	public static WriteHandlerPtr vsnes_coin_counter_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_counter_w( 0, data & 0x01 );
		coin = data;
		if( data & 0xfe ) //"bnglngby" and "cluclu"
		{
			//do something?
			logerror("vsnes_coin_counter_w: pc = 0x%04x - data = 0x%02x\n", activecpu_get_pc(), data);
		}
	} };
	
	public static ReadHandlerPtr vsnes_coin_counter_r  = new ReadHandlerPtr() { public int handler(int offset){
		//only for platoon
		return coin;
	} };
	
	public static WriteHandlerPtr vsnes_coin_counter_1_w = new WriteHandlerPtr() {public void handler(int offset, int data){
		coin_counter_w( 1, data & 0x01 );
		if( data & 0xfe ) //vsbball service mode
		{
			//do something?
			logerror("vsnes_coin_counter_1_w: pc = 0x%04x - data = 0x%02x\n", activecpu_get_pc(), data);
		}
	
	} };
	/******************************************************************************/
	
	static MEMORY_READ_START (readmem)
		{ 0x0000, 0x07ff, MRA_RAM },
		{ 0x0800, 0x1fff, mirror_ram_r },
		{ 0x2000, 0x3fff, ppu2c03b_0_r },
		{ 0x4000, 0x4015, NESPSG_0_r },
		{ 0x4016, 0x4016, vsnes_in0_r },
		{ 0x4017, 0x4017, vsnes_in1_r },
		{ 0x4020, 0x4020, vsnes_coin_counter_r },
		{ 0x8000, 0xffff, MRA_ROM },
	MEMORY_END
	
	static MEMORY_WRITE_START (writemem)
		{ 0x0000, 0x07ff, MWA_RAM, &work_ram },
		{ 0x0800, 0x1fff, mirror_ram_w },
		{ 0x2000, 0x3fff, ppu2c03b_0_w },
		{ 0x4011, 0x4011, DAC_0_data_w },
		{ 0x4014, 0x4014, sprite_dma_w },
		{ 0x4000, 0x4015, NESPSG_0_w },
		{ 0x4016, 0x4016, vsnes_in0_w },
		{ 0x4017, 0x4017, MWA_NOP }, /* in 1 writes ignored */
		{ 0x4020, 0x4020, vsnes_coin_counter_w },
		{ 0x8000, 0xffff, MWA_ROM },
	MEMORY_END
	
	static MEMORY_READ_START (readmem_1)
		{ 0x0000, 0x07ff, MRA_RAM },
		{ 0x0800, 0x1fff, mirror_ram_1_r },
		{ 0x2000, 0x3fff, ppu2c03b_1_r },
		{ 0x4000, 0x4015, NESPSG_0_r },
		{ 0x4016, 0x4016, vsnes_in0_1_r },
		{ 0x4017, 0x4017, vsnes_in1_1_r },
		{ 0x8000, 0xffff, MRA_ROM },
	MEMORY_END
	
	static MEMORY_WRITE_START (writemem_1)
		{ 0x0000, 0x07ff, MWA_RAM, &work_ram_1 },
		{ 0x0800, 0x1fff, mirror_ram_1_w },
		{ 0x2000, 0x3fff, ppu2c03b_1_w },
		{ 0x4011, 0x4011, DAC_1_data_w },
		{ 0x4014, 0x4014, sprite_dma_1_w },
		{ 0x4000, 0x4015, NESPSG_1_w },
		{ 0x4016, 0x4016, vsnes_in0_1_w },
		{ 0x4017, 0x4017, MWA_NOP }, /* in 1 writes ignored */
		{ 0x4020, 0x4020, vsnes_coin_counter_1_w },
		{ 0x8000, 0xffff, MWA_ROM },
	MEMORY_END
	
	/******************************************************************************/
	
	#define VS_CONTROLS( SELECT_IN0, START_IN0, SELECT_IN1, START_IN1 ) \
		PORT_START(); 	/* IN0 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */ \
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, SELECT_IN0 );			/* SELECT on a nes */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, START_IN0 );			/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );\
	\
		PORT_START(); 	/* IN1 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */ \
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, SELECT_IN1 );			/* SELECT on a nes */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, START_IN1 );			/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );\
	\
		PORT_START(); 	/* IN2 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */ \
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
	#define VS_CONTROLS_REVERSE( SELECT_IN0, START_IN0, SELECT_IN1, START_IN1 ) \
		PORT_START(); 	/* IN0 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */ \
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, SELECT_IN0 );			/* SELECT on a nes */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, START_IN0 );			/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );\
	 	\
		PORT_START(); 	/* IN1 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */ \
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, SELECT_IN1 );			/* SELECT on a nes */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, START_IN1 );			/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );\
	 	\
		PORT_START(); 	/* IN2 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */ \
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
	#define VS_ZAPPER \
	PORT_START(); 	/* IN0 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );/* sprite hit */ \
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_BUTTON1 );/* gun trigger */ \
		\
		PORT_START(); 	/* IN1 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );\
		\
		PORT_START(); 	/* IN2 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */ \
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
	#define VS_DUAL_CONTROLS_L \
		PORT_START(); 	/* IN0 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */ \
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START1 );			/* SELECT on a nes */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_START3 );			/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );\
	 	\
		PORT_START(); 	/* IN1 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */ \
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START2 );			/* SELECT on a nes */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_START4 );			/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );\
	 	\
		PORT_START(); 	/* IN2 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */ \
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );/* this bit masks irqs - dont change */
	
	#define VS_DUAL_CONTROLS_R \
		PORT_START(); 	/* IN3 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER3 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER3 );/* BUTTON B on a nes */ \
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "2nd Side 1 Player Start", KEYCODE_MINUS, IP_JOY_NONE );/* SELECT on a nes */ \
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "2nd Side 3 Player Start", KEYCODE_BACKSLASH, IP_JOY_NONE );/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER3 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER3 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER3 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 );\
	 	\
		PORT_START(); 	/* IN4 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER4 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER4 );/* BUTTON B on a nes */ \
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "2nd Side 2 Player Start", KEYCODE_EQUALS, IP_JOY_NONE );/* SELECT on a nes */ \
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "2nd Side 4 Player Start", KEYCODE_BACKSPACE, IP_JOY_NONE );/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER4 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER4 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER4 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 );\
		\
		PORT_START(); 	/* IN5 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE2 );/* service credit? */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN3 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN4 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );/* this bit masks irqs - dont change */ \
	
	#define VS_DUAL_CONTROLS_REVERSE_L \
		PORT_START(); 	/* IN0 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */ \
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START1 );			/* SELECT on a nes */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_START3 );			/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );\
	 	\
		PORT_START(); 	/* IN1 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */ \
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START2 );			/* SELECT on a nes */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_START4 );			/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );\
	 	\
		PORT_START(); 	/* IN2 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */ \
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );/* this bit masks irqs - dont change */
	
	#define VS_DUAL_CONTROLS_REVERSE_R \
		PORT_START(); 	/* IN3 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER4 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER4 );/* BUTTON B on a nes */ \
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "2nd Side 1 Player Start", KEYCODE_MINUS, IP_JOY_NONE );/* SELECT on a nes */ \
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "2nd Side 3 Player Start", KEYCODE_BACKSLASH, IP_JOY_NONE );/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER4 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER4 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER4 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER4 );\
	 	\
		PORT_START(); 	/* IN4 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER3 );/* BUTTON A on a nes */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER3 );/* BUTTON B on a nes */ \
		PORT_BITX(0x04, IP_ACTIVE_HIGH, 0, "2nd Side 2 Player Start", KEYCODE_EQUALS, IP_JOY_NONE );/* SELECT on a nes */ \
		PORT_BITX(0x08, IP_ACTIVE_HIGH, 0, "2nd Side 4 Player Start", KEYCODE_BACKSPACE, IP_JOY_NONE );/* START on a nes */ \
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER3 );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER3 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER3 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER3 );\
		\
		PORT_START(); 	/* IN5 */ \
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */ \
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );\
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE2 );/* service credit? */ \
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );\
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN3 );\
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN4 );\
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNUSED );/* this bit masks irqs - dont change */ \
	
	static InputPortPtr input_ports_vsnes = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsnes )
		VS_CONTROLS( IPT_START1, IPT_BUTTON3 | IPF_PLAYER1, IPT_START2, IPT_BUTTON3 | IPF_PLAYER2 )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_topgun = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( topgun )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, "Lives per Coin" );
		PORT_DIPSETTING(	0x00, "3 - 12 Max" );
		PORT_DIPSETTING(	0x08, "2 - 9 Max" );
		PORT_DIPNAME( 0x30, 0x00, "Bonus" );
		PORT_DIPSETTING(	0x00, "30k and Every 50k" );
		PORT_DIPSETTING(	0x20, "50k and Every 100k" );
		PORT_DIPSETTING(	0x10, "100k and Every 150k" );
		PORT_DIPSETTING(	0x30, "200k and Every 200k" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x40, "Hard" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_platoon = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( platoon )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0xE0, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0xc0, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0xa0, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x80, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x60, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x20, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x40, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0xe0, DEF_STR( "Free_Play") );
	INPUT_PORTS_END(); }}; 
	
	/*
	Stroke Play Off On
	Hole in 1 +5 +4
	Double Eagle +4 +3
	Eagle +3 +2
	Birdie +2 +1
	Par +1 0
	Bogey 0 -1
	Other 0 -2
	
	Match Play OFF ON
	Win Hole +1 +2
	Tie 0 0
	Lose Hole -1 -2
	*/
	
	static InputPortPtr input_ports_golf = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( golf )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x07, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, "Hole Size" );
		PORT_DIPSETTING(	0x00, "Large" );
		PORT_DIPSETTING(	0x08, "Small" );
		PORT_DIPNAME( 0x10, 0x00, "Points per Stroke" );
		PORT_DIPSETTING(	0x00, "Easier" );
		PORT_DIPSETTING(	0x10, "Harder" );
		PORT_DIPNAME( 0x60, 0x00, "Starting Points" );
		PORT_DIPSETTING(	0x00, "10" );
		PORT_DIPSETTING(	0x40, "13" );
		PORT_DIPSETTING(	0x20, "16" );
		PORT_DIPSETTING(	0x60, "20" );
		PORT_DIPNAME( 0x80, 0x00, "Difficulty Vs. Computer" );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x80, "Hard" );
	INPUT_PORTS_END(); }}; 
	
	/* Same as 'golf', but 4 start buttons */
	static InputPortPtr input_ports_golf4s = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( golf4s )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_START3, IPT_START2, IPT_START4 )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x07, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, "Hole Size" );
		PORT_DIPSETTING(	0x00, "Large" );
		PORT_DIPSETTING(	0x08, "Small" );
		PORT_DIPNAME( 0x10, 0x00, "Points per Stroke" );
		PORT_DIPSETTING(	0x00, "Easier" );
		PORT_DIPSETTING(	0x10, "Harder" );
		PORT_DIPNAME( 0x60, 0x00, "Starting Points" );
		PORT_DIPSETTING(	0x00, "10" );
		PORT_DIPSETTING(	0x40, "13" );
		PORT_DIPSETTING(	0x20, "16" );
		PORT_DIPSETTING(	0x60, "20" );
		PORT_DIPNAME( 0x80, 0x00, "Difficulty Vs. Computer" );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x80, "Hard" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vstennis = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vstennis )
		VS_DUAL_CONTROLS_L /* left side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x00, "Difficulty Vs. Computer" );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x02, "Normal" );
		PORT_DIPSETTING(	0x01, "Hard" );
		PORT_DIPSETTING(	0x03, "Very Hard" );
		PORT_DIPNAME( 0x0c, 0x00, "Difficulty Vs. Player" );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x08, "Normal" );
		PORT_DIPSETTING(	0x04, "Hard" );
		PORT_DIPSETTING(	0x0c, "Very Hard" );
		PORT_DIPNAME( 0x10, 0x00, "Raquet Size" );
		PORT_DIPSETTING(	0x00, "Large" );
		PORT_DIPSETTING(	0x10, "Small" );
		PORT_DIPNAME( 0x20, 0x00, "Extra Score" );
		PORT_DIPSETTING(	0x00, "1 Set" );
		PORT_DIPSETTING(	0x20, "1 Game" );
		PORT_DIPNAME( 0x40, 0x00, "Court Color" );
		PORT_DIPSETTING(	0x00, "Green" );
		PORT_DIPSETTING(	0x40, "Blue" );
		PORT_DIPNAME( 0x80, 0x00, "Copyright" );
		PORT_DIPSETTING(	0x00, "Japan" );
		PORT_DIPSETTING(	0x80, "USA" );
	
		VS_DUAL_CONTROLS_R /* Right Side Controls */
	
		PORT_START();  /* DSW1 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_SERVICE( 0x01, IP_ACTIVE_HIGH );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x08, "Doubles 4 Player" );
		PORT_DIPSETTING(	0x00, "2 Credits" );
		PORT_DIPSETTING(	0x08, "4 Credits" );
		PORT_DIPNAME( 0x10, 0x10, "Doubles Vs CPU" );
		PORT_DIPSETTING(	0x00, "1 Credit" );
		PORT_DIPSETTING(	0x10, "2 Credits" );
		PORT_DIPNAME( 0x60, 0x00, "Rackets Per Game" );
		PORT_DIPSETTING(	0x60, "2" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x40, "4" );
		PORT_DIPSETTING(	0x20, "5" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_wrecking = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( wrecking )
		VS_DUAL_CONTROLS_L /* left side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x02, "4" );
		PORT_DIPSETTING(	0x01, "5" );
		PORT_DIPSETTING(	0x03, "6" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		VS_DUAL_CONTROLS_R /* right side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x07, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_balonfgt = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( balonfgt )
		VS_DUAL_CONTROLS_L /* left side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_SERVICE( 0x80, IP_ACTIVE_HIGH );
	
		VS_DUAL_CONTROLS_R /* right side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x02, "4" );
		PORT_DIPSETTING(	0x01, "5" );
		PORT_DIPSETTING(	0x03, "6" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vsmahjng = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsmahjng )
		VS_DUAL_CONTROLS_L /* left side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, "Infinite Time" );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x00, "Time" );
		PORT_DIPSETTING(	0x30, "30" );
		PORT_DIPSETTING(	0x10, "45" );
		PORT_DIPSETTING(	0x20, "60" );
		PORT_DIPSETTING(	0x00, "90" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		VS_DUAL_CONTROLS_R /* right side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_SERVICE( 0x01, IP_ACTIVE_HIGH );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x20, "Starting Points" );
		PORT_DIPSETTING(	0x60, "15000" );
		PORT_DIPSETTING(	0x20, "20000" );
		PORT_DIPSETTING(	0x40, "25000" );
		PORT_DIPSETTING(	0x00, "30000" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vsbball = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsbball )
		VS_DUAL_CONTROLS_L /* left side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_SERVICE( 0x01, IP_ACTIVE_HIGH );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x38, 0x00, "Starting Points" );
		PORT_DIPSETTING(    0x00, "80 Pts" );
		PORT_DIPSETTING(    0x20, "100 Pts" );
		PORT_DIPSETTING(    0x10, "150 Pts" );
		PORT_DIPSETTING(    0x30, "200 Pts" );
		PORT_DIPSETTING(    0x08, "250 Pts" );
		PORT_DIPSETTING(    0x28, "300 Pts" );
		PORT_DIPSETTING(    0x18, "350 Pts" );
		PORT_DIPSETTING(    0x38, "400 Pts" );
		PORT_DIPNAME( 0x40, 0x00, "Bonus Play" );//?
		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		VS_DUAL_CONTROLS_R /* right side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x02, "Player Defense Strenght" );
		PORT_DIPSETTING(	0x00, "Weak" );
		PORT_DIPSETTING(	0x02, "Normal" );
		PORT_DIPSETTING(	0x01, "Strong" );
		PORT_DIPSETTING(	0x03, "Very Strong" );
		PORT_DIPNAME( 0x0c, 0x08, "Player Offense Strenght" );
		PORT_DIPSETTING(	0x00, "Weak" );
		PORT_DIPSETTING(	0x08, "Normal" );
		PORT_DIPSETTING(	0x04, "Strong" );
		PORT_DIPSETTING(	0x0c, "Very Strong" );
		PORT_DIPNAME( 0x30, 0x20, "Computer Defense Strenght" );
		PORT_DIPSETTING(	0x00, "Weak" );
		PORT_DIPSETTING(	0x20, "Normal" );
		PORT_DIPSETTING(	0x10, "Strong" );
		PORT_DIPSETTING(	0x30, "Very Strong" );
		PORT_DIPNAME( 0xc0, 0x80, "Computer Offense Strenght" );
		PORT_DIPSETTING(	0x00, "Weak" );
		PORT_DIPSETTING(	0x80, "Normal" );
		PORT_DIPSETTING(	0x40, "Strong" );
		PORT_DIPSETTING(	0xc0, "Very Strong" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vsbballj = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsbballj )
		VS_DUAL_CONTROLS_L /* left side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x02, "Player Defense Strenght" );
		PORT_DIPSETTING(	0x00, "Weak" );
		PORT_DIPSETTING(	0x02, "Normal" );
		PORT_DIPSETTING(	0x01, "Strong" );
		PORT_DIPSETTING(	0x03, "Very Strong" );
		PORT_DIPNAME( 0x0c, 0x08, "Player Offense Strenght" );
		PORT_DIPSETTING(	0x00, "Weak" );
		PORT_DIPSETTING(	0x08, "Normal" );
		PORT_DIPSETTING(	0x04, "Strong" );
		PORT_DIPSETTING(	0x0c, "Very Strong" );
		PORT_DIPNAME( 0x30, 0x20, "Computer Defense Strenght" );
		PORT_DIPSETTING(	0x00, "Weak" );
		PORT_DIPSETTING(	0x20, "Normal" );
		PORT_DIPSETTING(	0x10, "Strong" );
		PORT_DIPSETTING(	0x30, "Very Strong" );
		PORT_DIPNAME( 0xc0, 0x80, "Computer Offense Strenght" );
		PORT_DIPSETTING(	0x00, "Weak" );
		PORT_DIPSETTING(	0x80, "Normal" );
		PORT_DIPSETTING(	0x40, "Strong" );
		PORT_DIPSETTING(	0xc0, "Very Strong" );
	
		VS_DUAL_CONTROLS_R /* right side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_SERVICE( 0x01, IP_ACTIVE_HIGH );
		PORT_DIPNAME( 0x06, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x38, 0x00, "Starting Points" );
		PORT_DIPSETTING(    0x00, "80 Pts" );
		PORT_DIPSETTING(    0x20, "100 Pts" );
		PORT_DIPSETTING(    0x10, "150 Pts" );
		PORT_DIPSETTING(    0x30, "200 Pts" );
		PORT_DIPSETTING(    0x08, "250 Pts" );
		PORT_DIPSETTING(    0x28, "300 Pts" );
		PORT_DIPSETTING(    0x18, "350 Pts" );
		PORT_DIPSETTING(    0x38, "400 Pts" );
		PORT_DIPNAME( 0x40, 0x00, "Bonus Play" );//?
		PORT_DIPSETTING(	0x40, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_iceclmrj = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( iceclmrj )
		VS_DUAL_CONTROLS_REVERSE_L /* left side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, "Coinage (Left Side"));
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, "Lives (Left Side"));
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x10, "4" );
		PORT_DIPSETTING(	0x08, "5" );
		PORT_DIPSETTING(	0x18, "7" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_BITX(    0x80, 0x00, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Service Mode (Left Side"));EYCODE_F2, IP_JOY_NONE )
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		VS_DUAL_CONTROLS_REVERSE_R /* right side controls */
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, "Coinage (Right Side"));
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, "Lives (Right Side"));
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x10, "4" );
		PORT_DIPSETTING(	0x08, "5" );
		PORT_DIPSETTING(	0x18, "7" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_BITX(    0x80, 0x00, IPT_DIPSWITCH_NAME | IPF_TOGGLE, "Service Mode (Right Side"));EYCODE_F1, IP_JOY_NONE )
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_drmario = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( drmario )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x00, "Drop Rate Increases After" );
		PORT_DIPSETTING(	0x00, "7 Pills" );
		PORT_DIPSETTING(	0x01, "8 Pills" );
		PORT_DIPSETTING(	0x02, "9 Pills" );
		PORT_DIPSETTING(	0x03, "10 Pills" );
		PORT_DIPNAME( 0x0c, 0x00, "Virus Level" );
		PORT_DIPSETTING(	0x00, "1" );
		PORT_DIPSETTING(	0x04, "3" );
		PORT_DIPSETTING(	0x08, "5" );
		PORT_DIPSETTING(	0x0c, "7" );
		PORT_DIPNAME( 0x30, 0x00, "Drop Speed Up" );
		PORT_DIPSETTING(	0x00, "Slow" );
		PORT_DIPSETTING(	0x10, "Medium" );
		PORT_DIPSETTING(	0x20, "Fast" );
		PORT_DIPSETTING(	0x30, "Fastest" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Free_Play") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_rbibb = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( rbibb )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x0c, 0x0c, "Max. 1p/in, 2p/in, Min" );
		PORT_DIPSETTING(	0x04, "2, 1, 3" );
		PORT_DIPSETTING(	0x0c, "2, 2, 4" );
		PORT_DIPSETTING(	0x00, "3, 2, 6" );
		PORT_DIPSETTING(	0x08, "4, 3, 7" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x10, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0xe0, 0x80, "Color Palette" );
		PORT_DIPSETTING(	0x80, "Normal" );
		PORT_DIPSETTING(    0x00, "Wrong 1" );
		PORT_DIPSETTING(    0x40, "Wrong 2" );
		PORT_DIPSETTING(    0x20, "Wrong 3" );
		PORT_DIPSETTING(    0xc0, "Wrong 4" );
		/* 0x60,0xa0,0xe0:again "Wrong 3" */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_btlecity = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( btlecity )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x01, 0x01, "Credits for 2 Players" );
		PORT_DIPSETTING(	0x00, "1" );
		PORT_DIPSETTING(	0x01, "2" );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x02, "5" );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x04, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x00, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0xc0, 0x80, "Color Palette" );
		PORT_DIPSETTING(	0x80, "Normal" );
		PORT_DIPSETTING(	0x00, "Wrong 1" );
		PORT_DIPSETTING(	0x40, "Wrong 2" );
		PORT_DIPSETTING(	0xc0, "Wrong 3" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_cluclu = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( cluclu )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x60, "2" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x40, "4" );
		PORT_DIPSETTING(	0x20, "5" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_cstlevna = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( cstlevna )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x08, "2" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPNAME( 0x30, 0x00, "Bonus" );
		PORT_DIPSETTING(	0x00, "100k" );
		PORT_DIPSETTING(	0x20, "200k" );
		PORT_DIPSETTING(	0x10, "300k" );
		PORT_DIPSETTING(	0x30, "400k" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Difficulty") );	// Damage taken
		PORT_DIPSETTING(	0x00, "Normal" );			// Normal
		PORT_DIPSETTING(	0x40, "Hard" );				// Double
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_iceclimb = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( iceclimb )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x10, "4" );
		PORT_DIPSETTING(	0x08, "5" );
		PORT_DIPSETTING(	0x18, "7" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x20, "Hard" );
		PORT_DIPNAME( 0x40, 0x00, "Time before bear appears" );//?
		PORT_DIPSETTING(	0x00, "Long" );
		PORT_DIPSETTING(	0x40, "Short" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	/* Same as 'iceclimb', but different buttons mapping and input protection */
	static InputPortPtr input_ports_iceclmbj = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( iceclmbj )
		PORT_START(); 	/* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START1 );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_SPECIAL );// protection /* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START2 );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_SPECIAL );// protection /* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x10, "4" );
		PORT_DIPSETTING(	0x08, "5" );
		PORT_DIPSETTING(	0x18, "7" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x20, "Hard" );
		PORT_DIPNAME( 0x40, 0x00, "Time before bear appears ?" );
		PORT_DIPSETTING(	0x00, "Long" );
		PORT_DIPSETTING(	0x40, "Short" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_excitebk = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( excitebk )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, "Bonus" );
		PORT_DIPSETTING(	0x00, "100k and Every 50k" );
		PORT_DIPSETTING(	0x10, "Every 100k" );
		PORT_DIPSETTING(	0x08, "100k Only" );
		PORT_DIPSETTING(	0x18, "None" );
		PORT_DIPNAME( 0x20, 0x00, "1st Half Qualifying Time" );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x20, "Hard" );
		PORT_DIPNAME( 0x40, 0x00, "2nd Half Qualifying Time" );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x40, "Hard" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_jajamaru = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( jajamaru )
		PORT_START(); 	/* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START1 );			 /* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_START2 );			/* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );			/* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_UNUSED );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x10, "4" );
		PORT_DIPSETTING(	0x08, "5" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_machridr = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( machridr )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, "Time" );
		PORT_DIPSETTING(	0x00, "280" );
		PORT_DIPSETTING(	0x10, "250" );
		PORT_DIPSETTING(	0x08, "220" );
		PORT_DIPSETTING(	0x18, "200" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_machridj = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( machridj )
		PORT_START(); 	/* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START1 );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );			/* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_UNUSED );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );			/* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT ( 0x60, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, "Km 1st Race" );//?
		PORT_DIPSETTING(	0x00, "12" );
		PORT_DIPSETTING(	0x10, "15" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_suprmrio = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( suprmrio )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x02, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "1C_5C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x08, "2" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x00, "100" );
		PORT_DIPSETTING(	0x20, "150" );
		PORT_DIPSETTING(	0x10, "200" );
		PORT_DIPSETTING(	0x30, "250" );
		PORT_DIPNAME( 0x40, 0x00, "Timer" );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x40, "Fast" );
		PORT_DIPNAME( 0x80, 0x80, "Continue Lives" );
		PORT_DIPSETTING(	0x80, "3" );
		PORT_DIPSETTING(	0x00, "4" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_duckhunt = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( duckhunt )
		VS_ZAPPER
	
		PORT_START();  /* IN3 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0X03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0X01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0X00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0X02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x08, "Normal" );
		PORT_DIPSETTING(	0x10, "Hard" );
		PORT_DIPSETTING(	0x18, "Hardest" );
		PORT_DIPNAME( 0x20, 0x20, "Misses per game" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x20, "5" );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x00, "30000" );
		PORT_DIPSETTING(	0x40, "50000" );
		PORT_DIPSETTING(	0x80, "80000" );
		PORT_DIPSETTING(	0xc0, "100000" );
	
		PORT_START(); 	/* IN4 - FAKE - Gun X pos */
		PORT_ANALOG( 0xff, 0x80, IPT_LIGHTGUN_X, 70, 30, 0, 255 );
	
		PORT_START(); 	/* IN5 - FAKE - Gun Y pos */
		PORT_ANALOG( 0xff, 0x80, IPT_LIGHTGUN_Y, 50, 30, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_hogalley = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( hogalley )
		VS_ZAPPER
	
		PORT_START();  /* IN3 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x08, "Normal" );
		PORT_DIPSETTING(	0x10, "Hard" );
		PORT_DIPSETTING(	0x18, "Hardest" );
		PORT_DIPNAME( 0x20, 0x20, "Misses per game" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x20, "5" );
		PORT_DIPNAME( 0xc0, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x00, "30000" );
		PORT_DIPSETTING(	0x40, "50000" );
		PORT_DIPSETTING(	0x80, "80000" );
		PORT_DIPSETTING(	0xc0, "100000" );
	
		PORT_START(); 	/* IN4 - FAKE - Gun X pos */
		PORT_ANALOG( 0xff, 0x80, IPT_LIGHTGUN_X, 70, 30, 0, 255 );
	
		PORT_START(); 	/* IN5 - FAKE - Gun Y pos */
		PORT_ANALOG( 0xff, 0x80, IPT_LIGHTGUN_Y, 50, 30, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vsgshoe = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsgshoe )
		VS_ZAPPER
	
		PORT_START();  /* IN3 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0X03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0X01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0X00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0X02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x08, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x08, "Normal" );
		PORT_DIPSETTING(	0x10, "Hard" );
		PORT_DIPSETTING(	0x18, "Hardest" );
		PORT_DIPNAME( 0x20, 0x20, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "5" );
		PORT_DIPSETTING(	0x20, "3" );
		PORT_DIPNAME( 0x40, 0x00, "Bullets per Balloon" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x40, "2" );
		PORT_DIPNAME( 0x80, 0x00, "1 Bonus Man Awarded at 50k" );
		PORT_DIPSETTING(	0x00, "80000" );
		PORT_DIPSETTING(	0x80, "100000" );
	
		PORT_START(); 	/* IN4 - FAKE - Gun X pos */
		PORT_ANALOG( 0xff, 0x80, IPT_LIGHTGUN_X, 70, 30, 0, 255 );
	
		PORT_START(); 	/* IN5 - FAKE - Gun Y pos */
		PORT_ANALOG( 0xff, 0x80, IPT_LIGHTGUN_Y, 50, 30, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vsfdf = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsfdf )
		VS_ZAPPER
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	
		PORT_START(); 	/* IN4 - FAKE - Gun X pos */
		PORT_ANALOG( 0xff, 0x80, IPT_LIGHTGUN_X, 70, 30, 0, 255 );
	
		PORT_START(); 	/* IN5 - FAKE - Gun Y pos */
		PORT_ANALOG( 0xff, 0x80, IPT_LIGHTGUN_Y, 50, 30, 0, 255 );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vstetris = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vstetris )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x0c, 0x04, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x04, "Normal" );
		PORT_DIPSETTING(	0x08, "Hard" );
		PORT_DIPSETTING(	0x0c, "Very Hard" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0xe0, 0x80, "Color Palette" );
		PORT_DIPSETTING(	0x80, "Normal" );
		PORT_DIPSETTING(  0x00, "Wrong 1" );
		PORT_DIPSETTING(	0x40, "Wrong 2" );
		PORT_DIPSETTING(	0x20, "Wrong 3" );
		PORT_DIPSETTING(  0xc0, "Wrong 4" );
		/* 0x60,0xa0,0xe0:again "Wrong 3" */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vsskykid = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsskykid )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_START2, IPT_UNKNOWN, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x04, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "2" );
		PORT_DIPSETTING(	0x04, "3" );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x18, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x08, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xe0, 0x20, "Color Palette" );
		PORT_DIPSETTING(	0x20, "Normal" );
		PORT_DIPSETTING(    0x00, "Wrong 1" );
		PORT_DIPSETTING(	0x40, "Wrong 2" );
		PORT_DIPSETTING(	0x80, "Wrong 3" );
		PORT_DIPSETTING(    0xc0, "Wrong 4" );
		/* 0x60,0xa0,0xe0:again "Normal" */
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vspinbal = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vspinbal )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x07, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, "Side Drain Walls" );
		PORT_DIPSETTING(	0x00, "High" );
		PORT_DIPSETTING(	0x08, "Low" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x00, "50000" );
		PORT_DIPSETTING(	0x10, "70000" );
		PORT_DIPNAME( 0x60, 0x00, "Balls" );
		PORT_DIPSETTING(	0x60, "2" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x40, "4" );
		PORT_DIPSETTING(	0x20, "5" );
		PORT_DIPNAME( 0x80, 0x00, "Ball speed" );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x80, "Fast" );
	INPUT_PORTS_END(); }}; 
	
	/* Same as 'vspinbal', but different buttons mapping */
	static InputPortPtr input_ports_vspinblj = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vspinblj )
		PORT_START(); 	/* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );	/* Right flipper */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );	/* Right flipper */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START1 );				/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );				/* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );	/* Left flipper */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );	/* Left flipper */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START2 );					/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );			 	/* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x01, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x07, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, "Side Drain Walls" );
		PORT_DIPSETTING(	0x00, "High" );
		PORT_DIPSETTING(	0x08, "Low" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Bonus_Life") );
		PORT_DIPSETTING(	0x00, "50000" );
		PORT_DIPSETTING(	0x10, "70000" );
		PORT_DIPNAME( 0x60, 0x00, "Balls" );
		PORT_DIPSETTING(	0x60, "2" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x40, "4" );
		PORT_DIPSETTING(	0x20, "5" );
		PORT_DIPNAME( 0x80, 0x00, "Ball speed" );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x80, "Fast" );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_goonies = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( goonies )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x08, "2" );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME(0x40,  0x00, "Timer" );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x40, "Fast" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vssoccer = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vssoccer )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x08, "Points Timer" );
		PORT_DIPSETTING(	0x00, "600 Pts" );
		PORT_DIPSETTING(	0x10, "800 Pts" );
		PORT_DIPSETTING(	0x08, "1000 Pts" );
		PORT_DIPSETTING(	0x18, "1200 Pts" );
		PORT_DIPNAME( 0x60, 0x40, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x40, "Normal" );
		PORT_DIPSETTING(	0x20, "Hard" );
		PORT_DIPSETTING(	0x60, "Very Hard" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unused") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vsgradus = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsgradus )
		VS_CONTROLS_REVERSE( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x08, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x08, "3" );
		PORT_DIPSETTING(	0x00, "4" );
		PORT_DIPNAME( 0x30, 0x00, "Bonus" );
		PORT_DIPSETTING(	0x00, "100k" );
		PORT_DIPSETTING(	0x20, "200k" );
		PORT_DIPSETTING(	0x10, "300k" );
		PORT_DIPSETTING(	0x30, "400k" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(	0x40, "Hard" );
		PORT_DIPNAME( 0x80, 0x80, DEF_STR( "Demo_Sounds") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_vsslalom = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( vsslalom )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x08, 0x00, "Freestyle Points" );
		PORT_DIPSETTING(	0x00, "Left / Right" );
		PORT_DIPSETTING(	0x08, "Hold Time" );
		PORT_DIPNAME( 0x30, 0x10, DEF_STR( "Difficulty") );
		PORT_DIPSETTING(	0x00, "Easy" );
		PORT_DIPSETTING(	0x10, "Normal" );
		PORT_DIPSETTING(	0x20, "Hard" );
		PORT_DIPSETTING(	0x30, "Hardest" );
		PORT_DIPNAME( 0x40, 0x00, "Allow Continue" );
		PORT_DIPSETTING(	0x40, DEF_STR( "No") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Yes") );
		PORT_DIPNAME( 0x80, 0x00, "Inverted input" );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_starlstr = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( starlstr )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x60, 0x40, "Palette Color" );
		PORT_DIPSETTING(	0x40, "Black" );
		PORT_DIPSETTING(	0x20, "Green" );
		PORT_DIPSETTING(	0x60, "Grey" );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_tkoboxng = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( tkoboxng )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x03, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x10, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x10, DEF_STR( "On") );
		PORT_DIPNAME( 0x20, 0x00, "Palette Color" );
		PORT_DIPSETTING(	0x00, "Black" );
		PORT_DIPSETTING(	0x20, "White" );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_bnglngby = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( bnglngby )
		PORT_START(); 	/* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START1 );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_SPECIAL );// protection /* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START2 );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_LOW,  IPT_SPECIAL );// protection /* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_COIN2 );
		PORT_BIT ( 0x80, IP_ACTIVE_LOW,  IPT_SPECIAL );
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPSETTING(	0x07, DEF_STR( "Free_Play") );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x00, "2" );
		PORT_DIPSETTING(	0x08, "3" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_mightybj = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( mightybj )
		VS_CONTROLS( IPT_START1, IPT_UNKNOWN, IPT_START2, IPT_UNKNOWN )
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x07, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x07, DEF_STR( "5C_1C") );
		PORT_DIPSETTING(	0x03, DEF_STR( "4C_1C") );
		PORT_DIPSETTING(	0x05, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x01, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x04, DEF_STR( "1C_2C") );
		PORT_DIPSETTING(	0x02, DEF_STR( "1C_3C") );
		PORT_DIPSETTING(	0x06, DEF_STR( "1C_4C") );
		PORT_DIPNAME( 0x18, 0x00, DEF_STR( "Lives") );
		PORT_DIPSETTING(	0x10, "2" );
		PORT_DIPSETTING(	0x00, "3" );
		PORT_DIPSETTING(	0x08, "4" );
		PORT_DIPSETTING(	0x18, "5" );
		PORT_DIPNAME( 0x20, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x20, DEF_STR( "On") );
		PORT_DIPNAME( 0x40, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x40, DEF_STR( "On") );
		PORT_DIPNAME( 0x80, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x80, DEF_STR( "On") );
	INPUT_PORTS_END(); }}; 
	
	static InputPortPtr input_ports_supxevs = new InputPortPtr(){ public void handler() { INPUT_PORTS_START( supxevs )
		PORT_START(); 	/* IN0 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER1 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER1 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_START1 );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );			/* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER1 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER1 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER1 );
	
		PORT_START(); 	/* IN1 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_BUTTON2 | IPF_PLAYER2 );/* BUTTON A on a nes */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_BUTTON1 | IPF_PLAYER2 );/* BUTTON B on a nes */
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_UNKNOWN );			/* SELECT on a nes */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNKNOWN );			/* START on a nes */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_JOYSTICK_UP    | IPF_PLAYER2 );
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_JOYSTICK_DOWN  | IPF_PLAYER2 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_JOYSTICK_LEFT  | IPF_PLAYER2 );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_JOYSTICK_RIGHT | IPF_PLAYER2 );
	
		PORT_START(); 	/* IN2 */
		PORT_BIT ( 0x01, IP_ACTIVE_HIGH, IPT_UNUSED );/* serial pin from controller */
		PORT_BIT ( 0x02, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x04, IP_ACTIVE_HIGH, IPT_SERVICE1 );/* service credit? */
		PORT_BIT ( 0x08, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 0 of dsw goes here */
		PORT_BIT ( 0x10, IP_ACTIVE_HIGH, IPT_UNUSED );/* bit 1 of dsw goes here */
		PORT_BIT ( 0x20, IP_ACTIVE_HIGH, IPT_COIN1 );
		PORT_BIT ( 0x40, IP_ACTIVE_HIGH, IPT_UNKNOWN );
		PORT_BIT ( 0x80, IP_ACTIVE_HIGH, IPT_UNKNOWN );
	
		PORT_START();  /* DSW0 - bit 0 and 1 read from bit 3 and 4 on $4016, rest of the bits read on $4017 */
		PORT_DIPNAME( 0x01, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x01, DEF_STR( "On") );
		PORT_DIPNAME( 0x02, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x02, DEF_STR( "On") );
		PORT_DIPNAME( 0x04, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x04, DEF_STR( "On") );
		PORT_DIPNAME( 0x08, 0x00, DEF_STR( "Unknown") );
		PORT_DIPSETTING(	0x00, DEF_STR( "Off") );
		PORT_DIPSETTING(	0x08, DEF_STR( "On") );
		PORT_DIPNAME( 0x30, 0x00, DEF_STR( "Coinage") );
		PORT_DIPSETTING(	0x30, DEF_STR( "3C_1C") );
		PORT_DIPSETTING(	0x20, DEF_STR( "2C_1C") );
		PORT_DIPSETTING(	0x00, DEF_STR( "1C_1C") );
		PORT_DIPSETTING(	0x10, DEF_STR( "1C_2C") );
		PORT_DIPNAME( 0xc0, 0x00, "Color Palette" );
		PORT_DIPSETTING(	0x00, "Normal" );
		PORT_DIPSETTING(    0x40, "Wrong 1" );
		PORT_DIPSETTING(    0x80, "Wrong 2" );
		PORT_DIPSETTING(    0xc0, "Wrong 3" );
	INPUT_PORTS_END(); }}; 
	
	static GfxDecodeInfo nes_gfxdecodeinfo[] =
	{
		/* none, the ppu generates one */
		new GfxDecodeInfo( -1 ) /* end of array */
	};
	
	static struct NESinterface nes_interface =
	{
		1,
		{ REGION_CPU1 },
		{ 50 },
	};
	
	static DACinterface nes_dac_interface = new DACinterface
	(
		1,
		new int[] { 50 },
	);
	
	static struct NESinterface nes_dual_interface =
	{
		2,
		{ REGION_CPU1, REGION_CPU2 },
		{ 25, 25 },
	};
	
	static DACinterface nes_dual_dac_interface = new DACinterface
	(
		2,
		new int[] { 25, 25 },
	);
	
	static MACHINE_DRIVER_START( vsnes )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(N2A03,N2A03_DEFAULTCLOCK)
		MDRV_CPU_MEMORY(readmem,writemem)
									/* some carts also trigger IRQs */
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(( ( ( 1.0 / 60.0 ) * 1000000.0 ) / 262 ) * ( 262 - 239 ))
	
		MDRV_MACHINE_INIT(vsnes)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER)
		MDRV_SCREEN_SIZE(32*8, 30*8)
		MDRV_VISIBLE_AREA(0*8, 32*8-1, 0*8, 30*8-1)
		MDRV_GFXDECODE(nes_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(4*16)
		MDRV_COLORTABLE_LENGTH(4*8)
	
		MDRV_PALETTE_INIT(vsnes)
		MDRV_VIDEO_START(vsnes)
		MDRV_VIDEO_UPDATE(vsnes)
	
		/* sound hardware */
		MDRV_SOUND_ADD(NES, nes_interface)
		MDRV_SOUND_ADD(DAC, nes_dac_interface)
	MACHINE_DRIVER_END
	
	
	static MACHINE_DRIVER_START( vsdual )
	
		/* basic machine hardware */
		MDRV_CPU_ADD(N2A03,N2A03_DEFAULTCLOCK)
		MDRV_CPU_MEMORY(readmem,writemem)
									/* some carts also trigger IRQs */
		MDRV_CPU_ADD(N2A03,N2A03_DEFAULTCLOCK)
		MDRV_CPU_MEMORY(readmem_1,writemem_1)
									/* some carts also trigger IRQs */
		MDRV_FRAMES_PER_SECOND(60)
		MDRV_VBLANK_DURATION(( ( ( 1.0 / 60.0 ) * 1000000.0 ) / 262 ) * ( 262 - 239 ))
	
		MDRV_MACHINE_INIT(vsdual)
	
		/* video hardware */
		MDRV_VIDEO_ATTRIBUTES(VIDEO_TYPE_RASTER | VIDEO_DUAL_MONITOR)
		MDRV_ASPECT_RATIO(8,3)
		MDRV_SCREEN_SIZE(32*8*2, 30*8)
		MDRV_VISIBLE_AREA(0*8, 32*8*2-1, 0*8, 30*8-1)
		MDRV_GFXDECODE(nes_gfxdecodeinfo)
		MDRV_PALETTE_LENGTH(2*4*16)
		MDRV_COLORTABLE_LENGTH(2*4*16)
	
		MDRV_PALETTE_INIT(vsdual)
		MDRV_VIDEO_START(vsdual)
		MDRV_VIDEO_UPDATE(vsdual)
	
		/* sound hardware */
		MDRV_SOUND_ADD(NES, nes_dual_interface)
		MDRV_SOUND_ADD(DAC, nes_dual_dac_interface)
	MACHINE_DRIVER_END
	
	
	/******************************************************************************/
	
	
	static RomLoadPtr rom_suprmrio = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "1d",  0x8000, 0x2000, CRC(be4d5436) SHA1(08162a7c987f1939d09bebdb676f596c86abf465) )
		ROM_LOAD( "1c",  0xa000, 0x2000, CRC(0011fc5a) SHA1(5c2c49938a12affc03e64e5bdab307998be20020) )
		ROM_LOAD( "1b",  0xc000, 0x2000, CRC(b1b87893) SHA1(8563ceaca664cf4495ef1020c07179ca7e4af9f3) )
		ROM_LOAD( "1a",  0xe000, 0x2000, CRC(1abf053c) SHA1(f17db88ce0c9bf1ed88dc16b9650f11d10835cec) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0  ) /* PPU memory */
		ROM_LOAD( "2b",  0x0000, 0x2000, CRC(42418d40) SHA1(22ab61589742cfa4cc6856f7205d7b4b8310bc4d) )
		ROM_LOAD( "2a",  0x2000, 0x2000, CRC(15506b86) SHA1(69ecf7a3cc8bf719c1581ec7c0d68798817d416f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_iceclimb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "ic-1d",  0x8000, 0x2000, CRC(65e21765) SHA1(900f1efe5e8005ee8cdccbf5039914dfe466aa3d) )
		ROM_LOAD( "ic-1c",  0xa000, 0x2000, CRC(a7909c51) SHA1(04708a9e429cbddab6988ff7b3ec5aa0109f6228) )
		ROM_LOAD( "ic-1b",  0xc000, 0x2000, CRC(7fb3cc21) SHA1(bed673211f2251d4112ea41c4a1f917fee32d93c) )
		ROM_LOAD( "ic-1a",  0xe000, 0x2000, CRC(bf196bf7) SHA1(7d7b34894caab41ac51ca9c89d09e72053798784) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "ic-2b",  0x0000, 0x2000, CRC(331460b4) SHA1(4cf94d711cdb5715d14f1ab3cadec245e0adfb1e) )
		ROM_LOAD( "ic-2a",  0x2000, 0x2000, CRC(4ec44fb3) SHA1(676e0ab574dec08df562c6f278e8a9cc7c8afa41) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_iceclmbj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "ic4_46db.bin",  0x8000, 0x2000, CRC(0ea5f9cb) SHA1(3ba6228ac8011371fc36ce9dde4fc158a81a99a2) )
		ROM_LOAD( "ic4_46cb.bin",  0xa000, 0x2000, CRC(51fe438e) SHA1(f40812d4275dabaac6f9539e1300c08d07992654) )
		ROM_LOAD( "ic446bb1.bin",  0xc000, 0x2000, CRC(a8afdc62) SHA1(f798da6c107926790026d4a4d384961dbff2380e) )
		ROM_LOAD( "ic4-46ab.bin",  0xe000, 0x2000, CRC(96505d4d) SHA1(0fb913853decebec1d5d15ee5adc8027cd66f016) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "ic-2b",  0x0000, 0x2000, CRC(331460b4) SHA1(4cf94d711cdb5715d14f1ab3cadec245e0adfb1e) )
		ROM_LOAD( "ic-2a",  0x2000, 0x2000, CRC(4ec44fb3) SHA1(676e0ab574dec08df562c6f278e8a9cc7c8afa41) )
	ROM_END(); }}; 
	
	/* Gun games */
	static RomLoadPtr rom_duckhunt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "1d",  0x8000, 0x2000, CRC(3f51f0ed) SHA1(984d8a5cecddde776ffd4f718ee0ca7a9959228b) )
		ROM_LOAD( "1c",  0xa000, 0x2000, CRC(8bc7376c) SHA1(d90d663c5e5b6d5247089c8ba618912305049b19) )
		ROM_LOAD( "1b",  0xc000, 0x2000, CRC(a042b6e1) SHA1(df571c31a6a52df56869eda0621f7615a625e66d) )
		ROM_LOAD( "1a",  0xe000, 0x2000, CRC(1906e3ab) SHA1(bff68829a96e2d251dd12129f84bdf1dbdf61d06) )
	
		ROM_REGION( 0x4000, REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "2b",  0x0000, 0x2000, CRC(0c52ec28) SHA1(c8fb6a5d4c13a7075d313326e2da9ce88780a88d) )
		ROM_LOAD( "2a",  0x2000, 0x2000, CRC(3d238df3) SHA1(e868ef3d5357ef5294e4faeecc9dbf801c5253e8) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_hogalley = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1,0  ) /* 6502 memory */
		ROM_LOAD( "1d",  0x8000, 0x2000, CRC(2089e166) SHA1(7db09b5b6bcd87589bed89a5fc1a4b772155a0f3) )
		ROM_LOAD( "1c",  0xa000, 0x2000, CRC(a85934ae) SHA1(f26af4f60a4072c45e900dff7f74d9907bc2e1e0) )
		ROM_LOAD( "1b",  0xc000, 0x2000, CRC(718e25b3) SHA1(2710827931d3cd55984c3107c3b8e0f691965eaa) )
		ROM_LOAD( "1a",  0xe000, 0x2000, CRC(f9526852) SHA1(244c6a12801d4aa774a416f7c3dd8465d01dbca2) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "2b",  0x0000, 0x2000, CRC(7623e954) SHA1(65cfce87bb4e72f0c84ce5eff48985a38c3dfc4a) )
		ROM_LOAD( "2a",  0x2000, 0x2000, CRC(78c842b6) SHA1(39f2a7fc1f1cbe2378a369e45b5cbb05057db3f0) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsgshoe = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "mds-gm5.1d",  0x10000, 0x4000, CRC(063b342f) SHA1(66f69de27db5b08969f9250d0a6760e7311bd9bf)  ) // its probably not bad .. just banked somehow
		ROM_LOAD( "mds-gm5.1c",  0x14000, 0x2000, CRC(e1b7915e) SHA1(ed0fdf74b05a3ccd1645c4f580436fd439f81dea) )
		ROM_LOAD( "mds-gm5.1b",  0x16000, 0x2000, CRC(5b73aa3c) SHA1(4069a6139091fbff48758953bd894808a8356d46) )
		ROM_LOAD( "mds-gm5.1a",  0x18000, 0x2000, CRC(70e606bc) SHA1(8207ded20cb9109d605ce73deb722de3514ed9bf) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "mds-gm5.2b",  0x0000, 0x2000, CRC(192c3360) SHA1(5ddbe007d8bc693a0b7c92f33e6ed6b27dc1c08e) )
		ROM_LOAD( "mds-gm5.2a",  0x2000, 0x2000, CRC(823dd178) SHA1(77578a48ded0c244d1ae30aafaa9259b7dd0dfc4) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsfdf = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "prg2", 0x10000, 0x10000, CRC(3bce8f0f) SHA1(5a9b91bae4b28c1df54fb290efdec4805f4f217e) )
		ROM_LOAD( "prg1", 0x20000, 0x10000, CRC(c74499ce) SHA1(14f50d4d11c363e761a6472a6e57a5e5a6dab9ce) )
	
		ROM_REGION( 0x10000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "cha2.1",  0x00000, 0x10000, CRC(a2f88df0) SHA1(10ef432d3132b01a1fcb38d8f521edd2a029ac5e) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_goonies = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "prg.u7",  0x10000, 0x10000, CRC(1e438d52) SHA1(ac187904c125e56a71acff979e53f3398a05c075) )
	
		ROM_REGION( 0x10000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "chr.u4",  0x0000, 0x10000, CRC(4c4b61b0) SHA1(7221c2499531e591a5a99e2cb339ae3a76b662c2) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsgradus = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000,REGION_CPU1, 0  ) /* 6502 memory */
		ROM_LOAD( "prg.u7",  0x10000, 0x10000, CRC(d99a2087) SHA1(b26efe78798453a903921723f3c9ac69f579b7d2) )
	
		ROM_REGION( 0x10000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "chr.u4",  0x0000, 0x10000, CRC(23cf2fc3) SHA1(0a3f48aec529b92abc261952e632af7ff766b1ef) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_btlecity = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "bc.1d",  0x8000, 0x2000, CRC(6aa87037) SHA1(f3313700955498800a3d59c523ba2a4e0cf443bc) )
		ROM_LOAD( "bc.1c",  0xa000, 0x2000, CRC(bdb317db) SHA1(a8b3e8deb1e625d764aaffe86a513bc7ede51a46) )
		ROM_LOAD( "bc.1b",  0xc000, 0x2000, CRC(1a0088b8) SHA1(ba90d8178a23caedbf0e7188256b7cbfebf35eeb) )
		ROM_LOAD( "bc.1a",  0xe000, 0x2000, CRC(86307c89) SHA1(e4e73e4dcaa5c2374d7e3844d6d3fdb192ac9674) )
	
		ROM_REGION( 0x4000, REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "bc.2b",  0x0000, 0x2000, CRC(634f68bd) SHA1(db1a18083667fdaf6cdd9ed7666bec6bf6e39f29) )
		ROM_LOAD( "bc.2a",  0x2000, 0x2000, CRC(a9b49a05) SHA1(c14706e6a5524f81e79c101e32deef9f3d60de3f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_cluclu = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0  ) /* 6502 memory */
		ROM_LOAD( "cl.6d",  0x8000, 0x2000, CRC(1e9f97c9) SHA1(47d847632145d8160d006f014f9e0a7483783d0e) )
		ROM_LOAD( "cl.6c",  0xa000, 0x2000, CRC(e8b843a7) SHA1(03827b31d47d2a8a132bf9944fee724c6c1c6d2e) )
		ROM_LOAD( "cl.6b",  0xc000, 0x2000, CRC(418ee9ea) SHA1(a68e8a97899e850884cb9484fe539b86c419f10f) )
		ROM_LOAD( "cl.6a",  0xe000, 0x2000, CRC(5e8a8457) SHA1(8e53de132db2e1299bd8f2329758f3ccb096584a) )
	
		ROM_REGION( 0x4000, REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "cl.8b",  0x0000, 0x2000, CRC(960d9a6c) SHA1(2569d59fd880cfc2eb4638294d1429ba749f5dcb) )
		ROM_LOAD( "cl.8a",  0x2000, 0x2000, CRC(e3139791) SHA1(33d9e6d2a3233ee311c2cef2d0a425ded2cf3b0f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_excitebk = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "eb-1d",  0x8000, 0x2000, CRC(7e54df1d) SHA1(38d878041976386e8608c73133040b18d0e4b9cd) )
		ROM_LOAD( "eb-1c",  0xa000, 0x2000, CRC(89baae91) SHA1(6aebf13c415e3246edf7daa847533b7e3ae0425f) )
		ROM_LOAD( "eb-1b",  0xc000, 0x2000, CRC(4c0c2098) SHA1(078f24ce02f5fb91d7ed7fa59aec8efbec38aed1) )
		ROM_LOAD( "eb-1a",  0xe000, 0x2000, CRC(b9ab7110) SHA1(89e3bd5f42b5b5e869ee46afe4f25a1a17d3814d) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "eb-2b",  0x0000, 0x2000, CRC(80be1f50) SHA1(d8544b9a0a9d8719ab601fa9c68c4305385b14c7) )
		ROM_LOAD( "eb-2a",  0x2000, 0x2000, CRC(a9b49a05) SHA1(c14706e6a5524f81e79c101e32deef9f3d60de3f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_excitbkj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "eb4-46da.bin",  0x8000, 0x2000, CRC(6aa87037) SHA1(f3313700955498800a3d59c523ba2a4e0cf443bc) )
		ROM_LOAD( "eb4-46ca.bin",  0xa000, 0x2000, CRC(bdb317db) SHA1(a8b3e8deb1e625d764aaffe86a513bc7ede51a46) )
		ROM_LOAD( "eb4-46ba.bin",  0xc000, 0x2000, CRC(d1afe2dd) SHA1(ef0f44d98464b7dab7c51be4379242f7a4e4fcdd) )
		ROM_LOAD( "eb4-46aa.bin",  0xe000, 0x2000, CRC(46711d0e) SHA1(6ce2f395b3f407671a87c6e1133ab63a637022f2) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "eb4-48ba.bin",  0x0000, 0x2000, CRC(62a76c52) SHA1(7ebd0dac976abe8636f4f75a3b2a473d7a54934d) )
	//	ROM_LOAD( "eb4-48aa.bin",  0x2000, 0x2000, CRC(a9b49a05) SHA1(c14706e6a5524f81e79c101e32deef9f3d60de3f) )
		ROM_LOAD( "eb-2a",         0x2000, 0x2000, CRC(a9b49a05) SHA1(c14706e6a5524f81e79c101e32deef9f3d60de3f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_jajamaru = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "10.bin", 0x8000, 0x2000, CRC(16af1704) SHA1(ebcf9ad06e302c51ee4432631a6b0fb85a9630ed) )
		ROM_LOAD( "9.bin",  0xa000, 0x2000, CRC(db7d1814) SHA1(6a0c9cf97006a8a41dc2f025a5f8acbb798dec60) )
		ROM_LOAD( "8.bin",  0xc000, 0x2000, CRC(ce263271) SHA1(1e5e2a9e0dcebeccd7df59491ca0bc5ac4d0d42b) )
		ROM_LOAD( "7.bin",  0xe000, 0x2000, CRC(a406d0e4) SHA1(1f67b58bacb145a3ff8b8380b44cd60251051c71) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "12.bin",  0x0000, 0x2000, CRC(c91d536a) SHA1(8cb4b0819652df484553b9dd1f82391d51c90fcc) )
		ROM_LOAD( "11.bin",  0x2000, 0x2000, CRC(f0034c04) SHA1(402dcf6ad443baeee3038ecab12db008a1ad2787) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_ladygolf = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0  ) /* 6502 memory */
		ROM_LOAD( "lg-1d",  0x8000, 0x2000, CRC(8b2ab436) SHA1(145a75f30f1fab5b1babf01ada9ed23f59c2c18d) )
		ROM_LOAD( "lg-1c",  0xa000, 0x2000, CRC(bda6b432) SHA1(c8322f07df0adbd70cb49f2284b046478a3a57c1) )
		ROM_LOAD( "lg-1b",  0xc000, 0x2000, CRC(dcdd8220) SHA1(563028f8db9ad221d8ac8f8096b4587b822eedb7) )
		ROM_LOAD( "lg-1a",  0xe000, 0x2000, CRC(26a3cb3b) SHA1(00131637eb76154c4f04eb54707e0e7b453d4580) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "lg-2b",  0x0000, 0x2000, CRC(95618947) SHA1(e8f09bffa3fa1a1cac8fa25df9fba962951c1fb3) )
		ROM_LOAD( "lg-2a",  0x2000, 0x2000, CRC(d07407b1) SHA1(b998b46fe83e76fac3d7b71495d1da8580a731f9) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_smgolfj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0  ) /* 6502 memory */
		ROM_LOAD( "gf3_6d_b.bin",  0x8000, 0x2000, CRC(8ce375b6) SHA1(f787f5ebe584cc95428b63660cd41e2b3df6ddf2) )
		ROM_LOAD( "gf3_6c_b.bin",  0xa000, 0x2000, CRC(50a938d3) SHA1(5f5c5e50024fe113240f1b0b3b6d38cbf9130133) )
		ROM_LOAD( "gf3_6b_b.bin",  0xc000, 0x2000, CRC(7dc39f1f) SHA1(12ff2f0ec7418754f9b6e600746e15f345e3ddaa) )
		ROM_LOAD( "gf3_6a_b.bin",  0xe000, 0x2000, CRC(9b8a2106) SHA1(008ab9098f9ce564bcb4beb17285c2bc18b529ff) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "gf3_8b_b.bin",  0x0000, 0x2000, CRC(7ef68029) SHA1(a639e463fd0abfb1bff0dd17aa0c9f70a72ee139) )
		ROM_LOAD( "gf3_8a_b.bin",  0x2000, 0x2000, CRC(f2285878) SHA1(e0d34161a1879975f51c12222cf366228170b0e3) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_machridr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1,0 ) /* 6502 memory */
		ROM_LOAD( "mr-1d",  0x8000, 0x2000, CRC(379c44b9) SHA1(7b148ba7f7eea64509733f94b4eaafe5bfcf3527) )
		ROM_LOAD( "mr-1c",  0xa000, 0x2000, CRC(cb864802) SHA1(65f06a8eaca3347432f3f2f673a24692415d869f) )
		ROM_LOAD( "mr-1b",  0xc000, 0x2000, CRC(5547261f) SHA1(aedb7ab1ef0cd32f325ec9fc948ca1e21a78aa7a) )
		ROM_LOAD( "mr-1a",  0xe000, 0x2000, CRC(e3e3900d) SHA1(c66807ca372d2e5ac11030fdf3d30e30617d4e72) )
	
		ROM_REGION( 0x4000,REGION_GFX1 , 0) /* PPU memory */
		ROM_LOAD( "mr-2b",  0x0000, 0x2000, CRC(33a2b41a) SHA1(671f37bce742e63250296e62c143f8a82f860b04) )
		ROM_LOAD( "mr-2a",  0x2000, 0x2000, CRC(685899d8) SHA1(02b6a9bc21367c481d0091fa8a8f2d1b841244bf) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_machridj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1,0 ) /* 6502 memory */
		ROM_LOAD( "mr4-11da.bin",  0x8000, 0x2000, CRC(ab7e0594) SHA1(fc5982a93791608a20e5ec9e3a4b71d702bda354) )
		ROM_LOAD( "mr4-11ca.bin",  0xa000, 0x2000, CRC(d4a341c3) SHA1(c799e40d0ebd1447032d8767fb2caeee6b33f31a) )
		ROM_LOAD( "mr4-11ba.bin",  0xc000, 0x2000, CRC(cbdcfece) SHA1(91f3a0e1e91bdbb61721e9777009299f7e8efa96) )
		ROM_LOAD( "mr4-11aa.bin",  0xe000, 0x2000, CRC(e5b1e350) SHA1(ab30f84597cbf470a02a2d083587cdc589a29a3c) )
	
		ROM_REGION( 0x4000,REGION_GFX1 , 0) /* PPU memory */
		ROM_LOAD( "mr4-12ba.bin",  0x0000, 0x2000, CRC(59867e36) SHA1(2b5546aa9f140277d611d6d5516b1343e5e672a0) )
		ROM_LOAD( "mr4-12aa.bin",  0x2000, 0x2000, CRC(ccfedc5a) SHA1(3d6321681fbe256d7c71037205d45d22fc264569) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_smgolf = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1,0 ) /* 6502 memory */
		ROM_LOAD( "golf-1d",  0x8000, 0x2000, CRC(a3e286d3) SHA1(ee7539a46e0e062ffd63d84e8b83de29b860a501) )
		ROM_LOAD( "golf-1c",  0xa000, 0x2000, CRC(e477e48b) SHA1(2ebcc548ac8defc521860d2d2f585be0eee6620e) )
		ROM_LOAD( "golf-1b",  0xc000, 0x2000, CRC(7d80b511) SHA1(52aa7e798ff8d933b023bcade81a39f7e27d02c5) )
		ROM_LOAD( "golf-1a",  0xe000, 0x2000, CRC(7b767da6) SHA1(0f0f3a24b844265c304b10016f33e91b323a9a98) )
	
		ROM_REGION( 0x4000, REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "golf-2b",  0x0000, 0x2000, CRC(2782a3e5) SHA1(4e57aab58fb250da951a9aecd21d6aa79e697bcb) )
		ROM_LOAD( "golf-2a",  0x2000, 0x2000, CRC(6e93fdef) SHA1(44f46421adabbc40135c681592cb5226b7c9012a) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_smgolfb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "gf4-2.1df",	0x8000, 0x2000, CRC(4a723087) SHA1(87dc063d295f55871598a1e1eb4e62ce298b2f0c) )
		ROM_LOAD( "gf4-2.1cf",  0xa000, 0x2000, CRC(2debda63) SHA1(33b42eb5641ec947b2f2dcbc632ee6c81fa2ffe3) )
		ROM_LOAD( "gf4-2.1bf",  0xc000, 0x2000, CRC(6783652f) SHA1(7165ee59d3787cb56eed4791351da07f4bcc68ed) )
		ROM_LOAD( "gf4-2.1af",  0xe000, 0x2000, CRC(ce788209) SHA1(b62f1a6567cd94e5443afdbc5df33dd1b8ad039d) )
	
		ROM_REGION( 0x4000, REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "gf4-2.2bf",  0x0000, 0x2000, NO_DUMP )
		ROM_LOAD( "gf4-2.2af",  0x2000, 0x2000, CRC(47e9b8c6) SHA1(2eee6eaf7d15e215816363c7bb7142a2bdc2d530) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vspinbal = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "pb-6d",  0x8000, 0x2000, CRC(69fc575e) SHA1(d5165959c3569f5ebccd03d2cad4714f9240cc4c) )
		ROM_LOAD( "pb-6c",  0xa000, 0x2000, CRC(fa9472d2) SHA1(d20ffb156bea1f474ad7d9776e217cb05048f00f) )
		ROM_LOAD( "pb-6b",  0xc000, 0x2000, CRC(f57d89c5) SHA1(03f3a27d806c61fef13b0d8b2d8b9a15ee968e80) )
		ROM_LOAD( "pb-6a",  0xe000, 0x2000, CRC(640c4741) SHA1(930bed577bfc75b03d064dc0ef523c45186fc3c4) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "pb-8b",  0x0000, 0x2000, CRC(8822ee9e) SHA1(950113952e6d356e45e03479ba5dd5a8cb131609) )
		ROM_LOAD( "pb-8a",  0x2000, 0x2000, CRC(cbe98a28) SHA1(c00c5f15a33611bfe3ad420b93b1cc2cae011c3e) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vspinblj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000, REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "pn3_6d_b.bin",  0x8000, 0x2000, CRC(fd50c42e) SHA1(4a3ea9e85b60caf8b6975fd2798bc59e86ec257f) )
		ROM_LOAD( "pn3_6c_b.bin",  0xa000, 0x2000, CRC(59beb9e5) SHA1(682b31dfbdf1ee44fd5d5d63169ab35409e93546) )
		ROM_LOAD( "pn3_6b_b.bin",  0xc000, 0x2000, CRC(ce7f47ce) SHA1(c548c1b94d3807b4968629c7fdce8aae3a61e6e0) )
		ROM_LOAD( "pn3_6a_b.bin",  0xe000, 0x2000, CRC(5685e2ee) SHA1(a38fbf25c93dfc73658d3837b2b6397736e8d2f2) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "pn3_8b_b.bin",  0x0000, 0x2000, CRC(1e3fec3e) SHA1(aef18cee358af202ec48c1c36986e42e134466b1) )
		ROM_LOAD( "pn3_8a_b.bin",  0x2000, 0x2000, CRC(6f963a65) SHA1(af69564b51aa42ef0815c952e0d0d0d928651685) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsslalom = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "slalom.1d",  0x8000, 0x2000, CRC(6240a07d) SHA1(c9a3743a1caaa417c3828365a4c7a75272c20146) )
		ROM_LOAD( "slalom.1c",  0xa000, 0x2000, CRC(27c355e4) SHA1(ba55258396a17858e136fe45332f6cc13a46b072) )
		ROM_LOAD( "slalom.1b",  0xc000, 0x2000, CRC(d4825fbf) SHA1(5e7fcfa1999c52f94be28c693acffc6e5d434674) )
		ROM_LOAD( "slalom.1a",  0xe000, 0x2000, CRC(82333f80) SHA1(fa85f8a481f3847b33fd9df005df4fde59080bce) )
	
		ROM_REGION( 0x2000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "slalom.2a",  0x0000, 0x2000, CRC(977bb126) SHA1(9b12cd37246237c24a8077c6184a2f71d342ac47) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vssoccer = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "soccer1d",  0x8000, 0x2000, CRC(0ac52145) SHA1(148d9850cd80fb64e28f478891c16dac71e67e96) )
		ROM_LOAD( "soccer1c",  0xa000, 0x2000, CRC(f132e794) SHA1(f289f5acec7e2a62fc569a401e7ab5200df302f5) )
		ROM_LOAD( "soccer1b",  0xc000, 0x2000, CRC(26bb7325) SHA1(80e97a36c364a07cf9862202454651fb2872cd51) )
		ROM_LOAD( "soccer1a",  0xe000, 0x2000, CRC(e731635a) SHA1(8089bc49a0115225d26c4cbaaf08431376eafa59) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "soccer2b",  0x0000, 0x2000, CRC(307b19ab) SHA1(b35ef4c2cf071db77cec1b4529b43a20cfcce172) )
		ROM_LOAD( "soccer2a",  0x2000, 0x2000, CRC(7263613a) SHA1(aa5673b57833d1f32c2cb0230a809397ec6103b4) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_starlstr = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "sl_04.1d",  0x8000, 0x2000, CRC(4fd5b385) SHA1(a4cfdb9d74538a162825d9fbbed67e2a645bcc2c) )
		ROM_LOAD( "sl_03.1c",  0xa000, 0x2000, CRC(f26cd7ca) SHA1(f6fd5a6028b111a8fca68684bad651a92e0fd7be) )
		ROM_LOAD( "sl_02.1b",  0xc000, 0x2000, CRC(9308f34e) SHA1(4438d13dad793bbc158a5d163ccd4ae26f914fb5) )
		ROM_LOAD( "sl_01.1a",  0xe000, 0x2000, CRC(d87296e4) SHA1(a1220313f4c6ee1ee0beee9792f2e9038eaa4cb3) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "sl_06.2b",  0x0000, 0x2000, CRC(25f0e027) SHA1(4fcbe4bb959689948cb8f505d5c495dabb893f7b) )
		ROM_LOAD( "sl_05.2a",  0x2000, 0x2000, CRC(2bbb45fd) SHA1(53c3588bd25baa6b8ff41f4755db9e0e806c9719) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vstetris = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "a000.6c",  0xa000, 0x2000, CRC(92a1cf10) SHA1(463f62aec3f26d70b35e804398a38baf8f41a5e3) )
		ROM_LOAD( "c000.6b",  0xc000, 0x2000, CRC(9e9cda9d) SHA1(27d91b957ff0b3abd5567341574318548470fb3c) )
		ROM_LOAD( "e000.6a",  0xe000, 0x2000, CRC(bfeaf6c1) SHA1(2f2150138c023cb7962f3e04d34bd01be9fa2e24) )
	
		ROM_REGION( 0x2000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "char.8b",  0x0000, 0x2000, CRC(51e8d403) SHA1(ed734994d164c4b59794249a13bce333896b3ee5) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_drmario = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "dm-uiprg",  0x10000, 0x10000, CRC(d5d7eac4) SHA1(cd74c3a7a2fc7c25420037ae5f4a25307aff6587) )
	
		ROM_REGION( 0x8000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "dm-u3chr",  0x0000, 0x8000, CRC(91871aa5) SHA1(32a4299ead7b37f49877dc9597653b07a73ddbf3) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_cstlevna = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "mds-cv.prg",  0x10000, 0x20000, CRC(ffbef374) SHA1(9eb3b75e7b45df51b8bcd29df84689a7e8557f4f) )
	
		/* No cart gfx - uses vram */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_topgun = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "rc-003",  0x10000, 0x20000, CRC(8c0c2df5) SHA1(d9b1b87204e025a637821a0168475e1209ce0c8a) )
	
		/* No cart gfx - uses vram */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_tkoboxng = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "tkoprg.bin",  0x10000, 0x10000, CRC(eb2dba63) SHA1(257c9f3565ff1d136094e99636ca57e300352b7e) )
	
		ROM_REGION( 0x10000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "tkochr.bin",  0x0000, 0x10000, CRC(21275ba5) SHA1(160131586aeeca848deabff258a2ce5f62b17c5f) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_rbibb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000,REGION_CPU1,0 ) /* 6502 memory */
		ROM_LOAD( "rbi-prg",  0x10000, 0x10000, CRC(135adf7c) SHA1(e090b0aec98463c565e300a910561499d8bd9676) )
	
		ROM_REGION( 0x8000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "rbi-cha",  0x0000, 0x8000, CRC(a3c14889) SHA1(ef00f4fbf21cf34e946957b9b6825b8e2cb16536) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_rbibba = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x20000,REGION_CPU1,0 ) /* 6502 memory */
		ROM_LOAD( "rbi-prga", 0x10000, 0x10000, CRC(a5939d0d) SHA1(476ac2a3974b69082bb8eebdfc0d15befaa2e165) )
	
		ROM_REGION( 0x8000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "rbi-cha",  0x0000, 0x8000, CRC(a3c14889) SHA1(ef00f4fbf21cf34e946957b9b6825b8e2cb16536) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsskykid = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x18000,REGION_CPU1,0 ) /* 6502 memory */
		ROM_LOAD( "sk-prg1",  0x10000, 0x08000, CRC(cf36261e) SHA1(e4a3d2a223f066c231631d92504f08e60b303dfd) )
	
		ROM_REGION( 0x8000,REGION_GFX1 , 0) /* PPU memory */
		ROM_LOAD( "sk-cha",  0x0000, 0x8000, CRC(9bd44dad) SHA1(bf33d175b6ab991d63a0acaf83ba22d5b7ab11b9) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_platoon = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "prgver0.ic4",  0x10000, 0x20000, CRC(e2c0a2be) SHA1(1f8e33d6da8402be6a376668a424bfde38471021) )
	
		ROM_REGION( 0x20000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "chrver0.ic6",  0x00000, 0x20000, CRC(689df57d) SHA1(854aaa9feb16e3f239fba6069fbf65e69858fe73) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_bnglngby = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "rb4-26db.bin", 0x8000, 0x2000, CRC(d152d8c2) SHA1(d127195be8219df1c6f7bdd86658ed26c658470e) )
		ROM_LOAD( "rb4-26cb.bin", 0xa000, 0x2000, CRC(c3383935) SHA1(8ed1e8ed36069e5e6f2f3c672aae5e1f3dabbdd0) )
		ROM_LOAD( "rb4-26bb.bin", 0xc000, 0x2000, CRC(e2a24af8) SHA1(89cca4188b859882487fe64776c1ca0173fee142) )
		ROM_LOAD( "rb4-26ab.bin", 0xe000, 0x2000, CRC(024ad874) SHA1(b02241c3d2ae90ccd5402410fa650741034a2f78) )
	
		ROM_REGION( 0x4000, REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "rb4-28bb.bin", 0x0000, 0x2000, CRC(d3d946ab) SHA1(e2ed8af0cf6edb925c1ff47fccb5caabd0b8c09f) )
		ROM_LOAD( "rb4-28ab.bin", 0x2000, 0x2000, CRC(ca08126a) SHA1(48b315e3e90b19b2d74dcd88c734dcdf3539d6ca) )
	
		ROM_REGION( 0x2000, REGION_USER1, 0 ) /* unknown */
		ROM_LOAD( "rb4-21ab.bin", 0x0000, 0x2000, CRC(b49939ad) SHA1(ebaab2864d9ff9876e9d2666746c4bab57e49ec3) ) /* Unknown, maps at 0xe000, maybe from another set, but we have other roms? */
	ROM_END(); }}; 
	
	static RomLoadPtr rom_supxevs = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x30000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "prg2",  0x10000, 0x10000, CRC(645669f0) SHA1(3b18c0bb33dd5a95f52a2de7b9a5730990517ad9) )
		ROM_LOAD( "prg1",  0x20000, 0x10000, CRC(ff762ceb) SHA1(04ca386ef4ad79f99d1efdc0a4d908ef0e523d75) )
	
		ROM_REGION( 0x8000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "cha",   0x00000, 0x08000, CRC(e27c7434) SHA1(a033bbaf0c28504ed2a641dea28f016a88ef03ac) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_mightybj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "1d.bin",  0x8000, 0x2000, CRC(55dc8d77) SHA1(eafb8636d994a10caee9eb0ba544260281706058) )
		ROM_LOAD( "1c.bin",  0xa000, 0x2000, CRC(151a6d15) SHA1(2652aef97aae122711ef471d9dc1d42f6393b91f) )
		ROM_LOAD( "1b.bin",  0xc000, 0x2000, CRC(9f9944bc) SHA1(58b1aca3e0cd32769978c704177d6ddeb70ac95a) )
		ROM_LOAD( "1a.bin",  0xe000, 0x2000, CRC(76f49b65) SHA1(c50fd29ea91bba3d59e943496d0941fe0e4efcb2) )
	
		ROM_REGION( 0x2000, REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "2b.bin",  0x0000, 0x2000, CRC(5425a4d0) SHA1(09eb9d93b680c9eefde5ee6e16cf81de931cccb9) )
	ROM_END(); }}; 
	
	/* Dual System */
	
	static RomLoadPtr rom_balonfgt = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "bf.1d",  0x08000, 0x02000, CRC(1248a6d6) SHA1(0f6c586e8e021a0710ec4e967750b55a74229d74) )
		ROM_LOAD( "bf.1c",  0x0a000, 0x02000, CRC(14af0e42) SHA1(ceb749eca2dfe81fddc6cb57e4aa87a4bfac0316) )
		ROM_LOAD( "bf.1b",  0x0c000, 0x02000, CRC(a420babf) SHA1(ab296a86132bb9103cbb107518b4ac9beb8b2e11) )
		ROM_LOAD( "bf.1a",  0x0e000, 0x02000, CRC(9c31f94d) SHA1(19bccd6b79423f495b0ee49dd3b219ffc4676470) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "bf.2b",  0x0000, 0x2000, CRC(f27d9aa0) SHA1(429a1ad2a07947e4c4809495bfab55bf0f0e428f) )
		ROM_LOAD( "bf.2a",  0x2000, 0x2000, CRC(76e6bbf8) SHA1(a4cae3a129a787162050187453b1583c8735fb46) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 ) /* 6502 memory */
		ROM_LOAD( "bf.6d",  0x08000, 0x02000, CRC(ef4ebff1) SHA1(17153ad44a402f05f7ddfe3ac364a0e4adb6f16b) )
		ROM_LOAD( "bf.6c",  0x0a000, 0x02000, CRC(14af0e42) SHA1(ceb749eca2dfe81fddc6cb57e4aa87a4bfac0316) )
		ROM_LOAD( "bf.6b",  0x0c000, 0x02000, CRC(a420babf) SHA1(ab296a86132bb9103cbb107518b4ac9beb8b2e11) )
		ROM_LOAD( "bf.6a",  0x0e000, 0x02000, CRC(3aa5c095) SHA1(3815016e5615c9327200150e0181357f16f3d636) )
	
		ROM_REGION( 0x4000,REGION_GFX2, 0 ) /* PPU memory */
		ROM_LOAD( "bf.8b",  0x0000, 0x2000, CRC(f27d9aa0) SHA1(429a1ad2a07947e4c4809495bfab55bf0f0e428f) )
		ROM_LOAD( "bf.8a",  0x2000, 0x2000, CRC(76e6bbf8) SHA1(a4cae3a129a787162050187453b1583c8735fb46) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsmahjng = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "mj.1c",  0x0a000, 0x02000, CRC(ec77671f) SHA1(3716a4d5ab1efee0416dd7f6466d29379dc6f296) )
		ROM_LOAD( "mj.1b",  0x0c000, 0x02000, CRC(ac53398b) SHA1(2582c73efec233a389900949d6af7c4c9a9e7148) )
		ROM_LOAD( "mj.1a",  0x0e000, 0x02000, CRC(62f0df8e) SHA1(5628397c5d9acf470cc0cbffdba20e9e4cc8ea91) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "mj.2b",  0x0000, 0x2000, CRC(9dae3502) SHA1(b7ffbc17af35eeac1b06c651f6c25f71827e9c3b) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 ) /* 6502 memory */
		ROM_LOAD( "mj.6c",  0x0a000, 0x02000, CRC(3cee11e9) SHA1(03ae904a98a12b5571374417069e50f8bc824c24) )
		ROM_LOAD( "mj.6b",  0x0c000, 0x02000, CRC(e8341f7b) SHA1(cf3c43e4f87dbcd4ae9a74f2808282883c8ba38a) )
		ROM_LOAD( "mj.6a",  0x0e000, 0x02000, CRC(0ee69f25) SHA1(078e8f51887be58336ff23f90bacfa90c1730f36) )
	
		ROM_REGION( 0x4000,REGION_GFX2, 0 ) /* PPU memory */
		ROM_LOAD( "mj.8b",  0x0000, 0x2000, CRC(9dae3502) SHA1(b7ffbc17af35eeac1b06c651f6c25f71827e9c3b) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsbball = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "bb-1d",  0x08000, 0x02000, CRC(0cc5225f) SHA1(a8eb3153ce3f1282901c305177347112df0fb3b2) )
		ROM_LOAD( "bb-1c",  0x0a000, 0x02000, CRC(9856ac60) SHA1(f033171c3dea6af63f1f328fee74e695c67adc92) )
		ROM_LOAD( "bb-1b",  0x0c000, 0x02000, CRC(d1312e63) SHA1(0fc46a4ef0fb8a304320f8b3cac3edd1cd9ed286) )
		ROM_LOAD( "bb-1a",  0x0e000, 0x02000, CRC(28199b4d) SHA1(e63d69662d3b70b883028d3103c8f65de8f5edda) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "bb-2b",  0x0000, 0x2000, CRC(3ff8bec3) SHA1(28c1bf89ed1046243ca8cf122cefa0752c242577) )
		ROM_LOAD( "bb-2a",  0x2000, 0x2000, CRC(ebb88502) SHA1(010fdffbd1cddcde8176aaaae5ca8e9c3411c02a) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 ) /* 6502 memory */
		ROM_LOAD( "bb-6d",  0x08000, 0x02000, CRC(7ec792bc) SHA1(92d1f8809db89a8d99f7ea1d2ba3f9be69195866) )
		ROM_LOAD( "bb-6c",  0x0a000, 0x02000, CRC(b631f8aa) SHA1(0ee8a8def9512552037fdac1a14a3ea9393bb943) )
		ROM_LOAD( "bb-6b",  0x0c000, 0x02000, CRC(c856b45a) SHA1(7f15613120d72859ea1ed647c9eee3074f63f0b9) )
		ROM_LOAD( "bb-6a",  0x0e000, 0x02000, CRC(06b74c18) SHA1(9a61161b4856b88e40eee6edb39e0a608748cf0b) )
	
		ROM_REGION( 0x4000,REGION_GFX2, 0 ) /* PPU memory */
		ROM_LOAD( "bb-8b",  0x0000, 0x2000, CRC(3ff8bec3) SHA1(28c1bf89ed1046243ca8cf122cefa0752c242577) )
		ROM_LOAD( "bb-8a",  0x2000, 0x2000, CRC(13b20cfd) SHA1(cb333cbea09557a9d2bdc351fabc61fc7760c35d) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsbballj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "ba_1d_a1.bin",  0x08000, 0x02000, CRC(6dbc129b) SHA1(3e786632563364bf7ae13c7d25c522999f237009) )
		ROM_LOAD( "ba_1c_a1.bin",  0x0a000, 0x02000, CRC(2a684b3a) SHA1(316aa1051a5ff33e5a2369f9e984b34f637595ff) )
		ROM_LOAD( "ba_1b_a1.bin",  0x0c000, 0x02000, CRC(7ca0f715) SHA1(cf87e530c15c142efa48d6462870bbdf44002f45) )
		ROM_LOAD( "ba_1a_a1.bin",  0x0e000, 0x02000, CRC(926bb4fc) SHA1(b9b8611b90d73f39f65166010058e03d0aad5bb0) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "ba_2b_a.bin",  0x0000, 0x2000, CRC(919147d0) SHA1(9fccdfccc2a3ec634e350880ded7053f36c377bc) )
		ROM_LOAD( "ba_2a_a.bin",  0x2000, 0x2000, CRC(3f7edb00) SHA1(f59d24f15bdb8903187eabc1578dcb60443614ed) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 ) /* 6502 memory */
		ROM_LOAD( "ba_6d_a1.bin",  0x08000, 0x02000, CRC(d534dca4) SHA1(6d454a2b5944f98c95d3a1bdeee8e8e52524cb21) )
		ROM_LOAD( "ba_6c_a1.bin",  0x0a000, 0x02000, CRC(73904bbc) SHA1(d32a0f659d628b98a0b06f846842432f83e79a07) )
		ROM_LOAD( "ba_6b_a1.bin",  0x0c000, 0x02000, CRC(7c130724) SHA1(99134180e158eaa4b260d1dacf9aa56a6d48ad73) )
		ROM_LOAD( "ba_6a_a1.bin",  0x0e000, 0x02000, CRC(d938080e) SHA1(35e00bd76364ec88fb3bb8908bc9171df9cd26de) )
	
		ROM_REGION( 0x4000,REGION_GFX2, 0 ) /* PPU memory */
		ROM_LOAD( "ba_8b_a.bin",  0x0000, 0x2000, CRC(919147d0) SHA1(9fccdfccc2a3ec634e350880ded7053f36c377bc) )
		ROM_LOAD( "ba_8a_a.bin",  0x2000, 0x2000, CRC(3f7edb00) SHA1(f59d24f15bdb8903187eabc1578dcb60443614ed) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsbbalja = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "ba_1d_a2.bin",  0x08000, 0x02000, CRC(f3820b70) SHA1(c50d0c2e34f646dd186ee0f2774e94add733f21d) )
		ROM_LOAD( "ba_1c_a2.bin",  0x0a000, 0x02000, CRC(39fbbf28) SHA1(9941defda548f2c51cf62f0ad62a041ee9a69c37) )
		ROM_LOAD( "ba_1b_a2.bin",  0x0c000, 0x02000, CRC(b1377b12) SHA1(9afca83f343b768de8ac51c5967f8825de9d7883) )
		ROM_LOAD( "ba_1a_a2.bin",  0x0e000, 0x02000, CRC(08fab347) SHA1(b6ecd1464c47afac922355b8d5e961892e58a0ed) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "ba_2b_a.bin",  0x0000, 0x2000, CRC(919147d0) SHA1(9fccdfccc2a3ec634e350880ded7053f36c377bc) )
		ROM_LOAD( "ba_2a_a.bin",  0x2000, 0x2000, CRC(3f7edb00) SHA1(f59d24f15bdb8903187eabc1578dcb60443614ed) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 ) /* 6502 memory */
		ROM_LOAD( "ba_6d_a2.bin",  0x08000, 0x02000, CRC(c69561b0) SHA1(4234d88ffa957e7f70ef9da8c61db4e251c3bc66) )
		ROM_LOAD( "ba_6c_a2.bin",  0x0a000, 0x02000, CRC(17d1ca39) SHA1(2fa61a2c39495b72a22f001a72e4526e86d9544e) )
		ROM_LOAD( "ba_6b_a2.bin",  0x0c000, 0x02000, CRC(37481900) SHA1(dbab48d6c95e365ee4ab6ca4c61224b2c813e538) )
		ROM_LOAD( "ba_6a_a2.bin",  0x0e000, 0x02000, CRC(a44ffc4b) SHA1(ec65c3b52659dacfd2b7afe1e744e7bbd61fd6e1) )
	
		ROM_REGION( 0x4000,REGION_GFX2, 0 ) /* PPU memory */
		ROM_LOAD( "ba_8b_a.bin",  0x0000, 0x2000, CRC(919147d0) SHA1(9fccdfccc2a3ec634e350880ded7053f36c377bc) )
		ROM_LOAD( "ba_8a_a.bin",  0x2000, 0x2000, CRC(3f7edb00) SHA1(f59d24f15bdb8903187eabc1578dcb60443614ed) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vsbbaljb = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "ba_1d_a3.bin",  0x08000, 0x02000, CRC(e234d609) SHA1(a148d6b57fbc9d5f91737fa30c2df2c2b66df404) )
		ROM_LOAD( "ba_1c_a3.bin",  0x0a000, 0x02000, CRC(ca1a9591) SHA1(3544f244c59d3dab40c2745e84775b7c1defaf54) )
		ROM_LOAD( "ba_1b_a3.bin",  0x0c000, 0x02000, CRC(50e1f6cf) SHA1(8eb4ccb4817295084280ffd1ee5261eee02485c5) )
		ROM_LOAD( "ba_1a_a3.bin",  0x0e000, 0x02000, BAD_DUMP CRC(4312aa6d) SHA1(dfadbbb6b03a3c1b5cc56c6c60f5005d4b572d8d) ) //FIXED BITS (xxxxxxx1)
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "ba_2b_a.bin",  0x0000, 0x2000, CRC(919147d0) SHA1(9fccdfccc2a3ec634e350880ded7053f36c377bc) )
		ROM_LOAD( "ba_2a_a.bin",  0x2000, 0x2000, CRC(3f7edb00) SHA1(f59d24f15bdb8903187eabc1578dcb60443614ed) )
	
		ROM_REGION( 0x10000,REGION_CPU2,0 ) /* 6502 memory */
		ROM_LOAD( "ba_6d_a3.bin",  0x08000, 0x02000, CRC(6eb9e36e) SHA1(3877dee54a1a11417296150f7e7a1ae2c2847484) )
		ROM_LOAD( "ba_6c_a3.bin",  0x0a000, 0x02000, CRC(dca4dc75) SHA1(231819edb58caf96b4f5c56a44163fbb666dc67d) )
		ROM_LOAD( "ba_6b_a3.bin",  0x0c000, 0x02000, CRC(46cf6f84) SHA1(125af20e1e9066e4b92174ba0a7f59271ef57557) )
		ROM_LOAD( "ba_6a_a3.bin",  0x0e000, 0x02000, CRC(4cbc2cac) SHA1(90bed7694836075738d99aa8fe672dbffa7bbd6d) )
	
		ROM_REGION( 0x4000,REGION_GFX2, 0 ) /* PPU memory */
		ROM_LOAD( "ba_8b_a.bin",  0x0000, 0x2000, CRC(919147d0) SHA1(9fccdfccc2a3ec634e350880ded7053f36c377bc) )
		ROM_LOAD( "ba_8a_a.bin",  0x2000, 0x2000, CRC(3f7edb00) SHA1(f59d24f15bdb8903187eabc1578dcb60443614ed) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_vstennis = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "vst-1d",  0x08000, 0x02000, CRC(f4e9fca0) SHA1(05b91f578bc0a118ab75ce487b14adcd1fb6e714) )
		ROM_LOAD( "vst-1c",  0x0a000, 0x02000, CRC(7e52df58) SHA1(a5ddebfa1f7f1a2b6b46d4b4a7f2c36477158e7e) )
		ROM_LOAD( "vst-1b",  0x0c000, 0x02000, CRC(1a0d809a) SHA1(44ce2f9250940bf5f754918b4a2ae63f76181eff) )
		ROM_LOAD( "vst-1a",  0x0e000, 0x02000, CRC(8483a612) SHA1(c854f72d86fe4e99c4c6426cfc5ea6f2997bfc8c) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "vst-2b",  0x0000, 0x2000, CRC(9de19c9c) SHA1(1cb65e423a6c2d2a56c67ad08ecf7e746551c322) )
		ROM_LOAD( "vst-2a",  0x2000, 0x2000, CRC(67a5800e) SHA1(7bad1b486d9dac962fa8c87984038be4ac6b699b) )
	
		ROM_REGION( 0x10000,REGION_CPU2, 0 ) /* 6502 memory */
		ROM_LOAD( "vst-6d",  0x08000, 0x02000, CRC(3131b1bf) SHA1(ed26df260df3a295b5c9747530428efec29676c0) )
		ROM_LOAD( "vst-6c",  0x0a000, 0x02000, CRC(27195d13) SHA1(a1d6960a194cb048c5c26f9378b49da7d6e7d1af) )
		ROM_LOAD( "vst-6b",  0x0c000, 0x02000, CRC(4b4e26ca) SHA1(68821357f473a0e1c575b547cc8c67be965fe73a) )
		ROM_LOAD( "vst-6a",  0x0e000, 0x02000, CRC(b6bfee07) SHA1(658458931efbb260faec3a11ee530326c56e63a9) )
	
		ROM_REGION( 0x4000,REGION_GFX2 , 0) /* PPU memory */
		ROM_LOAD( "vst-8b",  0x0000, 0x2000, CRC(c81e9260) SHA1(6d4809a05364cc05485ee1add833428529af2be6) )
		ROM_LOAD( "vst-8a",  0x2000, 0x2000, CRC(d91eb295) SHA1(6b69bcef5421a6bcde89a2d1f514853f9f7992c3) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_wrecking = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "wr.1d",  0x08000, 0x02000, CRC(8897e1b9) SHA1(7d33f6ee78d8663d62e6e05e231fd3d19ad09baa) )
		ROM_LOAD( "wr.1c",  0x0a000, 0x02000, CRC(d4dc5ebb) SHA1(bce9b2ebabe7b882f1bc71e2dd50906365521d78) )
		ROM_LOAD( "wr.1b",  0x0c000, 0x02000, CRC(8ee4a454) SHA1(58e970780a2ef5d44950dba6b44e501d320c9588) )
		ROM_LOAD( "wr.1a",  0x0e000, 0x02000, CRC(63d6490a) SHA1(40f573713b7729bc26f41978583defca47f75033) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "wr.2b",  0x0000, 0x2000, CRC(455d77ac) SHA1(fa09d0be51cc780f6c16cd314facc84043e1e69b) )
		ROM_LOAD( "wr.2a",  0x2000, 0x2000, CRC(653350d8) SHA1(d9aa699394654deaf50fadd8a652f08a340377eb) )
	
		ROM_REGION( 0x10000,REGION_CPU2, 0 ) /* 6502 memory */
		ROM_LOAD( "wr.6d",  0x08000, 0x02000, CRC(90e49ce7) SHA1(dca3004305979dc09500fae4667084363fac761f) )
		ROM_LOAD( "wr.6c",  0x0a000, 0x02000, CRC(a12ae745) SHA1(15deabebc4ef59f08aa8ead3f576ed5cbde4c62e) )
		ROM_LOAD( "wr.6b",  0x0c000, 0x02000, CRC(03947ca9) SHA1(02f0404d2351d2475240818b6b103a6e01691daf) )
		ROM_LOAD( "wr.6a",  0x0e000, 0x02000, CRC(2c0a13ac) SHA1(47e6a50c210508fab51062eb5c8a3e1129c18125) )
	
		ROM_REGION( 0x4000,REGION_GFX2, 0 ) /* PPU memory */
		ROM_LOAD( "wr.8b",  0x0000, 0x2000, CRC(455d77ac) SHA1(fa09d0be51cc780f6c16cd314facc84043e1e69b) )
		ROM_LOAD( "wr.8a",  0x2000, 0x2000, CRC(653350d8) SHA1(d9aa699394654deaf50fadd8a652f08a340377eb) )
	ROM_END(); }}; 
	
	static RomLoadPtr rom_iceclmrj = new RomLoadPtr(){ public void handler(){ 
		ROM_REGION( 0x10000,REGION_CPU1, 0 ) /* 6502 memory */
		ROM_LOAD( "ic4-41da.bin",  0x08000, 0x02000, CRC(94e3197d) SHA1(414156809a3fe2c072d8947a91708f3ed40008b2) )
		ROM_LOAD( "ic4-41ca.bin",  0x0a000, 0x02000, CRC(b253011e) SHA1(abc2c84e342d1f8e8d0dbb580370733ef4b38413) )
		ROM_LOAD( "ic441ba1.bin",  0x0c000, 0x02000, CRC(f3795874) SHA1(f22f786960a27ab886a7fad7e312bdf28ffa5362) )
		ROM_LOAD( "ic4-41aa.bin",  0x0e000, 0x02000, CRC(094c246c) SHA1(82aba548706041c2de0cda02d21409fe8a09338c) )
	
		ROM_REGION( 0x4000,REGION_GFX1, 0 ) /* PPU memory */
		ROM_LOAD( "ic4-42ba.bin",  0x0000, 0x2000, CRC(331460b4) SHA1(4cf94d711cdb5715d14f1ab3cadec245e0adfb1e) )
		ROM_LOAD( "ic4-42aa.bin",  0x2000, 0x2000, CRC(4ec44fb3) SHA1(676e0ab574dec08df562c6f278e8a9cc7c8afa41) )
	
		ROM_REGION( 0x10000,REGION_CPU2, 0 ) /* 6502 memory */
		ROM_LOAD( "ic4-46da.bin",  0x08000, 0x02000, CRC(94e3197d) SHA1(414156809a3fe2c072d8947a91708f3ed40008b2) )
		ROM_LOAD( "ic4-46ca.bin",  0x0a000, 0x02000, CRC(b253011e) SHA1(abc2c84e342d1f8e8d0dbb580370733ef4b38413) )
		ROM_LOAD( "ic4-46ba.bin",  0x0c000, 0x02000, CRC(2ee9c1f9) SHA1(71619cff6d41cf5a8f74a689e30c2a24020f7d06) )
		ROM_LOAD( "ic4-46aa.bin",  0x0e000, 0x02000, CRC(094c246c) SHA1(82aba548706041c2de0cda02d21409fe8a09338c) )
	
		ROM_REGION( 0x4000,REGION_GFX2, 0 ) /* PPU memory */
		ROM_LOAD( "ic4-48ba.bin",  0x0000, 0x2000, CRC(331460b4) SHA1(4cf94d711cdb5715d14f1ab3cadec245e0adfb1e) )
		ROM_LOAD( "ic4-48aa.bin",  0x2000, 0x2000, CRC(4ec44fb3) SHA1(676e0ab574dec08df562c6f278e8a9cc7c8afa41) )
	ROM_END(); }}; 
	
	/******************************************************************************/
	
	/*    YEAR  NAME      PARENT    MACHINE  INPUT     INIT  	   MONITOR  */
	GAME( 1985, btlecity, 0,        vsnes,   btlecity, btlecity, ROT0, "Namco",     "Vs. Battle City" )
	GAME( 1985, starlstr, 0,        vsnes,   starlstr, vsnormal, ROT0, "Namco",     "Vs. Star Luster" )
	GAME( 1987,	cstlevna, 0,        vsnes,   cstlevna, cstlevna, ROT0, "Konami",    "Vs. Castlevania" )
	GAME( 1984, cluclu,   0,        vsnes,   cluclu,   suprmrio, ROT0, "Nintendo",  "Vs. Clu Clu Land" )
	GAME( 1990,	drmario,  0,        vsnes,   drmario,  drmario,  ROT0, "Nintendo",  "Vs. Dr. Mario" )
	GAME( 1985, duckhunt, 0,        vsnes,   duckhunt, duckhunt, ROT0, "Nintendo",  "Vs. Duck Hunt" )
	GAME( 1984, excitebk, 0,        vsnes,   excitebk, excitebk, ROT0, "Nintendo",  "Vs. Excitebike" )
	GAME( 1984, excitbkj, excitebk, vsnes,   excitebk, excitbkj, ROT0, "Nintendo",  "Vs. Excitebike (Japan)" )
	GAME( 1986,	goonies,  0,        vsnes,   goonies,  goonies,  ROT0, "Konami",    "Vs. The Goonies" )
	GAME( 1985, hogalley, 0,        vsnes,   hogalley, hogalley, ROT0, "Nintendo",  "Vs. Hogan's Alley" )
	GAME( 1984, iceclimb, 0,        vsnes,   iceclimb, suprmrio, ROT0, "Nintendo",  "Vs. Ice Climber" )
	GAME( 1984, iceclmbj, iceclimb, vsnes,   iceclmbj, suprmrio, ROT0, "Nintendo",  "Vs. Ice Climber (Japan)" )
	GAME( 1984, ladygolf, 0,        vsnes,   golf,     machridr, ROT0, "Nintendo",  "Vs. Stroke and Match Golf (Ladies Version)" )
	GAMEX(1985, machridr, 0,        vsnes,   machridr, machridr, ROT0, "Nintendo",  "Vs. Mach Rider (Endurance Course Version)", GAME_IMPERFECT_GRAPHICS )
	GAMEX(1985, machridj, machridr, vsnes,   machridj, vspinbal, ROT0, "Nintendo",  "Vs. Mach Rider (Japan, Fighting Course Version)", GAME_IMPERFECT_GRAPHICS )
	GAME( 1986, rbibb,    0,        vsnes,   rbibb,    rbibb,    ROT0, "Namco",     "Vs. Atari R.B.I. Baseball (set 1)" )
	GAME( 1986, rbibba,	  rbibb,    vsnes,   rbibb,    rbibb,    ROT0, "Namco",     "Vs. Atari R.B.I. Baseball (set 2)" )
	GAME( 1986, suprmrio, 0,        vsnes,   suprmrio, suprmrio, ROT0, "Nintendo",  "Vs. Super Mario Bros." )
	GAME( 1985, vsskykid, 0,        vsnes,   vsskykid, MMC3,	 ROT0, "Namco",     "Vs. Super SkyKid"  )
	GAMEX(1987, tkoboxng, 0,        vsnes,   tkoboxng, tkoboxng, ROT0, "Namco LTD.","Vs. TKO Boxing", GAME_WRONG_COLORS | GAME_IMPERFECT_GRAPHICS )
	GAME( 1984, smgolf,   0,        vsnes,   golf4s,   machridr, ROT0, "Nintendo",  "Vs. Stroke and Match Golf (Men Version)" )
	GAME( 1984, smgolfj,  smgolf,   vsnes,   golf,     vsnormal, ROT0, "Nintendo",  "Vs. Stroke and Match Golf (Men Version) (Japan)" )
	GAME( 1984, vspinbal, 0,        vsnes,   vspinbal, vspinbal, ROT0, "Nintendo",  "Vs. Pinball" )
	GAME( 1984, vspinblj, vspinbal, vsnes,   vspinblj, vsnormal, ROT0, "Nintendo",  "Vs. Pinball (Japan)" )
	GAMEX(1986, vsslalom, 0,        vsnes,   vsslalom, vsslalom, ROT0, "Rare LTD.", "Vs. Slalom", GAME_IMPERFECT_GRAPHICS )
	GAME( 1985, vssoccer, 0,        vsnes,   vssoccer, excitebk, ROT0, "Nintendo",  "Vs. Soccer" )
	GAME( 1986, vsgradus, 0,        vsnes,   vsgradus, vsgradus, ROT0, "Konami",    "Vs. Gradius" )
	GAMEX(1987, platoon,  0,        vsnes,   platoon,  platoon,  ROT0, "Ocean Software Limited", "Vs. Platoon", GAME_WRONG_COLORS )
	GAMEX(1987, vstetris, 0,        vsnes,   vstetris, vstetris, ROT0, "Academysoft-Elory", "Vs. Tetris" , GAME_IMPERFECT_COLORS )
	GAME( 1986, mightybj, 0,        vsnes,   mightybj, mightybj, ROT0, "Tecmo",     "Vs. Mighty Bomb Jack (Japan)" )
	GAMEX(1985, jajamaru, 0,        vsnes,   jajamaru, jajamaru, ROT0, "Jaleco",    "Vs. Ninja Jajamaru Kun (Japan)", GAME_IMPERFECT_GRAPHICS )
	GAME( 1987, topgun,   0,        vsnes,   topgun,   topgun,   ROT0, "Konami",    "Vs. Top Gun")
	GAME( 1985, bnglngby, 0,        vsnes,   bnglngby, bnglngby, ROT0, "Nintendo / Broderbund Software Inc.",  "Vs. Raid on Bungeling Bay (Japan)" )
	GAME( 1986, supxevs,  0,        vsnes,   supxevs,  supxevs,  ROT0, "Namco",		"Vs. Super Xevious" )
	GAME( 1988, vsfdf,    0,        vsnes,   vsfdf,    vsfdf,	 ROT0, "Konami",	"Vs. Freedom Force" )
	
	/* Dual games */
	GAME( 1984, vstennis, 0,        vsdual,  vstennis, vstennis, ROT0, "Nintendo",  "Vs. Tennis"  )
	GAME( 1984, wrecking, 0,        vsdual,  wrecking, wrecking, ROT0, "Nintendo",  "Vs. Wrecking Crew" )
	GAME( 1984, balonfgt, 0,        vsdual,  balonfgt, balonfgt, ROT0, "Nintendo",  "Vs. Balloon Fight" )
	GAME( 1984, vsmahjng, 0,        vsdual,  vsmahjng, vstennis, ROT0, "Nintendo",  "Vs. Mahjang (Japan)"  )
	GAME( 1984, vsbball,  0,        vsdual,  vsbball,  vsbball,  ROT0, "Nintendo of America",  "Vs. BaseBall" )
	GAME( 1984, vsbballj, vsbball,  vsdual,  vsbballj, vsbball,  ROT0, "Nintendo of America",  "Vs. BaseBall (Japan set 1)" )
	GAME( 1984, vsbbalja, vsbball,  vsdual,  vsbballj, vsbball,  ROT0, "Nintendo of America",  "Vs. BaseBall (Japan set 2)" )
	GAME( 1984, iceclmrj, 0,        vsdual,  iceclmrj, iceclmrj, ROT0, "Nintendo",  "Vs. Ice Climber Dual (Japan)"  )
	
	/* Partially working */
	GAME( 1986, vsgshoe,  0,        vsnes,   vsgshoe,  vsgshoe,  ROT0, "Nintendo",  "Vs. Gumshoe" )
	
	/* Not Working */
	GAMEX(1985, smgolfb,  smgolf,   vsnes,   golf,     machridr, ROT0, "Nintendo",	"Vs. Stroke and Match Golf (Men set 2)", GAME_NOT_WORKING )
	GAMEX(1984, vsbbaljb, vsbball,  vsdual,  vsbballj, vsbball,  ROT0, "Nintendo of America",  "Vs. BaseBall (Japan set 3)", GAME_NOT_WORKING )
}
