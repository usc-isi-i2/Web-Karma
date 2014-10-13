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

package edu.isi.karma.reserach.alignment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SemanticLabel{

	static Logger logger = LoggerFactory.getLogger(SemanticLabel.class);

	private String nodeUri;
	private String linkUri;
	private String leafName;
	private SemanticLabelType type;
	
	public SemanticLabel(String nodeUri) {
		this.nodeUri = nodeUri;
		this.linkUri = null;
		this.leafName = null;
		this.type = SemanticLabelType.Class;
	}
	
	public SemanticLabel(String nodeUri, String linkUri, String leafName) {
		this.nodeUri = nodeUri;
		this.linkUri = linkUri;
		this.leafName = leafName;
		if (this.linkUri == null) this.type = SemanticLabelType.Class;
		else this.type = SemanticLabelType.DataProperty;
	}

	
	public String getNodeUri() {
		return nodeUri;
	}


	public String getLinkUri() {
		return linkUri;
	}

	public String getLeafName() {
		return leafName;
	}
	
	public SemanticLabelType getType() {
		return type;
	}

	public void print() {
		String s = "";
		if (this.nodeUri != null) s += this.nodeUri;
		if (this.linkUri != null) s += this.linkUri;
		logger.info(s);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((linkUri == null) ? 0 : linkUri.hashCode());
		result = prime * result + ((nodeUri == null) ? 0 : nodeUri.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SemanticLabel other = (SemanticLabel) obj;
		if (linkUri == null) {
			if (other.linkUri != null)
				return false;
		} else if (!linkUri.equalsIgnoreCase(other.linkUri))
			return false;
		if (nodeUri == null) {
			if (other.nodeUri != null)
				return false;
		} else if (!nodeUri.equalsIgnoreCase(other.nodeUri))
			return false;
		return true;
	}


	
}
