package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class CSVMetadata extends KarmaPublishedMetadata {

	public CSVMetadata(ServletContextParameterMap contextParameters) throws KarmaException {
		super(contextParameters);
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
