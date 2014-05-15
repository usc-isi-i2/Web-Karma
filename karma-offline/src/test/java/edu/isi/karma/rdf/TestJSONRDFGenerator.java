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

import edu.isi.karma.kr2rml.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.util.EncodingDetector;


/**
 * @author dipsy
 * 
 */
public class TestJSONRDFGenerator extends TestRdfGenerator{

	JSONRDFGenerator rdfGen;
	private static Logger logger = LoggerFactory.getLogger(TestJSONRDFGenerator.class);
	

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

			String filename = "people.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(getTestResource(filename).toURI()),
					"utf-8");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			rdfGen.generateRDF("people-model", jsonData, true, pw);
			String rdf = sw.toString();
			System.out.println(rdf);
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			int count = lines.length;
			 
			assertEquals(102, count);
		} catch (Exception e) {
			logger.error("testGenerateRDF1 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}

	@Test
	public void testGenerateJSON1() {
		try {

			String filename = "people.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(getTestResource(filename).toURI()),
					"utf-8");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			JSONKR2RMLRDFWriter writer = new JSONKR2RMLRDFWriter(pw);
			rdfGen.generateRDF("people-model", jsonData, false, writer);
			String rdf = sw.toString();
			System.out.println(rdf);
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			int count = lines.length;
			 
			assertEquals(16, count);
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

			String filename = "cs548-events.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(getTestResource(filename).toURI()),
					"utf-8");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			rdfGen.generateRDF("cs548-events-model", jsonData, true, pw);
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split(System.getProperty("line.separator"));
			assertEquals(238, lines.length);
		} catch (Exception e) {
			logger.error("testGenerateRDF1 failed:", e);
			fail("Execption: " + e.getMessage());
		}
	}

	private URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
