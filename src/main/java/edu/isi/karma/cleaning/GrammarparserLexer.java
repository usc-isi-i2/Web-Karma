/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
// $ANTLR 3.4 Grammarparser.g 2011-10-02 15:18:36
package edu.isi.karma.cleaning;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class GrammarparserLexer extends Lexer {
    public static final int EOF=-1;
    public static final int END=4;
    public static final int LOWERLETTER=5;
    public static final int LOWWRD=6;
    public static final int OR=7;
    public static final int QUE=8;
    public static final int SEP=9;
    public static final int UPPERLETTER=10;
    public static final int UPPERWRD=11;
    public static final int WS=12;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public GrammarparserLexer() {} 
    public GrammarparserLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public GrammarparserLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "Grammarparser.g"; }

    // $ANTLR start "LOWERLETTER"
    public final void mLOWERLETTER() throws RecognitionException {
        try {
            int _type = LOWERLETTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:19:2: ( 'a' .. 'z' )
            // Grammarparser.g:
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

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LOWERLETTER"

    // $ANTLR start "UPPERLETTER"
    public final void mUPPERLETTER() throws RecognitionException {
        try {
            int _type = UPPERLETTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:21:2: ( 'A' .. 'Z' )
            // Grammarparser.g:
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
    // $ANTLR end "UPPERLETTER"

    // $ANTLR start "LOWWRD"
    public final void mLOWWRD() throws RecognitionException {
        try {
            int _type = LOWWRD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:22:8: ( LOWERLETTER ( UPPERLETTER | LOWERLETTER )* )
            // Grammarparser.g:22:9: LOWERLETTER ( UPPERLETTER | LOWERLETTER )*
            {
            mLOWERLETTER(); 


            // Grammarparser.g:22:20: ( UPPERLETTER | LOWERLETTER )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0 >= 'A' && LA1_0 <= 'Z')||(LA1_0 >= 'a' && LA1_0 <= 'z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // Grammarparser.g:
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
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LOWWRD"

    // $ANTLR start "UPPERWRD"
    public final void mUPPERWRD() throws RecognitionException {
        try {
            int _type = UPPERWRD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:25:2: ( UPPERLETTER ( UPPERLETTER | LOWERLETTER )* )
            // Grammarparser.g:25:4: UPPERLETTER ( UPPERLETTER | LOWERLETTER )*
            {
            mUPPERLETTER(); 


            // Grammarparser.g:25:15: ( UPPERLETTER | LOWERLETTER )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0 >= 'A' && LA2_0 <= 'Z')||(LA2_0 >= 'a' && LA2_0 <= 'z')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Grammarparser.g:
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
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "UPPERWRD"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:26:3: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // Grammarparser.g:26:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // Grammarparser.g:26:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
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
            	    // Grammarparser.g:
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

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:27:4: ( '|' )
            // Grammarparser.g:27:6: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "SEP"
    public final void mSEP() throws RecognitionException {
        try {
            int _type = SEP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:28:5: ( ':' )
            // Grammarparser.g:28:7: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "SEP"

    // $ANTLR start "END"
    public final void mEND() throws RecognitionException {
        try {
            int _type = END;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:29:5: ( ';' )
            // Grammarparser.g:29:7: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "END"

    // $ANTLR start "QUE"
    public final void mQUE() throws RecognitionException {
        try {
            int _type = QUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Grammarparser.g:30:5: ( '?' )
            // Grammarparser.g:30:7: '?'
            {
            match('?'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "QUE"

    public void mTokens() throws RecognitionException {
        // Grammarparser.g:1:8: ( LOWERLETTER | UPPERLETTER | LOWWRD | UPPERWRD | WS | OR | SEP | END | QUE )
        int alt4=9;
        switch ( input.LA(1) ) {
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
            int LA4_1 = input.LA(2);

            if ( ((LA4_1 >= 'A' && LA4_1 <= 'Z')||(LA4_1 >= 'a' && LA4_1 <= 'z')) ) {
                alt4=3;
            }
            else {
                alt4=1;
            }
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
            int LA4_2 = input.LA(2);

            if ( ((LA4_2 >= 'A' && LA4_2 <= 'Z')||(LA4_2 >= 'a' && LA4_2 <= 'z')) ) {
                alt4=4;
            }
            else {
                alt4=2;
            }
            }
            break;
        case '\t':
        case '\n':
        case '\r':
        case ' ':
            {
            alt4=5;
            }
            break;
        case '|':
            {
            alt4=6;
            }
            break;
        case ':':
            {
            alt4=7;
            }
            break;
        case ';':
            {
            alt4=8;
            }
            break;
        case '?':
            {
            alt4=9;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 4, 0, input);

            throw nvae;

        }

        switch (alt4) {
            case 1 :
                // Grammarparser.g:1:10: LOWERLETTER
                {
                mLOWERLETTER(); 


                }
                break;
            case 2 :
                // Grammarparser.g:1:22: UPPERLETTER
                {
                mUPPERLETTER(); 


                }
                break;
            case 3 :
                // Grammarparser.g:1:34: LOWWRD
                {
                mLOWWRD(); 


                }
                break;
            case 4 :
                // Grammarparser.g:1:41: UPPERWRD
                {
                mUPPERWRD(); 


                }
                break;
            case 5 :
                // Grammarparser.g:1:50: WS
                {
                mWS(); 


                }
                break;
            case 6 :
                // Grammarparser.g:1:53: OR
                {
                mOR(); 


                }
                break;
            case 7 :
                // Grammarparser.g:1:56: SEP
                {
                mSEP(); 


                }
                break;
            case 8 :
                // Grammarparser.g:1:60: END
                {
                mEND(); 


                }
                break;
            case 9 :
                // Grammarparser.g:1:64: QUE
                {
                mQUE(); 


                }
                break;

        }

    }


 

}
