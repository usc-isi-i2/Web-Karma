// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.graph;

import java.util.ArrayList;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.ConstTerm;
import edu.isi.mediator.rule.FunctionTerm;
import edu.isi.mediator.gav.util.MediatorConstants;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.Term;
import edu.isi.mediator.gav.source.SourceAttribute;

/*
if we have X<Y in a query and X and Y come from different relations =>
cross product + a selection with X<Y
 */
public class SelectNode extends BaseNode
{
	
	private Predicate select;
	public BaseNode child;

	public SelectNode(Predicate select, BaseNode child){
		this.select=select;
		this.child=child;
	}
	
	public ArrayList<BaseNode> getSubNodes() {
		ArrayList<BaseNode> subnodes = new ArrayList();
		if (child != null)
			subnodes.add(child);
		return subnodes;
	}	
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.graph.BaseNode#setSQL()
	 * Sets the select statement for this node. 
	 */
	public void setSQL() throws MediatorException{
		
		child.setSQL();
		
		sqlFrom.addAll(child.sqlFrom);
		sqlSelect.addAll(child.sqlSelect);
		sqlWhere.addAll(child.sqlWhere);

		String theSelect;
		if(select.getName().equals(MediatorConstants.IN))
			theSelect=generateINSelect();
		else if(select.getName().equals(MediatorConstants.NOT_IN))
			theSelect=generateNotINSelect();
		else
			theSelect=generateSelect();

		sqlWhere.add(theSelect);
	}

	/**
	 * Generates the select statement for this node.
	 * @return
	 * 		the select statement for this node.
	 * @throws MediatorException
	 * For Example:
	 * 		x=5; y like 'foo'
	 */
	public String generateSelect() throws MediatorException{
		
		//generate the select statement
		ArrayList<Term> terms = select.getTerms();
		Term t1 = terms.get(0);
		Term t2 = terms.get(1);
		
		Boolean termIsNumber1 = termIsNumber(t1);
		Boolean termIsNumber2 = termIsNumber(t2);

		String val1 = getValueForTerm(t1, termIsNumber2);
		String val2 = getValueForTerm(t2, termIsNumber1);
		
		String theSelect = val1 + " " + select.getName() + " " + val2;

		/* not needed, NULL w/EQUALS is handled elsewhere
		if(val1.equals(MediatorConstants.NULL_VALUE) || val2.equals(MediatorConstants.NULL_VALUE)) 
			theSelect = generateNullSelect(val1, val2, select.getName());
		*/
		return theSelect;
	}

	/**
	 * Generates the select statement for IN operator.
	 * @return
	 * 		the select statement for this node.
	 * @throws MediatorException
	 * For Example:
	 * 	A node of the form (y in ['a','b',c']) is represented as (y='a' or y='b' or y='c')
	 */
	public String generateINSelect() throws MediatorException{
		
		//generate the select statement
		ArrayList<Term> terms = select.getTerms();
		Term t1 = terms.get(0);
		
		Boolean termIsNumber1 = termIsNumber(t1);

		String theSelect = "(";
			
		String varName = getValueForTerm(t1, termIsNumber1);
		for(int i=1; i<terms.size(); i++){
			if(i>1) theSelect += " or ";
			String val = getValueForTerm(terms.get(i), termIsNumber1);
			theSelect += varName + " = " + val;
		}
		theSelect += ")";
		return theSelect;
	}

	/**
	 * Generates the select statement for NOT IN operator.
	 * @return
	 * 		the select statement for this node.
	 * @throws MediatorException
	 * For Example:
	 * 	A node of the form (y not in ['a','b',c']) is represented as (y!='a' and y!='b' and y!='c')
	 */
	public String generateNotINSelect() throws MediatorException{
		
		//generate the select statement
		ArrayList<Term> terms = select.getTerms();
		Term t1 = terms.get(0);
		
		Boolean termIsNumber1 = termIsNumber(t1);

		String theSelect = "(";
			
		String varName = getValueForTerm(t1, termIsNumber1);
		for(int i=1; i<terms.size(); i++){
			if(i>1) theSelect += " and ";
			String val = getValueForTerm(terms.get(i), termIsNumber1);
			theSelect += varName + " != " + val;
		}
		theSelect += ")";
		return theSelect;
	}

	//We never really have a Select Node with null, unless it is
	//a IS_NULL or ISNOT_NULL select; this method never used.
	/**
	 * Generates the select statement for NULL.
	 * @return
	 * 		the select statement for this node.
	 * @throws MediatorException
	 * For Example:
	 * 	a is NULL or a is not NULL
	 */
	/*
	public String generateNullSelect(String val1, String val2, String op) throws MediatorException{
		
		String leftVal=val1, rightVal=val2, operator=op;
		if(val1.equals(MediatorConstants.NULL_VALUE)){
			leftVal=val2;
			rightVal=val1;
		}
		else if(val2.equals(MediatorConstants.NULL_VALUE)){
			leftVal=val1;
			rightVal=val2;
		}
		if(op.equals(MediatorConstants.EQUALS)){
			operator = MediatorConstants.IS_NULL;
		}
		else if(op.equals(MediatorConstants.NOT_EQUAL1) || op.equals(MediatorConstants.NOT_EQUAL2)){
			operator = MediatorConstants.ISNOT_NULL;
		}
		else{
			throw new MediatorException("NULL can appear only in equality/non-equality " + select);
		}

		String theSelect = leftVal + " " + operator + " " + rightVal;

		return theSelect;
	}
*/
	
	/**
	 * @param t
	 * 			the Term
	 * @param termIsNumber
	 * 			true if this term is a number, false if it is a string and "null" if we don't know
	 * @return
	 * 			the SQL value for this term
	 * @throws MediatorException
	 */
	private String getValueForTerm(Term t, Boolean termIsNumber) throws MediatorException{
		String val;
		if(t instanceof ConstTerm){
			if(termIsNumber==null){
				//the other term is also a constant so return it as is
				//if it has ", return it as String
				//when we go to syntax without " remove this ... just return getVal()
				val = t.getSqlValNoType();
			}
			else
				val = t.getSqlVal(termIsNumber);
		}
		else if(t instanceof FunctionTerm){
			val = getActualName(t.getFunction());			
		}
		else{
			//variable
			val = getActualName(t.getVar());
		}
		return val;
	}
	
	/**
	 * @param t
	 * 			the Term
	 * @return
	 * 			true if this term is a number, false if it is a string and "null" if we don't know (for a ConstTerm we don't know)
	 */
	private Boolean termIsNumber(Term t){
		boolean termIsNumber = true; 
		
		if(t instanceof FunctionTerm){
			termIsNumber = ((FunctionTerm)t).outputIsNumber();
		}else if(t instanceof ConstTerm){
			//I don't know the type
			return null;
		}else{
			//it's a VarTerm
			//it is either a simple var OR a var that is associated with a function
			//if it is associated with a function, this is the outVar of that function
			SourceAttribute sourceAttr = getSourceAttribute(t.getVar());
			if(sourceAttr==null){
				//I am probably dealing with a var that is defined only in the head of the rule
				//so it doesn't have a corresponding source attribute => we consider all those strings
				termIsNumber=false;
			}
			else
				termIsNumber = sourceAttr.isNumber();
		}
		return termIsNumber;
	}
	
    public String getString(){
    	String s = "";
    	s += "------------ Select " + this+ "---------\n";
    	s += select + "\n";
    	s += "child=" + child.getString();

    	s += "-------------------------------------\n";
    	return s;
    }

}
