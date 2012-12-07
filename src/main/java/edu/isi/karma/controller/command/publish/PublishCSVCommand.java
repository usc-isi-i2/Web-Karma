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
package edu.isi.karma.controller.command.publish;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.csv.CSVFileExport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorkspace;

public class PublishCSVCommand extends Command {
	private final String vWorksheetId;

	private enum JsonKeys {
		updateType, fileUrl, vWorksheetId
	}

	private static Logger logger = LoggerFactory
			.getLogger(PublishCSVCommand.class);

	protected PublishCSVCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Export CSV file";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory()
				.getVWorksheet(vWorksheetId).getWorksheet();

		CSVFileExport csvFileExport = new CSVFileExport(worksheet);

		try {
			final String fileName = csvFileExport.publishCSV();

			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(),
								"PublishCSVUpdate");
						outputObject.put(JsonKeys.fileUrl.name(),
								fileName);
						outputObject.put(JsonKeys.vWorksheetId.name(),
								vWorksheetId);
						pw.println(outputObject.toString(4));
						
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
		} catch (FileNotFoundException e) {
			logger.error("CSV folder not found!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occurred while exporting CSV file!"));
		}
		
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
