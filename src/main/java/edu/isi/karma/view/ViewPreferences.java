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

	public ViewPreferences(String workspaceId) {
		this.workspaceId = workspaceId;
		populatePreferences();
	}
	
	public static void main(String[] args) {
		ViewPreferences vprf = new ViewPreferences("VWSP1");
		System.out.println(vprf.getDefaultRowsToShowInTopTables() + " " 
				+ vprf.getMaxCharactersInHeader() + " " + vprf.getMaxRowsToShowInNestedTables());
		vprf.setMaxCharactersInHeader(30);
		vprf.setDefaultRowsToShowInTopTables(50);
		vprf.setMaxRowsToShowInNestedTables(2);
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

	public int getMaxCharactersInHeader() {
		return getIntViewPreferenceValue("maxCharactersInHeader");
	}

	public void setMaxCharactersInHeader(int maxCharactersInHeader) {
		setViewPreferenceIntValue("maxCharactersInHeader", maxCharactersInHeader);
	}

	public int getMaxRowsToShowInNestedTables() {
		return getIntViewPreferenceValue("maxRowsToShowInNestedTables");
	}

	public void setMaxRowsToShowInNestedTables(int maxRowsToShowInNestedTables) {
		setViewPreferenceIntValue("maxRowsToShowInNestedTables", maxRowsToShowInNestedTables);
	}

	public int getDefaultRowsToShowInTopTables() {
		return getIntViewPreferenceValue("defaultRowsToShowInTopTables");
	}

	public void setDefaultRowsToShowInTopTables(int defaultRowsToShowInTopTables) {
		setViewPreferenceIntValue("defaultRowsToShowInTopTables", defaultRowsToShowInTopTables);
	}

	public int getIntViewPreferenceValue(String key) {
		try {
			return json.getJSONObject("ViewPreferences").getInt(key);
		} catch (JSONException e) {
			logger.error("Error getting int value!", e);
		}
		return -1;
	}
	
	private void setViewPreferenceIntValue(String key,
			int value) {
		try {
			json.getJSONObject("ViewPreferences").put(key, value);
			FileUtil.writePrettyPrintedJSONObjectToFile(json, jsonFile);
		} catch (JSONException e) {
			logger.error("Error setting int value!", e);
		} catch (IOException e) {
			logger.error("Error writing the changed preferences to file!" + jsonFile.getName(), e);
		}
	}
	
}
