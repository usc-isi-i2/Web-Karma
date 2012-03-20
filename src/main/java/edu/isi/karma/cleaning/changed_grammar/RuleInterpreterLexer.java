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
// $ANTLR 3.4 RuleInterpreter.g 2012-02-13 14:41:38
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
    public static final int FRMB=11;
    public static final int FRME=12;
    public static final int FST=13;
    public static final int INCLD=14;
    public static final int LST=15;
    public static final int NEWLINE=16;
    public static final int NUM=17;
    public static final int NUMTYP=18;
    public static final int SYBTYP=19;
    public static final int TOKEN=20;
    public static final int WRDTYP=21;
    public static final int WS=22;

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

    // $ANTLR start "FRMB"
    public final void mFRMB() throws RecognitionException {
        try {
            int _type = FRMB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // RuleInterpreter.g:8:6: ( 'from_beginning' )
            // RuleInterpreter.g:8:8: 'from_beginning'
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
            // RuleInterpreter.g:9:6: ( 'from_end' )
            // RuleInterpreter.g:9:8: 'from_end'
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
            // RuleInterpreter.g:10:5: ( 'first' )
            // RuleInterpreter.g:10:7: 'first'
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
            // RuleInterpreter.g:11:7: ( 'incld' )
            // RuleInterpreter.g:11:9: 'incld'
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
            // RuleInterpreter.g:12:5: ( 'last' )
            // RuleInterpreter.g:12:7: 'last'
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
            // RuleInterpreter.g:13:8: ( 'Number' )
            // RuleInterpreter.g:13:10: 'Number'
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
            // RuleInterpreter.g:14:8: ( 'Symbol' )
            // RuleInterpreter.g:14:10: 'Symbol'
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
            // RuleInterpreter.g:15:8: ( 'Word' )
            // RuleInterpreter.g:15:10: 'Word'
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
            // RuleInterpreter.g:73:7: ( '\\\"' ( . )+ '\\\"' )
            // RuleInterpreter.g:73:9: '\\\"' ( . )+ '\\\"'
            {
            match('\"'); 

            // RuleInterpreter.g:73:13: ( . )+
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
            	    // RuleInterpreter.g:73:13: .
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
            // RuleInterpreter.g:75:8: ( ( '\\r' )? '\\n' )
            // RuleInterpreter.g:75:10: ( '\\r' )? '\\n'
            {
            // RuleInterpreter.g:75:10: ( '\\r' )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='\r') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // RuleInterpreter.g:75:10: '\\r'
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
            // RuleInterpreter.g:77:3: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // RuleInterpreter.g:77:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // RuleInterpreter.g:77:5: ( ' ' | '\\t' | '\\r' | '\\n' )+
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
            // RuleInterpreter.g:79:5: ( ( DIGIT )+ )
            // RuleInterpreter.g:79:7: ( DIGIT )+
            {
            // RuleInterpreter.g:79:7: ( DIGIT )+
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
            // RuleInterpreter.g:81:2: ( '0' .. '9' )
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
        // RuleInterpreter.g:1:8: ( ANYNUM | ANYTOK | ANYTOKS | ANYTYP | BNKTYP | DEL | FRMB | FRME | FST | INCLD | LST | NUMTYP | SYBTYP | WRDTYP | TOKEN | NEWLINE | WS | NUM )
        int alt5=18;
        switch ( input.LA(1) ) {
        case 'a':
            {
            int LA5_1 = input.LA(2);

            if ( (LA5_1=='n') ) {
                int LA5_16 = input.LA(3);

                if ( (LA5_16=='y') ) {
                    int LA5_21 = input.LA(4);

                    if ( (LA5_21=='n') ) {
                        alt5=1;
                    }
                    else if ( (LA5_21=='t') ) {
                        alt5=3;
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
                int LA5_17 = input.LA(3);

                if ( (LA5_17=='Y') ) {
                    int LA5_22 = input.LA(4);

                    if ( (LA5_22=='T') ) {
                        int LA5_26 = input.LA(5);

                        if ( (LA5_26=='O') ) {
                            alt5=2;
                        }
                        else if ( (LA5_26=='Y') ) {
                            alt5=4;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 26, input);

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
                        new NoViableAltException("", 5, 17, input);

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
        case 'f':
            {
            int LA5_5 = input.LA(2);

            if ( (LA5_5=='r') ) {
                int LA5_18 = input.LA(3);

                if ( (LA5_18=='o') ) {
                    int LA5_23 = input.LA(4);

                    if ( (LA5_23=='m') ) {
                        int LA5_27 = input.LA(5);

                        if ( (LA5_27=='_') ) {
                            int LA5_30 = input.LA(6);

                            if ( (LA5_30=='b') ) {
                                alt5=7;
                            }
                            else if ( (LA5_30=='e') ) {
                                alt5=8;
                            }
                            else {
                                NoViableAltException nvae =
                                    new NoViableAltException("", 5, 30, input);

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
                            new NoViableAltException("", 5, 23, input);

                        throw nvae;

                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 18, input);

                    throw nvae;

                }
            }
            else if ( (LA5_5=='i') ) {
                alt5=9;
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
            alt5=10;
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
            alt5=13;
            }
            break;
        case 'W':
            {
            alt5=14;
            }
            break;
        case '\"':
            {
            alt5=15;
            }
            break;
        case '\r':
            {
            int LA5_12 = input.LA(2);

            if ( (LA5_12=='\n') ) {
                int LA5_13 = input.LA(3);

                if ( ((LA5_13 >= '\t' && LA5_13 <= '\n')||LA5_13=='\r'||LA5_13==' ') ) {
                    alt5=17;
                }
                else {
                    alt5=16;
                }
            }
            else {
                alt5=17;
            }
            }
            break;
        case '\n':
            {
            int LA5_13 = input.LA(2);

            if ( ((LA5_13 >= '\t' && LA5_13 <= '\n')||LA5_13=='\r'||LA5_13==' ') ) {
                alt5=17;
            }
            else {
                alt5=16;
            }
            }
            break;
        case '\t':
        case ' ':
            {
            alt5=17;
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
            alt5=18;
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
                // RuleInterpreter.g:1:50: FRMB
                {
                mFRMB(); 


                }
                break;
            case 8 :
                // RuleInterpreter.g:1:55: FRME
                {
                mFRME(); 


                }
                break;
            case 9 :
                // RuleInterpreter.g:1:60: FST
                {
                mFST(); 


                }
                break;
            case 10 :
                // RuleInterpreter.g:1:64: INCLD
                {
                mINCLD(); 


                }
                break;
            case 11 :
                // RuleInterpreter.g:1:70: LST
                {
                mLST(); 


                }
                break;
            case 12 :
                // RuleInterpreter.g:1:74: NUMTYP
                {
                mNUMTYP(); 


                }
                break;
            case 13 :
                // RuleInterpreter.g:1:81: SYBTYP
                {
                mSYBTYP(); 


                }
                break;
            case 14 :
                // RuleInterpreter.g:1:88: WRDTYP
                {
                mWRDTYP(); 


                }
                break;
            case 15 :
                // RuleInterpreter.g:1:95: TOKEN
                {
                mTOKEN(); 


                }
                break;
            case 16 :
                // RuleInterpreter.g:1:101: NEWLINE
                {
                mNEWLINE(); 


                }
                break;
            case 17 :
                // RuleInterpreter.g:1:109: WS
                {
                mWS(); 


                }
                break;
            case 18 :
                // RuleInterpreter.g:1:112: NUM
                {
                mNUM(); 


                }
                break;

        }

    }


 

}
