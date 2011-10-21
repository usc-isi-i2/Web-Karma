package edu.isi.karma.controller.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.semantictypes.CRFColumnModel;
import edu.isi.karma.modeling.semantictypes.crfmodelhandler.CRFModelHandler;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.semantictypes.SemanticType;
import edu.isi.karma.view.VWorkspace;

public class GenerateSemanticTypesCommand extends Command {

	private final String vWorksheetIdArg;

	// TODO Make this parameter configurable through web.xml
	private final static int TRAINING_EXAMPLE_MAX_COUNT = 100;

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
			prepareCRFModelHandler();
		} catch (IOException e) {
			logger.error("Error creating CRF Model file!", e);
		}

		// Populating (or re-populating) the semantic types for the worksheet
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetIdArg).getWorksheet();
		Table table = worksheet.getDataTable();
		List<HNodePath> paths = worksheet.getHeaders().getAllPaths();

		for (HNodePath path : paths) {
			Collection<Node> nodes = new ArrayList<Node>();
			table.collectNodes(path, nodes);

			ArrayList<String> trainingExamples = prepareTrainingExamples(nodes);

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

			// System.out.println("Labels: " + labels.toString());
			// System.out.println("Scores:" + scores.toString());
			// System.out.println("Column Name: " +
			// path.getLeaf().getColumnName());
			// System.out.println("HNodeID: " + path.getLeaf().getId());
		}

		// Update the container
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetIdArg));
		return c;
	}

	private ArrayList<String> prepareTrainingExamples(Collection<Node> nodes) {
		ArrayList<String> nodeValues = new ArrayList<String>();
		for (Node n : nodes) {
			String nodeValue = n.getValue().asString().trim();
			if(nodeValue != "")
				nodeValues.add(nodeValue);
		}

		// Get rid of duplicate strings by creating a set over the list
		HashSet<String> valueSet = new HashSet<String>(nodeValues);
		nodeValues.clear();
		nodeValues.addAll(valueSet);

		// Shuffling the values so that we get randomly chosen values to train
		Collections.shuffle(nodeValues);

		if (nodeValues.size() > TRAINING_EXAMPLE_MAX_COUNT){
			ArrayList<String> subset = new ArrayList<String>();
			//	SubList method of ArrayList causes ClassCast exception
			for(int i=0; i< TRAINING_EXAMPLE_MAX_COUNT; i++)
				subset.add(nodeValues.get(i));
			return subset;
		}

		return nodeValues;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

	private void prepareCRFModelHandler() throws IOException {
		File file = new File("CRF_Model.txt");
		if (!file.exists()) {
			file.createNewFile();
		}
		boolean result = CRFModelHandler.readModelFromFile("CRF_Model.txt");
		if (!result)
			logger.error("Error occured while reading CRF Model!");
	}

}
