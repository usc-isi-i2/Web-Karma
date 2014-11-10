package edu.isi.karma.rdf;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.avro.AvroImport;
import edu.isi.karma.imp.csv.CSVImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.kr2rml.planning.RootStrategy;
import edu.isi.karma.kr2rml.planning.SteinerTreeRootStrategy;
import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.planning.WorksheetDepthRootStrategy;
import edu.isi.karma.kr2rml.writer.BloomFilterKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class GenericRDFGenerator extends RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(GenericRDFGenerator.class);
	protected HashMap<String, R2RMLMappingIdentifier> modelIdentifiers;
	protected HashMap<String, WorksheetR2RMLJenaModelParser> readModelParsers;
	protected HashMap<String, ContextIdentifier> contextIdentifiers;
	protected HashMap<String, JSONObject> contextCache;
	protected String rootTripleMap;
	protected List<String> tripleMapToKill;
	protected List<String> tripleMapToStop;
	protected List<String> POMToKill;
	public enum InputType {
		CSV,
		JSON,
		XML,
		AVRO
	};
	
	public GenericRDFGenerator(String selectionName) {
		super(selectionName);
		this.modelIdentifiers = new HashMap<String, R2RMLMappingIdentifier>();
		this.readModelParsers = new HashMap<String, WorksheetR2RMLJenaModelParser>();
		this.contextCache = new HashMap<String, JSONObject>();
		this.contextIdentifiers = new HashMap<String, ContextIdentifier>();
		tripleMapToKill = new ArrayList<String>();
		tripleMapToStop = new ArrayList<String>();
		POMToKill = new ArrayList<String>();
		rootTripleMap = "";
	}
	
	public GenericRDFGenerator(String selectionName, List<String> tripleMapToKill, 
			List<String> tripleMapToStop, List<String> POMToKill, String rootTripleMap) {
		super(selectionName);
		this.modelIdentifiers = new HashMap<String, R2RMLMappingIdentifier>();
		this.readModelParsers = new HashMap<String, WorksheetR2RMLJenaModelParser>();
		this.contextCache = new HashMap<String, JSONObject>();
		this.contextIdentifiers = new HashMap<String, ContextIdentifier>();
		this.tripleMapToKill = tripleMapToKill;
		this.tripleMapToStop = tripleMapToStop;
		this.POMToKill = POMToKill;
		this.rootTripleMap = rootTripleMap;
	}

	public void addModel(R2RMLMappingIdentifier modelIdentifier) {
		this.modelIdentifiers.put(modelIdentifier.getName(), modelIdentifier);
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
	
	private void generateRDF(String modelName, String sourceName,String contextName, InputStream data, InputType dataType, int maxNumLines, 
			boolean addProvenance, List<KR2RMLRDFWriter> writers, RootStrategy rootStrategy)
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
			if (writer instanceof BloomFilterKR2RMLRDFWriter) {
				BloomFilterKR2RMLRDFWriter t = (BloomFilterKR2RMLRDFWriter)writer;
				t.setR2RMLMappingIdentifier(id);
			}
		}
		//Check if the parser for this model exists, else create one
		WorksheetR2RMLJenaModelParser modelParser = getModelParser(modelName);
		generateRDF(modelParser, sourceName, data, dataType, maxNumLines, addProvenance, writers, rootStrategy);
	}
	
	

	private void generateRDF(WorksheetR2RMLJenaModelParser modelParser, String sourceName, InputStream data, InputType dataType, int maxNumLines, 
			boolean addProvenance, List<KR2RMLRDFWriter> writers, RootStrategy rootStrategy) throws KarmaException, IOException {
		logger.debug("Generating rdf for " + sourceName);
		
		Workspace workspace = initializeWorkspace();
		try
		{
		
		
				Worksheet worksheet = generateWorksheet(sourceName, new BufferedInputStream(data), dataType, 
					workspace, maxNumLines);
			
			
			//Generate mappping data for the worksheet using the model parser
			KR2RMLMapping mapping = modelParser.parse();
			
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
				if(rootTripleMap != null)
				{
					rootStrategy = new UserSpecifiedRootStrategy(rootTripleMap, new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy()));
				}
				else
				{
					rootStrategy = new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy());;
				}
			}
			KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet,
			        workspace.getFactory(), workspace.getOntologyManager(), writers,
			        addProvenance, rootStrategy, tripleMapToKill, tripleMapToStop, POMToKill, 
			        mapping, errorReport, selection);
			rdfGen.generateRDF(true);
		}
		catch( Exception e)
		{
			throw new KarmaException(e.getMessage());
		}
		finally
		{
			removeWorkspace(workspace);
		}
		
		logger.debug("Generated rdf for " + sourceName);
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
			inputStream = IOUtils.toInputStream(request.getInputData());
		}
		else if(request.getInputStream() != null)
		{
			inputStream = request.getInputStream();
		}
		
		generateRDF(request.getModelName(), request.getSourceName(), request.getContextName(), inputStream,request.getDataType(), request.getMaxNumLines(), request.isAddProvenance(), request.getWriters(), request.getStrategy());
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
		}
		return null;
	}
	
	protected Worksheet generateWorksheet(String sourceName, BufferedInputStream is, InputType inputType,
			Workspace workspace, int maxNumLines) throws IOException, KarmaException {
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
			} else {
				encoding = EncodingDetector.detect(is);
			}
			is.reset();
			
			if(inputType == null) {
		     	throw new KarmaException("Content type unrecognized");
		     }
			
			switch (inputType) {
				case JSON : {
	
					worksheet = generateWorksheetFromJSONStream(sourceName, is,
							workspace, encoding, maxNumLines);
					break;
				}
				case XML : {
					worksheet = generateWorksheetFromXMLStream(sourceName, is,
							workspace, encoding, maxNumLines);
					break;
				}
				case CSV : {
					worksheet = generateWorksheetFromDelimitedStream(sourceName,
							is, workspace, encoding, maxNumLines);
					break;
				}
				case AVRO : {
					worksheet = generateWorksheetFromAvroStream(sourceName, is, workspace, encoding, maxNumLines);
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


	private WorksheetR2RMLJenaModelParser loadModel(R2RMLMappingIdentifier modelIdentifier) throws JSONException, KarmaException {
		WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(modelIdentifier);
		this.readModelParsers.put(modelIdentifier.getName(), parser);
		return parser;
	}
	
	private JSONObject loadContext(ContextIdentifier id) throws IOException {
		JSONTokener token = new JSONTokener(id.getLocation().openStream());
		JSONObject obj = new JSONObject(token);
		this.contextCache.put(id.getName(), obj);
		return obj;
	}
	
	public Map<String, R2RMLMappingIdentifier> getModels()
	{
		return Collections.unmodifiableMap(modelIdentifiers);
	}
	
	
	
	private Worksheet generateWorksheetFromDelimitedStream(String sourceName, InputStream is,
			Workspace workspace, String encoding, int maxNumLines) throws IOException,
			KarmaException, ClassNotFoundException {
		Worksheet worksheet;
		Import fileImport = new CSVImport(1, 2, ',', '\"', encoding, maxNumLines, 
				sourceName, is, workspace, null);

		worksheet = fileImport.generateWorksheet();
		return worksheet;
	}

	private Worksheet generateWorksheetFromXMLStream(String sourceName, InputStream is,
			Workspace workspace, String encoding, int maxNumLines)
			throws IOException {
		Worksheet worksheet;
		String contents = IOUtils.toString(is, encoding);
		JSONObject json = XML.toJSONObject(contents);
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}

	private Worksheet generateWorksheetFromJSONStream(String sourceName, InputStream is,
			Workspace workspace, String encoding, int maxNumLines)
			throws IOException {
		Worksheet worksheet;
		Reader reader = EncodingDetector.getInputStreamReader(is, encoding);
		Object json = JSONUtil.createJson(reader);
		JsonImport imp = new JsonImport(json, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}
	private Worksheet generateWorksheetFromAvroStream(String sourceName, InputStream is,
			Workspace workspace, String encoding, int maxNumLines)
			throws IOException, JSONException, KarmaException {
		Worksheet worksheet;
		AvroImport imp = new AvroImport(is, sourceName, workspace, encoding, maxNumLines);
		worksheet = imp.generateWorksheet();
		return worksheet;
	}
}