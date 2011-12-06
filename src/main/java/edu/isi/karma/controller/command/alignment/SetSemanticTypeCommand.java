package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler.ColumnFeature;
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
			String hNodeId, String type, String domain) {
		super(id);
		this.vWorksheetId = vWorksheetId;
		System.out.println("Domain: " + domain);
		System.out.println("type: " + type);
		newType = new SemanticType(hNodeId, type, domain,
				SemanticType.Origin.User, 1.0);
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

		// Prepare the column name for training
		String columnName = currentColumnPath.getLeaf().getColumnName();
		Collection<String> columnNameList = new ArrayList<String>();
		columnNameList.add(columnName);
		Map<ColumnFeature, Collection<String>> columnFeatures = new HashMap<ColumnFeature, Collection<String>>();
		columnFeatures.put(ColumnFeature.ColumnHeaderName, columnNameList);

		// Train the model with the new type
		ArrayList<String> trainingExamples = SemanticTypeUtil
				.getTrainingExamples(worksheet, currentColumnPath);
		if (newType.getDomain().equals(""))
			CRFModelHandler.addOrUpdateLabel(newType.getType(),
					trainingExamples, columnFeatures);
		else
			CRFModelHandler.addOrUpdateLabel(newType.getDomain() + "|"
					+ newType.getType(), trainingExamples, columnFeatures);
		
		System.out.println("Using type:" + newType.getDomain() + "|"
				+ newType.getType());

		// Add the new CRF column model for this column
		ArrayList<String> labels = new ArrayList<String>();
		ArrayList<Double> scores = new ArrayList<Double>();
		CRFModelHandler.predictLabelForExamples(trainingExamples, 4, labels,
				scores, null, columnFeatures);
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
