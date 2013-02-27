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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

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

public class Approach1 {

	private static Logger logger = Logger.getLogger(Approach1.class);
	private static String ontologyDir = "/Users/mohsen/Dropbox/Service Modeling/ontologies/";
	private static String outputDir = "/Users/mohsen/Dropbox/Service Modeling/output/";
	
	private DirectedWeightedMultigraph<Node, Link> graph;
	private HashMap<NodeType, List<Node>> typeToNodesMap;
	private HashMap<String, List<Node>> uriToNodesMap;
	private LinkIdFactory linkIdFactory;
	private List<DomainRangePair> sourceTargetList;
	private HashMap<String, HashMap<String, Integer>> sourceTargetToLinkCounterMap;
	private HashMap<Node, List<SemanticLabel>> nodeToSemanticLabels;
	private HashMap<SemanticLabel, SemanticLabel> inputLabelsToOutputLabelsMatching;
	
	private List<ServiceModel> trainingData;
	private OntologyManager ontologyManager;
	private List<SemanticLabel> inputSemanticLabels;
	private List<SemanticLabel> outputSemanticLabels;
	
	public Approach1(List<ServiceModel> trainingData, OntologyManager ontologyManager) {
		this.trainingData = trainingData;
		this.ontologyManager = ontologyManager;
		this.linkIdFactory = new LinkIdFactory();
		this.inputSemanticLabels = new ArrayList<SemanticLabel>();
		this.outputSemanticLabels = new ArrayList<SemanticLabel>();
		this.typeToNodesMap = new HashMap<NodeType, List<Node>>();
		this.uriToNodesMap = new HashMap<String, List<Node>>();
		this.sourceTargetList = new ArrayList<DomainRangePair>();
		this.sourceTargetToLinkCounterMap = new HashMap<String, HashMap<String,Integer>>();
		this.nodeToSemanticLabels = new HashMap<Node, List<SemanticLabel>>();
		this.inputLabelsToOutputLabelsMatching = new HashMap<SemanticLabel, SemanticLabel>();
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
	
	private void buildSourceTargetLinkCounterMap() {
		Integer count;
		for (ServiceModel sm : this.trainingData) {
			DirectedWeightedMultigraph<Node, Link> m = sm.getModels().get(1);
			for (Link link : m.edgeSet()) {
				Node source = link.getSource();
				Node target = link.getTarget();
				if (source instanceof InternalNode && target instanceof InternalNode) {
					String key = source.getLabel().getUri() +
								 target.getLabel().getUri();
					
					HashMap<String, Integer> linkCount = this.sourceTargetToLinkCounterMap.get(key);
					if (linkCount == null) { 
						linkCount = new HashMap<String, Integer>();
						linkCount.put(link.getLabel().getUri(), 1);
						this.sourceTargetList.add(
								new DomainRangePair(source.getLabel().getUri(), target.getLabel().getUri()));
						this.sourceTargetToLinkCounterMap.put(key, linkCount);
					} else {
						count = linkCount.get(link.getLabel().getUri());
						if (count == null) linkCount.put(link.getLabel().getUri(), 1);
						else linkCount.put(link.getLabel().getUri(), ++count);
					}
				}
			}
		}
	}
	
	private List<SemanticLabel> getModelSemanticLabels(
			DirectedWeightedMultigraph<Node, Link> serviceModel,
			boolean updateNodeToSemanticLabelsMap) {
		
		List<SemanticLabel> semanticLabels = new ArrayList<SemanticLabel>();

		for (Node n : serviceModel.vertexSet()) {
			if (!(n instanceof ColumnNode) && !(n instanceof LiteralNode)) continue;
			
			Set<Link> incomingLinks = serviceModel.incomingEdgesOf(n);
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

	public void preprocess(DirectedWeightedMultigraph<Node, Link> serviceModel) {
		
		this.buildSourceTargetLinkCounterMap();
		
		this.inputSemanticLabels = getModelSemanticLabels(serviceModel, true);
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
				logger.info("Cannot find any match for semantic label:");
				sl.print();
				continue;
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
					ColumnNode c = alignment.addColumnNodeWithoutUpdatingGraph(sl.getLeafName(), sl.getLeafName());
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
		Integer count;
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
			
			HashMap<String, Integer> linkCount = this.sourceTargetToLinkCounterMap.get(dr.getDomain() + dr.getRange());
			if (linkCount == null || linkCount.size() == 0) continue;

			boolean linkExistInOntology;
			
			for (Node n1 : sources) {
				for (Node n2 : targets) {
					for (String s : linkCount.keySet()) {
						
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
//									" cannot be inferred from the ontology, so we don't consider it in our graph.");
//							continue;
							logger.warn("The link " + s + " from " + dr.getDomain() + " to " + dr.getRange() + 
									" cannot be inferred from the ontology, but we assert it because it exists in training data.");
//							continue;
						}
						
						count = linkCount.get(s);
						if (count == null) continue;
						
						id = linkIdFactory.getLinkId(s);
						label = new Label(s);
						Link newLink = new ObjectPropertyLink(id, label);
						// prefer the links that are actually defined between source and target in the ontology 
						// over inherited ones.
						graph.addEdge(n1, n2, newLink);
						graph.setEdgeWeight(newLink, ModelingParams.PROPERTY_DIRECT_WEIGHT - count.intValue());
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
		this.updateWeights();

		UndirectedGraph<Node, Link> undirectedGraph = new AsUndirectedGraph<Node, Link>(graph);
		logger.info("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		DirectedWeightedMultigraph<Node, Link> tree = 
				(DirectedWeightedMultigraph<Node, Link>)GraphUtil.asDirectedGraph(steinerTree.getSteinerTree());
		return buildOutputTree(tree);

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
		ontManager.doImport(new File(Approach1.ontologyDir + "helper.owl"));
//		ontManager.doImport(new File(this.ontologyDir + "wgs84_pos.xml"));
//		ontManager.doImport(new File(this.ontologyDir + "schema.rdf"));
		ontManager.updateCache();

		for (int i = 0; i < serviceModels.size(); i++) {
			
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
			
			Approach1 app = new Approach1(trainingData, ontManager);
			app.preprocess(inputModel);
			
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
			if (hypothesis == null)
				continue;
			
			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
			
			graphs.put("1-input", inputModel);
			graphs.put("2-output", outputModel);
			graphs.put("3-hypothesis", hypothesis);
			
			GraphVizUtil.exportJGraphToGraphvizFile(graphs, 
					sm.getServiceDescription(), 
					Approach1.outputDir + "output" + String.valueOf(i+1) + ".dot");
			
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
