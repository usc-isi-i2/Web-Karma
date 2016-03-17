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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.jgrapht.graph.WeightedMultigraph;
import org.python.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
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
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphCompact;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphType;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.alignment.learner.PatternWeightSystem;
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
import edu.isi.karma.semantictypes.evaluation.EvaluateMRR;
import edu.isi.karma.semantictypes.evaluation.MRRItem;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ModelLearner_KnownModels {

	private static Logger logger = LoggerFactory.getLogger(ModelLearner_KnownModels.class);
	private OntologyManager ontologyManager = null;
	private GraphBuilder graphBuilder = null;
	private NodeIdFactory nodeIdFactory = null; 
	private List<Node> steinerNodes = null;
	
	public ModelLearner_KnownModels(OntologyManager ontologyManager, 
			List<Node> steinerNodes) {
		if (ontologyManager == null || 
				steinerNodes == null || 
				steinerNodes.isEmpty()) {
			logger.error("cannot instanciate model learner!");
			return;
		}
		GraphBuilder gb = ModelLearningGraph.getInstance(ontologyManager, ModelLearningGraphType.Compact).getGraphBuilder();
		this.ontologyManager = ontologyManager;
		this.steinerNodes = steinerNodes;
//		if (this.steinerNodes != null) Collections.sort(this.steinerNodes);
		this.graphBuilder = cloneGraphBuilder(gb); // create a copy of the graph builder
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
	}

	public ModelLearner_KnownModels(GraphBuilder graphBuilder, 
			List<Node> steinerNodes) {
		if (graphBuilder == null || 
				steinerNodes == null || 
				steinerNodes.isEmpty()) {
			logger.error("cannot instanciate model learner!");
			return;
		}
		this.ontologyManager = graphBuilder.getOntologyManager();
		this.steinerNodes = steinerNodes;
//		if (this.steinerNodes != null) Collections.sort(this.steinerNodes);
		this.graphBuilder = cloneGraphBuilder(graphBuilder); // create a copy of the graph builder
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
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

	public List<SortableSemanticModel> hypothesize(boolean useCorrectTypes, int numberOfCandidates) throws Exception {

		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontologyManager.getContextId());
		List<SortableSemanticModel> sortableSemanticModels = new ArrayList<SortableSemanticModel>();
		Set<Node> addedNodes = new HashSet<Node>(); //They should be deleted from the graph after computing the semantic models

		List<ColumnNode> columnNodes = new LinkedList<ColumnNode>();
		for (Node n : steinerNodes)
			if (n instanceof ColumnNode)
				columnNodes.add((ColumnNode)n);
		
//		long start = System.currentTimeMillis();

		logger.info("finding candidate steiner sets ... ");
		CandidateSteinerSets candidateSteinerSets = getCandidateSteinerSets(steinerNodes, useCorrectTypes, numberOfCandidates, addedNodes);

//		long elapsedTimeMillis = System.currentTimeMillis() - start;
//		float elapsedTimeSec = elapsedTimeMillis/1000F;
//		System.out.println(elapsedTimeSec);

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
			logger.info("computing steiner tree for steiner nodes set " + number + " ...");
//			logger.info(sn.getScoreDetailsString());
//			for (Entry<ColumnNode, SemanticTypeMapping> n : sn.getColumnNodeInfo().entrySet()) {
//				System.out.println(sn.getMappingToSourceColumns().get(n.getKey()).getColumnName() + "---" +
//						n.getValue().getLink().getId());
//			}
			number++;
//			logger.info("START ...");
			
			List<DirectedWeightedMultigraph<Node, LabeledLink>> topKSteinerTrees;
			if (this.graphBuilder instanceof GraphBuilderTopK) {
				topKSteinerTrees =  ((GraphBuilderTopK)this.graphBuilder).getTopKSteinerTrees(sn, 
						modelingConfiguration.getTopKSteinerTree(), 
						5, 3, true);
			} 
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
//					System.out.println(sortableSemanticModel.getLinkCoherence().printCoherenceList());
				}
			}
			if (number >= modelingConfiguration.getNumCandidateMappings())
				break;

		}
		
		Collections.sort(sortableSemanticModels);
//		int count = Math.min(sortableSemanticModels.size(), modelingConfiguration.getNumCandidateMappings());

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

