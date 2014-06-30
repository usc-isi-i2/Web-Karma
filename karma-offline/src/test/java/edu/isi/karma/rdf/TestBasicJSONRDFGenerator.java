package edu.isi.karma.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;

public class TestBasicJSONRDFGenerator extends TestJSONRDFGenerator {
	private static Logger logger = LoggerFactory.getLogger(TestBasicJSONRDFGenerator.class);
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		rdfGen = new GenericRDFGenerator();

		// Add the models in
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"people-model", getTestResource(
						 "people-model.ttl"));
		rdfGen.addModel(modelIdentifier);
		
		modelIdentifier = new R2RMLMappingIdentifier("cs548-events-model",
				 getTestResource("cs548-events-model.ttl")
						);
		rdfGen.addModel(modelIdentifier);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link edu.isi.karma.rdf.JSONRDFGenerator#generateRDF(java.lang.String, java.lang.String, boolean, java.io.PrintWriter)}
	 * .
	 */
	@Test
	public void testGenerateRDF1() {
		try {
			executeBasicJSONTest("people.json", "people-model", true, 102);
		} catch (Exception e) {
			logger.error("testGenerateRDF1 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}

	@Test
	public void testGenerateJSON1() {
		try {
			String filename = "people.json";
			logger.info("Loading json file: " + filename);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			JSONKR2RMLRDFWriter writer = new JSONKR2RMLRDFWriter(pw);
			rdfGen.generateRDF("people-model", new File(getTestResource(filename).toURI()), InputType.JSON, false, writer);
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split("(\r\n|\n)");
			int count = lines.length;
			
			assertEquals(155, count);
		} catch (Exception e) {
			logger.error("testGenerateRDF1 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}

	/**
	 * Test method for
	 * {@link edu.isi.karma.rdf.JSONRDFGenerator#generateRDF(java.lang.String, java.lang.String, boolean, java.io.PrintWriter)}
	 * .
	 */
	@Test
	public void testGenerateRDF2() {
		try {
			executeBasicJSONTest("cs548-events.json", "cs548-events-model", true, 238);
		} catch (Exception e) {
			logger.error("testGenerateRDF1 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}
}
