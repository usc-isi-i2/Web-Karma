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
 * ****************************************************************************
 */
package edu.isi.karma.controller.command.importdata;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;

import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.XMLImport;
import edu.isi.karma.rep.Workspace;

public class ImportXMLFileCommand extends ImportFileCommand implements IPreviewable {
	    
	protected ImportXMLFileCommand(String id, String model, File uploadedFile) {
        super(id, model, uploadedFile);
    }

    protected ImportXMLFileCommand(String id, String model, String revisedId, File uploadedFile) {
        super(id, model, revisedId, uploadedFile);
    }

    @Override
    public String getTitle() {
        return "Import XML File";
    }

    @Override
    public String getDescription() {
        if (isExecuted()) {
            return getFile().getName() + " imported";
        }
        return "";
    }
    
    @Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		columnsJson = request.getParameter("columnsJson");
		savePreset = Boolean.parseBoolean(request.getParameter("savePreset"));
		return super.handleUserActions(request);
	}

    @Override
    protected Import createImport(Workspace workspace) {
    	JSONArray tree = generateSelectTree(columnsJson, true);
        return new XMLImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines, tree);
    }
    
    @Override
    protected Import createImport(Workspace workspace, int sampleSize) {
        return new XMLImport(getFile(), getFile().getName(), workspace, encoding, sampleSize, null);
    }
    
  
}
