package edu.isi.karma.controller.history;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryJsonUtil {
	
	public enum ClientJsonKeys {
		isPrimary, name, value, type
	}
	
	public enum ParameterType {
		hNodeId, vWorksheetId, other
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
		return new File(constructWorksheetHistoryJsonFilePath(worksheetName, vworkspacePreferenceId)).exists();
	}
	
	public static String constructWorksheetHistoryJsonFilePath (String worksheetName, String vworkspacePreferenceId) {
		return "./publish/History/" + constructWorksheetHistoryJsonFileName(worksheetName, vworkspacePreferenceId);
	}
	
	public static String constructWorksheetHistoryJsonFileName (String worksheetName, String vworkspacePreferenceId) {
		return vworkspacePreferenceId + "_" + worksheetName.replaceAll("[\\./]", "") + ".json";
	}

//	private double getDoubleValue(String arg, JSONArray json) throws JSONException {
//	return getJSONObjectWithName(arg, json).getDouble(ClientJsonKeys.value.name());
//}
}
