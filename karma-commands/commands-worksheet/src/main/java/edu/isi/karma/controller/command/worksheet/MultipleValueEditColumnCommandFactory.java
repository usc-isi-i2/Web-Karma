package edu.isi.karma.controller.command.worksheet;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

public class MultipleValueEditColumnCommandFactory extends JSONInputCommandFactory{
	public enum Arguments {
		hNodeID, worksheetId, rows, rowID, value
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null; // Not to be used
	}

	public Command createCommand(JSONArray inputJson, Workspace workspace) throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structues to be passed to the command **/
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeID.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		JSONArray rows = CommandInputJSONUtil.getJSONArrayValue(Arguments.rows.name(), inputJson);
		
		Map<String, String> rowValueMap = new HashMap<String, String>();
		for (int i=0; i<rows.length(); i++) {
			JSONObject rowObj = rows.getJSONObject(i);
			rowValueMap.put(rowObj.getString(Arguments.rowID.name()), 
					rowObj.getString(Arguments.value.name()));
		}
		return new MultipleValueEditColumnCommand(getNewId(workspace), worksheetId, hNodeID, rowValueMap);
	}

	@Override
	protected Class<? extends Command> getCorrespondingCommand()
	{
		return MultipleValueEditColumnCommand.class;
	}
}
