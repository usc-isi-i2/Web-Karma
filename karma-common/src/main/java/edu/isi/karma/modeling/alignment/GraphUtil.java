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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.CompactObjectPropertyLink;
import edu.isi.karma.rep.alignment.CompactSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.DisplayModel;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.rep.alignment.SubClassLink;

public class GraphUtil {

	private static Logger logger = LoggerFactory.getLogger(GraphUtil.class);

	private GraphUtil() {
	}

	public static DirectedGraph<Node, DefaultLink> asDirectedGraph(UndirectedGraph<Node, DefaultLink> undirectedGraph) {
		
		if (undirectedGraph == null) {
			logger.debug("graph is null.");
			return null;
		}		

		DirectedGraph<Node, DefaultLink> g = new DirectedWeightedMultigraph<>(DefaultLink.class);
		
		for (Node v : undirectedGraph.vertexSet())
			g.addVertex(v);
		
		for (DefaultLink e: undirectedGraph.edgeSet())
			g.addEdge(e.getSource(), e.getTarget(), e);
		
		return g;
	}

	
	public static DirectedWeightedMultigraph<Node, DefaultLink> asDefaultGraph(DirectedWeightedMultigraph<Node, LabeledLink> graph) {
		
		if (graph == null) {
			logger.debug("graph is null.");
			return null;
		}		

		DirectedWeightedMultigraph<Node, DefaultLink> g = new DirectedWeightedMultigraph<>(DefaultLink.class);
		
		for (Node v : graph.vertexSet())
			g.addVertex(v);
		
		for (DefaultLink e: graph.edgeSet())
			g.addEdge(e.getSource(), e.getTarget(), e);
		
		return g;
	}
	
	public static DirectedWeightedMultigraph<Node, LabeledLink> asLabeledGraph(DirectedWeightedMultigraph<Node, DefaultLink> graph) {
		
		if (graph == null) {
			logger.debug("graph is null.");
			return null;
		}		

		DirectedWeightedMultigraph<Node, LabeledLink> g = new DirectedWeightedMultigraph<>(LabeledLink.class);
		
		for (Node v : graph.vertexSet())
			g.addVertex(v);
		
		for (DefaultLink e: graph.edgeSet())
			if (e instanceof LabeledLink)
				g.addEdge(e.getSource(), e.getTarget(), (LabeledLink)e);
		
		return g;
	}
	
	public static UndirectedGraph<Node, DefaultLink> asDefaultGraph(UndirectedGraph<Node, LabeledLink> graph) {
		
		if (graph == null) {
			logger.debug("graph is null.");
			return null;
		}		

		UndirectedGraph<Node, DefaultLink> g = new WeightedMultigraph<>(DefaultLink.class);
		
		for (Node v : graph.vertexSet())
			g.addVertex(v);
		
		for (DefaultLink e: graph.edgeSet())
			g.addEdge(e.getSource(), e.getTarget(), e);
		
		return g;
	}
	
	public static WeightedMultigraph<Node, LabeledLink> asLabeledGraph(WeightedMultigraph<Node, DefaultLink> graph) {
		
		if (graph == null) {
			logger.debug("graph is null.");
			return null;
		}		

		WeightedMultigraph<Node, LabeledLink> g = new WeightedMultigraph<>(LabeledLink.class);
		
		for (Node v : graph.vertexSet())
			g.addVertex(v);
		
		for (DefaultLink e: graph.edgeSet())
			if (e instanceof LabeledLink)
				g.addEdge(e.getSource(), e.getTarget(), (LabeledLink)e);
		
		return g;
	}
	
	public static void printLabeledGraph(Graph<Node, LabeledLink> graph) {
		
		if (graph == null) {
			logger.debug("graph is null.");
			return;
		}		
		StringBuffer sb = new StringBuffer();
		sb.append("*** Nodes ***\n");
		for (Node n : graph.vertexSet()) {
			sb.append(n.getLocalId());
			sb.append("\n");
        }
		sb.append("*** Links ***\n");
		for (DefaultLink link : graph.edgeSet()) {
			sb.append(link.getId());
			sb.append(", ");
			sb.append(link.getType().toString());
			sb.append(", ");
			sb.append(link.getWeight());
			sb.append("\n");
        }
//		sb.append("------------------------------------------");
		logger.debug(sb.toString());
	}
	
