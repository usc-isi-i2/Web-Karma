// $ANTLR 3.4 RuleInterpreter.g 2012-06-03 21:17:08
package edu.isi.karma.cleaning.changed_grammar;
import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class RuleInterpreterLexer extends Lexer {
    public static final int EOF=-1;
    public static final int ANYNUM=4;
    public static final int ANYTOK=5;
    public static final int ANYTOKS=6;
    public static final int ANYTYP=7;
    public static final int BNKTYP=8;
    public static final int DEL=9;
    public static final int DIGIT=10;
    public static final int ENDTYP=11;
    public static final int FRMB=12;
    public static final int FRME=13;
    public static final int FST=14;
    public static final int INCLD=15;
    public static final int INS=16;
    public static final int LST=17;
    public static final int MOV=18;
    public static final int NEWLINE=19;
    public static final int NUM=20;
    public static final int NUMTYP=21;
    public static final int SRTTYP=22;
    public static final int SYBTYP=23;
    public static final int TOKEN=24;
    public static final int WRDTYP=25;
    public static final int WS=26;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public RuleInterpreterLexer() {} 
    public RuleInterpreterLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public RuleInterpreterLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "RuleInterpreter.g"; }

    // $ANTLR start "ANYNUM"
    public final void mANYNUM() throws RecognitionException {
        try {
            int _type = ANYNUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:2:8: ( 'anynumber' )
            // RuleInterpreter.g:2:10: 'anynumber'
            {
            match("anynumber"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ANYNUM"

    // $ANTLR start "ANYTOK"
    public final void mANYTOK() throws RecognitionException {
        try {
            int _type = ANYTOK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:3:8: ( 'ANYTOK' )
            // RuleInterpreter.g:3:10: 'ANYTOK'
            {
            match("ANYTOK"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ANYTOK"

    // $ANTLR start "ANYTOKS"
    public final void mANYTOKS() throws RecognitionException {
        try {
            int _type = ANYTOKS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:4:9: ( 'anytoks' )
            // RuleInterpreter.g:4:11: 'anytoks'
            {
            match("anytoks"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ANYTOKS"

    // $ANTLR start "ANYTYP"
    public final void mANYTYP() throws RecognitionException {
        try {
            int _type = ANYTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:5:8: ( 'ANYTYP' )
            // RuleInterpreter.g:5:10: 'ANYTYP'
            {
            match("ANYTYP"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ANYTYP"

    // $ANTLR start "BNKTYP"
    public final void mBNKTYP() throws RecognitionException {
        try {
            int _type = BNKTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:6:8: ( 'Blank' )
            // RuleInterpreter.g:6:10: 'Blank'
            {
            match("Blank"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BNKTYP"

    // $ANTLR start "DEL"
    public final void mDEL() throws RecognitionException {
        try {
            int _type = DEL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:7:5: ( 'del' )
            // RuleInterpreter.g:7:7: 'del'
            {
            match("del"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DEL"

    // $ANTLR start "ENDTYP"
    public final void mENDTYP() throws RecognitionException {
        try {
            int _type = ENDTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:8:8: ( 'END' )
            // RuleInterpreter.g:8:10: 'END'
            {
            match("END"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ENDTYP"

    // $ANTLR start "FRMB"
    public final void mFRMB() throws RecognitionException {
        try {
            int _type = FRMB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:9:6: ( 'from_beginning' )
            // RuleInterpreter.g:9:8: 'from_beginning'
            {
            match("from_beginning"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FRMB"

    // $ANTLR start "FRME"
    public final void mFRME() throws RecognitionException {
        try {
            int _type = FRME;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:10:6: ( 'from_end' )
            // RuleInterpreter.g:10:8: 'from_end'
            {
            match("from_end"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FRME"

    // $ANTLR start "FST"
    public final void mFST() throws RecognitionException {
        try {
            int _type = FST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:11:5: ( 'first' )
            // RuleInterpreter.g:11:7: 'first'
            {
            match("first"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FST"

    // $ANTLR start "INCLD"
    public final void mINCLD() throws RecognitionException {
        try {
            int _type = INCLD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:12:7: ( 'incld' )
            // RuleInterpreter.g:12:9: 'incld'
            {
            match("incld"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INCLD"

    // $ANTLR start "INS"
    public final void mINS() throws RecognitionException {
        try {
            int _type = INS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:13:5: ( 'ins' )
            // RuleInterpreter.g:13:7: 'ins'
            {
            match("ins"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INS"

    // $ANTLR start "LST"
    public final void mLST() throws RecognitionException {
        try {
            int _type = LST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:14:5: ( 'last' )
            // RuleInterpreter.g:14:7: 'last'
            {
            match("last"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LST"

    // $ANTLR start "MOV"
    public final void mMOV() throws RecognitionException {
        try {
            int _type = MOV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:15:5: ( 'mov' )
            // RuleInterpreter.g:15:7: 'mov'
            {
            match("mov"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "MOV"

    // $ANTLR start "NUMTYP"
    public final void mNUMTYP() throws RecognitionException {
        try {
            int _type = NUMTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:16:8: ( 'Number' )
            // RuleInterpreter.g:16:10: 'Number'
            {
            match("Number"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NUMTYP"

    // $ANTLR start "SRTTYP"
    public final void mSRTTYP() throws RecognitionException {
        try {
            int _type = SRTTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:17:8: ( 'START' )
            // RuleInterpreter.g:17:10: 'START'
            {
            match("START"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SRTTYP"

    // $ANTLR start "SYBTYP"
    public final void mSYBTYP() throws RecognitionException {
        try {
            int _type = SYBTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:18:8: ( 'Symbol' )
            // RuleInterpreter.g:18:10: 'Symbol'
            {
            match("Symbol"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SYBTYP"

    // $ANTLR start "WRDTYP"
    public final void mWRDTYP() throws RecognitionException {
        try {
            int _type = WRDTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:19:8: ( 'Word' )
            // RuleInterpreter.g:19:10: 'Word'
            {
            match("Word"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WRDTYP"

    // $ANTLR start "TOKEN"
    public final void mTOKEN() throws RecognitionException {
        try {
            int _type = TOKEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:77:7: ( '\\\"' ( . )+ '\\\"' )
            // RuleInterpreter.g:77:9: '\\\"' ( . )+ '\\\"'
            {
            match('\"'); 

            // RuleInterpreter.g:77:13: ( . )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\"') ) {
                    alt1=2;
                }
                else if ( ((LA1_0 >= '\u0000' && LA1_0 <= '!')||(LA1_0 >= '#' && LA1_0 <= '\uFFFF')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // RuleInterpreter.g:77:13: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "TOKEN"

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:79:8: ( ( '\\r' )? '\\n' )
            // RuleInterpreter.g:79:10: ( '\\r' )? '\\n'
            {
            // RuleInterpreter.g:79:10: ( '\\r' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='\r') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // RuleInterpreter.g:79:10: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }


            match('\n'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NEWLINE"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:81:3: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // RuleInterpreter.g:81:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // RuleInterpreter.g:81:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0 >= '\t' && LA3_0 <= '\n')||LA3_0=='\r'||LA3_0==' ') ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // RuleInterpreter.g:
            	    {
            	    if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "NUM"
    public final void mNUM() throws RecognitionException {
        try {
            int _type = NUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:83:5: ( ( DIGIT )+ )
            // RuleInterpreter.g:83:7: ( DIGIT )+
            {
            // RuleInterpreter.g:83:7: ( DIGIT )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0 >= '0' && LA4_0 <= '9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // RuleInterpreter.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NUM"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // RuleInterpreter.g:85:2: ( '0' .. '9' )
            // RuleInterpreter.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DIGIT"

    public void mTokens() throws RecognitionException {
        // RuleInterpreter.g:1:8: ( ANYNUM | ANYTOK | ANYTOKS | ANYTYP | BNKTYP | DEL | ENDTYP | FRMB | FRME | FST | INCLD | INS | LST | MOV | NUMTYP | SRTTYP | SYBTYP | WRDTYP | TOKEN | NEWLINE | WS | NUM )
        int alt5=22;
        switch ( input.LA(1) ) {
        case 'a':
            {
            int LA5_1 = input.LA(2);

            if ( (LA5_1=='n') ) {
                int LA5_18 = input.LA(3);

                if ( (LA5_18=='y') ) {
                    int LA5_26 = input.LA(4);

                    if ( (LA5_26=='n') ) {
                        alt5=1;
                    }
                    else if ( (LA5_26=='t') ) {
                        alt5=3;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 5, 26, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 18, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 1, input);

                throw nvae;

            }
            }
            break;
        case 'A':
            {
            int LA5_2 = input.LA(2);

            if ( (LA5_2=='N') ) {
                int LA5_19 = input.LA(3);

                if ( (LA5_19=='Y') ) {
                    int LA5_27 = input.LA(4);

                    if ( (LA5_27=='T') ) {
                        int LA5_33 = input.LA(5);

                        if ( (LA5_33=='O') ) {
                            alt5=2;
                        }
                        else if ( (LA5_33=='Y') ) {
                            alt5=4;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 33, input);

                            throw nvae;

                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 5, 27, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 19, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 2, input);

                throw nvae;

            }
            }
            break;
        case 'B':
            {
            alt5=5;
            }
            break;
        case 'd':
            {
            alt5=6;
            }
            break;
        case 'E':
            {
            alt5=7;
            }
            break;
        case 'f':
            {
            int LA5_6 = input.LA(2);

            if ( (LA5_6=='r') ) {
                int LA5_20 = input.LA(3);

                if ( (LA5_20=='o') ) {
                    int LA5_28 = input.LA(4);

                    if ( (LA5_28=='m') ) {
                        int LA5_34 = input.LA(5);

                        if ( (LA5_34=='_') ) {
                            int LA5_37 = input.LA(6);

                            if ( (LA5_37=='b') ) {
                                alt5=8;
                            }
                            else if ( (LA5_37=='e') ) {
                                alt5=9;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 5, 37, input);

                                throw nvae;

                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 34, input);

                            throw nvae;

                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 5, 28, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 20, input);

                    throw nvae;

                }
            }
            else if ( (LA5_6=='i') ) {
                alt5=10;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 6, input);

                throw nvae;

            }
            }
            break;
        case 'i':
            {
            int LA5_7 = input.LA(2);

            if ( (LA5_7=='n') ) {
                int LA5_22 = input.LA(3);

                if ( (LA5_22=='c') ) {
                    alt5=11;
                }
                else if ( (LA5_22=='s') ) {
                    alt5=12;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 22, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 7, input);

                throw nvae;

            }
            }
            break;
        case 'l':
            {
            alt5=13;
            }
            break;
        case 'm':
            {
            alt5=14;
            }
            break;
        case 'N':
            {
            alt5=15;
            }
            break;
        case 'S':
            {
            int LA5_11 = input.LA(2);

            if ( (LA5_11=='T') ) {
                alt5=16;
            }
            else if ( (LA5_11=='y') ) {
                alt5=17;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 11, input);

                throw nvae;

            }
            }
            break;
        case 'W':
            {
            alt5=18;
            }
            break;
        case '\"':
            {
            alt5=19;
            }
            break;
        case '\r':
            {
            int LA5_14 = input.LA(2);

            if ( (LA5_14=='\n') ) {
                int LA5_15 = input.LA(3);

                if ( ((LA5_15 >= '\t' && LA5_15 <= '\n')||LA5_15=='\r'||LA5_15==' ') ) {
                    alt5=21;
                }
                else {
                    alt5=20;
                }
            }
            else {
                alt5=21;
            }
            }
            break;
        case '\n':
            {
            int LA5_15 = input.LA(2);

            if ( ((LA5_15 >= '\t' && LA5_15 <= '\n')||LA5_15=='\r'||LA5_15==' ') ) {
                alt5=21;
            }
            else {
                alt5=20;
            }
            }
            break;
        case '\t':
        case ' ':
            {
            alt5=21;
            }
            break;
        case '0':
        case '1':
        case '2':
        case '3':
        case '4':
        case '5':
        case '6':
        case '7':
        case '8':
        case '9':
            {
            alt5=22;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 5, 0, input);

            throw nvae;

        }

        switch (alt5) {
            case 1 :
                // RuleInterpreter.g:1:10: ANYNUM
                {
                mANYNUM(); 


                }
                break;
            case 2 :
                // RuleInterpreter.g:1:17: ANYTOK
                {
                mANYTOK(); 


                }
                break;
            case 3 :
                // RuleInterpreter.g:1:24: ANYTOKS
                {
                mANYTOKS(); 


                }
                break;
            case 4 :
                // RuleInterpreter.g:1:32: ANYTYP
                {
                mANYTYP(); 


                }
                break;
            case 5 :
                // RuleInterpreter.g:1:39: BNKTYP
                {
                mBNKTYP(); 


                }
                break;
            case 6 :
                // RuleInterpreter.g:1:46: DEL
                {
                mDEL(); 


                }
                break;
            case 7 :
                // RuleInterpreter.g:1:50: ENDTYP
                {
                mENDTYP(); 


                }
                break;
            case 8 :
                // RuleInterpreter.g:1:57: FRMB
                {
                mFRMB(); 


                }
                break;
            case 9 :
                // RuleInterpreter.g:1:62: FRME
                {
                mFRME(); 


                }
                break;
            case 10 :
                // RuleInterpreter.g:1:67: FST
                {
                mFST(); 


                }
                break;
            case 11 :
                // RuleInterpreter.g:1:71: INCLD
                {
                mINCLD(); 


                }
                break;
            case 12 :
                // RuleInterpreter.g:1:77: INS
                {
                mINS(); 


                }
                break;
            case 13 :
                // RuleInterpreter.g:1:81: LST
                {
                mLST(); 


                }
                break;
            case 14 :
                // RuleInterpreter.g:1:85: MOV
                {
                mMOV(); 


                }
                break;
            case 15 :
                // RuleInterpreter.g:1:89: NUMTYP
                {
                mNUMTYP(); 


                }
                break;
            case 16 :
                // RuleInterpreter.g:1:96: SRTTYP
                {
                mSRTTYP(); 


                }
                break;
            case 17 :
                // RuleInterpreter.g:1:103: SYBTYP
                {
                mSYBTYP(); 


                }
                break;
            case 18 :
                // RuleInterpreter.g:1:110: WRDTYP
                {
                mWRDTYP(); 


                }
                break;
            case 19 :
                // RuleInterpreter.g:1:117: TOKEN
                {
                mTOKEN(); 


                }
                break;
            case 20 :
                // RuleInterpreter.g:1:123: NEWLINE
                {
                mNEWLINE(); 


                }
                break;
            case 21 :
                // RuleInterpreter.g:1:131: WS
                {
                mWS(); 


                }
                break;
            case 22 :
                // RuleInterpreter.g:1:134: NUM
                {
                mNUM(); 


                }
                break;

        }

    }


 

}