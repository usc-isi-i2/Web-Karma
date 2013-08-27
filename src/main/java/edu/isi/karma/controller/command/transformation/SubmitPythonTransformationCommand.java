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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.worksheet.AddColumnCommand;
import edu.isi.karma.controller.command.worksheet.AddColumnCommandFactory;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommand;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.transformation.PythonTransformationHelper;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class SubmitPythonTransformationCommand extends Command {
	final private String newColumnName;
	final private String transformationCode;
	final private String vWorksheetId;
	final private String hNodeId;
	final private String hTableId;
	final private String errorDefaultValue;
	private AddColumnCommand addColCmd;
	
	private static Logger logger = LoggerFactory
			.getLogger(SubmitPythonTransformationCommand.class);

	public SubmitPythonTransformationCommand(String id, String newColumnName, String transformationCode, 
			String vWorksheetId, String hNodeId, String hTableId, String errorDefaultValue) {
		super(id);
		this.newColumnName = newColumnName;
		this.transformationCode = transformationCode;
		this.vWorksheetId = vWorksheetId;
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		this.errorDefaultValue = errorDefaultValue;
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Python Transformation";
	}

	@Override
	public String getDescription() {
		return newColumnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		RepFactory f = vWorkspace.getRepFactory();
		HNode hNode = f.getHNode(hNodeId);
		ExecutionController ctrl = WorkspaceRegistry.getInstance().getExecutionController(
				vWorkspace.getWorkspace().getId());
		
		List<HNode> accessibleHNodes = f.getHNode(hNodeId).getHNodesAccessibleList(f);
		List<HNode> nodesWithNestedTable = new ArrayList<HNode>();
		Map<String, String> hNodeIdtoColumnNameMap = new HashMap<String, String>();
		for (HNode accessibleHNode:accessibleHNodes) {
			if(accessibleHNode.hasNestedTable()) {
				nodesWithNestedTable.add(accessibleHNode);
			} else {
				hNodeIdtoColumnNameMap.put(accessibleHNode.getId(), accessibleHNode.getColumnName());
			}
		}
		accessibleHNodes.removeAll(nodesWithNestedTable);
		
		PythonTransformationHelper pyHelper = new PythonTransformationHelper();
		Map<String,String> normalizedColumnNameMap = new HashMap<String,String>();
		String clsStmt = pyHelper.getPythonClassCreationStatement(worksheet, normalizedColumnNameMap, accessibleHNodes);
		String transformMethodStmt = pyHelper.getPythonTransformMethodDefinitionState(worksheet, transformationCode);
		String columnNameDictStmt = pyHelper.getColumnNameDictionaryStatement(normalizedColumnNameMap);
		String defGetValueStmt = pyHelper.getGetValueDefStatement(normalizedColumnNameMap);
		
		
		// Create a map from hNodeId to normalized column name
		Map<String, String> hNodeIdToNormalizedColumnName = new HashMap<String, String>();
		for (HNode accHNode:accessibleHNodes) {
			hNodeIdToNormalizedColumnName.put(accHNode.getId(), 
					normalizedColumnNameMap.get(accHNode.getColumnName()));
		}
		
		try {
			// Prepare the Python interpreter
			PythonInterpreter interpreter = new PythonInterpreter();
			interpreter.exec(pyHelper.getImportStatements());
			interpreter.exec(clsStmt);
			interpreter.exec(columnNameDictStmt);
			interpreter.exec(defGetValueStmt);
			interpreter.exec(transformMethodStmt);
			
			Collection<Node> nodes = new ArrayList<Node>();
			worksheet.getDataTable().collectNodes(hNode.getHNodePath(f), nodes);
			
			Map<String, String> rowToValueMap = new HashMap<String, String>();
			
			// Go through all nodes collected for the column with given hNodeId
			for (Node node:nodes) {
				Row row = node.getBelongsToRow();
				Map<String, String> values = node.getColumnValues();
				StringBuilder objectCreationStmt = new StringBuilder();
				objectCreationStmt.append("r = " + pyHelper.normalizeString(worksheet.getTitle()) + "(");
				
				int keyCounter = 0;
				for (String valHNodeId : values.keySet()) {
					String nodeId = values.get(valHNodeId);
					String nodeVal = f.getNode(nodeId).getValue().asString();
					
					if (keyCounter++ != 0)
						objectCreationStmt.append(",");
					if (nodeVal == null || nodeVal.isEmpty()) {
						objectCreationStmt.append(hNodeIdToNormalizedColumnName.get(valHNodeId) + "=\"\"");
					} else {
						nodeVal = nodeVal.replaceAll("\"", "\\\\\"");
						nodeVal = nodeVal.replaceAll("\n", " ");
						objectCreationStmt.append(hNodeIdToNormalizedColumnName.get(valHNodeId) + "=\"" + nodeVal + "\"");
					}
				}
				objectCreationStmt.append(")");
				
				interpreter.exec(objectCreationStmt.toString());
				try {
					PyObject output = interpreter.eval("transform(r)");
					// Execution ran successfully, put the value returned in the HashMap
					rowToValueMap.put(row.getId(), pyHelper.getPyObjectValueAsString(output));
				} catch (PyException p) {
					p.printStackTrace();
					// Error occured in the Python method execution
					rowToValueMap.put(row.getId(), errorDefaultValue);
				} catch (Exception t) {
					// Error occured in the Python method execution
					t.printStackTrace();
					logger.debug("Error occured while transforming.", t);
					rowToValueMap.put(row.getId(), errorDefaultValue);
				}
			}
			
			// Invoke the add column command
			JSONArray addColumnInput = getAddColumnCommandInputJSON();
			AddColumnCommandFactory addColumnFac = (AddColumnCommandFactory)ctrl.
					getCommandFactoryMap().get(AddColumnCommand.class.getSimpleName());
			addColCmd = (AddColumnCommand) addColumnFac.createCommand(addColumnInput, vWorkspace);
			addColCmd.saveInHistory(false);
			addColCmd.doIt(vWorkspace);
			
			// Invoke the MultipleValueEditColumnCommand
			JSONArray multiCellEditInput = getMultiCellValueEditInputJSON(rowToValueMap, addColCmd.getNewHNodeId());
			MultipleValueEditColumnCommandFactory mfc = (MultipleValueEditColumnCommandFactory)
					ctrl.getCommandFactoryMap().get(MultipleValueEditColumnCommand.class.getSimpleName());
			MultipleValueEditColumnCommand mvecc = (MultipleValueEditColumnCommand) mfc.createCommand(multiCellEditInput, vWorkspace);
			mvecc.doIt(vWorkspace);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Error occured during python transformation.",e);
			return new UpdateContainer(new ErrorUpdate("Error occured while applying Python transformation to the column."));
		}
		
		// Prepare the output container
		UpdateContainer c = new UpdateContainer();
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet,worksheet.getHeaders().getAllPaths(), vWorkspace);
		vWorkspace.getViewFactory().getVWorksheet(this.vWorksheetId).update(c);
		
		/** Add the alignment update **/
		 addAlignmentUpdate(c, vWorkspace, worksheet);
		
		c.add(new InfoUpdate("Transformation complete"));
		return c;
	}

	private JSONArray getMultiCellValueEditInputJSON(Map<String, String> rowToValueMap, String newHNodeId) throws JSONException {
		JSONArray arr = new JSONArray();
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.vWorksheetID.name(), 
				vWorksheetId, ParameterType.vWorksheetId));
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.hNodeID.name(), 
				newHNodeId, ParameterType.vWorksheetId));
		JSONArray rowsArray = new JSONArray();
		for (String rowId: rowToValueMap.keySet()) {
			JSONObject row = new JSONObject();
			row.put(MultipleValueEditColumnCommandFactory.Arguments.rowID.name(), rowId);
			row.put(MultipleValueEditColumnCommandFactory.Arguments.value.name(), rowToValueMap.get(rowId));
			rowsArray.put(row);
		}
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.rows.name(), 
				rowsArray, ParameterType.other));
		return arr;
	}

	private JSONArray getAddColumnCommandInputJSON() throws JSONException {
		JSONArray arr = new JSONArray();
		arr.put(CommandInputJSONUtil.createJsonObject(AddColumnCommandFactory.Arguments.newColumnName.name(), newColumnName, ParameterType.other));
		arr.put(CommandInputJSONUtil.createJsonObject(AddColumnCommandFactory.Arguments.hTableId.name(), hTableId, ParameterType.other));
		arr.put(CommandInputJSONUtil.createJsonObject(AddColumnCommandFactory.Arguments.vWorksheetId.name(), vWorksheetId, ParameterType.vWorksheetId));
		arr.put(CommandInputJSONUtil.createJsonObject(AddColumnCommandFactory.Arguments.hNodeId.name(), hNodeId, ParameterType.vWorksheetId));
		return arr;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		addColCmd.undoIt(vWorkspace);
		
		UpdateContainer c = new UpdateContainer();
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet,worksheet.getHeaders().getAllPaths(), vWorkspace);
		vWorkspace.getViewFactory().getVWorksheet(this.vWorksheetId).update(c);
		return c;
	}

	private void addAlignmentUpdate(UpdateContainer c, VWorkspace vWorkspace, Worksheet worksheet) {
		String alignmentId = AlignmentManager.Instance().constructAlignmentId(
				vWorkspace.getWorkspace().getId(), vWorksheetId);
		Alignment alignment = AlignmentManager.Instance().getAlignment(alignmentId);
		if (alignment == null) {
			alignment = new Alignment(vWorkspace.getWorkspace().getOntologyManager());
			AlignmentManager.Instance().addAlignmentToMap(alignmentId, alignment);
		}
		// Compute the semantic type suggestions
		SemanticTypeUtil.computeSemanticTypesSuggestion(worksheet, vWorkspace.getWorkspace()
				.getCrfModelHandler(), vWorkspace.getWorkspace().getOntologyManager(), alignment);
		c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, alignment));
		c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vWorkspace.getViewFactory().
				getVWorksheet(vWorksheetId), alignment));
	}
}
