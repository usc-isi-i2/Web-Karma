package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;

public class GetPropertiesAndClassesListCommandFactory extends CommandFactory {

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		return new GetPropertiesAndClassesList(getNewId(vWorkspace));
	}

}
