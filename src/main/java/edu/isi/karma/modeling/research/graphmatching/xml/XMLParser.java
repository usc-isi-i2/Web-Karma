package edu.isi.karma.modeling.research.graphmatching.xml;



import java.io.FileReader;
import java.util.Enumeration;
import java.util.Vector;

import edu.isi.karma.modeling.research.graphmatching.nanoxml.XMLElement;
import edu.isi.karma.modeling.research.graphmatching.util.Edge;
import edu.isi.karma.modeling.research.graphmatching.util.Graph;
import edu.isi.karma.modeling.research.graphmatching.util.GraphSet;
import edu.isi.karma.modeling.research.graphmatching.util.Node;


public class XMLParser {

	/** the path to the graph sets*/
	private String graphPath;
	
	public void setGraphPath(String graphPath) {
		this.graphPath = graphPath;
	}
	
	

	
	/**
	 * @return a graph set with 
	 * @param filename
	 * @throws Exception
	 */
	public GraphSet parseCXL(String filename) throws Exception {
		XMLElement xml = new XMLElement();
		FileReader reader = new FileReader(filename);
		xml.parseFromReader(reader);
		GraphSet graphSet = new GraphSet();
		Vector<XMLElement> children = xml.getChildren();
		XMLElement root = children.get(0);
		Enumeration<XMLElement> enumerator = root.enumerateChildren();
		int i = 1;
		while (enumerator.hasMoreElements()) {
			
			XMLElement child = enumerator.nextElement();
			Graph g = this.parseGXL(this.graphPath
					+ child.getAttribute("file", null)+"");
			g.setClassName((String) child.getAttribute("class", "NO_CLASS"));
			graphSet.add(g);
		}
		return graphSet;
	}

	/**
	 * @return a graph with 
	 * @param filename
	 * @throws Exception
	 */
	public Graph parseGXL(String filename) throws Exception {
	
		XMLElement xml = new XMLElement();
		FileReader reader = new FileReader(filename);
		xml.parseFromReader(reader);
		reader.close();
		Graph graph1 = new Graph();
		Vector children = xml.getChildren();
		XMLElement root = (XMLElement) children.get(0);
		String id = (String) root.getAttribute("id", null);
		String edgemode = (String) root.getAttribute("edgemode", "undirected");
		graph1.setGraphID(id);
		if (edgemode.equals("undirected")){
			graph1.setDirected(false);
		} else {
			graph1.setDirected(true);
		}
		Enumeration enumerator = root.enumerateChildren();
		int n = 0;
		while (enumerator.hasMoreElements()) {
			XMLElement child = (XMLElement) enumerator.nextElement();
			if (child.getName().equals("node")) {
				String nodeId = (String) (child.getAttribute("id", null));
				Node node = new Node();
				node.setNodeID(nodeId);
				Enumeration enum1 = child.enumerateChildren();
				while (enum1.hasMoreElements()) {
					XMLElement child1 = (XMLElement) enum1.nextElement();
					if (child1.getName().equals("attr")) {
						String key = (String) child1.getAttribute("name", null);
						Vector children2 = child1.getChildren();
						XMLElement child2 = (XMLElement) children2.get(0);
						String value = child2.getContent();
						node.put(key, value);
					}

				}
				graph1.add(node);
				n++;
			}
		}
		Edge[][] edges = new Edge[n][n]; 
		graph1.setAdjacenyMatrix(edges);
		enumerator = root.enumerateChildren();
		while (enumerator.hasMoreElements()) {	
			XMLElement child = (XMLElement) enumerator.nextElement();
			if (child.getName().equals("edge")) {
				Edge edge = new Edge();
				String from = (String) child.getAttribute("from", null);
				String to = (String) child.getAttribute("to", null);
				edge.put("from", from);
				edge.put("to", to);
				edge.setEdgeID(from + "_<>" + to);
				// *******************************
				Enumeration enum1 = child.enumerateChildren();
				while (enum1.hasMoreElements()) {
					XMLElement child1 = (XMLElement) enum1.nextElement();
					if (child1.getName().equals("attr")) {
						String key = (String) child1.getAttribute("name",
								"key failed!");
						Vector children2 = child1.getChildren();
						XMLElement child2 = (XMLElement) children2.get(0);
						String value = child2.getContent();
						edge.put(key, value);
					}
				}
							
				for (int i = 0; i < graph1.size(); i++){
					Node nodeI = graph1.get(i); 
					if (nodeI.getNodeID().equals(from)) {
						edge.setStartNode(nodeI);
						nodeI.getEdges().add(edge);
						for (int j = 0; j < graph1.size(); j++){
							Node nodeJ = graph1.get(j); 
							if (nodeJ.getNodeID().equals(to)) {
								edge.setEndNode(nodeJ);
								nodeJ.getEdges().add(edge);
								edges[i][j] = edge;
								if (!graph1.isDirected()){
									edges[j][i] = edge;
								}
							}
						}
					}
				}
			}
		}
		
		return graph1;
	}


