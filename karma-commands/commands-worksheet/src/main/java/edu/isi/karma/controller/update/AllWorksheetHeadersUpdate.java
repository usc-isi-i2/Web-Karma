package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.alignment.SemanticTypes;
import edu.isi.karma.view.VHNode;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class AllWorksheetHeadersUpdate extends AbstractUpdate {
	private String worksheetId;
	private enum JsonKeys {
		worksheetId, columns, name, id, visible, hideable, children
	}
	
	public AllWorksheetHeadersUpdate(String worksheetId) {
		super();
		this.worksheetId = worksheetId;
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		
		try {
			JSONObject response = new JSONObject();
			response.put(JsonKeys.worksheetId.name(), worksheetId);
			response.put(AbstractUpdate.GenericJsonKeys.updateType.name(), 
					this.getClass().getSimpleName());
			
			Worksheet wk = vWorksheet.getWorksheet();
			List<VHNode> viewHeaders = vWorksheet.getHeaderViewNodes();
			
			JSONArray columns = getColumnsJsonArray(viewHeaders, wk.getSemanticTypes());
			response.put(JsonKeys.columns.name(), columns);
			
			pw.println(response.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
	
	private JSONArray getColumnsJsonArray(List<VHNode> viewHeaders, SemanticTypes semTypes) {
		JSONArray columns = new JSONArray();
		for(VHNode headerNode : viewHeaders) {
			JSONObject column = new JSONObject();
			column.put(JsonKeys.id.name(), headerNode.getId());
			
		
			column.put(JsonKeys.name.name(), headerNode.getColumnName());
			column.put(JsonKeys.visible.name(), headerNode.isVisible());
			boolean hideable = (semTypes.getSemanticTypeForHNodeId(headerNode.getId()) == null) ? true : false;
			
			
			if(headerNode.hasNestedTable()) {
				JSONArray children = getColumnsJsonArray(headerNode.getNestedNodes(), semTypes);
				column.put(JsonKeys.children.name(), children);
				if(hideable) {
					//check if any of the children are not hideable, then this is not hideable
					for(int i=0; i<children.length(); i++) {
						JSONObject child = children.getJSONObject(i);
						if(child.getBoolean("hideable") == false) {
							hideable = false;
							break;
						}
					}
				}
			}
			
			column.put(JsonKeys.hideable.name(), hideable);
			columns.put(column);
		}
		return columns;
	}
	
	public boolean equals(Object o) {
		if (o instanceof AllWorksheetHeadersUpdate) {
			AllWorksheetHeadersUpdate t = (AllWorksheetHeadersUpdate)o;
			return t.worksheetId.equals(worksheetId);
		}
		return false;
	}

}
