package edu.isi.karma.modeling.semantictypes;

import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.SemanticType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SemanticTypeTrainingThread implements Runnable {

	private final ISemanticTypeModelHandler modelHandler;
	private final Worksheet worksheet;
	private final SemanticType newType;
	
	private final Logger logger = LoggerFactory.getLogger(SemanticTypeTrainingThread.class);
	
	public SemanticTypeTrainingThread(ISemanticTypeModelHandler modelHandler, Worksheet worksheet, SemanticType newType) {
		this.modelHandler = modelHandler;
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


		// Train the model with the new type
		ArrayList<String> trainingExamples = SemanticTypeUtil.getTrainingExamples(worksheet, currentColumnPath);
		
		boolean typeAdded = modelHandler.addType(newType.getCrfModelLabelString(), trainingExamples);

		if (!typeAdded) {
			logger.error("Error occured while training CRF Model.");
		}

		List<SemanticTypeLabel> trainingResult = modelHandler.predictType(trainingExamples, 4);
		if (trainingResult == null) {
			logger.error("Error occured while predicting labels");
		}
		SemanticTypeColumnModel newModel = new SemanticTypeColumnModel(trainingResult);
		worksheet.getSemanticTypeModel().addColumnModel(newType.getHNodeId(), newModel);

		long elapsedTimeMillis = System.currentTimeMillis() - start;
		float elapsedTimeSec = elapsedTimeMillis / 1000F;
		logger.debug("Time required for training the semantic type: " + elapsedTimeSec);
	
	}

}
