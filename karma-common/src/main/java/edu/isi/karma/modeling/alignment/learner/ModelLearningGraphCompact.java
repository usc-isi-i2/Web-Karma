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

package edu.isi.karma.modeling.alignment.learner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizLabelType;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeSupportingModelsComparator;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class ModelLearningGraphCompact extends ModelLearningGraph {

	private static Logger logger = LoggerFactory.getLogger(ModelLearningGraphCompact.class);
	
	public ModelLearningGraphCompact(OntologyManager ontologyManager) throws IOException {
		super(ontologyManager, ModelLearningGraphType.Compact);
	}
	
	public ModelLearningGraphCompact(OntologyManager ontologyManager, boolean emptyInstance) {
		super(ontologyManager, emptyInstance, ModelLearningGraphType.Compact);
	}
	
	private void addInternalNodes(SemanticModel model, Set<InternalNode> addedNodes) {
	
		if (model == null || model.getGraph() == null) 
			return;
		
		if (addedNodes == null) addedNodes = new HashSet<InternalNode>();

		HashMap<String, Integer> uriCount = new HashMap<String, Integer>();
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				Integer count = uriCount.get(n.getUri());
				if (count == null) uriCount.put(n.getUri(), 1);
				else uriCount.put(n.getUri(), count.intValue() + 1);
			}
		}
		
		for (String uri : uriCount.keySet()) {
			int modelNodeCount = uriCount.get(uri);
			Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(uri);
			int graphNodeCount = matchedNodes == null ? 0 : matchedNodes.size();
			
			for (int i = 0; i < modelNodeCount - graphNodeCount; i++) {
				String id = this.nodeIdFactory.getNodeId(uri);
				Node n = new InternalNode(id, new Label(uri));
				if (this.graphBuilder.addNode(n))
					addedNodes.add((InternalNode)n);
			}
		}		
	}
	
	private HashMap<Node,Node> getInternalNodeMapping(SemanticModel model) {
		
		HashMap<Node,Node> internalNodeMapping = 
				new HashMap<Node,Node>();
		
		HashMap<String, List<Node>> uriMatchedNodes = 
				new HashMap<String, List<Node>>();
		
		String uri;
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				uri = n.getUri();
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(uri);
				if (sortedMatchedNodes == null) {
					sortedMatchedNodes = new ArrayList<Node>();
					Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(uri);
					if (matchedNodes != null) sortedMatchedNodes.addAll(matchedNodes);
					Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
					uriMatchedNodes.put(uri, sortedMatchedNodes);
				}
			}
		}

		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				List<Node> sortedMatchedNodes = uriMatchedNodes.get(n.getUri());
				internalNodeMapping.put(n, sortedMatchedNodes.get(0));
				sortedMatchedNodes.remove(0);
			}
		}

		return internalNodeMapping;
	}
	
	private HashMap<Node,Node> getColumnNodeMapping(SemanticModel model, 
			HashMap<Node,Node> internalNodeMapping) {
		
		if (model == null || model.getGraph() == null) 
			return null;
		
		if (internalNodeMapping == null || internalNodeMapping.isEmpty()) 
			return null;

		HashMap<String,List<Node>> dataPropertyColumnNodes = new HashMap<String,List<Node>>();
		
		HashMap<Node,Node> columnNodeMapping = new HashMap<Node,Node>();
		
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof ColumnNode) {
				ColumnNode c = (ColumnNode)n;
				Set<LabeledLink> domainLinks = GraphUtil.getDomainLinksInLabeledGraph(model.getGraph(), (ColumnNode)n);
				if (domainLinks == null || domainLinks.isEmpty())
					continue;
				for (LabeledLink l : domainLinks) {
					if (l.getSource() == null) continue;
					Node domain = l.getSource();
					Node mappedNode = internalNodeMapping.get(domain);
					String linkUri =  l.getUri(); 
					String key =  mappedNode.getId() + linkUri;
					
					List<Node> sortedMatchedNodes;
					if (dataPropertyColumnNodes.containsKey(key)) { 
						sortedMatchedNodes = dataPropertyColumnNodes.get(key);
					} else {
						Set<Node> matchedColumnNodes = this.graphBuilder.getNodeDataProperties().get(key);
						sortedMatchedNodes = new ArrayList<Node>();
						if (matchedColumnNodes != null) sortedMatchedNodes.addAll(matchedColumnNodes);
						Collections.sort(sortedMatchedNodes, new NodeSupportingModelsComparator());
						dataPropertyColumnNodes.put(key, sortedMatchedNodes);
					}
					
					if (sortedMatchedNodes.isEmpty()) {
						ColumnNode newNode = new ColumnNode(new RandomGUID().toString(), 
								c.getHNodeId(), c.getColumnName(), c.getRdfLiteralType());
						if (this.graphBuilder.addNode(newNode)) {
							columnNodeMapping.put(n, newNode);
						}
					} else {
						columnNodeMapping.put(n, sortedMatchedNodes.get(0));
						sortedMatchedNodes.remove(0);
					}
				}
			}
		}
		return columnNodeMapping;
	}
	
	private void addLinks(SemanticModel model, 
			HashMap<Node, Node> internalNodeMapping, 
			HashMap<Node, Node> columnNodeMapping,
			boolean useOriginalWeights) {
		
		if (model == null) 
			return;
		
		String modelId = model.getId();

		Node source, target;
		Node n1, n2;

		HashMap<Node,Node> mapping = new HashMap<Node,Node>();
		if (internalNodeMapping != null) mapping.putAll(internalNodeMapping);
		if (columnNodeMapping != null) mapping.putAll(columnNodeMapping);
		
		for (LabeledLink e : model.getGraph().edgeSet()) {
			
			source = e.getSource();
			target = e.getTarget();
			
			n1 = mapping.get(source);
			if (n1 == null) {
//					logger.warn("the mappings does not include the source node " + source.getId());
				continue;
			}
			
			n2 = mapping.get(target);
			if (n2 == null) {
//					logger.warn("the mappings does not include the target node " + target.getId());
				continue;
			}
			
			String id = LinkIdFactory.getLinkId(e.getUri(), n1.getId(), n2.getId());
			LabeledLink l = this.graphBuilder.getIdToLinkMap().get(id);
			if (l != null) {
				int numOfPatterns = l.getModelIds().size();
//					this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
//					this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT / (double) (numOfPatterns + 1) );
				if (useOriginalWeights) {
					double currentW = l.getWeight();
					double newW = model.getGraph().getEdgeWeight(e);
					if (newW < currentW)
						this.graphBuilder.changeLinkWeight(l, newW);
				} else {
					if (n2 instanceof InternalNode)
//						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT / (double) (numOfPatterns + 1) );
						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT - (0.00001 * numOfPatterns) );
					else
						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
				}
				l.getModelIds().add(modelId);
				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
			} else {
//					System.out.println("added links: " + i);
//					i++;
				LabeledLink link = e.copy(id);
				
				if (link == null) {
		    		logger.error("cannot instanciate a link from the type: " + e.getType().toString());
		    		continue;
				}
				link.setStatus(LinkStatus.Normal); // all the links in learning graph are normal

				if (link.getModelIds() != null)
					link.getModelIds().clear();
				link.getModelIds().add(modelId);

				if (useOriginalWeights) {
					if (!this.graphBuilder.addLink(n1, n2, link, model.getGraph().getEdgeWeight(e))) continue;
				} else {
					if (!this.graphBuilder.addLink(n1, n2, link, ModelingParams.PATTERN_LINK_WEIGHT)) continue;
				}

				n1.getModelIds().add(modelId);
				n2.getModelIds().add(modelId);
				
			}
		}
	}
	
	@Override
	public Set<InternalNode> addModel(SemanticModel model, boolean useOriginalWeights) {
				
		// adding the patterns to the graph
		
		if (model == null) 
			return null;
		
//		String modelId = model.getId();
//		if (this.graphBuilder.getModelIds().contains(modelId)) {
//			// FIXME	
//			// we need to somehow update the graph, but I don't know how to do that yet.
//			// so, we rebuild the whole graph from scratch.
//			logger.info("the graph already includes the model and needs to be updated, we re-initialize the graph from the repository!");
//			initializeFromJsonRepository();
//			return null;
//		}
		
		// add the model  nodes that are not in the graph
		Set<InternalNode> addedInternalNodes = new HashSet<InternalNode>();
		this.addInternalNodes(model, addedInternalNodes);
		HashMap<Node, Node> internalNodeMapping = this.getInternalNodeMapping(model);

		HashMap<Node, Node> columnNodeMapping = this.getColumnNodeMapping(model, internalNodeMapping);
		
		this.addLinks(model, internalNodeMapping, columnNodeMapping, useOriginalWeights);

		this.lastUpdateTime = System.currentTimeMillis();
		return addedInternalNodes;
	}

	public static void main(String[] args) throws Exception {

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		OntologyManager ontologyManager = new OntologyManager(contextParameters.getId());
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		for (File f : files) {
			ontologyManager.doImport(f, "UTF-8");
		}
		ontologyManager.updateCache();  
		
		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);

		String graphPath = Params.GRAPHS_DIR;
		String graphName = graphPath + "graph.json";
		String graphVizName = graphPath + "graph.dot";
		

		ModelLearningGraph ml = ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);
		int i = 0;
		Set<InternalNode> addedNodes = new HashSet<InternalNode>();
		Set<InternalNode> temp;
		for (SemanticModel sm : semanticModels) {
			i++;
			if (i == 4) continue;
			System.out.println(sm.getId());
			temp = ml.addModel(sm, false);
			if (temp != null) addedNodes.addAll(temp);
		}
		
		ml.updateGraphUsingOntology(addedNodes);
		try {
			GraphUtil.exportJson(ml.getGraphBuilder().getGraph(), graphName, true, true);
			GraphVizUtil.exportJGraphToGraphviz(ml.getGraphBuilder().getGraph(), 
					"main graph", 
					true, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					false, 
					false, 
					graphVizName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
