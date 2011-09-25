package edu.isi.karma.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

	private static Logger logger = LoggerFactory.getLogger(Util.class);

	public static String enclose(String x, String delimiter) {
		return delimiter + x + delimiter;
	}

	public static String doubleQuote(String x) {
		return enclose(x, "\"");
	}

	public static String jsonLast(Enum<?> key, String value) {
		return doubleQuote(key.name()) + " : " + JSONObject.quote(value);
	}

	public static String jsonLast(Enum<?> key, int value) {
		return doubleQuote(key.name()) + " : " + value;
	}

	public static String jsonLast(Enum<?> key, boolean value) {
		return doubleQuote(key.name()) + " : " + value;
	}

	public static String json(Enum<?> key, String value) {
		return doubleQuote(key.name()) + " : " + JSONObject.quote(value)
				+ " , ";
	}

	public static String json(Enum<?> key, boolean value) {
		return doubleQuote(key.name()) + " : " + value + " , ";
	}

	public static String json(Enum<?> key, int value) {
		return doubleQuote(key.name()) + " : " + value + " , ";
	}

	public static String jsonStartList(Enum<?> key) {
		return Util.doubleQuote(key.name()) + " : [";
	}

	public static String jsonStartObject(Enum<?> key) {
		return Util.doubleQuote(key.name()) + " : ";
	}

	public static String truncateForHeader(String x, int maxChars) {
		if (x.length() > maxChars) {
			if (maxChars > 5) {
				String prefix = x.substring(0, maxChars - 5);
				String suffix = x.substring(x.length() - 3, x.length());
				return prefix + ".." + suffix;
			} else {
				return x.substring(0, maxChars - 2) + "..";
			}
		} else {
			return x;
		}
	}

	private static String readerToString(Reader reader) {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader bufferedReader = new BufferedReader(reader);
		char[] buf = new char[1024];
		int numRead = 0;
		try {
			while ((numRead = bufferedReader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileData.toString();
	}

	private static JSONArray createJSONArray(JSONTokener tokener) {
		JSONArray result = null;
		try {
			result = new JSONArray(tokener);
		} catch (JSONException e1) {
			// Don't do anything.
		}
		return result;
	}

	private static JSONObject createJSONObject(JSONTokener tokener) {
		JSONObject result = null;
		try {
			result = new JSONObject(tokener);
		} catch (JSONException e1) {
			// Don't do anything.
		}
		return result;
	}

	public static Object createJson(String jsonString) {
		Object result = createJSONObject(new JSONTokener(jsonString));
		if (result == null) {
			result = createJSONArray(new JSONTokener(jsonString));
		}
		if (result == null) {
			logger.error("Could not parse as JSONObject or JSONArray");
		}
		return result;
	}

	public static Object createJson(Reader reader) {
		// This is an ugly, and surely inefficient solution, but I can't figure
		// out a way around it.
		String x = readerToString(reader);
		return createJson(x);
	}
	
	public static String prettyPrintJson(String jsonString) {
		try {
			Object o = createJson(jsonString);
			if (o instanceof JSONObject) {
				return ((JSONObject) o).toString(4);
			} else if (o instanceof JSONArray) {
				return ((JSONArray) o).toString(4);
			} else {
				return "not JSON";
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return "not JSON";
		}
	}
}
