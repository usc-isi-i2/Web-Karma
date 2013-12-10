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
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.DisplayModel;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.rep.alignment.PlainLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.rep.alignment.SubClassLink;

public class GraphUtil {

	private static Logger logger = LoggerFactory.getLogger(GraphUtil.class);
	
	// FIXME: change methods to get an Outputstream as input and write on it.
	
	public static Set<ColumnNode> getColumnNodes(
			DirectedWeightedMultigraph<Node, Link> model) {

		if (model == null)
			return new HashSet<ColumnNode>();
		
		Set<ColumnNode> columnNodes = new HashSet<ColumnNode>();

		for (Node n : model.vertexSet()) 
			if (n instanceof ColumnNode)
				columnNodes.add((ColumnNode)n);
			
		return columnNodes;
	}
	
	public static void printVertex(Node node, StringBuffer sb) {
		
		if (node == null) {
			sb.append("node is null.");
			return;
		}
		
		sb.append("(");
		sb.append( node.getLocalId());
//    	sb.append( vertex.getID());
		sb.append(", ");
		if (node instanceof ColumnNode)
			sb.append( ((ColumnNode)node).getColumnName());
		else
			sb.append(node.getLabel().getLocalName());
		sb.append(", ");
		sb.append(node.getType().toString());
		sb.append(")");
	}
	
	public static void printEdge(Link link, StringBuffer sb) {
		
		if (link == null) {
			sb.append("link is null.");
			return;
		}
		
		sb.append("(");
    	sb.append( link.getLocalId());
    	sb.append(", ");
    	sb.append(link.getLabel().getLocalName());
    	sb.append(", ");
    	sb.append(link.getType().toString());
    	sb.append(", ");
    	sb.append(link.getWeight());
    	sb.append(") - From ");
    	printVertex(link.getSource(), sb);
    	sb.append(" To ");
    	printVertex(link.getTarget(), sb);
	}

	public static DirectedGraph<Node, Link> asDirectedGraph(UndirectedGraph<Node, Link> undirectedGraph) {
		
		if (undirectedGraph == null) {
			logger.debug("graph is null.");
			return null;
		}		

		DirectedGraph<Node, Link> g = new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		for (Node v : undirectedGraph.vertexSet())
			g.addVertex(v);
		
		for (Link e: undirectedGraph.edgeSet())
			g.addEdge(e.getSource(), e.getTarget(), e);
		
		return g;
	}
	
	public static void printGraph(Graph<Node, Link> graph) {
		
		if (graph == null) {
			logger.debug("graph is null.");
			return;
		}		
		StringBuffer sb = new StringBuffer();
		sb.append("*** Nodes ***");
		for (Node vertex : graph.vertexSet()) {
			printVertex(vertex, sb);
			sb.append("\n");
        }
		sb.append("*** Links ***");
		for (Link edge : graph.edgeSet()) {
			printEdge(edge, sb);
			sb.append("\n");
        }
		sb.append("------------------------------------------");
		logger.debug(sb.toString());
		
	}
	
	public static String graphToString(Graph<Node, Link> graph) {
		
		if (graph == null) {
			logger.error("The input graph is null.");
			return "";
		}		

		StringBuffer sb = new StringBuffer();
		for (Link edge : graph.edgeSet()) {
			sb.append("(");
			sb.append(edge.getId());
			sb.append(" - status=" + edge.getStatus().name());
			sb.append(" - w=" + edge.getWeight());
			sb.append("\n");
        }
		sb.append("------------------------------------------");
		return sb.toString();
		
	}
	
//	public static void serialize(DirectedWeightedMultigraph<Node, Link> graph, String fileName) throws Exception
//	{
//		
//		if (graph == null) {
//			logger.error("The input graph is null.");
//			return;
//		}		
//
////		ByteArrayOutputStream bout = new ByteArrayOutputStream();
//		FileOutputStream f = new FileOutputStream(fileName);
//		ObjectOutputStream out = new ObjectOutputStream(f);
//
//		out.writeObject(graph);
//		out.flush();
//		out.close();
//	}
//	
//	@SuppressWarnings("unchecked")
//	public static DirectedWeightedMultigraph<Node, Link> deserialize(String fileName) throws Exception
//	{
////		ByteArrayOutputStream bout = new ByteArrayOutputStream();
//		FileInputStream f = new FileInputStream(fileName);
//        ObjectInputStream in = new ObjectInputStream(f);
//
//        Object obj  = in.readObject();
//        in.close();
//        
//        if (obj instanceof DirectedWeightedMultigraph<?, ?>)
//        	return (DirectedWeightedMultigraph<Node, Link>)obj;
//        else 
//        	return null;
//	}
	
