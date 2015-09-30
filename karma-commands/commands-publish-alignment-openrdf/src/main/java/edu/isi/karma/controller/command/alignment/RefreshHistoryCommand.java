package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.CommandHistoryUtil;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

/**
 * Created by chengyey on 9/25/15.
 */
public class RefreshHistoryCommand extends WorksheetSelectionCommand{
    public RefreshHistoryCommand(String id, String model, String worksheetId, String selectionId) {
        super(id, model, worksheetId, selectionId);
    }

    @Override
    public String getCommandName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Refresh History";
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
        CommandHistory history = workspace.getCommandHistory();
        UpdateContainer updateContainer = new UpdateContainer();
        if (history.isStale(worksheetId)) {
            CommandHistoryUtil util = new CommandHistoryUtil(history.getCommandsFromWorksheetId(worksheetId), workspace, worksheetId);
            updateContainer.append(util.replayHistory());
            updateContainer.add(new HistoryUpdate(history));
        }
        return updateContainer;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return null;
    }
}
