package edu.isi.karma.controller.command.worksheet;

import org.json.JSONArray;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.RegenerateWorksheetUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetCleaningUpdate;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.controller.update.WorksheetHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.Workspace;

public class RefreshWorksheetCommand extends WorksheetSelectionCommand {

	private JSONArray updates;
	
	protected RefreshWorksheetCommand(String id, String model, String worksheetId, 
			JSONArray updates, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.updates = updates;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Refresh Worksheet";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer uc = new UpdateContainer();
		SuperSelection sel = this.getSuperSelection(workspace);
		for(int i=0; i<updates.length(); i++) {
			String update = updates.getString(i);
			switch(update) {
				case "headers": uc.add(new WorksheetHeadersUpdate(worksheetId, sel));
								break;
				case "list": uc.add(new WorksheetListUpdate());
								break;
				case "data": uc.add(new WorksheetDataUpdate(worksheetId, sel));
								break;
				case "alignment": 
				{
					uc.add(new AlignmentSVGVisualizationUpdate(worksheetId));
					break;
				}
				case "semanticTypes":
				{
					
					uc.add(new SemanticTypesUpdate(workspace.getWorksheet(worksheetId), worksheetId));
					break;
				}
				case "regenerate":
					uc.add(new RegenerateWorksheetUpdate(worksheetId));
					break;
				case "all":
					uc = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, sel, workspace.getContextId());
					break;
				case "cleaning":
					uc.add(new WorksheetCleaningUpdate(worksheetId, false, sel));
					break;
			}
		}
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
