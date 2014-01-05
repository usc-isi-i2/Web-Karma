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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.kr2rml.KR2RMLWorksheetHistoryCompatibilityVerifier;
import edu.isi.karma.kr2rml.WorksheetModelWriter;
import edu.isi.karma.modeling.ModelingConfiguration;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.alignment.SemanticModel;
import edu.isi.karma.modeling.alignment.learner.ModelLearningGraph;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class GenerateR2RMLModelCommand extends Command {
	
	private final String worksheetId;
	private String worksheetName;
	private String tripleStoreUrl;
	private String graphContext;
	
	private static Logger logger = LoggerFactory.getLogger(GenerateR2RMLModelCommand.class);
	
	public enum JsonKeys {
		updateType, fileUrl, worksheetId
	}
	
	public enum PreferencesKeys {
		rdfPrefix, rdfNamespace, modelSparqlEndPoint
	}
	
	protected GenerateR2RMLModelCommand(String id, String worksheetId, String url, String context) {
		super(id);
		this.worksheetId = worksheetId;
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
				
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		this.worksheetName = worksheet.getTitle();
		
		// Prepare the model file path and names
		final String modelFileName = workspace.getCommandPreferencesId() + worksheetId + "-" + 
				this.worksheetName +  "-model.ttl"; 
		final String modelFileLocalPath = ServletContextParameterMap.getParameterValue(
				ContextParameter.USER_DIRECTORY_PATH) +  "publish/R2RML/" + modelFileName;

		// Get the alignment for this Worksheet
		Alignment alignment = AlignmentManager.Instance().getAlignment(AlignmentManager.
				Instance().constructAlignmentId(workspace.getId(), worksheetId));
		
		if (alignment == null) {
			logger.info("Alignment is NULL for " + worksheetId);
			return new UpdateContainer(new ErrorUpdate(
					"Please align the worksheet before generating R2RML Model!"));
		}
		
		// mohsen: my code to enable Karma to leran semantic models
		// *****************************************************************************************
		// *****************************************************************************************

		SemanticModel semanticModel = new SemanticModel(worksheetName, alignment.getSteinerTree());
		semanticModel.setName(worksheetName);
		try {
			semanticModel.writeJson(ModelingConfiguration.getModelsJsonDir() + 
					semanticModel.getName() + 
					".model.json");
		} catch (Exception e) {
			logger.error("error in exporting the model to JSON!");
//			e.printStackTrace();
		}
		try {
			semanticModel.writeGraphviz(ModelingConfiguration.getModelsGraphvizDir() + 
					semanticModel.getName() + 
					".model.dot", false, false);
		} catch (Exception e) {
			logger.error("error in exporting the model to GRAPHVIZ!");
//			e.printStackTrace();
		}

		if (ModelingConfiguration.isLearnerEnabled())
			ModelLearningGraph.getInstance(workspace.getOntologyManager()).addModelAndUpdateGraphJson(semanticModel);
		
		// *****************************************************************************************
		// *****************************************************************************************

		try {
			// Get the namespace and prefix from the preferences
			String namespace = "";
			String prefix = "";
			JSONObject prefObject = workspace.getCommandPreferences().getCommandPreferencesJSONObject(
					PublishRDFCommand.class.getSimpleName()+"Preferences");
			if (prefObject != null) {
				namespace = prefObject.getString(PreferencesKeys.rdfNamespace.name());
				prefix = prefObject.getString(PreferencesKeys.rdfPrefix.name());
				namespace = ((namespace == null) || (namespace.equals(""))) ? 
						Namespaces.KARMA_DEV : namespace;
				prefix = ((prefix == null) || (prefix.equals(""))) ? 
						Prefixes.KARMA_DEV : prefix;
			} else {
				namespace = Namespaces.KARMA_DEV;
				prefix = Prefixes.KARMA_DEV;
			}
			
			// Generate the KR2RML data structures for the RDF generation
			final ErrorReport errorReport = new ErrorReport();
			KR2RMLMappingGenerator mappingGen = new KR2RMLMappingGenerator(workspace, worksheet, alignment, 
					worksheet.getSemanticTypes(), prefix, namespace, true, errorReport);
			boolean isR2RMLCompatible = KR2RMLWorksheetHistoryCompatibilityVerifier.verify(workspace, mappingGen.getKR2RMLMapping().getWorksheetHistory().toString());
			if(!isR2RMLCompatible)
			{
				uc.add(new InfoUpdate("The KR2RMLModel generated is not compatible with R2RML"));
			}
			// Write the model
			writeModel(workspace, workspace.getOntologyManager(), mappingGen, worksheet, modelFileLocalPath);
			
			// Write the model to the triple store
			TripleStoreUtil utilObj = new TripleStoreUtil();

			// Get the graph name from properties
			String graphName = worksheet.getMetadataContainer().getWorksheetProperties()
					.getPropertyValue(Property.graphName);
			if (graphName == null || graphName.isEmpty()) {
				// Set to default
				worksheet.getMetadataContainer().getWorksheetProperties().setPropertyValue(
						Property.graphName, WorksheetProperties.createDefaultGraphName(worksheet.getTitle()));
				graphName = WorksheetProperties.createDefaultGraphName(worksheet.getTitle());
			}
			
			boolean result = utilObj.saveToStore(modelFileLocalPath, tripleStoreUrl, graphName, true);
			if (result) {
				logger.info("Saved model to triple store");
				uc.add(new AbstractUpdate() {
					public void generateJson(String prefix, PrintWriter pw,	
							VWorkspace vWorkspace) {
						JSONObject outputObject = new JSONObject();
						try {
							outputObject.put(JsonKeys.updateType.name(), "PublishR2RMLUpdate");
							outputObject.put(JsonKeys.fileUrl.name(), "publish/R2RML/" + modelFileName);
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
	
	private void writeModel(Workspace workspace, OntologyManager ontMgr, 
			KR2RMLMappingGenerator mappingGen, Worksheet worksheet, String modelFileLocalPath) 
					throws RepositoryException, FileNotFoundException, 
							UnsupportedEncodingException, JSONException {
		File f = new File(modelFileLocalPath);
		File parentDir = f.getParentFile();
		parentDir.mkdirs();
		PrintWriter writer = new PrintWriter(f, "UTF-8");
		WorksheetModelWriter modelWriter = new WorksheetModelWriter(writer, 
				workspace.getFactory(), ontMgr, worksheet, workspace);

		// Writer worksheet properties such as Service URL
		modelWriter.writeWorksheetProperties(worksheet);
		
		// Write the worksheet history
		modelWriter.writeCompleteWorksheetHistory(mappingGen.getKR2RMLMapping());
		
		// Write the R2RML mapping
		modelWriter.writeR2RMLMapping(ontMgr, mappingGen);
		modelWriter.close();
		writer.flush();
		writer.close();
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
