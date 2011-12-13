// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;

import edu.isi.mediator.gav.util.MediatorConstants;

//a Predicate can be a normal relation and built-in predicates like equality, lessThan, like, etc. 
//for a normal relation, the "name" is the name of the relation
//for built-in predicates like equality, lessThan, the "name" will be PredicateType 
/**
 * @author mariam
 * Defines a BuiltInPredicate (equality, lessThan, like, etc.)
 * Example: (X >= 5) (Y like 'foo')
 */
public class BuiltInPredicate extends Predicate{
		
	/**
	 * true if this equality predicate is represented as an Assign operator in the query graph.
	 * false otherwise (if it is represented as a Select operator)
	 */
	private boolean isAssignment=false;
	
	/**
	 * @param name
	 * 		is one of:
	 * 		MediatorConstants.EQUALS
	 *		MediatorConstants.GREATER_THAN
	 *	    MediatorConstants.GREATER_THAN_EQ
	 *	    MediatorConstants.LESS_THAN
	 *	    MediatorConstants.LESS_THAN_EQ
	 *	    MediatorConstants.LIKE
	 *	    MediatorConstants.NOT_EQUAL
	 */
	public BuiltInPredicate(String name){
		this.name=name;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public BuiltInPredicate clone(){
		BuiltInPredicate p = new BuiltInPredicate(name);
		for(int i=0; i<terms.size(); i++){
			Term t = terms.get(i);
			p.addTerm(t.clone());
		}
		p.isAssignment=isAssignment;
		return p;
	}
	

	/**
	 * Set isAssignment member
	 * @param b
	 */
	public void isAssignment(boolean b){
		isAssignment=b;
	}
	/**
	 * @return
	 * 		the value of isAssignment
	 */
	public boolean isAssignment(){
		return isAssignment;
	}
	
	/**
	 * Checks if this predicate is an equality with same constant.
	 * @return
	 * 		true if equality with same constant
	 * 		false otherwise
	 */
	public boolean isEqualityWithSameConstant(){
		//a=a or NULL IS NULL
		if(name.equals(MediatorConstants.EQUALS) || name.equals(MediatorConstants.IS_NULL)){
			Term t1 = terms.get(0);
			Term t2 = terms.get(1);
			if(t1 instanceof ConstTerm && t2 instanceof ConstTerm){
				if(((ConstTerm)t1).equalsValue(((ConstTerm)t2)))
					return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		String s = "";
		s += "(" + terms.get(0).toString() + " " + name + " ";
		for(int i=1; i<terms.size(); i++){
			s+= terms.get(i).toString();
		}
		s += ")";
		if(isAssignment)
			s += " (assign) ";
		else
			s += " (select) ";
		return s;
	}

}