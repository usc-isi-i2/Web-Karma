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

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.ReplaceWorksheetUpdate;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.DefaultLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.metadata.MetadataContainer;
import edu.isi.karma.rep.sources.InvocationManager;
import edu.isi.karma.rep.sources.Table;
import edu.isi.karma.rep.sources.WebService;
import edu.isi.karma.webserver.KarmaException;

/**
 * @author taheriyan
 * 
 */
public class InvokeServiceCommand extends WorksheetSelectionCommand {

	private static Logger logger = LoggerFactory.getLogger(InvokeServiceCommand.class);
	private Alignment initialAlignment = null;
	private DirectedWeightedMultigraph<Node, DefaultLink> initialGraph = null;
	private final String hNodeId;
	
	private Worksheet worksheetBeforeInvocation = null;

	InvokeServiceCommand(String id, String model, String worksheetId, String hNodeId, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.hNodeId = hNodeId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		Worksheet wk = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(wk);
		String encoding = wk.getEncoding();
		// Clone the worksheet just before the invocation
		Cloner cloner = new Cloner();
		this.worksheetBeforeInvocation = cloner.deepClone(wk);
		
		workspace.getOntologyManager();
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			AlignmentManager.Instance().createAlignment(workspace.getId(), worksheetId, workspace.getOntologyManager());
		}
		
		if (initialAlignment == null) {
			initialAlignment = alignment.getAlignmentClone();
			initialGraph = (DirectedWeightedMultigraph<Node, DefaultLink>)alignment.getGraph().clone();
		}
		
		List<String> requestURLStrings = new ArrayList<>();
		List<Row> rows = wk.getDataTable().getRows(0, wk.getDataTable().getNumRows(), selection);
		if (rows == null || rows.isEmpty()) {
			logger.error("Data table does not have any row.");
			return new UpdateContainer(new ErrorUpdate("Data table does not have any row."));	
		}
		
		List<String> requestIds = new ArrayList<>();
		for (int i = 0; i < rows.size(); i++) {
			requestIds.add(rows.get(i).getId());
			requestURLStrings.add(rows.get(i).getNode(hNodeId).getValue().asString());
		}

		InvocationManager invocatioManager;
		try {
			invocatioManager = new InvocationManager(getUrlColumnName(wk), requestIds, requestURLStrings, encoding);
			logger.info("Requesting data with includeURL=" + false + ",includeInput=" + true + ",includeOutput=" + true);
			
			// This generate a flat table of the json results
			Table serviceTable = invocatioManager.getServiceData(false, true, true);
			ServiceTableUtil.populateWorksheet(serviceTable, wk, workspace.getFactory(), selection);
			
			// FIXME
//			String json = invocatioManager.getServiceJson(true);
			invocatioManager.getServiceJson(true);
//			new JsonImport(json, wk, ws.getFactory());
//			logger.debug(json);


			
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
		List<HNodePath> columnPaths = new ArrayList<>();
		for (HNode node : wk.getHeaders().getSortedHNodes()) {
			HNodePath path = new HNodePath(node);
			columnPaths.add(path);
		}

		
		alignment.updateColumnNodesInAlignment(wk);
		//alignment = AlignmentManager.Instance().getAlignmentOrCreateIt(workspace.getId(), wk.getId(), ontMgr);
		//AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		UpdateContainer c = new UpdateContainer();
		try {
			// Add the visualization update
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace),workspace.getContextId()));
			c.add(new SemanticTypesUpdate(wk, worksheetId));
			c.add(new AlignmentSVGVisualizationUpdate(worksheetId));
		} catch (Exception e) {
			logger.error("Error occured while populating the worksheet with service data!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while populating the worksheet with service data!"));
		}

		return c;
		
	}
	
	private String getUrlColumnName(Worksheet wk) {
		// TODO
		return null;
	}
	
	public Worksheet generateWorksheet(Workspace workspace, String title, String encoding) throws KarmaException, IOException {

		if (workspace == null)
			throw new KarmaException("Workspace is null.");
		
		Worksheet worksheet = workspace.getFactory().createWorksheet(title, workspace, encoding);
		
		return worksheet;
	}
	
	@Override
	public UpdateContainer undoIt(Workspace workspace) {

		Worksheet wk = workspace.getWorksheet(worksheetId);

		UpdateContainer c = new UpdateContainer();
		
		// Create new vWorksheet using the new header order
//		List<HNodePath> columnPaths = new ArrayList<HNodePath>();
//		for (HNode node : worksheetBeforeInvocation.getHeaders().getSortedHNodes()) {
//			HNodePath path = new HNodePath(node);
//			columnPaths.add(path);
//		}
		
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(workspace.getId(), worksheetId);
		Alignment alignment = initialAlignment;
		alignment.setGraph(initialGraph);
		if(!this.isExecutedInBatch())
			alignment.align();
		AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		try {
			// Add the visualization update
			workspace.getFactory().replaceWorksheet(worksheetId, worksheetBeforeInvocation);
			c.add(new ReplaceWorksheetUpdate(worksheetId, worksheetBeforeInvocation));
			c.add(new AlignmentSVGVisualizationUpdate(worksheetId));
			c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace), workspace.getContextId()));
			c.add(new SemanticTypesUpdate(wk, worksheetId));
			c.append(this.computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		} catch (Exception e) {
			logger.error("Error occured while populating the worksheet with service data!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured while populating the worksheet with service data!"));
		}
		
//		workspace.getFactory().replaceWorksheet(worksheetId, worksheetBeforeInvocation);
//		c.add(new ReplaceWorksheetUpdate(worksheetId, worksheetBeforeInvocation));
//		c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId));
//		//c.add(new SemanticTypesUpdate(wk, worksheetId, alignment));
//		c.add(new AlignmentSVGVisualizationUpdate(worksheetId, alignment));

		
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
