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
// $ANTLR 3.4 MOVInterpreterTree.g 2012-02-13 14:41:39

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
public class MOVInterpreterTree extends TreeParser {
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
    public TreeParser[] getDelegates() {
        return new TreeParser[] {};
    }

    // delegators


    public MOVInterpreterTree(TreeNodeStream input) {
        this(input, new RecognizerSharedState());
    }
    public MOVInterpreterTree(TreeNodeStream input, RecognizerSharedState state) {
        super(input, state);
    }

protected TreeAdaptor adaptor = new CommonTreeAdaptor();

public void setTreeAdaptor(TreeAdaptor adaptor) {
    this.adaptor = adaptor;
}
public TreeAdaptor getTreeAdaptor() {
    return adaptor;
}
    public String[] getTokenNames() { return MOVInterpreterTree.tokenNames; }
    public String getGrammarFileName() { return "MOVInterpreterTree.g"; }


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
    // MOVInterpreterTree.g:46:1: rule : operator what where dest ;
    public final MOVInterpreterTree.rule_return rule() throws RecognitionException {
        MOVInterpreterTree.rule_return retval = new MOVInterpreterTree.rule_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.operator_return operator1 =null;

        MOVInterpreterTree.what_return what2 =null;

        MOVInterpreterTree.where_return where3 =null;

        MOVInterpreterTree.dest_return dest4 =null;



        try {
            // MOVInterpreterTree.g:46:6: ( operator what where dest )
            // MOVInterpreterTree.g:46:8: operator what where dest
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


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_dest_in_rule77);
            dest4=dest();

            state._fsp--;

            adaptor.addChild(root_0, dest4.getTree());


            this.ruler.doOperation((operator1!=null?operator1.value:null),String.valueOf((dest4!=null?dest4.pos:0)),(what2!=null?what2.res:null),(where3!=null?where3.spos:0),(where3!=null?where3.epos:0));

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
    // MOVInterpreterTree.g:49:1: what returns [Vector<TNode> res] : ( tokenspec | ANYTOKS );
    public final MOVInterpreterTree.what_return what() throws RecognitionException {
        MOVInterpreterTree.what_return retval = new MOVInterpreterTree.what_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree ANYTOKS6=null;
        MOVInterpreterTree.tokenspec_return tokenspec5 =null;


        CommonTree ANYTOKS6_tree=null;

        try {
            // MOVInterpreterTree.g:49:33: ( tokenspec | ANYTOKS )
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
                    // MOVInterpreterTree.g:49:35: tokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_tokenspec_in_what90);
                    tokenspec5=tokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, tokenspec5.getTree());


                    retval.res =(tokenspec5!=null?tokenspec5.res:null);

                    }
                    break;
                case 2 :
                    // MOVInterpreterTree.g:49:67: ANYTOKS
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    ANYTOKS6=(CommonTree)match(input,ANYTOKS,FOLLOW_ANYTOKS_in_what93); 
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


    public static class tokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "tokenspec"
    // MOVInterpreterTree.g:52:1: tokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final MOVInterpreterTree.tokenspec_return tokenspec() throws RecognitionException {
        MOVInterpreterTree.tokenspec_return retval = new MOVInterpreterTree.tokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.singletokenspec_return singletokenspec7 =null;

        MOVInterpreterTree.singletokenspec_return singletokenspec8 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // MOVInterpreterTree.g:54:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // MOVInterpreterTree.g:54:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_tokenspec113);
            singletokenspec7=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec7.getTree());


            // MOVInterpreterTree.g:54:26: ( singletokenspec[toks] )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==BNKTYP||(LA2_0 >= NUMTYP && LA2_0 <= WRDTYP)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // MOVInterpreterTree.g:54:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_tokenspec117);
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
    // $ANTLR end "tokenspec"


    public static class stokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "stokenspec"
    // MOVInterpreterTree.g:55:1: stokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final MOVInterpreterTree.stokenspec_return stokenspec() throws RecognitionException {
        MOVInterpreterTree.stokenspec_return retval = new MOVInterpreterTree.stokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.singletokenspec_return singletokenspec9 =null;

