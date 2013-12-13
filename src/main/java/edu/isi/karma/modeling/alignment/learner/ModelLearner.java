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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LinkFrequency;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.ModelEvaluation;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.ModelReader;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.rep.alignment.SubClassLink;
import edu.isi.karma.util.RandomGUID;

public class ModelLearner {

	private static Logger logger = LoggerFactory.getLogger(ModelLearner.class);

	private OntologyManager ontologyManager = null;
	private GraphBuilder graphBuilder = null;
	private NodeIdFactory nodeIdFactory = null; 
	private List<ColumnNode> columnNodes = null;
	private SemanticModel semanticModel = null;
	private long lastUpdateTimeOfGraph;
	private ModelLearningGraph modelLearningGraph = null;

	public ModelLearner(OntologyManager ontologyManager, List<ColumnNode> columnNodes) {
		if (ontologyManager == null || 
				columnNodes == null || 
				columnNodes.isEmpty()) {
			logger.error("cannot instanciate model learner!");
			return;
		}
		this.ontologyManager = ontologyManager;
		this.columnNodes = columnNodes;
		this.init();
	}

	public SemanticModel getModel() {
		if (this.semanticModel == null)
			this.learn();
		
		return this.semanticModel;
	}
	
	public void learn() {
		
		if (!isGraphUpToDate()) {
			init();
		}
		
		List<SortableSemanticModel> hypothesisList = this.hypothesize();
		if (hypothesisList != null && !hypothesisList.isEmpty()) {
			SortableSemanticModel m = hypothesisList.get(0);
			this.semanticModel = new SemanticModel(m);
		}
	}
	
	private void init() {
		this.modelLearningGraph = ModelLearningGraph.getInstance(ontologyManager);
		this.lastUpdateTimeOfGraph = this.modelLearningGraph.getLastUpdateTime();
		this.graphBuilder = cloneGraphBuilder(modelLearningGraph.getGraphBuilder());
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
	}
	
	private GraphBuilder cloneGraphBuilder(GraphBuilder graphBuilder) {
		
		GraphBuilder clonedGraphBuilder = null;
		if (graphBuilder == null || graphBuilder.getGraph() == null) {
			clonedGraphBuilder = new GraphBuilder(this.ontologyManager, this.nodeIdFactory, false);
		} else {
			clonedGraphBuilder = new GraphBuilder(this.ontologyManager, graphBuilder.getGraph());
		}
		return clonedGraphBuilder;
	}

	private boolean isGraphUpToDate() {
		if (this.lastUpdateTimeOfGraph < this.modelLearningGraph.getLastUpdateTime())
			return false;
		return true;
	}
	
	public List<SortableSemanticModel> hypothesize() {

		Set<Node> addedNodes = new HashSet<Node>(); //They should be deleted from the graph after computing the semantic models
		CandidateSteinerSets candidateSteinerSets = getCandidateSteinerSets(columnNodes, addedNodes);
		
		if (candidateSteinerSets == null) {
			logger.error("there is no candidate set of steiner nodes.");
			return null;
		}
		
		logger.info("number of steiner sets: " + candidateSteinerSets.numberOfCandidateSets());

		logger.info("updating weights according to training data ...");
		long start = System.currentTimeMillis();
		this.updateWeights();
		long updateWightsElapsedTimeMillis = System.currentTimeMillis() - start;
		logger.info("time to update weights: " + (updateWightsElapsedTimeMillis/1000F));

		
		List<SortableSemanticModel> sortableSemanticModels = new ArrayList<SortableSemanticModel>();
		int count = 1;
		for (SteinerNodes sn : candidateSteinerSets.getSteinerSets()) {
			logger.debug("computing steiner tree for steiner nodes set " + count + " ...");
			logger.debug(sn.getScoreDetailsString());
			DirectedWeightedMultigraph<Node, Link> tree = computeSteinerTree(sn.getNodes());
			count ++;
			if (tree != null) {
				SemanticModel sm = new SemanticModel(new RandomGUID().toString(), 
						tree,
						columnNodes,
						sn.getMappingToSourceColumns()
						);
				SortableSemanticModel sortableSemanticModel = 
						new SortableSemanticModel(sm, sn);
				sortableSemanticModels.add(sortableSemanticModel);
			}
			if (count == ModelingConfiguration.getMaxCandidateModels())
				break;
		}
		
		List<SortableSemanticModel> uniqueModels = new ArrayList<SortableSemanticModel>();
		SortableSemanticModel current, previous;
		if (sortableSemanticModels != null) {
			Collections.sort(sortableSemanticModels);			
			if (sortableSemanticModels.size() > 0)
				uniqueModels.add(sortableSemanticModels.get(0));
			for (int i = 1; i < sortableSemanticModels.size(); i++) {
				current = sortableSemanticModels.get(i);
				previous = sortableSemanticModels.get(i - 1);
				if (current.getScore() == previous.getScore() && current.getCost() == previous.getCost())
					continue;
				uniqueModels.add(current);
			}
		}
		
		return uniqueModels;

	}
	
