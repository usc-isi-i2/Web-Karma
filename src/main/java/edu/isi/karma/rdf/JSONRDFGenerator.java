package edu.isi.karma.rdf;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMapping;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class JSONRDFGenerator extends RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(JSONRDFGenerator.class);
	private HashMap<String, R2RMLMappingIdentifier> modelIdentifiers;
	private HashMap<String, WorksheetR2RMLJenaModelParser> readModelParsers;
	
	
	private JSONRDFGenerator() {
		this.modelIdentifiers = new HashMap<String, R2RMLMappingIdentifier>();
		this.readModelParsers = new HashMap<String, WorksheetR2RMLJenaModelParser>();
	}
	
	private static JSONRDFGenerator instance = null;
	public static JSONRDFGenerator getInstance() {
		if(instance == null) {
			instance = new JSONRDFGenerator();
		}
		return instance;
	}
	
	public void addModel(R2RMLMappingIdentifier modelIdentifier) {
		this.modelIdentifiers.put(modelIdentifier.getName(), modelIdentifier);
	}
	
	public void generateRDF(String sourceName, String jsonData, boolean addProvenance, PrintWriter pw) throws KarmaException, JSONException, IOException {
		logger.debug("Generating rdf for " + sourceName);
		Workspace workspace = initializeWorkspace();
		initOfflineWorkspaceSettings(workspace);
		
		R2RMLMappingIdentifier id = this.modelIdentifiers.get(sourceName);
		if(id == null) {
			throw new KarmaException("Cannot generate RDF. Model named " + sourceName + " does not exist");
		}
		
		//Generate worksheet from the json data
		Object json = JSONUtil.createJson(jsonData);
        JsonImport imp = new JsonImport(json, sourceName, workspace, "utf-8", -1);
        Worksheet worksheet = imp.generateWorksheet();
        
		//Check if the parser for this model exists, else create one
		WorksheetR2RMLJenaModelParser modelParser = readModelParsers.get(sourceName);
		if(modelParser == null) {
			modelParser = loadModel(id);
		}
		
        //Generate mappping data for the worksheet using the model parser
		KR2RMLMapping mapping = modelParser.parse();
		
		applyHistoryToWorksheet(workspace, worksheet, mapping);

		//Generate RDF using the mapping data
		ErrorReport errorReport = new ErrorReport();
		KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet,
		        workspace.getFactory(), workspace.getOntologyManager(), pw,
		        mapping, errorReport, addProvenance);
		rdfGen.generateRDF(false);
		removeWorkspace(workspace);
		logger.debug("Generated rdf for " + sourceName);
	}

	private WorksheetR2RMLJenaModelParser loadModel(R2RMLMappingIdentifier modelIdentifier) throws JSONException, KarmaException {
		WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(modelIdentifier);
		this.readModelParsers.put(modelIdentifier.getName(), parser);
		return parser;
	}
	
	private void initOfflineWorkspaceSettings(Workspace workspace) {
		/**
         * CREATE THE REQUIRED KARMA OBJECTS *
         */
        ServletContextParameterMap.setParameterValue(
                ContextParameter.USER_DIRECTORY_PATH, "src/main/webapp/");
        ServletContextParameterMap.setParameterValue(
                ContextParameter.TRAINING_EXAMPLE_MAX_COUNT, "200");

        SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
        
		OntologyManager ontologyManager = workspace.getOntologyManager();
		/** Check if any ontology needs to be preloaded **/
		String preloadedOntDir = getRootFolder() + "preloaded-ontologies";
		logger.info("Loading ontologies from dir: " + preloadedOntDir);
		File ontDir = new File(preloadedOntDir);
		if (ontDir.exists()) {
			File[] ontologies = ontDir.listFiles();
			for (File ontology: ontologies) {
				if (ontology.getName().endsWith(".owl") || ontology.getName().endsWith(".rdf") || ontology.getName().endsWith(".xml")) {
					logger.info("Loading ontology file: " + ontology.getAbsolutePath());
					try {
						String encoding = EncodingDetector.detect(ontology);
						ontologyManager.doImport(ontology, encoding);
					} catch (Exception t) {
						logger.error ("Error loading ontology: " + ontology.getAbsolutePath(), t);
					}
				}
			}
			// update the cache at the end when all files are added to the model
			ontologyManager.updateCache();
		} else {
			logger.info("No directory for preloading ontologies exists.");
		}
	}
	
	private String getRootFolder() {
		return getClass().getClassLoader().getResource(".").getPath() + "/../../";
	}
}
