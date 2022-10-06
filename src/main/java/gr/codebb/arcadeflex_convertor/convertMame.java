package gr.codebb.arcadeflex_convertor;

import static gr.codebb.arcadeflex_convertor.Convertor.inpos;
import static gr.codebb.arcadeflex_convertor.Convertor.token;
import static gr.codebb.arcadeflex_convertor.sUtil.getToken;
import static gr.codebb.arcadeflex_convertor.sUtil.parseChar;
import static gr.codebb.arcadeflex_convertor.sUtil.parseToken;
import static gr.codebb.arcadeflex_convertor.sUtil.skipLine;
import static gr.codebb.arcadeflex_convertor.sUtil.skipSpace;

public class convertMame {

    public static void ConvertMame() {
        Analyse();
        Convert();
    }

    public static void Analyse() {

    }

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

            Convertor.outbuf[Convertor.outpos++] = Convertor.inbuf[Convertor.inpos++];//grapse to inputbuffer sto output
        } while (true);
        if (only_once_flag) {
            sUtil.putString("}\r\n");
        }
    }

}
