// $ANTLR 3.4 Tokenizer.g 2012-08-31 09:41:11
package edu.isi.karma.cleaning;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class Tokenizer extends Lexer {
    public static final int EOF=-1;
    public static final int BLANK=4;
    public static final int DIGIT=5;
    public static final int END=6;
    public static final int LETTER=7;
    public static final int LOWER=8;
    public static final int LWRD=9;
    public static final int NUMBER=10;
    public static final int START=11;
    public static final int SYBS=12;
    public static final int SYMBOL=13;
    public static final int UPPER=14;
    public static final int UWRD=15;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public Tokenizer() {} 
    public Tokenizer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public Tokenizer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "Tokenizer.g"; }

    // $ANTLR start "BLANK"
    public final void mBLANK() throws RecognitionException {
        try {
            int _type = BLANK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Tokenizer.g:3:6: ( ( '\\t' | ' ' | '\\r' | '\\n' | '\\u000C' ) )
            // Tokenizer.g:
            {
            if ( (input.LA(1) >= '\t' && input.LA(1) <= '\n')||(input.LA(1) >= '\f' && input.LA(1) <= '\r')||input.LA(1)==' ' ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BLANK"

    // $ANTLR start "UWRD"
    public final void mUWRD() throws RecognitionException {
        try {
            int _type = UWRD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Tokenizer.g:4:6: ( UPPER )
            // Tokenizer.g:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "UWRD"

    // $ANTLR start "LWRD"
    public final void mLWRD() throws RecognitionException {
        try {
            int _type = LWRD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Tokenizer.g:5:6: ( ( LOWER )+ )
            // Tokenizer.g:5:8: ( LOWER )+
            {
            // Tokenizer.g:5:8: ( LOWER )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // Tokenizer.g:
            	    {
            	    if ( (input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LWRD"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Tokenizer.g:6:7: ( ( DIGIT )+ )
            // Tokenizer.g:6:9: ( DIGIT )+
            {
            // Tokenizer.g:6:9: ( DIGIT )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0 >= '0' && LA2_0 <= '9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Tokenizer.g:
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
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "SYBS"
    public final void mSYBS() throws RecognitionException {
        try {
            int _type = SYBS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Tokenizer.g:7:5: ( ( SYMBOL ) )
            // Tokenizer.g:
            {

            if ( input.LA(1)=='<'||(input.LA(1) >= '!' && input.LA(1) <= '/')||(input.LA(1) >= ':' && input.LA(1) <= '@')||(input.LA(1) >= '[' && input.LA(1) <= '`')||(input.LA(1) >= '{' && input.LA(1) <= '~') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SYBS"

    // $ANTLR start "START"
    public final void mSTART() throws RecognitionException {
        try {
            int _type = START;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Tokenizer.g:8:7: ( '<_START>' )
            // Tokenizer.g:8:9: '<_START>'
            {
            match("<_START>"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "START"

    // $ANTLR start "END"
    public final void mEND() throws RecognitionException {
        try {
            int _type = END;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Tokenizer.g:9:5: ( '<_END>' )
            // Tokenizer.g:9:7: '<_END>'
            {
            match("<_END>"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "END"

    // $ANTLR start "SYMBOL"
    public final void mSYMBOL() throws RecognitionException {
        try {
            // Tokenizer.g:12:2: ( '!' | '#' .. '/' | ':' .. '@' | '[' .. '`' | '{' .. '~' )
            // Tokenizer.g:
            {
            if ( input.LA(1)=='!'||(input.LA(1) >= '#' && input.LA(1) <= '/')||(input.LA(1) >= ':' && input.LA(1) <= '@')||(input.LA(1) >= '[' && input.LA(1) <= '`')||(input.LA(1) >= '{' && input.LA(1) <= '~') ) {
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
    // $ANTLR end "SYMBOL"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // Tokenizer.g:14:2: ( LOWER | UPPER )
            // Tokenizer.g:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
    // $ANTLR end "LETTER"

    // $ANTLR start "LOWER"
    public final void mLOWER() throws RecognitionException {
        try {
            // Tokenizer.g:16:2: ( 'a' .. 'z' )
            // Tokenizer.g:
            {
            if ( (input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
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
    // $ANTLR end "LOWER"

    // $ANTLR start "UPPER"
    public final void mUPPER() throws RecognitionException {
        try {
            // Tokenizer.g:18:2: ( 'A' .. 'Z' )
            // Tokenizer.g:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z') ) {
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
    // $ANTLR end "UPPER"

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // Tokenizer.g:19:16: ( '0' .. '9' )
            // Tokenizer.g:
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
        // Tokenizer.g:1:8: ( BLANK | UWRD | LWRD | NUMBER | SYBS | START | END )
        int alt3=7;
        switch ( input.LA(1) ) {
        case '\t':
        case '\n':
        case '\f':
        case '\r':
        case ' ':
            {
            alt3=1;
            }
            break;
        case 'A':
        case 'B':
        case 'C':
        case 'D':
        case 'E':
        case 'F':
        case 'G':
        case 'H':
        case 'I':
        case 'J':
        case 'K':
        case 'L':
        case 'M':
        case 'N':
        case 'O':
        case 'P':
        case 'Q':
        case 'R':
        case 'S':
        case 'T':
        case 'U':
        case 'V':
        case 'W':
        case 'X':
        case 'Y':
        case 'Z':
            {
            alt3=2;
            }
            break;
        case 'a':
        case 'b':
        case 'c':
        case 'd':
        case 'e':
        case 'f':
        case 'g':
        case 'h':
        case 'i':
        case 'j':
        case 'k':
        case 'l':
        case 'm':
        case 'n':
        case 'o':
        case 'p':
        case 'q':
        case 'r':
        case 's':
        case 't':
        case 'u':
        case 'v':
        case 'w':
        case 'x':
        case 'y':
        case 'z':
            {
            alt3=3;
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
            alt3=4;
            }
            break;
        case '<':
            {
            int LA3_5 = input.LA(2);

            if ( (LA3_5=='_') ) {
                int LA3_7 = input.LA(3);

                if ( (LA3_7=='S') ) {
                    alt3=6;
                }
                else if ( (LA3_7=='E') ) {
                    alt3=7;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 7, input);

                    throw nvae;

                }
            }
            else {
                alt3=5;
            }
            }
            break;
        case '!':
        case '#':
        case '$':
        case '%':
        case '&':
        case '\'':
        case '(':
        case ')':
        case '*':
        case '+':
        case ',':
        case '-':
        case '.':
        case '/':
        case ':':
        case ';':
        case '=':
        case '>':
        case '?':
        case '@':
        case '[':
        case '\\':
        case ']':
        case '^':
        case '_':
        case '`':
        case '{':
        case '|':
        case '}':
        case '~':
            {
            alt3=5;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 3, 0, input);

            throw nvae;

        }

        switch (alt3) {
            case 1 :
                // Tokenizer.g:1:10: BLANK
                {
                mBLANK(); 


                }
                break;
            case 2 :
                // Tokenizer.g:1:16: UWRD
                {
                mUWRD(); 


                }
                break;
            case 3 :
                // Tokenizer.g:1:21: LWRD
                {
                mLWRD(); 


                }
                break;
            case 4 :
                // Tokenizer.g:1:26: NUMBER
                {
                mNUMBER(); 


                }
                break;
            case 5 :
                // Tokenizer.g:1:33: SYBS
                {
                mSYBS(); 


                }
                break;
            case 6 :
                // Tokenizer.g:1:38: START
                {
                mSTART(); 


                }
                break;
            case 7 :
                // Tokenizer.g:1:44: END
                {
                mEND(); 


                }
                break;

        }

    }


 

}