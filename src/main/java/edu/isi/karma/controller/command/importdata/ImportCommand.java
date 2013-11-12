/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.controller.command.importdata;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.KR2RMLModelGenerator;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This abstract class in an interface to all Commands that Import data
 *
 *
 * @author mielvandersande
 */
public abstract class ImportCommand extends Command {

    private static Logger logger = LoggerFactory
            .getLogger(ImportCommand.class.getSimpleName());
    // Id of the revised worksheet, or null if no revision is present
    private String revisionId;

    public ImportCommand(String id) {
        super(id);
        this.revisionId = null;
    }

    public ImportCommand(String id, String revisionId) {
        super(id);
        this.revisionId = revisionId;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public boolean hasRevisionId() {
        return revisionId != null;
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        Import imp = createImport(workspace);

        UpdateContainer c = new UpdateContainer();

        try {
            Worksheet wsht = imp.generateWorksheet();

            c.add(new WorksheetListUpdate());
            
            
            if (hasRevisionId()) {
                c.append(handleRevision(workspace, wsht));
            }
            
            c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId()));
        } catch (JSONException | IOException | KarmaException | ClassNotFoundException | SQLException e) {
            logger.error("Error occured while generating worksheet from " + getTitle() + "!", e);
            return new UpdateContainer(new ErrorUpdate(
                    "Error occured while importing JSON File."));
        }

        return c;
    }

    private UpdateContainer handleRevision(Workspace workspace, Worksheet wsht) {

        try {
            Worksheet revisedWorksheet = workspace.getWorksheet(getRevisionId());
            wsht.setRevisedWorksheet(revisedWorksheet);

            KR2RMLModelGenerator modelGenerator = new KR2RMLModelGenerator();

            KR2RMLMappingGenerator mappingGen = modelGenerator.generateModel(workspace, revisedWorksheet);
            // Write the model
            StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);
            modelGenerator.writeModel(workspace, mappingGen, revisedWorksheet, writer);

            String modelstring = sw.toString();
            InputStream is = new ByteArrayInputStream(modelstring.getBytes("UTF-8"));


            WorksheetCommandHistoryReader histReader = new WorksheetCommandHistoryReader(
                    revisedWorksheet.getId(), workspace);
            String historyStr = modelGenerator.extractHistoryFromModel(is);

            if (historyStr.isEmpty()) {
                return new UpdateContainer(new ErrorUpdate("No history found in R2RML Model!"));
            }
            JSONArray historyJson = new JSONArray(historyStr);
            histReader.readAndExecuteAllCommands(historyJson);

            //Alignment alignment = AlignmentManager.Instance().getAlignmentOrCreateIt(workspace.getId(), wsht.getId(), workspace.getOntologyManager());
            Alignment alignment = AlignmentManager.Instance().getAlignment(workspace.getId(), revisedWorksheet.getId()).getAlignmentClone();
            SemanticTypeUtil.computeSemanticTypesSuggestion(wsht, workspace.getCrfModelHandler(), workspace.getOntologyManager(), alignment);

            //UpdateContainer c = new UpdateContainer();
  
            UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(wsht.getId());
            c.append(WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(wsht.getId(), workspace, alignment));
            //UpdateContainer c = WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(wsht.getId(), workspace, alignment);
            c.add(new InfoUpdate("Model successfully applied!"));

            return c;
        } catch (Exception ex) {
            String msg = "Error occured while copying history from revised worksheet!";
            logger.error(msg, ex);
            return new UpdateContainer(new ErrorUpdate(msg));
        }

    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        return null;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.notUndoable;
    }

    protected abstract Import createImport(Workspace workspace);
}
