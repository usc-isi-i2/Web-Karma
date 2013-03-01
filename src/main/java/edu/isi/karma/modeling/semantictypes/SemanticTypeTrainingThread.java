package edu.isi.karma.modeling.semantictypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler.ColumnFeature;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.SemanticType;

public class SemanticTypeTrainingThread implements Runnable {

	private final CRFModelHandler crfModelHandler;
	private final Worksheet worksheet;
	private final SemanticType newType;
	
	private final Logger logger = LoggerFactory.getLogger(SemanticTypeTrainingThread.class);
	
	public SemanticTypeTrainingThread(CRFModelHandler crfModelHandler, Worksheet worksheet, SemanticType newType) {
		this.crfModelHandler = crfModelHandler;
		this.worksheet = worksheet;
		this.newType = newType;
	}

	
	public void run() {
		long start = System.currentTimeMillis();
		// Find the corresponding hNodePath. Used to find examples for training the CRF Model.
		HNodePath currentColumnPath = null;
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : paths) {
			if (path.getLeaf().getId().equals(newType.getHNodeId())) {
				currentColumnPath = path;
				break;
			}
		}

		Map<ColumnFeature, Collection<String>> columnFeatures = new HashMap<ColumnFeature, Collection<String>>();

		// Prepare the column name for training
		String columnName = currentColumnPath.getLeaf().getColumnName();
		Collection<String> columnNameList = new ArrayList<String>();
		columnNameList.add(columnName);
		columnFeatures.put(ColumnFeature.ColumnHeaderName, columnNameList);

		// Train the model with the new type
		ArrayList<String> trainingExamples = SemanticTypeUtil.getTrainingExamples(worksheet, currentColumnPath);
		boolean trainingResult = false;
		String newTypeString = (newType.getDomain() == null) ? 
				newType.getType().getUri() : newType.getDomain().getUri() + "|" + newType.getType().getUri();
		
		trainingResult = crfModelHandler.addOrUpdateLabel(newTypeString,trainingExamples, columnFeatures);

		if (!trainingResult) {
			logger.error("Error occured while training CRF Model.");
		}
//		logger.debug("Using type:" + newType.getDomain().getUri() + "|" + newType.getType().getUri());

		// Add the new CRF column model for this column
		ArrayList<String> labels = new ArrayList<String>();
		ArrayList<Double> scores = new ArrayList<Double>();
		trainingResult = crfModelHandler.predictLabelForExamples(trainingExamples, 4, labels, scores, null, columnFeatures);
		if (!trainingResult) {
			logger.error("Error occured while predicting labels");
		}
		CRFColumnModel newModel = new CRFColumnModel(labels, scores);
		worksheet.getCrfModel().addColumnModel(newType.getHNodeId(), newModel);

		long elapsedTimeMillis = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsedTimeMillis / 1000F;
		logger.info("Time required for training the semantic type: " + elapsedTimeSec);
		
//		long t2 = System.currentTimeMillis();
		
		// Identify the outliers for the column
//		SemanticTypeUtil.identifyOutliers(worksheet, newTypeString,currentColumnPath, vWorkspace.getWorkspace().getTagsContainer()
//				.getTag(TagName.Outlier), columnFeatures, crfModelHandler);

		

//		long t3 = System.currentTimeMillis();
//		logger.info("Identify outliers: "+ (t3-t2));
	}

}
