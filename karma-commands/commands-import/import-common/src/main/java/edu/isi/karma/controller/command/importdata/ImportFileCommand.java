/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.controller.command.importdata;

import java.io.File;
import java.io.FileNotFoundException;
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
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.ImportPropertiesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;
import edu.isi.karma.webserver.WorkspaceKarmaHomeRegistry;

/**
 * This abstract class in an interface to all Commands that import data from files
 * 
 * @author mielvandersande
 */
public abstract class ImportFileCommand extends ImportCommand implements IPreviewable{

	private static Logger logger = LoggerFactory
			.getLogger(ImportFileCommand.class);
	protected File file;
	protected String encoding;
	protected int maxNumLines = 10000;
	protected boolean savePreset = false;
	protected String columnsJson;

	
	protected enum JsonKeys {
		worksheetId, columns, name, id, visible, hideable, children, updateType, fileUrl
	}

	public ImportFileCommand(String id, String model, File file) {
		super(id, model);
		this.file = file;
		this.encoding = EncodingDetector.detect(file);
	}

	public ImportFileCommand(String id, String model, String revisionId, File file) {
		super(id, model, revisionId);
		this.file = file;
		this.encoding = EncodingDetector.detect(file);
	}

	@Override
	public String getDescription() {
		if (isExecuted()) {
			return getFile().getName() + " imported";
		}
		return "";
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	public File getFile() {
		return file;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public void setMaxNumLines(int lines) {
		this.maxNumLines = lines;
	}


	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {

		String strEncoding = request.getParameter("encoding");
		if(strEncoding == null || strEncoding == "") {
			try {
				strEncoding = EncodingDetector.detect(getFile());
			} catch(Exception e) {
				strEncoding = EncodingDetector.DEFAULT_ENCODING;
			}
		}
		setEncoding(strEncoding);

		String maxNumLines = request.getParameter("maxNumLines");
		if(maxNumLines != null && maxNumLines != "") {
			try {
				int num = Integer.parseInt(maxNumLines);
				setMaxNumLines(num);
			} catch (Throwable t) {
				logger.error("Wrong user input for Data Number of Lines to import");
				return null;
			}
		}
		/**
		 * Send response based on the interaction type *
		 */
		UpdateContainer c = null;
		ImportFileInteractionType type = ImportFileInteractionType.valueOf(request
				.getParameter("interactionType"));
		switch (type) {
		case generatePreview: {
			try {

				c = showPreview(request);
			} catch (CommandException e) {
				logger.error(
						"Error occured while creating output",
						e);
			}
			return c;
		}
		case importTable:
			return c;
		default:
			break;
		}
		return c;
	}

	@Override
	public UpdateContainer showPreview(HttpServletRequest request) throws CommandException {
		boolean filter = Boolean.parseBoolean(request.getParameter("filter"));
		UpdateContainer uc = new UpdateContainer(new ImportPropertiesUpdate(getFile(), encoding, maxNumLines, id));
		if (!filter)
			return uc;
		String workspaceId = request.getParameter("workspaceId");
		
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(WorkspaceKarmaHomeRegistry.getInstance().getKarmaHome(workspaceId));
		final Workspace workspace = WorkspaceManager.getInstance().createWorkspace(contextParameters.getId());
		Import imp = createImport(workspace, 1000);
		try {
			final Worksheet worksheet = imp.generateWorksheet();						
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
							if (children.length() > 0)
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
			throw new CommandException(this, "An error has occured!");
		}

	}
	
	protected abstract Import createImport(Workspace workspace, int sampleSize);
	
	protected UpdateContainer savePreset(Workspace workspace, final Worksheet wsht) throws FileNotFoundException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		final String jsonFileName = workspace.getCommandPreferencesId() + wsht.getId() + "-" + 
				wsht.getTitle().replaceAll("\\.", "_") +  "-preset"+".json"; 
		final String jsonFileLocalPath = contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_DIR) +  
				jsonFileName;
		PrintWriter printWriter = new PrintWriter(jsonFileLocalPath);
		printWriter.println(new JSONArray(columnsJson).toString(4));
		printWriter.close();
		return new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,	VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(),
							"PublishPresetUpdate");
					outputObject.put(JsonKeys.fileUrl.name(), 
							contextParameters.getParameterValue(ContextParameter.JSON_PUBLISH_RELATIVE_DIR) + jsonFileName);
					outputObject.put(JsonKeys.worksheetId.name(),
							wsht.getId());
					pw.println(outputObject.toString(4));

				} catch (JSONException e) {
					logger.error("Error occured while generating JSON!");
				}
			}
		});
	}
	
	protected JSONArray generateSelectTree(String columnsJson, boolean visible) {
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
			uc.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId(), SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
			if (savePreset) {
				uc.append(savePreset(workspace, wsht));
			}
		} catch (JSONException | IOException | KarmaException | NullPointerException | ClassNotFoundException e) {
			logger.error("Error occured while generating worksheet from " + getTitle() + "!", e);
			return new UpdateContainer(new ErrorUpdate(
					"Error occured during import: " + e.getMessage()));
		}
		return uc;
	}
}
