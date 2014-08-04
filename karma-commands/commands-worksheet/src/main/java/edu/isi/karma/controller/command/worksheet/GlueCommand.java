package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.util.Util;

public class GlueCommand extends WorksheetSelectionCommand {

	private String hNodeId;
	private String newhNodeId;
	private static Logger logger = LoggerFactory
			.getLogger(GlueCommand.class);

	protected GlueCommand(String id,String worksheetId, 
			String hTableId, String hNodeId, 
			String selectionId) {
		super(id, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return GlueCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Glue";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "Glue";
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		inputColumns.clear();
		outputColumns.clear();
		RepFactory factory = workspace.getFactory();
		Worksheet oldws = workspace.getWorksheet(worksheetId);
		Object para = JSONUtil.createJson(this.getInputParameterJson());
		List<String> hnodeIDs = new ArrayList<String>();
		List<HNode> hnodes = new ArrayList<HNode>();
		JSONArray checked = (JSONArray) JSONUtil.createJson(CommandInputJSONUtil.getStringValue("values", (JSONArray)para));
		HTable ht;
		//System.out.println("here" + hNodeId);
		if (hNodeId.compareTo("") != 0)
			ht = CloneTableUtils.getHTable(oldws.getHeaders(), hNodeId);
		else
			ht = oldws.getHeaders();
		for (int i = 0; i < checked.length(); i++) {
			JSONObject t = (checked.getJSONObject(i));
			hnodeIDs.add((String) t.get("value"));
			hnodes.add(ht.getHNode((String) t.get("value")));
		}
		if (ht != oldws.getHeaders())
			glueNestedTable(oldws, workspace, ht, hnodes, factory);
		else
			glueTopLevel(oldws, workspace, hnodes, factory);
		try{
			UpdateContainer c =  new UpdateContainer();
			c.add(new WorksheetListUpdate());
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(oldws.getId()));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		} catch (Exception e) {
			logger.error("Error in UnfoldCommand" + e.toString());
			e.printStackTrace();
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		UpdateContainer uc = new UpdateContainer();
		HNode ndid = workspace.getFactory().getHNode(newhNodeId);
		HTable currentTable = workspace.getFactory().getHTable(ndid.getHTableId());
		ndid.removeNestedTable();
		//remove the new column
		currentTable.removeHNode(newhNodeId, worksheet);
		uc.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
		uc.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return uc;
	}

	private void glueNestedTable(Worksheet oldws, Workspace workspace, HTable ht, List<HNode> hnodes, RepFactory factory) {
		SuperSelection selection = oldws.getSuperSelectionManager().getSuperSelection(selectionId);
		HTable parentHT = ht.getParentHNode().getHTable(factory);
		List<Table> parentTables = new ArrayList<Table>();
		CloneTableUtils.getDatatable(oldws.getDataTable(), parentHT,parentTables, selection);
		ArrayList<Row> parentRows = new ArrayList<Row>();
		for (Table tmp : parentTables) {
			for (Row row : tmp.getRows(0, tmp.getNumRows(), selection)) {
				parentRows.add(row);
			}
		}
		HNode newNode = ht.addHNode(ht.getNewColumnName("Glue"), HNodeType.Transformation, oldws, factory);
		outputColumns.add(newNode.getId());
		newhNodeId = newNode.getId();
		HTable newht = newNode.addNestedTable(newNode.getColumnName(), oldws, factory);
		List<HNode> childHNodes = new ArrayList<HNode>();
		for (HNode hnode : hnodes) {
			if (hnode.hasNestedTable()) {
				for (HNode hn : hnode.getNestedTable().getHNodes()) {
					childHNodes.add(hn);
				}
			}
		}
		Map<String, String> mapping = CloneTableUtils.cloneHTable(ht, newht, oldws, factory, childHNodes, selection);
		for (Entry<String, String> entry : mapping.entrySet()) {
			outputColumns.add(entry.getValue());
		}
		for (Row parentRow : parentRows) {
			Table t = null;
			for (Node node : parentRow.getNodes()) {
				if (node.hasNestedTable() && node.getNestedTable().getHTableId().compareTo(ht.getId()) == 0) {
					t = node.getNestedTable();
					break;
				}	
			}
			ArrayList<Row> rows = t.getRows(0, t.getNumRows(), selection);
			for (Row row : rows) {
				Table nestedTable = row.getNeighbor(newNode.getId()).getNestedTable();
				int max = 0;
				for (HNode hnode : hnodes) {
					if (!hnode.hasNestedTable())
						continue;
					Node tmp = row.getNeighbor(hnode.getId());
					if (tmp.getNestedTable().getNumRows() > max)
						max = tmp.getNestedTable().getNumRows();
				}
				List<Row> newRows = new ArrayList<Row>();
				for (int i = 0; i < max; i++)
					newRows.add(nestedTable.addRow(factory));
				for (HNode hnode : hnodes) {
					if (!hnode.hasNestedTable())
						continue;
					Node tmp = row.getNeighbor(hnode.getId());
					int i = 0;
					for (Row nestedRow : tmp.getNestedTable().getRows(0, tmp.getNestedTable().getNumRows(), selection)) {
						CloneTableUtils.cloneDataTableExistingRow(nestedRow, newRows.get(i), nestedTable, hnode.getNestedTable(), newht, childHNodes, factory, mapping, selection);
						i++;
					}
				}
			}

		}
	}

	private void glueTopLevel(Worksheet oldws, Workspace workspace, List<HNode> hnodes, RepFactory factory) {
		HTable parentHT = oldws.getHeaders();
		SuperSelection selection = oldws.getSuperSelectionManager().getSuperSelection(selectionId);
		HNode newNode = parentHT.addHNode(parentHT.getNewColumnName("Glue"), HNodeType.Transformation, oldws, factory);
		newhNodeId = newNode.getId();
		outputColumns.add(newhNodeId);
		HTable newht = newNode.addNestedTable(newNode.getColumnName(), oldws, factory);
		List<HNode> childHNodes = new ArrayList<HNode>();
		for (HNode hnode : hnodes) {
			if (hnode.hasNestedTable()) {
				for (HNode hn : hnode.getNestedTable().getHNodes()) {
					childHNodes.add(hn);
				}
			}
		}
		Map<String, String> mapping = CloneTableUtils.cloneHTable(oldws.getHeaders(), newht, oldws, factory, childHNodes, selection);
		for (Entry<String, String> entry : mapping.entrySet()) {
			outputColumns.add(entry.getValue());
		}
		ArrayList<Row> rows = oldws.getDataTable().getRows(0, oldws.getDataTable().getNumRows(), selection);
		for (Row row : rows) {
			Table nestedTable = row.getNeighbor(newNode.getId()).getNestedTable();
			int max = 0;
			for (HNode hnode : hnodes) {
				if (!hnode.hasNestedTable())
					continue;
				Node tmp = row.getNeighbor(hnode.getId());
				if (tmp.getNestedTable().getNumRows() > max) {
					max = tmp.getNestedTable().getNumRows();
				}
			}
			List<Row> newRows = new ArrayList<Row>();
			for (int i = 0; i < max; i++)
				newRows.add(nestedTable.addRow(factory));
			for (HNode hnode : hnodes) {
				if (!hnode.hasNestedTable())
					continue;
				Node tmp = row.getNeighbor(hnode.getId());
				int i = 0;
				for (Row nestedRow : tmp.getNestedTable().getRows(0, tmp.getNestedTable().getNumRows(), selection)) {
					CloneTableUtils.cloneDataTableExistingRow(nestedRow, newRows.get(i), nestedTable, hnode.getNestedTable(), newht, childHNodes, factory, mapping, selection);
					i++;
				}
			}
		}

	}


}
