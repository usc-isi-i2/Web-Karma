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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommandFactory;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.er.helper.PythonTransformationHelper;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;


public abstract class PythonTransformationCommand extends WorksheetSelectionCommand {

	protected String transformationCode;
	final protected String hNodeId;
	final protected String errorDefaultValue;
	

	private static Logger logger = LoggerFactory
			.getLogger(PythonTransformationCommand.class);

	private enum JsonKeys {
		row, error
	}

	public PythonTransformationCommand(String id, String model, String transformationCode,
			String worksheetId, String hNodeId, String errorDefaultValue, String selectionId) {
		super(id, model, worksheetId, selectionId);
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

	protected void generateTransformedValues(Workspace workspace,
			Worksheet worksheet, RepFactory f, HNode hNode,
			JSONArray transformedRows, JSONArray errorValues, Integer limit)
					throws JSONException, IOException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		SuperSelection selection = getSuperSelection(worksheet);
		String trimmedTransformationCode = transformationCode.trim();
		// Pedro: somehow we are getting empty statements, and these are causing
		// exceptions.
		if (trimmedTransformationCode.isEmpty()) {
			trimmedTransformationCode = "return \"\"";
			logger.info("Empty PyTransform statement in "
					+ hNode.getColumnName());
		}
		String transformMethodStmt = PythonTransformationHelper
				.getPythonTransformMethodDefinitionState(worksheet,
						trimmedTransformationCode, "");


		logger.debug("Executing PyTransform {}\n",  transformMethodStmt);

		// Prepare the Python interpreter
		PythonRepository repo = PythonRepositoryRegistry.getInstance().getPythonRepository(contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonInterpreter interpreter = repo.getInterpreter();

		repo.initializeInterpreter(interpreter);
		Collection<Node> nodes = new ArrayList<>(Math.max(1000, worksheet
				.getDataTable().getNumRows()));
		SuperSelection tmpSelection = new SuperSelection("TEMP_SUPER_SELECTION");
		worksheet.getDataTable().collectNodes(hNode.getHNodePath(f), nodes, tmpSelection);

		Map<String, String> rowToValueMap = new HashMap<>();

		int counter = 0;
		long starttime = System.currentTimeMillis();
		// Go through all nodes collected for the column with given hNodeId;
		PyObject locals = interpreter.getLocals();
		locals.__setitem__("workspaceid", new PyString(workspace.getId()));
		locals.__setitem__("command", Py.java2py(this));
		locals.__setitem__("worksheetId", new PyString(worksheet.getId()));
		locals.__setitem__("selectionName", new PyString(selection.getName()));
		
		repo.compileAndAddToRepositoryAndExec(interpreter, transformMethodStmt);
		PyCode py = repo.getTransformCode();

		int numRowsWithErrors = 0;

		for (Node node : nodes) {
			Row row = node.getBelongsToRow();
			
			locals.__setitem__("nodeid", new PyString(node.getId()));
		
			try {
		
				PyObject output = interpreter.eval(py);
				String transformedValue = PythonTransformationHelper
						.getPyObjectValueAsString(output);
				
				addTransformedValue(transformedRows, row, transformedValue);
			} catch (PyException p) {
				logger.debug("error in evaluation python, skipping one row");
				numRowsWithErrors++;
				// Error occured in the Python method execution
				addTransformedValue(transformedRows, row, errorDefaultValue);
				addError(errorValues, row, counter, p.value);
			} catch (Exception t) {
				// Error occured in the Python method execution
				logger.debug(
						"Error occured while transforming, using default value.",
						t);
				numRowsWithErrors++;
				rowToValueMap.put(row.getId(), errorDefaultValue);
			}
			if (limit != null && ++counter >= limit) {
				break;
			}
		}
		if (numRowsWithErrors > 0) {
			logger.debug("PyTransform errors in "
					+ numRowsWithErrors
					+ " rows. This could be normal when rows have unexpected values.");
		}
		logger.debug("transform time "
				+ (System.currentTimeMillis() - starttime));
	}

	private void addError(JSONArray errorValues, Row row, int counter,
			PyObject value) throws JSONException {
		errorValues.put(new JSONObject().put(JsonKeys.row.name(), counter).put(
				JsonKeys.error.name(), value));

	}

	private void addTransformedValue(JSONArray transformedRows, Row row,
			String transformedValue) throws JSONException {
		JSONObject transformedRow = new JSONObject();
		transformedRow.put(
				MultipleValueEditColumnCommandFactory.Arguments.rowID.name(),
				row.getId());
		transformedRow.put(
				MultipleValueEditColumnCommandFactory.Arguments.value.name(),
				transformedValue);
		transformedRows.put(transformedRow);
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		UpdateContainer c = (WorksheetUpdateFactory
				.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace), workspace.getContextId()));
		// TODO is it necessary to compute alignment and semantic types for
		// everything?
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	public String getTransformationCode() {
		return transformationCode;
	}

	public void setTransformationCode(String transformationCode) {
		this.transformationCode = transformationCode;
	}
	
	public void addInputColumns(String hNodeId) {
		inputColumns.add(hNodeId);
	}
	
	public void addSelectedRowsColumns(String hNodeId) {
	}
	
	public void setSelectedRowsMethod(boolean t) {
	}
}
