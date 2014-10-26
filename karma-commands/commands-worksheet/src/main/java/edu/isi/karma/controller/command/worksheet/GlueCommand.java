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
import edu.isi.karma.util.Util;

public class GlueCommand extends WorksheetSelectionCommand {

	private String hNodeId;
	private String newhNodeId;
	private String hNodeIdList;
	private GlueMethod implMethod;
	private enum GlueMethod{
		Longest, Shortest, CrossProduct
	}
	List<String> hNodeName;
	private static Logger logger = LoggerFactory
			.getLogger(GlueCommand.class);

	protected GlueCommand(String id,String worksheetId, 
			String hNodeId, String selectionId, 
			String hNodeIdList, String implMethod) {
		super(id, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.hNodeIdList = hNodeIdList;
		if (implMethod != null && !implMethod.isEmpty()) {
			this.implMethod = GlueMethod.valueOf(implMethod);
		}
		else {
			this.implMethod = GlueMethod.Longest;
		}
		hNodeName = new ArrayList<String>();
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return GlueCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Glue";
	}

	@Override
	public String getDescription() {
		StringBuilder builder = new StringBuilder();
		for (String name : hNodeName) {
			builder.append(name + " ");
		}
		return builder.substring(0, builder.length());
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		inputColumns.clear();
		outputColumns.clear();
		hNodeName.clear();
		RepFactory factory = workspace.getFactory();
		Worksheet oldws = workspace.getWorksheet(worksheetId);
		List<HNode> hnodes = new ArrayList<HNode>();
		JSONArray checked = new JSONArray(hNodeIdList);
		HTable ht;
		if (hNodeId.compareTo("") != 0)
			ht = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
		else
			ht = oldws.getHeaders();
		for (int i = 0; i < checked.length(); i++) {
			JSONObject t = (checked.getJSONObject(i));
			HNode hNode = ht.getHNode(t.getString("value"));
			if (hNode != null) {
				hnodes.add(hNode);
				hNodeName.add(hNode.getColumnName());
			}			
		}
		if (ht != oldws.getHeaders())
			glueNestedTable(oldws, workspace, ht, hnodes, factory);
		else
			glueTopLevel(oldws, workspace, hnodes, factory);
		try{
			UpdateContainer c =  new UpdateContainer();
			c.add(new WorksheetListUpdate());
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(oldws.getId(), getSuperSelection(oldws)));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
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
		uc.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet)));
		uc.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return uc;
	}

	private void glueNestedTable(Worksheet oldws, Workspace workspace, HTable ht, List<HNode> hnodes, RepFactory factory) {
		SuperSelection selection = getSuperSelection(oldws);
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
				generateRows(hnodes, selection, row, nestedTable, factory, mapping, childHNodes, newht);
			}

		}
	}

	private void glueTopLevel(Worksheet oldws, Workspace workspace, List<HNode> hnodes, RepFactory factory) {
		HTable parentHT = oldws.getHeaders();
		SuperSelection selection = getSuperSelection(oldws);
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
			generateRows(hnodes, selection, row, nestedTable, factory, mapping, childHNodes, newht);
		}

	}

	private void generateRows(List<HNode> hnodes, SuperSelection selection, Row row, 
			Table nestedTable, RepFactory factory, Map<String, String> mapping, 
			List<HNode> childHNodes, HTable newht) {
		if (implMethod != GlueMethod.CrossProduct) {
			int max = implMethod == GlueMethod.Longest ? Integer.MIN_VALUE : Integer.MAX_VALUE;
			for (HNode hnode : hnodes) {
				if (!hnode.hasNestedTable())
					continue;
				Node tmp = row.getNeighbor(hnode.getId());
				int size = tmp.getNestedTable().getRows(0, tmp.getNestedTable().getNumRows(), selection).size();
				if (size > max && implMethod == GlueMethod.Longest) {
					max = size;
				}
				if (size < max && implMethod == GlueMethod.Shortest) {
					max = size;
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
					if (i >= max) {
						break;
					}
					CloneTableUtils.cloneDataTableExistingRow(nestedRow, newRows.get(i), nestedTable, hnode.getNestedTable(), newht, childHNodes, factory, mapping, selection);
					i++;
				}
			}
		}
		else {
			List<List<Row>> tablesToCross = new ArrayList<List<Row>>();
			for (HNode hnode : hnodes) {
				if (!hnode.hasNestedTable())
					continue;
				Node tmp = row.getNeighbor(hnode.getId());
				ArrayList<Row> rowsInTable = new ArrayList<Row>();
				tablesToCross.add(rowsInTable);
				for (Row nestedRow : tmp.getNestedTable().getRows(0, tmp.getNestedTable().getNumRows(), selection)) {
					rowsInTable.add(nestedRow);
				}
			}
			int size = tablesToCross.size();
			int enumeration[] = new int[size + 1];
			while(enumeration[size] != 1) {
				Row r = nestedTable.addRow(factory);
				for (int i = 0; i < tablesToCross.size(); i++) {
					Row nestedRow = tablesToCross.get(i).get(enumeration[i]);
					HNode hnode = factory.getHNode(nestedRow.getBelongsToTable().getNestedTableInNode().getHNodeId());
					CloneTableUtils.cloneDataTableExistingRow(nestedRow, r, nestedTable, hnode.getNestedTable(), newht, childHNodes, factory, mapping, selection);
				}
				enumeration[0]++;
				for (int i = 0; i < tablesToCross.size(); i++) {
					if (enumeration[i] == tablesToCross.get(i).size()) {
						enumeration[i + 1]++;
						enumeration[i] = 0;
					}
				}
			}
			System.out.println(nestedTable);
		}
	}

}
