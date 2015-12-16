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

package edu.isi.karma.research.modeling;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import edu.isi.karma.modeling.alignment.GraphVizLabelType;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.alignment.ModelEvaluation;
import edu.isi.karma.modeling.alignment.NodeIdFactory;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.SteinerTree;
import edu.isi.karma.modeling.alignment.TreePostProcess;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraph;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphCompact;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphType;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.alignment.learner.PatternWeightSystem;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.ColumnSemanticTypeStatus;
import edu.isi.karma.rep.alignment.DataPropertyLink;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.alignment.SemanticType;
import edu.isi.karma.rep.alignment.SemanticType.Origin;
import edu.isi.karma.util.RandomGUID;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class ModelLearner_KnownModels2 {

	private static Logger logger = LoggerFactory.getLogger(ModelLearner_KnownModels2.class);
	private OntologyManager ontologyManager = null;
	private GraphBuilder graphBuilder = null;
	private NodeIdFactory nodeIdFactory = null; 
	private List<Node> steinerNodes = null;
	
	public ModelLearner_KnownModels2(OntologyManager ontologyManager, 
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
		this.graphBuilder = cloneGraphBuilder(gb); // create a copy of the graph builder
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
	}

	public ModelLearner_KnownModels2(GraphBuilder graphBuilder, 
			List<Node> steinerNodes) {
		if (graphBuilder == null || 
				steinerNodes == null || 
				steinerNodes.isEmpty()) {
			logger.error("cannot instanciate model learner!");
			return;
		}
		this.ontologyManager = graphBuilder.getOntologyManager();
		this.steinerNodes = steinerNodes;
		this.graphBuilder = cloneGraphBuilder(graphBuilder); // create a copy of the graph builder
		this.nodeIdFactory = this.graphBuilder.getNodeIdFactory();
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

	public List<SortableSemanticModel> hypothesize(boolean useCorrectTypes, int numberOfCandidates) throws Exception {

		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(ontologyManager.getContextId());
		List<SortableSemanticModel> sortableSemanticModels = new ArrayList<SortableSemanticModel>();

		Map<ColumnNode, ColumnNode> mappingToSourceColumns = new HashMap<ColumnNode, ColumnNode>();
		List<ColumnNode> columnNodes = new LinkedList<ColumnNode>();
		for (Node n : steinerNodes)
			if (n instanceof ColumnNode) {
				ColumnNode c = (ColumnNode)n;
				columnNodes.add(c);
				mappingToSourceColumns.put(c, c);
			}
		
		for (Node n : steinerNodes) {
			if (n instanceof ColumnNode) {
				ColumnNode steinerNode = (ColumnNode)n;
				List<SemanticType> candidateSemanticTypes = getCandidateSteinerSets(steinerNode, useCorrectTypes, numberOfCandidates);
				addSteinerNodeToTheGraph(steinerNode, candidateSemanticTypes);
			}
		}

		logger.info("graph nodes: " + this.graphBuilder.getGraph().vertexSet().size());
		logger.info("graph links: " + this.graphBuilder.getGraph().edgeSet().size());


		logger.info("computing steiner trees ...");
			
		Set<Node> sn = new HashSet<Node>(steinerNodes);
		List<DirectedWeightedMultigraph<Node, LabeledLink>> topKSteinerTrees;
		if (this.graphBuilder instanceof GraphBuilderTopK) {
			topKSteinerTrees =  ((GraphBuilderTopK)this.graphBuilder).getTopKSteinerTrees(sn, 
					modelingConfiguration.getTopKSteinerTree(), 
					null, null, false);
		} 
		else 
		{
			topKSteinerTrees = new LinkedList<DirectedWeightedMultigraph<Node, LabeledLink>>();
			SteinerTree steinerTree = new SteinerTree(
					new AsUndirectedGraph<Node, DefaultLink>(this.graphBuilder.getGraph()), Lists.newLinkedList(sn));
			WeightedMultigraph<Node, DefaultLink> t = steinerTree.getDefaultSteinerTree();
			TreePostProcess treePostProcess = new TreePostProcess(this.graphBuilder, t);
			if (treePostProcess.getTree() != null)
				topKSteinerTrees.add(treePostProcess.getTree());
		}
			
//		System.out.println(GraphUtil.labeledGraphToString(treePostProcess.getTree()));
		
//		logger.info("END ...");

		for (DirectedWeightedMultigraph<Node, LabeledLink> tree: topKSteinerTrees) {
			if (tree != null) {
//					System.out.println();
				SemanticModel sm = new SemanticModel(new RandomGUID().toString(), 
						tree,
						columnNodes,
						mappingToSourceColumns
						);
				SortableSemanticModel sortableSemanticModel = 
						new SortableSemanticModel(sm, false);
				sortableSemanticModels.add(sortableSemanticModel);
				
//					System.out.println(GraphUtil.labeledGraphToString(sm.getGraph()));
//					System.out.println(sortableSemanticModel.getLinkCoherence().printCoherenceList());
			}
		}
		
		Collections.sort(sortableSemanticModels);
		int count = Math.min(sortableSemanticModels.size(), modelingConfiguration.getNumCandidateMappings());
		logger.info("results are ready ...");
//		sortableSemanticModels.get(0).print();
		return sortableSemanticModels.subList(0, count);

	}
	
	private List<SemanticType> getCandidateSteinerSets(ColumnNode steinerNode, boolean useCorrectTypes, int numberOfCandidates) {

		if (steinerNode == null)
			return null;

		List<SemanticType> candidateSemanticTypes = null;

		if (!useCorrectTypes) {
			candidateSemanticTypes = steinerNode.getTopKLearnedSemanticTypes(numberOfCandidates);
		} else if (steinerNode.getSemanticTypeStatus() == ColumnSemanticTypeStatus.UserAssigned) {
			candidateSemanticTypes = steinerNode.getUserSemanticTypes();
		}

		if (candidateSemanticTypes == null) {
			logger.error("No candidate semantic type found for the column " + steinerNode.getColumnName());
			return null;
		}
		
		return candidateSemanticTypes;
	}


	private void addSteinerNodeToTheGraph(ColumnNode steinerNode, List<SemanticType> semanticTypes) {
		
		if (!this.graphBuilder.addNode(steinerNode)) return ;

		if (semanticTypes == null) {
			logger.error("semantic type is null.");
			return;
		}
		
		for (SemanticType semanticType : semanticTypes) {
			
			if (semanticType == null) {
				logger.error("semantic type is null.");
				continue;

			}
			if (semanticType.getDomain() == null) {
				logger.error("semantic type does not have any domain");
				continue;
			}

			if (semanticType.getType() == null) {
				logger.error("semantic type does not have any link");
				continue;
			}

			String domainUri = semanticType.getDomain().getUri();
			String propertyUri = semanticType.getType().getUri();
			Double confidence = semanticType.getConfidenceScore();
			Origin origin = semanticType.getOrigin();
			
			logger.debug("semantic type: " + domainUri + "|" + propertyUri + "|" + confidence + "|" + origin);

			Set<Node> nodesWithSameUriOfDomain = this.graphBuilder.getUriToNodesMap().get(domainUri);
			if (nodesWithSameUriOfDomain == null || nodesWithSameUriOfDomain.isEmpty()) {
				String nodeId = nodeIdFactory.getNodeId(domainUri);
				Node source = new InternalNode(nodeId, new Label(domainUri));
				if (!this.graphBuilder.addNodeAndUpdate(source, null)) continue;
				String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), steinerNode.getId());	
				LabeledLink link = new DataPropertyLink(linkId, new Label(propertyUri));
				if (!this.graphBuilder.addLink(source, steinerNode, link)) continue;;
			} else {
				for (Node source : nodesWithSameUriOfDomain) {
					String linkId = LinkIdFactory.getLinkId(propertyUri, source.getId(), steinerNode.getId());	
					LabeledLink link = new DataPropertyLink(linkId, new Label(propertyUri));
					if (!this.graphBuilder.addLink(source, steinerNode, link)) continue;;
				}
			}

		}
	}

	private static double roundDecimals(double d, int k) {
		String format = "";
		for (int i = 0; i < k; i++) format += "#";
        DecimalFormat DForm = new DecimalFormat("#." + format);
        return Double.valueOf(DForm.format(d));
	}

	@SuppressWarnings("unused")
	private static void getStatistics1(List<SemanticModel> semanticModels) {
		for (int i = 0; i < semanticModels.size(); i++) {
			SemanticModel source = semanticModels.get(i);
			int attributeCount = source.getColumnNodes().size();
			int nodeCount = source.getGraph().vertexSet().size();
			int linkCount = source.getGraph().edgeSet().size();
			int datanodeCount = 0;
			int classNodeCount = 0;
			for (Node n : source.getGraph().vertexSet()) {
				if (n instanceof InternalNode) classNodeCount++;
				if (n instanceof ColumnNode) datanodeCount++;
			}
			System.out.println(attributeCount + "\t" + nodeCount + "\t" + linkCount + "\t" + classNodeCount + "\t" + datanodeCount);
			
			List<ColumnNode> columnNodes = source.getColumnNodes();
			getStatistics2(columnNodes);

		}
	}
	
	private static void getStatistics2(List<ColumnNode> columnNodes) {

		if (columnNodes == null)
			return;

		int numberOfAttributesWhoseTypeIsFirstCRFType = 0;
		int numberOfAttributesWhoseTypeIsInCRFTypes = 0;
		for (ColumnNode cn : columnNodes) {
			List<SemanticType> userSemanticTypes = cn.getUserSemanticTypes();
			List<SemanticType> top4Suggestions = cn.getTopKLearnedSemanticTypes(4);

			for (int i = 0; i < top4Suggestions.size(); i++) {
				SemanticType st = top4Suggestions.get(i);
				if (userSemanticTypes != null) {
					for (SemanticType t : userSemanticTypes) {
						if (st.getModelLabelString().equalsIgnoreCase(t.getModelLabelString())) {
							if (i == 0) numberOfAttributesWhoseTypeIsFirstCRFType ++;
							numberOfAttributesWhoseTypeIsInCRFTypes ++;
							i = top4Suggestions.size();
							break;
						}
					}
				} 
			}

		}

		System.out.println(numberOfAttributesWhoseTypeIsInCRFTypes + "\t" + numberOfAttributesWhoseTypeIsFirstCRFType);
//		System.out.println(columnNodes.size() + "\t" + numberOfAttributesWhoseTypeIsInCRFTypes + "\t" + numberOfAttributesWhoseTypeIsFirstCRFType);

//		System.out.println("totalNumberOfAttributes: " + columnNodes.size());
//		System.out.println("numberOfAttributesWhoseTypeIsInCRFTypes: " + numberOfAttributesWhoseTypeIsInCRFTypes);
//		System.out.println("numberOfAttributesWhoseTypeIsFirstCRFType:" + numberOfAttributesWhoseTypeIsFirstCRFType);
	}

	public static void test() throws Exception {

		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();

		//		String inputPath = Params.INPUT_DIR;
		String graphPath = Params.GRAPHS_DIR;

		//		List<SemanticModel> semanticModels = ModelReader.importSemanticModels(inputPath);
		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);

		//		ModelEvaluation me2 = semanticModels.get(20).evaluate(semanticModels.get(20));
		//		System.out.println(me2.getPrecision() + "--" + me2.getRecall());
		//		if (true)
		//			return;

		List<SemanticModel> trainingData = new ArrayList<SemanticModel>();

		OntologyManager ontologyManager = new OntologyManager(contextParameters.getId());
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		for (File f : files) {
			ontologyManager.doImport(f, "UTF-8");
		}
		ontologyManager.updateCache();  

