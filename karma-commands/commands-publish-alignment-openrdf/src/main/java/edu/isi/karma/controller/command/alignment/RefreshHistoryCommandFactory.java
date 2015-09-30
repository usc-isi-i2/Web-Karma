package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chengyey on 9/25/15.
 */
public class RefreshHistoryCommandFactory extends JSONInputCommandFactory {
    private enum Arguments {
        worksheetId, selectionName
    }
    @Override
    public Command createCommand(HttpServletRequest request, Workspace workspace) {
        return null;
    }

    @Override
    public Command createCommand(JSONArray inputJson, String model, Workspace workspace) throws JSONException, KarmaException {
        String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
        this.normalizeSelectionId(worksheetId, inputJson, workspace);
        String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
        Command command = new RefreshHistoryCommand(getNewId(workspace), model, worksheetId, selectionName);
        command.setInputParameterJson(inputJson.toString());
        return command;
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return RefreshHistoryCommand.class;
    }
}
