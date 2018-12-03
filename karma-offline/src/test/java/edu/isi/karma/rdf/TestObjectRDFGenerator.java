package edu.isi.karma.rdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.ContextParametersRegistry;

public class TestObjectRDFGenerator extends TestRdfGenerator {
	protected GenericRDFGenerator rdfGen;
	private static Logger logger = LoggerFactory.getLogger(TestCSVRDFGenerator.class);

	@Test
	public void testScheduleRDFPyTranform() {
		boolean tag = true;

		String csvDirect = "csv";
		String modelDirect = "model";
		String standardRdfDirect = "standardrdf";

		try {

			File fileList[] = (new File(getTestResource(modelDirect).toURI()).listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {

					return !name.startsWith(".") && name.endsWith(".ttl");
				}

			}));

			for (int i = 0; i < fileList.length; i++) {
				File modelFile = fileList[i];
				logger.info("Load file: " + modelFile.getName());

				String name = modelFile.getName().replace("-model.ttl", "");
				File standardRdfFile = new File(getTestResource(standardRdfDirect + "/" + name + "-rdf.ttl").toURI());
				File csvFile = new File(getTestResource(csvDirect + "/" + name + ".csv").toURI());

				if (!standardRdfFile.exists()) {
					logger.error(standardRdfFile + " doesn't  exist");
					continue;
				}
				if (!csvFile.exists()) {
					logger.error(csvFile + " doesn't  exist");
					continue;
				}

				StringWriter sw = new StringWriter();// generated RDF triples
				PrintWriter pw = new PrintWriter(sw);

				generateRdfFile(csvFile, InputType.OBJECT, modelFile.getName(), modelFile, pw);

				HashSet<String> standardSet = getFileContent(standardRdfFile);
				HashSet<String> generatedSet = getHashSet(sw.toString().split(System.getProperty("line.separator")));

				if (!standardSet.containsAll(generatedSet) || !generatedSet.containsAll(standardSet)) {
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
	 * @param standardSet  the standard triples
	 * @param generatedSet the generated triples
	 * @param modelName    the model name
	 * @param out          records the error message.
	 */
	private void outputError(HashSet<String> standardSet, HashSet<String> generatedSet, String modelName) {
		for (String temp : standardSet)
			if (!generatedSet.contains(temp)) {
				logger.error(modelName + " missing triple error:" + temp);
			}

		for (String temp : generatedSet)
			if (!standardSet.contains(temp)) {
				logger.error(modelName + " extra triple error:" + temp);
			}
	}

	@Override
	protected void generateRdfFile(File inputFile, InputType inputType, String modelName, File modelFile,
			PrintWriter pw) throws Exception {
		GenericRDFGenerator rdfGen = new GenericRDFGenerator(null);
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(modelName, modelFile.toURI().toURL());
		rdfGen.addModel(modelIdentifier);
		List<KR2RMLRDFWriter> writers = createBasicWriter(pw);
		RDFGeneratorRequest request = new RDFGeneratorRequest(modelName, inputFile.getName());
		InputStreamReader isr = EncodingDetector.getInputStreamReader(new FileInputStream(inputFile), "UTF-8");
		BufferedReader br = new BufferedReader(isr);
		CSVReader reader = new CSVReader(br);

		List<String> headers = null;
		String[] rowValues = null;
		List<List<String>> values = new LinkedList<List<String>>();
		while ((rowValues = reader.readNext()) != null) {
			if (headers == null) {
				headers = Arrays.asList(rowValues);
			} else {
				values.add(Arrays.asList(rowValues));
			}
		}
		reader.close();

		RDFGeneratorInputWrapper wrapper = new RDFGeneratorInputWrapper(headers, values);
		request.setInput(wrapper);
		request.setAddProvenance(false);
		request.setDataType(inputType);
		request.addWriters(writers);
		request.setContextParameters(ContextParametersRegistry.getInstance().getDefault());
		rdfGen.generateRDF(request);
	}

	protected URL getTestResource(String name) {
		return getClass().getClassLoader().getResource(name);
	}
}
