// $ANTLR 3.4 Grammarparser.g 2011-10-02 15:18:36

package edu.isi.karma.cleaning;
import java.util.ArrayList;
import java.util.HashMap;

import org.antlr.runtime.BitSet;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class GrammarparserParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "END", "LOWERLETTER", "LOWWRD", "OR", "QUE", "SEP", "UPPERLETTER", "UPPERWRD", "WS"
    };

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
    public Parser[] getDelegates() {
        return new Parser[] {};
    }

    // delegators


    public GrammarparserParser(TokenStream input) {
        this(input, new RecognizerSharedState());
    }
    public GrammarparserParser(TokenStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return GrammarparserParser.tokenNames; }
    public String getGrammarFileName() { return "Grammarparser.g"; }


    	HashMap<String,ArrayList<ArrayList<String>>> Nonterminals = new HashMap<String,ArrayList<ArrayList<String>>>();
    	RuleGenerator gen;
    	public void setGen(RuleGenerator gen)
    	{
    		this.gen = gen;
    	}


    public static class terminal_return extends ParserRuleReturnScope {
        public String name;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "terminal"
    // Grammarparser.g:32:1: terminal returns [String name] : UPPERWRD ;
    public final GrammarparserParser.terminal_return terminal() throws RecognitionException {
        GrammarparserParser.terminal_return retval = new GrammarparserParser.terminal_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token UPPERWRD1=null;

        Object UPPERWRD1_tree=null;

        try {
            // Grammarparser.g:32:31: ( UPPERWRD )
            // Grammarparser.g:32:33: UPPERWRD
            {
            root_0 = (Object)adaptor.nil();


            UPPERWRD1=(Token)match(input,UPPERWRD,FOLLOW_UPPERWRD_in_terminal129); 
            UPPERWRD1_tree = 
            (Object)adaptor.create(UPPERWRD1)
            ;
            adaptor.addChild(root_0, UPPERWRD1_tree);


            retval.name =(UPPERWRD1!=null?UPPERWRD1.getText():null);

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
    // $ANTLR end "terminal"


    public static class nonterminal_return extends ParserRuleReturnScope {
        public String name;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "nonterminal"
    // Grammarparser.g:33:1: nonterminal returns [String name] : LOWWRD ;
    public final GrammarparserParser.nonterminal_return nonterminal() throws RecognitionException {
        GrammarparserParser.nonterminal_return retval = new GrammarparserParser.nonterminal_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token LOWWRD2=null;

        Object LOWWRD2_tree=null;

        try {
            // Grammarparser.g:34:2: ( LOWWRD )
            // Grammarparser.g:34:4: LOWWRD
            {
            root_0 = (Object)adaptor.nil();


            LOWWRD2=(Token)match(input,LOWWRD,FOLLOW_LOWWRD_in_nonterminal143); 
            LOWWRD2_tree = 
            (Object)adaptor.create(LOWWRD2)
            ;
            adaptor.addChild(root_0, LOWWRD2_tree);


            retval.name =(LOWWRD2!=null?LOWWRD2.getText():null);

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
    // $ANTLR end "nonterminal"


    public static class podrule_return extends ParserRuleReturnScope {
        public ArrayList<ArrayList<String>> res;
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "podrule"
    // Grammarparser.g:36:1: podrule returns [ArrayList<ArrayList<String>> res] : (t= terminal |n= nonterminal |w= terminal QUE |z= nonterminal QUE )+ ;
    public final GrammarparserParser.podrule_return podrule() throws RecognitionException {
        GrammarparserParser.podrule_return retval = new GrammarparserParser.podrule_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token QUE3=null;
        Token QUE4=null;
        GrammarparserParser.terminal_return t =null;

        GrammarparserParser.nonterminal_return n =null;

        GrammarparserParser.terminal_return w =null;

        GrammarparserParser.nonterminal_return z =null;


        Object QUE3_tree=null;
        Object QUE4_tree=null;

         ArrayList<ArrayList<String>> s = new ArrayList<ArrayList<String>>(); 
        try {
            // Grammarparser.g:38:2: ( (t= terminal |n= nonterminal |w= terminal QUE |z= nonterminal QUE )+ )
            // Grammarparser.g:38:4: (t= terminal |n= nonterminal |w= terminal QUE |z= nonterminal QUE )+
            {
            root_0 = (Object)adaptor.nil();


            // Grammarparser.g:38:4: (t= terminal |n= nonterminal |w= terminal QUE |z= nonterminal QUE )+
            int cnt1=0;
            loop1:
            do {
                int alt1=5;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==UPPERWRD) ) {
                    int LA1_2 = input.LA(2);

                    if ( (LA1_2==END||(LA1_2 >= LOWWRD && LA1_2 <= OR)||LA1_2==UPPERWRD) ) {
                        alt1=1;
                    }
                    else if ( (LA1_2==QUE) ) {
                        alt1=3;
                    }


                }
                else if ( (LA1_0==LOWWRD) ) {
                    int LA1_3 = input.LA(2);

                    if ( (LA1_3==END||(LA1_3 >= LOWWRD && LA1_3 <= OR)||LA1_3==UPPERWRD) ) {
                        alt1=2;
                    }
                    else if ( (LA1_3==QUE) ) {
                        alt1=4;
                    }


                }


                switch (alt1) {
            	case 1 :
            	    // Grammarparser.g:38:5: t= terminal
            	    {
            	    pushFollow(FOLLOW_terminal_in_podrule168);
            	    t=terminal();

            	    state._fsp--;

            	    adaptor.addChild(root_0, t.getTree());

            	    gen.addElement(s,(t!=null?t.name:null),false);retval.res = s;

            	    }
            	    break;
            	case 2 :
            	    // Grammarparser.g:38:60: n= nonterminal
            	    {
            	    pushFollow(FOLLOW_nonterminal_in_podrule174);
            	    n=nonterminal();

            	    state._fsp--;

            	    adaptor.addChild(root_0, n.getTree());

            	    gen.addElement(s,(n!=null?n.name:null),false);retval.res =s;

            	    }
            	    break;
            	case 3 :
            	    // Grammarparser.g:38:115: w= terminal QUE
            	    {
            	    pushFollow(FOLLOW_terminal_in_podrule179);
            	    w=terminal();

            	    state._fsp--;

            	    adaptor.addChild(root_0, w.getTree());

            	    QUE3=(Token)match(input,QUE,FOLLOW_QUE_in_podrule181); 
            	    QUE3_tree = 
            	    (Object)adaptor.create(QUE3)
            	    ;
            	    adaptor.addChild(root_0, QUE3_tree);


            	    gen.addElement(s,(w!=null?w.name:null),true);retval.res =s;

            	    }
            	    break;
            	case 4 :
            	    // Grammarparser.g:38:170: z= nonterminal QUE
            	    {
            	    pushFollow(FOLLOW_nonterminal_in_podrule186);
            	    z=nonterminal();

            	    state._fsp--;

            	    adaptor.addChild(root_0, z.getTree());

            	    QUE4=(Token)match(input,QUE,FOLLOW_QUE_in_podrule188); 
            	    QUE4_tree = 
            	    (Object)adaptor.create(QUE4)
            	    ;
            	    adaptor.addChild(root_0, QUE4_tree);


            	    gen.addElement(s,(z!=null?z.name:null),true);retval.res =s;

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
    // $ANTLR end "podrule"


    public static class line_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "line"
    // Grammarparser.g:39:1: line : nonterminal SEP q= podrule ( OR p= podrule )* END ;
    public final GrammarparserParser.line_return line() throws RecognitionException {
        GrammarparserParser.line_return retval = new GrammarparserParser.line_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        Token SEP6=null;
        Token OR7=null;
        Token END8=null;
        GrammarparserParser.podrule_return q =null;

        GrammarparserParser.podrule_return p =null;

        GrammarparserParser.nonterminal_return nonterminal5 =null;


        Object SEP6_tree=null;
        Object OR7_tree=null;
        Object END8_tree=null;

        try {
            // Grammarparser.g:39:6: ( nonterminal SEP q= podrule ( OR p= podrule )* END )
            // Grammarparser.g:39:8: nonterminal SEP q= podrule ( OR p= podrule )* END
            {
            root_0 = (Object)adaptor.nil();


            pushFollow(FOLLOW_nonterminal_in_line198);
            nonterminal5=nonterminal();

            state._fsp--;

            adaptor.addChild(root_0, nonterminal5.getTree());

            SEP6=(Token)match(input,SEP,FOLLOW_SEP_in_line200); 
            SEP6_tree = 
            (Object)adaptor.create(SEP6)
            ;
            adaptor.addChild(root_0, SEP6_tree);


            pushFollow(FOLLOW_podrule_in_line204);
            q=podrule();

            state._fsp--;

            adaptor.addChild(root_0, q.getTree());


            	gen.initRule((nonterminal5!=null?nonterminal5.name:null),(q!=null?q.res:null));


            // Grammarparser.g:43:2: ( OR p= podrule )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==OR) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // Grammarparser.g:43:2: OR p= podrule
            	    {
            	    OR7=(Token)match(input,OR,FOLLOW_OR_in_line209); 
            	    OR7_tree = 
            	    (Object)adaptor.create(OR7)
            	    ;
            	    adaptor.addChild(root_0, OR7_tree);


            	    pushFollow(FOLLOW_podrule_in_line213);
            	    p=podrule();

            	    state._fsp--;

            	    adaptor.addChild(root_0, p.getTree());

            	    gen.addRule((nonterminal5!=null?nonterminal5.name:null),(p!=null?p.res:null));

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);


            END8=(Token)match(input,END,FOLLOW_END_in_line218); 
            END8_tree = 
            (Object)adaptor.create(END8)
            ;
            adaptor.addChild(root_0, END8_tree);


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
    // $ANTLR end "line"


    public static class alllines_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "alllines"
    // Grammarparser.g:45:1: alllines : ( line )+ ;
    public final GrammarparserParser.alllines_return alllines() throws RecognitionException {
        GrammarparserParser.alllines_return retval = new GrammarparserParser.alllines_return();
        retval.start = input.LT(1);


        Object root_0 = null;

        GrammarparserParser.line_return line9 =null;



        try {
            // Grammarparser.g:45:10: ( ( line )+ )
            // Grammarparser.g:45:12: ( line )+
            {
            root_0 = (Object)adaptor.nil();


            // Grammarparser.g:45:12: ( line )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==LOWWRD) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // Grammarparser.g:45:13: line
            	    {
            	    pushFollow(FOLLOW_line_in_alllines227);
            	    line9=line();

            	    state._fsp--;

            	    adaptor.addChild(root_0, line9.getTree());

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
    // $ANTLR end "alllines"

    // Delegated rules


 

    public static final BitSet FOLLOW_UPPERWRD_in_terminal129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LOWWRD_in_nonterminal143 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_terminal_in_podrule168 = new BitSet(new long[]{0x0000000000000842L});
    public static final BitSet FOLLOW_nonterminal_in_podrule174 = new BitSet(new long[]{0x0000000000000842L});
    public static final BitSet FOLLOW_terminal_in_podrule179 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_QUE_in_podrule181 = new BitSet(new long[]{0x0000000000000842L});
    public static final BitSet FOLLOW_nonterminal_in_podrule186 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_QUE_in_podrule188 = new BitSet(new long[]{0x0000000000000842L});
    public static final BitSet FOLLOW_nonterminal_in_line198 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEP_in_line200 = new BitSet(new long[]{0x0000000000000840L});
    public static final BitSet FOLLOW_podrule_in_line204 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_OR_in_line209 = new BitSet(new long[]{0x0000000000000840L});
    public static final BitSet FOLLOW_podrule_in_line213 = new BitSet(new long[]{0x0000000000000090L});
    public static final BitSet FOLLOW_END_in_line218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_line_in_alllines227 = new BitSet(new long[]{0x0000000000000042L});

}