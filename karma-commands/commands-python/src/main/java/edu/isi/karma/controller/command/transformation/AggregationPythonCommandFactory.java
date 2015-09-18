package edu.isi.karma.controller.command.transformation;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONArray;
import org.json.JSONException;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by chengyey on 9/15/15.
 */
public class AggregationPythonCommandFactory extends JSONInputCommandFactory {
    private enum Arguments {
        worksheetId, hNodeId,
        selectionName, pythonCode,
        constructor, newColumnName
    }
    @Override
    public Command createCommand(HttpServletRequest request, Workspace workspace) {
        return null;
    }

    @Override
    public Command createCommand(JSONArray inputJson, String model, Workspace workspace) throws JSONException, KarmaException {
        String hNodeId = CommandInputJSONUtil.getStringValue(Arguments.hNodeId.name(), inputJson);
        String worksheetId = CommandInputJSONUtil.getStringValue(Arguments.worksheetId.name(), inputJson);
        String pythonCode = CommandInputJSONUtil.getStringValue(Arguments.pythonCode.name(), inputJson);
        String constructor = CommandInputJSONUtil.getStringValue(Arguments.constructor.name(), inputJson);
        String newColumnName = CommandInputJSONUtil.getStringValue(Arguments.newColumnName.name(), inputJson);
        this.normalizeSelectionId(worksheetId, inputJson, workspace);
        String selectionName = CommandInputJSONUtil.getStringValue(Arguments.selectionName.name(), inputJson);
        AggregationPythonCommand command = new AggregationPythonCommand(getNewId(workspace), model,
                worksheetId, selectionName,
                hNodeId, pythonCode,
                constructor, newColumnName);
        command.setInputParameterJson(inputJson.toString());
        return command;
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return AggregationPythonCommand.class;
    }
}
