package edu.isi.karma.rdf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.kr2rml.planning.RootStrategy;
import edu.isi.karma.kr2rml.planning.SteinerTreeRootStrategy;
import edu.isi.karma.kr2rml.planning.WorksheetDepthRootStrategy;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rdf.util.JSONLDContextManager;
import edu.isi.karma.rdf.util.R2RMLMappingManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;

public class GenericRDFGenerator extends RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(GenericRDFGenerator.class);

	protected R2RMLMappingManager mappingManager = new R2RMLMappingManager();
	protected JSONLDContextManager contextManager = new JSONLDContextManager();
	
	public enum InputType {
		CSV,
		JSON,
		XML,
		AVRO,
		EXCEL,
		JL,
		OBJECT
	};
	
	public GenericRDFGenerator() {
		this(null);
	}
	
	public GenericRDFGenerator(String selectionName) {
		super(selectionName);
	}

	public void addModel(R2RMLMappingIdentifier id) {
		this.mappingManager.addModel(id);
	}

    public void addContext(ContextIdentifier id) {
    	this.contextManager.addContext(id);
    }
    
    public JSONObject getContext(ContextIdentifier id) throws IOException {
    	return this.contextManager.getContext(null, id);
    }
    
    public WorksheetR2RMLJenaModelParser getModelParser(String modelName) throws JSONException, KarmaException {
    	return this.mappingManager.getModelParser(modelName);
    }
    
	private void generateRDF(String modelName, String sourceName,String contextName, RDFGeneratorInputWrapper input, InputType dataType,  InputProperties inputTypeParameters, 
			boolean addProvenance, List<KR2RMLRDFWriter> writers, RootStrategy rootStrategy, 
			List<String> tripleMapToKill, List<String> tripleMapToStop, List<String> POMToKill, ServletContextParameterMap contextParameters)
					throws KarmaException, IOException {
		
		R2RMLMappingIdentifier id = mappingManager.getMappingIdentifierByName(modelName);
		ContextIdentifier contextId = contextManager.getContextIdentifier(contextName);
		JSONObject context = contextManager.getContext(contextName, contextId);
		initializeRDFWriters(writers, id, contextId, context);
		//Check if the parser for this model exists, else create one
		WorksheetR2RMLJenaModelParser modelParser = mappingManager.getModelParser(modelName);
		generateRDF(modelParser, sourceName, input, dataType, inputTypeParameters, addProvenance, writers, rootStrategy, tripleMapToKill, tripleMapToStop, POMToKill, contextParameters);
	}

	private void initializeRDFWriters(List<KR2RMLRDFWriter> writers, R2RMLMappingIdentifier id,
			ContextIdentifier contextId, JSONObject context) {
		for (KR2RMLRDFWriter writer : writers) {
			if (writer instanceof JSONKR2RMLRDFWriter) {
				JSONKR2RMLRDFWriter t = (JSONKR2RMLRDFWriter)writer;
				t.setGlobalContext(context, contextId);
			}
			writer.setR2RMLMappingIdentifier(id);
		}
	}

	
	private void generateRDF(WorksheetR2RMLJenaModelParser modelParser, String sourceName, RDFGeneratorInputWrapper input, InputType dataType,  InputProperties inputTypeParameters,
			boolean addProvenance, List<KR2RMLRDFWriter> writers, RootStrategy rootStrategy, 
			List<String> tripleMapToKill, List<String> tripleMapToStop, List<String> POMToKill, ServletContextParameterMap contextParameters) throws KarmaException, IOException {
		logger.debug("Generating rdf for " + sourceName);
		
		if(contextParameters == null)
		{
			contextParameters = ContextParametersRegistry.getInstance().getDefault();
			logger.debug("No context specified.  Defaulting to: " + contextParameters.getKarmaHome());
		}
		logger.debug("Initializing workspace for {}", sourceName);
		Workspace workspace = initializeWorkspace(contextParameters);
		logger.debug("Initialized workspace for {}", sourceName);
		try
		{
		
			logger.debug("Generating worksheet for {}", sourceName);
			Worksheet worksheet = null;
			if(input.getHeaders() != null) {
				worksheet = WorksheetGenerator.generateWorksheet(sourceName, input.getHeaders(), input.getValues(), workspace);
			}
			else {
				InputStream data = input.getInputAsStream();
				worksheet = WorksheetGenerator.generateWorksheet(sourceName, new BufferedInputStream(data), dataType, inputTypeParameters,
					workspace);
			}
			logger.debug("Generated worksheet for {}", sourceName);
			logger.debug("Parsing mapping for {}", sourceName);
			//Generate mappping data for the worksheet using the model parser
			KR2RMLMapping mapping = modelParser.parse();
			logger.debug("Parsed mapping for {}", sourceName);
			applyHistoryToWorksheet(workspace, worksheet, mapping);
			SuperSelection selection = SuperSelectionManager.DEFAULT_SELECTION;
			if (selectionName != null && !selectionName.trim().isEmpty())
				selection = worksheet.getSuperSelectionManager().getSuperSelection(selectionName);
			if (selection == null)
				return;
			//Generate RDF using the mapping data
			ErrorReport errorReport = new ErrorReport();
			if(rootStrategy == null)
			{
				rootStrategy = new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy());
			}
			logger.debug("Generating output for {}", sourceName);
			KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet,
			        workspace, writers,
			        addProvenance, rootStrategy, tripleMapToKill, tripleMapToStop, POMToKill, 
			        mapping, errorReport, selection);
			rdfGen.generateRDF(true);
			logger.debug("Generated output for {}", sourceName);
		}
		catch( Exception e)
		{
			logger.error("Error occurred while generating RDF", e);
			throw new KarmaException(e.getMessage());
		}
		finally
		{
			removeWorkspace(workspace);
		}
		
		logger.debug("Generated rdf for {}", sourceName);
	}
	
	public void generateRDF(RDFGeneratorRequest request) throws KarmaException, IOException
	{
		generateRDF(request.getModelName(), request.getSourceName(), request.getContextName(), 
				request.getInput(), request.getDataType(), request.getInputTypeProperties(), request.isAddProvenance(), 
				request.getWriters(), request.getStrategy(), 
				request.getTripleMapToKill(), request.getTripleMapToStop(), request.getPOMToKill(), request.getContextParameters());
	}

	

}