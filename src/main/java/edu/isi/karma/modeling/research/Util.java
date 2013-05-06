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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.research.graph.roek.nlpged.algorithm.GraphEditDistance;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;

public class Util {

	
	public static List<Node> getAttributes(DirectedWeightedMultigraph<Node, Link> graph) {
		List<Node> attributes = new ArrayList<Node>();
		for (Node n : graph.vertexSet()) {
			if (n instanceof ColumnNode || n instanceof LiteralNode)
				attributes.add(n);
		}
		Collections.sort(attributes);
		return attributes;
	}

	public static edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Graph toRoekGraph(
			DirectedWeightedMultigraph<Node, Link> g) {
		
		edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Graph graph = 
				new edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Graph();
		
		String label;
		Node domain;
		Link link;

		HashMap<Node, edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Node> nodeMap = 
				new HashMap<Node, edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Node>();
		
		for (Node n : g.vertexSet()) {
			if (n instanceof InternalNode)
				label = n.getLabel().getUri();
			else {
				Set<Link> incomingLinks = g.incomingEdgesOf(n);
				link = incomingLinks.toArray(new Link[0])[0];
				domain = link.getSource();
				label = domain.getLabel().getUri() + link.getLabel().getUri();
			}
			
			edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Node node = 
					new edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Node("", label);
			
			nodeMap.put(n, node);
			graph.addNode(node);
		}
		
		for (Link e : g.edgeSet()) {
			edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Edge edge = 
					new edu.isi.karma.modeling.research.graph.roek.nlpged.graph.Edge("", 
							nodeMap.get(e.getSource()), nodeMap.get(e.getTarget()), 
							e.getLabel().getUri());
			graph.addEdge(edge);
		}
		
		return graph;
	}
	
	public static double getDistance(DirectedWeightedMultigraph<Node, Link> g1, 
			DirectedWeightedMultigraph<Node, Link> g2) {
		GraphEditDistance ged = new GraphEditDistance(toRoekGraph(g1), toRoekGraph(g2));
		return ged.getDistance();
	}

	public static String toGxl(DirectedWeightedMultigraph<Node, Link> graph) {
		
		String s = "";
		int nodeCount = 1;
		
		s += "<?xml version=\"1.0\"?>"; s+= "\n";
		s += "<gxl>"; s+= "\n";
		s += "<graph edgeids=\"false\" edgemode=\"undirected\">"; s += "\n";
		
		HashMap<Node, Integer> nodeToIds = new HashMap<Node, Integer>();
		Node domain;
		Link link;
		
		for (Node n : graph.vertexSet()) {
			nodeToIds.put(n, nodeCount);
			if (n instanceof InternalNode)
				s += "<node id=\"" + nodeCount + 
						"\"><attr name=\"label\"><string>" + 
						n.getLabel().getUri() + "</string></attr></node>";
			else {
				Set<Link> incomingLinks = graph.incomingEdgesOf(n);
				if (incomingLinks != null && incomingLinks.size() > 0) { 
					link = incomingLinks.toArray(new Link[0])[0];
					domain = link.getSource();
					s += "<node id=\"" + nodeCount + 
							"\"><attr name=\"label\"><string>" + 
							domain.getLabel().getUri() + link.getLabel().getUri() + 
							"</string></attr></node>";
				}
			}
			s += "\n";
			nodeCount ++;
		}
		
		for (Link e : graph.edgeSet()) {
			s += "<edge from=\"" + nodeToIds.get(e.getSource()).intValue() +
					"\" to=\"" + nodeToIds.get(e.getTarget()).intValue() + 
					"\"><attr name=\"label\"><string>" + 
					e.getLabel().getUri() + "</string></attr></edge>";
			s += "\n";
		}
		
		s += "</graph>"; s+= "\n";
		s += "</gxl>"; s+= "\n";
		
		return s;
	}

	
}
