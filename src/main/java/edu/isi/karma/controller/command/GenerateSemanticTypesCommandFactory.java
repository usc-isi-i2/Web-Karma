package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class GenerateSemanticTypesCommandFactory extends CommandFactory{
	public enum Arguments {
		vWorksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String vWorksheetId =request.getParameter(Arguments.vWorksheetId.name());
		return new GenerateSemanticTypesCommand(getNewId(vWorkspace), vWorksheetId);
	}
}
