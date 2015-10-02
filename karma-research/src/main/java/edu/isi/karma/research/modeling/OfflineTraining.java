package edu.isi.karma.research.modeling;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.text.DecimalFormat;

import org.apache.commons.io.FilenameUtils;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.avro.AvroImport;
import edu.isi.karma.imp.csv.CSVImport;
import edu.isi.karma.imp.excel.ToCSV;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.metadata.OntologyMetadata;
import edu.isi.karma.metadata.SemanticTypeModelMetadata;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.rdf.InputProperties;
import edu.isi.karma.rdf.InputProperties.InputProperty;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.rep.metadata.Tag;
import edu.isi.karma.rep.metadata.TagsContainer.Color;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;
import edu.isi.karma.semantictypes.evaluation.EvaluateMRR;
import edu.isi.karma.semantictypes.evaluation.MRRItem;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;
import edu.isi.karma.webserver.WorkspaceKarmaHomeRegistry;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class OfflineTraining {

	private static Logger logger = LoggerFactory.getLogger(OfflineTraining.class);

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

	protected Workspace initializeWorkspace(ServletContextParameterMap contextParameters) throws KarmaException {

		Workspace workspace = WorkspaceManager.getInstance().createWorkspace(contextParameters.getId());
		WorkspaceRegistry.getInstance().register(new ExecutionController(workspace));
		WorkspaceKarmaHomeRegistry.getInstance().register(workspace.getId(), contextParameters.getKarmaHome());
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().register(contextParameters.getId());        
		modelingConfiguration.setManualAlignment();
		// to load the ontologies in the /preloaded-ontologies folder
		OntologyMetadata omd = new OntologyMetadata(contextParameters);
		omd.setup(new UpdateContainer(), workspace);
		SemanticTypeModelMetadata stmd = new SemanticTypeModelMetadata(contextParameters);
		stmd.setup(new UpdateContainer(), workspace);
		Tag outlierTag = new Tag(TagName.Outlier, Color.Red);
		workspace.getTagsContainer().addTag(outlierTag);
		
		return workspace;

	}

	protected void removeWorkspace(Workspace workspace) {
		WorkspaceManager.getInstance().removeWorkspace(workspace.getId());
		WorkspaceRegistry.getInstance().deregister(workspace.getId());
		WorkspaceKarmaHomeRegistry.getInstance().deregister(workspace.getId());
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
			}

			}
			
			if (worksheet != null)
				AlignmentManager.Instance().createAlignment(workspace.getId(), worksheet.getId(), workspace.getOntologyManager());

		} catch (Exception e ) {
			logger.error("Error generating worksheet", e);
			throw new KarmaException("Unable to generate worksheet: " + e.getMessage());
		}
		if(worksheet == null) {
			throw new KarmaException("Content type unrecognized");
		}
		return worksheet;
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
	
	protected void applyHistoryToWorksheet(Workspace workspace, Worksheet worksheet,
			KR2RMLMapping mapping) throws JSONException {
		WorksheetCommandHistoryExecutor wchr = new WorksheetCommandHistoryExecutor(worksheet.getId(), workspace);
		try
		{
			wchr.executeAllCommands(new JSONArray(mapping.getWorksheetHistoryString()));
//			List<CommandTag> tags = new ArrayList<CommandTag>();
//			tags.add(CommandTag.Transformation);
//			wchr.executeCommandsByTags(tags, new JSONArray(mapping.getWorksheetHistoryString()));
		}
		catch (CommandException | KarmaException e)
		{
			logger.error("Unable to execute column transformations", e);
		}
	}

	public SemanticModel applyModel(
			ServletContextParameterMap contextParameters,
			File source, 
			String sourceName,
			InputType dataType,
			File model, 
			boolean train, 
			boolean predict) throws 
			FileNotFoundException, 
			JSONException, KarmaException, MalformedURLException {

		if (source == null || model  == null)
			return null;
		
		InputStream data = new FileInputStream(source);
		R2RMLMappingIdentifier rmlID = new R2RMLMappingIdentifier(model.getAbsolutePath(), model.toURI().toURL());
		WorksheetR2RMLJenaModelParser modelParser = new WorksheetR2RMLJenaModelParser(rmlID);
		InputProperties inputTypeParameters = new InputProperties();

		ModelingConfiguration mConf = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextParameters.getId());
		mConf.setTrainOnApplyHistory(train);
		mConf.setPredictOnApplyHistory(predict);
		
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
			
			if (predict) {
				Alignment alignment = AlignmentManager.Instance().getAlignment(AlignmentManager.
						Instance().constructAlignmentId(workspace.getId(), worksheet.getId()));
				SuperSelection selection = SuperSelectionManager.DEFAULT_SELECTION;
				if (alignment == null) {
					logger.error("alignment is null!");
					return null;
				}
				SemanticModel semanticModel = new SemanticModel(workspace, worksheet, worksheet.getTitle(), alignment.getSteinerTree(), selection);
				semanticModel.setName(worksheet.getTitle());
				return semanticModel;
			}
			
			return null;

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

	}
	

	private InputType getDataType(String fileName) {
		String ext = FilenameUtils.getExtension(fileName);
		if (ext.equalsIgnoreCase("csv")) return InputType.CSV;
		else if (ext.equalsIgnoreCase("xml")) return InputType.XML;
		else if (ext.equalsIgnoreCase("json")) return InputType.JSON;
		else if (ext.equalsIgnoreCase("xsls")) return InputType.EXCEL;
		return null;
	}

	public SemanticModel getCorrectModel(ServletContextParameterMap contextParameters, 
			File[] trainingSources,
			File[] trainingModels,
			File testSource,
			File testModel,
			int numberOfCandidates) throws JSONException, KarmaException, IOException {
		
		ModelingConfiguration mConf = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextParameters.getId());
		boolean ontologyAlignment = mConf.getOntologyAlignment();
		boolean knownModelsAlignment = mConf.getKnownModelsAlignment();
		boolean learner = mConf.isLearnerEnabled();
		boolean addOntologyPaths = mConf.getAddOntologyPaths();
		
		mConf.setAddOntologyPaths(false);
		mConf.setKnownModelsAlignment(false);
		mConf.setLearnerEnabled(false);
		mConf.setOntologyAlignment(false);

		InputType trainingDataType = null;
		for (int i = 0; i < trainingSources.length; i++) {
			File f = trainingSources[i];
			String sourceName = f.getName();
			trainingDataType = getDataType(f.getName());
			if (trainingDataType == null) continue;
			applyModel(contextParameters, f, sourceName, trainingDataType, trainingModels[i], true, false);
		}

		if (testSource == null && testModel == null) 
			return null;

		InputType testDataType = getDataType(testSource.getName());
		if (testDataType == null) return null;
		SemanticModel sm = applyModel(contextParameters, testSource, testSource.getName(), testDataType, testModel, false, true);

		String modelJson = contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + sm.getName() + "." + trainingSources.length + ".model.json";
		String modelGraphviz = contextParameters.getParameterValue(ContextParameter.GRAPHVIZ_MODELS_DIR) + sm.getName() + "." + trainingSources.length + ".model.dot";
		String evaluateMRR = contextParameters.getParameterValue(ContextParameter.EVALUATE_MRR) + sm.getName() + "." + trainingSources.length + ".mrr.json";

		try {
			sm.writeJson(modelJson);
		} catch (Exception e) {
			logger.error("error in exporting the model to JSON!");
			//			e.printStackTrace();
		}
		try {
			sm.writeGraphviz(modelGraphviz, false, false);
		} catch (Exception e) {
			logger.error("error in exporting the model to GRAPHVIZ!");
			//			e.printStackTrace();
		}
		EvaluateMRR.printEvaluatedJSON(modelJson, evaluateMRR);

		MRRItem mrrItem = EvaluateMRR.calculateMRRValue(modelJson, numberOfCandidates);
		
		sm.setAccuracy(roundDecimals(mrrItem.getAccuracy(),2));
		sm.setMrr(roundDecimals(mrrItem.getMrr(),2));

		mConf.setAddOntologyPaths(ontologyAlignment);
		mConf.setKnownModelsAlignment(knownModelsAlignment);
		mConf.setLearnerEnabled(learner);
		mConf.setOntologyAlignment(addOntologyPaths);
		
