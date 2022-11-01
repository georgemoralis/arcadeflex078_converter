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
    static final int PALETTE_INIT=9;

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
                        sUtil.putString("package " + Convertor.packageName + ";\r\n");
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
                    Convertor.inpos = i;
                    break;
                }
                case 'V': {
                    i = Convertor.inpos;
                    if (sUtil.getToken("VIDEO_START(") || sUtil.getToken("VIDEO_UPDATE(") || sUtil.getToken("VIDEO_STOP(") || sUtil.getToken("VIDEO_EOF(")) {
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.getToken(");"))//if it is a front function skip it
                        {
                            sUtil.skipLine();
                            continue;
                        }
                    }
                    Convertor.inpos = i;
                    break;
                }
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
                    if (type == READ_HANDLER8 || type == WRITE_HANDLER8 || type == INTERRUPT || type==PALETTE_INIT) {
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
                    if (type == READ_HANDLER8 || type == WRITE_HANDLER8 || type == INTERRUPT || type==PALETTE_INIT) {
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
                case 'M': {
                    i = Convertor.inpos;
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
                        }
                        else {
                            sUtil.putString("public static PaletteInitHandlerPtr " + Convertor.token[0] + "  = new PaletteInitHandlerPtr() { public void handler(char[] colortable, UBytePtr color_prom)");
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
                case 'I':
                    int j = Convertor.inpos;
                    if (sUtil.getToken("INPUT_PORTS_START")) {
                        if (sUtil.parseChar() != '(') {
                            Convertor.inpos = j;
                            break;
                        }
                        sUtil.skipSpace();
                        Convertor.token[0] = sUtil.parseToken();
                        sUtil.skipSpace();
                        if (sUtil.parseChar() != ')') {
                            Convertor.inpos = j;
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
                case '&': {
                    if (type == MEMORY_READ8 || type == MEMORY_WRITE8 || type == PORT_READ8 || type == PORT_WRITE8) {
                        Convertor.inpos += 1;
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
            }

            Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];//grapse to inputbuffer sto output
        } while (true);
        if (only_once_flag) {
            sUtil.putString("}\r\n");
        }
    }

}
