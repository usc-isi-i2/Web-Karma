package edu.isi.karma.metadata;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GraphVizMetadata extends KarmaPublishedMetadata {

	
	public GraphVizMetadata(Workspace workspace) throws KarmaException
	{
		super(workspace);
	}
	
	@Override
	public void setup(UpdateContainer uc) {
	
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.GRAPHVIZ_DIRECTORY;
	}

	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.GRAPHVIZ_RELATIVE_DIRECTORY;
	}
	
	@Override
	protected String getDirectoryPath() {
		return ModelingConfiguration.getModelsGraphvizDir();
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.GRAPHVIZ;
	}
}
