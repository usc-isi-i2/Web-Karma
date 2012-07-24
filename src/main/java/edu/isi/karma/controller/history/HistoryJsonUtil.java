package edu.isi.karma.controller.history;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class HistoryJsonUtil {
	
	public enum ClientJsonKeys {
		isPrimary, name, value, type, SemanticType
	}
	
	public enum ParameterType {
		hNodeId, vWorksheetId, other, checkHistory
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
	
	public static ParameterType getParameterType(JSONObject json) throws JSONException {
		return ParameterType.valueOf(json.getString(ClientJsonKeys.type.name()));
	}
	
	public static boolean getBooleanValue(String arg, JSONArray json) throws JSONException {
		return getJSONObjectWithName(arg, json).getBoolean(ClientJsonKeys.value.name());
	}
	
	public static String getStringValue(String arg, JSONArray json) throws JSONException {
		return getJSONObjectWithName(arg, json).getString(ClientJsonKeys.value.name());
	}

	public static boolean historyExists(String worksheetName, String vworkspacePreferenceId) {
		File histFile = new File(constructWorksheetHistoryJsonFilePath(worksheetName, vworkspacePreferenceId));
		return histFile.exists();
	}
	
	public static String constructWorksheetHistoryJsonFilePath (String worksheetName, String vworkspacePreferenceId) {
		return ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + 
				"publish/History/" + constructWorksheetHistoryJsonFileName(worksheetName, vworkspacePreferenceId);
	}
	
	public static String constructWorksheetHistoryJsonFileName (String worksheetName, String vworkspacePreferenceId) {
		return vworkspacePreferenceId + "_" + worksheetName.replaceAll("[\\./]", "") + ".json";
	}

	public static boolean setArgumentValue(String name, Object value,
			JSONArray inputJson) throws JSONException {
		JSONObject obj = getJSONObjectWithName(name, inputJson);
		if(obj != null) {
			obj.put(ClientJsonKeys.value.name(), value);
			return true;
		} else
			return false;
	}

//	private double getDoubleValue(String arg, JSONArray json) throws JSONException {
//	return getJSONObjectWithName(arg, json).getDouble(ClientJsonKeys.value.name());
//}
}
