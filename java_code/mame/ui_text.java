/*********************************************************************

  ui_text.c

  Functions used to retrieve text used by MAME, to aid in
  translation.

*********************************************************************/

/*
 * ported to v0.78
 * using automatic conversion tool v0.01
 */ 
package mame;

public class ui_text
{
	
	#ifdef MESS
	extern const char *mess_default_text[];
	#endif /* MESS */
	
	
	struct lang_struct lang;
	
	/* All entries in this table must match the enum ordering in "ui_text.h" */
	static const char *mame_default_text[] =
	{
	#ifndef MESS
		"MAME",
	#else
		"MESS",
	#endif
	
		/* copyright stuff */
		"Usage of emulators in conjunction with ROMs you don't own is forbidden by copyright law.",
		"IF YOU ARE NOT LEGALLY ENTITLED TO PLAY \"%s\" ON THIS EMULATOR, PRESS ESC.",
		"Otherwise, type OK to continue",
	
		/* misc stuff */
		"Return to Main Menu",
		"Return to Prior Menu",
		"Press Any Key",
		"On",
		"Off",
		"NA",
		"OK",
		"INVALID",
		"(none)",
		"CPU",
		"Address",
		"Value",
		"Sound",
		"sound",
		"stereo",
		"Vector Game",
		"Screen Resolution",
		"Text",
		"Volume",
		"Relative",
		"ALL CHANNELS",
		"Brightness",
		"Gamma",
		"Vector Flicker",
		"Vector Intensity",
		"Overclock",
		"ALL CPUS",
	#ifndef MESS
		"History not available",
	#else
		"System Info not available",
	#endif
	
		/* special characters */
		"\x11",
		"\x10",
		"\x18",
		"\x19",
		"\x1a",
		"\x1b",
	
		/* known problems */
	#ifndef MESS
		"There are known problems with this game:",
	#else
		"There are known problems with this system",
	#endif
		"The colors aren't 100% accurate.",
		"The colors are completely wrong.",
		"The video emulation isn't 100% accurate.",
		"The sound emulation isn't 100% accurate.",
		"The game lacks sound.",
		"Screen flipping in cocktail mode is not supported.",
	#ifndef MESS
		"THIS GAME DOESN'T WORK PROPERLY",
	#else
		"THIS SYSTEM DOESN'T WORK PROPERLY",
	#endif
		"The game has protection which isn't fully emulated.",
		"There are working clones of this game. They are:",
		"Type OK to continue",
	
		/* main menu */
		"Input (general)",
		"Dip Switches",
		"Analog Controls",
		"Calibrate Joysticks",
		"Bookkeeping Info",
	
	#ifndef MESS
		"Input (this game)",
		"Game Information",
		"Game History",
		"Reset Game",
		"Return to Game",
	#else
		"Input (this machine)",
		"Machine Information",
		"Machine Usage & History",
		"Reset Machine",
		"Return to Machine",
	#endif /* MESS */
	
		"Cheat",
		"Memory Card",
	
		/* input */
		"Key/Joy Speed",
		"Reverse",
		"Sensitivity",
	
		/* stats */
		"Tickets dispensed",
		"Coin",
		"(locked)",
	
		/* memory card */
		"Load Memory Card",
		"Eject Memory Card",
		"Create Memory Card",
		"Failed To Load Memory Card!",
		"Load OK!",
		"Memory Card Ejected!",
		"Memory Card Created OK!",
		"Failed To Create Memory Card!",
		"(It already exists ?)",
		"DAMN!! Internal Error!",
	
		/* cheats */
		"Enable/Disable a Cheat",
		"Add/Edit a Cheat",
		"Start a New Cheat Search",
		"Continue Search",
		"View Last Results",
		"Restore Previous Results",
		"Configure Watchpoints",
		"General Help",
		"Options",
		"Reload Database",
		"Watchpoint",
		"Disabled",
		"Cheats",
		"Watchpoints",
		"More Info",
		"More Info for",
		"Name",
		"Description",
		"Activation Key",
		"Code",
		"Max",
		"Set",
		"Cheat conflict found: disabling",
		"Help not available yet",
	
		/* watchpoints */
		"Number of bytes",
		"Display Type",
		"Label Type",
		"Label",
		"X Position",
		"Y Position",
		"Watch",
	
		"Hex",
		"Decimal",
		"Binary",
	
		/* searching */
		"Lives (or another value)",
		"Timers (+/- some value)",
		"Energy (greater or less)",
		"Status (bits or flags)",
		"Slow But Sure (changed or not)",
		"Default Search Speed",
		"Fast",
		"Medium",
		"Slow",
		"Very Slow",
		"All Memory",
		"Select Memory Areas",
		"Matches found",
		"Search not initialized",
		"No previous values saved",
		"Previous values already restored",
		"Restoration successful",
		"Select a value",
		"All values saved",
		"One match found - added to list",
	
		NULL
	};
	
	
	
