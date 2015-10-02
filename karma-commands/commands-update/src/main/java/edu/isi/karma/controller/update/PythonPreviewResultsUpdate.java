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

package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.view.VWorkspace;

public class PythonPreviewResultsUpdate extends AbstractUpdate{
	
	private static Logger logger = LoggerFactory.getLogger(PythonPreviewResultsUpdate.class);
	private enum JsonKeys {
		updateType, result, errors, row, error
	}
	private JSONArray transformedRows;
	private JSONArray errorValues;
	
	public PythonPreviewResultsUpdate(JSONArray transformedRows, JSONArray errorValues)
	{
		super();
		this.transformedRows = transformedRows;
		this.errorValues = errorValues;
	}
	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		try {
			JSONObject outputObject = new JSONObject();
			outputObject.put(JsonKeys.updateType.name(), "PythonPreviewResultsUpdate");
			
			outputObject.put(JsonKeys.result.name(), transformedRows);
			outputObject.put(JsonKeys.errors.name(), errorValues);
			pw.println(outputObject.toString());
		} catch (JSONException e) {
			logger.error("Error while creating output update.", e);
			new ErrorUpdate("Error while creating Python results preview.").generateJson(prefix, pw, vWorkspace);
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof PythonPreviewResultsUpdate) {
			PythonPreviewResultsUpdate t = (PythonPreviewResultsUpdate)o;
			return t.errorValues.equals(errorValues) && t.transformedRows.equals(transformedRows);
		}
		return false;
	}
}
