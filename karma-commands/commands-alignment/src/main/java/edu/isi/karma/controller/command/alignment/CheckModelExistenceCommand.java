package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;

import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class CheckModelExistenceCommand extends WorksheetCommand {
	
	protected CheckModelExistenceCommand(String id, String model, String worksheetId) {
		super(id, model, worksheetId);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Check Model Existence";
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
		final boolean modelExist;
		Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
		if (alignment == null || alignment.GetTreeRoot() == null)
			modelExist = false;
		else
			modelExist = true;
		return new UpdateContainer(new AbstractUpdate() {

			@Override
			public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				obj.put("modelExist", modelExist);
				pw.println(obj.toString());
			}
		});
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