//	private DirectedWeightedMultigraph<Node, LabeledLink> computeSteinerTree(Set<Node> steinerNodes) {
//
//		if (steinerNodes == null || steinerNodes.size() == 0) {
//			logger.error("There is no steiner node.");
//			return null;
//		}
//
//		//		System.out.println(steinerNodes.size());
//		List<Node> steinerNodeList = new ArrayList<Node>(steinerNodes); 
//
//		long start = System.currentTimeMillis();
//		UndirectedGraph<Node, DefaultLink> undirectedGraph = new AsUndirectedGraph<Node, DefaultLink>(this.graphBuilder.getGraph());
//
//		logger.debug("computing steiner tree ...");
//		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodeList);
//		DirectedWeightedMultigraph<Node, LabeledLink> tree = new TreePostProcess(this.graphBuilder, steinerTree.getDefaultSteinerTree(), null, false).getTree();
//		//(DirectedWeightedMultigraph<Node, LabeledLink>)GraphUtil.asDirectedGraph(steinerTree.getDefaultSteinerTree());
//
//		logger.debug(GraphUtil.labeledGraphToString(tree));
//
//		long steinerTreeElapsedTimeMillis = System.currentTimeMillis() - start;
//		logger.debug("total number of nodes in steiner tree: " + tree.vertexSet().size());
//		logger.debug("total number of edges in steiner tree: " + tree.edgeSet().size());
//		logger.debug("time to compute steiner tree: " + (steinerTreeElapsedTimeMillis/1000F));
//
//		return tree;
//
//		//		long finalTreeElapsedTimeMillis = System.currentTimeMillis() - steinerTreeElapsedTimeMillis;
//		//		DirectedWeightedMultigraph<Node, Link> finalTree = buildOutputTree(tree);
//		//		logger.info("time to build final tree: " + (finalTreeElapsedTimeMillis/1000F));
//
//		//		GraphUtil.printGraph(finalTree);
//		//		return finalTree; 
//
//	}

	private CandidateSteinerSets getCandidateSteinerSets(List<Node> steinerNodes, boolean useCorrectTypes, int numberOfCandidates, Set<Node> addedNodes) {

		if (steinerNodes == null || steinerNodes.isEmpty())
			return null;

		int maxNumberOfSteinerNodes = steinerNodes.size() * 2;
		CandidateSteinerSets candidateSteinerSets = new CandidateSteinerSets(maxNumberOfSteinerNodes,ontologyManager.getContextId());

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

			Set<SemanticTypeMapping> semanticTypeMappings = null; 
			
//			if (cn.hasUserType()) {
//				HashMap<SemanticType, LabeledLink> domainLinks = 
//						GraphUtil.getDomainLinks(this.graphBuilder.getGraph(), cn, cn.getUserSemanticTypes());
//				if (domainLinks != null && !domainLinks.isEmpty()) {
//					for (SemanticType st : cn.getUserSemanticTypes()) {
//						semanticTypeMappings = new HashSet<SemanticTypeMapping>();
//						LabeledLink domainLink = domainLinks.get(st);
//						if (domainLink == null || domainLink.getSource() == null || !(domainLink.getSource() instanceof InternalNode))
//							continue;
//						SemanticTypeMapping mp = 
//								new SemanticTypeMapping(cn, st, (InternalNode)domainLink.getSource(), domainLink, cn);
//						semanticTypeMappings.add(mp);
//						candidateSteinerSets.updateSteinerSets(semanticTypeMappings);
//					}
//				}
//			} else 
			{
				semanticTypeMappings = new HashSet<SemanticTypeMapping>();
				for (SemanticType semanticType: candidateSemanticTypes) {
	
					logger.info("\t" + semanticType.getConfidenceScore() + " :" + semanticType.getModelLabelString());
	
					if (semanticType == null || 
							semanticType.getDomain() == null ||
							semanticType.getType() == null) continue;
	
					domainUri = semanticType.getDomain().getUri();
					
					propertyUri = semanticType.getType().getUri();
					Integer countOfSemanticType = semanticTypesCount.get(domainUri + propertyUri);
					logger.debug("count of semantic type: " +  countOfSemanticType);
	
					
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

				//			System.out.println("number of matches for column " + n.getColumnName() + 
				//					": " + (semanticTypeMappings == null ? 0 : semanticTypeMappings.size()));
				logger.debug("number of matches for column " + cn.getColumnName() + 
						": " + (semanticTypeMappings == null ? 0 : semanticTypeMappings.size()));
				numOfMappings *= (semanticTypeMappings == null || semanticTypeMappings.isEmpty() ? 1 : semanticTypeMappings.size());
	
				logger.debug("number of candidate steiner sets before update: " + candidateSteinerSets.getSteinerSets().size());
				candidateSteinerSets.updateSteinerSets(semanticTypeMappings);
				logger.debug("number of candidate steiner sets after update: " + candidateSteinerSets.getSteinerSets().size());
			}
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
		Set<SemanticTypeMapping> semanticTypeMatches = this.graphBuilder.getSemanticTypeMatches().get(domainUri + propertyUri);
		if (semanticTypeMatches != null) {
			for (SemanticTypeMapping stm : semanticTypeMatches) {

				SemanticTypeMapping mp = 
						new SemanticTypeMapping(sourceColumn, semanticType, stm.getSource(), stm.getLink(), stm.getTarget());
				mappings.add(mp);
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
				if (!this.graphBuilder.addLink(source, target, link)) continue;;

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

		nodeId = nodeIdFactory.getNodeId(domainUri);
		source = new InternalNode(nodeId, new Label(domainUri));
		if (!this.graphBuilder.addNodeAndUpdate(source, addedNodes)) return null;

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

	private static void getStatistics(List<SemanticModel> semanticModels, List<String> modelFiles, int numOfCandidates) {
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
			
			MRRItem mrrItem = null;
			if (modelFiles != null) {
				mrrItem = EvaluateMRR.calculateMRRValue(modelFiles.get(i),4);
			}

//			System.out.println(numberOfAttributesWhoseTypeIsInCRFTypes + "\t" + numberOfAttributesWhoseTypeIsFirstCRFType);
//			System.out.println(source.getName());
			System.out.println(
					attributeCount + "\t" + 
					nodeCount + "\t" + 
					linkCount + "\t" + 
					(linkCount - attributeCount) + "\t" +
					classNodeCount + "\t" + 
					datanodeCount + "\t" + 
					numberOfAttributesWhoseTypeIsFirstCRFType + "\t" + 
					numberOfAttributesWhoseTypeIsInCRFTypes + "\t" + 
					(mrrItem != null ? roundDecimals(mrrItem.getAccuracy(),2) : 0) + "\t" + 
					(mrrItem != null ? roundDecimals(mrrItem.getMrr(),2) : 0));

		}
	}
	
	public static void runResearchEvaluation() throws Exception {

		/***
		 * When running with k=1, change the flag "multiple.same.property.per.node" to true so all attributes have at least one semantic types
		 */
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().registerByKarmaHome("/Users/mohsen/karma/");
		contextParameters.setParameterValue(ContextParameter.USER_DIRECTORY_PATH, "/Users/mohsen/karma/");
		contextParameters.setParameterValue(ContextParameter.USER_CONFIG_DIRECTORY, "/Users/mohsen/karma/config");
		contextParameters.setParameterValue(ContextParameter.TRAINING_EXAMPLE_MAX_COUNT, "1000000");
		contextParameters.setParameterValue(ContextParameter.SEMTYPE_MODEL_DIRECTORY, "/Users/mohsen/karma/semantic-type-files/");
		contextParameters.setParameterValue(ContextParameter.JSON_MODELS_DIR, "/Users/mohsen/karma/models-json/");
		contextParameters.setParameterValue(ContextParameter.GRAPHVIZ_MODELS_DIR, "/Users/mohsen/karma/models-graphviz/");
		contextParameters.setParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY, "/Users/mohsen/karma/python/");
		contextParameters.setParameterValue(ContextParameter.EVALUATE_MRR, "/Users/mohsen/karma/evaluate-mrr/");
		PythonRepository pythonRepository = new PythonRepository(true, contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonRepositoryRegistry.getInstance().register(pythonRepository);

		//		String inputPath = Params.INPUT_DIR;
		String graphPath = Params.GRAPHS_DIR;
		File semFilesFolder = new File(contextParameters.getParameterValue(ContextParameter.SEMTYPE_MODEL_DIRECTORY));

		//		List<SemanticModel> semanticModels = ModelReader.importSemanticModels(inputPath);
		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);

		
		File[] sources = new File(Params.SOURCE_DIR).listFiles();
		File[] r2rmlModels = new File(Params.R2RML_DIR).listFiles();
		if (sources.length > 0 && sources[0].getName().startsWith(".")) 
			sources = (File[]) ArrayUtils.removeElement(sources, sources[0]);
		if (r2rmlModels.length > 0 && r2rmlModels[0].getName().startsWith(".")) 
			r2rmlModels = (File[]) ArrayUtils.removeElement(r2rmlModels, r2rmlModels[0]);


		List<SemanticModel> trainingData = new ArrayList<SemanticModel>();
		File[] trainingSources;
		File[] trainingModels;
		File trainingSource = null;
		File trainingModel = null;
		File testSource;
		File testModel;
		
		OntologyManager ontologyManager = new OntologyManager(contextParameters.getId());
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		for (File f : files) {
			if(f.getName().startsWith(".") || f.isDirectory()) {
				continue; //Ignore . files
			}
			ontologyManager.doImport(f, "UTF-8");
		}
		ontologyManager.updateCache();  

		ModelLearningGraph modelLearningGraph = null;
		
		ModelLearner_KnownModels modelLearner;
		
		boolean onlyGenerateSemanticTypeStatistics = false;
		boolean iterativeEvaluation = true;
		boolean useCorrectType = false;
		boolean onlyEvaluateInternalLinks = false || useCorrectType; 
		boolean zeroKnownModel = false;
		int numberOfCandidates = 1;

		if (onlyGenerateSemanticTypeStatistics) {
			getStatistics(semanticModels, null, 0);
			return;
		}
		int numberOfKnownModels;
		String filePath = Params.RESULTS_DIR + "temp/";
		String filename = ""; 
		filename += "results";
		filename += useCorrectType ? "-correct":"-k=" + numberOfCandidates;
		filename += zeroKnownModel ? "-ontology":"";
		filename += onlyEvaluateInternalLinks ? "-internal":"-all";
		filename += iterativeEvaluation ? "-iterative":"";
		filename += ".csv"; 
		
		PrintWriter resultFileIterative = null;
		PrintWriter resultFile = null;
		StringBuffer[] resultsArray = null;
		
		if (iterativeEvaluation) {
			resultFileIterative = new PrintWriter(new File(filePath + filename));
			resultsArray = new StringBuffer[semanticModels.size() + 2];
			for (int i = 0; i < resultsArray.length; i++) {
				resultsArray[i] = new StringBuffer();
			}
		} else {
			resultFile = new PrintWriter(new File(filePath + filename));
			resultFile.println("source \t p \t r \t t \t a \t m \n");
		}
		
//		new OfflineTraining().getCorrectModel(contextParameters, 
//				null, null, 
//				sources[20], r2rmlModels[20], 0, numberOfCandidates);
//		if (true) return;

		for (int i = 0; i < semanticModels.size(); i++) {
//		for (int i = 0; i <= 1; i++) {
//		int i = 1; {

			// clean semantic files folder in karma home
			FileUtils.cleanDirectory(semFilesFolder);
			trainingSource = null;
			trainingModel = null;

			int newSourceIndex = i;
			SemanticModel newSource = semanticModels.get(newSourceIndex);

			logger.info("======================================================");
			logger.info(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			System.out.println(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			logger.info("======================================================");
			
			if (zeroKnownModel)
				numberOfKnownModels = 0;
			else
				numberOfKnownModels = iterativeEvaluation ? 0 : semanticModels.size() - 1;
			
			if (iterativeEvaluation) {
				if (resultsArray[0].length() > 0)	resultsArray[0].append(" \t ");			
				resultsArray[0].append(newSource.getName() + "(" + newSource.getColumnNodes().size() + ")" + "\t" + " " + "\t" + " " + "\t" + " " + "\t" + " ");
				if (resultsArray[1].length() > 0)	resultsArray[1].append(" \t ");			
				resultsArray[1].append("p \t r \t t \t a \t m");
			}

//			numberOfKnownModels = 2;
			while (numberOfKnownModels <= semanticModels.size() - 1) 
			{

				trainingData.clear();
				trainingSources = new File[numberOfKnownModels];
				trainingModels = new File[numberOfKnownModels];

				int j = 0, count = 0;
				while (count < numberOfKnownModels) {
					if (j != newSourceIndex) {
						trainingData.add(semanticModels.get(j));
						trainingSources[count] = sources[j];
						trainingModels[count] = r2rmlModels[j];
						count++;
						if (count == numberOfKnownModels) {
							trainingSource = sources[j];
							trainingModel = r2rmlModels[j];
						}
					} 
					j++;
				}

				modelLearningGraph = (ModelLearningGraphCompact)ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);
				
				SemanticModel correctModel;
				if (useCorrectType) {
					correctModel = newSource;
					correctModel.setAccuracy(1.0);
					correctModel.setMrr(1.0);
				} else {
					testSource = sources[newSourceIndex];
					testModel = r2rmlModels[newSourceIndex];
					if (iterativeEvaluation) {
						correctModel = new OfflineTraining().getCorrectModel(contextParameters, 
								trainingSource, trainingModel, 
								testSource, testModel, numberOfKnownModels, numberOfCandidates);
					} else {
						correctModel = new OfflineTraining().getCorrectModel(contextParameters, 
								trainingSources, trainingModels, 
								testSource, testModel, numberOfCandidates);
					}
				}
				
				
				List<ColumnNode> columnNodes = correctModel.getColumnNodes();
				//				if (useCorrectType && numberOfCRFCandidates > 1)
				//					updateCrfSemanticTypesForResearchEvaluation(columnNodes);

				List<Node> steinerNodes = new LinkedList<Node>(columnNodes);
				modelLearner = new ModelLearner_KnownModels(modelLearningGraph.getGraphBuilder(), steinerNodes);
				long start = System.currentTimeMillis();

				String graphName = !iterativeEvaluation?
						graphPath + semanticModels.get(newSourceIndex).getName() + Params.GRAPH_JSON_FILE_EXT : 
							graphPath + semanticModels.get(newSourceIndex).getName() + ".knownModels=" + numberOfKnownModels + Params.GRAPH_JSON_FILE_EXT;

				if (new File(graphName).exists()) {
					// read graph from file
					try {
						logger.info("loading the graph ...");
						DirectedWeightedMultigraph<Node, DefaultLink> graph = GraphUtil.importJson(graphName);
						GraphBuilder gb = new GraphBuilderTopK(ontologyManager, graph);
						modelLearner = new ModelLearner_KnownModels(gb, steinerNodes);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					logger.info("building the graph ...");
					for (SemanticModel sm : trainingData)
//						modelLearningGraph.addModel(sm);
						modelLearningGraph.addModelAndUpdate(sm, PatternWeightSystem.JWSPaperFormula);
					modelLearner = new ModelLearner_KnownModels(modelLearningGraph.getGraphBuilder(), steinerNodes);
//					modelLearner.graphBuilder = modelLearningGraph.getGraphBuilder();
//					modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
					// save graph to file
					try {
//						GraphUtil.exportJson(modelLearningGraph.getGraphBuilder().getGraph(), graphName, true, true);
//						GraphVizUtil.exportJGraphToGraphviz(modelLearner.graphBuilder.getGraph(), 
//								"test", 
//								true, 						
//								GraphVizLabelType.LocalId,
//								GraphVizLabelType.LocalUri,
//								false, 
//								true, 
//								graphName + ".dot");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				List<SortableSemanticModel> hypothesisList = modelLearner.hypothesize(useCorrectType, numberOfCandidates);
				
				long elapsedTimeMillis = System.currentTimeMillis() - start;
				float elapsedTimeSec = elapsedTimeMillis/1000F;
				
				int cutoff = 20;//ModelingConfiguration.getMaxCandidateModels();
				List<SortableSemanticModel> topHypotheses = null;
				if (hypothesisList != null) {
					topHypotheses = hypothesisList.size() > cutoff ? 
							hypothesisList.subList(0, cutoff) : 
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
//								(m.getSteinerNodes() == null ? "" : m.getSteinerNodes().getScoreDetailsString()) +
								"link coherence:" + (m.getLinkCoherence() == null ? "" : m.getLinkCoherence().getCoherenceValue()) + "\n";
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
							System.out.println("number of known models: " + numberOfKnownModels +
									", precision: " + me.getPrecision() + 
									", recall: " + me.getRecall() + 
									", time: " + elapsedTimeSec + 
									", accuracy: " + correctModel.getAccuracy() + 
									", mrr: " + correctModel.getMrr()); 
							logger.info("number of known models: " + numberOfKnownModels +
									", precision: " + me.getPrecision() + 
									", recall: " + me.getRecall() + 
									", time: " + elapsedTimeSec + 
									", accuracy: " + correctModel.getAccuracy() + 
									", mrr: " + correctModel.getMrr()); 
//							resultFile.println("number of known models \t precision \t recall");
//							resultFile.println(numberOfKnownModels + "\t" + me.getPrecision() + "\t" + me.getRecall());
							String s = me.getPrecision() + "\t" + 
										me.getRecall() + "\t" + 
										elapsedTimeSec + "\t" + 
										correctModel.getAccuracy() + "\t" + 
										correctModel.getMrr();
							
							if (iterativeEvaluation) {
								if (resultsArray[numberOfKnownModels + 2].length() > 0) 
									resultsArray[numberOfKnownModels + 2].append(" \t ");
								resultsArray[numberOfKnownModels + 2].append(s);
							} else {
//								s = newSource.getName() + "\t" + 
//										me.getPrecision() + "\t" + 
//										me.getRecall() + "\t" + 
//										elapsedTimeSec + "\t" + 
//										correctModel.getAccuracy() + "\t" + 
//										correctModel.getMrr();
								s = me.getPrecision() + "\t" + 
										me.getRecall() + "\t" + 
										elapsedTimeSec; 
								resultFile.println(s);
							}

//							resultFile.println(me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec);
						}
					}

				String outputPath = Params.OUTPUT_DIR;
				String outName = !iterativeEvaluation?
						outputPath + semanticModels.get(newSourceIndex).getName() + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT : 
							outputPath + semanticModels.get(newSourceIndex).getName() + ".knownModels=" + numberOfKnownModels + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT;

				GraphVizUtil.exportSemanticModelsToGraphviz(
						models, 
						newSource.getName(),
						outName,
						GraphVizLabelType.LocalId,
						GraphVizLabelType.LocalUri,
						true,
						true);

				numberOfKnownModels ++;
				if (zeroKnownModel) break;
			}

		}
		if (iterativeEvaluation) {
			for (StringBuffer s : resultsArray)
				resultFileIterative.println(s.toString());
			resultFileIterative.close();
		} else {
			resultFile.close();
		}
		
	}
	
	public static void runUkHackEvaluation() throws Exception {

		String karmaHomeDir = "/Users/mohsen/karma-uk-hack/";
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().registerByKarmaHome(karmaHomeDir);
		contextParameters.setParameterValue(ContextParameter.USER_DIRECTORY_PATH, karmaHomeDir);
		contextParameters.setParameterValue(ContextParameter.USER_CONFIG_DIRECTORY, karmaHomeDir + "config");

		List<String> trainingSources = new ArrayList<String>();
		trainingSources.add("www.gunsinternational.com");
		trainingSources.add("www.alaskaslist.com");
		trainingSources.add("www.dallasguns.com");
		trainingSources.add("www.elpasoguntrader.com");
		trainingSources.add("www.floridagunclassifieds.com");
		
		List<String> testSources = new ArrayList<String>();
		testSources.add("www.armslist.com");
		testSources.add("www.floridaguntrader.com");
		testSources.add("www.hawaiiguntrader.com");
		testSources.add("www.montanagunclassifieds.com");
		testSources.add("www.msguntrader.com");
		testSources.add("www.tennesseegunexchange.com");
		testSources.add("www.kyclassifieds.com");
		testSources.add("www.nextechclassifieds.com");
		testSources.add("www.shooterswap.com");
		testSources.add("www.theoutdoorstrader.com");
		
		
		List<SemanticModel> trainingModels = new ArrayList<SemanticModel>(); 
		List<SemanticModel> testModels = new ArrayList<SemanticModel>();
		
		List<String> trainingFiles = new LinkedList<String>();
		List<String> testFiles = new LinkedList<String>();
		
		String ukHacKDirStr = karmaHomeDir + "git/data/weapons/";
		File ukHackDir = new File(ukHacKDirStr);
		File[] ukHackWebsites = ukHackDir.listFiles();
		if (ukHackWebsites != null) {
			for (File ukHackWebsite : ukHackWebsites) {
				if (!ukHackWebsite.isDirectory())
					continue;
				File[] ukHackWebsiteFiles = ukHackWebsite.listFiles();
				String sourceName = ukHackWebsite.getName();
				if (!trainingSources.contains(sourceName) && !testSources.contains(sourceName)) 
					continue;

				if (ukHackWebsiteFiles != null) {
					for (File ukHackWebsiteFile : ukHackWebsiteFiles) {
						if (ukHackWebsiteFile.getName().equalsIgnoreCase("model.json")) {
							SemanticModel sm = null;
							try {
								sm = SemanticModel.readJson(ukHackWebsiteFile.getAbsolutePath());
							} catch (Exception e) {
								System.out.println("Error in reading " + ukHackWebsiteFile.getAbsolutePath());
								break;
							}
							sm.setName(sourceName);
							if (trainingSources.contains(sourceName)) {
								trainingModels.add(sm);
								trainingFiles.add(ukHackWebsiteFile.getAbsolutePath());
							} else if (testSources.contains(sourceName)) {
								testModels.add(sm);
								testFiles.add(ukHackWebsiteFile.getAbsolutePath());
							}
						}
					}
				}
			}
		}
		
		OntologyManager ontologyManager = new OntologyManager(contextParameters.getId());
		File ff = new File(karmaHomeDir + "preloaded-ontologies/");
		File[] files = ff.listFiles();
		for (File f : files) {
			if(f.getName().startsWith(".") || f.isDirectory()) {
				continue; //Ignore . files
			}
			ontologyManager.doImport(f, "UTF-8");
		}
		ontologyManager.updateCache();  

		ModelLearningGraph modelLearningGraph = null;
		
		ModelLearner_KnownModels modelLearner;
		
		boolean onlyGenerateSemanticTypeStatistics = true;
		boolean useCorrectType = true;
		boolean onlyEvaluateInternalLinks = false || useCorrectType; 
		int numberOfCandidates = 1;

		if (onlyGenerateSemanticTypeStatistics) {
			System.out.println("==============================================");
			System.out.println("training");
			System.out.println("==============================================");
			getStatistics(trainingModels, trainingFiles, 4);
			System.out.println("==============================================");
			System.out.println("test");
			System.out.println("==============================================");
			getStatistics(testModels, testFiles, 4);
			return;
		}
		String filePath = karmaHomeDir + "result/";
		String filename = ""; 
		filename += "results";
		filename += useCorrectType ? "-correct":"-k=" + numberOfCandidates;
		filename += onlyEvaluateInternalLinks ? "-internal":"-all";
		filename += ".csv"; 
		
		PrintWriter resultFile = null;
		
		resultFile = new PrintWriter(new File(filePath + filename));
		resultFile.println("source \t p \t r \t t \t a \t m \n");

		for (int i = 0; i < testModels.size(); i++) {
//		for (int i = 0; i <= 1; i++) {
//		int i = 0; {

			int targetSourceIndex = i;
			SemanticModel targetSource = testModels.get(targetSourceIndex);

			logger.info("======================================================");
			logger.info(targetSource.getName() + "(#attributes:" + targetSource.getColumnNodes().size() + ")");
			System.out.println(targetSource.getName() + "(#attributes:" + targetSource.getColumnNodes().size() + ")");
			logger.info("======================================================");
			
			modelLearningGraph = (ModelLearningGraphCompact)ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);
				
			SemanticModel correctModel;
			correctModel = targetSource;
//			correctModel.setAccuracy(1.0);
//			correctModel.setMrr(1.0);
				
				
			List<ColumnNode> columnNodes = correctModel.getColumnNodes();
			//				if (useCorrectType && numberOfCRFCandidates > 1)
			//					updateCrfSemanticTypesForResearchEvaluation(columnNodes);

			List<Node> steinerNodes = new LinkedList<Node>(columnNodes);
			modelLearner = new ModelLearner_KnownModels(modelLearningGraph.getGraphBuilder(), steinerNodes);
			long start = System.currentTimeMillis();

			String graphName = karmaHomeDir + "alignment-graph/graph.json";

//			if (new File(graphName).exists()) {
//				// read graph from file
//				try {
//					logger.info("loading the graph ...");
//					DirectedWeightedMultigraph<Node, DefaultLink> graph = GraphUtil.importJson(graphName);
//					GraphBuilder gb = new GraphBuilderTopK(ontologyManager, graph);
//					modelLearner = new ModelLearner_KnownModels(gb, steinerNodes);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			} else 
			{
				logger.info("building the graph ...");
				for (SemanticModel sm : trainingModels)
//						modelLearningGraph.addModel(sm);
					modelLearningGraph.addModelAndUpdate(sm, PatternWeightSystem.JWSPaperFormula);
				modelLearner = new ModelLearner_KnownModels(modelLearningGraph.getGraphBuilder(), steinerNodes);
//					modelLearner.graphBuilder = modelLearningGraph.getGraphBuilder();
//					modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
				// save graph to file
				try {
						GraphUtil.exportJson(modelLearningGraph.getGraphBuilder().getGraph(), graphName, true, true);
						GraphVizUtil.exportJGraphToGraphviz(modelLearner.graphBuilder.getGraph(), 
								"test", 
								true, 						
								GraphVizLabelType.LocalId,
								GraphVizLabelType.LocalUri,
								false, 
								true, 
								graphName + ".dot");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
				
			List<SortableSemanticModel> hypothesisList = modelLearner.hypothesize(useCorrectType, numberOfCandidates);
			
			long elapsedTimeMillis = System.currentTimeMillis() - start;
			float elapsedTimeSec = elapsedTimeMillis/1000F;
			
			int cutoff = 3;//ModelingConfiguration.getMaxCandidateModels();
			List<SortableSemanticModel> topHypotheses = null;
			if (hypothesisList != null) {
				topHypotheses = hypothesisList.size() > cutoff ? 
						hypothesisList.subList(0, cutoff) : 
							hypothesisList;
			}

			Map<String, SemanticModel> models = 
					new TreeMap<String, SemanticModel>();

			ModelEvaluation me;
			models.put("1-correct model", correctModel);
			if (topHypotheses != null) {
				for (int k = 0; k < topHypotheses.size(); k++) {

					SortableSemanticModel m = topHypotheses.get(k);

					me = m.evaluate(correctModel, onlyEvaluateInternalLinks, false);

					String label = "candidate " + k + "\n" + 
//								(m.getSteinerNodes() == null ? "" : m.getSteinerNodes().getScoreDetailsString()) +
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
						System.out.println("number of known models: " + trainingModels.size() +
								", precision: " + me.getPrecision() + 
								", recall: " + me.getRecall() + 
								", time: " + elapsedTimeSec + 
								", accuracy: " + correctModel.getAccuracy() + 
								", mrr: " + correctModel.getMrr()); 
						logger.info("number of known models: " + trainingModels.size() +
								", precision: " + me.getPrecision() + 
								", recall: " + me.getRecall() + 
								", time: " + elapsedTimeSec + 
								", accuracy: " + correctModel.getAccuracy() + 
								", mrr: " + correctModel.getMrr()); 
//							resultFile.println("number of known models \t precision \t recall");
//							resultFile.println(numberOfKnownModels + "\t" + me.getPrecision() + "\t" + me.getRecall());
						String s = me.getPrecision() + "\t" + 
									me.getRecall() + "\t" + 
									elapsedTimeSec + "\t" + 
									correctModel.getAccuracy() + "\t" + 
									correctModel.getMrr();
						
//						s = newSource.getName() + "\t" + 
//								me.getPrecision() + "\t" + 
//								me.getRecall() + "\t" + 
//								elapsedTimeSec + "\t" + 
//								correctModel.getAccuracy() + "\t" + 
//								correctModel.getMrr();
						s = me.getPrecision() + "\t" + 
								me.getRecall() + "\t" + 
								elapsedTimeSec; 
						resultFile.println(s);

//						resultFile.println(me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec);
					}
				}
			}

			String outName = karmaHomeDir + "output/" + targetSource.getName() + ".out.dot";

			GraphVizUtil.exportSemanticModelsToGraphviz(
					models, 
					targetSource.getName(),
					outName,
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					true,
					true);

		}

		resultFile.close();
	}

	public static void main(String[] args) throws Exception {
		boolean uk_hack = true;
		if (uk_hack) runUkHackEvaluation();
		else runResearchEvaluation();
	}


}
