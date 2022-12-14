#define SYS16_SPR_FLIPX						0x01
#define SYS16_SPR_VISIBLE					0x04
#define SYS16_SPR_DRAW_TO_LEFT				0x08
#define SYS16_SPR_SPECIAL					0x10
#define SYS16_SPR_SHADOW					0x20 /* all pixels */
#define SYS16_SPR_PARTIAL_SHADOW			0x40 /* pen #10 */
#define SYS16_SPR_DRAW_TO_TOP				0x80

struct sys16_sprite_attributes {
	int priority, flags;
	int gfx, color;
	UINT8 pitch;
	int zoomx, zoomy;
	int x,y, screen_height;	/* in screen coordinates */
	int shadow_pen;
};

	struct sys16_sprite_attributes *sprite,
	const UINT16 *source,
	int bJustGetColor );


#define TRANSPARENT_SHADOWS

#ifdef TRANSPARENT_SHADOWS
#define NumOfShadowColors 32
#define ShadowColorsMultiplier 2
#else
#define NumOfShadowColors 0
#define ShadowColorsMultiplier 1
#endif



/* machine hardware */



#define SYS16_MWA16_PALETTERAM	sys16_paletteram_w, &paletteram16
#define SYS16_MRA16_PALETTERAM	paletteram16_word_r

#define SYS16_MRA16_WORKINGRAM	MRA16_RAM
#define SYS16_MWA16_WORKINGRAM	MWA16_RAM,&sys16_workingram

#define SYS16_MRA16_WORKINGRAM2	MRA16_RAM
#define SYS16_MWA16_WORKINGRAM2	MWA16_RAM,&sys16_workingram2



#define SYS16_MRA16_SPRITERAM		MRA16_RAM
#define SYS16_MWA16_SPRITERAM		MWA16_RAM,&sys16_spriteram

#define SYS16_MRA16_TILERAM		sys16_tileram_r
#define SYS16_MWA16_TILERAM		sys16_tileram_w,&sys16_tileram

#define SYS16_MRA16_TEXTRAM		sys16_textram_r
#define SYS16_MWA16_TEXTRAM		sys16_textram_w,&sys16_textram

#define SYS16_MRA16_EXTRAM		MRA16_RAM
#define SYS16_MWA16_EXTRAM		MWA16_RAM,&sys16_extraram

#define SYS16_MRA16_EXTRAM2		MRA16_RAM
#define SYS16_MWA16_EXTRAM2		MWA16_RAM,&sys16_extraram2

#define SYS16_MRA16_EXTRAM3		MRA16_RAM
#define SYS16_MWA16_EXTRAM3		MWA16_RAM,&sys16_extraram3

#define SYS16_MRA16_EXTRAM4		MRA16_RAM
#define SYS16_MWA16_EXTRAM4		MWA16_RAM,&sys16_extraram4

#define SYS16_MRA16_ROADRAM		MRA16_RAM
#define SYS16_MWA16_ROADRAM		MWA16_RAM,&sys16_roadram




// encryption decoding
void endurob2_decode_data(data16_t *dest,data16_t *source,int size);
void endurob2_decode_data2(data16_t *dest,data16_t *source,int size);
void enduror_decode_data(data16_t *dest,data16_t *source,int size);
void enduror_decode_data2(data16_t *dest,data16_t *source,int size);

void aurail_decode_data(data16_t *dest,data16_t *source,int size);
void aurail_decode_opcode1(data16_t *dest,data16_t *source,int size);
void aurail_decode_opcode2(data16_t *dest,data16_t *source,int size);

#define SYS16_JOY1 PORT_START();  \
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 );\
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 );\
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 );\
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );\
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );\
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );\
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );

#define SYS16_JOY2 PORT_START();  \
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );\
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );\
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );\
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );\
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );\
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );\
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );

#define SYS16_JOY3 PORT_START();  \
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_PLAYER3 );\
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_PLAYER3 );\
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_PLAYER3 );\
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_PLAYER3 );\
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_PLAYER3 );\
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_PLAYER3 );\
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_PLAYER3 );

#define SYS16_JOY1_SWAPPEDBUTTONS PORT_START();  \
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 );\
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 );\
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 );\
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY );\
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY );\
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY );\
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY );

#define SYS16_JOY2_SWAPPEDBUTTONS PORT_START();  \
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_BUTTON3 | IPF_COCKTAIL );\
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_BUTTON2 | IPF_COCKTAIL );\
	PORT_BIT( 0x04, IP_ACTIVE_LOW, IPT_BUTTON1 | IPF_COCKTAIL );\
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_JOYSTICK_DOWN  | IPF_8WAY | IPF_COCKTAIL );\
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_JOYSTICK_UP    | IPF_8WAY | IPF_COCKTAIL );\
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_JOYSTICK_RIGHT | IPF_8WAY | IPF_COCKTAIL );\
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_JOYSTICK_LEFT  | IPF_8WAY | IPF_COCKTAIL );

