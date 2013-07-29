package edu.isi.karma.controller.command.worksheet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class MultipleValueEditColumnCommandFactory extends CommandFactory implements JSONInputCommandFactory{
	public enum Arguments {
		hNodeID, vWorksheetID, rows, rowID, value
	}

	@Override
	public Command createCommand(HttpServletRequest request, VWorkspace vWorkspace) {
		return null; // Not to be used
	}

	public Command createCommand(JSONArray inputJson, VWorkspace vWorkspace) throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structues to be passed to the command **/
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeID.name(), inputJson);
		String vWorksheetID = CommandInputJSONUtil.getStringValue(Arguments.vWorksheetID.name(), inputJson);
		JSONArray rows = CommandInputJSONUtil.getJSONArrayValue(Arguments.rows.name(), inputJson);
		
		Map<String, String> rowValueMap = new HashMap<String, String>();
		for (int i=0; i<rows.length(); i++) {
			JSONObject rowObj = rows.getJSONObject(i);
			rowValueMap.put(rowObj.getString(Arguments.rowID.name()), 
					rowObj.getString(Arguments.value.name()));
		}
		return new MultipleValueEditColumnCommand(getNewId(vWorkspace), vWorksheetID, hNodeID, rowValueMap);
	}

}
