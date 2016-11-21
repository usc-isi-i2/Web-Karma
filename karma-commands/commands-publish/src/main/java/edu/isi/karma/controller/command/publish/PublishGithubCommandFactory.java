package edu.isi.karma.controller.command.publish;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by alse on 11/21/16.
 */

public class PublishGithubCommandFactory  extends CommandFactory {
    enum Arguments {
        worksheetID,
        repo,
        branch,
        username,
        password
    }
    @Override
    public Command createCommand(HttpServletRequest request, Workspace workspace) {
        return new PublishGithubCommand(getNewId(workspace),
                Command.NEW_MODEL,
                request.getParameter(Arguments.worksheetID.name()),
                request.getParameter(Arguments.repo.name()),
                request.getParameter(Arguments.branch.name()),
                request.getParameter(Arguments.username.name()),
                request.getParameter(Arguments.password.name()));
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return PublishGithubCommand.class;
    }
}
