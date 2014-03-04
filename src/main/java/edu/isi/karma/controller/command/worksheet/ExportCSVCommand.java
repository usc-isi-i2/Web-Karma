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

import java.io.PrintWriter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCommand;
import edu.isi.karma.controller.command.publish.PublishRDFCommand.PreferencesKeys;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.SPARQLGeneratorUtil;
import edu.isi.karma.er.helper.TripleStoreUtil;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMappingGenerator;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

/**
@author shri
 */
public class ExportCSVCommand extends WorksheetCommand {

	private final String rootNodeId;
	private final String tripleStoreUrl;
	private final String graphUrl;
	private final ArrayList<String> columnList;
	
	private static Logger logger = LoggerFactory.getLogger(ExportCSVCommand.class);
	
	public enum JsonKeys {
		worksheetId, columnList, tripleStoreUrl, graphUrl, rootNodeId
	}
	
	/**
	 * @param id
	 * @param worksheetId
	 * @param rootNode
	 * @param sparqlUrl
	 * @param graph
	 * @param nodes
	 * */
	protected ExportCSVCommand(String id, String worksheetId, String rootNode, String sparqlUrl, String graph, ArrayList<String> nodes ) {
		super(id, worksheetId);
		this.rootNodeId = rootNode;
		this.tripleStoreUrl = sparqlUrl;
		this.graphUrl = graph;
		this.columnList = nodes;
	}

	@Override
	public String getCommandName() {
		return ExportCSVCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Fetch Columns";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		try {

			return new UpdateContainer(new InfoUpdate("this is test message"));

		}catch (Exception e ) {
			String msg = "Error occured while fetching columns!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	

}
