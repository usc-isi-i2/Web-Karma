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

import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;


public class GraphPreProcess {

	static Logger logger = LoggerFactory.getLogger(GraphPreProcess.class);

	DirectedWeightedMultigraph<Node, DefaultLink> gPrime;
	Set<LabeledLink> linksPreferredByUI;
	Set<LabeledLink> linksForcedByUser;
	
	public GraphPreProcess(DirectedWeightedMultigraph<Node, DefaultLink> graph, 
			Set<LabeledLink> linksPreferredByUI,
			Set<LabeledLink> linksForcedByUser) {
		
		this.linksPreferredByUI = linksPreferredByUI;
		this.linksForcedByUser = linksForcedByUser;

		this.gPrime = cloneGraph(graph);
		this.updateWeights();
	}
	
	@SuppressWarnings("unchecked")
	private DirectedWeightedMultigraph<Node, DefaultLink> cloneGraph(DirectedWeightedMultigraph<Node, DefaultLink> graph) {
		gPrime = (DirectedWeightedMultigraph<Node, DefaultLink>)graph.clone();
		return gPrime;
	}
	
	private void updateWeights() {
		
		logger.debug("<enter");

		if (linksPreferredByUI != null) 
			for (LabeledLink link : linksPreferredByUI) 
				gPrime.setEdgeWeight(link, ModelingParams.PROPERTY_UI_PREFERRED_WEIGHT);
				
		if (linksForcedByUser != null) 
			for (LabeledLink link : linksForcedByUser) {
				
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

	public UndirectedGraph<Node, DefaultLink> getUndirectedGraph() {	
		return new AsUndirectedGraph<>(this.gPrime);
	}
	
}
