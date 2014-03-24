/*
 * Copyright (c) 2014 CUBRC, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *               http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package edu.isi.karma.webserver;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import org.json.JSONArray;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

/**
 * There is one ExecutionManager per user. In the HttpServlet implementation we
 * need a map from users to ExecutionManager, using the VWorkspace ID to
 * dispatch to the right one.
 *
 * @author szekely
 *
 */
public class ExecutionController {

    private static Logger logger = LoggerFactory
            .getLogger(ExecutionController.class);
    private final HashMap<String, CommandFactory> commandFactoryMap = new HashMap<String, CommandFactory>();
    private final Workspace workspace;

    public ExecutionController(Workspace workspace) {
        this.workspace = workspace;
//        initializeCommandFactoryMap();
	    dynamicallyBuildCommandFactoryMap();
    }

	private void dynamicallyBuildCommandFactoryMap()
	{
		Reflections reflections = new Reflections("edu.isi.karma");

		Set<Class<? extends CommandFactory>> subTypes =
				reflections.getSubTypesOf(CommandFactory.class);

		for (Class<? extends CommandFactory> subType : subTypes)
		{
			if(!Modifier.isAbstract(subType.getModifiers()) && !subType.isInterface())
			try
			{

				CommandFactory commandFactory = subType.newInstance();
				Class command = commandFactory.getCorrespondingCommand();

				commandFactoryMap.put(command.getSimpleName(), commandFactory);
			} catch (InstantiationException e)
			{
				logger.error("Error instantiating {} -- likely does not have no-arg constructor", subType);
				e.printStackTrace();
			} catch (IllegalAccessException e)
			{
				logger.error("Error instantiating {} -- likely does not have a public no-arg constructor", subType);
			}
		}

		logger.info("Loaded {} possible commands", commandFactoryMap.size());
	}

    public Workspace getWorkspace() {
        return workspace;
    }

    public HashMap<String, CommandFactory> getCommandFactoryMap() {
        return commandFactoryMap;
    }

    public Command getCommand(HttpServletRequest request) {
        CommandFactory cf = commandFactoryMap.get(request.getParameter("command"));
        if (cf != null) {
                try {
	                String newInfo = request.getParameter("newInfo");
	                return cf.createCommand(newInfo == null ? null : new JSONArray(newInfo), workspace);
                } catch (UnsupportedOperationException ignored)
                {
	                return cf.createCommand(request, workspace);
                } catch (Exception e) {
	                logger.error(commandFactoryMap.toString());
	                logger.error(request.toString());
	                logger.error("Error getting command!!", e);
                    return null;
                }
        } else {
            logger.error("Command " + request.getParameter("command")
                    + " not found!");
            return null;
        }
    }

    public UpdateContainer invokeCommand(Command command) {
        synchronized (this) {
            try {
                UpdateContainer updateContainer = null;
                workspace.getCommandHistory().setCurrentCommand(command);

                if (command instanceof IPreviewable) {
                    updateContainer = ((IPreviewable) command).showPreview();
                } else {
                    updateContainer = workspace.getCommandHistory().doCommand(command, workspace);
                }

                return updateContainer;
            } catch (CommandException e) {
                logger.error(
                        "Error occured with command " + command.toString(), e);
                UpdateContainer updateContainer = new UpdateContainer();
                updateContainer.add(new ErrorUpdate("Error occured with command " + command.toString()));
                return updateContainer; // TODO probably need a return that indicates an
                // error.
            }
        }

    }
}
