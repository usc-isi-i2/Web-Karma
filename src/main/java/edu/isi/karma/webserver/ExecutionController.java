package edu.isi.karma.webserver;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.CommandWithPreview;
import edu.isi.karma.controller.command.EditCellCommand;
import edu.isi.karma.controller.command.EditCellCommandFactory;
import edu.isi.karma.controller.command.GenerateSemanticTypesCommand;
import edu.isi.karma.controller.command.GenerateSemanticTypesCommandFactory;
import edu.isi.karma.controller.command.ImportCSVFileCommand;
import edu.isi.karma.controller.command.ImportCSVFileCommandFactory;
import edu.isi.karma.controller.command.ImportDatabaseTableCommand;
import edu.isi.karma.controller.command.ImportDatabaseTableCommandFactory;
import edu.isi.karma.controller.command.ImportJSONFileCommand;
import edu.isi.karma.controller.command.ImportJSONFileCommandFactory;
import edu.isi.karma.controller.command.ImportXMLFileCommand;
import edu.isi.karma.controller.command.ImportXMLFileCommandFactory;
import edu.isi.karma.controller.command.TablePagerCommand;
import edu.isi.karma.controller.command.TablePagerCommandFactory;
import edu.isi.karma.controller.command.TablePagerResizeCommand;
import edu.isi.karma.controller.command.TablePagerResizeCommandFactory;
import edu.isi.karma.controller.command.UndoRedoCommand;
import edu.isi.karma.controller.command.UndoRedoCommandFactory;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.view.VWorkspace;

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

	private final VWorkspace vWorkspace;

	public ExecutionController(VWorkspace vWorkspace) {
		this.vWorkspace = vWorkspace;
		initializeCommandFactoryMap();
	}

	private void initializeCommandFactoryMap() {
		// TODO: there must be a way to do this using Reflection with all
		// subclasses of CommandFactory.
		commandFactoryMap.put(EditCellCommand.class.getSimpleName(),
				new EditCellCommandFactory());
		commandFactoryMap.put(UndoRedoCommand.class.getSimpleName(),
				new UndoRedoCommandFactory());
		commandFactoryMap.put(TablePagerCommand.class.getSimpleName(),
				new TablePagerCommandFactory());
		commandFactoryMap.put(TablePagerResizeCommand.class.getSimpleName(),
				new TablePagerResizeCommandFactory());
		commandFactoryMap.put(ImportJSONFileCommand.class.getSimpleName(),
				new ImportJSONFileCommandFactory());
		commandFactoryMap.put(ImportCSVFileCommand.class.getSimpleName(),
				new ImportCSVFileCommandFactory());
		commandFactoryMap.put(ImportDatabaseTableCommand.class.getSimpleName(),
				new ImportDatabaseTableCommandFactory());
		commandFactoryMap.put(ImportXMLFileCommand.class.getSimpleName(),
				new ImportXMLFileCommandFactory());
		commandFactoryMap.put(GenerateSemanticTypesCommand.class.getSimpleName(),
				new GenerateSemanticTypesCommandFactory());
	}

	public VWorkspace getvWorkspace() {
		return vWorkspace;
	}

	public Command getCommand(HttpServletRequest request) {
		CommandFactory cf = commandFactoryMap.get(request
				.getParameter("command"));
		if (cf != null) {
			return cf.createCommand(request, vWorkspace);
		} else {
			logger.error("Command " + request.getParameter("command")
					+ " not found!");
			return null;
		}
	}

	public String invokeCommand(Command command) {
		synchronized (this) {
			try {
				UpdateContainer updateContainer = null;
				vWorkspace.getWorkspace().getCommandHistory().setCurrentCommand(command);
				if(command instanceof CommandWithPreview){
					updateContainer = ((CommandWithPreview)command).showPreview(vWorkspace);
				} else {
					updateContainer = vWorkspace.getWorkspace()
					.getCommandHistory().doCommand(command, vWorkspace);
				}
				
				String responseJson = updateContainer.generateJson(vWorkspace);
				logger.info(responseJson);
				return responseJson;
			} catch (CommandException e) {
				logger.error(
						"Error occured with command " + command.toString(), e);
				return ""; // TODO probably need a return that indicates an
				// error.
			}
		}

	}

}
