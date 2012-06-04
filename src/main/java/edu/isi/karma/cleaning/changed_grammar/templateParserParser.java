// $ANTLR 3.4 templateParser.g 2012-06-03 21:17:10

package edu.isi.karma.cleaning.changed_grammar;
import edu.isi.karma.cleaning.*;
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
public class templateParserParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ANYNUM", "ANYTOK", "BNKTYP", "DIGIT", "ENDTYP", "FRMB", "FRME", "FST", "INCLD", "LST", "NEWLINE", "NUM", "NUMTYP", "SRTTYP", "SYBTYP", "TOKEN", "WRDTYP", "WS"
    };

    public static final int EOF=-1;
    public static final int ANYNUM=4;
    public static final int ANYTOK=5;
    public static final int BNKTYP=6;
    public static final int DIGIT=7;
    public static final int ENDTYP=8;
    public static final int FRMB=9;
    public static final int FRME=10;
    public static final int FST=11;
    public static final int INCLD=12;
    public static final int LST=13;
    public static final int NEWLINE=14;
    public static final int NUM=15;
    public static final int NUMTYP=16;
    public static final int SRTTYP=17;
    public static final int SYBTYP=18;
    public static final int TOKEN=19;
    public static final int WRDTYP=20;
    public static final int WS=21;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public templateParserParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public templateParserParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return templateParserParser.tokenNames; }
    public String getGrammarFileName() { return "templateParser.g"; }


    public static class qnum_return extends ParserRuleReturnScope {
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "qnum"
    // templateParser.g:34:1: qnum returns [GrammarTreeNode tok] : NUM ;
    public final templateParserParser.qnum_return qnum() throws RecognitionException {
        templateParserParser.qnum_return retval = new templateParserParser.qnum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM1=null;

        Object NUM1_tree=null;

        retval.tok = new GrammarTreeNode("qnum");
        try {
            // templateParser.g:36:2: ( NUM )
            // templateParser.g:36:4: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM1=(Token)match(input,NUM,FOLLOW_NUM_in_qnum168); 
            NUM1_tree = 
            (Object)adaptor.create(NUM1)
            ;
            adaptor.addChild(root_0, NUM1_tree);


            GrammarTreeNode gtn = new GrammarTreeNode((NUM1!=null?NUM1.getText():null));retval.tok.addChild(gtn);

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
    // $ANTLR end "qnum"


    public static class snum_return extends ParserRuleReturnScope {
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "snum"
    // templateParser.g:38:1: snum returns [GrammarTreeNode tok] : NUM ;
    public final templateParserParser.snum_return snum() throws RecognitionException {
        templateParserParser.snum_return retval = new templateParserParser.snum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM2=null;

        Object NUM2_tree=null;

        retval.tok = new GrammarTreeNode("snum");
        try {
            // templateParser.g:40:2: ( NUM )
            // templateParser.g:40:4: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM2=(Token)match(input,NUM,FOLLOW_NUM_in_snum187); 
            NUM2_tree = 
            (Object)adaptor.create(NUM2)
            ;
            adaptor.addChild(root_0, NUM2_tree);


            GrammarTreeNode gtn = new GrammarTreeNode((NUM2!=null?NUM2.getText():null));retval.tok.addChild(gtn);

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
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tnum"
    // templateParser.g:41:1: tnum returns [GrammarTreeNode tok] : NUM ;
    public final templateParserParser.tnum_return tnum() throws RecognitionException {
        templateParserParser.tnum_return retval = new templateParserParser.tnum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM3=null;

        Object NUM3_tree=null;

        retval.tok = new GrammarTreeNode("tnum");
        try {
            // templateParser.g:43:2: ( NUM )
            // templateParser.g:43:4: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM3=(Token)match(input,NUM,FOLLOW_NUM_in_tnum205); 
            NUM3_tree = 
            (Object)adaptor.create(NUM3)
            ;
            adaptor.addChild(root_0, NUM3_tree);


            GrammarTreeNode gtn = new GrammarTreeNode((NUM3!=null?NUM3.getText():null));retval.tok.addChild(gtn);

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
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dnum"
    // templateParser.g:44:1: dnum returns [GrammarTreeNode tok] : NUM ;
    public final templateParserParser.dnum_return dnum() throws RecognitionException {
        templateParserParser.dnum_return retval = new templateParserParser.dnum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM4=null;

        Object NUM4_tree=null;

        retval.tok = new GrammarTreeNode("dnum");
        try {
            // templateParser.g:46:2: ( NUM )
            // templateParser.g:46:4: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM4=(Token)match(input,NUM,FOLLOW_NUM_in_dnum223); 
            NUM4_tree = 
            (Object)adaptor.create(NUM4)
            ;
            adaptor.addChild(root_0, NUM4_tree);


            GrammarTreeNode gtn = new GrammarTreeNode((NUM4!=null?NUM4.getText():null));retval.tok.addChild(gtn);

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


    public static class tokenspec_return extends ParserRuleReturnScope {
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tokenspec"
    // templateParser.g:48:1: tokenspec returns [GrammarTreeNode tok] : t= singletokenspec (s= singletokenspec )* ;
    public final templateParserParser.tokenspec_return tokenspec() throws RecognitionException {
        templateParserParser.tokenspec_return retval = new templateParserParser.tokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        templateParserParser.singletokenspec_return t =null;

        templateParserParser.singletokenspec_return s =null;



        retval.tok = new GrammarTreeNode("tokenspec");
        try {
            // templateParser.g:50:3: (t= singletokenspec (s= singletokenspec )* )
            // templateParser.g:50:3: t= singletokenspec (s= singletokenspec )*
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_singletokenspec_in_tokenspec244);
            t=singletokenspec();

            state._fsp--;

            adaptor.addChild(root_0, t.getTree());

            retval.tok.addChild((t!=null?t.tok:null));

            // templateParser.g:50:45: (s= singletokenspec )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==BNKTYP||LA1_0==ENDTYP||(LA1_0 >= NUMTYP && LA1_0 <= WRDTYP)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // templateParser.g:50:46: s= singletokenspec
            	    {
            	    pushFollow(FOLLOW_singletokenspec_in_tokenspec250);
            	    s=singletokenspec();

            	    state._fsp--;

            	    adaptor.addChild(root_0, s.getTree());

            	    retval.tok.addChild((s!=null?s.tok:null));

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


    public static class stokenspec_return extends ParserRuleReturnScope {
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "stokenspec"
    // templateParser.g:52:1: stokenspec returns [GrammarTreeNode tok] : ( ANYTOK |t= singletokenspec (s= singletokenspec )* );
    public final templateParserParser.stokenspec_return stokenspec() throws RecognitionException {
        templateParserParser.stokenspec_return retval = new templateParserParser.stokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ANYTOK5=null;
        templateParserParser.singletokenspec_return t =null;

        templateParserParser.singletokenspec_return s =null;


        Object ANYTOK5_tree=null;

        retval.tok = new GrammarTreeNode("stokenspec");
        try {
            // templateParser.g:54:3: ( ANYTOK |t= singletokenspec (s= singletokenspec )* )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ANYTOK) ) {
                alt3=1;
            }
            else if ( (LA3_0==BNKTYP||LA3_0==ENDTYP||(LA3_0 >= NUMTYP && LA3_0 <= WRDTYP)) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // templateParser.g:54:3: ANYTOK
                    {
                    root_0 = (Object)adaptor.nil();


                    ANYTOK5=(Token)match(input,ANYTOK,FOLLOW_ANYTOK_in_stokenspec272); 
                    ANYTOK5_tree = 
                    (Object)adaptor.create(ANYTOK5)
                    ;
                    adaptor.addChild(root_0, ANYTOK5_tree);


                    GrammarTreeNode gtn = new GrammarTreeNode((ANYTOK5!=null?ANYTOK5.getText():null));retval.tok.addChild(gtn);

                    }
                    break;
                case 2 :
                    // templateParser.g:54:88: t= singletokenspec (s= singletokenspec )*
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_stokenspec278);
                    t=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, t.getTree());

                    retval.tok.addChild((t!=null?t.tok:null));

                    // templateParser.g:54:130: (s= singletokenspec )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( (LA2_0==BNKTYP||LA2_0==ENDTYP||(LA2_0 >= NUMTYP && LA2_0 <= WRDTYP)) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // templateParser.g:54:131: s= singletokenspec
                    	    {
                    	    pushFollow(FOLLOW_singletokenspec_in_stokenspec284);
                    	    s=singletokenspec();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, s.getTree());

                    	    retval.tok.addChild((s!=null?s.tok:null));

                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);


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
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "etokenspec"
    // templateParser.g:55:1: etokenspec returns [GrammarTreeNode tok] : ( ANYTOK |t= singletokenspec (s= singletokenspec )* );
    public final templateParserParser.etokenspec_return etokenspec() throws RecognitionException {
        templateParserParser.etokenspec_return retval = new templateParserParser.etokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ANYTOK6=null;
        templateParserParser.singletokenspec_return t =null;

        templateParserParser.singletokenspec_return s =null;


        Object ANYTOK6_tree=null;

        retval.tok = new GrammarTreeNode("etokenspec");
        try {
            // templateParser.g:57:3: ( ANYTOK |t= singletokenspec (s= singletokenspec )* )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==ANYTOK) ) {
                alt5=1;
            }
            else if ( (LA5_0==BNKTYP||LA5_0==ENDTYP||(LA5_0 >= NUMTYP && LA5_0 <= WRDTYP)) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // templateParser.g:57:3: ANYTOK
                    {
                    root_0 = (Object)adaptor.nil();


                    ANYTOK6=(Token)match(input,ANYTOK,FOLLOW_ANYTOK_in_etokenspec305); 
                    ANYTOK6_tree = 
                    (Object)adaptor.create(ANYTOK6)
                    ;
                    adaptor.addChild(root_0, ANYTOK6_tree);


                    GrammarTreeNode gtn = new GrammarTreeNode((ANYTOK6!=null?ANYTOK6.getText():null));retval.tok.addChild(gtn);

                    }
                    break;
                case 2 :
                    // templateParser.g:57:88: t= singletokenspec (s= singletokenspec )*
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_etokenspec311);
                    t=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, t.getTree());

                    retval.tok.addChild((t!=null?t.tok:null));

                    // templateParser.g:57:130: (s= singletokenspec )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==BNKTYP||LA4_0==ENDTYP||(LA4_0 >= NUMTYP && LA4_0 <= WRDTYP)) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // templateParser.g:57:131: s= singletokenspec
                    	    {
                    	    pushFollow(FOLLOW_singletokenspec_in_etokenspec317);
                    	    s=singletokenspec();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, s.getTree());

                    	    retval.tok.addChild((s!=null?s.tok:null));

                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


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
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dtokenspec"
    // templateParser.g:58:1: dtokenspec returns [GrammarTreeNode tok] : ( ANYTOK |t= singletokenspec (s= singletokenspec )* );
    public final templateParserParser.dtokenspec_return dtokenspec() throws RecognitionException {
        templateParserParser.dtokenspec_return retval = new templateParserParser.dtokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ANYTOK7=null;
        templateParserParser.singletokenspec_return t =null;

        templateParserParser.singletokenspec_return s =null;


        Object ANYTOK7_tree=null;

        retval.tok = new GrammarTreeNode("dtokenspec");
        try {
            // templateParser.g:60:3: ( ANYTOK |t= singletokenspec (s= singletokenspec )* )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==ANYTOK) ) {
                alt7=1;
            }
            else if ( (LA7_0==BNKTYP||LA7_0==ENDTYP||(LA7_0 >= NUMTYP && LA7_0 <= WRDTYP)) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }
            switch (alt7) {
                case 1 :
                    // templateParser.g:60:3: ANYTOK
                    {
                    root_0 = (Object)adaptor.nil();


                    ANYTOK7=(Token)match(input,ANYTOK,FOLLOW_ANYTOK_in_dtokenspec338); 
                    ANYTOK7_tree = 
                    (Object)adaptor.create(ANYTOK7)
                    ;
                    adaptor.addChild(root_0, ANYTOK7_tree);


                    GrammarTreeNode gtn = new GrammarTreeNode((ANYTOK7!=null?ANYTOK7.getText():null));retval.tok.addChild(gtn);

                    }
                    break;
                case 2 :
                    // templateParser.g:60:88: t= singletokenspec (s= singletokenspec )*
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_dtokenspec344);
                    t=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, t.getTree());

                    retval.tok.addChild((t!=null?t.tok:null));

                    // templateParser.g:60:130: (s= singletokenspec )*
                    loop6:
                    do {
                        int alt6=2;
                        int LA6_0 = input.LA(1);

                        if ( (LA6_0==BNKTYP||LA6_0==ENDTYP||(LA6_0 >= NUMTYP && LA6_0 <= WRDTYP)) ) {
                            alt6=1;
                        }


                        switch (alt6) {
                    	case 1 :
                    	    // templateParser.g:60:131: s= singletokenspec
                    	    {
                    	    pushFollow(FOLLOW_singletokenspec_in_dtokenspec350);
                    	    s=singletokenspec();

                    	    state._fsp--;

                    	    adaptor.addChild(root_0, s.getTree());

                    	    retval.tok.addChild((s!=null?s.tok:null));

                    	    }
                    	    break;

                    	default :
                    	    break loop6;
                        }
                    } while (true);


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
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "singletokenspec"
    // templateParser.g:62:1: singletokenspec returns [GrammarTreeNode tok] : ( token | type );
    public final templateParserParser.singletokenspec_return singletokenspec() throws RecognitionException {
        templateParserParser.singletokenspec_return retval = new templateParserParser.singletokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        templateParserParser.token_return token8 =null;

        templateParserParser.type_return type9 =null;



        retval.tok = new GrammarTreeNode("singletokenspec");
        try {
            // templateParser.g:64:3: ( token | type )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==TOKEN) ) {
                alt8=1;
            }
            else if ( (LA8_0==BNKTYP||LA8_0==ENDTYP||(LA8_0 >= NUMTYP && LA8_0 <= SYBTYP)||LA8_0==WRDTYP) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }
            switch (alt8) {
                case 1 :
                    // templateParser.g:64:3: token
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_token_in_singletokenspec372);
                    token8=token();

                    state._fsp--;

                    adaptor.addChild(root_0, token8.getTree());

                    retval.tok.addChild((token8!=null?token8.tok:null));

                    }
                    break;
                case 2 :
                    // templateParser.g:64:37: type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_type_in_singletokenspec375);
                    type9=type();

                    state._fsp--;

                    adaptor.addChild(root_0, type9.getTree());

                    retval.tok.addChild((type9!=null?type9.tok:null));

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


    public static class token_return extends ParserRuleReturnScope {
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "token"
    // templateParser.g:66:1: token returns [GrammarTreeNode tok] : TOKEN ;
    public final templateParserParser.token_return token() throws RecognitionException {
        templateParserParser.token_return retval = new templateParserParser.token_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token TOKEN10=null;

        Object TOKEN10_tree=null;

        retval.tok = new GrammarTreeNode("token");
        try {
            // templateParser.g:68:3: ( TOKEN )
            // templateParser.g:68:3: TOKEN
            {
            root_0 = (Object)adaptor.nil();


            TOKEN10=(Token)match(input,TOKEN,FOLLOW_TOKEN_in_token394); 
            TOKEN10_tree = 
            (Object)adaptor.create(TOKEN10)
            ;
            adaptor.addChild(root_0, TOKEN10_tree);


            GrammarTreeNode gtn = new GrammarTreeNode((TOKEN10!=null?TOKEN10.getText():null));retval.tok.addChild(gtn);

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


    public static class type_return extends ParserRuleReturnScope {
        public GrammarTreeNode tok;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "type"
    // templateParser.g:70:1: type returns [GrammarTreeNode tok] : ( NUMTYP | WRDTYP | SYBTYP | BNKTYP | SRTTYP | ENDTYP );
    public final templateParserParser.type_return type() throws RecognitionException {
        templateParserParser.type_return retval = new templateParserParser.type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUMTYP11=null;
        Token WRDTYP12=null;
        Token SYBTYP13=null;
        Token BNKTYP14=null;
        Token SRTTYP15=null;
        Token ENDTYP16=null;

        Object NUMTYP11_tree=null;
        Object WRDTYP12_tree=null;
        Object SYBTYP13_tree=null;
        Object BNKTYP14_tree=null;
        Object SRTTYP15_tree=null;
        Object ENDTYP16_tree=null;

        retval.tok = new GrammarTreeNode("type");
        try {
            // templateParser.g:72:3: ( NUMTYP | WRDTYP | SYBTYP | BNKTYP | SRTTYP | ENDTYP )
            int alt9=6;
            switch ( input.LA(1) ) {
            case NUMTYP:
                {
                alt9=1;
                }
                break;
            case WRDTYP:
                {
                alt9=2;
                }
                break;
            case SYBTYP:
                {
                alt9=3;
                }
                break;
            case BNKTYP:
                {
                alt9=4;
                }
                break;
            case SRTTYP:
                {
                alt9=5;
                }
                break;
            case ENDTYP:
                {
                alt9=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }

            switch (alt9) {
                case 1 :
                    // templateParser.g:72:3: NUMTYP
                    {
                    root_0 = (Object)adaptor.nil();


                    NUMTYP11=(Token)match(input,NUMTYP,FOLLOW_NUMTYP_in_type414); 
                    NUMTYP11_tree = 
                    (Object)adaptor.create(NUMTYP11)
                    ;
                    adaptor.addChild(root_0, NUMTYP11_tree);


                    GrammarTreeNode gtn=new GrammarTreeNode((NUMTYP11!=null?NUMTYP11.getText():null));retval.tok.addChild(gtn);

                    }
                    break;
                case 2 :
                    // templateParser.g:72:86: WRDTYP
                    {
                    root_0 = (Object)adaptor.nil();


                    WRDTYP12=(Token)match(input,WRDTYP,FOLLOW_WRDTYP_in_type418); 
                    WRDTYP12_tree = 
                    (Object)adaptor.create(WRDTYP12)
                    ;
                    adaptor.addChild(root_0, WRDTYP12_tree);


                    GrammarTreeNode gtn=new GrammarTreeNode((WRDTYP12!=null?WRDTYP12.getText():null));retval.tok.addChild(gtn);

                    }
                    break;
                case 3 :
                    // templateParser.g:72:169: SYBTYP
                    {
                    root_0 = (Object)adaptor.nil();


                    SYBTYP13=(Token)match(input,SYBTYP,FOLLOW_SYBTYP_in_type422); 
                    SYBTYP13_tree = 
                    (Object)adaptor.create(SYBTYP13)
                    ;
                    adaptor.addChild(root_0, SYBTYP13_tree);


                    GrammarTreeNode gtn=new GrammarTreeNode((SYBTYP13!=null?SYBTYP13.getText():null));retval.tok.addChild(gtn);

                    }
                    break;
                case 4 :
                    // templateParser.g:72:252: BNKTYP
                    {
                    root_0 = (Object)adaptor.nil();


                    BNKTYP14=(Token)match(input,BNKTYP,FOLLOW_BNKTYP_in_type426); 
                    BNKTYP14_tree = 
                    (Object)adaptor.create(BNKTYP14)
                    ;
                    adaptor.addChild(root_0, BNKTYP14_tree);


                    GrammarTreeNode gtn=new GrammarTreeNode((BNKTYP14!=null?BNKTYP14.getText():null));retval.tok.addChild(gtn);

                    }
                    break;
                case 5 :
                    // templateParser.g:72:335: SRTTYP
                    {
                    root_0 = (Object)adaptor.nil();


                    SRTTYP15=(Token)match(input,SRTTYP,FOLLOW_SRTTYP_in_type430); 
                    SRTTYP15_tree = 
                    (Object)adaptor.create(SRTTYP15)
                    ;
                    adaptor.addChild(root_0, SRTTYP15_tree);


                    GrammarTreeNode gtn=new GrammarTreeNode((SRTTYP15!=null?SRTTYP15.getText():null));retval.tok.addChild(gtn);

                    }
                    break;
                case 6 :
                    // templateParser.g:72:418: ENDTYP
                    {
                    root_0 = (Object)adaptor.nil();


                    ENDTYP16=(Token)match(input,ENDTYP,FOLLOW_ENDTYP_in_type434); 
                    ENDTYP16_tree = 
                    (Object)adaptor.create(ENDTYP16)
                    ;
                    adaptor.addChild(root_0, ENDTYP16_tree);


                    GrammarTreeNode gtn=new GrammarTreeNode((ENDTYP16!=null?ENDTYP16.getText():null));retval.tok.addChild(gtn);

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
    // $ANTLR end "type"

    // Delegated rules


 

    public static final BitSet FOLLOW_NUM_in_qnum168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_snum187 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_tnum205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_dnum223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec244 = new BitSet(new long[]{0x00000000001F0142L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec250 = new BitSet(new long[]{0x00000000001F0142L});
    public static final BitSet FOLLOW_ANYTOK_in_stokenspec272 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec278 = new BitSet(new long[]{0x00000000001F0142L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec284 = new BitSet(new long[]{0x00000000001F0142L});
    public static final BitSet FOLLOW_ANYTOK_in_etokenspec305 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec311 = new BitSet(new long[]{0x00000000001F0142L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec317 = new BitSet(new long[]{0x00000000001F0142L});
    public static final BitSet FOLLOW_ANYTOK_in_dtokenspec338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec344 = new BitSet(new long[]{0x00000000001F0142L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec350 = new BitSet(new long[]{0x00000000001F0142L});
    public static final BitSet FOLLOW_token_in_singletokenspec372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_singletokenspec375 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_in_token394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMTYP_in_type414 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WRDTYP_in_type418 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYBTYP_in_type422 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BNKTYP_in_type426 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SRTTYP_in_type430 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ENDTYP_in_type434 = new BitSet(new long[]{0x0000000000000002L});

}