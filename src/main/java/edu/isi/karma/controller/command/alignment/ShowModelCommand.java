package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorkspace;

public class ShowModelCommand extends WorksheetCommand {

	private final String vWorksheetId;
	private String worksheetName;

	protected ShowModelCommand(String id, String worksheetId,
			String vWorksheetId) {
		super(id, worksheetId);
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Show Model";
	}

	@Override
	public String getDescription() {
		return worksheetName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();
		worksheetName = worksheet.getTitle();

		// Generate the semantic types for the worksheet
		boolean semanticTypesChangedOrAdded = SemanticTypeUtil
				.populateSemanticTypesUsingCRF(worksheet);
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Get the alignment update if any
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);
		align.update(c, semanticTypesChangedOrAdded);

		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
