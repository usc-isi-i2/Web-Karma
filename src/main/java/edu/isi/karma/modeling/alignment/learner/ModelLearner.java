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
import edu.isi.karma.rep.alignment.SubClassLink;
import edu.isi.karma.util.RandomGUID;

public class ModelLearner {

	private static Logger logger = LoggerFactory.getLogger(ModelLearner.class);

	private OntologyManager ontologyManager = null;
	private GraphBuilder graphBuilder = null;
	private NodeIdFactory nodeIdFactory = null; 
	private Set<ColumnNode> columnNodes = null;
	private SemanticModel semanticModel = null;
	private long lastUpdateTimeOfGraph;
	private ModelLearningGraph modelLearningGraph = null;

	public ModelLearner(OntologyManager ontologyManager, Set<ColumnNode> columnNodes) {
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
	
	private CandidateSteinerSets getCandidateSteinerSets(Set<ColumnNode> columnNodes, Set<Node> addedNodes) {

		if (columnNodes == null || columnNodes.isEmpty())
			return null;
		
		int maxNumberOfSteinerNodes = columnNodes.size() * 2;
		CandidateSteinerSets candidateSteinerSets = new CandidateSteinerSets(maxNumberOfSteinerNodes);
		
		if (addedNodes == null) 
			addedNodes = new HashSet<Node>();
		
		Set<SemanticTypeMapping> tempSemanticTypeMappings;
		List<SemanticType> candidateSemanticTypes;
		String domainUri = "", linkUri = "", origin = "";
		double confidence = 0.0;
		
		for (ColumnNode n : columnNodes) {
			
			candidateSemanticTypes = getCandidateSemanticTypes(n);
			
			logger.debug("===== Column: " + n.getColumnName());

			Set<SemanticTypeMapping> semanticTypeMappings = new HashSet<SemanticTypeMapping>();
			for (SemanticType semanticType: candidateSemanticTypes) {
				
				if (semanticType == null) continue;
				
				domainUri = semanticType.getDomain().getUri();
				linkUri = semanticType.getType().getUri();
				confidence = semanticType.getConfidenceScore();
				origin = semanticType.getOrigin().toString();
				
				logger.debug("======================= Semantic Type: " + domainUri + "|" + linkUri + "|" + confidence + "|" + origin);

				if (domainUri == null || domainUri.isEmpty()) {
					logger.error("semantic type does not have any domain");
					continue;
				}

				if (linkUri == null || linkUri.isEmpty()) {
					logger.error("semantic type does not have any link");
					continue;
				}
				
				tempSemanticTypeMappings = findMatchingStructsInGraph(n, semanticType, domainUri, linkUri, n.getColumnName(), addedNodes);
				
				if (tempSemanticTypeMappings != null) 
					semanticTypeMappings.addAll(tempSemanticTypeMappings);
				
				if (tempSemanticTypeMappings == null || tempSemanticTypeMappings.isEmpty()) // No struct in graph is matched with the semantic types, we add a new struct to the graph
				{
					SemanticTypeMapping mp = addSemanticTypeStruct(n, semanticType, addedNodes);
					semanticTypeMappings.add(mp);
				}
			}
			
			candidateSteinerSets.updateSteinerSets(semanticTypeMappings);
		}


		
		return candidateSteinerSets;
	}
	
	private Set<SemanticTypeMapping> findMatchingStructsInGraph(ColumnNode sourceColumn, SemanticType semanticType, 
			String domainUri, String propertyUri, String columnNodeName, Set<Node> addedNodes) {
		
		if (addedNodes == null)
			addedNodes = new HashSet<Node>();
		
		Set<SemanticTypeMapping> mappings = new HashSet<SemanticTypeMapping>();
		
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
					
					if (countOfExistingPropertyLinks >= 1)
						continue;

					String nodeId = new RandomGUID().toString();
					ColumnNode target = new ColumnNode(nodeId, nodeId, columnNodeName, null);
					this.graphBuilder.addNode(target);
					addedNodes.add(target);
					
					String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), target.getId());	
					Link link = new DataPropertyLink(linkId, new Label(propertyUri), false);
					this.graphBuilder.addLink(source, target, link);
					
