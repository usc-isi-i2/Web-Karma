package edu.isi.karma.er.helper;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.HNode.HNodeType;

public class CloneTableUtils {
	
	public static Map<String, String> cloneHTable(HTable oldht, HTable newht, Worksheet newws, RepFactory factory, List<HNode> hnodes) {
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
				tmp.putAll(cloneHTable(oldnested, newnested, newws, factory, new ArrayList<HNode>(oldnested.getHNodes())));
			}
		}
		return tmp;
	}

	public static Row cloneDataTable(Row oldRow, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory) {
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
				cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory);
			}
		}
		return newrow;
	}
	
	public static void cloneDataTableExistingRow(Row oldRow, Row newRow, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory, Map<String, String> mapping) {
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
				cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory);
			}
		}
	}

	public static void cloneDataTable(Table oldDataTable, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory) {
		ArrayList<Row> rows = oldDataTable.getRows(0, oldDataTable.getNumRows());
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
					cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory);
				}
			}
		}
	}

	public static Row getRow(List<Row> rows, String rowID) {
		for (Row row : rows) {
			if (row.getId().compareTo(rowID) == 0)
				return row;
		}
		return null;
	}

	public static HTable getHTable(HTable ht, String HNodeId) {
		for (HNode hn : ht.getHNodes()) {
			if (hn.getId().compareTo(HNodeId) == 0)
				return ht;
			if (hn.hasNestedTable()) {
				HTable tmp = getHTable(hn.getNestedTable(), HNodeId);
				if (tmp != null)
					return tmp;
			}		
		}
		return null;
	}
	
	public static HTable getChildHTable(HTable ht, String HTableId, boolean result) {
		if (ht.getId().compareTo(HTableId) == 0 && result)
			return ht;
		for (HNode hn : ht.getHNodes()) {
			if (hn.hasNestedTable()) {
				HTable tmp = getChildHTable(hn.getNestedTable(), hn.getNestedTable().getId(), true);
				if (tmp != null)
					return tmp;
			}		
		}
		return null;
	}

	public static void getDatatable(Table dt, HTable ht, List<Table> parentTables) {
		if (dt == null)
			return;
		if (dt.getHTableId().compareTo(ht.getId()) == 0)
			parentTables.add(dt);
		else {
			for (Row row : dt.getRows(0, dt.getNumRows())) {
				for (Node n : row.getNodes()) {
					getDatatable(n.getNestedTable(), ht, parentTables);
				}
			}
		}
	}

	public static Object cloneNodeToJSON(HNode hn, Node node) {
		if (node.hasNestedTable()) {
			HTable nestHT = hn.getNestedTable();
			Table dataTable = node.getNestedTable();
			JSONArray array = new JSONArray();
			for (Row row : dataTable.getRows(0, dataTable.getNumRows())) {
				JSONObject obj = new JSONObject();
				for (HNode hnode : nestHT.getHNodes()) {
					Node n = row.getNode(hnode.getId());
					obj.put(hnode.getColumnName(),cloneNodeToJSON(hnode, n));
				}
				array.put(obj);
			}
			return array;
		}
		else
			return node.getValue().asString();
	}

}
