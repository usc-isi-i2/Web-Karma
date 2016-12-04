package edu.isi.karma.controller.command;
import edu.isi.karma.rep.Workspace;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by alse on 10/4/16.
 */
public class SetKarmaClientNameCommandFactory extends CommandFactory{
    enum Arguments {
        value
    }
    @Override
    public Command createCommand(HttpServletRequest request, Workspace workspace) {
        return new SetKarmaClientNameCommand(getNewId(workspace), Command.NEW_MODEL, request.getParameter(Arguments.value.name()));
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return SetKarmaClientNameCommand.class;
    }
}
