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

import edu.isi.karma.controller.command.importdata.ImportServiceCommand.PreferencesKeys;
import edu.isi.karma.view.VWorkspace;

public class ImportServiceCommandPreferencesUpdate extends AbstractUpdate {
	private static Logger logger = LoggerFactory.getLogger(ImportServiceCommandPreferencesUpdate.class);
	private final String serviceUrl;
	private final String worksheetName;

	public ImportServiceCommandPreferencesUpdate(String serviceUrl, String worksheetName)
	{
		this.serviceUrl = serviceUrl;
		this.worksheetName = worksheetName;
	}
	
	@Override
	public void applyUpdate(VWorkspace vWorkspace)
	{
		savePreferences(vWorkspace);
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(GenericJsonKeys.updateType.name(), "ImportServiceCommandPreferencesUpdate");
			pw.println(obj.toString());
		} catch (JSONException e) {
			logger.error("Unable to generate Json", e);
		}
		
	}

	private void savePreferences(VWorkspace vWorkspace){
		try{
			JSONObject prefObject = new JSONObject();
			prefObject.put(PreferencesKeys.ServiceUrl.name(), serviceUrl);
			prefObject.put(PreferencesKeys.WorksheetName.name(), worksheetName);
			vWorkspace.getWorkspace().getCommandPreferences().setCommandPreferences(
					"ImportServiceCommandPreferences", prefObject);
			
			/*
			logger.debug("I Saved .....");
			ViewPreferences prefs = vWorkspace.getPreferences();
			JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishDatabaseCommandPreferences");
			logger.debug("I Saved ....."+prefObject1);
			 */
			
		} catch (JSONException e) {
			logger.error("Error in saving preferences", e);
		}
	}
	
}
