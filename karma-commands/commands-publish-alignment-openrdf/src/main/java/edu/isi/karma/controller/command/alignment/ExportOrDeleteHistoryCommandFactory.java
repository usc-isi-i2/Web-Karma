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
 * Created by Frank on 9/3/15.
 */
public class ExportOrDeleteHistoryCommandFactory extends JSONInputCommandFactory {
    private enum Arguments {
        worksheetId, selectionName,
        commandList, isDelete
    }
    @Override
    public Command createCommand(HttpServletRequest request, Workspace workspace) {
        return null;
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return ExportOrDeleteHistoryCommand.class;
    }

    @Override
    public Command createCommand(JSONArray inputJson, String model, Workspace workspace) throws JSONException, KarmaException {
        String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
        String commandList = CommandInputJSONUtil.getStringValue(Arguments.commandList.name(), inputJson);
        boolean isDelete = Boolean.parseBoolean(CommandInputJSONUtil.getStringValue(Arguments.isDelete.name(), inputJson));
        this.normalizeSelectionId(worksheetId, inputJson, workspace);
        String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
        Command command = new ExportOrDeleteHistoryCommand(getNewId(workspace), model, worksheetId, selectionName, commandList, isDelete);
        command.setInputParameterJson(inputJson.toString());
        return command;
    }
}
