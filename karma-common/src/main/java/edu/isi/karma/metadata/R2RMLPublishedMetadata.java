package edu.isi.karma.metadata;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class R2RMLPublishedMetadata extends KarmaPublishedMetadata {

	public R2RMLPublishedMetadata(Workspace workspace) throws KarmaException {
		super(workspace);
	}
	
	
	@Override
	public void setup(UpdateContainer uc) {
	
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.R2RML_PUBLISH_DIR;
	}
	
	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.R2RML_PUBLISH_RELATIVE_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "R2RML/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.R2RML_MODEL;
	}

}
