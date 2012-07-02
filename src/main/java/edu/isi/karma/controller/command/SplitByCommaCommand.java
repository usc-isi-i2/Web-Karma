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
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
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
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class SplitByCommaCommand extends WorksheetCommand {
	private final String hNodeId;
	private final String vWorksheetId;
	private final char delimiter;
	private String columnName;
	private HNode hNode;
	private String splitValueHNodeID;

	private HashMap<Node, CellValue> oldNodeValueMap = new HashMap<Node, CellValue>();
	private HashMap<Node, NodeStatus> oldNodeStatusMap = new HashMap<Node, NodeStatus>();

	private final Logger logger = LoggerFactory.getLogger(this.getClass()
			.getSimpleName());

	protected SplitByCommaCommand(String id, String worksheetId,
			String hNodeId, String vWorksheetId, char delimiter) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
		this.delimiter = delimiter;
		
		 addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Split By Comma";
	}

	@Override
	public String getDescription() {
		return columnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		Worksheet wk = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		RepFactory factory = vWorkspace.getRepFactory();
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);

		// Get the HNode
		hNode = vWorkspace.getRepFactory().getHNode(hNodeId);
		// The column should not have a nested table but check to make sure!
		if (hNode.hasNestedTable()) {
			c.add(new ErrorUpdate("Cannot split column with nested table!"));
			return c;
		}
		
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				hNode = path.getLeaf();
				selectedPath = path;
				break;
			}
		}

		columnName = hNode.getColumnName();

		// Add the nested new HTable to the hNode
		HTable newTable = hNode.addNestedTable("Comma Split Values", wk,
				factory);
		splitValueHNodeID = newTable.addHNode("Values", wk, factory)
				.getId();

		Collection<Node> nodes = new ArrayList<Node>();
		wk.getDataTable().collectNodes(selectedPath, nodes);

		for (Node node : nodes) {
			String originalVal = node.getValue().asString();
			oldNodeValueMap.put(node, node.getValue());
			oldNodeStatusMap.put(node, node.getStatus());

			if (originalVal != null && originalVal != "") {
				// Split the values
				CSVReader reader = new CSVReader(new StringReader(originalVal), delimiter);
				try {
					String[] rowValues = reader.readNext();
					if (rowValues == null || rowValues.length == 0)
						continue;

					// Get the nested table for the node
					Table table = node.getNestedTable();

					// Add the row one by one
					for (int i = 0; i < rowValues.length; i++) {
						String rowVal = rowValues[i];
						if(!rowVal.trim().equals("")) {
							Row row = table.addRow(factory);
							row.setValue(splitValueHNodeID, rowVal,
									NodeStatus.edited);
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
		for (HNodePath path : wk.getHeaders().getAllPaths()) {
			if (path.getLeaf().getId().equals(splitValueHNodeID)) {
				selectedPath = path;
			}
		}
		columnPaths.set(oldPathIndex, selectedPath);

		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, wk,
				columnPaths, vWorkspace);
		vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);

		vw.update(c);
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		UpdateContainer c = new UpdateContainer();
		Worksheet wk = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();

		// Get the path which has the split value hNodeId
		HNodePath selectedPath = null;
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(splitValueHNodeID)) {
				selectedPath = path;
			}
		}
		// Clear the nested table for the HNode
		hNode.removeNestedTable();
		
		// Replace the path
		int oldPathIndex = columnPaths.indexOf(selectedPath);
		for (HNodePath path : wk.getHeaders().getAllPaths()) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				hNode = path.getLeaf();
				selectedPath = path;
			}
		}
		columnPaths.set(oldPathIndex, selectedPath);

		// Populate the column with old values
		Collection<Node> nodes = new ArrayList<Node>();
		wk.getDataTable().collectNodes(selectedPath, nodes);

		for (Node node : nodes) {
			node.setNestedTable(null);
			node.setValue(oldNodeValueMap.get(node), oldNodeStatusMap.get(node));
		}

		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, wk,
				columnPaths, vWorkspace);
		vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);

		vw.update(c);
		return c;
	}
}
