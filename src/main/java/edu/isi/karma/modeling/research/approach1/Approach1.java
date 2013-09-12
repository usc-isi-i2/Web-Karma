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

package edu.isi.karma.modeling.research.approach1;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.AsUndirectedGraph;
import org.jgrapht.graph.DirectedWeightedMultigraph;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.GraphVizUtil;
import edu.isi.karma.modeling.research.ModelReader;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.modeling.research.PatternContainment;
import edu.isi.karma.modeling.research.SemanticLabel;
import edu.isi.karma.modeling.research.ServiceModel;
import edu.isi.karma.modeling.research.Util;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
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

	private HashMap<SemanticLabel, Set<MappingStruct>> labelToMappingStructs;
	
	private NodeIdFactory nodeIdFactory;
	
	private List<ServiceModel> trainingData;
	private OntologyManager ontologyManager;
	private GraphBuilder graphBuilder;
	
	private Set<DirectedWeightedMultigraph<Node, Link>> graphComponents;

	
	private static final int MAX_CANDIDATES = 5;
	private static final int MAX_STEINER_NODES_SETS = 100;
	
	private HashSet<Link> patternLinks;
	private HashMap<String, Integer> linkCountMap;
	private Multimap<String, String> sourceToTargetLinks;

	private class LinkFrequency implements Comparable<LinkFrequency>{
		
		public LinkFrequency(String linkUri, int type, int count) {
			this.linkUri = linkUri;
			this.type = type;
			this.count = count;
		}
		
		private String linkUri;
		private int type;
		private int count;
		
		public double getWeight() {
			
			double weight = 0.0;
			double w = ModelingParams.PROPERTY_DIRECT_WEIGHT;
			double epsilon = ModelingParams.PATTERN_LINK_WEIGHT;
//			double factor = 0.01;
			int c = this.count < (int)w ? this.count : (int)w - 1;
			
			if (type == 1) // match domain, link, and range
				weight = w - (epsilon / (w - c));
			else if (type == 2) // match link and range
				weight = w - (epsilon / ((w - c) * w));
			else if (type == 3) // match domain and link
				weight = w - (epsilon / ((w - c) * w));
			else if (type == 4) // match link
				weight = w - (epsilon / ((w - c) * w * w));
			else if (type == 5) // direct property
				weight = w;
			else if (type == 6) // indirect property
				weight = w + epsilon - (epsilon / (w - c));
			else if (type == 7) // property with only domain
				weight = w + epsilon + (epsilon / ((w - c) * w));
			else if (type == 8) // property with only range
				weight = w + epsilon + (epsilon / ((w - c) * w));
			else if (type == 9) // subClass
				weight = w + epsilon + (epsilon / ((w - c) * w * w));
			else if (type == 10) // property without domain and range
				weight = w + epsilon + (epsilon / ((w - c) * w * w * w));
			return weight;
		}
		
		@Override
		public int compareTo(LinkFrequency o) {
			if (linkUri == null && o.linkUri != null)
				return -1;
			else if (linkUri != null && o.linkUri == null)
				return 1;
			else if (linkUri == null && o.linkUri == null)
				return 0;
			else {
				if (type < o.type)
					return 1;
				else if (type > o.type)
					return -1;
				else {
					if (count >= o.count) 
						return 1;
					else 
						return -1;
				}
			}
		}
	}
	
	public Approach1(List<ServiceModel> trainingData, 
			OntologyManager ontologyManager) {
		
		this.graphComponents = new HashSet<DirectedWeightedMultigraph<Node,Link>>();
		this.trainingData = trainingData;
		this.ontologyManager = ontologyManager;
		
//		this.linkIdFactory = new LinkIdFactory();
		this.nodeIdFactory = new NodeIdFactory();
		
		this.graphBuilder = new GraphBuilder(ontologyManager, nodeIdFactory);//, linkIdFactory);

		this.labelToMappingStructs = new HashMap<SemanticLabel, Set<MappingStruct>>();
		this.patternLinks = new HashSet<Link>();
		
		this.linkCountMap = new HashMap<String, Integer>();
		this.sourceToTargetLinks = ArrayListMultimap.create();	
		
		this.buildLinkCountMap();
	}

	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return this.graphBuilder.getGraph();
	}
	
	public void saveGraph(String fileName) throws Exception {
		GraphUtil.serialize(this.graphBuilder.getGraph(), fileName);
	}
	
	public void loadGraph(OntologyManager ontologyManager, String fileName) throws Exception {
		DirectedWeightedMultigraph<Node, Link> graph = GraphUtil.deserialize(fileName);
		this.graphBuilder = new GraphBuilder(ontologyManager, graph);
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
//		this.linkIdFactory = this.graphBuilder.getLinkIdFactory();
		this.updateHashMaps();
	}

	private void updateGraphWithUserLinks() {
		
//		String[] parts;
//		String sourceUri, targetUri;
//		String linkId;
//		
//		for (String s : this.sourceToTargetLinks.keys()) {
//			
//			parts = s.split("---");
//			if (parts == null || parts.length != 2) continue;
//			sourceUri = parts[0]; targetUri = parts[1];
//			
//			List<Node> sources = this.graphBuilder.getUriToNodesMap().get(sourceUri);
//			List<Node> targets = this.graphBuilder.getUriToNodesMap().get(targetUri);
//			
//			
//			for (Node source : sources) {
//				for (Node target : targets) {
//					if (!this.graphBuilder.isConnected(source.getId(), target.getId())) {
//						linkId = LinkIdFactory.getLinkId(SimpleLink.getFixedLabel().getUri(), source.getId(), target.getId());
//						Link link = new SimpleLink(linkId, SimpleLink.getFixedLabel());
//						this.graphBuilder.addLink(source, target, link);
//					}
//				}
//			}
//		}
	}

	private void updateHashMaps() {
		
		this.labelToMappingStructs.clear();
		List<Node> columnNodes = this.graphBuilder.getTypeToNodesMap().get(NodeType.ColumnNode);
		if (columnNodes != null) {
			for (Node n : columnNodes) {
				Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(n);
				if (incomingLinks != null) {
					Link[] inLinks = incomingLinks.toArray(new Link[0]);
					for (Link link : inLinks) {
						Node domain = link.getSource();
						
						if (!(domain instanceof InternalNode)) continue;
						
						SemanticLabel sl = new SemanticLabel(domain.getLabel().getUri(), link.getLabel().getUri(), n.getId());
						
						Set<MappingStruct> labelStructs = this.labelToMappingStructs.get(sl);
						if (labelStructs == null) {
							labelStructs = new HashSet<MappingStruct>();
							this.labelToMappingStructs.put(sl, labelStructs);
						}
						labelStructs.add(new MappingStruct((InternalNode)domain, link, (ColumnNode)n));
					}
					
				} else 
					logger.error("The column node " + n.getId() + " does not have any domain or it has more than one domain.");
			}
		}
		
		for (Link l : this.graphBuilder.getGraph().edgeSet()) {
			if (l.getPatternIds().size() > 0)
				this.patternLinks.add(l);
		}
	}

	private static List<SemanticLabel> getModelSemanticLabels(
			DirectedWeightedMultigraph<Node, Link> model) {
		
		List<SemanticLabel> SemanticLabel2s = new ArrayList<SemanticLabel>();

		for (Node n : model.vertexSet()) {
			if (!(n instanceof ColumnNode) && !(n instanceof LiteralNode)) continue;
			
			Set<Link> incomingLinks = model.incomingEdgesOf(n);
			if (incomingLinks != null) { // && incomingLinks.size() == 1) {
				Link link = incomingLinks.toArray(new Link[0])[0];
				Node domain = link.getSource();
				
				SemanticLabel sl = new SemanticLabel(domain.getLabel().getUri(), link.getLabel().getUri(), n.getId());
				SemanticLabel2s.add(sl);
			} 
		}
		return SemanticLabel2s;
	}

	private void buildGraphFromTrainingModels() {
		
		String patternId;
		
		// adding the patterns to the graph
		
		for (ServiceModel sm : this.trainingData) {
			
			if (sm.getModel() == null) 
				continue;
			
			patternId = sm.getId();
			
			addPatternToGraph(patternId, sm.getModel());
		}

		// adding the links inferred from the ontology
		this.graphBuilder.updateGraph();
//		this.updateGraphWithUserLinks();
		this.updateHashMaps();

	}
	
	private void addPatternToGraph(String patternId, DirectedWeightedMultigraph<Node, Link> pattern) {
		
		for (DirectedWeightedMultigraph<Node, Link> c : this.graphComponents) {
			PatternContainment containment = new PatternContainment(c, pattern);
			Set<String> mappedNodes = new HashSet<String>();
			Set<String> mappedLinks = new HashSet<String>();
			if (containment.containedIn(mappedNodes, mappedLinks)) {
				for (String n : mappedNodes) this.graphBuilder.getIdToNodeMap().get(n).getPatternIds().add(patternId);
				for (String l : mappedLinks) this.graphBuilder.getIdToLinkMap().get(l).getPatternIds().add(patternId);
				return;
			}
		}
		
		// TODO: What if an existing pattern is contained in the new pattern?
		// Can we extend the same pattern instead of adding new one
		
		DirectedWeightedMultigraph<Node, Link> component = 
				new DirectedWeightedMultigraph<Node, Link>(Link.class);
		
		HashMap<Node, Node> visitedNodes;
		Node source, target;
		Node n1, n2;
		
		// adding the patterns to the graph
		
		if (pattern == null) 
			return;
		
		visitedNodes = new HashMap<Node, Node>();
	
		for (Link e : pattern.edgeSet()) {

			source = e.getSource();
			target = e.getTarget();

			n1 = visitedNodes.get(source);
			n2 = visitedNodes.get(target);
			
			if (n1 == null) {
				
				if (source instanceof InternalNode) {
					String id = nodeIdFactory.getNodeId(source.getLabel().getUri());
					InternalNode node = new InternalNode(id, new Label(source.getLabel()));
					if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) {
						n1 = node;
						component.addVertex(node);
					} else continue;
				}
				else {
					String id = nodeIdFactory.getNodeId(source.getId());
					ColumnNode node = new ColumnNode(id, id, "", "");
					if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) {
						n1 = node;
						component.addVertex(node);
					} else continue;
				}

				visitedNodes.put(source, n1);
			}
			
			if (n2 == null) {
				
				if (target instanceof InternalNode) {
					String id = nodeIdFactory.getNodeId(target.getLabel().getUri());
					InternalNode node = new InternalNode(id, new Label(target.getLabel()));
					if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) {
						n2 = node;
						component.addVertex(node);
					} else continue;
				}
				else {
					ColumnNode node = new ColumnNode(target.getId(), "", "", "");
					if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) {
						n2 = node;
						component.addVertex(node);
					} else continue;
				}

				visitedNodes.put(target, n2);
			}

			Link link;
			String id = LinkIdFactory.getLinkId(e.getLabel().getUri(), n1.getId(), n2.getId());	
			if (n2 instanceof ColumnNode) 
				link = new DataPropertyLink(id, e.getLabel(), false);
			else 
				link = new ObjectPropertyLink(id, e.getLabel());
			
			
			link.getPatternIds().add(patternId);
			
			if (this.graphBuilder.addLink(n1, n2, link)) {
				component.addEdge(n1, n2, link);
				this.graphBuilder.changeLinkWeight(link, ModelingParams.PATTERN_LINK_WEIGHT);
			}
			
			if (!n1.getPatternIds().contains(patternId))
				n1.getPatternIds().add(patternId);
			
			if (!n2.getPatternIds().contains(patternId))
				n2.getPatternIds().add(patternId);

		}
		
		this.graphComponents.add(component);

	}

	public void addPatternAndUpdateGraph(String patternId, DirectedWeightedMultigraph<Node, Link> pattern) {
		addPatternToGraph(patternId, pattern);
		// adding the links inferred from the ontology
		this.graphBuilder.updateGraph();
		this.updateGraphWithUserLinks();
		this.updateHashMaps();
	}
	
	private void buildLinkCountMap() {
		
		String key, sourceUri, targetUri, linkUri;
		for (ServiceModel sm : this.trainingData) {
			
			DirectedWeightedMultigraph<Node, Link> m = sm.getModel();

			for (Link link : m.edgeSet()) {

				if (link instanceof DataPropertyLink) continue;
				
				sourceUri = link.getSource().getLabel().getUri();
				targetUri = link.getTarget().getLabel().getUri();
				linkUri = link.getLabel().getUri();
				
				key = sourceUri + "<" + linkUri + ">" + targetUri;
				Integer count = this.linkCountMap.get(key);
				if (count == null) this.linkCountMap.put(key, 1);
				else this.linkCountMap.put(key, count.intValue() + 1);
				
				key = sourceUri+ "<" + linkUri;
				count = this.linkCountMap.get(key);
				if (count == null) this.linkCountMap.put(key, 1);
				else this.linkCountMap.put(key, count.intValue() + 1);

				key = linkUri + ">" + targetUri;
				count = this.linkCountMap.get(key);
				if (count == null) this.linkCountMap.put(key, 1);
				else this.linkCountMap.put(key, count.intValue() + 1);

				key = linkUri;
				count = this.linkCountMap.get(key);
				if (count == null) this.linkCountMap.put(key, 1);
				else this.linkCountMap.put(key, count.intValue() + 1);

				this.sourceToTargetLinks.put(sourceUri + "---" + targetUri, linkUri);
			}
		}
	}
	
	private void updateWeights() {

		List<Link> oldLinks = new ArrayList<Link>();
		
		List<Node> sources = new ArrayList<Node>();
		List<Node> targets = new ArrayList<Node>();
		List<String> newLinks = new ArrayList<String>();
		List<Double> weights = new ArrayList<Double>();
		
		HashMap<String, LinkFrequency> sourceTargetLinkFrequency = 
				new HashMap<String, LinkFrequency>();
		
		LinkFrequency lf1, lf2;
		
		String key1, key2;
		for (Link link : this.graphBuilder.getGraph().edgeSet()) {
			
			if (!(link instanceof SimpleLink)) {
				continue;
			}
			
			key1 = link.getSource().getLabel().getUri() + 
					link.getTarget().getLabel().getUri();
			key2 = link.getTarget().getLabel().getUri() + 
					link.getSource().getLabel().getUri();
			
//			if (link.getSource().getLabel().getUri().indexOf("Place") != -1)
//				if (link.getTarget().getLabel().getUri().indexOf("City") != -1)
//					System.out.println("debug1");

//			if (link.getSource().getLabel().getUri().indexOf("City") != -1)
//				if (link.getTarget().getLabel().getUri().indexOf("Country") != -1)
//					System.out.println("debug2");
			
			lf1 = sourceTargetLinkFrequency.get(key1);
			if (lf1 == null) {
				lf1 = this.getMoreFrequentLinkBetweenNodes(link.getSource(), link.getTarget());
				sourceTargetLinkFrequency.put(key1, lf1);
			}

			lf2 = sourceTargetLinkFrequency.get(key2);
			if (lf2 == null) {
				lf2 = this.getMoreFrequentLinkBetweenNodes(link.getTarget(), link.getSource());
				sourceTargetLinkFrequency.put(key2, lf2);
			}
			
			int c = lf1.compareTo(lf2);
			if (c > 0) {
				sources.add(link.getSource());
				targets.add(link.getTarget());
				newLinks.add(lf1.linkUri);
				weights.add(lf1.getWeight());
			} else if (c < 0) {
				sources.add(link.getTarget());
				targets.add(link.getSource());
				newLinks.add(lf2.linkUri);
				weights.add(lf2.getWeight());
			} else
				continue;
			
			oldLinks.add(link);
		}
		
		for (Link link : oldLinks)
			this.graphBuilder.getGraph().removeEdge(link);
		
		String id;
		String uri;
		Label label;
		Link newLink;
		for (int i = 0; i < newLinks.size(); i++) {
			uri = newLinks.get(i);
			id = LinkIdFactory.getLinkId(uri, sources.get(i).getId(), targets.get(i).getId());
			label = new Label(uri);
			if (uri.equalsIgnoreCase(Uris.RDFS_SUBCLASS_URI))
				newLink = new SubClassLink(id);
			else
				newLink = new ObjectPropertyLink(id, label);
			this.graphBuilder.addLink(sources.get(i), targets.get(i), newLink);
			this.graphBuilder.changeLinkWeight(newLink, weights.get(i));
		}
	}
	
	private Set<Node> addDataPropertyToDomainNodes(String domainUri, String propertyUri, String columnNodeName) {
		
		Set<Node> addedNodes = new HashSet<Node>();
		
		// add dataproperty to existing classes if sl is a data node mapping
		List<Node> nodesWithSameUriOfDomain = this.graphBuilder.getUriToNodesMap().get(domainUri);
		if (nodesWithSameUriOfDomain != null) {
			for (Node source : nodesWithSameUriOfDomain) {
				if (source instanceof InternalNode && 
						source.getPatternIds().size() > 0) {
					
//					boolean propertyLinkExists = false;
					int countOfExistingPropertyLinks = 0;
					List<Link> linkWithSameUris = this.graphBuilder.getUriToLinksMap().get(propertyUri);
					if (linkWithSameUris != null)
					for (Link l : linkWithSameUris) {
						if (l.getSource().equals(source)) {
							countOfExistingPropertyLinks ++;
//							propertyLinkExists = true;
//							break;
						}
					}
					
					if (countOfExistingPropertyLinks >= 1)
						continue;

					String nodeId = nodeIdFactory.getNodeId(columnNodeName);
					ColumnNode target = new ColumnNode(nodeId, "", "", "");
					this.graphBuilder.addNodeWithoutUpdatingGraph(target);
					addedNodes.add(target);
		
					String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), target.getId());	
					Link link = new DataPropertyLink(linkId, new Label(propertyUri), false);
					this.graphBuilder.addLink(source, target, link);
				}
			}
		}
		return addedNodes;
	}
	
	private Set<Node> addSemanticLabel(SemanticLabel sl) {

		Set<Node> addedNodes = new HashSet<Node>();

		InternalNode source = null;
		String nodeId;
		nodeId = nodeIdFactory.getNodeId(sl.getNodeUri());
		source = new InternalNode(nodeId, new Label(sl.getNodeUri()));
		this.graphBuilder.addNode(source, addedNodes);
		
		if (sl.getType() == SemanticLabelType.DataProperty) {
			nodeId = nodeIdFactory.getNodeId(sl.getLeafName());
			ColumnNode target = new ColumnNode(nodeId, "", "", "");
			this.graphBuilder.addNodeWithoutUpdatingGraph(target);
			addedNodes.add(target);
	
			String linkId = LinkIdFactory.getLinkId(sl.getLinkUri(), source.getId(), target.getId());	
			Link link = new DataPropertyLink(linkId, new Label(sl.getLinkUri()), false);
			this.graphBuilder.addLink(source, target, link);
		}
		return addedNodes;
	}
	
	private CandidateSteinerSets getCandidateSteinerSets(List<SemanticLabel> semanticLabels, Set<Node> addedNodes) {

		int maxNumberOfMappedNodes = 0;
		for (SemanticLabel sl : semanticLabels) {
			if (sl.getType() == SemanticLabelType.Class)
				maxNumberOfMappedNodes += 1;
			else
				maxNumberOfMappedNodes += 2;
		}
		
		CandidateSteinerSets candidateSteinerSets = new CandidateSteinerSets(maxNumberOfMappedNodes);
		if (addedNodes == null) 
			addedNodes = new HashSet<Node>();
		
		Set<Node> tempNodeSet = null;
		for (SemanticLabel sl : semanticLabels) {
			
			SemanticTypeMapping mapping;
			if (sl.getType() == SemanticLabelType.Class)
				mapping = new SemanticTypeMapping(null, MappingType.ClassNode);
			else
				mapping = new SemanticTypeMapping(null, MappingType.DataNode);
			
			boolean addSemanticLabel = false;
			Set<MappingStruct> similarStructsInGraph = this.labelToMappingStructs.get(sl);
			
			// if semantic label is a data property, we add this property to all the nodes having the same domain
			if (sl.getType() == SemanticLabelType.DataProperty) {
				tempNodeSet = addDataPropertyToDomainNodes(sl.getNodeUri(), sl.getLinkUri(), sl.getLeafName());
				addedNodes.addAll(tempNodeSet);
			}
			
			if ((sl.getType() == SemanticLabelType.Class && similarStructsInGraph == null) ||
					sl.getType() == SemanticLabelType.DataProperty && similarStructsInGraph == null && tempNodeSet.size() == 0) 
				addSemanticLabel = true;
			
			if (addSemanticLabel) {
				tempNodeSet = addSemanticLabel(sl);
				addedNodes.addAll(tempNodeSet);
			}
			
			this.updateHashMaps();
			similarStructsInGraph = this.labelToMappingStructs.get(sl); 
			
			for (MappingStruct ms : similarStructsInGraph) {
				mapping.addMappingStruct(ms);
			}
			
			candidateSteinerSets.updateSteinerSets(mapping);
			
		}
		
		return candidateSteinerSets;
	}
	
	
