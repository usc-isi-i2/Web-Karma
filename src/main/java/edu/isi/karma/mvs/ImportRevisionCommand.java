/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.mvs;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

/**
 *
 * @author mielvandersande
 */
public class ImportRevisionCommand  extends Command{
    
    private Command command;

    public ImportRevisionCommand(Command command, String id) {
        super(id);
        this.command = command;
    }

    @Override
    public String getCommandName() {
        return command.getCommandName();
    }

    @Override
    public String getTitle() {
        return command.getTitle();
    }

    @Override
    public String getDescription() {
        return command.getDescription();
    }

    @Override
    public CommandType getCommandType() {
        return command.getCommandType();
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        return command.doIt(workspace);
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return command.undoIt(workspace);
    }
    
    
}
