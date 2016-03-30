package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AddColumnUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
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
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.util.Util;
import edu.isi.karma.webserver.KarmaException;

public class AddValuesCommand extends WorksheetSelectionCommand{


	private final String hNodeId;
	//add column to this table
	private String hTableId;

	//the id of the new column that was created
	//needed for undo
	private String newHNodeId;
	private HNodeType type;
	private String newColumnName = "";
	private boolean isNewNode;
	private static Logger logger = LoggerFactory
			.getLogger(AddValuesCommand.class);

	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}

	protected AddValuesCommand(String id, String model, String worksheetId, 
			String hTableId, String hNodeId, HNodeType type, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		isNewNode = false;
		this.type = type;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return AddValuesCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add Values Command";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(
				worksheetId);
		inputColumns.clear();
		outputColumns.clear();
		Object para = JSONUtil.createJson(this.getInputParameterJson());
		String addValues;	
		HNode ndid = null;
		try{
			if (para instanceof JSONArray) {
				//System.out.println("JSONArray:" + para);
				addValues = CommandInputJSONUtil.getStringValue("AddValues", (JSONArray)para);
				Object t = JSONUtil.createJson(addValues);
				if (t instanceof JSONArray) {
					JSONArray a = (JSONArray) t;
					ndid = addColumn(workspace, worksheet, newColumnName, a);
				}
			}
			WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
			UpdateContainer c =  new UpdateContainer(new AddColumnUpdate(newHNodeId, worksheetId));		
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));
			if (ndid == null) {
				System.err.println("error: ndid");
			}
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		} catch (Exception e) {
			logger.error("Error in AddColumnCommand" + e.toString());
			Util.logException(logger, e);
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		HTable currentTable = workspace.getFactory().getHTable(hTableId);
		HNode ndid = workspace.getFactory().getHNode(newHNodeId);
		ndid.removeNestedTable();
		//remove the new column
		currentTable.removeHNode(newHNodeId, worksheet);
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}


	public String getNewHNodeId() {
		return newHNodeId;
	}
	
	public boolean isNewNode() {
		return isNewNode;
	}

	public void setColumnName(String name) {
		this.newColumnName = name;
	}

	private HNode addColumn(Workspace workspace, Worksheet worksheet, String newColumnName, JSONArray array) throws KarmaException {
		if(hTableId==null || hTableId.isEmpty()){
			//get table id based on the hNodeId
			if(hNodeId==null)
				hTableId = worksheet.getHeaders().getId();
			else
				hTableId = workspace.getFactory().getHNode(hNodeId).getHTableId();
		}
		HTable hTable = workspace.getFactory().getHTable(hTableId);
		if(hTable == null)
		{
			logger.error("No HTable for id "+ hTableId);
			throw new KarmaException("No HTable for id "+ hTableId );
		}

		//add new column to this table
		//add column after the column with hNodeId
		HNode ndid;
		if (newColumnName != null && !newColumnName.trim().isEmpty()) {
			if (hTable.getHNodeFromColumnName(newColumnName) != null) {
				ndid = hTable.getHNodeFromColumnName(newColumnName);
				outputColumns.add(ndid.getId());
			}
			else {
				ndid = hTable.addNewHNodeAfter(hNodeId, type, workspace.getFactory(), newColumnName, worksheet,true);
				outputColumns.add(ndid.getId());
				isNewNode = true;
			}
		}
		else {
			ndid = hTable.addNewHNodeAfter(hNodeId, type, workspace.getFactory(), hTable.getNewColumnName("default"), worksheet,true);
			outputColumns.add(ndid.getId());
			isNewNode = true;
		}
		newHNodeId = ndid.getId();
		//add as first column in the table if hNodeId is null
		//HNode ndid = currentTable.addNewHNodeAfter(null, vWorkspace.getRepFactory(), newColumnName, worksheet,true);
		if (array != null) {
			populateRowsWithDefaultValues(worksheet, workspace.getFactory(), array, hTable);
		}
		//save the new hNodeId for undo

		return ndid;
	}

	private void populateRowsWithDefaultValues(Worksheet worksheet, RepFactory factory, JSONArray array, HTable htable) {
		SuperSelection selection = getSuperSelection(worksheet);
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.contains(factory.getHNode(newHNodeId))) {	
				if (path.getLeaf().getId().compareTo(newHNodeId) != 0) {
					HNodePath hp = new HNodePath();
					HNode hn = path.getFirst();
					while (hn.getId().compareTo(newHNodeId) != 0) {
						hp.addHNode(hn);
						path = path.getRest();
						hn = path.getFirst();
					}
					hp.addHNode(hn);
					selectedPath = hp;
				}
				else
					selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<>(Math.max(1000, worksheet.getDataTable().getNumRows()));
		worksheet.getDataTable().collectNodes(selectedPath, nodes, selection);	
		for (Node node : nodes) {
			for (int i = 0; i < array.length(); i++) {
				if (array.get(i) instanceof JSONObject) {
					JSONObject obj = (JSONObject)array.get(i);
					if (node.getBelongsToRow().getId().compareTo(obj.getString("rowId")) == 0) {
						Object t = obj.get("values");
						if (t instanceof String) {
							String value = (String)t;
							addValues(node, value, factory, null);
						}
						else if (t instanceof JSONObject) {
							addJSONObjectValues((JSONObject)t, worksheet, htable, factory, node.getBelongsToRow(), newHNodeId);
						}
						else if (t instanceof JSONArray) {
							addJSONArrayValues((JSONArray)t, worksheet, htable, factory, node.getBelongsToRow(), newHNodeId);
						}
					}
				}
			}

		}
	}

	private boolean addJSONArrayValues(JSONArray array, Worksheet worksheet, HTable htable, RepFactory factory, Row row, String newHNodeId) {
		boolean flag = false;
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = (JSONObject)array.get(i);
			flag |= addJSONObjectValues(obj, worksheet, htable, factory, row, newHNodeId);
		}
		return flag;
	}

	private boolean addJSONObjectValues(JSONObject obj, Worksheet worksheet, HTable htable, RepFactory factory, Row row, String newHNodeId) {
		HNode ndid = htable.getHNode(newHNodeId);
		HTable nestedHTable = ndid.getNestedTable();
		if (nestedHTable == null)
			nestedHTable = ndid.addNestedTable("Table for test",
					worksheet, factory);
		Table nestedTable = row.getNode(newHNodeId).getNestedTable();
		Row r = nestedTable.addRow(factory);
		boolean flag = false;
		for (Object key : IteratorUtils.toList(obj.keys())) {
			Object value = obj.get(key.toString());
			HNode h = nestedHTable.getHNodeFromColumnName(key.toString());
			if ( h == null) {		
				h = nestedHTable.addHNode(key.toString(), type, worksheet, factory);
			}
			outputColumns.add(h.getId());
			//
			if (value instanceof String)
				flag |= addValues(r.getNode(h.getId()), (String)value, factory, nestedTable);
			if (value instanceof JSONObject)
				flag |= addJSONObjectValues((JSONObject)value, worksheet, nestedHTable, factory, r, h.getId());
			if (value instanceof JSONArray) 
				flag |= addJSONArrayValues((JSONArray) value, worksheet, nestedHTable, factory,r, h.getId());
		}
		if (!flag)
			nestedTable.removeRow(r);
		return flag;
	}

	private boolean addValues(Node node, String value, RepFactory factory, Table table) {
		Worksheet worksheet = factory.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		boolean flag = true;
		if (table != null) {
			for (Row r : table.getRows(0, table.getNumRows(), selection)) {
				Node n = r.getNeighbor(node.getHNodeId());
				if (n.getValue() != null && n.getValue().asString().compareTo(value) == 0) { 
					flag = false;
					break;
				}
			}
		}
		if (flag)
			node.setValue(value, NodeStatus.original, factory);
		return flag;
	}

}
