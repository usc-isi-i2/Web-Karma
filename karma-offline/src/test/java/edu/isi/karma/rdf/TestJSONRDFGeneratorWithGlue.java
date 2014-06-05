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

package edu.isi.karma.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.util.EncodingDetector;


/**
 * @author dipsy
 * 
 */
public class TestJSONRDFGeneratorWithGlue extends TestRdfGenerator{

	JSONRDFGenerator rdfGen;
	private static Logger logger = LoggerFactory.getLogger(TestJSONRDFGeneratorWithGlue.class);
	

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
		rdfGen = JSONRDFGenerator.getInstance();

		// Add the models in
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"glue-nested-model", getTestResource(
						 "glue/glue-nested-model.ttl"));
		rdfGen.addModel(modelIdentifier);
		modelIdentifier = new R2RMLMappingIdentifier(
				"glue-top-model", getTestResource(
						 "glue/glue-top-model.ttl"));
		rdfGen.addModel(modelIdentifier);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGlueNested() {
		try {

			String filename = "glue/glue-nested.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(getTestResource(filename).toURI()),
					"utf-8");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			rdfGen.generateRDF("glue-nested-model", jsonData, false, pw);
			String rdf = sw.toString();
			
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			int count = lines.length;
			 
			assertEquals(33, count);
		} catch (Exception e) {
			logger.error("testGlueNested failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}
	
	@Test
	public void testGlueTop() {
		try {

			String filename = "glue/glue-top.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(getTestResource(filename).toURI()),
					"utf-8");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			rdfGen.generateRDF("glue-top-model", jsonData, false, pw);
			String rdf = sw.toString();
			
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			int count = lines.length;
			 
			assertEquals(19, count);
		} catch (Exception e) {
			logger.error("testGlueTop failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}

	private URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
