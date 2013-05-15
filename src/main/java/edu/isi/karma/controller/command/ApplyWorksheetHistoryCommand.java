package edu.isi.karma.controller.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class ApplyWorksheetHistoryCommand extends Command {
	private final File historyFile;
	private final String vWorksheetId;
	
	private static Logger logger = LoggerFactory.getLogger(ApplyWorksheetHistoryCommand.class);
	
	protected ApplyWorksheetHistoryCommand(String id, File uploadedFile, String vWorksheetId) {
		super(id);
		this.historyFile = uploadedFile;
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return ApplyWorksheetHistoryCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Apply Command History";
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		WorksheetCommandHistoryReader histReader = new WorksheetCommandHistoryReader(vWorksheetId, vWorkspace);
		try {
			histReader.readAndExecuteAllCommandsFromFile(historyFile);
		} catch (Exception e) {
			String msg = "Error occured while applying history!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		
		// Add worksheet updates that could have resulted out of the transformation commands
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		UpdateContainer c =  new UpdateContainer();
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet,
				worksheet.getHeaders().getAllPaths(), vWorkspace);
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		vw.update(c);
		
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				vWorkspace.getWorkspace().getId(), vWorksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			alignment = new Alignment(vWorkspace.getWorkspace().getOntologyManager());
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}

		// Compute the semantic type suggestions
		SemanticTypeUtil.computeSemanticTypesSuggestion(worksheet, vWorkspace.getWorkspace().getCrfModelHandler(), 
				vWorkspace.getWorkspace().getOntologyManager(), alignment);
		// Add the alignment update
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, alignment));
		c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vWorkspace.getViewFactory().getVWorksheet(vWorksheetId), alignment));
		
		c.add(new InfoUpdate("History successfully applied!"));
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
