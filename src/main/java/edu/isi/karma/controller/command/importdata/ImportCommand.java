/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.controller.command.importdata;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

/**
 * This abstract class in an interface to all Commands that Import data
 * 
 * 
 * @author mielvandersande
 */
public abstract class ImportCommand extends Command {

    // Id of the revised worksheet, or null if no revision is present
    private String revisionId;

    public ImportCommand(String id) {
        super(id);
        this.revisionId = null;
    }

    public ImportCommand(String id, String revisionId) {
        super(id);
        this.revisionId = revisionId;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public boolean hasRevisionId() {
        return revisionId != null;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return null;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.notUndoable;
    }
}
