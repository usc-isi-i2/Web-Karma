package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

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
import edu.isi.karma.util.Util;

public class UnfoldCommand extends WorksheetSelectionCommand {

	//add column to this table
	//the id of the new column that was created
	//needed for undo
	private String newWorksheetId;
	private String newHNodeId;
	private String keyhNodeId;
	private String valuehNodeId;
	private String keyName;
	private String valueName;
	private boolean notOtherColumn;
	private static Logger logger = LoggerFactory
			.getLogger(FoldCommand.class);

	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}

	protected UnfoldCommand(String id, String worksheetId, 
			String keyHNodeid, String valueHNodeid, boolean notOtherColumn,
			String selectionId) {
		super(id, worksheetId, selectionId);
		newWorksheetId = null;
		newHNodeId = null;
		this.keyhNodeId = keyHNodeid;
		this.valuehNodeId = valueHNodeid;
		this.notOtherColumn = notOtherColumn;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Unfold";
	}

	@Override
	public String getDescription() {
		return keyName + " with " + valueName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		RepFactory factory = workspace.getFactory();
		inputColumns.clear();
		outputColumns.clear();
		Worksheet oldws = workspace.getWorksheet(
				worksheetId);
		Worksheet newws = null;
		HTable ht = factory.getHTable(factory.getHNode(keyhNodeId).getHTableId());
		if (ht == oldws.getHeaders()) {
			newws = unfoldTopLevel(oldws, keyhNodeId, valuehNodeId, workspace, factory);
			this.newWorksheetId = newws.getId();
		}
		else {
			try {
				inputColumns.add(keyhNodeId);
				inputColumns.add(valuehNodeId);
				unfoldNestedLevel(oldws, ht, keyhNodeId, valuehNodeId, factory);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		try{
			UpdateContainer c =  new UpdateContainer();
			c.add(new WorksheetListUpdate());
			if (newws == null)
				c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(oldws.getId(), getSuperSelection(oldws)));
			if (newws != null) {
				c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(newws.getId(), SuperSelectionManager.DEFAULT_SELECTION));
				Alignment alignment = AlignmentManager.Instance().createAlignment(workspace.getId(), newws.getId(), workspace.getOntologyManager());
				c.append(WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(newws.getId(), workspace));
			}
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		} catch (Exception e) {
			WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
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

	public void setValueName(String valueName) {
		this.valueName = valueName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	private void unfoldNestedLevel(Worksheet oldws, HTable ht, String keyHNodeid, String valueHNodeid, RepFactory factory) {
		ArrayList<HNode> topHNodes = new ArrayList<HNode>(ht.getHNodes());
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
		//ArrayList<Row> parentRows = parentTable.getRows(0, parentTable.getNumRows());
		HNode newNode = parentHT.addHNode("Unfold: " + ht.getHNode(keyHNodeid).getColumnName(), HNodeType.Transformation, oldws, factory);
		outputColumns.add(newNode.getId());
		this.newHNodeId = newNode.getId();
		HTable newHT = newNode.addNestedTable("Unfold: " + ht.getHNode(keyHNodeid).getColumnName(), oldws, factory);
		HNode key = ht.getHNode(keyHNodeid);
		HNode value = ht.getHNode(valueHNodeid);
		List<HNode> hnodes = new ArrayList<HNode>();
		List<String> hnodeIds = new ArrayList<String>();
		if (!notOtherColumn) {
			for (HNode h : topHNodes) {
				if (h.getId().compareTo(value.getId()) != 0 && h.getId().compareTo(key.getId()) != 0) {
					hnodes.add(h);
					hnodeIds.add(h.getId());
				}
			}
		}
		for (Entry<String, String> entry : CloneTableUtils.cloneHTable(ht, newHT, oldws, factory, hnodes, selection).entrySet()) {
			outputColumns.add(entry.getValue());
		}
		List<Row> resultRows = new ArrayList<Row>();
		for (Row parentRow: parentRows) {
			Table t = null;
			for (Node node : parentRow.getNodes()) {
				if (node.hasNestedTable() && node.getNestedTable().getHTableId().compareTo(ht.getId()) == 0) {
					t = node.getNestedTable();
					break;
				}	
			}
			Map<String, String> keyMapping = new HashMap<String, String>();
			Map<String, String> HNodeidMapping = new HashMap<String, String>();
			ArrayList<Row> rows = t.getRows(0, t.getNumRows(), selection);
			for (Row row : rows) {
				Node n = row.getNode(key.getId());
				keyMapping.put(HashValueManager.getHashValue(oldws, n.getId(), factory), n.getValue().asString());
			}
			for (String mapkey : keyMapping.keySet()) {
				HNode hn = newHT.getHNodeFromColumnName(keyMapping.get(mapkey).toLowerCase().replace('/', '_'));
				if (hn == null) {
					HNode n = newHT.addHNode(keyMapping.get(mapkey).toLowerCase().replace('/', '_'), HNodeType.Transformation, oldws, factory);
					outputColumns.add(n.getId());
					HTable htt = n.addNestedTable("values", oldws, factory);
					outputColumns.add(htt.addHNode("Values", HNodeType.Transformation, oldws, factory).getId());
					HNodeidMapping.put(keyMapping.get(mapkey), n.getId());
				}
				else
					HNodeidMapping.put(keyMapping.get(mapkey), hn.getId());
			}
			Map<String, ArrayList<String>> hash = new HashMap<String, ArrayList<String>>();
			for (Row row : rows) {
				String hashValue = HashValueManager.getHashValue(row, hnodeIds);
				ArrayList<String> ids = hash.get(hashValue);
				if (ids == null)
					ids = new ArrayList<String>();
				ids.add(row.getId());
				hash.put(hashValue, ids);
				//System.out.println("Hash: " + HashValueManager.getHashValue(row, hnodeIDs));
			}

			for (String hashKey : hash.keySet()) {
				ArrayList<String> r = hash.get(hashKey);
				Node node = parentRow.getNeighbor(newNode.getId());
				Row lastRow = CloneTableUtils.cloneDataTable(factory.getRow(r.get(0)), node.getNestedTable(), 
						parentHT, newHT, hnodes, factory, selection);
				for (String rowid : r) {
					Row cur = factory.getRow(rowid);
					String newId = HNodeidMapping.get(cur.getNode(key.getId()).getValue().asString());
					Node newnode = lastRow.getNode(newId);
					Node oldnode = cur.getNode(value.getId());
					Row tmprow = newnode.getNestedTable().addRow(factory);
					tmprow.getNeighborByColumnName("Values", factory).setValue(oldnode.getValue().asString(), oldnode.getStatus(), factory);
					//newnode.setValue(oldnode.getValue().asString(), oldnode.getStatus(), factory);
				}
				resultRows.add(lastRow);
			}
		}
		for (Row tmpRow : resultRows) {
			for (Node node : tmpRow.getNodes()) {
				if (node.hasNestedTable()) {
					Table tmpTable = node.getNestedTable();
					if (tmpTable.getNumRows() == 0) {
						tmpTable.addRow(factory);
					}
				}
			}
		}

	}
	private Worksheet unfoldTopLevel(Worksheet oldws, String keyHNodeid, String valueHNodeid, Workspace workspace, RepFactory factory) {
		Worksheet newws = factory.createWorksheet("Unfold: " + oldws.getTitle(), workspace, oldws.getEncoding());
		SuperSelection selection = getSuperSelection(oldws);
		newws.getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType, oldws.getMetadataContainer().getWorksheetProperties().getPropertyValue(Property.sourceType));
		ArrayList<HNode> topHNodes = new ArrayList<HNode>(oldws.getHeaders().getHNodes());
		ArrayList<Row> rows = oldws.getDataTable().getRows(0, oldws.getDataTable().getNumRows(), selection);
		HNode key = oldws.getHeaders().getHNode(keyHNodeid);
		HNode value = oldws.getHeaders().getHNode(valueHNodeid);
		List<HNode> hnodes = new ArrayList<HNode>();
		List<String> hnodeIds = new ArrayList<String>();
		if (!notOtherColumn) {
			for (HNode h : topHNodes) {
				if (h.getId().compareTo(value.getId()) != 0 && h.getId().compareTo(key.getId()) != 0) {
					hnodes.add(h);
					hnodeIds.add(h.getId());
				}
			}
		}
		CloneTableUtils.cloneHTable(oldws.getHeaders(), newws.getHeaders(), newws, factory, hnodes, selection);
		Map<String, String> keyMapping = new HashMap<String, String>();
		Map<String, String> HNodeidMapping = new HashMap<String, String>();
		for (Row row : rows) {
			Node n = row.getNode(key.getId());
			keyMapping.put(HashValueManager.getHashValue(oldws, n.getId(), factory), n.getValue().asString());
		}
		for (String mapkey : keyMapping.keySet()) {
			HNode n = newws.getHeaders().addHNode(keyMapping.get(mapkey), HNodeType.Transformation, newws, factory);
			HTable ht = n.addNestedTable("values", newws, factory);
			ht.addHNode("Values", HNodeType.Transformation, newws, factory);
			HNodeidMapping.put(keyMapping.get(mapkey), n.getId());
		}

		Map<String, ArrayList<String>> hash = new TreeMap<String, ArrayList<String>>();
		for (Row row : rows) {
			String hashValue = HashValueManager.getHashValue(row, hnodeIds);
			ArrayList<String> ids = hash.get(hashValue);
			if (ids == null)
				ids = new ArrayList<String>();
			ids.add(row.getId());
			hash.put(hashValue, ids);
		}
		List<Row> resultRows = new ArrayList<Row>();
		for (String hashKey : hash.keySet()) {
			ArrayList<String> r = hash.get(hashKey);
			Row lastRow = CloneTableUtils.cloneDataTable(factory.getRow(r.get(0)), newws.getDataTable(), 
					oldws.getHeaders(), newws.getHeaders(), hnodes, factory, selection);
			for (String rowid : r) {
				Row cur = factory.getRow(rowid);
				String newId = HNodeidMapping.get(cur.getNode(key.getId()).getValue().asString());
				Node newnode = lastRow.getNode(newId);
				Node oldnode = cur.getNode(value.getId());
				Row tmprow = newnode.getNestedTable().addRow(factory);
				tmprow.getNeighborByColumnName("Values", factory).setValue(oldnode.getValue().asString(), oldnode.getStatus(), factory);
				//newnode.setValue(oldnode.getValue().asString(), oldnode.getStatus(), factory);
			}
			resultRows.add(lastRow);
		}
		for (Row tmpRow : resultRows) {
			for (Node node : tmpRow.getNodes()) {
				if (node.hasNestedTable()) {
					Table tmpTable = node.getNestedTable();
					if (tmpTable.getNumRows() == 0) {
						tmpTable.addRow(factory);
					}
				}
			}
		}
		return newws;
	}

}
