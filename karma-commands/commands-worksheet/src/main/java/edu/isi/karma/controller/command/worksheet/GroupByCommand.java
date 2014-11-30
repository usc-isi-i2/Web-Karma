package edu.isi.karma.controller.command.worksheet;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDeleteUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
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

public class GroupByCommand extends WorksheetSelectionCommand {
	private String newWorksheetId;
	private String newHNodeId;
	private String hNodeId;
	private static Logger logger = LoggerFactory
			.getLogger(GroupByCommand.class);

	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}

	protected GroupByCommand(String id,String worksheetId, 
			String hTableId, String hNodeId, 
			String selectionId) {
		super(id, worksheetId, selectionId);
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
		List<HNode> keyhnodes = new ArrayList<HNode>();
		List<HNode> valuehnodes = new ArrayList<HNode>();
		JSONArray checked = (JSONArray) JSONUtil.createJson(CommandInputJSONUtil.getStringValue("values", (JSONArray)para));
		HTable ht;
		if (hNodeId.compareTo("") != 0)
			ht = factory.getHTable(factory.getHNode(hNodeId).getHTableId());
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
		else {
			inputColumns.addAll(hnodeIDs);
			groupByNestedTable(oldws, workspace, ht, hnodeIDs, keyhnodes, valuehnodes, factory);
		}
		try{
			UpdateContainer c =  new UpdateContainer();
			WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
			c.add(new WorksheetListUpdate());
			if (newws == null)
				c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(oldws.getId(), getSuperSelection(oldws)));
			if (newws != null) {
				c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(newws.getId(), SuperSelectionManager.DEFAULT_SELECTION));
				//c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(newws.getId()));
				Alignment alignment = AlignmentManager.Instance().createAlignment(workspace.getId(), newws.getId(), workspace.getOntologyManager());
				c.append(WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(newws.getId(), workspace));
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
		UpdateContainer c = new UpdateContainer();
		if (this.newWorksheetId != null) {
			workspace.removeWorksheet(newWorksheetId);
			workspace.getFactory().removeWorksheet(newWorksheetId, workspace.getCommandHistory());
			c.add(new WorksheetListUpdate());
			c.add(new WorksheetDeleteUpdate(newWorksheetId));
		}
		if (this.newHNodeId != null) {
			Worksheet worksheet = workspace.getWorksheet(worksheetId);
			HNode ndid = workspace.getFactory().getHNode(newHNodeId);
			HTable currentTable = workspace.getFactory().getHTable(ndid.getHTableId());
			ndid.removeNestedTable();
			//remove the new column
			currentTable.removeHNode(newHNodeId, worksheet);
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet)));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		}
		return c;
	}

	private Worksheet groupByTopLevel(Worksheet oldws, Workspace workspace, List<String> hnodeIDs, List<HNode> keyhnodes, List<HNode> valuehnodes, RepFactory factory) {
		SuperSelection selection = getSuperSelection(oldws);
		Worksheet newws = factory.createWorksheet("GroupBy: " + oldws.getTitle(), workspace, oldws.getEncoding());
		newws.getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType, oldws.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.sourceType));
		HTable newht =  newws.getHeaders();
		ArrayList<Row> rows = oldws.getDataTable().getRows(0, oldws.getDataTable().getNumRows(), selection);
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
		CloneTableUtils.cloneHTable(oldht, newht, newws, factory, keyhnodes, selection);
		newht.addHNode("Values", HNodeType.Transformation, newws, factory);
		HTable newValueTable = newht.getHNodeFromColumnName("Values").addNestedTable("Table for values", newws, factory);
		CloneTableUtils.cloneHTable(oldht, newValueTable, newws, factory, valuehnodes, selection);
		for (String key : hash.keySet()) {
			//System.out.println("key: " + hash.get(key));
			ArrayList<String> r = hash.get(key);
			Row lastRow = CloneTableUtils.cloneDataTable(factory.getRow(r.get(0)), newws.getDataTable(), oldws.getHeaders(), newht, keyhnodes, factory, selection);
			for (String rowid : r) {
				Row cur = factory.getRow(rowid);
				Table dataTable = lastRow.getNeighborByColumnName("Values", factory).getNestedTable();
				CloneTableUtils.cloneDataTable(cur, dataTable, oldws.getHeaders(), newValueTable, valuehnodes, factory, selection);
			}
		}
		newWorksheetId = newws.getId();
		return newws;
	}

	private void groupByNestedTable(Worksheet oldws, Workspace workspace, HTable ht, List<String> hnodeIDs, List<HNode> keyhnodes, List<HNode> valuehnodes, RepFactory factory) {
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
		HNode newNode = parentHT.addHNode(parentHT.getNewColumnName("GroupBy"), HNodeType.Transformation, oldws, factory);
		newHNodeId = newNode.getId();
		outputColumns.add(newNode.getId());
		HTable newht = newNode.addNestedTable(newNode.getColumnName(), oldws, factory);
		for (Entry<String, String> entry : CloneTableUtils.cloneHTable(ht, newht, oldws, factory, keyhnodes, selection).entrySet()) {
			outputColumns.add(entry.getValue());
		}
		outputColumns.add(newht.addHNode("Values", HNodeType.Transformation, oldws, factory).getId());
		HTable newValueTable = newht.getHNodeFromColumnName("Values").addNestedTable("Table for values", oldws, factory);
		for (Entry<String, String> entry : CloneTableUtils.cloneHTable(ht, newValueTable, oldws, factory, valuehnodes, selection).entrySet()) {
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
				Row lastRow = CloneTableUtils.cloneDataTable(factory.getRow(r.get(0)), node.getNestedTable(), ht, newht, keyhnodes, factory, selection);
				for (String rowid : r) {
					Row cur = factory.getRow(rowid);
					Table dataTable = lastRow.getNeighborByColumnName("Values", factory).getNestedTable();
					CloneTableUtils.cloneDataTable(cur, dataTable, ht, newValueTable, valuehnodes, factory, selection);
				}
			}
		}
	}

}
