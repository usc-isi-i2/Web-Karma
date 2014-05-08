package edu.isi.karma.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;


public class TestFileJSONGenerator extends TestRdfGenerator{
	
	@Test
	public void testScheduleRDFPyTranform() {
		try {

			String filename = "schedule.csv";
			System.out.println("Load file: " + filename);
			
			FileRdfGenerator rdfGen = new FileRdfGenerator();
			R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
					"schedule-model", getTestResource("schedule-model.txt"));
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			rdfGen.generateRdf("csv", modelIdentifier, pw, new File(getTestResource(filename).toURI()), 
					"utf-8", 0);
			
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			assertEquals(275, lines.length);
			
			int idx = rdf.indexOf("hasEventDate> \"2014-01-13\" .");
			assertNotEquals(idx, -1);
			
//			System.out.println("------------------------------------------");
//			System.out.println(rdf);
//			System.out.println("------------------------------------------");
		} catch (Exception e) {
			fail("Exception: " + e.getMessage());
		}
	}
	
	
	@Test
	public void testCWeb2RDFPyTransform() {
		//
		try {
			String filename = "cbev2.WebConAltNames.csv";
			System.out.println("Load file: " + filename);
			
			FileRdfGenerator rdfGen = new FileRdfGenerator();
			R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
					"cbev2.WebConAltNames-model", getTestResource("cbev2.WebConAltNames-model.ttl"));
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			rdfGen.generateRdf("csv", modelIdentifier, pw, new File(getTestResource(filename).toURI()), 
					"utf-8", 0);
			
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			assertEquals(599, lines.length);
			
			String triple = "<http://collection.americanart.si.edu/id/person-constituent/2> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.cidoc-crm.org/cidoc-crm/E21_Person>";
			int idx = rdf.indexOf(triple);
			assertNotEquals(idx, -1);
			
//			System.out.println("------------------------------------------");
//			System.out.println(rdf);
//			System.out.println("------------------------------------------");
		} catch (Exception e) {
			fail("Exception: " + e.getMessage());
		}
	}

	private URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
	
}
