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

import edu.isi.karma.rep.semantictypes.SemanticType;

public class Vertex {

	private String id;
	private NodeType nodeType;
	private Name name;
	private SemanticType semanticType;
	
	public Vertex(String id, Name name, NodeType nodeType) {
		this.id = id;
		this.name = name;
		this.nodeType = nodeType;
		this.semanticType = null;
	}
	
	public Vertex(String id, Name name, SemanticType semanticType, NodeType nodeType) {
		this.id = id;
		this.name = name;
		this.nodeType = nodeType;
		this.semanticType = semanticType;
	}
	
	public Vertex(Vertex v) {
		this.id = v.id;
		this.name = new Name(v.name);
		this.nodeType = v.nodeType;
		this.semanticType = v.semanticType;
	}
	
	public String getLocalID() {
		String s = id;
		s = s.replaceAll(this.name.getNs(), "");
		return s;
	}

	public String getLocalLabel() {
		String s = this.name.getUri();
		s = s.replaceAll(this.name.getNs(), "");
		return s;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getUri() {
		return this.name.getUri();
	}
	
	public String getNs() {
		return this.name.getNs();
	}
	
	public String getPrefix() {
		return this.name.getPrefix();
	}
	
	public NodeType getNodeType() {
		return this.nodeType;
	}
	
	public SemanticType getSemanticType() {
		return this.semanticType;
	}

	public boolean equals(Object obj){
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        if( ((Vertex)obj).getID() == this.getID()){
            return true;
        }
        return false;
    }
}
