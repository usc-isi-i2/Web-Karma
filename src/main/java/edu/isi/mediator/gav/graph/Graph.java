// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.gav.graph;

import java.util.ArrayList;

import edu.isi.mediator.gav.graph.cost_estimation.StatisticsInfo;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.Predicate;
import edu.isi.mediator.rule.RelationPredicate;

public class Graph
{
	private BaseNode parentNode;
	
    public Graph(){}
    
    public Graph(BaseNode parent){
    	parentNode=parent;
    }

    public void setParentNode(BaseNode n){
    	parentNode=n;
    }
    
    public BaseNode getParentNode(){
    	return parentNode;
    }
    
    public String getSQL() throws MediatorException{
    	parentNode.setSQL();
    	//String sql = parentNode.getSQL();
    	String sql = parentNode.getSQLWithOrderBy();
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
	private BaseNode buildJoin(RelationPredicate rel, BaseNode graphSoFar){
		System.out.println("Join with " + rel);
		/*
		System.out.println("Build join with " + source);
		if(joinSoFar!=null)
			System.out.println("JoinSoFar= " + joinSoFar.getString());
			*/
		DataAccessNode dn = new DataAccessNode(rel);
		
		if(graphSoFar==null){
			//it's the first source; build a data node
			return dn;
		}
		else{
			//find Common Attributes
			ArrayList<String> joinAttrs = new ArrayList<String>();
			dn.findCommonAttrs(graphSoFar, joinAttrs);
			JoinNode jn = new JoinNode(joinAttrs, dn, graphSoFar);
			return jn;
		}
	}

    // COST ESTIMATION
    public StatisticsInfo getCostEstimation() throws MediatorException{
    	return parentNode.evaluateSizeCost();
    }

    public String getString(){
    	return parentNode.getString();
    }

}
