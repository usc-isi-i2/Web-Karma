package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.LabeledWeightedEdge;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.view.VWorkspace;

public class GetAlternativeLinksCommand extends Command {
	private final String nodeId;
	private final String alignmentId;

	private enum JsonKeys {
		updateType, edgeLabel, edgeId, edgeSource, Edges
	}

	public String getNodeId() {
		return nodeId;
	}

	protected GetAlternativeLinksCommand(String id, String nodeId,
			String alignmentId) {
		super(id);
		this.nodeId = nodeId;
		this.alignmentId = alignmentId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get Alternative Links";
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Alignment alignment = AlignmentManager.Instance().getAlignment(
				alignmentId);
		final List<LabeledWeightedEdge> edges = alignment.getAlternatives(
				nodeId, true);

		UpdateContainer upd = new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject obj = new JSONObject();
				JSONArray edgesArray = new JSONArray();

				try {
					obj.put(JsonKeys.updateType.name(), "GetAlternativeLinks");
					for (LabeledWeightedEdge edge : edges) {
						JSONObject edgeObj = new JSONObject();
						edgeObj.put(JsonKeys.edgeId.name(), edge.getID());
						edgeObj.put(JsonKeys.edgeLabel.name(), SemanticTypeUtil
								.removeNamespace(edge.getLabel()));
						edgeObj.put(JsonKeys.edgeSource.name(),
								SemanticTypeUtil.removeNamespace(edge
										.getSource().getLabel()));
						edgesArray.put(edgeObj);
					}
					obj.put(JsonKeys.Edges.name(), edgesArray);
					pw.println(obj.toString(4));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
		return upd;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Not required!
		return null;
	}

}