	public static Set<Node> getOutNeighbors(DirectedWeightedMultigraph<Node, Link> g, Node n) {
		
		Set<Node> neighbors = new HashSet<Node>();
		if (g == null || n == null || !g.vertexSet().contains(n))
			return neighbors;
		
		Set<Link> outgoingLinks = g.outgoingEdgesOf(n);
		if (outgoingLinks != null) {
			for (Link l : outgoingLinks) {
				neighbors.add(l.getTarget());
			}
		}
		
		return neighbors;
	}

	public static Set<Node> getInNeighbors(DirectedWeightedMultigraph<Node, Link> g, Node n) {
		
		Set<Node> neighbors = new HashSet<Node>();
		if (g == null || n == null || !g.vertexSet().contains(n))
			return neighbors;
		
		Set<Link> incomingLinks = g.incomingEdgesOf(n);
		if (incomingLinks != null) {
			for (Link l : incomingLinks) {
				neighbors.add(l.getSource());
			}
		}
		
		return neighbors;
	}
	
	public static DisplayModel getDisplayModel(DirectedWeightedMultigraph<Node, Link> g, HTable hTable) {
		DisplayModel displayModel = new DisplayModel(g, hTable);
		return displayModel;
	}

	public static void treeToRootedTree(
			DirectedWeightedMultigraph<Node, Link> tree, 
			Node node, Link e, 
			Set<Node> visitedNodes, 
			Set<String> reversedLinks, 
			Set<String> removedLinks) {
		
		if (node == null)
			return;
		
		if (visitedNodes.contains(node)) // prevent having loop in the tree
			return;
		
		visitedNodes.add(node);
		
		Node source, target;
		
		Set<Link> incomingLinks = tree.incomingEdgesOf(node);
		if (incomingLinks != null) {
			Link[] incomingLinksArr = incomingLinks.toArray(new Link[0]);
			for (Link inLink : incomingLinksArr) {
				
				source = inLink.getSource();
				target = inLink.getTarget();
				
				// don't remove the incoming link from parent to this node
				if (e != null && inLink.equals(e))
					continue;
				
				// removeEdge method should always be called before addEdge because the new edge has the same id
				// and JGraph does not add the duplicate link
//				Label label = new Label(inLink.getLabel().getUri(), inLink.getLabel().getNs(), inLink.getLabel().getPrefix());
				Link reverseLink = inLink.clone(); //new Link(inLink.getId(), label);
				tree.removeEdge(inLink);
				tree.addEdge(target, source, reverseLink);
				tree.setEdgeWeight(reverseLink, inLink.getWeight());
				
				// Save the reversed links information
				reversedLinks.add(inLink.getId());
			}
		}

		Set<Link> outgoingLinks = tree.outgoingEdgesOf(node);

		if (outgoingLinks == null)
			return;
		
		
		Link[] outgoingLinksArr = outgoingLinks.toArray(new Link[0]);
		for (Link outLink : outgoingLinksArr) {
			target = outLink.getTarget();
			if (visitedNodes.contains(target)) {
				tree.removeEdge(outLink);
				removedLinks.add(outLink.getId());
			} else {
				treeToRootedTree(tree, target, outLink, visitedNodes, reversedLinks, removedLinks);
			}
		}
	}
	
	public static void exportJson(DirectedWeightedMultigraph<Node, Link> graph, String filename) throws IOException {
		logger.info("exporting the graph to json ...");
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}

