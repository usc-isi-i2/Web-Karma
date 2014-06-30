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
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rdf.GenericRDFGenerator.InputType;

/**
 * The class is used for test. There are csv files, model files and standard RDF
 * files in the directory. For each csv file, the associated model is applied to
 * generate RDF triples. Then the generated triples is compared with the
 * standard RDF file. If they are same, it predicates that everything works
 * well. The testResult.txt file will record the error models and triples.
 * 
 * @author Wan Jing 2014-02-26
 * 
 */
public class TestCSVFileRdfGenerator extends TestRdfGenerator {
	private static Logger logger = LoggerFactory.getLogger(TestCSVFileRdfGenerator.class);


	@Test
	public void testScheduleRDFPyTranform() {
		boolean tag = true;

		
		String csvDirect = "csv";
		String modelDirect = "model";
		String standardRdfDirect = "standardrdf";
	
		try {
	
			File fileList[] = (new File(getTestResource(modelDirect).toURI()).listFiles(new FilenameFilter(){

				@Override
				public boolean accept(File dir, String name) {

					return !name.startsWith(".") && name.endsWith(".ttl");
				}
				
			}));
			
			for (int i = 0; i < fileList.length; i++) {
				File modelFile = fileList[i];
				logger.info("Load file: " + modelFile.getName());

				String name = modelFile.getName().replace("-model.ttl", "");
				File standardRdfFile = new File(getTestResource(standardRdfDirect + "/" + name
						+ "-rdf.ttl").toURI());
				File csvFile = new File(getTestResource(csvDirect + "/" + name + ".csv").toURI());
				
				if(!standardRdfFile.exists())
				{
					logger.error(standardRdfFile+" doesn't  exist");
					continue;
				}
				if(!csvFile.exists())
				{
					logger.error(csvFile+" doesn't  exist");
					continue;
				}
					

				StringWriter sw = new StringWriter();// generated RDF triples
				PrintWriter pw = new PrintWriter(sw);

				generateRdfFile(csvFile, InputType.CSV, modelFile.getName(), modelFile, pw);

				HashSet<String> standardSet = getFileContent(standardRdfFile);
				HashSet<String> generatedSet = getHashSet(sw.toString().split(
						System.getProperty("line.separator")));

				if (!standardSet.containsAll(generatedSet)
						|| !generatedSet.containsAll(standardSet)) {
					tag = false;
					outputError(standardSet, generatedSet, modelFile.getName());
				} else {
					logger.info(modelFile.getName() + " is ok");
				}

				pw.close();
		
			}
			assertEquals(tag, true);
		} catch (Exception e) {
			logger.error("Exception", e);
			fail("Exception: " + e.getMessage());
		}
	}

	/**
	 * @param standardSet
	 *            the standard triples
	 * @param generatedSet
	 *            the generated triples
	 * @param modelName
	 *            the model name
	 * @param out
	 *            records the error message.
	 */
	private void outputError(HashSet<String> standardSet,
			HashSet<String> generatedSet, String modelName) {
		for (String temp : standardSet)
			if (!generatedSet.contains(temp))
			{
				logger.error(modelName + " missing triple error:" + temp);
			}

		for (String temp : generatedSet)
			if (!standardSet.contains(temp))
			{
				logger.error(modelName + " extra triple error:" + temp);
			}
	}

	private URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