#define SYS16_SERVICE PORT_START();  \
	PORT_BIT( 0x01, IP_ACTIVE_LOW, IPT_COIN1 );\
	PORT_BIT( 0x02, IP_ACTIVE_LOW, IPT_COIN2 );\
	PORT_BITX(0x04, IP_ACTIVE_LOW, IPT_SERVICE, DEF_STR( "Service_Mode") ); KEYCODE_F2, IP_JOY_NONE ) \
	PORT_BIT( 0x08, IP_ACTIVE_LOW, IPT_SERVICE1 );\
	PORT_BIT( 0x10, IP_ACTIVE_LOW, IPT_START1 );\
	PORT_BIT( 0x20, IP_ACTIVE_LOW, IPT_START2 );\
	PORT_BIT( 0x40, IP_ACTIVE_LOW, IPT_UNKNOWN );\
	PORT_BIT( 0x80, IP_ACTIVE_LOW, IPT_UNKNOWN );

#define SYS16_COINAGE PORT_START();  \
	PORT_DIPNAME( 0x0f, 0x0f, DEF_STR( "Coin_A") ); \
	PORT_DIPSETTING(    0x07, DEF_STR( "4C_1C") ); \
	PORT_DIPSETTING(    0x08, DEF_STR( "3C_1C") ); \
	PORT_DIPSETTING(    0x09, DEF_STR( "2C_1C") ); \
	PORT_DIPSETTING(    0x05, "2 Coins/1 Credit 5/3 6/4" );\
	PORT_DIPSETTING(    0x04, "2 Coins/1 Credit 4/3" );\
	PORT_DIPSETTING(    0x0f, DEF_STR( "1C_1C") ); \
	PORT_DIPSETTING(    0x01, "1 Coin/1 Credit 2/3" );\
	PORT_DIPSETTING(    0x02, "1 Coin/1 Credit 4/5" );\
	PORT_DIPSETTING(    0x03, "1 Coin/1 Credit 5/6" );\
	PORT_DIPSETTING(    0x06, DEF_STR( "2C_3C") ); \
	PORT_DIPSETTING(    0x0e, DEF_STR( "1C_2C") ); \
	PORT_DIPSETTING(    0x0d, DEF_STR( "1C_3C") ); \
	PORT_DIPSETTING(    0x0c, DEF_STR( "1C_4C") ); \
	PORT_DIPSETTING(    0x0b, DEF_STR( "1C_5C") ); \
	PORT_DIPSETTING(    0x0a, DEF_STR( "1C_6C") ); \
	PORT_DIPSETTING(    0x00, "Free Play (if Coin B too);or 1/1" ) \
	PORT_DIPNAME( 0xf0, 0xf0, DEF_STR( "Coin_B") ); \
	PORT_DIPSETTING(    0x70, DEF_STR( "4C_1C") ); \
	PORT_DIPSETTING(    0x80, DEF_STR( "3C_1C") ); \
	PORT_DIPSETTING(    0x90, DEF_STR( "2C_1C") ); \
	PORT_DIPSETTING(    0x50, "2 Coins/1 Credit 5/3 6/4" );\
	PORT_DIPSETTING(    0x40, "2 Coins/1 Credit 4/3" );\
	PORT_DIPSETTING(    0xf0, DEF_STR( "1C_1C") ); \
	PORT_DIPSETTING(    0x10, "1 Coin/1 Credit 2/3" );\
	PORT_DIPSETTING(    0x20, "1 Coin/1 Credit 4/5" );\
	PORT_DIPSETTING(    0x30, "1 Coin/1 Credit 5/6" );\
	PORT_DIPSETTING(    0x60, DEF_STR( "2C_3C") ); \
	PORT_DIPSETTING(    0xe0, DEF_STR( "1C_2C") ); \
	PORT_DIPSETTING(    0xd0, DEF_STR( "1C_3C") ); \
	PORT_DIPSETTING(    0xc0, DEF_STR( "1C_4C") ); \
	PORT_DIPSETTING(    0xb0, DEF_STR( "1C_5C") ); \
	PORT_DIPSETTING(    0xa0, DEF_STR( "1C_6C") ); \
	PORT_DIPSETTING(    0x00, "Free Play (if Coin A too);or 1/1" )


/* video hardware */

/* "normal" video hardware */

/* hang-on video hardware */

/* outrun video hardware */

/* aburner video hardware */

/* system18 video hardware */

/* video driver constants (vary with game) */

/* video driver registers */



#ifdef SPACEHARRIER_OFFSETS
#endif



/* sound */





