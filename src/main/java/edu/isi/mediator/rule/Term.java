// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorUtil;

/**
 * Defines a Term.
 * @author mariam
 *
 */
public abstract class Term{
	
	/**
	 * variable name
	 */
	protected String var;

	/**
	 * name for select attributes used in final sql query 
	 * (used only with SQL interface; (equivalent to the "as" name ... select a as x)
	 */
	protected String queryName;

	/**
	 * true if this term can be seen after we apply uac rules
	 * false otherwise; if it's false this term will become ConstTerm with value = "NOT ALLOWED"
	 */
	protected Visible isAllowedAfterUAC = Visible.NOT_SET;
	public enum Visible{TRUE,FALSE,NOT_SET};
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public abstract Term clone();
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();
	/**
	 * Two terms are equal if the var and the val are equal
	 * @param t
	 * @return
	 * 		true if this term is equal to t
	 */
	public abstract boolean equals(Term t);
	
	/**
	 * Returns true if the source attribute associated with this term needs binding.
	 * @param b
	 * @return
	 */
	public abstract boolean needsBinding(boolean b);

	/**
	 * Relevant only with FunctionTerms
	 * @return
	 */
	public FunctionPredicate getFunction(){return null;}
	/**
	 * @param isNumber
	 * 		true if the type of this Term is a number
	 * 		false otherwise
	 * @return
	 * 		value enclosed in '' if isNumber=false
	 * 		value with no quotes if isNumber=true
	 * @throws MediatorException
	 */
	public String getSqlVal(boolean isNumber) throws MediatorException{return null;}
	/**
	 * @return
	 * 		value as is; we don't know the type for this Term
	 * @throws MediatorException
	 */
	public String getSqlValNoType() throws MediatorException{return null;}
	
	////////////for UAC
	/**
	 * Sets isAllowedAfterUAC.
	 * @param b
	 * 		true if this term can be seen after we apply uac rules
	 * 		false otherwise; if it's false this term will become ConstTerm with value = "NOT ALLOWED"
	 */
	public void setIsAllowed(boolean b){
		Visible v;
		if(b==true){
			v=Visible.TRUE;
		}
		else v=Visible.FALSE;
		
		if(isAllowedAfterUAC==Visible.NOT_SET || isAllowedAfterUAC==Visible.FALSE)
			isAllowedAfterUAC=v;
		//else if I already determined that it is allowed just leave it alone
		//it will be allowed even if it is not allowed from other concepts
		//if not allowed from a _none_ concept I will see only vals from the visible concept
		//if from _none_but_joinable_ it will join, so I will see only the joined values, so OK
		//same if I have partial values visible, there will be a join
	}
	
	/**
	 * @return
	 * 		true if isAllowedAfterUAC==Visible.NOT_SET || isAllowedAfterUAC==Visible.TRUE
	 * 		false otherwise
	 */
	public boolean isAllowed(){
		if(isAllowedAfterUAC==Visible.NOT_SET || isAllowedAfterUAC==Visible.TRUE)
			return true;
		else return false;
	}
	/////////////////////////////////////////////
	
	//for VarTerm=var & ConstTerm=val
	/**
	 * @return
	 * 		var for VarTerm and Functionterm
	 * 		val for ConstTerm
	 */
	public String getTermValue(){
		return var;
	}
	
	/**
	 * @return
	 * 		var
	 */
	public String getVar(){
		return var;
	}
	/**
	 * @return
	 * 		constant value for ConstTerm
	 * 		null otherwise
	 */
	public String getVal(){return null;}
	
	/**
	 * @return
	 * 		queryName
	 */
	public String getQueryName(){
		return queryName;
	}
	/**
	 * Returns the variable name if this variable is not attached to a constant
	 * @return
	 * 		the variable name if this variable is not attached to a constant
	 * 		null otherwise
	 */
	public String getFreeVar(){
		return var;
	}
	/**
	 * Sets the variable name.
	 * @param v
	 */
	public void setVar(String v){
		var=v;
	}

	/**
	 * Replaces this var name with a unique name.
	 * @param index
	 * 		an integer used to construct the name.
	 */
	public void changeVarName(int index){
		if(var!=null)
			var = var + "_unique" + index;
	}
	
	/**
	 * Unify this term.
	 * @param binding
	 * 		the binding list.
	 * @return
	 * 		the modified term
	 */
	public Term unify(Binding binding){
		//String newV = binding.getVar(var);
		String newV = binding.getUnificationVar(var);
		//System.out.println("Binding:" + binding);
		//System.out.println("unify for:" + var + " with " + newV);
		if(newV!=null){
			if(MediatorUtil.isVar(newV)){
				//it's a var
				setVar(newV);
			}
			else{
				ConstTerm ct = new ConstTerm(var, newV, queryName);
				return ct;
			}
		}
		return this;
	}
	
}

