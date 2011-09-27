package edu.isi.karma.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.JSONException;
import org.json.JSONObject;

import junit.framework.TestCase;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.Util;
import edu.isi.karma.view.ViewPreferences.ViewPreference;

public class ViewPreferencesTest extends TestCase {
	private RepFactory f;
	private ViewPreferences vPref;
	private Workspace ws;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.ws = f.createWorkspace();
		
		// Delete the old preference file
		File oldPrefFile = new File("./UserPrefs/" + ws.getId() + ".json");
		if(oldPrefFile.exists())
			oldPrefFile.delete();
		
		this.vPref = new ViewPreferences(ws.getId());
	}
	
	public void testPopulatePreferences() throws FileNotFoundException, JSONException {
		File templateFile = new File("./UserPrefs/WorkspacePref_template.json");
		JSONObject obj = (JSONObject) Util.createJson(new FileReader(templateFile));
		
		// Testing if a new preference file is generated with the right values
		int maxCharactersInHeader = obj.getJSONObject("ViewPreferences").getInt("maxCharactersInHeader");
		assertEquals(maxCharactersInHeader, vPref.getIntViewPreferenceValue(ViewPreference.maxCharactersInHeader));
		
		// Testing if values are set properly
		vPref.setIntViewPreferenceValue(ViewPreference.maxCharactersInHeader, 40);
		assertEquals(40, vPref.getIntViewPreferenceValue(ViewPreference.maxCharactersInHeader));
		
		// Check if the file is being generated properly
		File prefFile = new File("./UserPrefs/" + ws.getId() + ".json");
		JSONObject newPrefObj = (JSONObject) Util.createJson(new FileReader(prefFile));
		assertEquals(40, newPrefObj.getJSONObject("ViewPreferences").getInt("maxCharactersInHeader"));
		
		// Check if a property that is not in file but present in code is added to the updated 
		// preferences file. To make this test work, add a preference object to the ViewPreference
		// and its default value. Then try to get it and check if it is written to the file.
		
//		assertEquals(20, vPref.getIntViewPreferenceValue(ViewPreference.notInYet));
//		newPrefObj = (JSONObject) Util.createJson(new FileReader(prefFile));
//		assertEquals(20, newPrefObj.getJSONObject("ViewPreferences").getInt("notInYet"));
	}

}
