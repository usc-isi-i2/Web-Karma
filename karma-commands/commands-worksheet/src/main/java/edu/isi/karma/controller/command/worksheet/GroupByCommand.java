package edu.isi.karma.controller.command.worksheet;


import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.HashValueManager;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.util.Util;

public class GroupByCommand extends WorksheetCommand {
	//if null add column at beginning of table
	//add column to this table
	Command cmd;
	//private 
	//the id of the new column that was created
	//needed for undo
	private String hNodeId;
	private static Logger logger = LoggerFactory
			.getLogger(GroupByCommand.class);

	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}

	protected GroupByCommand(String id,String worksheetId, 
			String hTableId, String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return GroupByCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "GroupBy";
	}

	@Override
	public String getDescription() {
		return "GroupBy";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		RepFactory factory = workspace.getFactory();
		Worksheet oldws = workspace.getWorksheet(worksheetId);
		Object para = JSONUtil.createJson(this.getInputParameterJson());
		List<String> hnodeIDs = new ArrayList<String>();
		List<HNode> keyhnodes = new ArrayList<HNode>();
		List<HNode> valuehnodes = new ArrayList<HNode>();
		JSONArray checked = (JSONArray) JSONUtil.createJson(CommandInputJSONUtil.getStringValue("values", (JSONArray)para));
		HTable ht;
		if (hNodeId.compareTo("") != 0)
			ht = CloneTableUtils.getHTable(oldws.getHeaders(), hNodeId);
		else
			ht = oldws.getHeaders();
		for (int i = 0; i < checked.length(); i++) {
			JSONObject t = (checked.getJSONObject(i));
			hnodeIDs.add((String) t.get("value"));
			keyhnodes.add(ht.getHNode((String) t.get("value")));
		}

		for (HNode oldhnode : ht.getHNodes()) {
			boolean found = false;
			for (HNode keynode : keyhnodes) {
				if (keynode.getId().compareTo(oldhnode.getId()) == 0)
					found = true;
			}
			if (!found)
				valuehnodes.add(oldhnode);
		}

		Worksheet newws = null;
		if (ht == oldws.getHeaders())
			newws = groupByTopLevel(oldws, workspace, hnodeIDs, keyhnodes, valuehnodes, factory);
		else
			groupByNestedTable(oldws, workspace, ht, hnodeIDs, keyhnodes, valuehnodes, factory);
		try{
			UpdateContainer c =  new UpdateContainer();
			c.add(new WorksheetListUpdate());
			if (newws == null)
				c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(oldws.getId()));
			if (newws != null) {
				c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(newws.getId()));
				//c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(newws.getId()));
				Alignment alignment = AlignmentManager.Instance().getAlignmentOrCreateIt(workspace.getId(), newws.getId(), workspace.getOntologyManager());
				SemanticTypeUtil.computeSemanticTypesSuggestion(workspace.getWorksheet(newws.getId()), workspace
						.getCrfModelHandler(), workspace.getOntologyManager());
				c.append(WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(newws.getId(), workspace, alignment));
			}
			//c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(oldws.getId()));
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

	private Worksheet groupByTopLevel(Worksheet oldws, Workspace workspace, List<String> hnodeIDs, List<HNode> keyhnodes, List<HNode> valuehnodes, RepFactory factory) {
		Worksheet newws = factory.createWorksheet("GroupBy: " + oldws.getTitle(), workspace, oldws.getEncoding());
		newws.getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType, oldws.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.sourceType));
		HTable newht =  newws.getHeaders();
		ArrayList<Row> rows = oldws.getDataTable().getRows(0, oldws.getDataTable().getNumRows());
		HTable oldht =  oldws.getHeaders();
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
		//HTable newKeyTable = newht.getHNodeFromColumnName("Keys").addNestedTable("Table for keys", newws, factory);
		//newValueTable.addHNode("Values", newws, factory);
		//HTable newValueNestedTable = newValueTable.getHNodeFromColumnName("Values").addNestedTable("Table for nested values", newws, factory);
		CloneTableUtils.cloneHTable(oldht, newht, newws, factory, keyhnodes);
		newht.addHNode("Values", newws, factory);
		HTable newValueTable = newht.getHNodeFromColumnName("Values").addNestedTable("Table for values", newws, factory);
		CloneTableUtils.cloneHTable(oldht, newValueTable, newws, factory, valuehnodes);
		for (String key : hash.keySet()) {
			//System.out.println("key: " + hash.get(key));
			ArrayList<String> r = hash.get(key);
			Row lastRow = CloneTableUtils.cloneDataTable(CloneTableUtils.getRow(rows, r.get(0)), newws.getDataTable(), oldws.getHeaders(), newht, keyhnodes, factory);
			for (String rowid : r) {
				Row cur = CloneTableUtils.getRow(rows, rowid);
				Table dataTable = lastRow.getNeighborByColumnName("Values", factory).getNestedTable();
				CloneTableUtils.cloneDataTable(cur, dataTable, oldws.getHeaders(), newValueTable, valuehnodes, factory);
			}
		}
		return newws;
	}

	private void groupByNestedTable(Worksheet oldws, Workspace workspace, HTable ht, List<String> hnodeIDs, List<HNode> keyhnodes, List<HNode> valuehnodes, RepFactory factory) {
		HTable parentHT = ht.getParentHNode().getHTable(factory);
		List<Table> parentTables = new ArrayList<Table>();
		CloneTableUtils.getDatatable(oldws.getDataTable(), parentHT,parentTables);
		ArrayList<Row> parentRows = new ArrayList<Row>();
		for (Table tmp : parentTables) {
			for (Row row : tmp.getRows(0, tmp.getNumRows())) {
				parentRows.add(row);
			}
		}
		HNode newNode = parentHT.addHNode(parentHT.getNewColumnName("GroupBy"), oldws, factory);
		HTable newht = newNode.addNestedTable(newNode.getColumnName(), oldws, factory);
		CloneTableUtils.cloneHTable(ht, newht, oldws, factory, keyhnodes);
		newht.addHNode("Values", oldws, factory);
		HTable newValueTable = newht.getHNodeFromColumnName("Values").addNestedTable("Table for values", oldws, factory);
		CloneTableUtils.cloneHTable(ht, newValueTable, oldws, factory, valuehnodes);
		for (Row parentRow : parentRows) {
			Table t = null;
			for (Node node : parentRow.getNodes()) {
				if (node.hasNestedTable() && node.getNestedTable().getHTableId().compareTo(ht.getId()) == 0) {
					t = node.getNestedTable();
					break;
				}	
			}
			ArrayList<Row> rows = t.getRows(0, t.getNumRows());
			Map<String, ArrayList<String>> hash = new TreeMap<String, ArrayList<String>>();
			for (Row row : rows) {
				String hashValue = HashValueManager.getHashValue(row, hnodeIDs);
				ArrayList<String> ids = hash.get(hashValue);
				if (ids == null)
					ids = new ArrayList<String>();
				ids.add(row.getId());
				hash.put(hashValue, ids);
			}	
			
			for (String key : hash.keySet()) {
				ArrayList<String> r = hash.get(key);
				Node node = parentRow.getNeighbor(newNode.getId());
				Row lastRow = CloneTableUtils.cloneDataTable(CloneTableUtils.getRow(rows, r.get(0)), node.getNestedTable(), ht, newht, keyhnodes, factory);
				for (String rowid : r) {
					Row cur = CloneTableUtils.getRow(rows, rowid);
					Table dataTable = lastRow.getNeighborByColumnName("Values", factory).getNestedTable();
					CloneTableUtils.cloneDataTable(cur, dataTable, ht, newValueTable, valuehnodes, factory);
				}
			}
		}
	}

}
