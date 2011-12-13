// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;

import java.util.ArrayList;


/**
 * Defines a Rule.
 * @author mariam
 *
 */
abstract public class Rule{
	
	//GAV_RULES: consequent <- antecedent (one predicate in consequent)
	//LAV_RULES: antecedent -> consequent (one predicate in antecedent)
	//GLAV_RULES: antecedent -> consequent (multiple predicates in antecedent and consequent)
	
	/**
	 * antecedent
	 */
	protected ArrayList<Predicate> antecedent = new ArrayList<Predicate>(); 
	/**
	 * consequent
	 */
	protected ArrayList<Predicate> consequent = new ArrayList<Predicate>();
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	abstract public Rule clone();
		
	/**
	 * Adds a predicate to antecedent.
	 * @param p
	 */
	public void addAntecedentPredicate(Predicate p){
		antecedent.add(p);
	}

	/**
	 * Adds a predicate to consequent.
	 * @param p
	 */
	public void addConsequentPredicate(Predicate p){
		consequent.add(p);
	}

	/**
	 * sets the Antecedent.
	 * @param predicates
	 */
	public void addAntecedent(ArrayList<Predicate> predicates){
		antecedent=predicates;
	}
	
	/**
	 * sets the Consequent.
	 * @param predicates
	 */
	public void addConsequent(ArrayList<Predicate> predicates){
		consequent=predicates;
	}

	/**
	 * Returns the Antecedent of the rule.
	 * @return
	 */
	public ArrayList<Predicate> getAntecedent(){
		return antecedent;
	}
	
	/**
	 * Returns the Consequent of the rule.
	 * @return
	 */
	public ArrayList<Predicate> getConsequent(){
		return consequent;
	}

	/**
	 * Returns all variables in antecedent.
	 * @return
	 */
	public ArrayList<String> getAllAntecedentVars(){
		ArrayList<String> vars = new ArrayList<String>();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			vars.addAll(p.getVars());
		}
		return vars;
	}

	public String antecedentToString(){
		String s = "";
		for(int i=0; i<antecedent.size(); i++){
			if(i>0) s += " ^ \t";
			s += antecedent.get(i).toString();
		}
		return s;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String s = "";
		for(int i=0; i<antecedent.size(); i++){
			if(i>0) s += " ^ \n\t";
			s += antecedent.get(i).toString();
		}
		s+= "->";
		for(int i=0; i<consequent.size(); i++){
			if(i>0) s += " ^ \n\t";
			s += consequent.get(i).toString();
		}
		return s;
	}

}