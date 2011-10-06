package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.view.VWorkspace;

public class ImportDatabaseTableCommandFactory extends CommandFactory{
	
	public enum Arguments {
		dBType, hostname, portNumber, username, password, dBorSIDName, tableName
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		
		String interactionType = request.getParameter("interactionType");

		ImportDatabaseTableCommand comm = new ImportDatabaseTableCommand(getNewId(vWorkspace), vWorkspace);
		
		if(interactionType.equals(ImportDatabaseTableCommand.InteractionType.getPreferencesValues.name()))
			comm.setRequestedInteractionType(ImportDatabaseTableCommand.InteractionType.getPreferencesValues);
		return comm;
	}
}
