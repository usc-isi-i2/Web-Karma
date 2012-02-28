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

import java.util.*;

import edu.isi.mediator.gav.main.MediatorException;

public class SimpleJoinNode extends SimpleBaseNode
{
	
	ArrayList<String> joinAttributes;
    public SimpleBaseNode child1;
    public SimpleBaseNode child2;

    public SimpleJoinNode(ArrayList<String> joinAttrs, SimpleBaseNode child1, SimpleBaseNode child2){
    	joinAttributes=joinAttrs;
    	this.child1=child1;
    	this.child2=child2;
    }
    
    public ArrayList<SimpleBaseNode> getSubNodes() {
        ArrayList<SimpleBaseNode> subnodes = new ArrayList<SimpleBaseNode>();
	if (child1 != null)
	    subnodes.add(child1);
	if (child2 != null)
	    subnodes.add(child2);
	return subnodes;
    }
    
    public void setSQL() throws MediatorException{
    	
		//I already did setSQL() in this node
    	//because of binding patterns there may be 2 ways to this node
		if(!sqlSelect.isEmpty())
			return;

    	child1.setSQL();
    	child2.setSQL();
    	
    	//where = child1.where + child2.where + the joinConditions
    	for(int i=0; i<joinAttributes.size(); i++){
    		String joinAttr = joinAttributes.get(i);
    		String actualName1 = child1.getActualName(joinAttr);
    		String actualName2 = child2.getActualName(joinAttr);
    		String bindMarker1 = child1.needsBinding(joinAttr);
    		String bindMarker2 = child2.needsBinding(joinAttr);
    		//System.out.println("Add Join=" + bindMarker1 + actualName1 + "=" + bindMarker2 + actualName2);
    		sqlWhere.add(bindMarker1 + actualName1 + "=" + bindMarker2 + actualName2);
    	}
    	sqlWhere.addAll(child1.sqlWhere);
    	sqlWhere.addAll(child2.sqlWhere);
    	
    	//union the froms
    	sqlFrom.addAll(child1.sqlFrom);
    	sqlFrom.addAll(child2.sqlFrom);
    	
    	//for each of the joinAttrs leave in the select only one
    	ArrayList<String> selectWithoutJoinAttrs = child1.removeFromSelect(joinAttributes);
    	sqlSelect.addAll(child2.sqlSelect);
    	sqlSelect.addAll(selectWithoutJoinAttrs);
    	for(int i=0; i<selectWithoutJoinAttrs.size(); i++){
    		String oneSelect = selectWithoutJoinAttrs.get(i);
    		if(!sqlSelect.contains(oneSelect))
    			sqlSelect.add(oneSelect);
    	}
		//System.out.println("Join select=" + sqlSelect);
    }

    public String getString(){
    	alreadyPrinted=true;
    	String s = "";
    	s += getPrintName();
    	s += "joinAttributes=" + joinAttributes + "\n";
    	if(child1 == null)
    		s += "child1=NULL\n";
    	else{
    		if(child1.alreadyPrinted)
    			s += "child1=" + child1.getPrintName();
    		else
    			s += "child1=" + child1.getString();
    	}
    	if(child2 == null)
    		s += "child2=NULL\n";
    	else{
    		if(child2.alreadyPrinted)
    			s += "child2=" + child2.getPrintName();
    		else
    			s += "child2=" + child2.getString();
    	}
    	s += "-------------------------------------\n";
    	return s;
    }
    
    public String getPrintName(){
    	return "------------ Join " + this+ "---------------\n";
    }
}
