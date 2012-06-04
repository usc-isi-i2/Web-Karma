// $ANTLR 3.4 INSInterpreterTree.g 2012-06-03 21:17:09

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
public class INSInterpreterTree extends TreeParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ANYNUM", "ANYTOK", "ANYTYP", "BNKTYP", "DIGIT", "ENDTYP", "FRMB", "FRME", "FST", "INCLD", "INS", "LST", "NEWLINE", "NUM", "NUMTYP", "SRTTYP", "SYBTYP", "TOKEN", "WRDTYP", "WS"
    };

    public static final int EOF=-1;
    public static final int ANYNUM=4;
    public static final int ANYTOK=5;
    public static final int ANYTYP=6;
    public static final int BNKTYP=7;
    public static final int DIGIT=8;
    public static final int ENDTYP=9;
    public static final int FRMB=10;
    public static final int FRME=11;
    public static final int FST=12;
    public static final int INCLD=13;
    public static final int INS=14;
    public static final int LST=15;
    public static final int NEWLINE=16;
    public static final int NUM=17;
    public static final int NUMTYP=18;
    public static final int SRTTYP=19;
    public static final int SYBTYP=20;
    public static final int TOKEN=21;
    public static final int WRDTYP=22;
    public static final int WS=23;

    // delegates
    public TreeParser[] getDelegates() {
        return new TreeParser[] {};
    }

    // delegators


    public INSInterpreterTree(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }
    public INSInterpreterTree(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return INSInterpreterTree.tokenNames; }
    public String getGrammarFileName() { return "INSInterpreterTree.g"; }


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
    // INSInterpreterTree.g:46:1: rule : operator what dest ;
    public final INSInterpreterTree.rule_return rule() throws RecognitionException {
        INSInterpreterTree.rule_return retval = new INSInterpreterTree.rule_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        INSInterpreterTree.operator_return operator1 =null;

        INSInterpreterTree.what_return what2 =null;

        INSInterpreterTree.dest_return dest3 =null;



        try {
            // INSInterpreterTree.g:46:6: ( operator what dest )
            // INSInterpreterTree.g:46:8: operator what dest
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
            pushFollow(FOLLOW_dest_in_rule75);
            dest3=dest();

            state._fsp--;

            adaptor.addChild(root_0, dest3.getTree());


            this.ruler.doOperation((operator1!=null?operator1.value:null),"-1",(what2!=null?what2.res:null),(dest3!=null?dest3.pos:0),-1);

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
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "what"
    // INSInterpreterTree.g:48:1: what returns [Vector<TNode> res] : tokenspec ;
    public final INSInterpreterTree.what_return what() throws RecognitionException {
        INSInterpreterTree.what_return retval = new INSInterpreterTree.what_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        INSInterpreterTree.tokenspec_return tokenspec4 =null;



        try {
            // INSInterpreterTree.g:48:33: ( tokenspec )
            // INSInterpreterTree.g:48:35: tokenspec
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_tokenspec_in_what88);
            tokenspec4=tokenspec();

            state._fsp--;

            adaptor.addChild(root_0, tokenspec4.getTree());


            retval.res =(tokenspec4!=null?tokenspec4.res:null);

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


    public static class tokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tokenspec"
    // INSInterpreterTree.g:50:1: tokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final INSInterpreterTree.tokenspec_return tokenspec() throws RecognitionException {
        INSInterpreterTree.tokenspec_return retval = new INSInterpreterTree.tokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        INSInterpreterTree.singletokenspec_return singletokenspec5 =null;

        INSInterpreterTree.singletokenspec_return singletokenspec6 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // INSInterpreterTree.g:52:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // INSInterpreterTree.g:52:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_tokenspec107);
            singletokenspec5=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec5.getTree());


            // INSInterpreterTree.g:52:26: ( singletokenspec[toks] )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==BNKTYP||LA1_0==ENDTYP||(LA1_0 >= NUMTYP && LA1_0 <= WRDTYP)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // INSInterpreterTree.g:52:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_tokenspec111);
            	    singletokenspec6=singletokenspec(toks);

            	    state._fsp--;

            	    adaptor.addChild(root_0, singletokenspec6.getTree());


            	    }
            	    break;

            	default :
            	    break loop1;
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


    public static class dtokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dtokenspec"
    // INSInterpreterTree.g:53:1: dtokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final INSInterpreterTree.dtokenspec_return dtokenspec() throws RecognitionException {
        INSInterpreterTree.dtokenspec_return retval = new INSInterpreterTree.dtokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        INSInterpreterTree.singletokenspec_return singletokenspec7 =null;

        INSInterpreterTree.singletokenspec_return singletokenspec8 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // INSInterpreterTree.g:55:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // INSInterpreterTree.g:55:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_dtokenspec133);
            singletokenspec7=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec7.getTree());


            // INSInterpreterTree.g:55:26: ( singletokenspec[toks] )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==BNKTYP||LA2_0==ENDTYP||(LA2_0 >= NUMTYP && LA2_0 <= WRDTYP)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // INSInterpreterTree.g:55:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_dtokenspec137);
            	    singletokenspec8=singletokenspec(toks);

            	    state._fsp--;

            	    adaptor.addChild(root_0, singletokenspec8.getTree());


            	    }
            	    break;

            	default :
            	    break loop2;
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
    // $ANTLR end "dtokenspec"


    public static class singletokenspec_return extends TreeRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "singletokenspec"
    // INSInterpreterTree.g:56:1: singletokenspec[Vector<TNode> tokspec] : ( token | type );
    public final INSInterpreterTree.singletokenspec_return singletokenspec(Vector<TNode> tokspec) throws RecognitionException {
        INSInterpreterTree.singletokenspec_return retval = new INSInterpreterTree.singletokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        INSInterpreterTree.token_return token9 =null;

        INSInterpreterTree.type_return type10 =null;



        try {
            // INSInterpreterTree.g:56:40: ( token | type )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==TOKEN) ) {
                alt3=1;
            }
            else if ( (LA3_0==BNKTYP||LA3_0==ENDTYP||(LA3_0 >= NUMTYP && LA3_0 <= SYBTYP)||LA3_0==WRDTYP) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;

            }
            switch (alt3) {
                case 1 :
                    // INSInterpreterTree.g:56:42: token
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_token_in_singletokenspec151);
                    token9=token();

                    state._fsp--;

                    adaptor.addChild(root_0, token9.getTree());


                    tokspec.add(new TNode("ANYTYP",(token9!=null?token9.value:null).substring(1, (token9!=null?token9.value:null).length()-1)));

                    }
                    break;
                case 2 :
                    // INSInterpreterTree.g:56:134: type
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_type_in_singletokenspec154);
                    type10=type();

                    state._fsp--;

                    adaptor.addChild(root_0, type10.getTree());


                    tokspec.add(new TNode((type10!=null?type10.value:null),"ANYTOK"));

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
    // INSInterpreterTree.g:59:1: operator returns [String value] : INS ;
    public final INSInterpreterTree.operator_return operator() throws RecognitionException {
        INSInterpreterTree.operator_return retval = new INSInterpreterTree.operator_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree INS11=null;

        CommonTree INS11_tree=null;

        try {
            // INSInterpreterTree.g:59:32: ( INS )
            // INSInterpreterTree.g:59:34: INS
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            INS11=(CommonTree)match(input,INS,FOLLOW_INS_in_operator167); 
            INS11_tree = (CommonTree)adaptor.dupNode(INS11);


            adaptor.addChild(root_0, INS11_tree);


            retval.value =(INS11!=null?INS11.getText():null);

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
    // INSInterpreterTree.g:61:1: type returns [String value] : ( NUMTYP | WRDTYP | SYBTYP | BNKTYP | SRTTYP | ENDTYP );
    public final INSInterpreterTree.type_return type() throws RecognitionException {
        INSInterpreterTree.type_return retval = new INSInterpreterTree.type_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUMTYP12=null;
        CommonTree WRDTYP13=null;
        CommonTree SYBTYP14=null;
        CommonTree BNKTYP15=null;
        CommonTree SRTTYP16=null;
        CommonTree ENDTYP17=null;

        CommonTree NUMTYP12_tree=null;
        CommonTree WRDTYP13_tree=null;
        CommonTree SYBTYP14_tree=null;
        CommonTree BNKTYP15_tree=null;
        CommonTree SRTTYP16_tree=null;
        CommonTree ENDTYP17_tree=null;

        try {
            // INSInterpreterTree.g:61:28: ( NUMTYP | WRDTYP | SYBTYP | BNKTYP | SRTTYP | ENDTYP )
            int alt4=6;
            switch ( input.LA(1) ) {
            case NUMTYP:
                {
                alt4=1;
                }
                break;
            case WRDTYP:
                {
                alt4=2;
                }
                break;
            case SYBTYP:
                {
                alt4=3;
                }
                break;
            case BNKTYP:
                {
                alt4=4;
                }
                break;
            case SRTTYP:
                {
                alt4=5;
                }
                break;
            case ENDTYP:
                {
                alt4=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;

            }

            switch (alt4) {
                case 1 :
                    // INSInterpreterTree.g:61:30: NUMTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    NUMTYP12=(CommonTree)match(input,NUMTYP,FOLLOW_NUMTYP_in_type180); 
                    NUMTYP12_tree = (CommonTree)adaptor.dupNode(NUMTYP12);


                    adaptor.addChild(root_0, NUMTYP12_tree);


                    retval.value = (NUMTYP12!=null?NUMTYP12.getText():null);

                    }
                    break;
                case 2 :
                    // INSInterpreterTree.g:61:62: WRDTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    WRDTYP13=(CommonTree)match(input,WRDTYP,FOLLOW_WRDTYP_in_type184); 
                    WRDTYP13_tree = (CommonTree)adaptor.dupNode(WRDTYP13);


                    adaptor.addChild(root_0, WRDTYP13_tree);


                    retval.value =(WRDTYP13!=null?WRDTYP13.getText():null);

                    }
                    break;
                case 3 :
                    // INSInterpreterTree.g:61:92: SYBTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    SYBTYP14=(CommonTree)match(input,SYBTYP,FOLLOW_SYBTYP_in_type188); 
                    SYBTYP14_tree = (CommonTree)adaptor.dupNode(SYBTYP14);


                    adaptor.addChild(root_0, SYBTYP14_tree);


                    retval.value =(SYBTYP14!=null?SYBTYP14.getText():null);

                    }
                    break;
                case 4 :
                    // INSInterpreterTree.g:61:122: BNKTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    BNKTYP15=(CommonTree)match(input,BNKTYP,FOLLOW_BNKTYP_in_type192); 
                    BNKTYP15_tree = (CommonTree)adaptor.dupNode(BNKTYP15);


                    adaptor.addChild(root_0, BNKTYP15_tree);


                    retval.value =(BNKTYP15!=null?BNKTYP15.getText():null);

                    }
                    break;
                case 5 :
                    // INSInterpreterTree.g:61:152: SRTTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    SRTTYP16=(CommonTree)match(input,SRTTYP,FOLLOW_SRTTYP_in_type196); 
                    SRTTYP16_tree = (CommonTree)adaptor.dupNode(SRTTYP16);


                    adaptor.addChild(root_0, SRTTYP16_tree);


                    retval.value =(SRTTYP16!=null?SRTTYP16.getText():null);

                    }
                    break;
                case 6 :
                    // INSInterpreterTree.g:61:182: ENDTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    ENDTYP17=(CommonTree)match(input,ENDTYP,FOLLOW_ENDTYP_in_type200); 
                    ENDTYP17_tree = (CommonTree)adaptor.dupNode(ENDTYP17);


                    adaptor.addChild(root_0, ENDTYP17_tree);


                    retval.value =(ENDTYP17!=null?ENDTYP17.getText():null);

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
    // INSInterpreterTree.g:63:1: token returns [String value] : TOKEN ;
    public final INSInterpreterTree.token_return token() throws RecognitionException {
        INSInterpreterTree.token_return retval = new INSInterpreterTree.token_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree TOKEN18=null;

        CommonTree TOKEN18_tree=null;

        try {
            // INSInterpreterTree.g:63:28: ( TOKEN )
            // INSInterpreterTree.g:63:30: TOKEN
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            TOKEN18=(CommonTree)match(input,TOKEN,FOLLOW_TOKEN_in_token212); 
            TOKEN18_tree = (CommonTree)adaptor.dupNode(TOKEN18);


            adaptor.addChild(root_0, TOKEN18_tree);


            retval.value =(TOKEN18!=null?TOKEN18.getText():null);

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


    public static class scanningOrder_return extends TreeRuleReturnScope {
        public String value;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "scanningOrder"
    // INSInterpreterTree.g:66:1: scanningOrder returns [String value] : ( FRMB | FRME );
    public final INSInterpreterTree.scanningOrder_return scanningOrder() throws RecognitionException {
        INSInterpreterTree.scanningOrder_return retval = new INSInterpreterTree.scanningOrder_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree FRMB19=null;
        CommonTree FRME20=null;

        CommonTree FRMB19_tree=null;
        CommonTree FRME20_tree=null;

        try {
            // INSInterpreterTree.g:67:2: ( FRMB | FRME )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==FRMB) ) {
                alt5=1;
            }
            else if ( (LA5_0==FRME) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }
            switch (alt5) {
                case 1 :
                    // INSInterpreterTree.g:67:4: FRMB
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FRMB19=(CommonTree)match(input,FRMB,FOLLOW_FRMB_in_scanningOrder229); 
                    FRMB19_tree = (CommonTree)adaptor.dupNode(FRMB19);


                    adaptor.addChild(root_0, FRMB19_tree);


                    retval.value =(FRMB19!=null?FRMB19.getText():null);

                    }
                    break;
                case 2 :
                    // INSInterpreterTree.g:67:30: FRME
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FRME20=(CommonTree)match(input,FRME,FOLLOW_FRME_in_scanningOrder233); 
                    FRME20_tree = (CommonTree)adaptor.dupNode(FRME20);


                    adaptor.addChild(root_0, FRME20_tree);


                    retval.value =(FRME20!=null?FRME20.getText():null);

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


    public static class dest_return extends TreeRuleReturnScope {
        public int pos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dest"
    // INSInterpreterTree.g:69:1: dest returns [int pos] : scanningOrder r= dwherequantifier[$scanningOrder.value] ;
    public final INSInterpreterTree.dest_return dest() throws RecognitionException {
        INSInterpreterTree.dest_return retval = new INSInterpreterTree.dest_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        INSInterpreterTree.dwherequantifier_return r =null;

        INSInterpreterTree.scanningOrder_return scanningOrder21 =null;



        try {
            // INSInterpreterTree.g:69:23: ( scanningOrder r= dwherequantifier[$scanningOrder.value] )
            // INSInterpreterTree.g:69:26: scanningOrder r= dwherequantifier[$scanningOrder.value]
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_scanningOrder_in_dest247);
            scanningOrder21=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder21.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_dwherequantifier_in_dest251);
            r=dwherequantifier((scanningOrder21!=null?scanningOrder21.value:null));

            state._fsp--;

            adaptor.addChild(root_0, r.getTree());


            retval.pos = r.pos;

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
    // $ANTLR end "dest"


    public static class dwherequantifier_return extends TreeRuleReturnScope {
        public int pos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dwherequantifier"
    // INSInterpreterTree.g:72:1: dwherequantifier[String order] returns [int pos] : ( FST (q= INCLD )? x= dtokenspec | LST (p= INCLD )? y= dtokenspec | dnum );
    public final INSInterpreterTree.dwherequantifier_return dwherequantifier(String order) throws RecognitionException {
        INSInterpreterTree.dwherequantifier_return retval = new INSInterpreterTree.dwherequantifier_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree q=null;
        CommonTree p=null;
        CommonTree FST22=null;
        CommonTree LST23=null;
        INSInterpreterTree.dtokenspec_return x =null;

        INSInterpreterTree.dtokenspec_return y =null;

        INSInterpreterTree.dnum_return dnum24 =null;


        CommonTree q_tree=null;
        CommonTree p_tree=null;
        CommonTree FST22_tree=null;
        CommonTree LST23_tree=null;

        try {
            // INSInterpreterTree.g:73:2: ( FST (q= INCLD )? x= dtokenspec | LST (p= INCLD )? y= dtokenspec | dnum )
            int alt8=3;
            switch ( input.LA(1) ) {
            case FST:
                {
                alt8=1;
                }
                break;
            case LST:
                {
                alt8=2;
                }
                break;
            case NUM:
                {
                alt8=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }

            switch (alt8) {
                case 1 :
                    // INSInterpreterTree.g:73:4: FST (q= INCLD )? x= dtokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FST22=(CommonTree)match(input,FST,FOLLOW_FST_in_dwherequantifier270); 
                    FST22_tree = (CommonTree)adaptor.dupNode(FST22);


                    adaptor.addChild(root_0, FST22_tree);


                    // INSInterpreterTree.g:73:9: (q= INCLD )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==INCLD) ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // INSInterpreterTree.g:73:9: q= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            q=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_dwherequantifier274); 
                            q_tree = (CommonTree)adaptor.dupNode(q);


                            adaptor.addChild(root_0, q_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_dtokenspec_in_dwherequantifier279);
                    x=dtokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, x.getTree());


                    retval.pos = this.ruler.evalPos((FST22!=null?FST22.getText():null)+(q!=null?q.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 2 :
                    // INSInterpreterTree.g:73:93: LST (p= INCLD )? y= dtokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    LST23=(CommonTree)match(input,LST,FOLLOW_LST_in_dwherequantifier284); 
                    LST23_tree = (CommonTree)adaptor.dupNode(LST23);


                    adaptor.addChild(root_0, LST23_tree);


                    // INSInterpreterTree.g:73:98: (p= INCLD )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==INCLD) ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // INSInterpreterTree.g:73:98: p= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            p=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_dwherequantifier288); 
                            p_tree = (CommonTree)adaptor.dupNode(p);


                            adaptor.addChild(root_0, p_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_dtokenspec_in_dwherequantifier293);
                    y=dtokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, y.getTree());


                    retval.pos = this.ruler.evalPos((LST23!=null?LST23.getText():null)+(p!=null?p.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 3 :
                    // INSInterpreterTree.g:73:181: dnum
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_dnum_in_dwherequantifier297);
                    dnum24=dnum();

                    state._fsp--;

                    adaptor.addChild(root_0, dnum24.getTree());


                    retval.pos =this.ruler.evalPos((dnum24!=null?dnum24.x:null),null,order);

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
    // $ANTLR end "dwherequantifier"


    public static class dnum_return extends TreeRuleReturnScope {
        public String x;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dnum"
    // INSInterpreterTree.g:75:1: dnum returns [String x] : NUM ;
    public final INSInterpreterTree.dnum_return dnum() throws RecognitionException {
        INSInterpreterTree.dnum_return retval = new INSInterpreterTree.dnum_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUM25=null;

        CommonTree NUM25_tree=null;

        try {
            // INSInterpreterTree.g:75:24: ( NUM )
            // INSInterpreterTree.g:75:26: NUM
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            NUM25=(CommonTree)match(input,NUM,FOLLOW_NUM_in_dnum310); 
            NUM25_tree = (CommonTree)adaptor.dupNode(NUM25);


            adaptor.addChild(root_0, NUM25_tree);


            retval.x =(NUM25!=null?NUM25.getText():null);

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
    // $ANTLR end "dnum"

    // Delegated rules


 

    public static final BitSet FOLLOW_operator_in_rule71 = new BitSet(new long[]{0x00000000007C0280L});
    public static final BitSet FOLLOW_what_in_rule73 = new BitSet(new long[]{0x0000000000000C00L});
    public static final BitSet FOLLOW_dest_in_rule75 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tokenspec_in_what88 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec107 = new BitSet(new long[]{0x00000000007C0282L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec111 = new BitSet(new long[]{0x00000000007C0282L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec133 = new BitSet(new long[]{0x00000000007C0282L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec137 = new BitSet(new long[]{0x00000000007C0282L});
    public static final BitSet FOLLOW_token_in_singletokenspec151 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_singletokenspec154 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INS_in_operator167 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMTYP_in_type180 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WRDTYP_in_type184 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYBTYP_in_type188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BNKTYP_in_type192 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SRTTYP_in_type196 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ENDTYP_in_type200 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_in_token212 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FRMB_in_scanningOrder229 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FRME_in_scanningOrder233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_dest247 = new BitSet(new long[]{0x0000000000029000L});
    public static final BitSet FOLLOW_dwherequantifier_in_dest251 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_dwherequantifier270 = new BitSet(new long[]{0x00000000007C2280L});
    public static final BitSet FOLLOW_INCLD_in_dwherequantifier274 = new BitSet(new long[]{0x00000000007C0280L});
    public static final BitSet FOLLOW_dtokenspec_in_dwherequantifier279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LST_in_dwherequantifier284 = new BitSet(new long[]{0x00000000007C2280L});
    public static final BitSet FOLLOW_INCLD_in_dwherequantifier288 = new BitSet(new long[]{0x00000000007C0280L});
    public static final BitSet FOLLOW_dtokenspec_in_dwherequantifier293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dnum_in_dwherequantifier297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_dnum310 = new BitSet(new long[]{0x0000000000000002L});

}