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
/**
 * 
 */
package edu.isi.karma.imp.json;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;

/**
 * @author szekely
 * 
 */
public class JsonImport {

	private static Logger logger = LoggerFactory.getLogger(JsonImport.class);

	private final Object json;
	private final RepFactory factory;
	private final Worksheet worksheet;

	public JsonImport(Object json, String worksheetName, Workspace workspace) {
		super();
		this.json = json;
		this.factory = workspace.getFactory();
		this.worksheet = factory.createWorksheet(worksheetName, workspace);
	}

	public JsonImport(String jsonString, String worksheetName,
			Workspace workspace) {
		this(JSONUtil.createJson(jsonString), worksheetName, workspace);
	}

	public Worksheet generateWorksheet() throws JSONException {

		if (json instanceof JSONArray) {
			JSONArray a = (JSONArray) json;
			for (int i = 0; i < a.length(); i++) {
				addListElement(a.get(i), worksheet.getHeaders(),
						worksheet.getDataTable());
			}
		}

		else if (json instanceof JSONObject) {
			addKeysAndValues((JSONObject) json, worksheet.getHeaders(),
					worksheet.getDataTable());
		}

		else {
			throw new Error("Can only import objects or arrays.");
		}

		writeJsonFile(json);
		return worksheet;
	}

	private static void writeJsonFile(Object o) {
		JSONUtil.writeJsonFile(o, "lastJsonImport.json");
	}

	private void addObjectElement(String key, Object value, HTable headers,
			Row row) throws JSONException {
		HNode hNode = addHNode(headers, key);
		String hNodeId = hNode.getId();

		if (value instanceof String) {
			row.setValue(hNodeId, (String) value);
		}

		else if (value instanceof Integer) {
			row.setValue(hNodeId, value.toString());
		}

		else if (value instanceof Double) {
			row.setValue(hNodeId, value.toString());
		}

		else if (value instanceof Long) {
			row.setValue(hNodeId, value.toString());
		}

		else if (value instanceof Boolean) {
			row.setValue(hNodeId, value.toString());
		}

		else if (value instanceof JSONObject) {
			HTable nestedHTable = addNestedHTable(hNode, key);
			Table nestedTable = row.getNode(hNodeId).getNestedTable();
			addKeysAndValues((JSONObject) value, nestedHTable, nestedTable);
		}

		else if (value instanceof JSONArray) {
			HTable nestedHTable = addNestedHTable(hNode, key);
			Table nestedTable = row.getNode(hNodeId).getNestedTable();
			JSONArray a = (JSONArray) value;
			for (int i = 0; i < a.length(); i++) {
				addListElement(a.get(i), nestedHTable, nestedTable);
			}
		}

		else if (value == JSONObject.NULL) {
			// Ignore
		}

		else {
			throw new Error("Cannot handle " + key + ": " + value + " yet.");
		}
	}

	private void addKeysAndValues(JSONObject object, HTable nestedHTable,
			Table nestedTable) throws JSONException {
		Row nestedRow = nestedTable.addRow(factory);
		@SuppressWarnings("unchecked")
		Iterator<String> it = object.sortedKeys();
		while (it.hasNext()) {
			String nestedKey = it.next();
			addObjectElement(nestedKey, object.get(nestedKey), nestedHTable,
					nestedRow);
		}
	}

	private void addListElement(Object listValue, HTable headers,
			Table dataTable) throws JSONException {
		if (listValue instanceof JSONObject) {
			Row row = dataTable.addRow(factory);
			JSONObject o = (JSONObject) listValue;
			@SuppressWarnings("unchecked")
			Iterator<String> it = o.sortedKeys();
			while (it.hasNext()) {
				String key = it.next();
				addObjectElement(key, o.get(key), headers, row);
			}
		}

		else if (isPrimitiveValue(listValue)) {
			HNode hNode = addHNode(headers, "values");
			String hNodeId = hNode.getId();
			Row row = dataTable.addRow(factory);
			// TODO, conserve the types of the primitive types.
			String value = "";
			if (listValue instanceof String || listValue instanceof Boolean)
				value = (String) listValue;
			else if (listValue instanceof Double)
				value = Double.toString((Double) listValue);
			else if (listValue instanceof Integer)
				value = Integer.toString((Integer) listValue);
			else if (listValue instanceof Long)
				value = Long.toString((Long) listValue);
			row.setValue(hNodeId, value);
		}

		else if (listValue instanceof JSONArray) {
			HNode hNode = addHNode(headers, "nested array");
			String hNodeId = hNode.getId();
			HTable nestedHTable = addNestedHTable(hNode, "nested array values");
			Row row = dataTable.addRow(factory);
			Table nestedTable = row.getNode(hNodeId).getNestedTable();
			JSONArray a = (JSONArray) listValue;
			for (int i = 0; i < a.length(); i++) {
				addListElement(a.get(i), nestedHTable, nestedTable);
			}
		}

		else {
			logger.error("Cannot handle whatever case is not covered by the if statements. Sorry.");
		}

	}

	private boolean isPrimitiveValue(Object value) {
		return value instanceof String || value instanceof Boolean
				|| value instanceof Integer || value instanceof Double
				|| value instanceof Long;
	}

	private HTable addNestedHTable(HNode hNode, String key) {
		HTable ht = hNode.getNestedTable();
		if (ht == null) {
			ht = hNode.addNestedTable(createNestedTableName(key), worksheet,
					factory);
		}
		return ht;
	}

	private HNode addHNode(HTable headers, String key) {
		HNode hn = headers.getHNodeFromColumnName(key);
		if (hn == null) {
			hn = headers.addHNode(key, worksheet, factory);
		}
		return hn;
	}

	private String createNestedTableName(String key) {
		return "Table for " + key;
	}
}
