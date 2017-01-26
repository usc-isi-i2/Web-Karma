package edu.isi.karma.imp.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.rep.ColumnMetadata.DataStructure;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;

public class JsonImportValues {
	private static Logger logger = LoggerFactory.getLogger(JsonImportValues.class);
	private int maxNumLines;
	private int numObjects;
	private RepFactory factory;
	private Worksheet worksheet;
	private JSONArray columnsJson;
	private Map<String, Boolean> columnsCache = new HashMap<>();
	public JsonImportValues(int maxNumLines, int numObjects, RepFactory factory, 
			Worksheet worksheet, JSONArray columnsJson) {
		this.maxNumLines = maxNumLines;
		this.numObjects = numObjects;
		this.factory = factory;
		this.worksheet = worksheet;
		this.columnsJson = columnsJson;
	}
	public void addObjectElement(String key, Object value, HTable headers,
			Row row) throws JSONException {
		HNode hNode = addHNode(headers, key, DataStructure.OBJECT, factory, worksheet);

		String hNodeId = hNode.getId();

		if (value instanceof String) {
			
			if (((String) value).isEmpty() && hNode.hasNestedTable()) {
				addEmptyRow(row.getNode(hNodeId).getNestedTable(), hNode);
			}
			row.setValue(hNodeId, (String) value, factory);
		} else if (value instanceof Integer) {
			row.setValue(hNodeId, value.toString(), factory);
		} else if (value instanceof Double) {
			row.setValue(hNodeId, value.toString(), factory);
		} else if (value instanceof Long) {
			row.setValue(hNodeId, value.toString(), factory);
		} else if (value instanceof Boolean) {
			row.setValue(hNodeId, value.toString(), factory);
		} else if (value instanceof JSONObject) {
			if (maxNumLines <= 0 || numObjects < maxNumLines) {
				HTable nestedHTable = addNestedHTable(hNode, key, row);
				Table nestedTable = row.getNode(hNodeId).getNestedTable();
				addKeysAndValues((JSONObject) value, nestedHTable, nestedTable);
			}
		} else if (value instanceof JSONArray) {
			if (maxNumLines <= 0 || numObjects < maxNumLines) {
				HTable nestedHTable = addNestedHTable(hNode, key, row);
				Table nestedTable = row.getNode(hNodeId).getNestedTable();
				JSONArray a = (JSONArray) value;
				for (int i = 0; i < a.length(); i++) {
					addListElement(a.get(i), nestedHTable, nestedTable);
				}
			}
		} else if (value == JSONObject.NULL) {
			// Ignore
		} else {
			throw new Error("Cannot handle " + key + ": " + value + " yet.");
		}
	}

	public void addObjectElement(String key, JSONTokener token, HTable headers,
			Row row) throws JSONException {
		HNode hNode = addHNode(headers, key, DataStructure.OBJECT, factory, worksheet);

		String hNodeId = hNode == null ? null : hNode.getId();
		char c = token.nextClean();
		if (maxNumLines > 0 && numObjects >= maxNumLines)
			return;
		if (c != '{' && c != '[') {
			token.back();
			Object tokenObj = token.nextValue();
			String value;
			if(tokenObj == null || tokenObj == JSONObject.NULL)
				value = "";
			else 
				value = tokenObj.toString();
				
			if (value.isEmpty() && hNode != null && hNode.hasNestedTable()) {
				addEmptyRow(row.getNode(hNodeId).getNestedTable(), hNode);
			}
			if (hNodeId != null)
				row.setValue(hNodeId, value, factory);
		}
		else if (c == '{') {
			if (maxNumLines <= 0 || numObjects < maxNumLines) {
				if (hNode != null) {
					HTable nestedHTable = addNestedHTable(hNode, key, row);
					Table nestedTable = row.getNode(hNodeId).getNestedTable();
					addKeysAndValues(token, nestedHTable, nestedTable);
				}
				else
					addKeysAndValues(token, null, null);
			}
		} else if (c == '[') {
			if (maxNumLines <= 0 || numObjects < maxNumLines) {
				if (hNode != null) {
					HTable nestedHTable = addNestedHTable(hNode, key, row);
					Table nestedTable = row.getNode(hNodeId).getNestedTable();
					addListElement(token, nestedHTable, nestedTable);
				}
				else
					addListElement(token, null, null);
			}
		} else {
			throw new Error("Cannot handle " + key + " yet.");
		}
	}

	public void addEmptyRow(Table nestedTable, HNode hNode) {
		HTable headersNestedTable = hNode.getNestedTable();
		Row emptyRow = nestedTable.addRow(factory);
		numObjects++;
		if (maxNumLines > 0 && numObjects >= maxNumLines)
			return;

		for (HNode nestedHNode : headersNestedTable.getHNodes()) {
			if (nestedHNode.hasNestedTable()) {
				addEmptyRow(emptyRow.getNode(nestedHNode.getId())
						.getNestedTable(), nestedHNode);
			} else {
				emptyRow.setValue(nestedHNode.getId(), "", factory);
			}
		}
	}

