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
package edu.isi.karma.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.util.FileUtil;
import edu.isi.karma.util.JSONUtil;

/**
 * @author szekely
 * 
 */
public class ViewPreferences {
	
	/**
	 * Pointer to the file where the preferences for each workspace is saved.
	 */
	private File jsonFile;
	
	/**
	 * Id of the workspace. Each workspace has its own view preference object.
	 */
	private String preferencesId;
	
	private JSONObject json;
	
	private static Logger logger = LoggerFactory.getLogger(ViewPreferences.class.getSimpleName());
	
	public enum ViewPreference {
		maxCharactersInHeader, maxCharactersInCell, maxRowsToShowInNestedTables, defaultRowsToShowInTopTables;
		
		public int getIntDefaultValue() {
			switch(this) {
			case maxCharactersInHeader: return 10;
			case maxCharactersInCell: return 100;
			case maxRowsToShowInNestedTables: return 5;
			case defaultRowsToShowInTopTables: return 10;
			}
			return -1;
		}
	}

	public ViewPreferences(String preferencesId) {
		this.preferencesId = preferencesId;
		populatePreferences();
	}
	
	private void populatePreferences() {
		try {
			// TODO Make this path to user preferences configurable through web.xml
			jsonFile = new File("./UserPrefs/" + preferencesId + ".json");
			if(jsonFile.exists()){
				// Populate from the existing preferences JSON file
				json = (JSONObject) JSONUtil.createJson(new FileReader(jsonFile));
				if(json == null) {
					// If error occurred with preferences file, create a new one
					logger.error("Preferences file corrupt! Creating new from template.");
					createNewPreferencesFileFromTemplate();
				}
			} else {
				// Create a new JSON preference file using the template preferences file
				createNewPreferencesFileFromTemplate();
			} 
		} catch(FileNotFoundException f) {
			logger.error("Preferences file not found! ", f);
		} catch (IOException e) {
			logger.error("Error occured while creating preferences file!", e);
		}
	}
	
	private void createNewPreferencesFileFromTemplate() throws IOException {
		jsonFile.createNewFile();
		File template_file = new File("./UserPrefs/WorkspacePref.template");
		FileUtil.copyFiles(jsonFile, template_file);
		json = (JSONObject) JSONUtil.createJson(new FileReader(jsonFile));
	}

	public int getIntViewPreferenceValue(ViewPreference pref) {
		try {
			return json.getJSONObject("ViewPreferences").getInt(pref.name());
		} catch (JSONException e) {
			logger.info("Preference key not found in the JSON Object. Going to add it ...");
			try {
				if(json.getJSONObject("ViewPreferences").optJSONObject(pref.name()) == null) {
					// Add it to the JSON Object and write it
					setIntViewPreferenceValue(pref, pref.getIntDefaultValue());
					logger.debug("New preference added to the user's file.");
					return pref.getIntDefaultValue();
				}
			} catch (JSONException e1) {
				logger.error("Error occured while adding a new key to the preferences JSON object!", e1);
			}
		}
		return -1;
	}
	
	
	public JSONObject getCommandPreferencesJSONObject(String commandName){
		System.out.println("Command name:" + commandName);
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
					FileUtil.writePrettyPrintedJSONObjectToFile(json, jsonFile);
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
			FileUtil.writePrettyPrintedJSONObjectToFile(json, jsonFile);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	public void setIntViewPreferenceValue(ViewPreference pref,
			int value) {
		try {
			json.getJSONObject("ViewPreferences").put(pref.name(), value);
			FileUtil.writePrettyPrintedJSONObjectToFile(json, jsonFile);
		} catch (JSONException e) {
			logger.error("Error setting int value!", e);
		} catch (IOException e) {
			logger.error("Error writing the changed preferences to file!" + jsonFile.getName(), e);
		}
	}
}
