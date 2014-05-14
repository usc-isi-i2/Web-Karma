package edu.isi.karma.controller.command.publish.json;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.worksheet.ExportCSVCommand.JsonKeys;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.ExportMongoDBUtil;
import edu.isi.karma.imp.json.JsonExport;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ExportJSONCommand extends WorksheetCommand {

    private static Logger logger = LoggerFactory.getLogger(ExportJSONCommand.class);
	private final String alignmentNodeId;
	private String rdfPrefix;
	private String rdfNamespace;

	private enum JsonKeys {
		updateType, fileUrl, worksheetId
	}
    
	public ExportJSONCommand(String id, String alignmentNodeId, String worksheetId) {
		super(id, worksheetId);
		this.alignmentNodeId = alignmentNodeId;
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Export JSON";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		logger.info("Entered ExportJSONCommand");

		
		RepFactory f = workspace.getFactory();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(workspace.getId(),
						worksheetId));
		OntologyManager ontMgr = workspace.getOntologyManager();
		// Set the prefix and namespace to be used while generating RDF
		fetchRdfPrefixAndNamespaceFromPreferences(workspace);
		
		// Generate the KR2RML data structures for the RDF generation
		final ErrorReport errorReport = new ErrorReport();
		KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(
				workspace, worksheet, alignment, 
				worksheet.getSemanticTypes(), rdfPrefix, rdfNamespace,
				true, errorReport);
		KR2RMLMapping mapping = mappingGen.getKR2RMLMapping();
//		TriplesMap triplesMap = mapping.getTriplesMapIndex().get(alignmentNodeId);

		logger.debug(mapping.toString());
		
		//****************************************************************************************************/
		logger.info(mapping.toString());
		logger.info("Got the mapping");

		//*** Extract list of TripleMaps *************************************************************************************************/
		List<TriplesMap> triplesMapList = mapping.getTriplesMapList();
		TriplesMap triplesMap = triplesMapList.get(3);
		logger.info("Size: " + Integer.toString(triplesMapList.size()));

		
		//****************************************************************************************************/
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0, worksheet.getDataTable().getNumRows());
		logger.info("Rows: " + Integer.toString(rows.size()));
		
		JSONArray JSONArray = new JSONArray();
		
		for (Row row:rows) {
			JSONObject obj = new JSONObject();
			Iterator<PredicateObjectMap> it = triplesMap.getPredicateObjectMaps().iterator();
			
			while (it.hasNext()) {
				PredicateObjectMap predicateObjectMap = it.next();
				String objectId = predicateObjectMap.getObject().getId();
				String key = predicateObjectMap.getPredicate().getTemplate().toString();
				Collection<Node> nodes = row.getNodes();
				String value = row.getNode(objectId).getValue().asString();
				obj.put(predicateObjectMap.getPredicate().getTemplate().toString(), row.getNode(objectId).getValue().asString());
			}
			JSONArray.put(obj);
		}
		
		logger.info("No. of JSON objects: " + JSONArray.length());
		logger.info(JSONArray.toString());

		// Prepare the output container
		//UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		//c.add(new InfoUpdate("JSON generation complete"));
		
		JsonExport jsonExport = new JsonExport(worksheet);
//		final String fileName = jsonExport.publishJSON(JSONArray.toString(4));
		
		// create JSONKR2RMLRDFWriter
		final String jsonFileName = workspace.getCommandPreferencesId() + worksheetId + "-" + 
				worksheet.getTitle().replaceAll("\\.", "_") +  "-export"+".json"; 
		final String jsonFileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.JSON_PUBLISH_DIR) +  
				jsonFileName;
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(jsonFileLocalPath);
			JSONKR2RMLRDFWriter writer = new JSONKR2RMLRDFWriter(printWriter);
			KR2RMLWorksheetRDFGenerator generator = new KR2RMLWorksheetRDFGenerator(worksheet, f, ontMgr, writer, false, mapping, errorReport);
			try {
				generator.generateRDF(true);
				logger.info("RDF written to file.");
			} catch (IOException e1) {
				logger.error("Error occured while generating RDF!", e1);
				return new UpdateContainer(new ErrorUpdate("Error occured while generating RDF: " + e1.getMessage()));
			}
		} catch (FileNotFoundException e) {
			logger.error("File Not found", e);
			return new UpdateContainer(new ErrorUpdate("File Not found while generating RDF: " + e.getMessage()));
		}
		
		ExportMongoDBUtil mongo = new ExportMongoDBUtil();
		try {
			mongo.publishMongoDB(JSONArray);
		} catch (Exception e) {
			logger.error("Error inserting into MongoDB." + e.getMessage());
		}
//		try {
//			mongo.publishMongoDB(fileName);
//		} catch (IOException e) {
//			logger.error("Error inserting into MongoDB." + e.getMessage());
//		}
		
		return new UpdateContainer(new AbstractUpdate() {
			
			@Override
			public void generateJson(String prefix, PrintWriter pw,	VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(),
							"PublishJSONUpdate");
					outputObject.put(JsonKeys.fileUrl.name(), 
							ServletContextParameterMap.getParameterValue(ContextParameter.JSON_PUBLISH_RELATIVE_DIR) + jsonFileName);
					outputObject.put(JsonKeys.worksheetId.name(),
							worksheetId);
					pw.println(outputObject.toString(4));
					
				} catch (JSONException e) {
					logger.error("Error occured while generating JSON!");
				}
			}
		});
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	private void fetchRdfPrefixAndNamespaceFromPreferences(Workspace workspace) {
		//get the rdf prefix from the preferences
		JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
		this.rdfNamespace = "http://localhost/source/";
		this.rdfPrefix = "s";
		if(prefObject!=null){
			this.rdfPrefix = prefObject.optString("rdfPrefix");
			this.rdfNamespace = prefObject.optString("rdfNamespace");
		}
		if(rdfPrefix==null || rdfPrefix.trim().isEmpty()) {
			this.rdfPrefix = "http://localhost/source/";
		}
	}
}
