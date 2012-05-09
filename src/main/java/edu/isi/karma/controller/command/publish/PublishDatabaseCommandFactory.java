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
package edu.isi.karma.controller.command.publish;

import javax.servlet.http.HttpServletRequest;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishDatabaseCommandFactory extends CommandFactory {
	private enum Arguments {
		vWorksheetId, overwriteTable, insertTable, dbType,hostName,port,dbName,userName,password,tableName
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			VWorkspace vWorkspace) {
		String vWorksheetId = request.getParameter(Arguments.vWorksheetId
				.name());

		System.out.println("host="+request.getParameter(Arguments.hostName.name()));
		
		PublishDatabaseCommand comm = new PublishDatabaseCommand(getNewId(vWorkspace), vWorksheetId,
				request.getParameter(Arguments.dbType.name()),
				request.getParameter(Arguments.hostName.name()),
				request.getParameter(Arguments.port.name()),
				request.getParameter(Arguments.dbName.name()),
				request.getParameter(Arguments.userName.name()),
				request.getParameter(Arguments.password.name()),
				request.getParameter(Arguments.tableName.name()),
				request.getParameter(Arguments.overwriteTable.name()),
				request.getParameter(Arguments.insertTable.name()));
		
		return comm;
	}

}
