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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SubClassLink;
import edu.isi.karma.util.RandomGUID;

public class ModelLearningGraphCompact extends ModelLearningGraph {

	private static Logger logger = LoggerFactory.getLogger(ModelLearningGraphCompact.class);
		
	public ModelLearningGraphCompact(OntologyManager ontologyManager) throws IOException {
		super(ontologyManager, ModelLearningGraphType.Compact);
	}
	
	public ModelLearningGraphCompact(OntologyManager ontologyManager, boolean emptyInstance) {
		super(ontologyManager, emptyInstance, ModelLearningGraphType.Compact);
	}
	
//	protected static ModelLearningGraphCompact getInstance(OntologyManager ontologyManager) {
//		return (ModelLearningGraphCompact)ModelLearningGraph.getInstance(ontologyManager, ModelLearningGraphType.Compact);
//	}
//
//	protected static ModelLearningGraphCompact getEmptyInstance(OntologyManager ontologyManager) {
//		return (ModelLearningGraphCompact)ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);
//	}
	
	private HashMap<Node,Set<Node>> addInternalNodes(SemanticModel model, Set<InternalNode> addedNodes) {
	
		if (model == null || model.getGraph() == null) 
			return null;
		
		HashMap<Node,Set<Node>> internalNodeMatches = new HashMap<Node,Set<Node>>();
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
		
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(n.getUri());
				internalNodeMatches.put(n, matchedNodes);
			}
		}

		return internalNodeMatches;
	}
	
	// FIXME: What if a column node has more than one domain?
	private HashMap<Node,Set<Node>> addColumnNodes(SemanticModel model, HashMap<Node,Node> modelNodeDomains) {
		
		if (model == null || model.getGraph() == null) 
			return null;

		if (modelNodeDomains == null) modelNodeDomains = new HashMap<Node,Node>();
		HashMap<Node,Set<Node>> columnNodeMatches = new HashMap<Node,Set<Node>>();
		
		HashMap<Node,LabeledLink> nodeToDataProperty = new HashMap<Node,LabeledLink>();
		HashMap<String, Integer> dataPropertyCount = new HashMap<String, Integer>(); // key = domainUri + propertyUri
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof ColumnNode) {
				Set<LabeledLink> incomingLinks = model.getGraph().incomingEdgesOf(n);
				if (incomingLinks == null || incomingLinks.isEmpty())
					continue;
				if (incomingLinks != null && incomingLinks.size() == 1) {
					LabeledLink[] incomingLinksArray = incomingLinks.toArray(new LabeledLink[0]);
					Node domain = incomingLinksArray[0].getSource();
					String linkUri =  incomingLinksArray[0].getUri(); 
					String key =  domain.getId() + linkUri;
					Integer count = dataPropertyCount.get(key);
					if (count == null) dataPropertyCount.put(key, 1);
					else dataPropertyCount.put(key, count.intValue() + 1);
					modelNodeDomains.put(n, domain);
					nodeToDataProperty.put(n, incomingLinksArray[0]);
				} else {
					logger.debug("The column node " + ((ColumnNode)n).getColumnName() + " does not have any domain or it has more than one domain.");
					continue;
				}
			}
		}
		
		for (Node n : model.getGraph().vertexSet()) {
			Set<Node> matches = new HashSet<Node>();
			if (n instanceof ColumnNode) {
				Node domain = modelNodeDomains.get(n);
				LabeledLink incomingLink = nodeToDataProperty.get(n);
				Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(domain.getUri());
				if (matchedNodes == null || matchedNodes.isEmpty()) {
					logger.error("no match found for the node " + domain.getUri() + "in the graph");
					return null;
				}
				for (Node m : matchedNodes) {
					String graphKey = m.getId() + incomingLink.getUri(); 
					Set<Node> dataPropertyColumnNodes = this.graphBuilder.getNodeDataProperties().get(graphKey);
					Integer graphDataPropertyCount = this.graphBuilder.getNodeDataPropertyCount().get(graphKey);
					if (graphDataPropertyCount == null) graphDataPropertyCount = 0;
					if (dataPropertyColumnNodes != null) {
						for (Node cn : dataPropertyColumnNodes) {
							matches.add(cn);
						}
					}
					String modelKey = domain.getId() + incomingLink.getUri(); 
					int modelDataPropertyCount = dataPropertyCount.get(modelKey);
					for (int i = 0; i < modelDataPropertyCount - graphDataPropertyCount; i++) {
						Node newNode = null;
						if (n instanceof ColumnNode) {
							ColumnNode c = (ColumnNode)n;
							newNode = new ColumnNode(new RandomGUID().toString(), c.getHNodeId(), c.getColumnName(), c.getRdfLiteralType());
						}
						if (newNode == null) {
							return null;
						}
						if (this.graphBuilder.addNode(newNode)) {
							matches.add(newNode);
							String linkId = LinkIdFactory.getLinkId(incomingLink.getUri(), m.getId(), newNode.getId());
							DataPropertyLink link = new DataPropertyLink(linkId, new Label(incomingLink.getLabel()));
							this.graphBuilder.addLink(m, newNode, link);
						}
					}
					columnNodeMatches.put(n, matches);
				}
			}
		}

		return columnNodeMatches;
	}
	
	private List<HashMap<Node,Node>> updateMapping(List<HashMap<Node,Node>> mappings, 
			Node node, int size, 
			HashMap<Node, Set<Node>> internalNodeMatches,
			HashMap<Node, Set<Node>> columnNodeMatches,
			HashMap<Node,Node> modelNodeDomains) {
		
//		System.out.println("node: " + node.getId());
		
		List<HashMap<Node,Node>> newMappings = new LinkedList<HashMap<Node,Node>>();
		
		Set<Node> matchedNodes = null;
		if (node instanceof InternalNode) 
			matchedNodes = internalNodeMatches.get(node);
		else if (node instanceof ColumnNode)
			matchedNodes = columnNodeMatches.get(node);
		if (matchedNodes == null) {
			return null;
		}

		if (matchedNodes == null || matchedNodes.isEmpty()) {
			logger.error("no match found for the node " + node.getId() + "in the graph");
			return null;
		}

		if (mappings.size() == 0) {
			for (Node n : matchedNodes) {
				HashMap<Node,Node> nodeMap = new HashMap<Node,Node>();
				nodeMap.put(node, n);
				newMappings.add(nodeMap);
//				System.out.println("\t\t" + n.getId());
			}
		} else {
			for (int i = 0; i < mappings.size(); i++) {
				HashMap<Node,Node> nodeMap = mappings.get(i);
				for (Node n : matchedNodes) {
					
					if (n instanceof ColumnNode) {
						Node modelDomain = modelNodeDomains.get(node);
						Node correspondingMatch = nodeMap.get(modelDomain);
						if (this.graphBuilder.getNode2Domain().get(n) != correspondingMatch)
							continue;
						
					}
					HashMap<Node,Node> newMapping = new HashMap<Node,Node>(nodeMap);
					newMapping.put(node, n);
					if (new HashSet<Node>(newMapping.values()).size() != size)
						continue;

//					for (Node nnn : newMapping.values()) {
//						System.out.println("\t\t" + nnn.getId());
//					}
					newMappings.add(newMapping);
				}
			}
		}
		
		return newMappings;
	}
	
	private List<HashMap<Node,Node>> findMappings(SemanticModel model, 
			HashMap<Node, Set<Node>> internalNodeMatches,
			HashMap<Node, Set<Node>> columnNodeMatches,
			HashMap<Node,Node> modelNodeDomains) {
		
		if (model == null || model.getGraph() == null) 
			return null;
		
		List<HashMap<Node,Node>> mappings = new LinkedList<HashMap<Node,Node>>();

		int size = 0;
		for (Node node : model.getGraph().vertexSet()) {
			if (node instanceof InternalNode) {
				size ++;
				mappings = updateMapping(mappings, node, size, internalNodeMatches, columnNodeMatches, modelNodeDomains);
			}
		}

		for (Node node : model.getGraph().vertexSet()) {
			if (node instanceof ColumnNode) {
				size ++;
				mappings = updateMapping(mappings, node, size, internalNodeMatches, columnNodeMatches, modelNodeDomains);
			}
		}
		
		return mappings;
	}
	
	private String generateLinkModelId(String originalModelId, int index) {
		String separator = "/";
		String modelId = originalModelId + separator + index;
		return modelId;
	}
	
	@Override
	public Set<InternalNode> addModel(SemanticModel model) {
				
		// adding the patterns to the graph
		
		if (model == null) 
			return null;
		
		String modelId = model.getId();
		if (this.graphBuilder.getModelIds().contains(modelId)) {
			// FIXME
			// we need to somehow update the graph, but I don't know how to do that yet.
			// so, we rebuild the whole graph from scratch.
			logger.info("the graph already includes the model and needs to be updated, we re-initialize the graph from the repository!");
			initializeFromJsonRepository();
			return null;
		}
		
		// add the model  nodes that are not in the graph
		Set<InternalNode> addedInternalNodes = new HashSet<InternalNode>();
		HashMap<Node, Set<Node>> internalNodeMatches = addInternalNodes(model, addedInternalNodes);
//		if (modelId.equalsIgnoreCase("s21-s-met.json"))
//		for (Entry<Node, Set<Node>> entry : internalNodeMatches.entrySet()) {
//			System.out.println(entry.getKey().getId() + "--> size: " + entry.getValue().size());
//			for (Node n : entry.getValue()) {
//				System.out.println("\t" + n.getId());
//			}
//		}
		HashMap<Node,Node> modelNodeDomains = new HashMap<Node,Node>();
		HashMap<Node, Set<Node>> columnNodeMatches = addColumnNodes(model,modelNodeDomains);
//		if (modelId.equalsIgnoreCase("s21-s-met.json"))
//		for (Entry<Node, Set<Node>> entry : columnNodeMatches.entrySet()) {
//			System.out.println(((ColumnNode)entry.getKey()).getColumnName() + "--> size: " + entry.getValue().size());
//			for (Node n : entry.getValue()) {
//				System.out.println("\t" + ((ColumnNode)n).getColumnName());
//			}
//		}
		
		// find possible mappings between models nodes and the graph nodes
		List<HashMap<Node,Node>> mappings = findMappings(model, 
				internalNodeMatches, 
				columnNodeMatches, 
				modelNodeDomains);
		if (mappings == null) {
			return null;
		}
		
		logger.debug(model.getId() + " --> number of mappings: " + mappings.size());
		
		Node source, target;
		Node n1, n2;

		int index = 1;
//		int i = 0;
		for (HashMap<Node,Node> mapping : mappings) {
			String indexedModelId = generateLinkModelId(modelId, index++); // modelId
			for (LabeledLink e : model.getGraph().edgeSet()) {
				
				source = e.getSource();
				target = e.getTarget();
				
				n1 = mapping.get(source);
				if (n1 == null) {
					logger.error("the mappings does not include the source node " + source.getId());
					return null;
				}
				
				n2 = mapping.get(target);
				if (n2 == null) {
					logger.error("the mappings does not include the target node " + target.getId());
					return null;
				}
				String id = LinkIdFactory.getLinkId(e.getUri(), n1.getId(), n2.getId());
				LabeledLink l = this.graphBuilder.getIdToLinkMap().get(id);
				if (l != null) {
					int numOfPatterns = l.getModelIds().size();
//					this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
//					this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT / (double) (numOfPatterns + 1) );
					if (n2 instanceof InternalNode)
						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT - (0.00001 * numOfPatterns) );
					else
						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
					l.getModelIds().add(indexedModelId);
					n1.getModelIds().add(indexedModelId);
					n2.getModelIds().add(indexedModelId);
				} else {
//					System.out.println("added links: " + i);
//					i++;
					LabeledLink link;
					if (e instanceof DataPropertyLink) 
						link = new DataPropertyLink(id, e.getLabel());
					else if (e instanceof ObjectPropertyLink)
						link = new ObjectPropertyLink(id, e.getLabel(), ((ObjectPropertyLink)e).getObjectPropertyType());
					else if (e instanceof SubClassLink)
						link = new SubClassLink(id);
					else if (e instanceof ClassInstanceLink)
						link = new ClassInstanceLink(id, e.getKeyType());
					else if (e instanceof ColumnSubClassLink)
						link = new ColumnSubClassLink(id);
					else if (e instanceof DataPropertyOfColumnLink)
						link = new DataPropertyOfColumnLink(id, 
								((DataPropertyOfColumnLink)e).getSpecializedColumnHNodeId(),
								((DataPropertyOfColumnLink)e).getSpecializedLinkId()
								);
					else if (e instanceof ObjectPropertySpecializationLink)
						link = new ObjectPropertySpecializationLink(id, ((ObjectPropertySpecializationLink)e).getSpecializedLinkId());
					else {
			    		logger.error("cannot instanciate a link from the type: " + e.getType().toString());
			    		continue;
					}
					link.getModelIds().add(indexedModelId);
					if (!this.graphBuilder.addLink(n1, n2, link, ModelingParams.PATTERN_LINK_WEIGHT)) continue;

					n1.getModelIds().add(indexedModelId);
					n2.getModelIds().add(indexedModelId);
					
				}
			}
		}
				
		this.lastUpdateTime = System.currentTimeMillis();
		return addedInternalNodes;
	}

	public static void main(String[] args) throws Exception {

		OntologyManager ontologyManager = new OntologyManager();
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
			temp = ml.addModel(sm);
			if (temp != null) addedNodes.addAll(temp);
		}
		
		ml.updateGraphUsingOntology(addedNodes);
		try {
			GraphUtil.exportJson(ml.getGraphBuilder().getGraph(), graphName);
			GraphVizUtil.exportJGraphToGraphviz(ml.getGraphBuilder().getGraph(), "main graph", true, false, false, graphVizName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
