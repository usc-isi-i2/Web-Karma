package edu.isi.karma.controller.command.worksheet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class MultipleValueEditColumnCommandFactory extends JSONInputCommandFactory{
	public enum Arguments {
		hNodeID, worksheetId, rows, rowID, value
	}

	@Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return null; // Not to be used
	}

	public Command createCommand(JSONArray inputJson, String model, Workspace workspace) throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structues to be passed to the command **/
		String hNodeID = CommandInputJSONUtil.getStringValue(Arguments.hNodeID.name(), inputJson);
		String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
		JSONArray rows = CommandInputJSONUtil.getJSONArrayValue(Arguments.rows.name(), inputJson);
		
		Map<String, String> rowValueMap = new HashMap<>();
		for (int i=0; i<rows.length(); i++) {
			JSONObject rowObj = rows.getJSONObject(i);
			rowValueMap.put(rowObj.getString(Arguments.rowID.name()), 
					rowObj.getString(Arguments.value.name()));
		}
		return new MultipleValueEditColumnCommand(getNewId(workspace), model, worksheetId, hNodeID, rowValueMap);
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand()
	{
		return MultipleValueEditColumnCommand.class;
	}
}
