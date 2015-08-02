package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class JSONMetadata extends KarmaPublishedMetadata {


	public JSONMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
	}
	
	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.JSON_PUBLISH_DIR;
	}

	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.JSON_PUBLISH_RELATIVE_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "JSON/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.JSON;
	}

}
