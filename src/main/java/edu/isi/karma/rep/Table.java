/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author szekely
 * 
 */
public class Table extends RepEntity {
	// the HTable that defines my properties
	private final String hTableId;

	// My rows.
	private final ArrayList<Row> rows = new ArrayList<Row>();

	//mariam
	/**
	 * The node that this table is a nested table in.
	 */
	private Node nestedTableInNode;
	
	Table(String id, String hTableId) {
		super(id);
		this.hTableId = hTableId;
	}

	//mariam
	public void setNestedTableInNode(Node n){
		nestedTableInNode=n;
	}
	public Node getNestedTableInNode(){
		return nestedTableInNode;
	}
	
	public String getHTableId() {
		return hTableId;
	}

	public Row addRow(RepFactory factory) {
		Row r = factory.createRow(hTableId);
		rows.add(r);
		//mariam
		r.setBelongsToTable(this);
		return r;
	}

	public int getNumRows() {
		return rows.size();
	}

	/**
	 * We need to add a new Node to each row in this table to provide a place
	 * holder to store it's values.
	 * 
	 * @param newHNode
	 *            added, assume to be part of this table.
	 * @param factory
	 */
	void addNodeToDataTable(HNode newHNode, RepFactory factory) {
		for (Row r : rows) {
			r.addNodeToDataTable(newHNode, this, factory);
		}
	}

	/**
	 * The HNode acquired a nested HTable, so we need to add placeholders on all
	 * rows for it.
	 * 
	 * @param hNode
	 * @param factory
	 */
	public void addNestedTableToDataTable(HNode hNode, RepFactory factory) {
		for (Row r : rows) {
			r.addNestedTableToDataTable(hNode, this, factory);
		}
	}

	public List<Row> getRandomRows(int maxNumber) {
		List<Row> result = new LinkedList<Row>();
		int count = 0;
		for (Row r : rows) {
			result.add(r);
			count++;
			if (count >= maxNumber)
				return result;
		}
		return result;
	}

	/**
	 * @param startIndex
	 *            , first row at index 0.
	 * @param count
	 * @return the requested number of rows or less if the count or startIndex
	 *         are out of bounds.
	 */
	public ArrayList<Row> getRows(int startIndex, int count) {
		ArrayList<Row> result = new ArrayList<Row>();
		if (rows.size() > 0) {
			for (int i = Math.min(startIndex, rows.size() - 1); i < Math.min(
					startIndex + count, rows.size()); i++) {
				result.add(rows.get(i));
			}
		}
		return result;
	}

	@Override
	public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
		pw.print(prefix);
		pw.print("Table/" + id + "/" + hTableId + ": ");
		pw.println(factory.getHTable(hTableId).getTableName());

		for (Row r : rows) {
			r.prettyPrint(prefix, pw, factory);
		}
	}
	
	/**
	 * Populates the nodes Collection (present in the argument) with nodes from the 
	 * table that satisfy the given path.
	 * @param path Path to a given column
	 * @param nodes Collection of nodes that satisfy the path
	 */
	public void collectNodes(HNodePath path, Collection<Node> nodes) {
		if(nodes == null)
			nodes = new ArrayList<Node>();
		collectNodes(path, nodes, rows);
	}
	
	private void collectNodes(HNodePath path, Collection<Node> nodes, List<Row> rows) {
		RowIterator:
		for(Row r : rows) {
			Collection<Node> nodesInRow = r.getNodes();
			
			for(Node n : nodesInRow) {
				if(n.getHNodeId().equals(path.getFirst().getId())) {
					// Check if the path has only one HNode
					if(path.getRest() == null || path.getRest().isEmpty()) {
						nodes.add(n);
						continue RowIterator;
					}
					
					// Check if the node has a nested table
					if(n.hasNestedTable()) {
						int numRows = n.getNestedTable().getNumRows();
						if(numRows == 0)
							continue RowIterator;
						
						List<Row> rowsNestedTable = n.getNestedTable().getRows(0, numRows);
						if(rowsNestedTable != null && rowsNestedTable.size() != 0)
							collectNodes(path.getRest(), nodes, rowsNestedTable);
					}
				}
			}
		}
	}
}
