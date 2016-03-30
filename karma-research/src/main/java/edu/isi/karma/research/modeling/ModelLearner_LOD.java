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

package edu.isi.karma.research.modeling;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.python.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphBuilderTopK;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizLabelType;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.ModelEvaluation;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.alignment.TreePostProcess;
import edu.isi.karma.modeling.alignment.learner.CandidateSteinerSets;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraph;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphType;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.alignment.learner.SemanticTypeMapping;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
import edu.isi.karma.modeling.alignment.learner.SteinerNodes;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSemanticTypeStatus;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ModelLearner_LOD {

	private static Logger logger = LoggerFactory.getLogger(ModelLearner_LOD.class);
	private OntologyManager ontologyManager = null;
	private GraphBuilder graphBuilder = null;
	private NodeIdFactory nodeIdFactory = null; 
	private List<Node> steinerNodes = null;
	private SemanticModel semanticModel = null;
//	private long lastUpdateTimeOfGraph;
	private boolean useKarmaAlignmentGraph;

	private static final int NUM_SEMANTIC_TYPES = 4;

	public ModelLearner_LOD(OntologyManager ontologyManager, 
			List<Node> steinerNodes) {
		if (ontologyManager == null || 
				steinerNodes == null || 
				steinerNodes.isEmpty()) {
			logger.error("cannot instanciate model learner!");
			return;
		}
		this.ontologyManager = ontologyManager;
		this.steinerNodes = steinerNodes;
		this.useKarmaAlignmentGraph = true;
	}

	public ModelLearner_LOD(GraphBuilder graphBuilder, 
			List<Node> steinerNodes) {
		if (graphBuilder == null || 
				steinerNodes == null || 
				steinerNodes.isEmpty()) {
			logger.error("cannot instanciate model learner!");
			return;
		}
		this.steinerNodes = steinerNodes;
		this.graphBuilder = graphBuilder;
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
		this.ontologyManager = this.graphBuilder.getOntologyManager();
		this.useKarmaAlignmentGraph = false;
	}

	public SemanticModel getModel() {
		if (this.semanticModel == null)
			try {
				this.learn();
			} catch (Exception e) {
				logger.error("error in learing the semantic model for the source " + this.semanticModel != null ? this.semanticModel.getId() : "");
				e.printStackTrace();
			}

		return this.semanticModel;
	}

	public void learn() throws Exception {

		if (this.useKarmaAlignmentGraph) {
			this.graphBuilder = 
					ModelLearningGraph.getInstance(ontologyManager, ModelLearningGraphType.Compact).getGraphBuilder();
			this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
		}
		
		this.graphBuilder = cloneGraphBuilder(this.graphBuilder); // create a copy of the graph builder
		List<SortableSemanticModel> hypothesisList = this.hypothesize(true, NUM_SEMANTIC_TYPES);
		if (hypothesisList != null && !hypothesisList.isEmpty()) {
			SortableSemanticModel m = hypothesisList.get(0);
			this.semanticModel = new SemanticModel(m);
		} else {
			this.semanticModel = null;
		}
	}

	private GraphBuilder cloneGraphBuilder(GraphBuilder graphBuilder) {

		GraphBuilder clonedGraphBuilder = null;
		if (graphBuilder == null || graphBuilder.getGraph() == null) {
			clonedGraphBuilder = new GraphBuilderTopK(this.ontologyManager, false);
		} else {
			clonedGraphBuilder = new GraphBuilderTopK(this.ontologyManager, graphBuilder.getGraph());
		}
		return clonedGraphBuilder;
	}

//	private boolean isGraphUpToDate() {
//
//		if (this.lastUpdateTimeOfGraph < this.modelLearningGraph.getLastUpdateTime())
//			return false;
//		
//		return true;
//	}

	public List<SortableSemanticModel> hypothesize(boolean useCorrectTypes, int numberOfCandidates) throws Exception {

		logger.info("graph nodes: " + this.graphBuilder.getGraph().vertexSet().size());
		logger.info("graph links: " + this.graphBuilder.getGraph().edgeSet().size());
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontologyManager.getContextId());
		List<SortableSemanticModel> sortableSemanticModels = new ArrayList<SortableSemanticModel>();
		Set<Node> addedNodes = new HashSet<Node>(); //They should be deleted from the graph after computing the semantic models

		List<ColumnNode> columnNodes = new LinkedList<ColumnNode>();
		for (Node n : steinerNodes)
			if (n instanceof ColumnNode)
				columnNodes.add((ColumnNode)n);
		
		logger.info("finding candidate steiner sets ... ");
		CandidateSteinerSets candidateSteinerSets = getCandidateSteinerSets(steinerNodes, useCorrectTypes, numberOfCandidates, addedNodes);

		if (candidateSteinerSets == null || 
				candidateSteinerSets.getSteinerSets() == null || 
				candidateSteinerSets.getSteinerSets().isEmpty()) {
			logger.error("there is no candidate set of steiner nodes.");
			
			DirectedWeightedMultigraph<Node, LabeledLink> tree = 
					new DirectedWeightedMultigraph<Node, LabeledLink>(LabeledLink.class);
			
			for (Node n : steinerNodes)
				tree.addVertex(n);
			
			SemanticModel sm = new SemanticModel(new RandomGUID().toString(), tree);
			SortableSemanticModel sortableSemanticModel = new SortableSemanticModel(sm, null, false);
			sortableSemanticModels.add(sortableSemanticModel);
			return sortableSemanticModels;
		}
		
		logger.info("graph nodes: " + this.graphBuilder.getGraph().vertexSet().size());
		logger.info("graph links: " + this.graphBuilder.getGraph().edgeSet().size());

		logger.info("number of steiner sets: " + candidateSteinerSets.numberOfCandidateSets());

//		logger.info("updating weights according to training data ...");
//		long start = System.currentTimeMillis();
//		this.updateWeights();
//		long updateWightsElapsedTimeMillis = System.currentTimeMillis() - start;
//		logger.info("time to update weights: " + (updateWightsElapsedTimeMillis/1000F));
		
		logger.info("computing steiner trees ...");
		int number = 0;
		for (SteinerNodes sn : candidateSteinerSets.getSteinerSets()) {
			if (sn == null) continue;
			logger.debug("computing steiner tree for steiner nodes set " + number + " ...");
			logger.debug(sn.getScoreDetailsString());
			number++;
//			logger.info("START ...");
			
			List<DirectedWeightedMultigraph<Node, LabeledLink>> topKSteinerTrees;
//			int permutation = 3;
//			if (sn.getNodesCount() > 18)
//				permutation = 2;
			if (this.graphBuilder instanceof GraphBuilderTopK) // which is not in ModelLearner_LOD
				topKSteinerTrees =  ((GraphBuilderTopK)this.graphBuilder).getTopKSteinerTrees(sn, 
						modelingConfiguration.getTopKSteinerTree(), 
						5, 2, true);
			else 
			{
				topKSteinerTrees = new LinkedList<DirectedWeightedMultigraph<Node, LabeledLink>>();
				SteinerTree steinerTree = new SteinerTree(
						new AsUndirectedGraph<Node, DefaultLink>(this.graphBuilder.getGraph()), Lists.newLinkedList(sn.getNodes()));
				WeightedMultigraph<Node, DefaultLink> t = steinerTree.getDefaultSteinerTree();
				TreePostProcess treePostProcess = new TreePostProcess(this.graphBuilder, t);
				if (treePostProcess.getTree() != null)
					topKSteinerTrees.add(treePostProcess.getTree());
			}
			
//			System.out.println(GraphUtil.labeledGraphToString(treePostProcess.getTree()));
			
//			logger.info("END ...");

			for (DirectedWeightedMultigraph<Node, LabeledLink> tree: topKSteinerTrees) {
				if (tree != null) {
//					System.out.println();
					SemanticModel sm = new SemanticModel(new RandomGUID().toString(), 
							tree,
							columnNodes,
							sn.getMappingToSourceColumns()
							);
					SortableSemanticModel sortableSemanticModel = 
							new SortableSemanticModel(sm, sn, false);
					sortableSemanticModels.add(sortableSemanticModel);
					
//					System.out.println(GraphUtil.labeledGraphToString(sm.getGraph()));
//					System.out.println(sortableSemanticModel.getRankingDetails());
//					System.out.println(sortableSemanticModel.getLinkCoherence().printCoherenceList());
				}
			}
			if (number >= modelingConfiguration.getNumCandidateMappings())
				break;

		}

		Collections.sort(sortableSemanticModels, new LOD_SemanticModelComparator());
//		int count = Math.min(sortableSemanticModels.size(), ModelingConfiguration.getNumCandidateMappings());
		logger.info("results are ready ...");
//		sortableSemanticModels.get(0).print();
//		return sortableSemanticModels.subList(0, count);

		List<SortableSemanticModel> uniqueModels = new ArrayList<SortableSemanticModel>();
		SortableSemanticModel current, previous;
		if (sortableSemanticModels != null) {
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
		
		logger.info("results are ready ...");
		return uniqueModels;

	}

	private CandidateSteinerSets getCandidateSteinerSets(List<Node> steinerNodes, boolean useCorrectTypes, int numberOfCandidates, Set<Node> addedNodes) {

		if (steinerNodes == null || steinerNodes.isEmpty())
			return null;

		int maxNumberOfSteinerNodes = steinerNodes.size() * 2;
		CandidateSteinerSets candidateSteinerSets = new CandidateSteinerSets(maxNumberOfSteinerNodes, ontologyManager.getContextId());

		if (addedNodes == null) 
			addedNodes = new HashSet<Node>();

		Set<SemanticTypeMapping> tempSemanticTypeMappings;
		HashMap<ColumnNode, List<SemanticType>> columnSemanticTypes = new HashMap<ColumnNode, List<SemanticType>>();
		HashMap<String, Integer> semanticTypesCount = new HashMap<String, Integer>();
		List<SemanticType> candidateSemanticTypes = null;
		String domainUri = "", propertyUri = "";

		for (Node n : steinerNodes) {

			ColumnNode cn = null;
			if (n instanceof ColumnNode)
				cn = (ColumnNode)n;
			else
				continue;
			
			if (!useCorrectTypes) {
				candidateSemanticTypes = cn.getTopKLearnedSemanticTypes(numberOfCandidates);
			} else if (cn.getSemanticTypeStatus() == ColumnSemanticTypeStatus.UserAssigned) {
				candidateSemanticTypes = cn.getUserSemanticTypes();
			}
			
			if (candidateSemanticTypes == null) {
				logger.error("No candidate semantic type found for the column " + cn.getColumnName());
				return null;
			}
			
			columnSemanticTypes.put(cn, candidateSemanticTypes);

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

		long numOfMappings = 1;
		
		for (Node n : steinerNodes) {

			if (n instanceof InternalNode) 
				continue;
			
			ColumnNode cn = null;
			if (n instanceof ColumnNode)
				cn = (ColumnNode)n;
			else
				continue;
			
			candidateSemanticTypes = columnSemanticTypes.get(n);
			if (candidateSemanticTypes == null) continue;

			logger.info("===== Column: " + cn.getColumnName());

			Set<SemanticTypeMapping> semanticTypeMappings = new HashSet<SemanticTypeMapping>();
			for (SemanticType semanticType: candidateSemanticTypes) {

				logger.info("\t" + semanticType.getConfidenceScore() + " :" + semanticType.getModelLabelString());

				if (semanticType == null || 
						semanticType.getDomain() == null ||
						semanticType.getType() == null) continue;

				domainUri = semanticType.getDomain().getUri();
				propertyUri = semanticType.getType().getUri();
				Integer countOfSemanticType = semanticTypesCount.get(domainUri + propertyUri);
				logger.debug("count of semantic type: " +  countOfSemanticType);

//				if (cn.hasUserType()) {
//					HashMap<SemanticType, LabeledLink> domainLinks = 
//							GraphUtil.getDomainLinks(this.graphBuilder.getGraph(), cn, cn.getUserSemanticTypes());
//					if (domainLinks != null && !domainLinks.isEmpty()) {
//						for (SemanticType st : cn.getUserSemanticTypes()) {
//							semanticTypeMappings = new HashSet<SemanticTypeMapping>();
//							LabeledLink domainLink = domainLinks.get(st);
//							if (domainLink.getSource() == null || !(domainLink.getSource() instanceof InternalNode))
//								continue;
//							SemanticTypeMapping mp = 
//									new SemanticTypeMapping(cn, st, (InternalNode)domainLink.getSource(), domainLink, cn);
//							semanticTypeMappings.add(mp);
//							candidateSteinerSets.updateSteinerSets(semanticTypeMappings);
//						}
//					}
//				} else 
				{

					tempSemanticTypeMappings = findSemanticTypeInGraph(cn, semanticType, semanticTypesCount, addedNodes);
					logger.debug("number of matches for semantic type: " +  
						 + (tempSemanticTypeMappings == null ? 0 : tempSemanticTypeMappings.size()));
	
					if (tempSemanticTypeMappings != null) 
						semanticTypeMappings.addAll(tempSemanticTypeMappings);
	
					int countOfMatches = tempSemanticTypeMappings == null ? 0 : tempSemanticTypeMappings.size();
//					if (countOfMatches < countOfSemanticType) 
					if (countOfMatches == 0) // No struct in graph is matched with the semantic type, we add a new struct to the graph
					{
						SemanticTypeMapping mp = addSemanticTypeStruct(cn, semanticType, addedNodes);
						if (mp != null)
							semanticTypeMappings.add(mp);
					}
				}
			}
			//			System.out.println("number of matches for column " + n.getColumnName() + 
			//					": " + (semanticTypeMappings == null ? 0 : semanticTypeMappings.size()));
			logger.debug("number of matches for column " + cn.getColumnName() + 
					": " + (semanticTypeMappings == null ? 0 : semanticTypeMappings.size()));
			numOfMappings *= (semanticTypeMappings == null || semanticTypeMappings.isEmpty() ? 1 : semanticTypeMappings.size());

			logger.debug("number of candidate steiner sets before update: " + candidateSteinerSets.getSteinerSets().size());
			candidateSteinerSets.updateSteinerSets(semanticTypeMappings);
			logger.debug("number of candidate steiner sets after update: " + candidateSteinerSets.getSteinerSets().size());
		}

		for (Node n : steinerNodes) {
			if (n instanceof InternalNode) {
				candidateSteinerSets.updateSteinerSets((InternalNode)n);
			}
		}
		
		//		System.out.println("number of possible mappings: " + numOfMappings);
		logger.info("number of possible mappings: " + numOfMappings);

		return candidateSteinerSets;
	}

	private Set<SemanticTypeMapping> findSemanticTypeInGraph(ColumnNode sourceColumn, SemanticType semanticType, 
			HashMap<String, Integer> semanticTypesCount, Set<Node> addedNodes) {

		logger.debug("finding matches for semantic type in the graph ... ");
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontologyManager.getContextId());
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
		//		Set<Node> foundInternalNodes = new HashSet<Node>();
		Double weight = null;
		Set<SemanticTypeMapping> semanticTypeMatches = this.graphBuilder.getSemanticTypeMatches().get(domainUri + propertyUri);
		if (semanticTypeMatches != null) {
			for (SemanticTypeMapping stm : semanticTypeMatches) {

				SemanticTypeMapping mp = 
						new SemanticTypeMapping(sourceColumn, semanticType, stm.getSource(), stm.getLink(), stm.getTarget());
				mappings.add(mp);
				weight = stm.getLink().getWeight();
				//				foundInternalNodes.add(stm.getSource());
			}
		}

		logger.debug("adding data property to the found internal nodes ...");

		Integer count;
		boolean allowMultipleSamePropertiesPerNode = modelingConfiguration.isMultipleSamePropertyPerNode();
		Set<Node> nodesWithSameUriOfDomain = this.graphBuilder.getUriToNodesMap().get(domainUri);
		if (nodesWithSameUriOfDomain != null) { 
			for (Node source : nodesWithSameUriOfDomain) {
				count = this.graphBuilder.getNodeDataPropertyCount().get(source.getId() + propertyUri);

				if (count != null) {
					if (allowMultipleSamePropertiesPerNode) {
						if (count >= countOfSemanticType.intValue())
							continue;
					} else {
						if (count >= 1) 
							continue;
					}
				}


				String nodeId = new RandomGUID().toString();
				ColumnNode target = new ColumnNode(nodeId, nodeId, sourceColumn.getColumnName(), null);
				if (!this.graphBuilder.addNode(target)) continue;;
				addedNodes.add(target);

				String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), target.getId());	
				LabeledLink link = new DataPropertyLink(linkId, new Label(propertyUri));
				
				boolean result = weight == null ? this.graphBuilder.addLink(source, target, link) : this.graphBuilder.addLink(source, target, link, weight);
				if (!result) continue;;

				SemanticTypeMapping mp = new SemanticTypeMapping(sourceColumn, semanticType, (InternalNode)source, link, target);
				mappings.add(mp);
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

		Set<Node> nodesWithSameUri = this.graphBuilder.getUriToNodesMap().get(domainUri);
		if (nodesWithSameUri != null && !nodesWithSameUri.isEmpty()) {
			InternalNode copyFrom = (InternalNode)nodesWithSameUri.iterator().next();
			source = this.graphBuilder.copyNode(copyFrom, false);
			if (source == null) return null;
		} else {
			nodeId = nodeIdFactory.getNodeId(domainUri);
			source = new InternalNode(nodeId, new Label(domainUri));
			if (!this.graphBuilder.addNodeAndUpdate(source, addedNodes)) return null;
		}

		nodeId = new RandomGUID().toString();
		ColumnNode target = new ColumnNode(nodeId, nodeId, sourceColumn.getColumnName(), null);
		if (!this.graphBuilder.addNode(target)) return null;
		addedNodes.add(target);

		String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), target.getId());	
		LabeledLink link;
		if (propertyUri.equalsIgnoreCase(ClassInstanceLink.getFixedLabel().getUri()))
			link = new ClassInstanceLink(linkId);
		else {
			Label label = this.ontologyManager.getUriLabel(propertyUri);
			link = new DataPropertyLink(linkId, label);
		}
		if (!this.graphBuilder.addLink(source, target, link)) return null;

		SemanticTypeMapping mappingStruct = new SemanticTypeMapping(sourceColumn, semanticType, source, link, target);

		return mappingStruct;
	}

	private static double roundDecimals(double d, int k) {
		String format = "";
		for (int i = 0; i < k; i++) format += "#";
        DecimalFormat DForm = new DecimalFormat("#." + format);
        return Double.valueOf(DForm.format(d));
	}

	private static void getStatistics(List<SemanticModel> semanticModels) {
		for (int i = 0; i < semanticModels.size(); i++) {
			SemanticModel source = semanticModels.get(i);
			int attributeCount = source.getColumnNodes().size();
			int nodeCount = source.getGraph().vertexSet().size();
			int linkCount = source.getGraph().edgeSet().size();
			int datanodeCount = 0;
			int classNodeCount = 0;
			for (Node n : source.getGraph().vertexSet()) {
				if (n instanceof InternalNode) classNodeCount++;
				if (n instanceof ColumnNode) datanodeCount++;
			}
//			System.out.println(attributeCount + "\t" + nodeCount + "\t" + linkCount + "\t" + classNodeCount + "\t" + datanodeCount);
			
			List<ColumnNode> columnNodes = source.getColumnNodes();

			if (columnNodes == null)
				return;
			
			
			int numberOfAttributesWhoseTypeIsFirstCRFType = 0;
			int numberOfAttributesWhoseTypeIsInCRFTypes = 0;
			for (ColumnNode cn : columnNodes) {
				List<SemanticType> userSemanticTypes = cn.getUserSemanticTypes();
				List<SemanticType> top4Suggestions = cn.getTopKLearnedSemanticTypes(4);

				for (int j = 0; j < top4Suggestions.size(); j++) {
					SemanticType st = top4Suggestions.get(j);
					if (userSemanticTypes != null) {
						for (SemanticType t : userSemanticTypes) {
							if (st.getModelLabelString().equalsIgnoreCase(t.getModelLabelString())) {
								if (j == 0) numberOfAttributesWhoseTypeIsFirstCRFType ++;
								numberOfAttributesWhoseTypeIsInCRFTypes ++;
								j = top4Suggestions.size();
								break;
							}
						}
					} 
				}

			}

//			System.out.println(numberOfAttributesWhoseTypeIsInCRFTypes + "\t" + numberOfAttributesWhoseTypeIsFirstCRFType);
			
			System.out.println(
					attributeCount + "\t" + 
					nodeCount + "\t" + 
					linkCount + "\t" + 
					(linkCount - attributeCount) + "\t" +
					classNodeCount + "\t" + 
					datanodeCount + "\t" + 
					numberOfAttributesWhoseTypeIsInCRFTypes + "\t" + 
					numberOfAttributesWhoseTypeIsFirstCRFType);

		}
	}
	
	
	public static void main(String[] args) throws Exception {

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().registerByKarmaHome("/Users/mohsen/karma/");
		contextParameters.setParameterValue(ContextParameter.USER_DIRECTORY_PATH, "/Users/mohsen/karma/");
		contextParameters.setParameterValue(ContextParameter.USER_CONFIG_DIRECTORY, "/Users/mohsen/karma/config");

		OntologyManager ontologyManager = new OntologyManager(contextParameters.getId());
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		if (files == null) {
			logger.error("no ontology to import at " + ff.getAbsolutePath());
			return;
		}

		for (File f : files) {
			if (f.getName().endsWith(".owl") || 
					f.getName().endsWith(".rdf") || 
					f.getName().endsWith(".n3") || 
					f.getName().endsWith(".ttl") || 
					f.getName().endsWith(".xml")) {
				logger.info("Loading ontology file: " + f.getAbsolutePath());
				ontologyManager.doImport(f, "UTF-8");
			}
		}
		ontologyManager.updateCache(); 

		boolean onlyGenerateSemanticTypeStatistics = false;
		boolean onlyUseOntology = true;
		boolean useCorrectType = true;
		int numberOfCandidates = 1;
		boolean onlyEvaluateInternalLinks = true; 
		int maxPatternSize = 1;
		boolean recreateGraphs = true;
		boolean useModifiedDS = false;
		String lodDSName = "ds";
//		String lodDSName = "saam";
//		String lodDSName = "musicbrainz";
		
		String modelDir;
		if (useModifiedDS) modelDir = Params.ROOT_DIR + "models-json-modified/";
		else modelDir = Params.MODEL_DIR;
		
//		FileUtils.cleanDirectory(new File(graphPath));
		
		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(modelDir, Params.MODEL_MAIN_FILE_EXT);

		if (onlyGenerateSemanticTypeStatistics) {
			getStatistics(semanticModels);
			return;
		}

		ModelLearner_LOD modelLearner = null;


		String filePath = Params.RESULTS_DIR + "temp/";
		String filename = "";

		filename += "results";
		if (useModifiedDS) filename += ".modified";
		filename += "." + lodDSName;
		filename += useCorrectType ? ".correct":".k=" + numberOfCandidates;
		filename += onlyUseOntology ? ".p0" : ".p" + maxPatternSize;
		filename += onlyEvaluateInternalLinks ? ".internal":".all";
		filename += ".csv"; 

		
		PrintWriter resultFile = new PrintWriter(new File(filePath + filename));

		resultFile.println("source \t p \t r \t t \n");

		String sourceName;

		for (int i = 0; i < semanticModels.size(); i++) {
//		for (int i = 0; i <= 10; i++) {
//		int i = 11; {

			int newSourceIndex = i;
			SemanticModel newSource = semanticModels.get(newSourceIndex);
		
			sourceName = newSource.getId().substring(0, newSource.getId().lastIndexOf("."));
			
			String outputDir = Params.OUTPUT_DIR + sourceName + "/";
			File f = new File(outputDir);
			if (!f.exists()) {
				f.mkdir();
			}

			String graphDir = Params.GRAPHS_DIR + sourceName + "/";
			f = new File(graphDir);
			if (!f.exists()) {
				f.mkdir();
			}

			logger.info("======================================================");
			logger.info(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			System.out.println(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			logger.info("======================================================");

			SemanticModel correctModel = newSource;
			List<ColumnNode> columnNodes = correctModel.getColumnNodes();

			List<Node> steinerNodes = new LinkedList<Node>(columnNodes);

			String graphName, graphPath;
			graphName = lodDSName + ".p" + maxPatternSize; 
			
			graphPath = graphDir + graphName + Params.GRAPH_JSON_FILE_EXT;

			if (onlyUseOntology) {
				modelLearner = new ModelLearner_LOD(new GraphBuilder(ontologyManager, false), steinerNodes);
			} else if (!recreateGraphs && new File(graphPath).exists()) {
				// read graph from file
				try {
					logger.info("loading the graph ...");
					DirectedWeightedMultigraph<Node, DefaultLink> graph = GraphUtil.importJson(graphPath);
					modelLearner = new ModelLearner_LOD(new GraphBuilderTopK(ontologyManager, graph), steinerNodes);
				} catch (Exception e) {
					e.printStackTrace();
					resultFile.close();
					return;
				}
			} else 
			{
				logger.info("building the graph ...");
				// create and save the graph to file
//				GraphBuilder_Popularity b = new GraphBuilder_Popularity(ontologyManager, 
//						Params.LOD_OBJECT_PROPERIES_FILE, 
//						Params.LOD_DATA_PROPERIES_FILE);
				String patternPath;
				if (lodDSName.equalsIgnoreCase("ds")) 
					patternPath = Params.LOD_DIR + sourceName + "/" + Params.PATTERNS_OUTPUT_DIR;
				else
					patternPath = Params.LOD_DIR + lodDSName + "/" + Params.PATTERNS_OUTPUT_DIR;
					
				GraphBuilder_LOD_Pattern b = new GraphBuilder_LOD_Pattern(ontologyManager, patternPath, maxPatternSize);
				b.serialize(graphPath);
				modelLearner = new ModelLearner_LOD(b.getGraphBuilder(), steinerNodes);
			}

			long start = System.currentTimeMillis();

			List<SortableSemanticModel> hypothesisList = modelLearner.hypothesize(useCorrectType, numberOfCandidates);

			long elapsedTimeMillis = System.currentTimeMillis() - start;
			float elapsedTimeSec = elapsedTimeMillis/1000F;

			List<SortableSemanticModel> topHypotheses = null;
			if (hypothesisList != null) {
				
//				for (SortableSemanticModel sss : hypothesisList) {
//					ModelEvaluation mmm = sss.evaluate(correctModel);
//					System.out.println(mmm.getPrecision() + ", " + mmm.getRecall());
//				}
				topHypotheses = hypothesisList.size() > 10 ? 
						hypothesisList.subList(0, 10) : 
							hypothesisList;
			}

			Map<String, SemanticModel> models = 
					new TreeMap<String, SemanticModel>();

			ModelEvaluation me;
			models.put("1-correct model", correctModel);
			if (topHypotheses != null)
				for (int k = 0; k < topHypotheses.size(); k++) {

					SortableSemanticModel m = topHypotheses.get(k);

					me = m.evaluate(correctModel, onlyEvaluateInternalLinks, false);

					String label = "candidate " + k + "\n" + 
//							(m.getSteinerNodes() == null ? "" : m.getSteinerNodes().getScoreDetailsString()) +
//							"link coherence:" + (m.getLinkCoherence() == null ? "" : m.getLinkCoherence().getCoherenceValue()) + "\n";
							"link coherence:" + (m.getLinkCoherence() == null ? "" : m.getCoherenceString()) + "\n";
					label += (m.getSteinerNodes() == null || m.getSteinerNodes().getCoherence() == null) ? 
							"" : "node coherence:" + m.getSteinerNodes().getCoherence().getCoherenceValue() + "\n";
					label += "confidence:" + m.getConfidenceScore() + "\n";
					label += m.getSteinerNodes() == null ? "" : "mapping score:" + m.getSteinerNodes().getScore() + "\n";
					label +=
							"cost:" + roundDecimals(m.getCost(), 6) + "\n" +
							//								"-distance:" + me.getDistance() + 
							"-precision:" + me.getPrecision() + 
							"-recall:" + me.getRecall();

					models.put(label, m);

					if (k == 0) { // first rank model
						System.out.println("precision: " + me.getPrecision() + 
								", recall: " + me.getRecall() + 
								", time: " + elapsedTimeSec);
						logger.info("precision: " + me.getPrecision() + 
								", recall: " + me.getRecall() + 
								", time: " + elapsedTimeSec);
						String s = newSource.getName() + "\t" + me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec;
						resultFile.println(s);

					}
				}
						
			String outputPath, outputName;
			outputName = newSource.getName();
			if (useModifiedDS) outputName += ".modified";
			outputName += lodDSName.equals("ds") ? "" : ("." + lodDSName);
			outputName += onlyUseOntology ? ".p0" : ".p" + maxPatternSize;
			outputName += Params.GRAPHVIS_OUT_FILE_EXT;
			outputPath = outputDir + outputName;

			GraphVizUtil.exportSemanticModelsToGraphviz(
					models, 
					newSource.getName(),
					outputPath,
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					true,
					true);

		}

		resultFile.close();

	}

}
