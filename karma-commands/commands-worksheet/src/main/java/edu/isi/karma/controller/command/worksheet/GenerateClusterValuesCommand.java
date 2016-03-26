package edu.isi.karma.controller.command.worksheet;
 

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDeleteUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.HTTPUtil;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GenerateClusterValuesCommand extends WorksheetSelectionCommand {
	private String hNodeId;

	private static Logger logger = LoggerFactory.getLogger(GenerateClusterValuesCommand.class.getSimpleName());
	
	public GenerateClusterValuesCommand(String id, String model, String hNodeId,
			String worksheetId, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
	}

	@Override
	public String getCommandName() {

		return this.getClass().getName();
	}

	@Override
	public String getTitle() {
		return "Generate Cluster Values";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<>();
		workspace.getFactory().getWorksheet(worksheetId).getDataTable()
				.collectNodes(selectedPath, nodes, selection);

		
		try {
			JSONArray requestJsonArray = new JSONArray();	
			for (Node node : nodes) {

				String originalVal = node.getValue().asString();
				originalVal = originalVal == null ? "" : originalVal;
		 		requestJsonArray.put(originalVal);
			}
			String jsonString = null;
			jsonString = requestJsonArray.toString();

			String url = contextParameters.getParameterValue(
					ContextParameter.CLUSTER_SERVICE_URL); 
			
			logger.info("Execute Cluster Service:" + url);
			
			StringEntity se = new StringEntity(jsonString);
			String reqResponse = HTTPUtil.executeHTTPPostRequest(url, "application/json",
					null, se);
			
			JSONObject jsonAnnotation = new JSONObject();
			jsonAnnotation.put("worksheetId", worksheetId);
			jsonAnnotation.put("hNodeId", hNodeId);
			jsonAnnotation.put("id", id);
			jsonAnnotation.put("cluster", new JSONObject(reqResponse));
						
			
			JsonImport obj  = new JsonImport(reqResponse, "cluster", workspace, "UTF-8", -1 );
	
			UpdateContainer c = new UpdateContainer();
			
			Worksheet ws = obj.generateWorksheet();
			ws.setJsonAnnotation(jsonAnnotation);
			
			if(worksheet.getJsonAnnotation() != null )
			{
				JSONObject jsonAnnotationCluster = new JSONObject (worksheet.getJsonAnnotation().toString());
				String clusterWorksheetId = jsonAnnotationCluster.get("ClusterId").toString();
				if(workspace.getWorksheet(clusterWorksheetId) != null) {
					DeleteWorksheetCommand deleteWorkseet = new DeleteWorksheetCommand(id, model, clusterWorksheetId );
					deleteWorkseet.doIt(workspace);
					c.add(new WorksheetDeleteUpdate(clusterWorksheetId));
				}
				jsonAnnotationCluster.remove("cluster");
			}
			
			JSONObject ClusterAnnotation = new JSONObject();
			
			ClusterAnnotation.put("ClusterId",ws.getId());
			
			worksheet.setJsonAnnotation(ClusterAnnotation);
			c.add(new WorksheetListUpdate());
			c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(ws.getId(), getSuperSelection(ws), workspace.getContextId()));
			
			return c;
			//return null;
		} catch (Exception e) {
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate("Error in Generating Values!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}