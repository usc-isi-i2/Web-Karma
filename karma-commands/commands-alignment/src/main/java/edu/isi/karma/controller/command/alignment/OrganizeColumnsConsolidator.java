package edu.isi.karma.controller.command.alignment;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.command.worksheet.OrganizeColumnsCommand;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.rep.Workspace;


public class OrganizeColumnsConsolidator extends CommandConsolidator {
    @Override
    public Pair<ICommand, Object> consolidateCommand(List<ICommand> commands, ICommand newCommand, Workspace workspace) {
        if (newCommand instanceof OrganizeColumnsCommand) {
            String model = newCommand.getModel();
            Iterator<ICommand> itr = commands.iterator();
            while(itr.hasNext()) {
                ICommand tmp = itr.next();
                if (tmp.getModel().equals(model)
                        && ((Command)tmp).getOutputColumns().equals(((Command)newCommand).getOutputColumns())
                        && (tmp instanceof OrganizeColumnsCommand)) {
                    return new ImmutablePair<>(tmp, (Object)newCommand);
                }
            }
        }
        return null;
    }
}
