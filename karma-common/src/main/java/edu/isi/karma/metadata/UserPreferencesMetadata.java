package edu.isi.karma.metadata;

import java.io.File;

import edu.isi.karma.controller.update.UpdateContainer;
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
	public void setup(UpdateContainer uc) {
		
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
