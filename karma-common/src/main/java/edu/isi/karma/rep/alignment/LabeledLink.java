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

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;


public abstract class LabeledLink extends DefaultLink {
	
	private static final long serialVersionUID = 1L;
	static Logger logger = LoggerFactory.getLogger(LabeledLink.class);

	private Label label;
	private LinkStatus status;
	private boolean isProvenance;
	private LinkKeyInfo keyInfo;
	private Set<String> modelIds;
	private boolean isMainProvenanceLink;
	
	public LabeledLink(String id, Label label, LinkType type, boolean isProvenance) {
		super(id, type);
		
		init();
		if (label != null) this.label = label;
		this.isProvenance = isProvenance;
		this.isMainProvenanceLink = false;
	}
	
	public LabeledLink(String id, Label label, LinkType type, LinkKeyInfo keyInfo) {
		super(id, type);

		this.init();
		if (label != null) this.label = label;
		if (keyInfo != null) this.keyInfo = keyInfo;
		this.isProvenance = false;
		this.isMainProvenanceLink = false;
	}
	
	public LabeledLink(LabeledLink e) {
		super(e);
		if (e == null) this.init();
		else {
			this.label = e.label;
			this.status = e.status;
			this.keyInfo = e.keyInfo;
			this.isProvenance = e.isProvenance;
			this.isMainProvenanceLink = e.isMainProvenanceLink;
		}
	}
	
	private void init() {
		Label l = null;
		this.label = new Label(l);
		this.status = LinkStatus.Normal;
		this.keyInfo = LinkKeyInfo.None;
		this.isProvenance = false;
		this.isMainProvenanceLink = false;
		this.modelIds = new HashSet<>();
	}
	
	public Label getLabel() {
		return this.label;
	}
	
	public String getUri() {
		if (this.label != null)
			return this.getLabel().getUri();
		return null;
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
	
	public Set<String> getModelIds() {
		if (this.modelIds == null)
			return new HashSet<>();
		return modelIds;
	}

	public void setModelIds(Set<String> patternIds) {
		this.modelIds = patternIds;
	}

    public LabeledLink clone() {
    	
    	Cloner cloner = new Cloner();
    	return cloner.deepClone(this);
    }

    public LabeledLink copy(String newId) {
    	
		LabeledLink newLink = null;
		Label label = this.getLabel();
		if (this instanceof DataPropertyLink)
			newLink = new DataPropertyLink(newId, label, this.isProvenance);
		else if (this instanceof ObjectPropertyLink)
			newLink = new ObjectPropertyLink(newId, label, ((ObjectPropertyLink)this).getObjectPropertyType());
		else if (this instanceof SubClassLink)
			newLink = new SubClassLink(newId);
		else if (this instanceof ClassInstanceLink)
			newLink = new ClassInstanceLink(newId, this.getKeyType());
		else if (this instanceof ColumnSubClassLink)
			newLink = new ColumnSubClassLink(newId);
		else if (this instanceof DataPropertyOfColumnLink)
			newLink = new DataPropertyOfColumnLink(newId, 
					((DataPropertyOfColumnLink)this).getSpecializedColumnHNodeId(),
					((DataPropertyOfColumnLink)this).getSpecializedLinkId()
					);
		else if (this instanceof ObjectPropertySpecializationLink)
			newLink = new ObjectPropertySpecializationLink(newId, ((ObjectPropertySpecializationLink)this).getSpecializedLinkId());
		else
			logger.error("cannot instanciate a link from the type: " + this.getType().toString());
		
		newLink.setStatus(this.getStatus());
		newLink.setModelIds(new HashSet<>(this.getModelIds()));
		newLink.setKeyType(this.getKeyType());
		newLink.setProvenance(this.isProvenance, this.isMainProvenanceLink);
		return newLink;
    }
    
    public boolean isProvenance() {
    	return isProvenance;
    }
    
    public boolean isMainProvenanceLink() {
    	return this.isMainProvenanceLink;
    }
    
    public void setProvenance(boolean isProvenance, boolean isMainProvLink) {
    	this.isProvenance = isProvenance;
    	this.isMainProvenanceLink = isMainProvLink;
    }
}
