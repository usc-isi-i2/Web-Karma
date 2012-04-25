// $ANTLR 3.4 RuleInterpreterTree.g 2012-04-23 21:53:46

  // We want the generated parser class to be in this package.
  package edu.isi.karma.cleaning.changed_grammar;
  import edu.isi.karma.cleaning.*;
  import java.util.Map;
  import java.util.TreeMap;
  import java.util.Collections;
  import java.util.HashMap; 
  import java.util.Iterator;
  import java.util.ListIterator;
  import java.util.StringTokenizer;
  import java.util.Vector;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


@SuppressWarnings({"all", "warnings", "unchecked"})
public class RuleInterpreterTree extends TreeParser {
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
    public TreeParser[] getDelegates() {
        return new TreeParser[] {};
    }

    // delegators


    public RuleInterpreterTree(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }
    public RuleInterpreterTree(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return RuleInterpreterTree.tokenNames; }
    public String getGrammarFileName() { return "RuleInterpreterTree.g"; }


    	//Rule r
    	Ruler ruler;
    	
    	//handle input Tokensequence
    	Vector<TNode> vec = new Vector<TNode>();
    	/*set the ruler*/
    	public void setRuler(Ruler r)
    	{
    		this.ruler = r;
    	}
    	public int str2int(String input)
    	{
    		return Integer.parseInt(input);
    	}


