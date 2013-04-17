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

package edu.isi.karma.modeling.research.experiment2;

import java.util.Set;

import edu.isi.karma.rep.alignment.Node;

public class RankedSteinerSet implements Comparable<RankedSteinerSet>{

	private Set<Node> nodes;
	
	public RankedSteinerSet(Set<Node> nodes) {
		this.nodes = nodes;
	}

	public Set<Node> getNodes() {
		return nodes;
	}

	@Override
	public int compareTo(RankedSteinerSet s) {
		
		int size1 = this.nodes == null ? 0 : this.nodes.size();
		int size2 = s.getNodes() == null ? 0 : s.getNodes().size();
		
		if (size1 < size2)
			return -1;
		else if (size1 > size2)
			return 1;
		else 
			return 0;
	}
	
}
