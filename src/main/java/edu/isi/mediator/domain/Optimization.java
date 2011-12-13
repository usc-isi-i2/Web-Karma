// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.domain;

import java.util.ArrayList;
import java.util.HashSet;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.relation.Database;
import edu.isi.mediator.gav.relation.Relation;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.Query;
import edu.isi.mediator.rule.RelationPredicate;

/**
 * Class that contains optimization functions.
 * @author mariam
 *
 */
public class Optimization {

	/**
	 * Containment Optimization used with UAC Rules.
	 * <br> First tries to remove redundant predicates before optimization.
	 * <br>If this approach fails, try the incremental optimization.
	 * @param q
	 * 		the query to be optimized.
	 * @throws MediatorException
	 */
	static public void containmentOptimization(Query q) throws MediatorException{

		//used with UAC Rules
		boolean optimized = containmentOptimization_removeALLDuplicates(q);

		if(!optimized){
			//try to remove one by one
			//System.out.println("Incremental Optimization!");
			containmentOptimization_Incremental(q);
		}
	}
		
    //OPTIMIZATION - based on containment
	//from the query q (Q1) keep removing one relation/predicate at a time and see if the
	//initial query is contained in the remaining one (Q2), and vice versa
	//determine if they are equivalent
	//q will contain the optimized query
	/**
	 * Containment Optimization.
	 * <br> Removes predicates incrementally, and checks optimization at every step.
	 * @param q
	 * 		the query to be optimized.
	 * @throws MediatorException
	 */
	static public void containmentOptimization_Incremental(Query q) throws MediatorException{
		
		System.out.println("Incremental Optimization ...");
		
		//if we have non-equality predicates we can't optimize using this method (a<5)
		ArrayList<Predicate> nonEquality = q.getNonEqualityRelations();
		if(!nonEquality.isEmpty()){
			return;
		}
		///////////////////////////
		
		//try to remove only duplicate predicates
		//if we remove nonduplicates equivalence will fail; we need same predicates in both queries to construct Q2(D)
		HashSet<String> duplicatePredicates = q.getDuplicatePredicates();
		//System.out.println("duplicatePredicates=" + duplicatePredicates);							
		
		ArrayList<Predicate> preds = q.getBody();
		for(int i=0; i<preds.size(); i++){
			Predicate p = preds.get(i);
			if(duplicatePredicates.contains(p.getName())){
				//construct a Q that doesn't have this relation
				Query newQ = q.clone();
				newQ.removePredicate(i);
				if(newQ.bindingPatternsSatisfied()){
					//System.out.println("Bindings Satisfied!!!");
					//System.out.println("Check equivalence!!!");
					boolean areEquivalent = isEquivalent(q,newQ);
					if(areEquivalent){
						System.out.println("OPTIMIZED!!!");
						//queries are equivalent => I can replace the initial query with the smaller one
						//remove the predicate from this query
						System.out.println("Remove Predicate " + p.getName());
						q.removePredicate(i);
						//and optimize again;
						//this way I will always have in this the optimum query that satisfies bindings
						containmentOptimization_Incremental(q);
						
					}
				}
			}
		}
	}
	
	//mainly used with UAC Rules; I want to try to remove redundant predicates all at once
	/**
	 * Containment Optimization used with UAC Rules.
	 * <br> Removes redundant predicates before optimization.
	 * @param q
	 * 		the query to be optimized.
	 * @throws MediatorException
	 */
	static public boolean containmentOptimization_removeALLDuplicates(Query q) throws MediatorException{
		
		//if we have non-equality predicates we can't optimize using this method (a<5)
		ArrayList<Predicate> nonEquality = q.getNonEqualityRelations();
		if(!nonEquality.isEmpty()){
			return false;
		}
		///////////////////////////
		
		//try to remove only duplicate predicates
		//if we remove nonduplicates equivalence will fail; we need same predicates in both queries to construct Q2(D)
		HashSet<String> duplicatePredicates = q.getDuplicatePredicates();
		//System.out.println("duplicatePredicates=" + duplicatePredicates);							
		
		//remove all duplicates at once
		Query newQ = q.clone();
		ArrayList<Predicate> preds = q.getBody();
		ArrayList<Integer> removedIds = new ArrayList<Integer>();
		boolean nothingRemoved = true;
		for(int i=0, removeId=0; i<preds.size(); i++,removeId++){
			Predicate p = preds.get(i);
			//System.out.println("Remove predicate=" + p.getClass().getName() + " " + p);			
			//leave only UAc Predicates; look at UAR.annotateForOptimization() to see how we define the UAc Predicates
			if(duplicatePredicates.contains(p.getName()) && !p.isUACPredicate()){
				//construct a Q that doesn't have this relation
				//System.out.println("Remove it");							
				newQ.removePredicate(removeId--);
				removedIds.add(new Integer(i));
				nothingRemoved=false;
			}
		}
		//System.out.println("Q after removing duplicates=" + newQ);							
		
		if(nothingRemoved){
			//try the incremental remove; we don't have any UAP so we can't use this method
			return false;
		}

		if(!newQ.bindingPatternsSatisfied()){
			return false;
		}
		
		boolean areEquivalent = isEquivalent(q,newQ);
		if(areEquivalent){
			//remove the predicates from q
			for(int i=0; i<removedIds.size(); i++){
				q.removePredicate(removedIds.get(i).intValue()-i);
			}
			//System.out.println("Optimized Q=" + q);
		}
		return areEquivalent;
	}

