// $ANTLR 3.4 INSInterpreter.g 2012-06-03 21:17:09
package edu.isi.karma.cleaning.changed_grammar;
import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class INSInterpreterLexer extends Lexer {
    public static final int EOF=-1;
    public static final int ANYNUM=4;
    public static final int ANYTOK=5;
    public static final int ANYTYP=6;
    public static final int BNKTYP=7;
    public static final int DIGIT=8;
    public static final int ENDTYP=9;
    public static final int FRMB=10;
    public static final int FRME=11;
    public static final int FST=12;
    public static final int INCLD=13;
    public static final int INS=14;
    public static final int LST=15;
    public static final int NEWLINE=16;
    public static final int NUM=17;
    public static final int NUMTYP=18;
    public static final int SRTTYP=19;
    public static final int SYBTYP=20;
    public static final int TOKEN=21;
    public static final int WRDTYP=22;
    public static final int WS=23;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public INSInterpreterLexer() {} 
    public INSInterpreterLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public INSInterpreterLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "INSInterpreter.g"; }

    // $ANTLR start "ANYNUM"
    public final void mANYNUM() throws RecognitionException {
        try {
            int _type = ANYNUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // INSInterpreter.g:2:8: ( 'anynumber' )
            // INSInterpreter.g:2:10: 'anynumber'
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
            // INSInterpreter.g:3:8: ( 'ANYTOK' )
            // INSInterpreter.g:3:10: 'ANYTOK'
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

    // $ANTLR start "ANYTYP"
    public final void mANYTYP() throws RecognitionException {
        try {
            int _type = ANYTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // INSInterpreter.g:4:8: ( 'ANYTYP' )
            // INSInterpreter.g:4:10: 'ANYTYP'
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
            // INSInterpreter.g:5:8: ( 'Blank' )
            // INSInterpreter.g:5:10: 'Blank'
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

    // $ANTLR start "ENDTYP"
    public final void mENDTYP() throws RecognitionException {
        try {
            int _type = ENDTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // INSInterpreter.g:6:8: ( 'END' )
            // INSInterpreter.g:6:10: 'END'
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
            // INSInterpreter.g:7:6: ( 'from_beginning' )
            // INSInterpreter.g:7:8: 'from_beginning'
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
            // INSInterpreter.g:8:6: ( 'from_end' )
            // INSInterpreter.g:8:8: 'from_end'
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
            // INSInterpreter.g:9:5: ( 'first' )
            // INSInterpreter.g:9:7: 'first'
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
            // INSInterpreter.g:10:7: ( 'incld' )
            // INSInterpreter.g:10:9: 'incld'
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
            // INSInterpreter.g:11:5: ( 'ins' )
            // INSInterpreter.g:11:7: 'ins'
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
            // INSInterpreter.g:12:5: ( 'last' )
            // INSInterpreter.g:12:7: 'last'
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

    // $ANTLR start "NUMTYP"
    public final void mNUMTYP() throws RecognitionException {
        try {
            int _type = NUMTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // INSInterpreter.g:13:8: ( 'Number' )
            // INSInterpreter.g:13:10: 'Number'
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
            // INSInterpreter.g:14:8: ( 'START' )
            // INSInterpreter.g:14:10: 'START'
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
            // INSInterpreter.g:15:8: ( 'Symbol' )
            // INSInterpreter.g:15:10: 'Symbol'
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
            // INSInterpreter.g:16:8: ( 'Word' )
            // INSInterpreter.g:16:10: 'Word'
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
            // INSInterpreter.g:72:7: ( '\\\"' ( . )+ '\\\"' )
            // INSInterpreter.g:72:9: '\\\"' ( . )+ '\\\"'
            {
            match('\"'); 

            // INSInterpreter.g:72:13: ( . )+
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
            	    // INSInterpreter.g:72:13: .
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
            // INSInterpreter.g:74:8: ( ( '\\r' )? '\\n' )
            // INSInterpreter.g:74:10: ( '\\r' )? '\\n'
            {
            // INSInterpreter.g:74:10: ( '\\r' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='\r') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // INSInterpreter.g:74:10: '\\r'
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
            // INSInterpreter.g:76:3: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // INSInterpreter.g:76:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // INSInterpreter.g:76:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
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
            	    // INSInterpreter.g:
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
            // INSInterpreter.g:78:5: ( ( DIGIT )+ )
            // INSInterpreter.g:78:7: ( DIGIT )+
            {
            // INSInterpreter.g:78:7: ( DIGIT )+
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
            	    // INSInterpreter.g:
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
            // INSInterpreter.g:80:2: ( '0' .. '9' )
            // INSInterpreter.g:
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
        // INSInterpreter.g:1:8: ( ANYNUM | ANYTOK | ANYTYP | BNKTYP | ENDTYP | FRMB | FRME | FST | INCLD | INS | LST | NUMTYP | SRTTYP | SYBTYP | WRDTYP | TOKEN | NEWLINE | WS | NUM )
        int alt5=19;
        switch ( input.LA(1) ) {
        case 'a':
            {
            alt5=1;
            }
            break;
        case 'A':
            {
            int LA5_2 = input.LA(2);

            if ( (LA5_2=='N') ) {
                int LA5_16 = input.LA(3);

                if ( (LA5_16=='Y') ) {
                    int LA5_23 = input.LA(4);

                    if ( (LA5_23=='T') ) {
                        int LA5_27 = input.LA(5);

                        if ( (LA5_27=='O') ) {
                            alt5=2;
                        }
                        else if ( (LA5_27=='Y') ) {
                            alt5=3;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 27, input);

                            throw nvae;

                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 5, 23, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 16, input);

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
            alt5=4;
            }
            break;
        case 'E':
            {
            alt5=5;
            }
            break;
        case 'f':
            {
            int LA5_5 = input.LA(2);

            if ( (LA5_5=='r') ) {
                int LA5_17 = input.LA(3);

                if ( (LA5_17=='o') ) {
                    int LA5_24 = input.LA(4);

                    if ( (LA5_24=='m') ) {
                        int LA5_28 = input.LA(5);

                        if ( (LA5_28=='_') ) {
                            int LA5_31 = input.LA(6);

                            if ( (LA5_31=='b') ) {
                                alt5=6;
                            }
                            else if ( (LA5_31=='e') ) {
                                alt5=7;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 5, 31, input);

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
                            new NoViableAltException("", 5, 24, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 17, input);

                    throw nvae;

                }
            }
            else if ( (LA5_5=='i') ) {
                alt5=8;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 5, input);

                throw nvae;

            }
            }
            break;
        case 'i':
            {
            int LA5_6 = input.LA(2);

            if ( (LA5_6=='n') ) {
                int LA5_19 = input.LA(3);

                if ( (LA5_19=='c') ) {
                    alt5=9;
                }
                else if ( (LA5_19=='s') ) {
                    alt5=10;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 19, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 6, input);

                throw nvae;

            }
            }
            break;
        case 'l':
            {
            alt5=11;
            }
            break;
        case 'N':
            {
            alt5=12;
            }
            break;
        case 'S':
            {
            int LA5_9 = input.LA(2);

            if ( (LA5_9=='T') ) {
                alt5=13;
            }
            else if ( (LA5_9=='y') ) {
                alt5=14;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 9, input);

                throw nvae;

            }
            }
            break;
        case 'W':
            {
            alt5=15;
            }
            break;
        case '\"':
            {
            alt5=16;
            }
            break;
        case '\r':
            {
            int LA5_12 = input.LA(2);

            if ( (LA5_12=='\n') ) {
                int LA5_13 = input.LA(3);

                if ( ((LA5_13 >= '\t' && LA5_13 <= '\n')||LA5_13=='\r'||LA5_13==' ') ) {
                    alt5=18;
                }
                else {
                    alt5=17;
                }
            }
            else {
                alt5=18;
            }
            }
            break;
        case '\n':
            {
            int LA5_13 = input.LA(2);

            if ( ((LA5_13 >= '\t' && LA5_13 <= '\n')||LA5_13=='\r'||LA5_13==' ') ) {
                alt5=18;
            }
            else {
                alt5=17;
            }
            }
            break;
        case '\t':
        case ' ':
            {
            alt5=18;
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
            alt5=19;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 5, 0, input);

            throw nvae;

        }

        switch (alt5) {
            case 1 :
                // INSInterpreter.g:1:10: ANYNUM
                {
                mANYNUM(); 


                }
                break;
            case 2 :
                // INSInterpreter.g:1:17: ANYTOK
                {
                mANYTOK(); 


                }
                break;
            case 3 :
                // INSInterpreter.g:1:24: ANYTYP
                {
                mANYTYP(); 


                }
                break;
            case 4 :
                // INSInterpreter.g:1:31: BNKTYP
                {
                mBNKTYP(); 


                }
                break;
            case 5 :
                // INSInterpreter.g:1:38: ENDTYP
                {
                mENDTYP(); 


                }
                break;
            case 6 :
                // INSInterpreter.g:1:45: FRMB
                {
                mFRMB(); 


                }
                break;
            case 7 :
                // INSInterpreter.g:1:50: FRME
                {
                mFRME(); 


                }
                break;
            case 8 :
                // INSInterpreter.g:1:55: FST
                {
                mFST(); 


                }
                break;
            case 9 :
                // INSInterpreter.g:1:59: INCLD
                {
                mINCLD(); 


                }
                break;
            case 10 :
                // INSInterpreter.g:1:65: INS
                {
                mINS(); 


                }
                break;
            case 11 :
                // INSInterpreter.g:1:69: LST
                {
                mLST(); 


                }
                break;
            case 12 :
                // INSInterpreter.g:1:73: NUMTYP
                {
                mNUMTYP(); 


                }
                break;
            case 13 :
                // INSInterpreter.g:1:80: SRTTYP
                {
                mSRTTYP(); 


                }
                break;
            case 14 :
                // INSInterpreter.g:1:87: SYBTYP
                {
                mSYBTYP(); 


                }
                break;
            case 15 :
                // INSInterpreter.g:1:94: WRDTYP
                {
                mWRDTYP(); 


                }
                break;
            case 16 :
                // INSInterpreter.g:1:101: TOKEN
                {
                mTOKEN(); 


                }
                break;
            case 17 :
                // INSInterpreter.g:1:107: NEWLINE
                {
                mNEWLINE(); 


                }
                break;
            case 18 :
                // INSInterpreter.g:1:115: WS
                {
                mWS(); 


                }
                break;
            case 19 :
                // INSInterpreter.g:1:118: NUM
                {
                mNUM(); 


                }
                break;

        }

    }


 

}