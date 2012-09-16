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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class SplitByCommaCommand extends WorksheetCommand {
	private final String hNodeId;
	private final String vWorksheetId;
	private final String delimiter;
	private String columnName;
	private HNode hNode;
	private String splitValueHNodeID;

	private HashMap<Node, CellValue> oldNodeValueMap = new HashMap<Node, CellValue>();
	private HashMap<Node, NodeStatus> oldNodeStatusMap = new HashMap<Node, NodeStatus>();

//	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected SplitByCommaCommand(String id, String worksheetId,
			String hNodeId, String vWorksheetId, String delimiter) {
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
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);

		// Get the HNode
		hNode = vWorkspace.getRepFactory().getHNode(hNodeId);
		// The column should not have a nested table but check to make sure!
		if (hNode.hasNestedTable()) {
			c.add(new ErrorUpdate("Cannot split column with nested table!"));
			return c;
		}
		columnName = hNode.getColumnName();
		
		SplitColumnByDelimiter split = new SplitColumnByDelimiter(hNodeId, wk, delimiter, vWorkspace.getWorkspace());
		split.split(oldNodeValueMap, oldNodeStatusMap);
		splitValueHNodeID = split.getSplitValueHNodeID();

		List<HNodePath> columnPaths = wk.getHeaders().getAllPaths();
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
			//pedro 2012-09-15 this does not look correct.
			node.setNestedTable(null, vWorkspace.getRepFactory());
			node.setValue(oldNodeValueMap.get(node), oldNodeStatusMap.get(node), vWorkspace.getRepFactory());
		}

		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, wk,
				columnPaths, vWorkspace);
		vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);

		vw.update(c);
		return c;
	}
}