	public static void printGraph(Graph<Node, DefaultLink> graph) {
		
		if (graph == null) {
			logger.debug("graph is null.");
			return;
		}		
		StringBuffer sb = new StringBuffer();
		sb.append("*** Nodes ***\n");
		for (Node n : graph.vertexSet()) {
			sb.append(n.getLocalId());
			sb.append("\n");
        }
		sb.append("*** Links ***\n");
		for (DefaultLink link : graph.edgeSet()) {
			sb.append(link.getId());
			sb.append(", ");
			sb.append(link.getType().toString());
			sb.append(", ");
			sb.append(link.getWeight());
			sb.append("\n");
        }
//		sb.append("------------------------------------------");
		logger.debug(sb.toString());
	}
	
	public static String defaultGraphToString(Graph<Node, DefaultLink> graph) {
		
		if (graph == null) {
			logger.debug("The input graph is null.");
			return "";
		}		

		StringBuffer sb = new StringBuffer();		
		sb.append("*** Nodes ***\n");
		for (Node n : graph.vertexSet()) {
			sb.append(n.getLocalId());
			sb.append("\n");
        }
		sb.append("*** Links ***\n");
		for (DefaultLink edge : graph.edgeSet()) {
			sb.append("(");
			sb.append(edge.getId());
			sb.append(" - w=" + edge.getWeight());
			sb.append("\n");
        }
		//sb.append("------------------------------------------");
		return sb.toString();
		
	}
	
	public static String labeledGraphToString(Graph<Node, LabeledLink> graph) {
		
		if (graph == null) {
			logger.debug("The input graph is null.");
			return "";
		}		

		StringBuffer sb = new StringBuffer();
		sb.append("*** Nodes ***\n");
		for (Node n : graph.vertexSet()) {
			sb.append(n.getLocalId());
			sb.append("\n");
        }
		sb.append("*** Links ***\n");
		for (LabeledLink edge : graph.edgeSet()) {
			sb.append("(");
			sb.append(edge.getId());
			sb.append(" - status=" + edge.getStatus().name());
			sb.append(" - w=" + edge.getWeight());
			sb.append(" - type=" + edge.getType().name());
			sb.append("\n");
        }
		//sb.append("------------------------------------------");
		return sb.toString();
		
	}
	
	public static List<GraphPath> getPaths(DirectedGraph<Node, DefaultLink> g, int length) {
		
		List<GraphPath> graphPaths =
				new LinkedList<>();

		if (g == null)
			return graphPaths;

		for (Node n : g.vertexSet()) {
			List<GraphPath> gpList = getOutgoingPaths(g, n, length);
			if (gpList != null) graphPaths.addAll(gpList);
		}

		return graphPaths;
		
	}
	
	public static List<GraphPath> getOutgoingPaths(DirectedGraph<Node, DefaultLink> g, Node n, int length) {
		
		List<GraphPath> graphPaths =
				new LinkedList<>();

		if (g == null || n == null || length <= 0 || !g.vertexSet().contains(n))
			return graphPaths;
		
		Set<DefaultLink> outgoingLinks =  g.outgoingEdgesOf(n);
		if (outgoingLinks == null || outgoingLinks.isEmpty())
			return graphPaths;
		
		for (DefaultLink l : outgoingLinks) {
			List<GraphPath> nextGraphPaths = getOutgoingPaths(g, l.getTarget(), length - 1);
			if (nextGraphPaths == null || nextGraphPaths.isEmpty()) {
				GraphPath gp = new GraphPath();
				gp.addLink(l);
				if (gp.getLength() == length)
					graphPaths.add(gp);
			} else {
				for (GraphPath p : nextGraphPaths) {
					GraphPath gp = new GraphPath(p);
					gp.addLinkToHead(l);
					if (gp.getLength() == length)
						graphPaths.add(gp);
				}
			}
		}
		
		return graphPaths;
		
	}
	
