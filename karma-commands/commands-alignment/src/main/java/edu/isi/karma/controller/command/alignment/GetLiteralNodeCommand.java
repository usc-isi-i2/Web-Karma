package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;

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
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.LiteralNode;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.view.VWorkspace;

public class GetLiteralNodeCommand extends WorksheetCommand {

	private String nodeId;
	private String alignmentId;
	private static Logger logger = LoggerFactory.getLogger(AddLiteralNodeCommand.class);
	
		
	protected GetLiteralNodeCommand(String id, String model, String worksheetId, String alignmentId, String nodeId) {
		super(id, model, worksheetId);
		this.alignmentId = alignmentId;
		this.nodeId = nodeId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Literal Node";
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
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		logCommand(logger, workspace);
	
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);

		final UpdateContainer uc = new UpdateContainer();
		Node node = alignment.getNodeById(nodeId);
		if(node instanceof LiteralNode) {
			final LiteralNode lNode = (LiteralNode)node;
			uc.add(new AbstractUpdate() {

				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					try {
						JSONStringer jsonStr = new JSONStringer();
						
						JSONWriter writer = jsonStr.object();
						writer.key("worksheetId").value(worksheetId);
						writer.key("updateType").value("LiteralNodeUpdate");	
						writer.key("node");
						
						writer.object();
						writer.key("value").value(lNode.getValue());	
						String type = lNode.getDatatype().getLocalName();
						if(type != null && type.length() > 0)
							type = lNode.getDatatype().getDisplayName();
						else
							type = "";
						writer.key("type").value(type);
						writer.key("language").value(lNode.getLanguage());
						writer.key("isUri").value(lNode.isUri());
						writer.endObject();
						
						writer.endObject();
	
						pw.print(writer.toString());
					} catch (JSONException e) {
						logger.error("Error occured while writing to JSON!", e);
						uc.add(new ErrorUpdate("Error occured while writing to JSON"));
					}
				}
				
			});
		} else {
			uc.add(new ErrorUpdate("Node does not exist in the Alignment"));
		}

		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}
}
