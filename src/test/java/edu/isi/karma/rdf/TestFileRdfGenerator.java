package edu.isi.karma.rdf;

import static org.junit.Assert.*;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.isi.karma.kr2rml.R2RMLMappingIdentifier;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;


public class TestFileRdfGenerator {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initOfflineWorkspaceSettings();
	}
	
	
	@Test
	public void testScheduleRDFPyTranform() {
		try {

			String filename = getTestDataFolder() + "/schedule.csv";
			System.out.println("Load file: " + filename);
			
			FileRdfGenerator rdfGen = new FileRdfGenerator();
			R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
					"schedule-model", new File(getTestDataFolder()
							+ "/schedule-model.txt").toURI().toURL());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			rdfGen.generateRdf("csv", modelIdentifier, pw, new File(filename), 
					"utf-8", 0);
			
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split("\n");
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
			String filename = getTestDataFolder() + "/cbev2.WebConAltNames.csv";
			System.out.println("Load file: " + filename);
			
			FileRdfGenerator rdfGen = new FileRdfGenerator();
			R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
					"cbev2.WebConAltNames-model", new File(getTestDataFolder()
							+ "/cbev2.WebConAltNames-model.ttl").toURI().toURL());
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			
			rdfGen.generateRdf("csv", modelIdentifier, pw, new File(filename), 
					"utf-8", 0);
			
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split("\n");
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

	private String getRootFolder() {
		return getClass().getClassLoader().getResource(".").getPath()
				+ "/../../";
	}

	private String getTestDataFolder() {
		return getRootFolder() + "src/test/karma-data";
	}
	
	private static void initOfflineWorkspaceSettings() {
		/**
         * CREATE THE REQUIRED KARMA OBJECTS *
         */
        ServletContextParameterMap.setParameterValue(
                ContextParameter.USER_DIRECTORY_PATH, "src/main/webapp/");
        ServletContextParameterMap.setParameterValue(
                ContextParameter.TRAINING_EXAMPLE_MAX_COUNT, "200");

        SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
	}
}
