package edu.isi.karma.metadata;

import java.io.File;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class UserPreferencesMetadata extends KarmaUserMetadata{

	public UserPreferencesMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {

		return ContextParameter.USER_PREFERENCES_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "user-preferences" + File.separator;
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.USER_PREFERENCES;
	}
}
