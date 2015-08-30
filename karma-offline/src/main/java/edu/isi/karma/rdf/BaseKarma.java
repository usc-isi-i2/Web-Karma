package edu.isi.karma.rdf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.util.Util;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

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
			Util.logException(LOG, e);
			throw new RuntimeException("Unable to complete Karma set up: "
					+ e.getMessage() + modelUri + "      " + rdfGenerationRoot);
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
		
		ServletContextParameterMap contextParameters = new ServletContextParameterMap(null);
		ContextParametersRegistry contextParametersRegistry = ContextParametersRegistry.getInstance();
		contextParametersRegistry.register(contextParameters);
		
		KarmaMetadataManager userMetadataManager;
		userMetadataManager = new KarmaMetadataManager(contextParameters);
		UpdateContainer uc = new UpdateContainer();
		userMetadataManager.register(new UserPreferencesMetadata(contextParameters), uc);
		userMetadataManager.register(new UserConfigMetadata(contextParameters), uc);
		userMetadataManager.register(new PythonTransformationMetadata(contextParameters), uc);
		PythonRepository pythonRepository = new PythonRepository(false, contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonRepositoryRegistry.getInstance().register(pythonRepository);
	}

	private void addModel() throws MalformedURLException {
		getModel();
		generator.addModel(new R2RMLMappingIdentifier("model", modelURL));
	}
	
	public void addModel(String modelName,String modelFile,String modelUri) throws MalformedURLException {
		URL modelURL = getModel(modelFile,modelUri);
		generator.addModel(new R2RMLMappingIdentifier(modelName,modelURL));
		
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
			
			//TODO the line below must be uncommented, problem is ar-15 model.  
			//this.rdfGenerationRoot = rdfGenerationRoot;
			
			if(generator != null){
	
				Model model = generator.getModelParser(modelName).getModel();
				//LOG.error("GOT MODEL IN SETRDFGENERATIONL:" + modelName + "," + rdfGenerationRoot);
				
				if (model != null){

					if (rdfGenerationRoot != null && !rdfGenerationRoot.isEmpty()) {
						StmtIterator itr = model.listStatements(null, model.getProperty(Uris.KM_NODE_ID_URI), rdfGenerationRoot);
						Resource subject = null;
						while (itr.hasNext()) {
							subject = itr.next().getSubject();
						}
						if (subject != null) {
							itr = model.listStatements(null, model.getProperty(Uris.RR_SUBJECTMAP_URI), subject);
							while (itr.hasNext()) {
								this.rdfGenerationRoot = itr.next().getSubject().toString();
							}
						}
						
					}
				}
				else{
					//LOG.error("***********BaseKarma Error: model not found:" + modelName);
				}
			}
			else{
				//LOG.error("***********BaseKarma Error: GenericRDFGenerator object is null");
			}
		}
		catch (KarmaException | JSONException | IOException e) {
			//LOG.error("Unable to set rdf generation root: " + e.getMessage() + "*****************" + rdfGenerationRoot + "**********" + modelName + "*********" + this.rdfGenerationRoot);
			throw new RuntimeException("Unable to set rdf generation root: "
					+ e.getMessage()+ "*****************" + rdfGenerationRoot + "**********" + modelName + "*********" + this.rdfGenerationRoot);
		}
		
	}
}
