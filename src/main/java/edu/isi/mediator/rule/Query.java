// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;

import java.util.ArrayList;

import edu.isi.mediator.domain.parser.QueryParser;
import edu.isi.mediator.gav.graph.BaseNode;
import edu.isi.mediator.gav.graph.Graph;
import edu.isi.mediator.gav.graph.UnionNode;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.GAVRule;
import edu.isi.mediator.rule.RelationPredicate;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.gav.source.SourceQuery;
import edu.isi.mediator.gav.sqlquery.SQLQueryDecomposition;


/**
 * Defines a Datalog query.
 * @author mariam
 *
 */
public class Query extends GAVRule{
	
	//for unique var names for concepts in this query
	public static int index = 0;
	
	/**
	 * query expressed in source terms
	 */
	protected ArrayList<SourceQuery> sourceQuery = new ArrayList<SourceQuery>();
	
	//for datalog queries, always true
	//for sql queries, set from the query
	/**
	 * true if it is a "distinct" query and false otherwise
	 */
	//set here to false if DISTINCT not wanted in datalog queries
	boolean distinct = true; 
	/**
	 * Contains the decomposition of the initial query: core query and additional operations
	 * that need to be applied to the core query (functions, sorting, grouping, etc.). 
	 */
	private SQLQueryDecomposition queryDecomposition;

	/**
	 * Constructs an empty query.
	 */
	public Query(){
		super();
	}

	/**
	 * Constructs a query from a string.
	 * @param s
	 * @throws MediatorException
	 */
	public Query(String s) throws MediatorException{
		if(!s.startsWith("QUERIES:"))
			s = "QUERIES:" + s;
		QueryParser sp = new QueryParser();

		Query q = sp.parseOneQuery(s);
		addHead(q.getHead());
		antecedent=q.getBody();
	}
	
	/**
	 * Constructs a copy of query "q"
	 * @param q
	 */
	public Query(Query q){
		consequent.add(q.getHead().clone());
		for(int i=0; i<q.antecedent.size(); i++){
			Predicate p = q.antecedent.get(i);
			antecedent.add(p.clone());
		}
	}

	/**
	 * Constructs a query from a Rule.
	 * @param r
	 */
	public Query(GAVRule r){
		consequent.add(r.getHead());
		antecedent=r.getBody();
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Rule#clone()
	 */
	public Query clone(){
		Query newQ = new Query();
		newQ.addHead(getHead().clone());
		newQ.distinct=distinct;
		newQ.queryDecomposition=queryDecomposition;
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			newQ.antecedent.add(p.clone());
		}
		return newQ;
	}

	/**	returns the source query.
	 * @return
	 */
	public ArrayList<SourceQuery> getSourceQuery(){
		return sourceQuery;
	}
	
	/**
	 * Sets the "distinct" flag.
	 * @param b
	 */
	public void setDistinct(boolean b){
		distinct=b;
	}
	
	/**
	 * Sets the query decomposition.
	 * @param qd
	 */
	public void setQueryDecomposition(SQLQueryDecomposition qd){
		this.queryDecomposition=qd;
	}
	/**
	 * Returns the QueryDecomposition.
	 * @return
	 * 		the QueryDecomposition.
	 */
	public SQLQueryDecomposition getQueryDecomposition(){
		return this.queryDecomposition;
	}
	
	/**
	 * Removes the predicate at position "i"
	 * @param i
	 * 		position of predicate to be removed.
	 */
	public void removePredicate(int i){
		if(i<antecedent.size())
			antecedent.remove(i);
	}
	
