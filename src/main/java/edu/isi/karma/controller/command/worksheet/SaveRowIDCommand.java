package edu.isi.karma.controller.command.worksheet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.util.Util;

public class SaveRowIDCommand extends WorksheetCommand {
	//if null add column at beginning of table
	@SuppressWarnings("unused")
	private final String hNodeId;
	//add column to this table
	private String hTableId;
	
	private static Logger logger = LoggerFactory
	.getLogger(SaveRowIDCommand.class);
	
	public enum JsonKeys {
		updateType, hNodeId, worksheetId
	}
	
	protected SaveRowIDCommand(String id,String worksheetId, 
			String hTableId, String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return SaveRowIDCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Save RowID";
	}

	@Override
	public String getDescription() {
			return "Save RowID";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
		try{
			JSONArray checked = (JSONArray) JSONUtil.createJson(CommandInputJSONUtil.getStringValue("values", (JSONArray)JSONUtil.createJson(this.getInputParameterJson())));
			for (int i = 0; i < checked.length(); i++) {
				JSONObject t = (checked.getJSONObject(i));
				System.out.println("selected row: " + t.get("value"));
			}
			UpdateContainer c =  new UpdateContainer();		
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
			c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			return c;
		} catch (Exception e) {
			logger.error("Error in AddColumnCommand" + e.toString());
			Util.logException(logger, e);
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
	}
	
}
