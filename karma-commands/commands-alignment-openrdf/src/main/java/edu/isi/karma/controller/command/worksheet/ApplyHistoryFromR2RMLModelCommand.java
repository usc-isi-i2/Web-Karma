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

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.rep.Workspace;
import org.json.JSONArray;
import org.json.JSONException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.kr2rml.KR2RMLMapping;
import edu.isi.karma.kr2rml.KR2RMLVersion;
import edu.isi.karma.kr2rml.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.KarmaException;

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
		UpdateContainer c = new UpdateContainer();
		UpdateContainer rwu = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		if(rwu != null)
		{
			c.append(rwu);
		}
		
		try {
			JSONArray historyJson  = extractHistoryFromModel(workspace, c);
			if (null == historyJson || historyJson.length() == 0) {
				return new UpdateContainer(new ErrorUpdate("No history found in R2RML Model!"));
			}
			WorksheetCommandHistoryExecutor histExecutor = new WorksheetCommandHistoryExecutor(
					worksheetId, workspace);
			UpdateContainer hc = histExecutor.executeAllCommands(historyJson);
			if(hc != null)
				c.append(hc);
		} catch (Exception e) {
			String msg = "Error occured while applying history!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		
		// Add worksheet updates that could have resulted out of the transformation commands
		
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));		
		c.add(new InfoUpdate("Model successfully applied!"));
		return c;
	}

	private JSONArray extractHistoryFromModel(Workspace workspace, UpdateContainer uc) 
			throws RepositoryException, RDFParseException, IOException, JSONException, KarmaException {
		
		Worksheet ws = workspace.getFactory().getWorksheet(worksheetId);
		R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(ws.getTitle(), r2rmlModelFile.toURI().toURL());
		WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(id);
		KR2RMLMapping mapping = parser.parse();
		KR2RMLVersion version = mapping.getVersion();
		if(version.compareTo(KR2RMLVersion.current) < 0)
		{
			uc.add(new InfoUpdate("Model version is " + version.toString() + ".  Current version is " + KR2RMLVersion.current.toString() + ".  Please publish it again."));
		}
		return mapping.getWorksheetHistory();
		
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
