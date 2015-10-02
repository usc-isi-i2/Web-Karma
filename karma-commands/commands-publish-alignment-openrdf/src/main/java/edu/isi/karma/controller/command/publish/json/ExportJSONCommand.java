package edu.isi.karma.controller.command.publish.json;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.ReaderInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.isi.karma.common.OSUtils;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.kr2rml.ContextGenerator;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMappingWriter;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.planning.RootStrategy;
import edu.isi.karma.kr2rml.planning.SteinerTreeRootStrategy;
import edu.isi.karma.kr2rml.planning.TriplesMap;
import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
import edu.isi.karma.kr2rml.planning.WorksheetDepthRootStrategy;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ExportJSONCommand extends WorksheetSelectionCommand {

	private static Logger logger = LoggerFactory.getLogger(ExportJSONCommand.class);
	private final String alignmentNodeId;
	private String rdfPrefix;
	private String rdfNamespace;
	private boolean contextFromModel;
	private String contextJSON;
	private String contextURL;
	private enum JsonKeys {
		updateType, fileUrl, worksheetId, contextUrl
	}

	public ExportJSONCommand(String id, String model, String alignmentNodeId, 
			String worksheetId, String selectionId, 
			boolean contextFromModel, String contextJSON,
			String contextURL) {
		super(id, model, worksheetId, selectionId);
		this.alignmentNodeId = alignmentNodeId;
		this.contextFromModel = contextFromModel;
		this.contextJSON = contextJSON;
		this.contextURL = contextURL;
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
		
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				AlignmentManager.Instance().constructAlignmentId(workspace.getId(),
						worksheetId));
		workspace.getOntologyManager();
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
		//		TriplesMap triplesMap = mapping.getTriplesMapIndex().get(alignmentNodeId);
		if (contextFromModel) {
			KR2RMLMappingWriter writer;
			try {
				StringWriter string = new StringWriter();
				PrintWriter pw = new PrintWriter(string);
				writer = new KR2RMLMappingWriter();
				writer.addR2RMLMapping(mapping, worksheet, workspace);
				writer.writeR2RMLMapping(pw);
				writer.close();
				pw.flush();
				pw.close();
				if(OSUtils.isWindows())
					System.gc();  //Invoke gc for windows, else it gives error: The requested operation cannot be performed on a file with a user-mapped section open
				//when the model is republished, and the original model is earlier open
				
				Model model = ModelFactory.createDefaultModel();
				InputStream s = new ReaderInputStream(new StringReader(string.toString()));
				model.read(s, null, "TURTLE");
				contextJSON = new ContextGenerator(model, true).generateContext().toString();

			} catch (Exception e) {

			}

		}
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
		// create JSONKR2RMLRDFWriter
		final String jsonFileName = workspace.getCommandPreferencesId() + worksheetId + "-" + 
				worksheet.getTitle().replaceAll("\\.", "_") +  "-export"+".json"; 
		final String jsonFileLocalPath = contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_DIR) +  
				jsonFileName;
		final String contextName = workspace.getCommandPreferencesId() + worksheetId + "-" + worksheet.getTitle().replaceAll("\\.", "_") +  "-context.json";
		final String jsonContextFileLocalPath = contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_DIR) + contextName;
		PrintWriter printWriter;
		try {
			printWriter = new PrintWriter(jsonFileLocalPath);
			String baseURI = worksheet.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.baseURI);
			JSONKR2RMLRDFWriter writer = new JSONKR2RMLRDFWriter(printWriter, baseURI);
			if((contextJSON == null || contextJSON.trim().isEmpty()) && contextURL != null && !contextURL.isEmpty())
			{
				try{
					contextJSON = IOUtils.toString(new URL(contextURL).openStream());	
				}
				catch (Exception e)
				{
					logger.error("Unable to read context from URL", e);
				}
				
			}
			if (contextJSON != null && !contextJSON.isEmpty()) {
				JSONObject context = new JSONObject();
				try {
					context = new JSONObject(this.contextJSON);
				}catch(Exception e)
				{

				}

				PrintWriter pw = new PrintWriter(jsonContextFileLocalPath);
				pw.println(context.toString(4));
				pw.close();
				if(contextURL == null || contextURL.trim().isEmpty())
				{
					StringBuilder url = new StringBuilder();
					url.append(contextParameters.getParameterValue(ContextParameter.JETTY_HOST));
					url.append(":");
					url.append(contextParameters.getParameterValue(ContextParameter.JETTY_PORT));
					url.append("/");
					url.append(contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_RELATIVE_DIR));
					url.append(contextName);
					contextURL = url.toString();
				}
				writer.setGlobalContext(context, new ContextIdentifier(context.toString(), new URL(contextURL)));
			}
			writer.addPrefixes(mapping.getPrefixes());
			RootStrategy strategy = new UserSpecifiedRootStrategy(rootTriplesMapId, new SteinerTreeRootStrategy(new WorksheetDepthRootStrategy()));
			KR2RMLWorksheetRDFGenerator generator = new KR2RMLWorksheetRDFGenerator(worksheet, workspace, writer, false, strategy, mapping, errorReport, selection);
			try {
				generator.generateRDF(true);
				logger.info("RDF written to file.");
			} catch (IOException e1) {
				logger.error("Error occured while generating RDF!", e1);
				return new UpdateContainer(new ErrorUpdate("Error occured while generating RDF: " + e1.getMessage()));
			}
			printWriter.close();
		} catch (FileNotFoundException | MalformedURLException e) {
			logger.error("File Not found", e);
			return new UpdateContainer(new ErrorUpdate("File Not found while generating RDF: " + e.getMessage()));
		}

		return new UpdateContainer(new AbstractUpdate() {

			@Override
			public void generateJson(String prefix, PrintWriter pw,	VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(),
							"PublishJSONUpdate");
					outputObject.put(JsonKeys.fileUrl.name(), 
							contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_RELATIVE_DIR) + jsonFileName);
					if (contextJSON != null && !contextJSON.isEmpty()) {
						outputObject.put(JsonKeys.contextUrl.name(), 
								contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_RELATIVE_DIR) + contextName);
					}
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
