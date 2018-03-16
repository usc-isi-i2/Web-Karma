package edu.isi.karma.controller.command.alignment;

import java.util.Set;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommand.LinkJsonKeys;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;

public class DeleteLinkCommand extends WorksheetCommand {

	
	private String displayLabel;
	private String alignmentId;
	private JSONObject edge;
	
	// Required for undo
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
		
	protected DeleteLinkCommand(String id, String model, String worksheetId, String alignmentId, JSONObject edge) {
		super(id, model, worksheetId);
		this.alignmentId = alignmentId;
		this.edge = edge;
		addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return DeleteLinkCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Delete Link" ;
	}

	@Override
	public String getDescription() {
		return this.displayLabel;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		OntologyManager ontMgr = workspace.getOntologyManager();

		// Save the original alignment for undo
		oldAlignment = alignment.getAlignmentClone();
		oldGraph = (DirectedWeightedMultigraph<Node, DefaultLink>) alignment
				.getGraph().clone();
		UpdateContainer uc = this.deleteLink(alignment, ontMgr, edge);
		if(!this.isExecutedInBatch())
			alignment.align();
		uc.append(this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Revert to the old alignment
		AlignmentManager.Instance()
				.addAlignmentToMap(alignmentId, oldAlignment);
		oldAlignment.setGraph(oldGraph);

		return this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace);
	}

	private UpdateContainer deleteLink(Alignment alignment, OntologyManager ontMgr, JSONObject edge)
			throws JSONException {
		
		UpdateContainer uc = new UpdateContainer();
		String targetId = edge.getString(LinkJsonKeys.edgeTargetId.name());
		String edgeUri = edge.getString(LinkJsonKeys.edgeId.name());
		String linkId = LinkIdFactory.getLinkId(
				edgeUri,
				edge.getString(LinkJsonKeys.edgeSourceId.name()),
				targetId);
		
		// Add info to description string
		LabeledLink delLink = alignment.getLinkById(linkId);
		if(delLink != null) {
			this.displayLabel = delLink.getLabel().getDisplayName();
		} else {
			this.displayLabel = edgeUri;
		}
			
		alignment.removeLink(linkId);
		
		return uc;
	}

}
