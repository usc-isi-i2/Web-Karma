package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class AlignToOntologyCommandFactory extends CommandFactory {
	
	public enum Arguments {
		vWorksheetId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		return new AlignToOntologyCommand(getNewId(vWorkspace), getWorksheetId(request, vWorkspace));
	}

}
