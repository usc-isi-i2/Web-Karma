package edu.isi.karma.controller.command.publish;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.command.Command.CommandType;
import edu.isi.karma.controller.command.publish.PublishRDFCommand.PreferencesKeys;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMapping;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.PredicateObjectMap;
import edu.isi.karma.kr2rml.TriplesMap;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishJSONCommand extends WorksheetCommand {

    private static Logger logger = LoggerFactory.getLogger(PublishJSONCommand.class);
	private final String alignmentNodeId;
	private String rdfPrefix;
	private String rdfNamespace;
    
	public PublishJSONCommand(String id, String alignmentNodeId, String worksheetId) {
		super(id, worksheetId);
		this.alignmentNodeId = alignmentNodeId;
		
//		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Publish JSON";
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
		logger.info("Entered PublishJSONCommand");
		
		RepFactory f = workspace.getFactory();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(workspace.getId(),
						worksheetId));
	
		// Set the prefix and namespace to be used while generating RDF
		fetchRdfPrefixAndNamespaceFromPreferences(workspace);
		
		// Generate the KR2RML data structures for the RDF generation
		final ErrorReport errorReport = new ErrorReport();
		KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(
				workspace, worksheet, alignment, 
				worksheet.getSemanticTypes(), rdfPrefix, rdfNamespace,
				true, errorReport);
		KR2RMLMapping mapping = mappingGen.getKR2RMLMapping();
		TriplesMap triplesMap = mapping.getTriplesMapIndex().get(alignmentNodeId);

		logger.debug(mapping.toString());
		
		//****************************************************************************************************/
		logger.info(mapping.toString());
		logger.info("Got the mapping");
		

		//*** Extract list of TripleMaps *************************************************************************************************/
		List<TriplesMap> triplesMapList = mapping.getTriplesMapList();
//		TriplesMap triplesMap = triplesMapList.get(0);
		logger.info("Size: " + Integer.toString(triplesMapList.size()));

		
		//****************************************************************************************************/
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0, worksheet.getDataTable().getNumRows());
		logger.info("Rows: " + Integer.toString(rows.size()));
		
		ArrayList<JSONObject> JSONObjectsList = new ArrayList<JSONObject>();
		for (Row row:rows) {
			JSONObject obj = new JSONObject();
			Iterator<PredicateObjectMap> it = triplesMap.getPredicateObjectMaps().iterator();
			
			while (it.hasNext()) {
				PredicateObjectMap predicateObjectMap = it.next();
				String objectId = predicateObjectMap.getObject().getId();
				obj.put(predicateObjectMap.getPredicate().getTemplate().toString(), row.getNode(objectId).getValue().asString());
			}

			JSONObjectsList.add(obj);
		}
		
		logger.info("No. of JSON objects: " + JSONObjectsList.size());
		logger.info(JSONObjectsList.toString());

		// Prepare the output container
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		c.add(new InfoUpdate("JSON generation complete"));
		return c;
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
