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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.google.common.collect.Sets;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.ontology.DomainRangePair;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SimpleLink;
import edu.isi.karma.rep.alignment.SubClassLink;
//import com.google.common.base.Function;
//import com.google.common.collect.Multimap;
//import com.google.common.collect.Multimaps;

public class Approach1 {

	private static Logger logger = Logger.getLogger(Approach1.class);
	private static String ontologyDir = "/Users/mohsen/Dropbox/Service Modeling/ontologies/";
	private static String outputDir = "/Users/mohsen/Dropbox/Service Modeling/output/";
	
	private DirectedWeightedMultigraph<Node, Link> graph;
	private HashMap<NodeType, List<Node>> typeToNodesMap;
	private HashMap<String, List<Node>> uriToNodesMap;
	private LinkIdFactory linkIdFactory;
	private List<DomainRangePair> sourceTargetList;
	private HashMap<String, HashMap<String, Double>> sourceTargetToLinkWeightMap;
	private HashMap<Node, List<SemanticLabel>> nodeToSemanticLabels;
	private HashMap<SemanticLabel, SemanticLabel> inputLabelsToOutputLabelsMatching;
	// service model id --> number of common links between the input model and this service model
	private HashMap<String, Integer> similarityMap;
	private HashMap<String, Double> modelsWeightsMap;
	
	private DirectedWeightedMultigraph<Node, Link> inputModel;
	private List<ServiceModel> trainingData;
	private OntologyManager ontologyManager;
	private List<SemanticLabel> inputSemanticLabels;
	private List<SemanticLabel> outputSemanticLabels;

	public Approach1(DirectedWeightedMultigraph<Node, Link> inputModel, 
			List<ServiceModel> trainingData, 
			OntologyManager ontologyManager) {
		this.inputModel = inputModel;
		this.trainingData = trainingData;
		this.ontologyManager = ontologyManager;
		this.linkIdFactory = new LinkIdFactory();
		this.inputSemanticLabels = new ArrayList<SemanticLabel>();
		this.outputSemanticLabels = new ArrayList<SemanticLabel>();
		this.typeToNodesMap = new HashMap<NodeType, List<Node>>();
		this.uriToNodesMap = new HashMap<String, List<Node>>();
		this.sourceTargetList = new ArrayList<DomainRangePair>();
		this.sourceTargetToLinkWeightMap = new HashMap<String, HashMap<String,Double>>();
		this.nodeToSemanticLabels = new HashMap<Node, List<SemanticLabel>>();
		this.inputLabelsToOutputLabelsMatching = new HashMap<SemanticLabel, SemanticLabel>();
		this.similarityMap = new HashMap<String, Integer>();
		this.modelsWeightsMap = new HashMap<String, Double>();
	}

	public List<SemanticLabel> getInputSemanticLabels() {
		return inputSemanticLabels;
	}

	public List<SemanticLabel> getOutputSemanticLabels() {
		return outputSemanticLabels;
	}

	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return this.graph;
	}
	
	public void loadGraph(DirectedWeightedMultigraph<Node, Link> graph) {
		this.graph = graph;
		this.updateHashMaps();
	}
	
