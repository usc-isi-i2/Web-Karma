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

package edu.isi.karma.modeling.research;

import org.apache.log4j.Logger;

import edu.isi.karma.rep.alignment.Label;

public class SemanticLabel implements Comparable<SemanticLabel>{

	static Logger logger = Logger.getLogger(SemanticLabel.class);

	private Label nodeLabel;
	private Label linkLabel;
	private String leafName;
	
	public SemanticLabel(Label nodeLabel) {
		this.nodeLabel = nodeLabel;
		this.linkLabel = null;
		this.leafName = null;
	}
	
	public SemanticLabel(Label nodeLabel, Label linkLabel, String leafName) {
		this.nodeLabel = nodeLabel;
		this.linkLabel = linkLabel;
		this.leafName = leafName;
	}

	public Label getNodeLabel() {
		return nodeLabel;
	}

	public Label getLinkLabel() {
		return linkLabel;
	}

	public String getLeafName() {
		return leafName;
	}

	@Override
	public int compareTo(SemanticLabel sl) {
		if (this.nodeLabel == null || sl.getNodeLabel() == null) return 1;
		if (this.nodeLabel.getUri() == null || sl.getNodeLabel().getUri() == null) return 1;
		if (this.nodeLabel.getUri().equalsIgnoreCase(sl.getNodeLabel().getUri ())) {
			if (this.linkLabel == null && sl.getLinkLabel() == null) return 0;
			if (this.linkLabel == null || sl.getLinkLabel() == null) return 1;
			if (this.linkLabel.getUri() == null || sl.getLinkLabel().getUri() == null) return 1;
			if (this.linkLabel.getUri().equalsIgnoreCase(sl.getLinkLabel().getUri ())) return 0;
		}
		return 1;
	}

	public void print() {
		String s = "";
		if (this.nodeLabel != null && this.nodeLabel.getUri() != null) s += this.nodeLabel.getUri();
		if (this.linkLabel != null && this.linkLabel.getUri() != null) s += " --- " + this.linkLabel.getUri();
		if (this.leafName != null)  s += " --- " + this.leafName;
		logger.info(s);
	}
	
}
