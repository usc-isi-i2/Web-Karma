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

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

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
	private static final HashMap<String, CommandFactory> commandFactoryMap = new HashMap<>();
	private final Workspace workspace;

	static{
		dynamicallyBuildCommandFactoryMap();
	}

	public ExecutionController(Workspace workspace) {
		this.workspace = workspace;

	}

	private static void dynamicallyBuildCommandFactoryMap()
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
					Class<? extends Command> command = commandFactory.getCorrespondingCommand();

					commandFactoryMap.put(command.getSimpleName(), commandFactory);
			} catch (InstantiationException e)
			{
				logger.error("Error instantiating {} -- likely does not have no-arg constructor", subType);
				e.printStackTrace();
			} catch (IllegalAccessException e)
			{
				logger.error("Error instantiating {} -- likely does not have a public no-arg constructor", subType);
			}
			catch (Exception e)
			{
				logger.error("we got a problem");
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
			if (cf instanceof JSONInputCommandFactory) {
				String newInfo = request.getParameter("newInfo");
				try {
					return cf.createCommand(newInfo == null ? null : new JSONArray(newInfo), Command.NEW_MODEL, workspace);
				} catch (Exception e) {
					logger.error("Error getting command!!", e);
					return null;
				}
			}
			else {
				return cf.createCommand(request, workspace);
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
				UpdateContainer updateContainer = workspace.getCommandHistory().doCommand(command, workspace);
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
