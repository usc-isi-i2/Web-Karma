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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.alignment.TreePostProcess;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSemanticTypeStatus;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;

public class ModelLearner {

	private static Logger logger = LoggerFactory.getLogger(ModelLearner.class);
	private OntologyManager ontologyManager = null;
	private GraphBuilder graphBuilder = null;
	private NodeIdFactory nodeIdFactory = null; 
	private List<Node> steinerNodes = null;
	private SemanticModel semanticModel = null;
//	private long lastUpdateTimeOfGraph;

	private static final int NUM_SEMANTIC_TYPES = 4;

	public ModelLearner(OntologyManager ontologyManager, 
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
		if (this.steinerNodes != null) Collections.sort(this.steinerNodes);
		this.graphBuilder = cloneGraphBuilder(gb); // create a copy of the graph builder
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
	}

	public ModelLearner(GraphBuilder graphBuilder, 
			List<Node> steinerNodes) {
		if (graphBuilder == null || 
				steinerNodes == null || 
				steinerNodes.isEmpty()) {
			logger.error("cannot instanciate model learner!");
			return;
		}
		this.ontologyManager = graphBuilder.getOntologyManager();
		this.steinerNodes = steinerNodes;
		if (this.steinerNodes != null) Collections.sort(this.steinerNodes);
		this.graphBuilder = graphBuilder;
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
	}

	public ModelLearner(OntologyManager ontologyManager, 
			Set<LabeledLink> forcedLinks,
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
		if (this.steinerNodes != null) Collections.sort(this.steinerNodes);
		this.graphBuilder = cloneGraphBuilder(gb); // create a copy of the graph builder
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
		if (steinerNodes != null) {
			for (Node n : steinerNodes) {
				if (this.graphBuilder.getIdToNodeMap().get(n.getId()) == null) {
					this.graphBuilder.addNodeAndUpdate(n);
				}
			}
		}
		if (forcedLinks != null) {
			for (LabeledLink l : forcedLinks) {
				if (l.getStatus() == LinkStatus.ForcedByUser) {
					Node source = l.getSource();
					Node target = l.getTarget();
					if (!this.graphBuilder.addLink(source, target, l)) {
						LabeledLink existingLink = this.graphBuilder.getIdToLinkMap().get(l.getId());
						if (existingLink != null) { // the link already exist, but it may not be forced by user
							this.graphBuilder.changeLinkStatus(existingLink, LinkStatus.ForcedByUser);
						}
					}
				}
			}
		}
	}
	
	public SemanticModel getModel() {
		if (this.semanticModel == null)
			try {
				this.learn();
			} catch (Exception e) {
				logger.error("error in learing the semantic model for the source " + ((this.semanticModel == null) ? "" : this.semanticModel.getId()));
				e.printStackTrace();
			}

		return this.semanticModel;
	}

	public void learn() throws Exception {

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

		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId()).getKarmaHome());
		List<SortableSemanticModel> sortableSemanticModels = new ArrayList<>();
		Set<Node> addedNodes = new HashSet<>(); //They should be deleted from the graph after computing the semantic models

		List<ColumnNode> columnNodes = new LinkedList<>();
		for (Node n : steinerNodes)
			if (n instanceof ColumnNode)
				columnNodes.add((ColumnNode)n);
		
		logger.info("finding candidate steiner sets ... ");
		CandidateSteinerSets candidateSteinerSets = getCandidateSteinerSets(steinerNodes, numberOfCandidates, addedNodes);

		if (candidateSteinerSets == null || 
				candidateSteinerSets.getSteinerSets() == null || 
				candidateSteinerSets.getSteinerSets().isEmpty()) {
			logger.error("there is no candidate set of steiner nodes.");
			
			DirectedWeightedMultigraph<Node, LabeledLink> tree =
					new DirectedWeightedMultigraph<>(LabeledLink.class);
			
			for (Node n : steinerNodes)
				tree.addVertex(n);
			
			SemanticModel sm = new SemanticModel(new RandomGUID().toString(), tree);
			SortableSemanticModel sortableSemanticModel = new SortableSemanticModel(sm, null, true);
			sortableSemanticModels.add(sortableSemanticModel);
			return sortableSemanticModels;
		}
		
		logger.info("graph nodes: " + this.graphBuilder.getGraph().vertexSet().size());
		logger.info("graph links: " + this.graphBuilder.getGraph().edgeSet().size());

		logger.info("number of steiner sets: " + candidateSteinerSets.numberOfCandidateSets());

		logger.info("computing steiner trees ...");
		int number = 0;
		for (SteinerNodes sn : candidateSteinerSets.getSteinerSets()) {
			if (sn == null) continue;
			logger.debug("computing steiner tree for steiner nodes set " + number + " ...");
			logger.debug(sn.getScoreDetailsString());
			number++;
//			logger.info("START ...");
			
			List<DirectedWeightedMultigraph<Node, LabeledLink>> topKSteinerTrees;
			if (this.graphBuilder instanceof GraphBuilderTopK) {
				topKSteinerTrees =  ((GraphBuilderTopK)this.graphBuilder).getTopKSteinerTrees(sn, 
						modelingConfiguration.getTopKSteinerTree(), 
						null, null, true);
			} 
			else 
			{
				topKSteinerTrees = new LinkedList<>();
				SteinerTree steinerTree = new SteinerTree(
						new AsUndirectedGraph<>(this.graphBuilder.getGraph()), Lists.newLinkedList(sn.getNodes()));
				WeightedMultigraph<Node, DefaultLink> t = steinerTree.getDefaultSteinerTree();
				TreePostProcess treePostProcess = new TreePostProcess(this.graphBuilder, t);
				if (treePostProcess.getTree() != null)
					topKSteinerTrees.add(treePostProcess.getTree());
			}
			
			for (DirectedWeightedMultigraph<Node, LabeledLink> tree: topKSteinerTrees) {
				if (tree != null) {
					SemanticModel sm = new SemanticModel(new RandomGUID().toString(), 
							tree,
							columnNodes,
							sn.getMappingToSourceColumns()
							);
					SortableSemanticModel sortableSemanticModel = 
							new SortableSemanticModel(sm, sn, true);
					sortableSemanticModels.add(sortableSemanticModel);
					
//					sortableSemanticModel.print();
//					System.out.println(sortableSemanticModel.getRankingDetails());
				}
			}
			if (number == modelingConfiguration.getNumCandidateMappings())

				break;

		}

		Collections.sort(sortableSemanticModels);
		int count = Math.min(sortableSemanticModels.size(), modelingConfiguration.getNumCandidateMappings());

		logger.info("results are ready ...");