					SemanticTypeMapping mp = new SemanticTypeMapping(sourceColumn, semanticType, (InternalNode)source, link, target);
					mappings.add(mp);
				}
			}
		}
		return mappings;
	}
	
	private SemanticTypeMapping addSemanticTypeStruct(ColumnNode sourceColumn, SemanticType semanticType, Set<Node> addedNodes) {

		if (addedNodes == null) 
			addedNodes = new HashSet<Node>();

		InternalNode source = null;
		String nodeId, domainUri, linkUri;
		
		domainUri = semanticType.getDomain().getUri();
		linkUri = semanticType.getType().getUri();
		
		nodeId = nodeIdFactory.getNodeId(domainUri);
		source = new InternalNode(nodeId, new Label(domainUri));
		this.graphBuilder.addNodeAndUpdate(source, addedNodes);
		
		nodeId = new RandomGUID().toString();
		ColumnNode target = new ColumnNode(nodeId, nodeId, sourceColumn.getColumnName(), null);
		this.graphBuilder.addNode(target);
		addedNodes.add(target);

		String linkId = LinkIdFactory.getLinkId(linkUri, source.getId(), target.getId());	
		Link link;
		if (linkUri.equalsIgnoreCase(ClassInstanceLink.getFixedLabel().getUri()))
			link = new ClassInstanceLink(linkId);
		else
			link = new DataPropertyLink(linkId, new Label(semanticType.getType().getUri()), false);
		this.graphBuilder.addLink(source, target, link);
		
		SemanticTypeMapping mappingStruct = new SemanticTypeMapping(sourceColumn, semanticType, source, link, target);

		return mappingStruct;
	}
	
	private List<SemanticType> getCandidateSemanticTypes(ColumnNode n) {
		
		if (n == null)
			return null;
		
		List<SemanticType> types = new ArrayList<>();
		
		List<SemanticType> crfSuggestions = n.getTopKSuggestions(4);
		if (crfSuggestions != null)
			types.addAll(crfSuggestions);
		
		SemanticType userSelectedType = n.getUserSelectedSemanticType();
		boolean existInCRFTypes = false;
		if (userSelectedType != null) {
			if (crfSuggestions != null) {
				for (SemanticType t : crfSuggestions) {
					if (userSelectedType.getCrfModelLabelString().equalsIgnoreCase(t.getCrfModelLabelString()))
						existInCRFTypes = true;
				}
			}
			if (!existInCRFTypes) { // add to types if the correct type is not in CRF syggested types
				double probability = 0.5;
				logger.info("type " + userSelectedType.getCrfModelLabelString() + " is not among CRF suggested types.");
				SemanticType newType = new SemanticType(
						userSelectedType.getHNodeId(),
						userSelectedType.getType(),
						userSelectedType.getDomain(),
						userSelectedType.getOrigin(),
						probability,
						false
						);
				types.add(newType);
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
		
//		for (int i = 0; i < semanticModels.size(); i++) {
		int i = 0; {
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
			Set<ColumnNode> columnNodes = correctModel.getColumnNodes();
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
			
			graphs.put("1-correct model", correctModel.getGraph());
			if (topHypotheses != null)
				for (int k = 0; k < topHypotheses.size(); k++) {
					
					SortableSemanticModel m = topHypotheses.get(k);

					double distance = correctModel.getDistance(m);

					String label = "candidate" + k + 
							"--distance:" + distance +
							"---" + m.getDescription();
					
					graphs.put(label, m.getGraph());
				}
			
			GraphUtil.exportGraphviz(
					graphs, 
					newSource.getDescription(),
					outputPath + semanticModels.get(i).getName() + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT);
			
		}
	}
	
}
