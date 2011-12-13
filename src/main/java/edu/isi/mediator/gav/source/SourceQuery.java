// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.source;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.*;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.domain.*;
import edu.isi.mediator.gav.graph.AssignNode;
import edu.isi.mediator.gav.graph.BaseNode;
import edu.isi.mediator.gav.graph.DataAccessNode;
import edu.isi.mediator.gav.graph.JoinNode;
import edu.isi.mediator.gav.graph.ProjectNode;
import edu.isi.mediator.gav.graph.SelectNode;


public class SourceQuery extends Query{
	
	//THE QUERY expressed in source terms
	
	//I have one binding list per sourceQuery
	//each sourceQuery is a node in a Union; I don't want the bindings/constants 
	//from one union node to interfere with the ones from another
	private Binding binding = new Binding();
	
	public SourceQuery(){
		super();
	}
	
	protected SourceQuery(GAVRule r){
		super(r);
	}

	public SourceQuery clone(){
		SourceQuery newQ = new SourceQuery();
		newQ.addHead(getHead().clone());
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			newQ.antecedent.add(p.clone());
		}
		newQ.binding = binding.clone();
		return newQ;
	}

	//if it's equality predicate add it to the bindings(I need to know about the constant)
	public boolean addToBinding(Predicate p) throws MediatorException{
		return binding.addPredicate(p);
	}
	
	public Binding getBinding(){
		return binding;
	}
		
	//when I see a constant add an entry var,const
	//if that var appears again, make sure that it isn't assigned to another const
	//if yes, unification fails
	public boolean unify(Predicate queryP, Predicate ruleP) throws MediatorException{
		//System.out.println("Unify:" + queryP + " and " + ruleP);
		//make sure that the number of terms is the same
		if(queryP.getTerms().size()!=ruleP.getTerms().size()){
			throw(new MediatorException("the number of terms is not the same in:" + queryP + " AND " + ruleP));
		}
		
		for(int i=0; i<queryP.getTerms().size(); i++){
			Term qT = queryP.getTerms().get(i);
			Term rT = ruleP.getTerms().get(i);
			if((qT instanceof VarTerm) && (rT instanceof VarTerm)){
				//if both are vars
				boolean unified = binding.unify(rT.getVar(), qT.getVar());
				if(!unified){
					return false;
				}
				//System.out.println("Binding1=" + binding);
			}
			else if((qT instanceof ConstTerm) && (rT instanceof ConstTerm)){
				//both are constants; make sure that it is the same constant
				if(!((ConstTerm)qT).equalsValue(((ConstTerm)rT))){
					//unification failed
					System.err.println("Unification failed:" + qT + " and " + rT + " do not unify!");
					//System.out.println("FAIL1");
					return false;
				}
				else{
					//they are the same constant, so everything is fine
				}
			}
			else if((qT instanceof ConstTerm) && (rT instanceof VarTerm)){
				//I have a constant in the query, but not in the head
				//when I see the var in the rule body it will be replaced with the constant
				//at that time if I have x="6", and I have x bound to a constat in "unification"
				//it has to be the same constant
				boolean unified = binding.unify(rT.getVar(), qT.getVal());
				//System.out.println("Binding2=" + binding);
				//System.out.println("const=" + allConstants);
				if(!unified){
					return false;
				}
				//else Go ON!
			}
			else if((qT instanceof VarTerm) && (rT instanceof ConstTerm)){
				//I have a constant in the rule head, but not in the query
				boolean unified = binding.unify(qT.getVar(), rT.getVal());
				binding.putConst(qT.getVar(), rT.getVal());
				if(!unified){					
					return false;
				}
				//else Go ON!
			}
		}
		//System.out.println("After Unify=" + this);
		return true;
	}
	
	//look at the constants that I have in unification; I might have to add 
	//additional predicates to the query
	//if I have a constant as the key => "6", c ; I have to add a predicate (c="6")
	//this is the equivalent of an assign in the graph
	public void addAdditionalPredicates(){
		//System.out.println("Add additional Predicates ..........");
		Set<String> keys = binding.keySetConst();
		Iterator it = keys.iterator();
		while(it.hasNext()){
			String key = (String)it.next();
			String val = binding.getConst(key);
			BuiltInPredicate newP = new BuiltInPredicate(MediatorConstants.EQUALS);
			newP.isAssignment(true);
			newP.addTerm(key);
			newP.addTerm(val);
			//System.out.println("Add assignment: " + newP);
			addPredicate(newP);
		}
	}	

	public void normalize()  throws MediatorException{
		//System.out.println("Before Normalize " + this);
		//first normalize functions 
		//if the out var of the function is a constant (look at q(patient) :- getPatients3(patient) in func_q.txt)
		//f(a,"8");I don't want to normalize the equality predicate
		//f(a)="8"
		normalizeFunctions();
		//System.out.println("After Normalize Functions" + this);

		//push the constants into the predicates
		normalizeEquality();

		//System.out.println("After Normalize Equality" + this);

	}
	
	//remove equality predicates from the body and push to the source
	//r(a,b,c)^b=5 => r(a,5,c)
	//r(a,b,c)^a=b => r(b,b,c)
	//don't normalize if one of the vars is a an outVar of a function
	public void normalizeEquality(){
		//before I normalize I want to make sure that the var involved in equality
		//isn't involved in a non-equality predicate
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(p.getName().equals(MediatorConstants.EQUALS)){
				//if the equality is with a function leave it alone
				//sf(a)="8"
				ArrayList<Term> terms = p.getTerms();
				Term t1 = terms.get(0);
				Term t2 = terms.get(1);
				if(t1 instanceof FunctionTerm || t2 instanceof FunctionTerm)
					continue;
				//if the equality is an assignment leave it alone; I might has a select on that same var
				// and if I leave it only in the head can't do the select because that var doesn't 
				// exist in the body
				if(((BuiltInPredicate)p).isAssignment())
					continue;
				
				//if both terms are constants just leave them in the query
				if(t1 instanceof ConstTerm && t2 instanceof ConstTerm)
					continue;
				
				//remove it; I will use the info that I have in Bindings to normalize
				antecedent.remove(i);
				i--;
			}
		}
		
		//System.out.println("Binding List:" + binding);
		
		//I want to push it into all predicates, not only relations; otherwise I may lose information
		//x<=y and y=5 => x<=5
		ArrayList<Predicate> rels = getBody();
		//ArrayList<RelationPredicate> rels = getRelations();
		for(int i=0; i<rels.size(); i++){
			Predicate rel = rels.get(i);
			rel.unify(binding);
		}
		//also set the constants in the head; here I want to keep the variable names so I can refer to them in the query
		getHead().unify(binding);
	}
	
	//getPatients(patient, date, newdate) :-  patients(patient, sdate) ^ currentdate(date) ^ subdays(date, "60", newdate) ^ (newdate <= sdate)
	//where currentdate and subdays are functions
	//replace the outAttributes of the functions with the actual function =>
	//getPatients(patient, currentdate(), subdays(currentdate(), "60")) :- patients(patient, sdate) ^ (subdays(currentdate(), "60") <= sdate))
	//functions have only one out attribute; and it is the free one in the source schema
	public void normalizeFunctions() throws MediatorException{
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			if(p instanceof FunctionPredicate){
				antecedent.remove(i);
				i--;
				//find in the query all vars that are equal to the OUT var of this function
				//and replace those vars with the function
				normalizeWithFunction((FunctionPredicate)p);
			}
		}
	}
	
	
	//find in the query all vars that are equal to the OUT var of this function
	//and replace those vars with the function
	public void normalizeWithFunction(FunctionPredicate func) throws MediatorException{
		//System.out.println("Normalize " + this + " with " + func);
		//get out var
		String outVar = func.getOutVarForFunction();
		//I will replace the outVars of this func with the func, so I
		//can remove the outVar from the func
		func.removeOutVarForFunction();
		for(int i=0; i<antecedent.size(); i++){
			Predicate p = antecedent.get(i);
			//if p contains outVar, replace it with func
			p.normalizeWithFunction(func, outVar);
		}
		getHead().normalizeWithFunction(func, outVar);
	}
	
	//INITIAL PLAN GENERATOR 
	
	public BaseNode generateInitialPlanForSourceQuery(boolean optimize) throws MediatorException{
		
		//System.out.println("Generate Initial plan for:" + this);
		//optimize this query based on containment
		//after optimization this SQ will be optimal & satisfies bindings
		if(optimize){
			System.out.println("Before Optimization:" + this);
			Optimization.containmentOptimization_Incremental(this);
			System.out.println("OPTIMIZED QUERY:" + this);
		}
		//System.exit(1);
		
		//this will give the relation in an order that satisfies the binding patterns
		ArrayList<Predicate> sourceRel = satisfyBindingPatterns();
		
		BaseNode parentNode = null;
		for(int i=0; i<sourceRel.size(); i++){
			RelationPredicate rel = (RelationPredicate)sourceRel.get(i);
			parentNode = buildJoin(rel, parentNode);
		}
		//now build selects; everything that is not a relation will be constructed as a select or assign
		ArrayList<BuiltInPredicate> nonRels = getNonRelations();
		//first add the assignments because i might have a select on a var that is only in the Assign
		ArrayList<BuiltInPredicate> selects = new ArrayList<BuiltInPredicate>();
		ArrayList<BuiltInPredicate> assigns = new ArrayList<BuiltInPredicate>();
		for(int i=0; i<nonRels.size(); i++){
			BuiltInPredicate rel = nonRels.get(i);
			if(rel.isAssignment())
				assigns.add(rel);
			else selects.add(rel);
		}
		for(int i=0; i<assigns.size(); i++){
			BuiltInPredicate rel = assigns.get(i);
			parentNode = buildAssign(rel, parentNode);
		}
		for(int i=0; i<selects.size(); i++){
			BuiltInPredicate rel = selects.get(i);
			//remove selects of the for 'a'='a', they are redundant
			if(rel.isEqualityWithSameConstant())
				continue;
			parentNode = buildSelect(rel, parentNode);
		}
		//at the end have one project for the head of the query
		parentNode = buildProject(getHead(), parentNode);
		
		return parentNode;
	}	
	
	/**
	 * Adds a join node to the given graph.
	 * @param rel
	 * 		relation to be joined with the given graph
	 * @param joinSoFar
	 * 		graph that join is added to
	 * @return
	 * 		a new graph with a join node added to joinSoFar
	 */
	private BaseNode buildJoin(RelationPredicate source, BaseNode joinSoFar){
		/*
		System.out.println("Build join with " + source);
		if(joinSoFar!=null)
			System.out.println("JoinSoFar= " + joinSoFar.getString());
			*/
		DataAccessNode dn = new DataAccessNode(source);
		
		if(source.needsBinding()){
			//this is a source that needs bindings
			//it will get these bindings from the graph that was constructed so far
			//because the predicates were ordered before constructing the graph
			//dn will always get the bindings from joinSoFar (or they were already set as constants)
			dn.setChild(joinSoFar);
		}
		
		if(joinSoFar==null){
			//it's the first source; build a data node
			return dn;
		}
		else{
			//find Common Attributes
			ArrayList<String> joinAttrs = new ArrayList<String>();
			dn.findCommonAttrs(joinSoFar, joinAttrs);
			JoinNode jn = new JoinNode(joinAttrs, dn, joinSoFar);
			return jn;
		}
	}
	
	private BaseNode buildSelect(Predicate select, BaseNode graphSoFar){
		
		SelectNode sn = new SelectNode(select, graphSoFar);
		return sn;
	}

	private BaseNode buildAssign(Predicate assign, BaseNode graphSoFar){
		
		AssignNode sn = new AssignNode(assign, graphSoFar);
		return sn;
	}

	private BaseNode buildProject(Predicate project, BaseNode graphSoFar){
		
		ProjectNode pn = new ProjectNode(project, graphSoFar);
		return pn;
	}

	//BINDING PATTERNS
	//return the source predicates in an order that satisfies the binding patterns
	//initially satisfiedPredicates contains all sources that don't need BP
	//satisfiedVars contains all vars that are not bound => from sources ar from equality
	//bindingPredicates contains all sources that need bindings
	//at every iteration find at least one source in bindingPredicates that can be added to satisfiedPredicates
	//if I can't add at least one source it means that this query is not satisfiable given the binding restrictions of data sources in the domain model
	ArrayList<Predicate> satisfyBindingPatterns() throws MediatorException{
		
		ArrayList<Predicate> satisfiedPredicates = getRelationsWithNoBP();
		ArrayList<Predicate> equalityPredicates = getEqualityRelations();
		ArrayList<RelationPredicate> bindingPredicates = getRelationsWithBP();
		
		HashSet<String> satisfiedVars = getAllVars(satisfiedPredicates);
		satisfiedVars.addAll(getAllVars(equalityPredicates));
		
		//find relations whose vars are satisfied by vars present in satisfiedVars
		while(!bindingPredicates.isEmpty()){
			//I have to add at least one predicate from the list of unsatisfied preds
			//If I can't add any predicate the bindings in this query are not satisfied
			boolean unsatisfiedBindings = true;
			for(int i=0; i<bindingPredicates.size(); i++){
				RelationPredicate rel = bindingPredicates.get(i);
				//System.out.println("Check bindings for predicate:" + rel);
				ArrayList<String> needBindingVars = rel.getNeedBindingVars();
				
				//if all vars are in satisfiedVars => I can add this relation to satisfiedPredicates
				for(int j=0; j<needBindingVars.size(); j++){
					String var = needBindingVars.get(j);
					//System.out.println("Check binding variable:" + var + " in "  + satisfiedVars);
					if(satisfiedVars.contains(var)){
						//this var is satisfied => go to the next one unless it's the last var
						if(j==needBindingVars.size()-1){
							//I got to my last variable => all vars are satisfied
							//I can move this predicate to satisfiedPredicates
							satisfiedPredicates.add(rel);
							bindingPredicates.remove(i);
							i--;
							unsatisfiedBindings = false;
							//add the new vars to satisfiedVars
							satisfiedVars.addAll(rel.getVars());
						}
					}
					else{
						//BP not satisfied => try next relation
						break;
					}
				}
			}
			if(unsatisfiedBindings){
				throw(new MediatorException("This query is not satisfiable given the binding restrictions of data sources in the domain model:" + this));
			}
		}
		//this is the join order of predicates such that binding patterns are satisfied 
		return satisfiedPredicates;
	}
	
	public boolean bindingPatternsSatisfied(){
		//System.out.println("BP satisfied in SourceQuery!!!");
		try{
			satisfyBindingPatterns();
			return true;
		}
		catch(MediatorException e){
			return false;
		}
	}
	
	//return all relations that do not require binding patterns
	ArrayList<Predicate> getRelationsWithNoBP(){
		ArrayList<Predicate> noBindRelations = new ArrayList<Predicate>();
		
		ArrayList<RelationPredicate> allRels = getRelations();
		for(int i=0; i<allRels.size(); i++){
			RelationPredicate rel = allRels.get(i);
			if(!rel.needsBinding())
				noBindRelations.add(rel);
		}
		return noBindRelations;
	}

	//return all relations that require binding patterns
	ArrayList<RelationPredicate> getRelationsWithBP(){
		ArrayList<RelationPredicate> bindRelations = new ArrayList<RelationPredicate>();
		
		ArrayList<RelationPredicate> allRels = getRelations();
		for(int i=0; i<allRels.size(); i++){
			RelationPredicate rel = allRels.get(i);
			if(rel.needsBinding()){
				//System.out.println("Needs Binding!!!" + rel);
				bindRelations.add(rel);
			}
		}
		return bindRelations;
	}

	//get all the variables in these relations
	HashSet<String> getAllVars(ArrayList<Predicate> rels){
		HashSet<String> vars = new HashSet<String>();
		for(int i=0; i<rels.size(); i++){
			Predicate rel = rels.get(i);
			vars.addAll(rel.getVars());
		}
		return vars;
	}
	
	
	public String toString(){
		String s = super.toString() + "\n";
		//s+= binding;
		return s;
	}
	

}