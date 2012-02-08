package edu.isi.karma.controller.command;

import java.util.List;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class AddNewColumnCommand extends WorksheetCommand {
	private final String hNodeId;
	private final String vWorksheetId;

	protected AddNewColumnCommand(String id, String worksheetId, String hNodeId, String vWorksheetId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return AddNewColumnCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add New Column";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = vWorkspace.getWorkspace().getWorksheet(worksheetId);
		HTable headers = worksheet.getHeaders();
		String existingColumnName = headers.getHNode(hNodeId).getColumnName();
		
		worksheet.getHeaders().addNewHNodeAfter(hNodeId, vWorkspace.getRepFactory(), existingColumnName+"_copy", worksheet);
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet, columnPaths, vWorkspace);
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		vw.update(c);
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