	public void addKeysAndValues(JSONObject object, HTable nestedHTable,
			Table nestedTable) throws JSONException {
		if (maxNumLines > 0 && numObjects >= maxNumLines)
			return;

		Row nestedRow = nestedTable.addRow(factory);
		numObjects++;
		// if(maxNumLines > 0 && numObjects >= maxNumLines)
		// return;


		Iterator<String> it = getSortedKeysIterator(object);
		while (it.hasNext()) {
			String nestedKey = it.next();
			addObjectElement(nestedKey, object.get(nestedKey), nestedHTable,
					nestedRow);
		}
	}

	public void addKeysAndValues(JSONTokener token, HTable nestedHTable,
			Table nestedTable) throws JSONException {
		if (maxNumLines > 0 && numObjects >= maxNumLines)
			return;

		Row nestedRow = null;
		if (nestedTable != null) {
			nestedRow = nestedTable.addRow(factory);
			numObjects++;
		}
		// if(maxNumLines > 0 && numObjects >= maxNumLines)
		// return;
		char c = token.nextClean();
		while (c != '}') {
			if (maxNumLines > 0 && numObjects >= maxNumLines)
				break;
			token.back();
			Object key = token.nextValue();
			char t = token.nextClean();
			if (t != ':')
				throw new JSONException("Parse JSON object error");
			addObjectElement((String)key, token, nestedHTable,
					nestedRow);
			if (maxNumLines > 0 && numObjects >= maxNumLines)
				break;
			c = token.nextClean();
			if (c != ',' && c != '}')
				throw new JSONException("Parse JSON object error");
			if (c == ',') {
				c = token.nextClean();
			}
		}
	}

	@SuppressWarnings("unchecked")
	public Iterator<String> getSortedKeysIterator(JSONObject object) {
		List<String> keys = IteratorUtils.toList(object.keys());
		Collections.sort(keys);
		Iterator<String> it = keys.iterator();
		return it;
	}

	public void addListElement(Object listValue, HTable headers,
			Table dataTable) throws JSONException {
			
		if(JSONObject.NULL.equals(listValue)) {
			listValue = "";
		}
		
		if (listValue instanceof JSONObject) {
			if (maxNumLines <= 0 || numObjects < maxNumLines) {
				Row row = dataTable.addRow(factory);
				numObjects++;

				JSONObject o = (JSONObject) listValue;
				Iterator<String> it = getSortedKeysIterator(o);
				while (it.hasNext()) {
					String key = it.next();
					addObjectElement(key, o.get(key), headers, row);
				}
			}
		} else if (isPrimitiveValue(listValue)) {
			HNode hNode = addHNode(headers, HTable.VALUES_COLUMN, DataStructure.PRIMITIVE, factory, worksheet);
			String hNodeId = hNode.getId();
			Row row = dataTable.addRow(factory);
			numObjects++;
			// TODO, conserve the types of the primitive types.
			String value = "";
			if (listValue instanceof String || listValue instanceof Boolean) {
				value = listValue.toString();
			} else if (listValue instanceof Double) {
				value = Double.toString((Double) listValue);
			} else if (listValue instanceof Integer) {
				value = Integer.toString((Integer) listValue);
			} else if (listValue instanceof Long) {
				value = Long.toString((Long) listValue);
			} else {
				// Pedro 2012/09/14
				logger.error("Unexpected value in JSON array:"
						+ listValue.toString());
			}

			row.setValue(hNodeId, value, factory);
		} else if (listValue instanceof JSONArray) {
			if (maxNumLines <= 0 || numObjects < maxNumLines) {
				HNode hNode = addHNode(headers, "nested array", DataStructure.COLLECTION, factory, worksheet);
				String hNodeId = hNode.getId();
				Row row = dataTable.addRow(factory);
				numObjects++;
				if (maxNumLines > 0 && numObjects >= maxNumLines)
					return;
				HTable nestedHTable = addNestedHTable(hNode,
						"nested array values", row);
				Table nestedTable = row.getNode(hNodeId).getNestedTable();
				JSONArray a = (JSONArray) listValue;
				for (int i = 0; i < a.length(); i++) {
					addListElement(a.get(i), nestedHTable, nestedTable);
				}
			}
		} else {
			logger.error("Cannot handle whatever case is not covered by the if statements. Sorry");
			logger.error(listValue.toString());
		}

	}

