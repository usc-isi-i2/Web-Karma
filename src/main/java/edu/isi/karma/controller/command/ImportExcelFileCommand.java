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
package edu.isi.karma.controller.command;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.excel.ToCSV;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ImportExcelFileCommand extends Command {
	private final File excelFile;

	// Logger object
	private static Logger logger = LoggerFactory
			.getLogger(ImportExcelFileCommand.class.getSimpleName());

	protected ImportExcelFileCommand(String id, File excelFile,
			VWorkspace vWorkspace) {
		super(id);
		this.excelFile = excelFile;
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
		return excelFile.getName();
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Workspace ws = vWorkspace.getWorkspace();
		UpdateContainer c = new UpdateContainer();

		// Convert the Excel file to a CSV file.
		ToCSV csvConverter = new ToCSV();
		try {
			csvConverter.convertExcelToCSV(excelFile.getAbsolutePath(),
					ServletContextParameterMap.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + "publish/CSV");
		} catch (Exception e) {
			String message = "Error occured while converting the Excel file to CSV file.";
			logger.error(message, e);
			return new UpdateContainer(new ErrorUpdate(message));
		}

		List<File> csvFiles = csvConverter.getCsvFiles();

		// Each sheet is written to a separate CSV file
		if (csvFiles.size() != 0) {
			for(File csvFile:csvFiles) {
				CSVFileImport imp = new CSVFileImport(1, 2, ',', '"', csvFile,
						ws.getFactory(), ws);
				try {
					Worksheet wsht = imp.generateWorksheet();
					vWorkspace.addAllWorksheets();
					c.add(new WorksheetListUpdate(vWorkspace.getVWorksheetList()));
					VWorksheet vw = vWorkspace.getVWorksheet(wsht.getId());
					vw.update(c);
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
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// Not required
		return null;
	}

}
