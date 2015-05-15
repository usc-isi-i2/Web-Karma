package edu.isi.karma.controller.command.importdata;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class ImportSQLCommandFactory extends CommandFactory {
	 public enum Arguments {

	        dBType, hostname, portNumber, username, password, dBorSIDName, query
	    }

	    @Override
	    public Command createCommand(HttpServletRequest request,
	            Workspace workspace) {

	        String interactionType = request.getParameter("interactionType");

	        ImportSQLCommand comm = new ImportSQLCommand(getNewId(workspace), Command.NEW_MODEL);

	        if (request.getParameter("revisedWorksheet") != null) {
	            comm = new ImportSQLCommand(getNewId(workspace), request.getParameter("revisedWorksheet"));
	        }

	        if (interactionType.equals(ImportSQLCommand.InteractionType.getPreferencesValues.name())) {
	            comm.setRequestedInteractionType(ImportSQLCommand.InteractionType.getPreferencesValues);
	        }
	        return comm;
	    }

		@Override
		public Class<? extends Command> getCorrespondingCommand()
		{
			return ImportSQLCommand.class;
		}

}
