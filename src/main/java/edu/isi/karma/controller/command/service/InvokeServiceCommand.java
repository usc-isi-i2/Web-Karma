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

package edu.isi.karma.controller.command.service;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.service.ServiceManager;
import edu.isi.karma.service.Table;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

/**
 * @author taheriyan
 * 
 */
public class InvokeServiceCommand extends WorksheetCommand {

	static Logger logger = Logger.getLogger(InvokeServiceCommand.class);
	private final String hNodeId;
	private final String vWorksheetId;

	InvokeServiceCommand(String id, String worksheetId, String vWorksheetId, String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		Workspace ws = vWorkspace.getWorkspace();
		Worksheet wk = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		
		List<String> requestURLStrings = new ArrayList<String>();
		List<Row> rows = wk.getDataTable().getRows(0, wk.getDataTable().getNumRows());
		if (rows == null || rows.size() == 0) {
			logger.error("Data table does not have any row.");
			return new UpdateContainer(new ErrorUpdate("Data table does not have any row."));	
		}
		
		for (int i = 0; i < rows.size(); i++) {
			requestURLStrings.add(rows.get(i).getNode(hNodeId).getValue().asString());
		}

		ServiceManager sm = new ServiceManager(requestURLStrings);
		Table result = null;
		
		try {
			result = sm.getResponse();
		} catch (MalformedURLException e) {
			logger.error("Malformed service request URL.");
			return new UpdateContainer(new ErrorUpdate("Malformed service request URL."));
		}
		
		new PopulateWorksheetFromTable(ws, wk, result).populate();
		
		// Create new vWorksheet using the new header order
		List<HNodePath> columnPaths = new ArrayList<HNodePath>();
		for (HNode node : wk.getHeaders().getSortedHNodes()) {
			HNodePath path = new HNodePath(node);
			columnPaths.add(path);
		}
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId,
				wk, columnPaths, vWorkspace);
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		vw.update(c);
		
		return c;
	}
	

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

	@Override
	public String getTitle() {
		return "Invoke Service";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

}
