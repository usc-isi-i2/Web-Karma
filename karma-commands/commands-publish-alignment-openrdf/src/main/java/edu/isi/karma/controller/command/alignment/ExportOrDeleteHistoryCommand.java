package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.CommandHistoryUtil;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Frank on 9/3/15.
 */
public class ExportOrDeleteHistoryCommand extends WorksheetSelectionCommand {
    private boolean isDelete;
    private final Set<String> commandSet = new HashSet<>();
    private CommandHistory history;
    private String tripleStoreUrl;
    private String requestUrl;
    public ExportOrDeleteHistoryCommand(String id, String model,
                                        String worksheetId, String selectionId,
                                        String commandSet, String tripleStoreUrl,
                                        String requestUrl, boolean isDelete) {
        super(id, model, worksheetId, selectionId);
        this.commandSet.addAll(Arrays.asList(commandSet.split(",")));
        this.tripleStoreUrl = tripleStoreUrl;
        this.requestUrl = requestUrl;
        this.isDelete = isDelete;
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
            return CommandType.notUndoable;
        }
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        history = workspace.getCommandHistory().clone();
        CommandHistoryUtil historyUtil = new CommandHistoryUtil(history.getCommandsFromWorksheetId(worksheetId), workspace, worksheetId);
        if (isDelete) {
            historyUtil.removeCommands(commandSet);
            return historyUtil.replayHistory();
        }
        historyUtil.retainCommands(commandSet);
        UpdateContainer updateContainer = new UpdateContainer();
        updateContainer.append(historyUtil.replayHistory());
        Command command = new GenerateR2RMLModelCommandFactory().createCommand(model, workspace, worksheetId, tripleStoreUrl, selectionId);
        ((GenerateR2RMLModelCommand)command).setRESTserverAddress(requestUrl);
        updateContainer.append(command.doIt(workspace));
        return updateContainer;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        CommandHistoryUtil historyUtil = new CommandHistoryUtil(history.getCommandsFromWorksheetId(worksheetId), workspace, worksheetId);
        return  historyUtil.replayHistory();
    }
}