//	private List<RankedSteinerSet> rankSteinerSets(List<Set<Node>> steinerNodeSets) {
//		
//		List<RankedSteinerSet> rankedSteinerSets = new ArrayList<RankedSteinerSet>();
//		for (Set<Node> nodes : steinerNodeSets) {
////			if (nodes.size() == 17)
////				System.out.println(nodes.size());
//			RankedSteinerSet r = new RankedSteinerSet(nodes);
//			rankedSteinerSets.add(r);
//		}
//		
//		Collections.sort(rankedSteinerSets);
//
//
//		if (rankedSteinerSets != null && rankedSteinerSets.size() > MAX_STEINER_NODES_SETS )
//			return rankedSteinerSets.subList(0, MAX_STEINER_NODES_SETS);
//		
//		return rankedSteinerSets;
//	}
//	
//	private List<Set<Node>> getSteinerNodeSets(List<Set<MappingStruct>> labelStructSets, int numOfAttributes) {
//
//		if (labelStructSets == null)
//			return null;
//		
//		Set<List<MappingStruct>> labelStructLists = Sets.cartesianProduct(labelStructSets);
//		logger.info("cartesian product of label structs is done, size: " + labelStructLists.size());
//		
//		List<Set<Node>> steinerNodeSets = new ArrayList<Set<Node>>();
//		
//		int numOfTargets;
//		for (List<MappingStruct> labelStructs : labelStructLists) {
////			System.out.println(i++);
//			numOfTargets = 0;
//			Set<Node> steinerNodes = new HashSet<Node>();
////			Set<String> debug = new HashSet<String>();
//			for (MappingStruct ls : labelStructs) {
//				steinerNodes.add(ls.getSource());
//				if (!steinerNodes.contains(ls.getTarget()))
//					numOfTargets ++;
//				steinerNodes.add(ls.getTarget());
////				if (debug.contains(ls.getSource().getId() + ls.getLink().getId()))
////					System.out.println("debug");
////				debug.add(ls.getSource().getId() + ls.getLink().getId());
//			}
//			if (numOfTargets == numOfAttributes)
//				steinerNodeSets.add(steinerNodes);
//		}
//		
//		return steinerNodeSets;
//		
//	}
		
	private DirectedWeightedMultigraph<Node, Link> computeSteinerTree(Set<Node> steinerNodes) {
		
		if (steinerNodes == null || steinerNodes.size() == 0) {
			logger.error("There is no steiner node.");
			return null;
		}
		
//		System.out.println(steinerNodes.size());
		List<Node> steinerNodeList = new ArrayList<Node>(steinerNodes); 
		
//		List<Link> updatedLinks = new ArrayList<Link>();
//		for (Link l : this.patternLinks) { 
//			if (steinerNodes.contains(l.getSource()) && steinerNodes.contains(l.getTarget()))
//				continue;
//			updatedLinks.add(l);
//		}
//		
//		for (Link l : updatedLinks) {
//			this.graphBuilder.changeLinkWeight(l, ModelingParams.PROPERTY_DIRECT_WEIGHT);
//		}

//		GraphUtil.printGraphSimple(this.graphBuilder.getGraph());
		
		long start = System.currentTimeMillis();
		UndirectedGraph<Node, Link> undirectedGraph = new AsUndirectedGraph<Node, Link>(this.graphBuilder.getGraph());

		logger.info("computing steiner tree ...");
		SteinerTree steinerTree = new SteinerTree(undirectedGraph, steinerNodeList);
		DirectedWeightedMultigraph<Node, Link> tree = 
				(DirectedWeightedMultigraph<Node, Link>)GraphUtil.asDirectedGraph(steinerTree.getSteinerTree());
		
		GraphUtil.printGraphSimple(tree);
		
		long steinerTreeElapsedTimeMillis = System.currentTimeMillis() - start;
		logger.info("total number of nodes in steiner tree: " + tree.vertexSet().size());
		logger.info("total number of edges in steiner tree: " + tree.edgeSet().size());
		logger.info("time to compute steiner tree: " + (steinerTreeElapsedTimeMillis/1000F));

//		for (Link l : updatedLinks) {
//			this.graphBuilder.changeLinkWeight(l, ModelingParams.PATTERN_LINK_WEIGHT);
//		}
		
		return tree;
		
//		long finalTreeElapsedTimeMillis = System.currentTimeMillis() - steinerTreeElapsedTimeMillis;
//		DirectedWeightedMultigraph<Node, Link> finalTree = buildOutputTree(tree);
//		logger.info("time to build final tree: " + (finalTreeElapsedTimeMillis/1000F));

//		GraphUtil.printGraph(finalTree);
//		return finalTree; 

	}
		