//		getStatistics1(semanticModels);
//
//		if (true)
//			return;

		ModelLearningGraph modelLearningGraph = null;
		
		ModelLearner_KnownModels2 modelLearner;

		boolean iterativeEvaluation = false;
		boolean useCorrectType = true;
		boolean randomModel = false;

		int numberOfCandidates = 1;
		int numberOfKnownModels;
		String filePath = Params.RESULTS_DIR + "temp/";
		String filename = ""; 
		filename += "results";
		filename += useCorrectType ? "-correct types":"-k=" + numberOfCandidates;
		filename += randomModel ? "-random":"";
		filename += iterativeEvaluation ? "-iterative":"";
		filename += ".csv"; 
		
		PrintWriter resultFileIterative = null;
		PrintWriter resultFile = null;
		StringBuffer[] resultsArray = null;
		
		if (iterativeEvaluation) {
			resultFileIterative = new PrintWriter(new File(filePath + filename));
			resultsArray = new StringBuffer[semanticModels.size() + 2];
			for (int i = 0; i < resultsArray.length; i++) {
				resultsArray[i] = new StringBuffer();
			}
		} else {
			resultFile = new PrintWriter(new File(filePath + filename));
			resultFile.println("source \t p \t r \t t \n");
		}


		for (int i = 0; i < semanticModels.size(); i++) {
//		for (int i = 0; i <= 10; i++) {
//		int i = 3; {

			int newSourceIndex = i;
			SemanticModel newSource = semanticModels.get(newSourceIndex);

			logger.info("======================================================");
			logger.info(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			System.out.println(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			logger.info("======================================================");

			numberOfKnownModels = iterativeEvaluation ? 0 : semanticModels.size() - 1;

			if (iterativeEvaluation) {
				if (resultsArray[0].length() > 0)	resultsArray[0].append(" \t ");			
				resultsArray[0].append(newSource.getName() + "(" + newSource.getColumnNodes().size() + ")" + "\t" + " " + "\t" + " ");
				if (resultsArray[1].length() > 0)	resultsArray[1].append(" \t ");			
				resultsArray[1].append("p \t r \t t");
			}

//			numberOfKnownModels = 2;
			while (numberOfKnownModels <= semanticModels.size() - 1) 
			{

				trainingData.clear();

				int j = 0, count = 0;
				while (count < numberOfKnownModels) {
					if (j != newSourceIndex) {
						trainingData.add(semanticModels.get(j));
						count++;
					}
					j++;
				}

				modelLearningGraph = (ModelLearningGraphCompact)ModelLearningGraph.getEmptyInstance(ontologyManager, ModelLearningGraphType.Compact);
				
				
				SemanticModel correctModel = newSource;
				List<ColumnNode> columnNodes = correctModel.getColumnNodes();
				//				if (useCorrectType && numberOfCRFCandidates > 1)
				//					updateCrfSemanticTypesForResearchEvaluation(columnNodes);

				List<Node> steinerNodes = new LinkedList<Node>(columnNodes);
				modelLearner = new ModelLearner_KnownModels2(ontologyManager, steinerNodes);
				long start = System.currentTimeMillis();

				String graphName = !iterativeEvaluation?
						graphPath + semanticModels.get(newSourceIndex).getName() + Params.GRAPH_JSON_FILE_EXT : 
							graphPath + semanticModels.get(newSourceIndex).getName() + ".knownModels=" + numberOfKnownModels + Params.GRAPH_JSON_FILE_EXT;

				if (randomModel) {
					modelLearner = new ModelLearner_KnownModels2(new GraphBuilder(ontologyManager, false), steinerNodes);
				} else if (new File(graphName).exists()) {
					// read graph from file
					try {
						logger.info("loading the graph ...");
						DirectedWeightedMultigraph<Node, DefaultLink> graph = GraphUtil.importJson(graphName);
						modelLearner.graphBuilder = new GraphBuilderTopK(ontologyManager, graph);
						modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					logger.info("building the graph ...");
					for (SemanticModel sm : trainingData)
//						modelLearningGraph.addModel(sm);
						modelLearningGraph.addModelAndUpdate(sm, PatternWeightSystem.JWSPaperFormula);
					modelLearner.graphBuilder = modelLearningGraph.getGraphBuilder();
					modelLearner.nodeIdFactory = modelLearner.graphBuilder.getNodeIdFactory();
					// save graph to file
					try {
//						GraphUtil.exportJson(modelLearningGraph.getGraphBuilder().getGraph(), graphName);
//						GraphVizUtil.exportJGraphToGraphviz(modelLearner.graphBuilder.getGraph(), 
//								"test", 
//								true, 						
//								GraphVizLabelType.LocalId,
//								GraphVizLabelType.LocalUri,
//								false, 
//								true, 
//								graphName + ".dot");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
//				HashMap<String, Integer> test = new HashMap<String, Integer>();
//				for (DefaultLink l : modelLearner.graphBuilder.getGraph().edgeSet()) {
//					String key = l.getSource().getId() + l.getTarget().getId();
//					Integer c = test.get(key);
//					if (c == null) c = 0;
//					test.put(key, c + 1);
//				}
//				for (String s : test.keySet()) {
//					if (test.get(s) > 1)
//						System.out.println(s + "\n" + test.get(s));
//				}
				
				List<SortableSemanticModel> hypothesisList = modelLearner.hypothesize(useCorrectType, numberOfCandidates);

				long elapsedTimeMillis = System.currentTimeMillis() - start;
				float elapsedTimeSec = elapsedTimeMillis/1000F;

//				System.out.println("time: " + elapsedTimeSec);
				
				int cutoff = 20;//ModelingConfiguration.getMaxCandidateModels();
				List<SortableSemanticModel> topHypotheses = null;
				if (hypothesisList != null) {
					topHypotheses = hypothesisList.size() > cutoff ? 
							hypothesisList.subList(0, cutoff) : 
								hypothesisList;
				}

				Map<String, SemanticModel> models = 
						new TreeMap<String, SemanticModel>();

				// export to json
				//				if (topHypotheses != null)
				//					for (int k = 0; k < topHypotheses.size() && k < 3; k++) {
				//						
				//						String fileExt = null;
				//						if (k == 0) fileExt = Params.MODEL_RANK1_FILE_EXT;
				//						else if (k == 1) fileExt = Params.MODEL_RANK2_FILE_EXT;
				//						else if (k == 2) fileExt = Params.MODEL_RANK3_FILE_EXT;
				//						SortableSemanticModel m = topHypotheses.get(k);
				//						new SemanticModel(m).writeJson(Params.MODEL_DIR + 
				//								newSource.getName() + fileExt);
				//						
				//					}

				ModelEvaluation me;
				models.put("1-correct model", correctModel);
				if (topHypotheses != null)
					for (int k = 0; k < topHypotheses.size(); k++) {

						SortableSemanticModel m = topHypotheses.get(k);

						me = m.evaluate(correctModel);

						String label = "candidate " + k + "\n" + 
//								(m.getSteinerNodes() == null ? "" : m.getSteinerNodes().getScoreDetailsString()) +
								"link coherence:" + (m.getLinkCoherence() == null ? "" : m.getLinkCoherence().getCoherenceValue()) + "\n";
						label += (m.getSteinerNodes() == null || m.getSteinerNodes().getCoherence() == null) ? 
								"" : "node coherence:" + m.getSteinerNodes().getCoherence().getCoherenceValue() + "\n";
						label += "confidence:" + m.getConfidenceScore() + "\n";
						label += m.getSteinerNodes() == null ? "" : "mapping score:" + m.getSteinerNodes().getScore() + "\n";
						label +=
								"cost:" + roundDecimals(m.getCost(), 6) + "\n" +
								//								"-distance:" + me.getDistance() + 
								"-precision:" + me.getPrecision() + 
								"-recall:" + me.getRecall();

						models.put(label, m);

						if (k == 0) { // first rank model
							System.out.println("number of known models: " + numberOfKnownModels +
									", precision: " + me.getPrecision() + 
									", recall: " + me.getRecall() + 
									", time: " + elapsedTimeSec);
							logger.info("number of known models: " + numberOfKnownModels +
									", precision: " + me.getPrecision() + 
									", recall: " + me.getRecall() + 
									", time: " + elapsedTimeSec);
//							resultFile.println("number of known models \t precision \t recall");
//							resultFile.println(numberOfKnownModels + "\t" + me.getPrecision() + "\t" + me.getRecall());
							String s = me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec;
							if (iterativeEvaluation) {
								if (resultsArray[numberOfKnownModels + 2].length() > 0) 
									resultsArray[numberOfKnownModels + 2].append(" \t ");
								resultsArray[numberOfKnownModels + 2].append(s);
							} else {
								s = newSource.getName() + "\t" + me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec;
								resultFile.println(s);
							}

//							resultFile.println(me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec);
						}
					}

				String outputPath = Params.OUTPUT_DIR;
				String outName = !iterativeEvaluation?
						outputPath + semanticModels.get(newSourceIndex).getName() + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT : 
							outputPath + semanticModels.get(newSourceIndex).getName() + ".knownModels=" + numberOfKnownModels + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT;

				GraphVizUtil.exportSemanticModelsToGraphviz(
						models, 
						newSource.getName(),
						outName,
						GraphVizLabelType.LocalId,
						GraphVizLabelType.LocalUri,
						true,
						true);

				numberOfKnownModels ++;

			}

		//	resultFile.println("=======================================================");
		}
		if (iterativeEvaluation) {
			for (StringBuffer s : resultsArray)
				resultFileIterative.println(s.toString());
			resultFileIterative.close();
		} else {
			resultFile.close();
		}
	}
	
	public static void main(String[] args) throws Exception {

		test();

	}

}