//	private double getInitialLinkWeight() {
//		double w = 0;
//		for (ServiceModel sm : this.trainingData) {
//			DirectedWeightedMultigraph<Node, Link> m = sm.getModels().get(1);
//			w += m.edgeSet().size();
//		}
//		return w;
//	}
	
	private void buildSimilarityMap() {
		
		Set<String> inputModelSourceLinkTargetTriples = new HashSet<String>();
		for (Link link : this.inputModel.edgeSet()) {
			if (link.getTarget() instanceof InternalNode)
				inputModelSourceLinkTargetTriples.add(link.getSource().getLabel().getUri() + 
						link.getLabel().getUri() +
						link.getTarget().getLabel().getUri() );
			else
				inputModelSourceLinkTargetTriples.add(link.getSource().getLabel().getUri() + 
						link.getLabel().getUri());
		}
		
		int count;
		Set<String> modelSourceLinkTargetTriples;
		for (ServiceModel sm : this.trainingData) {
			
			DirectedWeightedMultigraph<Node, Link> m = sm.getModels().get(0);
			modelSourceLinkTargetTriples = new HashSet<String>();

			for (Link link : m.edgeSet()) {
				if (link.getTarget() instanceof InternalNode)
					modelSourceLinkTargetTriples.add(link.getSource().getLabel().getUri() + 
							link.getLabel().getUri() +
							link.getTarget().getLabel().getUri() );
				else
					modelSourceLinkTargetTriples.add(link.getSource().getLabel().getUri() + 
							link.getLabel().getUri());
			}
			
			Set<String> commonLinks = 
					Sets.intersection(inputModelSourceLinkTargetTriples, modelSourceLinkTargetTriples);
			
			count = (commonLinks != null)? commonLinks.size() : 0;
			this.similarityMap.put(sm.getId(), count);
		}
	}
	
	private void buildSimilarityMap2() {
		
		Set<String> inputModelNodes = new HashSet<String>();
		for (Node node : this.inputModel.vertexSet()) {
			if (node instanceof InternalNode)
				inputModelNodes.add(node.getLabel().getUri());
		}
		
		int count;
		Set<String> modelNodes;
		for (ServiceModel sm : this.trainingData) {
			
			DirectedWeightedMultigraph<Node, Link> m = sm.getModels().get(0);
			modelNodes = new HashSet<String>();

			for (Node node : m.vertexSet()) {
				if (node instanceof InternalNode)
					modelNodes.add(node.getLabel().getUri());
			}
			
			Set<String> commonNodes = 
					Sets.intersection(inputModelNodes, modelNodes);
			
			count = (commonNodes != null)? commonNodes.size() : 0;
			this.similarityMap.put(sm.getId(), count);
		}
	}
	
	private void buildModelsWeightMap() {
		
//		Function<String, Integer> countFunction = new Function<String, Integer>() {
//			public Integer apply(String in) {
//				return similarityMap.containsKey(in)? similarityMap.get(in): 0;
//		  	}
//		};		
//		Multimap<Integer, String> index = Multimaps.index(this.similarityMap.keySet(), countFunction);

		int totalCommonLinks = 0;
		for (Integer i : this.similarityMap.values()) 
			if (i != null)
				totalCommonLinks += i.intValue();
		
		int count;
		for (Entry<String, Integer> entry : this.similarityMap.entrySet()) {
			if (entry.getValue() != null) {
				count = entry.getValue().intValue();
				this.modelsWeightsMap.put(entry.getKey(), ((double)count + 1) / ((double)totalCommonLinks + 1));
			}
		}
	}
	
	private void buildSourceTargetLinkWieghtrMap() {
		
		Double w;
		Double modelWeight;
		
		for (ServiceModel sm : this.trainingData) {
			
			modelWeight = (this.modelsWeightsMap.containsKey(sm.getId())) ? 
					this.modelsWeightsMap.get(sm.getId()).doubleValue() : 0.0;
					
			DirectedWeightedMultigraph<Node, Link> m = sm.getModels().get(1);

			for (Link link : m.edgeSet()) {

				Node source = link.getSource();
				Node target = link.getTarget();
				
				if (source instanceof InternalNode && target instanceof InternalNode) {
					String key = source.getLabel().getUri() +
								 target.getLabel().getUri();
					
					HashMap<String, Double> linkWeight = this.sourceTargetToLinkWeightMap.get(key);
					if (linkWeight == null) { 
						linkWeight = new HashMap<String, Double>();
						linkWeight.put(link.getLabel().getUri(), modelWeight);
						this.sourceTargetList.add(
								new DomainRangePair(source.getLabel().getUri(), target.getLabel().getUri()));
						this.sourceTargetToLinkWeightMap.put(key, linkWeight);
					} else {
						w = linkWeight.get(link.getLabel().getUri());
						if (w == null) linkWeight.put(link.getLabel().getUri(), modelWeight);
						else linkWeight.put(link.getLabel().getUri(), (w.doubleValue() + modelWeight) );
					}
				}
			}
		}
	}
	
	private List<SemanticLabel> getModelSemanticLabels(
			DirectedWeightedMultigraph<Node, Link> model,
			boolean updateNodeToSemanticLabelsMap) {
		
		List<SemanticLabel> semanticLabels = new ArrayList<SemanticLabel>();

		for (Node n : model.vertexSet()) {
			if (!(n instanceof ColumnNode) && !(n instanceof LiteralNode)) continue;
			
			Set<Link> incomingLinks = model.incomingEdgesOf(n);
			if (incomingLinks != null) { // && incomingLinks.size() == 1) {
				Link link = incomingLinks.toArray(new Link[0])[0];
				Node domain = link.getSource();
				
				SemanticLabel sl = new SemanticLabel(domain.getLabel(), link.getLabel(), n.getId());
				semanticLabels.add(sl);
				
				if (!updateNodeToSemanticLabelsMap) continue;
				
				List<SemanticLabel> attachedSLs = this.nodeToSemanticLabels.get(domain);
				if (attachedSLs == null) {
					attachedSLs = new ArrayList<SemanticLabel>();
					this.nodeToSemanticLabels.put(domain, attachedSLs);
				}
				attachedSLs.add(sl);
			} 
		}
		return semanticLabels;
	}
	
	private List<SemanticLabel> findMatchedSemanticLabels(SemanticLabel semanticLabel) {

		List<SemanticLabel> matchedSemanticLabels = new ArrayList<SemanticLabel>();
		
		for (ServiceModel sm : trainingData) {
			//sm.computeMatchedSubGraphs(1);
			List<MatchedSubGraphs> matchedSubGraphs = sm.getMatchedSubGraphs();
			SemanticLabel sl1 = null, sl2 = null;
			List<SemanticLabel> slList1 = null, slList2 = null;
			for (MatchedSubGraphs m : matchedSubGraphs) {
				slList1 = getModelSemanticLabels(m.getSubGraph1(), false);
				if (slList1 != null && slList1.size() == 1) sl1 = slList1.get(0); else sl1 = null;
				
				slList2 = getModelSemanticLabels(m.getSubGraph2(), false);
				if (slList2 != null && slList2.size() == 1) sl2 = slList2.get(0); else sl2 = null;
				
				if (sl1 == null || sl2 == null) continue;
				
				if (sl1.compareTo(semanticLabel) == 0) {
					matchedSemanticLabels.add(sl2); 
					logger.info(sm.getServiceNameWithPrefix());
				} else if (sl2.compareTo(semanticLabel) == 0) {
					matchedSemanticLabels.add(sl1);
					logger.info(sm.getServiceNameWithPrefix());
				}
				
			}
		}
		return matchedSemanticLabels;
	}
	
	public SemanticLabel selectBestMatchedSemanticLabel(List<SemanticLabel> matchedSemanticLabels) {
		
		// select the most frequent semantic label from the matched list 

		if (matchedSemanticLabels == null || matchedSemanticLabels.size() == 0)
			return null;
		
		int indexOfMostFrequentSemanticLabel = 0;
		int countOfEqualSemanticLabels = 1;
		int maxNumberOfEqualSemanticLabels = 1;
		
		Collections.sort(matchedSemanticLabels);
		for (int i = 1; i < matchedSemanticLabels.size(); i++) {
			SemanticLabel prev = matchedSemanticLabels.get(i - 1);
			SemanticLabel curr = matchedSemanticLabels.get(i);
			if (curr.compareTo(prev) == 0) { // they are the same
				countOfEqualSemanticLabels ++;
			} else {
				if (countOfEqualSemanticLabels > maxNumberOfEqualSemanticLabels) {
					maxNumberOfEqualSemanticLabels = countOfEqualSemanticLabels;
					indexOfMostFrequentSemanticLabel = i - 1;
					countOfEqualSemanticLabels = 1;
				}
			}
		}
		// check the last item
		if (countOfEqualSemanticLabels > maxNumberOfEqualSemanticLabels) {
			maxNumberOfEqualSemanticLabels = countOfEqualSemanticLabels;
			indexOfMostFrequentSemanticLabel = matchedSemanticLabels.size() - 1;
		}

//		logger.debug("Count of most frequent semantic label is: " + maxNumberOfEqualSemanticLabels);
//		logger.debug("Most frequent semantic label is:");
//		matchedSemanticLabels.get(indexOfMostFrequentSemanticLabel).print();
		return matchedSemanticLabels.get(indexOfMostFrequentSemanticLabel);
	}

	public void preprocess() {
		
		this.buildSimilarityMap2();
		this.buildModelsWeightMap();
		this.buildSourceTargetLinkWieghtrMap();
		
		this.inputSemanticLabels = getModelSemanticLabels(this.inputModel, true);
		if (inputSemanticLabels == null || inputSemanticLabels.size() == 0) {
			logger.info("The input model does not have any semantic label.");
			return;
		}
		
		logger.info("=====================================================================");
		logger.info("Input Semantic Labels: ");
		for (SemanticLabel sl: inputSemanticLabels)
			sl.print();
		logger.info("=====================================================================");
		
		for (SemanticLabel sl : inputSemanticLabels) {

			sl.print();
			logger.info("-------------------------------------------");

			List<SemanticLabel> matchedSemanticLabels = findMatchedSemanticLabels(sl);
			if (matchedSemanticLabels == null || matchedSemanticLabels.size() == 0) {
				
				HashMap<String, Label> superClasses = 
						this.ontologyManager.getSuperClasses(sl.getNodeLabel().getUri(), true);
				
				boolean matchFound = false;
				if (superClasses != null && superClasses.size() > 0) {
					for (String s : superClasses.keySet()) {
						matchedSemanticLabels = findMatchedSemanticLabels(
								new SemanticLabel(new Label(s), sl.getLinkLabel(), sl.getLeafName()));
						
						if (matchedSemanticLabels != null && matchedSemanticLabels.size() > 0) {
							matchFound = true;
							logger.info("We use superclass: " + s + " instead of " + sl.getNodeLabel().getUri());
							break;
						}
					}
				}
				
				if (!matchFound) {
					logger.info("Cannot find any match for semantic label:");
					sl.print();
					continue;
				}
			}
			
			logger.info("Matched Semantic Labels: ");
			for (SemanticLabel matched: matchedSemanticLabels)
				matched.print();
			logger.info("-------------------------------------------");

			SemanticLabel bestMatch = selectBestMatchedSemanticLabel(matchedSemanticLabels);
			SemanticLabel outputSemanticLabel = 
					new SemanticLabel(bestMatch.getNodeLabel(), bestMatch.getLinkLabel(), sl.getLeafName());
			outputSemanticLabels.add(outputSemanticLabel);
			this.inputLabelsToOutputLabelsMatching.put(sl, outputSemanticLabel);

			logger.info("Best Match: ");
			bestMatch.print();
			logger.info("-------------------------------------------");
		}
		
		if (outputSemanticLabels == null || outputSemanticLabels.size() == 0) {
			logger.info("The output model does not have any semantic label.");
			return;
		}

		logger.info("=====================================================================");
		logger.info("Output Semantic Labels: ");
		for (SemanticLabel sl: outputSemanticLabels)
			sl.print();
		logger.info("=====================================================================");
		
	}
	
	public void buildGraph() {
		
		Alignment alignment = new Alignment(this.ontologyManager);
		
		for (List<SemanticLabel> slList : this.nodeToSemanticLabels.values()) {
			
			List<SemanticLabel> mappedOutputLabel = new ArrayList<SemanticLabel>();
			for (SemanticLabel sl : slList)
				if (this.inputLabelsToOutputLabelsMatching.get(sl) != null)
					mappedOutputLabel.add(this.inputLabelsToOutputLabelsMatching.get(sl));
			
	
			Map<String,List<SemanticLabel>> groupedMappedOutputLabelsByNodeUri =
				    new LinkedHashMap<String,List<SemanticLabel>>();
				
			for (SemanticLabel sl : mappedOutputLabel) {
			    List<SemanticLabel> outputLabelsWithSameNodeUri = 
			    		groupedMappedOutputLabelsByNodeUri.get(sl.getNodeLabel().getUri());
			    if (outputLabelsWithSameNodeUri == null) {
			    	outputLabelsWithSameNodeUri = new ArrayList<SemanticLabel>();
			    	groupedMappedOutputLabelsByNodeUri.put(sl.getNodeLabel().getUri(), outputLabelsWithSameNodeUri);
			    }
			    outputLabelsWithSameNodeUri.add(sl);
			}
			
			for (String s : groupedMappedOutputLabelsByNodeUri.keySet()) {
				List<SemanticLabel> groupedOutputList = groupedMappedOutputLabelsByNodeUri.get(s);
				InternalNode n = alignment.addInternalNodeWithoutUpdatingGraph(new Label(s));
				for (SemanticLabel sl : groupedOutputList) {
					ColumnNode c = alignment.addColumnNodeWithoutUpdatingGraph(sl.getLeafName(), sl.getLeafName(), "");
					alignment.addDataPropertyLink(n, c, sl.getLinkLabel(), false);
				}
			}
		}
//		GraphUtil.printGraphSimple(alignment.getGraph());
		alignment.updateGraph();
		this.graph = alignment.getGraph();
		this.updateHashMaps();
	}
	
	private void updateHashMaps() {
		for (Node node : this.graph.vertexSet()) {
			
			List<Node> nodesWithSameUri = uriToNodesMap.get(node.getLabel().getUri());
			if (nodesWithSameUri == null) {
				nodesWithSameUri = new ArrayList<Node>();
				uriToNodesMap.put(node.getLabel().getUri(), nodesWithSameUri);
			}
			nodesWithSameUri.add(node);
			
			List<Node> nodesWithSameType = typeToNodesMap.get(node.getType());
			if (nodesWithSameType == null) {
				nodesWithSameType = new ArrayList<Node>();
				typeToNodesMap.put(node.getType(), nodesWithSameType);
			}
			nodesWithSameType.add(node);
		}
	}
	
	private void updateWeights() {
		
		String id;
		Double weight, linkWeightInTrainingset;
		Label label;
		
		List<String> objectPropertiesDirect;
		List<String> objectPropertiesIndirect;
		List<String> objectPropertiesWithOnlyDomain;
		List<String> objectPropertiesWithOnlyRange;
		HashMap<String, Label> objectPropertiesWithoutDomainAndRange = 
				ontologyManager.getObjectPropertiesWithoutDomainAndRange();

		for (DomainRangePair dr : this.sourceTargetList) {
			
			List<Node> sources = this.uriToNodesMap.get(dr.getDomain());
			List<Node> targets = this.uriToNodesMap.get(dr.getRange());

			if (sources == null || sources.size() == 0) {
				logger.debug("Cannot find any node with uri=" + dr.getDomain() + " in the graph.");
				continue;
			}
			if (targets == null || targets.size() == 0) {
				logger.debug("Cannot find any node with uri=" + dr.getRange() + " in the graph.");
				continue;
			}
			
			objectPropertiesDirect = ontologyManager.getObjectPropertiesDirect(dr.getDomain(), dr.getRange());
			objectPropertiesIndirect = ontologyManager.getObjectPropertiesIndirect(dr.getDomain(), dr.getRange());
			objectPropertiesWithOnlyDomain = ontologyManager.getObjectPropertiesWithOnlyDomain(dr.getDomain(), dr.getRange());
			objectPropertiesWithOnlyRange = ontologyManager.getObjectPropertiesWithOnlyRange(dr.getDomain(), dr.getRange());
			
			HashMap<String, Double> linkWeight = this.sourceTargetToLinkWeightMap.get(dr.getDomain() + dr.getRange());
			if (linkWeight == null || linkWeight.size() == 0) continue;

			boolean linkExistInOntology;
			
			for (Node n1 : sources) {
				for (Node n2 : targets) {
					for (String s : linkWeight.keySet()) {
						
//						System.out.print(n1.getLabel().getUri() + "===");
//						System.out.print(n2.getLabel().getUri() + "===");
//						System.out.println(s + "===wieght: " + linkWeight.get(s));
						
						linkExistInOntology = false;
						
						if (objectPropertiesWithoutDomainAndRange != null && objectPropertiesWithoutDomainAndRange.containsKey(s)) 
							linkExistInOntology = true;
						else if (objectPropertiesDirect != null && objectPropertiesDirect.contains(s))
							linkExistInOntology = true;
						else if (objectPropertiesIndirect != null && objectPropertiesIndirect.contains(s))
							linkExistInOntology = true;
						else if (objectPropertiesWithOnlyDomain != null && objectPropertiesWithOnlyDomain.contains(s))
							linkExistInOntology = true;
						else if (objectPropertiesWithOnlyRange != null && objectPropertiesWithOnlyRange.contains(s))
							linkExistInOntology = true;
						
						if (!linkExistInOntology) {
//							logger.warn("The link " + s + " from " + dr.getDomain() + " to " + dr.getRange() + 
//									" cannot be inferred from the ontology, but we assert it because it exists in training data.");
							logger.warn("The link " + s + " from " + dr.getDomain() + " to " + dr.getRange() + 
									" cannot be inferred from the ontology, so we don't consider it in our graph.");
							continue;
						}
						
						linkWeightInTrainingset = linkWeight.get(s);
						if (linkWeightInTrainingset == null) continue;
						
						id = linkIdFactory.getLinkId(s);
						label = new Label(s);
						Link newLink = new ObjectPropertyLink(id, label);
						// prefer the links that are actually defined between source and target in the ontology 
						// over inherited ones.
						graph.addEdge(n1, n2, newLink);
						
						weight = ModelingParams.PROPERTY_DIRECT_WEIGHT - 10 * linkWeightInTrainingset.doubleValue();
						if (weight < 0) weight = 0.0;
						
						graph.setEdgeWeight(newLink,  weight);
					}					
				}
			}


		}
	}
	
	private List<Node> computeSteinerNodes() {
		
		List<Node> steinerNodes = new ArrayList<Node>();
		if (this.typeToNodesMap.get(NodeType.ColumnNode) != null) { 
			for (Node n : this.typeToNodesMap.get(NodeType.ColumnNode)) {
				steinerNodes.add(n);
				// add node domain
				Set<Link> incomingLinks = this.graph.incomingEdgesOf(n);
				if (incomingLinks != null) { // && incomingLinks.size() == 1) {
					Link link = incomingLinks.toArray(new Link[0])[0];
					steinerNodes.add(link.getSource());
				}
			}
		}
		if (this.typeToNodesMap.get(NodeType.LiteralNode) != null) { 
			for (Node n : this.typeToNodesMap.get(NodeType.LiteralNode)) {
				steinerNodes.add(n);
				// add node domain
				Set<Link> incomingLinks = this.graph.incomingEdgesOf(n);
				if (incomingLinks != null) { // && incomingLinks.size() == 1) {
					Link link = incomingLinks.toArray(new Link[0])[0];
					steinerNodes.add(link.getSource());
				}
			}
		}
		
		return steinerNodes;
	}
	
	public DirectedWeightedMultigraph<Node, Link> hypothesize() {
		
		List<Node> steinerNodes = this.computeSteinerNodes();
		if (steinerNodes == null || steinerNodes.size() == 0) {
			logger.error("There is no steiner node.");
			return null;
		}

		logger.info("updating weights according to training data ...");
		long start = System.currentTimeMillis();
		this.updateWeights();
		long updateWightsElapsedTimeMillis = System.currentTimeMillis() - start;
		logger.info("time to update weights: " + (updateWightsElapsedTimeMillis/1000F));

		UndirectedGraph<Node, Link> undirectedGraph = new AsUndirectedGraph<Node, Link>(graph);
		logger.info("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		DirectedWeightedMultigraph<Node, Link> tree = 
				(DirectedWeightedMultigraph<Node, Link>)GraphUtil.asDirectedGraph(steinerTree.getSteinerTree());
		
		long steinerTreeElapsedTimeMillis = System.currentTimeMillis() - updateWightsElapsedTimeMillis;
		logger.info("total number of nodes in steiner tree: " + tree.vertexSet().size());
		logger.info("total number of edges in steiner tree: " + tree.edgeSet().size());
		logger.info("time to compute steiner tree: " + (steinerTreeElapsedTimeMillis/1000F));

		long finalTreeElapsedTimeMillis = System.currentTimeMillis() - steinerTreeElapsedTimeMillis;
		DirectedWeightedMultigraph<Node, Link> finalTree = buildOutputTree(tree);
		logger.info("time to build final tree: " + (finalTreeElapsedTimeMillis/1000F));

		return finalTree;

	}
	
	private DirectedWeightedMultigraph<Node, Link> buildOutputTree(DirectedWeightedMultigraph<Node, Link> tree) {
		
		String sourceUri, targetUri;
		String id;
		Label label;
		String uri;
		Link[] links = tree.edgeSet().toArray(new Link[0]);
		List<String> possibleLinksFromSourceToTarget = new ArrayList<String>();
		List<String> possibleLinksFromTargetToSource = new ArrayList<String>();

		List<String> objectPropertiesDirect;
		List<String> objectPropertiesIndirect;
		List<String> objectPropertiesWithOnlyDomain;
		List<String> objectPropertiesWithOnlyRange;
		HashMap<String, Label> objectPropertiesWithoutDomainAndRange = 
				ontologyManager.getObjectPropertiesWithoutDomainAndRange();
		
		for (Link link : links) {
			if (!(link instanceof SimpleLink)) continue;
			
			// links from source to target
			sourceUri = link.getSource().getLabel().getUri();
			targetUri = link.getTarget().getLabel().getUri();
			
			possibleLinksFromSourceToTarget.clear();
			possibleLinksFromTargetToSource.clear();

			if (link.getWeight() == ModelingParams.PROPERTY_DIRECT_WEIGHT) {
				objectPropertiesDirect = ontologyManager.getObjectPropertiesDirect(sourceUri, targetUri);
				if (objectPropertiesDirect != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesDirect);
				objectPropertiesDirect = ontologyManager.getObjectPropertiesDirect(targetUri, sourceUri);
				if (objectPropertiesDirect != null) possibleLinksFromTargetToSource.addAll(objectPropertiesDirect);
			}
			
			if (link.getWeight() == ModelingParams.PROPERTY_INDIRECT_WEIGHT) {
				objectPropertiesIndirect = ontologyManager.getObjectPropertiesIndirect(sourceUri, targetUri);
				if (objectPropertiesIndirect != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesIndirect);
				objectPropertiesIndirect = ontologyManager.getObjectPropertiesIndirect(targetUri, sourceUri);
				if (objectPropertiesIndirect != null) possibleLinksFromTargetToSource.addAll(objectPropertiesIndirect);
			} 

			if (link.getWeight() == ModelingParams.PROPERTY_WITH_ONLY_DOMAIN_WEIGHT) {
				objectPropertiesWithOnlyDomain = ontologyManager.getObjectPropertiesWithOnlyRange(sourceUri, targetUri);
				if (objectPropertiesWithOnlyDomain != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesWithOnlyDomain);
				objectPropertiesWithOnlyDomain = ontologyManager.getObjectPropertiesWithOnlyRange(targetUri, sourceUri);
				if (objectPropertiesWithOnlyDomain != null) possibleLinksFromTargetToSource.addAll(objectPropertiesWithOnlyDomain);
			} 
			
			if (link.getWeight() == ModelingParams.PROPERTY_WITH_ONLY_RANGE_WEIGHT) {
				objectPropertiesWithOnlyRange = ontologyManager.getObjectPropertiesIndirect(sourceUri, targetUri);
				if (objectPropertiesWithOnlyRange != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesWithOnlyRange);
				objectPropertiesWithOnlyRange = ontologyManager.getObjectPropertiesIndirect(targetUri, sourceUri);
				if (objectPropertiesWithOnlyRange != null) possibleLinksFromTargetToSource.addAll(objectPropertiesWithOnlyRange);
			} 
			
			if (link.getWeight() == ModelingParams.PROPERTY_WITHOUT_DOMAIN_RANGE_WEIGHT) {
				if (objectPropertiesWithoutDomainAndRange != null) {
					possibleLinksFromSourceToTarget.addAll(objectPropertiesWithoutDomainAndRange.keySet());
					possibleLinksFromTargetToSource.addAll(objectPropertiesWithoutDomainAndRange.keySet());
				}
			}
			
			if (link.getWeight() == ModelingParams.SUBCLASS_WEIGHT) {
				if (ontologyManager.isSubClass(sourceUri, targetUri, true)) 
					possibleLinksFromSourceToTarget.add(Uris.RDFS_SUBCLASS_URI);
				if (ontologyManager.isSubClass(targetUri, sourceUri, true)) 
					possibleLinksFromTargetToSource.add(Uris.RDFS_SUBCLASS_URI);
			}

			if (possibleLinksFromSourceToTarget.size() > 0) {
				uri = possibleLinksFromSourceToTarget.get(0);
				id = linkIdFactory.getLinkId(uri);
				label = new Label(uri);
				
				Link newLink;
				if (uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
					newLink = new SubClassLink(id);
				else
					newLink = new ObjectPropertyLink(id, label);
				
				tree.addEdge(link.getSource(), link.getTarget(), newLink);
				tree.removeEdge(link);
				
			} else if (possibleLinksFromTargetToSource.size() > 0) {
				uri = possibleLinksFromTargetToSource.get(0);
				id = linkIdFactory.getLinkId(uri);
				label = new Label(uri);
				
				Link newLink;
				if (uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
					newLink = new SubClassLink(id);
				else
					newLink = new ObjectPropertyLink(id, label);
				
				tree.addEdge(link.getTarget(), link.getSource(), newLink);
				tree.removeEdge(link);
				
			} else {
				logger.error("Something is going wrong. There should be at least one possible object property between " +
						sourceUri + " and " + targetUri);
				return null;
			}
		}
		
		return tree;
	}
	
	private static void testApproach() throws IOException {
		
		List<ServiceModel> serviceModels = ModelReader.importServiceModels();
		for (ServiceModel sm : serviceModels)
			sm.computeMatchedSubGraphs(1);

		List<ServiceModel> trainingData = new ArrayList<ServiceModel>();
		
		OntologyManager ontManager = new OntologyManager();
		ontManager.doImport(new File(Approach1.ontologyDir + "dbpedia_3.8.owl"));
		ontManager.doImport(new File(Approach1.ontologyDir + "foaf.rdf"));
		ontManager.doImport(new File(Approach1.ontologyDir + "geonames.rdf"));
		ontManager.doImport(new File(Approach1.ontologyDir + "wgs84_pos.xml"));
		ontManager.doImport(new File(Approach1.ontologyDir + "schema.rdf"));
//		ontManager.doImport(new File(Approach1.ontologyDir + "helper.owl"));
		ontManager.updateCache();

		for (int i = 0; i < serviceModels.size(); i++) {
//		int i = 12; {
			
			trainingData.clear();
			int inputModelIndex = i;
			ServiceModel sm = serviceModels.get(inputModelIndex);
			
			logger.info("======================================================");
			logger.info(sm.getServiceDescription());
			logger.info("======================================================");
			
			for (int j = 0; j < serviceModels.size(); j++) {
				if (j != inputModelIndex) trainingData.add(serviceModels.get(j));
			}
			
			DirectedWeightedMultigraph<Node, Link> inputModel = sm.getModels().get(0);
			DirectedWeightedMultigraph<Node, Link> outputModel = sm.getModels().get(1);
			
			Approach1 app = new Approach1(inputModel, trainingData, ontManager);
			app.preprocess();
			
			String graphName = Approach1.outputDir + "graph" + String.valueOf(i+1);
			if (new File(graphName).exists()) {
				// read graph from file
				try {
					app.loadGraph(GraphUtil.deserialize(graphName));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				logger.info("building the graph ...");
				app.buildGraph();
				// save graph to file
				try {
					GraphUtil.serialize(app.getGraph(), graphName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
	
	//		GraphUtil.printGraph(graph);
	
			DirectedWeightedMultigraph<Node, Link> hypothesis = app.hypothesize();
//			if (hypothesis == null)
//				continue;
			
			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
			
			graphs.put("1-input", inputModel);
			graphs.put("2-output", outputModel);
			graphs.put("3-hypothesis", hypothesis);
			
			GraphVizUtil.exportJGraphToGraphvizFile(graphs, 
					sm.getServiceDescription(), 
					Approach1.outputDir + "output" + String.valueOf(i+1) + "-2.dot");
			
			GraphUtil.printGraphSimple(hypothesis);			

		}
	}
	
	public static void main(String[] args) {
		
		try {
//			testSelectionOfBestMatch();
			testApproach();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
