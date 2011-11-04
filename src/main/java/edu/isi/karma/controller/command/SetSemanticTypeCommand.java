package edu.isi.karma.controller.command;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.view.VWorkspace;

public class SetSemanticTypeCommand extends Command {

	private SemanticType oldType;
	private final String vWorksheetId;

	private CRFColumnModel oldColumnModel;

	private final SemanticType newType;

	protected SetSemanticTypeCommand(String id, String vWorksheetId,
			String hNodeId, String type) {
		super(id);
		this.vWorksheetId = vWorksheetId;
		newType = new SemanticType(hNodeId, type, SemanticType.Origin.User, 1.0);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Set Semantic Type";
	}

	@Override
	public String getDescription() {
		return newType.getType();
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		// Save the old SemanticType object and CRF Model for undo
		oldType = worksheet.getSemanticTypes().getSemanticTypeByHNodeId(
				newType.getHNodeId());
		oldColumnModel = worksheet.getCrfModel().getModelByHNodeId(
				newType.getHNodeId());
		
		// Update the SemanticTypes data structure for the worksheet
		worksheet.getSemanticTypes().addType(newType);

		// Find the corresponding hNodePath. Used to find examples for training
		// the CRF Model.
		HNodePath currentColumnPath = null;
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : paths) {
			if (path.getLeaf().getId().equals(newType.getHNodeId())) {
				currentColumnPath = path;
				break;
			}
		}

		// Train the model with the new type
		ArrayList<String> trainingExamples = SemanticTypeUtil
				.getTrainingExamples(worksheet, currentColumnPath);
		CRFModelHandler.addOrUpdateLabel(newType.getType(), trainingExamples);

		// Add the new CRF column model for this column
		ArrayList<String> labels = new ArrayList<String>();
		labels.add(newType.getType());
		ArrayList<Double> scores = new ArrayList<Double>();
		scores.add(1.00);
		CRFColumnModel newModel = new CRFColumnModel(labels, scores);
		worksheet.getCrfModel().addColumnModel(newType.getHNodeId(), newModel);

		return new UpdateContainer(new SemanticTypesUpdate(worksheet,
				vWorksheetId));
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();
		if (oldType == null)
			worksheet.getSemanticTypes().getTypes()
					.remove(newType.getHNodeId());
		else
			worksheet.getSemanticTypes().addType(oldType);

		worksheet.getCrfModel().addColumnModel(newType.getHNodeId(),
				oldColumnModel);

		return new UpdateContainer(new SemanticTypesUpdate(worksheet,
				vWorksheetId));
	}
}
