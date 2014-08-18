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

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;

import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;

public class ImportJSONFileCommand extends ImportFileCommand implements IPreviewable {

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
	protected Import createImport(Workspace workspace, int sampleSize) {
		return new JsonImport(getFile(), getFile().getName(), workspace, encoding, sampleSize, null);
	}
	
	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		columnsJson = request.getParameter("columnsJson");
		savePreset = Boolean.parseBoolean(request.getParameter("savePreset"));
		return super.handleUserActions(request);
	}

}
