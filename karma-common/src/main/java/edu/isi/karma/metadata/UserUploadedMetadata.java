package edu.isi.karma.metadata;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class UserUploadedMetadata extends KarmaUserMetadata{

	public UserUploadedMetadata() throws KarmaException {
		super(null);
	}
	public UserUploadedMetadata(Workspace workspace) throws KarmaException {
		super(workspace);
	}

	@Override
	public void setup(UpdateContainer uc) {
		
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {

		return ContextParameter.USER_UPLOADED_DIR;
	}

	@Override
	protected String getDirectoryPath() {
		return "user-uploaded-files";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.USER_UPLOADED;
	}
}
