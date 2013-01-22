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

import org.apache.log4j.Logger;
import org.jgrapht.graph.DefaultWeightedEdge;

import edu.isi.karma.util.RandomGUID;


public abstract class Link extends DefaultWeightedEdge implements Comparable<Link> {
	
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(Link.class);

	private final String id;
	private final Label label;
	private LinkStatus status;
	
	public Link(String id, Label label) {
		super();

		if (id == null || id.trim().length() == 0) {
			logger.info("The input id is empty. A random Guid has been assigned.");
			id = new RandomGUID().toString();
		}
		
		this.id = id;
		this.label = label;
		this.status = LinkStatus.Normal;
	}
	
	public Link(Link e) {
		super();
		this.id = e.id;
		this.label = e.label;
		this.status = e.status;
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getLocalId() {
		String s = this.id;

		if (this.label != null)
			s = s.replaceAll(this.label.getNs(), "");
		
		return s;
	}
	
	public String getLocalName() {
		if (this.label == null)
			return null;

		String s = this.label.getUriString();
		s = s.replaceAll(this.label.getNs(), "");
		return s;
	}
	
	public String getUriString() {
		return this.label.getUriString();
	}
	
	public String getNs() {
		return this.label.getNs();
	}
	
	public String getPrefix() {
		return this.label.getPrefix();
	}
	
	public LinkStatus getStatus() {
		return status;
	}

	public void setStatus(LinkStatus status) {
		this.status = status;
	}

	public Node getSource() {
		return (Node)super.getSource();
	}

	public Node getTarget() {
		return (Node)super.getTarget();
	}
	
	public double getWeight() {
		return super.getWeight();
	}

	@Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        Link link = (Link) obj;
        return this.id == link.getId();
    }
    
    @Override
    public int hashCode() {
    	return this.getId().hashCode();
    }

    @Override
    public int compareTo(Link link) {       
        //compare id
        return this.id.compareTo(link.getId());
    }
    
}
