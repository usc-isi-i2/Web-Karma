package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ReportMetadata extends KarmaPublishedMetadata {


	public ReportMetadata(ServletContextParameterMap contextParameters) throws KarmaException
	{
		super(contextParameters);
	}
	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.REPORT_PUBLISH_DIR;
	}

	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.REPORT_PUBLISH_RELATIVE_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "REPORT/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.REPORT;
	}
}
