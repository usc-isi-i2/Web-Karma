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

package edu.isi.karma.controller.command.worksheet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.HistoryJSONEditor;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.kr2rml.KR2RMLVersion;
import edu.isi.karma.kr2rml.mapping.KR2RMLMapping;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.util.SavedModelURLs;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class ApplyHistoryFromR2RMLModelCommand extends WorksheetCommand {
	private URL r2rmlModelFile;
	private File uploadedFile; 
	private String modelFileUrl;
	private boolean override;
	private static Logger logger = LoggerFactory.getLogger(ApplyHistoryFromR2RMLModelCommand.class);

	protected ApplyHistoryFromR2RMLModelCommand(String id, String model, File uploadedFile, 
			String worksheetId, boolean override) {
		super(id, model, worksheetId);
		this.uploadedFile = uploadedFile;
		
		this.override = override;
	}
	
	protected ApplyHistoryFromR2RMLModelCommand(String id, String model, String modelFileUrl, 
			String worksheetId, boolean override) {
		super(id, model, worksheetId);
		this.modelFileUrl = modelFileUrl;
		this.override = override;
	}

	private enum JsonKeys {
		updateType, worksheetId, baseURI, prefix, graphLabel, GithubURL
	}

	@Override
	public String getCommandName() {
		return ApplyHistoryFromR2RMLModelCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Apply R2RML Model";
	}

	@Override
	public String getDescription() {
		return r2rmlModelFile.getFile();
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		long start = System.currentTimeMillis();
		
		if(uploadedFile != null)
		{
			URL modelFile = null;
			try {
				modelFile = uploadedFile.toURI().toURL();
			} catch(MalformedURLException me) {
				logger.error("Error locating the model file:", me);
			} finally {
				this.r2rmlModelFile = modelFile;
			}
		}
		else if (modelFileUrl != null)
		{
			URL modelFile = null;
			try {
				modelFile = new URL(modelFileUrl);
				(new SavedModelURLs()).saveModelUrl(modelFileUrl, workspace.getContextId());
			} catch(Exception me) {
				logger.error("Error locating the model file:", me);
			} finally {
				this.r2rmlModelFile = modelFile;
			}
		}
		final Worksheet worksheet = workspace.getWorksheet(worksheetId);
		UpdateContainer c = new UpdateContainer();
		
		try {
			JSONArray historyJson  = extractHistoryFromModel(workspace, c);
			HistoryJSONEditor editor = new HistoryJSONEditor(new JSONArray(historyJson.toString()), workspace, worksheetId);
			if (null == historyJson || historyJson.length() == 0) {
				return new UpdateContainer(new ErrorUpdate("No history found in R2RML Model!"));
			}
			logger.info("Protocol:" + r2rmlModelFile.getProtocol());
			if(!r2rmlModelFile.getProtocol().equals("file"))
				editor.updateModelUrlInCommands(r2rmlModelFile.toString());
			WorksheetCommandHistoryExecutor histExecutor = new WorksheetCommandHistoryExecutor(
					worksheetId, workspace);
			AlignmentManager alignMgr = AlignmentManager.Instance();
			Alignment alignment = alignMgr.getAlignment(workspace.getId(), worksheetId);
			if (override || alignment == null || alignment.GetTreeRoot() == null) {
				worksheet.clearSemanticTypes();
				String alignmentId = alignMgr.constructAlignmentId(workspace.getId(), worksheetId);
				alignMgr.removeAlignment(alignmentId);
				alignMgr.createAlignment(workspace.getId(), worksheetId,workspace.getOntologyManager());
				editor.deleteExistingTransformationCommands();
				historyJson = editor.getHistoryJSON();
			}
			else {
				editor.deleteExistingTransformationAndModelingCommands();
				historyJson = editor.getHistoryJSON();
			}
			logger.info(editor.getHistoryJSON().toString(4));
			UpdateContainer hc = histExecutor.executeAllCommands(historyJson);
			if(hc != null) {
				hc.removeUpdateByClass(InfoUpdate.class);
				hc.removeUpdateByClass(ErrorUpdate.class);
				hc.removeUpdateByClass(AlignmentSVGVisualizationUpdate.class);
				c.append(hc);
			}
			alignment = alignMgr.getAlignment(workspace.getId(), worksheetId);
			if(alignment != null) {
				alignment.align();
				c.add(new AlignmentSVGVisualizationUpdate(worksheetId));
			}
		} catch (Exception e) {
			String msg = "Error occured while applying history!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		c.add(new InfoUpdate("Model successfully applied!"));
		c.add(new AbstractUpdate() {

			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(), "SetWorksheetProperties");
					outputObject.put(JsonKeys.worksheetId.name(), worksheetId);
					WorksheetProperties props = worksheet.getMetadataContainer().getWorksheetProperties();
					if (props.getPropertyValue(Property.baseURI) != null)
						outputObject.put(JsonKeys.baseURI.name(), props.getPropertyValue(Property.baseURI));
					if (props.getPropertyValue(Property.prefix) != null)
						outputObject.put(JsonKeys.prefix.name(), props.getPropertyValue(Property.prefix));
					if (props.getPropertyValue(Property.graphLabel) != null && !props.getPropertyValue(Property.graphLabel).trim().isEmpty()) 
						outputObject.put(JsonKeys.graphLabel.name(), props.getPropertyValue(Property.graphLabel));
					if (props.getPropertyValue(Property.GithubURL) != null && !props.getPropertyValue(Property.GithubURL).trim().isEmpty())
						outputObject.put(JsonKeys.GithubURL.name(), props.getPropertyValue(Property.GithubURL));
					
					pw.println(outputObject.toString());
				} catch (JSONException e) {
					e.printStackTrace();
					logger.error("Error occured while generating JSON!");
				}

			}

		});
		
		long end = System.currentTimeMillis();
		
		logger.info("Time taken to Apply R2RML Model: " + (end-start)/1000 + "sec");
		return c;
	}

	private JSONArray extractHistoryFromModel(Workspace workspace, UpdateContainer uc) 
			throws RepositoryException, RDFParseException, IOException, JSONException, KarmaException {
		if(r2rmlModelFile != null) {
			Worksheet ws = workspace.getFactory().getWorksheet(worksheetId);
			R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(ws.getTitle(), r2rmlModelFile);
			WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(id);
			KR2RMLMapping mapping = parser.parse();
			KR2RMLVersion version = mapping.getVersion();
			if(version.compareTo(KR2RMLVersion.current) < 0)
			{
				uc.add(new InfoUpdate("Model version is " + version.toString() + ".  Current version is " + KR2RMLVersion.current.toString() + ".  Please publish it again."));
			}
			return mapping.getWorksheetHistory();
		} else {
			uc.add(new ErrorUpdate("Model could not be found"));
			return new JSONArray();
		}

	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
