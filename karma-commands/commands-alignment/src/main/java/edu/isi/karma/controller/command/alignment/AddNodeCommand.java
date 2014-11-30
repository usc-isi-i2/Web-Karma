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
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.Node;

/**
 * Force Add a Node. This node need not be connected to anything in the Steiner Tree.
 * This is used for top-down modeling.
 * Nodes added using this command can be removed using the DeleteNodeCommand
 * @author dipsy
 *
 */
public class AddNodeCommand extends WorksheetCommand {
	
	private String nodeUri;
	private String nodeLabel;
	private String alignmentId;
	
	private static Logger logger = LoggerFactory.getLogger(AddNodeCommand.class);
	
	// Required for undo
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
		
	protected AddNodeCommand(String id, String worksheetId, String alignmentId, String uri, String label) {
		super(id, worksheetId);
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

		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>) alignment
				.getGraph().clone();

		try {
			alignment.addForcedInternalNode(new Label(nodeUri));
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

	

}