	/**
	 * Returns true if q1 is contained in q2
	 * @param q1
	 * @param q2
	 * @return
	 * 		true if q1 is contained in q2, false otherwise.
	 * @throws MediatorException
	 */
	public static boolean isContained(Query q1, Query q2) throws MediatorException{
		
		System.out.println("Is Contained: " + q1 + "\n IN " + q2);
		
		//create a canonical DB that is the frozen body of Q1
		Database db = createFrozenBody(q1);
		
		//System.out.println("Frozen Body of q1:\n " + db);
		
		//compute Q2(db) => execute all the joins from Q2 over the database db
		ArrayList<RelationPredicate> rels = q2.getRelations();
		Relation joinRelation = null;
		for(int i=0; i<rels.size(); i++){
			RelationPredicate p = rels.get(i);
			Relation rel = db.getRelation(p);
			if(rel==null){
				//relation not found so we can't evaluate q2(D)
				return false;
			}
			joinRelation = rel.join(joinRelation);
			if(joinRelation.isEmpty()){
				//all joins will be empty => it is not contained
				//System.out.println("EMPTY relation for Q2(D):\n ");
				return false;
			}
			//System.out.println("Result relation:\n " + joinRelation);
			//System.out.println("Result relation:\n " + joinRelation.getTuples().size());
		}

		//System.out.println("Result relation for Q2(D):\n " + joinRelation.getTuples().size());
		//System.out.println("Result relation for Q2(D):\n " + joinRelation);
		
		if(joinRelation==null){
			//System.out.println("NULL JoinRelation Return false\n ");
			return false;
		}

		//check if joinRelation contains the frozen head of Q1
		//the head of q1 and q2 can have variables and constants
		ArrayList<String> q1HeadValues = q1.getHead().getValues(); 
		ArrayList<String> q2HeadVars = q2.getHead().getVars();
		//in joinRelation, the attributes q2HeadVars should have the values q1HeadVars
		//if I have a constant in the head just drop it when I check if it contains 
		//(if it's a const you just add it to the tuple, so it doesn't need to be in the body)
		if(joinRelation.containsTuple(q2HeadVars, q1HeadValues)){
			//System.out.println("Contains head. Return true:\n ");
			//contains frozen head of q1
			return true;
		}
		
		//System.out.println("Return false\n ");
		return false;
	}
	
	/**
	 * Creates the frozen body of q.
	 * @param q
	 * @return
	 * 		The database that contains the relation equivalent to the frozen body.
	 */
	static private Database createFrozenBody(Query q){
				
		Database db = new Database();
		
		ArrayList<RelationPredicate> allRels = q.getRelations();
		for(int i=0; i<allRels.size(); i++){
			Predicate rel = allRels.get(i);
			db.addRelation(rel);
		}
		return db;
	}

	/**
	 * Returns true if the two queries are equivalent.
	 * @param q1
	 * @param q2
	 * @return
	 * 		true if the two queries are equivalent, false otherwise.
	 * @throws MediatorException
	 */
	static private boolean isEquivalent(Query q1, Query q2) throws MediatorException{ 
		if(isContained(q1,q2)){
			System.out.println("Is Contained!!!");
			//now check if Q2 is contained in Q1
			//we want equivalence
			if(isContained(q2, q1)){
				System.out.println("OPTIMIZED!!!");
				return true;
			}
			else{
				System.out.println("Equivalence Failed!!!");
				return false;
			}
		}
		else{
			System.out.println("Is NOT Contained!!!");
			return false;
		}
	}
	
}
