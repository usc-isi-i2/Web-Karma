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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class GraphPreProcess {

	static Logger logger = Logger.getLogger(GraphPreProcess.class);

	DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph;
	DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> gPrime;
	List<Vertex> semanticNodes;
	List<LabeledWeightedEdge> selectedLinks;
	List<Vertex> steinerNodes;
	
	public GraphPreProcess(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph, 
			List<Vertex> semanticNodes, List<LabeledWeightedEdge> selectedLinks) {
		this.graph = graph;
		this.semanticNodes = semanticNodes;
		this.selectedLinks = selectedLinks;
		// copy all semantic nodes into steiner nodes
		this.steinerNodes = new ArrayList<Vertex>(semanticNodes); 

		createDirectedGPrime();
	}
	
	@SuppressWarnings("unchecked")
	private void createDirectedGPrime() {
		logger.debug("<enter");
		
		gPrime = (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>)this.graph.clone();
		
		LabeledWeightedEdge e;
		LinkStatus status;
		
		if (selectedLinks != null) 
		for (int i = 0; i < selectedLinks.size(); i++) {
			
			e = (LabeledWeightedEdge)selectedLinks.get(i);
			status = e.getLinkStatus();
			
			if (status == LinkStatus.PreferredByUI) {
				gPrime.setEdgeWeight(e, GraphBuilder.DEFAULT_WEIGHT - GraphBuilder.MIN_WEIGHT);
				
			} else if (status == LinkStatus.ForcedByUser) {
				
				e = (LabeledWeightedEdge)selectedLinks.get(i);
				
				Vertex source = selectedLinks.get(i).getSource();
				Vertex target = selectedLinks.get(i).getTarget();
				
				if (!steinerNodes.contains(source))
					steinerNodes.add(source);
	
				if (!steinerNodes.contains(target))
					steinerNodes.add(target);
				
				// removing all links to target
				LabeledWeightedEdge[] incomingLinks = gPrime.incomingEdgesOf(target).toArray(new LabeledWeightedEdge[0]); 
				for (LabeledWeightedEdge inLink: incomingLinks) {
					gPrime.removeAllEdges( inLink.getSource(), inLink.getTarget() );
				}
	
				// adding the user selected link
				gPrime.addEdge(source, target, e);
				
				// if it is a subclass link, change the weight to epsilon
				//if (e.getType() == LinkType.HasSubClass)
				gPrime.setEdgeWeight(e, GraphBuilder.MIN_WEIGHT);
			}			
		}
		
		// It is possible that some data property nodes have multiple incoming links from 
		// different instances of the same class. We only keep the one that comes from its domain instance.
		for (Vertex v: gPrime.vertexSet()) {
			
			if (v.getNodeType() != NodeType.DataProperty)
				continue;
			
			String domainVertexId = v.getDomainVertexId();
			if (domainVertexId == null)
				continue;

			LabeledWeightedEdge[] incomingLinks = gPrime.incomingEdgesOf(v).toArray(new LabeledWeightedEdge[0]);
			if (incomingLinks != null && incomingLinks.length != 0) {
				
				if (incomingLinks.length > 1) {  // only for data property nodes who have links from multiple instances of the same class
					for (int i = 0; i < incomingLinks.length; i++)
						if (!incomingLinks[i].getSource().getID().equalsIgnoreCase(domainVertexId))
							gPrime.removeEdge(incomingLinks[i]);
				}
			}
		}

		logger.debug("exit>");
//		GraphUtil.printGraph(gPrime);
	}
	
	public List<Vertex> getSteinerNodes() {
		return this.steinerNodes;
	}
	
	public UndirectedGraph<Vertex, LabeledWeightedEdge> getUndirectedGraph() {
		
		return  new AsUndirectedGraph<Vertex, LabeledWeightedEdge>(this.gPrime);
	}
	
}
