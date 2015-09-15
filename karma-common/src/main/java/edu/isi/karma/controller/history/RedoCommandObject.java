package edu.isi.karma.controller.history;

import edu.isi.karma.controller.command.ICommand;
import org.json.JSONObject;

/**
 * Created by Frank on 9/14/15.
 */
public class RedoCommandObject {
    private ICommand command;
    private JSONObject historyObject;
    RedoCommandObject(ICommand command, JSONObject historyObject) {
        this.command = command;
        this.historyObject = historyObject;
    }

    public ICommand getCommand() {
        return command;
    }

    public JSONObject getHistoryObject() {
        return historyObject;
    }
}
