package edu.isi.karma.rdf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.N3KR2RMLRDFWriter;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.OntologyMetadata;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class TestRdfGenerator {
	private static final Logger logger = LoggerFactory.getLogger(TestRdfGenerator.class);
	
	public static ServletContextParameterMap contextParameters;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		ContextParametersRegistry contextParametersRegistry = ContextParametersRegistry.getInstance();
		contextParameters = contextParametersRegistry.registerByKarmaHome(null);
		
        KarmaMetadataManager userMetadataManager = new KarmaMetadataManager(contextParameters);
        UpdateContainer uc = new UpdateContainer();
        userMetadataManager.register(new UserPreferencesMetadata(contextParameters), uc);
        userMetadataManager.register(new UserConfigMetadata(contextParameters), uc);
        userMetadataManager.register(new PythonTransformationMetadata(contextParameters), uc);
        userMetadataManager.register(new OntologyMetadata(contextParameters), uc);
        PythonRepository pythonRepository = new PythonRepository(false, contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonRepositoryRegistry.getInstance().register(pythonRepository);
	}


	/**
	 * @param csvFile
	 * @param modelFile
	 * @param pw
	 *            stores generated RDF triples
	 * @throws Exception
	 */
	protected void generateRdfFile(File inputFile, InputType inputType, String modelName, File modelFile, PrintWriter pw)
			throws Exception {
		GenericRDFGenerator rdfGen = new GenericRDFGenerator(null);
		R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(
				modelName, modelFile.toURI().toURL());
		rdfGen.addModel(modelIdentifier);
		List<KR2RMLRDFWriter> writers = createBasicWriter(pw);
		RDFGeneratorRequest request = new RDFGeneratorRequest(modelName, inputFile.getName());
		request.setAddProvenance(false);
		request.setInputFile(inputFile);
		request.setDataType(inputType);
		request.addWriters(writers);
		request.setContextParameters(ContextParametersRegistry.getInstance().getDefault());
		rdfGen.generateRDF(request);
	}

	protected List<KR2RMLRDFWriter> createBasicWriter(PrintWriter pw) {
		N3KR2RMLRDFWriter writer = new N3KR2RMLRDFWriter(new URIFormatter(), pw);
		List<KR2RMLRDFWriter> writers = new LinkedList<>();
		writers.add(writer);
		return writers;
	}
	
	protected HashSet<String> getFileContent(File file) {
		HashSet<String> hashSet = new HashSet<>();
		
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
		HashSet<String> hashSet = new HashSet<>();
		for (int i = 0; i < array.length; i++) {
			String line = array[i].trim();
			if (line.length() > 0)
				hashSet.add(line);
		}
		return hashSet;
	}
	
}
