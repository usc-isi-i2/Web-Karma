package edu.isi.karma.controller.command.alignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler.ColumnFeature;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.rep.semantictypes.SemanticTypes;
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
		worksheetName = worksheet.getTitle();

		// Generate the semantic types for the worksheet
		boolean semanticTypesChangedOrAdded = populateSemanticTypes(worksheet);
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId));

		// Get the alignment update if any
		AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
				vWorksheetId);
		align.update(c, semanticTypesChangedOrAdded);

		return c;
	}

	private boolean populateSemanticTypes(Worksheet worksheet) {
		boolean semanticTypesChangedOrAdded = false;
		// Prepare the CRF Model
		// try {
		// SemanticTypeUtil.prepareCRFModelHandler();
		// } catch (IOException e) {
		// logger.error("Error creating CRF Model file!", e);
		// }

		SemanticTypes types = worksheet.getSemanticTypes();

		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();

		for (HNodePath path : paths) {
			ArrayList<String> trainingExamples = SemanticTypeUtil
					.getTrainingExamples(worksheet, path);

			Map<ColumnFeature, Collection<String>> columnFeatures = new HashMap<ColumnFeature, Collection<String>>();

			// Prepare the column name feature
			String columnName = path.getLeaf().getColumnName();
			Collection<String> columnNameList = new ArrayList<String>();
			columnNameList.add(columnName);
			columnFeatures.put(ColumnFeature.ColumnHeaderName, columnNameList);

			// // Prepare the table name feature
			// String tableName = worksheetName;
			// Collection<String> tableNameList = new ArrayList<String>();
			// tableNameList.add(tableName);
			// columnFeatures.put(ColumnFeature.TableName, tableNameList);

			// Stores the probability scores
			ArrayList<Double> scores = new ArrayList<Double>();
			// Stores the predicted labels
			ArrayList<String> labels = new ArrayList<String>();
			boolean predictResult = CRFModelHandler.predictLabelForExamples(
					trainingExamples, 4, labels, scores, null, columnFeatures);
			if (!predictResult) {
				logger.error("Error occured while predicting label.");
			}
			if (labels.size() == 0) {
				continue;
			}

			// Add the scores information to the Full CRF Model of the worksheet
			CRFColumnModel columnModel = new CRFColumnModel(labels, scores);
			worksheet.getCrfModel().addColumnModel(path.getLeaf().getId(),
					columnModel);

			// Create and add the semantic type to the semantic types set of the
			// worksheet
			String topLabel = labels.get(0);
			String domain = "";
			String type = topLabel;
			// Check if it contains domain information
			if (topLabel.contains("|")) {
				domain = topLabel.split("\\|")[0];
				type = topLabel.split("\\|")[1];
			}

			SemanticType semtype = new SemanticType(path.getLeaf().getId(),
					type, domain, SemanticType.Origin.CRFModel, scores.get(0), false);

			// Check if the user already provided a semantic type manually
			SemanticType existingType = types.getSemanticTypeByHNodeId(path
					.getLeaf().getId());
			if (existingType == null) {
				if (semtype.getConfidenceLevel() != SemanticType.ConfidenceLevel.Low) {
					worksheet.getSemanticTypes().addType(semtype);
					semanticTypesChangedOrAdded = true;
				}
			} else {
				if (existingType.getOrigin() != SemanticType.Origin.User) {
					worksheet.getSemanticTypes().addType(semtype);

					// Check if the new semantic type is different from the
					// older one
					if (!existingType.getType().equals(semtype.getType())
							|| !existingType.getDomain().equals(
									semtype.getDomain()))
						semanticTypesChangedOrAdded = true;
				}
			}
		}
		return semanticTypesChangedOrAdded;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
