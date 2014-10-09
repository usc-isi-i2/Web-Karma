package edu.isi.karma.metadata;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class R2RMLMetadata extends KarmaUserMetadata {

	public R2RMLMetadata(Workspace workspace) throws KarmaException {
		super(workspace);
	}
	
	
	@Override
	public void setup(UpdateContainer uc) {
	
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.R2RML_USER_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "models-autosave/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.R2RML_MODEL;
	}


	


	

}
