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
/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author szekely
 * 
 */
public class HNode extends RepEntity implements Comparable<HNode> {

	// The HTable I belong to.
	private final String hTableId;

	// The name of the column I represent.
	private String columnName;

	// A nested table, possibly null.
	private HTable nestedTable = null;

	// Mark whether this HNode was automatically added by Karma. Need to know
	// this to make sure that we don't over-write columns that came from source
	// data.
	private final boolean automaticallyAdded;
	
	private boolean derivedFromAnotherColumn;
	private String originalColumnHNodeId;
	private HNodeType type;
	private HashMap<String, HashSet<HNode>> commandsApplied;
	
	public enum HNodeType {
		Transformation, Regular, AugmentData
	}

	HNode(String id, String hTableId, String columnName,
			boolean automaticallyAdded, HNodeType type) {
		super(id);
		this.hTableId = hTableId;
		this.columnName = columnName;
		this.automaticallyAdded = automaticallyAdded;
		this.type = type;
		this.commandsApplied = new HashMap<>();
	}

	public String getColumnName() {
		return columnName;
	}

	public String getAbsoluteColumnName(RepFactory f) {
		String name = getColumnName();
		if(this.hTableId != null) {
			HTable table = getHTable(f);
			HNode parent = table.getParentHNode();
			if(parent != null) {
				String parentName = parent.getAbsoluteColumnName(f);
				name = parentName + " &gt; " + name;
			}
		}
		return name;
	}
	
	public String getParentColumnName(RepFactory f) {
		if(this.hTableId != null) {
			HTable table = getHTable(f);
			HNode parent = table.getParentHNode();
			if(parent != null) {
				return parent.getAbsoluteColumnName(f);
			}
		}
		return "";
	}
	
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @return the ID of the HTable containing this HNode.
	 */
	public String getHTableId() {
		return hTableId;
	}

	public boolean hasNestedTable() {
		return nestedTable != null;
	}

	public HTable getNestedTable() {
		return nestedTable;
	}
	
	boolean isAutomaticallyAdded() {
		return automaticallyAdded;
	}
	
	public void setAsDerivedFromAnotherColumn(String originalColumnHNodeId) {
		this.derivedFromAnotherColumn = true;
		this.originalColumnHNodeId = originalColumnHNodeId;
	}

	public boolean isDerivedFromAnotherColumn() {
		return derivedFromAnotherColumn;
	}

	public String getOriginalColumnHNodeId() {
		return originalColumnHNodeId;
	}

	public void setOriginalColumnHNodeId(String originalColumnHNodeId) {
		this.originalColumnHNodeId = originalColumnHNodeId;
	}

	public void setNestedTable(HTable nestedTable) {
		this.nestedTable = nestedTable;
		// mariam
		nestedTable.setParentHNode(this);
	}

	public void removeNestedTable() {
		this.nestedTable = null;
		// Pedro 2012-09-15
		// TODO: this is wrong.If we remove a nested table we have to go to the
		// data table and remove if from all the rows.
	}

	public HTable addNestedTable(String tableName, Worksheet worksheet,
			RepFactory factory) {
		nestedTable = factory.createHTable(tableName);
		// mariam
		nestedTable.setParentHNode(this);
		worksheet.addNestedTableToDataTable(this, factory);
		return nestedTable;
	}

	// mariam
	/**
	 * Returns the HTable that this node belongs to.
	 * 
	 * @param f
	 * @return the HTable that this node belongs to.
	 */
	public HTable getHTable(RepFactory f) {
		return f.getHTable(hTableId);
	}

	// mariam
	/**
	 * Returns the HNodePath for this node.
	 * 
	 * @param factory
	 * @return the HNodePath for this node.
	 */
	public HNodePath getHNodePath(RepFactory factory) {
		HNodePath p1 = new HNodePath(this);
		// get the table that it belongs to
		HTable t = factory.getHTable(hTableId);
		HNode parentNode = t.getParentHNode();
		if (parentNode != null) {
			HNodePath p2 = parentNode.getHNodePath(factory);
			p1 = HNodePath.concatenate(p2, p1);
		}
		return p1;
	}

	@Override
	public void prettyPrint(String prefix, PrintWriter pw, RepFactory factory) {
		pw.print(prefix + "- ");
		pw.print(columnName);
		pw.print("/" + id);
		if (nestedTable != null) {
			pw.println(": ");
			nestedTable.prettyPrint(prefix + "    ", pw, factory);
		} else {
			pw.println();
		}

	}

	public int compareTo(HNode other) {
		return columnName.compareTo(other.getColumnName());
	}

	public JSONArray getJSONArrayRepresentation(RepFactory f)
			throws JSONException {
		JSONArray arr = new JSONArray();
		Stack<HNode> st = new Stack<>();
		st.push(this);
		HTable t = f.getHTable(hTableId);
		HNode parentNode = t.getParentHNode();
		while (parentNode != null) {
			st.push(parentNode);
			t = f.getHTable(parentNode.getHTableId());
			parentNode = t.getParentHNode();
		}

		while (!st.isEmpty()) {
			HNode node = st.pop();
			JSONObject obj = new JSONObject();
			obj.put("columnName", node.getColumnName());
			arr.put(obj);
		}
		return arr;
	}
	
	public List<HNode> getHNodesAccessibleList(RepFactory f) {
		List<HNode> hNodeList = new ArrayList<>();
		HTable table = this.getHTable(f);
		
		while (table != null) {
			for (HNode hNode:table.getHNodes()) {
				hNodeList.add(hNode);
			}
			HNode tableHNode = table.getParentHNode();
			if (tableHNode == null) {
				break;
			} else {
				table = tableHNode.getHTable(f);
			}
		}
		return hNodeList;
	}
	
	public HNode getNeighborByColumnName(String columnName, RepFactory f)
	{
		HTable hTable = f.getHTable(this.hTableId);
		return hTable.getNeighborByColumnName(columnName, f);
	}
	
	public HNodeType getHNodeType() {
		return type;
	}
	
	public void addAppliedCommand(String commandName, HNode newHNode) {
		HashSet<HNode> nodes = this.commandsApplied.get(commandName);
		if(nodes == null)
			nodes = new HashSet<>();
		nodes.add(newHNode);
		this.commandsApplied.put(commandName, nodes);
	}
	
	public final HashMap<String, HashSet<HNode>> getAppliedCommands() {
		return this.commandsApplied;
	}
}
