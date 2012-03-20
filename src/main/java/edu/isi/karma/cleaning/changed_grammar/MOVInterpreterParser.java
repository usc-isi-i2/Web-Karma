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
// $ANTLR 3.4 MOVInterpreter.g 2012-02-13 14:41:39

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
public class MOVInterpreterParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ANYTOKS", "BNKTYP", "DIGIT", "FRMB", "FRME", "FST", "INCLD", "LST", "MOV", "NEWLINE", "NUM", "NUMTYP", "SYBTYP", "TOKEN", "WRDTYP", "WS"
    };

    public static final int EOF=-1;
    public static final int ANYTOKS=4;
    public static final int BNKTYP=5;
    public static final int DIGIT=6;
    public static final int FRMB=7;
    public static final int FRME=8;
    public static final int FST=9;
    public static final int INCLD=10;
    public static final int LST=11;
    public static final int MOV=12;
    public static final int NEWLINE=13;
    public static final int NUM=14;
    public static final int NUMTYP=15;
    public static final int SYBTYP=16;
    public static final int TOKEN=17;
    public static final int WRDTYP=18;
    public static final int WS=19;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public MOVInterpreterParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public MOVInterpreterParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return MOVInterpreterParser.tokenNames; }
    public String getGrammarFileName() { return "MOVInterpreter.g"; }


    /*Used to store all the parameter got from the grammar*/
    HashMap parameters = new HashMap();


    public static class rule_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "rule"
    // MOVInterpreter.g:36:1: rule : operator what where dest ;
    public final MOVInterpreterParser.rule_return rule() throws RecognitionException {
        MOVInterpreterParser.rule_return retval = new MOVInterpreterParser.rule_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.operator_return operator1 =null;

        MOVInterpreterParser.what_return what2 =null;

        MOVInterpreterParser.where_return where3 =null;

        MOVInterpreterParser.dest_return dest4 =null;



        try {
            // MOVInterpreter.g:36:6: ( operator what where dest )
            // MOVInterpreter.g:36:8: operator what where dest
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_operator_in_rule141);
            operator1=operator();

            state._fsp--;

            adaptor.addChild(root_0, operator1.getTree());

            pushFollow(FOLLOW_what_in_rule143);
            what2=what();

            state._fsp--;

            adaptor.addChild(root_0, what2.getTree());

            pushFollow(FOLLOW_where_in_rule145);
            where3=where();

            state._fsp--;

            adaptor.addChild(root_0, where3.getTree());

            pushFollow(FOLLOW_dest_in_rule147);
            dest4=dest();

            state._fsp--;

            adaptor.addChild(root_0, dest4.getTree());

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
    // MOVInterpreter.g:37:1: what : ( tokenspec | ANYTOKS );
    public final MOVInterpreterParser.what_return what() throws RecognitionException {
        MOVInterpreterParser.what_return retval = new MOVInterpreterParser.what_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ANYTOKS6=null;
        MOVInterpreterParser.tokenspec_return tokenspec5 =null;


        Object ANYTOKS6_tree=null;

        try {
            // MOVInterpreter.g:37:6: ( tokenspec | ANYTOKS )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==BNKTYP||(LA1_0 >= NUMTYP && LA1_0 <= WRDTYP)) ) {
                alt1=1;
            }
            else if ( (LA1_0==ANYTOKS) ) {
                alt1=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;

            }
            switch (alt1) {
                case 1 :
                    // MOVInterpreter.g:37:8: tokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_tokenspec_in_what154);
                    tokenspec5=tokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, tokenspec5.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:37:18: ANYTOKS
                    {
                    root_0 = (Object)adaptor.nil();


                    ANYTOKS6=(Token)match(input,ANYTOKS,FOLLOW_ANYTOKS_in_what156); 
                    ANYTOKS6_tree = 
                    (Object)adaptor.create(ANYTOKS6)
                    ;
                    adaptor.addChild(root_0, ANYTOKS6_tree);


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
    // $ANTLR end "what"


    public static class tokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tokenspec"
    // MOVInterpreter.g:40:1: tokenspec : ( singletokenspec | singletokenspec tokenspec );
    public final MOVInterpreterParser.tokenspec_return tokenspec() throws RecognitionException {
        MOVInterpreterParser.tokenspec_return retval = new MOVInterpreterParser.tokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.singletokenspec_return singletokenspec7 =null;

        MOVInterpreterParser.singletokenspec_return singletokenspec8 =null;

        MOVInterpreterParser.tokenspec_return tokenspec9 =null;



        try {
            // MOVInterpreter.g:40:11: ( singletokenspec | singletokenspec tokenspec )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==TOKEN) ) {
                int LA2_1 = input.LA(2);

                if ( ((LA2_1 >= FRMB && LA2_1 <= FRME)) ) {
                    alt2=1;
                }
                else if ( (LA2_1==BNKTYP||(LA2_1 >= NUMTYP && LA2_1 <= WRDTYP)) ) {
                    alt2=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA2_0==BNKTYP||(LA2_0 >= NUMTYP && LA2_0 <= SYBTYP)||LA2_0==WRDTYP) ) {
                int LA2_2 = input.LA(2);

                if ( ((LA2_2 >= FRMB && LA2_2 <= FRME)) ) {
                    alt2=1;
                }
                else if ( (LA2_2==BNKTYP||(LA2_2 >= NUMTYP && LA2_2 <= WRDTYP)) ) {
                    alt2=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 2, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }
            switch (alt2) {
                case 1 :
                    // MOVInterpreter.g:40:13: singletokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_tokenspec166);
                    singletokenspec7=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec7.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:40:30: singletokenspec tokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_tokenspec169);
                    singletokenspec8=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec8.getTree());

                    pushFollow(FOLLOW_tokenspec_in_tokenspec171);
                    tokenspec9=tokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, tokenspec9.getTree());

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
    // $ANTLR end "tokenspec"


    public static class stokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "stokenspec"
    // MOVInterpreter.g:41:1: stokenspec : ( singletokenspec | singletokenspec stokenspec );
    public final MOVInterpreterParser.stokenspec_return stokenspec() throws RecognitionException {
        MOVInterpreterParser.stokenspec_return retval = new MOVInterpreterParser.stokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.singletokenspec_return singletokenspec10 =null;

        MOVInterpreterParser.singletokenspec_return singletokenspec11 =null;

        MOVInterpreterParser.stokenspec_return stokenspec12 =null;



        try {
            // MOVInterpreter.g:41:12: ( singletokenspec | singletokenspec stokenspec )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==TOKEN) ) {
                int LA3_1 = input.LA(2);

                if ( ((LA3_1 >= FRMB && LA3_1 <= FRME)) ) {
                    alt3=1;
                }
                else if ( (LA3_1==BNKTYP||(LA3_1 >= NUMTYP && LA3_1 <= WRDTYP)) ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA3_0==BNKTYP||(LA3_0 >= NUMTYP && LA3_0 <= SYBTYP)||LA3_0==WRDTYP) ) {
                int LA3_2 = input.LA(2);

                if ( ((LA3_2 >= FRMB && LA3_2 <= FRME)) ) {
                    alt3=1;
                }
                else if ( (LA3_2==BNKTYP||(LA3_2 >= NUMTYP && LA3_2 <= WRDTYP)) ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 2, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // MOVInterpreter.g:41:14: singletokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_stokenspec178);
                    singletokenspec10=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec10.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:41:31: singletokenspec stokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_stokenspec181);
                    singletokenspec11=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec11.getTree());

                    pushFollow(FOLLOW_stokenspec_in_stokenspec183);
                    stokenspec12=stokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, stokenspec12.getTree());

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
    // $ANTLR end "stokenspec"


    public static class etokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "etokenspec"
    // MOVInterpreter.g:42:1: etokenspec : ( singletokenspec | singletokenspec etokenspec );
    public final MOVInterpreterParser.etokenspec_return etokenspec() throws RecognitionException {
        MOVInterpreterParser.etokenspec_return retval = new MOVInterpreterParser.etokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.singletokenspec_return singletokenspec13 =null;

        MOVInterpreterParser.singletokenspec_return singletokenspec14 =null;

        MOVInterpreterParser.etokenspec_return etokenspec15 =null;



        try {
            // MOVInterpreter.g:42:12: ( singletokenspec | singletokenspec etokenspec )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==TOKEN) ) {
                int LA4_1 = input.LA(2);

                if ( ((LA4_1 >= FRMB && LA4_1 <= FRME)) ) {
                    alt4=1;
                }
                else if ( (LA4_1==BNKTYP||(LA4_1 >= NUMTYP && LA4_1 <= WRDTYP)) ) {
                    alt4=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA4_0==BNKTYP||(LA4_0 >= NUMTYP && LA4_0 <= SYBTYP)||LA4_0==WRDTYP) ) {
                int LA4_2 = input.LA(2);

                if ( ((LA4_2 >= FRMB && LA4_2 <= FRME)) ) {
                    alt4=1;
                }
                else if ( (LA4_2==BNKTYP||(LA4_2 >= NUMTYP && LA4_2 <= WRDTYP)) ) {
                    alt4=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 2, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }
            switch (alt4) {
                case 1 :
                    // MOVInterpreter.g:42:14: singletokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_etokenspec190);
                    singletokenspec13=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec13.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:42:31: singletokenspec etokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_etokenspec193);
                    singletokenspec14=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec14.getTree());

                    pushFollow(FOLLOW_etokenspec_in_etokenspec195);
                    etokenspec15=etokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, etokenspec15.getTree());

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
    // $ANTLR end "etokenspec"


    public static class dtokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dtokenspec"
    // MOVInterpreter.g:43:1: dtokenspec : ( singletokenspec | singletokenspec dtokenspec );
    public final MOVInterpreterParser.dtokenspec_return dtokenspec() throws RecognitionException {
        MOVInterpreterParser.dtokenspec_return retval = new MOVInterpreterParser.dtokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.singletokenspec_return singletokenspec16 =null;

        MOVInterpreterParser.singletokenspec_return singletokenspec17 =null;

        MOVInterpreterParser.dtokenspec_return dtokenspec18 =null;



        try {
            // MOVInterpreter.g:43:12: ( singletokenspec | singletokenspec dtokenspec )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==TOKEN) ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1==EOF) ) {
                    alt5=1;
                }
                else if ( (LA5_1==BNKTYP||(LA5_1 >= NUMTYP && LA5_1 <= WRDTYP)) ) {
                    alt5=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 1, input);

                    throw nvae;

                }
            }
            else if ( (LA5_0==BNKTYP||(LA5_0 >= NUMTYP && LA5_0 <= SYBTYP)||LA5_0==WRDTYP) ) {
                int LA5_2 = input.LA(2);

                if ( (LA5_2==EOF) ) {
                    alt5=1;
                }
                else if ( (LA5_2==BNKTYP||(LA5_2 >= NUMTYP && LA5_2 <= WRDTYP)) ) {
                    alt5=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 2, input);

                    throw nvae;

                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // MOVInterpreter.g:43:14: singletokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_dtokenspec202);
                    singletokenspec16=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec16.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:43:31: singletokenspec dtokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_dtokenspec205);
                    singletokenspec17=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec17.getTree());

                    pushFollow(FOLLOW_dtokenspec_in_dtokenspec207);
                    dtokenspec18=dtokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, dtokenspec18.getTree());

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
    // $ANTLR end "dtokenspec"


    public static class singletokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "singletokenspec"
    // MOVInterpreter.g:44:1: singletokenspec : ( token | type );
    public final MOVInterpreterParser.singletokenspec_return singletokenspec() throws RecognitionException {
        MOVInterpreterParser.singletokenspec_return retval = new MOVInterpreterParser.singletokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.token_return token19 =null;

        MOVInterpreterParser.type_return type20 =null;



        try {
            // MOVInterpreter.g:44:17: ( token | type )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==TOKEN) ) {
                alt6=1;
            }
            else if ( (LA6_0==BNKTYP||(LA6_0 >= NUMTYP && LA6_0 <= SYBTYP)||LA6_0==WRDTYP) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }
            switch (alt6) {
                case 1 :
                    // MOVInterpreter.g:44:19: token
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_token_in_singletokenspec214);
                    token19=token();

                    state._fsp--;

                    adaptor.addChild(root_0, token19.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:44:25: type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_type_in_singletokenspec216);
                    type20=type();

                    state._fsp--;

                    adaptor.addChild(root_0, type20.getTree());

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


    public static class operator_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "operator"
    // MOVInterpreter.g:47:1: operator : MOV ;
    public final MOVInterpreterParser.operator_return operator() throws RecognitionException {
        MOVInterpreterParser.operator_return retval = new MOVInterpreterParser.operator_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token MOV21=null;

        Object MOV21_tree=null;

        try {
            // MOVInterpreter.g:47:10: ( MOV )
            // MOVInterpreter.g:47:12: MOV
            {
            root_0 = (Object)adaptor.nil();


            MOV21=(Token)match(input,MOV,FOLLOW_MOV_in_operator225); 
            MOV21_tree = 
            (Object)adaptor.create(MOV21)
            ;
            adaptor.addChild(root_0, MOV21_tree);


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


    public static class type_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "type"
    // MOVInterpreter.g:50:1: type : ( NUMTYP | WRDTYP | SYBTYP | BNKTYP );
    public final MOVInterpreterParser.type_return type() throws RecognitionException {
        MOVInterpreterParser.type_return retval = new MOVInterpreterParser.type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set22=null;

        Object set22_tree=null;

        try {
            // MOVInterpreter.g:50:7: ( NUMTYP | WRDTYP | SYBTYP | BNKTYP )
            // MOVInterpreter.g:
            {
            root_0 = (Object)adaptor.nil();


            set22=(Token)input.LT(1);

            if ( input.LA(1)==BNKTYP||(input.LA(1) >= NUMTYP && input.LA(1) <= SYBTYP)||input.LA(1)==WRDTYP ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set22)
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


    public static class token_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "token"
    // MOVInterpreter.g:52:1: token : TOKEN ;
    public final MOVInterpreterParser.token_return token() throws RecognitionException {
        MOVInterpreterParser.token_return retval = new MOVInterpreterParser.token_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TOKEN23=null;

        Object TOKEN23_tree=null;

        try {
            // MOVInterpreter.g:52:8: ( TOKEN )
            // MOVInterpreter.g:52:10: TOKEN
            {
            root_0 = (Object)adaptor.nil();


            TOKEN23=(Token)match(input,TOKEN,FOLLOW_TOKEN_in_token252); 
            TOKEN23_tree = 
            (Object)adaptor.create(TOKEN23)
            ;
            adaptor.addChild(root_0, TOKEN23_tree);


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


    public static class where_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "where"
    // MOVInterpreter.g:54:1: where : start end ;
    public final MOVInterpreterParser.where_return where() throws RecognitionException {
        MOVInterpreterParser.where_return retval = new MOVInterpreterParser.where_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.start_return start24 =null;

        MOVInterpreterParser.end_return end25 =null;



        try {
            // MOVInterpreter.g:54:8: ( start end )
            // MOVInterpreter.g:54:10: start end
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_start_in_where262);
            start24=start();

            state._fsp--;

            adaptor.addChild(root_0, start24.getTree());

            pushFollow(FOLLOW_end_in_where264);
            end25=end();

            state._fsp--;

            adaptor.addChild(root_0, end25.getTree());

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
    // $ANTLR end "where"


    public static class scanningOrder_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "scanningOrder"
    // MOVInterpreter.g:56:1: scanningOrder : ( FRMB | FRME );
    public final MOVInterpreterParser.scanningOrder_return scanningOrder() throws RecognitionException {
        MOVInterpreterParser.scanningOrder_return retval = new MOVInterpreterParser.scanningOrder_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set26=null;

        Object set26_tree=null;

        try {
            // MOVInterpreter.g:56:15: ( FRMB | FRME )
            // MOVInterpreter.g:
            {
            root_0 = (Object)adaptor.nil();


            set26=(Token)input.LT(1);

            if ( (input.LA(1) >= FRMB && input.LA(1) <= FRME) ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set26)
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


    public static class start_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "start"
    // MOVInterpreter.g:58:1: start : scanningOrder swherequantifier ;
    public final MOVInterpreterParser.start_return start() throws RecognitionException {
        MOVInterpreterParser.start_return retval = new MOVInterpreterParser.start_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.scanningOrder_return scanningOrder27 =null;

        MOVInterpreterParser.swherequantifier_return swherequantifier28 =null;



        try {
            // MOVInterpreter.g:58:8: ( scanningOrder swherequantifier )
            // MOVInterpreter.g:58:11: scanningOrder swherequantifier
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_scanningOrder_in_start285);
            scanningOrder27=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder27.getTree());

            pushFollow(FOLLOW_swherequantifier_in_start287);
            swherequantifier28=swherequantifier();

            state._fsp--;

            adaptor.addChild(root_0, swherequantifier28.getTree());

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
    // $ANTLR end "start"


    public static class end_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "end"
    // MOVInterpreter.g:59:1: end : scanningOrder ewherequantifier ;
    public final MOVInterpreterParser.end_return end() throws RecognitionException {
        MOVInterpreterParser.end_return retval = new MOVInterpreterParser.end_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.scanningOrder_return scanningOrder29 =null;

        MOVInterpreterParser.ewherequantifier_return ewherequantifier30 =null;



        try {
            // MOVInterpreter.g:59:6: ( scanningOrder ewherequantifier )
            // MOVInterpreter.g:59:9: scanningOrder ewherequantifier
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_scanningOrder_in_end296);
            scanningOrder29=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder29.getTree());

            pushFollow(FOLLOW_ewherequantifier_in_end298);
            ewherequantifier30=ewherequantifier();

            state._fsp--;

            adaptor.addChild(root_0, ewherequantifier30.getTree());

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
    // $ANTLR end "end"


    public static class dest_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dest"
    // MOVInterpreter.g:60:1: dest : scanningOrder dwherequantifier ;
    public final MOVInterpreterParser.dest_return dest() throws RecognitionException {
        MOVInterpreterParser.dest_return retval = new MOVInterpreterParser.dest_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        MOVInterpreterParser.scanningOrder_return scanningOrder31 =null;

        MOVInterpreterParser.dwherequantifier_return dwherequantifier32 =null;



        try {
            // MOVInterpreter.g:60:7: ( scanningOrder dwherequantifier )
            // MOVInterpreter.g:60:10: scanningOrder dwherequantifier
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_scanningOrder_in_dest307);
            scanningOrder31=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder31.getTree());

            pushFollow(FOLLOW_dwherequantifier_in_dest309);
            dwherequantifier32=dwherequantifier();

            state._fsp--;

            adaptor.addChild(root_0, dwherequantifier32.getTree());

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
    // MOVInterpreter.g:61:1: dwherequantifier : ( FST ( INCLD )? dtokenspec | dnum );
    public final MOVInterpreterParser.dwherequantifier_return dwherequantifier() throws RecognitionException {
        MOVInterpreterParser.dwherequantifier_return retval = new MOVInterpreterParser.dwherequantifier_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FST33=null;
        Token INCLD34=null;
        MOVInterpreterParser.dtokenspec_return dtokenspec35 =null;

        MOVInterpreterParser.dnum_return dnum36 =null;


        Object FST33_tree=null;
        Object INCLD34_tree=null;

        try {
            // MOVInterpreter.g:61:18: ( FST ( INCLD )? dtokenspec | dnum )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==FST) ) {
                alt8=1;
            }
            else if ( (LA8_0==NUM) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }
            switch (alt8) {
                case 1 :
                    // MOVInterpreter.g:61:20: FST ( INCLD )? dtokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    FST33=(Token)match(input,FST,FOLLOW_FST_in_dwherequantifier316); 
                    FST33_tree = 
                    (Object)adaptor.create(FST33)
                    ;
                    adaptor.addChild(root_0, FST33_tree);


                    // MOVInterpreter.g:61:24: ( INCLD )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==INCLD) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // MOVInterpreter.g:61:24: INCLD
                            {
                            INCLD34=(Token)match(input,INCLD,FOLLOW_INCLD_in_dwherequantifier318); 
                            INCLD34_tree = 
                            (Object)adaptor.create(INCLD34)
                            ;
                            adaptor.addChild(root_0, INCLD34_tree);


                            }
                            break;

                    }


                    pushFollow(FOLLOW_dtokenspec_in_dwherequantifier321);
                    dtokenspec35=dtokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, dtokenspec35.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:61:42: dnum
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_dnum_in_dwherequantifier323);
                    dnum36=dnum();

                    state._fsp--;

                    adaptor.addChild(root_0, dnum36.getTree());

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


    public static class swherequantifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "swherequantifier"
    // MOVInterpreter.g:62:1: swherequantifier : ( FST ( INCLD )? stokenspec | snum );
    public final MOVInterpreterParser.swherequantifier_return swherequantifier() throws RecognitionException {
        MOVInterpreterParser.swherequantifier_return retval = new MOVInterpreterParser.swherequantifier_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FST37=null;
        Token INCLD38=null;
        MOVInterpreterParser.stokenspec_return stokenspec39 =null;

        MOVInterpreterParser.snum_return snum40 =null;


        Object FST37_tree=null;
        Object INCLD38_tree=null;

        try {
            // MOVInterpreter.g:62:18: ( FST ( INCLD )? stokenspec | snum )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==FST) ) {
                alt10=1;
            }
            else if ( (LA10_0==NUM) ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;

            }
            switch (alt10) {
                case 1 :
                    // MOVInterpreter.g:62:20: FST ( INCLD )? stokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    FST37=(Token)match(input,FST,FOLLOW_FST_in_swherequantifier330); 
                    FST37_tree = 
                    (Object)adaptor.create(FST37)
                    ;
                    adaptor.addChild(root_0, FST37_tree);


                    // MOVInterpreter.g:62:24: ( INCLD )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==INCLD) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // MOVInterpreter.g:62:24: INCLD
                            {
                            INCLD38=(Token)match(input,INCLD,FOLLOW_INCLD_in_swherequantifier332); 
                            INCLD38_tree = 
                            (Object)adaptor.create(INCLD38)
                            ;
                            adaptor.addChild(root_0, INCLD38_tree);


                            }
                            break;

                    }


                    pushFollow(FOLLOW_stokenspec_in_swherequantifier335);
                    stokenspec39=stokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, stokenspec39.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:62:42: snum
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_snum_in_swherequantifier337);
                    snum40=snum();

                    state._fsp--;

                    adaptor.addChild(root_0, snum40.getTree());

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
    // $ANTLR end "swherequantifier"


    public static class ewherequantifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "ewherequantifier"
    // MOVInterpreter.g:63:1: ewherequantifier : ( FST ( INCLD )? etokenspec | tnum );
    public final MOVInterpreterParser.ewherequantifier_return ewherequantifier() throws RecognitionException {
        MOVInterpreterParser.ewherequantifier_return retval = new MOVInterpreterParser.ewherequantifier_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FST41=null;
        Token INCLD42=null;
        MOVInterpreterParser.etokenspec_return etokenspec43 =null;

        MOVInterpreterParser.tnum_return tnum44 =null;


        Object FST41_tree=null;
        Object INCLD42_tree=null;

        try {
            // MOVInterpreter.g:63:18: ( FST ( INCLD )? etokenspec | tnum )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==FST) ) {
                alt12=1;
            }
            else if ( (LA12_0==NUM) ) {
                alt12=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;

            }
            switch (alt12) {
                case 1 :
                    // MOVInterpreter.g:63:20: FST ( INCLD )? etokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    FST41=(Token)match(input,FST,FOLLOW_FST_in_ewherequantifier344); 
                    FST41_tree = 
                    (Object)adaptor.create(FST41)
                    ;
                    adaptor.addChild(root_0, FST41_tree);


                    // MOVInterpreter.g:63:24: ( INCLD )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==INCLD) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // MOVInterpreter.g:63:24: INCLD
                            {
                            INCLD42=(Token)match(input,INCLD,FOLLOW_INCLD_in_ewherequantifier346); 
                            INCLD42_tree = 
                            (Object)adaptor.create(INCLD42)
                            ;
                            adaptor.addChild(root_0, INCLD42_tree);


                            }
                            break;

                    }


                    pushFollow(FOLLOW_etokenspec_in_ewherequantifier349);
                    etokenspec43=etokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, etokenspec43.getTree());

                    }
                    break;
                case 2 :
                    // MOVInterpreter.g:63:42: tnum
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_tnum_in_ewherequantifier351);
                    tnum44=tnum();

                    state._fsp--;

                    adaptor.addChild(root_0, tnum44.getTree());

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
    // $ANTLR end "ewherequantifier"


    public static class snum_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "snum"
    // MOVInterpreter.g:64:1: snum : NUM ;
    public final MOVInterpreterParser.snum_return snum() throws RecognitionException {
        MOVInterpreterParser.snum_return retval = new MOVInterpreterParser.snum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM45=null;

        Object NUM45_tree=null;

        try {
            // MOVInterpreter.g:64:8: ( NUM )
            // MOVInterpreter.g:64:10: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM45=(Token)match(input,NUM,FOLLOW_NUM_in_snum360); 
            NUM45_tree = 
            (Object)adaptor.create(NUM45)
            ;
            adaptor.addChild(root_0, NUM45_tree);


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
    // $ANTLR end "snum"


    public static class tnum_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tnum"
    // MOVInterpreter.g:65:1: tnum : NUM ;
    public final MOVInterpreterParser.tnum_return tnum() throws RecognitionException {
        MOVInterpreterParser.tnum_return retval = new MOVInterpreterParser.tnum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM46=null;

        Object NUM46_tree=null;

        try {
            // MOVInterpreter.g:65:8: ( NUM )
            // MOVInterpreter.g:65:10: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM46=(Token)match(input,NUM,FOLLOW_NUM_in_tnum369); 
            NUM46_tree = 
            (Object)adaptor.create(NUM46)
            ;
            adaptor.addChild(root_0, NUM46_tree);


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
    // $ANTLR end "tnum"


    public static class dnum_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dnum"
    // MOVInterpreter.g:66:1: dnum : NUM ;
    public final MOVInterpreterParser.dnum_return dnum() throws RecognitionException {
        MOVInterpreterParser.dnum_return retval = new MOVInterpreterParser.dnum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM47=null;

        Object NUM47_tree=null;

        try {
            // MOVInterpreter.g:66:8: ( NUM )
            // MOVInterpreter.g:66:10: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM47=(Token)match(input,NUM,FOLLOW_NUM_in_dnum378); 
            NUM47_tree = 
            (Object)adaptor.create(NUM47)
            ;
            adaptor.addChild(root_0, NUM47_tree);


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


 

    public static final BitSet FOLLOW_operator_in_rule141 = new BitSet(new long[]{0x0000000000078030L});
    public static final BitSet FOLLOW_what_in_rule143 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_where_in_rule145 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_dest_in_rule147 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tokenspec_in_what154 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANYTOKS_in_what156 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec169 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_tokenspec_in_tokenspec171 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec178 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec181 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_stokenspec_in_stokenspec183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec193 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_etokenspec_in_etokenspec195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec205 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_dtokenspec_in_dtokenspec207 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_token_in_singletokenspec214 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_singletokenspec216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOV_in_operator225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_in_token252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_start_in_where262 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_end_in_where264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_start285 = new BitSet(new long[]{0x0000000000004200L});
    public static final BitSet FOLLOW_swherequantifier_in_start287 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_end296 = new BitSet(new long[]{0x0000000000004200L});
    public static final BitSet FOLLOW_ewherequantifier_in_end298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_dest307 = new BitSet(new long[]{0x0000000000004200L});
    public static final BitSet FOLLOW_dwherequantifier_in_dest309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_dwherequantifier316 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_dwherequantifier318 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_dtokenspec_in_dwherequantifier321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dnum_in_dwherequantifier323 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_swherequantifier330 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_swherequantifier332 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_stokenspec_in_swherequantifier335 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_snum_in_swherequantifier337 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_ewherequantifier344 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_ewherequantifier346 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_etokenspec_in_ewherequantifier349 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tnum_in_ewherequantifier351 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_snum360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_tnum369 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_dnum378 = new BitSet(new long[]{0x0000000000000002L});

}
