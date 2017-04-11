/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.webserver.KarmaException;

/**
 * @author szekely
 * 
 */
public class HTable extends RepEntity {

	/**
	 * The name of the column where we store values of JSON arrays.
	 */
	public static final String VALUES_COLUMN = "values";
	
	// My name.
	private String tableName;

	// My columns: map of HNodeId to HNode
	private final Map<String, HNode> nodes = new HashMap<>();

	private ArrayList<String> orderedNodeIds = new ArrayList<>();

	
	// mariam
	/**
	 * the HNode that contains this table (useful for backwards traversing)
	 */
	private HNode parentHNode = null;

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

	// mariam
	/**
	 * Returns the HNode that contains this table.
	 * 
	 * @return the HNode that contains this table.
	 */
	public HNode getParentHNode() {
		return parentHNode;
	}
	
	public String getNewColumnName(String prefix) {
		Pattern p = Pattern.compile(".*?(_\\d+)");
		Matcher m = p.matcher(prefix);
		if (m.find()) {
			return getNewColumnName(prefix.replaceAll(m.group(1), "")); 
		}
		for (int i=1; i<1000; i++) {
			if (isValidNewColumnName(prefix + "_" + i)) {
				return prefix + "_" + i;
			}
		}
		// Last resort
		return "New Column";
	}
	
	private boolean isValidNewColumnName (String columnName) {
		for (HNode node:nodes.values()) {
			if (node.getColumnName().equalsIgnoreCase(columnName))
				return false;
		}
		return true;
	}

	/**
	 * Returns the HNodeId for the first HNode with the given columnName.
	 * 
	 * @param columnName
	 * @return the HNodeId given a columnName. Should be used only with
	 *         worksheets that do not contain nested tables.
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
	 * 
	 * @return true if this table contains nested tables, false otherwise.
	 */
	public boolean hasNestedTables() {
		for (HNode n : getHNodes()) {
			if (n.hasNestedTable())
				return true;
		}
		return false;
	}

	// ////////////////////////////////////////////

	private String verifyColumnName(String columnName) {
		if(columnName.trim().length() == 0) {
			columnName = getNewColumnName("Column");
		}
		return columnName;
	}
	
	public HNode addHNode(String columnName, HNodeType type, Worksheet worksheet,
			RepFactory factory) {
		return addHNode(columnName, false, type, worksheet, factory);
	}

	public HNode addHNode(String columnName, boolean automaticallyAdded, HNodeType type,
			Worksheet worksheet, RepFactory factory) {
		columnName = verifyColumnName(columnName);
		HNode hn = factory.createHNode(id, columnName, automaticallyAdded, type);
		nodes.put(hn.getId(), hn);
		orderedNodeIds.add(hn.getId());
		worksheet.addNodeToDataTable(hn, factory);
		return hn;
	}

