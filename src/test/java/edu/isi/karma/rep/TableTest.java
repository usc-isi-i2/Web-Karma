package edu.isi.karma.rep;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import edu.isi.karma.webserver.SampleDataFactory;

public class TableTest extends TestCase {
	
	private Workspace workspace;
	private RepFactory f;

	protected void setUp() throws Exception {
		super.setUp();
		this.f = new RepFactory();
		this.workspace = f.createWorkspace();
	}

	public void testCollectNodes() {
		
		/*** Test it on a relational flat table ***/
		Worksheet ws = SampleDataFactory.createFlatWorksheet(workspace, 5, 5);
		Table table = ws.getDataTable();
		List<HNode> list = new ArrayList<HNode>();
		// Checking for the first column
		HNode node = ws.getHeaders().getHNodeFromColumnName("Column 1");
		list.add(node);
		HNodePath testPath = new HNodePath(list);
		Collection<Node> nodes = new ArrayList<Node>();
		table.collectNodes(testPath, nodes);
		// Check for the size
		assertEquals(5,nodes.size());
		// Check for the values
		int i = 1;
		for(Node n:nodes) {
			System.out.println(n.getValue().asString());
			assertEquals(n.getValue().asString(), "Value " + i);
			i += 5;
		}
		
		/*** Test it on a table with nested tables ***/
		Worksheet nestedWS = SampleDataFactory.createSampleJson(workspace, 3);
		// Getting the HNodePath for c.3.4.1 column
		HNodePath nestedTestPath = nestedWS.getHeaders().getAllPaths().get(9);
		Collection<Node> nestedNodes = new ArrayList<Node>();
		nestedWS.getDataTable().collectNodes(nestedTestPath, nestedNodes);
		
		//mariam:just to test
		/*
		System.out.println("Get node iDS="+nestedWS.getHeaders().getOrderedNodeIds());
		List<HNodePath> ps = nestedWS.getHeaders().getAllPaths();
		for(HNodePath p:ps){
			System.out.println("PATH:"+p.toString());
		}
		for(HNode n: f.getAllHNodes()){
			System.out.println("Path for " + n.getId() + n.getColumnName() + ":" + n.getHNodePath(f));
			System.out.println("Path for " + n.getId() + n.getColumnName() + ":" + n.getHNodePath(f).toColumnNames(f));
		}
		*/
		//////////////////////
		
		// Check for the size
		assertEquals(14,nestedNodes.size());
		// Check for the values
		for(Node n:nestedNodes) {
			assertEquals(n.getValue().asString(), "c.3.4.1_X");
		}
		
		/*** Test it with paths that don't exist in the table ***/
		// Collecting nodes from the relational table using a path from nested table
		HNodePath bogusPath = nestedWS.getHeaders().getAllPaths().get(0);
		Collection<Node> bogusNodes = new ArrayList<Node>();
		table.collectNodes(bogusPath, bogusNodes);
		assertEquals(0, bogusNodes.size());
	}
}
