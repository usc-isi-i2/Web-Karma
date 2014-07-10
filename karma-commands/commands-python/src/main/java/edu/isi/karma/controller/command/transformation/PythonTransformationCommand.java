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

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.python.core.PyCode;
import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandType;
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
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public abstract class PythonTransformationCommand extends WorksheetCommand {

	protected String transformationCode;
	final protected String hNodeId;
	final protected String errorDefaultValue;
	protected Set<String> inputColumns;

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
		inputColumns = new HashSet<String>();
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
					throws JSONException {

		PythonTransformationHelper pyHelper = new PythonTransformationHelper();
		String trimmedTransformationCode = transformationCode.trim();
		// Pedro: somehow we are getting empty statements, and these are causing
		// exceptions.
		if (trimmedTransformationCode.isEmpty()) {
			trimmedTransformationCode = "return \"\"";
			logger.info("Empty PyTransform statement in "
					+ hNode.getColumnName());
		}
		String transformMethodStmt = pyHelper
				.getPythonTransformMethodDefinitionState(worksheet,
						trimmedTransformationCode);


		logger.debug("Executing PyTransform\n" + transformMethodStmt);

		// Prepare the Python interpreter
		PythonInterpreter interpreter = new PythonInterpreter();


		interpreter.exec(pyHelper.getImportStatements());
		importUserScripts(interpreter);
		interpreter.exec(pyHelper.getGetValueDefStatement());
		interpreter.exec(pyHelper.getVDefStatement());
		interpreter.exec(transformMethodStmt);

		Collection<Node> nodes = new ArrayList<Node>(Math.max(1000, worksheet
				.getDataTable().getNumRows()));
		worksheet.getDataTable().collectNodes(hNode.getHNodePath(f), nodes);

		Map<String, String> rowToValueMap = new HashMap<String, String>();

		int counter = 0;
		long starttime = System.currentTimeMillis();
		// Go through all nodes collected for the column with given hNodeId

		interpreter.set("workspaceid", workspace.getId());
		interpreter.set("command", this);
		PyCode py = interpreter.compile("transform(nodeid)");

		int numRowsWithErrors = 0;

		for (Node node : nodes) {
			Row row = node.getBelongsToRow();

			interpreter.set("nodeid", node.getId());

			try {
				PyObject output = interpreter.eval(py);
				String transformedValue = pyHelper
						.getPyObjectValueAsString(output);
				addTransformedValue(transformedRows, row, transformedValue);
			} catch (PyException p) {
				logger.info("error in evaluation python, skipping one row");
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

	private void importUserScripts(PythonInterpreter interpreter) {
		String dirpathString = ServletContextParameterMap
				.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY);

		if (dirpathString != null && dirpathString.compareTo("") != 0) {
			File f = new File(dirpathString);
			String[] scripts = f.list(new FilenameFilter(){

				@Override
				public boolean accept(File dir, String name) {
					return name.endsWith(".py");
				}});
			for(String script : scripts)
			{
				interpreter.execfile(dirpathString  + File.separator + script);
			}
		}
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
				.createRegenerateWorksheetUpdates(worksheetId));
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
}
