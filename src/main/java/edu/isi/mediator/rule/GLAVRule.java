// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;


import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.Rule;


/**
 * Defines a Rule.
 * @author mariam
 *
 */
public class GLAVRule extends Rule{
	
	//GLAV_RULES: antecedent -> consequent (multiple predicates in antecedent and consequent)

	public GLAVRule(){}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Rule#clone()
	 */
	public GLAVRule clone(){
		GLAVRule r = new GLAVRule();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			r.antecedent.add(p.clone());
		}
		for(int i=0; i<consequent.size(); i++){
			Predicate p = consequent.get(i);
			r.consequent.add(p.clone());
		}
		return r;
	}

	public boolean isValid(){
		//I am not sure yet what this method should do for LAV rules
		return true;
	}

}