	public static Set<Node> getOutNeighbors(DirectedGraph<Node, DefaultLink> g, Node n) {
		
		Set<Node> neighbors = new HashSet<>();
		if (g == null || n == null || !g.vertexSet().contains(n))
			return neighbors;
		
		
		Set<DefaultLink> outgoingLinks = g.outgoingEdgesOf(n);
		if (outgoingLinks != null) {
			for (DefaultLink l : outgoingLinks) {
				neighbors.add(l.getTarget());
			}
		}
		
		return neighbors;
	}

	public static Set<Node> getInNeighbors(DirectedGraph<Node, DefaultLink> g, Node n) {
		
		Set<Node> neighbors = new HashSet<>();
		if (g == null || n == null || !g.vertexSet().contains(n))
			return neighbors;
		
		Set<DefaultLink> incomingLinks = g.incomingEdgesOf(n);
		if (incomingLinks != null) {
			for (DefaultLink l : incomingLinks) {
				neighbors.add(l.getSource());
			}
		}
		
		return neighbors;
	}
	
	public static Set<LabeledLink> getDomainLinksInLabeledGraph(DirectedGraph<Node, LabeledLink> g, ColumnNode n) {
		
		Set<LabeledLink> domainLinks = new HashSet<>();
		if (g == null || 
				n == null || 
				!g.vertexSet().contains(n))
			return domainLinks;
		
		Set<LabeledLink> incomingLinks = g.incomingEdgesOf(n);
		if (incomingLinks != null) {
			for (DefaultLink l : incomingLinks) {
				domainLinks.add((LabeledLink)l);
			}
		}
		
		return domainLinks;
	}
	
	public static Set<LabeledLink> getDomainLinksInDefaultGraph(DirectedGraph<Node, DefaultLink> g, ColumnNode n) {
		
		Set<LabeledLink> domainLinks = new HashSet<>();
		if (g == null || 
				n == null || 
				!g.vertexSet().contains(n))
			return domainLinks;
		
		Set<DefaultLink> incomingLinks = g.incomingEdgesOf(n);
		if (incomingLinks != null) {
			for (DefaultLink l : incomingLinks) {
				if (l instanceof LabeledLink)
					domainLinks.add((LabeledLink)l);
			}
		}
		
		return domainLinks;
	}
	
	public static HashMap<SemanticType, LabeledLink> getDomainLinks(DirectedGraph<Node, DefaultLink> g, ColumnNode n, List<SemanticType> semanticTypes) {
		
		HashMap<SemanticType, LabeledLink> domainLinks = new HashMap<>();
		if (g == null || 
				n == null || 
				semanticTypes == null ||
				!g.vertexSet().contains(n))
			return domainLinks;
		
		Set<DefaultLink> incomingLinks = g.incomingEdgesOf(n);
		if (incomingLinks != null) {
			for (SemanticType st : semanticTypes) {
				for (DefaultLink l : incomingLinks) {
					if (st.getDomain().getUri().equalsIgnoreCase(l.getSource().getUri()) &&
							st.getType().getUri().equalsIgnoreCase(l.getUri())) {
						if (l instanceof LabeledLink)
							domainLinks.put(st, (LabeledLink)l);
					}
				}
			}
		}
		
		return domainLinks;
	}
	
	public static DisplayModel getDisplayModel(DirectedWeightedMultigraph<Node, LabeledLink> g, HTable hTable) {
		DisplayModel displayModel = new DisplayModel(g, hTable);
		return displayModel;
	}

