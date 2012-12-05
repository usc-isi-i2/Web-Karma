/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.cleaning.MyLogger;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandWithPreview;
import edu.isi.karma.controller.update.UpdateContainer;

public class RequestController extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(RequestController.class);
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String workspaceId = request.getParameter("workspaceId");
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(workspaceId);
		if (ctrl == null) {
			logger.debug("No execution controller found. This sometime happens when the server is restarted and " +
					"an already open window is refereshed (and is okay to happen). A command is sent to the server " +
					"to destroy all workspace objects.");
			return;
		}
		
		String responseString = "";
		/************collect info************/
		MyLogger xLogger = new MyLogger();
		String id = request.getSession().getId();
		MyLogger.user_id = id;
		/*************************************/
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
			Command command = ctrl.getCommand(request);
			if(command != null)
				responseString = ctrl.invokeCommand(command);
			else
				logger.error("Error occured while creating command (Could not create Command object): " + request.getParameter("command"));
		}
		
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(responseString);
		response.flushBuffer();
	}

}
