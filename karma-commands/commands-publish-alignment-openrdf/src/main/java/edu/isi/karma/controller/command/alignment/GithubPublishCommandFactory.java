package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by alse on 11/1/16.
 */
public class GithubPublishCommandFactory extends CommandFactory {
    enum Arguments {
        modelName
    }
    @Override
    public Command createCommand(HttpServletRequest request, Workspace workspace) {
        return new GithubPublishCommand(getNewId(workspace), Command.NEW_MODEL, request.getParameter(Arguments.modelName.name()));
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return GithubPublishCommand.class;
    }
}
