package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class SaveR2RMLModelCommandFactory extends CommandFactory{

	private enum Arguments {
		worksheetId, modelUrl, tripleStoreUrl, graphContext, collection
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		String modelUrl = request.getParameter(Arguments.modelUrl.name());
		String tripleStoreUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		String context = request.getParameter(Arguments.graphContext.name());
		String collection = request.getParameter(Arguments.collection.name());
		return new SaveR2RMLModelCommand(getNewId(workspace), 
				Command.NEW_MODEL, modelUrl, tripleStoreUrl, context, collection);
	}
	
	public SaveR2RMLModelCommand createCommand(String model, Workspace workspace, String modelUrl, String tripleStoreUrl, String context, String collection) {
		return new SaveR2RMLModelCommand(getNewId(workspace), model, modelUrl, tripleStoreUrl, context, collection);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
		// TODO Auto-generated method stub
		return SaveR2RMLModelCommand.class;
	}

}
