package edu.isi.karma.metadata;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class KarmaUserMetadataManager {

	private Map<KarmaUserMetadataType, KarmaUserMetadata> metadataTypes;
	public KarmaUserMetadataManager() throws KarmaException
	{
		createDirectoryForMetadata();
		metadataTypes = new HashMap<KarmaUserMetadataType, KarmaUserMetadata>();
		
	}
	private void createDirectoryForMetadata() throws KarmaException {
		String userDirPath = ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH);
		
		File userDir = new File(userDirPath);
		if(userDir.exists() && !userDir.isDirectory())
		{
			throw new KarmaException("Directory provided for user preferences is actually a file!");
		}
		if(!userDir.exists())
		{
			if(!userDir.mkdirs())
			{
				throw new KarmaException("Unable to create directory for KARMA_WORK_HOME.  Please define the environment variable KARMA_WORK_HOME to save preferences and Karma's learning");
			}
		}
	}
	public void register(KarmaUserMetadata metadata) throws KarmaException
	{
		metadata.setup();
		metadataTypes.put(metadata.getType(), metadata);
	}
	
	public boolean isMetadataSupported(KarmaUserMetadataType type)
	{
		return metadataTypes.containsKey(type);
	}
}
