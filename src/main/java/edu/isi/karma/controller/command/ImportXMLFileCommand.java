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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class ImportXMLFileCommand extends Command {
	private File xmlFile;

	private static Logger logger = LoggerFactory.getLogger(ImportXMLFileCommand.class);
	
	protected ImportXMLFileCommand(String id, File uploadedFile) {
		super(id);
		this.xmlFile = uploadedFile;
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
			return xmlFile.getName() + " imported";
		} else {
			return "";
		}
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Workspace ws = vWorkspace.getWorkspace();
		UpdateContainer c = new UpdateContainer();
		try {
			String fileContents = FileUtil.readFileContentsToString(xmlFile);
//			System.out.println(fileContents);
			// Converting the XML to JSON
			JSONObject json = XML.toJSONObject(fileContents);
//			System.out.println(json.toString(4));
			JsonImport imp = new JsonImport(json, xmlFile.getName(), ws);
			
			Worksheet wsht = imp.generateWorksheet();
			vWorkspace.addAllWorksheets();
			
			c.add(new WorksheetListUpdate(vWorkspace.getVWorksheetList()));
			VWorksheet vw = vWorkspace.getVWorksheet(wsht.getId());
			vw.update(c);
		} catch (FileNotFoundException e) {
			logger.error("File Not Found", e);
		} catch (JSONException e) {
			logger.error("JSON Exception!", e);
		} catch (IOException e) {
			logger.error("I/O Exception occured while reading file contents!", e);
		}
		
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
