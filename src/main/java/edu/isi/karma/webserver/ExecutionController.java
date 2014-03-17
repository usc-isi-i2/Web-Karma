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

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CloseWorkspaceCommand;
import edu.isi.karma.controller.command.CloseWorkspaceCommandFactory;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.FetchGraphsFromTripleStoreCommand;
import edu.isi.karma.controller.command.FetchGraphsFromTripleStoreCommandFactory;
import edu.isi.karma.controller.command.FetchPreferencesCommand;
import edu.isi.karma.controller.command.FetchPreferencesCommandFactory;
import edu.isi.karma.controller.command.GetUniqueGraphUrlCommand;
import edu.isi.karma.controller.command.GetUniqueGraphUrlCommandFactory;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.controller.command.ResetKarmaCommand;
import edu.isi.karma.controller.command.ResetKarmaCommandFactory;
import edu.isi.karma.controller.command.TestSPARQLEndPointCommand;
import edu.isi.karma.controller.command.TestSPARQLEndPointCommandFactory;
import edu.isi.karma.controller.command.UndoRedoCommand;
import edu.isi.karma.controller.command.UndoRedoCommandFactory;
import edu.isi.karma.controller.command.alignment.AddUserLinkToAlignmentCommand;
import edu.isi.karma.controller.command.alignment.AddUserLinkToAlignmentCommandFactory;
import edu.isi.karma.controller.command.alignment.ApplyModelFromTripleStoreCommand;
import edu.isi.karma.controller.command.alignment.ApplyModelFromTripleStoreCommandFactory;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommand;
import edu.isi.karma.controller.command.alignment.ChangeInternalNodeLinksCommandFactory;
import edu.isi.karma.controller.command.alignment.CreateNewModelCommand;
import edu.isi.karma.controller.command.alignment.CreateNewModelCommandFactory;
import edu.isi.karma.controller.command.alignment.FetchExistingModelsForWorksheetCommand;
import edu.isi.karma.controller.command.alignment.FetchExistingModelsForWorksheetCommandFactory;
import edu.isi.karma.controller.command.alignment.FetchR2RMLModelsCommand;
import edu.isi.karma.controller.command.alignment.FetchR2RMLModelsCommandFactory;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommand;
import edu.isi.karma.controller.command.alignment.GenerateR2RMLModelCommandFactory;
import edu.isi.karma.controller.command.alignment.GetAlternativeLinksCommand;
import edu.isi.karma.controller.command.alignment.GetAlternativeLinksCommandFactory;
import edu.isi.karma.controller.command.alignment.GetCurrentLinksOfInternalNodeCommand;
import edu.isi.karma.controller.command.alignment.GetCurrentLinksOfInternalNodeCommandFactory;
import edu.isi.karma.controller.command.alignment.GetDataPropertiesForClassCommand;
import edu.isi.karma.controller.command.alignment.GetDataPropertiesForClassCommandFactory;
import edu.isi.karma.controller.command.alignment.GetDataPropertyHierarchyCommand;
import edu.isi.karma.controller.command.alignment.GetDataPropertyHierarchyCommandFactory;
import edu.isi.karma.controller.command.alignment.GetDomainsForDataPropertyCommand;
import edu.isi.karma.controller.command.alignment.GetDomainsForDataPropertyCommandFactory;
import edu.isi.karma.controller.command.alignment.GetInternalNodesListOfAlignmentCommand;
import edu.isi.karma.controller.command.alignment.GetInternalNodesListOfAlignmentCommandFactory;
import edu.isi.karma.controller.command.alignment.GetLinksOfAlignmentCommand;
import edu.isi.karma.controller.command.alignment.GetLinksOfAlignmentCommandFactory;
import edu.isi.karma.controller.command.alignment.GetOntologyClassHierarchyCommand;
import edu.isi.karma.controller.command.alignment.GetOntologyClassHierarchyCommandFactory;
import edu.isi.karma.controller.command.alignment.GetPropertiesAndClassesList;
import edu.isi.karma.controller.command.alignment.GetPropertiesAndClassesListCommandFactory;
import edu.isi.karma.controller.command.alignment.InvokeDataMiningServiceCommand;
import edu.isi.karma.controller.command.alignment.InvokeDataMiningServiceCommandFactory;
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
import edu.isi.karma.controller.command.cleaning.InvokeCleaningServiceCommand;
import edu.isi.karma.controller.command.cleaning.InvokeCleaningServiceCommandFactory;
import edu.isi.karma.controller.command.cleaning.SubmitCleaningCommand;
import edu.isi.karma.controller.command.cleaning.SubmitCleaningCommandFactory;
import edu.isi.karma.controller.command.importdata.ImportCSVFileCommand;
import edu.isi.karma.controller.command.importdata.ImportCSVFileCommandFactory;
import edu.isi.karma.controller.command.importdata.ImportDatabaseTableCommand;
import edu.isi.karma.controller.command.importdata.ImportDatabaseTableCommandFactory;
import edu.isi.karma.controller.command.importdata.ImportExcelFileCommand;
import edu.isi.karma.controller.command.importdata.ImportExcelFileCommandFactory;
import edu.isi.karma.controller.command.importdata.ImportJSONFileCommand;
import edu.isi.karma.controller.command.importdata.ImportJSONFileCommandFactory;
import edu.isi.karma.controller.command.importdata.ImportOntologyCommand;
import edu.isi.karma.controller.command.importdata.ImportOntologyCommandFactory;
import edu.isi.karma.controller.command.importdata.ImportServiceCommand;
import edu.isi.karma.controller.command.importdata.ImportServiceCommandFactory;
import edu.isi.karma.controller.command.importdata.ImportXMLFileCommand;
import edu.isi.karma.controller.command.importdata.ImportXMLFileCommandFactory;
import edu.isi.karma.controller.command.publish.PublishCSVCommand;
import edu.isi.karma.controller.command.publish.PublishCSVCommandFactory;
import edu.isi.karma.controller.command.publish.PublishDatabaseCommand;
import edu.isi.karma.controller.command.publish.PublishDatabaseCommandFactory;
import edu.isi.karma.controller.command.publish.PublishKMLLayerCommand;
import edu.isi.karma.controller.command.publish.PublishKMLLayerCommandFactory;
import edu.isi.karma.controller.command.publish.PublishMDBCommand;
import edu.isi.karma.controller.command.publish.PublishMDBCommandFactory;
import edu.isi.karma.controller.command.publish.PublishRDFCellCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCellCommandFactory;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCommandFactory;
import edu.isi.karma.controller.command.publish.PublishSpatialDataCommand;
import edu.isi.karma.controller.command.publish.PublishSpatialDataCommandFactory;
import edu.isi.karma.controller.command.publish.PublishWorksheetHistoryCommand;
import edu.isi.karma.controller.command.publish.PublishWorksheetHistoryCommandFactory;
import edu.isi.karma.controller.command.reconciliation.InvokeRubenReconciliationService;
import edu.isi.karma.controller.command.reconciliation.InvokeRubenReconciliationServiceFactory;
import edu.isi.karma.controller.command.service.InvokeServiceCommand;
import edu.isi.karma.controller.command.service.InvokeServiceCommandFactory;
import edu.isi.karma.controller.command.service.PopulateCommand;
import edu.isi.karma.controller.command.service.PopulateCommandFactory;
import edu.isi.karma.controller.command.service.PublishModelCommand;
import edu.isi.karma.controller.command.service.PublishModelCommandFactory;
import edu.isi.karma.controller.command.transformation.PreviewPythonTransformationResultsCommand;
import edu.isi.karma.controller.command.transformation.PreviewPythonTransformationResultsCommandFactory;
import edu.isi.karma.controller.command.transformation.SubmitEditPythonTransformationCommand;
import edu.isi.karma.controller.command.transformation.SubmitEditPythonTransformationCommandFactory;
import edu.isi.karma.controller.command.transformation.SubmitPythonTransformationCommand;
import edu.isi.karma.controller.command.transformation.SubmitPythonTransformationCommandFactory;
import edu.isi.karma.controller.command.worksheet.AddColumnCommand;
import edu.isi.karma.controller.command.worksheet.AddColumnCommandFactory;
import edu.isi.karma.controller.command.worksheet.AddRowCommand;
import edu.isi.karma.controller.command.worksheet.AddRowCommandFactory;
import edu.isi.karma.controller.command.worksheet.AddValuesCommand;
import edu.isi.karma.controller.command.worksheet.AddValuesCommandFactory;
import edu.isi.karma.controller.command.worksheet.ApplyHistoryFromR2RMLModelCommand;
import edu.isi.karma.controller.command.worksheet.ApplyHistoryFromR2RMLModelCommandFactory;
import edu.isi.karma.controller.command.worksheet.ApplyWorksheetHistoryCommand;
import edu.isi.karma.controller.command.worksheet.ApplyWorksheetHistoryCommandFactory;
import edu.isi.karma.controller.command.worksheet.DeleteWorksheetCommand;
import edu.isi.karma.controller.command.worksheet.DeleteWorksheetCommandFactory;
import edu.isi.karma.controller.command.worksheet.EditCellCommand;
import edu.isi.karma.controller.command.worksheet.EditCellCommandFactory;
import edu.isi.karma.controller.command.worksheet.FetchExistingWorksheetPropertiesCommand;
import edu.isi.karma.controller.command.worksheet.FetchExistingWorksheetPropertiesCommandFactory;
import edu.isi.karma.controller.command.worksheet.FoldCommand;
import edu.isi.karma.controller.command.worksheet.FoldCommandFactory;
import edu.isi.karma.controller.command.worksheet.LoadAdditionalWorksheetRowsCommand;
import edu.isi.karma.controller.command.worksheet.LoadAdditionalWorksheetRowsCommandFactory;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommand;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommandFactory;
import edu.isi.karma.controller.command.worksheet.RefreshSVGAligmentCommandFactory;
import edu.isi.karma.controller.command.worksheet.RefreshSVGAlignmentCommand;
import edu.isi.karma.controller.command.worksheet.RenameColumnCommand;
import edu.isi.karma.controller.command.worksheet.RenameColumnCommandFactory;
import edu.isi.karma.controller.command.worksheet.SaveRowIDCommand;
import edu.isi.karma.controller.command.worksheet.SaveRowIDCommandFactory;
import edu.isi.karma.controller.command.worksheet.SetWorksheetPropertiesCommand;
import edu.isi.karma.controller.command.worksheet.SetWorksheetPropertiesCommandFactory;
import edu.isi.karma.controller.command.worksheet.SplitByCommaCommand;
import edu.isi.karma.controller.command.worksheet.SplitByCommaCommandFactory;
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
    private final HashMap<String, CommandFactory> commandFactoryMap = new HashMap<String, CommandFactory>();
    private final Workspace workspace;

    public ExecutionController(Workspace workspace) {
        this.workspace = workspace;
        initializeCommandFactoryMap();
    }

    private void initializeCommandFactoryMap() {
        // TODO: there must be a way to do this using Reflection with all
        // subclasses of CommandFactory.
        commandFactoryMap.put(EditCellCommand.class.getSimpleName(),
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
        commandFactoryMap.put(AddValuesCommand.class.getSimpleName(),
                new AddValuesCommandFactory());
        commandFactoryMap.put(FoldCommand.class.getSimpleName(),
                new FoldCommandFactory());
        commandFactoryMap.put(SaveRowIDCommand.class.getSimpleName(),
                new SaveRowIDCommandFactory());
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
                new LoadAdditionalWorksheetRowsCommandFactory());
        commandFactoryMap.put(RefreshSVGAlignmentCommand.class.getSimpleName(), 
        		new RefreshSVGAligmentCommandFactory());
        commandFactoryMap.put(DeleteWorksheetCommand.class.getSimpleName(), 
        		new DeleteWorksheetCommandFactory());
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
                try {
                    JSONInputCommandFactory scf = (JSONInputCommandFactory) cf;
                    return scf.createCommand(new JSONArray(request.getParameter("newInfo")), workspace);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
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
