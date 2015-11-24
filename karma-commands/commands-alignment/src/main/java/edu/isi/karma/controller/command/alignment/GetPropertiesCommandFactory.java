package edu.isi.karma.controller.command.alignment;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.alignment.GetPropertiesCommand.INTERNAL_PROP_RANGE;
import edu.isi.karma.rep.Workspace;

public class GetPropertiesCommandFactory extends CommandFactory {

	public enum Arguments {
		worksheetId, propertiesRange, classURI, domainURI, rangeURI, linkId
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String domainURI = request.getParameter(Arguments.domainURI.name());
		String rangeURI = request.getParameter(Arguments.rangeURI.name());
		String classURI = request.getParameter(Arguments.classURI.name());
		String linkId = request.getParameter(Arguments.linkId.name());
		String worksheetId =request.getParameter(Arguments.worksheetId.name());
		INTERNAL_PROP_RANGE range = INTERNAL_PROP_RANGE.valueOf(
				request.getParameter(Arguments.propertiesRange.name()));
		
		return new GetPropertiesCommand(getNewId(workspace), 
				Command.NEW_MODEL, worksheetId, range, classURI, domainURI, rangeURI, linkId);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return GetPropertiesCommand.class;
	}

}
