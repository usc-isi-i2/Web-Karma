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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.imp.rdf.UnionImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;



public class ImportUnionResultCommand extends Command {

	private File inputJsonFile;
	private static Logger logger = LoggerFactory.getLogger(ImportJSONFileCommand.class);
	
	protected ImportUnionResultCommand(String id, File uploadedFile) {
		super(id);
		inputJsonFile = uploadedFile;
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCommandName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		// TODO Auto-generated method stub
		
		Workspace ws = vWorkspace.getWorkspace();
		UpdateContainer c = new UpdateContainer();
		try {

			UnionImport uim=new UnionImport("union", ws, inputJsonFile);
			Worksheet wsht=uim.generateWorksheet();
			
			
//			Worksheet wsht = imp.generateWorksheet();
			vWorkspace.addAllWorksheets();
			
			c.add(new WorksheetListUpdate(vWorkspace.getVWorksheetList()));
			VWorksheet vw = vWorkspace.getVWorksheet(wsht.getId());
			vw.update(c);
		} catch (Exception e) {
			logger.error("Error occured while generating worksheet from JSON!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while importing JSON File."));
		} 
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace)  {
        return null;
	}

}
