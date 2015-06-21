package edu.isi.karma.metadata;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class KarmaMetadata {

	protected ServletContextParameterMap contextParameters; 
	
	public KarmaMetadata(ServletContextParameterMap contextParameters) throws KarmaException
	{
		this.contextParameters = contextParameters;
		createDirectoryForMetadata(contextParameters, getDirectoryContextParameter(), getDirectoryPath());
	}
	
	protected abstract ContextParameter getDirectoryContextParameter();
	protected abstract String getDirectoryPath();
	
	protected abstract void createDirectoryForMetadata(ServletContextParameterMap contextParameters, ContextParameter parameter, String directory) throws KarmaException;
	public abstract KarmaMetadataType getType();

	public void setup(UpdateContainer uc, Workspace workspace)
	{
		
	}
}
