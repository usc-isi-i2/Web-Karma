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

import java.util.List;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class AddNewColumnCommand extends WorksheetCommand {
	private final String hNodeId;
	private final String vWorksheetId;

	protected AddNewColumnCommand(String id, String worksheetId, String hNodeId, String vWorksheetId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return AddNewColumnCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add New Column";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = vWorkspace.getWorkspace().getWorksheet(worksheetId);
		HTable headers = worksheet.getHeaders();
		String existingColumnName = headers.getHNode(hNodeId).getColumnName();
		
		worksheet.getHeaders().addNewHNodeAfter(hNodeId, vWorkspace.getRepFactory(), existingColumnName+"_copy", worksheet);
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet, columnPaths, vWorkspace);
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		vw.update(c);
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
