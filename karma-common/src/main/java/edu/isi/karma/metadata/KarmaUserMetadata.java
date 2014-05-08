package edu.isi.karma.metadata;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class KarmaUserMetadata extends KarmaMetadata{

	private static final Logger logger = LoggerFactory.getLogger(KarmaUserMetadata.class);
	
	public KarmaUserMetadata(Workspace workspace) throws KarmaException
	{
		super(workspace);
	}
	
	protected void createDirectoryForMetadata(ContextParameter parameter, String directory) throws KarmaException {
		
		String metadataDirPath = ServletContextParameterMap.getParameterValue(parameter);
		if(metadataDirPath == null || metadataDirPath.isEmpty())
		{
			String userDirPath = ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH);
			metadataDirPath = userDirPath + directory;
			ServletContextParameterMap.setParameterValue(parameter, metadataDirPath);
		}
		logger.info("Set parameter: " + parameter + " -> " + metadataDirPath);
		try{ 
		
			createDirectory(metadataDirPath);
		}
		catch(KarmaException  e)
		{
			logger.error("Unable to create directory for " + parameter.name());
			throw new KarmaException("Unable to create directory for " + parameter.name(), e);
		}
	}

	protected void createDirectory(
			String metadataDirPath) throws KarmaException {
		File metadataDir = new File(metadataDirPath);
		if(metadataDir.exists() && !metadataDir.isDirectory())
		{
			throw new KarmaException("Directory provided is actually a file!" + metadataDirPath);
		}
		if(!metadataDir.exists())
		{
			if(!metadataDir.mkdirs())
			{
				throw new KarmaException("Unable to create directory for metadata! " + metadataDirPath);
			}
		}
	}
	protected void createFile(
			String metadataFilePath) throws KarmaException {
		File metadataFile = new File(metadataFilePath);
		if(!metadataFile.exists())
		{
			try{
				metadataFile.createNewFile();
			}
			catch (IOException e)
			{
				throw new KarmaException("Unable to create directory for metadata! " + metadataFilePath, e);
			}
		}
	}
}
