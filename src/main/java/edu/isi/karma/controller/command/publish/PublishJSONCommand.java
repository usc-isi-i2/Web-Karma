package edu.isi.karma.controller.command.publish;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.publish.PublishRDFCommand.PreferencesKeys;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMapping;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.TriplesMap;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PublishJSONCommand extends Command {

    private static Logger logger = LoggerFactory.getLogger(PublishJSONCommand.class);
	private final String worksheetId;
	private String rdfSourcePrefix;
	private String rdfSourceNamespace;
	private String addInverseProperties;
	private boolean saveToStore;
	private String hostName;
	private String dbName;
	private String userName;
	private String password;
	private String modelName;
	private String worksheetName;
	private String tripleStoreUrl;
	private String graphUri;
	private boolean replaceContext;

	protected PublishJSONCommand(String id, String worksheetId,
			String publicRDFAddress, String rdfSourcePrefix, String rdfSourceNamespace, String addInverseProperties,
			String saveToStore,String hostName,String dbName,String userName,String password, String modelName, String tripleStoreUrl,
			String graphUri, boolean replace) {
		super(id);
		this.worksheetId = worksheetId;
		this.rdfSourcePrefix = rdfSourcePrefix;
		this.rdfSourceNamespace = rdfSourceNamespace;
		this.addInverseProperties = addInverseProperties;
		this.saveToStore=Boolean.valueOf(saveToStore);
		this.hostName=hostName;
		this.dbName=dbName;
		this.userName=userName;
		this.password=password;
		if(modelName==null || modelName.trim().isEmpty())
			this.modelName="karma";
		else
			this.modelName=modelName;
		this.tripleStoreUrl = tripleStoreUrl;
		this.graphUri = graphUri;
		this.replaceContext = replace;

	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Generate JSON";
	}

	@Override
	public String getDescription() {
		return this.worksheetName;
		}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
		}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		logger.info("Entered PublishJSONCommand");
		
		//save the preferences 
		savePreferences(workspace);

		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		this.worksheetName = worksheet.getTitle();
		
		// Prepare the file path and names
		final String rdfFileName = workspace.getCommandPreferencesId() + worksheetId + ".ttl"; 
		final String rdfFileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) +  
				"publish/RDF/" + rdfFileName;

		// Get the alignment for this worksheet
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId));
		
		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Please align the worksheet before generating RDF!"));
		}
		
		// Generate the KR2RML data structures for the RDF generation
		final ErrorReport errorReport = new ErrorReport();
		KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(workspace, worksheet,
				alignment, worksheet.getSemanticTypes(), rdfSourcePrefix, rdfSourceNamespace, 
				Boolean.valueOf(addInverseProperties), errorReport);
		
		KR2RMLMapping mapping = mappingGen.getKR2RMLMapping();
		logger.debug(mapping.toString());
		
		//****************************************************************************************************/
		logger.info(mapping.toString());
		logger.info("Got the mapping");
		

		//*** Extract list of TripleMaps *************************************************************************************************/
		List<TriplesMap> triplesMapList = mapping.getTriplesMapList();
		logger.info("Size: " + Integer.toString(triplesMapList.size()));

		
		//****************************************************************************************************/
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0, worksheet.getDataTable().getNumRows());
		logger.info("Rows: " + Integer.toString(rows.size()));
		
		for (Row row:rows) {
			JSONObject obj = new JSONObject();
			
			obj.put(triplesMapList.get(0).getPredicateObjectMaps().get(0).getPredicate().toString(),
					triplesMapList.get(0).getPredicateObjectMaps().get(0).getObject().toString());
			logger.info(obj.toString());
			
			logger.info(row.getId());
			break;
		}


		// Prepare the output container
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		c.add(new InfoUpdate("JSON generation complete"));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	
	private void savePreferences(Workspace workspace){
		try{
			JSONObject prefObject = new JSONObject();
			prefObject.put(PreferencesKeys.addInverseProperties.name(), addInverseProperties);
			prefObject.put(PreferencesKeys.rdfPrefix.name(), rdfSourcePrefix);
			prefObject.put(PreferencesKeys.rdfNamespace.name(), rdfSourceNamespace);
			prefObject.put(PreferencesKeys.saveToStore.name(), saveToStore);
			prefObject.put(PreferencesKeys.dbName.name(), dbName);
			prefObject.put(PreferencesKeys.hostName.name(), hostName);
			prefObject.put(PreferencesKeys.modelName.name(), modelName);
			prefObject.put(PreferencesKeys.userName.name(), userName);
			prefObject.put(PreferencesKeys.rdfSparqlEndPoint.name(), tripleStoreUrl);
			workspace.getCommandPreferences().setCommandPreferences(
					"PublishRDFCommandPreferences", prefObject);
			
			/*
			logger.debug("I Saved .....");
			ViewPreferences prefs = vWorkspace.getPreferences();
			JSONObject prefObject1 = prefs.getCommandPreferencesJSONObject("PublishRDFCommandPreferences");
			logger.debug("I Saved ....."+prefObject1);
			 */
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
