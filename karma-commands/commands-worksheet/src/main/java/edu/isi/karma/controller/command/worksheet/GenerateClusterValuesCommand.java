package edu.isi.karma.controller.command.worksheet;
 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.*;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.controller.update.WorksheetDeleteUpdate;

public class GenerateClusterValuesCommand extends Command {
	private String hNodeId;
	private String worksheetId;

	public GenerateClusterValuesCommand(String id, String hNodeId,
			String worksheetId) {
		super(id);
		this.hNodeId = hNodeId;
		this.worksheetId = worksheetId;
	}

	@Override
	public String getCommandName() {

		return this.getClass().getName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Generate Cluster Values";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "";
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		// TODO Auto-generated method stub
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<Node>();
		workspace.getFactory().getWorksheet(worksheetId).getDataTable()
				.collectNodes(selectedPath, nodes);

		
		try {
			JSONArray requestJsonArray = new JSONArray();	
			for (Node node : nodes) {

				String originalVal = node.getValue().asString();
				originalVal = originalVal == null ? "" : originalVal;
		 		requestJsonArray.put(originalVal);
			}
			String jsonString = null;
			jsonString = requestJsonArray.toString();

			// String url =
			// "http://localhost:8080/cleaningService/IdentifyData";

			String url = "http://localhost:8080/cluster-7.0/cluster/post/";

			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			HttpResponse response = null;
			HttpEntity entity;
			StringBuilder out = new StringBuilder();

			URI u = null;
			u = new URI(url);
			
			StringEntity se = new StringEntity(jsonString);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

			httppost = new HttpPost(u);
			httppost.setEntity(se);
			response = httpclient.execute(httppost);
			entity = response.getEntity();
			String line = new String();
			if (entity != null) {
				BufferedReader buf = new BufferedReader(new InputStreamReader(
						entity.getContent()));
				line = buf.readLine();
				
				while (line != null) {
					out.append(line);
					line = buf.readLine();
				}
			}
			
			
			JSONObject jsonAnnotation = new JSONObject();
			jsonAnnotation.put("worksheetId", worksheetId);
			jsonAnnotation.put("hNodeId", hNodeId);
			jsonAnnotation.put("id", id);
			jsonAnnotation.put("cluster", new JSONObject(out.toString()));
						
			
			JsonImport obj  = new JsonImport(out.toString(), "cluster", workspace, "UTF-8", -1 );
	
			UpdateContainer c = new UpdateContainer();
			
			Worksheet ws = obj.generateWorksheet();
			ws.setJsonAnnotation(jsonAnnotation);
			
			if(worksheet.getJsonAnnotation() != null )
			{
				JSONObject jsonAnnotationCluster = new JSONObject (worksheet.getJsonAnnotation().toString());
				
				DeleteWorksheetCommand deleteWorkseet = new DeleteWorksheetCommand(id, jsonAnnotationCluster.get("ClusterId").toString() );
				deleteWorkseet.doIt(workspace);
				jsonAnnotationCluster.remove("cluster");
				c.add(new WorksheetDeleteUpdate(jsonAnnotationCluster.get("ClusterId").toString()));
				
			}
			
			JSONObject ClusterAnnotation = new JSONObject();
			
			ClusterAnnotation.put("ClusterId",ws.getId());
			
			worksheet.setJsonAnnotation(ClusterAnnotation);
			c.add(new WorksheetListUpdate());
			c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(ws.getId()));
			
			return c;
			//return null;
		} catch (Exception e) {
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate("Error in Generating Values!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}