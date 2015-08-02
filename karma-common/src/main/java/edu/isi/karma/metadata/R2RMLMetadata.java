package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class R2RMLMetadata extends KarmaUserMetadata {

	public R2RMLMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
	}
	
	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.R2RML_USER_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "models-autosave/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.R2RML_MODEL;
	}


	


	

}
