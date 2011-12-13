// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.simplegraph;

import java.util.ArrayList;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.RelationPredicate;

public class SimpleGraph
{
	private SimpleBaseNode parentNode;
	
    public SimpleGraph(){}
    
    public SimpleGraph(SimpleBaseNode parent){
    	parentNode=parent;
    }

    public void setParentNode(SimpleBaseNode n){
    	parentNode=n;
    }
    
    public SimpleBaseNode getParentNode(){
    	return parentNode;
    }
    
    public String getSQL() throws MediatorException{
    	parentNode.setSQL();
    	String sql = parentNode.getSQL();
    	return sql;
    }
    
    /**
     * Generates a graph given a list of Predicates. Only considers Joins at this time.
     */
    public void generateGraph(ArrayList<Predicate> predicates){
		for(int i=0; i<predicates.size(); i++){
			RelationPredicate rel = (RelationPredicate)predicates.get(i);
			parentNode = buildJoin(rel, parentNode);
		}
    }
    
	/**
	 * Adds a join node to the given graph.
	 * @param rel
	 * 		relation to be joined with the given graph
	 * @param graphSoFar
	 * 		graph that join is added to
	 * @return
	 * 		a new graph with a join node added to graphSoFar
	 */
	private SimpleBaseNode buildJoin(RelationPredicate rel, SimpleBaseNode graphSoFar){
		System.out.println("Join with " + rel);
		/*
		System.out.println("Build join with " + source);
		if(joinSoFar!=null)
			System.out.println("JoinSoFar= " + joinSoFar.getString());
			*/
		SimpleDataAccessNode dn = new SimpleDataAccessNode(rel);
		
		if(graphSoFar==null){
			//it's the first source; build a data node
			return dn;
		}
		else{
			//find Common Attributes
			ArrayList<String> joinAttrs = new ArrayList<String>();
			dn.findCommonAttrs(graphSoFar, joinAttrs);
			SimpleJoinNode jn = new SimpleJoinNode(joinAttrs, dn, graphSoFar);
			return jn;
		}
	}

    public String getString(){
    	return parentNode.getString();
    }

}
