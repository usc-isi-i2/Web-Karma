package edu.isi.karma.metadata;

import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class AvroMetadata extends KarmaPublishedMetadata {
	
	public AvroMetadata(ServletContextParameterMap contextParameters)
			throws KarmaException {
		super(contextParameters);
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {
		return ContextParameter.AVRO_PUBLISH_DIR;
	}

	@Override
	protected ContextParameter getRelativeDirectoryContextParameter() {
		return ContextParameter.AVRO_PUBLISH_RELATIVE_DIR;
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