//		String modelJson = contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + testSource.getName() + "." + trainingSources.length + ".model.json";
//		MRRItem mrrItem = EvaluateMRR.calculateMRRValue(modelJson, numberOfCandidates);
//		SemanticModel sm = SemanticModel.readJson(modelJson);
//		sm.setAccuracy(roundDecimals(mrrItem.getAccuracy(),2));
//		sm.setMrr(roundDecimals(mrrItem.getMrr(),2));
		
		return sm;
	}
	
	private static double roundDecimals(double d, int k) {
		String format = "";
		for (int i = 0; i < k; i++) format += "#";
        DecimalFormat DForm = new DecimalFormat("#." + format);
        return Double.valueOf(DForm.format(d));
	}
	
	public SemanticModel getCorrectModel(ServletContextParameterMap contextParameters, 
			File trainingSource,
			File trainingModel,
			File testSource,
			File testModel,
			int index,
			int numberOfCandidates) throws JSONException, KarmaException, IOException {
		
		ModelingConfiguration mConf = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextParameters.getId());
		boolean ontologyAlignment = mConf.getOntologyAlignment();
		boolean knownModelsAlignment = mConf.getKnownModelsAlignment();
		boolean learner = mConf.isLearnerEnabled();
		boolean addOntologyPaths = mConf.getAddOntologyPaths();
		
		mConf.setAddOntologyPaths(false);
		mConf.setKnownModelsAlignment(false);
		mConf.setLearnerEnabled(false);
		mConf.setOntologyAlignment(false);

		if (trainingSource != null && trainingModel != null) {
			InputType trainingDataType = getDataType(trainingSource.getName());
			if (trainingDataType == null) return null;
			applyModel(contextParameters, trainingSource, trainingSource.getName(), trainingDataType, trainingModel, true, false);
		}

		if (testSource == null && testModel == null) 
			return null;
		
		InputType testDataType = getDataType(testSource.getName());
		if (testDataType == null) return null;
		SemanticModel sm = applyModel(contextParameters, testSource, testSource.getName(), testDataType, testModel, false, true);

		String modelJson = contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + sm.getName() + "." + index + ".model.json";
		String modelGraphviz = contextParameters.getParameterValue(ContextParameter.GRAPHVIZ_MODELS_DIR) + sm.getName() + "." + index + ".model.dot";
		String evaluateMRR = contextParameters.getParameterValue(ContextParameter.EVALUATE_MRR) + sm.getName() + "." + index + ".mrr.json";

		try {
			sm.writeJson(modelJson);
		} catch (Exception e) {
			logger.error("error in exporting the model to JSON!");
			//			e.printStackTrace();
		}
		try {
			sm.writeGraphviz(modelGraphviz, false, false);
		} catch (Exception e) {
			logger.error("error in exporting the model to GRAPHVIZ!");
			//			e.printStackTrace();
		}
		EvaluateMRR.printEvaluatedJSON(modelJson, evaluateMRR);
		
		MRRItem mrrItem = EvaluateMRR.calculateMRRValue(modelJson, numberOfCandidates);
		
		
		sm.setAccuracy(roundDecimals(mrrItem.getAccuracy(),2));
		sm.setMrr(roundDecimals(mrrItem.getMrr(),2));

		mConf.setAddOntologyPaths(ontologyAlignment);
		mConf.setKnownModelsAlignment(knownModelsAlignment);
		mConf.setLearnerEnabled(learner);
		mConf.setOntologyAlignment(addOntologyPaths);
		
//		String modelJson = contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + testSource.getName() + "." + index + ".model.json";
//		MRRItem mrrItem = EvaluateMRR.calculateMRRValue(modelJson, numberOfCandidates);
//		SemanticModel sm = SemanticModel.readJson(modelJson);
//		sm.setAccuracy(roundDecimals(mrrItem.getAccuracy(),2));
//		sm.setMrr(roundDecimals(mrrItem.getMrr(),2));
		
		return sm;
	}

}
