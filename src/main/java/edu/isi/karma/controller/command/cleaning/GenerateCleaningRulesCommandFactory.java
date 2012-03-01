package edu.isi.karma.controller.command.cleaning;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;

public class GenerateCleaningRulesCommandFactory extends CommandFactory {
	
	private enum Arguments {
		vWorksheetId, hNodeId, examples
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String examples = request.getParameter(Arguments.examples.name());
		
		System.out.println("Examples:" + examples);
		return new GenerateCleaningRulesCommand(getNewId(vWorkspace), getWorksheetId(request, vWorkspace), hNodeId);
	}
}