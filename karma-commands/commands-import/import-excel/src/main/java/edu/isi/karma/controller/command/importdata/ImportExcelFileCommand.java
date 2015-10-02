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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.imp.excel.ToCSV;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ImportExcelFileCommand extends ImportFileCommand implements IPreviewable {
	
    private static Logger logger = LoggerFactory
            .getLogger(ImportExcelFileCommand.class);

    protected ImportExcelFileCommand(String id, String model, File excelFile) {
        super(id, model, excelFile);
    }

    protected ImportExcelFileCommand(String id, String model, String revisedId, File uploadedFile) {
        super(id, model, revisedId, uploadedFile);
    }

    @Override
    public String getTitle() {
        return "Import Excel File";
    }

    @Override
    public String getDescription() {
        return getFile().getName();
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
    	final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
        UpdateContainer c = new UpdateContainer();

        // Convert the Excel file to a CSV file.
        ToCSV csvConverter = new ToCSV();
        try {
            csvConverter.convertExcelToCSV(getFile().getAbsolutePath(),
            		contextParameters.getParameterValue(ContextParameter.CSV_PUBLISH_DIR));
        } catch (Exception e) {
            String message = "Error occured while converting the Excel file to CSV file.";
            logger.error(message, e);
            return new UpdateContainer(new ErrorUpdate(message));
        }

        List<File> csvFiles = csvConverter.getCsvFiles();

        // Each sheet is written to a separate CSV file
        if (!csvFiles.isEmpty()) {
            for (File csvFile : csvFiles) {


                try {
                    Import imp = new CSVFileImport(1, 2, ',', '"', encoding, maxNumLines, 
                    		csvFile, workspace, null);
                    Worksheet wsht = imp.generateWorksheet();

                    if (hasRevisionId()) {
                        Worksheet revisedWorksheet = workspace.getWorksheet(getRevisionId());
                        wsht.setRevisedWorksheet(revisedWorksheet);
                    }

                    c.add(new WorksheetListUpdate());
                    c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId(), SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
                } catch (Exception e) {
                    logger.error("Error occured while importing CSV file.", e);
                    return new UpdateContainer(new ErrorUpdate(
                            "Error occured while importing CSV File."));
                }
            }
        }
        return c;
    }

    @Override
    protected Import createImport(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    protected Import createImport(Workspace workspace, int sampleSize) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
