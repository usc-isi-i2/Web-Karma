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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;

/**
 * @author dipsy
 * 
 */
public class TestJSONDagRDFGenerator extends TestJSONRDFGenerator{

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		

		// Add the models in
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"menu-model", getTestResource(
						 "menu.json.model.txt"));
		rdfGen.addModel(modelIdentifier);
		modelIdentifier = new R2RMLMappingIdentifier(
				"menus-model", getTestResource(
						 "menus.json.model.txt"));
		rdfGen.addModel(modelIdentifier);
	
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGenerateRDF1() {
		try {

			executeBasicJSONTest("menu.json", "menu-model", false, 190);
			
		} catch (Exception e) {
			fail("Exception: " + e.getMessage());
		}
	}

	@Test
	public void testGenerateRDF2() {
		testMenus();
	}

	private void testMenus() {
		try {
			
			executeBasicJSONTest("menus.json", "menus-model", false, 411);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e.getMessage());
		}
	}
	
	@Test
	public void testGenerateRDFThreaded()
	{
		List<Future<Boolean>> results = new LinkedList<>();
		ExecutorService es  = Executors.newFixedThreadPool(4);
		for(int i = 0; i < 4 ; i++)
		{
			results.add(es.submit(new Callable<Boolean>(){

			@Override
			public Boolean call() throws Exception {
				for(int i = 0; i < 100; i++)
				{
					testMenus();
				}
				return true;
			}
			
		}));
		}
		boolean failure = false;
		for(Future<Boolean> result : results)
		{
		try
		{
		
			result.get();
		}
		
		catch(Exception e)
		{
			failure = true;
			e.printStackTrace();
			
		}
		}
		if(failure)
		{
			fail();
		}
	}
	
}
