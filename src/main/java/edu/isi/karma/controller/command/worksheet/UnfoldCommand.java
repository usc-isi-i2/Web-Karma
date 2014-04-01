package edu.isi.karma.controller.command.worksheet;


import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
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

public class UnfoldCommand extends WorksheetCommand {
	//if null add column at beginning of table
	//add column to this table
	Command cmd;
	//private 
	//the id of the new column that was created
	//needed for undo
	private String newHNodeId;

	private static Logger logger = LoggerFactory
			.getLogger(UnfoldCommand.class);

	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}

	protected UnfoldCommand(String id,String worksheetId, 
			String hTableId, String hNodeId) {
		super(id, worksheetId);
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return UnfoldCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Unfold";
	}

	@Override
	public String getDescription() {
		return "Unfold";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		RepFactory factory = workspace.getFactory();
		Worksheet oldws = workspace.getWorksheet(worksheetId);
		Worksheet newws = factory.createWorksheet("Unfold: " + oldws.getTitle(), workspace, oldws.getEncoding());
		Object para = JSONUtil.createJson(this.getInputParameterJson());
		HTable oldht =  oldws.getHeaders();
		HTable newht =  newws.getHeaders();
		ArrayList<Row> rows = oldws.getDataTable().getRows(0, oldws.getDataTable().getNumRows());
		List<String> hnodeIDs = new ArrayList<String>();
		List<HNode> keyhnodes = new ArrayList<HNode>();
		List<HNode> valuehnodes = new ArrayList<HNode>();
		JSONArray checked = (JSONArray) JSONUtil.createJson(CommandInputJSONUtil.getStringValue("values", (JSONArray)para));
		
		for (int i = 0; i < checked.length(); i++) {
			JSONObject t = (checked.getJSONObject(i));
			hnodeIDs.add((String) t.get("value"));
			keyhnodes.add(oldht.getHNode((String) t.get("value")));
		}

		for (HNode oldhnode : oldht.getHNodes()) {
			boolean found = false;
			for (HNode keynode : keyhnodes) {
				if (keynode.getId().compareTo(oldhnode.getId()) == 0)
					found = true;
			}
			if (!found)
				valuehnodes.add(oldhnode);
		}
		Map<String, ArrayList<String>> hash = new TreeMap<String, ArrayList<String>>();
		for (Row row : rows) {
			String hashValue = HashValueManager.getHashValue(row, hnodeIDs);
			ArrayList<String> ids = hash.get(hashValue);
			if (ids == null)
				ids = new ArrayList<String>();
			ids.add(row.getId());
			hash.put(hashValue, ids);
			//System.out.println("Hash: " + HashValueManager.getHashValue(row, hnodeIDs));
		}
		newht.addHNode("Keys", newws, factory);
		newht.addHNode("Values", newws, factory);
		
		HTable newKeyTable = newht.getHNodeFromColumnName("Keys").addNestedTable("Table for keys", newws, factory);
		HTable newValueTable = newht.getHNodeFromColumnName("Values").addNestedTable("Table for values", newws, factory);
		//newValueTable.addHNode("Values", newws, factory);
		//HTable newValueNestedTable = newValueTable.getHNodeFromColumnName("Values").addNestedTable("Table for nested values", newws, factory);
		cloneHTable(oldht, newKeyTable, newws, factory, keyhnodes);
		cloneHTable(oldht, newValueTable, newws, factory, valuehnodes);
		for (String key : hash.keySet()) {
			//System.out.println("key: " + hash.get(key));
			ArrayList<String> r = hash.get(key);
			Row firstrow = newws.addRow(factory);
			Row lastRow = cloneDataTable(getRow(rows, r.get(0)), firstrow.getNeighborByColumnName("Keys", factory).getNestedTable(), oldws.getHeaders(), newKeyTable, keyhnodes, factory);
			for (String rowid : r) {
				Row cur = getRow(rows, rowid);
				Table dataTable = lastRow.getNeighborByColumnName("Values", factory).getNestedTable();
				cloneDataTable(cur, dataTable, oldws.getHeaders(), newValueTable, valuehnodes, factory);
			}
		}
		//cloneDataTable(oldws.getDataTable(), firstrow.getNeighborByColumnName("Keys", factory).getNestedTable(), oldws.getHeaders(), newKeyTable, keyhnodes, factory);
		//cloneDataTable(oldws.getDataTable(), firstrow.getNeighborByColumnName("Values", factory).getNestedTable(), oldws.getHeaders(), newValueTable, valuehnodes, factory);	
		try{
			UpdateContainer c =  new UpdateContainer();
			c.add(new WorksheetListUpdate());
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(oldws.getId()));
			c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(newws.getId()));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		} catch (Exception e) {
			logger.error("Error in UnfoldCommand" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		//cmd.undoIt(workspace);
		//remove the new column
		//currentTable.removeHNode(newHNodeId, worksheet);
		//c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return null;
	}


	public String getNewHNodeId() {
		return newHNodeId;
	}

	private void cloneHTable(HTable oldht, HTable newht, Worksheet newws, RepFactory factory, List<HNode> hnodes) {
		Collections.sort(hnodes);
		for (HNode hnode : hnodes) {
			HNode newhnode = newht.addHNode(hnode.getColumnName(), newws, factory);
			if (hnode.hasNestedTable()) {
				HTable oldnested = hnode.getNestedTable();
				HTable newnested = newhnode.addNestedTable(hnode.getNestedTable().getTableName(), newws, factory);		
				cloneHTable(oldnested, newnested, newws, factory, new ArrayList<HNode>(oldnested.getHNodes()));
			}
		}
	}

	private Row cloneDataTable(Row oldRow, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory) {
		Row newrow = newDataTable.addRow(factory);
		for (HNode hnode : hnodes) {
			HNode newHNode = newHTable.getHNodeFromColumnName(hnode.getColumnName());
			Node oldNode = oldRow.getNode(hnode.getId());
			Node newNode = newrow.getNode(newHNode.getId());
			if (!oldNode.hasNestedTable()) {
				newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
			}
			else {					
				cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory);
			}
		}
		return newrow;
	}

	private void cloneDataTable(Table oldDataTable, Table newDataTable, HTable oldHTable, HTable newHTable, List<HNode> hnodes, RepFactory factory) {
		ArrayList<Row> rows = oldDataTable.getRows(0, oldDataTable.getNumRows());
		for (Row row : rows) {
			Row newrow = newDataTable.addRow(factory);
			for (HNode hnode : hnodes) {
				HNode newHNode = newHTable.getHNodeFromColumnName(hnode.getColumnName());
				Node oldNode = row.getNode(hnode.getId());
				Node newNode = newrow.getNode(newHNode.getId());
				if (!oldNode.hasNestedTable()) {
					newNode.setValue(oldNode.getValue(), oldNode.getStatus(), factory);
				}
				else {					
					cloneDataTable(oldNode.getNestedTable(), newNode.getNestedTable(), hnode.getNestedTable(), newHNode.getNestedTable(), hnode.getNestedTable().getSortedHNodes(), factory);
				}
			}
		}
	}

	private Row getRow(List<Row> rows, String rowID) {
		for (Row row : rows) {
			if (row.getId().compareTo(rowID) == 0)
				return row;
		}
		return null;
	}

}
