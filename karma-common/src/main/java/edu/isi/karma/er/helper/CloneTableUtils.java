package edu.isi.karma.er.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public static Map<String, String> cloneHTable(HTable oldht, HTable newht, Worksheet newws, RepFactory factory, List<HNode> hnodes, SuperSelection sel) {
		Collections.sort(hnodes);
		Map<String, String> tmp = new HashMap<String, String>();
		for (HNode hnode : hnodes) {
			HNode newhnode = null;
			if (newht.getHNodeFromColumnName(hnode.getColumnName()) == null)
				newhnode = newht.addHNode(hnode.getColumnName(), HNodeType.Transformation, newws, factory);
			else
				newhnode = newht.addHNode(newht.getNewColumnName(hnode.getColumnName()), HNodeType.Transformation, newws, factory);
			tmp.put(hnode.getId(), newhnode.getId());
			if (hnode.hasNestedTable()) {
				HTable oldnested = hnode.getNestedTable();
				HTable newnested = newhnode.addNestedTable(hnode.getNestedTable().getTableName(), newws, factory);		
				tmp.putAll(cloneHTable(oldnested, newnested, newws, factory, new ArrayList<HNode>(oldnested.getHNodes()), sel));
			}
		}
		return tmp;
	}

	public static Row cloneDataTable(Row oldRow, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory, SuperSelection sel) {
		Row newrow = newDataTable.addRow(factory);
		for (HNode hnode : hnodes) {
			HNode newHNode = newHTable.getHNodeFromColumnName(hnode.getColumnName());
			if (newHNode == null)
				continue;
			
			Node oldNode = oldRow.getNode(hnode.getId());
			Node newNode = newrow.getNode(newHNode.getId());
			if (oldNode == null)
				continue;
			if (!oldNode.hasNestedTable()) {
				newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
			}
			else {					
				cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory, sel);
			}
		}
		return newrow;
	}
	
	public static void cloneDataTableExistingRow(Row oldRow, Row newRow, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory, Map<String, String> mapping, SuperSelection sel) {
		for (HNode hnode : hnodes) {
			HNode newHNode = factory.getHNode(mapping.get(hnode.getId()));
			Node oldNode = oldRow.getNode(hnode.getId());
			Node newNode = newRow.getNode(newHNode.getId());
			if (oldNode == null)
				continue;
			if (!oldNode.hasNestedTable()) {
				newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
			}
			else {					
				cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory, sel);
			}
		}
	}

	public static void cloneDataTable(Table oldDataTable, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory, SuperSelection sel) {
		ArrayList<Row> rows = oldDataTable.getRows(0, oldDataTable.getNumRows(), sel);
		for (Row row : rows) {
			Row newrow = newDataTable.addRow(factory);
			for (HNode hnode : hnodes) {
				HNode newHNode = newHTable.getHNodeFromColumnName(hnode.getColumnName());
				Node oldNode = row.getNode(hnode.getId());
				Node newNode = newrow.getNode(newHNode.getId());
				if (!oldNode.hasNestedTable()) {
					newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
				}
				else {					
					cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory, sel);
				}
			}
		}
	}

	public static void getDatatable(Table dt, HTable ht, List<Table> parentTables, SuperSelection sel) {
		if (dt == null)
			return;
		if (dt.getHTableId().compareTo(ht.getId()) == 0)
			parentTables.add(dt);
		else {
			for (Row row : dt.getRows(0, dt.getNumRows(), sel)) {
				for (Node n : row.getNodes()) {
					getDatatable(n.getNestedTable(), ht, parentTables, sel);
				}
			}
		}
	}

	public static Object cloneNodeToJSON(HNode hn, Node node, SuperSelection sel) {
		if (node.hasNestedTable()) {
			HTable nestHT = hn.getNestedTable();
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
