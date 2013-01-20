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

import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;

public class GraphPreProcess {

	static Logger logger = Logger.getLogger(GraphPreProcess.class);

	DirectedWeightedMultigraph<Node, Link> graph;
	DirectedWeightedMultigraph<Node, Link> gPrime;
	List<Node> semanticNodes;
	List<Link> selectedLinks;
	List<Node> steinerNodes;
	
	public GraphPreProcess(DirectedWeightedMultigraph<Node, Link> graph, 
			List<Node> semanticNodes, List<Link> selectedLinks) {
		this.graph = graph;
		this.semanticNodes = semanticNodes;
		this.selectedLinks = selectedLinks;
		// copy all semantic nodes into steiner nodes
		this.steinerNodes = new ArrayList<Node>(semanticNodes); 

		createDirectedGPrime();
	}
	
	@SuppressWarnings("unchecked")
	private void createDirectedGPrime() {
		logger.debug("<enter");
		
		gPrime = (DirectedWeightedMultigraph<Node, Link>)this.graph.clone();
		
		Link e;
		LinkStatus status;
		
		if (selectedLinks != null) 
		for (int i = 0; i < selectedLinks.size(); i++) {
			
			e = (Link)selectedLinks.get(i);
			status = e.getLinkStatus();
			
			if (status == LinkStatus.PreferredByUI) {
				gPrime.setEdgeWeight(e, GraphBuilder.DEFAULT_WEIGHT - GraphBuilder.MIN_WEIGHT);
				
			} else if (status == LinkStatus.ForcedByUser) {
				
				e = (Link)selectedLinks.get(i);
				
				Node source = selectedLinks.get(i).getSource();
				Node target = selectedLinks.get(i).getTarget();
				
				if (!steinerNodes.contains(source))
					steinerNodes.add(source);
	
				if (!steinerNodes.contains(target))
					steinerNodes.add(target);
				
				// removing all links to target
				Link[] incomingLinks = gPrime.incomingEdgesOf(target).toArray(new Link[0]); 
				for (Link inLink: incomingLinks) {
					gPrime.removeAllEdges( inLink.getSource(), inLink.getTarget() );
				}
	
				// adding the user selected link
				gPrime.addEdge(source, target, e);
				
				// if it is a subclass link, change the weight to epsilon
				//if (e.getType() == LinkType.HasSubClass)
				gPrime.setEdgeWeight(e, GraphBuilder.MIN_WEIGHT);
				
				if (target.getNodeType() == NodeType.DataProperty)
					target.setDomainVertexId(source.getID());
			}			
		}
		
		// adding the domains of data property nodes to steiner nodes collection
		// It is possible that some data property nodes have multiple incoming links from 
		// different instances of the same class. We only keep the one that comes from its domain instance.
		for (Node v: gPrime.vertexSet()) {
			
			if (v.getNodeType() != NodeType.DataProperty)
				continue;
			
			String domainVertexId = v.getDomainVertexId();
			if (domainVertexId == null)
				continue;

			Link[] incomingLinks = gPrime.incomingEdgesOf(v).toArray(new Link[0]);
			if (incomingLinks != null && incomingLinks.length != 0) {
				
					for (int i = 0; i < incomingLinks.length; i++) {
						if (!incomingLinks[i].getSource().getID().equalsIgnoreCase(domainVertexId)) {
							if (incomingLinks.length > 1)   // only for data property nodes who have links from multiple instances of the same class
								gPrime.removeEdge(incomingLinks[i]); 
						}
						else if (!steinerNodes.contains(incomingLinks[i].getSource()))
							steinerNodes.add(incomingLinks[i].getSource());
				}
			}
		}

		logger.debug("exit>");
//		GraphUtil.printGraph(gPrime);
	}
	
	public List<Node> getSteinerNodes() {
		return this.steinerNodes;
	}
	
	public UndirectedGraph<Node, Link> getUndirectedGraph() {
		
		return  new AsUndirectedGraph<Node, Link>(this.gPrime);
	}
	
}
