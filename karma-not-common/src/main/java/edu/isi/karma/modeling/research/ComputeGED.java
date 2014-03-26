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
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import edu.isi.karma.modeling.alignment.GraphVizUtil;
import edu.isi.karma.modeling.alignment.ModelEvaluation;
import edu.isi.karma.modeling.alignment.SemanticModel;

public class ComputeGED {

	private static Logger logger = LoggerFactory.getLogger(ComputeGED.class);

	public static void main(String[] args) throws Exception {
		computeGEDApp1();
//		computeGEDApp2();
	}
	
	private static void computeGEDApp1() throws Exception {
		
		File ff = new File(Params.MODEL_DIR);
		File[] files = ff.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase();
				if (lowercaseName.contains(".model")) {
					return true;
				} else {
					return false;
				}
			}
		});
		
		SemanticModel mMain, 
			mKarmaInitial, mKarmaFinal, 
			mApp1Rank1, mApp1Rank2, mApp1Rank3;
		
		HashSet<File> fileSet = new HashSet<File>(Arrays.asList(files));
		
		Function<File, String> sameService = new Function<File, String>() {
		  @Override public String apply(final File s) {
		    return s.getName().substring(0, s.getName().indexOf(".model"));
		  }
		};

		Multimap<String, File> index =
				   Multimaps.index(fileSet, sameService);	
	
		int countOfRank1Models = 0, countOfRank2Models = 0, countOfRank3Models = 0,
				countOfKarmaInitialModels = 0, countOfKarmaFinalModels = 0;
		double sumGEDRank1 = 0.0, sumGEDRank2 = 0.0, sumGEDRank3 = 0.0,
				sumGEDKarmaInitial = 0.0, sumGEDKarmaFinal = 0.0;
		double sumPrecisionRank1 = 0.0, sumPrecisionRank2 = 0.0, sumPrecisionRank3 = 0.0,
				sumPrecisionKarmaInitial = 0.0, sumPrecisionKarmaFinal = 0.0;
		double sumRecallRank1 = 0.0, sumRecallRank2 = 0.0, sumRecallRank3 = 0.0,
				sumRecallKarmaInitial = 0.0, sumRecallKarmaFinal = 0.0;
		
		for (String s : index.keySet()) {
			
			System.out.println(s);
			Collection<File> serviceFiles = index.get(s);
			mMain = null; 
			mKarmaInitial = null; mKarmaFinal = null; 
			mApp1Rank1 = null; mApp1Rank2 = null; mApp1Rank3 = null; 
			
			for (File f : serviceFiles) {
				if (f.getName().endsWith(Params.MODEL_MAIN_FILE_EXT)) {
					try { mMain = SemanticModel.readJson(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(Params.MODEL_KARMA_INITIAL_FILE_EXT)) {
					try { mKarmaInitial = SemanticModel.readJson(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(Params.MODEL_KARMA_FINAL_FILE_EXT)) {
					try { mKarmaFinal = SemanticModel.readJson(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(Params.MODEL_RANK1_FILE_EXT)) {
					try { mApp1Rank1 = SemanticModel.readJson(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(Params.MODEL_RANK2_FILE_EXT)) {
					try { mApp1Rank2 = SemanticModel.readJson(f.getPath()); } catch (Exception e) {}
				} else if (f.getName().endsWith(Params.MODEL_RANK3_FILE_EXT)) {
					try { mApp1Rank3 = SemanticModel.readJson(f.getPath()); } catch (Exception e) {}
				}					
			}
			
			if (mMain == null) continue;
			String label; ModelEvaluation me;
			
			Map<String, SemanticModel> models = 
					new TreeMap<String, SemanticModel>();
			
			label = "1- main";
			models.put(label, mMain);

			if (mKarmaInitial != null) {
				me = mKarmaInitial.evaluate(mMain);
				sumGEDKarmaInitial += me.getDistance();
				sumPrecisionKarmaInitial += me.getPrecision();
				sumRecallKarmaInitial += me.getRecall();
				label = "2-karma initial" + 
						"-distance:" + me.getDistance() + 
						"-precision:" + me.getPrecision() + 
						"-recall:" + me.getRecall();
				models.put(label, mKarmaInitial);
				countOfKarmaInitialModels++;
			}
			
			if (mKarmaFinal != null) {
				me = mKarmaFinal.evaluate(mMain);
				sumGEDKarmaFinal += me.getDistance();
				sumPrecisionKarmaFinal += me.getPrecision();
				sumRecallKarmaFinal += me.getRecall();
				label = "3-karma final" + 
						"-distance:" + me.getDistance() + 
						"-precision:" + me.getPrecision() + 
						"-recall:" + me.getRecall();
				models.put(label, mKarmaFinal);
				countOfKarmaFinalModels++;
			}
			
			if (mApp1Rank1 != null) {
				me = mApp1Rank1.evaluate(mMain);
				sumGEDRank1 += me.getDistance();
				sumPrecisionRank1 += me.getPrecision();
				sumRecallRank1 += me.getRecall();
				label = "4-rank1" + 
						"-distance:" + me.getDistance() + 
						"-precision:" + me.getPrecision() + 
						"-recall:" + me.getRecall();
				models.put(label, mApp1Rank1);
				countOfRank1Models++;
			}
			
			if (mApp1Rank2 != null) {
				me = mApp1Rank2.evaluate(mMain);
				sumGEDRank2 += me.getDistance();
				sumPrecisionRank2 += me.getPrecision();
				sumRecallRank2 += me.getRecall();
				label = "5-rank2" + 
						"-distance:" + me.getDistance() + 
						"-precision:" + me.getPrecision() + 
						"-recall:" + me.getRecall();
				models.put(label, mApp1Rank2);
				countOfRank2Models++;
			}

			if (mApp1Rank3 != null) {
				me = mApp1Rank3.evaluate(mMain);
				sumGEDRank3 += me.getDistance();
				sumPrecisionRank3 += me.getPrecision();
				sumRecallRank3 += me.getRecall();
				label = "6-rank3" + 
						"-distance:" + me.getDistance() + 
						"-precision:" + me.getPrecision() + 
						"-recall:" + me.getRecall();
				models.put(label, mApp1Rank3);
				countOfRank3Models++;
			}

			GraphVizUtil.exportSemanticModelsToGraphviz(models, s, Params.OUTPUT_DIR + s + Params.GRAPHVIS_OUT_FILE_EXT);
		}
		
		if (countOfKarmaInitialModels > 0) {
			logger.info("total GED for karma initial models: " + sumGEDKarmaInitial);
			logger.info("average precision for karma initial models: " + (sumPrecisionKarmaInitial/(double)countOfKarmaInitialModels));
			logger.info("average recall for karma initial models: " + roundTwoDecimals(sumRecallKarmaInitial/(double)countOfKarmaInitialModels));
		}
		if (countOfKarmaFinalModels > 0) {
			logger.info("total GED for karma final models:   " + sumGEDKarmaFinal);
			logger.info("average precision for karma final models: " + roundTwoDecimals(sumPrecisionKarmaFinal/(double)countOfKarmaFinalModels));
			logger.info("average recall for karma initial models: " + roundTwoDecimals(sumRecallKarmaFinal/(double)countOfKarmaFinalModels));
		}
		if (countOfRank1Models > 0) {
			logger.info("total GED for rank1 models:   " + sumGEDRank1);
			logger.info("average precision for rank1 models: " + roundTwoDecimals(sumPrecisionRank1/(double)countOfRank1Models));
			logger.info("average recall for rank1 models: " + roundTwoDecimals(sumRecallRank1/(double)countOfRank1Models));
		}
		if (countOfRank2Models > 0) {
			logger.info("total GED for rank2 models:   " + sumGEDRank2);
			logger.info("average precision for rank2 models: " + roundTwoDecimals(sumPrecisionRank2/(double)countOfRank2Models));
			logger.info("average recall for rank2 models: " + roundTwoDecimals(sumRecallRank2/(double)countOfRank2Models));
		}
		if (countOfRank3Models > 0) {
			logger.info("total GED for rank3 models:   " + sumGEDRank3);
			logger.info("average precision for rank3 models: " + roundTwoDecimals(sumPrecisionRank3/(double)countOfRank3Models));
			logger.info("average recall for rank3 models: " + roundTwoDecimals(sumRecallRank3/(double)countOfRank3Models));
		}

	}

	private static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
	}
	
//	private static void computeGEDApp2() throws Exception {
//		
//		File ff = new File(Params.JGRAPHT_DIR);
//		File[] files = ff.listFiles();
//		
//		DirectedWeightedMultigraph<Node, Link> gMain, 
//			gKarmaInitial, gKarmaFinal, gApp2;
//		
//		HashSet<File> fileSet = new HashSet<File>(Arrays.asList(files));
//		
//		Function<File, String> sameService = new Function<File, String>() {
//		  @Override public String apply(final File s) {
//		    return s.getName().substring(0, s.getName().indexOf('.'));
//		  }
//		};
//
//		Multimap<String, File> index =
//				   Multimaps.index(fileSet, sameService);	
//	
//		for (String s : index.keySet()) {
//			
//			System.out.println(s);
//			Collection<File> serviceFiles = index.get(s);
//			gMain = null; 
//			gKarmaInitial = null; gKarmaFinal = null; gApp2 = null;
//			
//			for (File f : serviceFiles) {
//				if (f.getName().endsWith(".main.jgraph")) {
//					try { gMain = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
//				} else if (f.getName().endsWith(".karma.initial.jgraph")) {
//					try { gKarmaInitial = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
//				} else if (f.getName().endsWith(".karma.final.jgraph")) {
//					try { gKarmaFinal = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
//				} else if (f.getName().endsWith(".app2.jgraph")) {
//					try { gApp2 = GraphUtil.deserialize(f.getPath()); } catch (Exception e) {}
//					gApp2 = GraphUtil.deserialize(f.getPath());
//				}					
//			}
//			
//			if (gMain == null) continue;
//			String label; double distance;
//			
//			Map<String, DirectedWeightedMultigraph<Node, Link>> graphs = 
//					new TreeMap<String, DirectedWeightedMultigraph<Node,Link>>();
//			
//			label = "0- Main";
//			graphs.put(label, gMain);
//
//			if (gKarmaInitial != null) {
//				distance = getDistance(gMain, gKarmaInitial);
//				label = "1-Karma Initial" + "-distance:" + distance;
//				graphs.put(label, gKarmaInitial);
//			}
//			
//			if (gKarmaFinal != null) {
//				distance = getDistance(gMain, gKarmaFinal);
//				label = "3-Karma Final" + "-distance:" + distance;
//				graphs.put(label, gKarmaFinal);
//			}
//			
//			if (gApp2 != null) {
//				distance = getDistance(gMain, gApp2);
//				label = "2-Output" + "-distance:" + distance;
//				graphs.put(label, gApp2);
//			}
//			
//
//
//			GraphVizUtil.exportJGraphToGraphvizFile(graphs, s, Params.OUTPUT_DIR + s + ".app2.out.dot");
//		}
//	}
	
}
