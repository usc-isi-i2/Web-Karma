// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.graph;

import java.util.*;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.Term;

/*
if we have X<Y in a query and X and Y come from different relations =>
cross product + a selection with X<Y
 */
public class AssignNode extends BaseNode
{
	
	private Predicate assign;
	public BaseNode child;

	public AssignNode(Predicate assign, BaseNode child){
		this.assign=assign;
		this.child=child;
	}
	
	public ArrayList<BaseNode> getSubNodes() {
		ArrayList<BaseNode> subnodes = new ArrayList();
		if (child != null)
			subnodes.add(child);
		return subnodes;
	}
	
	public void setSQL() throws MediatorException{
		
		child.setSQL();
		
		sqlFrom.addAll(child.sqlFrom);
		sqlWhere.addAll(child.sqlWhere);
		sqlSelect.addAll(child.sqlSelect);

		//add the assign to the sqlSelect
		//generate the select statement
		ArrayList<Term> terms = assign.getTerms();
		String var = terms.get(0).getVar();
		String val = terms.get(1).getSqlValNoType();

		String theAssign = val + " as " + var + " ";
		
		if(!sqlSelect.contains(theAssign))
			sqlSelect.add(theAssign);
	}

    public String getString(){
    	String s = "";
    	s += "------------ Assign " + this+ "---------\n";
    	s += assign + "\n";
    	s += "child=" + child.getString();

    	s += "-------------------------------------\n";
    	return s;
    }

}
