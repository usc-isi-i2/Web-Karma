package edu.isi.karma.controller.command.alignment;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommand.LinkJsonKeys;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommandFactory.Arguments;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;

public class AddLinkConsolidator extends CommandConsolidator {

	@Override
	public Pair<ICommand, Object> consolidateCommand(List<ICommand> commands,
			ICommand newCommand, Workspace workspace) {
		if (newCommand instanceof AddLinkCommand) {
            String model = newCommand.getModel();
            JSONArray inputParams = new JSONArray(newCommand.getInputParameterJson());
            JSONObject newEdge = HistoryJsonUtil.getJSONObjectWithName(
    				Arguments.edge.name(), inputParams).getJSONObject("value");
            String edgeSourceId = newEdge.getString(LinkJsonKeys.edgeSourceId.name());
            String edgeTargetId = newEdge.getString(LinkJsonKeys.edgeTargetId.name());
            String edgePropId = newEdge.getString(LinkJsonKeys.edgeId.name());
            String edgeId = edgeSourceId + "--" + edgePropId + "--" + edgeTargetId;
            Iterator<ICommand> itr = commands.iterator();
            while(itr.hasNext()) {
                ICommand tmp = itr.next();
                if (tmp.getModel().equals(model)
                        && (tmp instanceof AddLinkCommand)) {
                	JSONArray tmpParams = new JSONArray(tmp.getInputParameterJson());
                	JSONObject tmpEdge = HistoryJsonUtil.getJSONObjectWithName(
            				Arguments.edge.name(), tmpParams).getJSONObject("value");
                    String tmpSourceId = tmpEdge.getString(LinkJsonKeys.edgeSourceId.name());
                    String tmpTargetId = tmpEdge.getString(LinkJsonKeys.edgeTargetId.name());
                    String tmpPropId = tmpEdge.getString(LinkJsonKeys.edgeId.name());
                    String tmpEdgeId = tmpSourceId + "--" + tmpPropId + "--" + tmpTargetId;
                    if(edgeId.equals(tmpEdgeId))
                    	return new ImmutablePair<>(tmp, (Object)newCommand);
                }
            }
        }
        return null;
	}

}
