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

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;


/**
 * @author dipsy
 * 
 */
public class TestJSONRDFGeneratorWithUnfold extends TestJSONRDFGenerator{

	private static Logger logger = LoggerFactory.getLogger(TestJSONRDFGeneratorWithUnfold.class);
	

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
				"unfold-top-model", getTestResource(
						 "unfold/unfold-top-model.ttl"));
		rdfGen.addModel(modelIdentifier);
		modelIdentifier = new R2RMLMappingIdentifier(
				"unfold-nested-model", getTestResource(
						 "unfold/unfold-nested-model.ttl"));
		rdfGen.addModel(modelIdentifier);
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testUnfoldNested() {
		try {

			executeBasicJSONTest("unfold/unfold-nested.json", "unfold-nested-model", false, 25);
		} catch (Exception e) {
			logger.error("testUnfoldNested failed:", e);
			fail("Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testUnfoldTop() {
		try {

			executeBasicJSONTest("unfold/unfold-top.json", "unfold-top-model", false, 23);

		} catch (Exception e) {
			logger.error("testUnfoldTop failed:", e);
			fail("Exceptions: " + e.getMessage());
		}
	}

}
