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

package edu.isi.karma.controller.command.worksheet;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.kr2rml.KR2RMLModelGenerator;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.Workspace;
import java.io.FileInputStream;

public class ApplyHistoryFromR2RMLModelCommand extends WorksheetCommand {
	private final File r2rmlModelFile;
	private final String worksheetId;
	
	private static Logger logger = LoggerFactory.getLogger(ApplyHistoryFromR2RMLModelCommand.class);

	protected ApplyHistoryFromR2RMLModelCommand(String id, File uploadedFile, String worksheetId) {
		super(id, worksheetId);
		this.r2rmlModelFile = uploadedFile;
		this.worksheetId = worksheetId;
	}

	@Override
	public String getCommandName() {
		return ApplyHistoryFromR2RMLModelCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Apply History";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		WorksheetCommandHistoryReader histReader = new WorksheetCommandHistoryReader(
				worksheetId, workspace);
                
                KR2RMLModelGenerator modelGenerator = new KR2RMLModelGenerator();
		try {
                        FileInputStream fis = new FileInputStream(r2rmlModelFile);
			String historyStr = modelGenerator.extractHistoryFromModel(fis);
			if (historyStr.isEmpty()) {
				return new UpdateContainer(new ErrorUpdate("No history found in R2RML Model!"));
			}
			JSONArray historyJson = new JSONArray(historyStr);
			histReader.readAndExecuteAllCommands(historyJson);
		} catch (Exception e) {
			String msg = "Error occured while applying history!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		
		// Add worksheet updates that could have resulted out of the transformation commands
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));		
		c.add(new InfoUpdate("Model successfully applied!"));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
