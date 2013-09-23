/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.mvs;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

/**
 *
 * @author mielvandersande
 */
public abstract class ImportCommand extends Command{

    public ImportCommand(String id) {
        super(id);
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
