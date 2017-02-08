package edu.isi.karma.controller.command.alignment;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.TrivialErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LinkIdFactory;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommand.LinkJsonKeys;

public class AddLinkCommand extends WorksheetCommand {

	private String displayLabel;
	private String alignmentId;
	private JSONObject edge;
	
	// Required for undo
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
		
	protected AddLinkCommand(String id, String model, String worksheetId, String alignmentId, JSONObject edge) {
		super(id, model, worksheetId);
		this.alignmentId = alignmentId;
		this.edge = edge;
		
		addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return AddLinkCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add Link";
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
		UpdateContainer uc =  this.addNewLinks(alignment, ontMgr, edge);
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

	private UpdateContainer addNewLinks(Alignment alignment, OntologyManager ontMgr, JSONObject newEdge)
			throws JSONException {
		
		UpdateContainer uc = new UpdateContainer();			
		String edgeUri = newEdge.getString(LinkJsonKeys.edgeId.name());
		
		String sourceUri = newEdge.has(LinkJsonKeys.edgeSourceUri.name()) ? newEdge.getString(LinkJsonKeys.edgeSourceUri.name()) : null;
		String sourceId = newEdge.has(LinkJsonKeys.edgeSourceId.name()) ? newEdge.getString(LinkJsonKeys.edgeSourceId.name()) : null;
		
		String targetId = newEdge.has(LinkJsonKeys.edgeTargetId.name()) ? newEdge.getString(LinkJsonKeys.edgeTargetId.name()) : null;
		String targetUri = newEdge.has(LinkJsonKeys.edgeTargetUri.name()) ? newEdge.getString(LinkJsonKeys.edgeTargetUri.name()) : null;
		
		if(edgeUri != null && sourceId != null && targetId != null) {
			String linkId = LinkIdFactory.getLinkId(edgeUri, sourceId, targetId);
			
			if (alignment.getLinkById(linkId) != null) {
				//Link already exists
				alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
				return uc;
			}
		}	
			
		Node sourceNode = null;
		Label sourceLabel = null;
		if(sourceUri != null)
			sourceLabel = ontMgr.getUriLabel(sourceUri);
		if(sourceId != null) {
			if(sourceId.endsWith(" (add)"))
				sourceId = sourceId.substring(0, sourceId.length()-5).trim();
			sourceNode = alignment.getNodeById(sourceId);
			
			if(sourceNode == null) {
				sourceNode = alignment.addInternalNode(new InternalNode(sourceId, sourceLabel));
			}
		} else if(sourceUri != null){
			sourceNode = alignment.addInternalNode(sourceLabel);
			sourceId = sourceNode.getId();
		}
		
		
		
		Label targetLabel = null;
		if(targetUri != null)
			targetLabel = ontMgr.getUriLabel(targetUri);
		Node targetNode = null;
		if(targetId != null) {
			if(targetId.endsWith(" (add)"))
				targetId = targetId.substring(0, targetId.length()-5).trim();
			targetNode = alignment.getNodeById(targetId);
			
			if(targetNode == null) {
				targetNode = alignment.addInternalNode(new InternalNode(targetId, targetLabel));
			}
		} else if(targetUri != null) {
			targetNode = alignment.addInternalNode(targetLabel);
			targetId = targetNode.getId();
		}
		
		if(sourceNode == null) {
			uc.add(new TrivialErrorUpdate("Could not add links from " + sourceId));
			return uc;
		}
		if(targetNode == null) {
			uc.add(new TrivialErrorUpdate("Could not add links to " + targetId));
			return uc;
		}
		String linkId = LinkIdFactory.getLinkId(edgeUri, sourceId, targetId);
		LabeledLink newLink = alignment.getLinkById(linkId);
		if (newLink != null) {
			alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);
		} else {
			Label linkLabel = ontMgr.getUriLabel(edgeUri);
			newLink = alignment.addObjectPropertyLink(sourceNode,
					targetNode, linkLabel);
			linkId = newLink.getId();
		}
			
		alignment.changeLinkStatus(linkId, LinkStatus.ForcedByUser);


		this.displayLabel = newLink.getLabel().getDisplayName();
			
		return uc;
	}
}
