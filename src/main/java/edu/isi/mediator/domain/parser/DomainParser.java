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

package edu.isi.mediator.domain.parser;

import java.util.ArrayList;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

import edu.isi.mediator.domain.DomainModel;
import edu.isi.mediator.domain.parser.grammar.DomainModelLexer;
import edu.isi.mediator.domain.parser.grammar.DomainModelParser;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.ANTLRNoCaseStringStream;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.gav.util.MediatorLogger;
import edu.isi.mediator.gav.util.ParserRecognitionException;
import edu.isi.mediator.rdf.RDFDomainModel;
import edu.isi.mediator.rule.GLAVRule;
import edu.isi.mediator.rule.LAVRule;

/**
 * Domain File parser.
 * @author mariam
 *
 */
public class DomainParser {
	
	private static final MediatorLogger logger = MediatorLogger.getLogger(DomainParser.class.getName());

	/**
	 * Parse domain file and populate Mediator data structures.
	 * @param domainStr
	 * 			Domain file sections as string
	 * @param dm
	 * 			the DomainModel
	 * @throws MediatorException
	 */
	public DomainModel parseDomain(String domainStr) throws MediatorException{
		
		DomainModel dm = new DomainModel();
		CommonTree t = parse(domainStr);
		
		logger.debug("AST=" + t.toStringTree());

		SchemaParser schemaParser = new SchemaParser();
		GAVRuleParser gavRuleParser = new GAVRuleParser();
		LAVRuleParser lavRuleParser = new LAVRuleParser();
		GLAVRuleParser glavRuleParser = new GLAVRuleParser();

		//first parse the functions
		ArrayList<String> functions = new ArrayList<String>();
		for(int i=0; i<t.getChildCount(); i++){
			CommonTree child = (CommonTree) t.getChild(i);
			if(child.getText().equals(MediatorConstants.FUNCTIONS)){
				functions = schemaParser.parseFunctions(child);
				break;
			}
		}
		
		for(int i=0; i<t.getChildCount(); i++){
			CommonTree child = (CommonTree) t.getChild(i);
			//System.out.println(child.getText());
			if(child.getText().equals(MediatorConstants.SOURCE_SCHEMA)){
				schemaParser.parseSourceSchema(child, functions, dm);
			}
			else if(child.getText().equals(MediatorConstants.DOMAIN_SCHEMA)){
				schemaParser.parseDomainSchema(child, dm);
			}
			else if(child.getText().equals(MediatorConstants.GAV_RULES)){
				gavRuleParser.parseGAVRules(child, dm);
			}
			else if(child.getText().equals(MediatorConstants.GLAV_RULES)){
				ArrayList<GLAVRule> glavRules = glavRuleParser.parseGLAVRules(child);
				dm.setGLAVRules(glavRules);
			}
			else if(child.getText().equals(MediatorConstants.LAV_RULES)){
				ArrayList<LAVRule> lavRules = lavRuleParser.parseLAVRules(child);
				dm.setLAVRules(lavRules);
			}
			else if(child.getText().equals(MediatorConstants.NAMESPACES)){
				//appears in RDF generation
				dm = new RDFDomainModel(dm);
				lavRuleParser.parseNamespaces(child, (RDFDomainModel)dm);
			}
		}
		return dm;
	}
	
	/**
	 * @param domainStr
	 * 			Domain file sections as string
	 * @return Abstract Syntax Tree
	 * @throws MediatorException
	 */
	public CommonTree parse(String domainStr) throws MediatorException{
		
		//converts the input string to uppercase; so we can have case insensitive parsing
		//in grammar only use uppercase constant values
        DomainModelLexer lex = new DomainModelLexer(new ANTLRNoCaseStringStream(
                domainStr));
		//passes the input string as is
        //DomainModelLexer lex = new DomainModelLexer(new ANTLRStringStream(domainStr));

        CommonTokenStream tokens = new CommonTokenStream(lex);
		//System.out.println("Return ..." + tokens.toString());

        DomainModelParser g = new DomainModelParser(tokens);

        try
        {
        	DomainModelParser.domain_model_return result = g.domain_model();
            return (CommonTree) result.getTree();
        }
        catch (RecognitionException e)
        {
            throw new MediatorException(ParserRecognitionException
                    .getInstance(e).toString());
        }

	}
}
