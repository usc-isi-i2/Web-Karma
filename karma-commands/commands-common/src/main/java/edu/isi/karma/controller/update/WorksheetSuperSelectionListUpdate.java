package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class WorksheetSuperSelectionListUpdate extends AbstractUpdate {

	private String worksheetId;
	public WorksheetSuperSelectionListUpdate(String worksheetId) {
		this.worksheetId = worksheetId;
	}
	
	public enum JsonKeys {
		updateType, worksheetId, selectionList
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		Workspace workspace = vWorkspace.getWorkspace();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		JSONObject outputObject = new JSONObject();
		JSONArray array = new JSONArray();
		for (SuperSelection sel : worksheet.getSuperSelectionManager().getAllDefinedSelection()) {
			JSONObject obj = new JSONObject();
			obj.put("name", sel.getName());
			obj.put("status", sel.refreshStatus().name());
			array.put(obj);
		}
		outputObject.put(JsonKeys.updateType.name(),
				"WorksheetSuperSelectionListUpdate");		
		outputObject.put(JsonKeys.worksheetId.name(),
				worksheetId);
		outputObject.put(JsonKeys.selectionList.name(),
				array.toString());
		pw.println(outputObject.toString());

	}
	
	public boolean equals(Object o) {
		if (o instanceof WorksheetSuperSelectionListUpdate) {
			WorksheetSuperSelectionListUpdate t = (WorksheetSuperSelectionListUpdate)o;
			return t.worksheetId.equals(worksheetId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.worksheetId != null ? this.worksheetId.hashCode() : 0;
	}
}
