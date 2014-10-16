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

package edu.isi.karma.controller.command.alignment;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.CommandHistoryUtil;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.HistoryAddCommandUpdate;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraph;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphType;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GenerateR2RMLModelCommand extends WorksheetSelectionCommand {

	private String worksheetName;
	private String tripleStoreUrl;
	private String graphContext;
	private String RESTserverAddress;
	private static Logger logger = LoggerFactory.getLogger(GenerateR2RMLModelCommand.class);

	public enum JsonKeys {
		updateType, fileUrl, worksheetId
	}

	public enum PreferencesKeys {
		rdfPrefix, rdfNamespace, modelSparqlEndPoint
	}

	protected GenerateR2RMLModelCommand(String id, String worksheetId, String url, String context, String selectionId) {
		super(id, worksheetId, selectionId);
		this.tripleStoreUrl = url;
		this.graphContext = context;
	}

	public String getTripleStoreUrl() {
		return tripleStoreUrl;
	}

	public void setTripleStoreUrl(String tripleStoreUrl) {
		this.tripleStoreUrl = tripleStoreUrl;
	}

	public String getGraphContext() {
		return graphContext;
	}

	public void setGraphContext(String graphContext) {
		this.graphContext = graphContext;
	}

	public void setRESTserverAddress(String RESTserverAddress) {
		this.RESTserverAddress = RESTserverAddress;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Generate R2RML Model";
	}

	@Override
	public String getDescription() {
		return this.worksheetName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}


	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer uc = new UpdateContainer();
		//save the preferences 
		savePreferences(workspace);
		boolean storeOldHistory = ModelingConfiguration.isStoreOldHistoryEnabled();
		System.out.println("storeOldHistory: " + storeOldHistory);
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		SuperSelection selection = getSuperSelection(worksheet);
		CommandHistory history = workspace.getCommandHistory();
		List<Command> oldCommands = history.getCommandsFromWorksheetId(worksheetId);
		if (storeOldHistory) {			
			JSONArray oldCommandsArray = new JSONArray();
			for (Command refined : oldCommands)
				oldCommandsArray.put(workspace.getCommandHistory().getCommandJSON(workspace, refined));
			worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
					Property.oldCommandHistory, oldCommandsArray.toString());
		}
		CommandHistoryUtil historyUtil = new CommandHistoryUtil(history.getCommandsFromWorksheetId(worksheetId), workspace, worksheetId);
		historyUtil.consolidateHistory();	
		if (!oldCommands.equals(historyUtil.getCommands())) {
			uc.append(historyUtil.replayHistory());
			uc.removeUpdateByClass(HistoryAddCommandUpdate.class);
			uc.removeUpdateByClass(InfoUpdate.class);
			uc.removeUpdateByClass(ErrorUpdate.class);
			historyUtil.consolidateHistory();
			uc.add(new HistoryUpdate(workspace.getCommandHistory()));

		}
		Set<String> inputColumns = historyUtil.generateInputColumns();
		Set<String> outputColumns = historyUtil.generateOutputColumns();
		JSONArray inputColumnsArray = new JSONArray();
		JSONArray outputColumnsArray = new JSONArray();
		for (String hNodeId : inputColumns) {
			HNode hnode = workspace.getFactory().getHNode(hNodeId);
			JSONArray hNodeRepresentation = hnode.getJSONArrayRepresentation(workspace.getFactory());
			inputColumnsArray.put(hNodeRepresentation);
		}

		for (String hNodeId : outputColumns) {
			HNode hnode = workspace.getFactory().getHNode(hNodeId);
			JSONArray hNodeRepresentation = hnode.getJSONArrayRepresentation(workspace.getFactory());
			outputColumnsArray.put(hNodeRepresentation);
		}
		worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
				Property.inputColumns, inputColumnsArray.toString());
		worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
				Property.outputColumns, outputColumnsArray.toString());		
		this.worksheetName = worksheet.getTitle();
		String graphLabel = worksheet.getMetadataContainer().getWorksheetProperties().
				getPropertyValue(Property.graphLabel); 

		if (graphLabel == null || graphLabel.isEmpty()) {
			worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
					Property.graphLabel, worksheet.getTitle());
			graphLabel = worksheet.getTitle();
			worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
					Property.graphName, WorksheetProperties.createDefaultGraphName(graphLabel));	
		}
		// Prepare the model file path and names
		final String modelFileName = graphLabel + "-model.ttl"; 
		final String modelFileLocalPath = ServletContextParameterMap.getParameterValue(
				ContextParameter.R2RML_PUBLISH_DIR) +  modelFileName;

		// Get the alignment for this Worksheet
		Alignment alignment = AlignmentManager.Instance().getAlignment(AlignmentManager.
				Instance().constructAlignmentId(workspace.getId(), worksheetId));

		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Please align the worksheet before generating R2RML Model!"));
		}
		Set<LabeledLink> links = new HashSet<LabeledLink>();
		if (alignment.getSteinerTree() != null) {
			for (LabeledLink link : alignment.getSteinerTree().edgeSet()) {
				if ((link.getStatus() == LinkStatus.Normal || link.getStatus() == LinkStatus.PreferredByUI) && (link.getType() == LinkType.ObjectPropertyLink)) {
					links.add(link);
				}
			}
		}
		JSONArray newEdges = new JSONArray();
		JSONArray initialEdges = new JSONArray();
		ChangeInternalNodeLinksCommandFactory cinlcf = new ChangeInternalNodeLinksCommandFactory();
		for (LabeledLink link : links) {
			JSONObject newEdge = new JSONObject();
			JSONObject initialEdge = new JSONObject();
			newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeSourceId.name(), link.getSource().getId());
			newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeTargetId.name(), link.getTarget().getId());
			newEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeId.name(), link.getUri());
			initialEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeSourceId.name(), link.getSource().getId());
			initialEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeTargetId.name(), link.getTarget().getId());
			initialEdge.put(ChangeInternalNodeLinksCommand.JsonKeys.edgeId.name(), link.getUri());
			newEdges.put(newEdge);
			initialEdges.put(initialEdge);
		}
		JSONArray inputJSON = new JSONArray();
		JSONObject t = new JSONObject();
		t.put("name", "worksheetId");
		t.put("type", HistoryJsonUtil.ParameterType.worksheetId.name());
		t.put("value", worksheetId);
		inputJSON.put(t);
		t = new JSONObject();
		t.put("name", "initialEdges");
		t.put("type", HistoryJsonUtil.ParameterType.other.name());
		t.put("value", initialEdges);
		inputJSON.put(t);
		t = new JSONObject();
		t.put("name", "newEdges");
		t.put("type", HistoryJsonUtil.ParameterType.other.name());
		t.put("value", newEdges);
		inputJSON.put(t);
		if (newEdges.length() > 0 || initialEdges.length() > 0) {
			try {
				Command changeInternalNodeLinksCommand = cinlcf.createCommand(inputJSON, workspace);
				workspace.getCommandHistory().doCommand(changeInternalNodeLinksCommand, workspace);
				uc.add(new HistoryUpdate(workspace.getCommandHistory()));
				uc.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet)));
				uc.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		// mohsen: my code to enable Karma to leran semantic models
		// *****************************************************************************************
		// *****************************************************************************************

		SemanticModel semanticModel = new SemanticModel(workspace, worksheet, worksheetName, alignment.getSteinerTree(), selection);
		semanticModel.setName(worksheetName);
		try {
			semanticModel.writeJson(ServletContextParameterMap.getParameterValue(ContextParameter.JSON_MODELS_DIR) + 
					semanticModel.getName() + 
					".model.json");
		} catch (Exception e) {
			logger.error("error in exporting the model to JSON!");
			//			e.printStackTrace();
		}
		try {
			semanticModel.writeGraphviz(ServletContextParameterMap.getParameterValue(ContextParameter.GRAPHVIZ_MODELS_DIR) + 
					semanticModel.getName() + 
					".model.dot", false, false);
		} catch (Exception e) {
			logger.error("error in exporting the model to GRAPHVIZ!");
			//			e.printStackTrace();
		}

		if (ModelingConfiguration.isLearnerEnabled())
			ModelLearningGraph.getInstance(workspace.getOntologyManager(), ModelLearningGraphType.Compact).
			addModelAndUpdateAndExport(semanticModel);

		// *****************************************************************************************
		// *****************************************************************************************

		try {
			R2RMLAlignmentFileSaver fileSaver = new R2RMLAlignmentFileSaver(workspace);

			fileSaver.saveAlignment(alignment, modelFileLocalPath);

			// Write the model to the triple store

			// Get the graph name from properties
			String graphName = worksheet.getMetadataContainer().getWorksheetProperties()
					.getPropertyValue(Property.graphName);
			if (graphName == null || graphName.isEmpty()) {
				// Set to default
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
						Property.graphName, WorksheetProperties.createDefaultGraphName(worksheet.getTitle()));
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
						Property.graphLabel, worksheet.getTitle());
				graphName = WorksheetProperties.createDefaultGraphName(worksheet.getTitle());
			}

			boolean result = true;//utilObj.saveToStore(modelFileLocalPath, tripleStoreUrl, graphName, true, null);
			if (tripleStoreUrl != null && tripleStoreUrl.trim().compareTo("") != 0) {
				UriBuilder builder = UriBuilder.fromPath(modelFileName);
				String url = RESTserverAddress + "/R2RMLMapping/local/" + builder.build().toString();
				SaveR2RMLModelCommandFactory factory = new SaveR2RMLModelCommandFactory();
				SaveR2RMLModelCommand cmd = factory.createCommand(workspace, url, tripleStoreUrl, graphName, "URL");
				cmd.doIt(workspace);
				result &= cmd.getSuccessful();
				workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelUrl, url);
				workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelContext, graphName);
				workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelRepository, tripleStoreUrl);
			}
			if (result) {
				logger.info("Saved model to triple store");
				uc.add(new AbstractUpdate() {
					public void generateJson(String prefix, PrintWriter pw,	
							VWorkspace vWorkspace) {
						JSONObject outputObject = new JSONObject();
						try {
							outputObject.put(JsonKeys.updateType.name(), "PublishR2RMLUpdate");

							outputObject.put(JsonKeys.fileUrl.name(), ServletContextParameterMap.getParameterValue(
									ContextParameter.R2RML_PUBLISH_RELATIVE_DIR) + modelFileName);
							outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
							pw.println(outputObject.toString());
						} catch (JSONException e) {
							logger.error("Error occured while generating JSON!");
						}
					}
				});
				return uc;
			} 

			return new UpdateContainer(new ErrorUpdate("Error occured while generating R2RML model!"));

		} catch (Exception e) {
			logger.error("Error occured while generating R2RML Model!", e);
			return new UpdateContainer(new ErrorUpdate("Error occured while generating R2RML model!"));
		}
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Not required
		return null;
	}




	private void savePreferences(Workspace workspace){
		try{
			JSONObject prefObject = new JSONObject();
			prefObject.put(PreferencesKeys.modelSparqlEndPoint.name(), tripleStoreUrl);
			workspace.getCommandPreferences().setCommandPreferences(
					"GenerateR2RMLModelCommandPreferences", prefObject);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
