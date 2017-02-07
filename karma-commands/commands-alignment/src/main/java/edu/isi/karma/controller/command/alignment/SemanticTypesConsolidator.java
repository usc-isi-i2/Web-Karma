package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.ICommand;
import edu.isi.karma.controller.history.CommandConsolidator;
import edu.isi.karma.rep.Workspace;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Iterator;
import java.util.List;

/**
 * Created by chengyey on 9/28/15.
 */
public class SemanticTypesConsolidator extends CommandConsolidator {
    @Override
    public Pair<ICommand, Object> consolidateCommand(List<ICommand> commands, ICommand newCommand, Workspace workspace) {
        if (newCommand instanceof SetSemanticTypeCommand || newCommand instanceof SetMetaPropertyCommand) {
            String model = newCommand.getModel();
            Iterator<ICommand> itr = commands.iterator();
            while(itr.hasNext()) {
                ICommand tmp = itr.next();
                if (tmp.getModel().equals(model)
                        && ((Command)tmp).getOutputColumns().equals(((Command)newCommand).getOutputColumns())
                        && (tmp instanceof SetSemanticTypeCommand)) {
                    return new ImmutablePair<>(tmp, (Object)newCommand);
                }
            }
        }
        return null;
    }
}
