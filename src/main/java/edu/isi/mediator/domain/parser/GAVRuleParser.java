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

import org.antlr.runtime.tree.CommonTree;

import edu.isi.mediator.domain.DomainModel;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.GAVRule;
import edu.isi.mediator.rule.Predicate;

/**
 * GAV Rule parser.
 * @author mariam
 *
 */
public class GAVRuleParser {
	
	
	/**
	 * Parse GAV Rules and populate Mediator data structures
	 * @param rules
	 * 			rules as AST
	 * @param dm
	 * 			the DomainModel
	 * @throws MediatorException
	 */
	protected void parseGAVRules(CommonTree rules, DomainModel dm) throws MediatorException{

		for(int i=0; i<rules.getChildCount(); i++){
			CommonTree rule = (CommonTree) rules.getChild(i);
			GAVRule oneRule = parseGAVRule(rule,dm);
			//System.out.println("Rule=" + oneRule);
			dm.addGAVRule((GAVRule)oneRule);
		}
	}

	/**
	 * Parse one rule.
	 * @param rule
	 * 			rule as AST
	 * @param dm
	 * 			the DomainModel
	 * @return	Mediator Rule
	 * @throws MediatorException 
	 */
	public GAVRule parseGAVRule(CommonTree rule,DomainModel dm) throws MediatorException{

		//System.out.println("The Rule is:" + rule.toStringTree());
		
		RuleParser rp = new RuleParser();
		CommonTree antecedentTree = (CommonTree)rule.getChild(0);
		CommonTree consequentTree = (CommonTree)rule.getChild(1);

		GAVRule r = new GAVRule();
		
		ArrayList<Predicate> antecedent = rp.parseConjunction(antecedentTree, dm);
		ArrayList<Predicate> consequent = rp.parseConjunction(consequentTree, dm);
		
		r.addHead(consequent.get(0));
		r.addBody(antecedent);
		
		if(r.isValid()){
			// all vars in the head are in the body
			return r;
		}
		else
			return null;
	}	

}