	public void addListElement(JSONTokener token, HTable headers,
			Table dataTable) throws JSONException {
		char c = token.nextClean();
		while (c != ']') {
			if (maxNumLines > 0 && numObjects >= maxNumLines)
				break;
			if (c != '{' && c != '[') {
				token.back();
				HNode hNode = addHNode(headers, HTable.VALUES_COLUMN, DataStructure.PRIMITIVE, factory, worksheet);
				String hNodeId = hNode == null ? null : hNode.getId();
				String value = token.nextValue().toString();
				if (hNodeId != null) {
					Row row = dataTable.addRow(factory);
					numObjects++;
					row.setValue(hNodeId, value, factory);
				}
			}
			else if (c == '{') {
				if (maxNumLines <= 0 || numObjects < maxNumLines) {
					if (headers != null && dataTable != null)
						numObjects++;
					addKeysAndValues(token, headers, dataTable);
				}
			}
			else if (c == '[') {
				if (maxNumLines <= 0 || numObjects < maxNumLines) {
					HNode hNode = addHNode(headers, "nested array", DataStructure.COLLECTION, factory, worksheet);
					String hNodeId = hNode == null ? null : hNode.getId();
					if (hNodeId != null) {
						Row row = dataTable.addRow(factory);
						numObjects++;
						if (maxNumLines > 0 && numObjects >= maxNumLines)
							return;
						HTable nestedHTable = addNestedHTable(hNode,
								"nested array values", row);
						Table nestedTable = row.getNode(hNodeId).getNestedTable();
						addListElement(token, nestedHTable, nestedTable);
					}
					else
						addListElement(token, null, null);
				}
			} 
			else if (c != ','){
				logger.error("Cannot handle whatever case is not covered by the if statements. Sorry.");

			}
			if (maxNumLines > 0 && numObjects >= maxNumLines)
				break;
			c = token.nextClean();
			if (c != ',' && c != ']')
				throw new JSONException("Parse JSON array error");
			if (c == ',') {
				c = token.nextClean();
			}
		}

	}

	public boolean isPrimitiveValue(Object value) {
		return value instanceof String || value instanceof Boolean
				|| value instanceof Integer || value instanceof Double
				|| value instanceof Long;
	}

	public HTable addNestedHTable(HNode hNode, String key, Row row) {
		HTable ht = hNode.getNestedTable();
		if (ht == null) {
			ht = hNode.addNestedTable(createNestedTableName(key),
					worksheet, factory);

			// Check for all the nodes that have value and nested tables
			Collection<Node> nodes = new ArrayList<>();
			worksheet.getDataTable().collectNodes(
					hNode.getHNodePath(factory), nodes, SuperSelectionManager.DEFAULT_SELECTION);
			for (Node node : nodes) {
				if (node.getBelongsToRow() == row)
					break;

				// Add an empty row for each nested table that does not have any
				// row
				if (node.getNestedTable().getNumRows() == 0) {
					addEmptyRow(node.getNestedTable(), hNode);
				}
			}
		}
		return ht;
	}

	public HNode addHNode(HTable headers, String key, DataStructure dataStructure, RepFactory factory, Worksheet worksheet) {
		if (headers == null)
			return null;
		HNode hn = headers.getHNodeFromColumnName(key);
		if (hn == null && isVisible(headers, key, factory)) {
			hn = headers.addHNode(key, HNodeType.Regular, worksheet, factory);
			Worksheet ws = worksheet;
			ws.getMetadataContainer().getColumnMetadata().addColumnDataStructure(hn.getId(), dataStructure);
		}
		return hn;
	}

	public String createNestedTableName(String key) {
		return "Table for " + key;
	}

	private boolean isVisible(HTable headers, String key, RepFactory factory) {
		if (columnsJson == null)
			return true;
		HNode hn = headers.getParentHNode();		
		if (hn != null) {
			HNodePath hPath = hn.getHNodePath(factory);
			String path = hPath.toColumnNamePath() + "/" + key;
			Boolean b = columnsCache.get(path);
			if (b != null)
				return b;
			HNode first = null;
			JSONArray t = columnsJson;
			JSONObject tree = null;
			while (first != hn) {				
				first = hPath.getFirst();
				tree = getCorrespondingObject(t, first.getColumnName());
				if (tree == null || !tree.has("children")) {
					columnsCache.put(path, true);
					return true;
				}
				t = tree.getJSONArray("children");
				hPath = hPath.getRest();				
			}
			if (tree == null || !tree.has("children")) {
				columnsCache.put(path, true);
				return true;
			}
			JSONObject obj = getCorrespondingObject(tree.getJSONArray("children"), key);
			if (obj == null || !obj.has(key)) {
				columnsCache.put(path, true);
				return true;
			}
			b = obj.getBoolean(key);
			columnsCache.put(path, b);
			return b;
		}
		else {
			Boolean b = columnsCache.get(key);
			if (b != null)
				return b;
			JSONObject obj = getCorrespondingObject(columnsJson, key);
			if (obj == null || !obj.has(key)) {
				columnsCache.put(key, true);
				return true;
			}
			b = obj.getBoolean(key);
			columnsCache.put(key, b);
			return b;
		}
		
	}
	
	private JSONObject getCorrespondingObject(JSONArray array, String colName) {
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			if (obj.has(colName))
				return obj;
		}
		return null;
	}

	public int getNumberOfObjectsImported() {
		return numObjects;
	}
}
