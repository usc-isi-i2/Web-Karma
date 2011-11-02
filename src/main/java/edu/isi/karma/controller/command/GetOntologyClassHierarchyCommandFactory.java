package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class GetOntologyClassHierarchyCommandFactory extends CommandFactory {

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		return new GetOntologyClassHierarchyCommand(getNewId(vWorkspace));
	}

}
