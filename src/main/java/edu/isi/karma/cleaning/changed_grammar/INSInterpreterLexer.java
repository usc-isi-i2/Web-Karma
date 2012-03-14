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
// $ANTLR 3.4 INSInterpreter.g 2012-02-13 14:41:39
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
    public static final int FRMB=9;
    public static final int FRME=10;
    public static final int FST=11;
    public static final int INCLD=12;
    public static final int INS=13;
    public static final int LST=14;
    public static final int NEWLINE=15;
    public static final int NUM=16;
    public static final int NUMTYP=17;
    public static final int SYBTYP=18;
    public static final int TOKEN=19;
    public static final int WRDTYP=20;
    public static final int WS=21;

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

    // $ANTLR start "FRMB"
    public final void mFRMB() throws RecognitionException {
        try {
            int _type = FRMB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // INSInterpreter.g:6:6: ( 'from_beginning' )
            // INSInterpreter.g:6:8: 'from_beginning'
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
            // INSInterpreter.g:7:6: ( 'from_end' )
            // INSInterpreter.g:7:8: 'from_end'
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
            // INSInterpreter.g:8:5: ( 'first' )
            // INSInterpreter.g:8:7: 'first'
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
            // INSInterpreter.g:9:7: ( 'incld' )
            // INSInterpreter.g:9:9: 'incld'
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
            // INSInterpreter.g:10:5: ( 'ins' )
            // INSInterpreter.g:10:7: 'ins'
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
            // INSInterpreter.g:11:5: ( 'last' )
            // INSInterpreter.g:11:7: 'last'
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
            // INSInterpreter.g:12:8: ( 'Number' )
            // INSInterpreter.g:12:10: 'Number'
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
            // INSInterpreter.g:13:8: ( 'Symbol' )
            // INSInterpreter.g:13:10: 'Symbol'
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
            // INSInterpreter.g:14:8: ( 'Word' )
            // INSInterpreter.g:14:10: 'Word'
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
            // INSInterpreter.g:70:7: ( '\\\"' ( . )+ '\\\"' )
            // INSInterpreter.g:70:9: '\\\"' ( . )+ '\\\"'
            {
            match('\"'); 

            // INSInterpreter.g:70:13: ( . )+
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
            	    // INSInterpreter.g:70:13: .
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
            // INSInterpreter.g:72:8: ( ( '\\r' )? '\\n' )
            // INSInterpreter.g:72:10: ( '\\r' )? '\\n'
            {
            // INSInterpreter.g:72:10: ( '\\r' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='\r') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // INSInterpreter.g:72:10: '\\r'
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
            // INSInterpreter.g:74:3: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // INSInterpreter.g:74:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // INSInterpreter.g:74:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
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
            // INSInterpreter.g:76:5: ( ( DIGIT )+ )
            // INSInterpreter.g:76:7: ( DIGIT )+
            {
            // INSInterpreter.g:76:7: ( DIGIT )+
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
            // INSInterpreter.g:78:2: ( '0' .. '9' )
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
        // INSInterpreter.g:1:8: ( ANYNUM | ANYTOK | ANYTYP | BNKTYP | FRMB | FRME | FST | INCLD | INS | LST | NUMTYP | SYBTYP | WRDTYP | TOKEN | NEWLINE | WS | NUM )
        int alt5=17;
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
                int LA5_15 = input.LA(3);

                if ( (LA5_15=='Y') ) {
                    int LA5_20 = input.LA(4);

                    if ( (LA5_20=='T') ) {
                        int LA5_24 = input.LA(5);

                        if ( (LA5_24=='O') ) {
                            alt5=2;
                        }
                        else if ( (LA5_24=='Y') ) {
                            alt5=3;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 24, input);

                            throw nvae;

                        }
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 5, 20, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 15, input);

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
        case 'f':
            {
            int LA5_4 = input.LA(2);

            if ( (LA5_4=='r') ) {
                int LA5_16 = input.LA(3);

                if ( (LA5_16=='o') ) {
                    int LA5_21 = input.LA(4);

                    if ( (LA5_21=='m') ) {
                        int LA5_25 = input.LA(5);

                        if ( (LA5_25=='_') ) {
                            int LA5_28 = input.LA(6);

                            if ( (LA5_28=='b') ) {
                                alt5=5;
                            }
                            else if ( (LA5_28=='e') ) {
                                alt5=6;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 5, 28, input);

                                throw nvae;

                            }
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 25, input);

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
                alt5=7;
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
            int LA5_5 = input.LA(2);

            if ( (LA5_5=='n') ) {
                int LA5_18 = input.LA(3);

                if ( (LA5_18=='c') ) {
                    alt5=8;
                }
                else if ( (LA5_18=='s') ) {
                    alt5=9;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 18, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 5, input);

                throw nvae;

            }
            }
            break;
        case 'l':
            {
            alt5=10;
            }
            break;
        case 'N':
            {
            alt5=11;
            }
            break;
        case 'S':
            {
            alt5=12;
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
            int LA5_11 = input.LA(2);

            if ( (LA5_11=='\n') ) {
                int LA5_12 = input.LA(3);

                if ( ((LA5_12 >= '\t' && LA5_12 <= '\n')||LA5_12=='\r'||LA5_12==' ') ) {
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
            int LA5_12 = input.LA(2);

            if ( ((LA5_12 >= '\t' && LA5_12 <= '\n')||LA5_12=='\r'||LA5_12==' ') ) {
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
                // INSInterpreter.g:1:38: FRMB
                {
                mFRMB(); 


                }
                break;
            case 6 :
                // INSInterpreter.g:1:43: FRME
                {
                mFRME(); 


                }
                break;
            case 7 :
                // INSInterpreter.g:1:48: FST
                {
                mFST(); 


                }
                break;
            case 8 :
                // INSInterpreter.g:1:52: INCLD
                {
                mINCLD(); 


                }
                break;
            case 9 :
                // INSInterpreter.g:1:58: INS
                {
                mINS(); 


                }
                break;
            case 10 :
                // INSInterpreter.g:1:62: LST
                {
                mLST(); 


                }
                break;
            case 11 :
                // INSInterpreter.g:1:66: NUMTYP
                {
                mNUMTYP(); 


                }
                break;
            case 12 :
                // INSInterpreter.g:1:73: SYBTYP
                {
                mSYBTYP(); 


                }
                break;
            case 13 :
                // INSInterpreter.g:1:80: WRDTYP
                {
                mWRDTYP(); 


                }
                break;
            case 14 :
                // INSInterpreter.g:1:87: TOKEN
                {
                mTOKEN(); 


                }
                break;
            case 15 :
                // INSInterpreter.g:1:93: NEWLINE
                {
                mNEWLINE(); 


                }
                break;
            case 16 :
                // INSInterpreter.g:1:101: WS
                {
                mWS(); 


                }
                break;
            case 17 :
                // INSInterpreter.g:1:104: NUM
                {
                mNUM(); 


                }
                break;

        }

    }


 

}
