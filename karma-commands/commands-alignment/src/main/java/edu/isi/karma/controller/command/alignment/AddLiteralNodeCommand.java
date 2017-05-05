package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.view.VWorkspace;

/**
 * Add a Literal Node. This node need not be connected to anything in the Steiner Tree.
 * This is used for top-down modeling.
 * Nodes added using this command can be removed using the DeleteNodeCommand
 * @author dipsy
 *
 */
public class AddLiteralNodeCommand extends WorksheetCommand {
	
	private String literalValue;
	private String literalType;
	private String language;
	private boolean isUri;
	private String nodeId;
	
	private String alignmentId;
	
	private static Logger logger = LoggerFactory.getLogger(AddLiteralNodeCommand.class);
	
	// Required for undo
	private Alignment oldAlignment;
	private DirectedWeightedMultigraph<Node, DefaultLink> oldGraph;
		
	protected AddLiteralNodeCommand(String id, String model, String worksheetId, String alignmentId, 
			String nodeId, String literalValue, String literalType, String language, boolean isUri) {
		super(id, model, worksheetId);
		this.alignmentId = alignmentId;
		this.literalValue = literalValue;
		this.literalType = literalType;
		this.language = language;
		this.isUri = isUri;
		this.nodeId = nodeId;

		addTag(CommandTag.Modeling);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add Literal Node";
	}

	@Override
	public String getDescription() {
		return literalValue;
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

		UpdateContainer uc = new UpdateContainer();
		try {
			if(nodeId == null) {
				final LiteralNode ln = alignment.addLiteralNode(literalValue, literalType, language, isUri);
				nodeId = ln.getId();
				
				uc.add(new AbstractUpdate() {
					@Override
					public void generateJson(String prefix, PrintWriter pw,
							VWorkspace vWorkspace) {
						try {
							JSONStringer jsonStr = new JSONStringer();
							
							JSONWriter writer = jsonStr.object();
							writer.key("worksheetId").value(worksheetId);
							writer.key("updateType").value("AddLiteralNodeUpdate");	
							writer.key("hNodeId").value(nodeId);
							writer.key("uri").value(literalValue);
							writer.endObject();
							pw.print(writer.toString());
						} catch (JSONException e) {
							logger.error("Error occured while writing to JSON!", e);
							
						}
						
					}
					
				});
			} else {
				alignment.updateLiteralNode(nodeId, literalValue, literalType, language, isUri);
			}
			
			if(!this.isExecutedInBatch())
				alignment.align();
			
		} catch (Exception e) {
			logger.error("Error adding Literal Node:" , e);
			uc.add(new ErrorUpdate("Error adding Literal Node"));
			return uc;
		}

		uc.append(WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(worksheetId, workspace));
		return uc;
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