package edu.isi.karma.util;

import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.isi.karma.rep.Worksheet;


public class TestJSONLDUtilSimple {
	Worksheet worksheet;
	static String contextParametersId;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

	}
	
	@Before
	public void setUp() throws Exception {
		
	}
	@Test
	public void testProvenance() throws IOException, ParseException {
		HashMap<String, String> provProperties = new HashMap<String, String>();
		provProperties.put("source", "string");
		provProperties.put("publisher", "string");
		provProperties.put("dateRecorded", "date");
		
		JSONParser parser = new JSONParser();
		String json_line = "{\"a\": [\"Vulnerability\"], "
				+ "\"dateRecorded\": [\"2017-02-10T12:13:51\",\"2017-02-09T18:13:51\"], "
				+ "\"description\": \"d\", "
				+ "\"publisher\": \"hg-cve\", \"uri\": \"http://effect.isi.edu/data/vulnerability/CVE-2013-2067\", "
				+ "\"source\": \"hg-cve-2A7DF54A\", "
				+ "\"hasCVSS\": {\"uri\" :\"http://effect.isi.edu/data/vulnerability/CVE-2013-2067/scoring\"}, "
				+ "\"vulnerabilityOf\": [{\"uri\":\"cpe:/a:apache:tomcat:7.0.12\"}, {\"uri\": \"cpe:/a:apache:tomcat:6.0.27\"}], "
				+ "\"name\": [\"CVE-2013-2067\"]}";
		JSONObject json1 = (JSONObject)parser.parse(json_line);
		JSONObject json2 = (JSONObject)parser.parse(json_line);
		
		//Change the dateRecorded for json2
		json2.put("dateRecorded", "2017-02-09T16:13:51");
		json2.put("source", "hg-cve-FFFFFFFF");
		JSONObject result = JSONLDUtilSimple.mergeJSONObjects(json1, json2, provProperties);
		
		//dateRecorded should be min/max. source should be only for json1 as we have not added any new data
		assert(result.get("dateRecorded").toString().equals("[\"2017-02-09T16:13:51\",\"2017-02-10T12:13:51\"]"));
		assert(result.get("source").toString().equals("hg-cve-2A7DF54A"));
		
		//Add new vulnerability. It should not be added to source as its an object property
		JSONArray vulArr = (JSONArray)json2.get("vulnerabilityOf");
		JSONObject vul = new JSONObject();
		vul.put("uri", "cpe:/a:apache:tomcat:8.0.12");
		vulArr.add(vul);
		json2.put("vulnerabilityOf", vulArr);
		result = JSONLDUtilSimple.mergeJSONObjects(json1, json2, provProperties);
		assert(result.get("dateRecorded").toString().equals("[\"2017-02-09T16:13:51\",\"2017-02-10T12:13:51\"]"));
		assert(result.get("source").toString().equals("hg-cve-2A7DF54A"));
		
		//Add a new field to json2. The source should get added as we have added new data
		json2.put("tempField", "test");
		result = JSONLDUtilSimple.mergeJSONObjects(json1, json2, provProperties);
		assert(result.get("dateRecorded").toString().equals("[\"2017-02-09T16:13:51\",\"2017-02-10T12:13:51\"]"));
		assert(result.get("source").toString().equals("[\"hg-cve-2A7DF54A\",\"hg-cve-FFFFFFFF\"]"));
	}
	
	
}