    public static class rule_return extends TreeRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "rule"
    // RuleInterpreterTree.g:46:1: rule : operator what where ;
    public final RuleInterpreterTree.rule_return rule() throws RecognitionException {
        RuleInterpreterTree.rule_return retval = new RuleInterpreterTree.rule_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        RuleInterpreterTree.operator_return operator1 =null;

        RuleInterpreterTree.what_return what2 =null;

        RuleInterpreterTree.where_return where3 =null;



        try {
            // RuleInterpreterTree.g:46:6: ( operator what where )
            // RuleInterpreterTree.g:46:8: operator what where
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_operator_in_rule71);
            operator1=operator();

            state._fsp--;

            adaptor.addChild(root_0, operator1.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_what_in_rule73);
            what2=what();

            state._fsp--;

            adaptor.addChild(root_0, what2.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_where_in_rule75);
            where3=where();

            state._fsp--;

            adaptor.addChild(root_0, where3.getTree());


            this.ruler.doOperation((operator1!=null?operator1.value:null),(what2!=null?what2.num:null),(what2!=null?what2.res:null),(where3!=null?where3.spos:0),(where3!=null?where3.epos:0));

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "rule"


    public static class what_return extends TreeRuleReturnScope {
        public String num;
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "what"
    // RuleInterpreterTree.g:49:1: what returns [String num,Vector<TNode> res] : ( quantifier tokenspec | ANYTOKS );
    public final RuleInterpreterTree.what_return what() throws RecognitionException {
        RuleInterpreterTree.what_return retval = new RuleInterpreterTree.what_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree ANYTOKS6=null;
        RuleInterpreterTree.quantifier_return quantifier4 =null;

        RuleInterpreterTree.tokenspec_return tokenspec5 =null;


        CommonTree ANYTOKS6_tree=null;

        try {
            // RuleInterpreterTree.g:49:44: ( quantifier tokenspec | ANYTOKS )
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
                    // RuleInterpreterTree.g:49:46: quantifier tokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_quantifier_in_what89);
                    quantifier4=quantifier();

                    state._fsp--;

                    adaptor.addChild(root_0, quantifier4.getTree());


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_tokenspec_in_what91);
                    tokenspec5=tokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, tokenspec5.getTree());


                    retval.num =(quantifier4!=null?quantifier4.value:null);retval.res =(tokenspec5!=null?tokenspec5.res:null);

                    }
                    break;
                case 2 :
                    // RuleInterpreterTree.g:49:112: ANYTOKS
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    ANYTOKS6=(CommonTree)match(input,ANYTOKS,FOLLOW_ANYTOKS_in_what94); 
                    ANYTOKS6_tree = (CommonTree)adaptor.dupNode(ANYTOKS6);


                    adaptor.addChild(root_0, ANYTOKS6_tree);


                    retval.res =null;

                    }
                    break;

            }
            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "what"


    public static class quantifier_return extends TreeRuleReturnScope {
        public String value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "quantifier"
    // RuleInterpreterTree.g:51:1: quantifier returns [String value] : ( ANYNUM | qnum );
    public final RuleInterpreterTree.quantifier_return quantifier() throws RecognitionException {
        RuleInterpreterTree.quantifier_return retval = new RuleInterpreterTree.quantifier_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree ANYNUM7=null;
        RuleInterpreterTree.qnum_return qnum8 =null;


        CommonTree ANYNUM7_tree=null;

        try {
            // RuleInterpreterTree.g:52:2: ( ANYNUM | qnum )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ANYNUM) ) {
                alt2=1;
            }
            else if ( (LA2_0==NUM) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;

            }
            switch (alt2) {
                case 1 :
                    // RuleInterpreterTree.g:52:4: ANYNUM
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    ANYNUM7=(CommonTree)match(input,ANYNUM,FOLLOW_ANYNUM_in_quantifier107); 
                    ANYNUM7_tree = (CommonTree)adaptor.dupNode(ANYNUM7);


                    adaptor.addChild(root_0, ANYNUM7_tree);


                    retval.value =(ANYNUM7!=null?ANYNUM7.getText():null);

                    }
                    break;
                case 2 :
                    // RuleInterpreterTree.g:52:34: qnum
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_qnum_in_quantifier111);
                    qnum8=qnum();

                    state._fsp--;

                    adaptor.addChild(root_0, qnum8.getTree());


                    retval.value =(qnum8!=null?qnum8.x:null);

                    }
                    break;

            }
            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "quantifier"


    public static class tokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tokenspec"
    // RuleInterpreterTree.g:55:1: tokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final RuleInterpreterTree.tokenspec_return tokenspec() throws RecognitionException {
        RuleInterpreterTree.tokenspec_return retval = new RuleInterpreterTree.tokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        RuleInterpreterTree.singletokenspec_return singletokenspec9 =null;

        RuleInterpreterTree.singletokenspec_return singletokenspec10 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // RuleInterpreterTree.g:57:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // RuleInterpreterTree.g:57:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_tokenspec131);
            singletokenspec9=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec9.getTree());


            // RuleInterpreterTree.g:57:26: ( singletokenspec[toks] )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==ANYTOK||(LA3_0 >= ANYTYP && LA3_0 <= BNKTYP)||LA3_0==ENDTYP||(LA3_0 >= NUMTYP && LA3_0 <= WRDTYP)) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // RuleInterpreterTree.g:57:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_tokenspec135);
            	    singletokenspec10=singletokenspec(toks);

            	    state._fsp--;

            	    adaptor.addChild(root_0, singletokenspec10.getTree());


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            retval.res = toks;

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "tokenspec"


    public static class stokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "stokenspec"
    // RuleInterpreterTree.g:58:1: stokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final RuleInterpreterTree.stokenspec_return stokenspec() throws RecognitionException {
        RuleInterpreterTree.stokenspec_return retval = new RuleInterpreterTree.stokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        RuleInterpreterTree.singletokenspec_return singletokenspec11 =null;

        RuleInterpreterTree.singletokenspec_return singletokenspec12 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // RuleInterpreterTree.g:60:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // RuleInterpreterTree.g:60:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_stokenspec157);
            singletokenspec11=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec11.getTree());


            // RuleInterpreterTree.g:60:26: ( singletokenspec[toks] )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==ANYTOK||(LA4_0 >= ANYTYP && LA4_0 <= BNKTYP)||LA4_0==ENDTYP||(LA4_0 >= NUMTYP && LA4_0 <= WRDTYP)) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // RuleInterpreterTree.g:60:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_stokenspec161);
            	    singletokenspec12=singletokenspec(toks);

            	    state._fsp--;

            	    adaptor.addChild(root_0, singletokenspec12.getTree());


            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            retval.res = toks;

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "stokenspec"


    public static class etokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "etokenspec"
    // RuleInterpreterTree.g:61:1: etokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final RuleInterpreterTree.etokenspec_return etokenspec() throws RecognitionException {
        RuleInterpreterTree.etokenspec_return retval = new RuleInterpreterTree.etokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        RuleInterpreterTree.singletokenspec_return singletokenspec13 =null;

        RuleInterpreterTree.singletokenspec_return singletokenspec14 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // RuleInterpreterTree.g:63:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // RuleInterpreterTree.g:63:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_etokenspec183);
            singletokenspec13=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec13.getTree());


            // RuleInterpreterTree.g:63:26: ( singletokenspec[toks] )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==ANYTOK||(LA5_0 >= ANYTYP && LA5_0 <= BNKTYP)||LA5_0==ENDTYP||(LA5_0 >= NUMTYP && LA5_0 <= WRDTYP)) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // RuleInterpreterTree.g:63:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_etokenspec187);
            	    singletokenspec14=singletokenspec(toks);

            	    state._fsp--;

            	    adaptor.addChild(root_0, singletokenspec14.getTree());


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            retval.res = toks;

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "etokenspec"


    public static class singletokenspec_return extends TreeRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "singletokenspec"
    // RuleInterpreterTree.g:65:1: singletokenspec[Vector<TNode> tokspec] : ( token | type );
    public final RuleInterpreterTree.singletokenspec_return singletokenspec(Vector<TNode> tokspec) throws RecognitionException {
        RuleInterpreterTree.singletokenspec_return retval = new RuleInterpreterTree.singletokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        RuleInterpreterTree.token_return token15 =null;

        RuleInterpreterTree.type_return type16 =null;



        try {
            // RuleInterpreterTree.g:65:40: ( token | type )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==ANYTOK||LA6_0==TOKEN) ) {
                alt6=1;
            }
            else if ( ((LA6_0 >= ANYTYP && LA6_0 <= BNKTYP)||LA6_0==ENDTYP||(LA6_0 >= NUMTYP && LA6_0 <= SYBTYP)||LA6_0==WRDTYP) ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }
            switch (alt6) {
                case 1 :
                    // RuleInterpreterTree.g:65:42: token
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_token_in_singletokenspec202);
                    token15=token();

                    state._fsp--;

                    adaptor.addChild(root_0, token15.getTree());


                    tokspec.add(new TNode("ANYTYP",(token15!=null?token15.value:null).substring(1, (token15!=null?token15.value:null).length()-1)));

                    }
                    break;
                case 2 :
                    // RuleInterpreterTree.g:65:134: type
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_type_in_singletokenspec205);
                    type16=type();

                    state._fsp--;

                    adaptor.addChild(root_0, type16.getTree());


                    tokspec.add(new TNode((type16!=null?type16.value:null),"ANYTOK"));

                    }
                    break;

            }
            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "singletokenspec"


    public static class operator_return extends TreeRuleReturnScope {
        public String value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "operator"
    // RuleInterpreterTree.g:68:1: operator returns [String value] : DEL ;
    public final RuleInterpreterTree.operator_return operator() throws RecognitionException {
        RuleInterpreterTree.operator_return retval = new RuleInterpreterTree.operator_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree DEL17=null;

        CommonTree DEL17_tree=null;

        try {
            // RuleInterpreterTree.g:68:32: ( DEL )
            // RuleInterpreterTree.g:68:34: DEL
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            DEL17=(CommonTree)match(input,DEL,FOLLOW_DEL_in_operator218); 
            DEL17_tree = (CommonTree)adaptor.dupNode(DEL17);


            adaptor.addChild(root_0, DEL17_tree);


            retval.value =(DEL17!=null?DEL17.getText():null);

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "operator"


    public static class type_return extends TreeRuleReturnScope {
        public String value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "type"
    // RuleInterpreterTree.g:71:1: type returns [String value] : ( ANYTYP | NUMTYP | WRDTYP | SYBTYP | BNKTYP | SRTTYP | ENDTYP );
    public final RuleInterpreterTree.type_return type() throws RecognitionException {
        RuleInterpreterTree.type_return retval = new RuleInterpreterTree.type_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree ANYTYP18=null;
        CommonTree NUMTYP19=null;
        CommonTree WRDTYP20=null;
        CommonTree SYBTYP21=null;
        CommonTree BNKTYP22=null;
        CommonTree SRTTYP23=null;
        CommonTree ENDTYP24=null;

        CommonTree ANYTYP18_tree=null;
        CommonTree NUMTYP19_tree=null;
        CommonTree WRDTYP20_tree=null;
        CommonTree SYBTYP21_tree=null;
        CommonTree BNKTYP22_tree=null;
        CommonTree SRTTYP23_tree=null;
        CommonTree ENDTYP24_tree=null;

        try {
            // RuleInterpreterTree.g:71:28: ( ANYTYP | NUMTYP | WRDTYP | SYBTYP | BNKTYP | SRTTYP | ENDTYP )
            int alt7=7;
            switch ( input.LA(1) ) {
            case ANYTYP:
                {
                alt7=1;
                }
                break;
            case NUMTYP:
                {
                alt7=2;
                }
                break;
            case WRDTYP:
                {
                alt7=3;
                }
                break;
            case SYBTYP:
                {
                alt7=4;
                }
                break;
            case BNKTYP:
                {
                alt7=5;
                }
                break;
            case SRTTYP:
                {
                alt7=6;
                }
                break;
            case ENDTYP:
                {
                alt7=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }

            switch (alt7) {
                case 1 :
                    // RuleInterpreterTree.g:71:30: ANYTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    ANYTYP18=(CommonTree)match(input,ANYTYP,FOLLOW_ANYTYP_in_type232); 
                    ANYTYP18_tree = (CommonTree)adaptor.dupNode(ANYTYP18);


                    adaptor.addChild(root_0, ANYTYP18_tree);


                    retval.value = (ANYTYP18!=null?ANYTYP18.getText():null);

                    }
                    break;
                case 2 :
                    // RuleInterpreterTree.g:71:62: NUMTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    NUMTYP19=(CommonTree)match(input,NUMTYP,FOLLOW_NUMTYP_in_type236); 
                    NUMTYP19_tree = (CommonTree)adaptor.dupNode(NUMTYP19);


                    adaptor.addChild(root_0, NUMTYP19_tree);


                    retval.value = (NUMTYP19!=null?NUMTYP19.getText():null);

                    }
                    break;
                case 3 :
                    // RuleInterpreterTree.g:71:94: WRDTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    WRDTYP20=(CommonTree)match(input,WRDTYP,FOLLOW_WRDTYP_in_type240); 
                    WRDTYP20_tree = (CommonTree)adaptor.dupNode(WRDTYP20);


                    adaptor.addChild(root_0, WRDTYP20_tree);


                    retval.value =(WRDTYP20!=null?WRDTYP20.getText():null);

                    }
                    break;
                case 4 :
                    // RuleInterpreterTree.g:71:124: SYBTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    SYBTYP21=(CommonTree)match(input,SYBTYP,FOLLOW_SYBTYP_in_type244); 
                    SYBTYP21_tree = (CommonTree)adaptor.dupNode(SYBTYP21);


                    adaptor.addChild(root_0, SYBTYP21_tree);


                    retval.value =(SYBTYP21!=null?SYBTYP21.getText():null);

                    }
                    break;
                case 5 :
                    // RuleInterpreterTree.g:71:154: BNKTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    BNKTYP22=(CommonTree)match(input,BNKTYP,FOLLOW_BNKTYP_in_type248); 
                    BNKTYP22_tree = (CommonTree)adaptor.dupNode(BNKTYP22);


                    adaptor.addChild(root_0, BNKTYP22_tree);


                    retval.value =(BNKTYP22!=null?BNKTYP22.getText():null);

                    }
                    break;
                case 6 :
                    // RuleInterpreterTree.g:71:184: SRTTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    SRTTYP23=(CommonTree)match(input,SRTTYP,FOLLOW_SRTTYP_in_type252); 
                    SRTTYP23_tree = (CommonTree)adaptor.dupNode(SRTTYP23);


                    adaptor.addChild(root_0, SRTTYP23_tree);


                    retval.value =(SRTTYP23!=null?SRTTYP23.getText():null);

                    }
                    break;
                case 7 :
                    // RuleInterpreterTree.g:71:214: ENDTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    ENDTYP24=(CommonTree)match(input,ENDTYP,FOLLOW_ENDTYP_in_type256); 
                    ENDTYP24_tree = (CommonTree)adaptor.dupNode(ENDTYP24);


                    adaptor.addChild(root_0, ENDTYP24_tree);


                    retval.value =(ENDTYP24!=null?ENDTYP24.getText():null);

                    }
                    break;

            }
            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "type"


    public static class token_return extends TreeRuleReturnScope {
        public String value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "token"
    // RuleInterpreterTree.g:73:1: token returns [String value] : ( ANYTOK | TOKEN );
    public final RuleInterpreterTree.token_return token() throws RecognitionException {
        RuleInterpreterTree.token_return retval = new RuleInterpreterTree.token_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree ANYTOK25=null;
        CommonTree TOKEN26=null;

        CommonTree ANYTOK25_tree=null;
        CommonTree TOKEN26_tree=null;

        try {
            // RuleInterpreterTree.g:73:28: ( ANYTOK | TOKEN )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==ANYTOK) ) {
                alt8=1;
            }
            else if ( (LA8_0==TOKEN) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }
            switch (alt8) {
                case 1 :
                    // RuleInterpreterTree.g:73:30: ANYTOK
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    ANYTOK25=(CommonTree)match(input,ANYTOK,FOLLOW_ANYTOK_in_token268); 
                    ANYTOK25_tree = (CommonTree)adaptor.dupNode(ANYTOK25);


                    adaptor.addChild(root_0, ANYTOK25_tree);


                    retval.value ="("+(ANYTOK25!=null?ANYTOK25.getText():null)+")";

                    }
                    break;
                case 2 :
                    // RuleInterpreterTree.g:73:69: TOKEN
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    TOKEN26=(CommonTree)match(input,TOKEN,FOLLOW_TOKEN_in_token273); 
                    TOKEN26_tree = (CommonTree)adaptor.dupNode(TOKEN26);


                    adaptor.addChild(root_0, TOKEN26_tree);


                    retval.value =(TOKEN26!=null?TOKEN26.getText():null);

                    }
                    break;

            }
            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "token"


    public static class where_return extends TreeRuleReturnScope {
        public int spos;
        public int epos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "where"
    // RuleInterpreterTree.g:76:1: where returns [int spos, int epos] : start end ;
    public final RuleInterpreterTree.where_return where() throws RecognitionException {
        RuleInterpreterTree.where_return retval = new RuleInterpreterTree.where_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        RuleInterpreterTree.start_return start27 =null;

        RuleInterpreterTree.end_return end28 =null;



        try {
            // RuleInterpreterTree.g:76:35: ( start end )
            // RuleInterpreterTree.g:76:37: start end
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_start_in_where288);
            start27=start();

            state._fsp--;

            adaptor.addChild(root_0, start27.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_end_in_where290);
            end28=end();

            state._fsp--;

            adaptor.addChild(root_0, end28.getTree());


            retval.spos =(start27!=null?start27.xpos:0);retval.epos =(end28!=null?end28.xpos:0);

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "where"


    public static class scanningOrder_return extends TreeRuleReturnScope {
        public String value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "scanningOrder"
    // RuleInterpreterTree.g:78:1: scanningOrder returns [String value] : ( FRMB | FRME );
    public final RuleInterpreterTree.scanningOrder_return scanningOrder() throws RecognitionException {
        RuleInterpreterTree.scanningOrder_return retval = new RuleInterpreterTree.scanningOrder_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree FRMB29=null;
        CommonTree FRME30=null;

        CommonTree FRMB29_tree=null;
        CommonTree FRME30_tree=null;

        try {
            // RuleInterpreterTree.g:79:2: ( FRMB | FRME )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==FRMB) ) {
                alt9=1;
            }
            else if ( (LA9_0==FRME) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }
            switch (alt9) {
                case 1 :
                    // RuleInterpreterTree.g:79:4: FRMB
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FRMB29=(CommonTree)match(input,FRMB,FOLLOW_FRMB_in_scanningOrder304); 
                    FRMB29_tree = (CommonTree)adaptor.dupNode(FRMB29);


                    adaptor.addChild(root_0, FRMB29_tree);


                    retval.value =(FRMB29!=null?FRMB29.getText():null);

                    }
                    break;
                case 2 :
                    // RuleInterpreterTree.g:79:30: FRME
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FRME30=(CommonTree)match(input,FRME,FOLLOW_FRME_in_scanningOrder308); 
                    FRME30_tree = (CommonTree)adaptor.dupNode(FRME30);


                    adaptor.addChild(root_0, FRME30_tree);


                    retval.value =(FRME30!=null?FRME30.getText():null);

                    }
                    break;

            }
            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "scanningOrder"


    public static class start_return extends TreeRuleReturnScope {
        public int xpos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "start"
    // RuleInterpreterTree.g:81:1: start returns [int xpos] : scanningOrder r= swherequantifier[$scanningOrder.value] ;
    public final RuleInterpreterTree.start_return start() throws RecognitionException {
        RuleInterpreterTree.start_return retval = new RuleInterpreterTree.start_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        RuleInterpreterTree.swherequantifier_return r =null;

        RuleInterpreterTree.scanningOrder_return scanningOrder31 =null;



        try {
            // RuleInterpreterTree.g:81:25: ( scanningOrder r= swherequantifier[$scanningOrder.value] )
            // RuleInterpreterTree.g:81:28: scanningOrder r= swherequantifier[$scanningOrder.value]
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_scanningOrder_in_start322);
            scanningOrder31=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder31.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_swherequantifier_in_start326);
            r=swherequantifier((scanningOrder31!=null?scanningOrder31.value:null));

            state._fsp--;

            adaptor.addChild(root_0, r.getTree());


            retval.xpos = (r!=null?r.xpos:0);

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "start"


    public static class end_return extends TreeRuleReturnScope {
        public int xpos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "end"
    // RuleInterpreterTree.g:82:1: end returns [int xpos] : scanningOrder r= ewherequantifier[$scanningOrder.value] ;
    public final RuleInterpreterTree.end_return end() throws RecognitionException {
        RuleInterpreterTree.end_return retval = new RuleInterpreterTree.end_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        RuleInterpreterTree.ewherequantifier_return r =null;

        RuleInterpreterTree.scanningOrder_return scanningOrder32 =null;



        try {
            // RuleInterpreterTree.g:82:23: ( scanningOrder r= ewherequantifier[$scanningOrder.value] )
            // RuleInterpreterTree.g:82:26: scanningOrder r= ewherequantifier[$scanningOrder.value]
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_scanningOrder_in_end340);
            scanningOrder32=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder32.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_ewherequantifier_in_end344);
            r=ewherequantifier((scanningOrder32!=null?scanningOrder32.value:null));

            state._fsp--;

            adaptor.addChild(root_0, r.getTree());


            retval.xpos = (r!=null?r.xpos:0);

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "end"


    public static class swherequantifier_return extends TreeRuleReturnScope {
        public int xpos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "swherequantifier"
    // RuleInterpreterTree.g:85:1: swherequantifier[String order] returns [int xpos] : ( FST (q= INCLD )? x= stokenspec | LST (p= INCLD )? y= stokenspec | snum );
    public final RuleInterpreterTree.swherequantifier_return swherequantifier(String order) throws RecognitionException {
        RuleInterpreterTree.swherequantifier_return retval = new RuleInterpreterTree.swherequantifier_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree q=null;
        CommonTree p=null;
        CommonTree FST33=null;
        CommonTree LST34=null;
        RuleInterpreterTree.stokenspec_return x =null;

        RuleInterpreterTree.stokenspec_return y =null;

        RuleInterpreterTree.snum_return snum35 =null;


        CommonTree q_tree=null;
        CommonTree p_tree=null;
        CommonTree FST33_tree=null;
        CommonTree LST34_tree=null;

        try {
            // RuleInterpreterTree.g:86:2: ( FST (q= INCLD )? x= stokenspec | LST (p= INCLD )? y= stokenspec | snum )
            int alt12=3;
            switch ( input.LA(1) ) {
            case FST:
                {
                alt12=1;
                }
                break;
            case LST:
                {
                alt12=2;
                }
                break;
            case NUM:
                {
                alt12=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;

            }

            switch (alt12) {
                case 1 :
                    // RuleInterpreterTree.g:86:4: FST (q= INCLD )? x= stokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FST33=(CommonTree)match(input,FST,FOLLOW_FST_in_swherequantifier363); 
                    FST33_tree = (CommonTree)adaptor.dupNode(FST33);


                    adaptor.addChild(root_0, FST33_tree);


                    // RuleInterpreterTree.g:86:9: (q= INCLD )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==INCLD) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // RuleInterpreterTree.g:86:9: q= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            q=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_swherequantifier367); 
                            q_tree = (CommonTree)adaptor.dupNode(q);


                            adaptor.addChild(root_0, q_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_stokenspec_in_swherequantifier372);
                    x=stokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, x.getTree());


                    retval.xpos = this.ruler.evalPos((FST33!=null?FST33.getText():null)+(q!=null?q.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 2 :
                    // RuleInterpreterTree.g:86:94: LST (p= INCLD )? y= stokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    LST34=(CommonTree)match(input,LST,FOLLOW_LST_in_swherequantifier377); 
                    LST34_tree = (CommonTree)adaptor.dupNode(LST34);


                    adaptor.addChild(root_0, LST34_tree);


                    // RuleInterpreterTree.g:86:99: (p= INCLD )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==INCLD) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // RuleInterpreterTree.g:86:99: p= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            p=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_swherequantifier381); 
                            p_tree = (CommonTree)adaptor.dupNode(p);


                            adaptor.addChild(root_0, p_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_stokenspec_in_swherequantifier386);
                    y=stokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, y.getTree());


                    retval.xpos = this.ruler.evalPos((LST34!=null?LST34.getText():null)+(p!=null?p.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 3 :
                    // RuleInterpreterTree.g:86:183: snum
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_snum_in_swherequantifier390);
                    snum35=snum();

                    state._fsp--;

                    adaptor.addChild(root_0, snum35.getTree());


                    retval.xpos =this.ruler.evalPos((snum35!=null?snum35.x:null),null,order);

                    }
                    break;

            }
            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "swherequantifier"


    public static class ewherequantifier_return extends TreeRuleReturnScope {
        public int xpos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "ewherequantifier"
    // RuleInterpreterTree.g:87:1: ewherequantifier[String order] returns [int xpos] : ( FST (q= INCLD )? x= etokenspec | LST (p= INCLD )? y= etokenspec | tnum );
    public final RuleInterpreterTree.ewherequantifier_return ewherequantifier(String order) throws RecognitionException {
        RuleInterpreterTree.ewherequantifier_return retval = new RuleInterpreterTree.ewherequantifier_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree q=null;
        CommonTree p=null;
        CommonTree FST36=null;
        CommonTree LST37=null;
        RuleInterpreterTree.etokenspec_return x =null;

        RuleInterpreterTree.etokenspec_return y =null;

        RuleInterpreterTree.tnum_return tnum38 =null;


        CommonTree q_tree=null;
        CommonTree p_tree=null;
        CommonTree FST36_tree=null;
        CommonTree LST37_tree=null;

        try {
            // RuleInterpreterTree.g:88:2: ( FST (q= INCLD )? x= etokenspec | LST (p= INCLD )? y= etokenspec | tnum )
            int alt15=3;
            switch ( input.LA(1) ) {
            case FST:
                {
                alt15=1;
                }
                break;
            case LST:
                {
                alt15=2;
                }
                break;
            case NUM:
                {
                alt15=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;

            }

            switch (alt15) {
                case 1 :
                    // RuleInterpreterTree.g:88:4: FST (q= INCLD )? x= etokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FST36=(CommonTree)match(input,FST,FOLLOW_FST_in_ewherequantifier406); 
                    FST36_tree = (CommonTree)adaptor.dupNode(FST36);


                    adaptor.addChild(root_0, FST36_tree);


                    // RuleInterpreterTree.g:88:9: (q= INCLD )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==INCLD) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // RuleInterpreterTree.g:88:9: q= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            q=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_ewherequantifier410); 
                            q_tree = (CommonTree)adaptor.dupNode(q);


                            adaptor.addChild(root_0, q_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_etokenspec_in_ewherequantifier415);
                    x=etokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, x.getTree());


                    retval.xpos = this.ruler.evalPos((FST36!=null?FST36.getText():null)+(q!=null?q.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 2 :
                    // RuleInterpreterTree.g:88:94: LST (p= INCLD )? y= etokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    LST37=(CommonTree)match(input,LST,FOLLOW_LST_in_ewherequantifier420); 
                    LST37_tree = (CommonTree)adaptor.dupNode(LST37);


                    adaptor.addChild(root_0, LST37_tree);


                    // RuleInterpreterTree.g:88:99: (p= INCLD )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==INCLD) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // RuleInterpreterTree.g:88:99: p= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            p=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_ewherequantifier424); 
                            p_tree = (CommonTree)adaptor.dupNode(p);


                            adaptor.addChild(root_0, p_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_etokenspec_in_ewherequantifier429);
                    y=etokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, y.getTree());


                    retval.xpos = this.ruler.evalPos((LST37!=null?LST37.getText():null)+(p!=null?p.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 3 :
                    // RuleInterpreterTree.g:88:183: tnum
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_tnum_in_ewherequantifier433);
                    tnum38=tnum();

                    state._fsp--;

                    adaptor.addChild(root_0, tnum38.getTree());


                    retval.xpos =this.ruler.evalPos((tnum38!=null?tnum38.x:null),null,order);

                    }
                    break;

            }
            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "ewherequantifier"


    public static class snum_return extends TreeRuleReturnScope {
        public String x;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "snum"
    // RuleInterpreterTree.g:89:1: snum returns [ String x] : NUM ;
    public final RuleInterpreterTree.snum_return snum() throws RecognitionException {
        RuleInterpreterTree.snum_return retval = new RuleInterpreterTree.snum_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUM39=null;

        CommonTree NUM39_tree=null;

        try {
            // RuleInterpreterTree.g:89:25: ( NUM )
            // RuleInterpreterTree.g:89:27: NUM
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            NUM39=(CommonTree)match(input,NUM,FOLLOW_NUM_in_snum445); 
            NUM39_tree = (CommonTree)adaptor.dupNode(NUM39);


            adaptor.addChild(root_0, NUM39_tree);


            retval.x = (NUM39!=null?NUM39.getText():null);

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "snum"


    public static class tnum_return extends TreeRuleReturnScope {
        public String x;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tnum"
    // RuleInterpreterTree.g:90:1: tnum returns [String x] : NUM ;
    public final RuleInterpreterTree.tnum_return tnum() throws RecognitionException {
        RuleInterpreterTree.tnum_return retval = new RuleInterpreterTree.tnum_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUM40=null;

        CommonTree NUM40_tree=null;

        try {
            // RuleInterpreterTree.g:90:24: ( NUM )
            // RuleInterpreterTree.g:90:26: NUM
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            NUM40=(CommonTree)match(input,NUM,FOLLOW_NUM_in_tnum456); 
            NUM40_tree = (CommonTree)adaptor.dupNode(NUM40);


            adaptor.addChild(root_0, NUM40_tree);


            retval.x =(NUM40!=null?NUM40.getText():null);

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "tnum"


    public static class qnum_return extends TreeRuleReturnScope {
        public String x;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "qnum"
    // RuleInterpreterTree.g:91:1: qnum returns [ String x] : NUM ;
    public final RuleInterpreterTree.qnum_return qnum() throws RecognitionException {
        RuleInterpreterTree.qnum_return retval = new RuleInterpreterTree.qnum_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUM41=null;

        CommonTree NUM41_tree=null;

        try {
            // RuleInterpreterTree.g:91:25: ( NUM )
            // RuleInterpreterTree.g:91:27: NUM
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            NUM41=(CommonTree)match(input,NUM,FOLLOW_NUM_in_qnum467); 
            NUM41_tree = (CommonTree)adaptor.dupNode(NUM41);


            adaptor.addChild(root_0, NUM41_tree);


            retval.x = (NUM41!=null?NUM41.getText():null);

            }

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }

        finally {
        	// do for sure before leaving
        }
        return retval;
    }
    // $ANTLR end "qnum"

    // Delegated rules


 

    public static final BitSet FOLLOW_operator_in_rule71 = new BitSet(new long[]{0x0000000000100050L});
    public static final BitSet FOLLOW_what_in_rule73 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_where_in_rule75 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_quantifier_in_what89 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_tokenspec_in_what91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANYTOKS_in_what94 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANYNUM_in_quantifier107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_qnum_in_quantifier111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec131 = new BitSet(new long[]{0x0000000003E009A2L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec135 = new BitSet(new long[]{0x0000000003E009A2L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec157 = new BitSet(new long[]{0x0000000003E009A2L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec161 = new BitSet(new long[]{0x0000000003E009A2L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec183 = new BitSet(new long[]{0x0000000003E009A2L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec187 = new BitSet(new long[]{0x0000000003E009A2L});
    public static final BitSet FOLLOW_token_in_singletokenspec202 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_singletokenspec205 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DEL_in_operator218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANYTYP_in_type232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMTYP_in_type236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WRDTYP_in_type240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYBTYP_in_type244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BNKTYP_in_type248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SRTTYP_in_type252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ENDTYP_in_type256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANYTOK_in_token268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_in_token273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_start_in_where288 = new BitSet(new long[]{0x0000000000003000L});
    public static final BitSet FOLLOW_end_in_where290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FRMB_in_scanningOrder304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FRME_in_scanningOrder308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_start322 = new BitSet(new long[]{0x0000000000124000L});
    public static final BitSet FOLLOW_swherequantifier_in_start326 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_end340 = new BitSet(new long[]{0x0000000000124000L});
    public static final BitSet FOLLOW_ewherequantifier_in_end344 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_swherequantifier363 = new BitSet(new long[]{0x0000000003E089A0L});
    public static final BitSet FOLLOW_INCLD_in_swherequantifier367 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_stokenspec_in_swherequantifier372 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LST_in_swherequantifier377 = new BitSet(new long[]{0x0000000003E089A0L});
    public static final BitSet FOLLOW_INCLD_in_swherequantifier381 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_stokenspec_in_swherequantifier386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_snum_in_swherequantifier390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_ewherequantifier406 = new BitSet(new long[]{0x0000000003E089A0L});
    public static final BitSet FOLLOW_INCLD_in_ewherequantifier410 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_etokenspec_in_ewherequantifier415 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LST_in_ewherequantifier420 = new BitSet(new long[]{0x0000000003E089A0L});
    public static final BitSet FOLLOW_INCLD_in_ewherequantifier424 = new BitSet(new long[]{0x0000000003E009A0L});
    public static final BitSet FOLLOW_etokenspec_in_ewherequantifier429 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tnum_in_ewherequantifier433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_snum445 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_tnum456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_qnum467 = new BitSet(new long[]{0x0000000000000002L});

}