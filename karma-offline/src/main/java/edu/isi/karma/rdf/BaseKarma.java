package edu.isi.karma.rdf;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class BaseKarma {
	private static Logger LOG = LoggerFactory.getLogger(BaseKarma.class);

	protected GenericRDFGenerator generator;
	protected String baseURI;
	protected InputType inputType;
	
	protected String modelUri;
	protected String modelFile;
	protected URL modelURL;
	protected String modelContent;
	
	protected ContextIdentifier contextId; 
	protected String rdfGenerationRoot = null;
	public void setup(String karmaHomePath, String inputTypeString, 
			String modelUri, String modelFile, String modelContent,
			String baseURI, String contextURI, String contextContent,
			String root, String selection) {

		try {
			setupKarmaHome(karmaHomePath);
			determineInputType(inputTypeString);
			generator = new GenericRDFGenerator(selection);
			this.modelUri = modelUri;
			this.modelFile = modelFile;
			this.modelContent = modelContent;
			
			this.baseURI = baseURI;
			
			addModel();
			if (contextURI != null && !contextURI.isEmpty()) {
				addContext(contextURI, contextContent);
			}
			setRdfGenerationRoot(root, "model");
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
		
		ContextParametersRegistry contextParametersRegistry = ContextParametersRegistry.getInstance();
		ServletContextParameterMap contextParameters = contextParametersRegistry.registerByKarmaHome(null);
		KarmaMetadataManager userMetadataManager;
		userMetadataManager = new KarmaMetadataManager(contextParameters);
		UpdateContainer uc = new UpdateContainer();
		userMetadataManager.register(new UserPreferencesMetadata(contextParameters), uc);
		userMetadataManager.register(new UserConfigMetadata(contextParameters), uc);
		userMetadataManager.register(new PythonTransformationMetadata(contextParameters), uc);
		PythonRepositoryRegistry.getInstance().registerSafe(contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
	}

	private void addModel() throws MalformedURLException {
		getModel();
		generator.addModel(new R2RMLMappingIdentifier("model", modelURL, modelContent));
	}
	
	public void addModel(String modelName, String modelFile, String modelUri, String modelContent) throws MalformedURLException {
		URL modelURL = getModel(modelFile, modelUri);
		generator.addModel(new R2RMLMappingIdentifier(modelName, modelURL, modelContent));
		
	}

	private void addContext(String contextURI, String contextContent)    {
		try {
			contextId = new ContextIdentifier("context", new URL(contextURI), contextContent);
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
	
	private URL getModel(String modelFile,String modelUri) throws MalformedURLException
	{
		URL newModelURL=null;
		
		if (modelUri != null) {
			newModelURL = new URL(modelUri);
		} else if (modelFile != null) {
			newModelURL = new File(modelFile).toURI().toURL();
		}
	
		return newModelURL;
	}

	public String getRdfGenerationRoot() {
		return rdfGenerationRoot;
	}
	
	public void setRdfGenerationRoot(String rdfGenerationRoot, String modelName) {
		try{
			if (rdfGenerationRoot != null && !rdfGenerationRoot.isEmpty()){
				KR2RMLMapping kr2rmlMapping = generator.getModelParser(modelName).parse();
				String triplesMapId = kr2rmlMapping.translateGraphNodeIdToTriplesMapId(rdfGenerationRoot);
				if(triplesMapId != null){
					this.rdfGenerationRoot = triplesMapId;
				}else{
					throw new RuntimeException("triplesMapId not found for rdfGenerationRoot:" + rdfGenerationRoot);
				}
			}
		}
		catch (KarmaException | JSONException | IOException e) {
			LOG.error("Not able to set root", e);
			throw new RuntimeException("Unable to set rdf generation root: " + e.getMessage());
		}
		
	}
}
