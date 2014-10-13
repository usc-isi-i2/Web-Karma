package edu.isi.karma.mapreduce.driver;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.rdf.GenericRDFGenerator;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.webserver.KarmaException;

public class BaseKarma {
	private static Logger LOG = LoggerFactory.getLogger(BaseKarma.class);

	protected GenericRDFGenerator generator;
	protected String baseURI;
	protected InputType inputType;
	protected String modelUri;
	protected String modelFile;
	protected URL modelURL;
	public void setup(String inputTypeString, String modelUri, String modelFile, String baseURI) {

		try {
			setupKarmaHome();
			determineInputType(inputTypeString);
			generator = new GenericRDFGenerator(null);
			this.modelUri = modelUri;
			this.modelFile = modelFile;
			addModel();
		} catch (KarmaException | IOException e) {
			LOG.error("Unable to complete Karma set up: " + e.getMessage());
			throw new RuntimeException("Unable to complete Karma set up: "
					+ e.getMessage());
		}
	}

	private void setupKarmaHome() throws KarmaException {
		// TODO dynamically discover the archive
		File karmaUserHome = new File("./karma.zip/karma");
		if (!karmaUserHome.exists()) {
			LOG.info("No Karma user home provided.  Creating default Karma configuration");
		} else {
			System.setProperty("KARMA_USER_HOME",
					karmaUserHome.getAbsolutePath());
		}
		KarmaMetadataManager userMetadataManager;
		userMetadataManager = new KarmaMetadataManager();
		UpdateContainer uc = new UpdateContainer();
		userMetadataManager.register(new UserPreferencesMetadata(), uc);
		userMetadataManager.register(new UserConfigMetadata(), uc);
		userMetadataManager.register(new PythonTransformationMetadata(), uc);
		PythonRepository.disableReloadingLibrary();
	}

	private void addModel() throws MalformedURLException {
		getModel();
		generator.addModel(new R2RMLMappingIdentifier("model", modelURL));
	}

	private void determineInputType(String inputTypeString) {
	
		inputType = null;
		if (inputTypeString != null) {
			try {
				inputType = InputType.valueOf(inputTypeString.toUpperCase());
				LOG.info("Expecting input of type {}.", inputType.toString());
			} catch (Exception e) {
				LOG.error(
						"Unable to recognize input type {}. Will attempt to automatically detect serialization format.",
						inputTypeString);
			}
		} else {
			LOG.info("No input type provided.  Will attempt to automatically detect serialization format.");
		}
	}

	public GenericRDFGenerator getGenerator() {
		return generator;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public InputType getInputType() {
		return inputType;
	}

	public URL getModel() throws MalformedURLException
	{
		if(modelURL == null)
		{
			if (modelUri != null) {
				modelURL = new URL(modelUri);
			} else if (modelFile != null) {
				modelURL = new File(modelFile).toURI().toURL();
			}
		}
		return modelURL;
	}
}
