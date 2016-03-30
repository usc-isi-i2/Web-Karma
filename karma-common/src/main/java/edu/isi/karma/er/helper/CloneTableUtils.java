package edu.isi.karma.er.helper;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;

public class CloneTableUtils {

	private CloneTableUtils() {
	}

	public static Map<String, String> cloneHTable(HTable newHTable, Worksheet newWorksheet, RepFactory factory, List<HNode> hNodes, boolean isFirst) {
		Collections.sort(hNodes);
		Map<String, String> tmp = new HashMap<>();
		for (HNode hnode : hNodes) {
			HNode newHNode;
			if (!isFirst) {
				newHNode = newHTable.addHNode(hnode.getColumnName(), HNodeType.Transformation, newWorksheet, factory);
			}
			else {
				newHNode = newHTable.addHNode(hnode.getHNodePath(factory).toColumnNamePath().replace("/", "_"), HNodeType.Transformation, newWorksheet, factory);
			}
			tmp.put(hnode.getId(), newHNode.getId());
			if (hnode.hasNestedTable()) {
				HTable oldNested = hnode.getNestedTable();
				HTable newNested = newHNode.addNestedTable(hnode.getNestedTable().getTableName(), newWorksheet, factory);
				tmp.putAll(cloneHTable(newNested, newWorksheet, factory, new ArrayList<>(oldNested.getHNodes()), false));
			}
		}
		return tmp;
	}

	public static Row cloneDataTable(Row oldRow, Table newDataTable, HTable newHTable, List<HNode> hNodes, RepFactory factory, SuperSelection sel) {
		Row newRow = newDataTable.addRow(factory);
		for (HNode hnode : hNodes) {
			HNode newHNode = newHTable.getHNodeFromColumnName(hnode.getColumnName());
			if (newHNode == null)
				continue;
			
			Node oldNode = oldRow.getNode(hnode.getId());
			Node newNode = newRow.getNode(newHNode.getId());
			if (oldNode == null)
				continue;
			if (!oldNode.hasNestedTable()) {
				newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
			}
			else {					
				cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory, sel);
			}
		}
		return newRow;
	}
	
	public static void cloneDataTableExistingRow(Row oldRow, Row newRow, List<HNode> hNodes, RepFactory factory, Map<String, String> mapping, SuperSelection sel) {
		for (HNode hnode : hNodes) {
			HNode newHNode = factory.getHNode(mapping.get(hnode.getId()));
			Node oldNode = oldRow.getNode(hnode.getId());
			Node newNode = newRow.getNode(newHNode.getId());
			if (oldNode == null)
				continue;
			if (!oldNode.hasNestedTable()) {
				newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
			}
			else {					
				cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory, sel);
			}
		}
	}

	public static void cloneDataTable(Table oldDataTable, Table newDataTable, HTable newHTable, List<HNode> hNodes, RepFactory factory, SuperSelection sel) {
		ArrayList<Row> rows = oldDataTable.getRows(0, oldDataTable.getNumRows(), sel);
		for (Row row : rows) {
			Row newrow = newDataTable.addRow(factory);
			for (HNode hnode : hNodes) {
				HNode newHNode = newHTable.getHNodeFromColumnName(hnode.getColumnName());
				Node oldNode = row.getNode(hnode.getId());
				Node newNode = newrow.getNode(newHNode.getId());
				if (!oldNode.hasNestedTable()) {
					newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
				}
				else {					
					cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory, sel);
				}
			}
		}
	}

	public static void getDatatable(Table dataTable, HTable headerTable, List<Table> parentTables, SuperSelection sel) {
		if (dataTable == null)
			return;
		if (dataTable.getHTableId().compareTo(headerTable.getId()) == 0)
			parentTables.add(dataTable);
		else {
			for (Row row : dataTable.getRows(0, dataTable.getNumRows(), sel)) {
				for (Node n : row.getNodes()) {
					getDatatable(n.getNestedTable(), headerTable, parentTables, sel);
				}
			}
		}
	}

	public static Object cloneNodeToJSON(HNode hNode, Node node, SuperSelection sel) {
		if (node.hasNestedTable()) {
			HTable nestHT = hNode.getNestedTable();
			Table dataTable = node.getNestedTable();
			JSONArray array = new JSONArray();
			for (Row row : dataTable.getRows(0, dataTable.getNumRows(), sel)) {
				JSONObject obj = new JSONObject();
				for (HNode hnode : nestHT.getHNodes()) {
					Node n = row.getNode(hnode.getId());
					obj.put(hnode.getColumnName(),cloneNodeToJSON(hnode, n, sel));
				}
				array.put(obj);
			}
			return array;
		}
		else
			return node.getValue().asString();
	}

}