		FileOutputStream out = new FileOutputStream(file); 
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		//writer.setIndent("    ");
		try {
			writeGraph(graph, writer);
		} catch (Exception e) {
			logger.error("error in writing the model in json!");
	    	e.printStackTrace();
	     } finally {
			writer.close();
		}
		logger.info("export is done.");
	}
	
	public static DirectedWeightedMultigraph<Node, Link> importJson(String filename) throws IOException {

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
	
	public static void writeGraph(DirectedWeightedMultigraph<Node, Link> graph, JsonWriter writer) throws IOException {
		
		writer.beginObject();

		writer.name("nodes");
		writer.beginArray();
		if (graph != null)
			for (Node n : graph.vertexSet())
				writeNode(writer, n);
		writer.endArray();
		
		writer.name("links");
		writer.beginArray();
		if (graph != null)
			for (Link l : graph.edgeSet())
				writeLink(writer, l);
		writer.endArray();
		
		writer.endObject();
		
	}
	
	private static void writeNode(JsonWriter writer, Node node) throws IOException {
		
		if (node == null)
			return;
		
		String nullStr = null;
		
		writer.beginObject();
		writer.name("id").value(node.getId());
		writer.name("label");
		if (node.getLabel() == null) writer.value(nullStr);
		else writeLabel(writer, node.getLabel());
		
		writer.name("type").value(node.getType().toString());
		
		if (node instanceof ColumnNode) {
			ColumnNode cn = (ColumnNode) node;
			writer.name("hNodeId").value(cn.getHNodeId());
			writer.name("columnName").value(cn.getColumnName());
			writer.name("rdfLiteralType");
			if (cn.getRdfLiteralType() == null) writer.value(nullStr);
			else writeLabel(writer, cn.getRdfLiteralType());
			writer.name("userSelectedSemanticType");
			if (cn.getUserSelectedSemanticType() == null) writer.value(nullStr);
			else writeSemanticType(writer, cn.getUserSelectedSemanticType());
			writer.name("crfSuggestedSemanticTypes");
			if (cn.getCrfSuggestedSemanticTypes() == null) writer.value(nullStr);
			else {
				writer.beginArray();
				if (cn.getCrfSuggestedSemanticTypes() != null)
					for (SemanticType semanticType : cn.getCrfSuggestedSemanticTypes())
						writeSemanticType(writer, semanticType);
				writer.endArray();
			}
		}
		
		writer.name("modelIds");
		if (node.getModelIds() == null) writer.value(nullStr);
		else writeModelIds(writer, node.getModelIds());
		writer.endObject();
	}

	private static void writeLink(JsonWriter writer, Link link) throws IOException {
		
		if (link == null)
			return;
		
		String nullStr = null;

		writer.beginObject();
		writer.name("id").value(link.getId());
		writer.name("label");
		if (link.getLabel() == null) writer.value(nullStr);
		else writeLabel(writer, link.getLabel());
		writer.name("type").value(link.getType().toString());
		if (link instanceof DataPropertyOfColumnLink)
			writer.name("hNodeId").value( ((DataPropertyOfColumnLink)link).getSpecializedColumnHNodeId());
		if (link instanceof ObjectPropertyLink)
			writer.name("objectPropertyType").value( ((ObjectPropertyLink)link).getObjectPropertyType().toString());
		if (link instanceof ObjectPropertySpecializationLink) {
			writer.name("specializedLink").value(((ObjectPropertySpecializationLink)link).getSpecializedLinkId());
		}
		writer.name("status").value(link.getStatus().toString());
		writer.name("keyInfo").value(link.getKeyType().toString());
		writer.name("modelIds");
		writeModelIds(writer, link.getModelIds());
		writer.name("weight").value(link.getWeight());
		writer.endObject();
	}
	
	private static void writeLabel(JsonWriter writer, Label label) throws IOException {
		
		if (label == null)
			return;
		
		writer.beginObject();
		writer.name("uri").value(label.getUri());
		writer.name("ns").value(label.getNs());
		writer.name("prefix").value(label.getPrefix());
		writer.name("rdfsLabel").value(label.getRdfsLabel());
		writer.name("rdfsComment").value(label.getRdfsComment());
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
		writer.name("isPartOfKey").value(semanticType.isPartOfKey());
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
	
	public static DirectedWeightedMultigraph<Node, Link> readGraph(JsonReader reader) throws IOException {
		
		DirectedWeightedMultigraph<Node, Link> graph = 
				new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		Node n, source, target;
		Link l;
		Double[] weight = new Double[1];
		HashMap<String, Node> idToNodes = new HashMap<String, Node>();
		
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
		SemanticType userSelectedSemanticType = null;
		List<SemanticType> crfSuggestedSemanticTypes = null;
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
			} else if (key.equals("rdfLiteralType") && reader.peek() != JsonToken.NULL) {
				rdfLiteralType = readLabel(reader);
			} else if (key.equals("userSelectedSemanticType") && reader.peek() != JsonToken.NULL) {
				userSelectedSemanticType = readSemanticType(reader);
			} else if (key.equals("crfSuggestedSemanticTypes") && reader.peek() != JsonToken.NULL) {
				crfSuggestedSemanticTypes = new ArrayList<SemanticType>();
				reader.beginArray();
			    while (reader.hasNext()) {
			    	SemanticType semanticType = readSemanticType(reader);
			    	crfSuggestedSemanticTypes.add(semanticType);
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
    		n = new ColumnNode(id, hNodeId, columnName, rdfLiteralType);
    		((ColumnNode)n).setUserSelectedSemanticType(userSelectedSemanticType);
    		((ColumnNode)n).setCrfSuggestedSemanticTypes(crfSuggestedSemanticTypes);
    	}
		n.setModelIds(modelIds);
    	
    	return n;
	}
	
	private static Link readLink(JsonReader reader, Double[] weight) throws IOException {
		
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
    	
    	Link l = null;
    	if (type == LinkType.ClassInstanceLink) {
    		l = new ClassInstanceLink(id, keyInfo);
    	} else if (type == LinkType.ColumnSubClassLink) {
    		l = new ColumnSubClassLink(id);
    	} else if (type == LinkType.DataPropertyLink) {
    		l = new DataPropertyLink(id, label, keyInfo == LinkKeyInfo.PartOfKey ? true : false);
    	} else if (type == LinkType.DataPropertyOfColumnLink) {
    		l = new DataPropertyOfColumnLink(id, hNodeId);
    	} else if (type == LinkType.ObjectPropertyLink) {
    		l = new ObjectPropertyLink(id, label, objectPropertyType);
    	} else if (type == LinkType.ObjectPropertySpecializationLink) {
    		l = new ObjectPropertySpecializationLink(hNodeId, specializedLinkId);
    	} else if (type == LinkType.SubClassLink) {
    		l = new SubClassLink(id);
    	} else if (type == LinkType.PlainLink) {
    		l = new PlainLink(id, label);
    	} else {
    		l = new PlainLink(id, label);
    	}
    	
    	l.setStatus(status);
    	l.setModelIds(modelIds);
    	return l;
	}
	
	private static Label readLabel(JsonReader reader) throws IOException {
		
		String uri = null;
		String ns = null;
		String prefix = null;
		String rdfsComment = null;
		String rdfsLabel = null;
		
		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("uri") && reader.peek() != JsonToken.NULL) {
				uri = reader.nextString();
			} else if (key.equals("ns") && reader.peek() != JsonToken.NULL) {
				ns = reader.nextString();
			} else if (key.equals("prefix") && reader.peek() != JsonToken.NULL) {
				prefix = reader.nextString();
			} else if (key.equals("rdfsLabel") && reader.peek() != JsonToken.NULL) {
				rdfsLabel = reader.nextString();
			} else if (key.equals("rdfsComment") && reader.peek() != JsonToken.NULL) {
				rdfsComment = reader.nextString();
			} else {
			  reader.skipValue();
			}
		}
    	reader.endObject();
    	
    	Label label = new Label(uri, ns, prefix, rdfsLabel, rdfsComment);
    	return label;
	}
	
	private static SemanticType readSemanticType(JsonReader reader) throws IOException {

		String hNodeId = null;
		Label domain = null;
		Label type = null;
		Origin origin = null;
		Boolean isPartOfKey = null;
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
			} else if (key.equals("isPartOfKey") && reader.peek() != JsonToken.NULL) {
				isPartOfKey = reader.nextBoolean();
			} else if (key.equals("confidenceScore") && reader.peek() != JsonToken.NULL) {
				confidenceScore = reader.nextDouble();
			} else {
			  reader.skipValue();
			}
		}
    	reader.endObject();
    	
    	SemanticType semanticType = new SemanticType(hNodeId, type, domain, origin, confidenceScore, isPartOfKey);
    	return semanticType;	
    }
	
	private static Set<String> readModelIds(JsonReader reader) throws IOException {
		
		Set<String> modelIds = new HashSet<String>();
		
		reader.beginArray();
	    while (reader.hasNext()) {
	    	modelIds.add(reader.nextString());
		}
    	reader.endArray();
    	
    	return modelIds;
	}
}
