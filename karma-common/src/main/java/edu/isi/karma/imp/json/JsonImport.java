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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.ColumnMetadata.DataStructure;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.metadata.WorksheetProperties.Property;
import edu.isi.karma.rep.metadata.WorksheetProperties.SourceTypes;
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
	private Workspace workspace;
	private JSONArray columnsJson;
	private String worksheetName;
	private String encoding;
	
	private class FileObject {
		File file;
		String encoding;
		public FileObject(File file, String encoding) {
			this.file = file;
			this.encoding = encoding;
		}
		
	}
	
	public JsonImport(Object json, String worksheetName, Workspace workspace,
			String encoding, int maxNumLines) {
		super(worksheetName, workspace, encoding);
		this.json = json;
		this.workspace = workspace;
		this.maxNumLines = maxNumLines;
		this.worksheetName = worksheetName;
		this.encoding = encoding;
	}

	public JsonImport duplicate() {
		return new JsonImport(this.json, this.worksheetName, this.workspace, this.encoding, this.maxNumLines);
	}
	
	public JsonImport(File jsonFile, String worksheetName, Workspace workspace,String encoding, int maxNumLines, JSONArray tree,boolean isJSONLines) throws FileNotFoundException, Exception {
		
		super(worksheetName, workspace, encoding);
		FileObject fo = new FileObject(jsonFile, encoding);

		if(isJSONLines){
			this.json = JSONUtil.convertJSONLinesToJSONArray(new FileInputStream(fo.file), fo.encoding);
		}
		else{
			this.json = fo;
		}
		this.workspace = workspace;
		this.maxNumLines = maxNumLines;
		this.columnsJson = tree;
		this.worksheetName = worksheetName;
		this.encoding = encoding;
		
	}

	public JsonImport(String jsonString, String worksheetName,
			Workspace workspace, String encoding, int maxNumLines) {
		this(JSONUtil.createJson(jsonString), worksheetName, workspace,
				encoding, maxNumLines);
	}

	public JsonImport(String jsonString, RepFactory repFactory, Worksheet wk, Workspace workspace, 
			int maxNumLines) {
		this(JSONUtil.createJson(jsonString), repFactory, wk, workspace, maxNumLines);
	}

	public JsonImport(Object json, RepFactory repFactory, Worksheet wk, Workspace workspace, 
			int maxNumLines) {
		super(repFactory, wk);
		this.json = json;
		this.workspace = workspace;
		this.maxNumLines = maxNumLines;
		this.worksheetName = wk.getTitle();
		this.encoding = "UTF-8";
	}
	
	public JsonImport(File json, RepFactory repFactory, Worksheet wk, Workspace workspace, 
			int maxNumLines, JSONArray columnsJson) {
		super(repFactory, wk);
		FileObject fo = new FileObject(json, "UTF-8");
		this.json = fo;
		this.workspace = workspace;
		this.maxNumLines = maxNumLines;
		this.columnsJson = columnsJson;
		this.worksheetName = wk.getTitle();
		this.encoding = "UTF-8";
	}
	

	@Override
	public Worksheet generateWorksheet() throws JSONException {
		int numObjects = 0;
		if (json instanceof JSONArray) {
			getWorksheet().getMetadataContainer().getWorksheetProperties().setWorksheetDataStructure(DataStructure.COLLECTION);
			JSONArray a = (JSONArray) json;
			for (int i = 0; i < a.length(); i++) {
				JsonImportValues JsonImportValues = new JsonImportValues(maxNumLines, numObjects, getFactory(), getWorksheet(), columnsJson);
				JsonImportValues.addListElement(a.get(i), getWorksheet().getHeaders(),
						getWorksheet().getDataTable());
				numObjects = JsonImportValues.getNumberOfObjectsImported();
				if (maxNumLines > 0 && numObjects >= maxNumLines)
					break;
			}
		} else if (json instanceof JSONObject) {
			getWorksheet().getMetadataContainer().getWorksheetProperties().setWorksheetDataStructure(DataStructure.OBJECT);
			JsonImportValues JsonImportValues = new JsonImportValues(maxNumLines, numObjects, getFactory(), getWorksheet(), columnsJson);
			JsonImportValues.addKeysAndValues((JSONObject) json, getWorksheet().getHeaders(),
					getWorksheet().getDataTable());
		}
		else if (json != null && json instanceof FileObject) {
			FileObject fo = (FileObject)json;
			boolean flag = true;
			try {
				JSONTokener tokener = new JSONTokener(new InputStreamReader(new FileInputStream(fo.file), fo.encoding));
				char c = tokener.nextClean();			
				if (c == '{') {
					getWorksheet().getMetadataContainer().getWorksheetProperties().setWorksheetDataStructure(DataStructure.OBJECT);
					JsonImportValues JsonImportValues = new JsonImportValues(maxNumLines, numObjects, getFactory(), getWorksheet(), columnsJson);
					JsonImportValues.addKeysAndValues(tokener, getWorksheet().getHeaders(),
							getWorksheet().getDataTable());
				}
				else if (c == '['){
					flag = false;
					getWorksheet().getMetadataContainer().getWorksheetProperties().setWorksheetDataStructure(DataStructure.COLLECTION);
					JsonImportValues JsonImportValues = new JsonImportValues(maxNumLines, numObjects, getFactory(), getWorksheet(), columnsJson);
					JsonImportValues.addListElement(tokener, getWorksheet().getHeaders(), getWorksheet().getDataTable());
				}
			}catch(Exception e) {
				String worksheetname = getWorksheet().getHeaders().getTableName();
				String encoding = getWorksheet().getEncoding();
				workspace.removeWorksheet(getWorksheet().getId());
				getFactory().removeWorksheet(getWorksheet().getId(), workspace.getCommandHistory());
				createWorksheet(worksheetname, workspace, encoding);
				if (flag)
					getWorksheet().getMetadataContainer().getWorksheetProperties().setWorksheetDataStructure(DataStructure.OBJECT);
				else
					getWorksheet().getMetadataContainer().getWorksheetProperties().setWorksheetDataStructure(DataStructure.COLLECTION);
				logger.error("Parsing failure", e);
			}
		}
		Worksheet ws = getWorksheet();
		ws.getMetadataContainer().getWorksheetProperties().setPropertyValue(Property.sourceType, SourceTypes.JSON.toString());
		return ws;
	}

}
