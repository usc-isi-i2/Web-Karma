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

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.GLAVRule;
import edu.isi.mediator.rule.Predicate;

/**
 * Parser for Domain file containing GLAV Rules.
 * @author Maria Muslea(USC/ISI)
 *
 */
public class GLAVRuleParser{

	public GLAVRuleParser(){}

	/**
	 * Parse GLAV Rules and populate the glavRules member.
	 * @param rules
	 * 			the AST for GLAV Rules
	 * @throws MediatorException
	 */
	public ArrayList<GLAVRule> parseGLAVRules(CommonTree rules) throws MediatorException{
		
		ArrayList<GLAVRule> glavRules = new ArrayList<GLAVRule>();
	
		for(int i=0; i<rules.getChildCount(); i++){
			CommonTree child = (CommonTree) rules.getChild(i);
		
			GLAVRule glavRule =  parseGLAVRule((CommonTree)child);
			glavRules.add(glavRule);
		}
		return glavRules;
	}
	
	/**
	 * Parse GLAV Rule and build data structures.
	 * @param rule
	 * 			the AST for LAv Rule
	 * @throws MediatorException
	 */
	public GLAVRule parseGLAVRule(String rule) throws MediatorException{
		if(!rule.startsWith("GLAV_RULES:"))
			rule = "GLAV_RULES:" + rule;
		
		DomainParser dp = new DomainParser();
		CommonTree t = dp.parse(rule);
		
		//System.out.println("AST=" + t.toStringTree());

		//there should be one child that contains the query
		//t.getChild(0)is the QUERIES node
		if(t.getChild(0).getChildCount()!=1){
			throw new MediatorException("There should be only one query in:" + t);
		}
		
		CommonTree child = (CommonTree) t.getChild(0).getChild(0);
		
		GLAVRule glavRule =  parseGLAVRule(child);
		//System.out.println("Parsed query=" + query);
		return glavRule;
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
	protected GLAVRule parseGLAVRule(CommonTree rule) throws MediatorException{

		//System.out.println("The Rule is:" + rule.toStringTree());
		
		RuleParser rp = new RuleParser();
		CommonTree antecedentTree = (CommonTree)rule.getChild(0);
		CommonTree consequentTree = (CommonTree)rule.getChild(1);

		GLAVRule r = new GLAVRule();
		
		ArrayList<Predicate> antecedent = rp.parseConjunction(antecedentTree, null);
		ArrayList<Predicate> consequent = rp.parseConjunction(consequentTree, null);
		
		r.addAntecedent(antecedent);
		r.addConsequent(consequent);
		
		if(r.isValid()){
			return r;
		}
		else
			return null;
	}	
}
