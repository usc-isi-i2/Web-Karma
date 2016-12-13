package edu.isi.karma.controller.command.worksheet;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kenai.jffi.Array;

import com.opencsv.CSVReader;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
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
	private SuperSelection selection;
	private final String newhNodeId;
	private String splitValueHNodeId;
	private String regExSplitter;
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public SplitColumnByDelimiter(String hNodeId, Worksheet worksheet,
			String delimiter, Workspace workspace, SuperSelection sel) {
		super();
		this.hNodeId = hNodeId;
		this.worksheet = worksheet;
		this.delimiter = delimiter;
		this.workspace = workspace;
		this.selection = sel;
		this.newhNodeId = null;
		this.regExSplitter = "";
	}

	public SplitColumnByDelimiter(String hNodeId, String newhNodeId, Worksheet worksheet,
			String delimiter, Workspace workspace, SuperSelection sel) {
		super();
		this.hNodeId = hNodeId;
		this.worksheet = worksheet;
		this.delimiter = delimiter;
		this.workspace = workspace;
		this.newhNodeId = newhNodeId;
		this.selection = sel;
		this.regExSplitter = "";
	}

	public String getSplitValueHNodeId() {
		return splitValueHNodeId;
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
		char delimiterChar = getDelimiterChar();

		Collection<Node> nodes = new ArrayList<>();
		worksheet.getDataTable().collectNodes(selectedPath, nodes, selection);

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
		splitValueHNodeId = newTable.addHNode("Values", HNodeType.Transformation, worksheet, factory)
				.getId();

		for (Node node : nodes) {
			//String originalVal = node.getValue().asString();
			String originalVal = oldNodeValueMap.get(node).asString();

			if (originalVal != null && !originalVal.equals("")) {
				// Split the values
				
				try {
					String[] rowValues;
					int startIndex = 0;
					if(delimiterChar == '\u0000') {
						rowValues = originalVal.split(regExSplitter);
						//Ignore first empty one
						if(rowValues.length > 0 && rowValues[0].length() == 0)
							startIndex = 1;
					} else {
						CSVReader reader = new CSVReader(new StringReader(originalVal),
								delimiterChar);
						rowValues = reader.readNext();
						reader.close();
					}
					if (rowValues == null || rowValues.length == 0)
						continue;

					// Get the nested table for the node
					Table table = node.getNestedTable();

					// Add the row one by one
					for (int i = startIndex; i < rowValues.length; i++) {
						String rowVal = rowValues[i];
						if (!rowVal.trim().equals("")) {
							Row row = table.addRow(factory);
							row.setValue(splitValueHNodeId, rowVal,
									NodeStatus.edited, factory);
						}
					}
					
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
			if (path.getLeaf().getId().equals(splitValueHNodeId)) {
				selectedPath = path;
			}
		}
		columnPaths.set(oldPathIndex, selectedPath);
	}

	public void empty() {
		RepFactory factory = workspace.getFactory();
		HTable ht = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		List<Table> tables = new ArrayList<>();
		
		CloneTableUtils.getDatatable(worksheet.getDataTable(), ht, tables, selection);
		for (Table t : tables) {
			for (Row r : t.getRows(0, t.getNumRows(), selection)) {
				Node newNode = r.getNeighbor(newhNodeId);
				newNode.getNestedTable().removeAllRows();
			}
		}
	}
	
	public void split() throws IOException {
		RepFactory factory = workspace.getFactory();
		HTable ht = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		List<Table> tables = new ArrayList<>();
		char delimiterChar  = getDelimiterChar();
		
		CloneTableUtils.getDatatable(worksheet.getDataTable(), ht, tables, selection);
		for (Table t : tables) {
			for (Row r : t.getRows(0, t.getNumRows(), selection)) {
				String orgValue = r.getNeighbor(hNodeId).getValue().asString();
				String[] rowValues;
				int startIndex = 0;
				if(delimiterChar == '\u0000') {
					rowValues = orgValue.split(regExSplitter);
					////Ignore first empty one
					if(rowValues.length > 0 && rowValues[0].length() == 0)
						startIndex = 1;
				} else {
					CSVReader reader = new CSVReader(new StringReader(orgValue),
							delimiterChar);
					rowValues = reader.readNext();
					reader.close();
				}
				if(rowValues != null) {
					Node newNode = r.getNeighbor(newhNodeId);
					for (int i = startIndex; i < rowValues.length; i++) {
						Row dest = newNode.getNestedTable().addRow(factory);
						Node destNode = dest.getNeighborByColumnName("Values", factory);
						destNode.setValue(rowValues[i], NodeStatus.original, factory);
					}
				}
			}
		}
	}

	private char getDelimiterChar() {
		char delimiterChar;
		if (delimiter.equalsIgnoreCase("space"))
			delimiterChar = ' ';
		else if (delimiter.equalsIgnoreCase("tab"))
			delimiterChar = '\t';
		else if (delimiter.equalsIgnoreCase("character")) {
			delimiterChar = '\u0000';
			regExSplitter = "";
		} else if(delimiter.toLowerCase().startsWith("regex:")) {
			delimiterChar = '\u0000';
			regExSplitter = delimiter.substring(6);
		} else {
			delimiterChar = new Character(delimiter.charAt(0));
		}
		return delimiterChar;
	}
	
}
