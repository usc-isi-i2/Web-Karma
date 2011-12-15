// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;

import java.math.BigDecimal;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorConstants;

/**
 * Defines a constant term.
 * @author mariam
 *
 */
public class ConstTerm extends Term{
	
	/**
	 * the constant
	 */
	private String val;
	
	/**
	 * Constructs an empty ConstTerm.
	 */
	public ConstTerm(){}
	
	/**
	 * Constructs a ConstTerm with the value "val"
	 * @param val
	 * 		constant value
	 */
	public ConstTerm(String val){
		this.val = normalizeVal(val);
	}
	/**
	 * Constructs a ConstTerm with the variable "var" and value "val".
	 * @param var
	 * 		variable name
	 * @param val
	 * 		constant value
	 */
	public ConstTerm(String var, String val){
		this.var=var;
		this.val = normalizeVal(val);
	}

	/**
	 * Constructs a ConstTerm with the variable "var" and value "val".
	 * Associates this term with the SQL query name "queryName" (equivalent to the "as" name ... select a as x)
	 * @param var
	 * 		variable name
	 * @param val
	 * 		constant value
	 * @param queryName
	 * 		sql query variable name (used only with SQL interface; (equivalent to the "as" name ... select a as x)
	 */
	public ConstTerm(String var, String val, String queryName){
		this.var=var;
		this.val = normalizeVal(val);
		this.queryName = queryName;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	public ConstTerm clone(){
		ConstTerm t = new ConstTerm();
		t.val=val;
		t.var=var;
		t.queryName = queryName;
		t.isAllowedAfterUAC=isAllowedAfterUAC;
		return t;
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#getFreeVar()
	 * return null
	 */
	public String getFreeVar(){
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#getTermValue()
	 * return val
	 */
	public String getTermValue(){
		return val;
	}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#getVal()
	 */
	public String getVal(){
		return val;
	}
	
	/**
	 * Sets the constant vale;
	 * @param v
	 * 		the constant value
	 */
	public void setVal(String v){
		//System.out.println("Set Value " + v);
		val=normalizeVal(v);
	}	
		
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#equals(edu.isi.mediator.gav.domain.Term)
	 */
	public boolean equals(Term t){
		//System.out.println("Equal term : " + this + " and " + t);
		String var1, var2;
		if(var==null)
			var1="null";
		else var1=var;
		
		if(t.var==null)
			var2="null";
		else var2=t.var;

		if(!(t instanceof ConstTerm))
			return false;
		if(val.equals(((ConstTerm)t).val) && var1.equals(var2)){
				return true;
		}
		else return false;
	}
	
	/**
	 * Checks equality of VALUE.
	 * @param t
	 * @return
	 * 		true if the VALUE of the two terms is equal
	 * 		false otherwise
	 */
	public boolean equalsValue(ConstTerm t){
		
		String newV1=val, newV2=t.val;
		if(newV1.startsWith("\"") || newV1.startsWith("'"))
			newV1 = newV1.substring(1, newV1.length()-1);

		if(newV2.startsWith("\"") || newV2.startsWith("'"))
			newV2 = newV2.substring(1, newV2.length()-1);

		if(newV1.equals(newV2)){
			return true;
		}
		else return false;
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#needsBinding(boolean)
	 * Always returns false; A ConstTerm is already bound.
	 */
	public boolean needsBinding(boolean b){
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#unify(edu.isi.mediator.gav.domain.Binding)
	 */
	public Term unify(Binding binding){
		return this;
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#getSqlVal(boolean)
	 */
	public String getSqlVal(boolean isNumber) throws MediatorException{
		if(isNumber)
			return getNumberVal();
		else
			return getStringVal();
	}
	
	//I don't know what it is, so I return t as is
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#getSqlValNoType()
	 */
	public String getSqlValNoType() throws MediatorException{
		String newV = val;
		if(newV.equals(MediatorConstants.NULL_VALUE)){
			return MediatorConstants.NULL_VALUE;
		}
		if(newV.startsWith("\"") || newV.startsWith("'")){
			newV = val.substring(1, val.length()-1);
			if(newV.equals(MediatorConstants.NULL_VALUE))
				return MediatorConstants.NULL_VALUE;
			else
				return "'" + newV + "'";
		}
		else{
			return newV;
		}
	}
	
	/**
	 * Returns the value as a number.
	 * @return
	 * 		the value as a number (not enclosed between single quotes)
	 * @throws MediatorException
	 */
	private String getNumberVal() throws MediatorException{
		//val could start with " or '
		//if it does remove them
		String newV = val;
		if(newV.equals(MediatorConstants.NULL_VALUE)){
			return MediatorConstants.NULL_VALUE;
		}
		try{
			if(newV.startsWith("\"") || newV.startsWith("'")){
				newV = val.substring(1, val.length()-1);
			}
			//a number can be between "'" in a sql query
			/*
			else if(newV.startsWith("'")){
				throw new MediatorException(newV + " should be a Number! Do not enclose it between \"'\"");
			}
			*/
			//make sure that newV is a number
			if(newV.equals(MediatorConstants.NULL_VALUE)){
				return MediatorConstants.NULL_VALUE;
			}
			else{
				new BigDecimal(newV);
			}
		}catch(NumberFormatException ne){
			throw new MediatorException(newV + " should be a number!" + ne.getMessage());			
		}catch(Exception e){
			throw new MediatorException(e.getMessage());
		}
		
		return newV;
	}

	/**
	 * Returns the value as a string.
	 * @return
	 * 		the value as a string (enclosed between single quotes)
	 * @throws MediatorException
	 */
	public String getStringVal() throws MediatorException{
		//if val should be a string it has to start with " or '
		//if it doesn't it's not a string
		
		String newV = val;
		if(newV.equals(MediatorConstants.NULL_VALUE)){
			return MediatorConstants.NULL_VALUE;
		}
		if(newV.startsWith("\"") || newV.startsWith("'")){
			newV = val.substring(1, val.length()-1);
			if(newV.equals(MediatorConstants.NULL_VALUE))
				return MediatorConstants.NULL_VALUE;
			else
				return "'" + newV + "'";
		}
		else{
			throw new MediatorException(newV + " should be a String! Enclose it between \"'\"");
		}
	}
		
	/**
	 * Returns NULL for an input of null (always uppercase)
	 * @param val
	 * 		input value
	 * @return
	 * 		NULL for an input of null (always uppercase)
	 * 		input value otherwise
	 */
	private String normalizeVal(String val){
		if(val==null)
			return MediatorConstants.NULL_VALUE;
		else if(val.toUpperCase().equals(MediatorConstants.NULL_VALUE))
			return MediatorConstants.NULL_VALUE;
		else return val;
	}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#toString()
	 */
	public String toString(){
		String s = val;
		if(var!=null)
			s = var + ":" + s;
		if(queryName!=null)
			s += ":" + queryName;
		return s;
	}

}

