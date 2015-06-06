package edu.isi.karma.controller.command.alignment;

import org.json.JSONArray;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.Workspace;

public class AugmentDataDispachCommand extends WorksheetSelectionCommand {
	private String predicateIncoming, predicateOutgoing;
	private AugmentDataCommand incoming;
	private AugmentDataCommand outgoing;
	public AugmentDataDispachCommand(String id, String model, String dataRepoUrl, String worksheetId, 
			String columnUri, String predicateIncoming, String otherClassIncoming, 
			String predicateOutgoing, String otherClassOutgoing, 
			String hNodeId, String sameAsPredicate, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.predicateIncoming = predicateIncoming;
		this.predicateOutgoing = predicateOutgoing;
		incoming = new AugmentDataCommand(id, model, dataRepoUrl, worksheetId, columnUri, predicateIncoming, otherClassIncoming, hNodeId, true, sameAsPredicate, selectionId);
		outgoing = new AugmentDataCommand(id, model, dataRepoUrl, worksheetId, columnUri, predicateOutgoing, otherClassOutgoing, hNodeId, false, sameAsPredicate, selectionId);
		addTag(CommandTag.Transformation);
	}
	
	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Augment Data";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		JSONArray predicatesarray = new JSONArray(predicateIncoming);
		StringBuilder builder = new StringBuilder();
		boolean flag = false;
		if (predicatesarray.length() > 0) {
			for(int i = 0; i < predicatesarray.length() - 1; i++) 
				builder.append(predicatesarray.getJSONObject(i).getString("predicate")).append(" ");
			builder.append(predicatesarray.getJSONObject(predicatesarray.length() - 1).getString("predicate"));
			flag = true;
		}
		predicatesarray = new JSONArray(predicateOutgoing);
		if (predicatesarray.length() > 0 && flag)
			builder.append(" ");
		if (predicatesarray.length() > 0) {
			for(int i = 0; i < predicatesarray.length() - 1; i++) 
				builder.append(predicatesarray.getJSONObject(i).getString("predicate")).append(" ");
			builder.append(predicatesarray.getJSONObject(predicatesarray.length() - 1).getString("predicate"));
		}
		return builder.toString();
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		// TODO Auto-generated method stub
		UpdateContainer c =  new UpdateContainer();
		JSONArray predicatesincomingarray = new JSONArray(predicateIncoming);
		JSONArray predicatesoutgoingarray = new JSONArray(predicateOutgoing);
		if (predicatesincomingarray.length() > 0)
			incoming.doIt(workspace);
		if (predicatesoutgoingarray.length() > 0)
			outgoing.doIt(workspace);
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace), workspace.getContextId()));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		UpdateContainer c =  new UpdateContainer();
		JSONArray predicatesincomingarray = new JSONArray(predicateIncoming);
		JSONArray predicatesoutgoingarray = new JSONArray(predicateOutgoing);
		if (predicatesincomingarray.length() > 0)
			incoming.undoIt(workspace);
		if (predicatesoutgoingarray.length() > 0)
			outgoing.undoIt(workspace);
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace), workspace.getContextId()));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

}
