/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.isi.karma.controller.command.importdata;

import java.io.File;
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ImportPropertiesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.view.VWorkspace;

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
	protected int maxNumLines = 1000;
	
	protected enum JsonKeys {
		worksheetId, columns, name, id, visible, hideable, children, updateType, fileUrl
	}

	public ImportFileCommand(String id, File file) {
		super(id);
		this.file = file;
		this.encoding = EncodingDetector.detect(file);
	}

	public ImportFileCommand(String id, String revisionId, File file) {
		super(id, revisionId);
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
		}
		return c;
	}

	@Override
	public UpdateContainer showPreview(HttpServletRequest request) throws CommandException {
		boolean filter = Boolean.parseBoolean(request.getParameter("filter"));
		UpdateContainer uc = new UpdateContainer(new ImportPropertiesUpdate(getFile(), encoding, maxNumLines, id));
		if (!filter)
			return uc;
		final Workspace workspace = WorkspaceManager.getInstance().createWorkspace();
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
	
	protected abstract Import createImport(Workspace workspace, int sampleSize);
}
