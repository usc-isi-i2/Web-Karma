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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;

public class SemanticModel {

	private static Logger logger = LoggerFactory.getLogger(SemanticModel.class);

	protected String id;
	protected String name;
	protected String description;
	protected DirectedWeightedMultigraph<Node, Link> graph;
	protected Set<ColumnNode> sourceColumns;
	protected Map<ColumnNode, ColumnNode> mappingToSourceColumns;
	
	public SemanticModel(
			String id,
			DirectedWeightedMultigraph<Node, Link> graph) {
		this.id = id;
		this.graph = graph;
		this.sourceColumns = GraphUtil.getColumnNodes(this.graph);
		this.mappingToSourceColumns = new HashMap<ColumnNode, ColumnNode>();
		for (ColumnNode c : this.sourceColumns)
			this.mappingToSourceColumns.put(c, c);
		this.setUserSelectedTypeForColumnNodes();
	}
	
	public SemanticModel(
			String id,
			DirectedWeightedMultigraph<Node, Link> graph,
			Set<ColumnNode> sourceColumns,
			Map<ColumnNode, ColumnNode> mappingToSourceColumns) {
		this.id = id;
		this.graph = graph;
		this.sourceColumns = sourceColumns;
		this.mappingToSourceColumns = mappingToSourceColumns;
	}
	
	public SemanticModel(SemanticModel semanticModel) {
		this.id = semanticModel.getId();
		this.name = semanticModel.getName();
		this.description = semanticModel.getDescription();
		this.graph = semanticModel.getGraph();
		this.sourceColumns = semanticModel.getSourceColumns();
		this.mappingToSourceColumns = semanticModel.getMappingToSourceColumns();
	}
	
	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return graph;
	}

	public Map<ColumnNode, ColumnNode> getMappingToSourceColumns() {
		return mappingToSourceColumns;
	}
	
	public Set<ColumnNode> getSourceColumns() {
		return sourceColumns;
	}

	private void setUserSelectedTypeForColumnNodes() {
		
		if (this.graph == null || this.sourceColumns == null)
			return;
		
		for (Node n : this.graph.vertexSet()) {
			
			if (!(n instanceof ColumnNode)) continue;
			
			ColumnNode cn = (ColumnNode)n;
			
			if (!this.sourceColumns.contains(cn)) continue;
			
			Set<Link> incomingLinks = this.graph.incomingEdgesOf(n);
			if (incomingLinks != null && incomingLinks.size() == 1) {
				Link link = incomingLinks.toArray(new Link[0])[0];
				Node domain = link.getSource();
				SemanticType st = new SemanticType(cn.getHNodeId(), link.getLabel(), domain.getLabel(), Origin.User, 1.0, false);
				cn.setUserSelectedSemanticType(st);
			} else
				logger.debug("The column node " + ((ColumnNode)n).getColumnName() + " does not have any domain or it has more than one domain.");
		}
	}
	
