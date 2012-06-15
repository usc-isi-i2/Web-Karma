// $ANTLR 3.4 MOVInterpreter.g 2012-06-03 21:17:09
package edu.isi.karma.cleaning.changed_grammar;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class MOVInterpreterLexer extends Lexer {
    public static final int EOF=-1;
    public static final int ANYTOKS=4;
    public static final int BNKTYP=5;
    public static final int DIGIT=6;
    public static final int ENDTYP=7;
    public static final int FRMB=8;
    public static final int FRME=9;
    public static final int FST=10;
    public static final int INCLD=11;
    public static final int LST=12;
    public static final int MOV=13;
    public static final int NEWLINE=14;
    public static final int NUM=15;
    public static final int NUMTYP=16;
    public static final int SRTTYP=17;
    public static final int SYBTYP=18;
    public static final int TOKEN=19;
    public static final int WRDTYP=20;
    public static final int WS=21;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public MOVInterpreterLexer() {} 
    public MOVInterpreterLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public MOVInterpreterLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "MOVInterpreter.g"; }

    // $ANTLR start "ANYTOKS"
    public final void mANYTOKS() throws RecognitionException {
        try {
            int _type = ANYTOKS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // MOVInterpreter.g:2:9: ( 'anytoks' )
            // MOVInterpreter.g:2:11: 'anytoks'
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

    // $ANTLR start "BNKTYP"
    public final void mBNKTYP() throws RecognitionException {
        try {
            int _type = BNKTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // MOVInterpreter.g:3:8: ( 'Blank' )
            // MOVInterpreter.g:3:10: 'Blank'
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
            // MOVInterpreter.g:4:8: ( 'END' )
            // MOVInterpreter.g:4:10: 'END'
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
            // MOVInterpreter.g:5:6: ( 'from_beginning' )
            // MOVInterpreter.g:5:8: 'from_beginning'
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
            // MOVInterpreter.g:6:6: ( 'from_end' )
            // MOVInterpreter.g:6:8: 'from_end'
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
            // MOVInterpreter.g:7:5: ( 'first' )
            // MOVInterpreter.g:7:7: 'first'
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
            // MOVInterpreter.g:8:7: ( 'incld' )
            // MOVInterpreter.g:8:9: 'incld'
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

    // $ANTLR start "LST"
    public final void mLST() throws RecognitionException {
        try {
            int _type = LST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // MOVInterpreter.g:9:5: ( 'last' )
            // MOVInterpreter.g:9:7: 'last'
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
            // MOVInterpreter.g:10:5: ( 'mov' )
            // MOVInterpreter.g:10:7: 'mov'
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
            // MOVInterpreter.g:11:8: ( 'Number' )
            // MOVInterpreter.g:11:10: 'Number'
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
            // MOVInterpreter.g:12:8: ( 'START' )
            // MOVInterpreter.g:12:10: 'START'
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
            // MOVInterpreter.g:13:8: ( 'Symbol' )
            // MOVInterpreter.g:13:10: 'Symbol'
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
            // MOVInterpreter.g:14:8: ( 'Word' )
            // MOVInterpreter.g:14:10: 'Word'
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
            // MOVInterpreter.g:70:7: ( '\\\"' ( . )+ '\\\"' )
            // MOVInterpreter.g:70:9: '\\\"' ( . )+ '\\\"'
            {
            match('\"'); 

            // MOVInterpreter.g:70:13: ( . )+
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
            	    // MOVInterpreter.g:70:13: .
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
            // MOVInterpreter.g:72:8: ( ( '\\r' )? '\\n' )
            // MOVInterpreter.g:72:10: ( '\\r' )? '\\n'
            {
            // MOVInterpreter.g:72:10: ( '\\r' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='\r') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // MOVInterpreter.g:72:10: '\\r'
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
            // MOVInterpreter.g:74:3: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // MOVInterpreter.g:74:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // MOVInterpreter.g:74:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
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
            	    // MOVInterpreter.g:
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
            // MOVInterpreter.g:76:5: ( ( DIGIT )+ )
            // MOVInterpreter.g:76:7: ( DIGIT )+
            {
            // MOVInterpreter.g:76:7: ( DIGIT )+
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
            	    // MOVInterpreter.g:
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
            // MOVInterpreter.g:78:2: ( '0' .. '9' )
            // MOVInterpreter.g:
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
        // MOVInterpreter.g:1:8: ( ANYTOKS | BNKTYP | ENDTYP | FRMB | FRME | FST | INCLD | LST | MOV | NUMTYP | SRTTYP | SYBTYP | WRDTYP | TOKEN | NEWLINE | WS | NUM )
        int alt5=17;
        switch ( input.LA(1) ) {
        case 'a':
            {
            alt5=1;
            }
            break;
        case 'B':
            {
            alt5=2;
            }
            break;
        case 'E':
            {
            alt5=3;
            }
            break;
        case 'f':
            {
            int LA5_4 = input.LA(2);

            if ( (LA5_4=='r') ) {
                int LA5_16 = input.LA(3);

                if ( (LA5_16=='o') ) {
                    int LA5_21 = input.LA(4);

                    if ( (LA5_21=='m') ) {
                        int LA5_22 = input.LA(5);

                        if ( (LA5_22=='_') ) {
                            int LA5_23 = input.LA(6);

                            if ( (LA5_23=='b') ) {
                                alt5=4;
                            }
                            else if ( (LA5_23=='e') ) {
                                alt5=5;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 5, 23, input);

                                throw nvae;

                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 22, input);

                            throw nvae;

                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 5, 21, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 16, input);

                    throw nvae;

                }
            }
            else if ( (LA5_4=='i') ) {
                alt5=6;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 4, input);

                throw nvae;

            }
            }
            break;
        case 'i':
            {
            alt5=7;
            }
            break;
        case 'l':
            {
            alt5=8;
            }
            break;
        case 'm':
            {
            alt5=9;
            }
            break;
        case 'N':
            {
            alt5=10;
            }
            break;
        case 'S':
            {
            int LA5_9 = input.LA(2);

            if ( (LA5_9=='T') ) {
                alt5=11;
            }
            else if ( (LA5_9=='y') ) {
                alt5=12;
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
            alt5=13;
            }
            break;
        case '\"':
            {
            alt5=14;
            }
            break;
        case '\r':
            {
            int LA5_12 = input.LA(2);

            if ( (LA5_12=='\n') ) {
                int LA5_13 = input.LA(3);

                if ( ((LA5_13 >= '\t' && LA5_13 <= '\n')||LA5_13=='\r'||LA5_13==' ') ) {
                    alt5=16;
                }
                else {
                    alt5=15;
                }
            }
            else {
                alt5=16;
            }
            }
            break;
        case '\n':
            {
            int LA5_13 = input.LA(2);

            if ( ((LA5_13 >= '\t' && LA5_13 <= '\n')||LA5_13=='\r'||LA5_13==' ') ) {
                alt5=16;
            }
            else {
                alt5=15;
            }
            }
            break;
        case '\t':
        case ' ':
            {
            alt5=16;
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
            alt5=17;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 5, 0, input);

            throw nvae;

        }

        switch (alt5) {
            case 1 :
                // MOVInterpreter.g:1:10: ANYTOKS
                {
                mANYTOKS(); 


                }
                break;
            case 2 :
                // MOVInterpreter.g:1:18: BNKTYP
                {
                mBNKTYP(); 


                }
                break;
            case 3 :
                // MOVInterpreter.g:1:25: ENDTYP
                {
                mENDTYP(); 


                }
                break;
            case 4 :
                // MOVInterpreter.g:1:32: FRMB
                {
                mFRMB(); 


                }
                break;
            case 5 :
                // MOVInterpreter.g:1:37: FRME
                {
                mFRME(); 


                }
                break;
            case 6 :
                // MOVInterpreter.g:1:42: FST
                {
                mFST(); 


                }
                break;
            case 7 :
                // MOVInterpreter.g:1:46: INCLD
                {
                mINCLD(); 


                }
                break;
            case 8 :
                // MOVInterpreter.g:1:52: LST
                {
                mLST(); 


                }
                break;
            case 9 :
                // MOVInterpreter.g:1:56: MOV
                {
                mMOV(); 


                }
                break;
            case 10 :
                // MOVInterpreter.g:1:60: NUMTYP
                {
                mNUMTYP(); 


                }
                break;
            case 11 :
                // MOVInterpreter.g:1:67: SRTTYP
                {
                mSRTTYP(); 


                }
                break;
            case 12 :
                // MOVInterpreter.g:1:74: SYBTYP
                {
                mSYBTYP(); 


                }
                break;
            case 13 :
                // MOVInterpreter.g:1:81: WRDTYP
                {
                mWRDTYP(); 


                }
                break;
            case 14 :
                // MOVInterpreter.g:1:88: TOKEN
                {
                mTOKEN(); 


                }
                break;
            case 15 :
                // MOVInterpreter.g:1:94: NEWLINE
                {
                mNEWLINE(); 


                }
                break;
            case 16 :
                // MOVInterpreter.g:1:102: WS
                {
                mWS(); 


                }
                break;
            case 17 :
                // MOVInterpreter.g:1:105: NUM
                {
                mNUM(); 


                }
                break;

        }

    }


 

}