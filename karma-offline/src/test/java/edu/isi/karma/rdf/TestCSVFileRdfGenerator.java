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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

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
public class TestCSVFileRdfGenerator {
	private static Logger logger = LoggerFactory.getLogger(TestCSVFileRdfGenerator.class);
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initOfflineWorkspaceSettings();
	}

	@Test
	public void testScheduleRDFPyTranform() {
		boolean tag = true;

		
		String csvDirect = "csv";
		String modelDirect = "model";
		String standardRdfDirect = "standardrdf";
		//String resultFileName = "testresult/testResult.txt";

		try {
		//	PrintWriter out = new PrintWriter(new FileWriter(resultFileName));
			//out.println("The test was carried out at " + new Date().toString()
				//	+ "\n");

			File fileList[] = (new File(getTestResource(modelDirect).getFile()).listFiles());
			
			for (int i = 0; i < fileList.length; i++) {
				File modelFile = fileList[i];
				logger.info("Load file: " + modelFile.getName());

				String name = modelFile.getName().replace("-model.ttl", "");
				File standardRdfFile = new File(getTestResource(standardRdfDirect + "/" + name
						+ "-rdf.ttl").getFile());
				File csvFile = new File(getTestResource(csvDirect + "/" + name + ".csv").getFile());
				
				if(!standardRdfFile.exists())
				{
				//	out.println(standardRdfFile+" doesn't  exist");
					logger.error(standardRdfFile+" doesn't  exist");
					continue;
				}
				if(!csvFile.exists())
				{
					//out.println(csvFile+" doesn't  exist");
					logger.error(csvFile+" doesn't  exist");
					continue;
				}
					

				StringWriter sw = new StringWriter();// generated RDF triples
				PrintWriter pw = new PrintWriter(sw);

				generateRdfFile(csvFile, modelFile, pw);

				HashSet<String> standardSet = getFileContent(standardRdfFile);
				HashSet<String> generatedSet = getHashSet(sw.toString().split(
						"\n"));

				if (!standardSet.containsAll(generatedSet)
						|| !generatedSet.containsAll(standardSet)) {
					tag = false;
					outputError(standardSet, generatedSet, modelFile.getName());
				} else {
				//	out.println(modelFile.getName() + " is ok");
					logger.info(modelFile.getName() + " is ok");
				}

				pw.close();
			//	out.flush();

			}
		//	out.close();
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
			//	out.println(modelName + " missing triple error:" + temp);
				logger.error(modelName + " missing triple error:" + temp);
			}

		for (String temp : generatedSet)
			if (!standardSet.contains(temp))
			{
				//out.println(modelName + " extra triple error:" + temp);
				logger.error(modelName + " extra triple error:" + temp);
			}
	}


	private static void initOfflineWorkspaceSettings() {
		/**
		 * CREATE THE REQUIRED KARMA OBJECTS *
		 */
		ServletContextParameterMap.setParameterValue(
				ContextParameter.USER_DIRECTORY_PATH, "src/main/webapp/");
		ServletContextParameterMap.setParameterValue(
				ContextParameter.TRAINING_EXAMPLE_MAX_COUNT, "200");

		SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
	}

	/**
	 * @param csvFile
	 * @param modelFile
	 * @param pw
	 *            stores generated RDF triples
	 * @throws Exception
	 */
	private void generateRdfFile(File csvFile, File modelFile, PrintWriter pw)
			throws Exception {

		FileRdfGenerator rdfGen = new FileRdfGenerator();
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"schedule-model", modelFile.toURI().toURL());
		String encoding = EncodingDetector.detect(csvFile);
		rdfGen.generateRdf("csv", modelIdentifier, pw, csvFile, encoding, 0);

	}

	private HashSet<String> getHashSet(String[] array) {
		HashSet<String> hashSet = new HashSet<String>();
		for (int i = 0; i < array.length; i++) {
			String line = array[i].trim();
			if (line.length() > 0)
				hashSet.add(line);
		}
		return hashSet;
	}

	private HashSet<String> getFileContent(File file) {
		HashSet<String> hashSet = new HashSet<String>();
		
		try {
			String encoding = EncodingDetector.detect(file);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), encoding));

			String line = in.readLine();
			while (line != null) {
				line = line.trim();
				if (line.length() > 0)
					hashSet.add(line);

				line = in.readLine();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Error getting file contents: " + file.getAbsolutePath());
		}
		return hashSet;
     }
	

	private URL getTestResource(String name)
	{
		return getClass().getClassLoader().getResource(name);
	}
}
