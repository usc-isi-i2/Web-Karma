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

import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommandFactory;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.transformation.PythonTransformationHelper;

public abstract class PythonTransformationCommand extends WorksheetCommand {
	
	final protected String transformationCode;
	final protected String hNodeId;
	final protected String errorDefaultValue;
	
	private static Logger logger = LoggerFactory
			.getLogger(PythonTransformationCommand.class);

	private enum JsonKeys {
		 row, error
	}
	
	public PythonTransformationCommand(String id, String transformationCode, 
			String worksheetId, String hNodeId, String errorDefaultValue) {
		super(id, worksheetId);
		this.transformationCode = transformationCode;
		this.hNodeId = hNodeId;
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
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	protected void generateTransformedValues(Worksheet worksheet,
			RepFactory f, HNode hNode,  JSONArray transformedRows, JSONArray errorValues, Integer limit) throws JSONException {
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
		
		int counter = 0;
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
				String transformedValue = pyHelper.getPyObjectValueAsString(output);
				addTransformedValue(transformedRows, row, transformedValue);
				// Execution ran successfully, put the value returned in the HashMap
			//	rowToValueMap.put(row.getId(), pyHelper.getPyObjectValueAsString(output));
			} catch (PyException p) {
				p.printStackTrace();
				// Error occured in the Python method execution
				addTransformedValue(transformedRows, row, errorDefaultValue);
				addError(errorValues, row, counter, p.value);
			} catch (Exception t) {
				// Error occured in the Python method execution
				t.printStackTrace();
				logger.debug("Error occured while transforming.", t);
				rowToValueMap.put(row.getId(), errorDefaultValue);
			}
			if (limit != null && ++counter > 5) {
				break;
			}
		}
	}

	private void addError(JSONArray errorValues, Row row, int counter, PyObject value) throws JSONException {
		errorValues.put(new JSONObject().put(JsonKeys.row.name(), counter)
				.put(JsonKeys.error.name(), value));
		
	}

	private void addTransformedValue(JSONArray transformedRows, Row row,
			String transformedValue) throws JSONException {
		JSONObject transformedRow = new JSONObject();
		transformedRow.put(MultipleValueEditColumnCommandFactory.Arguments.rowID.name(), row.getId());
		transformedRow.put(MultipleValueEditColumnCommandFactory.Arguments.value.name(), transformedValue);
		transformedRows.put(transformedRow);
	}


	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		UpdateContainer c = (WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

}
