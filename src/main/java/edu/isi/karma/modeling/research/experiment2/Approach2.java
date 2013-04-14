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

package edu.isi.karma.modeling.research.experiment2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.GraphVizUtil;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.SimpleLink;
import edu.isi.karma.rep.alignment.SubClassLink;
//import com.google.common.base.Function;
//import com.google.common.collect.Multimap;
//import com.google.common.collect.Multimaps;

public class Approach2 {

	private static Logger logger = Logger.getLogger(Approach2.class);

	private static String ontologyDir = "/Users/mohsen/Dropbox/Service Modeling/ontologies/";

	private static String importDir1 = "/Users/mohsen/Dropbox/Service Modeling/experiment2/models/dbpedia/";
	private static String exportDir1 = "/Users/mohsen/Dropbox/Service Modeling/experiment2/results/dbpedia/";

//	private static String importDir2 = "/Users/mohsen/Dropbox/Service Modeling/experiment2/models/schema/";
//	private static String exportDir2 = "/Users/mohsen/Dropbox/Service Modeling/experiment2/results/schema/";
	
	private HashMap<SemanticLabel2, List<LabelStruct>> labelToLabelStructs;
	
	private NodeIdFactory nodeIdFactory;
	private LinkIdFactory linkIdFactory;
	
	private List<ServiceModel2> trainingData;
	private OntologyManager ontologyManager;
	private GraphBuilder graphBuilder;
	
	public Approach2(List<ServiceModel2> trainingData, 
			OntologyManager ontologyManager) {
		this.trainingData = trainingData;
		this.ontologyManager = ontologyManager;
		
		this.linkIdFactory = new LinkIdFactory();
		this.nodeIdFactory = new NodeIdFactory();
		
		this.graphBuilder = new GraphBuilder(ontologyManager, nodeIdFactory, linkIdFactory);

		this.labelToLabelStructs = new HashMap<SemanticLabel2, List<LabelStruct>>();
	}

	public DirectedWeightedMultigraph<Node, Link> getGraph() {
		return this.graphBuilder.getGraph();
	}
	
	public void saveGraph(String fileName) throws IOException {
		this.graphBuilder.serialize(fileName);
	}
	
	public void loadGraph(String fileName) throws IOException, ClassNotFoundException {
		this.graphBuilder = GraphBuilder.deserialize(fileName);
	}
	
	private static List<SemanticLabel2> getModelSemanticLabels(
			DirectedWeightedMultigraph<Node, Link> model) {
		
		List<SemanticLabel2> SemanticLabel2s = new ArrayList<SemanticLabel2>();

		for (Node n : model.vertexSet()) {
			if (!(n instanceof ColumnNode) && !(n instanceof LiteralNode)) continue;
			
			Set<Link> incomingLinks = model.incomingEdgesOf(n);
			if (incomingLinks != null) { // && incomingLinks.size() == 1) {
				Link link = incomingLinks.toArray(new Link[0])[0];
				Node domain = link.getSource();
				
				SemanticLabel2 sl = new SemanticLabel2(domain.getLabel().getUri(), link.getLabel().getUri());
				SemanticLabel2s.add(sl);
			} 
		}
		return SemanticLabel2s;
	}

	private void buildGraphFromTrainingModels() {
		
		String patternId;
		
		// adding the patterns to the graph
		
		for (ServiceModel2 sm : this.trainingData) {
			
			if (sm.getModel() == null) 
				continue;
			
			patternId = sm.getId();
			
			addPatternToGraph(patternId, sm.getModel());
		}

//		GraphUtil.printGraphSimple(alignment.getGraph());

		// adding the links inferred from the ontology
		this.graphBuilder.updateGraph();
		this.updateHashMaps();

	}
	
