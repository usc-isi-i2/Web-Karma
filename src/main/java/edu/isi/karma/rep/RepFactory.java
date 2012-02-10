package edu.isi.karma.rep;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author szekely
 * 
 */
public class RepFactory {

	public RepFactory() {
	}

	private int nextId = 1;

	private final Map<String, HNode> hNodes = new HashMap<String, HNode>();
	private final Map<String, HTable> hTables = new HashMap<String, HTable>();
	private final Map<String, Worksheet> worksheets = new HashMap<String, Worksheet>();
	private final Map<String, Workspace> workspaces = new HashMap<String, Workspace>();
	private final Map<String, Table> tables = new HashMap<String, Table>();
	private final Map<String, Row> rows = new HashMap<String, Row>();
	private final Map<String, Node> nodes = new HashMap<String, Node>();

	
	public Workspace createWorkspace() {
		String id = getNewId("WSP");
		Workspace wsp = new Workspace(id, this);
		workspaces.put(id, wsp);
		return wsp;
	}
	
	public Worksheet createWorksheet(String tableName, Workspace workspace) {
		String id = getNewId("WS");
		HTable headers = createHTable(tableName);
		Table dataTable = createTable(headers.getId());
		Worksheet ws = new Worksheet(id, headers, dataTable);
		workspace.addWorksheet(ws);
		worksheets.put(id, ws);
		return ws;
	}
	
	public void removeWorkspace(String workspaceId) {
		workspaces.remove(workspaceId);
	}

	public String getNewId(String prefix) {
		return prefix + (nextId++);
	}

	HNode createHNode(String hTableId, String columnName) {
		String id = getNewId("HN");
		HNode hn = new HNode(id, hTableId, columnName);
		hNodes.put(id, hn);
		return hn;
	}

	//added for testing (mariam)
	/**
	 * Returns all HNodes.
	 * @return
	 */
	public Collection<HNode> getAllHNodes(){
		return hNodes.values();
	}
	
	public HNode getHNode(String id) {
		return hNodes.get(id);
	}

	public HTable getHTable(String id) {
		return hTables.get(id);
	}

	public Node getNode(String id){
		return nodes.get(id);
	}
	
	public Worksheet getWorksheet(String id){
		return worksheets.get(id);
	}
	
	public Table getTable(String id){
		return tables.get(id);
	}
	
	HTable createHTable(String tableName) {
		String id = getNewId("HT");
		HTable ht = new HTable(id, tableName);
		hTables.put(id, ht);
		return ht;
	}

	Table createTable(String hTableId) {
		String id = getNewId("T");
		Table t = new Table(id, hTableId);
		tables.put(id, t);
		return t;
	}

	Row createRow(String hTableId) {
		String id = getNewId("R");
		Row r = new Row(id);
		rows.put(id, r);

		HTable ht = hTables.get(hTableId);
		for (String hNodeId : ht.getHNodeIds()) {
			Node n = createNode(hNodeId);
			r.addNode(n);
		}

		return r;
	}

	Node createNode(String hNodeId) {
		String id = getNewId("N");
		Node n = new Node(id, hNodeId);
		nodes.put(id, n);
		HNode hn = hNodes.get(hNodeId);
		HTable nestedHTable = hn.getNestedTable();
		if (nestedHTable != null) {
			n.setNestedTable(createTable(nestedHTable.getId()));
		}
		return n;
	}

}