        MOVInterpreterTree.singletokenspec_return singletokenspec10 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // MOVInterpreterTree.g:57:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // MOVInterpreterTree.g:57:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_stokenspec139);
            singletokenspec9=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec9.getTree());


            // MOVInterpreterTree.g:57:26: ( singletokenspec[toks] )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==BNKTYP||(LA3_0 >= NUMTYP && LA3_0 <= WRDTYP)) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // MOVInterpreterTree.g:57:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_stokenspec143);
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
    // $ANTLR end "stokenspec"


    public static class etokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "etokenspec"
    // MOVInterpreterTree.g:58:1: etokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final MOVInterpreterTree.etokenspec_return etokenspec() throws RecognitionException {
        MOVInterpreterTree.etokenspec_return retval = new MOVInterpreterTree.etokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.singletokenspec_return singletokenspec11 =null;

        MOVInterpreterTree.singletokenspec_return singletokenspec12 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // MOVInterpreterTree.g:60:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // MOVInterpreterTree.g:60:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_etokenspec165);
            singletokenspec11=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec11.getTree());


            // MOVInterpreterTree.g:60:26: ( singletokenspec[toks] )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==BNKTYP||(LA4_0 >= NUMTYP && LA4_0 <= WRDTYP)) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // MOVInterpreterTree.g:60:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_etokenspec169);
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
    // $ANTLR end "etokenspec"


    public static class dtokenspec_return extends TreeRuleReturnScope {
        public Vector<TNode> res;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dtokenspec"
    // MOVInterpreterTree.g:61:1: dtokenspec returns [Vector<TNode> res] : singletokenspec[toks] ( singletokenspec[toks] )* ;
    public final MOVInterpreterTree.dtokenspec_return dtokenspec() throws RecognitionException {
        MOVInterpreterTree.dtokenspec_return retval = new MOVInterpreterTree.dtokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.singletokenspec_return singletokenspec13 =null;

        MOVInterpreterTree.singletokenspec_return singletokenspec14 =null;



         Vector<TNode> toks = new Vector<TNode>(); 
        try {
            // MOVInterpreterTree.g:63:2: ( singletokenspec[toks] ( singletokenspec[toks] )* )
            // MOVInterpreterTree.g:63:4: singletokenspec[toks] ( singletokenspec[toks] )*
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_singletokenspec_in_dtokenspec191);
            singletokenspec13=singletokenspec(toks);

            state._fsp--;

            adaptor.addChild(root_0, singletokenspec13.getTree());


            // MOVInterpreterTree.g:63:26: ( singletokenspec[toks] )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==BNKTYP||(LA5_0 >= NUMTYP && LA5_0 <= WRDTYP)) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // MOVInterpreterTree.g:63:27: singletokenspec[toks]
            	    {
            	    _last = (CommonTree)input.LT(1);
            	    pushFollow(FOLLOW_singletokenspec_in_dtokenspec195);
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
    // $ANTLR end "dtokenspec"


    public static class singletokenspec_return extends TreeRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "singletokenspec"
    // MOVInterpreterTree.g:65:1: singletokenspec[Vector<TNode> tokspec] : ( token | type );
    public final MOVInterpreterTree.singletokenspec_return singletokenspec(Vector<TNode> tokspec) throws RecognitionException {
        MOVInterpreterTree.singletokenspec_return retval = new MOVInterpreterTree.singletokenspec_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.token_return token15 =null;

        MOVInterpreterTree.type_return type16 =null;



        try {
            // MOVInterpreterTree.g:65:40: ( token | type )
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
                    // MOVInterpreterTree.g:65:42: token
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_token_in_singletokenspec210);
                    token15=token();

                    state._fsp--;

                    adaptor.addChild(root_0, token15.getTree());


                    tokspec.add(new TNode("ANYTYP",(token15!=null?token15.value:null).substring(1, (token15!=null?token15.value:null).length()-1)));

                    }
                    break;
                case 2 :
                    // MOVInterpreterTree.g:65:134: type
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_type_in_singletokenspec213);
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
    // MOVInterpreterTree.g:68:1: operator returns [String value] : MOV ;
    public final MOVInterpreterTree.operator_return operator() throws RecognitionException {
        MOVInterpreterTree.operator_return retval = new MOVInterpreterTree.operator_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree MOV17=null;

        CommonTree MOV17_tree=null;

        try {
            // MOVInterpreterTree.g:68:32: ( MOV )
            // MOVInterpreterTree.g:68:34: MOV
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            MOV17=(CommonTree)match(input,MOV,FOLLOW_MOV_in_operator226); 
            MOV17_tree = (CommonTree)adaptor.dupNode(MOV17);


            adaptor.addChild(root_0, MOV17_tree);


            retval.value =(MOV17!=null?MOV17.getText():null);

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
    // MOVInterpreterTree.g:71:1: type returns [String value] : ( NUMTYP | WRDTYP | SYBTYP | BNKTYP );
    public final MOVInterpreterTree.type_return type() throws RecognitionException {
        MOVInterpreterTree.type_return retval = new MOVInterpreterTree.type_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUMTYP18=null;
        CommonTree WRDTYP19=null;
        CommonTree SYBTYP20=null;
        CommonTree BNKTYP21=null;

        CommonTree NUMTYP18_tree=null;
        CommonTree WRDTYP19_tree=null;
        CommonTree SYBTYP20_tree=null;
        CommonTree BNKTYP21_tree=null;

        try {
            // MOVInterpreterTree.g:71:28: ( NUMTYP | WRDTYP | SYBTYP | BNKTYP )
            int alt7=4;
            switch ( input.LA(1) ) {
            case NUMTYP:
                {
                alt7=1;
                }
                break;
            case WRDTYP:
                {
                alt7=2;
                }
                break;
            case SYBTYP:
                {
                alt7=3;
                }
                break;
            case BNKTYP:
                {
                alt7=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }

            switch (alt7) {
                case 1 :
                    // MOVInterpreterTree.g:71:30: NUMTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    NUMTYP18=(CommonTree)match(input,NUMTYP,FOLLOW_NUMTYP_in_type240); 
                    NUMTYP18_tree = (CommonTree)adaptor.dupNode(NUMTYP18);


                    adaptor.addChild(root_0, NUMTYP18_tree);


                    retval.value = (NUMTYP18!=null?NUMTYP18.getText():null);

                    }
                    break;
                case 2 :
                    // MOVInterpreterTree.g:71:62: WRDTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    WRDTYP19=(CommonTree)match(input,WRDTYP,FOLLOW_WRDTYP_in_type244); 
                    WRDTYP19_tree = (CommonTree)adaptor.dupNode(WRDTYP19);


                    adaptor.addChild(root_0, WRDTYP19_tree);


                    retval.value =(WRDTYP19!=null?WRDTYP19.getText():null);

                    }
                    break;
                case 3 :
                    // MOVInterpreterTree.g:71:92: SYBTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    SYBTYP20=(CommonTree)match(input,SYBTYP,FOLLOW_SYBTYP_in_type248); 
                    SYBTYP20_tree = (CommonTree)adaptor.dupNode(SYBTYP20);


                    adaptor.addChild(root_0, SYBTYP20_tree);


                    retval.value =(SYBTYP20!=null?SYBTYP20.getText():null);

                    }
                    break;
                case 4 :
                    // MOVInterpreterTree.g:71:122: BNKTYP
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    BNKTYP21=(CommonTree)match(input,BNKTYP,FOLLOW_BNKTYP_in_type252); 
                    BNKTYP21_tree = (CommonTree)adaptor.dupNode(BNKTYP21);


                    adaptor.addChild(root_0, BNKTYP21_tree);


                    retval.value =(BNKTYP21!=null?BNKTYP21.getText():null);

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
    // MOVInterpreterTree.g:73:1: token returns [String value] : TOKEN ;
    public final MOVInterpreterTree.token_return token() throws RecognitionException {
        MOVInterpreterTree.token_return retval = new MOVInterpreterTree.token_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree TOKEN22=null;

        CommonTree TOKEN22_tree=null;

        try {
            // MOVInterpreterTree.g:73:28: ( TOKEN )
            // MOVInterpreterTree.g:73:30: TOKEN
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            TOKEN22=(CommonTree)match(input,TOKEN,FOLLOW_TOKEN_in_token264); 
            TOKEN22_tree = (CommonTree)adaptor.dupNode(TOKEN22);


            adaptor.addChild(root_0, TOKEN22_tree);


            retval.value =(TOKEN22!=null?TOKEN22.getText():null);

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
    // MOVInterpreterTree.g:75:1: where returns [int spos, int epos] : start end ;
    public final MOVInterpreterTree.where_return where() throws RecognitionException {
        MOVInterpreterTree.where_return retval = new MOVInterpreterTree.where_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.start_return start23 =null;

        MOVInterpreterTree.end_return end24 =null;



        try {
            // MOVInterpreterTree.g:75:35: ( start end )
            // MOVInterpreterTree.g:75:37: start end
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_start_in_where278);
            start23=start();

            state._fsp--;

            adaptor.addChild(root_0, start23.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_end_in_where280);
            end24=end();

            state._fsp--;

            adaptor.addChild(root_0, end24.getTree());


            retval.spos =(start23!=null?start23.pos:0);retval.epos =(end24!=null?end24.pos:0);

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
    // MOVInterpreterTree.g:77:1: scanningOrder returns [String value] : ( FRMB | FRME );
    public final MOVInterpreterTree.scanningOrder_return scanningOrder() throws RecognitionException {
        MOVInterpreterTree.scanningOrder_return retval = new MOVInterpreterTree.scanningOrder_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree FRMB25=null;
        CommonTree FRME26=null;

        CommonTree FRMB25_tree=null;
        CommonTree FRME26_tree=null;

        try {
            // MOVInterpreterTree.g:78:2: ( FRMB | FRME )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==FRMB) ) {
                alt8=1;
            }
            else if ( (LA8_0==FRME) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }
            switch (alt8) {
                case 1 :
                    // MOVInterpreterTree.g:78:4: FRMB
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FRMB25=(CommonTree)match(input,FRMB,FOLLOW_FRMB_in_scanningOrder294); 
                    FRMB25_tree = (CommonTree)adaptor.dupNode(FRMB25);


                    adaptor.addChild(root_0, FRMB25_tree);


                    retval.value =(FRMB25!=null?FRMB25.getText():null);

                    }
                    break;
                case 2 :
                    // MOVInterpreterTree.g:78:30: FRME
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FRME26=(CommonTree)match(input,FRME,FOLLOW_FRME_in_scanningOrder298); 
                    FRME26_tree = (CommonTree)adaptor.dupNode(FRME26);


                    adaptor.addChild(root_0, FRME26_tree);


                    retval.value =(FRME26!=null?FRME26.getText():null);

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
        public int pos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "start"
    // MOVInterpreterTree.g:80:1: start returns [int pos] : scanningOrder r= swherequantifier[$scanningOrder.value] ;
    public final MOVInterpreterTree.start_return start() throws RecognitionException {
        MOVInterpreterTree.start_return retval = new MOVInterpreterTree.start_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.swherequantifier_return r =null;

        MOVInterpreterTree.scanningOrder_return scanningOrder27 =null;



        try {
            // MOVInterpreterTree.g:80:24: ( scanningOrder r= swherequantifier[$scanningOrder.value] )
            // MOVInterpreterTree.g:80:27: scanningOrder r= swherequantifier[$scanningOrder.value]
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_scanningOrder_in_start312);
            scanningOrder27=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder27.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_swherequantifier_in_start316);
            r=swherequantifier((scanningOrder27!=null?scanningOrder27.value:null));

            state._fsp--;

            adaptor.addChild(root_0, r.getTree());


            retval.pos = r.xpos;

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
        public int pos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "end"
    // MOVInterpreterTree.g:81:1: end returns [int pos] : scanningOrder r= ewherequantifier[$scanningOrder.value] ;
    public final MOVInterpreterTree.end_return end() throws RecognitionException {
        MOVInterpreterTree.end_return retval = new MOVInterpreterTree.end_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.ewherequantifier_return r =null;

        MOVInterpreterTree.scanningOrder_return scanningOrder28 =null;



        try {
            // MOVInterpreterTree.g:81:22: ( scanningOrder r= ewherequantifier[$scanningOrder.value] )
            // MOVInterpreterTree.g:81:25: scanningOrder r= ewherequantifier[$scanningOrder.value]
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_scanningOrder_in_end330);
            scanningOrder28=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder28.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_ewherequantifier_in_end334);
            r=ewherequantifier((scanningOrder28!=null?scanningOrder28.value:null));

            state._fsp--;

            adaptor.addChild(root_0, r.getTree());


            retval.pos = r.xpos;

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


    public static class dest_return extends TreeRuleReturnScope {
        public int pos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dest"
    // MOVInterpreterTree.g:82:1: dest returns [int pos] : scanningOrder r= dwherequantifier[$scanningOrder.value] ;
    public final MOVInterpreterTree.dest_return dest() throws RecognitionException {
        MOVInterpreterTree.dest_return retval = new MOVInterpreterTree.dest_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        MOVInterpreterTree.dwherequantifier_return r =null;

        MOVInterpreterTree.scanningOrder_return scanningOrder29 =null;



        try {
            // MOVInterpreterTree.g:82:23: ( scanningOrder r= dwherequantifier[$scanningOrder.value] )
            // MOVInterpreterTree.g:82:26: scanningOrder r= dwherequantifier[$scanningOrder.value]
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_scanningOrder_in_dest348);
            scanningOrder29=scanningOrder();

            state._fsp--;

            adaptor.addChild(root_0, scanningOrder29.getTree());


            _last = (CommonTree)input.LT(1);
            pushFollow(FOLLOW_dwherequantifier_in_dest352);
            r=dwherequantifier((scanningOrder29!=null?scanningOrder29.value:null));

            state._fsp--;

            adaptor.addChild(root_0, r.getTree());


            retval.pos = r.xpos;

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
        public int xpos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dwherequantifier"
    // MOVInterpreterTree.g:83:1: dwherequantifier[String order] returns [int xpos] : ( FST (q= INCLD )? x= dtokenspec | LST (p= INCLD )? y= dtokenspec | dnum );
    public final MOVInterpreterTree.dwherequantifier_return dwherequantifier(String order) throws RecognitionException {
        MOVInterpreterTree.dwherequantifier_return retval = new MOVInterpreterTree.dwherequantifier_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree q=null;
        CommonTree p=null;
        CommonTree FST30=null;
        CommonTree LST31=null;
        MOVInterpreterTree.dtokenspec_return x =null;

        MOVInterpreterTree.dtokenspec_return y =null;

        MOVInterpreterTree.dnum_return dnum32 =null;


        CommonTree q_tree=null;
        CommonTree p_tree=null;
        CommonTree FST30_tree=null;
        CommonTree LST31_tree=null;

        try {
            // MOVInterpreterTree.g:84:2: ( FST (q= INCLD )? x= dtokenspec | LST (p= INCLD )? y= dtokenspec | dnum )
            int alt11=3;
            switch ( input.LA(1) ) {
            case FST:
                {
                alt11=1;
                }
                break;
            case LST:
                {
                alt11=2;
                }
                break;
            case NUM:
                {
                alt11=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }

            switch (alt11) {
                case 1 :
                    // MOVInterpreterTree.g:84:4: FST (q= INCLD )? x= dtokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FST30=(CommonTree)match(input,FST,FOLLOW_FST_in_dwherequantifier369); 
                    FST30_tree = (CommonTree)adaptor.dupNode(FST30);


                    adaptor.addChild(root_0, FST30_tree);


                    // MOVInterpreterTree.g:84:9: (q= INCLD )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==INCLD) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // MOVInterpreterTree.g:84:9: q= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            q=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_dwherequantifier373); 
                            q_tree = (CommonTree)adaptor.dupNode(q);


                            adaptor.addChild(root_0, q_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_dtokenspec_in_dwherequantifier378);
                    x=dtokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, x.getTree());


                    retval.xpos = this.ruler.evalPos((FST30!=null?FST30.getText():null)+(q!=null?q.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 2 :
                    // MOVInterpreterTree.g:84:94: LST (p= INCLD )? y= dtokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    LST31=(CommonTree)match(input,LST,FOLLOW_LST_in_dwherequantifier383); 
                    LST31_tree = (CommonTree)adaptor.dupNode(LST31);


                    adaptor.addChild(root_0, LST31_tree);


                    // MOVInterpreterTree.g:84:99: (p= INCLD )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==INCLD) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // MOVInterpreterTree.g:84:99: p= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            p=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_dwherequantifier387); 
                            p_tree = (CommonTree)adaptor.dupNode(p);


                            adaptor.addChild(root_0, p_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_dtokenspec_in_dwherequantifier392);
                    y=dtokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, y.getTree());


                    retval.xpos = this.ruler.evalPos((LST31!=null?LST31.getText():null)+(p!=null?p.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 3 :
                    // MOVInterpreterTree.g:84:183: dnum
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_dnum_in_dwherequantifier396);
                    dnum32=dnum();

                    state._fsp--;

                    adaptor.addChild(root_0, dnum32.getTree());


                    retval.xpos =this.ruler.evalPos((dnum32!=null?(input.getTokenStream().toString(input.getTreeAdaptor().getTokenStartIndex(dnum32.start),input.getTreeAdaptor().getTokenStopIndex(dnum32.start))):null),null,order);

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


    public static class swherequantifier_return extends TreeRuleReturnScope {
        public int xpos;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "swherequantifier"
    // MOVInterpreterTree.g:85:1: swherequantifier[String order] returns [int xpos] : ( FST (q= INCLD )? x= stokenspec | LST (p= INCLD )? y= stokenspec | snum );
    public final MOVInterpreterTree.swherequantifier_return swherequantifier(String order) throws RecognitionException {
        MOVInterpreterTree.swherequantifier_return retval = new MOVInterpreterTree.swherequantifier_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree q=null;
        CommonTree p=null;
        CommonTree FST33=null;
        CommonTree LST34=null;
        MOVInterpreterTree.stokenspec_return x =null;

        MOVInterpreterTree.stokenspec_return y =null;

        MOVInterpreterTree.snum_return snum35 =null;


        CommonTree q_tree=null;
        CommonTree p_tree=null;
        CommonTree FST33_tree=null;
        CommonTree LST34_tree=null;

        try {
            // MOVInterpreterTree.g:86:2: ( FST (q= INCLD )? x= stokenspec | LST (p= INCLD )? y= stokenspec | snum )
            int alt14=3;
            switch ( input.LA(1) ) {
            case FST:
                {
                alt14=1;
                }
                break;
            case LST:
                {
                alt14=2;
                }
                break;
            case NUM:
                {
                alt14=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }

            switch (alt14) {
                case 1 :
                    // MOVInterpreterTree.g:86:4: FST (q= INCLD )? x= stokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FST33=(CommonTree)match(input,FST,FOLLOW_FST_in_swherequantifier412); 
                    FST33_tree = (CommonTree)adaptor.dupNode(FST33);


                    adaptor.addChild(root_0, FST33_tree);


                    // MOVInterpreterTree.g:86:9: (q= INCLD )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==INCLD) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // MOVInterpreterTree.g:86:9: q= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            q=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_swherequantifier416); 
                            q_tree = (CommonTree)adaptor.dupNode(q);


                            adaptor.addChild(root_0, q_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_stokenspec_in_swherequantifier421);
                    x=stokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, x.getTree());


                    retval.xpos = this.ruler.evalPos((FST33!=null?FST33.getText():null)+(q!=null?q.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 2 :
                    // MOVInterpreterTree.g:86:94: LST (p= INCLD )? y= stokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    LST34=(CommonTree)match(input,LST,FOLLOW_LST_in_swherequantifier426); 
                    LST34_tree = (CommonTree)adaptor.dupNode(LST34);


                    adaptor.addChild(root_0, LST34_tree);


                    // MOVInterpreterTree.g:86:99: (p= INCLD )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==INCLD) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // MOVInterpreterTree.g:86:99: p= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            p=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_swherequantifier430); 
                            p_tree = (CommonTree)adaptor.dupNode(p);


                            adaptor.addChild(root_0, p_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_stokenspec_in_swherequantifier435);
                    y=stokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, y.getTree());


                    retval.xpos = this.ruler.evalPos((LST34!=null?LST34.getText():null)+(p!=null?p.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 3 :
                    // MOVInterpreterTree.g:86:183: snum
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_snum_in_swherequantifier439);
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
    // MOVInterpreterTree.g:87:1: ewherequantifier[String order] returns [int xpos] : ( FST (q= INCLD )? x= etokenspec | LST (p= INCLD )? y= etokenspec | tnum );
    public final MOVInterpreterTree.ewherequantifier_return ewherequantifier(String order) throws RecognitionException {
        MOVInterpreterTree.ewherequantifier_return retval = new MOVInterpreterTree.ewherequantifier_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree q=null;
        CommonTree p=null;
        CommonTree FST36=null;
        CommonTree LST37=null;
        MOVInterpreterTree.etokenspec_return x =null;

        MOVInterpreterTree.etokenspec_return y =null;

        MOVInterpreterTree.tnum_return tnum38 =null;


        CommonTree q_tree=null;
        CommonTree p_tree=null;
        CommonTree FST36_tree=null;
        CommonTree LST37_tree=null;

        try {
            // MOVInterpreterTree.g:88:2: ( FST (q= INCLD )? x= etokenspec | LST (p= INCLD )? y= etokenspec | tnum )
            int alt17=3;
            switch ( input.LA(1) ) {
            case FST:
                {
                alt17=1;
                }
                break;
            case LST:
                {
                alt17=2;
                }
                break;
            case NUM:
                {
                alt17=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;

            }

            switch (alt17) {
                case 1 :
                    // MOVInterpreterTree.g:88:4: FST (q= INCLD )? x= etokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    FST36=(CommonTree)match(input,FST,FOLLOW_FST_in_ewherequantifier455); 
                    FST36_tree = (CommonTree)adaptor.dupNode(FST36);


                    adaptor.addChild(root_0, FST36_tree);


                    // MOVInterpreterTree.g:88:9: (q= INCLD )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==INCLD) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // MOVInterpreterTree.g:88:9: q= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            q=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_ewherequantifier459); 
                            q_tree = (CommonTree)adaptor.dupNode(q);


                            adaptor.addChild(root_0, q_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_etokenspec_in_ewherequantifier464);
                    x=etokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, x.getTree());


                    retval.xpos = this.ruler.evalPos((FST36!=null?FST36.getText():null)+(q!=null?q.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 2 :
                    // MOVInterpreterTree.g:88:94: LST (p= INCLD )? y= etokenspec
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    LST37=(CommonTree)match(input,LST,FOLLOW_LST_in_ewherequantifier469); 
                    LST37_tree = (CommonTree)adaptor.dupNode(LST37);


                    adaptor.addChild(root_0, LST37_tree);


                    // MOVInterpreterTree.g:88:99: (p= INCLD )?
                    int alt16=2;
                    int LA16_0 = input.LA(1);

                    if ( (LA16_0==INCLD) ) {
                        alt16=1;
                    }
                    switch (alt16) {
                        case 1 :
                            // MOVInterpreterTree.g:88:99: p= INCLD
                            {
                            _last = (CommonTree)input.LT(1);
                            p=(CommonTree)match(input,INCLD,FOLLOW_INCLD_in_ewherequantifier473); 
                            p_tree = (CommonTree)adaptor.dupNode(p);


                            adaptor.addChild(root_0, p_tree);


                            }
                            break;

                    }


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_etokenspec_in_ewherequantifier478);
                    y=etokenspec();

                    state._fsp--;

                    adaptor.addChild(root_0, y.getTree());


                    retval.xpos = this.ruler.evalPos((LST37!=null?LST37.getText():null)+(p!=null?p.getText():null),(x!=null?x.res:null),order);

                    }
                    break;
                case 3 :
                    // MOVInterpreterTree.g:88:183: tnum
                    {
                    root_0 = (CommonTree)adaptor.nil();


                    _last = (CommonTree)input.LT(1);
                    pushFollow(FOLLOW_tnum_in_ewherequantifier482);
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
    // MOVInterpreterTree.g:89:1: snum returns [ String x] : NUM ;
    public final MOVInterpreterTree.snum_return snum() throws RecognitionException {
        MOVInterpreterTree.snum_return retval = new MOVInterpreterTree.snum_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUM39=null;

        CommonTree NUM39_tree=null;

        try {
            // MOVInterpreterTree.g:89:25: ( NUM )
            // MOVInterpreterTree.g:89:27: NUM
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            NUM39=(CommonTree)match(input,NUM,FOLLOW_NUM_in_snum494); 
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
    // MOVInterpreterTree.g:90:1: tnum returns [String x] : NUM ;
    public final MOVInterpreterTree.tnum_return tnum() throws RecognitionException {
        MOVInterpreterTree.tnum_return retval = new MOVInterpreterTree.tnum_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUM40=null;

        CommonTree NUM40_tree=null;

        try {
            // MOVInterpreterTree.g:90:24: ( NUM )
            // MOVInterpreterTree.g:90:26: NUM
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            NUM40=(CommonTree)match(input,NUM,FOLLOW_NUM_in_tnum505); 
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


    public static class dnum_return extends TreeRuleReturnScope {
        public String x;
        CommonTree tree;
        public Object getTree() { return tree; }
    };


    // $ANTLR start "dnum"
    // MOVInterpreterTree.g:91:1: dnum returns [String x] : NUM ;
    public final MOVInterpreterTree.dnum_return dnum() throws RecognitionException {
        MOVInterpreterTree.dnum_return retval = new MOVInterpreterTree.dnum_return();
        retval.start = input.LT(1);


        CommonTree root_0 = null;

        CommonTree _first_0 = null;
        CommonTree _last = null;

        CommonTree NUM41=null;

        CommonTree NUM41_tree=null;

        try {
            // MOVInterpreterTree.g:91:24: ( NUM )
            // MOVInterpreterTree.g:91:26: NUM
            {
            root_0 = (CommonTree)adaptor.nil();


            _last = (CommonTree)input.LT(1);
            NUM41=(CommonTree)match(input,NUM,FOLLOW_NUM_in_dnum516); 
            NUM41_tree = (CommonTree)adaptor.dupNode(NUM41);


            adaptor.addChild(root_0, NUM41_tree);


            retval.x =(NUM41!=null?NUM41.getText():null);

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


 

    public static final BitSet FOLLOW_operator_in_rule71 = new BitSet(new long[]{0x0000000000078030L});
    public static final BitSet FOLLOW_what_in_rule73 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_where_in_rule75 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_dest_in_rule77 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tokenspec_in_what90 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ANYTOKS_in_what93 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec113 = new BitSet(new long[]{0x0000000000078022L});
    public static final BitSet FOLLOW_singletokenspec_in_tokenspec117 = new BitSet(new long[]{0x0000000000078022L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec139 = new BitSet(new long[]{0x0000000000078022L});
    public static final BitSet FOLLOW_singletokenspec_in_stokenspec143 = new BitSet(new long[]{0x0000000000078022L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec165 = new BitSet(new long[]{0x0000000000078022L});
    public static final BitSet FOLLOW_singletokenspec_in_etokenspec169 = new BitSet(new long[]{0x0000000000078022L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec191 = new BitSet(new long[]{0x0000000000078022L});
    public static final BitSet FOLLOW_singletokenspec_in_dtokenspec195 = new BitSet(new long[]{0x0000000000078022L});
    public static final BitSet FOLLOW_token_in_singletokenspec210 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_singletokenspec213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MOV_in_operator226 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMTYP_in_type240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WRDTYP_in_type244 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SYBTYP_in_type248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BNKTYP_in_type252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TOKEN_in_token264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_start_in_where278 = new BitSet(new long[]{0x0000000000000180L});
    public static final BitSet FOLLOW_end_in_where280 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FRMB_in_scanningOrder294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FRME_in_scanningOrder298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_start312 = new BitSet(new long[]{0x0000000000004A00L});
    public static final BitSet FOLLOW_swherequantifier_in_start316 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_end330 = new BitSet(new long[]{0x0000000000004A00L});
    public static final BitSet FOLLOW_ewherequantifier_in_end334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scanningOrder_in_dest348 = new BitSet(new long[]{0x0000000000004A00L});
    public static final BitSet FOLLOW_dwherequantifier_in_dest352 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_dwherequantifier369 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_dwherequantifier373 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_dtokenspec_in_dwherequantifier378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LST_in_dwherequantifier383 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_dwherequantifier387 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_dtokenspec_in_dwherequantifier392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dnum_in_dwherequantifier396 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_swherequantifier412 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_swherequantifier416 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_stokenspec_in_swherequantifier421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LST_in_swherequantifier426 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_swherequantifier430 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_stokenspec_in_swherequantifier435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_snum_in_swherequantifier439 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FST_in_ewherequantifier455 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_ewherequantifier459 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_etokenspec_in_ewherequantifier464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LST_in_ewherequantifier469 = new BitSet(new long[]{0x0000000000078420L});
    public static final BitSet FOLLOW_INCLD_in_ewherequantifier473 = new BitSet(new long[]{0x0000000000078020L});
    public static final BitSet FOLLOW_etokenspec_in_ewherequantifier478 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tnum_in_ewherequantifier482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_snum494 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_tnum505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUM_in_dnum516 = new BitSet(new long[]{0x0000000000000002L});

}
