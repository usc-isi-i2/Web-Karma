package edu.isi.karma.controller.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.view.VWorkspace;

public class GenerateSemanticTypesCommand extends Command {

	private final String vWorksheetIdArg;

	private static Logger logger = LoggerFactory
			.getLogger(GenerateSemanticTypesCommand.class);

	protected GenerateSemanticTypesCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetIdArg = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return "Generate Semantic Types";
	}

	@Override
	public String getTitle() {
		return "Generate Semantic Types";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		// Prepare the CRF Model
		try {
			SemanticTypeUtil.prepareCRFModelHandler();
		} catch (IOException e) {
			logger.error("Error creating CRF Model file!", e);
		}

		// Populating (or re-populating) the semantic types for the worksheet
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetIdArg).getWorksheet();
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();

		for (HNodePath path : paths) {
			ArrayList<String> trainingExamples = SemanticTypeUtil
					.getTrainingExamples(worksheet, path);

			// Stores the probability scores
			ArrayList<Double> scores = new ArrayList<Double>();
			// Stores the predicted labels
			ArrayList<String> labels = new ArrayList<String>();
			CRFModelHandler.predictLabelForExamples(trainingExamples, 4,
					labels, scores);
			if (labels.size() == 0) {
				continue;
			}

			// Add the scores information to the Full CRF Model of the worksheet
			CRFColumnModel columnModel = new CRFColumnModel(labels, scores);
			worksheet.getCrfModel().addColumnModel(path.getLeaf().getId(),
					columnModel);

			// Create and add the semantic type to the semantic types set of the
			// worksheet
			SemanticType type = new SemanticType(path.getLeaf().getId(),
					labels.get(0), SemanticType.Origin.CRFModel, scores.get(0));
			worksheet.getSemanticTypes().addType(type);
		}

		// Update the container
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetIdArg));
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}
}
