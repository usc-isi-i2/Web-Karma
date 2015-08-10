package edu.isi.karma.research.modeling;

import java.text.DecimalFormat;
import java.util.ArrayList;
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

//	public static void main(String[] args) throws Exception {
//		
//		List<SemanticModel> semanticModels = 
//				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);
//		
//		double sum = 0.0;
//		int count = 0;
//		ModelEvaluation me;
//		for (int i = 0; i < semanticModels.size() - 1; i++) {
//			SemanticModel s1 = semanticModels.get(i);
//			for (int j = i + 1; j < semanticModels.size(); j++) {
//				SemanticModel s2 = semanticModels.get(j);
//				me = s1.evaluate(s2, false, true);
//				sum += me.getJaccard();
//				count ++;
//				System.out.println("jaccard similarity of (" + i + "," + j + "): " + me.getJaccard());
//			}
//		}
//
//		double overlap = count == 0 ? 0.0 : sum/(double)count;
//		logger.info("overlap: " + roundTwoDecimals(overlap));
//		System.out.println("overlap: " + roundTwoDecimals(overlap));
//	}
	
	public static void main(String[] args) throws Exception {

		List<SemanticModel> semanticModels = 
				ModelReader.importSemanticModelsFromJsonFiles(Params.MODEL_DIR, Params.MODEL_MAIN_FILE_EXT);

		List<SemanticModel> trainingData = new ArrayList<SemanticModel>();
		for (int i = 0; i < semanticModels.size(); i++) {

			int newSourceIndex = i;
			int numberOfKnownModels = 0;

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
				
				double maxOverlap = ModelOverlap.getMaxOverlap(trainingData, semanticModels.get(newSourceIndex));
				logger.info("maxOverlap: " + maxOverlap);
				System.out.println(maxOverlap);

//				double avgOverlap = ModelOverlap.getAvgOverlap(trainingData, semanticModels.get(newSourceIndex));
//				logger.info("avgOverlap: " + maxOverlap);
//				System.out.println(avgOverlap);
				
				numberOfKnownModels ++;
			}
		}
	}
	
	
}
