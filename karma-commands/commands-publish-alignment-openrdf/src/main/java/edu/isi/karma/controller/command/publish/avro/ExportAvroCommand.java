package edu.isi.karma.controller.command.publish.avro;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.common.OSUtils;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.planning.RootStrategy;
import edu.isi.karma.kr2rml.planning.SteinerTreeRootStrategy;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.planning.WorksheetDepthRootStrategy;
import edu.isi.karma.kr2rml.writer.AvroKR2RMLRDFWriter;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ExportAvroCommand extends WorksheetSelectionCommand {

    private static Logger logger = LoggerFactory.getLogger(ExportAvroCommand.class);
	private final String alignmentNodeId;
	private String rdfPrefix;
	private String rdfNamespace;

	private enum JsonKeys {
		updateType, fileUrl, worksheetId
	}
    
	
	//TODO provde option to output pretty printed avro json
	public ExportAvroCommand(String id, String model, String alignmentNodeId, 
			String worksheetId, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.alignmentNodeId = alignmentNodeId;
		
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Export Avro";
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
		

		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(workspace.getId(),
						worksheetId));
		// Set the prefix and namespace to be used while generating RDF
		fetchRdfPrefixAndNamespaceFromPreferences(workspace);
		
		// Generate the KR2RML data structures for the RDF generation
		final ErrorReport errorReport = new ErrorReport();
		KR2RMLMappingGenerator mappingGen = null;
		try {
			mappingGen = new KR2RMLMappingGenerator(
					workspace, worksheet, alignment, 
					worksheet.getSemanticTypes(), rdfPrefix, rdfNamespace,
					false);
		} catch (KarmaException e)
		{
			logger.error("Error occured while generating RDF!", e);
			return new UpdateContainer(new ErrorUpdate("Error occured while generating RDF: " + e.getMessage()));
		}
		KR2RMLMapping mapping = mappingGen.getKR2RMLMapping();

		logger.debug(mapping.toString());
		
		//****************************************************************************************************/
		//*** Extract list of TripleMaps *************************************************************************************************/
		List<TriplesMap> triplesMapList = mapping.getTriplesMapList();
		

		String rootTriplesMapId = null;
		for(TriplesMap map: triplesMapList)
		{
			if(map.getSubject().getId().compareTo(alignmentNodeId) == 0)
			{
				rootTriplesMapId = map.getId();
				break;
			}
		}
		if(null == rootTriplesMapId)
		{
			String errmsg ="Invalid alignment id " + alignmentNodeId;
			logger.error(errmsg);
			return new UpdateContainer(new ErrorUpdate("Error occured while searching for root for JSON: " +errmsg));
		}
		final String avroFileName = workspace.getCommandPreferencesId() + worksheetId + "-" + 
				worksheet.getTitle().replaceAll("\\.", "_") +  "-export"+".avro"; 
		final String avroFileLocalPath = contextParameters.getParameterValue(ContextParameter.AVRO_PUBLISH_DIR) +  
				avroFileName;
		
		try {
			FileOutputStream fos = new FileOutputStream(new File(avroFileLocalPath));
			AvroKR2RMLRDFWriter writer = new AvroKR2RMLRDFWriter(fos);
			writer.addPrefixes(mapping.getPrefixes());
			RootStrategy strategy = new UserSpecifiedRootStrategy(rootTriplesMapId, new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy()));
			KR2RMLWorksheetRDFGenerator generator = new KR2RMLWorksheetRDFGenerator(worksheet, workspace, writer, 
					false, strategy, mapping, errorReport, selection);
			try {
				generator.generateRDF(true);
				logger.info("RDF written to file.");
			} catch (IOException e1) {
				logger.error("Error occured while generating RDF!", e1);
				return new UpdateContainer(new ErrorUpdate("Error occured while generating RDF: " + e1.getMessage()));
			}
			fos.flush();
			fos.close();
			if(OSUtils.isWindows())
				System.gc();  //Invoke gc for windows, else it gives error: The requested operation cannot be performed on a file with a user-mapped section open
			//when the model is republished, and the original model is earlier open
		} catch (FileNotFoundException e) {
			logger.error("File Not found", e);
			return new UpdateContainer(new ErrorUpdate("File Not found while generating RDF: " + e.getMessage()));
		} catch (IOException e) {
			logger.error("Error writing out  Not found", e);
			return new UpdateContainer(new ErrorUpdate("File Not found while generating RDF: " + e.getMessage()));
		}
			
		return new UpdateContainer(new AbstractUpdate() {
			
			@Override
			public void generateJson(String prefix, PrintWriter pw,	VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(),
							"PublishAvroUpdate");
					outputObject.put(JsonKeys.fileUrl.name(), 
							contextParameters.getParameterValue(ContextParameter.AVRO_PUBLISH_RELATIVE_DIR) + avroFileName);
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
