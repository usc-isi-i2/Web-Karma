package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class WorksheetSelectionListUpdate extends AbstractUpdate {

	private String worksheetId;
	private String hTableId;
	public WorksheetSelectionListUpdate(String worksheetId, String hTableId) {
		this.worksheetId = worksheetId;
		this.hTableId = hTableId;
	}
	
	public enum JsonKeys {
		updateType, worksheetId, selectionList, hTableId
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		Workspace workspace = vWorkspace.getWorkspace();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		JSONObject outputObject = new JSONObject();
		JSONArray array = new JSONArray();
		for (String name : worksheet.getSelectionManager().getAllDefinedSelection(hTableId)) {
			array.put(name);
		}
		outputObject.put(JsonKeys.updateType.name(),
				"WorksheetSelectionListUpdate");		
		outputObject.put(JsonKeys.worksheetId.name(),
				worksheetId);
		outputObject.put(JsonKeys.hTableId.name(),
				worksheetId);
		outputObject.put(JsonKeys.selectionList.name(),
				outputObject.toString());
		pw.println(outputObject.toString());

	}

}
