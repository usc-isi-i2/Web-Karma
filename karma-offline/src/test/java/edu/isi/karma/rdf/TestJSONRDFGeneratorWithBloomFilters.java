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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.apache.hadoop.util.bloom.Key;
import org.json.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.BloomFilterKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.KR2RMLBloomFilter;
import edu.isi.karma.kr2rml.TriplesMapBloomFilterManager;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.util.EncodingDetector;


/**
 * @author dipsy
 * 
 */
public class TestJSONRDFGeneratorWithBloomFilters extends TestRdfGenerator{

	JSONRDFGenerator rdfGen;
	private static Logger logger = LoggerFactory.getLogger(TestJSONRDFGeneratorWithBloomFilters.class);
	

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

			StringWriter bfsw = new StringWriter();
			PrintWriter bfpw = new PrintWriter(bfsw);

			
			BloomFilterKR2RMLRDFWriter bfWriter = new BloomFilterKR2RMLRDFWriter(bfpw);
			rdfGen.generateRDF("people-model", jsonData, false, bfWriter);
			String base64EncodedBloomFilterManager = bfsw.toString();
			TriplesMapBloomFilterManager manager = new TriplesMapBloomFilterManager(new JSONObject(base64EncodedBloomFilterManager));
			KR2RMLBloomFilter bf = manager.getBloomFilter("http://isi.edu/integration/karma/dev#TriplesMap_cb7bdfc8-5781-4be8-8dcb-51d14139960b");
			Key k = new Key(("<https://lh4.googleusercontent.com/-uonc-uQiTGw/AAAAAAAAAAI/AAAAAAAAATk/V_iGc4e8Vwk/photo.jpg?sz=80>").getBytes());
			assertEquals(7, bf.estimateNumberOfHashedValues());
			assertTrue(bf.membershipTest(k));
			
			
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
