/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.controller.command.importdata;

import java.io.IOException;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

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

    public ImportCommand(String id, String model) {
        super(id, model);
        this.revisionId = null;
        addTag(CommandTag.Import);
    }

    public ImportCommand(String id, String model, String revisionId) {
        super(id, model);
        this.revisionId = revisionId;
        addTag(CommandTag.Import);
    }

    public String getRevisionId() {
        return revisionId;
    }

    public boolean hasRevisionId() {
        return revisionId != null;
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        UpdateContainer c = new UpdateContainer();

        try {
        	Import imp = createImport(workspace);
            Worksheet wsht = imp.generateWorksheet();
            if (hasRevisionId()) {
                Worksheet revisedWorksheet = workspace.getWorksheet(getRevisionId());
                wsht.setRevisedWorksheet(revisedWorksheet);  
            }
            c.add(new WorksheetListUpdate());
            c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId(), SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
        } catch (JSONException | IOException | KarmaException | NullPointerException | ClassNotFoundException e) {
            logger.error("Error occured while generating worksheet from " + getTitle() + "!", e);
            return new UpdateContainer(new ErrorUpdate(
                    "Error occured during import: " + e.getMessage()));
        }

        return c;
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
