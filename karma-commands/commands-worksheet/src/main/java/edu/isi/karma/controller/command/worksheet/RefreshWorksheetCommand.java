package edu.isi.karma.controller.command.worksheet;

import org.json.JSONArray;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.RegenerateWorksheetUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetCleaningUpdate;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.controller.update.WorksheetHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;

public class RefreshWorksheetCommand extends WorksheetCommand {

	private JSONArray updates;
	
	protected RefreshWorksheetCommand(String id, String worksheetId, JSONArray updates) {
		super(id, worksheetId);
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
		for(int i=0; i<updates.length(); i++) {
			String update = updates.getString(i);
			switch(update) {
				case "headers": uc.add(new WorksheetHeadersUpdate(worksheetId));
								break;
				case "list": uc.add(new WorksheetListUpdate());
								break;
				case "data": uc.add(new WorksheetDataUpdate(worksheetId));
								break;
				case "alignment": 
				{
					Alignment alignment = AlignmentManager.Instance().getAlignmentOrCreateIt(
							workspace.getId(), worksheetId, workspace.getOntologyManager());
					uc.add(new AlignmentSVGVisualizationUpdate(worksheetId, alignment));
					break;
				}
				case "semanticTypes":
				{
					Alignment alignment = AlignmentManager.Instance().getAlignmentOrCreateIt(
							workspace.getId(), worksheetId, workspace.getOntologyManager());
					uc.add(new SemanticTypesUpdate(workspace.getWorksheet(worksheetId), worksheetId, alignment));
					break;
				}
				case "regenerate":
					uc.add(new RegenerateWorksheetUpdate(worksheetId));
					break;
				case "all":
					uc = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
					break;
				case "cleaning":
					uc.add(new WorksheetCleaningUpdate(worksheetId, false));
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
