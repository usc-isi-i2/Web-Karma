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
import java.util.HashSet;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorConstants;


/**
 * Defines a Rule.
 * @author mariam
 *
 */
public class GAVRule extends Rule{
	
	//GAV_RULES: consequent <- antecedent (one predicate in consequent)
	/**
	 * antecedent; the body of the GAV rule
	 */
	/**
	 * consequent; has only one predicate that we will call head
	 */
	
	//for UAC
	/**
	 * variable used in user access rule to denote ALL variables are visible in that predicate
	 */
	static public String ALL_VAR = "_all_";
	//it denotes that the predicate is not visible at all, can't even be used in an internal join
	/**
	 * variable used in user access rule to denote NO variables are visible in that predicate
	 */
	static public String NONE_VAR = "_none_";
	//
	//it denotes that the predicate is not visible at all, can be used in an internal join
	/**
	 * variable used in user access rule to denote NO variables are visible in that predicate,
	 * <br>but predicate can be used in a  join
	 */
	static public String NONE_BUT_JOINABLE_VAR = "_none_but_joinable_";

	//for UAR
	public void setAllowedProjections(Predicate qHead, Binding bind){}
	//defined in UAR, and returns true if the type of the rule is VarType.NONE
	public boolean isNoneRule(){return false;}
	public boolean isUACRule(){return false;}
	public boolean isUACVar(String v){
		if(v.equals(ALL_VAR) || v.equals(NONE_VAR) || v.equals(NONE_BUT_JOINABLE_VAR))
				return true;
		return false;
	}
	public boolean isNullVar(String v){
		if(v.toUpperCase().equals(MediatorConstants.NULL_VALUE))
				return true;
		return false;
	}
	//////////////////////////////

	public GAVRule(){}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Rule#clone()
	 */
	public GAVRule clone(){
		GAVRule r = new GAVRule();
		r.consequent.add(consequent.get(0).clone());
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			r.antecedent.add(p.clone());
		}
		return r;
	}

	/**
	 * Adds a predicate to the body.
	 * @param p
	 */
	public void addPredicate(Predicate p){
		antecedent.add(p);
	}

	/**
	 * sets the head of the rule.
	 * @param p
	 */
	public void addHead(Predicate p){
		consequent.add(p);
	}

	/**
	 * sets the body of the rule.
	 * @param body
	 */
	public void addBody(ArrayList<Predicate> body){
		antecedent=body;
	}
	
	/**
	 * Returns the head of the rule.
	 * @return
	 * 		the head of the rule.
	 */
	public Predicate getHead(){
		return consequent.get(0);
	}
	
	/**
	 * Returns the body of the rule.
	 * @return
	 */
	public ArrayList<Predicate> getBody(){
		return antecedent;
	}
	
	/**
	 * Returns all RelationPredicates in the body.
	 * @return
	 */
	public ArrayList<RelationPredicate> getRelations(){
		ArrayList<RelationPredicate> rels = new ArrayList<RelationPredicate>();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(p instanceof RelationPredicate)
				rels.add((RelationPredicate)p);
		}
		return rels;
	}

	/**
	 * Returns all BuiltInPredicates in the body.
	 * @return
	 */
	protected ArrayList<BuiltInPredicate> getNonRelations(){
		ArrayList<BuiltInPredicate> rels = new ArrayList<BuiltInPredicate>();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(p instanceof BuiltInPredicate)
				rels.add((BuiltInPredicate)p);
		}
		return rels;
	}

	/**
	 * Returns all equality relations. (BuiltInPredicate that represents equality)
	 * @return
	 */
	protected ArrayList<Predicate> getEqualityRelations(){
		ArrayList<Predicate> rels = new ArrayList<Predicate>();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(p.getName().equals(MediatorConstants.EQUALS))
				rels.add(p);
		}
		return rels;
	}

	/**
	 * Returns all non-equality relations. (BuiltInPredicate that are NOT equality)
	 * @return
	 */
	public ArrayList<Predicate> getNonEqualityRelations(){
		ArrayList<Predicate> rels = new ArrayList<Predicate>();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if((p instanceof BuiltInPredicate) && !p.getName().equals(MediatorConstants.EQUALS))
				rels.add(p);
		}
		return rels;
	}

	/**
	 * Return the names of duplicate predicates.
	 * <br> Predicates that appear more than once in the body.
	 * @return
	 */
	public HashSet<String> getDuplicatePredicates(){
		HashSet<String> predNames = new HashSet<String>();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(containsPredicate(p, i+1))
				predNames.add(p.getName());
		}
		return predNames;
	}
	
	//return true if the conjunctiveForm contains pred with name p.name
	/**
	 * Returns true if the rule contains a predicate with the name pred.name.
	 * @param pred
	 * 			comparison predicate
	 * @param index
	 * 		index from where we start the search in the body
	 * @return
	 * 		true if the rule contains a predicate with the name pred.name
	 * 		
	 */
	private boolean containsPredicate(Predicate pred, int index){
		//System.out.println("Find term " + t + " in " + terms + " starting at " + index);
		for(int i=index; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(pred.getName().equals(p.getName()))
				return true;
		}
		return false;
	}

	/**
	 * Returns all predicates with a given name.
	 * @param name
	 * @return
	 * 		all predicates with name equal to "name".
	 */
	protected ArrayList<Predicate> getPredicates(String name){
		ArrayList<Predicate> preds = new ArrayList<Predicate>();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(name.equals(p.getName()))
				preds.add(p);
		}
		return preds;
	}
	
	/**
	 * Checks validity of this Rule.
	 * @return
	 * 		true if predicate is valid, false otherwise.
	 * <br> make sure that all variables in the head are present in the body
	 * <br> make sure that all variables in a built-in predicate (x=3) appear in a body relation or the head
	 * @throws MediatorException
	 */
	public boolean isValid() throws MediatorException{
		ArrayList<String> headVars = consequent.get(0).getVars();
		ArrayList<String> bodyVars = new ArrayList<String>();
		ArrayList<String> bodyRelationVars = new ArrayList<String>();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			bodyVars.addAll(p.getVars());
			if(p instanceof RelationPredicate)
				bodyRelationVars.addAll(p.getVars());
		}
		//make sure that all variables in the head are present in the body
		for( int j=0; j<headVars.size(); j++){
			String headVar = headVars.get(j);
			//variable used in user access rule
			if(isUACVar(headVar)) continue;
			if(isNullVar(headVar)) continue;
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
	
	/**
	 * Generates unique variable names for all vars in this rule. 
	 * @param index
	 */
	public void setUniqueVarNames(int index){
		consequent.get(0).setUniqueVarNames(index);
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			p.setUniqueVarNames(index);
		}
		index++;
		//System.out.println("With unique var names=" + this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String s = "";
		s+= consequent.get(0) + "<-";
		for(int i=0; i<antecedent.size(); i++){
			if(i>0) s += " ^ \n\t";
			s += antecedent.get(i).toString();
			//s+= conjunctiveFormula.get(i).getClass().getName();
		}
		return s;
	}

}
