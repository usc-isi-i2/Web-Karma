package edu.isi.karma.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.util.EncodingDetector;

public abstract class TestRdfGenerator {
	private static final Logger logger = LoggerFactory.getLogger(TestRdfGenerator.class);
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

        KarmaMetadataManager userMetadataManager = new KarmaMetadataManager();
        UpdateContainer uc = new UpdateContainer();
        userMetadataManager.register(new UserPreferencesMetadata(), uc);
        userMetadataManager.register(new UserConfigMetadata(), uc);
        userMetadataManager.register(new PythonTransformationMetadata(), uc);
	}


	/**
	 * @param csvFile
	 * @param modelFile
	 * @param pw
	 *            stores generated RDF triples
	 * @throws Exception
	 */
	protected void generateRdfFile(String inputFormat, File inputFile, File modelFile, PrintWriter pw)
			throws Exception {

		FileRdfGenerator rdfGen = new FileRdfGenerator();
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				"schedule-model", modelFile.toURI().toURL());
		String encoding = EncodingDetector.detect(inputFile);
		rdfGen.generateRdf(inputFormat, modelIdentifier, pw, inputFile, encoding, 0);

	}

	protected HashSet<String> getFileContent(File file) {
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
	

	protected HashSet<String> getHashSet(String[] array) {
		HashSet<String> hashSet = new HashSet<String>();
		for (int i = 0; i < array.length; i++) {
			String line = array[i].trim();
			if (line.length() > 0)
				hashSet.add(line);
		}
		return hashSet;
	}
	
}
