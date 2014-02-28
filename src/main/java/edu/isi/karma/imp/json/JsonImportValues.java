package edu.isi.karma.imp.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.ColumnMetadata.DataStructure;

public class JsonImportValues {
	private static Logger logger = LoggerFactory.getLogger(JsonImport.class);

	public static void addObjectElement(String key, Object value, HTable headers,
			Row row, int maxNumLines, int numObjects, RepFactory factory, Worksheet worksheet) throws JSONException {
		HNode hNode = addHNode(headers, key, DataStructure.OBJECT, factory, worksheet);

		String hNodeId = hNode.getId();

		if (value instanceof String) {
			if (((String) value).isEmpty() && hNode.hasNestedTable()) {
				addEmptyRow(row.getNode(hNodeId).getNestedTable(), hNode, maxNumLines, numObjects, factory);
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
				HTable nestedHTable = addNestedHTable(hNode, key, row, maxNumLines, numObjects, factory, worksheet);
				Table nestedTable = row.getNode(hNodeId).getNestedTable();
				addKeysAndValues((JSONObject) value, nestedHTable, nestedTable, maxNumLines, numObjects, factory, worksheet);
			}
		} else if (value instanceof JSONArray) {
			if (maxNumLines <= 0 || numObjects < maxNumLines) {
				HTable nestedHTable = addNestedHTable(hNode, key, row, maxNumLines, numObjects, factory, worksheet);
				Table nestedTable = row.getNode(hNodeId).getNestedTable();
				JSONArray a = (JSONArray) value;
				for (int i = 0; i < a.length(); i++) {
					addListElement(a.get(i), nestedHTable, nestedTable, maxNumLines, numObjects, factory, worksheet);
				}
			}
		} else if (value == JSONObject.NULL) {
			// Ignore
		} else {
			throw new Error("Cannot handle " + key + ": " + value + " yet.");
		}
	}

	public static void addEmptyRow(Table nestedTable, HNode hNode, int maxNumLines, int numObjects, RepFactory factory) {
		HTable headersNestedTable = hNode.getNestedTable();
		Row emptyRow = nestedTable.addRow(factory);
		numObjects++;
		if (maxNumLines > 0 && numObjects >= maxNumLines)
			return;

		for (HNode nestedHNode : headersNestedTable.getHNodes()) {
			if (nestedHNode.hasNestedTable()) {
				addEmptyRow(emptyRow.getNode(nestedHNode.getId())
						.getNestedTable(), nestedHNode, maxNumLines, numObjects, factory);
			} else {
				emptyRow.setValue(nestedHNode.getId(), "", factory);
			}
		}
	}

	public static void addKeysAndValues(JSONObject object, HTable nestedHTable,
			Table nestedTable, int maxNumLines, int numObjects, RepFactory factory, Worksheet worksheet) throws JSONException {
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
					nestedRow, maxNumLines, numObjects, factory, worksheet);
		}
	}

	@SuppressWarnings("unchecked")
	public static Iterator<String> getSortedKeysIterator(JSONObject object) {
		List<String> keys = new LinkedList<String>();
		keys.addAll(object.keySet());
		Collections.sort(keys);
		Iterator<String> it = keys.iterator();
		return it;
	}

	public static void addListElement(Object listValue, HTable headers,
			Table dataTable, int maxNumLines, int numObjects, RepFactory factory, Worksheet worksheet) throws JSONException {
		if (listValue instanceof JSONObject) {
			if (maxNumLines <= 0 || numObjects < maxNumLines) {
				Row row = dataTable.addRow(factory);
				numObjects++;

				JSONObject o = (JSONObject) listValue;
				Iterator<String> it = getSortedKeysIterator(o);
				while (it.hasNext()) {
					String key = it.next();
					addObjectElement(key, o.get(key), headers, row, maxNumLines, numObjects, factory, worksheet);
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
				value = (String) listValue;
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
			logger.info("Adding 'values' column to store value '" + value);
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
						"nested array values", row, maxNumLines, numObjects, factory, worksheet);
				Table nestedTable = row.getNode(hNodeId).getNestedTable();
				JSONArray a = (JSONArray) listValue;
				for (int i = 0; i < a.length(); i++) {
					addListElement(a.get(i), nestedHTable, nestedTable, maxNumLines, numObjects, factory, worksheet);
				}
			}
		} else {
			logger.error("Cannot handle whatever case is not covered by the if statements. Sorry.");
		}

	}

	public static boolean isPrimitiveValue(Object value) {
		return value instanceof String || value instanceof Boolean
				|| value instanceof Integer || value instanceof Double
				|| value instanceof Long;
	}

	public static HTable addNestedHTable(HNode hNode, String key, Row row, int maxNumLines, int numObjects, RepFactory factory, Worksheet worksheet) {
		HTable ht = hNode.getNestedTable();
		if (ht == null) {
			ht = hNode.addNestedTable(createNestedTableName(key),
					worksheet, factory);

			// Check for all the nodes that have value and nested tables
			Collection<Node> nodes = new ArrayList<Node>();
			worksheet.getDataTable().collectNodes(
					hNode.getHNodePath(factory), nodes);
			for (Node node : nodes) {
				if (node.getBelongsToRow() == row)
					break;

				// Add an empty row for each nested table that does not have any
				// row
				if (node.getNestedTable().getNumRows() == 0) {
					addEmptyRow(node.getNestedTable(), hNode, numObjects, numObjects, factory);
				}
			}
		}
		return ht;
	}

	public static HNode addHNode(HTable headers, String key, DataStructure dataStructure, RepFactory factory, Worksheet worksheet) {
		HNode hn = headers.getHNodeFromColumnName(key);
		if (hn == null) {
			hn = headers.addHNode(key, worksheet, factory);
			Worksheet ws = worksheet;
			ws.getMetadataContainer().getColumnMetadata().addColumnDataStructure(hn.getId(), dataStructure);
		}
		return hn;
	}

	public static String createNestedTableName(String key) {
		return "Table for " + key;
	}

}
