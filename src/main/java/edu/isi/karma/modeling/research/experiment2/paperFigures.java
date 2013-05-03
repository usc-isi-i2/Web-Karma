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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.HashMap;

import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SubClassLink;

public class paperFigures {

	private static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
	}
	
	public static org.kohsuke.graphviz.Graph exportJGraphToGraphviz(DirectedWeightedMultigraph<Node, Link> model) {

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
		parameterNodeStyle.attr("fontsize", "10");
//		parameterNodeStyle.attr("style", "filled");
//		parameterNodeStyle.attr("fillcolor", "gold");

		org.kohsuke.graphviz.Style literalNodeStyle = new org.kohsuke.graphviz.Style();
		literalNodeStyle.attr("shape", "plaintext");
		literalNodeStyle.attr("style", "filled");
		literalNodeStyle.attr("fillcolor", "#CC7799");

		org.kohsuke.graphviz.Style objectPropertyLinkStyle = new org.kohsuke.graphviz.Style();
		objectPropertyLinkStyle.attr("color", "black");
		objectPropertyLinkStyle.attr("fontsize", "10");
		objectPropertyLinkStyle.attr("fontcolor", "blue");
		
		org.kohsuke.graphviz.Style dataPropertyLinkStyle = new org.kohsuke.graphviz.Style();
		dataPropertyLinkStyle.attr("color", "brown");
		dataPropertyLinkStyle.attr("fontsize", "10");
		dataPropertyLinkStyle.attr("fontcolor", "black");

		org.kohsuke.graphviz.Style subClassLinkStyle = new org.kohsuke.graphviz.Style();
		subClassLinkStyle.attr("arrowhead", "empty");
		subClassLinkStyle.attr("color", "black:black");
		subClassLinkStyle.attr("fontsize", "10");
		subClassLinkStyle.attr("fontcolor", "black");

		HashMap<Node, org.kohsuke.graphviz.Node> nodeIndex = new HashMap<Node, org.kohsuke.graphviz.Node>();
		
		for (Link e : model.edgeSet()) {
			
			Node source = e.getSource();
			Node target = e.getTarget();
			
			org.kohsuke.graphviz.Node n = nodeIndex.get(source);
			String id = source.getId();
			String uri = source.getLabel().getUri();
			String label = (uri == null?id:uri);
			if (source instanceof ColumnNode) label = "";// ((ColumnNode)source).getColumnName();
			if (n == null) {
				n = new org.kohsuke.graphviz.Node();
				n.attr("label", label);
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
			label = (uri == null?id:uri);
			if (target instanceof ColumnNode) label = "";//((ColumnNode)target).getColumnName();
			if (n == null) {
				n = new org.kohsuke.graphviz.Node();
				n.attr("label", label);
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
			
			id = e.getId();
			uri = e.getLabel().getUri();
			org.kohsuke.graphviz.Edge edge = new org.kohsuke.graphviz.Edge(nodeIndex.get(source), nodeIndex.get(target));
			
			label = (uri == null?id:uri);
			if (e instanceof SubClassLink)
				label = "";//"subClassOf";
			edge.attr("label", label);
			
			if (e instanceof SubClassLink)
				gViz.edgeWith(subClassLinkStyle);
			else if (e instanceof DataPropertyLink)
				gViz.edgeWith(dataPropertyLinkStyle);
			else
				gViz.edgeWith(objectPropertyLinkStyle);

			gViz.edge(edge);
		}


		return gViz;
	}
	
	public static void exportJGraphToGraphvizFile(
			DirectedWeightedMultigraph<Node, Link> model, String label, String exportPath) 
					throws FileNotFoundException {
		org.kohsuke.graphviz.Graph graphViz = exportJGraphToGraphviz(model);
		graphViz.attr("fontcolor", "blue");
		graphViz.attr("remincross", "true");
		graphViz.attr("label", label);

		OutputStream out = new FileOutputStream(exportPath);
		graphViz.writeTo(out);
	}
	
	private static DirectedWeightedMultigraph<Node, Link> buildOntology() {
		DirectedWeightedMultigraph<Node, Link> graph = 
				new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		InternalNode node_person = new InternalNode("n1", new Label("Person"));
		InternalNode node_place = new InternalNode("n2", new Label("Place"));
		InternalNode node_city = new InternalNode("n3", new Label("City"));
		InternalNode node_state = new InternalNode("n4", new Label("State"));
		InternalNode node_country = new InternalNode("n5", new Label("Country"));
		InternalNode node_organization = new InternalNode("n6", new Label("Organization"));
		
		ColumnNode node_personName = new ColumnNode("n6", "n6", "xsd:string", null);
		ColumnNode node_birthDate = new ColumnNode("n7", "n7", "xsd:dateTime", null);
		ColumnNode node_placeName = new ColumnNode("n8", "n8", "xsd:string", null);
//		ColumnNode node_latitude = new ColumnNode("n9", "n9", "xsd:double", null);
//		ColumnNode node_longitude = new ColumnNode("n10", "n10", "xsd:double", null);
//		ColumnNode node_countryCode = new ColumnNode("n11", "n11", "xsd:string", null);
		ColumnNode node_title = new ColumnNode("n12", "n12", "xsd:string", null);
		ColumnNode node_phone = new ColumnNode("n13", "n13", "xsd:string", null);
		ColumnNode node_email = new ColumnNode("n14", "n14", "xsd:string", null);
		ColumnNode node_postalcode = new ColumnNode("n15", "n15", "xsd:string", null);
		
		
		ObjectPropertyLink link_livesIn = new ObjectPropertyLink("e1", new Label("livesIn"));
		ObjectPropertyLink link_bornIn = new ObjectPropertyLink("e2", new Label("bornIn"));
		ObjectPropertyLink link_worksFor = new ObjectPropertyLink("e3", new Label("worksFor"));
		ObjectPropertyLink link_manager = new ObjectPropertyLink("e4", new Label("manager"));
		ObjectPropertyLink link_location = new ObjectPropertyLink("e5", new Label("location"));
		ObjectPropertyLink link_nearby = new ObjectPropertyLink("e6", new Label("nearby"));
		ObjectPropertyLink link_isPartOf = new ObjectPropertyLink("e7", new Label("isPartOf"));
		ObjectPropertyLink link_inState = new ObjectPropertyLink("e8", new Label("state"));
		
		DataPropertyLink link_personName = new DataPropertyLink("e9", new Label("name"));
		DataPropertyLink link_birthDate = new DataPropertyLink("e10", new Label("birthDate"));
		DataPropertyLink link_placeName = new DataPropertyLink("e11", new Label("name"));
//		DataPropertyLink link_latitude = new DataPropertyLink("e12", new Label("lat"));
//		DataPropertyLink link_longitude = new DataPropertyLink("e13", new Label("long"));
//		DataPropertyLink link_countryCode = new DataPropertyLink("e14", new Label("countryCode"));
		DataPropertyLink link_title = new DataPropertyLink("e15", new Label("title"));
		DataPropertyLink link_phone = new DataPropertyLink("e16", new Label("phone"));
		DataPropertyLink link_email = new DataPropertyLink("e17", new Label("email"));
		DataPropertyLink link_postalCode = new DataPropertyLink("e18", new Label("postalCode"));
		
		SubClassLink link_sub_city = new SubClassLink("e19");
		SubClassLink link_sub_state = new SubClassLink("e20");
		SubClassLink link_sub_country = new SubClassLink("e21");
		
		graph.addVertex(node_person);
		graph.addVertex(node_place);
		graph.addVertex(node_organization);
		graph.addVertex(node_city);
		graph.addVertex(node_state);
		graph.addVertex(node_country);
		
		graph.addVertex(node_personName);
		graph.addVertex(node_placeName);
//		graph.addVertex(node_latitude);
//		graph.addVertex(node_longitude);
//		graph.addVertex(node_countryCode);
		graph.addVertex(node_postalcode);
		graph.addVertex(node_title);
		graph.addVertex(node_phone);
		graph.addVertex(node_email);
		graph.addVertex(node_birthDate);
		
		graph.addEdge(node_person, node_place, link_livesIn);
		graph.addEdge(node_person, node_place, link_bornIn);
		graph.addEdge(node_person, node_organization, link_worksFor);
		graph.addEdge(node_person, node_personName, link_personName);
		graph.addEdge(node_person, node_birthDate, link_birthDate);
		
		graph.addEdge(node_organization, node_place, link_location);
		graph.addEdge(node_organization, node_person, link_manager);
		graph.addEdge(node_organization, node_title, link_title);
		graph.addEdge(node_organization, node_phone, link_phone);
		graph.addEdge(node_organization, node_email, link_email);
		
		graph.addEdge(node_place, node_place, link_nearby);
		graph.addEdge(node_place, node_place, link_isPartOf);
		graph.addEdge(node_place, node_placeName, link_placeName);
		graph.addEdge(node_place, node_postalcode, link_postalCode);
//		graph.addEdge(node_place, node_latitude, link_latitude);
//		graph.addEdge(node_place, node_longitude, link_longitude);
//		graph.addEdge(node_country, node_countryCode, link_countryCode);
		graph.addEdge(node_place, node_state, link_inState);

		graph.addEdge(node_city, node_place, link_sub_city);
		graph.addEdge(node_state, node_place, link_sub_state);
		graph.addEdge(node_country, node_place, link_sub_country);
		
		return graph;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		String ontologyPath = "/Users/mohsen/Dropbox/ISWC-2013/figures/ontology.dot";
		DirectedWeightedMultigraph<Node, Link> graph = buildOntology();
		exportJGraphToGraphvizFile(graph, "", ontologyPath);
		
	}
}
