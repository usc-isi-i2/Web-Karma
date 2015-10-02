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
/**
 * 
 */
package edu.isi.karma.controller.command;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

/**
 * @author szekely
 * 
 */
public abstract class CommandFactory {

	public abstract Command createCommand(HttpServletRequest request,
			Workspace workspace);

	public String getWorksheetId(HttpServletRequest request,
			Workspace workspace) {
		return request.getParameter("worksheetId");
	}
	
	protected String getNewId(Workspace workspace){
		return workspace.getFactory().getNewId("C");
	}

	/* this is a hack to get around instanceof jsoninputcommandfactory */
	public Command createCommand(JSONArray inputParamArr, String model, Workspace workspace) throws JSONException, KarmaException
	{
		throw new UnsupportedOperationException("This is not supported on this command factory!");
	}

	public abstract Class<? extends Command> getCorrespondingCommand();
}
