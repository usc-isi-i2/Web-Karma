package edu.isi.karma.metadata;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.TrivialErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class SemanticTypeModelMetadata extends KarmaUserMetadata {

	private static final Logger logger = LoggerFactory.getLogger(SemanticTypeModelMetadata.class);
	
	public SemanticTypeModelMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
	}
	
	@Override
	public void setup(UpdateContainer uc, Workspace workspace) {
		File modelFile;
		modelFile = new File(contextParameters.getParameterValue(ContextParameter.SEMTYPE_MODEL_DIRECTORY));
		/* Read and populate CRF Model from a file */
		if(!modelFile.exists())
		{
			try {
				modelFile.createNewFile();
			} catch (IOException e) {
				uc.add(new TrivialErrorUpdate("Unable to create CRF Model file at " + modelFile.getAbsolutePath()));
				return;
			}
		}
		boolean result = workspace.getSemanticTypeModelHandler().readModelFromFile(modelFile.getAbsolutePath());
		if (!result)
			logger.error("Error occured while reading CRF Model!");
		String trainingExampleMaxCount = contextParameters
						.getParameterValue(ContextParameter.TRAINING_EXAMPLE_MAX_COUNT);
		if(trainingExampleMaxCount == null || trainingExampleMaxCount.isEmpty())
		{
			contextParameters.setParameterValue(ContextParameter.TRAINING_EXAMPLE_MAX_COUNT, "200");
		}
		workspace.getSemanticTypeModelHandler().setModelHandlerEnabled(true);
	}


	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.SEMTYPE_MODEL_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "semantic-type-files/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.SEMTYPE_MODEL;
	}


}
