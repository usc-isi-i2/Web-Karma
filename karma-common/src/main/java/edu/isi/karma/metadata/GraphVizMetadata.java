package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GraphVizMetadata extends KarmaUserMetadata {

	
	public GraphVizMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
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
