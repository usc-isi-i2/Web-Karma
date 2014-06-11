package edu.isi.karma.metadata;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.TrivialErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class CRFModelMetadata extends KarmaUserMetadata {

	private static final Logger logger = LoggerFactory.getLogger(CRFModelMetadata.class);
	
	public CRFModelMetadata(Workspace workspace) throws KarmaException {
		super(workspace);
	}
	
	@Override
	public void setup(UpdateContainer uc) {
		File crfModelFile = null;
		crfModelFile = new File(ServletContextParameterMap.getParameterValue(ContextParameter.CRF_MODEL_DIRECTORY) + 
				workspace.getId()+"_CRFModel.txt");
		/* Read and populate CRF Model from a file */
		if(!crfModelFile.exists())
		{
			try {
				crfModelFile.createNewFile();
			} catch (IOException e) {
				uc.add(new TrivialErrorUpdate("Unable to create CRF Model file at " + crfModelFile.getAbsolutePath()));
				return;
			}
		}
		boolean result = workspace.getCrfModelHandler().readModelFromFile(crfModelFile.getAbsolutePath());
		if (!result)
			logger.error("Error occured while reading CRF Model!");
		String trainingExampleMaxCount = ServletContextParameterMap
						.getParameterValue(ContextParameter.TRAINING_EXAMPLE_MAX_COUNT);
		if(trainingExampleMaxCount == null || trainingExampleMaxCount.isEmpty())
		{
			ServletContextParameterMap.setParameterValue(ContextParameter.TRAINING_EXAMPLE_MAX_COUNT, "200");
		}
		CRFModelHandler.setCRFModelHandlerEnabled(true);
	}


	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.CRF_MODEL_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "CRF_Models/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.CRF_MODEL;
	}


}
