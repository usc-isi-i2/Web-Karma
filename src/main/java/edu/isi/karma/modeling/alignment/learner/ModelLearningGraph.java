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

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Link;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.ObjectPropertyLink;
import edu.isi.karma.rep.alignment.ObjectPropertyType;
import edu.isi.karma.rep.alignment.SubClassLink;
import edu.isi.karma.util.RandomGUID;

public class ModelLearningGraph {

	private static Logger logger = LoggerFactory.getLogger(ModelLearningGraph.class);
	
	private static ModelLearningGraph instance = null;
	private OntologyManager ontologyManager;
	private GraphBuilder graphBuilder;
	private NodeIdFactory nodeIdFactory; 
	private long lastUpdateTime;

	public static synchronized ModelLearningGraph getInstance(OntologyManager ontologyManager) {
		if (instance == null || !ontologyManager.equals(instance.ontologyManager)) {
			try {
				instance = new ModelLearningGraph(ontologyManager);
			} catch (IOException e) {
				logger.error("error in importing the main learning graph!");			
				e.printStackTrace();
				return null;
			}
		}
		return instance;
	}

	public static ModelLearningGraph getEmptyInstance(OntologyManager ontologyManager) {
		instance = new ModelLearningGraph(ontologyManager, true);
		return instance;
	}

	private ModelLearningGraph(OntologyManager ontologyManager, boolean emptyInstance) {
		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = new NodeIdFactory();
		this.graphBuilder = new GraphBuilder(ontologyManager, this.nodeIdFactory, false);
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	private ModelLearningGraph(OntologyManager ontologyManager) throws IOException {
		
		this.ontologyManager = ontologyManager;
		
		File file = new File(ModelingConfiguration.getAlignmentGraphPath());
		if (!file.exists()) {
			this.initializeFromJsonRepository();
		} else {
			DirectedWeightedMultigraph<Node, Link> graph =
					GraphUtil.importJson(ModelingConfiguration.getAlignmentGraphPath());
			this.graphBuilder = new GraphBuilder(ontologyManager, graph);
			this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
		}
		this.lastUpdateTime = System.currentTimeMillis();
	}

	public void empty() {
		this.nodeIdFactory = new NodeIdFactory();
		this.graphBuilder = new GraphBuilder(ontologyManager, this.nodeIdFactory, false);
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
		logger.info("initializing the graph from models in the repository ...");
		
		this.nodeIdFactory = new NodeIdFactory();
		this.graphBuilder = new GraphBuilder(ontologyManager, this.nodeIdFactory, false);

		File ff = new File(ModelingConfiguration.getModelsJsonDir());
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
		this.lastUpdateTime = System.currentTimeMillis();
		logger.info("initialization is done.");
	}
	
	public void exportJson() {
		try {
			GraphUtil.exportJson(this.graphBuilder.getGraph(), ModelingConfiguration.getAlignmentGraphPath());
		} catch (Exception e) {
			logger.error("error in updating the alignment graph json!");
		}
	}
	
	public void addModel(SemanticModel model) {
		this.addModelGraph(model);
		this.graphBuilder.addClosureAndLinksOfNodes(model.getInternalNodes(), null);
	}
	
	public void addModelAndUpdateGraphJson(SemanticModel model) {
		this.addModel(model);
		this.exportJson();
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
		
		visitedNodes = new HashMap<Node, Node>();
	
		for (Link e : model.getGraph().edgeSet()) {

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
				else {
					String id = new RandomGUID().toString();
					ColumnNode node = new ColumnNode(id, id, ((ColumnNode)target).getColumnName(), null);
					if (this.graphBuilder.addNode(node)) {
						n2 = node;
					} else continue;
				}

				visitedNodes.put(target, n2);
			}

			Link link;
			String id = LinkIdFactory.getLinkId(e.getLabel().getUri(), n1.getId(), n2.getId());	
			if (e instanceof DataPropertyLink) 
				link = new DataPropertyLink(id, e.getLabel(), false);
			else if (e instanceof ObjectPropertyLink)
				link = new ObjectPropertyLink(id, e.getLabel(), ((ObjectPropertyLink)e).getObjectPropertyType());
			else if (e instanceof SubClassLink)
				link = new SubClassLink(id);
			else
				link = new ObjectPropertyLink(id, e.getLabel(), ObjectPropertyType.None); 
			
			
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

}
