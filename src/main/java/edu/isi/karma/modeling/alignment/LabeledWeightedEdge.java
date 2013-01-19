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

import org.jgrapht.graph.DefaultWeightedEdge;

public class LabeledWeightedEdge extends DefaultWeightedEdge {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String id;
	private LinkType linkType;
	private boolean inverse;
	private URI uri;
	private LinkStatus linkStatus;
	
	public LabeledWeightedEdge(String id, URI name, LinkType linkType) {
		super();
		this.id = id;
		this.linkType = linkType;
		this.uri = name;
		this.inverse = false;
		this.linkStatus = LinkStatus.None;
	}
	
	public LabeledWeightedEdge(String id, URI name, LinkType linkType, boolean inverse) {
		super();
		this.id = id;
		this.linkType = linkType;
		this.uri = name;
		this.inverse = inverse;;
		this.linkStatus = LinkStatus.None;
	}
	
	public LabeledWeightedEdge(LabeledWeightedEdge e) {
		super();
		this.id = e.id;
		this.linkType = e.linkType;
		this.uri = new URI(e.uri);
		this.inverse = e.inverse;;
		this.linkStatus = LinkStatus.None;
	}
	
	public String getLocalID() {
		String s = this.id;
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
	
	
	public LinkStatus getLinkStatus() {
		return linkStatus;
	}

	public void setLinkStatus(LinkStatus linkStatus) {
		this.linkStatus = linkStatus;
	}
	
	public boolean isInverse() {
		return this.inverse;
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
	
	public LinkType getLinkType() {
		return this.linkType;
	}
	
	public Vertex getSource() {
		return (Vertex)super.getSource();
	}

	public Vertex getTarget() {
		return (Vertex)super.getTarget();
	}
	
	public double getWeight() {
		return super.getWeight();
	}
	
    public boolean equals(Object obj){
        if(obj == null || obj.getClass() != this.getClass()){
            return false;
        }
        if( ((LabeledWeightedEdge)obj).getID() == this.getID()){
            return true;
        }
        return false;
    }
}
