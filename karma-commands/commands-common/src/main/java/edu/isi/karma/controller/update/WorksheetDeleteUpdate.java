package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class WorksheetDeleteUpdate extends AbstractUpdate {
	private String worksheetId;
	private static Logger logger =LoggerFactory.getLogger(WorksheetDeleteUpdate.class);
	
	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}
	
	public WorksheetDeleteUpdate(String worksheetId) {
		this.worksheetId = worksheetId;
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject outputObject = new JSONObject();
		try {
			outputObject.put(GenericJsonKeys.updateType.name(),
					"WorksheetDeleteUpdate");
			
			outputObject.put(JsonKeys.worksheetId.name(),
					worksheetId);
			pw.println(outputObject.toString(4));
		} catch (JSONException e) {
			logger.error("Error occured while generating JSON!");
		}

	}
	@Override
	public void applyUpdate(VWorkspace vWorkspace) {
		
		VWorksheet vws = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		vWorkspace.getViewFactory().removeWorksheet(vws.getId());
	}
	
	public boolean equals(Object o) {
		if (o instanceof WorksheetDeleteUpdate) {
			WorksheetDeleteUpdate t = (WorksheetDeleteUpdate)o;
			return t.worksheetId.equals(worksheetId);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.worksheetId != null ? this.worksheetId.hashCode() : 0;
	}
}
