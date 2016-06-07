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
import java.util.Map;
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
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class ModelLearningGraphCompact_Old extends ModelLearningGraph {

	private static Logger logger = LoggerFactory.getLogger(ModelLearningGraphCompact_Old.class);
	private static int MAX_MAPPING_SIZE = 1000;
	
	public ModelLearningGraphCompact_Old(OntologyManager ontologyManager) throws IOException {
		super(ontologyManager, ModelLearningGraphType.Compact);
	}
	
	public ModelLearningGraphCompact_Old(OntologyManager ontologyManager, boolean emptyInstance) {
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
		
		HashMap<Node,Set<Node>> internalNodeMatches = new HashMap<>();
		if (addedNodes == null) addedNodes = new HashSet<>();

		HashMap<String, Integer> uriCount = new HashMap<>();
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				Integer count = uriCount.get(n.getUri());
				if (count == null) uriCount.put(n.getUri(), 1);
				else uriCount.put(n.getUri(), count.intValue() + 1);
			}
		}
		
		for (Map.Entry<String, Integer> stringIntegerEntry : uriCount.entrySet()) {
			int modelNodeCount = stringIntegerEntry.getValue();
			Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(stringIntegerEntry.getKey());
			int graphNodeCount = matchedNodes == null ? 0 : matchedNodes.size();
			
			for (int i = 0; i < modelNodeCount - graphNodeCount; i++) {
				String id = this.nodeIdFactory.getNodeId(stringIntegerEntry.getKey());
				Node n = new InternalNode(id, new Label(stringIntegerEntry.getKey()));
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
	
	private HashMap<Node,Set<Node>> addColumnNodes(SemanticModel model, 
			HashMap<Node,Set<LabeledLink>> modelNodeDomains,
			HashMap<Node,Set<LabeledLink>> graphNodeDomains) {
		
		if (model == null || model.getGraph() == null) 
			return null;

		if (modelNodeDomains == null) modelNodeDomains = new HashMap<>();
		if (graphNodeDomains == null) graphNodeDomains = new HashMap<>();
		
		HashMap<Node,Set<Node>> columnNodeMatches = new HashMap<>();
		
		HashMap<String, Integer> dataPropertyCount = new HashMap<>(); // key = domainUri + propertyUri
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof ColumnNode) {
				Set<LabeledLink> domainLinks = GraphUtil.getDomainLinksInLabeledGraph(model.getGraph(), (ColumnNode)n);
				if (domainLinks == null || domainLinks.isEmpty())
					continue;
				for (LabeledLink l : domainLinks) {
					if (l.getSource() == null) continue;
					Node domain = l.getSource();
					String linkUri =  l.getUri(); 
					String key =  domain.getId() + linkUri;
					Integer count = dataPropertyCount.get(key);
					if (count == null) dataPropertyCount.put(key, 1);
					else dataPropertyCount.put(key, count.intValue() + 1);
				}
				modelNodeDomains.put(n, domainLinks);
			} 
		}
		
		for (Node n : model.getGraph().vertexSet()) {
			Set<Node> matches = new HashSet<>();
			if (n instanceof ColumnNode) {
				Set<LabeledLink> domainLinks = modelNodeDomains.get(n);
				if (domainLinks == null || domainLinks.isEmpty())
					continue;
				for (LabeledLink l : domainLinks) {
					if (l.getSource() == null) continue;
					Node domain = l.getSource();
					LabeledLink incomingLink = l;
					Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(domain.getUri());
					if (matchedNodes == null || matchedNodes.isEmpty()) {
						logger.error("no match found for the node " + domain.getUri() + " in the graph");
						return null;
					}
					for (Node m : matchedNodes) {
						String graphKey = m.getId() + incomingLink.getUri(); 
						Set<Node> dataPropertyColumnNodes = this.graphBuilder.getNodeDataProperties().get(graphKey);
						Integer graphDataPropertyCount = this.graphBuilder.getNodeDataPropertyCount().get(graphKey);
						if (graphDataPropertyCount == null) graphDataPropertyCount = 0;
						if (dataPropertyColumnNodes != null) {
							for (Node cn : dataPropertyColumnNodes) {
								if (cn instanceof ColumnNode) {
									matches.add(cn);
									graphNodeDomains.put(cn, 
											GraphUtil.getDomainLinksInDefaultGraph(this.graphBuilder.getGraph(), (ColumnNode)cn));
								}
							}
						}
						String modelKey = domain.getId() + incomingLink.getUri(); 
						int modelDataPropertyCount = dataPropertyCount.get(modelKey);
						for (int i = 0; i < modelDataPropertyCount - graphDataPropertyCount; i++) {
							Node newNode = null;
							if (n instanceof ColumnNode) {
								ColumnNode c = (ColumnNode)n;
								newNode = new ColumnNode(new RandomGUID().toString(), c.getHNodeId(), 
										c.getColumnName(), c.getRdfLiteralType(), c.getLanguage());
							}
							if (newNode == null) {
								return null;
							}
							if (this.graphBuilder.addNode(newNode)) {
								String linkId = LinkIdFactory.getLinkId(incomingLink.getUri(), m.getId(), newNode.getId());
								DataPropertyLink link = new DataPropertyLink(linkId, new Label(incomingLink.getLabel()));
								this.graphBuilder.addLink(m, newNode, link, ModelingParams.PATTERN_LINK_WEIGHT);
								matches.add(newNode);
								graphNodeDomains.put(newNode, 
										GraphUtil.getDomainLinksInDefaultGraph(this.graphBuilder.getGraph(), (ColumnNode)newNode));
							}
						}
					}
				}
				columnNodeMatches.put(n, matches);
			}
		}

		return columnNodeMatches;
	}
	
	private List<HashMap<Node,Node>> updateMapping(List<HashMap<Node,Node>> mappings, 
			Node node, int size, 
			HashMap<Node, Set<Node>> internalNodeMatches,
			HashMap<Node, Set<Node>> columnNodeMatches,
			HashMap<Node,Set<LabeledLink>> modelNodeDomains,
			HashMap<Node,Set<LabeledLink>> graphNodeDomains) {
		
//		System.out.println("node: " + node.getId());
		
		List<HashMap<Node,Node>> newMappings = new LinkedList<>();
		
		Set<Node> matchedNodes = null;
		if (node instanceof InternalNode && internalNodeMatches != null) 
			matchedNodes = internalNodeMatches.get(node);
		else if (node instanceof ColumnNode && columnNodeMatches != null)
			matchedNodes = columnNodeMatches.get(node);
		if (matchedNodes == null) {
			return null;//mappings;
		}

		if (matchedNodes == null || matchedNodes.isEmpty()) {
			logger.error("no match found for the node " + node.getId() + " in the graph");
			return null;
		}

		if (mappings.isEmpty()) {
			for (Node n : matchedNodes) {
				HashMap<Node,Node> nodeMap = new HashMap<>();
				nodeMap.put(node, n);
				newMappings.add(nodeMap);
//				System.out.println("\t\t" + n.getId());
			}
		} else {
			for (int i = 0; i < mappings.size(); i++) {
				HashMap<Node,Node> nodeMap = mappings.get(i);
				Set<Node> correspondingMatches = new HashSet<>();
				for (Node n : matchedNodes) {
					if (n instanceof ColumnNode) {
						Set<LabeledLink> modelDomainLinks = modelNodeDomains.get(node);
						if (modelDomainLinks != null) {
							for (LabeledLink l : modelDomainLinks) {
								if (l.getSource() != null) {
									if (nodeMap.containsKey(l.getSource())) {
										correspondingMatches.add(nodeMap.get(l.getSource()));
									}
								}
							}
						}
						Set<LabeledLink> graphDomainLinks = graphNodeDomains.get(n);
						Set<Node> domainNodes = new HashSet<>();
						if (graphDomainLinks != null) {
							for (LabeledLink l : graphDomainLinks) {
								if (l.getSource() != null) {
									domainNodes.add(l.getSource());
								}
							}
						}
						boolean found = false;
						if (domainNodes != null) {
							for (Node domain : domainNodes) {
								if (correspondingMatches.contains(domain))
									found = true;
							}
						}
						if (!found)
							continue;
						
					}
					HashMap<Node,Node> newMapping = new HashMap<>(nodeMap);
					newMapping.put(node, n);
					if (new HashSet<>(newMapping.values()).size() != size)
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
			HashMap<Node,Set<LabeledLink>> modelNodeDomains,
			HashMap<Node,Set<LabeledLink>> graphNodeDomains) {
		
		if (model == null || model.getGraph() == null) 
			return null;
		
		List<HashMap<Node,Node>> mappings = new LinkedList<>();

//		logger.info("max mapping size: " + MAX_MAPPING_SIZE);
		
		int size = 0;
		for (Node node : model.getGraph().vertexSet()) {
			if (node instanceof InternalNode) {
				size ++;
				mappings = updateMapping(mappings, node, size, internalNodeMatches, columnNodeMatches, modelNodeDomains, graphNodeDomains);
//				System.out.println(mappings.size());
				if (mappings != null && mappings.size() >= MAX_MAPPING_SIZE)
					mappings = mappings.subList(0,  MAX_MAPPING_SIZE);
//				System.out.println(mappings.size());
			}
		}

		for (Node node : model.getGraph().vertexSet()) {
			if (node instanceof ColumnNode) {
				size ++;
				mappings = updateMapping(mappings, node, size, internalNodeMatches, columnNodeMatches, modelNodeDomains, graphNodeDomains);
//				System.out.println(mappings.size());
				if (mappings != null && mappings.size() >= MAX_MAPPING_SIZE)
					mappings = mappings.subList(0,  MAX_MAPPING_SIZE);
//				System.out.println(mappings.size());
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
	public Set<InternalNode> addModel(SemanticModel model, PatternWeightSystem weightSystem) {
				
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
		Set<InternalNode> addedInternalNodes = new HashSet<>();
		HashMap<Node, Set<Node>> internalNodeMatches = addInternalNodes(model, addedInternalNodes);
//		if (modelId.equalsIgnoreCase("s21-s-met.json"))
//		for (Entry<Node, Set<Node>> entry : internalNodeMatches.entrySet()) {
//			System.out.println(entry.getKey().getId() + "--> size: " + entry.getValue().size());
//			for (Node n : entry.getValue()) {
//				System.out.println("\t" + n.getId());
//			}
//		}
		HashMap<Node,Set<LabeledLink>> modelNodeDomains = new HashMap<>();
		HashMap<Node,Set<LabeledLink>> graphNodeDomains = new HashMap<>();
		HashMap<Node, Set<Node>> columnNodeMatches = addColumnNodes(model, modelNodeDomains, graphNodeDomains);
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
				modelNodeDomains,
				graphNodeDomains);
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
					if (weightSystem == PatternWeightSystem.OriginalWeights) {
						double currentW = l.getWeight();
						double newW = model.getGraph().getEdgeWeight(e);
						if (newW < currentW)
							this.graphBuilder.changeLinkWeight(l, newW);
					} else if (weightSystem == PatternWeightSystem.JWSPaperFormula) {
						if (n2 instanceof InternalNode) {
							// wl - x/(n+1)
							// wl = 1
							// x = (numOfPatterns + 1)
							// n = totalNumberOfPatterns
							this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT - 
									((double) (numOfPatterns + 1) / (double) (this.totalNumberOfKnownModels + 1) ));
//							this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT - (0.00001 * numOfPatterns) );
						} else {
							this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
						}
					} else {
						this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
					}
					l.getModelIds().add(indexedModelId);
					n1.getModelIds().add(indexedModelId);
					n2.getModelIds().add(indexedModelId);
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
					link.getModelIds().add(indexedModelId);

					if (weightSystem == PatternWeightSystem.OriginalWeights) {
						if (!this.graphBuilder.addLink(n1, n2, link, model.getGraph().getEdgeWeight(e))) continue;
					} else {
						if (!this.graphBuilder.addLink(n1, n2, link, ModelingParams.PATTERN_LINK_WEIGHT)) continue;
					}

					n1.getModelIds().add(indexedModelId);
					n2.getModelIds().add(indexedModelId);
					
				}
			}
		}
				
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
		Set<InternalNode> addedNodes = new HashSet<>();
		Set<InternalNode> temp;
		for (SemanticModel sm : semanticModels) {
			i++;
			if (i == 4) continue;
			System.out.println(sm.getId());
			temp = ml.addModel(sm, PatternWeightSystem.JWSPaperFormula);
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