	// REWRITE QUERY IN SOURCE TERMS /////////////////////

//	expand this Query that is a query expressed in domain terms
//	for each predicate in the body, find the matching rule in the domain model and expand
//	when I am done expanding, the query in source terms will be in sourceQuery 
	/**
	 * Rewrites the query in source terms.
	 * @param rules
	 * 			rewrites the query based on these domain rules.
	 * @throws MediatorException
	 */
	public void expandQuery(ArrayList<GAVRule> rules) throws MediatorException{
		
		//System.out.println("Expand Query------------------" + this);
		
		ArrayList<SourceQuery> sourceQuerySoFar = new ArrayList<SourceQuery>();
		
		SourceQuery query = new SourceQuery();
		
		//the head is the same
		query.addHead(getHead());
		//so far the query is not expanded; it only has a head
		sourceQuerySoFar.add(query);
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(p instanceof RelationPredicate){
				//System.out.println("Expand =" + p);
				//expand it
				//look in the rules and find the rule(s) that have the same head as this predicate
				ArrayList<SourceQuery> unionForOneRule = unionForOneRule(p, sourceQuerySoFar, rules);
				//I am done with one predicate from the query body
				//move everything from unionFromOneRule into sourceQuery
				//System.out.println("unionForOneRule=" + unionForOneRule);
				sourceQuerySoFar.clear();
				sourceQuerySoFar = unionForOneRule;
			}
			else{
				//leave it as is
				addSourceOrBuiltInPredicate(p, sourceQuerySoFar);
			}
		}
		
		sourceQuery=sourceQuerySoFar;
		
		//System.out.println("Source Query=" + sourceQuery);

		for(int k=0; k<sourceQuery.size(); k++){
			SourceQuery sq = sourceQuery.get(k);
			//look at the constants that I have in unification; I might have to add 
			//additional predicates to the query
			//if I have a constant as the key => "6", c ; I have to add a predicate (c="6")
			//if I have a constant in the value it is handled in p.unify()
			sq.addAdditionalPredicates();
			
			//normalize the query; move all the equalities inside the relation predicates & head
			sq.normalize();
			//System.out.println("Normalized Query=" + sq);
		}
	}

	//append to all queries the body of the rules found for p
	private ArrayList<SourceQuery> unionForOneRule(Predicate p, ArrayList<SourceQuery> sourceQuerySoFar, ArrayList<GAVRule> rules) throws MediatorException{
		
		//System.out.println("Rules=" + rules);
		
		ArrayList<SourceQuery> unionForOneRule = new ArrayList<SourceQuery>();
		boolean foundRule = false;
		for(int j=0; j<rules.size(); j++){
			GAVRule r = rules.get(j);
			if(p.getName().equals(r.getHead().getName())){
				//System.out.println("Same rule ...");
				//it's the same rule
				foundRule=true;
				ArrayList<SourceQuery> sourceQuerySoFarNew = expandWithRule(p,r, sourceQuerySoFar, rules);
				
				//System.out.println("So Far=" + sourceQuerySoFar);
				unionForOneRule.addAll(sourceQuerySoFarNew);
			}
		}
		if(!foundRule){
			throw new MediatorException("Rule:" + p.getName() + " not found in domain model");
		}
		return unionForOneRule;
	}

	//add predicate p tp all queries in sq
	private void addSourceOrBuiltInPredicate(Predicate p, ArrayList<SourceQuery> sourceQuerySoFar) throws MediatorException{
		for(int k=0; k<sourceQuerySoFar.size(); k++){
			//if it's equality predicate add it to the bindings(I need to know about the constant)
			SourceQuery sq = sourceQuerySoFar.get(k);
			//System.out.println("Add Predicate: " + p + " to : " + sq);
			//rename based on bindings
			Predicate pClone = p.clone();
			pClone.unify(sq.getBinding());
			boolean unified = sq.addToBinding(pClone);
			if(unified){
				sq.addPredicate(pClone);
			}
			else{
				//unification failed => abort this query
				sourceQuerySoFar.remove(k);
				k=k-1;
			}
		}
	}
	
