/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *  
 *    This code was developed by the Information Integration Group as part 
 *    of the Karma project at the Information Sciences Institute of the 
 *    University of Southern California.  For more information, publications, 
 *    and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.mediator.domain.parser.grammar;

// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g 2011-07-20 11:01:06

import org.antlr.runtime.BitSet;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.RewriteEarlyExitException;
import org.antlr.runtime.tree.RewriteRuleSubtreeStream;
import org.antlr.runtime.tree.RewriteRuleTokenStream;
import org.antlr.runtime.tree.TreeAdaptor;

@SuppressWarnings("all")
public class DomainModelParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "DOMAIN_MODEL", "SOURCE_SCHEMA", "DOMAIN_SCHEMA", "GAV_RULES", "LAV_RULES", "GLAV_RULES", "UAC_RULES", "QUERIES", "RULE", "ANTECEDENT", "CONSEQUENT", "RELATION_PRED", "FUNCTION_PRED", "BUILTIN_PRED", "FUNCTIONS", "NAMESPACES", "IS_NULL", "ISNOT_NULL", "IN", "NOT_IN", "STRING", "INT", "FLOAT", "ID", "NUMERIC", "WS", "COMMENT", "'SOURCE_SCHEMA:'", "'DOMAIN_SCHEMA:'", "'FUNCTIONS:'", "'NAMESPACES:'", "':'", "'('", "','", "')'", "'LAV_RULES:'", "'GLAV_RULES:'", "'GAV_RULES:'", "'UAC_RULES:'", "'QUERIES:'", "'<-'", "'::'", "':.'", "'->'", "'^'", "'NULL'", "'['", "']'", "'='", "'!='", "'<'", "'>'", "'>='", "'<='", "'<>'", "'LIKE'", "'IS'", "'NOT'", "'IN'"
    };
    public static final int GLAV_RULES=9;
    public static final int T__62=62;
    public static final int FUNCTIONS=18;
    public static final int FLOAT=26;
    public static final int ID=27;
    public static final int T__61=61;
    public static final int ANTECEDENT=13;
    public static final int EOF=-1;
    public static final int T__60=60;
    public static final int DOMAIN_MODEL=4;
    public static final int GAV_RULES=7;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int IN=22;
    public static final int T__52=52;
    public static final int IS_NULL=20;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int FUNCTION_PRED=16;
    public static final int T__59=59;
    public static final int COMMENT=30;
    public static final int T__50=50;
    public static final int LAV_RULES=8;
    public static final int BUILTIN_PRED=17;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int RULE=12;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int UAC_RULES=10;
    public static final int CONSEQUENT=14;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int DOMAIN_SCHEMA=6;
    public static final int INT=25;
    public static final int NUMERIC=28;
    public static final int ISNOT_NULL=21;
    public static final int RELATION_PRED=15;
    public static final int SOURCE_SCHEMA=5;
    public static final int NAMESPACES=19;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int WS=29;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int QUERIES=11;
    public static final int T__39=39;
    public static final int NOT_IN=23;
    public static final int STRING=24;

    // delegates
    // delegators


        public DomainModelParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DomainModelParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return DomainModelParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g"; }


    protected void mismatch(IntStream input, int ttype, BitSet follow)
        throws RecognitionException
    { 
        throw new MismatchedTokenException(ttype, input);
    }

    public Object recoverFromMismatchedSet(IntStream input, RecognitionException re, BitSet follow)
        throws RecognitionException
    {
        throw re;
    }


    public static class domain_model_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "domain_model"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:80:1: domain_model : ( section )* -> ^( DOMAIN_MODEL ( section )+ ) ;
    public final DomainModelParser.domain_model_return domain_model() throws RecognitionException {
        DomainModelParser.domain_model_return retval = new DomainModelParser.domain_model_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        DomainModelParser.section_return section1 = null;


        RewriteRuleSubtreeStream stream_section=new RewriteRuleSubtreeStream(adaptor,"rule section");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:81:2: ( ( section )* -> ^( DOMAIN_MODEL ( section )+ ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:81:4: ( section )*
            {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:81:4: ( section )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=31 && LA1_0<=34)||(LA1_0>=39 && LA1_0<=43)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:81:5: section
            	    {
            	    pushFollow(FOLLOW_section_in_domain_model157);
            	    section1=section();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_section.add(section1.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);



            // AST REWRITE
            // elements: section
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 81:15: -> ^( DOMAIN_MODEL ( section )+ )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:81:18: ^( DOMAIN_MODEL ( section )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DOMAIN_MODEL, "DOMAIN_MODEL"), root_1);

                if ( !(stream_section.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_section.hasNext() ) {
                    adaptor.addChild(root_1, stream_section.nextTree());

                }
                stream_section.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "domain_model"

    public static class section_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "section"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:83:1: section : ( schema | functions | rules | namespaces );
    public final DomainModelParser.section_return section() throws RecognitionException {
        DomainModelParser.section_return retval = new DomainModelParser.section_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        DomainModelParser.schema_return schema2 = null;

        DomainModelParser.functions_return functions3 = null;

        DomainModelParser.rules_return rules4 = null;

        DomainModelParser.namespaces_return namespaces5 = null;



        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:84:2: ( schema | functions | rules | namespaces )
            int alt2=4;
            switch ( input.LA(1) ) {
            case 31:
            case 32:
                {
                alt2=1;
                }
                break;
            case 33:
                {
                alt2=2;
                }
                break;
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
                {
                alt2=3;
                }
                break;
            case 34:
                {
                alt2=4;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }

            switch (alt2) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:84:4: schema
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_schema_in_section177);
                    schema2=schema();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, schema2.getTree());

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:84:13: functions
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_functions_in_section181);
                    functions3=functions();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, functions3.getTree());

                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:84:25: rules
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_rules_in_section185);
                    rules4=rules();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, rules4.getTree());

                    }
                    break;
                case 4 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:84:33: namespaces
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_namespaces_in_section189);
                    namespaces5=namespaces();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, namespaces5.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "section"

    public static class schema_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "schema"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:86:1: schema : schema_name ( schema_exp )* -> ^( schema_name ( schema_exp )* ) ;
    public final DomainModelParser.schema_return schema() throws RecognitionException {
        DomainModelParser.schema_return retval = new DomainModelParser.schema_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        DomainModelParser.schema_name_return schema_name6 = null;

        DomainModelParser.schema_exp_return schema_exp7 = null;


        RewriteRuleSubtreeStream stream_schema_name=new RewriteRuleSubtreeStream(adaptor,"rule schema_name");
        RewriteRuleSubtreeStream stream_schema_exp=new RewriteRuleSubtreeStream(adaptor,"rule schema_exp");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:87:2: ( schema_name ( schema_exp )* -> ^( schema_name ( schema_exp )* ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:87:4: schema_name ( schema_exp )*
            {
            pushFollow(FOLLOW_schema_name_in_schema199);
            schema_name6=schema_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_schema_name.add(schema_name6.getTree());
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:87:16: ( schema_exp )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==ID) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:87:17: schema_exp
            	    {
            	    pushFollow(FOLLOW_schema_exp_in_schema202);
            	    schema_exp7=schema_exp();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_schema_exp.add(schema_exp7.getTree());

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);



            // AST REWRITE
            // elements: schema_exp, schema_name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 87:30: -> ^( schema_name ( schema_exp )* )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:87:33: ^( schema_name ( schema_exp )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_schema_name.nextNode(), root_1);

                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:87:47: ( schema_exp )*
                while ( stream_schema_exp.hasNext() ) {
                    adaptor.addChild(root_1, stream_schema_exp.nextTree());

                }
                stream_schema_exp.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "schema"

    public static class schema_name_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "schema_name"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:89:1: schema_name : ( 'SOURCE_SCHEMA:' -> ^( SOURCE_SCHEMA ) | 'DOMAIN_SCHEMA:' -> ^( DOMAIN_SCHEMA ) );
    public final DomainModelParser.schema_name_return schema_name() throws RecognitionException {
        DomainModelParser.schema_name_return retval = new DomainModelParser.schema_name_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal8=null;
        Token string_literal9=null;

        CommonTree string_literal8_tree=null;
        CommonTree string_literal9_tree=null;
        RewriteRuleTokenStream stream_32=new RewriteRuleTokenStream(adaptor,"token 32");
        RewriteRuleTokenStream stream_31=new RewriteRuleTokenStream(adaptor,"token 31");

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:90:2: ( 'SOURCE_SCHEMA:' -> ^( SOURCE_SCHEMA ) | 'DOMAIN_SCHEMA:' -> ^( DOMAIN_SCHEMA ) )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==31) ) {
                alt4=1;
            }
            else if ( (LA4_0==32) ) {
                alt4=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:90:4: 'SOURCE_SCHEMA:'
                    {
                    string_literal8=(Token)match(input,31,FOLLOW_31_in_schema_name222); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_31.add(string_literal8);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 90:21: -> ^( SOURCE_SCHEMA )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:90:24: ^( SOURCE_SCHEMA )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(SOURCE_SCHEMA, "SOURCE_SCHEMA"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:91:4: 'DOMAIN_SCHEMA:'
                    {
                    string_literal9=(Token)match(input,32,FOLLOW_32_in_schema_name233); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_32.add(string_literal9);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 91:21: -> ^( DOMAIN_SCHEMA )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:91:24: ^( DOMAIN_SCHEMA )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(DOMAIN_SCHEMA, "DOMAIN_SCHEMA"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "schema_name"

    public static class functions_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "functions"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:93:1: functions : 'FUNCTIONS:' ( function_name )* -> ^( FUNCTIONS ( function_name )* ) ;
    public final DomainModelParser.functions_return functions() throws RecognitionException {
        DomainModelParser.functions_return retval = new DomainModelParser.functions_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal10=null;
        DomainModelParser.function_name_return function_name11 = null;


        CommonTree string_literal10_tree=null;
        RewriteRuleTokenStream stream_33=new RewriteRuleTokenStream(adaptor,"token 33");
        RewriteRuleSubtreeStream stream_function_name=new RewriteRuleSubtreeStream(adaptor,"rule function_name");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:94:2: ( 'FUNCTIONS:' ( function_name )* -> ^( FUNCTIONS ( function_name )* ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:94:4: 'FUNCTIONS:' ( function_name )*
            {
            string_literal10=(Token)match(input,33,FOLLOW_33_in_functions249); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_33.add(string_literal10);

            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:94:17: ( function_name )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==ID) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:94:18: function_name
            	    {
            	    pushFollow(FOLLOW_function_name_in_functions252);
            	    function_name11=function_name();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_function_name.add(function_name11.getTree());

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);



            // AST REWRITE
            // elements: function_name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 94:34: -> ^( FUNCTIONS ( function_name )* )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:94:37: ^( FUNCTIONS ( function_name )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FUNCTIONS, "FUNCTIONS"), root_1);

                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:94:49: ( function_name )*
                while ( stream_function_name.hasNext() ) {
                    adaptor.addChild(root_1, stream_function_name.nextTree());

                }
                stream_function_name.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "functions"

    public static class namespaces_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "namespaces"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:96:1: namespaces : 'NAMESPACES:' ( namespace )* -> ^( NAMESPACES ( namespace )* ) ;
    public final DomainModelParser.namespaces_return namespaces() throws RecognitionException {
        DomainModelParser.namespaces_return retval = new DomainModelParser.namespaces_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal12=null;
        DomainModelParser.namespace_return namespace13 = null;


        CommonTree string_literal12_tree=null;
        RewriteRuleTokenStream stream_34=new RewriteRuleTokenStream(adaptor,"token 34");
        RewriteRuleSubtreeStream stream_namespace=new RewriteRuleSubtreeStream(adaptor,"rule namespace");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:97:2: ( 'NAMESPACES:' ( namespace )* -> ^( NAMESPACES ( namespace )* ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:97:4: 'NAMESPACES:' ( namespace )*
            {
            string_literal12=(Token)match(input,34,FOLLOW_34_in_namespaces273); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_34.add(string_literal12);

            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:97:18: ( namespace )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==ID) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:97:19: namespace
            	    {
            	    pushFollow(FOLLOW_namespace_in_namespaces276);
            	    namespace13=namespace();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_namespace.add(namespace13.getTree());

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);



            // AST REWRITE
            // elements: namespace
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 97:31: -> ^( NAMESPACES ( namespace )* )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:97:34: ^( NAMESPACES ( namespace )* )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NAMESPACES, "NAMESPACES"), root_1);

                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:97:47: ( namespace )*
                while ( stream_namespace.hasNext() ) {
                    adaptor.addChild(root_1, stream_namespace.nextTree());

                }
                stream_namespace.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "namespaces"

    public static class namespace_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "namespace"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:99:1: namespace : namespace_prefix ':' namespace_uri -> ^( namespace_prefix namespace_uri ) ;
    public final DomainModelParser.namespace_return namespace() throws RecognitionException {
        DomainModelParser.namespace_return retval = new DomainModelParser.namespace_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal15=null;
        DomainModelParser.namespace_prefix_return namespace_prefix14 = null;

        DomainModelParser.namespace_uri_return namespace_uri16 = null;


        CommonTree char_literal15_tree=null;
        RewriteRuleTokenStream stream_35=new RewriteRuleTokenStream(adaptor,"token 35");
        RewriteRuleSubtreeStream stream_namespace_prefix=new RewriteRuleSubtreeStream(adaptor,"rule namespace_prefix");
        RewriteRuleSubtreeStream stream_namespace_uri=new RewriteRuleSubtreeStream(adaptor,"rule namespace_uri");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:100:2: ( namespace_prefix ':' namespace_uri -> ^( namespace_prefix namespace_uri ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:100:4: namespace_prefix ':' namespace_uri
            {
            pushFollow(FOLLOW_namespace_prefix_in_namespace297);
            namespace_prefix14=namespace_prefix();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_namespace_prefix.add(namespace_prefix14.getTree());
            char_literal15=(Token)match(input,35,FOLLOW_35_in_namespace299); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_35.add(char_literal15);

            pushFollow(FOLLOW_namespace_uri_in_namespace301);
            namespace_uri16=namespace_uri();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_namespace_uri.add(namespace_uri16.getTree());


            // AST REWRITE
            // elements: namespace_prefix, namespace_uri
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 100:39: -> ^( namespace_prefix namespace_uri )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:100:42: ^( namespace_prefix namespace_uri )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_namespace_prefix.nextNode(), root_1);

                adaptor.addChild(root_1, stream_namespace_uri.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "namespace"

    public static class schema_exp_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "schema_exp"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:102:1: schema_exp : table_name '(' column_identifier ( ',' column_identifier )* ')' -> ^( table_name ( column_identifier )+ ) ;
    public final DomainModelParser.schema_exp_return schema_exp() throws RecognitionException {
        DomainModelParser.schema_exp_return retval = new DomainModelParser.schema_exp_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal18=null;
        Token char_literal20=null;
        Token char_literal22=null;
        DomainModelParser.table_name_return table_name17 = null;

        DomainModelParser.column_identifier_return column_identifier19 = null;

        DomainModelParser.column_identifier_return column_identifier21 = null;


        CommonTree char_literal18_tree=null;
        CommonTree char_literal20_tree=null;
        CommonTree char_literal22_tree=null;
        RewriteRuleTokenStream stream_36=new RewriteRuleTokenStream(adaptor,"token 36");
        RewriteRuleTokenStream stream_37=new RewriteRuleTokenStream(adaptor,"token 37");
        RewriteRuleTokenStream stream_38=new RewriteRuleTokenStream(adaptor,"token 38");
        RewriteRuleSubtreeStream stream_table_name=new RewriteRuleSubtreeStream(adaptor,"rule table_name");
        RewriteRuleSubtreeStream stream_column_identifier=new RewriteRuleSubtreeStream(adaptor,"rule column_identifier");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:103:2: ( table_name '(' column_identifier ( ',' column_identifier )* ')' -> ^( table_name ( column_identifier )+ ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:103:4: table_name '(' column_identifier ( ',' column_identifier )* ')'
            {
            pushFollow(FOLLOW_table_name_in_schema_exp318);
            table_name17=table_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_table_name.add(table_name17.getTree());
            char_literal18=(Token)match(input,36,FOLLOW_36_in_schema_exp320); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_36.add(char_literal18);

            pushFollow(FOLLOW_column_identifier_in_schema_exp322);
            column_identifier19=column_identifier();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_column_identifier.add(column_identifier19.getTree());
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:103:37: ( ',' column_identifier )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==37) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:103:38: ',' column_identifier
            	    {
            	    char_literal20=(Token)match(input,37,FOLLOW_37_in_schema_exp325); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_37.add(char_literal20);

            	    pushFollow(FOLLOW_column_identifier_in_schema_exp327);
            	    column_identifier21=column_identifier();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_column_identifier.add(column_identifier21.getTree());

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            char_literal22=(Token)match(input,38,FOLLOW_38_in_schema_exp331); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_38.add(char_literal22);



            // AST REWRITE
            // elements: table_name, column_identifier
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 103:66: -> ^( table_name ( column_identifier )+ )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:103:69: ^( table_name ( column_identifier )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_table_name.nextNode(), root_1);

                if ( !(stream_column_identifier.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_column_identifier.hasNext() ) {
                    adaptor.addChild(root_1, stream_column_identifier.nextTree());

                }
                stream_column_identifier.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "schema_exp"

    public static class column_identifier_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "column_identifier"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:105:1: column_identifier : column_name ':' column_type ( ':' column_binding )? -> ^( column_name column_type ( column_binding )? ) ;
    public final DomainModelParser.column_identifier_return column_identifier() throws RecognitionException {
        DomainModelParser.column_identifier_return retval = new DomainModelParser.column_identifier_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal24=null;
        Token char_literal26=null;
        DomainModelParser.column_name_return column_name23 = null;

        DomainModelParser.column_type_return column_type25 = null;

        DomainModelParser.column_binding_return column_binding27 = null;


        CommonTree char_literal24_tree=null;
        CommonTree char_literal26_tree=null;
        RewriteRuleTokenStream stream_35=new RewriteRuleTokenStream(adaptor,"token 35");
        RewriteRuleSubtreeStream stream_column_binding=new RewriteRuleSubtreeStream(adaptor,"rule column_binding");
        RewriteRuleSubtreeStream stream_column_type=new RewriteRuleSubtreeStream(adaptor,"rule column_type");
        RewriteRuleSubtreeStream stream_column_name=new RewriteRuleSubtreeStream(adaptor,"rule column_name");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:106:2: ( column_name ':' column_type ( ':' column_binding )? -> ^( column_name column_type ( column_binding )? ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:106:5: column_name ':' column_type ( ':' column_binding )?
            {
            pushFollow(FOLLOW_column_name_in_column_identifier350);
            column_name23=column_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_column_name.add(column_name23.getTree());
            char_literal24=(Token)match(input,35,FOLLOW_35_in_column_identifier352); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_35.add(char_literal24);

            pushFollow(FOLLOW_column_type_in_column_identifier354);
            column_type25=column_type();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_column_type.add(column_type25.getTree());
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:106:33: ( ':' column_binding )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==35) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:106:34: ':' column_binding
                    {
                    char_literal26=(Token)match(input,35,FOLLOW_35_in_column_identifier357); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_35.add(char_literal26);

                    pushFollow(FOLLOW_column_binding_in_column_identifier359);
                    column_binding27=column_binding();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_column_binding.add(column_binding27.getTree());

                    }
                    break;

            }



            // AST REWRITE
            // elements: column_binding, column_type, column_name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 106:56: -> ^( column_name column_type ( column_binding )? )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:106:59: ^( column_name column_type ( column_binding )? )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot(stream_column_name.nextNode(), root_1);

                adaptor.addChild(root_1, stream_column_type.nextTree());
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:106:85: ( column_binding )?
                if ( stream_column_binding.hasNext() ) {
                    adaptor.addChild(root_1, stream_column_binding.nextTree());

                }
                stream_column_binding.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "column_identifier"

    public static class rules_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rules"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:108:1: rules : ( rule_name ( rule )* -> ^( rule_name ( rule )+ ) | 'LAV_RULES:' ( lav_rule )* -> ^( LAV_RULES ( lav_rule )+ ) | 'GLAV_RULES:' ( glav_rule )* -> ^( GLAV_RULES ( glav_rule )+ ) );
    public final DomainModelParser.rules_return rules() throws RecognitionException {
        DomainModelParser.rules_return retval = new DomainModelParser.rules_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal30=null;
        Token string_literal32=null;
        DomainModelParser.rule_name_return rule_name28 = null;

        DomainModelParser.rule_return rule29 = null;

        DomainModelParser.lav_rule_return lav_rule31 = null;

        DomainModelParser.glav_rule_return glav_rule33 = null;


        CommonTree string_literal30_tree=null;
        CommonTree string_literal32_tree=null;
        RewriteRuleTokenStream stream_40=new RewriteRuleTokenStream(adaptor,"token 40");
        RewriteRuleTokenStream stream_39=new RewriteRuleTokenStream(adaptor,"token 39");
        RewriteRuleSubtreeStream stream_lav_rule=new RewriteRuleSubtreeStream(adaptor,"rule lav_rule");
        RewriteRuleSubtreeStream stream_rule=new RewriteRuleSubtreeStream(adaptor,"rule rule");
        RewriteRuleSubtreeStream stream_glav_rule=new RewriteRuleSubtreeStream(adaptor,"rule glav_rule");
        RewriteRuleSubtreeStream stream_rule_name=new RewriteRuleSubtreeStream(adaptor,"rule rule_name");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:109:2: ( rule_name ( rule )* -> ^( rule_name ( rule )+ ) | 'LAV_RULES:' ( lav_rule )* -> ^( LAV_RULES ( lav_rule )+ ) | 'GLAV_RULES:' ( glav_rule )* -> ^( GLAV_RULES ( glav_rule )+ ) )
            int alt12=3;
            switch ( input.LA(1) ) {
            case 41:
            case 42:
            case 43:
                {
                alt12=1;
                }
                break;
            case 39:
                {
                alt12=2;
                }
                break;
            case 40:
                {
                alt12=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:109:4: rule_name ( rule )*
                    {
                    pushFollow(FOLLOW_rule_name_in_rules384);
                    rule_name28=rule_name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rule_name.add(rule_name28.getTree());
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:109:14: ( rule )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( (LA9_0==ID) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:109:15: rule
                    	    {
                    	    pushFollow(FOLLOW_rule_in_rules387);
                    	    rule29=rule();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_rule.add(rule29.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: rule, rule_name
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 109:22: -> ^( rule_name ( rule )+ )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:109:25: ^( rule_name ( rule )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot(stream_rule_name.nextNode(), root_1);

                        if ( !(stream_rule.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_rule.hasNext() ) {
                            adaptor.addChild(root_1, stream_rule.nextTree());

                        }
                        stream_rule.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:110:4: 'LAV_RULES:' ( lav_rule )*
                    {
                    string_literal30=(Token)match(input,39,FOLLOW_39_in_rules403); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_39.add(string_literal30);

                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:110:17: ( lav_rule )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( (LA10_0==ID) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:110:18: lav_rule
                    	    {
                    	    pushFollow(FOLLOW_lav_rule_in_rules406);
                    	    lav_rule31=lav_rule();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_lav_rule.add(lav_rule31.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: lav_rule
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 110:29: -> ^( LAV_RULES ( lav_rule )+ )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:110:32: ^( LAV_RULES ( lav_rule )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(LAV_RULES, "LAV_RULES"), root_1);

                        if ( !(stream_lav_rule.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_lav_rule.hasNext() ) {
                            adaptor.addChild(root_1, stream_lav_rule.nextTree());

                        }
                        stream_lav_rule.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:111:4: 'GLAV_RULES:' ( glav_rule )*
                    {
                    string_literal32=(Token)match(input,40,FOLLOW_40_in_rules422); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_40.add(string_literal32);

                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:111:18: ( glav_rule )*
                    loop11:
                    do {
                        int alt11=2;
                        int LA11_0 = input.LA(1);

                        if ( (LA11_0==ID||LA11_0==36) ) {
                            alt11=1;
                        }


                        switch (alt11) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:111:19: glav_rule
                    	    {
                    	    pushFollow(FOLLOW_glav_rule_in_rules425);
                    	    glav_rule33=glav_rule();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_glav_rule.add(glav_rule33.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop11;
                        }
                    } while (true);



                    // AST REWRITE
                    // elements: glav_rule
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 111:31: -> ^( GLAV_RULES ( glav_rule )+ )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:111:34: ^( GLAV_RULES ( glav_rule )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(GLAV_RULES, "GLAV_RULES"), root_1);

                        if ( !(stream_glav_rule.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_glav_rule.hasNext() ) {
                            adaptor.addChild(root_1, stream_glav_rule.nextTree());

                        }
                        stream_glav_rule.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rules"

    public static class rule_name_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rule_name"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:113:1: rule_name : ( 'GAV_RULES:' -> ^( GAV_RULES ) | 'UAC_RULES:' -> ^( UAC_RULES ) | 'QUERIES:' -> ^( QUERIES ) );
    public final DomainModelParser.rule_name_return rule_name() throws RecognitionException {
        DomainModelParser.rule_name_return retval = new DomainModelParser.rule_name_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal34=null;
        Token string_literal35=null;
        Token string_literal36=null;

        CommonTree string_literal34_tree=null;
        CommonTree string_literal35_tree=null;
        CommonTree string_literal36_tree=null;
        RewriteRuleTokenStream stream_43=new RewriteRuleTokenStream(adaptor,"token 43");
        RewriteRuleTokenStream stream_42=new RewriteRuleTokenStream(adaptor,"token 42");
        RewriteRuleTokenStream stream_41=new RewriteRuleTokenStream(adaptor,"token 41");

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:114:2: ( 'GAV_RULES:' -> ^( GAV_RULES ) | 'UAC_RULES:' -> ^( UAC_RULES ) | 'QUERIES:' -> ^( QUERIES ) )
            int alt13=3;
            switch ( input.LA(1) ) {
            case 41:
                {
                alt13=1;
                }
                break;
            case 42:
                {
                alt13=2;
                }
                break;
            case 43:
                {
                alt13=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }

            switch (alt13) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:114:4: 'GAV_RULES:'
                    {
                    string_literal34=(Token)match(input,41,FOLLOW_41_in_rule_name446); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_41.add(string_literal34);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 114:17: -> ^( GAV_RULES )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:114:20: ^( GAV_RULES )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(GAV_RULES, "GAV_RULES"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:115:4: 'UAC_RULES:'
                    {
                    string_literal35=(Token)match(input,42,FOLLOW_42_in_rule_name457); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_42.add(string_literal35);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 115:17: -> ^( UAC_RULES )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:115:20: ^( UAC_RULES )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(UAC_RULES, "UAC_RULES"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:116:4: 'QUERIES:'
                    {
                    string_literal36=(Token)match(input,43,FOLLOW_43_in_rule_name468); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_43.add(string_literal36);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 116:15: -> ^( QUERIES )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:116:18: ^( QUERIES )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(QUERIES, "QUERIES"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rule_name"

    public static class rule_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rule"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:118:1: rule : ( relation_predicate ( '<-' | '::' ) rule_body -> ^( RULE ^( ANTECEDENT rule_body ) ^( CONSEQUENT relation_predicate ) ) | relation_predicate ':.' -> ^( RULE ^( CONSEQUENT relation_predicate ) ) );
    public final DomainModelParser.rule_return rule() throws RecognitionException {
        DomainModelParser.rule_return retval = new DomainModelParser.rule_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal38=null;
        Token string_literal39=null;
        Token string_literal42=null;
        DomainModelParser.relation_predicate_return relation_predicate37 = null;

        DomainModelParser.rule_body_return rule_body40 = null;

        DomainModelParser.relation_predicate_return relation_predicate41 = null;


        CommonTree string_literal38_tree=null;
        CommonTree string_literal39_tree=null;
        CommonTree string_literal42_tree=null;
        RewriteRuleTokenStream stream_45=new RewriteRuleTokenStream(adaptor,"token 45");
        RewriteRuleTokenStream stream_44=new RewriteRuleTokenStream(adaptor,"token 44");
        RewriteRuleTokenStream stream_46=new RewriteRuleTokenStream(adaptor,"token 46");
        RewriteRuleSubtreeStream stream_relation_predicate=new RewriteRuleSubtreeStream(adaptor,"rule relation_predicate");
        RewriteRuleSubtreeStream stream_rule_body=new RewriteRuleSubtreeStream(adaptor,"rule rule_body");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:2: ( relation_predicate ( '<-' | '::' ) rule_body -> ^( RULE ^( ANTECEDENT rule_body ) ^( CONSEQUENT relation_predicate ) ) | relation_predicate ':.' -> ^( RULE ^( CONSEQUENT relation_predicate ) ) )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==ID) ) {
                int LA15_1 = input.LA(2);

                if ( (synpred19_DomainModel()) ) {
                    alt15=1;
                }
                else if ( (true) ) {
                    alt15=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:4: relation_predicate ( '<-' | '::' ) rule_body
                    {
                    pushFollow(FOLLOW_relation_predicate_in_rule483);
                    relation_predicate37=relation_predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_relation_predicate.add(relation_predicate37.getTree());
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:23: ( '<-' | '::' )
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==44) ) {
                        alt14=1;
                    }
                    else if ( (LA14_0==45) ) {
                        alt14=2;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 14, 0, input);

                        throw nvae;
                    }
                    switch (alt14) {
                        case 1 :
                            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:24: '<-'
                            {
                            string_literal38=(Token)match(input,44,FOLLOW_44_in_rule486); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_44.add(string_literal38);


                            }
                            break;
                        case 2 :
                            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:31: '::'
                            {
                            string_literal39=(Token)match(input,45,FOLLOW_45_in_rule490); if (state.failed) return retval; 
                            if ( state.backtracking==0 ) stream_45.add(string_literal39);


                            }
                            break;

                    }

                    pushFollow(FOLLOW_rule_body_in_rule493);
                    rule_body40=rule_body();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_rule_body.add(rule_body40.getTree());


                    // AST REWRITE
                    // elements: relation_predicate, rule_body
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 119:47: -> ^( RULE ^( ANTECEDENT rule_body ) ^( CONSEQUENT relation_predicate ) )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:50: ^( RULE ^( ANTECEDENT rule_body ) ^( CONSEQUENT relation_predicate ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(RULE, "RULE"), root_1);

                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:57: ^( ANTECEDENT rule_body )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ANTECEDENT, "ANTECEDENT"), root_2);

                        adaptor.addChild(root_2, stream_rule_body.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:81: ^( CONSEQUENT relation_predicate )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONSEQUENT, "CONSEQUENT"), root_2);

                        adaptor.addChild(root_2, stream_relation_predicate.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:120:4: relation_predicate ':.'
                    {
                    pushFollow(FOLLOW_relation_predicate_in_rule516);
                    relation_predicate41=relation_predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_relation_predicate.add(relation_predicate41.getTree());
                    string_literal42=(Token)match(input,46,FOLLOW_46_in_rule518); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_46.add(string_literal42);



                    // AST REWRITE
                    // elements: relation_predicate
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 120:28: -> ^( RULE ^( CONSEQUENT relation_predicate ) )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:120:31: ^( RULE ^( CONSEQUENT relation_predicate ) )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(RULE, "RULE"), root_1);

                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:120:38: ^( CONSEQUENT relation_predicate )
                        {
                        CommonTree root_2 = (CommonTree)adaptor.nil();
                        root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONSEQUENT, "CONSEQUENT"), root_2);

                        adaptor.addChild(root_2, stream_relation_predicate.nextTree());

                        adaptor.addChild(root_1, root_2);
                        }

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rule"

    public static class lav_rule_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "lav_rule"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:122:1: lav_rule : relation_predicate ( '->' ) rule_body -> ^( RULE ^( ANTECEDENT relation_predicate ) ^( CONSEQUENT rule_body ) ) ;
    public final DomainModelParser.lav_rule_return lav_rule() throws RecognitionException {
        DomainModelParser.lav_rule_return retval = new DomainModelParser.lav_rule_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal44=null;
        DomainModelParser.relation_predicate_return relation_predicate43 = null;

        DomainModelParser.rule_body_return rule_body45 = null;


        CommonTree string_literal44_tree=null;
        RewriteRuleTokenStream stream_47=new RewriteRuleTokenStream(adaptor,"token 47");
        RewriteRuleSubtreeStream stream_relation_predicate=new RewriteRuleSubtreeStream(adaptor,"rule relation_predicate");
        RewriteRuleSubtreeStream stream_rule_body=new RewriteRuleSubtreeStream(adaptor,"rule rule_body");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:123:2: ( relation_predicate ( '->' ) rule_body -> ^( RULE ^( ANTECEDENT relation_predicate ) ^( CONSEQUENT rule_body ) ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:123:4: relation_predicate ( '->' ) rule_body
            {
            pushFollow(FOLLOW_relation_predicate_in_lav_rule540);
            relation_predicate43=relation_predicate();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_relation_predicate.add(relation_predicate43.getTree());
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:123:23: ( '->' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:123:24: '->'
            {
            string_literal44=(Token)match(input,47,FOLLOW_47_in_lav_rule543); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_47.add(string_literal44);


            }

            pushFollow(FOLLOW_rule_body_in_lav_rule546);
            rule_body45=rule_body();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rule_body.add(rule_body45.getTree());


            // AST REWRITE
            // elements: relation_predicate, rule_body
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 123:40: -> ^( RULE ^( ANTECEDENT relation_predicate ) ^( CONSEQUENT rule_body ) )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:123:43: ^( RULE ^( ANTECEDENT relation_predicate ) ^( CONSEQUENT rule_body ) )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(RULE, "RULE"), root_1);

                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:123:50: ^( ANTECEDENT relation_predicate )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ANTECEDENT, "ANTECEDENT"), root_2);

                adaptor.addChild(root_2, stream_relation_predicate.nextTree());

                adaptor.addChild(root_1, root_2);
                }
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:123:83: ^( CONSEQUENT rule_body )
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONSEQUENT, "CONSEQUENT"), root_2);

                adaptor.addChild(root_2, stream_rule_body.nextTree());

                adaptor.addChild(root_1, root_2);
                }

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "lav_rule"

    public static class glav_rule_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "glav_rule"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:125:1: glav_rule : v1= rule_body ( '->' ) v2= rule_body -> ^( RULE ^( ANTECEDENT $v1) ^( CONSEQUENT $v2) ) ;
    public final DomainModelParser.glav_rule_return glav_rule() throws RecognitionException {
        DomainModelParser.glav_rule_return retval = new DomainModelParser.glav_rule_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal46=null;
        DomainModelParser.rule_body_return v1 = null;

        DomainModelParser.rule_body_return v2 = null;


        CommonTree string_literal46_tree=null;
        RewriteRuleTokenStream stream_47=new RewriteRuleTokenStream(adaptor,"token 47");
        RewriteRuleSubtreeStream stream_rule_body=new RewriteRuleSubtreeStream(adaptor,"rule rule_body");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:126:2: (v1= rule_body ( '->' ) v2= rule_body -> ^( RULE ^( ANTECEDENT $v1) ^( CONSEQUENT $v2) ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:126:4: v1= rule_body ( '->' ) v2= rule_body
            {
            pushFollow(FOLLOW_rule_body_in_glav_rule576);
            v1=rule_body();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rule_body.add(v1.getTree());
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:126:17: ( '->' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:126:18: '->'
            {
            string_literal46=(Token)match(input,47,FOLLOW_47_in_glav_rule579); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_47.add(string_literal46);


            }

            pushFollow(FOLLOW_rule_body_in_glav_rule584);
            v2=rule_body();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_rule_body.add(v2.getTree());


            // AST REWRITE
            // elements: v2, v1
            // token labels: 
            // rule labels: v1, retval, v2
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_v1=new RewriteRuleSubtreeStream(adaptor,"rule v1",v1!=null?v1.tree:null);
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
            RewriteRuleSubtreeStream stream_v2=new RewriteRuleSubtreeStream(adaptor,"rule v2",v2!=null?v2.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 126:37: -> ^( RULE ^( ANTECEDENT $v1) ^( CONSEQUENT $v2) )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:126:40: ^( RULE ^( ANTECEDENT $v1) ^( CONSEQUENT $v2) )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(RULE, "RULE"), root_1);

                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:126:47: ^( ANTECEDENT $v1)
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ANTECEDENT, "ANTECEDENT"), root_2);

                adaptor.addChild(root_2, stream_v1.nextTree());

                adaptor.addChild(root_1, root_2);
                }
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:126:65: ^( CONSEQUENT $v2)
                {
                CommonTree root_2 = (CommonTree)adaptor.nil();
                root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(CONSEQUENT, "CONSEQUENT"), root_2);

                adaptor.addChild(root_2, stream_v2.nextTree());

                adaptor.addChild(root_1, root_2);
                }

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "glav_rule"

    public static class rule_body_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "rule_body"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:128:1: rule_body : predicate ( '^' predicate )* -> ( ^( predicate ) )+ ;
    public final DomainModelParser.rule_body_return rule_body() throws RecognitionException {
        DomainModelParser.rule_body_return retval = new DomainModelParser.rule_body_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal48=null;
        DomainModelParser.predicate_return predicate47 = null;

        DomainModelParser.predicate_return predicate49 = null;


        CommonTree char_literal48_tree=null;
        RewriteRuleTokenStream stream_48=new RewriteRuleTokenStream(adaptor,"token 48");
        RewriteRuleSubtreeStream stream_predicate=new RewriteRuleSubtreeStream(adaptor,"rule predicate");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:129:2: ( predicate ( '^' predicate )* -> ( ^( predicate ) )+ )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:129:4: predicate ( '^' predicate )*
            {
            pushFollow(FOLLOW_predicate_in_rule_body613);
            predicate47=predicate();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_predicate.add(predicate47.getTree());
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:129:14: ( '^' predicate )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==48) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:129:15: '^' predicate
            	    {
            	    char_literal48=(Token)match(input,48,FOLLOW_48_in_rule_body616); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_48.add(char_literal48);

            	    pushFollow(FOLLOW_predicate_in_rule_body618);
            	    predicate49=predicate();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_predicate.add(predicate49.getTree());

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);



            // AST REWRITE
            // elements: predicate
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 129:31: -> ( ^( predicate ) )+
            {
                if ( !(stream_predicate.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_predicate.hasNext() ) {
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:129:34: ^( predicate )
                    {
                    CommonTree root_1 = (CommonTree)adaptor.nil();
                    root_1 = (CommonTree)adaptor.becomeRoot(stream_predicate.nextNode(), root_1);

                    adaptor.addChild(root_0, root_1);
                    }

                }
                stream_predicate.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "rule_body"

    public static class predicate_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "predicate"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:131:1: predicate : ( relation_predicate | builtin_predicate );
    public final DomainModelParser.predicate_return predicate() throws RecognitionException {
        DomainModelParser.predicate_return retval = new DomainModelParser.predicate_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        DomainModelParser.relation_predicate_return relation_predicate50 = null;

        DomainModelParser.builtin_predicate_return builtin_predicate51 = null;



        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:132:2: ( relation_predicate | builtin_predicate )
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==ID) ) {
                alt17=1;
            }
            else if ( (LA17_0==36) ) {
                alt17=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 17, 0, input);

                throw nvae;
            }
            switch (alt17) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:132:4: relation_predicate
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_relation_predicate_in_predicate636);
                    relation_predicate50=relation_predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, relation_predicate50.getTree());

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:132:25: builtin_predicate
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_builtin_predicate_in_predicate640);
                    builtin_predicate51=builtin_predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, builtin_predicate51.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "predicate"

    public static class relation_predicate_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "relation_predicate"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:134:1: relation_predicate : table_name '(' column_value ( ',' column_value )* ')' -> ^( RELATION_PRED table_name ( column_value )+ ) ;
    public final DomainModelParser.relation_predicate_return relation_predicate() throws RecognitionException {
        DomainModelParser.relation_predicate_return retval = new DomainModelParser.relation_predicate_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal53=null;
        Token char_literal55=null;
        Token char_literal57=null;
        DomainModelParser.table_name_return table_name52 = null;

        DomainModelParser.column_value_return column_value54 = null;

        DomainModelParser.column_value_return column_value56 = null;


        CommonTree char_literal53_tree=null;
        CommonTree char_literal55_tree=null;
        CommonTree char_literal57_tree=null;
        RewriteRuleTokenStream stream_36=new RewriteRuleTokenStream(adaptor,"token 36");
        RewriteRuleTokenStream stream_37=new RewriteRuleTokenStream(adaptor,"token 37");
        RewriteRuleTokenStream stream_38=new RewriteRuleTokenStream(adaptor,"token 38");
        RewriteRuleSubtreeStream stream_table_name=new RewriteRuleSubtreeStream(adaptor,"rule table_name");
        RewriteRuleSubtreeStream stream_column_value=new RewriteRuleSubtreeStream(adaptor,"rule column_value");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:135:2: ( table_name '(' column_value ( ',' column_value )* ')' -> ^( RELATION_PRED table_name ( column_value )+ ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:135:4: table_name '(' column_value ( ',' column_value )* ')'
            {
            pushFollow(FOLLOW_table_name_in_relation_predicate650);
            table_name52=table_name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_table_name.add(table_name52.getTree());
            char_literal53=(Token)match(input,36,FOLLOW_36_in_relation_predicate652); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_36.add(char_literal53);

            pushFollow(FOLLOW_column_value_in_relation_predicate654);
            column_value54=column_value();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_column_value.add(column_value54.getTree());
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:135:32: ( ',' column_value )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);

                if ( (LA18_0==37) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:135:33: ',' column_value
            	    {
            	    char_literal55=(Token)match(input,37,FOLLOW_37_in_relation_predicate657); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_37.add(char_literal55);

            	    pushFollow(FOLLOW_column_value_in_relation_predicate659);
            	    column_value56=column_value();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_column_value.add(column_value56.getTree());

            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);

            char_literal57=(Token)match(input,38,FOLLOW_38_in_relation_predicate663); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_38.add(char_literal57);



            // AST REWRITE
            // elements: table_name, column_value
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 135:56: -> ^( RELATION_PRED table_name ( column_value )+ )
            {
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:135:59: ^( RELATION_PRED table_name ( column_value )+ )
                {
                CommonTree root_1 = (CommonTree)adaptor.nil();
                root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(RELATION_PRED, "RELATION_PRED"), root_1);

                adaptor.addChild(root_1, stream_table_name.nextTree());
                if ( !(stream_column_value.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_column_value.hasNext() ) {
                    adaptor.addChild(root_1, stream_column_value.nextTree());

                }
                stream_column_value.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "relation_predicate"

    public static class function_predicate_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_predicate"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:137:1: function_predicate : ( function_name '(' column_value ( ',' column_value )* ')' -> ^( FUNCTION_PRED function_name ( column_value )+ ) | function_name '(' ')' -> ^( FUNCTION_PRED function_name ) );
    public final DomainModelParser.function_predicate_return function_predicate() throws RecognitionException {
        DomainModelParser.function_predicate_return retval = new DomainModelParser.function_predicate_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal59=null;
        Token char_literal61=null;
        Token char_literal63=null;
        Token char_literal65=null;
        Token char_literal66=null;
        DomainModelParser.function_name_return function_name58 = null;

        DomainModelParser.column_value_return column_value60 = null;

        DomainModelParser.column_value_return column_value62 = null;

        DomainModelParser.function_name_return function_name64 = null;


        CommonTree char_literal59_tree=null;
        CommonTree char_literal61_tree=null;
        CommonTree char_literal63_tree=null;
        CommonTree char_literal65_tree=null;
        CommonTree char_literal66_tree=null;
        RewriteRuleTokenStream stream_36=new RewriteRuleTokenStream(adaptor,"token 36");
        RewriteRuleTokenStream stream_37=new RewriteRuleTokenStream(adaptor,"token 37");
        RewriteRuleTokenStream stream_38=new RewriteRuleTokenStream(adaptor,"token 38");
        RewriteRuleSubtreeStream stream_function_name=new RewriteRuleSubtreeStream(adaptor,"rule function_name");
        RewriteRuleSubtreeStream stream_column_value=new RewriteRuleSubtreeStream(adaptor,"rule column_value");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:138:2: ( function_name '(' column_value ( ',' column_value )* ')' -> ^( FUNCTION_PRED function_name ( column_value )+ ) | function_name '(' ')' -> ^( FUNCTION_PRED function_name ) )
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==ID) ) {
                int LA20_1 = input.LA(2);

                if ( (LA20_1==36) ) {
                    int LA20_2 = input.LA(3);

                    if ( (LA20_2==38) ) {
                        alt20=2;
                    }
                    else if ( ((LA20_2>=STRING && LA20_2<=ID)||LA20_2==49) ) {
                        alt20=1;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 20, 2, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 20, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 20, 0, input);

                throw nvae;
            }
            switch (alt20) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:138:4: function_name '(' column_value ( ',' column_value )* ')'
                    {
                    pushFollow(FOLLOW_function_name_in_function_predicate683);
                    function_name58=function_name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_function_name.add(function_name58.getTree());
                    char_literal59=(Token)match(input,36,FOLLOW_36_in_function_predicate685); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_36.add(char_literal59);

                    pushFollow(FOLLOW_column_value_in_function_predicate687);
                    column_value60=column_value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_column_value.add(column_value60.getTree());
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:138:35: ( ',' column_value )*
                    loop19:
                    do {
                        int alt19=2;
                        int LA19_0 = input.LA(1);

                        if ( (LA19_0==37) ) {
                            alt19=1;
                        }


                        switch (alt19) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:138:36: ',' column_value
                    	    {
                    	    char_literal61=(Token)match(input,37,FOLLOW_37_in_function_predicate690); if (state.failed) return retval; 
                    	    if ( state.backtracking==0 ) stream_37.add(char_literal61);

                    	    pushFollow(FOLLOW_column_value_in_function_predicate692);
                    	    column_value62=column_value();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) stream_column_value.add(column_value62.getTree());

                    	    }
                    	    break;

                    	default :
                    	    break loop19;
                        }
                    } while (true);

                    char_literal63=(Token)match(input,38,FOLLOW_38_in_function_predicate696); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_38.add(char_literal63);



                    // AST REWRITE
                    // elements: function_name, column_value
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 138:59: -> ^( FUNCTION_PRED function_name ( column_value )+ )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:138:62: ^( FUNCTION_PRED function_name ( column_value )+ )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FUNCTION_PRED, "FUNCTION_PRED"), root_1);

                        adaptor.addChild(root_1, stream_function_name.nextTree());
                        if ( !(stream_column_value.hasNext()) ) {
                            throw new RewriteEarlyExitException();
                        }
                        while ( stream_column_value.hasNext() ) {
                            adaptor.addChild(root_1, stream_column_value.nextTree());

                        }
                        stream_column_value.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:139:4: function_name '(' ')'
                    {
                    pushFollow(FOLLOW_function_name_in_function_predicate712);
                    function_name64=function_name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_function_name.add(function_name64.getTree());
                    char_literal65=(Token)match(input,36,FOLLOW_36_in_function_predicate714); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_36.add(char_literal65);

                    char_literal66=(Token)match(input,38,FOLLOW_38_in_function_predicate715); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_38.add(char_literal66);



                    // AST REWRITE
                    // elements: function_name
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 139:25: -> ^( FUNCTION_PRED function_name )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:139:28: ^( FUNCTION_PRED function_name )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(FUNCTION_PRED, "FUNCTION_PRED"), root_1);

                        adaptor.addChild(root_1, stream_function_name.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_predicate"

    public static class builtin_predicate_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "builtin_predicate"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:141:1: builtin_predicate : ( '(' v1= column_value comparison (v2= column_value )? ')' -> ^( BUILTIN_PRED comparison $v1 ( $v2)? ) | '(' v1= column_value in_comparison v3= value_list ')' -> ^( BUILTIN_PRED in_comparison $v1 $v3) );
    public final DomainModelParser.builtin_predicate_return builtin_predicate() throws RecognitionException {
        DomainModelParser.builtin_predicate_return retval = new DomainModelParser.builtin_predicate_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal67=null;
        Token char_literal69=null;
        Token char_literal70=null;
        Token char_literal72=null;
        DomainModelParser.column_value_return v1 = null;

        DomainModelParser.column_value_return v2 = null;

        DomainModelParser.value_list_return v3 = null;

        DomainModelParser.comparison_return comparison68 = null;

        DomainModelParser.in_comparison_return in_comparison71 = null;


        CommonTree char_literal67_tree=null;
        CommonTree char_literal69_tree=null;
        CommonTree char_literal70_tree=null;
        CommonTree char_literal72_tree=null;
        RewriteRuleTokenStream stream_36=new RewriteRuleTokenStream(adaptor,"token 36");
        RewriteRuleTokenStream stream_38=new RewriteRuleTokenStream(adaptor,"token 38");
        RewriteRuleSubtreeStream stream_column_value=new RewriteRuleSubtreeStream(adaptor,"rule column_value");
        RewriteRuleSubtreeStream stream_in_comparison=new RewriteRuleSubtreeStream(adaptor,"rule in_comparison");
        RewriteRuleSubtreeStream stream_comparison=new RewriteRuleSubtreeStream(adaptor,"rule comparison");
        RewriteRuleSubtreeStream stream_value_list=new RewriteRuleSubtreeStream(adaptor,"rule value_list");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:142:2: ( '(' v1= column_value comparison (v2= column_value )? ')' -> ^( BUILTIN_PRED comparison $v1 ( $v2)? ) | '(' v1= column_value in_comparison v3= value_list ')' -> ^( BUILTIN_PRED in_comparison $v1 $v3) )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==36) ) {
                int LA22_1 = input.LA(2);

                if ( (synpred26_DomainModel()) ) {
                    alt22=1;
                }
                else if ( (true) ) {
                    alt22=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 22, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:142:4: '(' v1= column_value comparison (v2= column_value )? ')'
                    {
                    char_literal67=(Token)match(input,36,FOLLOW_36_in_builtin_predicate732); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_36.add(char_literal67);

                    pushFollow(FOLLOW_column_value_in_builtin_predicate736);
                    v1=column_value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_column_value.add(v1.getTree());
                    pushFollow(FOLLOW_comparison_in_builtin_predicate738);
                    comparison68=comparison();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_comparison.add(comparison68.getTree());
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:142:37: (v2= column_value )?
                    int alt21=2;
                    int LA21_0 = input.LA(1);

                    if ( ((LA21_0>=STRING && LA21_0<=ID)||LA21_0==49) ) {
                        alt21=1;
                    }
                    switch (alt21) {
                        case 1 :
                            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:0:0: v2= column_value
                            {
                            pushFollow(FOLLOW_column_value_in_builtin_predicate742);
                            v2=column_value();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) stream_column_value.add(v2.getTree());

                            }
                            break;

                    }

                    char_literal69=(Token)match(input,38,FOLLOW_38_in_builtin_predicate745); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_38.add(char_literal69);



                    // AST REWRITE
                    // elements: v1, comparison, v2
                    // token labels: 
                    // rule labels: v1, retval, v2
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_v1=new RewriteRuleSubtreeStream(adaptor,"rule v1",v1!=null?v1.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_v2=new RewriteRuleSubtreeStream(adaptor,"rule v2",v2!=null?v2.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 142:56: -> ^( BUILTIN_PRED comparison $v1 ( $v2)? )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:142:59: ^( BUILTIN_PRED comparison $v1 ( $v2)? )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BUILTIN_PRED, "BUILTIN_PRED"), root_1);

                        adaptor.addChild(root_1, stream_comparison.nextTree());
                        adaptor.addChild(root_1, stream_v1.nextTree());
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:142:89: ( $v2)?
                        if ( stream_v2.hasNext() ) {
                            adaptor.addChild(root_1, stream_v2.nextTree());

                        }
                        stream_v2.reset();

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:143:4: '(' v1= column_value in_comparison v3= value_list ')'
                    {
                    char_literal70=(Token)match(input,36,FOLLOW_36_in_builtin_predicate765); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_36.add(char_literal70);

                    pushFollow(FOLLOW_column_value_in_builtin_predicate769);
                    v1=column_value();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_column_value.add(v1.getTree());
                    pushFollow(FOLLOW_in_comparison_in_builtin_predicate771);
                    in_comparison71=in_comparison();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_in_comparison.add(in_comparison71.getTree());
                    pushFollow(FOLLOW_value_list_in_builtin_predicate775);
                    v3=value_list();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) stream_value_list.add(v3.getTree());
                    char_literal72=(Token)match(input,38,FOLLOW_38_in_builtin_predicate777); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_38.add(char_literal72);



                    // AST REWRITE
                    // elements: v3, in_comparison, v1
                    // token labels: 
                    // rule labels: v1, retval, v3
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_v1=new RewriteRuleSubtreeStream(adaptor,"rule v1",v1!=null?v1.tree:null);
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);
                    RewriteRuleSubtreeStream stream_v3=new RewriteRuleSubtreeStream(adaptor,"rule v3",v3!=null?v3.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 143:56: -> ^( BUILTIN_PRED in_comparison $v1 $v3)
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:143:59: ^( BUILTIN_PRED in_comparison $v1 $v3)
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(BUILTIN_PRED, "BUILTIN_PRED"), root_1);

                        adaptor.addChild(root_1, stream_in_comparison.nextTree());
                        adaptor.addChild(root_1, stream_v1.nextTree());
                        adaptor.addChild(root_1, stream_v3.nextTree());

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "builtin_predicate"

    public static class column_value_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "column_value"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:145:1: column_value : ( column_name | constant | function_predicate );
    public final DomainModelParser.column_value_return column_value() throws RecognitionException {
        DomainModelParser.column_value_return retval = new DomainModelParser.column_value_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        DomainModelParser.column_name_return column_name73 = null;

        DomainModelParser.constant_return constant74 = null;

        DomainModelParser.function_predicate_return function_predicate75 = null;



        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:146:2: ( column_name | constant | function_predicate )
            int alt23=3;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==ID) ) {
                int LA23_1 = input.LA(2);

                if ( (LA23_1==36) ) {
                    alt23=3;
                }
                else if ( (LA23_1==EOF||(LA23_1>=37 && LA23_1<=38)||(LA23_1>=52 && LA23_1<=62)) ) {
                    alt23=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 23, 1, input);

                    throw nvae;
                }
            }
            else if ( ((LA23_0>=STRING && LA23_0<=FLOAT)||LA23_0==49) ) {
                alt23=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 23, 0, input);

                throw nvae;
            }
            switch (alt23) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:146:4: column_name
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_column_name_in_column_value801);
                    column_name73=column_name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, column_name73.getTree());

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:146:18: constant
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_constant_in_column_value805);
                    constant74=constant();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, constant74.getTree());

                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:146:29: function_predicate
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_function_predicate_in_column_value809);
                    function_predicate75=function_predicate();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, function_predicate75.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "column_value"

    public static class constant_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "constant"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:148:1: constant : ( STRING | INT | FLOAT | 'NULL' );
    public final DomainModelParser.constant_return constant() throws RecognitionException {
        DomainModelParser.constant_return retval = new DomainModelParser.constant_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token set76=null;

        CommonTree set76_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:149:2: ( STRING | INT | FLOAT | 'NULL' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:
            {
            root_0 = (CommonTree)adaptor.nil();

            set76=(Token)input.LT(1);
            if ( (input.LA(1)>=STRING && input.LA(1)<=FLOAT)||input.LA(1)==49 ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, (CommonTree)adaptor.create(set76));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "constant"

    public static class value_list_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "value_list"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:151:1: value_list : '[' constant ( ',' constant )* ']' -> ( constant )+ ;
    public final DomainModelParser.value_list_return value_list() throws RecognitionException {
        DomainModelParser.value_list_return retval = new DomainModelParser.value_list_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal77=null;
        Token char_literal79=null;
        Token char_literal81=null;
        DomainModelParser.constant_return constant78 = null;

        DomainModelParser.constant_return constant80 = null;


        CommonTree char_literal77_tree=null;
        CommonTree char_literal79_tree=null;
        CommonTree char_literal81_tree=null;
        RewriteRuleTokenStream stream_51=new RewriteRuleTokenStream(adaptor,"token 51");
        RewriteRuleTokenStream stream_37=new RewriteRuleTokenStream(adaptor,"token 37");
        RewriteRuleTokenStream stream_50=new RewriteRuleTokenStream(adaptor,"token 50");
        RewriteRuleSubtreeStream stream_constant=new RewriteRuleSubtreeStream(adaptor,"rule constant");
        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:152:2: ( '[' constant ( ',' constant )* ']' -> ( constant )+ )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:152:4: '[' constant ( ',' constant )* ']'
            {
            char_literal77=(Token)match(input,50,FOLLOW_50_in_value_list841); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_50.add(char_literal77);

            pushFollow(FOLLOW_constant_in_value_list843);
            constant78=constant();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) stream_constant.add(constant78.getTree());
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:152:17: ( ',' constant )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==37) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:152:18: ',' constant
            	    {
            	    char_literal79=(Token)match(input,37,FOLLOW_37_in_value_list846); if (state.failed) return retval; 
            	    if ( state.backtracking==0 ) stream_37.add(char_literal79);

            	    pushFollow(FOLLOW_constant_in_value_list848);
            	    constant80=constant();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) stream_constant.add(constant80.getTree());

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);

            char_literal81=(Token)match(input,51,FOLLOW_51_in_value_list852); if (state.failed) return retval; 
            if ( state.backtracking==0 ) stream_51.add(char_literal81);



            // AST REWRITE
            // elements: constant
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            // wildcard labels: 
            if ( state.backtracking==0 ) {
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

            root_0 = (CommonTree)adaptor.nil();
            // 152:37: -> ( constant )+
            {
                if ( !(stream_constant.hasNext()) ) {
                    throw new RewriteEarlyExitException();
                }
                while ( stream_constant.hasNext() ) {
                    adaptor.addChild(root_0, stream_constant.nextTree());

                }
                stream_constant.reset();

            }

            retval.tree = root_0;}
            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "value_list"

    public static class comparison_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "comparison"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:154:1: comparison : ( '=' | '!=' | '<' | '>' | '>=' | '<=' | '<>' | 'LIKE' | null_comparison );
    public final DomainModelParser.comparison_return comparison() throws RecognitionException {
        DomainModelParser.comparison_return retval = new DomainModelParser.comparison_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token char_literal82=null;
        Token string_literal83=null;
        Token char_literal84=null;
        Token char_literal85=null;
        Token string_literal86=null;
        Token string_literal87=null;
        Token string_literal88=null;
        Token string_literal89=null;
        DomainModelParser.null_comparison_return null_comparison90 = null;


        CommonTree char_literal82_tree=null;
        CommonTree string_literal83_tree=null;
        CommonTree char_literal84_tree=null;
        CommonTree char_literal85_tree=null;
        CommonTree string_literal86_tree=null;
        CommonTree string_literal87_tree=null;
        CommonTree string_literal88_tree=null;
        CommonTree string_literal89_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:2: ( '=' | '!=' | '<' | '>' | '>=' | '<=' | '<>' | 'LIKE' | null_comparison )
            int alt25=9;
            switch ( input.LA(1) ) {
            case 52:
                {
                alt25=1;
                }
                break;
            case 53:
                {
                alt25=2;
                }
                break;
            case 54:
                {
                alt25=3;
                }
                break;
            case 55:
                {
                alt25=4;
                }
                break;
            case 56:
                {
                alt25=5;
                }
                break;
            case 57:
                {
                alt25=6;
                }
                break;
            case 58:
                {
                alt25=7;
                }
                break;
            case 59:
                {
                alt25=8;
                }
                break;
            case 60:
                {
                alt25=9;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }

            switch (alt25) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:4: '='
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal82=(Token)match(input,52,FOLLOW_52_in_comparison867); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal82_tree = (CommonTree)adaptor.create(char_literal82);
                    adaptor.addChild(root_0, char_literal82_tree);
                    }

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:10: '!='
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal83=(Token)match(input,53,FOLLOW_53_in_comparison871); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal83_tree = (CommonTree)adaptor.create(string_literal83);
                    adaptor.addChild(root_0, string_literal83_tree);
                    }

                    }
                    break;
                case 3 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:17: '<'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal84=(Token)match(input,54,FOLLOW_54_in_comparison875); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal84_tree = (CommonTree)adaptor.create(char_literal84);
                    adaptor.addChild(root_0, char_literal84_tree);
                    }

                    }
                    break;
                case 4 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:23: '>'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    char_literal85=(Token)match(input,55,FOLLOW_55_in_comparison879); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    char_literal85_tree = (CommonTree)adaptor.create(char_literal85);
                    adaptor.addChild(root_0, char_literal85_tree);
                    }

                    }
                    break;
                case 5 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:29: '>='
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal86=(Token)match(input,56,FOLLOW_56_in_comparison883); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal86_tree = (CommonTree)adaptor.create(string_literal86);
                    adaptor.addChild(root_0, string_literal86_tree);
                    }

                    }
                    break;
                case 6 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:36: '<='
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal87=(Token)match(input,57,FOLLOW_57_in_comparison887); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal87_tree = (CommonTree)adaptor.create(string_literal87);
                    adaptor.addChild(root_0, string_literal87_tree);
                    }

                    }
                    break;
                case 7 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:43: '<>'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal88=(Token)match(input,58,FOLLOW_58_in_comparison891); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal88_tree = (CommonTree)adaptor.create(string_literal88);
                    adaptor.addChild(root_0, string_literal88_tree);
                    }

                    }
                    break;
                case 8 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:50: 'LIKE'
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    string_literal89=(Token)match(input,59,FOLLOW_59_in_comparison895); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    string_literal89_tree = (CommonTree)adaptor.create(string_literal89);
                    adaptor.addChild(root_0, string_literal89_tree);
                    }

                    }
                    break;
                case 9 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:155:59: null_comparison
                    {
                    root_0 = (CommonTree)adaptor.nil();

                    pushFollow(FOLLOW_null_comparison_in_comparison899);
                    null_comparison90=null_comparison();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, null_comparison90.getTree());

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "comparison"

    public static class null_comparison_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "null_comparison"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:157:1: null_comparison : ( 'IS' 'NULL' -> ^( IS_NULL ) | 'IS' 'NOT' 'NULL' -> ^( ISNOT_NULL ) );
    public final DomainModelParser.null_comparison_return null_comparison() throws RecognitionException {
        DomainModelParser.null_comparison_return retval = new DomainModelParser.null_comparison_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal91=null;
        Token string_literal92=null;
        Token string_literal93=null;
        Token string_literal94=null;
        Token string_literal95=null;

        CommonTree string_literal91_tree=null;
        CommonTree string_literal92_tree=null;
        CommonTree string_literal93_tree=null;
        CommonTree string_literal94_tree=null;
        CommonTree string_literal95_tree=null;
        RewriteRuleTokenStream stream_49=new RewriteRuleTokenStream(adaptor,"token 49");
        RewriteRuleTokenStream stream_60=new RewriteRuleTokenStream(adaptor,"token 60");
        RewriteRuleTokenStream stream_61=new RewriteRuleTokenStream(adaptor,"token 61");

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:158:2: ( 'IS' 'NULL' -> ^( IS_NULL ) | 'IS' 'NOT' 'NULL' -> ^( ISNOT_NULL ) )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==60) ) {
                int LA26_1 = input.LA(2);

                if ( (LA26_1==49) ) {
                    alt26=1;
                }
                else if ( (LA26_1==61) ) {
                    alt26=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 26, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:158:4: 'IS' 'NULL'
                    {
                    string_literal91=(Token)match(input,60,FOLLOW_60_in_null_comparison909); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_60.add(string_literal91);

                    string_literal92=(Token)match(input,49,FOLLOW_49_in_null_comparison911); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_49.add(string_literal92);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 158:16: -> ^( IS_NULL )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:158:19: ^( IS_NULL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IS_NULL, "IS_NULL"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:159:5: 'IS' 'NOT' 'NULL'
                    {
                    string_literal93=(Token)match(input,60,FOLLOW_60_in_null_comparison924); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_60.add(string_literal93);

                    string_literal94=(Token)match(input,61,FOLLOW_61_in_null_comparison926); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_61.add(string_literal94);

                    string_literal95=(Token)match(input,49,FOLLOW_49_in_null_comparison928); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_49.add(string_literal95);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 159:23: -> ^( ISNOT_NULL )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:159:26: ^( ISNOT_NULL )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(ISNOT_NULL, "ISNOT_NULL"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "null_comparison"

    public static class in_comparison_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "in_comparison"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:161:1: in_comparison : ( 'IN' -> ^( IN ) | 'NOT' 'IN' -> ^( NOT_IN ) );
    public final DomainModelParser.in_comparison_return in_comparison() throws RecognitionException {
        DomainModelParser.in_comparison_return retval = new DomainModelParser.in_comparison_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token string_literal96=null;
        Token string_literal97=null;
        Token string_literal98=null;

        CommonTree string_literal96_tree=null;
        CommonTree string_literal97_tree=null;
        CommonTree string_literal98_tree=null;
        RewriteRuleTokenStream stream_62=new RewriteRuleTokenStream(adaptor,"token 62");
        RewriteRuleTokenStream stream_61=new RewriteRuleTokenStream(adaptor,"token 61");

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:162:2: ( 'IN' -> ^( IN ) | 'NOT' 'IN' -> ^( NOT_IN ) )
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==62) ) {
                alt27=1;
            }
            else if ( (LA27_0==61) ) {
                alt27=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:162:4: 'IN'
                    {
                    string_literal96=(Token)match(input,62,FOLLOW_62_in_in_comparison944); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_62.add(string_literal96);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 162:9: -> ^( IN )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:162:12: ^( IN )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(IN, "IN"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:163:5: 'NOT' 'IN'
                    {
                    string_literal97=(Token)match(input,61,FOLLOW_61_in_in_comparison957); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_61.add(string_literal97);

                    string_literal98=(Token)match(input,62,FOLLOW_62_in_in_comparison959); if (state.failed) return retval; 
                    if ( state.backtracking==0 ) stream_62.add(string_literal98);



                    // AST REWRITE
                    // elements: 
                    // token labels: 
                    // rule labels: retval
                    // token list labels: 
                    // rule list labels: 
                    // wildcard labels: 
                    if ( state.backtracking==0 ) {
                    retval.tree = root_0;
                    RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.tree:null);

                    root_0 = (CommonTree)adaptor.nil();
                    // 163:16: -> ^( NOT_IN )
                    {
                        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:163:19: ^( NOT_IN )
                        {
                        CommonTree root_1 = (CommonTree)adaptor.nil();
                        root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(NOT_IN, "NOT_IN"), root_1);

                        adaptor.addChild(root_0, root_1);
                        }

                    }

                    retval.tree = root_0;}
                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "in_comparison"

    public static class column_type_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "column_type"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:166:1: column_type : ID ;
    public final DomainModelParser.column_type_return column_type() throws RecognitionException {
        DomainModelParser.column_type_return retval = new DomainModelParser.column_type_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID99=null;

        CommonTree ID99_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:167:2: ( ID )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:167:4: ID
            {
            root_0 = (CommonTree)adaptor.nil();

            ID99=(Token)match(input,ID,FOLLOW_ID_in_column_type975); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID99_tree = (CommonTree)adaptor.create(ID99);
            adaptor.addChild(root_0, ID99_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "column_type"

    public static class column_binding_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "column_binding"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:170:1: column_binding : ID ;
    public final DomainModelParser.column_binding_return column_binding() throws RecognitionException {
        DomainModelParser.column_binding_return retval = new DomainModelParser.column_binding_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID100=null;

        CommonTree ID100_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:171:2: ( ID )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:171:4: ID
            {
            root_0 = (CommonTree)adaptor.nil();

            ID100=(Token)match(input,ID,FOLLOW_ID_in_column_binding987); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID100_tree = (CommonTree)adaptor.create(ID100);
            adaptor.addChild(root_0, ID100_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "column_binding"

    public static class table_name_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "table_name"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:173:1: table_name : ID ;
    public final DomainModelParser.table_name_return table_name() throws RecognitionException {
        DomainModelParser.table_name_return retval = new DomainModelParser.table_name_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID101=null;

        CommonTree ID101_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:174:2: ( ID )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:174:4: ID
            {
            root_0 = (CommonTree)adaptor.nil();

            ID101=(Token)match(input,ID,FOLLOW_ID_in_table_name999); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID101_tree = (CommonTree)adaptor.create(ID101);
            adaptor.addChild(root_0, ID101_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "table_name"

    public static class column_name_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "column_name"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:176:1: column_name : ID ;
    public final DomainModelParser.column_name_return column_name() throws RecognitionException {
        DomainModelParser.column_name_return retval = new DomainModelParser.column_name_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID102=null;

        CommonTree ID102_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:177:2: ( ID )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:177:4: ID
            {
            root_0 = (CommonTree)adaptor.nil();

            ID102=(Token)match(input,ID,FOLLOW_ID_in_column_name1009); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID102_tree = (CommonTree)adaptor.create(ID102);
            adaptor.addChild(root_0, ID102_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "column_name"

    public static class function_name_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "function_name"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:179:1: function_name : ID ;
    public final DomainModelParser.function_name_return function_name() throws RecognitionException {
        DomainModelParser.function_name_return retval = new DomainModelParser.function_name_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID103=null;

        CommonTree ID103_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:180:2: ( ID )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:180:4: ID
            {
            root_0 = (CommonTree)adaptor.nil();

            ID103=(Token)match(input,ID,FOLLOW_ID_in_function_name1019); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID103_tree = (CommonTree)adaptor.create(ID103);
            adaptor.addChild(root_0, ID103_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "function_name"

    public static class namespace_prefix_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "namespace_prefix"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:182:1: namespace_prefix : ID ;
    public final DomainModelParser.namespace_prefix_return namespace_prefix() throws RecognitionException {
        DomainModelParser.namespace_prefix_return retval = new DomainModelParser.namespace_prefix_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token ID104=null;

        CommonTree ID104_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:183:2: ( ID )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:183:4: ID
            {
            root_0 = (CommonTree)adaptor.nil();

            ID104=(Token)match(input,ID,FOLLOW_ID_in_namespace_prefix1028); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID104_tree = (CommonTree)adaptor.create(ID104);
            adaptor.addChild(root_0, ID104_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "namespace_prefix"

    public static class namespace_uri_return extends ParserRuleReturnScope {
        CommonTree tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "namespace_uri"
    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:185:1: namespace_uri : STRING ;
    public final DomainModelParser.namespace_uri_return namespace_uri() throws RecognitionException {
        DomainModelParser.namespace_uri_return retval = new DomainModelParser.namespace_uri_return();
        retval.start = input.LT(1);

        CommonTree root_0 = null;

        Token STRING105=null;

        CommonTree STRING105_tree=null;

        try {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:186:2: ( STRING )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:186:4: STRING
            {
            root_0 = (CommonTree)adaptor.nil();

            STRING105=(Token)match(input,STRING,FOLLOW_STRING_in_namespace_uri1037); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            STRING105_tree = (CommonTree)adaptor.create(STRING105);
            adaptor.addChild(root_0, STRING105_tree);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }

        catch (RecognitionException re)
        {
            reportError(re);
            throw re;
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "namespace_uri"

    // $ANTLR start synpred19_DomainModel
    public final void synpred19_DomainModel_fragment() throws RecognitionException {   
        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:4: ( relation_predicate ( '<-' | '::' ) rule_body )
        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:119:4: relation_predicate ( '<-' | '::' ) rule_body
        {
        pushFollow(FOLLOW_relation_predicate_in_synpred19_DomainModel483);
        relation_predicate();

        state._fsp--;
        if (state.failed) return ;
        if ( (input.LA(1)>=44 && input.LA(1)<=45) ) {
            input.consume();
            state.errorRecovery=false;state.failed=false;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            MismatchedSetException mse = new MismatchedSetException(null,input);
            throw mse;
        }

        pushFollow(FOLLOW_rule_body_in_synpred19_DomainModel493);
        rule_body();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred19_DomainModel

    // $ANTLR start synpred26_DomainModel
    public final void synpred26_DomainModel_fragment() throws RecognitionException {   
        DomainModelParser.column_value_return v1 = null;

        DomainModelParser.column_value_return v2 = null;


        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:142:4: ( '(' v1= column_value comparison (v2= column_value )? ')' )
        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:142:4: '(' v1= column_value comparison (v2= column_value )? ')'
        {
        match(input,36,FOLLOW_36_in_synpred26_DomainModel732); if (state.failed) return ;
        pushFollow(FOLLOW_column_value_in_synpred26_DomainModel736);
        v1=column_value();

        state._fsp--;
        if (state.failed) return ;
        pushFollow(FOLLOW_comparison_in_synpred26_DomainModel738);
        comparison();

        state._fsp--;
        if (state.failed) return ;
        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:142:37: (v2= column_value )?
        int alt31=2;
        int LA31_0 = input.LA(1);

        if ( ((LA31_0>=STRING && LA31_0<=ID)||LA31_0==49) ) {
            alt31=1;
        }
        switch (alt31) {
            case 1 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:0:0: v2= column_value
                {
                pushFollow(FOLLOW_column_value_in_synpred26_DomainModel742);
                v2=column_value();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }

        match(input,38,FOLLOW_38_in_synpred26_DomainModel745); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred26_DomainModel

    // Delegated rules

    public final boolean synpred26_DomainModel() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred26_DomainModel_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred19_DomainModel() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred19_DomainModel_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


 

    public static final BitSet FOLLOW_section_in_domain_model157 = new BitSet(new long[]{0x00000F8780000002L});
    public static final BitSet FOLLOW_schema_in_section177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functions_in_section181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rules_in_section185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_namespaces_in_section189 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_name_in_schema199 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_schema_exp_in_schema202 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_31_in_schema_name222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_schema_name233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_functions249 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_function_name_in_functions252 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_34_in_namespaces273 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_namespace_in_namespaces276 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_namespace_prefix_in_namespace297 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_35_in_namespace299 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_namespace_uri_in_namespace301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_schema_exp318 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_schema_exp320 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_column_identifier_in_schema_exp322 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_37_in_schema_exp325 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_column_identifier_in_schema_exp327 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_38_in_schema_exp331 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_name_in_column_identifier350 = new BitSet(new long[]{0x0000000800000000L});
    public static final BitSet FOLLOW_35_in_column_identifier352 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_column_type_in_column_identifier354 = new BitSet(new long[]{0x0000000800000002L});
    public static final BitSet FOLLOW_35_in_column_identifier357 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_column_binding_in_column_identifier359 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_name_in_rules384 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_rule_in_rules387 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_39_in_rules403 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_lav_rule_in_rules406 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_40_in_rules422 = new BitSet(new long[]{0x0000001008000002L});
    public static final BitSet FOLLOW_glav_rule_in_rules425 = new BitSet(new long[]{0x0000001008000002L});
    public static final BitSet FOLLOW_41_in_rule_name446 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_42_in_rule_name457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_43_in_rule_name468 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_predicate_in_rule483 = new BitSet(new long[]{0x0000300000000000L});
    public static final BitSet FOLLOW_44_in_rule486 = new BitSet(new long[]{0x0000001008000000L});
    public static final BitSet FOLLOW_45_in_rule490 = new BitSet(new long[]{0x0000001008000000L});
    public static final BitSet FOLLOW_rule_body_in_rule493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_predicate_in_rule516 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_46_in_rule518 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_predicate_in_lav_rule540 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_47_in_lav_rule543 = new BitSet(new long[]{0x0000001008000000L});
    public static final BitSet FOLLOW_rule_body_in_lav_rule546 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rule_body_in_glav_rule576 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_47_in_glav_rule579 = new BitSet(new long[]{0x0000001008000000L});
    public static final BitSet FOLLOW_rule_body_in_glav_rule584 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_predicate_in_rule_body613 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_48_in_rule_body616 = new BitSet(new long[]{0x0000001008000000L});
    public static final BitSet FOLLOW_predicate_in_rule_body618 = new BitSet(new long[]{0x0001000000000002L});
    public static final BitSet FOLLOW_relation_predicate_in_predicate636 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_builtin_predicate_in_predicate640 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_table_name_in_relation_predicate650 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_relation_predicate652 = new BitSet(new long[]{0x000200000F000000L});
    public static final BitSet FOLLOW_column_value_in_relation_predicate654 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_37_in_relation_predicate657 = new BitSet(new long[]{0x000200000F000000L});
    public static final BitSet FOLLOW_column_value_in_relation_predicate659 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_38_in_relation_predicate663 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_name_in_function_predicate683 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_function_predicate685 = new BitSet(new long[]{0x000200000F000000L});
    public static final BitSet FOLLOW_column_value_in_function_predicate687 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_37_in_function_predicate690 = new BitSet(new long[]{0x000200000F000000L});
    public static final BitSet FOLLOW_column_value_in_function_predicate692 = new BitSet(new long[]{0x0000006000000000L});
    public static final BitSet FOLLOW_38_in_function_predicate696 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_name_in_function_predicate712 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_36_in_function_predicate714 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_38_in_function_predicate715 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_builtin_predicate732 = new BitSet(new long[]{0x000200000F000000L});
    public static final BitSet FOLLOW_column_value_in_builtin_predicate736 = new BitSet(new long[]{0x1FF0000000000000L});
    public static final BitSet FOLLOW_comparison_in_builtin_predicate738 = new BitSet(new long[]{0x000200400F000000L});
    public static final BitSet FOLLOW_column_value_in_builtin_predicate742 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_38_in_builtin_predicate745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_builtin_predicate765 = new BitSet(new long[]{0x000200000F000000L});
    public static final BitSet FOLLOW_column_value_in_builtin_predicate769 = new BitSet(new long[]{0x6000000000000000L});
    public static final BitSet FOLLOW_in_comparison_in_builtin_predicate771 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_value_list_in_builtin_predicate775 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_38_in_builtin_predicate777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_column_name_in_column_value801 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_constant_in_column_value805 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_function_predicate_in_column_value809 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_constant0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_50_in_value_list841 = new BitSet(new long[]{0x0002000007000000L});
    public static final BitSet FOLLOW_constant_in_value_list843 = new BitSet(new long[]{0x0008002000000000L});
    public static final BitSet FOLLOW_37_in_value_list846 = new BitSet(new long[]{0x0002000007000000L});
    public static final BitSet FOLLOW_constant_in_value_list848 = new BitSet(new long[]{0x0008002000000000L});
    public static final BitSet FOLLOW_51_in_value_list852 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_52_in_comparison867 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_53_in_comparison871 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_54_in_comparison875 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_55_in_comparison879 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_56_in_comparison883 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_57_in_comparison887 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_58_in_comparison891 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_59_in_comparison895 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_null_comparison_in_comparison899 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_60_in_null_comparison909 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_null_comparison911 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_60_in_null_comparison924 = new BitSet(new long[]{0x2000000000000000L});
    public static final BitSet FOLLOW_61_in_null_comparison926 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_49_in_null_comparison928 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_62_in_in_comparison944 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_61_in_in_comparison957 = new BitSet(new long[]{0x4000000000000000L});
    public static final BitSet FOLLOW_62_in_in_comparison959 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_column_type975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_column_binding987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_table_name999 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_column_name1009 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_function_name1019 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_namespace_prefix1028 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_namespace_uri1037 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_relation_predicate_in_synpred19_DomainModel483 = new BitSet(new long[]{0x0000300000000000L});
    public static final BitSet FOLLOW_set_in_synpred19_DomainModel485 = new BitSet(new long[]{0x0000001008000000L});
    public static final BitSet FOLLOW_rule_body_in_synpred19_DomainModel493 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_synpred26_DomainModel732 = new BitSet(new long[]{0x000200000F000000L});
    public static final BitSet FOLLOW_column_value_in_synpred26_DomainModel736 = new BitSet(new long[]{0x1FF0000000000000L});
    public static final BitSet FOLLOW_comparison_in_synpred26_DomainModel738 = new BitSet(new long[]{0x000200400F000000L});
    public static final BitSet FOLLOW_column_value_in_synpred26_DomainModel742 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_38_in_synpred26_DomainModel745 = new BitSet(new long[]{0x0000000000000002L});

}
