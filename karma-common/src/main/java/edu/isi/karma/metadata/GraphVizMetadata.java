package edu.isi.karma.metadata;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GraphVizMetadata extends KarmaUserMetadata {

	
	public GraphVizMetadata(Workspace workspace) throws KarmaException
	{
		super(workspace);
	}
	
	@Override
	public void setup(UpdateContainer uc) {
	
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.GRAPHVIZ_MODELS_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "models-graphviz/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardUserMetadataTypes.GRAPHVIZ_MODELS;
	}
}
