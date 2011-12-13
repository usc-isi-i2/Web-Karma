// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.domain.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

import edu.isi.mediator.domain.DomainModel;
import edu.isi.mediator.domain.parser.DomainParser;
import edu.isi.mediator.domain.parser.RuleParser;
import edu.isi.mediator.domain.parser.grammar.DomainModelLexer;
import edu.isi.mediator.domain.parser.grammar.DomainModelParser;
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