//	private List<RankedModel> rankModels(List<DirectedWeightedMultigraph<Node, Link>> models) {
//		
//		List<RankedModel> rankedModels = new ArrayList<RankedModel>();
//		if (models == null || models.size() == 0)
//			return rankedModels;
//
//		int count = 1;
//		
//		for (DirectedWeightedMultigraph<Node, Link> m : models) {
//			logger.info("computing raking factors for model " + count + " ...");
//			RankedModel r = new RankedModel(m);
//			rankedModels.add(r);
//			count ++;
//			logger.info("coherence=" + r.getCoherenceString() + ", cost=" + r.getCost());
//		}
//		
////		Collections.sort(rankedModels);
//		return rankedModels;
//	}
	
	public List<RankedModel> hypothesize(List<SemanticLabel> semanticLabels, int numOfAttributes) {

		Set<Node> addedNodes = new HashSet<Node>(); //They should be deleted from the graph after computing the semantic models
		CandidateSteinerSets candidateSteinerSets = getCandidateSteinerSets(semanticLabels, addedNodes);
		
		logger.info("number of steiner sets: " + candidateSteinerSets.numberOfCandidateSets());

//		List<Set<Node>> steinerNodeSets = getSteinerNodeSets(labelStructSets, numOfAttributes);
//		if (steinerNodeSets == null || steinerNodeSets.size() == 0) return null;
//		
//		logger.info("number of possible steiner nodes sets:" + steinerNodeSets.size());
//		
//		
////		for (List<Node> steinerNodes : steinerNodeSets) {
////			System.out.println();
////			System.out.println();
////
////			for (Node n : steinerNodes) {
////				System.out.println(n.getId());
////			}
////			
////			System.out.println();
////			System.out.println();
////		}
//		
//		List<RankedSteinerSet> rankedSteinerSets = rankSteinerSets(steinerNodeSets);
//		
////		for (RankedSteinerSet r : rankedSteinerSets)
////			System.out.println(r.getCohesionString());

		logger.info("updating weights according to training data ...");
		long start = System.currentTimeMillis();
		this.updateWeights();
		long updateWightsElapsedTimeMillis = System.currentTimeMillis() - start;
		logger.info("time to update weights: " + (updateWightsElapsedTimeMillis/1000F));

//		int count = 1;
//		for (RankedSteinerSet r : rankedSteinerSets) {
//			logger.info("computing steiner tree for steiner nodes set " + count + " ...");
//			DirectedWeightedMultigraph<Node, Link> tree = computeSteinerTree(r.getNodes());
//			count ++;
//			if (tree != null) models.add(tree);
//		}

//		List<DirectedWeightedMultigraph<Node, Link>> models = 
//				new ArrayList<DirectedWeightedMultigraph<Node,Link>>();
		
		List<RankedModel> rankedModels = new ArrayList<RankedModel>();
		int count = 1;
		for (SteinerNodes sn : candidateSteinerSets.getSteinerSets()) {
			logger.info("computing steiner tree for steiner nodes set " + count + " ...");
			sn.print();
			DirectedWeightedMultigraph<Node, Link> tree = computeSteinerTree(sn.getNodes());
			count ++;
			if (tree != null) {
				RankedModel r = new RankedModel(tree, sn);
				rankedModels.add(r);
			}
			if (count == MAX_STEINER_NODES_SETS)
				break;
		}
		
		List<RankedModel> uniqueModels = new ArrayList<RankedModel>();
		RankedModel current, previous;
		if (rankedModels != null) {
			Collections.sort(rankedModels);			
			if (rankedModels.size() > 0)
				uniqueModels.add(rankedModels.get(0));
			for (int i = 1; i < rankedModels.size(); i++) {
				current = rankedModels.get(i);
				previous = rankedModels.get(i - 1);
				if (current.getScore() == previous.getScore() && current.getCost() == previous.getCost())
					continue;
				uniqueModels.add(current);
			}
			if (uniqueModels.size() > MAX_CANDIDATES )
				return uniqueModels.subList(0, MAX_CANDIDATES);
		}
		
		return uniqueModels;

	}
	
	private LinkFrequency getMoreFrequentLinkBetweenNodes(Node source, Node target) {

		String sourceUri, targetUri;
		List<String> possibleLinksFromSourceToTarget = new ArrayList<String>();

		sourceUri = source.getLabel().getUri();
		targetUri = target.getLabel().getUri();
		
		HashSet<String> objectPropertiesDirect;
		HashSet<String> objectPropertiesIndirect;
		HashSet<String> objectPropertiesWithOnlyDomain;
		HashSet<String> objectPropertiesWithOnlyRange;
		HashMap<String, Label> objectPropertiesWithoutDomainAndRange = 
				ontologyManager.getObjectPropertiesWithoutDomainAndRange();

		sourceUri = source.getLabel().getUri();
		targetUri = target.getLabel().getUri();

		possibleLinksFromSourceToTarget.clear();

		objectPropertiesDirect = ontologyManager.getObjectPropertiesDirect(sourceUri, targetUri);
		if (objectPropertiesDirect != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesDirect);

		objectPropertiesIndirect = ontologyManager.getObjectPropertiesIndirect(sourceUri, targetUri);
		if (objectPropertiesIndirect != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesIndirect);

		objectPropertiesWithOnlyDomain = ontologyManager.getObjectPropertiesWithOnlyDomain(sourceUri, targetUri);
		if (objectPropertiesWithOnlyDomain != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesWithOnlyDomain);

		objectPropertiesWithOnlyRange = ontologyManager.getObjectPropertiesWithOnlyRange(sourceUri, targetUri);
		if (objectPropertiesWithOnlyRange != null) possibleLinksFromSourceToTarget.addAll(objectPropertiesWithOnlyRange);

		if (ontologyManager.isSubClass(sourceUri, targetUri, true)) 
			possibleLinksFromSourceToTarget.add(Uris.RDFS_SUBCLASS_URI);

		if (objectPropertiesWithoutDomainAndRange != null) {
			possibleLinksFromSourceToTarget.addAll(objectPropertiesWithoutDomainAndRange.keySet());
		}
		
//		Collection<String> userLinks = this.sourceToTargetLinks.get(sourceUri + "---" + targetUri);
//		if (userLinks != null) {
//			for (String s : userLinks)
//				possibleLinksFromSourceToTarget.add(s);
//		}

		String selectedLinkUri1 = null;
		int maxCount1 = 0;

		String selectedLinkUri2 = null;
		int maxCount2 = 0;

		String selectedLinkUri3 = null;
		int maxCount3 = 0;

		String selectedLinkUri4 = null;
		int maxCount4 = 0;

		String key;
		
		if (possibleLinksFromSourceToTarget != null  && possibleLinksFromSourceToTarget.size() > 0) {

			for (String s : possibleLinksFromSourceToTarget) {
				key = sourceUri + "<" + s + ">" + targetUri;
				Integer count1 = this.linkCountMap.get(key);
				if (count1 != null && count1.intValue() > maxCount1) {
					maxCount1 = count1.intValue();
					selectedLinkUri1 = s;
				}
			}
			
			for (String s : possibleLinksFromSourceToTarget) {
				key = s + ">" + targetUri;
				Integer count2 = this.linkCountMap.get(key);
				if (count2 != null && count2.intValue() > maxCount2) {
					maxCount2 = count2.intValue();
					selectedLinkUri2 = s;
				}
			}
			
			for (String s : possibleLinksFromSourceToTarget) {
				key = sourceUri + "<" + s;
				Integer count3 = this.linkCountMap.get(key);
				if (count3 != null && count3.intValue() > maxCount3) {
					maxCount3 = count3.intValue();
					selectedLinkUri3 = s;
				}
			}

			for (String s : possibleLinksFromSourceToTarget) {
				key = s;
				Integer count4 = this.linkCountMap.get(key);
				if (count4 != null && count4.intValue() > maxCount4) {
					maxCount4 = count4.intValue();
					selectedLinkUri4 = s;
				}
			}

		} else {
			logger.error("Something is going wrong. There should be at least one possible object property between " +
					sourceUri + " and " + targetUri);
			return null;
		}
		
		String selectedLinkUri;
		int maxCount;
		int type;

		if (selectedLinkUri1 != null && selectedLinkUri1.trim().length() > 0) {
			selectedLinkUri = selectedLinkUri1;
			maxCount = maxCount1;
			type = 1; // match domain and link and range
		} else if (selectedLinkUri2 != null && selectedLinkUri2.trim().length() > 0) {
			selectedLinkUri = selectedLinkUri2;
			maxCount = maxCount2;
			type = 2; // match link and range
		} else if (selectedLinkUri3 != null && selectedLinkUri3.trim().length() > 0) {
			selectedLinkUri = selectedLinkUri3;
			maxCount = maxCount3;
			type = 3; // match domain and link
		} else if (selectedLinkUri4 != null && selectedLinkUri4.trim().length() > 0) {
			selectedLinkUri = selectedLinkUri4;
			maxCount = maxCount4;
			type = 4; // match link label
		} else {
			if (objectPropertiesDirect != null && objectPropertiesDirect.size() > 0) {
				selectedLinkUri = objectPropertiesDirect.iterator().next();
				type = 5;
			} else 	if (objectPropertiesIndirect != null && objectPropertiesIndirect.size() > 0) {
				selectedLinkUri = objectPropertiesIndirect.iterator().next();
				type = 6;
			} else 	if (objectPropertiesWithOnlyDomain != null && objectPropertiesWithOnlyDomain.size() > 0) {
				selectedLinkUri = objectPropertiesWithOnlyDomain.iterator().next();
				type = 7;
			} else 	if (objectPropertiesWithOnlyRange != null && objectPropertiesWithOnlyRange.size() > 0) {
				selectedLinkUri = objectPropertiesWithOnlyRange.iterator().next();;
				type = 8;
			} else if (ontologyManager.isSubClass(sourceUri, targetUri, true)) {
				selectedLinkUri = Uris.RDFS_SUBCLASS_URI;
				type = 9;
			} else {	// if (objectPropertiesWithoutDomainAndRange != null && objectPropertiesWithoutDomainAndRange.keySet().size() > 0) {
				selectedLinkUri = new ArrayList<String>(objectPropertiesWithoutDomainAndRange.keySet()).get(0);
				type = 10;
			}

			maxCount = 0;
		} 
		
		LinkFrequency lf = new LinkFrequency(selectedLinkUri, type, maxCount);
		
		return lf;
		
	}
	
