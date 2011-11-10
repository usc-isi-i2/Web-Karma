package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;

public class SetSemanticTypeCommandFactory extends CommandFactory {

	public enum Arguments {
		vWorksheetId, hNodeId, type, domain, resourceType
	}
	
	private enum ResourceType {
		Class, DataProperty
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {

		String hNodeId = request.getParameter(Arguments.hNodeId.name());
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());
		String resourceType = request.getParameter(Arguments.resourceType
				.name());
		String type = request.getParameter(Arguments.type
				.name());
		String domain = "";	// Included with Data Property type resource only
		if(resourceType.equals(ResourceType.DataProperty.name())) {
			domain = request.getParameter(Arguments.domain
					.name());
		}
		
		return new SetSemanticTypeCommand(getNewId(vWorkspace), vWorksheetId,
				hNodeId, type, domain);
	}
}
