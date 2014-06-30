package edu.isi.karma.rdf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class GenericRDFGenerator extends RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(GenericRDFGenerator.class);
	protected HashMap<String, R2RMLMappingIdentifier> modelIdentifiers;
	protected HashMap<String, WorksheetR2RMLJenaModelParser> readModelParsers;

	public enum InputType {
		CSV,
		JSON,
		XML
	};
	
	public GenericRDFGenerator() {
		super();
		this.modelIdentifiers = new HashMap<String, R2RMLMappingIdentifier>();
		this.readModelParsers = new HashMap<String, WorksheetR2RMLJenaModelParser>();
	}

	public void addModel(R2RMLMappingIdentifier modelIdentifier) {
		this.modelIdentifiers.put(modelIdentifier.getName(), modelIdentifier);
	}

	public void generateRDF(String modelName, String sourceName, String data, InputType dataType, boolean addProvenance,
			KR2RMLRDFWriter writer) throws KarmaException, JSONException, IOException {
		generateRDF(modelName, sourceName, data, dataType, -1, addProvenance, writer);
	}
	
	public void generateRDF(String modelName, String sourceName, String data, InputType dataType, int maxNumLines, boolean addProvenance,
			KR2RMLRDFWriter writer) throws KarmaException, JSONException, IOException {
		List<KR2RMLRDFWriter> writers = new LinkedList<KR2RMLRDFWriter>();
		writers.add(writer);
		generateRDF(modelName, sourceName, data, dataType, maxNumLines, addProvenance, writers);
	}
	
	public void generateRDF(String modelName, String sourceName, String data, InputType dataType, boolean addProvenance,
			List<KR2RMLRDFWriter> writers ) throws KarmaException, JSONException, IOException {
		generateRDF(modelName, sourceName, data, dataType, -1, addProvenance, writers);
	}
	
	public void generateRDF(String modelName, String sourceName, String data, InputType dataType, int maxNumLines, boolean addProvenance,
			List<KR2RMLRDFWriter> writers ) throws KarmaException, JSONException, IOException {
		generateRDF(modelName, sourceName, IOUtils.toInputStream(data), dataType, maxNumLines, addProvenance, writers);
	}
	
	public void generateRDF(String modelName, String sourceName, InputStream data, InputType dataType,  
			boolean addProvenance, KR2RMLRDFWriter writer) throws KarmaException, IOException {
		generateRDF(modelName, sourceName, data, dataType, -1, addProvenance, writer);
	}
	
	public void generateRDF(String modelName, String sourceName, InputStream data, InputType dataType, int maxNumLines, 
			boolean addProvenance, KR2RMLRDFWriter writer) throws KarmaException, IOException {
		List<KR2RMLRDFWriter> writers = new LinkedList<KR2RMLRDFWriter>();
		writers.add(writer);
		generateRDF(modelName, sourceName, data, dataType, maxNumLines, addProvenance, writers);
	}
	
	public void generateRDF(String modelName, File inputFile, InputType inputType,
			boolean addProvenance, KR2RMLRDFWriter writer) throws KarmaException, IOException {
		generateRDF(modelName, inputFile, inputType, -1, addProvenance, writer);
	}
	
	public void generateRDF(String modelName, File inputFile, InputType inputType, int maxNumLines,
			boolean addProvenance, KR2RMLRDFWriter writer) throws KarmaException, IOException {
		List<KR2RMLRDFWriter> writers = new LinkedList<KR2RMLRDFWriter>();
		writers.add(writer);
		generateRDF(modelName, inputFile, inputType, maxNumLines, addProvenance, writers);
	}
	
	public void generateRDF(String modelName, File inputFile, InputType inputType, 
			boolean addProvenance, List<KR2RMLRDFWriter> writers) throws KarmaException, IOException {
		generateRDF(modelName, inputFile, inputType, -1, addProvenance, writers);
	}
	
	public void generateRDF(String modelName, File inputFile, InputType inputType, int maxNumLines,
			boolean addProvenance, List<KR2RMLRDFWriter> writers) throws KarmaException, IOException {
		String sourceName = inputFile.getName();
		InputStream is = new FileInputStream(inputFile);
		generateRDF(modelName, sourceName, is, inputType, maxNumLines, addProvenance, writers);
	}
	
	public void generateRDF(String modelName, String sourceName, InputStream data, InputType dataType,
			boolean addProvenance, List<KR2RMLRDFWriter> writers) throws KarmaException, IOException  {
		generateRDF(modelName, sourceName, data, dataType, -1, addProvenance, writers);
	}
	
	public void generateRDF(String modelName, String sourceName, InputStream data, InputType dataType, int maxNumLines, 
			boolean addProvenance, List<KR2RMLRDFWriter> writers)
			throws KarmaException, IOException {
		logger.debug("Generating rdf for " + sourceName);
		
		Workspace workspace = initializeWorkspace();
		R2RMLMappingIdentifier id = this.modelIdentifiers.get(modelName);
		if(id == null) {
			throw new KarmaException("Cannot generate RDF. Model named " + modelName + " does not exist");
		}
		
		Worksheet worksheet = generateWorksheet(sourceName, new BufferedInputStream(data), dataType, 
				workspace, maxNumLines);
		
		//Check if the parser for this model exists, else create one
		WorksheetR2RMLJenaModelParser modelParser = readModelParsers.get(modelName);
		if(modelParser == null) {
			modelParser = loadModel(id);
		}
		
		//Generate mappping data for the worksheet using the model parser
		KR2RMLMapping mapping = modelParser.parse();
		
		applyHistoryToWorksheet(workspace, worksheet, mapping);

		//Generate RDF using the mapping data
		ErrorReport errorReport = new ErrorReport();
		
		KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet,
		        workspace.getFactory(), workspace.getOntologyManager(), writers,
		        addProvenance, mapping, errorReport);
		rdfGen.generateRDF(true);
		removeWorkspace(workspace);
		
		logger.debug("Generated rdf for " + sourceName);
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
	
	public Map<String, R2RMLMappingIdentifier> getModels()
	{
		return Collections.unmodifiableMap(modelIdentifiers);
	}
	
	
	
	private Worksheet generateWorksheetFromDelimitedStream(String sourceName, InputStream is,
			Workspace workspace, String encoding, int maxNumLines) throws IOException,
			KarmaException, ClassNotFoundException {
		Worksheet worksheet;
		Import fileImport = new CSVImport(1, 2, ',', '\"', encoding, maxNumLines, sourceName, is, workspace);

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
}