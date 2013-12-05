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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;

public class SemanticModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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

	public void serialize(String fileName) throws IOException {
		if (graph == null) {
			logger.error("The input graph is null.");
			return;
		}
		
		FileOutputStream f = new FileOutputStream(fileName);
		ObjectOutputStream out = new ObjectOutputStream(f);

		out.writeObject(this);
		out.flush();
		out.close();
	}
	
	public static SemanticModel deserialize(String fileName) throws Exception
	{
//		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		FileInputStream f = new FileInputStream(fileName);
        ObjectInputStream in = new ObjectInputStream(f);

        Object obj  = in.readObject();
        in.close();
        
        if (obj instanceof SemanticModel)
        	return (SemanticModel)obj;
        else 
        	return null;
	}
	
	public void print() {
		logger.info("id: " + this.getId());
		logger.info("name: " + this.getName());
		logger.info("description: " + this.getDescription());
		logger.info(GraphUtil.graphToString(this.graph));
	}
	
	public void exportModelToGraphviz(String exportDirectory) throws FileNotFoundException {
		
		OutputStream out = new FileOutputStream(exportDirectory + this.getName() + ".dot");
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

		logger.info("node insertion cost: " + nodeInsertion);
		logger.info("node deletion cost: " + nodeDeletion);
		logger.info("link insertion cost: " + linkInsertion);
		logger.info("link deletion cost: " + linkDeletion);
		logger.info("link relabeling cost: " + linkRelabeling);

		return nodeInsertion + nodeDeletion + linkInsertion + linkDeletion + linkRelabeling;
	}
}
