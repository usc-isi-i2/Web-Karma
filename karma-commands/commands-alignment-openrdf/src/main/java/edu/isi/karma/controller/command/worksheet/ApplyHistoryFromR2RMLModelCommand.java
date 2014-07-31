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
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
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
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class ApplyHistoryFromR2RMLModelCommand extends WorksheetCommand {
	private final File r2rmlModelFile;
	private final String worksheetId;
	private boolean override;
	private static Logger logger = LoggerFactory.getLogger(ApplyHistoryFromR2RMLModelCommand.class);

	protected ApplyHistoryFromR2RMLModelCommand(String id, File uploadedFile, String worksheetId, boolean override) {
		super(null, id, worksheetId);
		this.r2rmlModelFile = uploadedFile;
		this.worksheetId = worksheetId;
		this.override = override;
	}

	private enum JsonKeys {
		updateType, worksheetId, baseURI, prefix, graphLabel
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
		return r2rmlModelFile.getName();
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		final Worksheet worksheet = workspace.getWorksheet(worksheetId);
		UpdateContainer c = new UpdateContainer();
		c.add(new WorksheetListUpdate());
		UpdateContainer rwu = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		if(rwu != null)
		{
			c.append(rwu);
		}

		try {
			JSONArray historyJson  = extractHistoryFromModel(workspace, c);
			HistoryJSONEditor editor = new HistoryJSONEditor(new JSONArray(historyJson.toString()), workspace, worksheetId);
			if (null == historyJson || historyJson.length() == 0) {
				return new UpdateContainer(new ErrorUpdate("No history found in R2RML Model!"));
			}
			WorksheetCommandHistoryExecutor histExecutor = new WorksheetCommandHistoryExecutor(
					worksheetId, workspace);
			AlignmentManager alignMgr = AlignmentManager.Instance();
			Alignment alignment = alignMgr.getAlignment(workspace.getId(), worksheetId);
			if (override || alignment == null || alignment.GetTreeRoot() == null) {
				String alignmentId = alignMgr.constructAlignmentId(workspace.getId(), worksheetId);
				alignMgr.removeAlignment(alignmentId);
				alignMgr.getAlignmentOrCreateIt(workspace.getId(), worksheetId, workspace.getOntologyManager());
				editor.deleteExistingTransformationCommands();
				historyJson = editor.getHistoryJSON();
			}
			else {
				editor.deleteExistingTransformationAndModelingCommands();
				historyJson = editor.getHistoryJSON();
			}
			System.out.println(editor.getHistoryJSON().toString(4));
			UpdateContainer hc = histExecutor.executeAllCommands(historyJson);
			if(hc != null)
				c.append(hc);
		} catch (Exception e) {
			String msg = "Error occured while applying history!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}

		// Add worksheet updates that could have resulted out of the transformation commands
		for (Worksheet newws : workspace.getWorksheets()) {
			if (newws.getId().compareTo(worksheetId) != 0) {
				c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(newws.getId()));
				Alignment alignment = AlignmentManager.Instance().getAlignmentOrCreateIt(workspace.getId(), newws.getId(), workspace.getOntologyManager());
				c.append(WorksheetUpdateFactory.createSemanticTypesAndSVGAlignmentUpdates(newws.getId(), workspace, alignment));
			}
		}
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));	
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
					pw.println(outputObject.toString());
				} catch (JSONException e) {
					e.printStackTrace();
					logger.error("Error occured while generating JSON!");
				}

			}

		});
		return c;
	}

	private JSONArray extractHistoryFromModel(Workspace workspace, UpdateContainer uc) 
			throws RepositoryException, RDFParseException, IOException, JSONException, KarmaException {

		Worksheet ws = workspace.getFactory().getWorksheet(worksheetId);
		R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(ws.getTitle(), r2rmlModelFile.toURI().toURL());
		WorksheetR2RMLJenaModelParser parser = new WorksheetR2RMLJenaModelParser(id);
		KR2RMLMapping mapping = parser.parse();
		KR2RMLVersion version = mapping.getVersion();
		if(version.compareTo(KR2RMLVersion.current) < 0)
		{
			uc.add(new InfoUpdate("Model version is " + version.toString() + ".  Current version is " + KR2RMLVersion.current.toString() + ".  Please publish it again."));
		}
		return mapping.getWorksheetHistory();

	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