	static const char **default_text[] =
	{
		mame_default_text,
	#ifdef MESS
		mess_default_text,
	#endif /* MESS */
		NULL
	};
	
	
	
	static const char **trans_text;
	
	
	int uistring_init (mame_file *langfile)
	{
		/*
			TODO: This routine needs to do several things:
				- load an external font if needed
				- determine the number of characters in the font
				- deal with multibyte languages
	
		*/
	
		int i, j, str;
		char curline[255];
		char section[255] = "\0";
		char *ptr;
		int string_count;
	
		/* count the total amount of strings */
		string_count = 0;
		for (i = 0; default_text[i]; i++)
		{
			for (j = 0; default_text[i][j]; j++)
				string_count++;
		}
	
		/* allocate the translated text array, and set defaults */
		trans_text = auto_malloc(sizeof(const char *) * string_count);
		if (!trans_text)
			return 1;
	
		/* copy in references to all of the strings */
		str = 0;
		for (i = 0; default_text[i]; i++)
		{
			for (j = 0; default_text[i][j]; j++)
				trans_text[str++] = default_text[i][j];
		}
	
		memset(&lang, 0, sizeof(lang));
	
		/* if no language file, exit */
		if (!langfile)
			return 0;
	
		while (mame_fgets (curline, sizeof(curline) / sizeof(curline[0]), langfile) != NULL)
		{
			/* Ignore commented and blank lines */
			if (curline[0] == ';') continue;
			if (curline[0] == '\n') continue;
			if (curline[0] == '\r') continue;
	
			if (curline[0] == '[')
			{
				ptr = strtok (&curline[1], "]");
				/* Found a section, indicate as such */
				strcpy (section, ptr);
	
				/* Skip to the next line */
				continue;
			}
	
			/* Parse the LangInfo section */
			if (strcmp (section, "LangInfo") == 0)
			{
				ptr = strtok (curline, "=");
				if (strcmp (ptr, "Version") == 0)
				{
					ptr = strtok (NULL, "\n\r");
					sscanf (ptr, "%d", &lang.version);
				}
				else if (strcmp (ptr, "Language") == 0)
				{
					ptr = strtok (NULL, "\n\r");
					strcpy (lang.langname, ptr);
				}
				else if (strcmp (ptr, "Author") == 0)
				{
					ptr = strtok (NULL, "\n\r");
					strcpy (lang.author, ptr);
				}
				else if (strcmp (ptr, "Font") == 0)
				{
					ptr = strtok (NULL, "\n\r");
					strcpy (lang.fontname, ptr);
				}
			}
	
			/* Parse the Strings section */
			if (strcmp (section, "Strings") == 0)
			{
				/* Get all text up to the first line ending */
				ptr = strtok (curline, "\n\r");
	
				/* Find a matching default string */
				str = 0;
				for (i = 0; default_text[i]; i++)
				{
					for (j = 0; default_text[i][j]; j++)
					{
						if (strcmp (curline, default_text[i][j]) == 0)
					{
						char transline[255];
	
						/* Found a match, read next line as the translation */
						mame_fgets (transline, 255, langfile);
	
						/* Get all text up to the first line ending */
						ptr = strtok (transline, "\n\r");
	
						/* Allocate storage and copy the string */
							trans_text[str] = auto_strdup(transline);
							if (!trans_text[str])
								return 1;
						}
						str++;
					}
				}
			}
		}
	
		/* indicate success */
		return 0;
	}
	
	
	
	const char * ui_getstring (int string_num)
	{
			return trans_text[string_num];
	}
}
