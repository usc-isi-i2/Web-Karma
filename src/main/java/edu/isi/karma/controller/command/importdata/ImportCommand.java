/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.controller.command.importdata;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

/**
 *
 * @author mielvandersande
 */
public abstract class ImportCommand extends Command {
    
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
