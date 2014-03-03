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
package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.controller.command.JSONInputCommandFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class ExportCSVCommandFactory extends CommandFactory {

	private static Logger logger = LoggerFactory.getLogger(ExportCSVCommand.class);
	public enum Arguments {
		worksheetId, columnList, tripleStoreUrl, graphUrl, rootNodeId
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String tripleStoreUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		String graphUrl = request.getParameter(Arguments.graphUrl.name());
		String nodeId = request.getParameter(Arguments.rootNodeId.name());
		String colList = request.getParameter(Arguments.columnList.name());
		ArrayList<String> cols = new ArrayList<String>();
		try {
			JSONArray arr = new JSONArray(colList);
			for(int i=0; i<arr.length(); i++) {
				cols.add(arr.getString(i));
			}
		} catch (Exception e) {
			logger.error("Error parsing column list",e);
		}
		return new ExportCSVCommand(getNewId(workspace), worksheetId, nodeId, tripleStoreUrl, graphUrl, cols );
	}

}
