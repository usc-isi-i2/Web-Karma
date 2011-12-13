// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rule;

/**
 * Defines a Variable Term described by a var name.
 * @author mariam
 *
 */
public class VarTerm extends Term{
		
	/**
	 * Construct an empty VarTerm.
	 */
	public VarTerm(){}
	
	/**
	 * Constructs a VarTerm with the variable name "var"
	 * @param var
	 */
	public VarTerm(String var){
		this.var = var;
	}

	/**
	 * Constructs a VarTerm with the variable name "var" and query name "qName"
	 * @param var
	 * @param qName
	 */
	public VarTerm(String var, String qName){
		this.var = var;
		queryName=qName;
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#clone()
	 */
	public VarTerm clone(){
		VarTerm t = new VarTerm();
		t.var = var;
		t.queryName = queryName;
		t.isAllowedAfterUAC=isAllowedAfterUAC;
		return t;
	}
	
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#equals(edu.isi.mediator.gav.domain.Term)
	 */
	public boolean equals(Term t){
		if(!(t instanceof VarTerm))
			return false;
		if(var.equals(t.var))
			return true;
		else return false;
	}
	
	/**
	 * @param b
	 * 		needsBinding value of the associated {@link edu.isi.mediator.gav.source.SourceAttribute}
	 * @return
	 * 		true if the source attribute associated with this term needs binding, false otherwise.
	 */
	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#needsBinding(boolean)
	 */
	public boolean needsBinding(boolean b){
		return b;
	}

	/* (non-Javadoc)
	 * @see edu.isi.mediator.gav.domain.Term#toString()
	 */
	public String toString(){
		String s = var;
		if(queryName!=null)
			s += ":" + queryName;
		//s += " " + isAllowedAfterUAC;
		return s;
	}

}

