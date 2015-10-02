package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.Set;

import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkKeyInfo;
import edu.isi.karma.view.VWorkspace;

public class FetchHNodeIdFromAlignmentCommand extends Command{
	private String alignmentId;
	private String columnUri;
	public FetchHNodeIdFromAlignmentCommand(String id, String model, String alignmentId, String columnUri) {
		super(id, model);
		this.alignmentId = alignmentId;
		this.columnUri = columnUri;
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Fetch HNodeId From Alignment";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		String hNodeId = gethNodeId(alignmentId, columnUri);
		final String copy = hNodeId;
		return new UpdateContainer(new AbstractUpdate() {
			
			@Override
			public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				obj.put("HNodeId", copy);
				pw.print(obj.toString());
			}
		});
	}
	
	public static String gethNodeId(String alignmentId, String columnUri) {
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		Set<LabeledLink> tmp = alignment.getCurrentOutgoingLinksToNode(columnUri);
		String hNodeId = null;
		for (LabeledLink link : tmp) {
			if (link.getKeyType() == LinkKeyInfo.UriOfInstance) {
				hNodeId = link.getTarget().getId();
			}
		}
		return hNodeId;
	}
	
	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
