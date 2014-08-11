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
import java.io.PrintWriter;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ImportPropertiesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.view.VWorkspace;

public class ImportJSONFileCommand extends ImportFileCommand implements IPreviewable {
	private String columnsJson;
	private enum JsonKeys {
		worksheetId, columns, name, id, visible, hideable, children
	}
    
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
    	System.out.println(columnsJson);
        return new JsonImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines);
    }
    
    @Override
    public UpdateContainer showPreview(HttpServletRequest request, VWorkspace vWorkspace) throws CommandException {
    	boolean filter = Boolean.parseBoolean(request.getParameter("filter"));
    	if (!filter)
    		return super.showPreview(request, vWorkspace);
    	final Workspace workspace = WorkspaceManager.getInstance().createWorkspace();
    	Import imp = new JsonImport(getFile(), getFile().getName(), workspace, encoding, 1000);
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
    
    public UpdateContainer handleUserActions(HttpServletRequest request, VWorkspace vWorkspace) {
    	columnsJson = request.getParameter("columnsJson");
    	return super.handleUserActions(request, vWorkspace);
    }
   
}