	private DirectedWeightedMultigraph<Node, Link> computeSteinerTree(Set<Node> steinerNodes) {
		
		if (steinerNodes == null || steinerNodes.size() == 0) {
			logger.error("There is no steiner node.");
			return null;
		}
		
//		System.out.println(steinerNodes.size());
		List<Node> steinerNodeList = new ArrayList<Node>(steinerNodes); 
		
		long start = System.currentTimeMillis();
		UndirectedGraph<Node, Link> undirectedGraph = new AsUndirectedGraph<Node, Link>(this.graphBuilder.getGraph());

		logger.debug("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodeList);
		DirectedWeightedMultigraph<Node, Link> tree = 
				(DirectedWeightedMultigraph<Node, Link>)GraphUtil.asDirectedGraph(steinerTree.getSteinerTree());
		
		logger.debug(GraphUtil.graphToString(tree));
		
		long steinerTreeElapsedTimeMillis = System.currentTimeMillis() - start;
		logger.debug("total number of nodes in steiner tree: " + tree.vertexSet().size());
		logger.debug("total number of edges in steiner tree: " + tree.edgeSet().size());
		logger.debug("time to compute steiner tree: " + (steinerTreeElapsedTimeMillis/1000F));
		
		return tree;
		
//		long finalTreeElapsedTimeMillis = System.currentTimeMillis() - steinerTreeElapsedTimeMillis;
//		DirectedWeightedMultigraph<Node, Link> finalTree = buildOutputTree(tree);
//		logger.info("time to build final tree: " + (finalTreeElapsedTimeMillis/1000F));

//		GraphUtil.printGraph(finalTree);
//		return finalTree; 

	}
	
	private CandidateSteinerSets getCandidateSteinerSets(List<ColumnNode> columnNodes, Set<Node> addedNodes) {

		if (columnNodes == null || columnNodes.isEmpty())
			return null;
		
		int maxNumberOfSteinerNodes = columnNodes.size() * 2;
		CandidateSteinerSets candidateSteinerSets = new CandidateSteinerSets(maxNumberOfSteinerNodes);
		
		if (addedNodes == null) 
			addedNodes = new HashSet<Node>();
		
		Set<SemanticTypeMapping> tempSemanticTypeMappings;
		HashMap<ColumnNode, List<SemanticType>> columnSemanticTypes = new HashMap<ColumnNode, List<SemanticType>>();
		HashMap<String, Integer> semanticTypesCount = new HashMap<String, Integer>();
		List<SemanticType> candidateSemanticTypes;
		String domainUri = "", propertyUri = "";
		
		for (ColumnNode n : columnNodes) {
			
			candidateSemanticTypes = getCandidateSemanticTypes(n);
			columnSemanticTypes.put(n, candidateSemanticTypes);
			
			for (SemanticType semanticType: candidateSemanticTypes) {

				if (semanticType == null || 
						semanticType.getDomain() == null ||
						semanticType.getType() == null) continue;

				domainUri = semanticType.getDomain().getUri();
				propertyUri = semanticType.getType().getUri();

				Integer count = semanticTypesCount.get(domainUri + propertyUri);
				if (count == null) semanticTypesCount.put(domainUri + propertyUri, 1);
				else semanticTypesCount.put(domainUri + propertyUri, count.intValue() + 1);
			}
		}

		for (ColumnNode n : columnNodes) {
			
			candidateSemanticTypes = columnSemanticTypes.get(n);
			if (candidateSemanticTypes == null) continue;
			
			logger.debug("===== Column: " + n.getColumnName());

			Set<SemanticTypeMapping> semanticTypeMappings = new HashSet<SemanticTypeMapping>();
			for (SemanticType semanticType: candidateSemanticTypes) {
				
				tempSemanticTypeMappings = findSemanticTypeInGraph(n, semanticType, semanticTypesCount, addedNodes);
				
				if (tempSemanticTypeMappings != null) 
					semanticTypeMappings.addAll(tempSemanticTypeMappings);
				
				if (tempSemanticTypeMappings == null || tempSemanticTypeMappings.isEmpty()) // No struct in graph is matched with the semantic type, we add a new struct to the graph
				{
					SemanticTypeMapping mp = addSemanticTypeStruct(n, semanticType, addedNodes);
					semanticTypeMappings.add(mp);
				}
			}
			
			candidateSteinerSets.updateSteinerSets(semanticTypeMappings);
		}

		return candidateSteinerSets;
	}
	
	private Set<SemanticTypeMapping> findSemanticTypeInGraph(ColumnNode sourceColumn, SemanticType semanticType, 
			HashMap<String, Integer> semanticTypesCount, Set<Node> addedNodes) {
		
		logger.debug("finding matches for semantic type in the graph ... ");

		if (addedNodes == null)
			addedNodes = new HashSet<Node>();
		
		Set<SemanticTypeMapping> mappings = new HashSet<SemanticTypeMapping>();

		if (semanticType == null) {
			logger.error("semantic type is null.");
			return mappings;
			 
		}
		if (semanticType.getDomain() == null) {
			logger.error("semantic type does not have any domain");
			return mappings;
		}

		if (semanticType.getType() == null) {
			logger.error("semantic type does not have any link");
			return mappings;
		}

		String domainUri = semanticType.getDomain().getUri();
		String propertyUri = semanticType.getType().getUri();
		Double confidence = semanticType.getConfidenceScore();
		Origin origin = semanticType.getOrigin();
		
		Integer countOfSemanticType = semanticTypesCount.get(domainUri + propertyUri);
		if (countOfSemanticType == null) {
			logger.error("count of semantic type should not be null or zero");
			return mappings;
		}
		
		if (domainUri == null || domainUri.isEmpty()) {
			logger.error("semantic type does not have any domain");
			return mappings;
		}

		if (propertyUri == null || propertyUri.isEmpty()) {
			logger.error("semantic type does not have any link");
			return mappings;
		}

		logger.debug("semantic type: " + domainUri + "|" + propertyUri + "|" + confidence + "|" + origin);

		// add dataproperty to existing classes if sl is a data node mapping
		Set<Node> nodesWithSameUriOfDomain = this.graphBuilder.getUriToNodesMap().get(domainUri);
		if (nodesWithSameUriOfDomain != null) {
			for (Node source : nodesWithSameUriOfDomain) {
				if (source instanceof InternalNode) {
					
//					boolean propertyLinkExists = false;
					int countOfExistingPropertyLinks = 0;
					Set<Link> outgoingLinks = this.graphBuilder.getGraph().outgoingEdgesOf(source);
					if (outgoingLinks != null) {
						for (Link l : outgoingLinks) {
							if (l.getLabel().getUri().equals(propertyUri)) {
								if (l.getTarget() instanceof ColumnNode) {
									SemanticTypeMapping mp = 
											new SemanticTypeMapping(sourceColumn, semanticType, (InternalNode)source, l, (ColumnNode)l.getTarget());
									mappings.add(mp);
									countOfExistingPropertyLinks ++;
								}
							}
						}
					}
					
					if (countOfExistingPropertyLinks >= countOfSemanticType.intValue())
						continue;

					String nodeId = new RandomGUID().toString();
					ColumnNode target = new ColumnNode(nodeId, nodeId, sourceColumn.getColumnName(), null);
					if (!this.graphBuilder.addNode(target)) continue;;
					addedNodes.add(target);
					
					String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), target.getId());	
					Link link = new DataPropertyLink(linkId, new Label(propertyUri), false);
					if (!this.graphBuilder.addLink(source, target, link)) continue;;
					
					SemanticTypeMapping mp = new SemanticTypeMapping(sourceColumn, semanticType, (InternalNode)source, link, target);
					mappings.add(mp);
				}
			}
		}
		return mappings;
	}
	
	private SemanticTypeMapping addSemanticTypeStruct(ColumnNode sourceColumn, SemanticType semanticType, Set<Node> addedNodes) {

		logger.debug("adding semantic type to the graph ... ");

		if (addedNodes == null) 
			addedNodes = new HashSet<Node>();

		if (semanticType == null) {
			logger.error("semantic type is null.");
			return null;
			 
		}
		if (semanticType.getDomain() == null) {
			logger.error("semantic type does not have any domain");
			return null;
		}

		if (semanticType.getType() == null) {
			logger.error("semantic type does not have any link");
			return null;
		}

		String domainUri = semanticType.getDomain().getUri();
		String propertyUri = semanticType.getType().getUri();
		Double confidence = semanticType.getConfidenceScore();
		Origin origin = semanticType.getOrigin();
		
		if (domainUri == null || domainUri.isEmpty()) {
			logger.error("semantic type does not have any domain");
			return null;
		}

		if (propertyUri == null || propertyUri.isEmpty()) {
			logger.error("semantic type does not have any link");
			return null;
		}

		logger.debug("semantic type: " + domainUri + "|" + propertyUri + "|" + confidence + "|" + origin);
		
		InternalNode source = null;
		String nodeId;

		nodeId = nodeIdFactory.getNodeId(domainUri);
		source = new InternalNode(nodeId, new Label(domainUri));
		if (!this.graphBuilder.addNodeAndUpdate(source, addedNodes)) return null;
		
		nodeId = new RandomGUID().toString();
		ColumnNode target = new ColumnNode(nodeId, nodeId, sourceColumn.getColumnName(), null);
		if (!this.graphBuilder.addNode(target)) return null;
		addedNodes.add(target);

		String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), target.getId());	
		Link link;
		if (propertyUri.equalsIgnoreCase(ClassInstanceLink.getFixedLabel().getUri()))
			link = new ClassInstanceLink(linkId);
		else {
			Label label = this.ontologyManager.getUriLabel(propertyUri);
			link = new DataPropertyLink(linkId, label, false);
		}
		if (!this.graphBuilder.addLink(source, target, link)) return null;
		
		SemanticTypeMapping mappingStruct = new SemanticTypeMapping(sourceColumn, semanticType, source, link, target);

		return mappingStruct;
	}
	
	private List<SemanticType> getCandidateSemanticTypes(ColumnNode n) {
		
		if (n == null)
			return null;
		
		List<SemanticType> types = new ArrayList<>();
		
		SemanticType userSelectedType = n.getUserSelectedSemanticType();
		if (userSelectedType != null) {
			double probability = 1.0;
			logger.debug("type " + userSelectedType.getCrfModelLabelString() + " is not among CRF suggested types.");
			SemanticType newType = new SemanticType(
					userSelectedType.getHNodeId(),
					userSelectedType.getType(),
					userSelectedType.getDomain(),
					userSelectedType.getOrigin(),
					probability,
					false
					);
			types.add(newType);
		} else {
			List<SemanticType> crfSuggestions = n.getTopKSuggestions(4);
			if (crfSuggestions != null) {
				for (SemanticType st : crfSuggestions) {
					if (userSelectedType != null &&
						st.getCrfModelLabelString().equalsIgnoreCase(userSelectedType.getCrfModelLabelString()))
						continue;
					types.add(st);
				}
			}
		}
	
		return types;
		
	}
	
	private void updateWeights() {

		List<Link> oldLinks = new ArrayList<Link>();
		
		List<Node> sources = new ArrayList<Node>();
		List<Node> targets = new ArrayList<Node>();
		List<Link> newLinks = new ArrayList<Link>();
		List<Double> weights = new ArrayList<Double>();
		
		HashMap<String, LinkFrequency> sourceTargetLinkFrequency = 
				new HashMap<String, LinkFrequency>();
		
		LinkFrequency lf1, lf2;
		
		String key1, key2;
		for (Link link : this.graphBuilder.getGraph().edgeSet()) {
			
			if (!link.getLabel().getUri().equalsIgnoreCase(Uris.PLAIN_LINK_URI)) {
				continue;
			}
			
			key1 = link.getSource().getLabel().getUri() + 
					link.getTarget().getLabel().getUri();
			key2 = link.getTarget().getLabel().getUri() + 
					link.getSource().getLabel().getUri();
			
			lf1 = sourceTargetLinkFrequency.get(key1);
			if (lf1 == null) {
				lf1 = this.graphBuilder.getMoreFrequentLinkBetweenNodes(link.getSource().getLabel().getUri(), link.getTarget().getLabel().getUri());
				sourceTargetLinkFrequency.put(key1, lf1);
			}

			lf2 = sourceTargetLinkFrequency.get(key2);
			if (lf2 == null) {
				lf2 = this.graphBuilder.getMoreFrequentLinkBetweenNodes(link.getTarget().getLabel().getUri(), link.getSource().getLabel().getUri());
				sourceTargetLinkFrequency.put(key2, lf2);
			}
			
			int c = lf1.compareTo(lf2);
			String id = null;
			if (c > 0) {
				sources.add(link.getSource());
				targets.add(link.getTarget());
				
				id = LinkIdFactory.getLinkId(lf1.getLinkUri(), link.getSource().getId(), link.getTarget().getId());
				if (link instanceof ObjectPropertyLink)
					newLinks.add(new ObjectPropertyLink(id, new Label(lf1.getLinkUri()), ((ObjectPropertyLink) link).getObjectPropertyType()));
				else if (link instanceof SubClassLink)
					newLinks.add(new SubClassLink(id));
				
				weights.add(lf1.getWeight());
			} else if (c < 0) {
				sources.add(link.getTarget());
				targets.add(link.getSource());
				
				id = LinkIdFactory.getLinkId(lf2.getLinkUri(), link.getSource().getId(), link.getTarget().getId());
				if (link instanceof ObjectPropertyLink)
					newLinks.add(new ObjectPropertyLink(id, new Label(lf2.getLinkUri()), ((ObjectPropertyLink) link).getObjectPropertyType()));
				else if (link instanceof SubClassLink)
					newLinks.add(new SubClassLink(id));
				
				weights.add(lf2.getWeight());
			} else
				continue;
			
			oldLinks.add(link);
		}
		
		for (Link link : oldLinks)
			this.graphBuilder.getGraph().removeEdge(link);
		
		Link newLink;
		for (int i = 0; i < newLinks.size(); i++) {
			newLink = newLinks.get(i);
			this.graphBuilder.addLink(sources.get(i), targets.get(i), newLink);
			this.graphBuilder.changeLinkWeight(newLink, weights.get(i));
		}
	}

	public static void main(String[] args) throws Exception {

		//		String inputPath = Params.INPUT_DIR;
		String outputPath = Params.OUTPUT_DIR;
		String graphPath = Params.GRAPHS_DIR;
		
//		List<SemanticModel> semanticModels = ModelReader.importSemanticModels(inputPath);
		List<SemanticModel> semanticModels = ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);
		
		List<SemanticModel> trainingData = new ArrayList<SemanticModel>();
		
		OntologyManager ontologyManager = new OntologyManager();
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		for (File f : files) {
			ontologyManager.doImport(f, "UTF-8");
		}
		ontologyManager.updateCache();  
		
		ModelLearningGraph modelLearningGraph;
		ModelLearner modelLearner;
		
		for (int i = 0; i < semanticModels.size(); i++) {
//		int i = 0; {
			trainingData.clear();
			int newSourceIndex = i;
			SemanticModel newSource = semanticModels.get(newSourceIndex);
			
			logger.info("======================================================");
			logger.info(newSource.getDescription());
			logger.info("======================================================");
			
//			int[] trainingModels = {0, 4};
//			for (int n = 0; n < trainingModels.length; n++) { int j = trainingModels[n];
			for (int j = 0; j < semanticModels.size(); j++) {
				if (j != newSourceIndex) 
					trainingData.add(semanticModels.get(j));
			}
			
			modelLearningGraph = ModelLearningGraph.getEmptyInstance(ontologyManager);
			SemanticModel correctModel = newSource;
			List<ColumnNode> columnNodes = correctModel.getColumnNodes();
			modelLearner = new ModelLearner(ontologyManager, columnNodes);
			
			String graphName = graphPath + semanticModels.get(i).getName() + Params.GRAPH_FILE_EXT;
			if (new File(graphName).exists()) {
				// read graph from file
				try {
					logger.info("loading the graph ...");
					DirectedWeightedMultigraph<Node, Link> graph = GraphUtil.importJson(graphName);
					modelLearner.graphBuilder = new GraphBuilder(ontologyManager, graph);
					modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else 
			{
				logger.info("building the graph ...");
				for (SemanticModel sm : trainingData)
					modelLearningGraph.addModel(sm);
				modelLearner.graphBuilder = modelLearningGraph.getGraphBuilder();
				// save graph to file
				try {
					GraphUtil.exportJson(modelLearningGraph.getGraphBuilder().getGraph(), graphName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
	
			List<SortableSemanticModel> hypothesisList = modelLearner.hypothesize();
			List<SortableSemanticModel> topHypotheses = null;
			if (hypothesisList != null) {
				topHypotheses = hypothesisList.size() > ModelingConfiguration.getMaxCandidateModels() ? 
					hypothesisList.subList(0, ModelingConfiguration.getMaxCandidateModels()) : 
					hypothesisList;
			}

			// Updating the weights
//			WeightTuning.getInstance().updateWeights(hypothesisList, correctModel);
			
			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
			
			if (topHypotheses != null)
				for (int k = 0; k < topHypotheses.size() && k < 3; k++) {
					
					String fileExt = null;
					if (k == 0) fileExt = Params.MODEL_RANK1_FILE_EXT;
					else if (k == 1) fileExt = Params.MODEL_RANK2_FILE_EXT;
					else if (k == 2) fileExt = Params.MODEL_RANK3_FILE_EXT;
					SortableSemanticModel m = topHypotheses.get(k);
					new SemanticModel(m).writeJson(Params.MODEL_DIR + 
							newSource.getName() + fileExt);
					
				}
			
			ModelEvaluation me;
			graphs.put("1-correct model", correctModel.getGraph());
			if (topHypotheses != null)
				for (int k = 0; k < topHypotheses.size(); k++) {
					
					SortableSemanticModel m = topHypotheses.get(k);

					me = m.evaluate(correctModel);

					String label = "candidate" + k + 
							"-distance:" + me.getDistance() + 
							"-precision:" + me.getPrecision() + 
							"-recall:" + me.getRecall();
					
					graphs.put(label, m.getGraph());
				}
			
			GraphUtil.exportGraphviz(
					graphs, 
					newSource.getDescription(),
					outputPath + semanticModels.get(i).getName() + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT);
			
		}
	}
	
}
