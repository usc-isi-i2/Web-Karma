package edu.isi.karma.controller.command.worksheet;


import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.util.Util;

public class FoldCommand extends WorksheetCommand {
	//if null add column at beginning of table
	private String hNodeId;
	//add column to this table
	private String hTableId;
	Command cmd;
	private Collection<HNode> hnodes = new ArrayList<HNode>();
	//the id of the new column that was created
	//needed for undo
	private String newHNodeId;

	private static Logger logger = LoggerFactory
			.getLogger(FoldCommand.class);

	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}

	protected FoldCommand(String id,String worksheetId, 
			String hTableId, String hNodeId) {
		super(id, worksheetId);
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
		String t = "";
		for (HNode hnode : hnodes) {
			t += hnode.getColumnName() + " ";
		}
		return "" + t;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		//System.out.println("here");
		Worksheet worksheet = workspace.getWorksheet(
				worksheetId);
		Object para = JSONUtil.createJson(this.getInputParameterJson());
		HTable htable =  worksheet.getHeaders();
		hnodes.clear();
		//List<String> HNodeIds = new ArrayList<String>();
		JSONArray checked = (JSONArray) JSONUtil.createJson(CommandInputJSONUtil.getStringValue("values", (JSONArray)para));
		for (int i = 0; i < checked.length(); i++) {
			JSONObject t = (checked.getJSONObject(i));
			hnodes.add(htable.getHNode((String) t.get("value")));
		}
		//System.out.println("HNodeID: " + htable.getHNodeIdFromColumnName("homeworks"));
		//HNodeIds.add(htable.getHNodeIdFromColumnName("homeworks"));
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0, worksheet.getDataTable().getNumRows());
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
				String value = node.getValue().asString();
				JSONObject obj = new JSONObject();
				obj.put("values", value);
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
			cmd = factory.createCommand(input, workspace, hNodeId, worksheetId, hTableId, "test");
			cmd.doIt(workspace);
			UpdateContainer c =  new UpdateContainer();		
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
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

		return WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
	}


	public String getNewHNodeId() {
		return newHNodeId;
	}


}
