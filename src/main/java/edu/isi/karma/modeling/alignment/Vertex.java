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
	private URI uri;
	private SemanticType semanticType;
	// only used for the vertexes of type DataProperty
	private String domainVertexId;
	
	public Vertex(String id, URI name, NodeType nodeType) {
		this.id = id;
		this.uri = name;
		this.nodeType = nodeType;
		this.semanticType = null;
	}
	
	public Vertex(String id, URI name, SemanticType semanticType, NodeType nodeType) {
		this.id = id;
		this.uri = name;
		this.nodeType = nodeType;
		this.semanticType = semanticType;
	}
	
	public Vertex(Vertex v) {
		this.id = v.id;
		this.uri = new URI(v.uri);
		this.nodeType = v.nodeType;
		this.semanticType = v.semanticType;
		this.domainVertexId = v.domainVertexId;
	}
	
	public String getLocalID() {
		String s = id;
		if (this.uri == null)
			return s;
		s = s.replaceAll(this.uri.getNs(), "");
		return s;
	}

	public String getLocalLabel() {
		if (this.uri == null)
			return null;
		String s = this.uri.getUriString();
		s = s.replaceAll(this.uri.getNs(), "");
		return s;
	}
	
	public String getID() {
		return this.id;
	}
	
	public String getUriString() {
		return this.uri.getUriString();
	}
	
	public String getNs() {
		return this.uri.getNs();
	}
	
	public String getPrefix() {
		return this.uri.getPrefix();
	}
	
	public NodeType getNodeType() {
		return this.nodeType;
	}
	
	public SemanticType getSemanticType() {
		return this.semanticType;
	}

	public String getDomainVertexId() {
		return domainVertexId;
	}

	public void setDomainVertexId(String domainVertexId) {
		this.domainVertexId = domainVertexId;
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