	public static void treeToRootedTree(
			DirectedWeightedMultigraph<Node, DefaultLink> tree, 
			Node node, LabeledLink e, 
			Set<Node> visitedNodes, 
			Set<String> reversedLinks, 
			Set<String> removedLinks) {
		
		if (node == null)
			return;
		
		if (visitedNodes.contains(node)) // prevent having loop in the tree
			return;
		
		visitedNodes.add(node);
		
		Node source, target;
		
		Set<DefaultLink> incomingLinks = tree.incomingEdgesOf(node);
		if (incomingLinks != null) {
			LabeledLink[] incomingLinksArr = incomingLinks.toArray(new LabeledLink[0]);
			for (LabeledLink inLink : incomingLinksArr) {
				
				source = inLink.getSource();
				target = inLink.getTarget();
				
				// don't remove the incoming link from parent to this node
				if (e != null && inLink.equals(e))
					continue;
				
				// removeEdge method should always be called before addEdge because the new edge has the same id
				// and JGraph does not add the duplicate link
//				Label label = new Label(inLink.getLabel().getUri(), inLink.getLabel().getNs(), inLink.getLabel().getPrefix());
				LabeledLink reverseLink = inLink.clone(); //new Link(inLink.getId(), label);
				tree.removeEdge(inLink);
				tree.addEdge(target, source, reverseLink);
				tree.setEdgeWeight(reverseLink, inLink.getWeight());
				
				// Save the reversed links information
				reversedLinks.add(inLink.getId());
			}
		}

		Set<DefaultLink> outgoingLinks = tree.outgoingEdgesOf(node);

		if (outgoingLinks == null)
			return;
		
		
		LabeledLink[] outgoingLinksArr = outgoingLinks.toArray(new LabeledLink[0]);
		for (LabeledLink outLink : outgoingLinksArr) {
			target = outLink.getTarget();
			if (visitedNodes.contains(target)) {
				tree.removeEdge(outLink);
				removedLinks.add(outLink.getId());
			} else {
				treeToRootedTree(tree, target, outLink, visitedNodes, reversedLinks, removedLinks);
			}
		}
	}
	
//	public static void exportJson(Workspace workspace, Worksheet worksheet, DirectedWeightedMultigraph<Node, DefaultLink> graph, String filename) throws IOException {
	public static void exportJson(DirectedWeightedMultigraph<Node, DefaultLink> graph, String filename, 
			boolean writeNodeAnnotations,
			boolean writeLinkAnnotations) throws IOException {
		logger.info("exporting the graph to json ...");
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream out = new FileOutputStream(file); 
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("    ");
		try {
			writeGraph(graph, writer, writeNodeAnnotations, writeLinkAnnotations);
//			writeGraph(workspace, worksheet, graph, writer);
		} catch (Exception e) {
			logger.error("error in writing the model in json!");
	    	e.printStackTrace();
	     } finally {
			writer.close();
		}
		logger.info("export is done.");
	}
	
	public static DirectedWeightedMultigraph<Node, DefaultLink> importJson(String filename) throws IOException {

		File file = new File(filename);
		if (!file.exists()) {
			logger.error("cannot open the file " + filename);
		}
		
		FileInputStream in = new FileInputStream(file);
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	    try {
	    	return readGraph(reader);
	    } catch (Exception e) {
	    	logger.error("error in reading the model from json!");
	    	e.printStackTrace();
	    	return null;
	    } finally {
	    	reader.close();
	    }
	}
	
//	public static void writeGraph(Workspace workspace, Worksheet worksheet, DirectedWeightedMultigraph<Node, DefaultLink> graph, JsonWriter writer) throws IOException {
	public static void writeGraph(DirectedWeightedMultigraph<Node, DefaultLink> graph, JsonWriter writer, 
			boolean writeNodeAnnotations,
			boolean writeLinkAnnotations) throws IOException {
		
		writer.beginObject();

		writer.name("nodes");
		writer.beginArray();
		if (graph != null)
			for (Node n : graph.vertexSet())
				writeNode(writer, n, writeNodeAnnotations);
//				writeNode(workspace, worksheet, writer, n);
		writer.endArray();
		
		writer.name("links");
		writer.beginArray();
		if (graph != null)
			for (DefaultLink l : graph.edgeSet())
				writeLink(writer, l, writeLinkAnnotations);
		writer.endArray();
		
		writer.endObject();
		
	}
	
//	private static void writeNode(Workspace workspace, Worksheet worksheet, JsonWriter writer, Node node) throws IOException {
	private static void writeNode(JsonWriter writer, Node node, boolean writeNodeAnnotations) throws IOException {
		
		if (node == null)
			return;
		
		String nullStr = null;
		
		writer.beginObject();
		writer.name("id").value(node.getId());
		writer.name("label");
		if (node.getLabel() == null) writer.value(nullStr);
		else writeLabel(writer, node.getLabel());
		
		writer.name("type").value(node.getType().toString());
//		SemanticTypeUtil semUtil = new SemanticTypeUtil();
		if (node instanceof ColumnNode) {
			ColumnNode cn = (ColumnNode) node;
			writer.name("hNodeId").value(cn.getHNodeId());
			writer.name("columnName").value(cn.getColumnName());
			writer.name("rdfLiteralType");
			if (cn.getRdfLiteralType() == null) writer.value(nullStr);
			else writeLabel(writer, cn.getRdfLiteralType());
			if(cn.getLanguage() == null) writer.name("language").value(nullStr);
			else writer.name("language").value(cn.getLanguage());
			writer.name("userSemanticTypes");
			if (cn.getUserSemanticTypes() == null) writer.value(nullStr);
			else {
				writer.beginArray();
				for (SemanticType semanticType : cn.getUserSemanticTypes())
					writeSemanticType(writer, semanticType);
				writer.endArray();
			}
			writer.name("learnedSemanticTypes");
			if (cn.getLearnedSemanticTypes() == null) writer.value(nullStr);
			else {
				writer.beginArray();
				for (SemanticType semanticType : cn.getLearnedSemanticTypes())
					writeSemanticType(writer, semanticType);
				writer.endArray();
			}
		}
		if (node instanceof LiteralNode) {
			LiteralNode ln = (LiteralNode) node;
			writer.name("value").value(ln.getValue());
			writer.name("datatype");
			if (ln.getDatatype() == null) writer.value(nullStr);
			else writeLabel(writer, ln.getDatatype());
			writer.name("language").value(ln.getLanguage());
			writer.name("isUri").value(Boolean.toString(ln.isUri()));
		}
		
		writer.name("modelIds");
		if (!writeNodeAnnotations || node.getModelIds() == null) writer.value(nullStr);
		else writeModelIds(writer, node.getModelIds());
		writer.endObject();
	}

