package edu.isi.karma.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.history.HistoryJsonUtil.ClientJsonKeys;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;



public class CommandInputJSONUtil {

	private CommandInputJSONUtil() {
	}

	public enum JsonKeys {
		name, value, type
	}
	
	public static JSONObject getJSONObjectWithName(String arg, JSONArray json) throws JSONException {
		for(int i=0; i<json.length(); i++) {
			JSONObject obj = json.getJSONObject(i);
			String nameS = obj.getString(JsonKeys.name.name());
			if(nameS.equals(arg)) {
				return obj;
			}
		}
		return null;
	}
	
	public static String getStringValue(String arg, JSONArray json) throws JSONException {
		JSONObject obj = getJSONObjectWithName(arg, json);
		if (obj == null)
			return null;
		else
			return obj.getString(JsonKeys.value.name());
	}

	public static JSONArray getJSONArrayValue(String name, JSONArray json) throws JSONException {
		JSONObject obj = getJSONObjectWithName(name, json);
		if (obj == null)
			return null;
		else
			return obj.getJSONArray(JsonKeys.value.name());
	}
	
	public static JSONObject createJsonObject(String name, Object value, ParameterType type) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put(ClientJsonKeys.name.name(), name);
		obj.put(ClientJsonKeys.value.name(), value);
		obj.put(ClientJsonKeys.type.name(), type.toString());
		return obj;
	}
}
