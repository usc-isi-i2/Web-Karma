package edu.isi.karma.controller.command;

import edu.isi.karma.rep.Workspace;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by alse on 9/29/16.
 * Request @param property: is the name of property in modeling.properties eg. online.semantic.typing, train.on.apply.history
 * Request @param value: is the value of the property to be set.
 */
public class UpdateModelingConfigurationCommandFactory extends CommandFactory{
    enum Arguments {
        property,
        value
    }
    @Override
	public Command createCommand(HttpServletRequest request, Workspace workspace) {
		return new UpdateModelingConfigurationCommand(getNewId(workspace),
				Command.NEW_MODEL, request.getParameter(Arguments.property.name()), request.getParameter(Arguments.value.name()));
    }

    @Override
    public Class<? extends Command> getCorrespondingCommand() {
        return UpdateModelingConfigurationCommand.class;
    }
}
