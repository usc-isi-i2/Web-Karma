package edu.isi.karma.metadata;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class KarmaMetadataManager {

	private Map<KarmaMetadataType, KarmaMetadata> metadataTypes;
	public KarmaMetadataManager() throws KarmaException
	{
		createDirectoryForMetadata();
		metadataTypes = new HashMap<KarmaMetadataType, KarmaMetadata>();
		
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
	public void register(KarmaMetadata metadata, UpdateContainer uc) throws KarmaException
	{
		metadata.setup(uc);
		metadataTypes.put(metadata.getType(), metadata);
	}
	
	public boolean isMetadataSupported(KarmaMetadataType type)
	{
		return metadataTypes.containsKey(type);
	}

}
