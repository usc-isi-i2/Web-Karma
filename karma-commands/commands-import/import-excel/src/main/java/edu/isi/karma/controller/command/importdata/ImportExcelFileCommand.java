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

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.*;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.imp.excel.ToCSV;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import java.io.File;
import java.util.List;

public class ImportExcelFileCommand extends ImportFileCommand implements IPreviewable {
	private String encoding = null;
    private int maxNumLines = 1000;
    
    // Logger object
    private static Logger logger = LoggerFactory
            .getLogger(ImportExcelFileCommand.class.getSimpleName());

    protected ImportExcelFileCommand(String id, File excelFile) {
        super(id, excelFile);
        this.encoding = EncodingDetector.detect(excelFile);
    }

    protected ImportExcelFileCommand(String id, String revisedId, File uploadedFile) {
        super(id, revisedId, uploadedFile);
        this.encoding = EncodingDetector.detect(uploadedFile);
    }

    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
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
        UpdateContainer c = new UpdateContainer();

        // Convert the Excel file to a CSV file.
        ToCSV csvConverter = new ToCSV();
        try {
            csvConverter.convertExcelToCSV(getFile().getAbsolutePath(),
            		ServletContextParameterMap.getParameterValue(ContextParameter.CSV_PUBLISH_DIR));
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
                	//this.encoding = EncodingDetector.detect(csvFile);
                    Import imp = new CSVFileImport(1, 2, ',', '"', encoding, maxNumLines, csvFile,
                            workspace);
                    Worksheet wsht = imp.generateWorksheet();

                    if (hasRevisionId()) {
                        Worksheet revisedWorksheet = workspace.getWorksheet(getRevisionId());
                        wsht.setRevisedWorksheet(revisedWorksheet);
                    }

                    c.add(new WorksheetListUpdate());
                    c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId(), SuperSelectionManager.DEFAULT_SELECTION));
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
    
    public void setEncoding(String encoding) {
    	this.encoding = encoding;
    }
    
    public void setMaxNumLines(int lines) {
    	this.maxNumLines = lines;
    }
    
    @Override
    public UpdateContainer handleUserActions(HttpServletRequest request) {
       
        String strEncoding = request.getParameter("encoding");
        if(strEncoding == null || strEncoding == "") {
        	try {
        		strEncoding = EncodingDetector.detect(getFile());
        	} catch(Exception e) {
        		strEncoding = EncodingDetector.DEFAULT_ENCODING;
        	}
        }
        setEncoding(strEncoding);
        
        String maxNumLines = request.getParameter("maxNumLines");
        if(maxNumLines != null && maxNumLines != "") {
        	try {
                int num = Integer.parseInt(maxNumLines);
                setMaxNumLines(num);
            } catch (Throwable t) {
                logger.error("Wrong user input for Data Number of Lines to import");
                return null;
            }
        }
        /**
         * Send response based on the interaction type *
         */
        UpdateContainer c = null;
        ImportFileInteractionType type = ImportFileInteractionType.valueOf(request
                .getParameter("interactionType"));
        switch (type) {
            case generatePreview: {
                try {

                    c = showPreview();
                } catch (CommandException e) {
                    logger.error(
                            "Error occured while creating utput JSON for JSON Import",
                            e);
                }
                return c;
            }
            case importTable:
                return c;
        }
        return c;
    }

	@Override
	public UpdateContainer showPreview() throws CommandException {
		
        UpdateContainer c = new UpdateContainer();
        c.add(new ImportPropertiesUpdate(getFile(), encoding, maxNumLines, id));
        return c;
	   
	}
}
