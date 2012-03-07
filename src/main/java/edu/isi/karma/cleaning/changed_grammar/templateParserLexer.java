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
// $ANTLR 3.4 templateParser.g 2012-02-13 14:41:40
package edu.isi.karma.cleaning.changed_grammar;
import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class templateParserLexer extends Lexer {
    public static final int EOF=-1;
    public static final int ANYNUM=4;
    public static final int ANYTOK=5;
    public static final int BNKTYP=6;
    public static final int DIGIT=7;
    public static final int FRMB=8;
    public static final int FRME=9;
    public static final int FST=10;
    public static final int INCLD=11;
    public static final int LST=12;
    public static final int NEWLINE=13;
    public static final int NUM=14;
    public static final int NUMTYP=15;
    public static final int SYBTYP=16;
    public static final int TOKEN=17;
    public static final int WRDTYP=18;
    public static final int WS=19;

    // delegates
    // delegators
    public Lexer[] getDelegates() {
        return new Lexer[] {};
    }

    public templateParserLexer() {} 
    public templateParserLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public templateParserLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "templateParser.g"; }

    // $ANTLR start "ANYNUM"
    public final void mANYNUM() throws RecognitionException {
        try {
            int _type = ANYNUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // templateParser.g:2:8: ( 'anynumber' )
            // templateParser.g:2:10: 'anynumber'
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
            // templateParser.g:3:8: ( 'ANYTOK' )
            // templateParser.g:3:10: 'ANYTOK'
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

    // $ANTLR start "BNKTYP"
    public final void mBNKTYP() throws RecognitionException {
        try {
            int _type = BNKTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // templateParser.g:4:8: ( 'Blank' )
            // templateParser.g:4:10: 'Blank'
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

    // $ANTLR start "FRMB"
    public final void mFRMB() throws RecognitionException {
        try {
            int _type = FRMB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // templateParser.g:5:6: ( 'from_beginning' )
            // templateParser.g:5:8: 'from_beginning'
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
            // templateParser.g:6:6: ( 'from_end' )
            // templateParser.g:6:8: 'from_end'
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
            // templateParser.g:7:5: ( 'first' )
            // templateParser.g:7:7: 'first'
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
            // templateParser.g:8:7: ( 'incld' )
            // templateParser.g:8:9: 'incld'
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
            // templateParser.g:9:5: ( 'last' )
            // templateParser.g:9:7: 'last'
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
            // templateParser.g:10:8: ( 'Number' )
            // templateParser.g:10:10: 'Number'
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

    // $ANTLR start "SYBTYP"
    public final void mSYBTYP() throws RecognitionException {
        try {
            int _type = SYBTYP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // templateParser.g:11:8: ( 'Symbol' )
            // templateParser.g:11:10: 'Symbol'
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
            // templateParser.g:12:8: ( 'Word' )
            // templateParser.g:12:10: 'Word'
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
            // templateParser.g:75:7: ( '\\\"' ( . )+ '\\\"' )
            // templateParser.g:75:9: '\\\"' ( . )+ '\\\"'
            {
            match('\"'); 

            // templateParser.g:75:13: ( . )+
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
            	    // templateParser.g:75:13: .
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
            // templateParser.g:77:8: ( ( '\\r' )? '\\n' )
            // templateParser.g:77:10: ( '\\r' )? '\\n'
            {
            // templateParser.g:77:10: ( '\\r' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='\r') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // templateParser.g:77:10: '\\r'
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
            // templateParser.g:79:3: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // templateParser.g:79:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // templateParser.g:79:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
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
            	    // templateParser.g:
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
            // templateParser.g:81:5: ( ( DIGIT )+ )
            // templateParser.g:81:7: ( DIGIT )+
            {
            // templateParser.g:81:7: ( DIGIT )+
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
            	    // templateParser.g:
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
            // templateParser.g:83:2: ( '0' .. '9' )
            // templateParser.g:
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
        // templateParser.g:1:8: ( ANYNUM | ANYTOK | BNKTYP | FRMB | FRME | FST | INCLD | LST | NUMTYP | SYBTYP | WRDTYP | TOKEN | NEWLINE | WS | NUM )
        int alt5=15;
        switch ( input.LA(1) ) {
        case 'a':
            {
            alt5=1;
            }
            break;
        case 'A':
            {
            alt5=2;
            }
            break;
        case 'B':
            {
            alt5=3;
            }
            break;
        case 'f':
            {
            int LA5_4 = input.LA(2);

            if ( (LA5_4=='r') ) {
                int LA5_15 = input.LA(3);

                if ( (LA5_15=='o') ) {
                    int LA5_18 = input.LA(4);

                    if ( (LA5_18=='m') ) {
                        int LA5_19 = input.LA(5);

                        if ( (LA5_19=='_') ) {
                            int LA5_20 = input.LA(6);

                            if ( (LA5_20=='b') ) {
                                alt5=4;
                            }
                            else if ( (LA5_20=='e') ) {
                                alt5=5;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 5, 20, input);

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
                            new NoViableAltException("", 5, 18, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 15, input);

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
        case 'N':
            {
            alt5=9;
            }
            break;
        case 'S':
            {
            alt5=10;
            }
            break;
        case 'W':
            {
            alt5=11;
            }
            break;
        case '\"':
            {
            alt5=12;
            }
            break;
        case '\r':
            {
            int LA5_11 = input.LA(2);

            if ( (LA5_11=='\n') ) {
                int LA5_12 = input.LA(3);

                if ( ((LA5_12 >= '\t' && LA5_12 <= '\n')||LA5_12=='\r'||LA5_12==' ') ) {
                    alt5=14;
                }
                else {
                    alt5=13;
                }
            }
            else {
                alt5=14;
            }
            }
            break;
        case '\n':
            {
            int LA5_12 = input.LA(2);

            if ( ((LA5_12 >= '\t' && LA5_12 <= '\n')||LA5_12=='\r'||LA5_12==' ') ) {
                alt5=14;
            }
            else {
                alt5=13;
            }
            }
            break;
        case '\t':
        case ' ':
            {
            alt5=14;
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
            alt5=15;
            }
            break;
        default:
            NoViableAltException nvae =
                new NoViableAltException("", 5, 0, input);

            throw nvae;

        }

        switch (alt5) {
            case 1 :
                // templateParser.g:1:10: ANYNUM
                {
                mANYNUM(); 


                }
                break;
            case 2 :
                // templateParser.g:1:17: ANYTOK
                {
                mANYTOK(); 


                }
                break;
            case 3 :
                // templateParser.g:1:24: BNKTYP
                {
                mBNKTYP(); 


                }
                break;
            case 4 :
                // templateParser.g:1:31: FRMB
                {
                mFRMB(); 


                }
                break;
            case 5 :
                // templateParser.g:1:36: FRME
                {
                mFRME(); 


                }
                break;
            case 6 :
                // templateParser.g:1:41: FST
                {
                mFST(); 


                }
                break;
            case 7 :
                // templateParser.g:1:45: INCLD
                {
                mINCLD(); 


                }
                break;
            case 8 :
                // templateParser.g:1:51: LST
                {
                mLST(); 


                }
                break;
            case 9 :
                // templateParser.g:1:55: NUMTYP
                {
                mNUMTYP(); 


                }
                break;
            case 10 :
                // templateParser.g:1:62: SYBTYP
                {
                mSYBTYP(); 


                }
                break;
            case 11 :
                // templateParser.g:1:69: WRDTYP
                {
                mWRDTYP(); 


                }
                break;
            case 12 :
                // templateParser.g:1:76: TOKEN
                {
                mTOKEN(); 


                }
                break;
            case 13 :
                // templateParser.g:1:82: NEWLINE
                {
                mNEWLINE(); 


                }
                break;
            case 14 :
                // templateParser.g:1:90: WS
                {
                mWS(); 


                }
                break;
            case 15 :
                // templateParser.g:1:93: NUM
                {
                mNUM(); 


                }
                break;

        }

    }


 

}
