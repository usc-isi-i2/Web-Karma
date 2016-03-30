package edu.isi.karma.rdf;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.avro.AvroImport;
import edu.isi.karma.imp.csv.CSVImport;
import edu.isi.karma.imp.excel.ToCSV;
import edu.isi.karma.imp.json.JsonImport;
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
import edu.isi.karma.rdf.InputProperties.InputProperty;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MediaTypeRegistry;
import org.apache.tika.mime.MimeTypes;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GenericRDFGenerator extends RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(GenericRDFGenerator.class);
	protected ConcurrentHashMap<String, R2RMLMappingIdentifier> modelIdentifiers;
	protected ConcurrentHashMap<String, WorksheetR2RMLJenaModelParser> readModelParsers;
	protected HashMap<String, ContextIdentifier> contextIdentifiers;
	protected HashMap<String, JSONObject> contextCache;
	
	
	public enum InputType {
		CSV,
		JSON,
		XML,
		AVRO,
		EXCEL,
		JL
	};
	
	public GenericRDFGenerator() {
		this(null);
	}
	
	public GenericRDFGenerator(String selectionName) {
		super(selectionName);
		this.modelIdentifiers = new ConcurrentHashMap<>();
		this.readModelParsers = new ConcurrentHashMap<>();
		this.contextCache = new HashMap<>();
		this.contextIdentifiers = new HashMap<>();
	}

	public void addModel(R2RMLMappingIdentifier modelIdentifier) {
		
		if(!modelIdentifiers.containsKey(modelIdentifier.getName())){
			this.modelIdentifiers.put(modelIdentifier.getName(), modelIdentifier);
		}
		
	}
	
	public void addContext(ContextIdentifier id) {
		this.contextIdentifiers.put(id.getName(), id);
	}
	
	public WorksheetR2RMLJenaModelParser getModelParser(String modelName) throws JSONException, KarmaException {
		WorksheetR2RMLJenaModelParser modelParser = readModelParsers.get(modelName);
		R2RMLMappingIdentifier id = this.modelIdentifiers.get(modelName);
		if(modelParser == null) {
			modelParser = loadModel(id);
		}
		return modelParser;
	}
	
	private void generateRDF(String modelName, String sourceName,String contextName, InputStream data, InputType dataType,  InputProperties inputTypeParameters, 
			boolean addProvenance, List<KR2RMLRDFWriter> writers, RootStrategy rootStrategy, 
			List<String> tripleMapToKill, List<String> tripleMapToStop, List<String> POMToKill, ServletContextParameterMap contextParameters)
					throws KarmaException, IOException {
		
		R2RMLMappingIdentifier id = this.modelIdentifiers.get(modelName);
		ContextIdentifier contextId = this.contextIdentifiers.get(contextName);
		if(id == null) {
			throw new KarmaException("Cannot generate RDF. Model named " + modelName + " does not exist");
		}
		JSONObject context;
		if (contextId == null) {
			context = new JSONObject();
		}
		else {
			context = this.contextCache.get(contextName);
		}
		if (context == null) {
			try {
				context = loadContext(contextId);
			}catch(Exception e) {
				context = new JSONObject();
			}
		}
		for (KR2RMLRDFWriter writer : writers) {
			if (writer instanceof JSONKR2RMLRDFWriter) {
				JSONKR2RMLRDFWriter t = (JSONKR2RMLRDFWriter)writer;
				t.setGlobalContext(context, contextId);
			}
			
			writer.setR2RMLMappingIdentifier(id);
			
		}
		//Check if the parser for this model exists, else create one
		WorksheetR2RMLJenaModelParser modelParser = getModelParser(modelName);
		generateRDF(modelParser, sourceName, data, dataType, inputTypeParameters, addProvenance, writers, rootStrategy, tripleMapToKill, tripleMapToStop, POMToKill, contextParameters);
	}
	
	private void generateRDF(WorksheetR2RMLJenaModelParser modelParser, String sourceName, InputStream data, InputType dataType,  InputProperties inputTypeParameters,
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
			Worksheet worksheet = generateWorksheet(sourceName, new BufferedInputStream(data), dataType, inputTypeParameters,
					workspace);
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
		InputStream inputStream = null;
		if(request.getInputFile() != null)
		{
			inputStream = new FileInputStream(request.getInputFile());
		}
		else if(request.getInputData() != null)
		{
			inputStream = IOUtils.toInputStream(request.getInputData(), Charset.forName("UTF-8"));
			request.setEncoding("UTF-8");
		}
		else if(request.getInputStream() != null)
		{
			inputStream = request.getInputStream();
		}
		
		generateRDF(request.getModelName(), request.getSourceName(), request.getContextName(), 
				inputStream, request.getDataType(), request.getInputTypeProperties(), request.isAddProvenance(), 
				request.getWriters(), request.getStrategy(), 
				request.getTripleMapToKill(), request.getTripleMapToStop(), request.getPOMToKill(), request.getContextParameters());
	}
	
	private InputType getInputType(Metadata metadata) {
		String[] contentType = metadata.get(Metadata.CONTENT_TYPE).split(";");
		switch (contentType[0]) {
			case "application/json" : {
				return InputType.JSON;
			}
			case "application/xml": {
				return InputType.XML;
			}
			case "text/csv": {
				return InputType.CSV;
			}
			case "text/excel": {
				return InputType.EXCEL;
			}
			case "text/x-excel": {
				return InputType.EXCEL;
			}
		}
		return null;
	}
	
	protected Worksheet generateWorksheet(String sourceName, BufferedInputStream is, InputType inputType, InputProperties inputParameters,
			Workspace workspace) throws IOException, KarmaException {
		Worksheet worksheet = null;
		try{
			is.mark(Integer.MAX_VALUE);
			String encoding = null;
			if(inputType == null) {
				Metadata metadata = new Metadata();
				metadata.set(Metadata.RESOURCE_NAME_KEY, sourceName);
				DefaultDetector detector = new DefaultDetector();
				MediaType type = detector.detect(is, metadata);
		
				ContentHandler contenthandler = new BodyContentHandler();
				AutoDetectParser parser = new AutoDetectParser();
				try {
					parser.parse(is, contenthandler, metadata);
				} catch (SAXException | TikaException e) {
					logger.error("Unable to parse stream: " + e.getMessage());
					throw new KarmaException("Unable to parse stream: "
							+ e.getMessage());
				}
				MediaTypeRegistry registry = MimeTypes.getDefaultMimeTypes()
						.getMediaTypeRegistry();
				registry.addSuperType(new MediaType("text", "csv"), new MediaType(
						"text", "plain"));
				MediaType parsedType = MediaType.parse(metadata
						.get(Metadata.CONTENT_TYPE));
		
				if (registry.isSpecializationOf(registry.normalize(type), registry
						.normalize(parsedType).getBaseType())) {
					metadata.set(Metadata.CONTENT_TYPE, type.toString());
				}
				logger.info("Detected " + metadata.get(Metadata.CONTENT_TYPE));
				inputType = getInputType(metadata);
				encoding = metadata.get(Metadata.CONTENT_ENCODING);
			} else if(inputParameters.get(InputProperty.ENCODING) != null) {
				encoding = (String)inputParameters.get(InputProperty.ENCODING);
			} else {
				encoding = EncodingDetector.detect(is);
			}
			is.reset();
			
			if(inputType == null) {
		     	throw new KarmaException("Content type unrecognized");
		     }
			
			inputParameters.set(InputProperty.ENCODING, encoding);
			
			switch (inputType) {
				case JSON : {
	
					worksheet = generateWorksheetFromJSONStream(sourceName, is, inputParameters,
							workspace);
					break;
				}
				case XML : {
					worksheet = generateWorksheetFromXMLStream(sourceName, is, inputParameters,
							workspace);
					break;
				}
				case CSV : {
					worksheet = generateWorksheetFromDelimitedStream(sourceName,
							is, inputParameters, workspace);
					break;
				}
				case EXCEL: {
					worksheet = generateWorksheetFromExcelStream(sourceName, is, inputParameters, workspace);
					break;
				}
				case AVRO : {
					worksheet = generateWorksheetFromAvroStream(sourceName, is, inputParameters, workspace);
					break;
				}
				case JL: {
					worksheet = generateWorksheetFromJLStream(sourceName, is, inputParameters, workspace);
				}
				
			}
		} catch (Exception e ) {
			logger.error("Error generating worksheet", e);
			throw new KarmaException("Unable to generate worksheet: " + e.getMessage());
		}
		if(worksheet == null) {
	     	throw new KarmaException("Content type unrecognized");
	     }
		return worksheet;
	}

	private synchronized WorksheetR2RMLJenaModelParser loadModel(R2RMLMappingIdentifier modelIdentifier) throws JSONException, KarmaException {
		if(readModelParsers.containsKey(modelIdentifier.getName()))
		{
			return readModelParsers.get(modelIdentifier.getName());
		}
		WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(modelIdentifier);
		this.readModelParsers.put(modelIdentifier.getName(), parser);
		return parser;
	}
	
	public JSONObject loadContext(ContextIdentifier id) throws IOException {
		if (contextCache.containsKey(id.getName())) {
			return contextCache.get(id.getName());
		}
		InputStream jsonStream;
		if(id.getContent() != null)
			jsonStream = IOUtils.toInputStream(id.getContent(), "utf-8");
		else
			jsonStream = id.getLocation().openStream();
		JSONTokener token = new JSONTokener(new InputStreamReader(jsonStream));
		JSONObject obj = new JSONObject(token);
		this.contextCache.put(id.getName(), obj);
		return obj;
	}
	
	public Map<String, R2RMLMappingIdentifier> getModels()
	{
		return Collections.unmodifiableMap(modelIdentifiers);
	}
	
	private Worksheet generateWorksheetFromExcelStream(String sourceName, InputStream is,  InputProperties inputTypeParams,
			Workspace workspace) throws IOException,
			KarmaException, ClassNotFoundException, InvalidFormatException {
		
		
		int worksheetIndex =  (inputTypeParams.get(InputProperty.WORKSHEET_INDEX) != null)? 
				(int)inputTypeParams.get(InputProperty.WORKSHEET_INDEX) : 1;
				
		 // Convert the Excel file to a CSV file.
        ToCSV csvConverter = new ToCSV();
        StringWriter writer = new StringWriter();
        csvConverter.convertWorksheetToCSV(is, worksheetIndex-1, writer);
        String csv= writer.toString();
        InputStream sheet = IOUtils.toInputStream(csv);
        return this.generateWorksheetFromDelimitedStream(sourceName, sheet, inputTypeParams, workspace);	
	}
	
	private Worksheet generateWorksheetFromDelimitedStream(String sourceName, InputStream is,  InputProperties inputTypeParams,
			Workspace workspace) throws IOException,
			KarmaException, ClassNotFoundException {
		Worksheet worksheet;
		int headerStartIndex =  (inputTypeParams.get(InputProperty.HEADER_START_INDEX) != null)? 
				(int)inputTypeParams.get(InputProperty.HEADER_START_INDEX) : 1;
		int dataStartIndex =  (inputTypeParams.get(InputProperty.DATA_START_INDEX) != null)? 
				(int)inputTypeParams.get(InputProperty.DATA_START_INDEX) : 2;
		char delimiter = (inputTypeParams.get(InputProperty.DELIMITER) != null)? 
				((String)inputTypeParams.get(InputProperty.DELIMITER)).charAt(0): ',';
		
		char qualifier = (inputTypeParams.get(InputProperty.TEXT_QUALIFIER) != null)? 
					((String)inputTypeParams.get(InputProperty.TEXT_QUALIFIER)).charAt(0): '\"';
					
		String encoding = (String)inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)? 
				(int)inputTypeParams.get(InputProperty.MAX_NUM_LINES) : -1;
		
		Import fileImport = new CSVImport(headerStartIndex, dataStartIndex, delimiter, qualifier, encoding, maxNumLines, 
				sourceName, is, workspace, null);

		worksheet = fileImport.generateWorksheet();
		return worksheet;
	}

	private Worksheet generateWorksheetFromXMLStream(String sourceName, InputStream is,  InputProperties inputTypeParams,
			Workspace workspace)
			throws IOException {
		Worksheet worksheet;
		String encoding = (String)inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)? 
				(int)inputTypeParams.get(InputProperty.MAX_NUM_LINES) : -1;
				
		String contents = IOUtils.toString(is, encoding);
		JSONObject json = XML.toJSONObject(contents);
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}

	private Worksheet generateWorksheetFromJSONStream(String sourceName, InputStream is, InputProperties inputTypeParams,
			Workspace workspace)
			throws IOException {
		Worksheet worksheet;
		String encoding = (String)inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)? 
				(int)inputTypeParams.get(InputProperty.MAX_NUM_LINES) : -1;
		Reader reader = EncodingDetector.getInputStreamReader(is, encoding);
		Object json = JSONUtil.createJson(reader);
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}
	private Worksheet generateWorksheetFromAvroStream(String sourceName, InputStream is,  InputProperties inputTypeParams,
			Workspace workspace)
			throws IOException, JSONException, KarmaException {
		Worksheet worksheet;
		String encoding = (String)inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)? 
				(int)inputTypeParams.get(InputProperty.MAX_NUM_LINES) : -1;
		AvroImport imp = new AvroImport(is, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}
	
	private Worksheet generateWorksheetFromJLStream(String sourceName, InputStream is, InputProperties inputTypeParams,
			Workspace workspace) throws Exception{
		Worksheet worksheet;
		String encoding = (String)inputTypeParams.get(InputProperty.ENCODING);
		int maxNumLines = (inputTypeParams.get(InputProperty.MAX_NUM_LINES) != null)? (int)inputTypeParams.get(InputProperty.MAX_NUM_LINES) : -1;
		Object json=JSONUtil.convertJSONLinesToJSONArray(is,encoding);
		
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
		
	}
}