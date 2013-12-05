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

package edu.isi.karma.modeling.research.approach1;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.SemanticType;

public class SemanticTypeMapping {
	
	private SemanticType semanticType;
	private InternalNode source;
	private Link link;
	private ColumnNode target;
	private ColumnNode sourceColumn;
	
	public SemanticTypeMapping(ColumnNode sourceColumn, 
			SemanticType semanticType, 
			InternalNode source, Link link, 
			ColumnNode target) {
		this.sourceColumn = sourceColumn;
		this.semanticType = semanticType;
		this.source = source;
		this.link = link;
		this.target = target;
	}

	public InternalNode getSource() {
		return source;
	}

	public Link getLink() {
		return link;
	}

	public ColumnNode getTarget() {
		return target;
	}
	
	public ColumnNode getSourceColumn() {
		return sourceColumn;
	}
	
	public double getConfidence() {
		return this.semanticType.getConfidenceScore();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		SemanticTypeMapping other = (SemanticTypeMapping) obj;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (source == null) {
			if (other.source != null)
				return false;
		} else if (!source.equals(other.source))
			return false;
		return true;
	}
}
