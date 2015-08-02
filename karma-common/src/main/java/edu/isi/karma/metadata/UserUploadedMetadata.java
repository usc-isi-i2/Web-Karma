package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class UserUploadedMetadata extends KarmaUserMetadata{

	public UserUploadedMetadata(ServletContextParameterMap contextParameters) throws KarmaException
	{
		super(contextParameters);
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
