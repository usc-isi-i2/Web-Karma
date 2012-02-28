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

