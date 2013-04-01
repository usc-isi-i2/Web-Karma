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
package edu.isi.karma.modeling.alignment;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class GraphPreProcess {

	static Logger logger = Logger.getLogger(GraphPreProcess.class);

	DirectedWeightedMultigraph<Node, Link> gPrime;
	List<Link> linksPreferredByUI;
	List<Link> linksForcedByUser;
	
	public GraphPreProcess(DirectedWeightedMultigraph<Node, Link> graph, 
			List<Link> linksPreferredByUI,
			List<Link> linksForcedByUser) {
		
		this.linksPreferredByUI = linksPreferredByUI;
		this.linksForcedByUser = linksForcedByUser;

		this.gPrime = cloneGraph(graph);
		this.updateWeights();
	}
	
	@SuppressWarnings("unchecked")
	private DirectedWeightedMultigraph<Node, Link> cloneGraph(DirectedWeightedMultigraph<Node, Link> graph) {
		gPrime = (DirectedWeightedMultigraph<Node, Link>)graph.clone();
		return gPrime;
	}
	
	private void updateWeights() {
		
		logger.debug("<enter");

		if (linksPreferredByUI != null) 
			for (Link link : linksPreferredByUI) 
				gPrime.setEdgeWeight(link, ModelingParams.PROPERTY_UI_PREFERRED_WEIGHT);
				
		if (linksForcedByUser != null) 
			for (Link link : linksForcedByUser) {
				
//				// removing all the links to target
//				Set<Link> incomingLinks = gPrime.incomingEdgesOf(link.getTarget());
//				if (incomingLinks == null) continue;
//				Link[] incomingLinksArray = incomingLinks.toArray(new Link[0]);
//				for (Link inLink: incomingLinksArray) {
//					gPrime.removeAllEdges( inLink.getSource(), inLink.getTarget() );
//				}
//	
//				// adding the user selected link
//				gPrime.addEdge(link.getSource(), link.getTarget(), link);
				
				gPrime.setEdgeWeight(link, ModelingParams.PROPERTY_USER_PREFERRED_WEIGHT);
			}			

		logger.debug("exit>");
//		GraphUtil.printGraph(gPrime);
	}

	public UndirectedGraph<Node, Link> getUndirectedGraph() {	
		return  new AsUndirectedGraph<Node, Link>(this.gPrime);
	}
	
}
