/**
 * 
 */
package edu.isi.karma.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.util.FileUtil;
import edu.isi.karma.util.Util;

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
	private String workspaceId;
	
	private JSONObject json;
	
	private static Logger logger = LoggerFactory.getLogger(ViewPreferences.class.getSimpleName());
	
	public enum ViewPreference {
		maxCharactersInHeader, maxRowsToShowInNestedTables, defaultRowsToShowInTopTables;
		
		public int getIntDefaultValue() {
			switch(this) {
			case maxCharactersInHeader: return 10;
			case maxRowsToShowInNestedTables: return 5;
			case defaultRowsToShowInTopTables: return 10;
			}
			return -1;
		}
	}

	public ViewPreferences(String workspaceId) {
		this.workspaceId = workspaceId;
		populatePreferences();
	}
	
	private void populatePreferences() {
		try {
			jsonFile = new File("./UserPrefs/" + workspaceId + ".json");
			if(jsonFile.exists()){
				// Populate from the existing preferences JSON file
				json = (JSONObject) Util.createJson(new FileReader(jsonFile));
			} else {
				// Create a new JSON preference file using the template preferences file
				jsonFile.createNewFile();
				File template_file = new File("./UserPrefs/WorkspacePref_template.json");
				FileUtil.copyFiles(jsonFile, template_file);
				json = (JSONObject) Util.createJson(new FileReader(jsonFile));
			} 
		} catch(FileNotFoundException f) {
			logger.error("Preferences file not found! ", f);
		} catch (IOException e) {
			logger.error("Error occured while creating preferences file!", e);
		}
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
