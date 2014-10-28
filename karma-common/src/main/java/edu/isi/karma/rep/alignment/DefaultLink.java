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

import org.jgrapht.graph.DefaultWeightedEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.util.RandomGUID;


public class DefaultLink extends DefaultWeightedEdge implements Comparable<DefaultLink> {
	
	private static final long serialVersionUID = 1L;
	static Logger logger = LoggerFactory.getLogger(DefaultLink.class);

	protected String id;
	private LinkType type;

	public DefaultLink() {
		super();
		this.init();
	}
	
	public DefaultLink(String id) {
		super();

		this.init();
		if (id != null && id.trim().length() > 0) this.id = id;
	}
	
	public DefaultLink(String id, LinkType type) {
		super();

		this.init();
		if (id != null && id.trim().length() > 0) this.id = id;
		if (type != null) this.type = type;

	}
	
	public DefaultLink(DefaultLink e) {
		super();
		if (e == null) this.init();
		else {
			this.id = e.id;
			this.type = e.type;
		}
	}
	
	private void init() {
		this.id = new RandomGUID().toString();
		this.type = LinkType.None;
	}
	
	public String getId() {
		return this.id;
	}
	
	public LinkType getType() {
		return type;
	}
	
	public String getUri() {
		return Uris.DEFAULT_LINK_URI; 
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

        DefaultLink link = (DefaultLink) obj;
        return this.id.equals(link.getId());
    }
    
    @Override
    public int hashCode() {
    	return this.getId().hashCode();
    }

    @Override
    public int compareTo(DefaultLink link) {       
        //compare id
        return this.id.compareTo(link.getId());
    }
    
    public DefaultLink getCopy(String newId) {
    	
    	if (this instanceof LabeledLink)
    		return ((LabeledLink)this).copy(newId);
    	else if (this instanceof CompactLink)
    		return ((CompactLink)this).copy(newId);
    	else {
			logger.error("cannot instanciate a link from the type: " + this.getType().toString());
			return null;
    	}
    }
}