	private static void writeLink(JsonWriter writer, DefaultLink link, boolean writeLinkAnnotations) throws IOException {
		
		if (link == null)
			return;
		
		String nullStr = null;

		writer.beginObject();
		writer.name("id").value(link.getId());
		writer.name("weight").value(link.getWeight());
		writer.name("type").value(link.getType().toString());
		if (link instanceof CompactObjectPropertyLink)
			writer.name("objectPropertyType").value( ((CompactObjectPropertyLink)link).getObjectPropertyType().toString());
		else if (link instanceof LabeledLink) {
			LabeledLink l = (LabeledLink)link;
			writer.name("label");
			if (l.getLabel() == null) writer.value(nullStr);
			else writeLabel(writer, l.getLabel());
			if (l instanceof DataPropertyOfColumnLink)
				writer.name("hNodeId").value( ((DataPropertyOfColumnLink)l).getSpecializedColumnHNodeId());
			if (l instanceof ObjectPropertyLink)
				writer.name("objectPropertyType").value( ((ObjectPropertyLink)l).getObjectPropertyType().toString());
			if (l instanceof ObjectPropertySpecializationLink) {
				writer.name("specializedLink").value(((ObjectPropertySpecializationLink)l).getSpecializedLinkId());
			}
			writer.name("status").value(l.getStatus().toString());
			writer.name("keyInfo").value(l.getKeyType().toString());
			writer.name("modelIds");
			if (!writeLinkAnnotations || l.getModelIds() == null) writer.value(nullStr);
			else writeModelIds(writer, l.getModelIds());
		}
		writer.endObject();
	}
	
	private static void writeLabel(JsonWriter writer, Label label) throws IOException {
		
		if (label == null)
			return;
		
		writer.beginObject();
		writer.name("uri").value(label.getUri());
//		writer.name("ns").value(label.getNs());
//		writer.name("prefix").value(label.getPrefix());
		writer.name("rdfsLabel").value(label.getRdfsLabel());
//		writer.name("rdfsComment").value(label.getRdfsComment());
		writer.endObject();
	}
	
