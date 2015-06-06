package edu.isi.karma.metadata;

import java.io.File;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class UserConfigMetadata extends KarmaUserMetadata {

	public UserConfigMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
	}
	
	@Override
	protected ContextParameter getDirectoryContextParameter() {

		return ContextParameter.USER_CONFIG_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "config" + File.separator;
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.USER_CONFIG;
	}

}
