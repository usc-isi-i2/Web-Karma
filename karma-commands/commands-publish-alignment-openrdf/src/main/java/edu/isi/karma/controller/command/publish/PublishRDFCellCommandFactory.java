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

import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

@Deprecated
public class PublishRDFCellCommandFactory extends CommandFactory {
	private enum Arguments {
		worksheetId,
		nodeId
	}

	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId
				.name());
		//get the rdf prefix from the preferences
		JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
		String rdfNamespace = "http://localhost/source/";
		String rdfPrefix = "s";
		if(prefObject!=null){
			rdfPrefix = prefObject.optString("rdfPrefix");
		}
		if(rdfPrefix==null || rdfPrefix.trim().isEmpty())
			rdfPrefix = "http://localhost/source/"; 
		String nodeId = request.getParameter(Arguments.nodeId.name());
		return new PublishRDFCellCommand(getNewId(workspace), 
				Command.NEW_MODEL, worksheetId, nodeId,
				rdfPrefix,rdfNamespace);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return PublishRDFCellCommand.class;
	}
}
