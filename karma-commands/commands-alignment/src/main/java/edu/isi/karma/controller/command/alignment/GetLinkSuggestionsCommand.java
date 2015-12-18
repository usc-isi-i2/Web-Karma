package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.NodeType;
import edu.isi.karma.view.VWorkspace;

public class GetLinkSuggestionsCommand extends WorksheetSelectionCommand {
	private final String hNodeId;
	private static Logger logger = LoggerFactory.getLogger(GetLinkSuggestionsCommand.class.getSimpleName());
	
	private enum Arguments {
		links, source, target, uri, label, id
	}
	
	public GetLinkSuggestionsCommand(String id, String model,
			String worksheetId, String hNodeId, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(final Workspace workspace) throws CommandException {
		logger.info("Get Link Suggestions: " + worksheetId + "," + hNodeId);
		UpdateContainer uc = new UpdateContainer();
		final Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), worksheetId);
		
		final List<LabeledLink> finalProperties = alignment.suggestLinks(hNodeId);
		uc.add(new AbstractUpdate() {

			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject result = new JSONObject();
				JSONArray links = new JSONArray();
				
				for(LabeledLink property : finalProperties) {
					if(property.getTarget().getType() == NodeType.ColumnNode)
						continue;
					
					JSONObject link = new JSONObject();
					
					link.put(Arguments.uri.name(), property.getUri());
					link.put(Arguments.label.name(), property.getLabel().getDisplayName());
					
					JSONObject sourceObject = new JSONObject();
					sourceObject.put(Arguments.uri.name(), property.getSource().getUri());
					sourceObject.put(Arguments.id.name(), property.getSource().getId());
					sourceObject.put(Arguments.label.name(), property.getSource().getLabel().getDisplayName());
					
					JSONObject targetObject = new JSONObject();
					targetObject.put(Arguments.uri.name(), property.getTarget().getUri());
					targetObject.put(Arguments.id.name(), property.getTarget().getId());
					targetObject.put(Arguments.label.name(), property.getTarget().getLabel().getDisplayName());
					
					link.put(Arguments.source.name(), sourceObject);
					link.put(Arguments.target.name(), targetObject);
					links.put(link);
					logger.info("Suggestion:" + link.toString());
				}
				result.put(Arguments.links.name(), links);
				
				pw.println(result.toString());
				
			}
			
		});
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
