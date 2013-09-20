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

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.worksheet.AddColumnCommand.JsonKeys;
import edu.isi.karma.view.VWorkspace;

public class AddColumnUpdate extends AbstractUpdate {

	private static Logger logger =Logger.getLogger(AddColumnUpdate.class);
	
	private final String newHNodeId;
	private final String worksheetId;

	public AddColumnUpdate(String newHNodeId, String worksheetId)
	{
		this.newHNodeId = newHNodeId;
		this.worksheetId = worksheetId;
	}
	
	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		JSONObject outputObject = new JSONObject();
		try {
			outputObject.put(JsonKeys.updateType.name(),
					"AddColumnUpdate");
			outputObject.put(JsonKeys.hNodeId.name(),newHNodeId);
			outputObject.put(JsonKeys.worksheetId.name(),
					worksheetId);
			pw.println(outputObject.toString(4));
		} catch (JSONException e) {
			logger.error("Error occured while generating JSON!");
		}
		
	}

}
