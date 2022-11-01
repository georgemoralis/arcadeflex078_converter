/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

  The I8742 MCU takes care of handling the coin inputs and the tilt switch.
  To simulate this, we read the status in the interrupt handler for the main
  CPU and update the counters appropriately. We also must take care of
  handling the coin/credit settings ourselves.

***************************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package machine;

public class tnzs
{
	
	
	static int mcu_type;
	static int tnzs_input_select;
	
	enum
	{
		MCU_NONE_INSECTX,
		MCU_NONE_KAGEKI,
		MCU_NONE_TNZSB,
		MCU_EXTRMATN,
		MCU_ARKANOID,
		MCU_DRTOPPEL,
		MCU_CHUKATAI,
		MCU_TNZS
	};
	
	static int mcu_initializing,mcu_coinage_init,mcu_command,mcu_readcredits;
	static int mcu_reportcoin;
	static int tnzs_workram_backup;
	static unsigned char mcu_coinage[4];
	static unsigned char mcu_coinsA,mcu_coinsB,mcu_credits;
	
	
	
	public static ReadHandlerPtr mcu_tnzs_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		unsigned char data;
	
		if (offset == 0)
		{
			data = cpunum_get_reg(2, I8X41_DATA);
			cpu_yield();
		}
		else
		{
			data = cpunum_get_reg(2, I8X41_STAT);
			cpu_yield();
		}
	
	//	logerror("PC %04x: read %02x from mcu $c00%01x\n", activecpu_get_previouspc(), data, offset);
	
