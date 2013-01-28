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

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class MatchedSubGraphs {

	private DirectedWeightedMultigraph<Node, Link> subGraph1;
	private DirectedWeightedMultigraph<Node, Link> subGraph2;
	
	public DirectedWeightedMultigraph<Node, Link> getSubGraph1() {
		return subGraph1;
	}
	public DirectedWeightedMultigraph<Node, Link> getSubGraph2() {
		return subGraph2;
	}

	public MatchedSubGraphs(DirectedWeightedMultigraph<Node, Link> subGraph1, 
			DirectedWeightedMultigraph<Node, Link> subGraph2) {
		this.subGraph1 = subGraph1;
		this.subGraph2 = subGraph2;
	}
	
}
