package edu.isi.karma.controller.command.alignment;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;

public class AddNodeCommand extends Command {
	private String worksheetId;
	private String nodeUri;
	private String nodeLabel;
	private String alignmentId;
	
	private static Logger logger = LoggerFactory.getLogger(AddNodeCommand.class);
	
	// Required for undo
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
		
	protected AddNodeCommand(String id, String worksheetId, String alignmentId, String uri, String label) {
		super(id);
		this.worksheetId = worksheetId;
		this.alignmentId = alignmentId;
		this.nodeUri = uri;
		this.nodeLabel = label;
		
		addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add Node";
	}

	@Override
	public String getDescription() {
		return nodeLabel;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		logCommand(logger, workspace);
	
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
//		OntologyManager ontMgr = workspace.getOntologyManager();

		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>) alignment
				.getGraph().clone();

		try {
			alignment.addForcedInternalNode(new Label(nodeUri));
			alignment.align();
		} catch (JSONException e) {
			logger.error("Error adding Internal Node:" , e);
		}

		return getAlignmentUpdateContainer(alignment, worksheet, workspace);
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);

		// Revert to the old alignment
		AlignmentManager.Instance()
				.addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);

		// Get the alignment update
		return getAlignmentUpdateContainer(oldAlignment, worksheet, workspace);
	}

	private UpdateContainer getAlignmentUpdateContainer(Alignment alignment,
			Worksheet worksheet, Workspace workspace) {
		// Add the visualization update
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, worksheetId, alignment));
		c.add(new AlignmentSVGVisualizationUpdate(worksheetId, alignment));
		return c;
	}

}