//		sortableSemanticModels.get(0).print();
		return sortableSemanticModels.subList(0, count);

//		List<SortableSemanticModel> uniqueModels = new ArrayList<SortableSemanticModel>();
//		SortableSemanticModel current, previous;
//		if (sortableSemanticModels != null) {
//			if (sortableSemanticModels.size() > 0)
//				uniqueModels.add(sortableSemanticModels.get(0));
//			for (int i = 1; i < sortableSemanticModels.size(); i++) {
//				current = sortableSemanticModels.get(i);
//				previous = sortableSemanticModels.get(i - 1);
//				if (current.getScore() == previous.getScore() && current.getCost() == previous.getCost())
//					continue;
//				uniqueModels.add(current);
//			}
//		}
//		
//		logger.info("results are ready ...");
//		return uniqueModels;

	}

	private CandidateSteinerSets getCandidateSteinerSets(List<Node> steinerNodes, int numberOfCandidates, Set<Node> addedNodes) {

		if (steinerNodes == null || steinerNodes.isEmpty())
			return null;

		int maxNumberOfSteinerNodes = steinerNodes.size() * 2;
		CandidateSteinerSets candidateSteinerSets = new CandidateSteinerSets(maxNumberOfSteinerNodes, ontologyManager.getContextId());

		if (addedNodes == null) 
			addedNodes = new HashSet<>();

		Set<SemanticTypeMapping> tempSemanticTypeMappings;
		HashMap<ColumnNode, List<SemanticType>> columnSemanticTypes = new HashMap<>();
		HashMap<String, Integer> semanticTypesCount = new HashMap<>();
		List<SemanticType> candidateSemanticTypes;
		String domainUri = "", propertyUri = "";

		for (Node n : steinerNodes) {

			ColumnNode cn = null;
			if (n instanceof ColumnNode)
				cn = (ColumnNode)n;
			else
				continue;
				
			candidateSemanticTypes = cn.getTopKLearnedSemanticTypes(numberOfCandidates);
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
			
			if (cn.getSemanticTypeStatus() == ColumnSemanticTypeStatus.UserAssigned) {
				HashMap<SemanticType, LabeledLink> domainLinks = 
						GraphUtil.getDomainLinks(this.graphBuilder.getGraph(), cn, cn.getUserSemanticTypes());
				if (domainLinks != null && !domainLinks.isEmpty()) {
					for (SemanticType st : cn.getUserSemanticTypes()) {
						semanticTypeMappings = new HashSet<>();
						LabeledLink domainLink = domainLinks.get(st);
						if (domainLink == null || domainLink.getSource() == null || !(domainLink.getSource() instanceof InternalNode))
							continue;
						SemanticTypeMapping mp = 
								new SemanticTypeMapping(cn, st, (InternalNode)domainLink.getSource(), domainLink, cn);
						semanticTypeMappings.add(mp);
						candidateSteinerSets.updateSteinerSets(semanticTypeMappings);
					}
				}
			} else {
				semanticTypeMappings = new HashSet<>();
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
	//				if (countOfMatches < countOfSemanticType) 
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

		if (addedNodes == null)
			addedNodes = new HashSet<>();

		Set<SemanticTypeMapping> mappings = new HashSet<>();

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
		boolean allowMultipleSamePropertiesPerNode = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId()).getKarmaHome()).isMultipleSamePropertyPerNode();
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
				ColumnNode target = new ColumnNode(nodeId, nodeId, sourceColumn.getColumnName(), null, null);
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
			addedNodes = new HashSet<>();

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
		ColumnNode target = new ColumnNode(nodeId, nodeId, sourceColumn.getColumnName(), null, null);
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

}
