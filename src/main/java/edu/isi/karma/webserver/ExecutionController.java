package edu.isi.karma.webserver;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CloseWorkspaceCommand;
import edu.isi.karma.controller.command.CloseWorkspaceCommandFactory;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.CommandWithPreview;
import edu.isi.karma.controller.command.EditCellCommand;
import edu.isi.karma.controller.command.EditCellCommandFactory;
import edu.isi.karma.controller.command.ImportCSVFileCommand;
import edu.isi.karma.controller.command.ImportCSVFileCommandFactory;
import edu.isi.karma.controller.command.ImportDatabaseTableCommand;
import edu.isi.karma.controller.command.ImportDatabaseTableCommandFactory;
import edu.isi.karma.controller.command.ImportExcelFileCommand;
import edu.isi.karma.controller.command.ImportExcelFileCommandFactory;
import edu.isi.karma.controller.command.ImportJSONFileCommand;
import edu.isi.karma.controller.command.ImportJSONFileCommandFactory;
import edu.isi.karma.controller.command.ImportOntologyCommand;
import edu.isi.karma.controller.command.ImportOntologyCommandFactory;
import edu.isi.karma.controller.command.ImportXMLFileCommand;
import edu.isi.karma.controller.command.ImportXMLFileCommandFactory;
import edu.isi.karma.controller.command.SplitByCommaCommand;
import edu.isi.karma.controller.command.SplitByCommaCommandFactory;
import edu.isi.karma.controller.command.TablePagerCommand;
import edu.isi.karma.controller.command.TablePagerCommandFactory;
import edu.isi.karma.controller.command.TablePagerResizeCommand;
import edu.isi.karma.controller.command.TablePagerResizeCommandFactory;
import edu.isi.karma.controller.command.UndoRedoCommand;
import edu.isi.karma.controller.command.UndoRedoCommandFactory;
import edu.isi.karma.controller.command.alignment.AddUserLinkToAlignmentCommand;
import edu.isi.karma.controller.command.alignment.AddUserLinkToAlignmentCommandFactory;
import edu.isi.karma.controller.command.alignment.AlignToOntologyCommand;
import edu.isi.karma.controller.command.alignment.AlignToOntologyCommandFactory;
import edu.isi.karma.controller.command.alignment.DuplicateDomainOfLinkCommand;
import edu.isi.karma.controller.command.alignment.DuplicateDomainOfLinkCommandFactory;
import edu.isi.karma.controller.command.alignment.GenerateSemanticTypesCommand;
import edu.isi.karma.controller.command.alignment.GenerateSemanticTypesCommandFactory;
import edu.isi.karma.controller.command.alignment.GetAlternativeLinksCommand;
import edu.isi.karma.controller.command.alignment.GetAlternativeLinksCommandFactory;
import edu.isi.karma.controller.command.alignment.GetDataPropertiesForClassCommand;
import edu.isi.karma.controller.command.alignment.GetDataPropertiesForClassCommandFactory;
import edu.isi.karma.controller.command.alignment.GetDataPropertyHierarchyCommand;
import edu.isi.karma.controller.command.alignment.GetDataPropertyHierarchyCommandFactory;
import edu.isi.karma.controller.command.alignment.GetDomainsForDataPropertyCommand;
import edu.isi.karma.controller.command.alignment.GetDomainsForDataPropertyCommandFactory;
import edu.isi.karma.controller.command.alignment.GetOntologyClassHierarchyCommand;
import edu.isi.karma.controller.command.alignment.GetOntologyClassHierarchyCommandFactory;
import edu.isi.karma.controller.command.alignment.SetSemanticTypeCommand;
import edu.isi.karma.controller.command.alignment.SetSemanticTypeCommandFactory;
import edu.isi.karma.controller.command.alignment.ShowModelCommand;
import edu.isi.karma.controller.command.alignment.ShowModelCommandFactory;
import edu.isi.karma.controller.command.alignment.UnassignSemanticTypeCommand;
import edu.isi.karma.controller.command.alignment.UnassignSemanticTypeCommandFactory;
import edu.isi.karma.controller.command.publish.PublishKMLLayerCommand;
import edu.isi.karma.controller.command.publish.PublishKMLLayerCommandFactory;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCommandFactory;
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
		commandFactoryMap.put(GetOntologyClassHierarchyCommand.class.getSimpleName(),
				new GetOntologyClassHierarchyCommandFactory());
		commandFactoryMap.put(GetDataPropertyHierarchyCommand.class.getSimpleName(),
				new GetDataPropertyHierarchyCommandFactory());
		commandFactoryMap.put(SetSemanticTypeCommand.class.getSimpleName(),
				new SetSemanticTypeCommandFactory());
		commandFactoryMap.put(AlignToOntologyCommand.class.getSimpleName(),
				new AlignToOntologyCommandFactory());
		commandFactoryMap.put(ImportOntologyCommand.class.getSimpleName(),
				new ImportOntologyCommandFactory());
		commandFactoryMap.put(GetDomainsForDataPropertyCommand.class.getSimpleName(),
				new GetDomainsForDataPropertyCommandFactory());
		commandFactoryMap.put(GetDataPropertiesForClassCommand.class.getSimpleName(),
				new GetDataPropertiesForClassCommandFactory());
		commandFactoryMap.put(GetAlternativeLinksCommand.class.getSimpleName(),
				new GetAlternativeLinksCommandFactory());
		commandFactoryMap.put(AddUserLinkToAlignmentCommand.class.getSimpleName(),
				new AddUserLinkToAlignmentCommandFactory());
		commandFactoryMap.put(UnassignSemanticTypeCommand.class.getSimpleName(),
				new UnassignSemanticTypeCommandFactory());
		commandFactoryMap.put(ShowModelCommand.class.getSimpleName(),
				new ShowModelCommandFactory());
		commandFactoryMap.put(SplitByCommaCommand.class.getSimpleName(),
				new SplitByCommaCommandFactory());
		commandFactoryMap.put(DuplicateDomainOfLinkCommand.class.getSimpleName(),
				new DuplicateDomainOfLinkCommandFactory());
		commandFactoryMap.put(CloseWorkspaceCommand.class.getSimpleName(),
				new CloseWorkspaceCommandFactory());
		commandFactoryMap.put(PublishKMLLayerCommand.class.getSimpleName(),
				new PublishKMLLayerCommandFactory());
		commandFactoryMap.put(ImportExcelFileCommand.class.getSimpleName(),
				new ImportExcelFileCommandFactory());
		commandFactoryMap.put(PublishRDFCommand.class.getSimpleName(),
				new PublishRDFCommandFactory());
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
				//logger.info(responseJson);
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
