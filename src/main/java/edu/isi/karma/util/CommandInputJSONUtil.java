package edu.isi.karma.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class CommandInputJSONUtil {
	
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
		return getJSONObjectWithName(arg, json).getString(JsonKeys.value.name());
	}

	public static JSONArray getJSONArrayValue(String name, JSONArray json) throws JSONException {
		return getJSONObjectWithName(name, json).getJSONArray(JsonKeys.value.name());
	}
}
