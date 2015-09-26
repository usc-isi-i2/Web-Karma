package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.*;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.CommandHistoryUtil;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import org.json.JSONArray;

import java.util.*;

/**
 * Created by Frank on 9/3/15.
 */
public class ExportOrDeleteHistoryCommand extends WorksheetSelectionCommand {
    private boolean isDelete;
    private final Set<String> commandSet = new HashSet<>();
    private JSONArray historyCommandsBackup;
    private String tripleStoreUrl;
    private String requestUrl;
    private String volatileWorksheetId;
    public ExportOrDeleteHistoryCommand(String id, String model,
                                        String worksheetId, String selectionId,
                                        String commandSet, String tripleStoreUrl,
                                        String requestUrl, boolean isDelete) {
        super(id, model, worksheetId, selectionId);
        this.commandSet.addAll(Arrays.asList(commandSet.split(",")));
        this.tripleStoreUrl = tripleStoreUrl;
        this.requestUrl = requestUrl;
        this.isDelete = isDelete;
        volatileWorksheetId = worksheetId;
        historyCommandsBackup = new JSONArray();
    }

    @Override
    public String getCommandName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return isDelete ? "Delete History" : "Export History";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CommandType getCommandType() {
        if (isDelete) {
            return CommandType.undoable;
        }
        else {
            return CommandType.notInHistory;
        }
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        List<Command> commands = workspace.getCommandHistory().getCommandsFromWorksheetId(volatileWorksheetId);
        for (Command command : commands) {
            historyCommandsBackup.put(workspace.getCommandHistory().getCommandJSON(workspace, command));
        }
        CommandHistoryUtil historyUtil = new CommandHistoryUtil(commands, workspace, volatileWorksheetId);
        if (isDelete) {
            historyUtil.removeCommands(commandSet);
            UpdateContainer updateContainer = historyUtil.replayHistory();
            this.volatileWorksheetId = historyUtil.getWorksheetId();
            return updateContainer;
        }
        historyUtil.retainCommands(commandSet);
        UpdateContainer updateContainer = new UpdateContainer();
        updateContainer.append(historyUtil.replayHistory());
        Command command = new GenerateR2RMLModelCommandFactory().createCommand(model, workspace, volatileWorksheetId, tripleStoreUrl, selectionId);
        ((GenerateR2RMLModelCommand)command).setRESTserverAddress(requestUrl);
        updateContainer.append(command.doIt(workspace));
        return updateContainer;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        CommandHistoryUtil historyUtil = new CommandHistoryUtil(Collections.EMPTY_LIST, workspace, volatileWorksheetId);
        UpdateContainer updateContainer = historyUtil.replayHistory(historyCommandsBackup);
        this.volatileWorksheetId = historyUtil.getWorksheetId();
        return updateContainer;
    }

    @Override
    public String getWorksheetId() {
        return volatileWorksheetId;
    }
}
