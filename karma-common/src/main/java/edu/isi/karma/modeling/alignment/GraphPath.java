/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.modeling.alignment;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.Node;

public class GraphPath {
	
	private List<DefaultLink> edgeList;
	private Node startNode, endNode;
	
	
	public GraphPath() {
		this.edgeList = new LinkedList<>();
		this.startNode = null;
		this.endNode = null;
	}
	
	public GraphPath(GraphPath gp) {
		this.edgeList = new LinkedList<>();
		for (DefaultLink l : gp.getLinks())
			this.addLink(l);
	}
	
	public void addLink(DefaultLink e) {
		if (this.edgeList.isEmpty())
			this.startNode = e.getSource();
		this.edgeList.add(e);
		this.endNode = e.getTarget();
	}
	
	public void addLinkToHead(DefaultLink e) {
		if (this.edgeList.isEmpty())
			this.endNode = e.getTarget();
		this.edgeList.add(0, e);
		this.startNode = e.getSource();
	}
	
	public Node getStartNode() {
		return this.startNode;
	}
	
	public Node getEndNode() {
		return this.endNode;
	}

	public List<DefaultLink> getLinks() {
		return Collections.unmodifiableList(this.edgeList);
	}
	
	public int getLength() {
		return this.edgeList.size();
	}
	
	
	public String toString() {
		String key = "";
		
		if (this.edgeList.isEmpty())
			return key;
		
		if (this.startNode != null)
			key += this.startNode.getLocalId();
		
		for (DefaultLink l : this.edgeList) {
			key += "<";
			key += l.getUri();
			key += ">";
			if (l.getTarget() != null) key += l.getTarget().getLocalId();
		}
		return key;
	}
}
