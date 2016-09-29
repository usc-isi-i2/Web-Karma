package edu.isi.karma.controller.command;

import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by alse on 9/29/16.
 */
public class UpdateOnlineSemanticTypingConfigurationCommandFactory extends CommandFactory{
    enum Arguments {
        isSemanticLabelingOnline
    }
    @Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return new UpdateOnlineSemanticTypingConfigurationCommand(getNewId(workspace),
				Command.NEW_MODEL, Boolean.valueOf(request.getParameter(Arguments.isSemanticLabelingOnline.name())));
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return UpdateOnlineSemanticTypingConfigurationCommand.class;
    }
}
