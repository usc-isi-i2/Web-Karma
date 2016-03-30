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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.ReplaceWorksheetUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.model.serialization.WebServiceLoader;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.DataSource;
import edu.isi.karma.rep.sources.InvocationManager;
import edu.isi.karma.rep.sources.Table;
import edu.isi.karma.rep.sources.WebService;
import edu.isi.karma.webserver.KarmaException;

public class PopulateCommand extends WorksheetSelectionCommand{


	private Worksheet worksheetBeforeInvocation = null;

	// Logger object
	private static Logger logger = LoggerFactory
			.getLogger(PopulateCommand.class.getSimpleName());

	public PopulateCommand(String id, String model, String worksheetId, String selectionId) {
		super(id, model, worksheetId, selectionId);
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Populating Source";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		
		UpdateContainer c = new UpdateContainer();
		Worksheet wk = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(wk);
		// Clone the worksheet just before the invocation
		Cloner cloner = new Cloner();
		this.worksheetBeforeInvocation = cloner.deepClone(wk);

		AlignmentManager mgr = AlignmentManager.Instance();
		String alignmentId = mgr.constructAlignmentId(workspace.getId(), worksheetId);
		Alignment al = mgr.getAlignment(alignmentId);
		
//		/**
//		 * 
//		 */
//		// FIXME
//		String exportDir = "/Users/mohsen/Dropbox/Service Modeling/iswc2013-exp2/jgraph/";
//		try {
//			GraphUtil.serialize(al.getSteinerTree(), exportDir + wk.getTitle() + ".karma.final.jgraph");
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		if (true) return null;
		
		if (al == null) { 
			logger.error("The alignment model is null.");
			return new UpdateContainer(new ErrorUpdate(
				"Error occured while populating the source. The alignment model is null."));
		} 
		
		DirectedWeightedMultigraph<Node, LabeledLink> tree = al.getSteinerTree();
			
		if (tree == null) {
			logger.error("The alignment tree is null.");
			return new UpdateContainer(new ErrorUpdate(
				"Error occured while populating the source. The alignment model is null."));
		} 

		DataSource source = new DataSource(wk.getTitle(), tree);
		
		Map<WebService, Map<String, String>> servicesAndMappings = 
			WebServiceLoader.getInstance().getServicesWithInputContainedInModel(source.getModel(), null);
		
		if (servicesAndMappings == null) {
			logger.error("Cannot find any services to be invoked according to this source model.");
			return new UpdateContainer(new ErrorUpdate(
				"Error occured while populating the source. Cannot find any services to be invoked according to this source model."));
		}
		
		// For now, we just use the first service, 
		// later we can suggest the user a list of available services and user select among them
		WebService service = null;
		Iterator<WebService> itr = servicesAndMappings.keySet().iterator();
		if (itr != null && itr.hasNext()) {
			service = itr.next();
		}
		
		if (service == null) {
			logger.error("Cannot find any services to be invoked according to this source model.");
			return new UpdateContainer(new ErrorUpdate(
				"Error occured while populating the source. Cannot find any services to be invoked according to this source model."));
		}
		
		List<String> requestIds = new ArrayList<>();
		Map<String, String> serviceToSourceAttMapping =  servicesAndMappings.get(service);
		List<String> requestURLStrings = getUrlStrings(service, source, wk, serviceToSourceAttMapping, requestIds);
		if (requestURLStrings == null || requestURLStrings.isEmpty()) {
			logger.error("Data table does not have any row.");
			return new UpdateContainer(new ErrorUpdate("Data table does not have any row."));	
		}
		
		
		InvocationManager invocatioManager;
		try {
			String encoding = wk.getEncoding();
			invocatioManager = new InvocationManager(getUrlColumnName(wk), requestIds, requestURLStrings, encoding);
			logger.info("Requesting data with includeURL=" + true + ",includeInput=" + true + ",includeOutput=" + true);
			Table serviceTable = invocatioManager.getServiceData(false, false, true);
//			logger.debug(serviceTable.getPrintInfo());
			ServiceTableUtil.populateWorksheet(serviceTable, wk, workspace.getFactory(), selection);
			logger.info("The service " + service.getUri() + " has been invoked successfully.");


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
		
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace),workspace.getContextId()));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
				
		return c;
	}

	private String getUrlColumnName(Worksheet wk) {
		// TODO
		return null;
	}
	
	private List<String> getUrlStrings(WebService service, DataSource source, 
			Worksheet wk, Map<String, String> serviceToSourceAttMapping, 
			List<String> requestIds) {
		SuperSelection selection = getSuperSelection(wk);
		List<String> requestURLStrings = new ArrayList<>();
		List<Row> rows = wk.getDataTable().getRows(0, wk.getDataTable().getNumRows(), selection);
		if (rows == null || rows.isEmpty()) {
			logger.error("Data table does not have any row.");
			return null;	
		}
		
		Map<String, String> attIdToValue = null;
		for (int i = 0; i < rows.size(); i++) {
			attIdToValue = new HashMap<>();
			for (Map.Entry<String, String> stringStringEntry : serviceToSourceAttMapping.entrySet()) {
				String sourceAttId = stringStringEntry.getValue();
				Attribute sourceAtt = source.getAttribute(sourceAttId);
				if (sourceAtt == null) {
//					logger.debug("Cannot find the source attribute with the id " + sourceAttId);
					continue;
				}
				String hNodeId = sourceAtt.gethNodeId();
				if (hNodeId == null || hNodeId.trim().length() == 0) {
					logger.debug("The attribute with the id " + sourceAttId + " does not have a hNodeId.");
					continue;
				}
				
				String value = rows.get(i).getNode(hNodeId).getValue().asString().trim();
				
				attIdToValue.put(stringStringEntry.getKey(), value);
				
			}
			String urlString = service.getPopulatedAddress(attIdToValue, null);
			
			//FIXME
			urlString = urlString.replaceAll("\\{p3\\}", "karma");
			
			requestIds.add(rows.get(i).getId());
			requestURLStrings.add(urlString);
			
			logger.debug("The invocation url " + urlString + " has been added to the invocation list.");
		}
		
		
		return requestURLStrings;
	}
	
	@Override
	public UpdateContainer undoIt(Workspace workspace) {

		UpdateContainer c = new UpdateContainer();
		
		// Create new vWorksheet using the new header order
		List<HNodePath> columnPaths = new ArrayList<>();
		for (HNode node : worksheetBeforeInvocation.getHeaders().getSortedHNodes()) {
			HNodePath path = new HNodePath(node);
			columnPaths.add(path);
		}
		workspace.getFactory().replaceWorksheet(this.worksheetId, this.worksheetBeforeInvocation);
		
		c.add(new ReplaceWorksheetUpdate(worksheetId, this.worksheetBeforeInvocation));
		c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, getSuperSelection(workspace),workspace.getContextId()));
		
		return c;	
		
	}

}
