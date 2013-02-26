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

	private String id;
	private Label label;
	private LinkType type;
	private LinkStatus status;
	private LinkKeyInfo keyInfo;
	
	public Link(String id, Label label, LinkType type) {
		super();

		this.init();
		if (id != null && id.trim().length() > 0) this.id = id;
		if (label != null) this.label = label;
		if (type != null) this.type = type;
	}
	
	public Link(String id, Label label, LinkType type, LinkKeyInfo keyInfo) {
		super();

		this.init();
		if (id != null && id.trim().length() > 0) this.id = id;
		if (label != null) this.label = label;
		if (type != null) this.type = type;
		if (keyInfo != null) this.keyInfo = keyInfo;
	}
	
	public Link(Link e) {
		super();
		if (e == null) this.init();
		else {
			this.id = e.id;
			this.label = e.label;
			this.type = e.type;
			this.status = e.status;
			this.keyInfo = e.keyInfo;
		}
	}
	
	private void init() {
		this.id = new RandomGUID().toString();
		Label l = null;
		this.label = new Label(l);
		this.type = LinkType.None;
		this.status = LinkStatus.Normal;
		this.keyInfo = LinkKeyInfo.None;
	}
	
	public String getId() {
		return this.id;
	}
	
	public Label getLabel() {
		return this.label;
	}
	
	public String getLocalId() {
		String s = this.id;

		if (this.label != null && this.label.getNs() != null)
			s = s.replaceAll(this.label.getNs(), "");
		
		return s;
	}

	public LinkType getType() {
		return type;
	}
	
	public LinkStatus getStatus() {
		return status;
	}

	public void setStatus(LinkStatus status) {
		this.status = status;
	}

	public LinkKeyInfo getKeyType() {
		return keyInfo;
	}

	public void setKeyType(LinkKeyInfo keyType) {
		this.keyInfo = keyType;
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
    
    public Link clone() {

    	Link link = null;
    	switch (this.type) {
			case None: { 
				link = new SimpleLink(this.getId(), this.getLabel()); 
			} 
			case ClassInstanceLink: { 
				link = new ClassInstanceLink(this.getId(), this.getKeyType()); 
			} 
			case ColumnSubClassLink: {
				link = new ColumnSubClassLink(this.getId());
			}
			case DataPropertyLink: 
			{
				if (this.getKeyType() == LinkKeyInfo.PartOfKey) 
					link = new DataPropertyLink(this.getId(), this.getLabel(), true);
				else
					link = new DataPropertyLink(this.getId(), this.getLabel());
			}
			case DataPropertyOfColumnLink: { 
				link = new DataPropertyOfColumnLink(this.getId(), ((DataPropertyOfColumnLink) this).getSpecializedColumnHNodeId());
			}
			case ObjectPropertyLink: {
				link = new ObjectPropertyLink(this.getId(), this.getLabel());
			}
			case SubClassLink: {
				link = new SubClassLink(this.getId());
			}

		}
    	
    	if (link != null) 
			link.setStatus(this.getStatus());
    	else
    		logger.error("Cloning the link has been failed. Cannot identify the type of the link.");
		
    	return link;
    }
}
