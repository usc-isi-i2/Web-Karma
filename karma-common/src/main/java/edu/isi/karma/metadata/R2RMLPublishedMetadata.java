package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class R2RMLPublishedMetadata extends KarmaPublishedMetadata {

	public R2RMLPublishedMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.R2RML_PUBLISH_DIR;
	}
	
	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.R2RML_PUBLISH_RELATIVE_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "R2RML/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.R2RML_MODEL;
	}

}