		return data;
	} };
	
	public static WriteHandlerPtr mcu_tnzs_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	//	logerror("PC %04x: write %02x to mcu $c00%01x\n", activecpu_get_previouspc(), data, offset);
	
		if (offset == 0)
			cpunum_set_reg(2, I8X41_DATA, data);
		else
			cpunum_set_reg(2, I8X41_CMND, data);
	} };
	
	
	public static ReadHandlerPtr tnzs_port1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int data = 0;
	
		switch (tnzs_input_select & 0x0f)
		{
			case 0x0a:	data = input_port_4_r(0); break;
			case 0x0c:	data = input_port_2_r(0); break;
			case 0x0d:	data = input_port_3_r(0); break;
			default:	data = 0xff; break;
		}
	
	//	logerror("I8742:%04x  Read %02x from port 1\n", activecpu_get_previouspc(), data);
	
		return data;
	} };
	
	public static ReadHandlerPtr tnzs_port2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int data = input_port_4_r(0);
	
	//	logerror("I8742:%04x  Read %02x from port 2\n", activecpu_get_previouspc(), data);
	
		return data;
	} };
	
	public static WriteHandlerPtr tnzs_port2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		logerror("I8742:%04x  Write %02x to port 2\n", activecpu_get_previouspc(), data);
	
		coin_lockout_w( 0, (data & 0x40) );
		coin_lockout_w( 1, (data & 0x80) );
		coin_counter_w( 0, (~data & 0x10) );
		coin_counter_w( 1, (~data & 0x20) );
	
		tnzs_input_select = data;
	} };
	
	
	
	public static ReadHandlerPtr arknoid2_sh_f000_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int val;
	
		logerror("PC %04x: read input %04x\n", activecpu_get_pc(), 0xf000 + offset);
	
		val = readinputport(7 + offset/2);
		if ((offset & 1) != 0)
		{
			return ((val >> 8) & 0xff);
		}
		else
		{
			return val & 0xff;
		}
	} };
	
	
	static void mcu_reset(void)
	{
		mcu_initializing = 3;
		mcu_coinage_init = 0;
		mcu_coinage[0] = 1;
		mcu_coinage[1] = 1;
		mcu_coinage[2] = 1;
		mcu_coinage[3] = 1;
		mcu_coinsA = 0;
		mcu_coinsB = 0;
		mcu_credits = 0;
		mcu_reportcoin = 0;
		mcu_command = 0;
	}
	
	static void mcu_handle_coins(int coin)
	{
		static int insertcoin;
	
		/* The coin inputs and coin counters are managed by the i8742 mcu. */
		/* Here we simulate it. */
		/* Credits are limited to 9, so more coins should be rejected */
		/* Coin/Play settings must also be taken into consideration */
	
		if ((coin & 0x08) != 0)	/* tilt */
			mcu_reportcoin = coin;
		else if (coin && coin != insertcoin)
		{
			if ((coin & 0x01) != 0)	/* coin A */
			{
				logerror("Coin dropped into slot A\n");
				coin_counter_w(0,1); coin_counter_w(0,0); /* Count slot A */
				mcu_coinsA++;
				if (mcu_coinsA >= mcu_coinage[0])
				{
					mcu_coinsA -= mcu_coinage[0];
					mcu_credits += mcu_coinage[1];
					if (mcu_credits >= 9)
					{
						mcu_credits = 9;
						coin_lockout_global_w(1); /* Lock all coin slots */
					}
					else
					{
						coin_lockout_global_w(0); /* Unlock all coin slots */
					}
				}
			}
			if ((coin & 0x02) != 0)	/* coin B */
			{
				logerror("Coin dropped into slot B\n");
				coin_counter_w(1,1); coin_counter_w(1,0); /* Count slot B */
				mcu_coinsB++;
				if (mcu_coinsB >= mcu_coinage[2])
				{
					mcu_coinsB -= mcu_coinage[2];
					mcu_credits += mcu_coinage[3];
					if (mcu_credits >= 9)
					{
						mcu_credits = 9;
						coin_lockout_global_w(1); /* Lock all coin slots */
					}
					else
					{
						coin_lockout_global_w(0); /* Unlock all coin slots */
					}
				}
			}
			if ((coin & 0x04) != 0)	/* service */
			{
				logerror("Coin dropped into service slot C\n");
				mcu_credits++;
			}
			mcu_reportcoin = coin;
		}
		else
		{
			if (mcu_credits < 9)
				coin_lockout_global_w(0); /* Unlock all coin slots */
			mcu_reportcoin = 0;
		}
		insertcoin = coin;
	}
	
	
	
	public static ReadHandlerPtr mcu_arknoid2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		const char *mcu_startup = "\x55\xaa\x5a";
	
	//	logerror("PC %04x: read mcu %04x\n", activecpu_get_pc(), 0xc000 + offset);
	
		if (offset == 0)
		{
			/* if the mcu has just been reset, return startup code */
			if (mcu_initializing != 0)
			{
				mcu_initializing--;
				return mcu_startup[2 - mcu_initializing];
			}
	
			switch (mcu_command)
			{
				case 0x41:
					return mcu_credits;
	
				case 0xc1:
					/* Read the credit counter or the inputs */
					if (mcu_readcredits == 0)
					{
						mcu_readcredits = 1;
						if ((mcu_reportcoin & 0x08) != 0)
						{
							mcu_initializing = 3;
							return 0xee;	/* tilt */
						}
						else return mcu_credits;
					}
					else return readinputport(2);	/* buttons */
	
				default:
					logerror("error, unknown mcu command\n");
					/* should not happen */
					return 0xff;
					break;
			}
		}
		else
		{
			/*
			status bits:
			0 = mcu is ready to send data (read from c000)
			1 = mcu has read data (from c000)
			2 = unused
			3 = unused
			4-7 = coin code
			      0 = nothing
			      1,2,3 = coin switch pressed
			      e = tilt
			*/
			if ((mcu_reportcoin & 0x08) != 0) return 0xe1;	/* tilt */
			if ((mcu_reportcoin & 0x01) != 0) return 0x11;	/* coin 1 (will trigger "coin inserted" sound) */
			if ((mcu_reportcoin & 0x02) != 0) return 0x21;	/* coin 2 (will trigger "coin inserted" sound) */
			if ((mcu_reportcoin & 0x04) != 0) return 0x31;	/* coin 3 (will trigger "coin inserted" sound) */
			return 0x01;
		}
	} };
	
	public static WriteHandlerPtr mcu_arknoid2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0)
		{
	//		logerror("PC %04x (re %04x): write %02x to mcu %04x\n", activecpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);
			if (mcu_command == 0x41)
			{
				mcu_credits = (mcu_credits + data) & 0xff;
			}
		}
		else
		{
			/*
			0xc1: read number of credits, then buttons
			0x54+0x41: add value to number of credits
			0x15: sub 1 credit (when "Continue Play" only)
			0x84: coin 1 lockout (issued only in test mode)
			0x88: coin 2 lockout (issued only in test mode)
			0x80: release coin lockout (issued only in test mode)
			during initialization, a sequence of 4 bytes sets coin/credit settings
			*/
	//		logerror("PC %04x (re %04x): write %02x to mcu %04x\n", activecpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);
	
			if (mcu_initializing != 0)
			{
				/* set up coin/credit settings */
				mcu_coinage[mcu_coinage_init++] = data;
				if (mcu_coinage_init == 4) mcu_coinage_init = 0;	/* must not happen */
			}
	
			if (data == 0xc1)
				mcu_readcredits = 0;	/* reset input port number */
	
			if (data == 0x15)
			{
				mcu_credits = (mcu_credits - 1) & 0xff;
				if (mcu_credits == 0xff) mcu_credits = 0;
			}
			mcu_command = data;
		}
	} };
	
	
	public static ReadHandlerPtr mcu_extrmatn_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		const char *mcu_startup = "\x5a\xa5\x55";
	
		logerror("PC %04x (re %04x): read mcu %04x\n", activecpu_get_pc(), cpu_geturnpc(), 0xc000 + offset);
	
		if (offset == 0)
		{
			/* if the mcu has just been reset, return startup code */
			if (mcu_initializing != 0)
			{
				mcu_initializing--;
				return mcu_startup[2 - mcu_initializing];
			}
	
			switch (mcu_command)
			{
				case 0x01:
					return readinputport(2) ^ 0xff;	/* player 1 joystick + buttons */
	
				case 0x02:
					return readinputport(3) ^ 0xff;	/* player 2 joystick + buttons */
	
				case 0x1a:
					return (readinputport(5) | (readinputport(6) << 1));
	
				case 0x21:
					return readinputport(4) & 0x0f;
	
				case 0x41:
					return mcu_credits;
	
				case 0xa0:
					/* Read the credit counter */
					if ((mcu_reportcoin & 0x08) != 0)
					{
						mcu_initializing = 3;
						return 0xee;	/* tilt */
					}
					else return mcu_credits;
	
				case 0xa1:
					/* Read the credit counter or the inputs */
					if (mcu_readcredits == 0)
					{
						mcu_readcredits = 1;
						if ((mcu_reportcoin & 0x08) != 0)
						{
							mcu_initializing = 3;
							return 0xee;	/* tilt */
	//						return 0x64;	/* theres a reset input somewhere */
						}
						else return mcu_credits;
					}
					/* buttons */
					else return ((readinputport(2) & 0xf0) | (readinputport(3) >> 4)) ^ 0xff;
	
				default:
					logerror("error, unknown mcu command\n");
					/* should not happen */
					return 0xff;
					break;
			}
		}
		else
		{
			/*
			status bits:
			0 = mcu is ready to send data (read from c000)
			1 = mcu has read data (from c000)
			2 = unused
			3 = unused
			4-7 = coin code
			      0 = nothing
			      1,2,3 = coin switch pressed
			      e = tilt
			*/
			if ((mcu_reportcoin & 0x08) != 0) return 0xe1;	/* tilt */
			if ((mcu_reportcoin & 0x01) != 0) return 0x11;	/* coin 1 (will trigger "coin inserted" sound) */
			if ((mcu_reportcoin & 0x02) != 0) return 0x21;	/* coin 2 (will trigger "coin inserted" sound) */
			if ((mcu_reportcoin & 0x04) != 0) return 0x31;	/* coin 3 (will trigger "coin inserted" sound) */
			return 0x01;
		}
	} };
	
	public static WriteHandlerPtr mcu_extrmatn_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (offset == 0)
		{
			logerror("PC %04x (re %04x): write %02x to mcu %04x\n", activecpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);
			if (mcu_command == 0x41)
			{
				mcu_credits = (mcu_credits + data) & 0xff;
			}
		}
		else
		{
			/*
			0xa0: read number of credits
			0xa1: read number of credits, then buttons
			0x01: read player 1 joystick + buttons
			0x02: read player 2 joystick + buttons
			0x1a: read coin switches
			0x21: read service & tilt switches
			0x4a+0x41: add value to number of credits
			0x84: coin 1 lockout (issued only in test mode)
			0x88: coin 2 lockout (issued only in test mode)
			0x80: release coin lockout (issued only in test mode)
			during initialization, a sequence of 4 bytes sets coin/credit settings
			*/
	
			logerror("PC %04x (re %04x): write %02x to mcu %04x\n", activecpu_get_pc(), cpu_geturnpc(), data, 0xc000 + offset);
	
			if (mcu_initializing != 0)
			{
				/* set up coin/credit settings */
				mcu_coinage[mcu_coinage_init++] = data;
				if (mcu_coinage_init == 4) mcu_coinage_init = 0;	/* must not happen */
			}
	
			if (data == 0xa1)
				mcu_readcredits = 0;	/* reset input port number */
	
			/* Dr Toppel decrements credits differently. So handle it */
			if ((data == 0x09) && (mcu_type == MCU_DRTOPPEL))
				mcu_credits = (mcu_credits - 1) & 0xff;		/* Player 1 start */
			if ((data == 0x18) && (mcu_type == MCU_DRTOPPEL))
				mcu_credits = (mcu_credits - 2) & 0xff;		/* Player 2 start */
	
			mcu_command = data;
		}
	} };
	
	
	
	DRIVER_INIT( extrmatn )
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		mcu_type = MCU_EXTRMATN;
	
		/* there's code which falls through from the fixed ROM to bank #7, I have to */
		/* copy it there otherwise the CPU bank switching support will not catch it. */
		memcpy(&RAM[0x08000],&RAM[0x2c000],0x4000);
	}
	
	DRIVER_INIT( arknoid2 )
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		mcu_type = MCU_ARKANOID;
	
		/* there's code which falls through from the fixed ROM to bank #2, I have to */
		/* copy it there otherwise the CPU bank switching support will not catch it. */
		memcpy(&RAM[0x08000],&RAM[0x18000],0x4000);
	}
	
	DRIVER_INIT( drtoppel )
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		mcu_type = MCU_DRTOPPEL;
	
		/* there's code which falls through from the fixed ROM to bank #0, I have to */
		/* copy it there otherwise the CPU bank switching support will not catch it. */
		memcpy(&RAM[0x08000],&RAM[0x18000],0x4000);
	
		/* drtoppel writes to the palette RAM area even if it has PROMs! We have to patch it out. */
		install_mem_write_handler(0, 0xf800, 0xfbff, MWA_NOP);
	}
	
	DRIVER_INIT( chukatai )
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
		mcu_type = MCU_CHUKATAI;
	
		/* there's code which falls through from the fixed ROM to bank #0, I have to */
		/* copy it there otherwise the CPU bank switching support will not catch it. */
		memcpy(&RAM[0x08000],&RAM[0x18000],0x4000);
	}
	
	DRIVER_INIT( tnzs )
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
		mcu_type = MCU_TNZS;
	
		/* there's code which falls through from the fixed ROM to bank #0, I have to */
		/* copy it there otherwise the CPU bank switching support will not catch it. */
		memcpy(&RAM[0x08000],&RAM[0x18000],0x4000);
	}
	
	DRIVER_INIT( tnzsb )
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
		mcu_type = MCU_NONE_TNZSB;
	
		/* there's code which falls through from the fixed ROM to bank #0, I have to */
		/* copy it there otherwise the CPU bank switching support will not catch it. */
		memcpy(&RAM[0x08000],&RAM[0x18000],0x4000);
	}
	
	DRIVER_INIT( insectx )
	{
		mcu_type = MCU_NONE_INSECTX;
	
		/* this game has no mcu, replace the handler with plain input port handlers */
		install_mem_read_handler(1, 0xc000, 0xc000, input_port_2_r );
		install_mem_read_handler(1, 0xc001, 0xc001, input_port_3_r );
		install_mem_read_handler(1, 0xc002, 0xc002, input_port_4_r );
	}
	
	DRIVER_INIT( kageki )
	{
		mcu_type = MCU_NONE_KAGEKI;
	}
	
	
	public static ReadHandlerPtr tnzs_mcu_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		switch (mcu_type)
		{
			case MCU_TNZS:
			case MCU_CHUKATAI:
				return mcu_tnzs_r(offset);
				break;
			case MCU_ARKANOID:
				return mcu_arknoid2_r(offset);
				break;
			case MCU_EXTRMATN:
			case MCU_DRTOPPEL:
				return mcu_extrmatn_r(offset);
				break;
			default:
				return 0xff;
				break;
		}
	} };
	
	public static WriteHandlerPtr tnzs_mcu_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		switch (mcu_type)
		{
			case MCU_TNZS:
			case MCU_CHUKATAI:
				mcu_tnzs_w(offset,data);
				break;
			case MCU_ARKANOID:
				mcu_arknoid2_w(offset,data);
				break;
			case MCU_EXTRMATN:
			case MCU_DRTOPPEL:
				mcu_extrmatn_w(offset,data);
				break;
			default:
				break;
		}
	} };
	
	public static InterruptHandlerPtr arknoid2_interrupt = new InterruptHandlerPtr() {public void handler()
	{
		int coin;
	
		switch (mcu_type)
		{
			case MCU_ARKANOID:
			case MCU_EXTRMATN:
			case MCU_DRTOPPEL:
				coin  = 0;
				coin |= ((readinputport(5) & 1) << 0);
				coin |= ((readinputport(6) & 1) << 1);
				coin |= ((readinputport(4) & 3) << 2);
				coin ^= 0x0c;
				mcu_handle_coins(coin);
				break;
			default:
				break;
		}
	
		cpu_set_irq_line(0, 0, HOLD_LINE);
	} };
	
	MACHINE_INIT( tnzs )
	{
		/* initialize the mcu simulation */
		switch (mcu_type)
		{
			case MCU_ARKANOID:
			case MCU_EXTRMATN:
			case MCU_DRTOPPEL:
				mcu_reset();
				break;
			default:
				break;
		}
	
		tnzs_workram_backup = -1;
	
		/* preset the banks */
		{
			unsigned char *RAM;
	
			RAM = memory_region(REGION_CPU1);
			cpu_setbank(1,&RAM[0x18000]);
	
			RAM = memory_region(REGION_CPU2);
			cpu_setbank(2,&RAM[0x10000]);
		}
	}
	
	
	public static ReadHandlerPtr tnzs_workram_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* Location $EF10 workaround required to stop TNZS getting */
		/* caught in and endless loop due to shared ram sync probs */
	
		if ((offset == 0xf10) && ((mcu_type == MCU_TNZS) || (mcu_type == MCU_NONE_TNZSB)))
		{
			int tnzs_cpu0_pc;
	
			tnzs_cpu0_pc = activecpu_get_pc();
			switch (tnzs_cpu0_pc)
			{
				case 0xc66:		/* tnzs */
				case 0xc64:		/* tnzsb */
				case 0xab8:		/* tnzs2 */
					tnzs_workram[offset] = (tnzs_workram_backup & 0xff);
					return tnzs_workram_backup;
					break;
				default:
					break;
			}
		}
		return tnzs_workram[offset];
	} };
	
	public static ReadHandlerPtr tnzs_workram_sub_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return tnzs_workram[offset];
	} };
	
	public static WriteHandlerPtr tnzs_workram_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		/* Location $EF10 workaround required to stop TNZS getting */
		/* caught in and endless loop due to shared ram sync probs */
	
		tnzs_workram_backup = -1;
	
		if ((offset == 0xf10) && ((mcu_type == MCU_TNZS) || (mcu_type == MCU_NONE_TNZSB)))
		{
			int tnzs_cpu0_pc;
	
			tnzs_cpu0_pc = activecpu_get_pc();
			switch (tnzs_cpu0_pc)
			{
				case 0xab5:		/* tnzs2 */
					if (activecpu_get_previouspc() == 0xab4)
						break;  /* unfortunantly tnzsb is true here too, so stop it */
				case 0xc63:		/* tnzs */
				case 0xc61:		/* tnzsb */
					tnzs_workram_backup = data;
					break;
				default:
					break;
			}
		}
		if (tnzs_workram_backup == -1)
			tnzs_workram[offset] = data;
	} };
	
	public static WriteHandlerPtr tnzs_workram_sub_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		tnzs_workram[offset] = data;
	} };
	
	public static WriteHandlerPtr tnzs_bankswitch_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		unsigned char *RAM = memory_region(REGION_CPU1);
	
	//	logerror("PC %04x: writing %02x to bankswitch\n", activecpu_get_pc(),data);
	
		/* bit 4 resets the second CPU */
		if ((data & 0x10) != 0)
			cpu_set_reset_line(1,CLEAR_LINE);
		else
			cpu_set_reset_line(1,ASSERT_LINE);
	
		/* bits 0-2 select RAM/ROM bank */
		cpu_setbank (1, &RAM[0x10000 + 0x4000 * (data & 0x07)]);
	} };
	
	public static WriteHandlerPtr tnzs_bankswitch1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		unsigned char *RAM = memory_region(REGION_CPU2);
	
		logerror("PC %04x: writing %02x to bankswitch 1\n", activecpu_get_pc(),data);
	
		switch (mcu_type)
		{
			case MCU_TNZS:
			case MCU_CHUKATAI:
					/* bit 2 resets the mcu */
					if ((data & 0x04) != 0)
					{
						if (Machine.drv.cpu[2].cpu_type == CPU_I8X41)
							cpu_set_reset_line(2,PULSE_LINE);
					}
					/* Coin count and lockout is handled by the i8742 */
					break;
			case MCU_NONE_INSECTX:
					coin_lockout_w( 0, (~data & 0x04) );
					coin_lockout_w( 1, (~data & 0x08) );
					coin_counter_w( 0, (data & 0x10) );
					coin_counter_w( 1, (data & 0x20) );
					break;
			case MCU_NONE_TNZSB:
					coin_lockout_w( 0, (~data & 0x10) );
					coin_lockout_w( 1, (~data & 0x20) );
					coin_counter_w( 0, (data & 0x04) );
					coin_counter_w( 1, (data & 0x08) );
					break;
			case MCU_NONE_KAGEKI:
					coin_lockout_global_w( (~data & 0x20) );
					coin_counter_w( 0, (data & 0x04) );
					coin_counter_w( 1, (data & 0x08) );
					break;
			case MCU_ARKANOID:
			case MCU_EXTRMATN:
			case MCU_DRTOPPEL:
					/* bit 2 resets the mcu */
					if ((data & 0x04) != 0)
						mcu_reset();
					break;
			default:
					break;
		}
	
		/* bits 0-1 select ROM bank */
		cpu_setbank (2, &RAM[0x10000 + 0x2000 * (data & 3)]);
	} };
}
