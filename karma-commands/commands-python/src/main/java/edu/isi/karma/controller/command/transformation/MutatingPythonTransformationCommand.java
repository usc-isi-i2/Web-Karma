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

package edu.isi.karma.controller.command.transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.worksheet.AddValuesCommand;
import edu.isi.karma.controller.command.worksheet.AddValuesCommandFactory;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommand;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNode.HNodeType;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;

public abstract class MutatingPythonTransformationCommand extends
		PythonTransformationCommand {

	private static Logger logger = LoggerFactory.getLogger(MutatingPythonTransformationCommand.class);
	
	protected final String newColumnName;
	protected List<Node> affectedNodes = new LinkedList<>();
	protected boolean isJSONOutput = false;
	public MutatingPythonTransformationCommand(String id, String model, String newColumnName,
			String transformationCode, String worksheetId, String hNodeId,
			String errorDefaultValue, String selectionId, boolean isJSONOutput) {
		super(id, model, transformationCode, worksheetId, hNodeId,
				errorDefaultValue, selectionId);
		this.newColumnName = newColumnName;
		this.isJSONOutput = isJSONOutput;
	}

	@Override
	public String getDescription()
	{
		return newColumnName;
	}

	protected UpdateContainer applyPythonTransformation(Workspace workspace,
			Worksheet worksheet, RepFactory f, HNode hNode,
			ExecutionController ctrl, String newHNodeId) {
	
		
		try {
			JSONArray transformedRows = new JSONArray();
			JSONArray errorValues = new JSONArray();
			generateTransformedValues(workspace, 
					worksheet, f, hNode, transformedRows, errorValues, null);

			// Invoke the MultipleValueEditColumnCommand
			JSONArray multiCellEditInput = getMultiCellValueEditInputJSON(transformedRows, newHNodeId);
			MultipleValueEditColumnCommandFactory mfc = (MultipleValueEditColumnCommandFactory)
					ctrl.getCommandFactoryMap().get(MultipleValueEditColumnCommand.class.getSimpleName());
			MultipleValueEditColumnCommand mvecc = (MultipleValueEditColumnCommand) mfc.createCommand(multiCellEditInput, model, workspace);
			mvecc.doIt(workspace);
			
		} catch (Exception e) {
			logger.error("Error occured during python transformation.",e);
			return new UpdateContainer(new ErrorUpdate("Error occured while applying Python transformation to the column."));
		}

		worksheet.getMetadataContainer().getColumnMetadata().addColumnPythonTransformation(newHNodeId, this.transformationCode);
		worksheet.getMetadataContainer().getColumnMetadata().addPreviousCommandId(newHNodeId, this.id);
		worksheet.getMetadataContainer().getColumnMetadata().addColumnDerivedFrom(newHNodeId, hNodeId);
		// Prepare the output container
		UpdateContainer c = new UpdateContainer();
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));
		
		/** Add the alignment update **/
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		
		c.add(new InfoUpdate("Transformation complete"));
		return c;
	}

	private JSONArray getMultiCellValueEditInputJSON(JSONArray rowsArray, String newHNodeId) throws JSONException {
		JSONArray arr = new JSONArray();
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.worksheetId.name(), 
				worksheetId, ParameterType.worksheetId));
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.hNodeID.name(), 
				newHNodeId, ParameterType.worksheetId));
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.rows.name(), 
				rowsArray, ParameterType.other));
		return arr;
	}
	
	protected Map<String, String> gatherTransformedResults(Workspace workspace, String hNodeId) {
		Map<String, String> rowToValueMapping = new HashMap<>();
		HNodePath hNodePath = workspace.getFactory().getHNode(hNodeId).getHNodePath(workspace.getFactory());
		List<Node> nodes = new ArrayList<>();
		workspace.getWorksheet(worksheetId).getDataTable().collectNodes(hNodePath, nodes, getSuperSelection(workspace));
		for (Node node : nodes) {
			rowToValueMapping.put(node.getBelongsToRow().getId(), node.getValue().asString());
			node.clearValue(NodeStatus.original);
			affectedNodes.add(node);
		}
		return rowToValueMapping;
	}
	
	protected void handleJSONOutput(Workspace workspace, Map<String, String> mapping, HNode newNode) throws JSONException, KarmaException, CommandException {
		String name = newNode.getColumnName();
		AddValuesCommandFactory addFactory = new AddValuesCommandFactory();
		for (Entry<String, String> entry : mapping.entrySet()) {
			JSONArray array = new JSONArray();
			JSONObject obj2 = new JSONObject();
			Object obj = entry.getValue();
			try {
				obj = new JSONObject(entry.getValue());
			}
			catch(Exception e) 
			{}
			try {
				obj = new JSONArray(entry.getValue());
			}
			catch(Exception e) 
			{}
			obj2.put("rowId", entry.getKey());
			obj2.put("rowIdHash", "");
			obj2.put("values", obj);
			array.put(obj2);
			JSONArray input = new JSONArray();
			JSONObject obj3 = new JSONObject();
			obj3.put("name", "AddValues");
			obj3.put("value", array.toString());
			obj3.put("type", "other");
			input.put(obj3);
			AddValuesCommand command = (AddValuesCommand) addFactory.createCommand(input, model, workspace, hNodeId, worksheetId, newNode.getHTableId(), name, HNodeType.Transformation, getSuperSelection(workspace).getName());
			command.doIt(workspace);
		}
	}
}
