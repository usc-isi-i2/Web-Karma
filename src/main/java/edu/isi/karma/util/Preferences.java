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

package edu.isi.karma.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class Preferences {
	/**
	 * Pointer to the file where the preferences for each workspace is saved.
	 */
	private File jsonFile;
	
	/**
	 * Id of the workspace. Each workspace has its own view preference object.
	 */
	protected String preferencesId;
	
	protected JSONObject json;
	
	protected static Logger logger = LoggerFactory.getLogger(Preferences.class.getSimpleName());

	public Preferences(String preferencesId) {
		this.preferencesId = preferencesId;
		populatePreferences();
	}

	private void populatePreferences() {
		try {
			if(ModelingConfiguration.getManualAlignment()) {
				loadDefaultPreferences();
			} else {
				jsonFile = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + 
						"UserPrefs/" + preferencesId + ".json");
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
			}
		} catch(FileNotFoundException f) {
			logger.error("Preferences file not found! ", f);
		} catch (IOException e) {
			logger.error("Error occured while creating preferences file!", e);
		}
	}

	
	private void createNewPreferencesFileFromTemplate() throws IOException {
		jsonFile.createNewFile();
		File template_file = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + 
				"UserPrefs/WorkspacePref.template");
		FileUtil.copyFiles(jsonFile, template_file);
		json = (JSONObject) JSONUtil.createJson(new FileReader(jsonFile));
	}
	
	private void loadDefaultPreferences() throws IOException {
		jsonFile = new File(ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + 
				"UserPrefs/WorkspacePref.template");
		json = (JSONObject) JSONUtil.createJson(new FileReader(jsonFile));
	}
	
	protected void savePreferences() throws JSONException, IOException {
		if(!ModelingConfiguration.getManualAlignment()) {
			FileUtil.writePrettyPrintedJSONObjectToFile(json, jsonFile);
		}
	}
}