	private static void writeSemanticType(JsonWriter writer, SemanticType semanticType) throws IOException {
		
		if (semanticType == null)
			return;
		
		String nullStr = null;
		
		writer.beginObject();
		writer.name("hNodeId").value(semanticType.getHNodeId());
		writer.name("domain");
		if (semanticType.getDomain() == null) writer.value(nullStr);
		else writeLabel(writer, semanticType.getDomain());
		writer.name("type");
		if (semanticType.getType() == null) writer.value(nullStr);
		else writeLabel(writer, semanticType.getType());
		writer.name("origin").value(semanticType.getOrigin().toString());
		writer.name("confidenceScore").value(semanticType.getConfidenceScore());
		writer.endObject();
	}
	
	private static void writeModelIds(JsonWriter writer, Set<String> modelIds) throws IOException {
		
		if (modelIds == null)
			return;
		
		writer.beginArray();
		for (String s : modelIds)
			writer.value(s);
		writer.endArray();
	}
	
	public static DirectedWeightedMultigraph<Node, DefaultLink> readGraph(JsonReader reader) throws IOException {
		
		DirectedWeightedMultigraph<Node, DefaultLink> graph = 
				new DirectedWeightedMultigraph<Node, DefaultLink>(LabeledLink.class);
		
		Node n, source, target;
		DefaultLink l;
		Double[] weight = new Double[1];
		HashMap<String, Node> idToNodes = new HashMap<>();
		
		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("nodes") && reader.peek() != JsonToken.NULL) {
				reader.beginArray();
			    while (reader.hasNext()) {
			    	n = readNode(reader);
			    	if (n != null) {
			    		idToNodes.put(n.getId(), n);
			    		graph.addVertex(n);
			    	}
			    }
			    reader.endArray();
			} else if (key.equals("links") && reader.peek() != JsonToken.NULL) {
				reader.beginArray();
			    while (reader.hasNext()) {
			    	l = readLink(reader, weight);
			    	if (l != null) {
			    		source = idToNodes.get(LinkIdFactory.getLinkSourceId(l.getId()));
			    		target = idToNodes.get(LinkIdFactory.getLinkTargetId(l.getId()));
			    		if (source  != null && target != null) {
				    		graph.addEdge(source, target, l);
				    		if (weight[0] != null) graph.setEdgeWeight(l, weight[0].doubleValue());
			    		}
			    	}
			    }
			    reader.endArray();			
			} else {
				reader.skipValue();
			}
		}
    	reader.endObject();
    	
		return graph;
	}

	private static Node readNode(JsonReader reader) throws IOException {
		
		String id = null;
		Label label = null;
		NodeType type = null;
		String hNodeId = null;
		String columnName = null;
		Label rdfLiteralType = null;
		String language = null;
		Label datatype = null;
		String value = null;
		boolean isUri = false;
		SemanticType userSelectedSemanticType = null;
		List<SemanticType> learnedSemanticTypes = null;
		List<SemanticType> userSemanticTypes = null;
		Set<String> modelIds = null;
		
		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("id") && reader.peek() != JsonToken.NULL) {
				id = reader.nextString();
			} else if (key.equals("label") && reader.peek() != JsonToken.NULL) {
				label = readLabel(reader);
			} else if (key.equals("type") && reader.peek() != JsonToken.NULL) {
				type = NodeType.valueOf(reader.nextString());
			} else if (key.equals("hNodeId") && reader.peek() != JsonToken.NULL) {
				hNodeId = reader.nextString();
			} else if (key.equals("columnName") && reader.peek() != JsonToken.NULL) {
				columnName = reader.nextString();
			} else if (key.equals("datatype") && reader.peek() != JsonToken.NULL) {
				datatype = readLabel(reader);
			} else if (key.equals("value") && reader.peek() != JsonToken.NULL) {
				value = reader.nextString();
			} else if (key.equals("isUri") && reader.peek() != JsonToken.NULL) {
				isUri = Boolean.parseBoolean(reader.nextString());
			} else if (key.equals("rdfLiteralType") && reader.peek() != JsonToken.NULL) {
				rdfLiteralType = readLabel(reader);
			} else if (key.equals("language") && reader.peek() != JsonToken.NULL) {
				language = reader.nextString();
			} else if (key.equals("userSelectedSemanticType") && reader.peek() != JsonToken.NULL) {
				userSelectedSemanticType = readSemanticType(reader);
			} else if (key.equals("suggestedSemanticTypes") && reader.peek() != JsonToken.NULL) {
				learnedSemanticTypes = new ArrayList<>();
				reader.beginArray();
			    while (reader.hasNext()) {
			    	SemanticType semanticType = readSemanticType(reader);
			    	learnedSemanticTypes.add(semanticType);
				}
		    	reader.endArray();				
			} else if (key.equals("userSemanticTypes") && reader.peek() != JsonToken.NULL) {
				userSemanticTypes = new ArrayList<>();
				reader.beginArray();
			    while (reader.hasNext()) {
			    	SemanticType semanticType = readSemanticType(reader);
			    	userSemanticTypes.add(semanticType);
				}
		    	reader.endArray();				} 
			else if (key.equals("learnedSemanticTypes") && reader.peek() != JsonToken.NULL) {
				learnedSemanticTypes = new ArrayList<>();
				reader.beginArray();
			    while (reader.hasNext()) {
			    	SemanticType semanticType = readSemanticType(reader);
			    	learnedSemanticTypes.add(semanticType);
				}
		    	reader.endArray();				
			} else if (key.equals("modelIds") && reader.peek() != JsonToken.NULL) {
				modelIds = readModelIds(reader);
			} else {
				reader.skipValue();
			}
		}
    	reader.endObject();
    	
    	Node n = null;
    	if (type == NodeType.InternalNode) {
    		n = new InternalNode(id, label);
    	} else if (type == NodeType.ColumnNode) {
    		n = new ColumnNode(id, hNodeId, columnName, rdfLiteralType, language);
    		if (userSemanticTypes == null && userSelectedSemanticType != null) {
				userSemanticTypes = new ArrayList<>();
				userSemanticTypes.add(userSelectedSemanticType);
    		}
    		if (userSemanticTypes != null) {
	    		for (SemanticType st : userSemanticTypes)
	    			((ColumnNode)n).assignUserType(st);
    		}
    		((ColumnNode)n).setLearnedSemanticTypes(learnedSemanticTypes);
    	} else if (type == NodeType.LiteralNode) {
    		n = new LiteralNode(id, value, datatype, language, isUri);
    	} else {
    		logger.error("cannot instanciate a node from the type: " + type.toString());
    		return null;
    	}
    	
		n.setModelIds(modelIds);
    	
    	return n;
	}
	
	private static DefaultLink readLink(JsonReader reader, Double[] weight) throws IOException {
		
		String id = null;
		Label label = null;
		LinkType type = null;
		String hNodeId = null;
		ObjectPropertyType objectPropertyType = null;
		String specializedLinkId = null;
		LinkStatus status = null;
		LinkKeyInfo keyInfo = null;
		Set<String> modelIds = null;
		if (weight == null) weight = new Double[1];

		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("id") && reader.peek() != JsonToken.NULL) {
				id = reader.nextString();
			} else if (key.equals("label") && reader.peek() != JsonToken.NULL) {
				label = readLabel(reader);
			} else if (key.equals("type") && reader.peek() != JsonToken.NULL) {
				type = LinkType.valueOf(reader.nextString());
			} else if (key.equals("hNodeId") && reader.peek() != JsonToken.NULL) {
				hNodeId = reader.nextString();
			} else if (key.equals("objectPropertyType") && reader.peek() != JsonToken.NULL) {
				objectPropertyType = ObjectPropertyType.valueOf(reader.nextString());
			} else if (key.equals("specializedLinkId") && reader.peek() != JsonToken.NULL) {
				specializedLinkId = reader.nextString();
			} else if (key.equals("status") && reader.peek() != JsonToken.NULL) {
				status = LinkStatus.valueOf(reader.nextString());
			} else if (key.equals("keyInfo") && reader.peek() != JsonToken.NULL) {
				keyInfo = LinkKeyInfo.valueOf(reader.nextString());
			} else if (key.equals("modelIds") && reader.peek() != JsonToken.NULL) {
				modelIds = readModelIds(reader);
			} else if (key.equals("weight") && reader.peek() != JsonToken.NULL) {
				weight[0] = new Double(reader.nextDouble());
			} else {
				reader.skipValue();
			}
		}
    	reader.endObject();
    	
    	DefaultLink l = null;
    	if (type == LinkType.ClassInstanceLink) {
    		l = new ClassInstanceLink(id, keyInfo);
    	} else if (type == LinkType.ColumnSubClassLink) {
    		l = new ColumnSubClassLink(id);
    	} else if (type == LinkType.DataPropertyLink) {
    		l = new DataPropertyLink(id, label);
    	} else if (type == LinkType.DataPropertyOfColumnLink) {
    		l = new DataPropertyOfColumnLink(id, hNodeId, specializedLinkId);
    	} else if (type == LinkType.ObjectPropertyLink) {
    		l = new ObjectPropertyLink(id, label, objectPropertyType);
    	} else if (type == LinkType.ObjectPropertySpecializationLink) {
    		l = new ObjectPropertySpecializationLink(hNodeId, specializedLinkId);
    	} else if (type == LinkType.SubClassLink) {
    		l = new SubClassLink(id);
    	} else if (type == LinkType.CompactObjectPropertyLink) {
    		l = new CompactObjectPropertyLink(id, objectPropertyType);
    	} else if (type == LinkType.CompactSubClassLink) {
    		l = new CompactSubClassLink(id);
    	} else {
    		logger.error("cannot instanciate a link from the type: " + type.toString());
    		return null;
    	}
    	
    	if (l instanceof LabeledLink) {
	    	((LabeledLink)l).setStatus(status);
	    	((LabeledLink)l).setModelIds(modelIds);
    	}
    	return l;
	}
	
	private static Label readLabel(JsonReader reader) throws IOException {
		
		String uri = null;
//		String ns = null;
//		String prefix = null;
//		String rdfsComment = null;
//		String rdfsLabel = null;
		
		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("uri") && reader.peek() != JsonToken.NULL) {
				uri = reader.nextString();
			//} else if (key.equals("ns") && reader.peek() != JsonToken.NULL) {
			//	ns = reader.nextString();
			//} else if (key.equals("prefix") && reader.peek() != JsonToken.NULL) {
			//	prefix = reader.nextString();
			//} else if (key.equals("rdfsLabel") && reader.peek() != JsonToken.NULL) {
			//	rdfsLabel = reader.nextString();
			//} else if (key.equals("rdfsComment") && reader.peek() != JsonToken.NULL) {
			//	rdfsComment = reader.nextString();
			} else {
			  reader.skipValue();
			}
		}
    	reader.endObject();
    	
