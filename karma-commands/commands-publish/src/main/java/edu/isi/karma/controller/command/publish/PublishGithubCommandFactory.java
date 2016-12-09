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
        worksheetId,
        repo,
        auth
    }
    @Override
    public Command createCommand(HttpServletRequest request, Workspace workspace) {
        return new PublishGithubCommand(getNewId(workspace),
                Command.NEW_MODEL,
                request.getParameter(Arguments.worksheetId.name()),
                request.getParameter(Arguments.repo.name()),
                request.getParameter(Arguments.auth.name()));
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return PublishGithubCommand.class;
    }
}
