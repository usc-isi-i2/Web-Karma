/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
package edu.isi.karma.webserver;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.AddColumnCommand;
import edu.isi.karma.controller.command.AddColumnCommandFactory;
import edu.isi.karma.controller.command.AddNewColumnCommand;
import edu.isi.karma.controller.command.AddNewColumnCommandFactory;
import edu.isi.karma.controller.command.ApplyWorksheetHistoryCommand;
import edu.isi.karma.controller.command.ApplyWorksheetHistoryCommandFactory;
import edu.isi.karma.controller.command.CloseWorkspaceCommand;
import edu.isi.karma.controller.command.CloseWorkspaceCommandFactory;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.CommandWithPreview;
import edu.isi.karma.controller.command.EditCellCommand;
import edu.isi.karma.controller.command.EditCellCommandFactory;
import edu.isi.karma.controller.command.FetchPreferencesCommand;
import edu.isi.karma.controller.command.FetchPreferencesCommandFactory;
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
import edu.isi.karma.controller.command.ImportServiceCommand;
import edu.isi.karma.controller.command.ImportServiceCommandFactory;
import edu.isi.karma.controller.command.ImportUnionResultCommand;
import edu.isi.karma.controller.command.ImportUnionResultCommandFactory;
import edu.isi.karma.controller.command.ImportXMLFileCommand;
import edu.isi.karma.controller.command.ImportXMLFileCommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.command.ResetKarmaCommand;
import edu.isi.karma.controller.command.ResetKarmaCommandFactory;
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
import edu.isi.karma.controller.command.alignment.GetPropertiesAndClassesList;
import edu.isi.karma.controller.command.alignment.GetPropertiesAndClassesListCommandFactory;
import edu.isi.karma.controller.command.alignment.ResetModelCommand;
import edu.isi.karma.controller.command.alignment.ResetModelCommandFactory;
import edu.isi.karma.controller.command.alignment.SetMetaPropertyCommand;
import edu.isi.karma.controller.command.alignment.SetMetaPropertyCommandFactory;
import edu.isi.karma.controller.command.alignment.SetSemanticTypeCommand;
import edu.isi.karma.controller.command.alignment.SetSemanticTypeCommandFactory;
import edu.isi.karma.controller.command.alignment.ShowAutoModelCommand;
import edu.isi.karma.controller.command.alignment.ShowAutoModelCommandFactory;
import edu.isi.karma.controller.command.alignment.ShowModelCommand;
import edu.isi.karma.controller.command.alignment.ShowModelCommandFactory;
import edu.isi.karma.controller.command.alignment.UnassignSemanticTypeCommand;
import edu.isi.karma.controller.command.alignment.UnassignSemanticTypeCommandFactory;
import edu.isi.karma.controller.command.cleaning.FetchTransformingDataCommand;
import edu.isi.karma.controller.command.cleaning.FetchTransformingDataFactory;
import edu.isi.karma.controller.command.cleaning.GenerateCleaningRulesCommand;
import edu.isi.karma.controller.command.cleaning.GenerateCleaningRulesCommandFactory;
import edu.isi.karma.controller.command.publish.PublishCSVCommand;
import edu.isi.karma.controller.command.publish.PublishCSVCommandFactory;
import edu.isi.karma.controller.command.publish.PublishDatabaseCommand;
import edu.isi.karma.controller.command.publish.PublishDatabaseCommandFactory;
import edu.isi.karma.controller.command.publish.PublishKMLLayerCommand;
import edu.isi.karma.controller.command.publish.PublishKMLLayerCommandFactory;
import edu.isi.karma.controller.command.publish.PublishRDFCellCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCellCommandFactory;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCommandFactory;
import edu.isi.karma.controller.command.publish.PublishWorksheetHistoryCommand;
import edu.isi.karma.controller.command.publish.PublishWorksheetHistoryCommandFactory;
import edu.isi.karma.controller.command.service.InvokeServiceCommand;
import edu.isi.karma.controller.command.service.InvokeServiceCommandFactory;
import edu.isi.karma.controller.command.service.PopulateCommand;
import edu.isi.karma.controller.command.service.PopulateCommandFactory;
import edu.isi.karma.controller.command.service.PublishModelCommand;
import edu.isi.karma.controller.command.service.PublishModelCommandFactory;
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
		commandFactoryMap.put(GetOntologyClassHierarchyCommand.class.getSimpleName(),
				new GetOntologyClassHierarchyCommandFactory());
		commandFactoryMap.put(GetDataPropertyHierarchyCommand.class.getSimpleName(),
				new GetDataPropertyHierarchyCommandFactory());
		commandFactoryMap.put(SetSemanticTypeCommand.class.getSimpleName(),
				new SetSemanticTypeCommandFactory());
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
		commandFactoryMap.put(ShowAutoModelCommand.class.getSimpleName(),
				new ShowAutoModelCommandFactory());
		commandFactoryMap.put(SplitByCommaCommand.class.getSimpleName(),
				new SplitByCommaCommandFactory());
		commandFactoryMap.put(CloseWorkspaceCommand.class.getSimpleName(),
				new CloseWorkspaceCommandFactory());
		commandFactoryMap.put(PublishKMLLayerCommand.class.getSimpleName(),
				new PublishKMLLayerCommandFactory());
		commandFactoryMap.put(ImportExcelFileCommand.class.getSimpleName(),
				new ImportExcelFileCommandFactory());
		commandFactoryMap.put(ImportServiceCommand.class.getSimpleName(),
				new ImportServiceCommandFactory());
		commandFactoryMap.put(PublishRDFCommand.class.getSimpleName(),
				new PublishRDFCommandFactory());
		commandFactoryMap.put(PublishDatabaseCommand.class.getSimpleName(),
				new PublishDatabaseCommandFactory());
		commandFactoryMap.put(AddNewColumnCommand.class.getSimpleName(),
				new AddNewColumnCommandFactory());
		commandFactoryMap.put(AddColumnCommand.class.getSimpleName(),
				new AddColumnCommandFactory());
		commandFactoryMap.put(PublishRDFCellCommand.class.getSimpleName(),
				new PublishRDFCellCommandFactory());
		commandFactoryMap.put(FetchPreferencesCommand.class.getSimpleName(),
				new FetchPreferencesCommandFactory());
		commandFactoryMap.put(ResetModelCommand.class.getSimpleName(),
				new ResetModelCommandFactory());
		commandFactoryMap.put(GenerateCleaningRulesCommand.class.getSimpleName(),
				new GenerateCleaningRulesCommandFactory());
		commandFactoryMap.put(InvokeServiceCommand.class.getSimpleName(),
				new InvokeServiceCommandFactory());
		commandFactoryMap.put(GetPropertiesAndClassesList.class.getSimpleName(),
				new GetPropertiesAndClassesListCommandFactory());
		commandFactoryMap.put(PublishModelCommand.class.getSimpleName(),
				new PublishModelCommandFactory());
		commandFactoryMap.put(PopulateCommand.class.getSimpleName(),
				new PopulateCommandFactory());
		commandFactoryMap.put(PublishWorksheetHistoryCommand.class.getSimpleName(),
				new PublishWorksheetHistoryCommandFactory());
		commandFactoryMap.put(ApplyWorksheetHistoryCommand.class.getSimpleName(),
				new ApplyWorksheetHistoryCommandFactory());
		commandFactoryMap.put(PublishCSVCommand.class.getSimpleName(),
				new PublishCSVCommandFactory());
		commandFactoryMap.put(SetMetaPropertyCommand.class.getSimpleName(),
				new SetMetaPropertyCommandFactory());
		commandFactoryMap.put(ResetKarmaCommand.class.getSimpleName(),
				new ResetKarmaCommandFactory());
		commandFactoryMap.put(FetchTransformingDataCommand.class.getSimpleName(),
				new FetchTransformingDataFactory());
		commandFactoryMap.put(ImportUnionResultCommand.class.getSimpleName(),
				new ImportUnionResultCommandFactory());
	}

	public VWorkspace getvWorkspace() {
		return vWorkspace;
	}

	public HashMap<String, CommandFactory> getCommandFactoryMap() {
		return commandFactoryMap;
	}

	public Command getCommand(HttpServletRequest request) {
		CommandFactory cf = commandFactoryMap.get(request.getParameter("command"));
		if (cf != null) {
			if (cf instanceof JSONInputCommandFactory) {
				try {
					JSONInputCommandFactory scf = (JSONInputCommandFactory)cf;
					return scf.createCommand(new JSONArray(request.getParameter("newInfo")), vWorkspace);
				}  catch (Exception e) {
					e.printStackTrace();
					return null;
				} 
			} else
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
					updateContainer = vWorkspace.getWorkspace().getCommandHistory().doCommand(command, vWorkspace);
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
