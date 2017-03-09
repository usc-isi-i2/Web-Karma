package edu.isi.karma.util;

import java.io.IOException;
import java.util.HashMap;

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
		String json_line = "{\"a\": [\"Vulnerability\"], \"dateRecorded\": [\"2017-02-10T12:13:51\",\"2017-02-09T18:13:51\"], \"description\": \"java/org/apache/catalina/authenticator/FormAuthenticator.java in the form authentication feature in Apache Tomcat 6.0.21 through 6.0.36 and 7.x before 7.0.33 does not properly handle the relationships between authentication requirements and sessions, which allows remote attackers to inject a request into a session by sending this request during completion of the login form, a variant of a session fixation attack.\", \"publisher\": \"hg-cve\", \"uri\": \"http://effect.isi.edu/data/vulnerability/CVE-2013-2067\", \"source\": \"hg-cve-2A7DF54A\", \"hasCVSS\": \"http://effect.isi.edu/data/vulnerability/CVE-2013-2067/scoring\", \"vulnerabilityOf\": [\"cpe:/a:apache:tomcat:7.0.18\", \"cpe:/a:apache:tomcat:6.0.33\", \"cpe:/a:apache:tomcat:7.0.0:beta\", \"cpe:/a:apache:tomcat:7.0.19\", \"cpe:/a:apache:tomcat:6.0.31\", \"cpe:/a:apache:tomcat:7.0.30\", \"cpe:/a:apache:tomcat:6.0.32\", \"cpe:/a:apache:tomcat:7.0.14\", \"cpe:/a:apache:tomcat:7.0.32\", \"cpe:/a:apache:tomcat:7.0.15\", \"cpe:/a:apache:tomcat:6.0.30\", \"cpe:/a:apache:tomcat:7.0.16\", \"cpe:/a:apache:tomcat:7.0.17\", \"cpe:/a:apache:tomcat:7.0.10\", \"cpe:/a:apache:tomcat:7.0.0\", \"cpe:/a:apache:tomcat:7.0.11\", \"cpe:/a:apache:tomcat:7.0.12\", \"cpe:/a:apache:tomcat:7.0.13\", \"cpe:/a:apache:tomcat:7.0.4\", \"cpe:/a:apache:tomcat:7.0.3\", \"cpe:/a:apache:tomcat:6.0.35\", \"cpe:/a:apache:tomcat:7.0.2\", \"cpe:/a:apache:tomcat:6.0.36\", \"cpe:/a:apache:tomcat:7.0.1\", \"cpe:/a:apache:tomcat:7.0.7\", \"cpe:/a:apache:tomcat:7.0.8\", \"cpe:/a:apache:tomcat:7.0.5\", \"cpe:/a:apache:tomcat:7.0.6\", \"cpe:/a:apache:tomcat:7.0.9\", \"cpe:/a:apache:tomcat:7.0.4:beta\", \"cpe:/a:apache:tomcat:6.0.21\", \"cpe:/a:apache:tomcat:7.0.28\", \"cpe:/a:apache:tomcat:7.0.25\", \"cpe:/a:apache:tomcat:6.0.28\", \"cpe:/a:apache:tomcat:7.0.23\", \"cpe:/a:apache:tomcat:6.0.29\", \"cpe:/a:apache:tomcat:7.0.21\", \"cpe:/a:apache:tomcat:7.0.22\", \"cpe:/a:apache:tomcat:6.0.24\", \"cpe:/a:apache:tomcat:7.0.20\", \"cpe:/a:apache:tomcat:6.0.26\", \"cpe:/a:apache:tomcat:6.0.27\", \"cpe:/a:apache:tomcat:7.0.2:beta\"], \"name\": [\"CVE-2013-2067\"]}";
		JSONObject json1 = (JSONObject)parser.parse(json_line);
		JSONObject json2 = (JSONObject)parser.parse(json_line);
		
		//Change the dateRecorded for json2
		json2.put("dateRecorded", "2017-02-09T16:13:51");
		json2.put("source", "hg-cve-FFFFFFFF");
		JSONObject result = JSONLDUtilSimple.mergeJSONObjects(json1, json2, provProperties);
		
		//dateRecorded should be min/max. source should be only for json1 as we have not added any new data
		assert(result.get("dateRecorded").toString().equals("[\"2017-02-09T16:13:51\",\"2017-02-10T12:13:51\"]"));
		assert(result.get("source").toString().equals("hg-cve-2A7DF54A"));
		
		//Add a new field to json2. The source should get added as we have added new data
		json2.put("tempField", "test");
		result = JSONLDUtilSimple.mergeJSONObjects(json1, json2, provProperties);
		assert(result.get("dateRecorded").toString().equals("[\"2017-02-09T16:13:51\",\"2017-02-10T12:13:51\"]"));
		assert(result.get("source").toString().equals("[\"hg-cve-2A7DF54A\",\"hg-cve-FFFFFFFF\"]"));
	}
	
	
}
