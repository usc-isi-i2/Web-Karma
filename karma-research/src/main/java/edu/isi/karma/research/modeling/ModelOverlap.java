package edu.isi.karma.research.modeling;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.alignment.ModelEvaluation;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelReader;
import edu.isi.karma.modeling.research.Params;

public class ModelOverlap {
	
	private static Logger logger = LoggerFactory.getLogger(ModelOverlap.class);

	private static double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        return Double.valueOf(twoDForm.format(d));
	}
	
	public static double getMaxOverlap(List<SemanticModel> trainingModels, SemanticModel testModel) {
		
		if (trainingModels == null || testModel == null)
			return 0.0;

		double maxOverlap = 0.0;
		ModelEvaluation me;
		for (SemanticModel trainingModel : trainingModels) {
			if (trainingModel == null)
				continue;
			me = trainingModel.evaluate(testModel, false, true);
			if (me.getJaccard() > maxOverlap)
				maxOverlap = me.getJaccard();
		}
		return maxOverlap;
	}

	public static double getAvgOverlap(List<SemanticModel> trainingModels, SemanticModel testModel) {
		
		if (trainingModels == null || testModel == null)
			return 0.0;

		double sum = 0.0;
		int count = 0;
		ModelEvaluation me;
		for (SemanticModel trainingModel : trainingModels) {
			if (trainingModel == null)
				continue;
			me = trainingModel.evaluate(testModel, false, true);
			sum += me.getJaccard();
			count ++;
		}
		return roundTwoDecimals(count == 0 ? 0.0 : sum/(double)count);
	}

	public static void main(String[] args) throws Exception {
		
		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);
		
		double sum = 0.0;
		double min = 0.0;
		double max = 0.0;
		double avg = 0.0;
		double median = 0.0;
		
		List<Double> jaccard = new ArrayList<Double>();
		
		ModelEvaluation me;
		for (int i = 0; i < semanticModels.size() - 1; i++) {
			SemanticModel s1 = semanticModels.get(i);
			for (int j = i + 1; j < semanticModels.size(); j++) {
				SemanticModel s2 = semanticModels.get(j);
				me = s1.evaluate(s2, false, true);
				jaccard.add(me.getJaccard());
				System.out.println("jaccard similarity of (" + i + "," + j + "): " + me.getJaccard());
			}
		}
		
		int n = jaccard.size();
		if (n == 0)
			return;
		
		for (Double d : jaccard) {
			sum += d;
		}
		
		Collections.sort(jaccard);
		min = jaccard.get(0);
		max = jaccard.get(jaccard.size() - 1);
		avg = roundTwoDecimals(sum/(double)n);
		
		if (jaccard.size() % 2 == 0)
		    median = (jaccard.get(n/2) + jaccard.get(n/2 - 1))/2;
		else
		    median = jaccard.get(n/2);
		
		System.out.println("dataset: " + Params.DATASET_NAME);
		System.out.println("min overlap: " + min);
		System.out.println("max overlap: " + max);
		System.out.println("average overlap: " + avg);
		System.out.println("median overlap: " + median);
		
		logger.info("done.");
	}
	
//	public static void main(String[] args) throws Exception {
//
//		boolean useMaxOverlap = false;
//		boolean useAvgOverlap = !useMaxOverlap;
//		
//		List<SemanticModel> semanticModels = 
//				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);
//
//		double[] sumOverlap = new double[semanticModels.size()];
//		for (int i = 0; i < sumOverlap.length; i++) sumOverlap[i] = 0.0;
//		
//		List<SemanticModel> trainingData = new ArrayList<SemanticModel>();
//		for (int i = 0; i < semanticModels.size(); i++) {
//
//			int newSourceIndex = i;
//			int numberOfKnownModels = 0;
//
//			while (numberOfKnownModels < semanticModels.size()) 
//			{
//
//				trainingData.clear();
//
//				int j = 0, count = 0;
//				while (count < numberOfKnownModels) {
//					if (j != newSourceIndex) {
//						trainingData.add(semanticModels.get(j));
//						count++;
//					} 
//					j++;
//				}
//				
//				double overlap = 0.0;
//				if (useMaxOverlap)
//					overlap = ModelOverlap.getMaxOverlap(trainingData, semanticModels.get(newSourceIndex));
//				else if (useAvgOverlap)
//					overlap = ModelOverlap.getAvgOverlap(trainingData, semanticModels.get(newSourceIndex));
//				
////				logger.info(overlap);
////				System.out.println(overlap);
//				
//				sumOverlap[numberOfKnownModels] += overlap;
//				numberOfKnownModels ++;
//			}
//		}
//		
//		double avgOverlap;
//		for (int i = 0; i < semanticModels.size(); i++) {
//			avgOverlap = semanticModels.size() == 0 ? 0.0 : roundTwoDecimals(sumOverlap[i] / (double)semanticModels.size());
//			System.out.println("avg overlap , num of known models " + i + ": " + avgOverlap);
//		}
//	}
	
	
}
