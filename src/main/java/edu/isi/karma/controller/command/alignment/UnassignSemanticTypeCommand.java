package edu.isi.karma.controller.command.alignment;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorkspace;

public class UnassignSemanticTypeCommand extends Command {

	private final String vWorksheetId;
	private final String hNodeId;
	private String columnName;
	private SemanticType oldSemanticType;

	private static Logger logger = LoggerFactory
			.getLogger(UnassignSemanticTypeCommand.class);

	public UnassignSemanticTypeCommand(String id, String hNodeId,
			String vWorksheetId) {
		super(id);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Unassign Semantic Type";
	}

	@Override
	public String getDescription() {
		return columnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		// Save the old SemanticType object for undo
		SemanticTypes types = worksheet.getSemanticTypes();
		oldSemanticType = types.getSemanticTypeByHNodeId(hNodeId);
		types.unassignColumnSemanticType(hNodeId);

		// Get the column name
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(hNodeId)) {
				columnName = path.getLeaf().getColumnName();
				break;
			}
		}

		// Update the container
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Update the alignment
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);
		try {
			align.update(c, true);
		} catch (Exception e) {
			logger.error("Error occured while unassigning the semantic type!",
					e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while unassigning the semantic type!"));
		}

		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		// Add the old SemanticType object if it is not null
		SemanticTypes types = worksheet.getSemanticTypes();
		if (oldSemanticType != null)
			types.addType(oldSemanticType);

		// Update the container
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Update the alignment
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);
		try {
			align.update(c, true);
		} catch (Exception e) {
			logger.error("Error occured while unassigning the semantic type!",
					e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while unassigning the semantic type!"));
		}
		return c;
	}

}
