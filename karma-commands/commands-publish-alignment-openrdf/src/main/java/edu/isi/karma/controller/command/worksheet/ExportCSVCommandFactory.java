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
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandFactory;
import edu.isi.karma.rep.Workspace;

public class ExportCSVCommandFactory extends CommandFactory {

	private static Logger logger = LoggerFactory.getLogger(ExportCSVCommandFactory.class);
	public enum Arguments {
		worksheetId, columnList, tripleStoreUrl, graphUrl, rootNodeId, model_graph
	}
	
	@Override
	public Command createCommand(HttpServletRequest request,
			Workspace workspace) {
		String worksheetId = request.getParameter(Arguments.worksheetId.name());
		String tripleStoreUrl = request.getParameter(Arguments.tripleStoreUrl.name());
		String graphUrl = request.getParameter(Arguments.graphUrl.name());
		String nodeId = request.getParameter(Arguments.rootNodeId.name());
		String colList = request.getParameter(Arguments.columnList.name());
		String model_graph = request.getParameter(Arguments.model_graph.name());
		
		return new ExportCSVCommand(getNewId(workspace), worksheetId, nodeId, tripleStoreUrl, graphUrl, formatColumnInformation(colList), model_graph );
	}
	
	private ArrayList<HashMap<String, String>> formatColumnInformation(String jsonArrayColumns) {
		ArrayList<HashMap<String, String>> cols = new ArrayList<HashMap<String, String>>();
		try {
			
			JSONObject arr = new JSONObject(jsonArrayColumns);
			for(int i=0; i<arr.keySet().size(); i++) {
				JSONObject obj = arr.getJSONObject(String.valueOf(i));
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("url", obj.getString("url"));
				map.put("name", obj.getString("name"));
				cols.add(map);
				logger.info(obj.getString("url") + "  --->  " + obj.getString("name"));
			}
			return cols;
		} catch (Exception e) {
			logger.error("Error parsing column list",e);
		}
		return null;
	}
	
	public Command createCommand( Workspace workspace, String worksheetId, String nodeId, String tripleStoreUrl, String graphUrl, ArrayList<HashMap<String, String>> columns ) {
		return new ExportCSVCommand(getNewId(workspace), worksheetId, nodeId, tripleStoreUrl, graphUrl, columns, null );
	}

	@Override
	public Class<? extends Command> getCorrespondingCommand() {
	
		return ExportCSVCommand.class;
	}

}
