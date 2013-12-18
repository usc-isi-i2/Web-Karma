package edu.isi.karma.rdf;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMapping;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class JSONRDFGenerator {
	
	private HashMap<String, R2RMLMappingIdentifier> modelIdentifiers;
	private HashMap<String, WorksheetR2RMLJenaModelParser> readModelParsers;
	
	private Workspace workspace;
	
	private JSONRDFGenerator(List<R2RMLMappingIdentifier> modelIdentifiers) {
		this.modelIdentifiers = new HashMap<>();
		for(R2RMLMappingIdentifier mi : modelIdentifiers) {
			this.modelIdentifiers.put(mi.getName(), mi);
		}
		this.readModelParsers = new HashMap<String, WorksheetR2RMLJenaModelParser>();
		this.workspace = WorkspaceManager.getInstance().createWorkspace();
		WorkspaceRegistry.getInstance().register(new ExecutionController(this.workspace));
	}
	
	private static JSONRDFGenerator instance = null;
	public static JSONRDFGenerator getInstance() {
		return instance;
	}
	public static JSONRDFGenerator createInstance(List<R2RMLMappingIdentifier> modelIdentifiers) {
		instance = new JSONRDFGenerator(modelIdentifiers);
		return getInstance();
	}
	
	public void generateRDF(String sourceName, String jsonData, boolean addProvenance, PrintWriter pw) throws KarmaException, JSONException, IOException {
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
		KR2RMLMapping mapping = modelParser.parse(worksheet, workspace);
		

		//Generate RDF using the mapping data
		ErrorReport errorReport = new ErrorReport();
		KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet,
		        workspace.getFactory(), workspace.getOntologyManager(), pw,
		        mapping.getAuxInfo(), errorReport, addProvenance);
		rdfGen.generateRDF(false);
	}
	
	private WorksheetR2RMLJenaModelParser loadModel(R2RMLMappingIdentifier modelIdentifier) throws JSONException, KarmaException {
		WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(modelIdentifier);
		this.readModelParsers.put(modelIdentifier.getName(), parser);
		return parser;
	}
}
