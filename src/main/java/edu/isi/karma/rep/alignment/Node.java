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
package edu.isi.karma.rep.alignment;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

import edu.isi.karma.util.RandomGUID;

public abstract class Node implements Comparable<Node>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Logger logger = LoggerFactory.getLogger(Node.class);

	private String id;
	private Label label;
	private NodeType type;
	private Set<String> patternIds;

	public Node(String id, Label label, NodeType type) {
		
		this.init();
		if (id != null && id.trim().length() > 0) this.id = id;
		if (label != null) this.label = label;
		if (type != null) this.type = type;
	}
	
	public Node(Node v) {
		if (v == null) this.init();
		else {
			this.id = v.id;
			this.label = v.label;
			this.type = v.type;
		}
	}
	
	private void init() {
		this.id = new RandomGUID().toString();
		Label l = null;
		this.label = new Label(l);
		this.type = NodeType.None;
		this.patternIds = new HashSet<String>();
	}
	
	public String getId() {
		return this.id;
	}
	
	public Label getLabel() {
		return this.label;
	}
	
	public String getLocalId() {
		
		String s = this.id;

		if (this.label.getNs() != null)
			s = s.replaceAll(this.label.getNs(), "");
		
		return s;
	}
	
	public String getDisplayId() {
		
		if (this.label.getPrefix() == null)
			return this.getLocalId();
		
		return this.label.getPrefix() + ":" + this.getLocalId();
	}
	
	public NodeType getType() {
		return type;
	}
	
	public Set<String> getPatternIds() {
		return patternIds;
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Node node = (Node) obj;
        return this.id == node.getId();
    }
    
    @Override
    public int hashCode() {
    	return this.getId().hashCode();
    }

    @Override
    public int compareTo(Node node) {       
        //compare id
        return this.id.compareTo(node.getId());
    }

    public Node clone() {

    	Cloner cloner = new Cloner();
    	return cloner.deepClone(this);

//    	switch (this.type) {
//			case None: return new SimpleNode(this.getId(), this.getLabel()); 
//			case ColumnNode: return new ColumnNode(this.getId(), ((ColumnNode)this).getHNodeId(), ((ColumnNode)this).getColumnName()); 
//			case LiteralNode: return new LiteralNode(this.getId(), ((LiteralNode)this).getValue(), ((LiteralNode)this).getDatatype()); 
//			case InternalNode: return new InternalNode(this.getId(), this.getLabel());
//		}
//
//		logger.error("Cloning the node has been failed. Cannot identify the type of the node.");
//		return null;
    }
}
