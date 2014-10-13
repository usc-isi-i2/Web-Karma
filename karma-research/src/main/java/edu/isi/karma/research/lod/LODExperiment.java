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

package edu.isi.karma.research.lod;

import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.modeling.alignment.GraphBuilder;
import edu.isi.karma.modeling.alignment.GraphUtil;
import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.ModelEvaluation;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelLearner;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.alignment.learner.SortableSemanticModel;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.research.Params;
import edu.isi.karma.rep.alignment.ColumnNode;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class LODExperiment {

	private static Logger logger = LoggerFactory.getLogger(LODExperiment.class);


	private static double roundDecimals(double d, int k) {
		String format = "";
		for (int i = 0; i < k; i++) format += "#";
		DecimalFormat DForm = new DecimalFormat("#." + format);
		return Double.valueOf(DForm.format(d));
	}
	
	
	public static void main(String[] args) throws Exception {

		ServletContextParameterMap.setParameterValue(ContextParameter.USER_CONFIG_DIRECTORY, "/Users/mohsen/karma/config");

		OntologyManager ontologyManager = new OntologyManager();
		File ff = new File(Params.ONTOLOGY_DIR);
		File[] files = ff.listFiles();
		if (files == null) {
			logger.error("no ontology to import at " + ff.getAbsolutePath());
			return;
		}

		for (File f : files) {
			if (f.getName().endsWith(".owl") || 
					f.getName().endsWith(".rdf") || 
					f.getName().endsWith(".n3") || 
					f.getName().endsWith(".ttl") || 
					f.getName().endsWith(".xml")) {
				logger.info("Loading ontology file: " + f.getAbsolutePath());
				ontologyManager.doImport(f, "UTF-8");
			}
		}
		ontologyManager.updateCache(); 


		String outputPath = Params.OUTPUT_DIR;
		String graphPath = Params.GRAPHS_DIR;

		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);

		List<SemanticModel> trainingData = new ArrayList<SemanticModel>();


		ModelLearner modelLearner = null;

		boolean useCorrectType = false;
		int numberOfCRFCandidates = 4;
		String filePath = Params.RESULTS_DIR;
		String filename = "results,k=" + numberOfCRFCandidates + ".csv"; 
		PrintWriter resultFile = new PrintWriter(new File(filePath + filename));

		resultFile.println("source \t p \t r \t t \n");

		for (int i = 0; i < semanticModels.size(); i++) {
//		for (int i = 0; i <= 10; i++) {
//		int i = 0; {

			int newSourceIndex = i;
			SemanticModel newSource = semanticModels.get(newSourceIndex);

			logger.info("======================================================");
			logger.info(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			System.out.println(newSource.getName() + "(#attributes:" + newSource.getColumnNodes().size() + ")");
			logger.info("======================================================");


			trainingData.clear();

			for (int j = 0; j < semanticModels.size(); j++) {
				if (j != newSourceIndex) {
					trainingData.add(semanticModels.get(j));
				}
			}

			SemanticModel correctModel = newSource;
			List<ColumnNode> columnNodes = correctModel.getColumnNodes();

			List<Node> steinerNodes = new LinkedList<Node>(columnNodes);
			long start = System.currentTimeMillis();

			String graphName = graphPath + "lod" + Params.GRAPH_FILE_EXT; 

			if (new File(graphName).exists()) {
				// read graph from file
				try {
					logger.info("loading the graph ...");
					DirectedWeightedMultigraph<Node, DefaultLink> graph = GraphUtil.importJson(graphName);
					modelLearner = new ModelLearner(new GraphBuilder(ontologyManager, graph), steinerNodes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else 
			{
				logger.info("building the graph ...");
				// create and save the graph to file
				LODSchemaGraphBuilder b = new LODSchemaGraphBuilder(ontologyManager, 
						Params.LOD_OBJECT_PROPERIES_FILE, 
						Params.LOD_DATA_PROPERIES_FILE);
				modelLearner = new ModelLearner(b.getGraphBuilder(), steinerNodes);
			}

			if (modelLearner == null) {
				resultFile.close();
				return;
			}

			List<SortableSemanticModel> hypothesisList = modelLearner.hypothesize(useCorrectType, numberOfCRFCandidates);

			long elapsedTimeMillis = System.currentTimeMillis() - start;
			float elapsedTimeSec = elapsedTimeMillis/1000F;

			List<SortableSemanticModel> topHypotheses = null;
			if (hypothesisList != null) {
				topHypotheses = hypothesisList.size() > ModelingConfiguration.getMaxCandidateModels() ? 
						hypothesisList.subList(0, ModelingConfiguration.getMaxCandidateModels()) : 
							hypothesisList;
			}

			Map<String, SemanticModel> models = 
					new TreeMap<String, SemanticModel>();

			ModelEvaluation me;
			models.put("1-correct model", correctModel);
			if (topHypotheses != null)
				for (int k = 0; k < topHypotheses.size(); k++) {

					SortableSemanticModel m = topHypotheses.get(k);

					me = m.evaluate(correctModel);

					String label = "candidate" + k + 
							m.getSteinerNodes().getScoreDetailsString() +
							"cost:" + roundDecimals(m.getCost(), 6) + 
							//								"-distance:" + me.getDistance() + 
							"-precision:" + me.getPrecision() + 
							"-recall:" + me.getRecall();

					models.put(label, m);

					if (k == 0) { // first rank model
						System.out.println("precision: " + me.getPrecision() + 
								", recall: " + me.getRecall() + 
								", time: " + elapsedTimeSec);
						logger.info("precision: " + me.getPrecision() + 
								", recall: " + me.getRecall() + 
								", time: " + elapsedTimeSec);
						String s = newSource.getName() + me.getPrecision() + "\t" + me.getRecall() + "\t" + elapsedTimeSec;
						resultFile.println(s);

					}
				}

			String outName = outputPath + newSource.getName() + Params.GRAPHVIS_OUT_DETAILS_FILE_EXT;

			GraphVizUtil.exportSemanticModelsToGraphviz(
					models, 
					newSource.getName(),
					outName,
					true,
					false);

		}

		resultFile.close();

	}
	
}
