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

public class MappingStruct {
	
	private InternalNode source;
	private Link link;
	private ColumnNode target;

	public MappingStruct(InternalNode source) {
		this.source = source;
		this.link = null;
		this.target = null;
	}
	
	public MappingStruct(InternalNode source, Link link, ColumnNode target) {
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

	
}
