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

import java.io.PrintWriter;
import java.util.ArrayList;
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
import edu.isi.karma.controller.command.alignment.GetDataPropertiesForClassCommand;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.transformation.PythonTransformationHelper;
import edu.isi.karma.view.VWorkspace;

public class PreviewPythonTransformationResultsCommand extends Command {
	final private String vWorksheetId;
	final private String transformationCode;
	final private String errorDefaultValue;
	
	private static Logger logger = LoggerFactory
			.getLogger(GetDataPropertiesForClassCommand.class.getSimpleName());
	
	private enum JsonKeys {
		updateType, result, errors, row, error
	}
	
	protected PreviewPythonTransformationResultsCommand(String id, String vWorksheetId, String transformationCode, String errorDefaultValue) {
		super(id);
		this.vWorksheetId = vWorksheetId;
		this.transformationCode = transformationCode;
		this.errorDefaultValue = errorDefaultValue;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getName();
	}

	@Override
	public String getTitle() {
		return "Preview Transformation Results";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		PythonTransformationHelper pyHelper = new PythonTransformationHelper();
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		List<String> hNodeIds = new ArrayList<String>();
		Map<String,String> columnNameMap = new HashMap<String,String>();
		String clsStmt = pyHelper.getPythonClassCreationStatement(worksheet, hNodeIds, columnNameMap);
		String transformMethodStmt = pyHelper.getPythonTransformMethodDefinitionState(worksheet, transformationCode);
		String columnNameDictStmt = pyHelper.getColumnNameDictionaryStatement(columnNameMap);
		String defGetValueStmt = pyHelper.getGetValueDefStatement(columnNameMap);
		
		final JSONArray errorsArray = new JSONArray();
		
		try {
			PythonInterpreter interpreter = new PythonInterpreter();
			interpreter.exec(clsStmt);
			interpreter.exec(transformMethodStmt);
			interpreter.exec(columnNameDictStmt);
			interpreter.exec(defGetValueStmt);
			
			List<Row> rows = worksheet.getDataTable().getRows(0, 5);
			final List<String> resultValues = new ArrayList<String>();
			int rowCounter = 1;
			// Get preview values for the top 5 rows
			for (Row row:rows) {
				StringBuilder objectCreationStmt = new StringBuilder();
				objectCreationStmt.append("r = " + pyHelper.normalizeString(worksheet.getTitle()) + "(");
				int counter = 0;
				for (String hNodeId:hNodeIds) {
					String nodeVal = row.getNode(hNodeId).getValue().asString();
					if (counter++ != 0)
						objectCreationStmt.append(",");
					objectCreationStmt.append("\"" + nodeVal + "\"");
				}
				objectCreationStmt.append(")");
				interpreter.exec(objectCreationStmt.toString());
				try {
					PyObject output = interpreter.eval("transform(r)");
					resultValues.add(pyHelper.getPyObjectValueAsString(output));
				} catch (PyException p) {
					p.printStackTrace();
					resultValues.add(errorDefaultValue);
					errorsArray.put(new JSONObject().put(JsonKeys.row.name(), rowCounter)
							.put(JsonKeys.error.name(), p.value));
				} catch (Exception t) {
					t.printStackTrace();
					logger.debug("Error occured while transforming.", t);
					resultValues.add(errorDefaultValue);
				}
				rowCounter++;
			}
			
			return new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
					try {
						JSONObject outputObject = new JSONObject();
						outputObject.put(JsonKeys.updateType.name(), "PythonPreviewResultsUpdate");
						JSONArray outputArr = new JSONArray();
						for (String result:resultValues) {
							outputArr.put(result);
						}
						outputObject.put(JsonKeys.result.name(), outputArr);
						outputObject.put(JsonKeys.errors.name(), errorsArray);
						pw.println(outputObject.toString());
					} catch (JSONException e) {
						logger.error("Error while creating output update.", e);
						new ErrorUpdate("Error while creating Python results preview.").generateJson(prefix, pw, vWorkspace);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
