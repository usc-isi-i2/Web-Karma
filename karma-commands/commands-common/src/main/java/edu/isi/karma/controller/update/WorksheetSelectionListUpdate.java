package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.selection.Selection;
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
	
	public WorksheetSelectionListUpdate(String worksheetId) {
		this.worksheetId = worksheetId;
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
		List<Selection> sels;
		if (hTableId != null)
			sels = worksheet.getSelectionManager().getAllDefinedSelection(hTableId);
		else
			sels = worksheet.getSelectionManager().getAllDefinedSelection();
		for (Selection sel : sels) {
			JSONObject obj = new JSONObject();
			obj.put("name", sel.getId());
			obj.put("status", sel.getStatus());
			array.put(obj);
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
	
	public boolean equals(Object o) {
		if (o instanceof WorksheetSelectionListUpdate) {
			WorksheetSelectionListUpdate t = (WorksheetSelectionListUpdate)o;
			if (t.hTableId == null)
				return t.worksheetId.equals(worksheetId) && hTableId == null;
			else
				return t.worksheetId.equals(worksheetId) && hTableId != null && t.hTableId.equals(hTableId);
		}
		return false;
	}

}
