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
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import edu.isi.karma.modeling.ModelingParams;
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
	
	private List<ServiceModel> trainingData;
	private OntologyManager ontologyManager;
	private List<SemanticLabel> inputSemanticLabels;
	private List<SemanticLabel> outputSemanticLabels;
	
	private double directLinkWeight;
	private double indirectLinkWeight;
	
	public Approach1(List<ServiceModel> trainingData, OntologyManager ontologyManager) {
		this.trainingData = trainingData;
		this.ontologyManager = ontologyManager;
		this.linkIdFactory = new LinkIdFactory();
		this.sourceTargetList = new ArrayList<DomainRangePair>();
		this.sourceTargetToLinkCounterMap = new HashMap<String, HashMap<String,Integer>>();
		this.inputSemanticLabels = new ArrayList<SemanticLabel>();
		this.outputSemanticLabels = new ArrayList<SemanticLabel>();
		this.directLinkWeight = this.getInitialLinkWeight();
		this.indirectLinkWeight = this.directLinkWeight + ModelingParams.MIN_WEIGHT;
		this.typeToNodesMap = new HashMap<NodeType, List<Node>>();
		this.uriToNodesMap = new HashMap<String, List<Node>>();
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
	
	private double getInitialLinkWeight() {
		double w = 0;
		for (ServiceModel sm : this.trainingData) {
			DirectedWeightedMultigraph<Node, Link> m = sm.getModels().get(1);
			w += m.edgeSet().size();
		}
		return w;
	}
	
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
			DirectedWeightedMultigraph<Node, Link> serviceModel) {
		
		List<SemanticLabel> semanticLabels = new ArrayList<SemanticLabel>();

		for (Node n : serviceModel.vertexSet()) {
			if (!(n instanceof ColumnNode) && !(n instanceof LiteralNode)) continue;
			
			Set<Link> incomingLinks = serviceModel.incomingEdgesOf(n);
			if (incomingLinks != null) { // && incomingLinks.size() == 1) {
				Link link = incomingLinks.toArray(new Link[0])[0];
				Node domain = link.getSource();
				
				SemanticLabel sl = new SemanticLabel(domain.getLabel(), link.getLabel(), n.getId());
				semanticLabels.add(sl);
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
				slList1 = getModelSemanticLabels(m.getSubGraph1());
				if (slList1 != null && slList1.size() == 1) sl1 = slList1.get(0); else sl1 = null;
				
				slList2 = getModelSemanticLabels(m.getSubGraph2());
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
		
		this.inputSemanticLabels = getModelSemanticLabels(serviceModel);
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
				return;
			}
			
			logger.info("Matched Semantic Labels: ");
			for (SemanticLabel matched: matchedSemanticLabels)
				matched.print();
			logger.info("-------------------------------------------");

			SemanticLabel bestMatch = selectBestMatchedSemanticLabel(matchedSemanticLabels);
			outputSemanticLabels.add(bestMatch);

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

	public void buildGraph(List<SemanticLabel> semanticLabels) {
		
		Alignment alignment = new Alignment(this.ontologyManager);
		
		for (SemanticLabel sl : semanticLabels) {
			InternalNode n = alignment.addInternalNodeWithoutUpdatingGraph(sl.getNodeLabel());
//			ColumnNode c = alignment.addColumnNode("H" + String.valueOf(i), sl.getLeafName());
			ColumnNode c = alignment.addColumnNodeWithoutUpdatingGraph(sl.getLeafName(), sl.getLeafName());
			alignment.addDataPropertyLink(n, c, sl.getLinkLabel(), false);
		}
		
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
	
	public void updateWeights() {
		String key, id;
		Integer count;
		double w;
		Label label;
		
		Link[] links = graph.edgeSet().toArray(new Link[0]);
		for (Link link : links) {
			
			if (!(link instanceof SimpleLink)) continue;
			
			w = this.directLinkWeight;
			
			if (link.getWeight() == ModelingParams.DEFAULT_WEIGHT + ModelingParams.MIN_WEIGHT)
				w = this.indirectLinkWeight;
			
			graph.setEdgeWeight(link, w);
		}
		
		for (DomainRangePair dr : this.sourceTargetList) {
			
			key = dr.getDomain() + 
					dr.getRange();

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
			
			List<String> possibleLinksFromSourceToTarget = 
					this.ontologyManager.getIndirectDomainRangeProperties().get(key);

			HashMap<String, Integer> linkCount = this.sourceTargetToLinkCounterMap.get(key);
			if (linkCount == null || linkCount.size() == 0) continue;

			for (Node n1 : sources) {
				for (Node n2 : targets) {
					for (String s : linkCount.keySet()) {
						
						if (!possibleLinksFromSourceToTarget.contains(s)) {
							logger.warn("The link " + s + " from " + dr.getDomain() + " to " + dr.getRange() + " cannot be inferred from the ontology, " +
									"so we don't consider it in our graph.");
							continue;
						}
						
						count = linkCount.get(s);
						if (count == null) continue;
						
						id = linkIdFactory.getLinkId(s);
						label = new Label(s);
						Link newLink = new ObjectPropertyLink(id, label);
						// prefer the links that are actually defined between source and target in the ontology 
						// over inherited ones.
						graph.addEdge(n1, n2, newLink);
						graph.setEdgeWeight(newLink, this.indirectLinkWeight - count.intValue());
					}					
				}
			}


		}
	}
	
	private List<Node> computeSteinerNodes() {
		
		List<Node> steinerNodes = this.typeToNodesMap.get(NodeType.ColumnNode);
		return steinerNodes;
	}
	
	public DirectedWeightedMultigraph<Node, Link> hypothesize() {
		
		List<Node> steinerNodes = this.computeSteinerNodes();
		this.updateWeights();

		UndirectedGraph<Node, Link> undirectedGraph = new AsUndirectedGraph<Node, Link>(graph);
		logger.info("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodes);
		DirectedWeightedMultigraph<Node, Link> tree = 
				(DirectedWeightedMultigraph<Node, Link>)GraphUtil.asDirectedGraph(steinerTree.getSteinerTree());
		return buildOutputTree(tree);

	}
	
	private DirectedWeightedMultigraph<Node, Link> buildOutputTree(DirectedWeightedMultigraph<Node, Link> tree) {
		
		String key1, key2;
		String id;
		Label label;
		Link[] links = tree.edgeSet().toArray(new Link[0]);
		for (Link link : links) {
			if (!(link instanceof SimpleLink)) continue;
			
			// links from source to target
			List<String> possibleLinksFromSourceToTarget;
			key1 = link.getSource().getLabel().getUri() + 
					link.getTarget().getLabel().getUri();

			if (link.getWeight() == this.indirectLinkWeight)
				possibleLinksFromSourceToTarget = this.ontologyManager.getIndirectDomainRangeProperties().get(key1);
			else
				possibleLinksFromSourceToTarget = this.ontologyManager.getDirectDomainRangeProperties().get(key1);
			
			// links from target to source
			List<String> possibleLinksFromTargetToSource;
			key2 = link.getTarget().getLabel().getUri() + 
					link.getSource().getLabel().getUri();

			if (link.getWeight() == this.indirectLinkWeight)
				possibleLinksFromTargetToSource = this.ontologyManager.getIndirectDomainRangeProperties().get(key2);
			else
				possibleLinksFromTargetToSource = this.ontologyManager.getDirectDomainRangeProperties().get(key2);
			
			if (possibleLinksFromSourceToTarget != null && possibleLinksFromSourceToTarget.size() > 0) {
				id = linkIdFactory.getLinkId(possibleLinksFromSourceToTarget.get(0));
				label = new Label(possibleLinksFromSourceToTarget.get(0));
				Link newLink = new ObjectPropertyLink(id, label);
				// prefer the links that are actually defined between source and target in the ontology 
				// over inherited ones.
				tree.addEdge(link.getSource(), link.getTarget(), newLink);
				tree.removeEdge(link);
			} else if (possibleLinksFromTargetToSource != null && possibleLinksFromTargetToSource.size() > 0) {
				id = linkIdFactory.getLinkId(possibleLinksFromTargetToSource.get(0));
				label = new Label(possibleLinksFromTargetToSource.get(0));
				Link newLink = new ObjectPropertyLink(id, label);
				// prefer the links that are actually defined between source and target in the ontology 
				// over inherited ones.
				tree.addEdge(link.getTarget(), link.getSource(), newLink);
				tree.removeEdge(link);
			} else {
				logger.error("Something is going wrong. There should be at least one possible object property.");
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
			
			for (int j = 0; j < serviceModels.size(); j++) {
				if (j != inputModelIndex) trainingData.add(serviceModels.get(j));
			}
			
			DirectedWeightedMultigraph<Node, Link> inputModel = 
					serviceModels.get(inputModelIndex).getModels().get(inputModelIndex);
//			DirectedWeightedMultigraph<Node, Link> oututModel = serviceModels.get(inputModelIndex).getModels().get(1);
			
			Approach1 app = new Approach1(trainingData, ontManager);
			app.preprocess(inputModel);
			app.buildGraph(app.getOutputSemanticLabels());
			
			// save graph to file
			try {
				GraphUtil.serialize(app.getGraph(), Approach1.outputDir + "graph" + String.valueOf(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// read graph from file
	//		try {
	//			app.loadGraph(GraphUtil.deserialize(graphPath));
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	
	//		GraphUtil.printGraph(graph);
	
			DirectedWeightedMultigraph<Node, Link> hypothesis = app.hypothesize();
			GraphVizUtil.exportJGraphToGraphvizFile(hypothesis, Approach1.outputDir + "output" + String.valueOf(i) + ".dot");
			
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
