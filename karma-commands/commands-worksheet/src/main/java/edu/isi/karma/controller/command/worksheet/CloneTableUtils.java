package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;

public class CloneTableUtils {

	public static void cloneHTable(HTable oldht, HTable newht, Worksheet newws, RepFactory factory, List<HNode> hnodes) {
		Collections.sort(hnodes);
		for (HNode hnode : hnodes) {
			HNode newhnode = newht.addHNode(hnode.getColumnName(), newws, factory);
			if (hnode.hasNestedTable()) {
				HTable oldnested = hnode.getNestedTable();
				HTable newnested = newhnode.addNestedTable(hnode.getNestedTable().getTableName(), newws, factory);		
				cloneHTable(oldnested, newnested, newws, factory, new ArrayList<HNode>(oldnested.getHNodes()));
			}
		}
	}

	public static Row cloneDataTable(Row oldRow, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory) {
		Row newrow = newDataTable.addRow(factory);
		for (HNode hnode : hnodes) {
			HNode newHNode = newHTable.getHNodeFromColumnName(hnode.getColumnName());
			Node oldNode = oldRow.getNode(hnode.getId());
			Node newNode = newrow.getNode(newHNode.getId());
			if (!oldNode.hasNestedTable()) {
				newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
			}
			else {					
				cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory);
			}
		}
		return newrow;
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

}
