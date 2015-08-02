package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class JSONModelsMetadata extends KarmaUserMetadata {

	
	public JSONModelsMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
	}
	
	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.JSON_MODELS_DIR;
	}

	@Override
	protected String getDirectoryPath() {
		return "models-json/";
	}
	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.JSON_MODELS;
	}
}
