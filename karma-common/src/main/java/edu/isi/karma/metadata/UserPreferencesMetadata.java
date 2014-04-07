package edu.isi.karma.metadata;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class UserPreferencesMetadata extends KarmaUserMetadata{

	public UserPreferencesMetadata() throws KarmaException {
		super(null);
	}
	public UserPreferencesMetadata(Workspace workspace) throws KarmaException {
		super(workspace);
	}

	@Override
	public void setup() throws KarmaException {
		
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {

		return ContextParameter.USER_PREFERENCES_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "UserPrefs/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.USER_PREFERENCES;
	}
}
