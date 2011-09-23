package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author szekely
 * 
 */
public class HTable extends RepEntity {
	// My name.
	private String tableName;

	// My columns: map of HNodeId to HNode
	private final Map<String, HNode> nodes = new HashMap<String, HNode>();

	private ArrayList<String> orderedNodeIds = new ArrayList<String>();

	public HTable(String id, String tableName) {
		super(id);
		this.tableName = tableName;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Collection<HNode> getHNodes() {
		return nodes.values();
	}

	public Collection<String> getHNodeIds() {
		return nodes.keySet();
	}

	public boolean contains(HNode hNode) {
		return nodes.containsKey(hNode.getId());
	}

	public HNode getHNode(String hNodeId) {
		return nodes.get(hNodeId);
	}

	public HNode getHNodeFromColumnName(String columnName) {
		for (HNode n : nodes.values()) {
			if (columnName.equals(n.getColumnName())) {
				return n;
			}
		}
		return null;
	}

	public HNode addHNode(String columnName, Worksheet worksheet,
			RepFactory factory) {
		HNode hn = factory.createHNode(id, columnName);
		nodes.put(hn.getId(), hn);
		orderedNodeIds.add(hn.getId());
		worksheet.addNodeToDataTable(hn, factory);
		return hn;
	}

	public List<HNode> getSortedHNodes() {
		List<HNode> allHNodes = new LinkedList<HNode>();
		for (String hNodeId : orderedNodeIds) {
			allHNodes.add(nodes.get(hNodeId));
		}
		return allHNodes;
	}

	@Override
	public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
		pw.print(prefix);
		pw.print("Headers/" + id + ": ");
		pw.println(tableName);

		for (HNode hn : getSortedHNodes()) {
			hn.prettyPrint(prefix, pw, factory);
		}
	}

	public List<HNodePath> getAllPaths() {
		List<HNodePath> x = new LinkedList<HNodePath>();
		for (HNode hn : getSortedHNodes()) {
			x.add(new HNodePath(hn));
		}
		return expandPaths(x);
	}

	private List<HNodePath> expandPaths(List<HNodePath> paths) {
		List<HNodePath> x = new LinkedList<HNodePath>();
		for (HNodePath p : paths) {
			if (p.getLeaf().getNestedTable() != null) {
				HTable nestedHTable = p.getLeaf().getNestedTable();
				for (HNodePath nestedP : nestedHTable.getAllPaths()) {
					x.add(HNodePath.concatenate(p, nestedP));
				}
			} else {
				x.add(p);
			}
		}
		return x;
	}
}
