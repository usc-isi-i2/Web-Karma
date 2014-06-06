package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class AugmentDataCommandFactory extends CommandFactory{
	
	private enum Arguments {
		worksheetId, predicate, triplesMap, alignmentId, columnUri, otherClass, tripleStoreUrl
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		// TODO Auto-generated method stub
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String predicate = request.getParameter(Arguments.predicate.name());
		String triplesMap = request.getParameter(Arguments.triplesMap.name());
		String alignmentId = request.getParameter(Arguments.alignmentId.name());
		String columnUri = request.getParameter(Arguments.columnUri.name());
		String otherClass = request.getParameter(Arguments.otherClass.name());
		String dataRepoUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		return new AugmentDataCommand(getNewId(workspace), dataRepoUrl, worksheetId, columnUri, alignmentId, predicate, triplesMap, otherClass);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// 
		return AugmentDataCommand.class;
	}

}
