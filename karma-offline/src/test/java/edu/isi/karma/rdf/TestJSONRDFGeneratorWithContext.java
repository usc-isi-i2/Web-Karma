package edu.isi.karma.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;

public class TestJSONRDFGeneratorWithContext extends TestJSONRDFGenerator {
	private static Logger logger = LoggerFactory.getLogger(TestJSONRDFGeneratorWithContext.class);
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

		// Add the models in
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"events-model", getTestResource(
						 "context/events-no-augmentation-model.ttl"));
		rdfGen.addModel(modelIdentifier);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateJSONWithContext() {
		try {
			String filename = "context/events.json";
			String contextName = "context/events_context.json";
			logger.info("Loading json file: " + filename);
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			File contextFile =  new File(getTestResource(contextName).toURI());
			JSONTokener token = new JSONTokener(new FileInputStream(contextFile));
			ContextIdentifier contextId = new ContextIdentifier("events-context", contextFile.toURI().toURL());
			JSONKR2RMLRDFWriter writer = new JSONKR2RMLRDFWriter(pw);
			writer.setGlobalContext(new JSONObject(token), contextId);
			RDFGeneratorRequest request = new RDFGeneratorRequest("events-model", filename);
			request.setInputFile(new File(getTestResource(filename).toURI()));
			request.setAddProvenance(false);
			request.setDataType(InputType.JSON);
			request.addWriter(writer);
			rdfGen.generateRDF(request);
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split("(\r\n|\n)");
			int count = lines.length;
			
			assertEquals(318, count);
		} catch (Exception e) {
			logger.error("testGenerateJSONWithContext failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}
}
