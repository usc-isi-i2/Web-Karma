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

	//mariam
	/**
	 * the HNode that contains this table (useful for backwards traversing)
	 */
	private HNode parentHNode=null;
	
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
	
	//mariam
	/**
	 * Returns the HNode that contains this table.
	 * @return
	 * 		the HNode that contains this table.
	 */
	public HNode getParentHNode(){
		return parentHNode;
	}

	/**
	 * Returns the HNodeId for the first HNode with the given columnName.
	 * @param columnName
	 * @return
	 * 		the HNodeId given a columnName.
	 * Should be used only with worksheets that do not contain nested tables.
	 */
	public String getHNodeIdFromColumnName(String columnName) {
		for (Map.Entry<String, HNode> n : nodes.entrySet()) {
			if (columnName.equals(n.getValue().getColumnName())) {
				return n.getKey();
			}
		}
		return null;
	}

	/**
	 * Returns true if this table contains nested tables, false otherwise.
	 * @return
	 * 		true if this table contains nested tables, false otherwise.
	 */
	public boolean hasNestedTables(){
		for(HNode n: getHNodes()){
			if(n.hasNestedTable())
				return true;
		}
		return false;
	}
//////////////////////////////////////////////
	
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

	public void getSortedLeafHNodes(List<HNode> sortedLeafHNodes) {
		for (String hNodeId : orderedNodeIds) {
			HNode node = nodes.get(hNodeId);
			if (node.hasNestedTable()) {
				node.getNestedTable().getSortedLeafHNodes(sortedLeafHNodes);
			} else {
				sortedLeafHNodes.add(node);
			}
		}
	}
	
	public void addNewHNodeAfter(String hNodeId, RepFactory factory, String columnName, Worksheet worksheet) {
		HNode hNode = getHNode(hNodeId);
		if(hNode == null) {
			for(HNode node : nodes.values()) {
				if(node.hasNestedTable()) {
					node.getNestedTable().addNewHNodeAfter(hNodeId, factory, columnName, worksheet);
				}
			}
		} else {
			HNode newNode = factory.createHNode(getId(), columnName);
			nodes.put(newNode.getId(), newNode);
			int index = orderedNodeIds.indexOf(hNodeId);
			
			if(index == orderedNodeIds.size()-1)
				orderedNodeIds.add(newNode.getId());
			else
				orderedNodeIds.add(index+1, newNode.getId());
			worksheet.addNodeToDataTable(newNode, factory);
		}
	}

	/** Returns ordered nodeIds.
	 * @return
	 * 		ordered nodeIds.
	 * @author mariam
	 */
	public ArrayList<String> getOrderedNodeIds(){
		return orderedNodeIds;
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

	/**
	 * Sets the parent HNode.
	 * @param hNode
	 * @author mariam
	 */
	public void setParentHNode(HNode hNode) {
		parentHNode = hNode;
	}
}
