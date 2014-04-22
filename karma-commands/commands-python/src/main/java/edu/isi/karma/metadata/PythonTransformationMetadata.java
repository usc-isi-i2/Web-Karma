package edu.isi.karma.metadata;

import java.io.File;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
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
	public void setup() throws KarmaException {
		String directory = ServletContextParameterMap.getParameterValue(getDirectoryContextParameter());
		StringBuilder transformationDirectoryBuilder = new StringBuilder();
		
		transformationDirectoryBuilder.append(directory);
		transformationDirectoryBuilder.append(File.separator);
		transformationDirectoryBuilder.append("karma");
		transformationDirectoryBuilder.append(File.separator);
		String karmaDirectory = transformationDirectoryBuilder.toString();
		transformationDirectoryBuilder.append("transformation");
		transformationDirectoryBuilder.append(File.separator);
		String transformationDirectory = transformationDirectoryBuilder.toString();
		createDirectory(transformationDirectory);
		createFile(directory + "__init__.py");
		createFile(karmaDirectory + "__init__.py");
		createFile(transformationDirectory + "__init__.py");
		
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
