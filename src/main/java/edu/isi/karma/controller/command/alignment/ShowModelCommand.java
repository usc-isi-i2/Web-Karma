package edu.isi.karma.controller.command.alignment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.metadata.Tag;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;
import edu.isi.karma.view.VWorkspace;

public class ShowModelCommand extends WorksheetCommand {

	private final String vWorksheetId;
	private String worksheetName;

	private static Logger logger = LoggerFactory
			.getLogger(ShowModelCommand.class);

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

		// Show error update for aligning hierarchical sources
		if (worksheet.getHeaders().hasNestedTables()) {
			return new UpdateContainer(new ErrorUpdate(
					"Karma cannot align hierarchical sources yet!"));
		}

		worksheetName = worksheet.getTitle();

		// Get the Outlier Tag
		Tag outlierTag = vWorkspace.getWorkspace().getTagsContainer()
				.getTag(TagName.Outlier);

		// Generate the semantic types for the worksheet
		SemanticTypeUtil.populateSemanticTypesUsingCRF(worksheet, outlierTag);
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Get the alignment update if any
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);

		try {
			align.update(c, true);
		} catch (Exception e) {
			logger.error("Error occured while generating the model Reason:.", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while generating the model for the source."));
		}

		c.add(new TagsUpdate());
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
