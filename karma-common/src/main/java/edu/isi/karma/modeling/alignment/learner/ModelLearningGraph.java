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
import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphBuilderTopK;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class ModelLearningGraph {

	private static Logger logger = LoggerFactory.getLogger(ModelLearningGraph.class);
	
	private static ModelLearningGraph instance = null;
	protected OntologyManager ontologyManager;
	protected GraphBuilder graphBuilder;
	protected NodeIdFactory nodeIdFactory; 
	protected long lastUpdateTime;
	
	private static final String getGraphJsonName()
	{
		return ServletContextParameterMap.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY) + "graph.json";
	}
	private static final String getGraphGraphvizName()
	{
		return ServletContextParameterMap.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY) + "graph.dot";
	}

	public static synchronized ModelLearningGraph getInstance(OntologyManager ontologyManager, ModelLearningGraphType type) {
		if (instance == null || !ontologyManager.equals(instance.ontologyManager)) {
			try {
				if (type == ModelLearningGraphType.Compact)
					instance = new ModelLearningGraphCompact(ontologyManager);
				else
					instance = new ModelLearningGraphSparse(ontologyManager);
			} catch (IOException e) {
				logger.error("error in importing the main learning graph!", e);
				return null;
			}
		}
		return instance;
	}

	public static ModelLearningGraph getEmptyInstance(OntologyManager ontologyManager, ModelLearningGraphType type) {
		if (type == ModelLearningGraphType.Compact)
			instance = new ModelLearningGraphCompact(ontologyManager, true);
		else
			instance = new ModelLearningGraphSparse(ontologyManager, true);
		return instance;
	}
	
	protected ModelLearningGraph(OntologyManager ontologyManager, ModelLearningGraphType type) throws IOException {
		
		this.ontologyManager = ontologyManager;
		
		File file = new File(getGraphJsonName());
		if (!file.exists()) {
			this.initializeFromJsonRepository();
		} else {
			logger.info("loading the alignment graph ...");
			DirectedWeightedMultigraph<Node, DefaultLink> graph =
					GraphUtil.importJson(getGraphJsonName());
			if (type == ModelLearningGraphType.Compact)
				this.graphBuilder = new GraphBuilderTopK(ontologyManager, graph);
			else
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
	
	protected ModelLearningGraph(OntologyManager ontologyManager, boolean emptyInstance, ModelLearningGraphType type) {
		this.ontologyManager = ontologyManager;
		this.nodeIdFactory = new NodeIdFactory();
		if (type == ModelLearningGraphType.Compact)
			this.graphBuilder = new GraphBuilderTopK(ontologyManager, this.nodeIdFactory, false);
		else
			this.graphBuilder = new GraphBuilder(ontologyManager, this.nodeIdFactory, false);
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	public GraphBuilder getGraphBuilder() {
		return this.graphBuilder;
	}
	
	public GraphBuilder getGraphBuilderClone() {
		GraphBuilder clonedGraphBuilder = null;
		if (this instanceof ModelLearningGraphSparse) {
			clonedGraphBuilder = new GraphBuilder(this.ontologyManager, this.getGraphBuilder().getGraph());
		} else if (this instanceof ModelLearningGraphCompact) {
			clonedGraphBuilder = new GraphBuilderTopK(this.ontologyManager, this.getGraphBuilder().getGraph());
		}
		return clonedGraphBuilder;
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
		if (ff.exists()) {
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
	
	public abstract Set<InternalNode> addModel(SemanticModel model);
	
	public void addModelAndUpdate(SemanticModel model) {
		this.addModel(model);
		this.updateGraphUsingOntology(model);
	}
	
	public void addModelAndUpdateAndExport(SemanticModel model) {
		this.addModel(model);
		this.updateGraphUsingOntology(model);
		this.exportJson();
		this.exportGraphviz();
	}
	
	private void updateGraphUsingOntology(SemanticModel model) {
		this.graphBuilder.addClosureAndLinksOfNodes(model.getInternalNodes(), null);
	}
	
	public void updateGraphUsingOntology(Set<InternalNode> nodes) {
		this.graphBuilder.addClosureAndLinksOfNodes(nodes, null);
	}
	
}
