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
import edu.isi.karma.util.Util;

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
		this(Util.createJson(jsonString), worksheetName, workspace);
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
		Util.writeJsonFile(o, "lastJsonImport.json");
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
			Iterator<String> it = o.keys();
			//TODO: should be replaced by Iterator<String> it = o.sortedKeys();
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
			row.setValue(hNodeId, (String) listValue);
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
				|| value instanceof String;
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
