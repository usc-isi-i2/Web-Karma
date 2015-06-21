package edu.isi.karma.metadata;

import java.io.File;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PythonTransformationMetadata extends KarmaUserMetadata {

	
	public PythonTransformationMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "python" + File.separator;
	}
	@Override
	public KarmaMetadataType getType() {
		return PythonUserMetadataTypes.USER_PYTHON_SCRIPTS;
	}
	
	public enum PythonUserMetadataTypes implements KarmaMetadataType
	{USER_PYTHON_SCRIPTS
	
	}
}