//	private static double roundTwoDecimals(double d) {
//        DecimalFormat twoDForm = new DecimalFormat("#.##");
//        return Double.valueOf(twoDForm.format(d));
//	}
	
	private static void testApproach() throws Exception {
		
		String inputPath = Params.INPUT_DIR;
		String outputPath = Params.OUTPUT_DIR;
		String graphPath = Params.GRAPHS_DIR;
		
		List<ServiceModel> serviceModels = ModelReader.importServiceModels(inputPath);

		List<ServiceModel> trainingData = new ArrayList<ServiceModel>();
		
		OntologyManager ontManager = new OntologyManager();
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		for (File f : files) {
			ontManager.doImport(f);
		}
		ontManager.updateCache();

//		// experiment 1
//		OntologyManager ontManager = new OntologyManager();
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "dbpedia_3.8.owl"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "foaf.rdf"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "wgs84_pos.xml"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "rdf-schema.rdf"));
//		ontManager.updateCache();

		// experiment 2 - museum data
//		OntologyManager ontManager = new OntologyManager();
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "100_rdf.owl"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "105_Rdf-schema.owl"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "120_dcterms.rdf"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "140_foaf.owl"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "180_rdaGr2.rdf"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "190_ore.owl"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "220_edm_from_xuming.owl"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "230_saam-ont.owl"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "250_skos.owl"));
//		ontManager.doImport(new File(Params.ONTOLOGY_DIR + "260_aac-ont.owl"));
//		ontManager.updateCache();

		for (int i = 0; i < serviceModels.size(); i++) {
//		int i = 2; {
			trainingData.clear();
			int newServiceIndex = i;
			ServiceModel newService = serviceModels.get(newServiceIndex);
			
			logger.info("======================================================");
			logger.info(newService.getServiceDescription());
			logger.info("======================================================");
			
//			int[] trainingModels = {0, 4};
//			for (int n = 0; n < trainingModels.length; n++) { int j = trainingModels[n];
			for (int j = 0; j < serviceModels.size(); j++) {
				if (j != newServiceIndex) 
					trainingData.add(serviceModels.get(j));
			}
			
			Approach1 app = new Approach1(trainingData, ontManager);
			
			String graphName = graphPath + "graph" + String.valueOf(i+1);
			if (new File(graphName).exists()) {
				// read graph from file
				try {
					app.loadGraph(ontManager, graphName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else 
			{
				logger.info("building the graph ...");
				app.buildGraphFromTrainingModels();
				// save graph to file
				try {
					app.saveGraph(graphName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
	
	//		GraphUtil.printGraph(graph);
	
			DirectedWeightedMultigraph<Node, Link> correctModel = newService.getModel();
			// we just get the semantic labels of the correct model
			List<SemanticLabel> newServiceSemanticLabel2s = getModelSemanticLabels(correctModel);
			int numOfattributes = newServiceSemanticLabel2s.size();
			List<RankedModel> hypothesisList = app.hypothesize(newServiceSemanticLabel2s, numOfattributes);
//			if (hypothesis == null)
//				continue;
			
			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
			
			if (hypothesisList != null)
				for (int k = 0; k < hypothesisList.size() && k < 3; k++) {
					
					RankedModel m = hypothesisList.get(k);
					GraphUtil.serialize(m.getModel(), 
							Params.JGRAPHT_DIR + newService.getServiceNameWithPrefix() + ".app1.rank" + (k+1) + ".jgraph");
					
				}
			
			graphs.put("1-correct model", correctModel);
			if (hypothesisList != null)
				for (int k = 0; k < hypothesisList.size(); k++) {
					
					RankedModel m = hypothesisList.get(k);

					double distance = Util.getDistance(correctModel, m.getModel());

//					double distance = new GraphMatching(Util.toGxl(correctModel), 
//							Util.toGxl(m.getModel())).getDistance();
					
					String label = "candidate" + k + 
							"--distance:" + distance +
							"---" + m.getDescription();
					
					graphs.put(label, m.getModel());
				}
			
			GraphVizUtil.exportJGraphToGraphvizFile(graphs, 
					newService.getServiceDescription(), 
					outputPath + serviceModels.get(i).getServiceNameWithPrefix() + ".app1.details.dot");
			
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
