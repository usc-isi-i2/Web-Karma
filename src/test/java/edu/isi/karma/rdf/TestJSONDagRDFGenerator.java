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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.isi.karma.kr2rml.R2RMLMappingIdentifier;
import edu.isi.karma.util.EncodingDetector;

/**
 * @author dipsy
 * 
 */
public class TestJSONDagRDFGenerator {

	JSONRDFGenerator rdfGen;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

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
				"menu-model", new File(getTestDataFolder()
						+ "/menu.json.model.txt").toURI().toURL());
		rdfGen.addModel(modelIdentifier);
		modelIdentifier = new R2RMLMappingIdentifier(
				"menus-model", new File(getTestDataFolder()
						+ "/menus.json.model.txt").toURI().toURL());
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

			String filename = getTestDataFolder() + "/menu.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(filename),
					"utf-8");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			rdfGen.generateRDF("menu-model", jsonData, false, pw);
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split("\n");
			int count = lines.length + 1;
			assertEquals(191, count);
		} catch (Exception e) {
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

			String filename = getTestDataFolder() + "/menus.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(filename),
					"utf-8");

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			rdfGen.generateRDF("menus-model", jsonData, false, pw);
			String rdf = sw.toString();
			assertNotEquals(rdf.length(), 0);
			String[] lines = rdf.split("\n");
			int count = lines.length + 1;
			assertEquals(386, count);
		} catch (Exception e) {
			fail("Execption: " + e.getMessage());
		}
	}
	
	private String getRootFolder() {
		return getClass().getClassLoader().getResource(".").getPath()
				+ "/../../";
	}

	private String getTestDataFolder() {
		return getRootFolder() + "src/test/karma-data";
	}
}
