/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.util;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONUtil {

	private static Logger logger = LoggerFactory.getLogger(JSONUtil.class);

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
		return JSONUtil.doubleQuote(key.name()) + " : [";
	}

	public static String jsonStartObject(Enum<?> key) {
		return JSONUtil.doubleQuote(key.name()) + " : ";
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

	public static String truncateCellValue(String x, int maxChars) {
		if (x.length() > maxChars) {
			return x.substring(0, Math.max(3, maxChars)) + " ...";
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

	public static void writeJsonFile(Object o, String name) {
		try {
			FileWriter outFile = new FileWriter(name);
			PrintWriter pw = new PrintWriter(outFile);
			if (o instanceof JSONObject) {
				JSONObject x = (JSONObject) o;
				pw.println(x.toString(2));
			} else if (o instanceof JSONArray) {
				JSONArray x = (JSONArray) o;
				pw.println(x.toString(2));
			}
			outFile.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
