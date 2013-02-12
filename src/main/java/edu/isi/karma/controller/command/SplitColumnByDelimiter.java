package edu.isi.karma.controller.command;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class SplitColumnByDelimiter {

	private final String hNodeId;
	private final Worksheet worksheet;
	private final String delimiter;
	private final Workspace workspace;
	private String splitValueHNodeID;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public SplitColumnByDelimiter(String hNodeId, Worksheet worksheet,
			String delimiter, Workspace workspace) {
		super();
		this.hNodeId = hNodeId;
		this.worksheet = worksheet;
		this.delimiter = delimiter;
		this.workspace = workspace;
	}

	public String getSplitValueHNodeID() {
		return splitValueHNodeID;
	}

	public void split(HashMap<Node, CellValue> oldNodeValueMap,
			HashMap<Node, NodeStatus> oldNodeStatusMap) {
		RepFactory factory = workspace.getFactory();
		HNode hNode = factory.getHNode(hNodeId);

		// The column should not have a nested table but check to make sure!
		if (hNode.hasNestedTable()) {
			logger.error("Column has nested table. Cannot perform split operation.");
			return;
		}

		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				hNode = path.getLeaf();
				selectedPath = path;
				break;
			}
		}

		// Convert the delimiter into character primitive type
		char delimiterChar = ',';
		if (delimiter.equalsIgnoreCase("space"))
			delimiterChar = ' ';
		else if (delimiter.equalsIgnoreCase("tab"))
			delimiterChar = '\t';
		else {
			delimiterChar = new Character(delimiter.charAt(0));
		}

		Collection<Node> nodes = new ArrayList<Node>();
		worksheet.getDataTable().collectNodes(selectedPath, nodes);

		//pedro: 2012-10-09
		// Need to save and clear the values before adding the nested table.
		// Otherwise we have both a value and a nested table, which is not legal.
		for (Node node : nodes) {
			if (oldNodeValueMap != null)
				oldNodeValueMap.put(node, node.getValue());
			if (oldNodeStatusMap != null)
				oldNodeStatusMap.put(node, node.getStatus());
			
			node.clearValue(NodeStatus.edited);
		}
		
		//pedro: 2012-10-09
		// Now that we cleared the values it is safe to add the nested table.
		//
		// Add the nested new HTable to the hNode
		HTable newTable = hNode.addNestedTable("Comma Split Values", worksheet,
				factory);
		splitValueHNodeID = newTable.addHNode("Values", worksheet, factory)
				.getId();
		
		for (Node node : nodes) {
			//String originalVal = node.getValue().asString();
			String originalVal = oldNodeValueMap.get(node).asString();
				
			if (originalVal != null && !originalVal.equals("")) {
				// Split the values
				CSVReader reader = new CSVReader(new StringReader(originalVal),
						delimiterChar);
				try {
					String[] rowValues = reader.readNext();
					if (rowValues == null || rowValues.length == 0)
						continue;

					// Get the nested table for the node
					Table table = node.getNestedTable();

					// Add the row one by one
					for (int i = 0; i < rowValues.length; i++) {
						String rowVal = rowValues[i];
						if (!rowVal.trim().equals("")) {
							Row row = table.addRow(factory);
							row.setValue(splitValueHNodeID, rowVal,
									NodeStatus.edited, factory);
						}
					}
					reader.close();
				} catch (IOException e) {
					logger.error("Error reading Line: " + originalVal, e);
				}
				// Clear the old value
				node.clearValue(NodeStatus.edited);
			}
		}

		// Get the new path with new split value hNode as leaf
		// and replace the old one with it
		int oldPathIndex = columnPaths.indexOf(selectedPath);
		for (HNodePath path : worksheet.getHeaders().getAllPaths()) {
			if (path.getLeaf().getId().equals(splitValueHNodeID)) {
				selectedPath = path;
			}
		}
		columnPaths.set(oldPathIndex, selectedPath);
	}
}
