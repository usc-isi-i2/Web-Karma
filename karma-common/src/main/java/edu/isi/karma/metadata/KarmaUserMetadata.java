package edu.isi.karma.metadata;

import java.io.File;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class KarmaUserMetadata {

	protected Workspace workspace;
	public KarmaUserMetadata(Workspace workspace) throws KarmaException
	{
		this.workspace = workspace;
		createDirectoryForMetadata(getDirectoryContextParameter(), getDirectoryPath());
	}
	public abstract void setup() throws KarmaException;
	protected abstract ContextParameter getDirectoryContextParameter();
	protected abstract String getDirectoryPath();
	protected void createDirectoryForMetadata(ContextParameter parameter, String directory) throws KarmaException {
		
		String metadataDirPath = ServletContextParameterMap.getParameterValue(parameter);
		if(metadataDirPath == null || metadataDirPath.isEmpty())
		{
			String userDirPath = ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH);
			metadataDirPath = userDirPath + directory;
			ServletContextParameterMap.setParameterValue(parameter, metadataDirPath);
		}
		File metadataDir = new File(metadataDirPath);
		if(metadataDir.exists() && !metadataDir.isDirectory())
		{
			throw new KarmaException("Directory provided for crf models is actually a file!");
		}
		if(!metadataDir.exists())
		{
			if(!metadataDir.mkdirs())
			{
				throw new KarmaException("Unable to create directory for metadata! " + parameter.name());
			}
		}
	
	}
	public abstract KarmaUserMetadataType getType();
}