	private void addPatternToGraph(String patternId, DirectedWeightedMultigraph<Node, Link> pattern) {
		
		//TODO: If pattern already exists in the graph or subsumed by another pattern, 
		// just add new pattern id to the matched links. 
		
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
					if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) n1 = node;
					else continue;
				}
				else {
					ColumnNode node = new ColumnNode(source.getId(), "", "", "");
					if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) n1 = node;
					else continue;
				}

				visitedNodes.put(source, n1);
			}
			
			if (n2 == null) {
				
				if (target instanceof InternalNode) {
					String id = nodeIdFactory.getNodeId(target.getLabel().getUri());
					InternalNode node = new InternalNode(id, new Label(target.getLabel()));
					if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) n1 = node;
					else continue;
				}
				else {
					ColumnNode node = new ColumnNode(target.getId(), "", "", "");
					if (this.graphBuilder.addNodeWithoutUpdatingGraph(node)) n1 = node;
					else continue;
				}

				visitedNodes.put(target, n2);
			}

			Link link;
			String id = linkIdFactory.getLinkId(e.getLabel().getUri());	
			if (n2 instanceof ColumnNode) 
				link = new DataPropertyLink(id, e.getLabel(), false);
			else 
				link = new ObjectPropertyLink(id, e.getLabel());
			
			
			link.getPatternIds().add(patternId);
			
			if (this.graphBuilder.addLink(n1, n2, link))
					this.graphBuilder.changeLinkWeight(link, ModelingParams.PATTERN_LINK_WEIGHT);
		}

	}

	public void addPatternAndUpdateGraph(String patternId, DirectedWeightedMultigraph<Node, Link> pattern) {
		addPatternToGraph(patternId, pattern);
		// adding the links inferred from the ontology
		this.graphBuilder.updateGraph();
	}
	
	private void updateHashMaps() {
		
		List<Node> columnNodes = this.graphBuilder.getTypeToNodesMap().get(NodeType.ColumnNode);
		if (columnNodes != null) {
			for (Node n : columnNodes) {
				Set<Link> incomingLinks = this.graphBuilder.getGraph().incomingEdgesOf(n);
				if (incomingLinks != null && incomingLinks.size() == 1) {
					Link link = incomingLinks.toArray(new Link[0])[0];
					Node domain = link.getSource();
					
					if (!(domain instanceof InternalNode)) continue;
					
					SemanticLabel2 sl = new SemanticLabel2(domain.getLabel().getUri(), link.getLabel().getUri());
					
					List<LabelStruct> labelStructs = this.labelToLabelStructs.get(sl);
					if (labelStructs == null)
						labelStructs = new ArrayList<LabelStruct>();
					labelStructs.add(new LabelStruct((InternalNode)domain, link, (ColumnNode)n));
					
				} else 
					logger.error("The column node " + n.getId() + " does not have any domain or it has more than one domain.");
			}
		}
	}
	
	private List<Node> computeSteinerNodes() {
		
		List<Node> steinerNodes = new ArrayList<Node>();

		
		return steinerNodes;
	}
	
	private List<SemanticLabel2> getSemanticLabelsNotInGraph(List<SemanticLabel2> semanticLabels) {
		
		List<SemanticLabel2> labelsNotInGraph = new ArrayList<SemanticLabel2>();

		for (SemanticLabel2 sl : semanticLabels) {
			
			if (!this.labelToLabelStructs.containsKey(sl)) 
				labelsNotInGraph.add(sl);
		}
		
		return labelsNotInGraph;
	}
		
	private void addSemanticLabelToGraph(DirectedWeightedMultigraph<Node, Link> g, SemanticLabel2 sl) {
		
		List<Node> nodesWithSameUriOfDomain = this.graphBuilder.getUriToNodesMap().get(sl.getNodeUri());
		if (nodesWithSameUriOfDomain != null) {
			for (Node n : nodesWithSameUriOfDomain) {
				
			}
		}
	}
	
	public DirectedWeightedMultigraph<Node, Link> hypothesize(List<SemanticLabel2> sematnticLabels) {
		
		@SuppressWarnings("unchecked")
		DirectedWeightedMultigraph<Node, Link> gPrime = 
				(DirectedWeightedMultigraph<Node, Link>)this.graphBuilder.getGraph().clone();
		
		List<SemanticLabel2> labelsNotInGraph = getSemanticLabelsNotInGraph(sematnticLabels);
		
		for (SemanticLabel2 sl : labelsNotInGraph)
			addSemanticLabelToGraph(gPrime, sl);
		
		///
		List<Node> steinerNodes = this.computeSteinerNodes();
		if (steinerNodes == null || steinerNodes.size() == 0) {
			logger.error("There is no steiner node.");
			return null;
		}

		logger.info("updating weights according to training data ...");
		long start = System.currentTimeMillis();
		long updateWightsElapsedTimeMillis = System.currentTimeMillis() - start;
		logger.info("time to update weights: " + (updateWightsElapsedTimeMillis/1000F));

		UndirectedGraph<Node, Link> undirectedGraph = new AsUndirectedGraph<Node, Link>(this.graphBuilder.getGraph());
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
		
		String inputPath = importDir1;
		String outputPath = exportDir1;
		
		List<ServiceModel2> serviceModels = ModelReader2.importServiceModels(inputPath);

		List<ServiceModel2> trainingData = new ArrayList<ServiceModel2>();
		
		OntologyManager ontManager = new OntologyManager();
		ontManager.doImport(new File(Approach2.ontologyDir + "dbpedia_3.8.owl"));
		ontManager.doImport(new File(Approach2.ontologyDir + "foaf.rdf"));
		ontManager.doImport(new File(Approach2.ontologyDir + "geonames.rdf"));
		ontManager.doImport(new File(Approach2.ontologyDir + "wgs84_pos.xml"));
		ontManager.doImport(new File(Approach2.ontologyDir + "schema.rdf"));
//		ontManager.doImport(new File(Approach1.ontologyDir + "helper.owl"));
		ontManager.updateCache();

//		for (int i = 0; i < serviceModels.size(); i++) {
		int i = 3; {
			
			trainingData.clear();
			int newServiceIndex = i;
			ServiceModel2 newService = serviceModels.get(newServiceIndex);
			
			logger.info("======================================================");
			logger.info(newService.getServiceDescription());
			logger.info("======================================================");
			
			for (int j = 0; j < serviceModels.size(); j++) {
				if (j != newServiceIndex) trainingData.add(serviceModels.get(j));
			}
			
			Approach2 app = new Approach2(trainingData, ontManager);
			
			String graphName = outputPath + "graph" + String.valueOf(i+1);
			if (new File(graphName).exists()) {
				// read graph from file
				try {
					app.loadGraph(graphName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
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
			List<SemanticLabel2> newServiceSemanticLabel2s = getModelSemanticLabels(correctModel);
			DirectedWeightedMultigraph<Node, Link> hypothesis = app.hypothesize(newServiceSemanticLabel2s);
//			if (hypothesis == null)
//				continue;
			
			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
			
			graphs.put("1-correct model", correctModel);
			graphs.put("2-hypothesis", hypothesis);
			
			GraphVizUtil.exportJGraphToGraphvizFile(graphs, 
					newService.getServiceDescription(), 
					outputPath + "output" + String.valueOf(i+1) + ".dot");
			
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
