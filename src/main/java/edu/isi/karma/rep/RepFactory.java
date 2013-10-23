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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.RandomBasedGenerator;

/**
 * @author szekely
 * 
 */
public class RepFactory {

	public RepFactory() {
			uuidGenerator = Generators.randomBasedGenerator();
		
	}
	EthernetAddress nic;
	RandomBasedGenerator uuidGenerator;

	private final Map<String, HNode> hNodes = new HashMap<String, HNode>(100);
	private final Map<String, HTable> hTables = new HashMap<String, HTable>(10);
	private final Map<String, Worksheet> worksheets = new HashMap<String, Worksheet>(10);
	private final Map<String, Table> tables = new HashMap<String, Table>(10);
	private final Map<String, Row> rows = new HashMap<String, Row>(1000);
	private final Map<String, Node> nodes = new HashMap<String, Node>(10000);

	public Worksheet createWorksheet(String tableName, Workspace workspace) {
		String id = getNewId("WS");
		HTable headers = createHTable(tableName);
		Table dataTable = createTable(headers.getId(), id);
		Worksheet ws = new Worksheet(id, headers, dataTable);
		workspace.addWorksheet(ws);
		worksheets.put(id, ws);
		return ws;
	}
        
	public void replaceWorksheet(String id, Worksheet worksheet) {
		if (worksheets.containsKey(id)) {
			worksheets.put(id, worksheet);
			hTables.put(worksheet.getHeaders().getId(), worksheet.getHeaders());
		}
	}

	public String getNewId(String prefix) {
		return prefix + uuidGenerator.generate().toString();
	}

	HNode createHNode(String id, String hTableId, String columnName,
			boolean automaticallyAdded) {
		HNode hn = new HNode(id, hTableId, columnName, automaticallyAdded);
		hNodes.put(id, hn);
		return hn;
	}
	
	HNode createHNode(String hTableId, String columnName,
			boolean automaticallyAdded) {
		String id = getNewId("HN");
		return createHNode(id, hTableId, columnName, automaticallyAdded);
	}

	// added for testing (mariam)
	/**
	 * Returns all HNodes.
	 * 
	 * @return
	 */
	public Collection<HNode> getAllHNodes() {
		return hNodes.values();
	}

	public HNode getHNode(String id) {
		return hNodes.get(id);
	}

	public String getColumnName(String id) {
		return hNodes.get(id).getColumnName();
	}

	public HTable getHTable(String id) {
		return hTables.get(id);
	}

	public Node getNode(String id) {
		return nodes.get(id);
	}

	public Worksheet getWorksheet(String id) {
		return worksheets.get(id);
	}

	public Table getTable(String id) {
		return tables.get(id);
	}
	
	public Row getRow(String id) {
		return rows.get(id);
	}

	HTable createHTable(String id, String tableName) {
		HTable ht = new HTable(id, tableName);
		hTables.put(id, ht);
		return ht;
	}
	
	HTable createHTable(String tableName) {
		String id = getNewId("HT");
		return createHTable(id, tableName);
	}

	Table createTable(String id, String hTableId, String worksheetId) {
		Table t = new Table(worksheetId, id, hTableId);
		tables.put(id, t);
		return t;
	}
	
	Table createTable(String hTableId, String worksheetId) {
		String id = getNewId("T");
		return createTable(id, hTableId, worksheetId);
	}

	Row createRow(String id, String hTableId, String worksheetId) {
		Row r = new Row(id);
		rows.put(id, r);

		HTable ht = hTables.get(hTableId);
		for (String hNodeId : ht.getHNodeIds()) {
			Node n = createNode(hNodeId, worksheetId);
			r.addNode(n);
		}

		return r;
	}
	Row createRow(String hTableId, String worksheetId) {
		String id = getNewId("R");
		return createRow(id, hTableId, worksheetId);
	}
	
	Node createNode(String id, String hNodeId, String worksheetId) {
		Node n = new Node(id, hNodeId);
		nodes.put(id, n);
		HNode hn = hNodes.get(hNodeId);
		HTable nestedHTable = hn.getNestedTable();
		if (nestedHTable != null) {
			n.setNestedTable(createTable(nestedHTable.getId(), worksheetId), this);
		}
		return n;
	}
	
	Node createNode(String hNodeId, String worksheetId) {
		String id = getNewId("N");
		return createNode(id, hNodeId, worksheetId);
	}

}
