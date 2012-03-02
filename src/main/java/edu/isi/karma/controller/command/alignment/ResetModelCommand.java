package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.TagsUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
import edu.isi.karma.view.VWorkspace;
import edu.isi.mediator.gav.util.MediatorUtil;

public class ResetModelCommand extends Command {

	private final String vWorksheetId;
	private SemanticTypes oldTypes;
	private Alignment oldAlignment;
	private String alignmentId;
	private String oldCRFModel;
	
	private static Logger logger = LoggerFactory
			.getLogger(ResetModelCommand.class);

	public ResetModelCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Reset Model";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		//save CRF model for undo
		try{
			oldCRFModel=MediatorUtil.getFileAsString("./CRF_Model.txt");
		} catch (Exception e) {
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
		//reset CRF model
		CRFModelHandler.removeAllLabels();
		
		// Save the old SemanticType object for undo
		SemanticTypes types = worksheet.getSemanticTypes();
		oldTypes = types;
		worksheet.clearSemanticTypes();
		//System.out.println("OLD TYPES=" + oldTypes.getTypes());
		
		//save old alignment for undo operation
		alignmentId = vWorkspace.getWorkspace().getId() + ":" + vWorksheetId + "AL";
		oldAlignment = AlignmentManager.Instance().getAlignment(alignmentId);
		
		// Remove the nodes (if any) from the outlier tag
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0, worksheet.getDataTable().getNumRows());
		Set<String> nodeIds = new HashSet<String>();
		for(Row r:rows){
			//for each node in the row
			//logger.debug("Process ROW="+i++);
			for (Node n : r.getNodes()) {
				nodeIds.add(n.getId());
			}
		}
		vWorkspace.getWorkspace().getTagsContainer().getTag(TagName.Outlier)
				.removeNodeIds(nodeIds);

		// Update the container
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Update the alignment
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);
		try {
			align.update(c, true);
		} catch (Exception e) {
			logger.error("Error occured while resetting model!",
					e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while resetting model!"));
		}
		c.add(new TagsUpdate());
		
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory()
		.getVWorksheet(vWorksheetId).getWorksheet();

		//reset CRF model
		try{
			MediatorUtil.saveStringToFile(oldCRFModel, "./CRF_Model.txt");
			CRFModelHandler.readModelFromFile("./CRF_Model.txt");
		} catch (Exception e) {
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
		//reset old semantic types
		worksheet.setSemanticTypes(oldTypes);

		//set the old alignment
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, oldAlignment);
		
		// Update the container
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Update the alignment
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);
		try {
			align.update(c, true);
		} catch (Exception e) {
			logger.error("Error occured while undoing alignment!",
					e);
			return new UpdateContainer(new ErrorUpdate(
			"Error occured while undoing alignment!"));
		}
		return c;

	}

}
