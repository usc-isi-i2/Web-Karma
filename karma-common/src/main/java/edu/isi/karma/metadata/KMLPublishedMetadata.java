package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class KMLPublishedMetadata extends KarmaPublishedMetadata {
	
	public KMLPublishedMetadata(ServletContextParameterMap contextParameters)
			throws KarmaException {
		super(contextParameters);
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.KML_PUBLISH_DIR;
	}

	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.KML_PUBLISH_RELATIVE_DIR;
	}
	
	@Override
	protected String getDirectoryPath() {
		return "AVRO/";
	}

	@Override
	public KarmaMetadataType getType() {
		return StandardPublishMetadataTypes.AVRO;
	}

}
