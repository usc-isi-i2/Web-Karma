package edu.isi.karma.controller.command.worksheet;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
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

public class FoldCommand extends WorksheetSelectionCommand {
	//if null add column at beginning of table
	private String hNodeId;
	//add column to this table
	private String hTableId;
	Command cmd;
	private List<HNode> hnodes = new ArrayList<>();
	//the id of the new column that was created
	//needed for undo
	private String newHNodeId;
	private String label;
	
	private static Logger logger = LoggerFactory
			.getLogger(FoldCommand.class);

	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}

	protected FoldCommand(String id, String model, String worksheetId, 
			String hTableId, String hNodeId, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return FoldCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Fold";
	}

	@Override
	public String getDescription() {
		return this.label;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		RepFactory Repfactory = workspace.getFactory();
		Worksheet worksheet = workspace.getWorksheet(
				worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		inputColumns.clear();
		outputColumns.clear();
		Object para = JSONUtil.createJson(this.getInputParameterJson());
		HTable htable;
		if (hNodeId.compareTo("") != 0)
			htable = Repfactory.getHTable(Repfactory.getHNode(hNodeId).getHTableId());
		else
			htable = worksheet.getHeaders();
		hnodes.clear();
		//List<String> HNodeIds = new ArrayList<String>();
		JSONArray checked = (JSONArray) JSONUtil.createJson(CommandInputJSONUtil.getStringValue("values", (JSONArray)para));
		for (int i = 0; i < checked.length(); i++) {
			JSONObject t = (checked.getJSONObject(i));
			hnodes.add(htable.getHNode((String) t.get("value")));
			inputColumns.add(t.getString("value"));
		}
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0, worksheet.getDataTable().getNumRows(), selection);
		if (htable != worksheet.getHeaders()) {
			HTable parentHT = htable.getParentHNode().getHTable(Repfactory);
			List<Table> parentTables = new ArrayList<>();
			CloneTableUtils.getDatatable(worksheet.getDataTable(), parentHT,parentTables, selection);
			ArrayList<Row> parentRows = new ArrayList<>();
			rows.clear();
			for (Table tmp : parentTables) {
				for (Row row : tmp.getRows(0, tmp.getNumRows(), selection)) {
					parentRows.add(row);
				}
			}
			for (Row parentRow : parentRows) {
				Table t = null;
				for (Node node : parentRow.getNodes()) {
					if (node.hasNestedTable() && node.getNestedTable().getHTableId().compareTo(htable.getId()) == 0) {
						t = node.getNestedTable();
						break;
					}	
				}
				ArrayList<Row> tmpRows = t.getRows(0, t.getNumRows(), selection);
				for (Row r : tmpRows) {
					rows.add(r);
				}
			}
		}
		
		this.label = "";
		if(!hnodes.isEmpty())
			this.label = hnodes.get(0).getParentColumnName(Repfactory);
		String sep = " &gt; ";
		for(HNode hnode : hnodes) {
			this.label += sep + hnode.getColumnName();
			sep = ", ";
		}
		
		
		//System.out.println("HNodeID: " + htable.getHNodeIdFromColumnName("homeworks"));
		//HNodeIds.add(htable.getHNodeIdFromColumnName("homeworks"));
		
		//HashValueManager.getHashValue(rows.get(0), HNodeIds);
		//hnodes.add(htable.getHNode("HN5"));
		//hnodes.add(htable.getHNode("HN7"));
		JSONArray array = new JSONArray();
		JSONArray input = new JSONArray();
		for (Row row : rows) {
			String id = row.getId();
			JSONArray t = new JSONArray();
			for (HNode hnode : hnodes) {
				Node node = row.getNode(hnode.getId());
				String name = hnode.getColumnName();
				Object value = CloneTableUtils.cloneNodeToJSON(hnode, node, selection);
				JSONObject obj = new JSONObject();
				JSONObject obj2 = new JSONObject();
				if (value instanceof String)
					obj2.put("values", value);
				else
					obj2.put(worksheet.getHeaders().getNewColumnName("nested"), value);
				obj.put("values", obj2);
				obj.put("names", name);
				t.put(obj);			
			}
			JSONObject obj = new JSONObject();
			obj.put("rowId", id);
			obj.put("rowIdHash", "");
			obj.put("values", t);
			array.put(obj);		
		}
		JSONObject obj = new JSONObject();
		obj.put("name", "AddValues");
		obj.put("value", array.toString());
		obj.put("type", "other");
		input.put(obj);
		try{
			AddValuesCommandFactory factory = new AddValuesCommandFactory();
			//hNodeId = hnodes.get(0).getId();
			cmd = factory.createCommand(input, model, workspace, hNodeId, worksheetId, hTableId, worksheet.getHeaders().getNewColumnName("fold"), HNodeType.Transformation, selection.getName());
			cmd.doIt(workspace);
			outputColumns.addAll(cmd.getOutputColumns());
			WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
			UpdateContainer c =  new UpdateContainer();		
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		} catch (Exception e) {
			logger.error("Error in FoldCommand" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		cmd.undoIt(workspace);
		//remove the new column
		//currentTable.removeHNode(newHNodeId, worksheet);
		UpdateContainer c =  new UpdateContainer();		
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace), workspace.getContextId()));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}


	public String getNewHNodeId() {
		return newHNodeId;
	}


}
