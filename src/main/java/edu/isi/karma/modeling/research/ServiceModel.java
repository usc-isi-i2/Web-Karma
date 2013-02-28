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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.kohsuke.graphviz.Edge;
import org.kohsuke.graphviz.Graph;
import org.kohsuke.graphviz.Node;
import org.kohsuke.graphviz.Style;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.alignment.Vertex;
import edu.isi.karma.modeling.alignment.VertexComparatorByID;

public class ServiceModel {

//	private static String varPrefix = "var:";
	private static String attPrefix = "att:";
	
	private String serviceNameWithPrefix;
	private String serviceName;
	private String serviceDescription;
	
	List<DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge>> models;
	HashMap<String, List<DijkstraShortestPath<Vertex, LabeledWeightedEdge>>> shortestPathsBetweenTwoAttributes; 

	public ServiceModel() {
		this.models = new ArrayList<DirectedWeightedMultigraph<Vertex,LabeledWeightedEdge>>();
		shortestPathsBetweenTwoAttributes = new HashMap<String, List<DijkstraShortestPath<Vertex,LabeledWeightedEdge>>>();
	}

	
	public String getServiceName() {
		return serviceName;
	}


	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}


	public String getServiceNameWithPrefix() {
		return serviceNameWithPrefix;
	}


	public void setServiceNameWithPrefix(String serviceNameWithPrefix) {
		this.serviceNameWithPrefix = serviceNameWithPrefix;
	}


	public String getServiceDescription() {
		return serviceDescription;
	}


	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}


	public List<DirectedWeightedMultigraph<Vertex,LabeledWeightedEdge>> getModels() {
		return models;
	}
	
	public void addModel(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph) {
		this.models.add(graph);
	}
	
	public void print() {
		System.out.println(this.getServiceName());
		System.out.println();
		for (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> g : this.getModels())
				GraphUtil.printGraphSimple(g);
		System.out.println();
		
		List<String> sortedKeys = Arrays.asList(shortestPathsBetweenTwoAttributes.keySet().toArray(new String[0]));
		Collections.sort(sortedKeys);
		
		String lastNodeId = "", nextId = "", currentId = "";
		for (String index : sortedKeys) {

			System.out.println(index + ": ");
			for (DijkstraShortestPath<Vertex,LabeledWeightedEdge> path : shortestPathsBetweenTwoAttributes.get(index)) {
				
				List<LabeledWeightedEdge> pathEdges = path.getPathEdgeList();
				lastNodeId = ""; nextId = ""; currentId = "";
				if (pathEdges == null)
					continue;
				for (int i = 0; i < pathEdges.size(); i++) {
					
					LabeledWeightedEdge e = pathEdges.get(i);
					
					if (i == 0) {
						currentId = e.getSource().getID();
						nextId = e.getTarget().getID();
						if (pathEdges.size() > 1) {
							LabeledWeightedEdge nextEdge = pathEdges.get(1);
							if (e.getSource().getID().equalsIgnoreCase(nextEdge.getSource().getID()) ||
									e.getSource().getID().equalsIgnoreCase(nextEdge.getTarget().getID())) {
								currentId = e.getTarget().getID();
								nextId = e.getSource().getID();
							}
						}
						System.out.print("\t");
						System.out.print("(");
						System.out.print(currentId);
						System.out.print(")");
					} else if (e.getSource().getID().equalsIgnoreCase(lastNodeId)) {
						nextId = e.getTarget().getID();
					} else if (e.getTarget().getID().equalsIgnoreCase(lastNodeId)) {
						nextId = e.getSource().getID();
					}
					lastNodeId = nextId;
					System.out.print("---");
					System.out.print(e.getID());
					System.out.print("---");
					System.out.print("(");
					System.out.print(nextId);
					System.out.print(")");
				}

			}
		}
		System.out.println();
		System.out.println();
	}
	
	
	private List<Vertex> getAttributes(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph) {
		List<Vertex> attributes = new ArrayList<Vertex>();
		for (Vertex v : graph.vertexSet()) {
			if (!v.getID().startsWith(attPrefix)) continue;
			attributes.add(v);
		}
		Collections.sort(attributes, new VertexComparatorByID());
		return attributes;
	}

	public void computeShortestPaths() {
		
		int modelNo = 1;
		DijkstraShortestPath<Vertex, LabeledWeightedEdge> path;
		
		for (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> graph : this.models) {
			
			List<Vertex> attributes = getAttributes(graph);
			for (int i = 0; i < attributes.size(); i++) {
				for (int j = i+1; j < attributes.size(); j++) {
					
					Vertex source = attributes.get(i);
					Vertex target = attributes.get(j);
					String index = source.getID().replaceAll(attPrefix, "") + 
									"-->" + 
									target.getID().replaceAll(attPrefix, "") + 
									" (m" + modelNo + ")";
					
					// TODO: How to get all the shortest paths?
					UndirectedGraph<Vertex, LabeledWeightedEdge> undirectedGraph = 
							new AsUndirectedGraph<Vertex, LabeledWeightedEdge>(graph);	
					
					path = new DijkstraShortestPath<Vertex, LabeledWeightedEdge>(undirectedGraph, source, target);
					
					List<DijkstraShortestPath<Vertex,LabeledWeightedEdge>> paths = 
							this.shortestPathsBetweenTwoAttributes.get(index);
					
					if (paths == null) {
						paths = new ArrayList<DijkstraShortestPath<Vertex,LabeledWeightedEdge>>();
						this.shortestPathsBetweenTwoAttributes.put(index, paths);
					}
					
					paths.add(path);
				}
			}
			
			modelNo ++;
		}
	}
	
	public void exportToGraphviz(String exportDirectory) throws FileNotFoundException {
		
		OutputStream out = new FileOutputStream(exportDirectory + this.getServiceNameWithPrefix() + ".dot");
		Graph graphViz = new Graph();
		
		graphViz.attr("fontcolor", "blue");
		graphViz.attr("remincross", "true");
		graphViz.attr("label", this.getServiceDescription());
//		graphViz.attr("page", "8.5,11");

		int modelNo = 1;
		for (DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> model : this.models) {
			Graph gViz = exportJGraphToGraphviz(model);
			gViz.attr("label", "model_" + modelNo);
			gViz.id("cluster_" + modelNo);
			graphViz.subGraph(gViz);
			modelNo ++;
		}
		graphViz.writeTo(out);


	}
	
	public void exportPathsToGraphviz(String exportDirectory) throws FileNotFoundException {
		
		OutputStream out = new FileOutputStream(exportDirectory + this.getServiceNameWithPrefix() + "_paths.dot");
		Graph graphViz = new Graph();
		graphViz.attr("fontcolor", "blue");
		graphViz.attr("remincross", "true");
		graphViz.attr("label", this.getServiceDescription());
//		graphViz.attr("page", "8.5,11");
		
		List<String> sortedKeys = Arrays.asList(shortestPathsBetweenTwoAttributes.keySet().toArray(new String[0]));
		Collections.sort(sortedKeys);

		Graph cluster = null;
		int counter = 0;
		for (String index : sortedKeys) {

			if (counter % 2 == 0) { 
				cluster = new Graph();
				cluster.id("cluster_" + counter);
				cluster.attr("label", index.substring(0, index.indexOf("(")).trim());
				graphViz.subGraph(cluster);
			}

			Graph gViz = exportJGrapPathToGraphviz(this.shortestPathsBetweenTwoAttributes.get(index).get(0));
			gViz.attr("label", index.substring(index.indexOf("(") + 1, index.indexOf(")")) );
			gViz.id("model_" + (counter % 2 + 1) );
			cluster.subGraph(gViz);
			counter ++;
			
		}
		graphViz.writeTo(out);

	}
	
	private Graph exportJGrapPathToGraphviz(DijkstraShortestPath<Vertex, LabeledWeightedEdge> path) {

		Graph gViz = new Graph();
			
		Style internalNodeStyle = new Style();
//		internalNodeStyle.attr("shape", "circle");
		internalNodeStyle.attr("style", "filled");
		internalNodeStyle.attr("color", "white");
		internalNodeStyle.attr("fontsize", "10");
		internalNodeStyle.attr("fillcolor", "lightgray");
		
		Style inputNodeStyle = new Style();
		inputNodeStyle.attr("shape", "plaintext");
		inputNodeStyle.attr("style", "filled");
		inputNodeStyle.attr("fillcolor", "#3CB371");

		Style outputNodeStyle = new Style();
		outputNodeStyle.attr("shape", "plaintext");
		outputNodeStyle.attr("style", "filled");
		outputNodeStyle.attr("fillcolor", "gold");

		Style literalNodeStyle = new Style();
		literalNodeStyle.attr("shape", "plaintext");
		literalNodeStyle.attr("style", "filled");
		literalNodeStyle.attr("fillcolor", "#CC7799");

		Style edgeStyle = new Style();
		edgeStyle.attr("color", "brown");
		edgeStyle.attr("fontsize", "10");
		edgeStyle.attr("fontcolor", "black");
		
		HashMap<Vertex, Node> nodeIndex = new HashMap<Vertex, Node>();
		
		Vertex lastNode = null;
		for (int i = 0; i < path.getPathEdgeList().size(); i++) {
			
			LabeledWeightedEdge e = path.getPathEdgeList().get(i);
			
			Vertex source = e.getSource();
			Vertex target = e.getTarget();
			
			Node n = nodeIndex.get(source);
			String id = source.getID();
			if (n == null) {
				n = new Node();
				n.attr("label", id);
				nodeIndex.put(source, n);
			
				if (id.indexOf("att") != -1 && id.indexOf("i") != -1) // input
					gViz.nodeWith(inputNodeStyle);
				else if (id.indexOf("att") != -1 && id.indexOf("o") != -1)  // output
					gViz.nodeWith(outputNodeStyle);
				else if (id.indexOf("att") == -1 && id.indexOf(":") == -1 && id.indexOf("\"") != -1)  // literal
					gViz.nodeWith(literalNodeStyle);
				else  // internal node
					gViz.nodeWith(internalNodeStyle);
					
				gViz.node(n);
			}

			n = nodeIndex.get(target);
			id = target.getID();
			if (n == null) {
				n = new Node();
				n.attr("label", id);
				nodeIndex.put(target, n);
			
				if (id.indexOf("att") != -1 && id.indexOf("i") != -1) // input
					gViz.nodeWith(inputNodeStyle);
				else if (id.indexOf("att") != -1 && id.indexOf("o") != -1)  // output
					gViz.nodeWith(outputNodeStyle);
				else if (id.indexOf("att") == -1 && id.indexOf(":") == -1 && id.indexOf("\"") != -1)  // literal
					gViz.nodeWith(literalNodeStyle);
				else  // internal node
					gViz.nodeWith(internalNodeStyle);
					
				gViz.node(n);
			}
			
//			/*
			Edge edge = null;
			if (i == 0) {
				edge = new Edge(nodeIndex.get(source), nodeIndex.get(target));
				lastNode = target;
				if (path.getPathEdgeList().size() > 1) {
					LabeledWeightedEdge nextEdge = path.getPathEdgeList().get(1);
					if (source.equals(nextEdge.getSource()) ||
							source.equals(nextEdge.getTarget())) {
						edge = new Edge(nodeIndex.get(target), nodeIndex.get(source));
						lastNode = source;					}
				}
			} else if (target.equals(lastNode)) {
				edge = new Edge(nodeIndex.get(target), nodeIndex.get(source));
				lastNode = source;
			} else if (source.equals(lastNode)) {
				edge = new Edge(nodeIndex.get(source), nodeIndex.get(target));
				lastNode = target;
			} 
//			*/
			
//			Edge edge = new Edge(nodeIndex.get(source), nodeIndex.get(target));
			edge.attr("label", e.getID());
			gViz.edgeWith(edgeStyle);
			gViz.edge(edge);
		}


		return gViz;
	}
	
	private Graph exportJGraphToGraphviz(DirectedWeightedMultigraph<Vertex, LabeledWeightedEdge> model) {

		Graph gViz = new Graph();
			
		Style internalNodeStyle = new Style();
//		internalNodeStyle.attr("shape", "circle");
		internalNodeStyle.attr("style", "filled");
		internalNodeStyle.attr("color", "white");
		internalNodeStyle.attr("fontsize", "10");
		internalNodeStyle.attr("fillcolor", "lightgray");
		
		Style inputNodeStyle = new Style();
		inputNodeStyle.attr("shape", "plaintext");
		inputNodeStyle.attr("style", "filled");
		inputNodeStyle.attr("fillcolor", "#3CB371");

		Style outputNodeStyle = new Style();
		outputNodeStyle.attr("shape", "plaintext");
		outputNodeStyle.attr("style", "filled");
		outputNodeStyle.attr("fillcolor", "gold");

		Style literalNodeStyle = new Style();
		literalNodeStyle.attr("shape", "plaintext");
		literalNodeStyle.attr("style", "filled");
		literalNodeStyle.attr("fillcolor", "#CC7799");

		Style edgeStyle = new Style();
		edgeStyle.attr("color", "brown");
		edgeStyle.attr("fontsize", "10");
		edgeStyle.attr("fontcolor", "black");
		
		HashMap<Vertex, Node> nodeIndex = new HashMap<Vertex, Node>();
		
		for (LabeledWeightedEdge e : model.edgeSet()) {
			
			Vertex source = e.getSource();
			Vertex target = e.getTarget();
			
			Node n = nodeIndex.get(source);
			String id = source.getID();
			if (n == null) {
				n = new Node();
				n.attr("label", id);
				nodeIndex.put(source, n);
			
				if (id.indexOf("att") != -1 && id.indexOf("i") != -1) // input
					gViz.nodeWith(inputNodeStyle);
				else if (id.indexOf("att") != -1 && id.indexOf("o") != -1)  // output
					gViz.nodeWith(outputNodeStyle);
				else if (id.indexOf("att") == -1 && id.indexOf(":") == -1 && id.indexOf("\"") != -1)  // literal
					gViz.nodeWith(literalNodeStyle);
				else  // internal node
					gViz.nodeWith(internalNodeStyle);
					
				gViz.node(n);
			}

			n = nodeIndex.get(target);
			id = target.getID();
			if (n == null) {
				n = new Node();
				n.attr("label", id);
				nodeIndex.put(target, n);
			
				if (id.indexOf("att") != -1 && id.indexOf("i") != -1) // input
					gViz.nodeWith(inputNodeStyle);
				else if (id.indexOf("att") != -1 && id.indexOf("o") != -1)  // output
					gViz.nodeWith(outputNodeStyle);
				else if (id.indexOf("att") == -1 && id.indexOf(":") == -1 && id.indexOf("\"") != -1)  // literal
					gViz.nodeWith(literalNodeStyle);
				else  // internal node
					gViz.nodeWith(internalNodeStyle);
					
				gViz.node(n);
			}
			
			Edge edge = new Edge(nodeIndex.get(source), nodeIndex.get(target));
			edge.attr("label", e.getID());
			gViz.edgeWith(edgeStyle);
			gViz.edge(edge);
		}


		return gViz;
	}
	
	
}
