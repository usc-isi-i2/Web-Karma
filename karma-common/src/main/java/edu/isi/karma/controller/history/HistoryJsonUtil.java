package edu.isi.karma.controller.history;

import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.ICommand.CommandTag;
import edu.isi.karma.controller.history.CommandHistory.HistoryArguments;

public class HistoryJsonUtil {

	private HistoryJsonUtil() {
	}

	public enum ClientJsonKeys {
		isPrimary, name, value, type, SemanticType, id, children
	}
	
	public enum ParameterType {
		hNodeId, worksheetId, other, orderedColumns, hNodeIdList, linkWithHNodeId
	}
	
	public static JSONObject getJSONObjectWithName(String arg, JSONArray json) throws JSONException {
		for(int i=0; i<json.length(); i++) {
			JSONObject obj = json.getJSONObject(i);
			String nameS = obj.getString(ClientJsonKeys.name.name());
			if(nameS.equals(arg)) {
				return obj;
			}
		}
		return null;
	}
	
	public static JSONArray getJSONArrayValue(String arg, JSONArray json) throws JSONException {
		JSONObject obj = getJSONObjectWithName(arg, json);
		if(obj.has(ClientJsonKeys.value.name()))
			return 	obj.getJSONArray(ClientJsonKeys.value.name());
		else
			return new JSONArray();
	}
	
	public static ParameterType getParameterType(JSONObject json) throws JSONException {
		return ParameterType.valueOf(json.getString(ClientJsonKeys.type.name()));
	}
	
	public static boolean getBooleanValue(String arg, JSONArray json) throws JSONException {
		return getJSONObjectWithName(arg, json).getBoolean(ClientJsonKeys.value.name());
	}
	
	public static String getStringValue(String arg, JSONArray json) throws JSONException {
		return getJSONObjectWithName(arg, json).get(ClientJsonKeys.value.name()).toString();
	}

	public static boolean valueExits(String arg, JSONArray json) throws JSONException {
		return getJSONObjectWithName(arg, json) != null;
	}
	
	public static boolean setArgumentValue(String name, Object value,
			JSONArray inputJson) throws JSONException {
		JSONObject obj = getJSONObjectWithName(name, inputJson);
		if(obj != null) {
			obj.put(ClientJsonKeys.value.name(), value);
			return true;
		}
		return false;
	}
	
	public static boolean historyExists(String workspaceId, String worksheetId) {
		String filename = CommandHistory.getHistorySaver(workspaceId).getHistoryFilepath(worksheetId);
		File file = new File(filename);
		return file.exists();
	}
	
	public static JSONArray removeCommandsByTag(List<CommandTag> removeFilters,
			 JSONArray historyJson)
			throws JSONException {
		JSONArray commandsJSON = new JSONArray();
		
		for (int i = 0; i< historyJson.length(); i++) {
			JSONObject commObject = (JSONObject) historyJson.get(i);
			JSONArray tags = commObject.getJSONArray(HistoryArguments.tags.name());
			boolean match = false;
			for (int j=0; j< tags.length(); j++) {
				
				String tag2 = tags.getString(j);
				for(CommandTag filter : removeFilters)
				{
					if(tag2.equals(filter.name()))
					{
						match = true;
						break;
					}
					
				}
				if(match)
				{
					break;
				}
			}
			if(!match)
				commandsJSON.put(commObject);
		}
		return commandsJSON;
	}
	
	public static JSONArray filterCommandsByTag(List<CommandTag> filters,
			 JSONArray historyJson)
			throws JSONException {
		JSONArray commandsJSON = new JSONArray();
		
		for (int i = 0; i< historyJson.length(); i++) {
			JSONObject commObject = (JSONObject) historyJson.get(i);
			JSONArray tags = commObject.getJSONArray(HistoryArguments.tags.name());
			boolean match = false;
			for (int j=0; j< tags.length(); j++) {
				
				String tag2 = tags.getString(j);
				for(CommandTag filter : filters)
				{
					if(tag2.equals(filter.name()))
					{
						commandsJSON.put(commObject);
						match = true;
						break;
					}
					
				}
				if(match)
				{
					break;
				}
			}
		}
		return commandsJSON;
	}
	
	public static JSONObject createParamObject(String name, ParameterType type, Object value) {
		JSONObject paramObj = new JSONObject();
		paramObj.put("name", name);
		paramObj.put("type", type);
		paramObj.put("value", value);
		return paramObj;
	}
	
}
