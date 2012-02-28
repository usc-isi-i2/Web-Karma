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
