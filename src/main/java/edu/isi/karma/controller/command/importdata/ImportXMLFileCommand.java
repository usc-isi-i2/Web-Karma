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
package edu.isi.karma.controller.command.importdata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.XMLImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;
import java.sql.SQLException;

public class ImportXMLFileCommand extends ImportFileCommand {

    private static Logger logger = LoggerFactory.getLogger(ImportXMLFileCommand.class);

    protected ImportXMLFileCommand(String id, File uploadedFile) {
        super(id, uploadedFile);
    }

    protected ImportXMLFileCommand(String id, String revisedId, File uploadedFile) {
        super(id, revisedId, uploadedFile);
    }

    @Override
    public String getCommandName() {
        return "ImportXMLFileCommand";
    }

    @Override
    public String getTitle() {
        return "Import XML File";
    }

    @Override
    public String getDescription() {
        if (isExecuted()) {
            return getFile().getName() + " imported";
        }
        return "";
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {

        UpdateContainer c = new UpdateContainer();
        try {
            Import imp = new XMLImport(getFile(), getFile().getName(), workspace);
            
            Worksheet wsht = imp.generateWorksheet();
            
            if (hasRevisionId()) {
                Worksheet revisedWorksheet = workspace.getWorksheet(getRevisionId());
                wsht.setRevisedWorksheet(revisedWorksheet);
            } 

            c.add(new WorksheetListUpdate());
            c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId()));
        } catch (ClassNotFoundException e) {
            logger.error("Class Not Found", e);
        } catch (SQLException e) {
            logger.error("SQL Error", e);
        } catch (FileNotFoundException e) {
            logger.error("File Not Found", e);
        } catch (JSONException e) {
            logger.error("JSON Exception!", e);
        } catch (IOException e) {
            logger.error("I/O Exception occured while reading file contents!", e);
        } catch (KarmaException e) {
            logger.error("Karma Exception!", e);
        }

        return c;
    }
}
