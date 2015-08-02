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

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.util.Preferences;

/**
 * @author szekely
 * 
 */
public class CommandPreferences extends Preferences{
	
	public CommandPreferences(String preferencesId, String contextId) {
		super(preferencesId, contextId);
	}	
	
	public JSONObject getCommandPreferencesJSONObject(String commandName){
		try {
			JSONArray commArray = json.getJSONArray("Commands");
			for(int i=0; i<commArray.length(); i++) {
				JSONObject obj = commArray.getJSONObject(i);
				if(obj.getString("Command").equals(commandName)) {
					return obj.getJSONObject("PreferenceValues");
				}
			}
		} catch (JSONException e) {
			return null;
		}
		return null;
	}
	
	public void setCommandPreferences(String commandName, JSONObject prefValues) {
		try {
			JSONArray commArray = null;
			
			// Check if the Commands element exists
			commArray = json.optJSONArray("Commands");
			if(commArray==null)	
				commArray = new JSONArray();
			
			// Check if the command already exists. In that case, we overwrite the values
			for(int i=0; i<commArray.length(); i++) {
				JSONObject obj = commArray.getJSONObject(i);
				if(obj.getString("Command").equals(commandName)) {
					obj.put("PreferenceValues", prefValues);
					// Save the new preferences to the file
					this.savePreferences();
					return;
				}
			}
			
			// If the command does not exists, create a new element
			JSONObject commObj = new JSONObject();
			commObj.put("Command", commandName);
			commObj.put("PreferenceValues", prefValues);
			commArray.put(commObj);
			json.put("Commands", commArray);
			
			// Write the new preferences to the file
			this.savePreferences();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
