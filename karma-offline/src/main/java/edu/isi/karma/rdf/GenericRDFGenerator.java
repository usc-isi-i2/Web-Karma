package edu.isi.karma.rdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.N3KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

public abstract class GenericRDFGenerator extends RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(GenericRDFGenerator.class);
	protected HashMap<String, R2RMLMappingIdentifier> modelIdentifiers;
	protected HashMap<String, WorksheetR2RMLJenaModelParser> readModelParsers;

	public GenericRDFGenerator() {
		super();
		this.modelIdentifiers = new HashMap<String, R2RMLMappingIdentifier>();
		this.readModelParsers = new HashMap<String, WorksheetR2RMLJenaModelParser>();
	}

	public void addModel(R2RMLMappingIdentifier modelIdentifier) {
		this.modelIdentifiers.put(modelIdentifier.getName(), modelIdentifier);
	}

	public void generateRDF(String sourceName, String data, boolean addProvenance,
			KR2RMLRDFWriter pw) throws KarmaException, JSONException, IOException {
				logger.debug("Generating rdf for " + sourceName);
				Workspace workspace = initializeWorkspace();
				
				generateRDF(sourceName, data, addProvenance, pw, workspace);
				logger.debug("Generated rdf for " + sourceName);
			}

	private void generateRDF(String sourceName, String data,
			boolean addProvenance, KR2RMLRDFWriter pw, Workspace workspace)
			throws KarmaException, IOException {
		R2RMLMappingIdentifier id = this.modelIdentifiers.get(sourceName);
		if(id == null) {
			throw new KarmaException("Cannot generate RDF. Model named " + sourceName + " does not exist");
		}
		
		Worksheet worksheet = generateWorksheet(sourceName, IOUtils.toInputStream(data),
				workspace, -1);
		
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
		        addProvenance, null, mapping, errorReport);
		rdfGen.generateRDF(true);
		removeWorkspace(workspace);
	}

	protected abstract Worksheet generateWorksheet(String sourceName, InputStream data,
			Workspace workspace, int maxNumLines) throws IOException, KarmaException ;

	public void generateRDF(String sourceName, String data, boolean addProvenance,
			PrintWriter pw) throws KarmaException, JSONException, IOException {
		
		logger.debug("Generating rdf for " + sourceName);
		Workspace workspace = initializeWorkspace();
		//Generate RDF using the mapping data
		ErrorReport errorReport = new ErrorReport();
				
		URIFormatter uriFormatter = new URIFormatter(workspace.getOntologyManager(), errorReport);
		KR2RMLRDFWriter outWriter = new N3KR2RMLRDFWriter(uriFormatter, pw);
		generateRDF(sourceName, data, addProvenance, outWriter, workspace);
		logger.debug("Generated rdf for " + sourceName);
		
	}

	private WorksheetR2RMLJenaModelParser loadModel(R2RMLMappingIdentifier modelIdentifier) throws JSONException, KarmaException {
		WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(modelIdentifier);
		this.readModelParsers.put(modelIdentifier.getName(), parser);
		return parser;
	}

}