package edu.isi.karma.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandWithPreview;
import edu.isi.karma.controller.update.UpdateContainer;

public class RequestController extends HttpServlet{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(RequestController.class);
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String workspaceId = request.getParameter("workspaceId");
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspaceId);
		String responseString = "";
		
		// If the current request is a part of a command that requires user-interaction
		if(request.getParameter("commandId") != null && !request.getParameter("command").equals("UndoRedoCommand")) {
			Command currentCommand = ctrl.getvWorkspace().getWorkspace().getCommandHistory().getCurrentCommand();
			
			if(currentCommand != null && request.getParameter("commandId").equals(currentCommand.getId())){	
				// Check if the command needs to be executed
				if(request.getParameter("execute") != null && request.getParameter("execute").equals("true")){
					try {
						
						// Set the parameters if any changed after the user preview
						((CommandWithPreview) currentCommand).handleUserActions(request);
						
						UpdateContainer updateContainer = ctrl.getvWorkspace().getWorkspace()
						.getCommandHistory().doCommand(currentCommand, ctrl.getvWorkspace());
						
						responseString = updateContainer.generateJson(ctrl.getvWorkspace());
					} catch (CommandException e) {
						logger.error("Error occured while executing command: " + currentCommand.getCommandName(), e);
					}
				} else
					responseString = ((CommandWithPreview) currentCommand).handleUserActions(request)
							.generateJson(ctrl.getvWorkspace());
			}
		} else {
			responseString = ctrl.invokeCommand(ctrl.getCommand(request));
		}
		
		System.out.println(responseString);
		
		response.getWriter().write(responseString);
		response.flushBuffer();
	}

}
