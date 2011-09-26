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
		assertEquals(maxCharactersInHeader, vPref.getMaxCharactersInHeader());
		
		// Testing if values are set properly
		vPref.setMaxCharactersInHeader(40);
		assertEquals(40, vPref.getMaxCharactersInHeader());
		
		// Check if the file is being generated properly
		File prefFile = new File("./UserPrefs/" + ws.getId() + ".json");
		JSONObject newPrefObj = (JSONObject) Util.createJson(new FileReader(prefFile));
		assertEquals(40, newPrefObj.getJSONObject("ViewPreferences").getInt("maxCharactersInHeader"));
	}

}
