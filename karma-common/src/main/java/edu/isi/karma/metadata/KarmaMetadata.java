package edu.isi.karma.metadata;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class KarmaMetadata {

	protected Workspace workspace;
	
	public KarmaMetadata(Workspace workspace) throws KarmaException
	{
		this.workspace = workspace;
		createDirectoryForMetadata(getDirectoryContextParameter(), getDirectoryPath());
	}
	
	public abstract void setup(UpdateContainer uc);
	protected abstract ContextParameter getDirectoryContextParameter();
	protected abstract String getDirectoryPath();
	
	protected abstract void createDirectoryForMetadata(ContextParameter parameter, String directory) throws KarmaException;
	public abstract KarmaMetadataType getType();
}
