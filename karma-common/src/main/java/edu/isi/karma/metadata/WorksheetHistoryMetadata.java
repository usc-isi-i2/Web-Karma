package edu.isi.karma.metadata;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class WorksheetHistoryMetadata extends KarmaUserMetadata{

	public WorksheetHistoryMetadata() throws KarmaException {
		super(null);
	}
	public WorksheetHistoryMetadata(Workspace workspace) throws KarmaException {
		super(workspace);
	}

	@Override
	public void setup() throws KarmaException {
		
	}

	@Override
	protected ContextParameter getDirectoryContextParameter() {

		return ContextParameter.WORKSHEET_HISTORY_DIRECTORY;
	}

	@Override
	protected String getDirectoryPath() {
		return "publish/History/";
	}
	@Override
	public KarmaUserMetadataType getType() {
		return StandardUserMetadataTypes.WORKSHEET_HISTORY;
	}
}
