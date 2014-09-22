package edu.isi.karma.rdf;

import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;

public class TestLiteralNodeCSVRDFGenerator extends TestCSVRDFGenerator{
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
		rdfGen = new GenericRDFGenerator(null);

		// Add the models in
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier("names.csv-model-new",
				 getTestResource("names.csv-model-new.ttl")
						);
		rdfGen.addModel(modelIdentifier);
	
	}
	@Test
	public void testGenerateWithLiteralNode() {
		try {
			executeBasicCSVTest("names.csv", "names.csv-model-new", false, 11);
		} catch (Exception e) {
			logger.error("testGenerateWithLiteralNode failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}
}
