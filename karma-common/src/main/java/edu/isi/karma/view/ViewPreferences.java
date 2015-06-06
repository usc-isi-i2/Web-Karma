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

import java.io.IOException;

import org.json.JSONException;

import edu.isi.karma.util.Preferences;

/**
 * @author szekely
 * 
 */
public class ViewPreferences extends Preferences{
	
	public ViewPreferences(String preferencesId,String contextId) {
		super(preferencesId, contextId);
	}

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


	public int getIntViewPreferenceValue(ViewPreference pref) {
		try {
			return json.getJSONObject("ViewPreferences").getInt(pref.name());
		} catch (JSONException e) {
			Preferences.logger.info("Preference key not found in the JSON Object. Going to add it ...");
			try {
				if(json.getJSONObject("ViewPreferences").optJSONObject(pref.name()) == null) {
					// Add it to the JSON Object and write it
					setIntViewPreferenceValue(pref, pref.getIntDefaultValue());
					Preferences.logger.debug("New preference added to the user's file.");
					return pref.getIntDefaultValue();
				}
			} catch (JSONException e1) {
				Preferences.logger.error("Error occured while adding a new key to the preferences JSON object!", e1);
			}
		}
		return -1;
	}
	
	public void setIntViewPreferenceValue(ViewPreference pref,
			int value) {
		try {
			json.getJSONObject("ViewPreferences").put(pref.name(), value);
			this.savePreferences();
		} catch (JSONException e) {
			Preferences.logger.error("Error setting int value!", e);
		} catch (IOException e) {
			Preferences.logger.error("Error writing the changed preferences to file!" + jsonFile.getName(), e);
		}
	}
}
