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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;

public class GraphVizUtil {

	public static org.kohsuke.graphviz.Graph exportJGrapPathToGraphviz(DijkstraShortestPath<Node, Link> path) {

		org.kohsuke.graphviz.Graph gViz = new org.kohsuke.graphviz.Graph();

		if (path == null)
			return gViz;
			
		org.kohsuke.graphviz.Style internalNodeStyle = new org.kohsuke.graphviz.Style();
//		internalNodeStyle.attr("shape", "circle");
		internalNodeStyle.attr("style", "filled");
		internalNodeStyle.attr("color", "white");
		internalNodeStyle.attr("fontsize", "10");
		internalNodeStyle.attr("fillcolor", "lightgray");
		
//		org.kohsuke.graphviz.Style inputNodeStyle = new org.kohsuke.graphviz.Style();
//		inputNodeStyle.attr("shape", "plaintext");
//		inputNodeStyle.attr("style", "filled");
//		inputNodeStyle.attr("fillcolor", "#3CB371");
//
//		org.kohsuke.graphviz.Style outputNodeStyle = new org.kohsuke.graphviz.Style();
//		outputNodeStyle.attr("shape", "plaintext");
//		outputNodeStyle.attr("style", "filled");
//		outputNodeStyle.attr("fillcolor", "gold");

		org.kohsuke.graphviz.Style parameterNodeStyle = new org.kohsuke.graphviz.Style();
		parameterNodeStyle.attr("shape", "plaintext");
		parameterNodeStyle.attr("style", "filled");
		parameterNodeStyle.attr("fillcolor", "gold");

		org.kohsuke.graphviz.Style literalNodeStyle = new org.kohsuke.graphviz.Style();
		literalNodeStyle.attr("shape", "plaintext");
		literalNodeStyle.attr("style", "filled");
		literalNodeStyle.attr("fillcolor", "#CC7799");

		org.kohsuke.graphviz.Style edgeStyle = new org.kohsuke.graphviz.Style();
		edgeStyle.attr("color", "brown");
		edgeStyle.attr("fontsize", "10");
		edgeStyle.attr("fontcolor", "black");
		
		HashMap<Node, org.kohsuke.graphviz.Node> nodeIndex = new HashMap<Node, org.kohsuke.graphviz.Node>();
		
//		Node lastNode = null;
		for (int i = 0; i < path.getPathEdgeList().size(); i++) {
			
			Link e = path.getPathEdgeList().get(i);
			
			Node source = e.getSource();
			Node target = e.getTarget();
			
			org.kohsuke.graphviz.Node n = nodeIndex.get(source);
			String id = source.getId();
			String uri = source.getLabel().getUri();
			if (n == null) {
				n = new org.kohsuke.graphviz.Node();
				n.attr("label", (uri == null || uri.trim().length() == 0?id:uri));
				nodeIndex.put(source, n);
			
//				if (id.indexOf("att") != -1 && id.indexOf("i") != -1) // input
//					gViz.nodeWith(inputNodeStyle);
//				else if (id.indexOf("att") != -1 && id.indexOf("o") != -1)  // output
//					gViz.nodeWith(outputNodeStyle);
				if (source instanceof ColumnNode)  // attribute
					gViz.nodeWith(parameterNodeStyle);
				else if (source instanceof LiteralNode)  // literal
					gViz.nodeWith(literalNodeStyle);
				else  // internal node
					gViz.nodeWith(internalNodeStyle);
					
				gViz.node(n);
			}

			n = nodeIndex.get(target);
			id = target.getId();
			uri = target.getLabel().getUri();
			if (n == null) {
				n = new org.kohsuke.graphviz.Node();
				n.attr("label", (uri == null || uri.trim().length() == 0?id:uri));
				nodeIndex.put(target, n);
			
//				if (id.indexOf("att") != -1 && id.indexOf("i") != -1) // input
//					gViz.nodeWith(inputNodeStyle);
//				else if (id.indexOf("att") != -1 && id.indexOf("o") != -1)  // output
//					gViz.nodeWith(outputNodeStyle);
				if (target instanceof ColumnNode)  // attribute
					gViz.nodeWith(parameterNodeStyle);
				else if (target instanceof LiteralNode)  // literal
					gViz.nodeWith(literalNodeStyle);
				else  // internal node
					gViz.nodeWith(internalNodeStyle);
					
				gViz.node(n);
			}
			
			/*
			org.kohsuke.graphviz.Edge edge = null;
			if (i == 0) {
				edge = new org.kohsuke.graphviz.Edge(nodeIndex.get(source), nodeIndex.get(target));
				lastNode = target;
				if (path.getPathEdgeList().size() > 1) {
					Link nextEdge = path.getPathEdgeList().get(1);
					if (source.equals(nextEdge.getSource()) ||
							source.equals(nextEdge.getTarget())) {
						edge = new org.kohsuke.graphviz.Edge(nodeIndex.get(target), nodeIndex.get(source));
						lastNode = source;					}
				}
			} else if (target.equals(lastNode)) {
				edge = new org.kohsuke.graphviz.Edge(nodeIndex.get(target), nodeIndex.get(source));
				lastNode = source;
			} else if (source.equals(lastNode)) {
				edge = new org.kohsuke.graphviz.Edge(nodeIndex.get(source), nodeIndex.get(target));
				lastNode = target;
			} 
			*/
			
			id = e.getId();
			uri = e.getLabel().getUri();
			org.kohsuke.graphviz.Edge edge = new org.kohsuke.graphviz.Edge(nodeIndex.get(source), nodeIndex.get(target));
			edge.attr("label", (uri == null?id:uri));
			gViz.edgeWith(edgeStyle);
			gViz.edge(edge);
		}


		return gViz;
	}
	
