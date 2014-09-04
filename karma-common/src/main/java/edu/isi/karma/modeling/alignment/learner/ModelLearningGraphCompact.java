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
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ClassInstanceLink;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSubClassLink;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DataPropertyOfColumnLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertySpecializationLink;
import edu.isi.karma.rep.alignment.SubClassLink;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ModelLearningGraphCompact {

	private static Logger logger = LoggerFactory.getLogger(ModelLearningGraphCompact.class);
	
	private static ModelLearningGraphCompact instance = null;
	private OntologyManager ontologyManager;
	private GraphBuilder graphBuilder;
	private NodeIdFactory nodeIdFactory; 
	private long lastUpdateTime;
		
	private static final String getGraphJsonName()
	{
		return ServletContextParameterMap.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY) + "graph.json";
	}
	private static final String getGraphGraphvizName()
	{
		return ServletContextParameterMap.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY) + "graph.dot";
	}

	public static synchronized ModelLearningGraphCompact getInstance(OntologyManager ontologyManager) {
		if (instance == null || !ontologyManager.equals(instance.ontologyManager)) {
			try {
				instance = new ModelLearningGraphCompact(ontologyManager);
			} catch (IOException e) {
				logger.error("error in importing the main learning graph!", e);
				return null;
			}
		}
		return instance;
	}

	public static ModelLearningGraphCompact getEmptyInstance(OntologyManager ontologyManager) {
		instance = new ModelLearningGraphCompact(ontologyManager, true);
		return instance;
	}

	private ModelLearningGraphCompact(OntologyManager ontologyManager, boolean emptyInstance) {
		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = new NodeIdFactory();
		this.graphBuilder = new GraphBuilder(ontologyManager, this.nodeIdFactory, false);
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	private ModelLearningGraphCompact(OntologyManager ontologyManager) throws IOException {
		
		this.ontologyManager = ontologyManager;
		
		File file = new File(getGraphJsonName());
		if (!file.exists()) {
			this.initializeFromJsonRepository();
		} else {
			logger.info("loading the alignment graph ...");
			DirectedWeightedMultigraph<Node, DefaultLink> graph =
					GraphUtil.importJson(getGraphJsonName());
			this.graphBuilder = new GraphBuilder(ontologyManager, graph);
			this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
			logger.info("loading is done!");
		}
		if (this.graphBuilder.getGraph() != null) {
			logger.info("number of nodes: " + this.graphBuilder.getGraph().vertexSet().size());
			logger.info("number of links: " + this.graphBuilder.getGraph().edgeSet().size());
		}
		this.lastUpdateTime = System.currentTimeMillis();
	}

	public GraphBuilder getGraphBuilder() {
		return this.graphBuilder;
	}
	
	public NodeIdFactory getNodeIdFactory() {
		return this.nodeIdFactory;
	}
	
	public long getLastUpdateTime() {
		return this.lastUpdateTime;
	}
	
	public void initializeFromJsonRepository() {
		logger.info("initializing the graph from models in the json repository ...");
		
		this.nodeIdFactory = new NodeIdFactory();
		this.graphBuilder = new GraphBuilder(ontologyManager, this.nodeIdFactory, false);

		File ff = new File(ServletContextParameterMap.getParameterValue(ContextParameter.JSON_MODELS_DIR));
		File[] files = ff.listFiles();
		
		for (File f : files) {
			if (f.getName().endsWith(".json")) {
				try {
					SemanticModel model = SemanticModel.readJson(f.getAbsolutePath());
					if (model != null) this.addModel(model);
				} catch (Exception e) {
				}
			}
		}
		this.exportJson();
		this.exportGraphviz();
		this.lastUpdateTime = System.currentTimeMillis();
		logger.info("initialization is done!");
	}
	
	public void exportJson() {
		try {
			GraphUtil.exportJson(this.graphBuilder.getGraph(), getGraphJsonName());
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to json!");
		}
	}
	
	public void exportGraphviz() {
		try {
			GraphVizUtil.exportJGraphToGraphviz(this.graphBuilder.getGraph(), "main graph", true, false, false, getGraphGraphvizName());
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to graphviz!");
		}
	}
	
	public void addModel(SemanticModel model) {
		this.addModelGraph(model);
		this.graphBuilder.addClosureAndLinksOfNodes(model.getInternalNodes(), null);
	}
	
	public void addModelAndUpdateGraphJson(SemanticModel model) {
		this.addModel(model);
		this.exportJson();
		this.exportGraphviz();
	}
	
	private HashMap<Node,Set<Node>> addInternalNodes(SemanticModel model) {
	
		if (model == null || model.getGraph() == null) 
			return null;
		
		HashMap<Node,Set<Node>> internalNodeMatches = new HashMap<Node,Set<Node>>();

		HashMap<String, Integer> uriCount = new HashMap<String, Integer>();
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				Integer count = uriCount.get(n.getUri());
				if (count == null) uriCount.put(n.getUri(), 1);
				else uriCount.put(n.getUri(), count.intValue() + 1);
			}
		}
		
		for (String uri : uriCount.keySet()) {
			int modelNodeCount = uriCount.get(uri);
			Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(uri);
			int graphNodeCount = matchedNodes == null ? 0 : matchedNodes.size();
			
			for (int i = 0; i < modelNodeCount - graphNodeCount; i++) {
				String id = this.nodeIdFactory.getNodeId(uri);
				Node n = new InternalNode(id, new Label(uri));
				this.graphBuilder.addNode(n);
			}
		}
		
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof InternalNode) {
				Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(n.getUri());
				internalNodeMatches.put(n, matchedNodes);
			}
		}

		return internalNodeMatches;
	}
	
	// FIXME: What if a column node has more than one domain?
	private HashMap<Node,Set<Node>> addColumnNodes(SemanticModel model) {
		
		if (model == null || model.getGraph() == null) 
			return null;

		HashMap<Node,Set<Node>> columnNodeMatches = new HashMap<Node,Set<Node>>();

		HashMap<Node,Node> nodeToDomain = new HashMap<Node,Node>();
		HashMap<Node,LabeledLink> nodeToDataProperty = new HashMap<Node,LabeledLink>();
		HashMap<String, Integer> dataPropertyCount = new HashMap<String, Integer>(); // key = domainUri + propertyUri
		for (Node n : model.getGraph().vertexSet()) {
			if (n instanceof ColumnNode || n instanceof LiteralNode) {
				Set<LabeledLink> incomingLinks = model.getGraph().incomingEdgesOf(n);
				if (incomingLinks == null || incomingLinks.isEmpty())
					continue;
				if (incomingLinks != null && incomingLinks.size() == 1) {
					LabeledLink[] incomingLinksArray = incomingLinks.toArray(new LabeledLink[0]);
					Node domain = incomingLinksArray[0].getSource();
					String linkUri =  incomingLinksArray[0].getUri(); 
					String key =  domain.getId() + linkUri;
					Integer count = dataPropertyCount.get(key);
					if (count == null) dataPropertyCount.put(key, 1);
					else dataPropertyCount.put(key, count.intValue() + 1);
					nodeToDomain.put(n, domain);
					nodeToDataProperty.put(n, incomingLinksArray[0]);
				} else {
					logger.debug("The column node " + ((ColumnNode)n).getColumnName() + " does not have any domain or it has more than one domain.");
					continue;
				}
			}
		}
		
		for (Node n : model.getGraph().vertexSet()) {
			Set<Node> matches = new HashSet<Node>();
			if (n instanceof ColumnNode) {
				Node domain = nodeToDomain.get(n);
				LabeledLink incomingLink = nodeToDataProperty.get(n);
				Set<Node> matchedNodes = this.graphBuilder.getUriToNodesMap().get(domain.getUri());
				if (matchedNodes == null || matchedNodes.isEmpty()) {
					logger.error("no match found for the node " + domain.getUri() + "in the graph");
					return null;
				}
				for (Node m : matchedNodes) {
					String graphKey = m.getId() + incomingLink.getUri(); 
					Set<SemanticTypeMapping> mappings = this.graphBuilder.getSemanticTypeMatches().get(graphKey);
					int graphDataPropertyCount = mappings == null ? 0 : mappings.size();
					if (mappings != null) {
						for (SemanticTypeMapping sm : mappings) {
							matches.add(sm.getTarget());
						}
					}
					String modelKey = domain.getId() + incomingLink.getUri(); 
					int modelDataPropertyCount = dataPropertyCount.get(modelKey);
					for (int i = 0; i < modelDataPropertyCount - graphDataPropertyCount; i++) {
						Node newNode = null;
						if (n instanceof ColumnNode) {
							ColumnNode c = (ColumnNode)n;
							newNode = new ColumnNode(new RandomGUID().toString(), c.getHNodeId(), c.getColumnName(), c.getRdfLiteralType());
						}
						if (newNode == null) {
							return null;
						}
						if (this.graphBuilder.addNode(newNode))
							matches.add(newNode);
					}
					columnNodeMatches.put(n, matches);
				}
			}
		}

		return columnNodeMatches;
	}
	
	private List<HashMap<Node,Node>> findMappings(SemanticModel model, 
			HashMap<Node, Set<Node>> internalNodeMatches,
			HashMap<Node, Set<Node>> columnNodeMatches) {
		
		if (model == null || model.getGraph() == null) 
			return null;
		
		List<HashMap<Node,Node>> mappings = new LinkedList<HashMap<Node,Node>>();
				
		int countOfNodes = 0;
		for (Node node : model.getGraph().vertexSet()) {
			countOfNodes ++;
			List<HashMap<Node,Node>> newMappings = new LinkedList<HashMap<Node,Node>>();
			Set<Node> matchedNodes = null;
			if (node instanceof InternalNode) 
				matchedNodes = internalNodeMatches.get(node);
			else if (node instanceof ColumnNode)
				matchedNodes = columnNodeMatches.get(node);
			if (matchedNodes == null) {
				return null;
			}

			if (matchedNodes == null || matchedNodes.isEmpty()) {
				logger.error("no match found for the node " + node.getId() + "in the graph");
				return null;
			}
			
			if (mappings.size() == 0) {
				for (Node n : matchedNodes) {
					HashMap<Node,Node> nodeMap = new HashMap<Node,Node>();
					nodeMap.put(node, n);
					mappings.add(nodeMap);
				}
			} else {
				for (HashMap<Node,Node> nodeMap : mappings) {
					for (Node n : matchedNodes) {
						HashMap<Node,Node> newMapping = new HashMap<Node,Node>(nodeMap);
						newMapping.put(node, n);
						if (newMapping.size() == countOfNodes) // prevent mapping two model nodes to same graph node
							newMappings.add(newMapping);
					}
				}
				mappings = newMappings;					
			}
		}
		
		return mappings;
	}
	
	private String generateLinkModelId(String originalModelId, int index) {
		String separator = "/";
		String modelId = originalModelId + separator + index;
		return modelId;
	}
	
	private void addModelGraph(SemanticModel model) {
		
		HashMap<Node, Node> visitedNodes;
		Node source, target;
		Node n1, n2;
		
		// adding the patterns to the graph
		
		if (model == null) 
			return;
		
		String modelId = model.getId();
		if (this.graphBuilder.getModelIds().contains(modelId)) {
			// FIXME
			// we need to somehow update the graph, but I don't know how to do that yet.
			// so, we rebuild the whole graph from scratch.
			logger.info("the graph already includes the model and needs to be updated, we re-initialize the graph from the repository!");
			initializeFromJsonRepository();
			return;
		}
		
		// add the model  nodes that are not in the graph
		HashMap<Node, Set<Node>> internalNodeMatches = addInternalNodes(model);
		HashMap<Node, Set<Node>> columnNodeMatches = addColumnNodes(model);
		
		// find possible mappings between models nodes and the graph nodes
		List<HashMap<Node,Node>> mappings = findMappings(model, internalNodeMatches, columnNodeMatches);
		
		if (mappings == null) {
			return;
		}
		
		int index = 1;
		for (HashMap<Node,Node> mapping : mappings) {
			String indexedModelId = generateLinkModelId(modelId, index++);
			for (LabeledLink e : model.getGraph().edgeSet()) {
				
				source = e.getSource();
				target = e.getTarget();
				
				n1 = mapping.get(source);
				if (n1 == null) {
					logger.error("the mappings does not include the source node " + source.getId());
					return;
				}
				
				n2 = mapping.get(target);
				if (n2 == null) {
					logger.error("the mappings does not include the target node " + target.getId());
					return;
				}
				String id = LinkIdFactory.getLinkId(e.getUri(), n1.getId(), n2.getId());
				LabeledLink l = this.graphBuilder.getIdToLinkMap().get(id);
				if (l != null) {
					l.getModelIds().add(indexedModelId);
				} else {
					LabeledLink link;
					if (e instanceof DataPropertyLink) 
						link = new DataPropertyLink(id, e.getLabel(), e.getKeyType() == LinkKeyInfo.PartOfKey? true : false);
					else if (e instanceof ObjectPropertyLink)
						link = new ObjectPropertyLink(id, e.getLabel(), ((ObjectPropertyLink)e).getObjectPropertyType());
					else if (e instanceof SubClassLink)
						link = new SubClassLink(id);
					else if (e instanceof ClassInstanceLink)
						link = new ClassInstanceLink(id, e.getKeyType());
					else if (e instanceof ColumnSubClassLink)
						link = new ColumnSubClassLink(id);
					else if (e instanceof DataPropertyOfColumnLink)
						link = new DataPropertyOfColumnLink(id, ((DataPropertyOfColumnLink)e).getSpecializedColumnHNodeId());
					else if (e instanceof ObjectPropertySpecializationLink)
						link = new ObjectPropertySpecializationLink(id, ((ObjectPropertySpecializationLink)e).getSpecializedLinkId());
					else {
			    		logger.error("cannot instanciate a link from the type: " + e.getType().toString());
			    		continue;
					}
					link.getModelIds().add(indexedModelId);
					if (this.graphBuilder.addLink(n1, n2, link)) {
						this.graphBuilder.changeLinkWeight(link, ModelingParams.PATTERN_LINK_WEIGHT);
					}
					n1.getModelIds().add(indexedModelId);
					n2.getModelIds().add(indexedModelId);
					
				}
			}
		}
		
		visitedNodes = new HashMap<Node, Node>();
	
		for (LabeledLink e : model.getGraph().edgeSet()) {

			source = e.getSource();
			target = e.getTarget();

			n1 = visitedNodes.get(source);
			n2 = visitedNodes.get(target);
			
			if (n1 == null) {
				
				if (source instanceof InternalNode) {
					String id = this.nodeIdFactory.getNodeId(source.getLabel().getUri());
					InternalNode node = new InternalNode(id, new Label(source.getLabel()));
					if (this.graphBuilder.addNode(node)) {
						n1 = node;
					} else continue;
				}
				else {
					String id = new RandomGUID().toString();
					ColumnNode node = new ColumnNode(id, id, ((ColumnNode)target).getColumnName(), null);
					if (this.graphBuilder.addNode(node)) {
						n1 = node;
					} else continue;
				}

				visitedNodes.put(source, n1);
			}
			
			if (n2 == null) {
				
				if (target instanceof InternalNode) {
					String id = nodeIdFactory.getNodeId(target.getLabel().getUri());
					InternalNode node = new InternalNode(id, new Label(target.getLabel()));
					if (this.graphBuilder.addNode(node)) {
						n2 = node;
					} else continue;
				}
				else if(target instanceof LiteralNode) {
					LiteralNode lTarget = (LiteralNode)target;
					String id = nodeIdFactory.getNodeId(lTarget.getValue());
					LiteralNode node = new LiteralNode(id, lTarget.getValue(), new Label(target.getLabel()), lTarget.isUri());
					if (this.graphBuilder.addNode(node)) {
						n2 = node;
					} else continue;
				}
				else {
					String id = new RandomGUID().toString();
					ColumnNode node = new ColumnNode(id, id, ((ColumnNode)target).getColumnName(), null);
					if (this.graphBuilder.addNode(node)) {
						n2 = node;
					} else continue;
				}

				visitedNodes.put(target, n2);
			}
			
			LabeledLink link;
			String id = LinkIdFactory.getLinkId(e.getLabel().getUri(), n1.getId(), n2.getId());	
			if (e instanceof DataPropertyLink) 
				link = new DataPropertyLink(id, e.getLabel(), e.getKeyType() == LinkKeyInfo.PartOfKey? true : false);
			else if (e instanceof ObjectPropertyLink)
				link = new ObjectPropertyLink(id, e.getLabel(), ((ObjectPropertyLink)e).getObjectPropertyType());
			else if (e instanceof SubClassLink)
				link = new SubClassLink(id);
			else if (e instanceof ClassInstanceLink)
				link = new ClassInstanceLink(id, e.getKeyType());
			else if (e instanceof ColumnSubClassLink)
				link = new ColumnSubClassLink(id);
			else if (e instanceof DataPropertyOfColumnLink)
				link = new DataPropertyOfColumnLink(id, ((DataPropertyOfColumnLink)e).getSpecializedColumnHNodeId());
			else if (e instanceof ObjectPropertySpecializationLink)
				link = new ObjectPropertySpecializationLink(id, ((ObjectPropertySpecializationLink)e).getSpecializedLinkId());
			else {
	    		logger.error("cannot instanciate a link from the type: " + e.getType().toString());
	    		continue;
			}
			
			
			link.getModelIds().add(modelId);
			
			if (this.graphBuilder.addLink(n1, n2, link)) {
				this.graphBuilder.changeLinkWeight(link, ModelingParams.PATTERN_LINK_WEIGHT);
			}
			
			if (!n1.getModelIds().contains(modelId))
				n1.getModelIds().add(modelId);
			
			if (!n2.getModelIds().contains(modelId))
				n2.getModelIds().add(modelId);

		}
		
		this.lastUpdateTime = System.currentTimeMillis();
	}

	public static void main(String[] args) {

		/** Check if any ontology needs to be preloaded **/
		String preloadedOntDir = "/Users/mohsen/Documents/Academic/ISI/_GIT/Web-Karma/preloaded-ontologies/";
		File ontDir = new File(preloadedOntDir);
		if (ontDir.exists()) {
			File[] ontologies = ontDir.listFiles();
			OntologyManager mgr = new OntologyManager();
			for (File ontology: ontologies) {
				if (ontology.getName().endsWith(".owl") || ontology.getName().endsWith(".rdf")) {
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try {
						String encoding = EncodingDetector.detect(ontology);
						mgr.doImport(ontology, encoding);
					} catch (Exception t) {
						logger.error ("Error loading ontology: " + ontology.getAbsolutePath(), t);
					}
				}
			}
			// update the cache at the end when all files are added to the model
			mgr.updateCache();
			ModelLearningGraphCompact.getInstance(mgr);
			
		} else {
			logger.info("No directory for preloading ontologies exists.");
		}


		
	}
}
