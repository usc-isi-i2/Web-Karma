/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 * ****************************************************************************
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
		// TODO
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

    private void initializeCommandFactoryMap() {
        // TODO: there must be a way to do this using Reflection with all
        // subclasses of CommandFactory.
       /* commandFactoryMap.put(EditCellCommand.class.getSimpleName(),
                new EditCellCommandFactory());
        commandFactoryMap.put(UndoRedoCommand.class.getSimpleName(),
                new UndoRedoCommandFactory());
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
        commandFactoryMap.put(AddColumnCommand.class.getSimpleName(),
                new AddColumnCommandFactory());
        commandFactoryMap.put(AddRowCommand.class.getSimpleName(),
                new AddRowCommandFactory());
        commandFactoryMap.put(PublishRDFCellCommand.class.getSimpleName(),
                new PublishRDFCellCommandFactory());
        commandFactoryMap.put(FetchPreferencesCommand.class.getSimpleName(),
                new FetchPreferencesCommandFactory());
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
        commandFactoryMap.put(MultipleValueEditColumnCommand.class.getSimpleName(),
                new MultipleValueEditColumnCommandFactory());
        commandFactoryMap.put(SubmitCleaningCommand.class.getSimpleName(),
                new SubmitCleaningCommandFactory());
        commandFactoryMap.put(RenameColumnCommand.class.getSimpleName(),
                new RenameColumnCommandFactory());
        commandFactoryMap.put(PublishMDBCommand.class.getSimpleName(),
                new PublishMDBCommandFactory());
        commandFactoryMap.put(PublishSpatialDataCommand.class.getSimpleName(),
                new PublishSpatialDataCommandFactory());
        commandFactoryMap.put(PreviewPythonTransformationResultsCommand.class.getSimpleName(),
                new PreviewPythonTransformationResultsCommandFactory());
        commandFactoryMap.put(SubmitPythonTransformationCommand.class.getSimpleName(),
                new SubmitPythonTransformationCommandFactory());
        commandFactoryMap.put(SubmitEditPythonTransformationCommand.class.getSimpleName(), 
        		new SubmitEditPythonTransformationCommandFactory());
        commandFactoryMap.put(GenerateR2RMLModelCommand.class.getSimpleName(),
                new GenerateR2RMLModelCommandFactory());
        commandFactoryMap.put(ApplyHistoryFromR2RMLModelCommand.class.getSimpleName(),
                new ApplyHistoryFromR2RMLModelCommandFactory());
        commandFactoryMap.put(GetCurrentLinksOfInternalNodeCommand.class.getSimpleName(),
                new GetCurrentLinksOfInternalNodeCommandFactory());
        commandFactoryMap.put(GetInternalNodesListOfAlignmentCommand.class.getSimpleName(),
                new GetInternalNodesListOfAlignmentCommandFactory());
        commandFactoryMap.put(GetLinksOfAlignmentCommand.class.getSimpleName(),
                new GetLinksOfAlignmentCommandFactory());
        commandFactoryMap.put(ChangeInternalNodeLinksCommand.class.getSimpleName(),
                new ChangeInternalNodeLinksCommandFactory());
        commandFactoryMap.put(InvokeCleaningServiceCommand.class.getSimpleName(),
                new InvokeCleaningServiceCommandFactory());
        commandFactoryMap.put(SetWorksheetPropertiesCommand.class.getSimpleName(),
                new SetWorksheetPropertiesCommandFactory());
        commandFactoryMap.put(FetchExistingWorksheetPropertiesCommand.class.getSimpleName(),
                new FetchExistingWorksheetPropertiesCommandFactory());
        commandFactoryMap.put(FetchR2RMLModelsCommand.class.getSimpleName(),
                new FetchR2RMLModelsCommandFactory());
        commandFactoryMap.put(FetchExistingModelsForWorksheetCommand.class.getSimpleName(),
                new FetchExistingModelsForWorksheetCommandFactory());
        commandFactoryMap.put(ApplyModelFromTripleStoreCommand.class.getSimpleName(),
                new ApplyModelFromTripleStoreCommandFactory());
        commandFactoryMap.put(CreateNewModelCommand.class.getSimpleName(),
                new CreateNewModelCommandFactory());
        commandFactoryMap.put(InvokeDataMiningServiceCommand.class.getSimpleName(),
                new InvokeDataMiningServiceCommandFactory());
        commandFactoryMap.put(InvokeRubenReconciliationService.class.getSimpleName(),
                new InvokeRubenReconciliationServiceFactory());
        commandFactoryMap.put(FetchGraphsFromTripleStoreCommand.class.getSimpleName(),
                new FetchGraphsFromTripleStoreCommandFactory());
        commandFactoryMap.put(GetUniqueGraphUrlCommand.class.getSimpleName(),
                new GetUniqueGraphUrlCommandFactory());
        commandFactoryMap.put(TestSPARQLEndPointCommand.class.getSimpleName(),
                new TestSPARQLEndPointCommandFactory());
        commandFactoryMap.put(LoadAdditionalWorksheetRowsCommand.class.getSimpleName(),
                new LoadAdditionalWorksheetRowsCommandFactory());*/
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

               //logger.info(responseJson);
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
