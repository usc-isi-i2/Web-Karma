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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class Util {

	
	public static List<Node> getAttributes(DirectedWeightedMultigraph<Node, Link> graph) {
		List<Node> attributes = new ArrayList<Node>();
		for (Node n : graph.vertexSet()) {
			if (!n.getId().startsWith(ModelReader.attPrefix)) continue;
			attributes.add(n);
		}
		Collections.sort(attributes);
		return attributes;
	}
	
}
