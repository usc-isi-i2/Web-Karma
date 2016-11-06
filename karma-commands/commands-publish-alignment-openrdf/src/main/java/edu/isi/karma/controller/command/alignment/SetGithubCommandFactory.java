package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by alse on 11/1/16.
 */
public class SetGithubCommandFactory extends CommandFactory {
    enum Arguments {
        worksheetID,
        repo,
        branch
    }
    @Override
    public Command createCommand(HttpServletRequest request, Workspace workspace) {
        return new SetGithubCommand(getNewId(workspace),
                Command.NEW_MODEL,
                request.getParameter(Arguments.worksheetID.name()),
                request.getParameter(Arguments.repo.name()),
                request.getParameter(Arguments.branch.name()));
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return SetGithubCommand.class;
    }
}
