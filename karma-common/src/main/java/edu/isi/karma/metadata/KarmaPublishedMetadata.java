package edu.isi.karma.metadata;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class KarmaPublishedMetadata extends KarmaMetadata {

	private static final Logger logger = LoggerFactory.getLogger(KarmaUserMetadata.class);
	
	public KarmaPublishedMetadata(ServletContextParameterMap contextParameters) throws KarmaException
	{
		super(contextParameters);
	}

	protected void createDirectoryForMetadata(ServletContextParameterMap contextParameters, ContextParameter parameter, String directory) throws KarmaException {
		
		String metadataDirPath = contextParameters.getParameterValue(parameter);
		if(metadataDirPath == null || metadataDirPath.isEmpty())
		{
			String userDirPath = contextParameters.getParameterValue(ContextParameter.WEBAPP_PATH) + "/publish/";
			metadataDirPath = userDirPath + directory;
			contextParameters.setParameterValue(parameter, metadataDirPath);
			contextParameters.setParameterValue(getRelativeDirectoryContextParameter(), "publish/" + directory);
		}
		logger.info("Set parameter: " + parameter + " -> " + metadataDirPath);
		File metadataDir = new File(metadataDirPath);
		if(metadataDir.exists() && !metadataDir.isDirectory())
		{
			logger.error("Directory provided for " + parameter + " is actually a file!");
			throw new KarmaException("Directory provided for " + parameter + " is actually a file!");
		}
		if(!metadataDir.exists() && !metadataDir.mkdirs())
		{
			logger.error("Unable to create directory for metadata: " + parameter);
			throw new KarmaException("Unable to create directory for metadata! " + parameter.name());
		}
	
	}
	
	protected abstract ContextParameter getRelativeDirectoryContextParameter();
	
}
