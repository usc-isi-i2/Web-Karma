package edu.isi.karma.metadata;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class CSVMetadata extends KarmaPublishedMetadata {

	public CSVMetadata(Workspace workspace) throws KarmaException {
		super(workspace);
	}
	
	
	@Override
	public void setup(UpdateContainer uc) {
	
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.CSV_PUBLISH_DIR;
	}

	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.CSV_PUBLISH_RELATIVE_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "CSV/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.CSV;
	}


}
