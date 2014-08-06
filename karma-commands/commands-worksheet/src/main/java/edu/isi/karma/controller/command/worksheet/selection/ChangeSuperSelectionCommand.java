package edu.isi.karma.controller.command.worksheet.selection;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class ChangeSuperSelectionCommand extends WorksheetCommand {

	private String oldSelectionName;
	private String newSelectionName;
	private static final Logger logger = LoggerFactory.getLogger(ChangeSuperSelectionCommand.class);
	private enum JsonKeys {
		updateType, worksheetId, currentSelectionName
	}
	
	public ChangeSuperSelectionCommand(String id, String worksheetId, 
			String oldSelectionName, String newSelectionName) {
		super(id, worksheetId);
		this.oldSelectionName = oldSelectionName;
		this.newSelectionName = newSelectionName;
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Change Superselection";
	}

	@Override
	public String getDescription() {
		return "From " + oldSelectionName + " to " + newSelectionName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection currentSelection = worksheet.getSuperSelectionManager().getCurrentSuperSelection();
		if (!currentSelection.getName().equals(oldSelectionName))
			return new UpdateContainer(new ErrorUpdate("Cannot change superselection"));
		try {
			worksheet.getSuperSelectionManager().setCurrentSuperSelection(newSelectionName);
		}catch(Exception e) {
			return new UpdateContainer(new ErrorUpdate("Cannot change superselection"));
		}
		return new UpdateContainer(new UpdateCurrentSuperSelection(newSelectionName));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection currentSelection = worksheet.getSuperSelectionManager().getCurrentSuperSelection();
		if (!currentSelection.getName().equals(newSelectionName)) 
			return new UpdateContainer(new ErrorUpdate("Cannot undo change superselection"));
		try {
			worksheet.getSuperSelectionManager().setCurrentSuperSelection(oldSelectionName);
		}catch(Exception e) {
			return new UpdateContainer(new ErrorUpdate("Cannot undo change superselection"));
		}
		return new UpdateContainer(new UpdateCurrentSuperSelection(oldSelectionName));
	}
	
	private class UpdateCurrentSuperSelection extends AbstractUpdate{
		private String currentSelectionName;
		UpdateCurrentSuperSelection (String currentSelectionName) {
			this.currentSelectionName = currentSelectionName;
		}
		@Override
		public void generateJson(String prefix, PrintWriter pw,
				VWorkspace vWorkspace) {
			JSONObject outputObject = new JSONObject();
			try {
				outputObject.put(JsonKeys.updateType.name(), "SetCurrentSuperSelection");
				outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
				outputObject.put(JsonKeys.currentSelectionName.name(), currentSelectionName);
				pw.println(outputObject.toString());
			} catch (JSONException e) {
				e.printStackTrace();
				logger.error("Error occured while generating JSON!");
			}

		}
		
	}

}
