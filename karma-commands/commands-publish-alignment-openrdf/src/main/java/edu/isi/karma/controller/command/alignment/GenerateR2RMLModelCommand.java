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
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.CommandHistoryUtil;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.TrivialErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraph;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraphType;
import edu.isi.karma.modeling.alignment.learner.PatternWeightSystem;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.alignment.InternalNode;
import edu.isi.karma.rep.alignment.LabeledLink;
import edu.isi.karma.rep.alignment.LinkStatus;
import edu.isi.karma.rep.alignment.LinkType;
import edu.isi.karma.rep.alignment.Node;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;
import edu.isi.karma.webserver.WorkspaceKarmaHomeRegistry;

public class GenerateR2RMLModelCommand extends WorksheetSelectionCommand {

	private String worksheetName;
	private String tripleStoreUrl;
	private String RESTserverAddress;
	private static Logger logger = LoggerFactory.getLogger(GenerateR2RMLModelCommand.class);

	public enum JsonKeys {
		updateType, fileUrl, worksheetId
	}

	public enum PreferencesKeys {
		rdfPrefix, rdfNamespace, modelSparqlEndPoint
	}

	protected GenerateR2RMLModelCommand(String id, String model, String worksheetId, String url, String selectionId) {
		super(id, model, worksheetId, selectionId);
		this.tripleStoreUrl = url;
	}

	public String getTripleStoreUrl() {
		return tripleStoreUrl;
	}

	public void setTripleStoreUrl(String tripleStoreUrl) {
		this.tripleStoreUrl = tripleStoreUrl;
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
		return CommandType.notInHistory;
	}


	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(WorkspaceKarmaHomeRegistry.getInstance().getKarmaHome(workspace.getId()));
		String worksheetId = this.worksheetId;
		UpdateContainer uc = new UpdateContainer();
		//save the preferences 
		savePreferences(workspace);
		boolean storeOldHistory = modelingConfiguration.isStoreOldHistoryEnabled();
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
		boolean isHistoryStale = history.isStale(worksheetId);
		if (isHistoryStale) {
			System.out.println("**** REPLAY HISTORY ***");
			uc.append(historyUtil.replayHistory());
			worksheetId = historyUtil.getWorksheetId();
			worksheet = workspace.getWorksheet(worksheetId);
			selection = getSuperSelection(worksheet);
			historyUtil = new CommandHistoryUtil(history.getCommandsFromWorksheetId(worksheetId), workspace, worksheetId);
		}
		
		//Execute all Provenance related commands again before publishing
		List<Command> wkCommands = historyUtil.getCommands();
		for(Command wkCommand : wkCommands) {
			if(wkCommand instanceof SetSemanticTypeCommand) {
				SetSemanticTypeCommand cmd = (SetSemanticTypeCommand)wkCommand;
				if(cmd.hasProvenanceType()) {
					uc.append(workspace.getCommandHistory().doCommand(cmd, workspace, false));
				}
			} else if(wkCommand instanceof AddLinkCommand) {
				AddLinkCommand cmd = (AddLinkCommand)wkCommand;
				if(cmd.hasProvenanceType()) {
					uc.append(workspace.getCommandHistory().doCommand(cmd, workspace, false));
				}
			}
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
		final String modelFileLocalPath = contextParameters.getParameterValue(
				ContextParameter.R2RML_PUBLISH_DIR) +  modelFileName;

		// Get the alignment for this Worksheet
		Alignment alignment = AlignmentManager.Instance().getAlignment(AlignmentManager.
				Instance().constructAlignmentId(workspace.getId(), worksheetId));

		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Please align the worksheet before generating R2RML Model!"));
		}
		Set<LabeledLink> links = new HashSet<>();
		if (alignment.getSteinerTree() != null) {
			for (LabeledLink link : alignment.getSteinerTree().edgeSet()) {
				if ((link.getStatus() == LinkStatus.Normal || link.getStatus() == LinkStatus.PreferredByUI) && (link.getType() == LinkType.ObjectPropertyLink)) {
					links.add(link);
				}
			}
		}
		
