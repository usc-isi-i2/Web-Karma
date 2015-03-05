package edu.isi.karma.rdf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.modeling.Uris;
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
	protected ContextIdentifier contextId; 
	protected String rdfGenerationRoot = null;
	public void setup(String karmaHomePath, String inputTypeString, String modelUri, String modelFile, 
			String baseURI, String contextURI, String root, String selection) {

		try {
			setupKarmaHome(karmaHomePath);
			determineInputType(inputTypeString);
			generator = new GenericRDFGenerator(selection);
			this.modelUri = modelUri;
			this.modelFile = modelFile;
			this.baseURI = baseURI;
			addModel();
			if (contextURI != null && !contextURI.isEmpty()) {
				addContext(contextURI);
			}
			Model model = generator.getModelParser("model").getModel();
			if (root != null && !root.isEmpty()) {
				StmtIterator itr = model.listStatements(null, model.getProperty(Uris.KM_NODE_ID_URI), root);
				Resource subject = null;
				while (itr.hasNext()) {
					subject = itr.next().getSubject();
				}
				if (subject != null) {
					itr = model.listStatements(null, model.getProperty(Uris.RR_SUBJECTMAP_URI), subject);
					while (itr.hasNext()) {
						rdfGenerationRoot = itr.next().getSubject().toString();
					}
				}
			}
		} catch (KarmaException | IOException e) {
			LOG.error("Unable to complete Karma set up: " + e.getMessage());
			throw new RuntimeException("Unable to complete Karma set up: "
					+ e.getMessage());
		}
	}

	private void setupKarmaHome(String karmaHomePath) throws KarmaException {
		// TODO dynamically discover the archive
		if(null == karmaHomePath)
		{
			LOG.info("No Karma user home provided.  Creating default Karma configuration");
		}
		else
		{
			File karmaUserHome = new File(karmaHomePath);
			if (!karmaUserHome.exists()) {
				LOG.info("No Karma user home provided.  Creating default Karma configuration");
			} else {
				System.setProperty("KARMA_USER_HOME",
						karmaUserHome.getAbsolutePath());
			}	
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

	private void addContext(String contextURI)    {
		try {
			contextId = new ContextIdentifier("context", new URL(contextURI));
			generator.addContext(contextId);
		}catch(Exception e) {

		}
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

	public ContextIdentifier getContextId() {
		return contextId;
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

	public String getRdfGenerationRoot() {
		return rdfGenerationRoot;
	}
}
