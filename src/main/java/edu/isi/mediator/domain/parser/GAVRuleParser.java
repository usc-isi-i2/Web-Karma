// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__


package edu.isi.mediator.domain.parser;

import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;

import edu.isi.mediator.domain.DomainModel;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.GAVRule;
import edu.isi.mediator.gav.main.MediatorException;

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
