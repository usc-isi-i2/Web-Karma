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

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

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
public class TestJSONRDFGenerator {

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

		//Add the models in
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"people-model", new File(getTestDataFolder() + "/people-model.ttl").toURI().toURL());
		rdfGen.addModel(modelIdentifier);
		modelIdentifier = new R2RMLMappingIdentifier(
				"cs548-events-model", new File(getTestDataFolder() + "/cs548-events-model.ttl").toURI().toURL());
		rdfGen.addModel(modelIdentifier);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link edu.isi.karma.rdf.JSONRDFGenerator#generateRDF(java.lang.String, java.lang.String, boolean, java.io.PrintWriter)}.
	 */
	@Test
	public void testGenerateRDF1() {
		try {
			
			String filename = getTestDataFolder() + "/people.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(filename), "utf-8");
			
			String outputFilePath = getTestDataFolder() + "/people.rdf";
			System.out.println("Generate RDF: " + outputFilePath);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8");
	       BufferedWriter bw = new BufferedWriter(fw);
	       PrintWriter pw = new PrintWriter(bw);
	            
			rdfGen.generateRDF("people-model", jsonData, true, pw);
			String readRDF = EncodingDetector.getString(new File(outputFilePath), "utf-8");
			assertNotEquals(readRDF.length(), 0);
		} catch(Exception e) {
			fail("Execption: " + e.getMessage());
		}
	}
	
	/**
	 * Test method for {@link edu.isi.karma.rdf.JSONRDFGenerator#generateRDF(java.lang.String, java.lang.String, boolean, java.io.PrintWriter)}.
	 */
	@Test
	public void testGenerateRDF2() {
		try {
			
			String filename = getTestDataFolder() + "/cs548-events.json";
			System.out.println("Load json file: " + filename);
			String jsonData = EncodingDetector.getString(new File(filename), "utf-8");
			
			String outputFilePath = getTestDataFolder() + "/cs548-events.rdf";
			System.out.println("Generate RDF: " + outputFilePath);
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8");
	       BufferedWriter bw = new BufferedWriter(fw);
	       PrintWriter pw = new PrintWriter(bw);
	            
			rdfGen.generateRDF("cs548-events-model", jsonData, true, pw);
			String readRDF = EncodingDetector.getString(new File(outputFilePath), "utf-8");
			assertNotEquals(readRDF.length(), 0);
		} catch(Exception e) {
			fail("Execption: " + e.getMessage());
		}
	}

	private String getRootFolder() {
		return getClass().getClassLoader().getResource(".").getPath() + "/../../";
	}
	
	private String getTestDataFolder() {
		return getRootFolder() + "src/test/karma-data";
	}
}
