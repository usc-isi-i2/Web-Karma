// $ANTLR 3.4 RuleInterpreter.g 2012-06-03 21:17:08

package edu.isi.karma.cleaning.changed_grammar;
import java.util.HashMap;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class RuleInterpreterParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ANYNUM", "ANYTOK", "ANYTOKS", "ANYTYP", "BNKTYP", "DEL", "DIGIT", "ENDTYP", "FRMB", "FRME", "FST", "INCLD", "INS", "LST", "MOV", "NEWLINE", "NUM", "NUMTYP", "SRTTYP", "SYBTYP", "TOKEN", "WRDTYP", "WS"
    };

    public static final int EOF=-1;
    public static final int ANYNUM=4;
    public static final int ANYTOK=5;
    public static final int ANYTOKS=6;
    public static final int ANYTYP=7;
    public static final int BNKTYP=8;
    public static final int DEL=9;
    public static final int DIGIT=10;
    public static final int ENDTYP=11;
    public static final int FRMB=12;
    public static final int FRME=13;
    public static final int FST=14;
    public static final int INCLD=15;
    public static final int INS=16;
    public static final int LST=17;
    public static final int MOV=18;
    public static final int NEWLINE=19;
    public static final int NUM=20;
    public static final int NUMTYP=21;
    public static final int SRTTYP=22;
    public static final int SYBTYP=23;
    public static final int TOKEN=24;
    public static final int WRDTYP=25;
    public static final int WS=26;

    // delegates
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public RuleInterpreterParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public RuleInterpreterParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return RuleInterpreterParser.tokenNames; }
    public String getGrammarFileName() { return "RuleInterpreter.g"; }


    /*Used to store all the parameter got from the grammar*/
    HashMap parameters = new HashMap();


    public static class rule_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "rule"
    // RuleInterpreter.g:44:1: rule : operator what where ;
    public final RuleInterpreterParser.rule_return rule() throws RecognitionException {
        RuleInterpreterParser.rule_return retval = new RuleInterpreterParser.rule_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        RuleInterpreterParser.operator_return operator1 =null;

        RuleInterpreterParser.what_return what2 =null;

        RuleInterpreterParser.where_return where3 =null;



        try {
            // RuleInterpreter.g:44:6: ( operator what where )
            // RuleInterpreter.g:44:8: operator what where
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_operator_in_rule209);
            operator1=operator();

            state._fsp--;

            adaptor.addChild(root_0, operator1.getTree());

            pushFollow(FOLLOW_what_in_rule211);
            what2=what();

            state._fsp--;

            adaptor.addChild(root_0, what2.getTree());

            pushFollow(FOLLOW_where_in_rule213);
            where3=where();

            state._fsp--;

            adaptor.addChild(root_0, where3.getTree());

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
    // RuleInterpreter.g:45:1: what : ( quantifier tokenspec | ANYTOKS );
    public final RuleInterpreterParser.what_return what() throws RecognitionException {
        RuleInterpreterParser.what_return retval = new RuleInterpreterParser.what_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token ANYTOKS6=null;
        RuleInterpreterParser.quantifier_return quantifier4 =null;

        RuleInterpreterParser.tokenspec_return tokenspec5 =null;


        Object ANYTOKS6_tree=null;

        try {
            // RuleInterpreter.g:45:6: ( quantifier tokenspec | ANYTOKS )
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0==ANYNUM||LA1_0==NUM) ) {
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
                    // RuleInterpreter.g:45:8: quantifier tokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_quantifier_in_what220);
                    quantifier4=quantifier();

                    state._fsp--;

                    adaptor.addChild(root_0, quantifier4.getTree());

                    pushFollow(FOLLOW_tokenspec_in_what222);
                    tokenspec5=tokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, tokenspec5.getTree());

                    }
                    break;
                case 2 :
                    // RuleInterpreter.g:45:29: ANYTOKS
                    {
                    root_0 = (Object)adaptor.nil();


                    ANYTOKS6=(Token)match(input,ANYTOKS,FOLLOW_ANYTOKS_in_what224); 
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


    public static class quantifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "quantifier"
    // RuleInterpreter.g:46:1: quantifier : ( ANYNUM | NUM );
    public final RuleInterpreterParser.quantifier_return quantifier() throws RecognitionException {
        RuleInterpreterParser.quantifier_return retval = new RuleInterpreterParser.quantifier_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set7=null;

        Object set7_tree=null;

        try {
            // RuleInterpreter.g:46:12: ( ANYNUM | NUM )
            // RuleInterpreter.g:
            {
            root_0 = (Object)adaptor.nil();


            set7=(Token)input.LT(1);

            if ( input.LA(1)==ANYNUM||input.LA(1)==NUM ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set7)
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
    // $ANTLR end "quantifier"


    public static class tokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tokenspec"
    // RuleInterpreter.g:48:1: tokenspec : ( singletokenspec | singletokenspec tokenspec );
    public final RuleInterpreterParser.tokenspec_return tokenspec() throws RecognitionException {
        RuleInterpreterParser.tokenspec_return retval = new RuleInterpreterParser.tokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        RuleInterpreterParser.singletokenspec_return singletokenspec8 =null;

        RuleInterpreterParser.singletokenspec_return singletokenspec9 =null;

        RuleInterpreterParser.tokenspec_return tokenspec10 =null;



        try {
            // RuleInterpreter.g:48:12: ( singletokenspec | singletokenspec tokenspec )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ANYTOK||LA2_0==TOKEN) ) {
                int LA2_1 = input.LA(2);

                if ( ((LA2_1 >= FRMB && LA2_1 <= FRME)) ) {
                    alt2=1;
                }
                else if ( (LA2_1==ANYTOK||(LA2_1 >= ANYTYP && LA2_1 <= BNKTYP)||LA2_1==ENDTYP||(LA2_1 >= NUMTYP && LA2_1 <= WRDTYP)) ) {
                    alt2=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 1, input);

                    throw nvae;

                }
            }
            else if ( ((LA2_0 >= ANYTYP && LA2_0 <= BNKTYP)||LA2_0==ENDTYP||(LA2_0 >= NUMTYP && LA2_0 <= SYBTYP)||LA2_0==WRDTYP) ) {
                int LA2_2 = input.LA(2);

                if ( ((LA2_2 >= FRMB && LA2_2 <= FRME)) ) {
                    alt2=1;
                }
                else if ( (LA2_2==ANYTOK||(LA2_2 >= ANYTYP && LA2_2 <= BNKTYP)||LA2_2==ENDTYP||(LA2_2 >= NUMTYP && LA2_2 <= WRDTYP)) ) {
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
                    // RuleInterpreter.g:48:14: singletokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_tokenspec243);
                    singletokenspec8=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec8.getTree());

                    }
                    break;
                case 2 :
                    // RuleInterpreter.g:48:32: singletokenspec tokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_tokenspec247);
                    singletokenspec9=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec9.getTree());

                    pushFollow(FOLLOW_tokenspec_in_tokenspec249);
                    tokenspec10=tokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, tokenspec10.getTree());

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
    // RuleInterpreter.g:49:1: stokenspec : ( singletokenspec | singletokenspec stokenspec );
    public final RuleInterpreterParser.stokenspec_return stokenspec() throws RecognitionException {
        RuleInterpreterParser.stokenspec_return retval = new RuleInterpreterParser.stokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        RuleInterpreterParser.singletokenspec_return singletokenspec11 =null;

        RuleInterpreterParser.singletokenspec_return singletokenspec12 =null;

        RuleInterpreterParser.stokenspec_return stokenspec13 =null;



        try {
            // RuleInterpreter.g:49:12: ( singletokenspec | singletokenspec stokenspec )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==ANYTOK||LA3_0==TOKEN) ) {
                int LA3_1 = input.LA(2);

                if ( ((LA3_1 >= FRMB && LA3_1 <= FRME)) ) {
                    alt3=1;
                }
                else if ( (LA3_1==ANYTOK||(LA3_1 >= ANYTYP && LA3_1 <= BNKTYP)||LA3_1==ENDTYP||(LA3_1 >= NUMTYP && LA3_1 <= WRDTYP)) ) {
                    alt3=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;

                }
            }
            else if ( ((LA3_0 >= ANYTYP && LA3_0 <= BNKTYP)||LA3_0==ENDTYP||(LA3_0 >= NUMTYP && LA3_0 <= SYBTYP)||LA3_0==WRDTYP) ) {
                int LA3_2 = input.LA(2);

                if ( ((LA3_2 >= FRMB && LA3_2 <= FRME)) ) {
                    alt3=1;
                }
                else if ( (LA3_2==ANYTOK||(LA3_2 >= ANYTYP && LA3_2 <= BNKTYP)||LA3_2==ENDTYP||(LA3_2 >= NUMTYP && LA3_2 <= WRDTYP)) ) {
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
                    // RuleInterpreter.g:49:14: singletokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_stokenspec256);
                    singletokenspec11=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec11.getTree());

                    }
                    break;
                case 2 :
                    // RuleInterpreter.g:49:32: singletokenspec stokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_stokenspec260);
                    singletokenspec12=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec12.getTree());

                    pushFollow(FOLLOW_stokenspec_in_stokenspec262);
                    stokenspec13=stokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, stokenspec13.getTree());

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
    // RuleInterpreter.g:50:1: etokenspec : ( singletokenspec | singletokenspec etokenspec );
    public final RuleInterpreterParser.etokenspec_return etokenspec() throws RecognitionException {
        RuleInterpreterParser.etokenspec_return retval = new RuleInterpreterParser.etokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        RuleInterpreterParser.singletokenspec_return singletokenspec14 =null;

        RuleInterpreterParser.singletokenspec_return singletokenspec15 =null;

        RuleInterpreterParser.etokenspec_return etokenspec16 =null;



        try {
            // RuleInterpreter.g:50:13: ( singletokenspec | singletokenspec etokenspec )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==ANYTOK||LA4_0==TOKEN) ) {
                int LA4_1 = input.LA(2);

                if ( (LA4_1==EOF) ) {
                    alt4=1;
                }
                else if ( (LA4_1==ANYTOK||(LA4_1 >= ANYTYP && LA4_1 <= BNKTYP)||LA4_1==ENDTYP||(LA4_1 >= NUMTYP && LA4_1 <= WRDTYP)) ) {
                    alt4=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 4, 1, input);

                    throw nvae;

                }
            }
            else if ( ((LA4_0 >= ANYTYP && LA4_0 <= BNKTYP)||LA4_0==ENDTYP||(LA4_0 >= NUMTYP && LA4_0 <= SYBTYP)||LA4_0==WRDTYP) ) {
                int LA4_2 = input.LA(2);

                if ( (LA4_2==EOF) ) {
                    alt4=1;
                }
                else if ( (LA4_2==ANYTOK||(LA4_2 >= ANYTYP && LA4_2 <= BNKTYP)||LA4_2==ENDTYP||(LA4_2 >= NUMTYP && LA4_2 <= WRDTYP)) ) {
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
                    // RuleInterpreter.g:50:15: singletokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_etokenspec270);
                    singletokenspec14=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec14.getTree());

                    }
                    break;
                case 2 :
                    // RuleInterpreter.g:50:33: singletokenspec etokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_singletokenspec_in_etokenspec274);
                    singletokenspec15=singletokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, singletokenspec15.getTree());

                    pushFollow(FOLLOW_etokenspec_in_etokenspec276);
                    etokenspec16=etokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, etokenspec16.getTree());

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


    public static class singletokenspec_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "singletokenspec"
    // RuleInterpreter.g:52:1: singletokenspec : ( token | type );
    public final RuleInterpreterParser.singletokenspec_return singletokenspec() throws RecognitionException {
        RuleInterpreterParser.singletokenspec_return retval = new RuleInterpreterParser.singletokenspec_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        RuleInterpreterParser.token_return token17 =null;

        RuleInterpreterParser.type_return type18 =null;



        try {
            // RuleInterpreter.g:52:16: ( token | type )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==ANYTOK||LA5_0==TOKEN) ) {
                alt5=1;
            }
            else if ( ((LA5_0 >= ANYTYP && LA5_0 <= BNKTYP)||LA5_0==ENDTYP||(LA5_0 >= NUMTYP && LA5_0 <= SYBTYP)||LA5_0==WRDTYP) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // RuleInterpreter.g:52:18: token
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_token_in_singletokenspec283);
                    token17=token();

                    state._fsp--;

                    adaptor.addChild(root_0, token17.getTree());

                    }
                    break;
                case 2 :
                    // RuleInterpreter.g:52:24: type
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_type_in_singletokenspec285);
                    type18=type();

                    state._fsp--;

                    adaptor.addChild(root_0, type18.getTree());

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
    // RuleInterpreter.g:54:1: operator : DEL ;
    public final RuleInterpreterParser.operator_return operator() throws RecognitionException {
        RuleInterpreterParser.operator_return retval = new RuleInterpreterParser.operator_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token DEL19=null;

        Object DEL19_tree=null;

        try {
            // RuleInterpreter.g:54:9: ( DEL )
            // RuleInterpreter.g:54:11: DEL
            {
            root_0 = (Object)adaptor.nil();


            DEL19=(Token)match(input,DEL,FOLLOW_DEL_in_operator292); 
            DEL19_tree = 
            (Object)adaptor.create(DEL19)
            ;
            adaptor.addChild(root_0, DEL19_tree);


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
    // RuleInterpreter.g:57:1: type : ( ANYTYP | NUMTYP | WRDTYP | SYBTYP | BNKTYP | SRTTYP | ENDTYP );
    public final RuleInterpreterParser.type_return type() throws RecognitionException {
        RuleInterpreterParser.type_return retval = new RuleInterpreterParser.type_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set20=null;

        Object set20_tree=null;

        try {
            // RuleInterpreter.g:57:6: ( ANYTYP | NUMTYP | WRDTYP | SYBTYP | BNKTYP | SRTTYP | ENDTYP )
            // RuleInterpreter.g:
            {
            root_0 = (Object)adaptor.nil();


            set20=(Token)input.LT(1);

            if ( (input.LA(1) >= ANYTYP && input.LA(1) <= BNKTYP)||input.LA(1)==ENDTYP||(input.LA(1) >= NUMTYP && input.LA(1) <= SYBTYP)||input.LA(1)==WRDTYP ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set20)
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
    // RuleInterpreter.g:59:1: token : ( ANYTOK | TOKEN );
    public final RuleInterpreterParser.token_return token() throws RecognitionException {
        RuleInterpreterParser.token_return retval = new RuleInterpreterParser.token_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set21=null;

        Object set21_tree=null;

        try {
            // RuleInterpreter.g:59:6: ( ANYTOK | TOKEN )
            // RuleInterpreter.g:
            {
            root_0 = (Object)adaptor.nil();


            set21=(Token)input.LT(1);

            if ( input.LA(1)==ANYTOK||input.LA(1)==TOKEN ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set21)
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
    // $ANTLR end "token"


    public static class where_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "where"
    // RuleInterpreter.g:62:1: where : start end ;
    public final RuleInterpreterParser.where_return where() throws RecognitionException {
        RuleInterpreterParser.where_return retval = new RuleInterpreterParser.where_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        RuleInterpreterParser.start_return start22 =null;

        RuleInterpreterParser.end_return end23 =null;



        try {
            // RuleInterpreter.g:62:7: ( start end )
            // RuleInterpreter.g:62:9: start end
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_start_in_where337);
            start22=start();

            state._fsp--;

            adaptor.addChild(root_0, start22.getTree());

            pushFollow(FOLLOW_end_in_where339);
            end23=end();

            state._fsp--;

            adaptor.addChild(root_0, end23.getTree());

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
    // RuleInterpreter.g:64:1: scanningOrder : ( FRMB | FRME );
    public final RuleInterpreterParser.scanningOrder_return scanningOrder() throws RecognitionException {
        RuleInterpreterParser.scanningOrder_return retval = new RuleInterpreterParser.scanningOrder_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token set24=null;

        Object set24_tree=null;

        try {
            // RuleInterpreter.g:64:16: ( FRMB | FRME )
            // RuleInterpreter.g:
            {
            root_0 = (Object)adaptor.nil();


            set24=(Token)input.LT(1);

            if ( (input.LA(1) >= FRMB && input.LA(1) <= FRME) ) {
                input.consume();
                adaptor.addChild(root_0, 
                (Object)adaptor.create(set24)
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
    // RuleInterpreter.g:66:1: start : scanningOrder swherequantifier ;
    public final RuleInterpreterParser.start_return start() throws RecognitionException {
        RuleInterpreterParser.start_return retval = new RuleInterpreterParser.start_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        RuleInterpreterParser.scanningOrder_return scanningOrder25 =null;

        RuleInterpreterParser.swherequantifier_return swherequantifier26 =null;



        try {
            // RuleInterpreter.g:66:6: ( scanningOrder swherequantifier )
            // RuleInterpreter.g:66:9: scanningOrder swherequantifier
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_scanningOrder_in_start360);
            scanningOrder25=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder25.getTree());

            pushFollow(FOLLOW_swherequantifier_in_start362);
            swherequantifier26=swherequantifier();

            state._fsp--;

            adaptor.addChild(root_0, swherequantifier26.getTree());

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
    // RuleInterpreter.g:67:1: end : scanningOrder ewherequantifier ;
    public final RuleInterpreterParser.end_return end() throws RecognitionException {
        RuleInterpreterParser.end_return retval = new RuleInterpreterParser.end_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        RuleInterpreterParser.scanningOrder_return scanningOrder27 =null;

        RuleInterpreterParser.ewherequantifier_return ewherequantifier28 =null;



        try {
            // RuleInterpreter.g:67:4: ( scanningOrder ewherequantifier )
            // RuleInterpreter.g:67:7: scanningOrder ewherequantifier
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_scanningOrder_in_end369);
            scanningOrder27=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder27.getTree());

            pushFollow(FOLLOW_ewherequantifier_in_end371);
            ewherequantifier28=ewherequantifier();

            state._fsp--;

            adaptor.addChild(root_0, ewherequantifier28.getTree());

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


    public static class swherequantifier_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "swherequantifier"
    // RuleInterpreter.g:70:1: swherequantifier : ( FST ( INCLD )? stokenspec | snum );
    public final RuleInterpreterParser.swherequantifier_return swherequantifier() throws RecognitionException {
        RuleInterpreterParser.swherequantifier_return retval = new RuleInterpreterParser.swherequantifier_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FST29=null;
        Token INCLD30=null;
        RuleInterpreterParser.stokenspec_return stokenspec31 =null;

        RuleInterpreterParser.snum_return snum32 =null;


        Object FST29_tree=null;
        Object INCLD30_tree=null;

        try {
            // RuleInterpreter.g:70:18: ( FST ( INCLD )? stokenspec | snum )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==FST) ) {
                alt7=1;
            }
            else if ( (LA7_0==NUM) ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }
            switch (alt7) {
                case 1 :
                    // RuleInterpreter.g:70:20: FST ( INCLD )? stokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    FST29=(Token)match(input,FST,FOLLOW_FST_in_swherequantifier380); 
                    FST29_tree = 
                    (Object)adaptor.create(FST29)
                    ;
                    adaptor.addChild(root_0, FST29_tree);


                    // RuleInterpreter.g:70:24: ( INCLD )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==INCLD) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // RuleInterpreter.g:70:24: INCLD
                            {
                            INCLD30=(Token)match(input,INCLD,FOLLOW_INCLD_in_swherequantifier382); 
                            INCLD30_tree = 
                            (Object)adaptor.create(INCLD30)
                            ;
                            adaptor.addChild(root_0, INCLD30_tree);


                            }
                            break;

                    }


                    pushFollow(FOLLOW_stokenspec_in_swherequantifier385);
                    stokenspec31=stokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, stokenspec31.getTree());

                    }
                    break;
                case 2 :
                    // RuleInterpreter.g:70:43: snum
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_snum_in_swherequantifier388);
                    snum32=snum();

                    state._fsp--;

                    adaptor.addChild(root_0, snum32.getTree());

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
    // RuleInterpreter.g:71:1: ewherequantifier : ( FST ( INCLD )? etokenspec | tnum );
    public final RuleInterpreterParser.ewherequantifier_return ewherequantifier() throws RecognitionException {
        RuleInterpreterParser.ewherequantifier_return retval = new RuleInterpreterParser.ewherequantifier_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token FST33=null;
        Token INCLD34=null;
        RuleInterpreterParser.etokenspec_return etokenspec35 =null;

        RuleInterpreterParser.tnum_return tnum36 =null;


        Object FST33_tree=null;
        Object INCLD34_tree=null;

        try {
            // RuleInterpreter.g:71:18: ( FST ( INCLD )? etokenspec | tnum )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==FST) ) {
                alt9=1;
            }
            else if ( (LA9_0==NUM) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }
            switch (alt9) {
                case 1 :
                    // RuleInterpreter.g:71:20: FST ( INCLD )? etokenspec
                    {
                    root_0 = (Object)adaptor.nil();


                    FST33=(Token)match(input,FST,FOLLOW_FST_in_ewherequantifier395); 
                    FST33_tree = 
                    (Object)adaptor.create(FST33)
                    ;
                    adaptor.addChild(root_0, FST33_tree);


                    // RuleInterpreter.g:71:24: ( INCLD )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==INCLD) ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // RuleInterpreter.g:71:24: INCLD
                            {
                            INCLD34=(Token)match(input,INCLD,FOLLOW_INCLD_in_ewherequantifier397); 
                            INCLD34_tree = 
                            (Object)adaptor.create(INCLD34)
                            ;
                            adaptor.addChild(root_0, INCLD34_tree);


                            }
                            break;

                    }


                    pushFollow(FOLLOW_etokenspec_in_ewherequantifier400);
                    etokenspec35=etokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, etokenspec35.getTree());

                    }
                    break;
                case 2 :
                    // RuleInterpreter.g:71:43: tnum
                    {
                    root_0 = (Object)adaptor.nil();


                    pushFollow(FOLLOW_tnum_in_ewherequantifier403);
                    tnum36=tnum();

                    state._fsp--;

                    adaptor.addChild(root_0, tnum36.getTree());

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
    // RuleInterpreter.g:72:1: snum : NUM ;
    public final RuleInterpreterParser.snum_return snum() throws RecognitionException {
        RuleInterpreterParser.snum_return retval = new RuleInterpreterParser.snum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM37=null;

        Object NUM37_tree=null;

        try {
            // RuleInterpreter.g:72:6: ( NUM )
            // RuleInterpreter.g:72:8: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM37=(Token)match(input,NUM,FOLLOW_NUM_in_snum410); 
            NUM37_tree = 
            (Object)adaptor.create(NUM37)
            ;
            adaptor.addChild(root_0, NUM37_tree);


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
    // RuleInterpreter.g:73:1: tnum : NUM ;
    public final RuleInterpreterParser.tnum_return tnum() throws RecognitionException {
        RuleInterpreterParser.tnum_return retval = new RuleInterpreterParser.tnum_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token NUM38=null;

        Object NUM38_tree=null;

        try {
            // RuleInterpreter.g:73:6: ( NUM )
            // RuleInterpreter.g:73:8: NUM
            {
            root_0 = (Object)adaptor.nil();


            NUM38=(Token)match(input,NUM,FOLLOW_NUM_in_tnum417); 
            NUM38_tree = 
            (Object)adaptor.create(NUM38)
            ;
            adaptor.addChild(root_0, NUM38_tree);


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

    // Delegated rules


 

    public static final BitSet FOLLOW_operator_in_rule209 = new BitSet(new long[]{0x0000000000100050L});
    public static final BitSet FOLLOW_what_in_rule211 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_where_in_rule213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifier_in_what220 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_tokenspec_in_what222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANYTOKS_in_what224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec243 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec247 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_tokenspec_in_tokenspec249 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec260 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_stokenspec_in_stokenspec262 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec274 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_etokenspec_in_etokenspec276 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_token_in_singletokenspec283 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_singletokenspec285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEL_in_operator292 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_start_in_where337 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_end_in_where339 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_start360 = new BitSet(new long[]{0x0000000000104000L});
    public static final BitSet FOLLOW_swherequantifier_in_start362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_end369 = new BitSet(new long[]{0x0000000000104000L});
    public static final BitSet FOLLOW_ewherequantifier_in_end371 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_swherequantifier380 = new BitSet(new long[]{0x0000000003E089A0L});
    public static final BitSet FOLLOW_INCLD_in_swherequantifier382 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_stokenspec_in_swherequantifier385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_snum_in_swherequantifier388 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_ewherequantifier395 = new BitSet(new long[]{0x0000000003E089A0L});
    public static final BitSet FOLLOW_INCLD_in_ewherequantifier397 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_etokenspec_in_ewherequantifier400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tnum_in_ewherequantifier403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_snum410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_tnum417 = new BitSet(new long[]{0x0000000000000002L});

}