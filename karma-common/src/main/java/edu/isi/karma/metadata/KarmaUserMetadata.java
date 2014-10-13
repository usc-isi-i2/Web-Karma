package edu.isi.karma.metadata;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
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
		String userDirPath = ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH);
		metadataDirPath = userDirPath + directory;
		ServletContextParameterMap.setParameterValue(parameter, metadataDirPath);
		
		logger.info("Set parameter: " + parameter + " -> " + metadataDirPath);
		try{ 
			renameExistingFolder(userDirPath, parameter, directory);
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
	
	
	private static void renameExistingFolder(String userDirPath, ContextParameter parameter, String directory) {
		
		HashMap<ContextParameter, String> oldNames = new HashMap<ContextParameter, String>();
		oldNames.put(ContextParameter.ALIGNMENT_GRAPH_DIRECTORY, "AlignmentGraph");
		oldNames.put(ContextParameter.JSON_MODELS_DIR, "JSON");
		oldNames.put(ContextParameter.R2RML_USER_DIR, "R2RML");
		oldNames.put(ContextParameter.SEMTYPE_MODEL_DIRECTORY, "SemanticTypeModels");
		oldNames.put(ContextParameter.USER_CONFIG_DIRECTORY, "Config");
		oldNames.put(ContextParameter.USER_PREFERENCES_DIRECTORY, "UserPrefs");
		oldNames.put(ContextParameter.USER_UPLOADED_DIR, "UserUploadedFiles");
		
		String oldName = oldNames.get(parameter);
		if (oldName == null)
			return;
		
		if (directory == null || directory.trim().isEmpty()) 
			return;
		
		File baseDir = new File(userDirPath);
		if (baseDir.exists()) {
			File f = new File(userDirPath + oldName);
			if (f.exists() && f.isDirectory()) {
				File newFile = new File(userDirPath + directory);
				if (!newFile.exists()) {
					try {
						FileUtils.moveDirectory(f, newFile);
						logger.info("changed name of the folder " + oldName + " to " + directory);
					} catch (Exception e) {
						logger.error("error in changing the name of the folder " + oldName + " to " + directory);
					}
				} else {
					if (!directory.equals(oldName)) {
						try {f.renameTo(newFile); } catch (Exception e) {}
					}
				}
			}
		}
		
	}
}
