// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.domain.parser;

import java.util.ArrayList;

import org.antlr.runtime.tree.CommonTree;

import edu.isi.mediator.domain.parser.DomainParser;
import edu.isi.mediator.domain.parser.RuleParser;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rdf.RDFDomainModel;
import edu.isi.mediator.rule.LAVRule;
import edu.isi.mediator.rule.Predicate;

/**
 * Parser for Domain file containing LAV Rules.
 * @author Maria Muslea(USC/ISI)
 *
 */
public class LAVRuleParser{
	
	public LAVRuleParser(){}

	/**
	 * Parse the namespaces and split in prefix and namespace name.
	 * @param t
	 * 		the AST for NAMESPACES.
	 * <p>For Example: (NAMESPACES (s 'http://www.domain.org/source/') (dv 'http://www.domain.org/ontology/'))
	 */
	public void parseNamespaces(CommonTree t, RDFDomainModel dm){
		for(int i=0; i<t.getChildCount(); i++){
			CommonTree child = (CommonTree) t.getChild(i);
			//System.out.println(child.getText());
			String prefix = child.getText();
			String namespace = child.getChild(0).getText();
			//remove the quotes
			namespace = namespace.substring(1, namespace.length()-1);
			//prefixes that start with "s" are source namespaces
			if(prefix.startsWith(RDFDomainModel.SOURCE_PREFIX)){
				dm.addSourceNamespace(prefix, namespace);
			}
			else
				dm.addOntologyNamespace(prefix, namespace);
		}
	}
	
	/**
	 * Parse LAV Rules and populate the lavRules member.
	 * @param rules
	 * 			the AST for LAV Rules
	 * @throws MediatorException
	 */
	public ArrayList<LAVRule> parseLAVRules(CommonTree rules) throws MediatorException{
		
		ArrayList<LAVRule> lavRules = new ArrayList<LAVRule>();
	
		for(int i=0; i<rules.getChildCount(); i++){
			CommonTree child = (CommonTree) rules.getChild(i);
		
			LAVRule lavRule =  parseLAVRule((CommonTree)child);
			lavRules.add(lavRule);
		}
		return lavRules;
	}
	
	/**
	 * Parse LAV Rule and build data structures.
	 * @param rule
	 * 			the AST for LAv Rule
	 * @throws MediatorException
	 */
	public LAVRule parseLAVRule(String rule) throws MediatorException{
		if(!rule.startsWith("LAV_RULES:"))
			rule = "LAV_RULES:" + rule;
		
		DomainParser dp = new DomainParser();
		CommonTree t = dp.parse(rule);
		
		//System.out.println("AST=" + t.toStringTree());

		//there should be one child that contains the query
		//t.getChild(0)is the QUERIES node
		if(t.getChild(0).getChildCount()!=1){
			throw new MediatorException("There should be only one query in:" + t);
		}
		
		CommonTree child = (CommonTree) t.getChild(0).getChild(0);
		
		LAVRule lavRule =  parseLAVRule(child);
		//System.out.println("Parsed query=" + query);
		return lavRule;
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
	protected LAVRule parseLAVRule(CommonTree rule) throws MediatorException{

		//System.out.println("The Rule is:" + rule.toStringTree());
		
		RuleParser rp = new RuleParser();
		CommonTree antecedentTree = (CommonTree)rule.getChild(0);
		CommonTree consequentTree = (CommonTree)rule.getChild(1);

		LAVRule r = new LAVRule();
		
		ArrayList<Predicate> antecedent = rp.parseConjunction(antecedentTree, null);
		ArrayList<Predicate> consequent = rp.parseConjunction(consequentTree, null);
		
		r.addHead(antecedent.get(0));
		r.addBody(consequent);
		
		if(r.isValid()){
			// all vars in the head are in the body
			return r;
		}
		else
			return null;
	}	

}
