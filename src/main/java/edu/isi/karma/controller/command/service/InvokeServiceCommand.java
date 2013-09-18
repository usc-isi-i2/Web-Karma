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

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.rits.cloning.Cloner;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.ReplaceWorksheetUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.MetadataContainer;
import edu.isi.karma.rep.sources.InvocationManager;
import edu.isi.karma.rep.sources.Table;
import edu.isi.karma.rep.sources.WebService;
import edu.isi.karma.webserver.KarmaException;

/**
 * @author taheriyan
 * 
 */
public class InvokeServiceCommand extends WorksheetCommand {

	static Logger logger = Logger.getLogger(InvokeServiceCommand.class);
	private final String hNodeId;
	
	private Worksheet worksheetBeforeInvocation = null;

	InvokeServiceCommand(String id, String worksheetId, String hNodeId) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer c = new UpdateContainer();
		Worksheet wk = workspace.getWorksheet(worksheetId);
		
		// Clone the worksheet just before the invocation
		Cloner cloner = new Cloner();
		this.worksheetBeforeInvocation = cloner.deepClone(wk);
		
		List<String> requestURLStrings = new ArrayList<String>();
		List<Row> rows = wk.getDataTable().getRows(0, wk.getDataTable().getNumRows());
		if (rows == null || rows.size() == 0) {
			logger.error("Data table does not have any row.");
			return new UpdateContainer(new ErrorUpdate("Data table does not have any row."));	
		}
		
		List<String> requestIds = new ArrayList<String>();
		for (int i = 0; i < rows.size(); i++) {
			requestIds.add(rows.get(i).getId());
			requestURLStrings.add(rows.get(i).getNode(hNodeId).getValue().asString());
		}

		InvocationManager invocatioManager;
		try {
			invocatioManager = new InvocationManager(getUrlColumnName(wk), requestIds, requestURLStrings);
			logger.info("Requesting data with includeURL=" + false + ",includeInput=" + true + ",includeOutput=" + true);
			
			// This generate a flat table of the json results
			Table serviceTable = invocatioManager.getServiceData(false, true, true);
			ServiceTableUtil.populateWorksheet(serviceTable, wk, workspace.getFactory());
			
			// FIXME
			String json = invocatioManager.getServiceJson(true);
//			new JsonImport(json, wk, ws.getFactory());
//			System.out.println(json);


			
			WebService service = invocatioManager.getInitialServiceModel(null);
			MetadataContainer metaData = wk.getMetadataContainer();
			if (metaData == null) {
				metaData = new MetadataContainer();
				wk.setMetadataContainer(metaData);
			}
			metaData.setService(service);
			logger.info("Service added to the Worksheet.");

		} catch (MalformedURLException e) {
			logger.error("Malformed service request URL.");
			return new UpdateContainer(new ErrorUpdate("Malformed service request URL."));
		} catch (KarmaException e) {
			logger.error(e.getMessage());
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
		
		// Create new vWorksheet using the new header order
		List<HNodePath> columnPaths = new ArrayList<HNodePath>();
		for (HNode node : wk.getHeaders().getSortedHNodes()) {
			HNodePath path = new HNodePath(node);
			columnPaths.add(path);
		}

		this.generateRegenerateWorksheetUpdates(c);
		
		return c;
	}
	
	private String getUrlColumnName(Worksheet wk) {
		// TODO
		return null;
	}
	
	public Worksheet generateWorksheet(Workspace workspace, String title) throws KarmaException, IOException {

		if (workspace == null)
			throw new KarmaException("Workspace is null.");
		
		Worksheet worksheet = workspace.getFactory().createWorksheet(title, workspace);
		
		return worksheet;
	}
	
	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		UpdateContainer c = new UpdateContainer();
		
		// Create new vWorksheet using the new header order
		List<HNodePath> columnPaths = new ArrayList<HNodePath>();
		for (HNode node : worksheetBeforeInvocation.getHeaders().getSortedHNodes()) {
			HNodePath path = new HNodePath(node);
			columnPaths.add(path);
		}
		workspace.getFactory().replaceWorksheet(this.worksheetId, this.worksheetBeforeInvocation);
		c.add(new ReplaceWorksheetUpdate(this.worksheetId, this.worksheetBeforeInvocation));
		WorksheetUpdateFactory.update(c, worksheetId);
	
		return c;	
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