//    	Label label = new Label(uri, ns, prefix, rdfsLabel, rdfsComment);
    	Label label = new Label(uri);
    	return label;
	}
	
	private static SemanticType readSemanticType(JsonReader reader) throws IOException {

		String hNodeId = null;
		Label domain = null;
		Label type = null;
		Origin origin = null;
		Double confidenceScore = null;
		
		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("hNodeId") && reader.peek() != JsonToken.NULL) {
				hNodeId = reader.nextString();
			} else if (key.equals("domain") && reader.peek() != JsonToken.NULL) {
				domain = readLabel(reader);
			} else if (key.equals("type") && reader.peek() != JsonToken.NULL) {
				type = readLabel(reader);
			} else if (key.equals("origin") && reader.peek() != JsonToken.NULL) {
				origin = Origin.valueOf(reader.nextString());
			} else if (key.equals("confidenceScore") && reader.peek() != JsonToken.NULL) {
				confidenceScore = reader.nextDouble();
			} else {
			  reader.skipValue();
			}
		}
    	reader.endObject();
    	
    	SemanticType semanticType = new SemanticType(hNodeId, type, domain, null, false, origin, confidenceScore);
    	return semanticType;	
    }
	
	private static Set<String> readModelIds(JsonReader reader) throws IOException {
		
		Set<String> modelIds = new HashSet<>();
		
		reader.beginArray();
	    while (reader.hasNext()) {
	    	modelIds.add(reader.nextString());
		}
    	reader.endArray();
    	
    	return modelIds;
	}
}
