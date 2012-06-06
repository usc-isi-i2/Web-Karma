package edu.isi.karma.controller.history;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.history.CommandHistoryWriter.HistoryArguments;
import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class WorksheetCommandHistoryReader {
	private final String vWorksheetId;
	private final VWorkspace vWorkspace;
	
	public WorksheetCommandHistoryReader(String vWorksheetId, VWorkspace vWorkspace) {
		super();
		this.vWorksheetId = vWorksheetId;
		this.vWorkspace = vWorkspace;
	}

	public void readAndExecuteCommands() throws FileNotFoundException, JSONException, KarmaException, CommandException {
		String worksheetName = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet().getTitle();
		File historyFile = new File(HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(worksheetName, vWorkspace.getPreferencesId()));
		JSONArray historyJson = (JSONArray) JSONUtil.createJson(new FileReader(historyFile));
		
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(vWorkspace.getWorkspace().getId());
		HashMap<String, CommandFactory> commandFactoryMap = ctrl.getCommandFactoryMap();
		
		for (int i = 0; i< historyJson.length(); i++) {
			JSONObject commObject = (JSONObject) historyJson.get(i);
			JSONArray inputParamArr = (JSONArray) commObject.get(HistoryArguments.inputParameters.name());
			// Change the hNode ids, vworksheet id to point to the current worksheet ids
			if(normalizeJsonInput(inputParamArr)) {
//				System.out.println(inputParamArr.toString(4));
				// Invoke the command
				CommandFactory cf = commandFactoryMap.get(commObject.get(HistoryArguments.commandName.name()));
				if(cf != null && cf instanceof JSONInputCommandFactory) {
					JSONInputCommandFactory scf = (JSONInputCommandFactory)cf;
					Command comm = scf.createCommand(inputParamArr, vWorkspace);
					vWorkspace.getWorkspace().getCommandHistory().doCommand(comm, vWorkspace);
				}
			}
		}
	}

	private boolean normalizeJsonInput(JSONArray inputArr) throws JSONException {
		HTable hTable = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet().getHeaders();
		for (int i = 0; i < inputArr.length(); i++) {
			JSONObject inpP = inputArr.getJSONObject(i);
			
			/*** Check the input parameter type and accordingly make changes ***/
			if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.hNodeId) {
				JSONArray hNodeJSONRep = new JSONArray(inpP.getString(ClientJsonKeys.value.name()));
				for (int j=0; j<hNodeJSONRep.length(); j++) {
					JSONObject cNameObj = (JSONObject) hNodeJSONRep.get(j);
					if(hTable == null)
						return false;
					HNode node = hTable.getHNodeFromColumnName(cNameObj.getString("columnName"));
					if(node == null) {
						return false;
					}
					else {
						if (j == hNodeJSONRep.length()-1) {		// Found!
							inpP.put(ClientJsonKeys.value.name(), node.getId());
						} else {
							hTable = node.getNestedTable();
						}
					}
				}
			} else if(HistoryJsonUtil.getParameterType(inpP) == ParameterType.vWorksheetId) {
				inpP.put(ClientJsonKeys.value.name(), this.vWorksheetId);
			}
		}
		return true;
	}
}
