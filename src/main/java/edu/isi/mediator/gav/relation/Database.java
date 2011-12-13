// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.relation;

import java.util.ArrayList;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.RelationPredicate;
import edu.isi.mediator.rule.Term;

public class Database 
{ 
  private ArrayList<Relation> relations = new ArrayList<Relation>();

 
  public void addRelation(Relation rel)
  {
    relations.add(rel);
  }

  public void addRelation(Predicate p){
	  
	  //System.out.println("Add to DB relation: " + p);
	  
	  Relation r = getRelation(p.getName());
	  if(r==null){
		  //relation doesn't exist 
		  //System.out.println("Relation doesn't exist in ... " + relations);
		  //r = new Relation(p.getName(), p.getTerms().size());
		  r = new Relation(p.getName());
		  relations.add(r);
	  }
	  ArrayList<Term> terms = p.getTerms();
	  Tuple tuple = new Tuple();
	  for(int i=0; i<terms.size(); i++){
		  Term t = terms.get(i);
		  tuple.addValue(t.getTermValue());
	  }
	  r.addTuple(tuple);
  }
  
  public Relation getRelation(String name){
      for (int i=0; i<relations.size(); i++) {
    	  Relation r = relations.get(i);
    	  if(r.getName().equals(name.toLowerCase()))
    		  return r;
      }
      return null;
  }
  
  public Relation getRelation(RelationPredicate p)  throws MediatorException{
	  //System.out.println("Get Relation:" + p.getName());
	  
	  ArrayList<Predicate> expandedPredicate = p.expandInConjunctiveFormula();
	  
	  //System.out.println("Expanded predicate=" + expandedPredicate);
	  
	  Predicate p0 = expandedPredicate.get(0);
	  //get relation
	  Relation r = getRelation(p0.getName());
	  if(r!=null){
		  r = r.clone();
		  ArrayList<String> attrs = p0.getVarsAndConst();

		  //System.out.println("Set attribs " + attrs + " to " + r);
		  
		  r.setAttrList(new AttrList(attrs));
		  
		  //execute the predicates over the relation
		  for(int i=1; i<expandedPredicate.size(); i++){
			  //these are either x="10" or x2=x4
			  Predicate p1 = expandedPredicate.get(i);
			  Term t1 = p1.getTerms().get(0);
			  Term t2 = p1.getTerms().get(1);
			  
			  r=r.select(t1.getVar(),t2.getTermValue());
		  }
		  //before returning use the initial attributes; we need the initial names to find joins between predicates
		  r.setAttrListAllowDuplicates(p.getVarsAndConst());
	  }	  
	  return r;
  }
  
  public String toString()
  {
	  String s = "Database:\n";
      for (int i=0; i<relations.size(); i++) {
        s += relations.get(i).toString();
      }
      return s;
  }

}