	private static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
	}
	
	public static String getLocalName(String uri) {

		if (uri == null)
			return "";
		
		String localName = uri;
		
		if (uri.contains("#") && !uri.endsWith("#"))
			localName = uri.substring(uri.lastIndexOf('#') + 1);
		else {
			if (uri.endsWith("/"))
				uri = uri.substring(0, uri.length() - 2);
			if (uri.contains("/"))
				localName = uri.substring(uri.lastIndexOf('/') + 1);
		}
		return localName;
	}
	
	private static String getPatterns(Set<String> patternIds) {
		String label = "";
		if (patternIds == null || patternIds.size() == 0)
			return label;
		label += "[";
		for (String pId : patternIds)
			label += pId + ",";
		if (label.endsWith(","))
			label = label.substring(0, label.length() - 1);
		label += "]";
		return label;
	}
	
	public static org.kohsuke.graphviz.Graph exportJGraphToGraphviz(DirectedWeightedMultigraph<Node, Link> model, boolean showDescription) {

		org.kohsuke.graphviz.Graph gViz = new org.kohsuke.graphviz.Graph();

		if (model == null)
			return gViz;

		org.kohsuke.graphviz.Style internalNodeStyle = new org.kohsuke.graphviz.Style();
//		internalNodeStyle.attr("shape", "circle");
		internalNodeStyle.attr("style", "filled");
		internalNodeStyle.attr("color", "white");
		internalNodeStyle.attr("fontsize", "10");
		internalNodeStyle.attr("fillcolor", "lightgray");
		
//		org.kohsuke.graphviz.Style inputNodeStyle = new org.kohsuke.graphviz.Style();
//		inputNodeStyle.attr("shape", "plaintext");
//		inputNodeStyle.attr("style", "filled");
//		inputNodeStyle.attr("fillcolor", "#3CB371");
//
//		org.kohsuke.graphviz.Style outputNodeStyle = new org.kohsuke.graphviz.Style();
//		outputNodeStyle.attr("shape", "plaintext");
//		outputNodeStyle.attr("style", "filled");
//		outputNodeStyle.attr("fillcolor", "gold");

		org.kohsuke.graphviz.Style parameterNodeStyle = new org.kohsuke.graphviz.Style();
		parameterNodeStyle.attr("shape", "plaintext");
		parameterNodeStyle.attr("style", "filled");
		parameterNodeStyle.attr("fillcolor", "gold");

		org.kohsuke.graphviz.Style literalNodeStyle = new org.kohsuke.graphviz.Style();
		literalNodeStyle.attr("shape", "plaintext");
		literalNodeStyle.attr("style", "filled");
		literalNodeStyle.attr("fillcolor", "#CC7799");

		org.kohsuke.graphviz.Style edgeStyle = new org.kohsuke.graphviz.Style();
		edgeStyle.attr("color", "brown");
		edgeStyle.attr("fontsize", "10");
		edgeStyle.attr("fontcolor", "black");
		
		HashMap<Node, org.kohsuke.graphviz.Node> nodeIndex = new HashMap<Node, org.kohsuke.graphviz.Node>();
		
		for (Link e : model.edgeSet()) {
			
			Node source = e.getSource();
			Node target = e.getTarget();
			
			org.kohsuke.graphviz.Node n = nodeIndex.get(source);
			String sourceId = source.getId();
			String sourceUri = sourceId;//source.getLabel().getUri();
			String sourceLocalName = getLocalName(sourceUri);
			String sourceLabel;
			if (n == null) {
				n = new org.kohsuke.graphviz.Node();
//				label = (uri == null || uri.trim().length() == 0?id:uri));
				sourceLabel = (sourceLocalName == null || sourceLocalName.trim().length() == 0?sourceId:sourceLocalName);
				if (showDescription) sourceLabel += " " + getPatterns(source.getModelIds()); 
				n.attr("label", sourceLabel);
				nodeIndex.put(source, n);
			
//				if (id.indexOf("att") != -1 && id.indexOf("i") != -1) // input
//					gViz.nodeWith(inputNodeStyle);
//				else if (id.indexOf("att") != -1 && id.indexOf("o") != -1)  // output
//					gViz.nodeWith(outputNodeStyle);
				if (source instanceof ColumnNode)  // attribute
					gViz.nodeWith(parameterNodeStyle);
				else if (source instanceof LiteralNode)  // literal
					gViz.nodeWith(literalNodeStyle);
				else  // internal node
					gViz.nodeWith(internalNodeStyle);
					
				gViz.node(n);
			}

			n = nodeIndex.get(target);
			String targetId = target.getId();
			String targetUri = targetId;//target.getLabel().getUri();
			String targetLocalName = getLocalName(targetUri);
			String targetLabel;
			if (n == null) {
				n = new org.kohsuke.graphviz.Node();
//				label = (uri == null || uri.trim().length() == 0?id:uri));
				targetLabel = (targetLocalName == null || targetLocalName.trim().length() == 0?targetId:targetLocalName);
				if (target instanceof ColumnNode) targetLabel = ((ColumnNode)target).getColumnName();
				if (showDescription) targetLabel += " " + getPatterns(target.getModelIds()); 
				n.attr("label", targetLabel);
				nodeIndex.put(target, n);
			
//				if (id.indexOf("att") != -1 && id.indexOf("i") != -1) // input
//					gViz.nodeWith(inputNodeStyle);
//				else if (id.indexOf("att") != -1 && id.indexOf("o") != -1)  // output
//					gViz.nodeWith(outputNodeStyle);
				if (target instanceof ColumnNode)  // attribute
					gViz.nodeWith(parameterNodeStyle);
				else if (target instanceof LiteralNode)  // literal
					gViz.nodeWith(literalNodeStyle);
				else  // internal node
					gViz.nodeWith(internalNodeStyle);
					
				gViz.node(n);
			}
			
			org.kohsuke.graphviz.Edge edge = new org.kohsuke.graphviz.Edge(nodeIndex.get(source), nodeIndex.get(target));
			
			String edgeId = e.getId();
			String edgetUri = e.getLabel().getUri();
			String edgeLocalName = getLocalName(edgetUri);
			String edgeLabel = (edgeLocalName == null?edgeId:edgeLocalName);

			if (showDescription) {
				edgeLabel += "-(" + roundTwoDecimals(e.getWeight()) + ")-";
				edgeLabel += " ";
				edgeLabel += getPatterns(e.getModelIds());
			}

			edge.attr("label", edgeLabel);
			gViz.edgeWith(edgeStyle);
			gViz.edge(edge);
		}


		return gViz;
	}
	
	public static void exportJGraphToGraphvizFile(
			DirectedWeightedMultigraph<Node, Link> model, String label, String exportPath) 
					throws FileNotFoundException {
		org.kohsuke.graphviz.Graph graphViz = exportJGraphToGraphviz(model, true);
		graphViz.attr("fontcolor", "blue");
		graphViz.attr("remincross", "true");
		graphViz.attr("label", label);

		OutputStream out = new FileOutputStream(exportPath);
		graphViz.writeTo(out);
	}

	public static void exportJGraphToGraphvizFile(Map<String, DirectedWeightedMultigraph<Node, Link>> models, 
			String label, String exportPath) throws FileNotFoundException {
		
		org.kohsuke.graphviz.Graph graphViz = new org.kohsuke.graphviz.Graph();
		graphViz.attr("fontcolor", "blue");
		graphViz.attr("remincross", "true");
		graphViz.attr("label", label);
		
		org.kohsuke.graphviz.Graph cluster = null;
		int counter = 0;
		
		boolean showLinkDescription;
		if (models != null) {
			for(Entry<String,DirectedWeightedMultigraph<Node, Link>> entry : models.entrySet()) {
				if (entry.getKey() == "1-correct model") showLinkDescription = false; else showLinkDescription = true;
				cluster = GraphVizUtil.exportJGraphToGraphviz(entry.getValue(), showLinkDescription);
				cluster.id("cluster_" + counter);
				cluster.attr("label", entry.getKey());
				graphViz.subGraph(cluster);
				counter++;
			}
		}

		OutputStream out = new FileOutputStream(exportPath);
		graphViz.writeTo(out);

	}
	
	
}
