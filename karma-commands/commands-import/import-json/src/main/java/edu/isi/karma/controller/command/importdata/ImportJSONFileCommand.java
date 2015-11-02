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

	public ImportJSONFileCommand(String id, String model, File file) {
		super(id, model, file);
	}

	public ImportJSONFileCommand(String id, String model, String revisedId, File file) {
		super(id, model, revisedId, file);
		this.encoding = EncodingDetector.detect(file);
	}

	@Override
	public String getTitle() {
		return "Import JSON File";
	}


	@Override
	protected Import createImport(Workspace workspace) {
		JSONArray tree = generateSelectTree(columnsJson, true);
		try {
			return new JsonImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines, tree,false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	protected Import createImport(Workspace workspace, int sampleSize) {
		try {
			return new JsonImport(getFile(), getFile().getName(), workspace, encoding, sampleSize, null,false);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public UpdateContainer handleUserActions(HttpServletRequest request) {
		columnsJson = request.getParameter("columnsJson");
		savePreset = Boolean.parseBoolean(request.getParameter("savePreset"));
		return super.handleUserActions(request);
	}

}