//	the expanded query so far is in sourceQuery
//	take all the queries in source query and add the body of this rule; if it unifies
//	returns the queries in sourceQuery, expanded with this rule
	private ArrayList<SourceQuery> expandWithRule(Predicate predInQuery, GAVRule ruleInDomain, ArrayList<SourceQuery> soFar, 
													ArrayList<GAVRule> rules) throws MediatorException{
		
		//System.out.println("Expand with=" + ruleInDomain);
		//System.out.println("Expand with=" + ruleInDomain.getClass());
		
		//make the variable names of this rule to be unique
		//If I expand twice the same rule in a disjunct, they will have the same var names => joins
		//and we don't want that
		//I don't want to change the rule in the domainModel; DM must remain intact for next query
		GAVRule r=ruleInDomain.clone();
		r.setUniqueVarNames(index++);
		
		//System.out.println("Rule="+r);
		
		ArrayList<SourceQuery> sourceQuerySoFar = new ArrayList<SourceQuery>();
		
		//take each query in sourceQuery and expand it with this rule
		for(int i=0; i<soFar.size(); i++){
			
			//I'm making a copy because I want to leave the queries in sourceQuery unchanged for now
			//I might have to add the body of another rule to that same query
			SourceQuery sq = soFar.get(i).clone();
			
			//unify the 2 predicates keeping track of the bindings in sq.binding
			boolean unified = sq.unify(predInQuery, r.getHead());
			if(!unified){
				//abort this sourceQuery; unification failed
				continue;
			}
			//System.out.println("Source Q after unify =" + sq);
			
			//enforce projections if I have a UAC Rule
			//set vars that are visible after applying this rule
			r.setAllowedProjections(sq.getHead(), sq.getBinding());
			//System.out.println("Source Q after enforceP =" + sq);
			
			sourceQuerySoFar.add(sq);
		}			
		
		//if this UAC rule is not visible at all => _none_ I have to remove the
		//predicate completely from the Query; I just don't expand with this rule
		if(r.isNoneRule()){
			return sourceQuerySoFar;
		}
		
		//add to the body of the rule
		//System.out.println("Add to=" + sq);
		for(int j=0; j<r.getBody().size(); j++){
			Predicate p = r.getBody().get(j).clone();
			
			if(p.isDomainPredicate() && !r.isUACRule()){
				ArrayList<SourceQuery> unionForOneRule = unionForOneRule(p, sourceQuerySoFar, rules);
				//I am done with one predicate from the query body
				//move everything from unionFromOneRule into sourceQuery
				sourceQuerySoFar.clear();
				sourceQuerySoFar = unionForOneRule;
				//System.out.println("Union=" + sourceQuerySoFar);
			}
			else{
				//System.out.println("Add predicate =" + p + " to " + sourceQuerySoFar);
				addSourceOrBuiltInPredicate(p, sourceQuerySoFar);
			}
			//System.out.println("Done one=" + sourceQuerySoFar);
		}
		return sourceQuerySoFar;
		
	}

	//INITIAL PLAN GENERATOR ////////////////////////
	
	/**
	 * Generates the initial plan.
	 * @param optimize
	 * 		if true, optimize the query before constructing the plan
	 * @return
	 * 		the initial plan
	 * @throws MediatorException
	 */
	public Graph generateInitialPlan(boolean optimize) throws MediatorException{
		
		//System.out.println("Generate Source Plan BEGIN nr queries:" + sourceQuery.size());		
		//System.out.println("DISTINCT:" + distinct + " for query:" + this);		

		UnionNode un = new UnionNode();
		if(sourceQuery.isEmpty()){
			throw new MediatorException("No source query was generated. Check log file for errors!");
		}
		SourceQuery oneQ = sourceQuery.get(0);
		BaseNode parent = oneQ.generateInitialPlanForSourceQuery(optimize);
		//I have to set distinct for all source queries of this domain query
		parent.setDistinct(distinct);
		for(int i=1; i<sourceQuery.size(); i++){
			if(i==1){
				un.addSubNode(parent);
				parent=un;
			}
			oneQ = sourceQuery.get(i);
			BaseNode parentForOneQ = oneQ.generateInitialPlanForSourceQuery(optimize);
			parentForOneQ.setDistinct(distinct);
			un.addSubNode(parentForOneQ);
		}
		//only the parent needs the decomposition; we apply it at this level
		parent.setQueryDecomposition(queryDecomposition);
		Graph g = new Graph(parent);
		return g;
	}
	
	//we only have BP for SourceQuery
	/**
	 * Returns true if this query has the binding patterns satisfied.
	 * <br> Relevant only with {@link edu.isi.mediator.gav.source.SourceQuery}
	 * @return
	 */
	public boolean bindingPatternsSatisfied(){
		return true;
	}

	/////////////////////////////////////
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Rule#toString()
	 */
	public String toString(){
		String s = super.toString();
		if(!sourceQuery.isEmpty()){
			s += "\nSourceQuery:";
			for(int i=0; i<sourceQuery.size(); i++){
				s += sourceQuery.get(i).toString() + "\n";
			}
		}
		//s += "\nQuery Decomposition:\n" + this.queryDecomposition;
		return s;
	}
}