//	public void serialize(String fileName) throws IOException {
//		if (graph == null) {
//			logger.error("The input graph is null.");
//			return;
//		}
//		
//		FileOutputStream f = new FileOutputStream(fileName);
//		ObjectOutputStream out = new ObjectOutputStream(f);
//
//		out.writeObject(this);
//		out.flush();
//		out.close();
//	}
//	
//	public static SemanticModel deserialize(String fileName) throws Exception
//	{
////		ByteArrayOutputStream bout = new ByteArrayOutputStream();
//		FileInputStream f = new FileInputStream(fileName);
//        ObjectInputStream in = new ObjectInputStream(f);
//
//        Object obj  = in.readObject();
//        in.close();
//        
//        if (obj instanceof SemanticModel)
//        	return (SemanticModel)obj;
//        else 
//        	return null;
//	}
	
	public void print() {
		logger.info("id: " + this.getId());
		logger.info("name: " + this.getName());
		logger.info("description: " + this.getDescription());
		logger.info(GraphUtil.graphToString(this.graph));
	}
	
	public void exportModelToGraphviz(String filename) throws Exception {
		
		OutputStream out = new FileOutputStream(filename);
		org.kohsuke.graphviz.Graph graphViz = new org.kohsuke.graphviz.Graph();
		
		graphViz.attr("fontcolor", "blue");
		graphViz.attr("remincross", "true");
		graphViz.attr("label", this.getDescription());
//		graphViz.attr("page", "8.5,11");

		org.kohsuke.graphviz.Graph gViz = GraphVizUtil.exportJGraphToGraphviz(this.graph, true);
		gViz.attr("label", "model");
		gViz.id("cluster");
		graphViz.subGraph(gViz);
		graphViz.writeTo(out);
		out.close();
	}
	
	public double getDistance(SemanticModel sm) {
		
		if (this.graph == null || sm.graph == null)
			return -1.0;
		
		if (this.mappingToSourceColumns == null || sm.mappingToSourceColumns == null)
			return -1.0;

		SemanticModel mainModel = this;
		SemanticModel targetModel = sm;
		
		int nodeInsertion = 0, 
				nodeDeletion = 0, 
				linkInsertion = 0, 
				linkDeletion = 0,
				linkRelabeling = 0;
		
		HashMap<String, Integer> mainNodes = new HashMap<String, Integer>();
		HashMap<String, Integer> targetNodes = new HashMap<String, Integer>();

		HashMap<String, Integer> mainLinks = new HashMap<String, Integer>();
		HashMap<String, Integer> targetLinks = new HashMap<String, Integer>();
		
		HashMap<String, Set<String>> mainNodePairToLinks = new HashMap<String, Set<String>>();
		HashMap<String, Set<String>> targetNodePairToLinks = new HashMap<String, Set<String>>();

		String key, sourceStr, targetStr, linkStr;
		Integer count = 0;
		
		// Adding the nodes to the maps
		for (Node n : mainModel.graph.vertexSet()) {
			if (n instanceof InternalNode) key = n.getLabel().getUri();
			else if (n instanceof ColumnNode) {
				ColumnNode cn = mainModel.mappingToSourceColumns.get(n);
				if (cn == null) continue; else key = cn.getId();
			}
			else continue;
			
			count = mainNodes.get(key);
			if (count == null) mainNodes.put(key, 1);
			else mainNodes.put(key, ++count);
		}
		for (Node n : targetModel.graph.vertexSet()) {
			if (n instanceof InternalNode) key = n.getLabel().getUri();
			else if (n instanceof ColumnNode) {
				ColumnNode cn = targetModel.mappingToSourceColumns.get(n);
				if (cn == null) continue; else key = cn.getId();
			}
			else continue;
			
			count = targetNodes.get(key);
			if (count == null) targetNodes.put(key, 1);
			else targetNodes.put(key, ++count);
		}
		
		// Adding the links to the maps
		Node source, target;
		for (Link l : mainModel.graph.edgeSet()) {			
			source = l.getSource();
			target = l.getTarget();
			
			if (!(source instanceof InternalNode)) continue;
			
			sourceStr = source.getLabel().getUri();
			linkStr = l.getLabel().getUri();
			if (target instanceof InternalNode) targetStr = target.getLabel().getUri();
			else if (target instanceof ColumnNode) {
				ColumnNode cn = mainModel.mappingToSourceColumns.get(target);
				if (cn == null) continue; else targetStr = cn.getId();
			}
			else continue;
			
			key = sourceStr + linkStr + targetStr;
			count = mainLinks.get(key);
			if (count == null) mainLinks.put(key, 1);
			else mainLinks.put(key, ++count);
			
			Set<String> links = mainNodePairToLinks.get(sourceStr + targetStr);
			if (links == null) { links = new HashSet<String>(); mainNodePairToLinks.put(sourceStr + targetStr, links); }
			links.add(linkStr);
		}
		for (Link l : targetModel.graph.edgeSet()) {
			source = l.getSource();
			target = l.getTarget();
			
			if (!(source instanceof InternalNode)) continue;
			
			sourceStr = source.getLabel().getUri();
			linkStr = l.getLabel().getUri();
			if (target instanceof InternalNode) targetStr = target.getLabel().getUri();
			else if (target instanceof ColumnNode) {
				ColumnNode cn = targetModel.mappingToSourceColumns.get(target);
				if (cn == null) continue; else targetStr = cn.getId();
			}
			else continue;
			
			key = sourceStr + linkStr + targetStr;
			count = targetLinks.get(key);
			if (count == null) targetLinks.put(key, 1);
			else targetLinks.put(key, ++count);
			
			Set<String> links = targetNodePairToLinks.get(sourceStr + targetStr);
			if (links == null) { links = new HashSet<String>(); targetNodePairToLinks.put(sourceStr + targetStr, links); }
			links.add(linkStr);
		}
		
		int diff;
		for (Entry<String, Integer> mainNodeEntry : mainNodes.entrySet()) {
			count = targetNodes.get(mainNodeEntry.getKey());
			if (count == null) count = 0;
			diff = mainNodeEntry.getValue() - count;
			nodeInsertion += diff > 0? diff : 0;
		}
		
		for (Entry<String, Integer> targetNodeEntry : targetNodes.entrySet()) {
			count = mainNodes.get(targetNodeEntry.getKey());
			if (count == null) count = 0;
			diff = targetNodeEntry.getValue() - count;
			nodeDeletion += diff > 0? diff : 0;
		}

		for (Entry<String, Integer> mainLinkEntry : mainLinks.entrySet()) {
			count = targetLinks.get(mainLinkEntry.getKey());
			if (count == null) count = 0;
			diff = mainLinkEntry.getValue() - count;
			linkInsertion += diff > 0? diff : 0;
		}
		
		for (Entry<String, Integer> targetLinkEntry : targetLinks.entrySet()) {
			count = mainLinks.get(targetLinkEntry.getKey());
			if (count == null) count = 0;
			diff = targetLinkEntry.getValue() - count;
			linkDeletion += diff > 0? diff : 0;
		}

		for (Entry<String, Set<String>> mainNodePairToLinksEntry : mainNodePairToLinks.entrySet()) {
			if (!targetNodePairToLinks.containsKey(mainNodePairToLinksEntry.getKey()))
				continue;
			Set<String> mainRelations = mainNodePairToLinksEntry.getValue();
			Set<String> targetRelations = targetNodePairToLinks.get(mainNodePairToLinksEntry.getKey());
			linkRelabeling += targetRelations.size() > mainRelations.size() ? 
					mainRelations.size() - Sets.intersection(mainRelations, targetRelations).size() :
					targetRelations.size() - Sets.intersection(mainRelations, targetRelations).size();
		}
		
		linkInsertion -= linkRelabeling;
		linkDeletion -= linkRelabeling;

		logger.debug("node insertion cost: " + nodeInsertion);
		logger.debug("node deletion cost: " + nodeDeletion);
		logger.debug("link insertion cost: " + linkInsertion);
		logger.debug("link deletion cost: " + linkDeletion);
		logger.debug("link relabeling cost: " + linkRelabeling);

		return nodeInsertion + nodeDeletion + linkInsertion + linkDeletion + linkRelabeling;
	}
	
	public void writeJson(String filename) throws IOException {
		
		File file = new File(filename);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileOutputStream out = new FileOutputStream(file); 
		JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));
		writer.setIndent("    ");
		try {
			writeModel(writer);
		} catch (Exception e) {
			logger.error("error in writing the model in json!");
	    	e.printStackTrace();
	     } finally {
			writer.close();
		}
		
	}
	
	private void writeModel(JsonWriter writer) throws IOException {
		String nullStr = null;
		writer.beginObject();
		writer.name("id").value(this.getId());
		writer.name("name").value(this.getName());
		writer.name("description").value(this.getDescription());
		writer.name("graph");
		if (this.graph == null) writer.value(nullStr);
		else GraphUtil.writeGraph(this.graph, writer);
		writer.endObject();
	}

	public static SemanticModel readJson(String filename) throws IOException {

		File file = new File(filename);
		if (!file.exists()) {
			logger.error("cannot open the file " + filename);
		}
		
		FileInputStream in = new FileInputStream(file);
		JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	    try {
	    	return readModel(reader);
	    } catch (Exception e) {
	    	logger.error("error in reading the model from json!");
	    	e.printStackTrace();
	    	return null;
	    } finally {
	    	reader.close();
	    }
	}
	
	private static SemanticModel readModel(JsonReader reader) throws IOException {
		
		String id = null;
		String name = null;
		String description = null;
		DirectedWeightedMultigraph<Node, Link> graph = null;
		
		reader.beginObject();
	    while (reader.hasNext()) {
	    	String key = reader.nextName();
			if (key.equals("id") && reader.peek() != JsonToken.NULL) {
				id = reader.nextString();
			} else if (key.equals("name") && reader.peek() != JsonToken.NULL) {
				name = reader.nextString();
			} else if (key.equals("description") && reader.peek() != JsonToken.NULL) {
				description = reader.nextString();
			} else if (key.equals("graph") && reader.peek() != JsonToken.NULL) {
				graph = GraphUtil.readGraph(reader);
			} else {
				reader.skipValue();
			}
		}
    	reader.endObject();
    	
    	SemanticModel semanticModel = new SemanticModel(id, graph);
    	semanticModel.setName(name);
    	semanticModel.setDescription(description);
    	
    	return semanticModel;
	}

}
