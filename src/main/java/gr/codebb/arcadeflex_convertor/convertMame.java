package gr.codebb.arcadeflex_convertor;

public class convertMame {

    public static void ConvertMame() {
        Analyse();
        Convert();
    }

    public static void Analyse() {

    }

    static final int MEMORY_READ8 = 1;
    static final int MEMORY_WRITE8 = 2;
    static final int PORT_READ8 = 3;
    static final int PORT_WRITE8 = 4;
    static final int READ_HANDLER8 = 5;
    static final int WRITE_HANDLER8 = 6;
    static final int INPUTPORTS = 7;
    static final int INTERRUPT = 8;
    static final int PALETTE_INIT = 9;
    static final int VIDEO_UPDATE = 10;
    static final int VIDEO_START = 11;

    public static void Convert() {
        Convertor.inpos = 0;//position of pointer inside the buffers
        Convertor.outpos = 0;
        boolean only_once_flag = false;//gia na baleis to header mono mia fora
        boolean line_change_flag = false;

        int kapa = 0;
        int i = 0;
        int type = 0;
        int i3 = -1;
        int i8 = -1;
        int type2 = 0;
        int[] insideagk = new int[10];//get the { that are inside functions

        do {
            if (Convertor.inpos >= Convertor.inbuf.length)//an to megethos einai megalitero spase to loop
            {
                break;
            }
            char c = sUtil.getChar(); //pare ton character
            if (line_change_flag) {
                for (int i1 = 0; i1 < kapa; i1++) {
                    sUtil.putString("\t");
                }

                line_change_flag = false;
            }
            switch (c) {
                case 35: // '#'
                {
                    if (!sUtil.getToken("#include"))//an den einai #include min to trexeis
                    {
                        break;
                    }
                    sUtil.skipLine();
                    if (!only_once_flag)//trekse auto to komati mono otan bris to proto include
                    {
                        only_once_flag = true;
                        sUtil.putString("/*\r\n");
                        sUtil.putString(" * ported to v" + Convertor.mameversion + "\r\n");
                        sUtil.putString(" * using automatic conversion tool v" + Convertor.convertorversion + "\r\n");
                        /*sUtil.putString(" * converted at : " + Convertor.timenow() + "\r\n");*/
                        sUtil.putString(" */ \r\n");
                        sUtil.putString("package arcadeflex.v" + Convertor.mameversion.replace(".", "") + "." + Convertor.packageName + ";\r\n");
                        sUtil.putString("\r\n");
                        sUtil.putString((new StringBuilder()).append("public class ").append(Convertor.className).append("\r\n").toString());
                        sUtil.putString("{\r\n");
                        kapa = 1;
                        line_change_flag = true;
                    }
                    continue;
                }
                case 10: // '\n'
                {
                    Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];
                    line_change_flag = true;
                    continue;
                }
                case 's': {
                    i = Convertor.inpos;
                    if (type == WRITE_HANDLER8 || type == VIDEO_UPDATE || type == VIDEO_START) {
                        if (sUtil.getToken("spriteram_size")) {
                            sUtil.putString((new StringBuilder()).append("spriteram_size[0]").toString());
                            continue;
                        }
                    }
                    if (sUtil.getToken("spriteram")) {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        } else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g = Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos = g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("spriteram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos += 1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("spriteram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if (sUtil.getToken("spriteram_2")) {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        } else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g = Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos = g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("spriteram_2.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos += 1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("spriteram_2.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if (sUtil.getToken("spriteram_3")) {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        } else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g = Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos = g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("spriteram_3.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos += 1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("spriteram_3.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if (type == WRITE_HANDLER8) {
                        if (sUtil.getToken("soundlatch_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch_w.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("soundlatch2_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch2_w.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("soundlatch3_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch3_w.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("soundlatch4_w")) {
                            sUtil.putString((new StringBuilder()).append("soundlatch4_w.handler").toString());
                            continue;
                        }
                    }
                    boolean isstatic = false;
                    if (sUtil.getToken("static")) {
                        sUtil.skipSpace();
                        isstatic = true;
                    }
                    if (sUtil.getToken("MEMORY_READ_START(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(")")) {
                            sUtil.putString("public static Memory_ReadAddress " + Convertor.token[0] + "[]={\n\t\tnew Memory_ReadAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),");
                            type = MEMORY_READ8;
                            i3 = 1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (sUtil.getToken("MEMORY_WRITE_START(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(")")) {
                            sUtil.putString("public static Memory_WriteAddress " + Convertor.token[0] + "[]={\n\t\tnew Memory_WriteAddress(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_MEM | MEMPORT_WIDTH_8),");
                            type = MEMORY_WRITE8;
                            i3 = 1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (sUtil.getToken("PORT_READ_START(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(")")) {
                            sUtil.putString("public static IO_ReadPort " + Convertor.token[0] + "[]={\n\t\tnew IO_ReadPort(MEMPORT_MARKER, MEMPORT_DIRECTION_READ | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),");
                            type = PORT_READ8;
                            i3 = 1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (sUtil.getToken("PORT_WRITE_START(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(")")) {
                            sUtil.putString("public static IO_WritePort " + Convertor.token[0] + "[]={\n\t\tnew IO_WritePort(MEMPORT_MARKER, MEMPORT_DIRECTION_WRITE | MEMPORT_TYPE_IO | MEMPORT_WIDTH_8),");
                            type = PORT_WRITE8;
                            i3 = 1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (sUtil.getToken("READ_HANDLER(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static ReadHandlerPtr " + Convertor.token[0] + "  = new ReadHandlerPtr() { public int handler(int offset)");
                            type = READ_HANDLER8;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (sUtil.getToken("WRITE_HANDLER(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static WriteHandlerPtr " + Convertor.token[0] + " = new WriteHandlerPtr() {public void handler(int offset, int data)");
                            type = WRITE_HANDLER8;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    } else if (sUtil.getToken("INTERRUPT_GEN(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static InterruptHandlerPtr " + Convertor.token[0] + " = new InterruptHandlerPtr() {public void handler()");
                            type = INTERRUPT;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    Convertor.inpos = i;
                    break;
                }
                case 'e': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("extern")) {
                        sUtil.skipLine();
                        continue;
                    }
                    if (sUtil.getToken("enum")) {
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != '{') {
                            Convertor.inpos = i;
                        } else {
                            sUtil.skipSpace();
                            int i5 = 0;
                            do {
                                Convertor.token[(i5++)] = sUtil.parseToken();
                                sUtil.skipSpace();
                                c = sUtil.parseChar();
                                if ((c != '}') && (c != ',')) {
                                    Convertor.inpos = i;
                                    break;
                                }
                                sUtil.skipSpace();
                            } while (c == ',');
                            if (sUtil.parseChar() != ';') {
                                Convertor.inpos = i;
                            } else {
                                sUtil.putString("static final int ");
                                for (int i6 = 0; i6 < i5; i6++) {
                                    sUtil.putString(Convertor.token[i6] + " = " + i6);
                                    sUtil.putString(i6 == i5 - 1 ? ";" : ", ");
                                }
                                continue;
                            }
                        }
                    }
                    Convertor.inpos = i;
                    break;
                }
                case 'W': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("WRITE_HANDLER(") || sUtil.getToken("WRITE_HANDLER (")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static WriteHandlerPtr " + Convertor.token[0] + " = new WriteHandlerPtr() {public void handler(int offset, int data)");
                            type = WRITE_HANDLER8;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    Convertor.inpos = i;
                    break;
                }
                case 'R': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("READ_HANDLER(") || sUtil.getToken("READ_HANDLER (")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static ReadHandlerPtr " + Convertor.token[0] + "  = new ReadHandlerPtr() { public int handler(int offset)");
                            type = READ_HANDLER8;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (sUtil.getToken("ROM_START")) {
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.putString((new StringBuilder()).append("static RomLoadPtr rom_").append(Convertor.token[0]).append(" = new RomLoadPtr(){ public void handler(){ ").toString());
                        continue;
                    }
                    if (sUtil.getToken("ROM_END")) {
                        sUtil.putString((new StringBuilder()).append("ROM_END(); }}; ").toString());
                        continue;
                    }
                    Convertor.inpos = i;
                    break;
                }
                case 'V': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("VIDEO_UPDATE(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static VideoUpdateHandlerPtr video_update_" + Convertor.token[0] + "  = new VideoUpdateHandlerPtr() { public void handler(mame_bitmap bitmap, rectangle cliprect)");
                            type = VIDEO_UPDATE;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (sUtil.getToken("VIDEO_START(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static VideoStartHandlerPtr video_start_" + Convertor.token[0] + "  = new VideoStartHandlerPtr() { public int handler()");
                            type = VIDEO_START;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    Convertor.inpos = i;
                    break;
                }
                case 'v':
                    int j = Convertor.inpos;
                    if (sUtil.getToken("video_start_generic")) {
                        sUtil.putString((new StringBuilder()).append("video_start_generic.handler").toString());
                        continue;
                    }
                    if (type == VIDEO_UPDATE || type == VIDEO_START || type == WRITE_HANDLER8) {
                        if (sUtil.getToken("videoram_size")) {
                            sUtil.putString((new StringBuilder()).append("videoram_size[0]").toString());
                            continue;
                        }
                    }
                    if (sUtil.getToken("videoram")) {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = j;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = j;
                            break;
                        } else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g = Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = j;
                                    break;
                                }
                                Convertor.inpos = g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("videoram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos += 1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("videoram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    Convertor.inpos = j;
                    break;
                case '{': {
                    if (type == MEMORY_READ8) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 2) {
                            sUtil.putString("new Memory_ReadAddress(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (type == MEMORY_WRITE8) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 2) {
                            sUtil.putString("new Memory_WriteAddress(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (type == PORT_READ8) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 2) {
                            sUtil.putString("new IO_ReadPort(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (type == PORT_WRITE8) {
                        i3++;
                        insideagk[i3] = 0;
                        if (i3 == 2) {
                            sUtil.putString("new IO_WritePort(");
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (type == READ_HANDLER8 || type == WRITE_HANDLER8 || type == INTERRUPT || type == PALETTE_INIT || type == VIDEO_UPDATE || type == VIDEO_START) {
                        i3++;
                    }
                }
                break;
                case '}': {
                    if ((type == MEMORY_READ8) || type == MEMORY_WRITE8 || type == PORT_READ8 || type == PORT_WRITE8) {
                        i3--;
                        if (i3 == 0) {
                            type = -1;
                        } else if (i3 == 1) {
                            Convertor.outbuf[(Convertor.outpos++)] = ')';
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (type == READ_HANDLER8 || type == WRITE_HANDLER8 || type == INTERRUPT || type == PALETTE_INIT || type == VIDEO_UPDATE || type == VIDEO_START) {
                        i3--;
                        if (i3 == -1) {
                            sUtil.putString("} };");
                            Convertor.inpos += 1;
                            type = -1;
                            continue;
                        }
                    }
                    break;
                }
                case 'm':
                    i = Convertor.inpos;
                    if (type == VIDEO_UPDATE || type == VIDEO_START || type == WRITE_HANDLER8) {
                        Convertor.token[0] = sUtil.parseToken();
                        if (Convertor.token[0].endsWith("_videoram_w")) {
                            sUtil.putString((new StringBuilder()).append(Convertor.token[0]).append(".handler").toString());
                            continue;
                        }
                    }
                    Convertor.inpos = i;
                    break;
                case 'M': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("videoram_size")) {
                        sUtil.putString((new StringBuilder()).append("videoram_size[0]").toString());
                        continue;
                    }
                    if (!sUtil.getToken("MEMORY_END")) {
                        Convertor.inpos = i;
                        break;
                    }
                    if (type == MEMORY_READ8) {
                        sUtil.putString("\tnew Memory_ReadAddress(MEMPORT_MARKER, 0)\n\t};");
                        type = -1;
                        Convertor.inpos += 1;
                        continue;
                    } else if (type == MEMORY_WRITE8) {
                        sUtil.putString("\tnew Memory_WriteAddress(MEMPORT_MARKER, 0)\n\t};");
                        type = -1;
                        Convertor.inpos += 1;
                        continue;
                    }
                    Convertor.inpos = i;
                    break;
                }
                case 'P': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("PORT_END")) {
                        if (type == PORT_READ8) {
                            sUtil.putString("\tnew IO_ReadPort(MEMPORT_MARKER, 0)\n\t};");
                            type = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                        if (type == PORT_WRITE8) {
                            sUtil.putString("\tnew IO_WritePort(MEMPORT_MARKER, 0)\n\t};");
                            type = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (sUtil.getToken("PALETTE_INIT(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static PaletteInitHandlerPtr palette_init_" + Convertor.token[0] + "  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom)");
                            type = PALETTE_INIT;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    if (sUtil.getToken("PORT_START")) {
                        sUtil.putString((new StringBuilder()).append("PORT_START(); ").toString());
                        continue;
                    }
                    if (sUtil.getToken("PORT_DIPNAME") || sUtil.getToken("PORT_BIT") || sUtil.getToken("PORT_DIPSETTING") || sUtil.getToken("PORT_BITX") || sUtil.getToken("PORT_SERVICE") || sUtil.getToken("PORT_BIT_IMPULSE") || sUtil.getToken("PORT_ANALOG") || sUtil.getToken("PORT_ANALOGX")) {
                        i8++;
                        type2 = INPUTPORTS;
                        sUtil.skipSpace();
                        if (sUtil.parseChar() == '(') {
                            Convertor.inpos = i;
                        }
                    }
                    Convertor.inpos = i;
                    break;
                }
                case 'p':
                    i = Convertor.inpos;
                    if (type == VIDEO_UPDATE || type == VIDEO_START || type == WRITE_HANDLER8) {
                        Convertor.token[0] = sUtil.parseToken();
                        if (Convertor.token[0].startsWith("plot_pixel")) {
                            sUtil.putString((new StringBuilder()).append(Convertor.token[0]).append(".handler").toString());
                            continue;
                        }
                    }
                    if (sUtil.getToken("paletteram")) {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        } else {
                            sUtil.skipSpace();

                            if (sUtil.parseChar() == '=') {
                                int g = Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos = g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("paletteram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos += 1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("paletteram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if (sUtil.getToken("paletteram_2")) {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        } else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g = Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos = g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("paletteram_2.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos += 1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("paletteram_2.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    Convertor.inpos = i;
                    break;
                case 'I':
                    i = Convertor.inpos;
                    if (sUtil.getToken("INPUT_PORTS_START")) {
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.putString((new StringBuilder()).append("static InputPortPtr input_ports_").append(Convertor.token[0]).append(" = new InputPortPtr(){ public void handler() { ").toString());
                    }
                    if (sUtil.getToken("INPUT_PORTS_END")) {
                        sUtil.putString((new StringBuilder()).append("INPUT_PORTS_END(); }}; ").toString());
                        continue;
                    }
                    if (sUtil.getToken("INTERRUPT_GEN(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        } else {
                            sUtil.putString("public static InterruptHandlerPtr " + Convertor.token[0] + " = new InterruptHandlerPtr() {public void handler()");
                            type = INTERRUPT;
                            i3 = -1;
                            Convertor.inpos += 1;
                            continue;
                        }
                    }
                    break;
                case 'i':
                    i = Convertor.inpos;
                    if (type == READ_HANDLER8) {
                        if (sUtil.getToken("input_port_0_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_0_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_1_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_1_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_2_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_2_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_3_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_3_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_4_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_4_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_5_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_5_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_6_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_6_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_7_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_7_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_8_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_8_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_9_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_9_r.handler").toString());
                            continue;
                        }
                        if (sUtil.getToken("input_port_10_r")) {
                            sUtil.putString((new StringBuilder()).append("input_port_10_r.handler").toString());
                            continue;
                        }
                    }
                    if (sUtil.getToken("if")) {
                        boolean is_zero = false;
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = i;
                            break;
                        }
                        sUtil.skipSpace();
                        if (sUtil.getChar() == '!') {
                            is_zero = true;
                            Convertor.inpos++;
                        }
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getChar() == '&') {
                            Convertor.inpos++;
                            sUtil.skipSpace();
                            Convertor.token[1] = sUtil.parseToken();
                            sUtil.skipSpace();
                            Convertor.token[0] = (new StringBuilder()).append("(").append(Convertor.token[0]).append(" & ").append(Convertor.token[1]).append(")").toString();
                        }
                        if (sUtil.getChar() == '(') {
                            Convertor.inpos++;
                            if (sUtil.getChar() == ')') {
                                if (sUtil.getChar() == ')') {
                                    Convertor.inpos++;
                                    Convertor.token[0] = (new StringBuilder()).append(Convertor.token[0]).append("(").append(")").toString();
                                }
                            }
                        }
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = i;
                            break;
                        }
                        if (!is_zero) {
                            sUtil.putString((new StringBuilder()).append("if (").append(Convertor.token[0]).append(" != 0)").toString());
                        } else {
                            sUtil.putString((new StringBuilder()).append("if (").append(Convertor.token[0]).append(" == 0)").toString());
                        }
                        continue;
                    }
                    break;
                case '&': {
                    if (type == MEMORY_READ8 || type == MEMORY_WRITE8 || type == PORT_READ8 || type == PORT_WRITE8) {
                        Convertor.inpos += 1;
                        continue;
                    }
                    if (sUtil.getToken("&Machine")) {
                        sUtil.putString((new StringBuilder()).append("Machine").toString());
                        continue;
                    }
                    break;
                }
                case ')': {
                    if (type2 == INPUTPORTS) {
                        i8--;
                        i = Convertor.inpos;
                        Convertor.inpos += 1;
                        if (sUtil.parseChar() == '\"') {
                            Convertor.outbuf[(Convertor.outpos++)] = '\"';
                            Convertor.outbuf[(Convertor.outpos++)] = ')';
                            Convertor.outbuf[(Convertor.outpos++)] = ')';
                            Convertor.outbuf[(Convertor.outpos++)] = ';';
                            Convertor.inpos += 3;
                        } else {
                            Convertor.inpos = i;
                            Convertor.outbuf[(Convertor.outpos++)] = ')';
                            Convertor.outbuf[(Convertor.outpos++)] = ';';
                            Convertor.inpos += 2;
                        }
                        if (sUtil.getChar() == ')') {
                            Convertor.inpos += 1;
                        }
                        type2 = -1;
                        continue;
                    }
                }
                break;
                case 'D':
                    if (type2 == INPUTPORTS) {
                        if (sUtil.getToken("DEF_STR(")) {
                            sUtil.skipSpace();
                            Convertor.token[0] = sUtil.parseToken();
                            sUtil.putString((new StringBuilder()).append("DEF_STR( \"").append(Convertor.token[0]).append("\")").toString());
                            i3 = -1;

                            continue;
                        }

                    }
                    break;

                case 'c':
                    i = Convertor.inpos;
                    if (sUtil.getToken("colorram")) {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        } else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g = Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos = g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("colorram.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos += 1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("colorram.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    if (sUtil.getToken("color_prom")) {
                        if (sUtil.parseChar() != '[') {
                            Convertor.inpos = i;
                            break;
                        }
                        Convertor.token[0] = sUtil.parseToken(']');
                        Convertor.token[0] = Convertor.token[0].replace("->", ".");
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ']') {
                            Convertor.inpos = i;
                            break;
                        } else {
                            sUtil.skipSpace();
                            if (sUtil.parseChar() == '=') {
                                int g = Convertor.inpos;
                                if (sUtil.parseChar() == '=') {
                                    Convertor.inpos = i;
                                    break;
                                }
                                Convertor.inpos = g;
                                sUtil.skipSpace();
                                Convertor.token[1] = sUtil.parseToken(';');
                                sUtil.putString((new StringBuilder()).append("color_prom.write(").append(Convertor.token[0]).append(",").append(Convertor.token[1]).append(");").toString());
                                Convertor.inpos += 1;
                                break;
                            }
                            sUtil.putString((new StringBuilder()).append("color_prom.read(").append(Convertor.token[0]).append(")").toString());
                            Convertor.inpos -= 1;
                            continue;
                        }
                    }
                    Convertor.inpos = i;
                    break;
                case '-':
                    char c3 = sUtil.getNextChar();
                    if (c3 != '>') {
                        break;
                    }
                    Convertor.outbuf[Convertor.outpos++] = '.';
                    Convertor.inpos += 2;
                    break;
            }

            Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];//grapse to inputbuffer sto output
        } while (true);
        if (only_once_flag) {
            sUtil.putString("}\r\n");
        }
    }

}