		//Make all links to be forced. Add a change links command for all links those links
		JSONArray newEdges = new JSONArray();
		JSONArray initialEdges = new JSONArray();
		ChangeInternalNodeLinksCommandFactory cinlcf = new ChangeInternalNodeLinksCommandFactory();
		for (LabeledLink link : links) {
			if(link.getStatus() != LinkStatus.ForcedByUser) {
				JSONObject newEdge = new JSONObject();
				newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeSourceId.name(), link.getSource().getId());
				newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeTargetId.name(), link.getTarget().getId());
				newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.edgeId.name(), link.getUri());
				newEdge.put(ChangeInternalNodeLinksCommand.LinkJsonKeys.isProvenance.name(), link.isProvenance());
				newEdges.put(newEdge);
			}
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
				Command changeInternalNodeLinksCommand = cinlcf.createCommand(inputJSON, model, workspace);
				workspace.getCommandHistory().doCommand(changeInternalNodeLinksCommand, workspace);
				uc.add(new HistoryUpdate(workspace.getCommandHistory()));
				uc.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));
				uc.append(((WorksheetCommand)changeInternalNodeLinksCommand).computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		//Remove all provenance links that do not have Data properties
		Set<Node> nodes = alignment.getSteinerTree().vertexSet();
		boolean linksRemoved = false;
		for (Node node:nodes) {
			if (node instanceof InternalNode) {
				Set<LabeledLink> outLinks = alignment.getOutgoingLinksInTree(node.getId());
				if(outLinks != null && outLinks.size() > 0) {
					boolean hasProvLink = false;
					boolean hasDataProperty = false;
					Set<String> provLinkIds = new HashSet<>();
					for(LabeledLink link : outLinks) {
						if(link.isProvenance()) {
							hasProvLink = true;
							provLinkIds.add(link.getId());
							continue;
						}
						if(link.getType() == LinkType.DataPropertyLink)
							hasDataProperty = true;
					}
					if(hasProvLink && !hasDataProperty) {
						//Remove all provenance links
						for(String linkId : provLinkIds) {
							logger.info("**** Remove Provenance Link:" + linkId);
							alignment.removeLink(linkId);
							linksRemoved = true;
						}
					}
				}
			}
		}
		if(linksRemoved) {
			uc.append(WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(worksheetId, workspace));
		}
		
		
		// mohsen: my code to enable Karma to leran semantic models
		// *****************************************************************************************
		// *****************************************************************************************

		SemanticModel semanticModel = new SemanticModel(workspace, worksheet, worksheetName, alignment.getSteinerTree(), selection);
		semanticModel.setName(worksheetName);
		try {
			semanticModel.writeJson(contextParameters.getParameterValue(ContextParameter.JSON_MODELS_DIR) + 
					semanticModel.getName() + 
					".model.json");
		} catch (Exception e) {
			logger.error("error in exporting the model to JSON!");
			//			e.printStackTrace();
		}
		try {
			semanticModel.writeGraphviz(contextParameters.getParameterValue(ContextParameter.GRAPHVIZ_MODELS_DIR) + 
					semanticModel.getName() + 
					".model.dot", false, false);
		} catch (Exception e) {
			logger.error("error in exporting the model to GRAPHVIZ!");
			//			e.printStackTrace();
		}

		if (modelingConfiguration.isLearnerEnabled())
			ModelLearningGraph.getInstance(workspace.getOntologyManager(), ModelLearningGraphType.Compact).
			addModelAndUpdateAndExport(semanticModel, PatternWeightSystem.JWSPaperFormula);

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
				try {
					UriBuilder builder = UriBuilder.fromPath(modelFileName);
					String url = RESTserverAddress + "/R2RMLMapping/local/" + builder.build().toString();
					SaveR2RMLModelCommandFactory factory = new SaveR2RMLModelCommandFactory();
					SaveR2RMLModelCommand cmd = factory.createCommand(model, workspace, url, tripleStoreUrl, graphName, "URL");
					cmd.doIt(workspace);
					result &= cmd.getSuccessful();
					workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelUrl, url);
					workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelContext, graphName);
					workspace.getWorksheet(worksheetId).getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.modelRepository, tripleStoreUrl);
				} catch(Exception e) {
					logger.error("Error pushing model to triple store", e);
					result = false;
				}
			}
			final String temp = worksheetId;
			
			uc.add(new AbstractUpdate() {
				public void generateJson(String prefix, PrintWriter pw,	
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(), "PublishR2RMLUpdate");

						outputObject.put(JsonKeys.fileUrl.name(), contextParameters.getParameterValue(
								ContextParameter.R2RML_PUBLISH_RELATIVE_DIR) + modelFileName);
						outputObject.put(JsonKeys.worksheetId.name(), temp);
						pw.println(outputObject.toString());
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
			if(!result)
				uc.add(new TrivialErrorUpdate("Error pushing model to Triple Store"));
			return uc;

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