	/**
	 * @return a graph with 
	 * @param filename
	 * @throws Exception
	 */
	public Graph parseGXLFromString(String graph) throws Exception {
	
		XMLElement xml = new XMLElement();
		xml.parseString(graph);
		Graph graph1 = new Graph();
		Vector children = xml.getChildren();
		XMLElement root = (XMLElement) children.get(0);
		String id = (String) root.getAttribute("id", null);
		String edgemode = (String) root.getAttribute("edgemode", "undirected");
		graph1.setGraphID(id);
		if (edgemode.equals("undirected")){
			graph1.setDirected(false);
		} else {
			graph1.setDirected(true);
		}
		Enumeration enumerator = root.enumerateChildren();
		int n = 0;
		while (enumerator.hasMoreElements()) {
			XMLElement child = (XMLElement) enumerator.nextElement();
			if (child.getName().equals("node")) {
				String nodeId = (String) (child.getAttribute("id", null));
				Node node = new Node();
				node.setNodeID(nodeId);
				Enumeration enum1 = child.enumerateChildren();
				while (enum1.hasMoreElements()) {
					XMLElement child1 = (XMLElement) enum1.nextElement();
					if (child1.getName().equals("attr")) {
						String key = (String) child1.getAttribute("name", null);
						Vector children2 = child1.getChildren();
						XMLElement child2 = (XMLElement) children2.get(0);
						String value = child2.getContent();
						node.put(key, value);
					}

				}
				graph1.add(node);
				n++;
			}
		}
		Edge[][] edges = new Edge[n][n]; 
		graph1.setAdjacenyMatrix(edges);
		enumerator = root.enumerateChildren();
		while (enumerator.hasMoreElements()) {	
			XMLElement child = (XMLElement) enumerator.nextElement();
			if (child.getName().equals("edge")) {
				Edge edge = new Edge();
				String from = (String) child.getAttribute("from", null);
				String to = (String) child.getAttribute("to", null);
				edge.put("from", from);
				edge.put("to", to);
				edge.setEdgeID(from + "_<>" + to);
				// *******************************
				Enumeration enum1 = child.enumerateChildren();
				while (enum1.hasMoreElements()) {
					XMLElement child1 = (XMLElement) enum1.nextElement();
					if (child1.getName().equals("attr")) {
						String key = (String) child1.getAttribute("name",
								"key failed!");
						Vector children2 = child1.getChildren();
						XMLElement child2 = (XMLElement) children2.get(0);
						String value = child2.getContent();
						edge.put(key, value);
					}
				}
							
				for (int i = 0; i < graph1.size(); i++){
					Node nodeI = graph1.get(i); 
					if (nodeI.getNodeID().equals(from)) {
						edge.setStartNode(nodeI);
						nodeI.getEdges().add(edge);
						for (int j = 0; j < graph1.size(); j++){
							Node nodeJ = graph1.get(j); 
							if (nodeJ.getNodeID().equals(to)) {
								edge.setEndNode(nodeJ);
								nodeJ.getEdges().add(edge);
								edges[i][j] = edge;
								if (!graph1.isDirected()){
									edges[j][i] = edge;
								}
							}
						}
					}
				}
			}
		}
		
		return graph1;
	}
	
	
	

	

	
}
