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
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences;

public class FetchPreferencesUpdate extends AbstractUpdate {
	VWorkspace vWorkspace;
	String commandName;

	private static Logger logger = LoggerFactory.getLogger(FetchPreferencesUpdate.class);
	
	public FetchPreferencesUpdate(VWorkspace vWorkspace, String commandName) {
		this.vWorkspace = vWorkspace;
		this.commandName = commandName;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		ViewPreferences prefs = vWorkspace.getPreferences();
		JSONObject prefObject = prefs.getCommandPreferencesJSONObject(commandName);
		JSONObject responseObj = new JSONObject();
		try {
			responseObj.put("commandId", vWorkspace.getWorkspace().getCommandHistory().getCurrentCommand().getId());
			responseObj.put("updateType", commandName);
			
			// Populate the preferences if there are any
			if(prefObject != null) {
				responseObj.put("PreferenceValues", prefObject);
			}
			pw.print(responseObj.toString());
		} catch (JSONException e) {
			logger.error("Error writing JSON values for the preference update!", e);
		}
	}

}
