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

package edu.isi.mediator.rule;

import java.util.ArrayList;

import edu.isi.mediator.domain.parser.LAVRuleParser;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.BuiltInPredicate;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.RelationPredicate;
import edu.isi.mediator.rule.Rule;

/**
 * @author Maria Muslea(USC/ISI)
 *
 */
public class LAVRule extends Rule{

	//LAV_RULES: antecedent -> consequent (one predicate in antecedent)

	/**
	 * Constructs an empty LAVRule.
	 */
	public LAVRule(){}

	/**
	 * Constructs a LAVRule.
	 * @param rule
	 * 		the rule as a string
	 * @throws MediatorException
	 */
	public LAVRule(String rule) throws MediatorException{
		LAVRuleParser parser = new LAVRuleParser();
		LAVRule r = parser.parseLAVRule(rule);
		antecedent.add(r.getHead());
		consequent=r.getBody();

		//System.out.println("Rule is:" + this);
	}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Rule#clone()
	 */
	public LAVRule clone(){
		LAVRule newQ = new LAVRule();
		newQ.addHead(getHead().clone());
		for(int i=0; i<consequent.size(); i++){
			Predicate p = consequent.get(i);
			newQ.consequent.add(p.clone());
		}
		return newQ;
	}

	/**
	 * Returns the head of the rule.
	 * @return
	 * 		the head of the rule.
	 */
	public Predicate getHead(){
		return antecedent.get(0);
	}

	
	/**
	 * sets the head of the rule.
	 * @param p
	 */
	public void addHead(Predicate p){
		antecedent.add(p);
	}

	/**
	 * Returns the body of the rule.
	 * @return
	 */
	public ArrayList<Predicate> getBody(){
		return consequent;
	}

	/**
	 * sets the body of the rule.
	 * @param body
	 */
	public void addBody(ArrayList<Predicate> body){
		consequent=body;
	}

	/**
	 * Adds a predicate to the body.
	 * @param p
	 */
	public void addPredicate(Predicate p){
		consequent.add(p);
	}

	/**
	 * Returns all BuiltInPredicates in the body.
	 * @return
	 */
	protected ArrayList<BuiltInPredicate> getNonRelations(){
		ArrayList<BuiltInPredicate> rels = new ArrayList<BuiltInPredicate>();
		for(int i=0; i<consequent.size(); i++){
			Predicate p = consequent.get(i);
			if(p instanceof BuiltInPredicate)
				rels.add((BuiltInPredicate)p);
		}
		return rels;
	}

	/**
	 * Checks validity of this Rule.
	 * @return
	 * 		true if predicate is valid, false otherwise.
	 * <br> make sure that all variables in the head are present in the body
	 * <br> make sure that all variables in a built-in predicate (x=3) appear in a body relation or the head
	 * @throws MediatorException
	 */
	public boolean isValid(){
		//I am not sure yet what this method should do for LAV rules
		return true;
	}
	public boolean isValidOld() throws MediatorException{
		ArrayList<String> headVars = antecedent.get(0).getVars();
		ArrayList<String> bodyVars = new ArrayList<String>();
		ArrayList<String> bodyRelationVars = new ArrayList<String>();
		for(int i=0; i<consequent.size(); i++){
			Predicate p = consequent.get(i);
			bodyVars.addAll(p.getVars());
			if(p instanceof RelationPredicate)
				bodyRelationVars.addAll(p.getVars());
		}
		
		//make sure that all variables in the head are present in the body
		for( int j=0; j<headVars.size(); j++){
			String headVar = headVars.get(j);
			//variable used in user access rule
			if(!bodyVars.contains(headVar)){
				throw(new MediatorException("The head variable " + headVar + " does not appear in the body of rule " + this));
			}
		}

		//make sure that all variables in a built-in predicate (x=3) appear in a relation in the body or head
		ArrayList<BuiltInPredicate> nonRels = getNonRelations();
		for(int i=0; i<nonRels.size(); i++){
			BuiltInPredicate p = nonRels.get(i);
			//System.out.println("Validate predicate : " + p);
			ArrayList<String> pVars = p.getVars();
			for(int k=0; k<pVars.size(); k++){
				String pVar = pVars.get(k);
				//System.out.println("Check var : " + pVar + " in " + bodyRelationVars + " and " + headVars);
				if(!bodyRelationVars.contains(pVar) && !headVars.contains(pVar))
					throw(new MediatorException("The variable " + pVar + " does not appear in a body relation or head of rule " + this));
				else{
					if(!bodyRelationVars.contains(pVar)){
						//it is in the head, but not the body => in the graph it will be represented as an AssignNode
						((BuiltInPredicate)p).isAssignment(true);
					}
				}
			}
		}
		return true;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String s = "";
		s+= antecedent.get(0) + "<-";
		for(int i=0; i<consequent.size(); i++){
			if(i>0) s += " ^ \n\t";
			s += consequent.get(i).toString();
		}
		return s;
	}

}
