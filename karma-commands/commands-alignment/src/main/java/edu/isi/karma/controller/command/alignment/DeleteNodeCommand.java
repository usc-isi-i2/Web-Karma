package edu.isi.karma.controller.command.alignment;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.Node;

/**
 * Delete a Node added by force using the AddNodeCommand
 * These nodes do not get deleted automatically when a link is removed and need to be explicitly deleted
 * using this command
 * @author dipsy
 *
 */
public class DeleteNodeCommand extends WorksheetCommand {
	
	private String nodeId;
	private String nodeLabel;
	private String alignmentId;
	
	private static Logger logger = LoggerFactory.getLogger(AddNodeCommand.class);
	
	// Required for undo
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
		
	protected DeleteNodeCommand(String id, String model, String worksheetId, String alignmentId, String nodeId, String nodeLabel) {
		super(id, model, worksheetId);
		this.alignmentId = alignmentId;
		this.nodeId = nodeId;
		this.nodeLabel = nodeLabel;
		addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Delete Node";
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

		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>) alignment
				.getGraph().clone();

		try {
			alignment.deleteForcedInternalNode(nodeId);
			if(!this.isExecutedInBatch())
				alignment.align();
		} catch (JSONException e) {
			logger.error("Error adding Internal Node:" , e);
		}

		return WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(worksheetId, workspace);
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Revert to the old alignment
		AlignmentManager.Instance()
				.addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);

		// Get the alignment update
		return WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(worksheetId, workspace);
	}

	public String getNodeId() {
		return nodeId;
	}
}
