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

// $ANTLR 3.2 Sep 23, 2009 12:02:23 C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g 2011-07-20 11:01:07

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.MismatchedTokenException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class DomainModelLexer extends Lexer {
    public static final int GLAV_RULES=9;
    public static final int T__62=62;
    public static final int FUNCTIONS=18;
    public static final int FLOAT=26;
    public static final int T__61=61;
    public static final int ID=27;
    public static final int T__60=60;
    public static final int ANTECEDENT=13;
    public static final int EOF=-1;
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
    public static final int RELATION_PRED=15;
    public static final int ISNOT_NULL=21;
    public static final int NUMERIC=28;
    public static final int SOURCE_SCHEMA=5;
    public static final int NAMESPACES=19;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__33=33;
    public static final int WS=29;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int QUERIES=11;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int NOT_IN=23;
    public static final int STRING=24;

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


    // delegates
    // delegators

    public DomainModelLexer() {;} 
    public DomainModelLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public DomainModelLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g"; }

    // $ANTLR start "T__31"
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:24:7: ( 'SOURCE_SCHEMA:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:24:9: 'SOURCE_SCHEMA:'
            {
            match("SOURCE_SCHEMA:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__31"

    // $ANTLR start "T__32"
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:25:7: ( 'DOMAIN_SCHEMA:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:25:9: 'DOMAIN_SCHEMA:'
            {
            match("DOMAIN_SCHEMA:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__32"

    // $ANTLR start "T__33"
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:26:7: ( 'FUNCTIONS:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:26:9: 'FUNCTIONS:'
            {
            match("FUNCTIONS:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__33"

    // $ANTLR start "T__34"
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:27:7: ( 'NAMESPACES:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:27:9: 'NAMESPACES:'
            {
            match("NAMESPACES:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__34"

    // $ANTLR start "T__35"
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:28:7: ( ':' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:28:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__35"

    // $ANTLR start "T__36"
    public final void mT__36() throws RecognitionException {
        try {
            int _type = T__36;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:29:7: ( '(' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:29:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__36"

    // $ANTLR start "T__37"
    public final void mT__37() throws RecognitionException {
        try {
            int _type = T__37;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:30:7: ( ',' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:30:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__37"

    // $ANTLR start "T__38"
    public final void mT__38() throws RecognitionException {
        try {
            int _type = T__38;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:31:7: ( ')' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:31:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__38"

    // $ANTLR start "T__39"
    public final void mT__39() throws RecognitionException {
        try {
            int _type = T__39;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:32:7: ( 'LAV_RULES:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:32:9: 'LAV_RULES:'
            {
            match("LAV_RULES:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__39"

    // $ANTLR start "T__40"
    public final void mT__40() throws RecognitionException {
        try {
            int _type = T__40;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:33:7: ( 'GLAV_RULES:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:33:9: 'GLAV_RULES:'
            {
            match("GLAV_RULES:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__40"

    // $ANTLR start "T__41"
    public final void mT__41() throws RecognitionException {
        try {
            int _type = T__41;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:34:7: ( 'GAV_RULES:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:34:9: 'GAV_RULES:'
            {
            match("GAV_RULES:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__41"

    // $ANTLR start "T__42"
    public final void mT__42() throws RecognitionException {
        try {
            int _type = T__42;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:35:7: ( 'UAC_RULES:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:35:9: 'UAC_RULES:'
            {
            match("UAC_RULES:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__42"

    // $ANTLR start "T__43"
    public final void mT__43() throws RecognitionException {
        try {
            int _type = T__43;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:36:7: ( 'QUERIES:' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:36:9: 'QUERIES:'
            {
            match("QUERIES:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__43"

    // $ANTLR start "T__44"
    public final void mT__44() throws RecognitionException {
        try {
            int _type = T__44;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:37:7: ( '<-' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:37:9: '<-'
            {
            match("<-"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__44"

    // $ANTLR start "T__45"
    public final void mT__45() throws RecognitionException {
        try {
            int _type = T__45;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:38:7: ( '::' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:38:9: '::'
            {
            match("::"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__45"

    // $ANTLR start "T__46"
    public final void mT__46() throws RecognitionException {
        try {
            int _type = T__46;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:39:7: ( ':.' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:39:9: ':.'
            {
            match(":."); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__46"

    // $ANTLR start "T__47"
    public final void mT__47() throws RecognitionException {
        try {
            int _type = T__47;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:40:7: ( '->' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:40:9: '->'
            {
            match("->"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__47"

    // $ANTLR start "T__48"
    public final void mT__48() throws RecognitionException {
        try {
            int _type = T__48;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:41:7: ( '^' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:41:9: '^'
            {
            match('^'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__48"

    // $ANTLR start "T__49"
    public final void mT__49() throws RecognitionException {
        try {
            int _type = T__49;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:42:7: ( 'NULL' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:42:9: 'NULL'
            {
            match("NULL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__49"

    // $ANTLR start "T__50"
    public final void mT__50() throws RecognitionException {
        try {
            int _type = T__50;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:43:7: ( '[' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:43:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__50"

    // $ANTLR start "T__51"
    public final void mT__51() throws RecognitionException {
        try {
            int _type = T__51;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:44:7: ( ']' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:44:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__51"

    // $ANTLR start "T__52"
    public final void mT__52() throws RecognitionException {
        try {
            int _type = T__52;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:45:7: ( '=' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:45:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__52"

    // $ANTLR start "T__53"
    public final void mT__53() throws RecognitionException {
        try {
            int _type = T__53;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:46:7: ( '!=' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:46:9: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__53"

    // $ANTLR start "T__54"
    public final void mT__54() throws RecognitionException {
        try {
            int _type = T__54;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:47:7: ( '<' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:47:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__54"

    // $ANTLR start "T__55"
    public final void mT__55() throws RecognitionException {
        try {
            int _type = T__55;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:48:7: ( '>' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:48:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__55"

    // $ANTLR start "T__56"
    public final void mT__56() throws RecognitionException {
        try {
            int _type = T__56;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:49:7: ( '>=' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:49:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__56"

    // $ANTLR start "T__57"
    public final void mT__57() throws RecognitionException {
        try {
            int _type = T__57;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:50:7: ( '<=' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:50:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__57"

    // $ANTLR start "T__58"
    public final void mT__58() throws RecognitionException {
        try {
            int _type = T__58;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:51:7: ( '<>' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:51:9: '<>'
            {
            match("<>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__58"

    // $ANTLR start "T__59"
    public final void mT__59() throws RecognitionException {
        try {
            int _type = T__59;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:52:7: ( 'LIKE' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:52:9: 'LIKE'
            {
            match("LIKE"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__59"

    // $ANTLR start "T__60"
    public final void mT__60() throws RecognitionException {
        try {
            int _type = T__60;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:53:7: ( 'IS' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:53:9: 'IS'
            {
            match("IS"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__60"

    // $ANTLR start "T__61"
    public final void mT__61() throws RecognitionException {
        try {
            int _type = T__61;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:54:7: ( 'NOT' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:54:9: 'NOT'
            {
            match("NOT"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__61"

    // $ANTLR start "T__62"
    public final void mT__62() throws RecognitionException {
        try {
            int _type = T__62;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:55:7: ( 'IN' )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:55:9: 'IN'
            {
            match("IN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__62"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:4: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '$' ) ( ( 'a' .. 'z' | 'A' .. 'Z' ) | ( '0' .. '9' ) | '_' | '#' )* | '`' (~ ( '\\'' | '\\n' | '\\r' | '`' ) ) ( (~ ( '\\'' | '\\n' | '\\r' | '`' ) ) )* '`' )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0=='$'||(LA3_0>='A' && LA3_0<='Z')||LA3_0=='_'||(LA3_0>='a' && LA3_0<='z')) ) {
                alt3=1;
            }
            else if ( (LA3_0=='`') ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:6: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '$' ) ( ( 'a' .. 'z' | 'A' .. 'Z' ) | ( '0' .. '9' ) | '_' | '#' )*
                    {
                    if ( input.LA(1)=='$'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}

                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:40: ( ( 'a' .. 'z' | 'A' .. 'Z' ) | ( '0' .. '9' ) | '_' | '#' )*
                    loop1:
                    do {
                        int alt1=5;
                        switch ( input.LA(1) ) {
                        case 'A':
                        case 'B':
                        case 'C':
                        case 'D':
                        case 'E':
                        case 'F':
                        case 'G':
                        case 'H':
                        case 'I':
                        case 'J':
                        case 'K':
                        case 'L':
                        case 'M':
                        case 'N':
                        case 'O':
                        case 'P':
                        case 'Q':
                        case 'R':
                        case 'S':
                        case 'T':
                        case 'U':
                        case 'V':
                        case 'W':
                        case 'X':
                        case 'Y':
                        case 'Z':
                        case 'a':
                        case 'b':
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'g':
                        case 'h':
                        case 'i':
                        case 'j':
                        case 'k':
                        case 'l':
                        case 'm':
                        case 'n':
                        case 'o':
                        case 'p':
                        case 'q':
                        case 'r':
                        case 's':
                        case 't':
                        case 'u':
                        case 'v':
                        case 'w':
                        case 'x':
                        case 'y':
                        case 'z':
                            {
                            alt1=1;
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
                            alt1=2;
                            }
                            break;
                        case '_':
                            {
                            alt1=3;
                            }
                            break;
                        case '#':
                            {
                            alt1=4;
                            }
                            break;

                        }

                        switch (alt1) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:42: ( 'a' .. 'z' | 'A' .. 'Z' )
                    	    {
                    	    if ( (input.LA(1)>='A' && input.LA(1)<='Z')||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;
                    	case 2 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:66: ( '0' .. '9' )
                    	    {
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:66: ( '0' .. '9' )
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:67: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }


                    	    }
                    	    break;
                    	case 3 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:79: '_'
                    	    {
                    	    match('_'); 

                    	    }
                    	    break;
                    	case 4 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:191:85: '#'
                    	    {
                    	    match('#'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);


                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:192:4: '`' (~ ( '\\'' | '\\n' | '\\r' | '`' ) ) ( (~ ( '\\'' | '\\n' | '\\r' | '`' ) ) )* '`'
                    {
                    match('`'); 
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:192:8: (~ ( '\\'' | '\\n' | '\\r' | '`' ) )
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:192:9: ~ ( '\\'' | '\\n' | '\\r' | '`' )
                    {
                    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='_')||(input.LA(1)>='a' && input.LA(1)<='\uFFFF') ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }

                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:192:32: ( (~ ( '\\'' | '\\n' | '\\r' | '`' ) ) )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>='\u0000' && LA2_0<='\t')||(LA2_0>='\u000B' && LA2_0<='\f')||(LA2_0>='\u000E' && LA2_0<='&')||(LA2_0>='(' && LA2_0<='_')||(LA2_0>='a' && LA2_0<='\uFFFF')) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:192:34: (~ ( '\\'' | '\\n' | '\\r' | '`' ) )
                    	    {
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:192:34: (~ ( '\\'' | '\\n' | '\\r' | '`' ) )
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:192:35: ~ ( '\\'' | '\\n' | '\\r' | '`' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='_')||(input.LA(1)>='a' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);

                    match('`'); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "FLOAT"
    public final void mFLOAT() throws RecognitionException {
        try {
            int _type = FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:193:7: ( ( '0' .. '9' )+ '.' ( '0' .. '9' )+ )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:193:9: ( '0' .. '9' )+ '.' ( '0' .. '9' )+
            {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:193:9: ( '0' .. '9' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:193:10: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

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

            match('.'); 
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:193:25: ( '0' .. '9' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:193:26: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FLOAT"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:194:5: ( ( '0' .. '9' )+ )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:194:7: ( '0' .. '9' )+
            {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:194:7: ( '0' .. '9' )+
            int cnt6=0;
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='0' && LA6_0<='9')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:194:8: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt6 >= 1 ) break loop6;
                        EarlyExitException eee =
                            new EarlyExitException(6, input);
                        throw eee;
                }
                cnt6++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "NUMERIC"
    public final void mNUMERIC() throws RecognitionException {
        try {
            int _type = NUMERIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:195:9: ( ( INT | FLOAT ) 'E' ( '+' | '-' )? INT )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:195:11: ( INT | FLOAT ) 'E' ( '+' | '-' )? INT
            {
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:195:11: ( INT | FLOAT )
            int alt7=2;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:195:12: INT
                    {
                    mINT(); 

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:195:18: FLOAT
                    {
                    mFLOAT(); 

                    }
                    break;

            }

            match('E'); 
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:195:29: ( '+' | '-' )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='+'||LA8_0=='-') ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:
                    {
                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                    }
                    break;

            }

            mINT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMERIC"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:196:8: ( '\"' (~ ( '\"' | '\\n' | '\\r' ) )* '\"' | '\\'' (~ ( '\\'' | '\\n' | '\\r' ) )* '\\'' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\"') ) {
                alt11=1;
            }
            else if ( (LA11_0=='\'') ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:196:10: '\"' (~ ( '\"' | '\\n' | '\\r' ) )* '\"'
                    {
                    match('\"'); 
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:196:14: (~ ( '\"' | '\\n' | '\\r' ) )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( ((LA9_0>='\u0000' && LA9_0<='\t')||(LA9_0>='\u000B' && LA9_0<='\f')||(LA9_0>='\u000E' && LA9_0<='!')||(LA9_0>='#' && LA9_0<='\uFFFF')) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:196:15: ~ ( '\"' | '\\n' | '\\r' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);

                    match('\"'); 

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:197:4: '\\'' (~ ( '\\'' | '\\n' | '\\r' ) )* '\\''
                    {
                    match('\''); 
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:197:9: (~ ( '\\'' | '\\n' | '\\r' ) )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( ((LA10_0>='\u0000' && LA10_0<='\t')||(LA10_0>='\u000B' && LA10_0<='\f')||(LA10_0>='\u000E' && LA10_0<='&')||(LA10_0>='(' && LA10_0<='\uFFFF')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:197:10: ~ ( '\\'' | '\\n' | '\\r' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='&')||(input.LA(1)>='(' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);

                    match('\''); 

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:198:4: ( ( ' ' | '\\t' | '\\r' | '\\n' ) )
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:198:6: ( ' ' | '\\t' | '\\r' | '\\n' )
            {
            if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            skip();

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:200:5: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n' | '/*' ( options {greedy=false; } : . )* '*/' )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0=='/') ) {
                int LA15_1 = input.LA(2);

                if ( (LA15_1=='/') ) {
                    alt15=1;
                }
                else if ( (LA15_1=='*') ) {
                    alt15=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:200:9: '//' (~ ( '\\n' | '\\r' ) )* ( '\\r' )? '\\n'
                    {
                    match("//"); 

                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:200:14: (~ ( '\\n' | '\\r' ) )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( ((LA12_0>='\u0000' && LA12_0<='\t')||(LA12_0>='\u000B' && LA12_0<='\f')||(LA12_0>='\u000E' && LA12_0<='\uFFFF')) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:200:14: ~ ( '\\n' | '\\r' )
                    	    {
                    	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
                    	        input.consume();

                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        recover(mse);
                    	        throw mse;}


                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);

                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:200:28: ( '\\r' )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0=='\r') ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:200:28: '\\r'
                            {
                            match('\r'); 

                            }
                            break;

                    }

                    match('\n'); 
                    _channel=HIDDEN;

                    }
                    break;
                case 2 :
                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:201:9: '/*' ( options {greedy=false; } : . )* '*/'
                    {
                    match("/*"); 

                    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:201:14: ( options {greedy=false; } : . )*
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( (LA14_0=='*') ) {
                            int LA14_1 = input.LA(2);

                            if ( (LA14_1=='/') ) {
                                alt14=2;
                            }
                            else if ( ((LA14_1>='\u0000' && LA14_1<='.')||(LA14_1>='0' && LA14_1<='\uFFFF')) ) {
                                alt14=1;
                            }


                        }
                        else if ( ((LA14_0>='\u0000' && LA14_0<=')')||(LA14_0>='+' && LA14_0<='\uFFFF')) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:201:42: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop14;
                        }
                    } while (true);

                    match("*/"); 

                    _channel=HIDDEN;

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    public void mTokens() throws RecognitionException {
        // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:8: ( T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | ID | FLOAT | INT | NUMERIC | STRING | WS | COMMENT )
        int alt16=39;
        alt16 = dfa16.predict(input);
        switch (alt16) {
            case 1 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:10: T__31
                {
                mT__31(); 

                }
                break;
            case 2 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:16: T__32
                {
                mT__32(); 

                }
                break;
            case 3 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:22: T__33
                {
                mT__33(); 

                }
                break;
            case 4 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:28: T__34
                {
                mT__34(); 

                }
                break;
            case 5 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:34: T__35
                {
                mT__35(); 

                }
                break;
            case 6 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:40: T__36
                {
                mT__36(); 

                }
                break;
            case 7 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:46: T__37
                {
                mT__37(); 

                }
                break;
            case 8 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:52: T__38
                {
                mT__38(); 

                }
                break;
            case 9 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:58: T__39
                {
                mT__39(); 

                }
                break;
            case 10 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:64: T__40
                {
                mT__40(); 

                }
                break;
            case 11 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:70: T__41
                {
                mT__41(); 

                }
                break;
            case 12 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:76: T__42
                {
                mT__42(); 

                }
                break;
            case 13 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:82: T__43
                {
                mT__43(); 

                }
                break;
            case 14 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:88: T__44
                {
                mT__44(); 

                }
                break;
            case 15 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:94: T__45
                {
                mT__45(); 

                }
                break;
            case 16 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:100: T__46
                {
                mT__46(); 

                }
                break;
            case 17 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:106: T__47
                {
                mT__47(); 

                }
                break;
            case 18 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:112: T__48
                {
                mT__48(); 

                }
                break;
            case 19 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:118: T__49
                {
                mT__49(); 

                }
                break;
            case 20 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:124: T__50
                {
                mT__50(); 

                }
                break;
            case 21 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:130: T__51
                {
                mT__51(); 

                }
                break;
            case 22 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:136: T__52
                {
                mT__52(); 

                }
                break;
            case 23 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:142: T__53
                {
                mT__53(); 

                }
                break;
            case 24 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:148: T__54
                {
                mT__54(); 

                }
                break;
            case 25 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:154: T__55
                {
                mT__55(); 

                }
                break;
            case 26 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:160: T__56
                {
                mT__56(); 

                }
                break;
            case 27 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:166: T__57
                {
                mT__57(); 

                }
                break;
            case 28 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:172: T__58
                {
                mT__58(); 

                }
                break;
            case 29 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:178: T__59
                {
                mT__59(); 

                }
                break;
            case 30 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:184: T__60
                {
                mT__60(); 

                }
                break;
            case 31 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:190: T__61
                {
                mT__61(); 

                }
                break;
            case 32 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:196: T__62
                {
                mT__62(); 

                }
                break;
            case 33 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:202: ID
                {
                mID(); 

                }
                break;
            case 34 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:205: FLOAT
                {
                mFLOAT(); 

                }
                break;
            case 35 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:211: INT
                {
                mINT(); 

                }
                break;
            case 36 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:215: NUMERIC
                {
                mNUMERIC(); 

                }
                break;
            case 37 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:223: STRING
                {
                mSTRING(); 

                }
                break;
            case 38 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:230: WS
                {
                mWS(); 

                }
                break;
            case 39 :
                // C:\\Documents and Settings\\mariam\\My Documents\\mediator-new\\workspace\\domainparser\\DomainModel.g:1:233: COMMENT
                {
                mCOMMENT(); 

                }
                break;

        }

    }


    protected DFA7 dfa7 = new DFA7(this);
    protected DFA16 dfa16 = new DFA16(this);
    static final String DFA7_eotS =
        "\4\uffff";
    static final String DFA7_eofS =
        "\4\uffff";
    static final String DFA7_minS =
        "\1\60\1\56\2\uffff";
    static final String DFA7_maxS =
        "\1\71\1\105\2\uffff";
    static final String DFA7_acceptS =
        "\2\uffff\1\2\1\1";
    static final String DFA7_specialS =
        "\4\uffff}>";
    static final String[] DFA7_transitionS = {
            "\12\1",
            "\1\2\1\uffff\12\1\13\uffff\1\3",
            "",
            ""
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "195:11: ( INT | FLOAT )";
        }
    }
    static final String DFA16_eotS =
        "\1\uffff\4\26\1\43\3\uffff\4\26\1\55\6\uffff\1\57\1\26\1\uffff"+
        "\1\63\3\uffff\6\26\3\uffff\6\26\6\uffff\1\101\1\102\3\uffff\5\26"+
        "\1\111\6\26\2\uffff\1\120\4\26\1\125\1\uffff\1\26\1\127\4\26\1\uffff"+
        "\4\26\1\uffff\1\26\1\uffff\36\26\1\uffff\12\26\1\uffff\1\26\1\uffff"+
        "\1\26\2\uffff\2\26\2\uffff\4\26\2\uffff";
    static final String DFA16_eofS =
        "\u0091\uffff";
    static final String DFA16_minS =
        "\1\11\2\117\1\125\1\101\1\56\3\uffff\3\101\1\125\1\55\6\uffff\1"+
        "\75\1\116\1\uffff\1\56\3\uffff\1\125\1\115\1\116\1\115\1\114\1\124"+
        "\3\uffff\1\126\1\113\1\101\1\126\1\103\1\105\6\uffff\2\43\1\60\2"+
        "\uffff\1\122\1\101\1\103\1\105\1\114\1\43\1\137\1\105\1\126\2\137"+
        "\1\122\2\uffff\1\60\1\103\1\111\1\124\1\123\1\43\1\uffff\1\122\1"+
        "\43\1\137\2\122\1\111\1\uffff\1\105\1\116\1\111\1\120\1\uffff\1"+
        "\125\1\uffff\1\122\2\125\1\105\2\137\1\117\1\101\1\114\1\125\2\114"+
        "\3\123\1\116\1\103\1\105\1\114\2\105\1\72\2\103\1\123\1\105\1\123"+
        "\1\105\2\123\1\uffff\2\110\1\72\1\123\1\72\1\123\2\72\2\105\1\uffff"+
        "\1\72\1\uffff\1\72\2\uffff\2\115\2\uffff\2\101\2\72\2\uffff";
    static final String DFA16_maxS =
        "\1\172\2\117\2\125\1\72\3\uffff\1\111\1\114\1\101\1\125\1\76\6"+
        "\uffff\1\75\1\123\1\uffff\1\105\3\uffff\1\125\1\115\1\116\1\115"+
        "\1\114\1\124\3\uffff\1\126\1\113\1\101\1\126\1\103\1\105\6\uffff"+
        "\2\172\1\71\2\uffff\1\122\1\101\1\103\1\105\1\114\1\172\1\137\1"+
        "\105\1\126\2\137\1\122\2\uffff\1\105\1\103\1\111\1\124\1\123\1\172"+
        "\1\uffff\1\122\1\172\1\137\2\122\1\111\1\uffff\1\105\1\116\1\111"+
        "\1\120\1\uffff\1\125\1\uffff\1\122\2\125\1\105\2\137\1\117\1\101"+
        "\1\114\1\125\2\114\3\123\1\116\1\103\1\105\1\114\2\105\1\72\2\103"+
        "\1\123\1\105\1\123\1\105\2\123\1\uffff\2\110\1\72\1\123\1\72\1\123"+
        "\2\72\2\105\1\uffff\1\72\1\uffff\1\72\2\uffff\2\115\2\uffff\2\101"+
        "\2\72\2\uffff";
    static final String DFA16_acceptS =
        "\6\uffff\1\6\1\7\1\10\5\uffff\1\21\1\22\1\24\1\25\1\26\1\27\2\uffff"+
        "\1\41\1\uffff\1\45\1\46\1\47\6\uffff\1\17\1\20\1\5\6\uffff\1\16"+
        "\1\33\1\34\1\30\1\32\1\31\3\uffff\1\43\1\44\14\uffff\1\36\1\40\6"+
        "\uffff\1\37\6\uffff\1\42\4\uffff\1\23\1\uffff\1\35\36\uffff\1\15"+
        "\12\uffff\1\3\1\uffff\1\11\1\uffff\1\13\1\14\2\uffff\1\4\1\12\4"+
        "\uffff\1\1\1\2";
    static final String DFA16_specialS =
        "\u0091\uffff}>";
    static final String[] DFA16_transitionS = {
            "\2\31\2\uffff\1\31\22\uffff\1\31\1\23\1\30\1\uffff\1\26\2\uffff"+
            "\1\30\1\6\1\10\2\uffff\1\7\1\16\1\uffff\1\32\12\27\1\5\1\uffff"+
            "\1\15\1\22\1\24\2\uffff\3\26\1\2\1\26\1\3\1\12\1\26\1\25\2\26"+
            "\1\11\1\26\1\4\2\26\1\14\1\26\1\1\1\26\1\13\5\26\1\20\1\uffff"+
            "\1\21\1\17\34\26",
            "\1\33",
            "\1\34",
            "\1\35",
            "\1\36\15\uffff\1\40\5\uffff\1\37",
            "\1\42\13\uffff\1\41",
            "",
            "",
            "",
            "\1\44\7\uffff\1\45",
            "\1\47\12\uffff\1\46",
            "\1\50",
            "\1\51",
            "\1\52\17\uffff\1\53\1\54",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\56",
            "\1\61\4\uffff\1\60",
            "",
            "\1\62\1\uffff\12\27\13\uffff\1\64",
            "",
            "",
            "",
            "\1\65",
            "\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\72",
            "",
            "",
            "",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
            "\1\77",
            "\1\100",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\26\14\uffff\12\26\7\uffff\32\26\4\uffff\1\26\1\uffff\32"+
            "\26",
            "\1\26\14\uffff\12\26\7\uffff\32\26\4\uffff\1\26\1\uffff\32"+
            "\26",
            "\12\103",
            "",
            "",
            "\1\104",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\26\14\uffff\12\26\7\uffff\32\26\4\uffff\1\26\1\uffff\32"+
            "\26",
            "\1\112",
            "\1\113",
            "\1\114",
            "\1\115",
            "\1\116",
            "\1\117",
            "",
            "",
            "\12\103\13\uffff\1\64",
            "\1\121",
            "\1\122",
            "\1\123",
            "\1\124",
            "\1\26\14\uffff\12\26\7\uffff\32\26\4\uffff\1\26\1\uffff\32"+
            "\26",
            "",
            "\1\126",
            "\1\26\14\uffff\12\26\7\uffff\32\26\4\uffff\1\26\1\uffff\32"+
            "\26",
            "\1\130",
            "\1\131",
            "\1\132",
            "\1\133",
            "",
            "\1\134",
            "\1\135",
            "\1\136",
            "\1\137",
            "",
            "\1\140",
            "",
            "\1\141",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\145",
            "\1\146",
            "\1\147",
            "\1\150",
            "\1\151",
            "\1\152",
            "\1\153",
            "\1\154",
            "\1\155",
            "\1\156",
            "\1\157",
            "\1\160",
            "\1\161",
            "\1\162",
            "\1\163",
            "\1\164",
            "\1\165",
            "\1\166",
            "\1\167",
            "\1\170",
            "\1\171",
            "\1\172",
            "\1\173",
            "\1\174",
            "\1\175",
            "\1\176",
            "",
            "\1\177",
            "\1\u0080",
            "\1\u0081",
            "\1\u0082",
            "\1\u0083",
            "\1\u0084",
            "\1\u0085",
            "\1\u0086",
            "\1\u0087",
            "\1\u0088",
            "",
            "\1\u0089",
            "",
            "\1\u008a",
            "",
            "",
            "\1\u008b",
            "\1\u008c",
            "",
            "",
            "\1\u008d",
            "\1\u008e",
            "\1\u008f",
            "\1\u0090",
            "",
            ""
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__31 | T__32 | T__33 | T__34 | T__35 | T__36 | T__37 | T__38 | T__39 | T__40 | T__41 | T__42 | T__43 | T__44 | T__45 | T__46 | T__47 | T__48 | T__49 | T__50 | T__51 | T__52 | T__53 | T__54 | T__55 | T__56 | T__57 | T__58 | T__59 | T__60 | T__61 | T__62 | ID | FLOAT | INT | NUMERIC | STRING | WS | COMMENT );";
        }
    }
 

}