	public List<HNode> getSortedHNodes() {
		List<HNode> allHNodes = new LinkedList<>();
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

	// pedro 2012-10-28: this method seems to recurse looking for the hNodeId.
	// Bad design. Adding a new column should return the HNode or hNodeId of the added column.
	// There should be a way to add before or after the given hNodeId.
	public void addNewHNodeAfter(String hNodeId, HNodeType type, RepFactory factory,
			String columnName, Worksheet worksheet) {
		HNode hNode = getHNode(hNodeId);
		if (hNode == null) {
			for (HNode node : nodes.values()) {
				if (node.hasNestedTable()) {
					node.getNestedTable().addNewHNodeAfter(hNodeId, type, factory,
							columnName, worksheet);
				}
			}
		} else {
			HNode newNode = factory.createHNode(getId(), columnName, false, type);
			nodes.put(newNode.getId(), newNode);
			int index = orderedNodeIds.indexOf(hNodeId);

			if (index == orderedNodeIds.size() - 1)
				orderedNodeIds.add(newNode.getId());
			else
				orderedNodeIds.add(index + 1, newNode.getId());
			worksheet.addNodeToDataTable(newNode, factory);
		}
	}

	//mariam 2012-11-28
	//add before hNodeId, or at the beginning of table if hNodeId is null
	//adds new HNode with given columnName
	public HNode addNewHNodeAfter(String hNodeId, HNodeType type, RepFactory factory,
			String columnName, Worksheet worksheet, boolean b) throws KarmaException {

		HNode hn = factory.createHNode(id, columnName, false, type);
		nodes.put(hn.getId(), hn);
		//if hNodeId==null add new node at the beginning
		if(hNodeId==null){
			orderedNodeIds.add(0,hn.getId());
		}
		else{
			//add it after hNodeId
			int index = orderedNodeIds.indexOf(hNodeId);
			if(index<0){
				//node not found; 
				throw new KarmaException("Node " + hNodeId + " not found in table " + tableName);
			}
			else if (index == orderedNodeIds.size() - 1)
				//last node
				orderedNodeIds.add(hn.getId());
			else
				orderedNodeIds.add(index + 1, hn.getId());
		}
		
		worksheet.addNodeToDataTable(hn, factory);

		return hn;
	}

	//mariam 2012-11-30
	public void removeHNode(String hNodeId,Worksheet worksheet){

		nodes.remove(hNodeId);
		orderedNodeIds.remove(hNodeId);
		worksheet.removeNodeFromDataTable(hNodeId);
	}

	/**
	 * Returns ordered nodeIds.
	 * 
	 * @return ordered nodeIds.
	 * @author mariam
	 */
	public ArrayList<String> getOrderedNodeIds() {
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
		List<HNodePath> x = new LinkedList<>();
		for (HNode hn : getSortedHNodes()) {
			x.add(new HNodePath(hn));
		}
		return expandPaths(x);
	}

	private List<HNodePath> expandPaths(List<HNodePath> paths) {
		List<HNodePath> x = new LinkedList<>();
		for (HNodePath p : paths) {
			if (p.getLeaf().getNestedTable() != null) {
				HTable nestedHTable = p.getLeaf().getNestedTable();
				List<HNodePath> allPaths =  nestedHTable.getAllPaths();
				if(allPaths!=null && allPaths.size()>0) {
					for (HNodePath nestedP : allPaths) {
						x.add(HNodePath.concatenate(p, nestedP));
					}
				}
				else {
					x.add(p);
				}
			} else {
				x.add(p);
			}
		}
		return x;
	}

	/**
	 * Sets the parent HNode.
	 * 
	 * @param hNode
	 * @author mariam
	 */
	public void setParentHNode(HNode hNode) {
		parentHNode = hNode;
	}

	/**
	 * When we automatically add a new column, we must make sure that it's name
	 * does not conflict with a column that was not added automatically. We
	 * append underscores until the name does not conflict.
	 * 
	 * @param columnName
	 *            the name we would like to use.
	 * @return a column name that does not conflict with a source column.
	 */
	String getAutomaticallyAddedColumnName(String columnName) {
		HNode hn = getHNodeFromColumnName(columnName);
		String name = columnName;
		while (hn != null && !hn.isAutomaticallyAdded()) {
			name = "_" + name + "_";
			hn = getHNodeFromColumnName(name);
		}
		return name;
	}
	
	public HNode getNeighborByColumnName(String columnName, RepFactory f)
	{
		HNode result = this.getHNodeFromColumnName(columnName);
		if(null != result)
		{
			return result;
		}
		if(this.parentHNode != null)
		{
			return parentHNode.getNeighborByColumnName(columnName, f);
		}
		return null;
	}
	
	public HNode getHNode(String hNodeId, boolean scanNested) {
		if(!scanNested)
			return getHNode(hNodeId);
		
		HNode node = getHNode(hNodeId);
		if(node == null && hasNestedTables()) {
			for (HNode n : getHNodes()) {
				if (n.hasNestedTable()) {
					HTable nestedTable = n.getNestedTable();
					node = nestedTable.getHNode(hNodeId, scanNested);
					if(node != null)
						break;
				}
					
			}
		}
		
		return node;
	}
}
