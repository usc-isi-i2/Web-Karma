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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.python.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphBuilderTopK;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizLabelType;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class ModelLearningGraph {

	private static Logger logger = LoggerFactory.getLogger(ModelLearningGraph.class);
	
	private static ConcurrentHashMap<OntologyManager, ModelLearningGraph> instances = new ConcurrentHashMap<>();
	protected OntologyManager ontologyManager;
	protected GraphBuilder graphBuilder;
	protected NodeIdFactory nodeIdFactory; 
	protected long lastUpdateTime;
	protected int totalNumberOfKnownModels;
	
	private final String getGraphJsonName()
	{
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId());
		return contextParameters.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY) + "graph.json";
	}
	private final String getGraphGraphvizName()
	{
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId());
		return contextParameters.getParameterValue(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY) + "graph.dot";
	}

	public static ModelLearningGraph getInstance(OntologyManager ontologyManager, ModelLearningGraphType type) {
		ModelLearningGraph instance = null;
		ModelLearningGraph previousInstance = null;
		if (!instances.containsKey(ontologyManager)) {
			try {
				if (type == ModelLearningGraphType.Compact)
				{
					instance = new ModelLearningGraphCompact(ontologyManager);
					previousInstance = instances.putIfAbsent(ontologyManager, instance);
					if(previousInstance != null)
						instance = previousInstance;
				}
					
				else
				{
					instance = new ModelLearningGraphSparse(ontologyManager);
					previousInstance = instances.putIfAbsent(ontologyManager, instance);
					if(previousInstance != null)
						instance = previousInstance;
				}
			} catch (IOException e) {
				logger.error("error in importing the main learning graph!", e);
				return null;
			}
		}
		else
		{
			instance = instances.get(ontologyManager);
		}
		return instance;
	}

	public static ModelLearningGraph getEmptyInstance(OntologyManager ontologyManager, ModelLearningGraphType type) {
		ModelLearningGraph instance = null;
//		ModelLearningGraph previousInstance = null;
		if (type == ModelLearningGraphType.Compact)
		{
			instance = new ModelLearningGraphCompact(ontologyManager, true);
//			previousInstance = instances.putIfAbsent(ontologyManager, instance);
//			if(previousInstance != null)
//				instance = previousInstance;
		}
		else
		{
			instance = new ModelLearningGraphSparse(ontologyManager, true);
//			previousInstance = instances.putIfAbsent(ontologyManager, instance);
//			if(previousInstance != null)
//				instance = previousInstance;
		}
		return instance;
	}
	
	protected ModelLearningGraph(OntologyManager ontologyManager, ModelLearningGraphType type) throws IOException {
		
		this.ontologyManager = ontologyManager;
		this.totalNumberOfKnownModels = 0;
		
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
				this.graphBuilder = new GraphBuilder(ontologyManager, graph, false);
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
		if (type == ModelLearningGraphType.Compact)
			this.graphBuilder = new GraphBuilderTopK(ontologyManager, false);
		else
			this.graphBuilder = new GraphBuilder(ontologyManager, false);
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
		this.lastUpdateTime = System.currentTimeMillis();
	}
	
	public GraphBuilder getGraphBuilder() {
		return this.graphBuilder;
	}
	
	public GraphBuilder getGraphBuilderClone() {
		GraphBuilder clonedGraphBuilder = null;
		if (this instanceof ModelLearningGraphSparse) {
			clonedGraphBuilder = new GraphBuilder(this.ontologyManager, this.getGraphBuilder().getGraph(), false);
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
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(ontologyManager.getContextId());
		if (this instanceof ModelLearningGraphSparse)
			this.graphBuilder = new GraphBuilder(ontologyManager, false);
		else 
			this.graphBuilder = new GraphBuilderTopK(ontologyManager, false);
		this.nodeIdFactory = new NodeIdFactory();
		
		Set<InternalNode> addedNodes = new HashSet<>();
		Set<InternalNode> temp;
		File ff = new File(contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR));
		if (ff.exists()) {
			File[] files = ff.listFiles();
			
			for (File f : files) {
				if (f.getName().endsWith(".json")) {
					try {
						SemanticModel model = SemanticModel.readJson(f.getAbsolutePath());
						if (model != null) {
							temp = this.addModel(model, PatternWeightSystem.JWSPaperFormula);
							if (temp != null) addedNodes.addAll(temp);
						}
					} catch (Exception e) {
						logger.error(e.getMessage());
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
			GraphUtil.exportJson(this.graphBuilder.getGraph(), getGraphJsonName(), true, true);
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to json!");
		}
	}
	
	public void exportGraphviz() {
		try {
			GraphVizUtil.exportJGraphToGraphviz(this.graphBuilder.getGraph(), 
					"main graph", 
					true, 
					GraphVizLabelType.LocalId,
					GraphVizLabelType.LocalUri,
					false, 
					true, 
					getGraphGraphvizName());
		} catch (Exception e) {
			logger.error("error in exporting the alignment graph to graphviz!");
		}
	}
	
	public abstract Set<InternalNode> addModel(SemanticModel model, PatternWeightSystem weightSystem);

	public void addModelAndUpdate(SemanticModel model, PatternWeightSystem weightSystem) {
		this.addModel(model, weightSystem);
		this.updateGraphUsingOntology(model);
	}
	
	public void addModelAndUpdateAndExport(SemanticModel model, PatternWeightSystem weightSystem) {
		this.addModel(model, weightSystem);
		this.updateGraphUsingOntology(model);
		this.exportJson();
		this.exportGraphviz();
	}
	
	private void updateGraphUsingOntology(SemanticModel model) {
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontologyManager.getContextId());
		if (modelingConfiguration.getAddOntologyPaths())
			this.graphBuilder.addClosureAndUpdateLinks(model.getInternalNodes(), null);
	}
	
	public void updateGraphUsingOntology(Set<InternalNode> nodes) {
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontologyManager.getContextId());
		if (modelingConfiguration.getAddOntologyPaths())
			this.graphBuilder.addClosureAndUpdateLinks(nodes, null);
	}
	
	public boolean contains(DirectedWeightedMultigraph<Node, LabeledLink> graph) {
		
		if (graph == null || this.graphBuilder == null || this.graphBuilder.getGraph() == null)
			return false;
		
		Set<String> commonModelIds = null;
		LabeledLink matchedLink = null;
		boolean firstVisit = true;
		for (LabeledLink l : graph.edgeSet()) {
			matchedLink = this.graphBuilder.getIdToLinkMap().get(l.getId());
			if (matchedLink == null)
				return false;
			if (firstVisit) {
				commonModelIds = matchedLink.getModelIds();
				firstVisit = false;
			} else {
				if (commonModelIds == null || matchedLink.getModelIds() == null)
					return false;
				commonModelIds = Sets.intersection(matchedLink.getModelIds(), commonModelIds);
				if (commonModelIds.isEmpty()) // no intersection
					return false;
			}
		}
		
		return true;
	}
}
