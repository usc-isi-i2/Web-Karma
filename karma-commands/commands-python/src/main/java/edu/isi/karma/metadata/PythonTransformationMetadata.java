package edu.isi.karma.metadata;

import java.io.File;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PythonTransformationMetadata extends KarmaUserMetadata {

	
	public PythonTransformationMetadata(Workspace workspace) throws KarmaException
	{
		super(workspace);
	}
	
	public PythonTransformationMetadata() throws KarmaException
	{
		super(null);
	}
	
	@Override
	public void setup(UpdateContainer uc) {
		
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
