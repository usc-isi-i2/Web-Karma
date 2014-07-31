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
/**
 *
 */
package edu.isi.karma.imp.json;


import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.ColumnMetadata.DataStructure;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.util.JSONUtil;

/**
 * @author szekely
 * @author mielvandersande
 * 
 */
public class JsonImport extends Import {

	private static Logger logger = LoggerFactory.getLogger(JsonImport.class);
	private final Object json;
	private int maxNumLines;
	private int numObjects;
	
	public JsonImport(Object json, String worksheetName, Workspace workspace,
			String encoding, int maxNumLines) {
		super(worksheetName, workspace, encoding);
		this.json = json;
		this.maxNumLines = maxNumLines;
	}

	public JsonImport(File jsonFile, String worksheetName, Workspace workspace,
			String encoding, int maxNumLines) {
		super(worksheetName, workspace, encoding);

		String fileContents = null;
		try {
			fileContents = FileUtil
					.readFileContentsToString(jsonFile, encoding);
		} catch (IOException ex) {
			logger.error("Error in reading the JSON file");
		}

		this.json = JSONUtil.createJson(fileContents);
		this.maxNumLines = maxNumLines;
	}

	public JsonImport(String jsonString, String worksheetName,
			Workspace workspace, String encoding, int maxNumLines) {
		this(JSONUtil.createJson(jsonString), worksheetName, workspace,
				encoding, maxNumLines);
	}

	public JsonImport(String jsonString, RepFactory repFactory, Worksheet wk,
			int maxNumLines) {
		this(JSONUtil.createJson(jsonString), repFactory, wk, maxNumLines);
	}

	public JsonImport(Object json, RepFactory repFactory, Worksheet wk,
			int maxNumLines) {
		super(repFactory, wk);
		this.json = json;
		this.maxNumLines = maxNumLines;
	}

	@Override
	public Worksheet generateWorksheet() throws JSONException {
		numObjects = 0;
		if (json instanceof JSONArray) {
			getWorksheet().getMetadataContainer().getWorksheetProperties().setWorksheetDataStructure(DataStructure.COLLECTION);
			JSONArray a = (JSONArray) json;
			for (int i = 0; i < a.length(); i++) {
				JsonImportValues.addListElement(a.get(i), getWorksheet().getHeaders(),
						getWorksheet().getDataTable(), maxNumLines, numObjects, getFactory(), getWorksheet());
				if (maxNumLines > 0 && numObjects >= maxNumLines)
					break;
			}
		} else if (json instanceof JSONObject) {
			getWorksheet().getMetadataContainer().getWorksheetProperties().setWorksheetDataStructure(DataStructure.OBJECT);
			JsonImportValues.addKeysAndValues((JSONObject) json, getWorksheet().getHeaders(),
					getWorksheet().getDataTable(), maxNumLines, numObjects, getFactory(), getWorksheet());
		}

		Worksheet ws = getWorksheet();
		ws.getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType, SourceTypes.JSON.toString());
		return ws;
	}
	
}
