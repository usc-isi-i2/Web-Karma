package edu.isi.karma.controller.command.alignment;

import edu.isi.karma.controller.command.*;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.CommandHistoryUtil;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Frank on 9/3/15.
 */
public class ExportOrDeleteHistoryCommand extends WorksheetSelectionCommand {
    private static Logger logger = LoggerFactory.getLogger(ExportOrDeleteHistoryCommand.class);

    public enum JsonKeys {
        updateType, fileUrl, worksheetId
    }
    private boolean isDelete;
    private final Set<String> commandSet = new HashSet<>();
    private JSONArray historyCommandsBackup;
    private String volatileWorksheetId;
    public ExportOrDeleteHistoryCommand(String id, String model,
                                        String worksheetId, String selectionId,
                                        String commandSet, boolean isDelete) {
        super(id, model, worksheetId, selectionId);
        this.commandSet.addAll(Arrays.asList(commandSet.split(",")));
        this.isDelete = isDelete;
        volatileWorksheetId = worksheetId;
        historyCommandsBackup = new JSONArray();
    }

    @Override
    public String getCommandName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return isDelete ? "Delete History" : "Export History";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public CommandType getCommandType() {
        if (isDelete) {
            return CommandType.undoable;
        }
        else {
            return CommandType.notUndoable;
        }
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        List<Command> commands = workspace.getCommandHistory().getCommandsFromWorksheetId(volatileWorksheetId);
        for (Command command : commands) {
            historyCommandsBackup.put(workspace.getCommandHistory().getCommandJSON(workspace, command));
        }
        CommandHistoryUtil historyUtil = new CommandHistoryUtil(commands, workspace, volatileWorksheetId);
        if (isDelete) {
            historyUtil.removeCommands(commandSet);
            UpdateContainer updateContainer = historyUtil.replayHistory();
            this.volatileWorksheetId = historyUtil.getWorksheetId();
            return updateContainer;
        }
        Worksheet worksheet = workspace.getWorksheet(worksheetId);
        final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
        historyUtil.retainCommands(commandSet);
        JSONArray commandsArray = historyUtil.getCommandsJSON();
        R2RMLAlignmentFileSaver fileSaver = new R2RMLAlignmentFileSaver(workspace);
        String graphLabel = worksheet.getMetadataContainer().getWorksheetProperties().
                getPropertyValue(WorksheetProperties.Property.graphLabel);

        if (graphLabel == null || graphLabel.isEmpty()) {
            worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
                    WorksheetProperties.Property.graphLabel, worksheet.getTitle());
            graphLabel = worksheet.getTitle();
            worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
                    WorksheetProperties.Property.graphName, WorksheetProperties.createDefaultGraphName(graphLabel));
        }
        final String modelFileName = graphLabel + "-history-model.ttl";
        final String modelFileLocalPath = contextParameters.getParameterValue(
                ServletContextParameterMap.ContextParameter.R2RML_PUBLISH_DIR) +  modelFileName;
        try {
            fileSaver.saveAlignment(AlignmentManager.Instance().getAlignment(AlignmentManager.
                    Instance().constructAlignmentId(workspace.getId(), worksheetId)), commandsArray, modelFileLocalPath, true);
        } catch (Exception e) {
            logger.error("Error occurred while generating model!", e);
        }
        UpdateContainer updateContainer = new UpdateContainer();
        updateContainer.add(new AbstractUpdate() {
            public void generateJson(String prefix, PrintWriter pw,
                                     VWorkspace vWorkspace) {
                JSONObject outputObject = new JSONObject();
                try {
                    outputObject.put(JsonKeys.updateType.name(), "PublishR2RMLUpdate");

                    outputObject.put(JsonKeys.fileUrl.name(), contextParameters.getParameterValue(
                            ServletContextParameterMap.ContextParameter.R2RML_PUBLISH_RELATIVE_DIR) + modelFileName);
                    outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
                    pw.println(outputObject.toString());
                } catch (JSONException e) {
                    logger.error("Error occurred while generating JSON!", e);
                }
            }
        });
        return updateContainer;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        CommandHistoryUtil historyUtil = new CommandHistoryUtil(Collections.EMPTY_LIST, workspace, volatileWorksheetId);
        UpdateContainer updateContainer = historyUtil.replayHistory(historyCommandsBackup);
        this.volatileWorksheetId = historyUtil.getWorksheetId();
        return updateContainer;
    }

    @Override
    public String getWorksheetId() {
        return volatileWorksheetId;
    }
}
