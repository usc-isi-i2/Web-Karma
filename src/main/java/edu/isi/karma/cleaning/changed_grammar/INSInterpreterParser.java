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
import edu.isi.karma.cleaning.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class INSInterpreterParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ANYNUM", "ANYTOK", "ANYTYP", "BNKTYP", "DIGIT", "FRMB", "FRME", "FST", "INCLD", "INS", "LST", "NEWLINE", "NUM", "NUMTYP", "SYBTYP", "TOKEN", "WRDTYP", "WS"
    };

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
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public INSInterpreterParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public INSInterpreterParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return INSInterpreterParser.tokenNames; }
    public String getGrammarFileName() { return "INSInterpreter.g"; }


    /*Used to store all the parameter got from the grammar*/
    HashMap parameters = new HashMap();


    public static class rule_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "rule"
    // INSInterpreter.g:39:1: rule : operator what dest ;
    public final INSInterpreterParser.rule_return rule() throws RecognitionException {
        INSInterpreterParser.rule_return retval = new INSInterpreterParser.rule_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        INSInterpreterParser.operator_return operator1 =null;

        INSInterpreterParser.what_return what2 =null;

        INSInterpreterParser.dest_return dest3 =null;



        try {
            // INSInterpreter.g:39:6: ( operator what dest )
            // INSInterpreter.g:39:8: operator what dest
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_operator_in_rule160);
            operator1=operator();

            state._fsp--;

            adaptor.addChild(root_0, operator1.getTree());

            pushFollow(FOLLOW_what_in_rule162);
            what2=what();

            state._fsp--;

            adaptor.addChild(root_0, what2.getTree());

            pushFollow(FOLLOW_dest_in_rule164);
            dest3=dest();

            state._fsp--;

            adaptor.addChild(root_0, dest3.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "rule"


    public static class what_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "what"
    // INSInterpreter.g:41:1: what : tokenspec ;
    public final INSInterpreterParser.what_return what() throws RecognitionException {
        INSInterpreterParser.what_return retval = new INSInterpreterParser.what_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        INSInterpreterParser.tokenspec_return tokenspec4 =null;



        try {
            // INSInterpreter.g:41:6: ( tokenspec )
            // INSInterpreter.g:41:8: tokenspec
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_tokenspec_in_what173);
            tokenspec4=tokenspec();

            state._fsp--;

            adaptor.addChild(root_0, tokenspec4.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "what"


    public static class tokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tokenspec"
    // INSInterpreter.g:43:1: tokenspec : singletokenspec ( singletokenspec )* ;
    public final INSInterpreterParser.tokenspec_return tokenspec() throws RecognitionException {
        INSInterpreterParser.tokenspec_return retval = new INSInterpreterParser.tokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        INSInterpreterParser.singletokenspec_return singletokenspec5 =null;

        INSInterpreterParser.singletokenspec_return singletokenspec6 =null;



        try {
            // INSInterpreter.g:44:2: ( singletokenspec ( singletokenspec )* )
            // INSInterpreter.g:44:4: singletokenspec ( singletokenspec )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_singletokenspec_in_tokenspec184);
            singletokenspec5=singletokenspec();

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec5.getTree());

            // INSInterpreter.g:44:20: ( singletokenspec )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==BNKTYP||(LA1_0 >= NUMTYP && LA1_0 <= WRDTYP)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // INSInterpreter.g:44:21: singletokenspec
            	    {
            	    pushFollow(FOLLOW_singletokenspec_in_tokenspec187);
            	    singletokenspec6=singletokenspec();

            	    state._fsp--;

            	    adaptor.addChild(root_0, singletokenspec6.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "tokenspec"


    public static class dtokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dtokenspec"
    // INSInterpreter.g:45:1: dtokenspec : singletokenspec ( singletokenspec )* ;
    public final INSInterpreterParser.dtokenspec_return dtokenspec() throws RecognitionException {
        INSInterpreterParser.dtokenspec_return retval = new INSInterpreterParser.dtokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        INSInterpreterParser.singletokenspec_return singletokenspec7 =null;

        INSInterpreterParser.singletokenspec_return singletokenspec8 =null;



        try {
            // INSInterpreter.g:46:2: ( singletokenspec ( singletokenspec )* )
            // INSInterpreter.g:46:4: singletokenspec ( singletokenspec )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_singletokenspec_in_dtokenspec200);
            singletokenspec7=singletokenspec();

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec7.getTree());

            // INSInterpreter.g:46:20: ( singletokenspec )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==BNKTYP||(LA2_0 >= NUMTYP && LA2_0 <= WRDTYP)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // INSInterpreter.g:46:21: singletokenspec
            	    {
            	    pushFollow(FOLLOW_singletokenspec_in_dtokenspec203);
            	    singletokenspec8=singletokenspec();

            	    state._fsp--;

            	    adaptor.addChild(root_0, singletokenspec8.getTree());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "dtokenspec"


    public static class singletokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "singletokenspec"
    // INSInterpreter.g:47:1: singletokenspec : ( token | type );
    public final INSInterpreterParser.singletokenspec_return singletokenspec() throws RecognitionException {
        INSInterpreterParser.singletokenspec_return retval = new INSInterpreterParser.singletokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        INSInterpreterParser.token_return token9 =null;

        INSInterpreterParser.type_return type10 =null;



        try {
            // INSInterpreter.g:47:17: ( token | type )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==TOKEN) ) {
                alt3=1;
            }
            else if ( (LA3_0==BNKTYP||(LA3_0 >= NUMTYP && LA3_0 <= SYBTYP)||LA3_0==WRDTYP) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // INSInterpreter.g:47:19: token
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_token_in_singletokenspec214);
                    token9=token();

                    state._fsp--;

                    adaptor.addChild(root_0, token9.getTree());

                    }
                    break;
                case 2 :
                    // INSInterpreter.g:47:25: type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_type_in_singletokenspec216);
                    type10=type();

                    state._fsp--;

                    adaptor.addChild(root_0, type10.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "singletokenspec"


    public static class type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "type"
    // INSInterpreter.g:49:1: type : ( NUMTYP | WRDTYP | SYBTYP | BNKTYP );
    public final INSInterpreterParser.type_return type() throws RecognitionException {
        INSInterpreterParser.type_return retval = new INSInterpreterParser.type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set11=null;

        Object set11_tree=null;

        try {
            // INSInterpreter.g:49:6: ( NUMTYP | WRDTYP | SYBTYP | BNKTYP )
            // INSInterpreter.g:
            {
            root_0 = (Object)adaptor.nil();


            set11=(Token)input.LT(1);

            if ( input.LA(1)==BNKTYP||(input.LA(1) >= NUMTYP && input.LA(1) <= SYBTYP)||input.LA(1)==WRDTYP ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set11)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "type"


    public static class operator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "operator"
    // INSInterpreter.g:51:1: operator : INS ;
    public final INSInterpreterParser.operator_return operator() throws RecognitionException {
        INSInterpreterParser.operator_return retval = new INSInterpreterParser.operator_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token INS12=null;

        Object INS12_tree=null;

        try {
            // INSInterpreter.g:51:10: ( INS )
            // INSInterpreter.g:51:12: INS
            {
            root_0 = (Object)adaptor.nil();


            INS12=(Token)match(input,INS,FOLLOW_INS_in_operator242); 
            INS12_tree = 
            (Object)adaptor.create(INS12)
            ;
            adaptor.addChild(root_0, INS12_tree);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "operator"


    public static class token_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "token"
    // INSInterpreter.g:54:1: token : TOKEN ;
    public final INSInterpreterParser.token_return token() throws RecognitionException {
        INSInterpreterParser.token_return retval = new INSInterpreterParser.token_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TOKEN13=null;

        Object TOKEN13_tree=null;

        try {
            // INSInterpreter.g:54:7: ( TOKEN )
            // INSInterpreter.g:54:9: TOKEN
            {
            root_0 = (Object)adaptor.nil();


            TOKEN13=(Token)match(input,TOKEN,FOLLOW_TOKEN_in_token252); 
            TOKEN13_tree = 
            (Object)adaptor.create(TOKEN13)
            ;
            adaptor.addChild(root_0, TOKEN13_tree);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "token"


    public static class scanningOrder_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "scanningOrder"
    // INSInterpreter.g:57:1: scanningOrder : ( FRMB | FRME );
    public final INSInterpreterParser.scanningOrder_return scanningOrder() throws RecognitionException {
        INSInterpreterParser.scanningOrder_return retval = new INSInterpreterParser.scanningOrder_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set14=null;

        Object set14_tree=null;

        try {
            // INSInterpreter.g:58:2: ( FRMB | FRME )
            // INSInterpreter.g:
            {
            root_0 = (Object)adaptor.nil();


            set14=(Token)input.LT(1);

            if ( (input.LA(1) >= FRMB && input.LA(1) <= FRME) ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set14)
                );
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "scanningOrder"


    public static class dest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dest"
    // INSInterpreter.g:60:1: dest : scanningOrder dwherequantifier ;
    public final INSInterpreterParser.dest_return dest() throws RecognitionException {
        INSInterpreterParser.dest_return retval = new INSInterpreterParser.dest_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        INSInterpreterParser.scanningOrder_return scanningOrder15 =null;

        INSInterpreterParser.dwherequantifier_return dwherequantifier16 =null;



        try {
            // INSInterpreter.g:60:6: ( scanningOrder dwherequantifier )
            // INSInterpreter.g:60:9: scanningOrder dwherequantifier
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_scanningOrder_in_dest278);
            scanningOrder15=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder15.getTree());

            pushFollow(FOLLOW_dwherequantifier_in_dest280);
            dwherequantifier16=dwherequantifier();

            state._fsp--;

            adaptor.addChild(root_0, dwherequantifier16.getTree());

            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "dest"


    public static class dwherequantifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dwherequantifier"
    // INSInterpreter.g:63:1: dwherequantifier : ( FST ( INCLD )? dtokenspec | LST ( INCLD )? dtokenspec | dnum );
    public final INSInterpreterParser.dwherequantifier_return dwherequantifier() throws RecognitionException {
        INSInterpreterParser.dwherequantifier_return retval = new INSInterpreterParser.dwherequantifier_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FST17=null;
        Token INCLD18=null;
        Token LST20=null;
        Token INCLD21=null;
        INSInterpreterParser.dtokenspec_return dtokenspec19 =null;

        INSInterpreterParser.dtokenspec_return dtokenspec22 =null;

        INSInterpreterParser.dnum_return dnum23 =null;


        Object FST17_tree=null;
        Object INCLD18_tree=null;
        Object LST20_tree=null;
        Object INCLD21_tree=null;

        try {
            // INSInterpreter.g:64:2: ( FST ( INCLD )? dtokenspec | LST ( INCLD )? dtokenspec | dnum )
            int alt6=3;
            switch ( input.LA(1) ) {
            case FST:
                {
                alt6=1;
                }
                break;
            case LST:
                {
                alt6=2;
                }
                break;
            case NUM:
                {
                alt6=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }

            switch (alt6) {
                case 1 :
                    // INSInterpreter.g:64:4: FST ( INCLD )? dtokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    FST17=(Token)match(input,FST,FOLLOW_FST_in_dwherequantifier292); 
                    FST17_tree = 
                    (Object)adaptor.create(FST17)
                    ;
                    adaptor.addChild(root_0, FST17_tree);


                    // INSInterpreter.g:64:8: ( INCLD )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==INCLD) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // INSInterpreter.g:64:8: INCLD
                            {
                            INCLD18=(Token)match(input,INCLD,FOLLOW_INCLD_in_dwherequantifier294); 
                            INCLD18_tree = 
                            (Object)adaptor.create(INCLD18)
                            ;
                            adaptor.addChild(root_0, INCLD18_tree);


                            }
                            break;

                    }


                    pushFollow(FOLLOW_dtokenspec_in_dwherequantifier297);
                    dtokenspec19=dtokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, dtokenspec19.getTree());

                    }
                    break;
                case 2 :
                    // INSInterpreter.g:64:27: LST ( INCLD )? dtokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    LST20=(Token)match(input,LST,FOLLOW_LST_in_dwherequantifier300); 
                    LST20_tree = 
                    (Object)adaptor.create(LST20)
                    ;
                    adaptor.addChild(root_0, LST20_tree);


                    // INSInterpreter.g:64:31: ( INCLD )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);

                    if ( (LA5_0==INCLD) ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // INSInterpreter.g:64:31: INCLD
                            {
                            INCLD21=(Token)match(input,INCLD,FOLLOW_INCLD_in_dwherequantifier302); 
                            INCLD21_tree = 
                            (Object)adaptor.create(INCLD21)
                            ;
                            adaptor.addChild(root_0, INCLD21_tree);


                            }
                            break;

                    }


                    pushFollow(FOLLOW_dtokenspec_in_dwherequantifier305);
                    dtokenspec22=dtokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, dtokenspec22.getTree());

                    }
                    break;
                case 3 :
                    // INSInterpreter.g:64:50: dnum
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_dnum_in_dwherequantifier308);
                    dnum23=dnum();

                    state._fsp--;

                    adaptor.addChild(root_0, dnum23.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "dwherequantifier"


    public static class dnum_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dnum"
    // INSInterpreter.g:66:1: dnum : NUM ;
    public final INSInterpreterParser.dnum_return dnum() throws RecognitionException {
        INSInterpreterParser.dnum_return retval = new INSInterpreterParser.dnum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM24=null;

        Object NUM24_tree=null;

        try {
            // INSInterpreter.g:66:6: ( NUM )
            // INSInterpreter.g:66:8: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM24=(Token)match(input,NUM,FOLLOW_NUM_in_dnum317); 
            NUM24_tree = 
            (Object)adaptor.create(NUM24)
            ;
            adaptor.addChild(root_0, NUM24_tree);


            }

            retval.stop = input.LT(-1);


            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "dnum"

    // Delegated rules


 

    public static final BitSet FOLLOW_operator_in_rule160 = new BitSet(new long[]{0x00000000001E0080L});
    public static final BitSet FOLLOW_what_in_rule162 = new BitSet(new long[]{0x0000000000000600L});
    public static final BitSet FOLLOW_dest_in_rule164 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tokenspec_in_what173 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec184 = new BitSet(new long[]{0x00000000001E0082L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec187 = new BitSet(new long[]{0x00000000001E0082L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec200 = new BitSet(new long[]{0x00000000001E0082L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec203 = new BitSet(new long[]{0x00000000001E0082L});
    public static final BitSet FOLLOW_token_in_singletokenspec214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_singletokenspec216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INS_in_operator242 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_in_token252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_dest278 = new BitSet(new long[]{0x0000000000014800L});
    public static final BitSet FOLLOW_dwherequantifier_in_dest280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_dwherequantifier292 = new BitSet(new long[]{0x00000000001E1080L});
    public static final BitSet FOLLOW_INCLD_in_dwherequantifier294 = new BitSet(new long[]{0x00000000001E0080L});
    public static final BitSet FOLLOW_dtokenspec_in_dwherequantifier297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LST_in_dwherequantifier300 = new BitSet(new long[]{0x00000000001E1080L});
    public static final BitSet FOLLOW_INCLD_in_dwherequantifier302 = new BitSet(new long[]{0x00000000001E0080L});
    public static final BitSet FOLLOW_dtokenspec_in_dwherequantifier305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dnum_in_dwherequantifier308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_dnum317 = new BitSet(new long[]{0x0000000000000002L});

}
