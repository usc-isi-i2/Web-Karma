/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 *****************************************************************************
 */
package edu.isi.karma.controller.command.importdata;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.ImportPropertiesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class ImportJSONFileCommand extends ImportFileCommand implements IPreviewable {
	private String columnsJson;
	private boolean savePreset;
	private enum JsonKeys {
		worksheetId, columns, name, id, visible, hideable, children, updateType, fileUrl
	}

	private static Logger logger = LoggerFactory
			.getLogger(ImportJSONFileCommand.class.getSimpleName());

	public ImportJSONFileCommand(String id, File file) {
		super(id, file);
	}

	public ImportJSONFileCommand(String id, String revisedId, File file) {
		super(id, revisedId, file);
		this.encoding = EncodingDetector.detect(file);
	}

	@Override
	public String getTitle() {
		return "Import JSON File";
	}


	@Override
	protected Import createImport(Workspace workspace) {
		JSONArray tree = generateSelectTree(columnsJson, true);
		return new JsonImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines, tree);
	}

	@Override
	public UpdateContainer showPreview(HttpServletRequest request) throws CommandException {
		boolean filter = Boolean.parseBoolean(request.getParameter("filter"));
		if (!filter)
			return super.showPreview(request);
		final Workspace workspace = WorkspaceManager.getInstance().createWorkspace();
		Import imp = new JsonImport(getFile(), getFile().getName(), workspace, encoding, 1000, null);
		try {
			final Worksheet worksheet = imp.generateWorksheet();
			UpdateContainer uc = new UpdateContainer(new ImportPropertiesUpdate(getFile(), encoding, maxNumLines, id));			
			uc.add(new AbstractUpdate() {				
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject response = new JSONObject();
					response.put(AbstractUpdate.GenericJsonKeys.updateType.name(), 
							"PreviewHeaderUpdate");
					JSONArray columns = getColumnsJsonArray(worksheet.getHeaders().getHNodes());
					response.put(JsonKeys.columns.name(), columns);
					pw.println(response.toString());
				}

				private JSONArray getColumnsJsonArray(Collection<HNode> headers) {
					JSONArray columns = new JSONArray();
					for(HNode headerNode : headers) {
						JSONObject column = new JSONObject();
						column.put(JsonKeys.id.name(), headerNode.getId());
						column.put(JsonKeys.name.name(), headerNode.getColumnName());
						column.put(JsonKeys.visible.name(), true);
						boolean hideable = true;
						if(headerNode.hasNestedTable()) {
							JSONArray children = getColumnsJsonArray(headerNode.getNestedTable().getHNodes());
							column.put(JsonKeys.children.name(), children);
						}
						column.put(JsonKeys.hideable.name(), hideable);
						columns.put(column);
					}
					return columns;
				}
			});
			WorkspaceManager.getInstance().removeWorkspace(workspace.getId());
			return uc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new UpdateContainer();

	}

	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		columnsJson = request.getParameter("columnsJson");
		savePreset = Boolean.parseBoolean(request.getParameter("savePreset"));
		return super.handleUserActions(request);
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer uc = new UpdateContainer();
		try {
			Import imp = createImport(workspace);
			final Worksheet wsht = imp.generateWorksheet();
			if (hasRevisionId()) {
				Worksheet revisedWorksheet = workspace.getWorksheet(getRevisionId());
				wsht.setRevisedWorksheet(revisedWorksheet);  
			}
			uc.add(new WorksheetListUpdate());
			uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId()));
			if (savePreset) {
				final String jsonFileName = workspace.getCommandPreferencesId() + wsht.getId() + "-" + 
						wsht.getTitle().replaceAll("\\.", "_") +  "-preset"+".json"; 
				final String jsonFileLocalPath = ServletContextParameterMap.getParameterValue(ContextParameter.JSON_PUBLISH_DIR) +  
						jsonFileName;
				PrintWriter printWriter = new PrintWriter(jsonFileLocalPath);
				printWriter.println(new JSONArray(columnsJson).toString(4));
				printWriter.close();
				uc.add(new AbstractUpdate() {
					@Override
					public void generateJson(String prefix, PrintWriter pw,	VWorkspace vWorkspace) {
						JSONObject outputObject = new JSONObject();
						try {
							outputObject.put(JsonKeys.updateType.name(),
									"PublishPresetUpdate");
							outputObject.put(JsonKeys.fileUrl.name(), 
									ServletContextParameterMap.getParameterValue(ContextParameter.JSON_PUBLISH_RELATIVE_DIR) + jsonFileName);
							outputObject.put(JsonKeys.worksheetId.name(),
									wsht.getId());
							pw.println(outputObject.toString(4));

						} catch (JSONException e) {
							logger.error("Error occured while generating JSON!");
						}
					}
				});
			}
		} catch (JSONException | IOException | KarmaException | NullPointerException | ClassNotFoundException e) {
			logger.error("Error occured while generating worksheet from " + getTitle() + "!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured during import: " + e.getMessage()));
		}
		return uc;
	}

	private JSONArray generateSelectTree(String columnsJson, boolean visible) {
		if (columnsJson == null || columnsJson.trim().isEmpty())
			return null;
		JSONArray array = new JSONArray(columnsJson);
		JSONArray tree = new JSONArray();
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			JSONObject newObj = new JSONObject();
			boolean v = Boolean.parseBoolean(obj.get(JsonKeys.visible.name()).toString());
			newObj.put(obj.getString(JsonKeys.name.name()), v & visible);
			if (obj.has(JsonKeys.children.name())) {
				String value = obj.get(JsonKeys.children.name()).toString();
				newObj.put(JsonKeys.children.name(), generateSelectTree(value, v & visible));
			}
			tree.put(newObj);
		}
		return tree;
	}

}
