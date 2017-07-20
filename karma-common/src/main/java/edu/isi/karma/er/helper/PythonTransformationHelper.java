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

package edu.isi.karma.er.helper;

import org.python.core.PyObject;
import org.python.core.PyType;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;

public class PythonTransformationHelper {

	private static String valueDefStatement = null;
	private static String isEmptyDefStatement = null;
	private static String hasSelectedRowsStatement = null;
	private static String importStatement = null;
	private static String valueFromNestedColumnByIndexDefStatement = null;
	private static String getRowIndexDefStatement = null;
	private static String modelNameStatement = null;
	private static String modelPrefixStatement = null;
	private static String modelBaseUriStatement = null;
	
	public static String getPyObjectValueAsString(PyObject obj) {
		if (obj == null)
			return "";
		PyType type = obj.getType();
		if (type.getName().equals("long"))
			return Long.toString(obj.asLong());
		else if (type.getName().equals("int"))
			return Integer.toString(obj.asInt());
		else if (type.getName().equals("bool")) 
			return obj.asInt() != 0 ? "true" : "false";
		return obj.asString();
	}

	public static boolean getPyObjectValueAsBoolean(PyObject obj) {
		if (obj == null)
			return false;
		PyType type = obj.getType();
		if (type.getName().equals("long"))
			return (obj.asLong() != 0);
		else if (type.getName().equals("int"))
			return (obj.asInt() != 0);
		else if (type.getName().equals("bool")) 
			return (obj.asInt() != 0);
		else if (type.getName().equals("NoneType")) 
			return false;

		return obj.asString().length() != 0;
	}

	public String normalizeString(String string) {
		return string.replaceAll(" ", "").replaceAll("[^\\p{L}\\p{N}]","");
	}

	public static String getPythonTransformMethodDefinitionState(Worksheet worksheet, String transformationCode, String transformId) {
		return getPythonMethodDefinition("transform", transformationCode, transformId);
	}

	public static String getPythonSelectionMethodDefinitionState(Worksheet worksheet, String code, String codeId) {
		return getPythonMethodDefinition("selection", code, codeId);
	}

	private static String getPythonMethodDefinition(String methodName, String code, String codeId) {
		StringBuilder methodStmt = new StringBuilder();
		methodStmt.append("def "+methodName+codeId+"(r):\n");
		String lines[] = code.split("\\r?\\n");
		for (String line:lines) {
			methodStmt.append("\t" + line + "\n");
		}
		return methodStmt.toString();
	}
	
	public static String getImportStatements() {
		if(importStatement == null)
		{
			StringBuilder importStmt = new StringBuilder();
			importStmt.append("import re\n");
			importStmt.append("import datetime\n");
			importStmt.append("import json\n");
			importStmt.append("import edu.isi.karma.rep.WorkspaceManager\n");
			importStmt.append("import edu.isi.karma.rep.Workspace\n");
			importStmt.append("import edu.isi.karma.rep.Worksheet\n");
			importStmt.append("import edu.isi.karma.rep.Node\n");
			importStmt.append("import edu.isi.karma.rep.Table\n");
			importStmt.append("import edu.isi.karma.rep.HTable\n");
			importStmt.append("import edu.isi.karma.rep.HNode\n");
			importStmt.append("import edu.isi.karma.rep.RepFactory\n");
			importStmt.append("import edu.isi.karma.er.helper.PythonTransformationHelper\n");
			importStmt.append("import edu.isi.karma.controller.command.transformation.PythonTransformationCommand\n");
			importStmt.append("import edu.isi.karma.rep.metadata.WorksheetProperties\n");
			importStmt.append("import edu.isi.karma.rep.metadata.WorksheetProperties.Property\n");
			
			importStatement = importStmt.toString();
		}
		return importStatement;
	}

	public static String getRowIndexDefStatement()
	{
		if(getRowIndexDefStatement == null)
		{
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def getRowIndex():\n");
			methodStmt.append("	factory = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getFactory()\n");
			methodStmt.append("	node = factory.getNode(nodeid)\n");
			methodStmt.append("	return node.getRowIndex()\n");
			getRowIndexDefStatement = methodStmt.toString();
		}
		return getRowIndexDefStatement;
	}
	public static String getGetValueDefStatement() {

		if(valueDefStatement == null)
		{
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def getValue(columnName):\n");
			methodStmt.append("	factory = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getFactory()\n");
			methodStmt.append("	worksheet = factory.getWorksheet(worksheetId)\n");
			methodStmt.append("	node = factory.getNode(nodeid)\n");
			methodStmt.append("	targetNode = node.getNeighborByColumnName(columnName, factory)\n");
			methodStmt.append("	if targetNode is not None:\n");
			methodStmt.append("		command.addInputColumns(targetNode.getHNodeId())\n");
			methodStmt.append("		superSelection = worksheet.getSuperSelectionManager().getSuperSelection(selectionName)\n");
			methodStmt.append("		value = targetNode.serializeToJSON(superSelection, factory)\n");
			methodStmt.append("		if value is not None:\n");
			methodStmt.append("			valueAsString = value\n");
			methodStmt.append("			if (not isinstance(value, str)) and (not isinstance(value, unicode)):\n");
			methodStmt.append("				valueAsString = value.toString()\n");
			methodStmt.append("			if valueAsString is not None:\n");
			methodStmt.append("				return valueAsString\n");
			methodStmt.append("	return ''\n");
			valueDefStatement = methodStmt.toString();
		}
		return valueDefStatement;
	}

	public static String getHasSelectedRowsStatement() {

		if(hasSelectedRowsStatement == null)
		{
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def hasSelectedRows(columnName):\n");
			methodStmt.append("	factory = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getFactory()\n");
			methodStmt.append("	node = factory.getNode(nodeid)\n");
			methodStmt.append("	targetNode = node.getNeighborByColumnName(columnName, factory)\n");
			methodStmt.append("	if targetNode is not None: \n");
			methodStmt.append("		if targetNode.hasNestedTable(): \n");
			methodStmt.append("			command.addInputColumns(targetNode.getHNodeId())\n");
			methodStmt.append("			command.addSelectedRowsColumns(targetNode.getHNodeId())\n");
			methodStmt.append("			command.setSelectedRowsMethod(True)\n");
			methodStmt.append("			nestedTable = targetNode.getNestedTable()\n");
			methodStmt.append("			if edu.isi.karma.er.helper.PythonTransformationHelper.hasSelectedRows(nestedTable, factory, selectionName) :\n");
			methodStmt.append("				return True\n");
			methodStmt.append("	return False\n");
			hasSelectedRowsStatement = methodStmt.toString();
		}
		return hasSelectedRowsStatement;
	}

	
	public static String getGetValueFromNestedColumnByIndexDefStatement() {
		
		if(valueFromNestedColumnByIndexDefStatement == null)
		{
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def getValueFromNestedColumnByIndex(columnName, nestedColumnName, index):\n");
			methodStmt.append("	factory = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getFactory()\n");
			methodStmt.append("	node = factory.getNode(nodeid)\n");
			methodStmt.append("	targetNode = node.getNeighborByColumnNameWithNestedColumnByRowIndex(columnName, factory, nestedColumnName, index)\n");
			methodStmt.append("	if targetNode is not None:\n");
			methodStmt.append("		command.addInputColumns(targetNode.getHNodeId())\n");
			methodStmt.append("		value = targetNode.getValue()\n");
			methodStmt.append("		if value is not None:\n");
			methodStmt.append("			valueAsString = value.asString()\n");
			methodStmt.append("			if valueAsString is not None:\n");
			methodStmt.append("				return valueAsString\n");
			methodStmt.append("	return ''\n");
			valueFromNestedColumnByIndexDefStatement = methodStmt.toString();
		}
		return valueFromNestedColumnByIndexDefStatement;
	}
	
	
	public static String getModelName() {
		if(modelNameStatement == null) {
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def getModelName():\n");
			methodStmt.append("	worksheet = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getWorksheet(worksheetId)\n");
			methodStmt.append("	props = worksheet.getMetadataContainer().getWorksheetProperties()\n");
			methodStmt.append("	modelName = props.getPropertyValue(edu.isi.karma.rep.metadata.WorksheetProperties.Property.graphLabel)\n");
			methodStmt.append("	if modelName is not None:\n");
			methodStmt.append("		return modelName\n");
			methodStmt.append("	return ''\n");
			modelNameStatement = methodStmt.toString();
		}
		return modelNameStatement;
	}
	
	public static String getModelPrefix() {
		if(modelPrefixStatement == null) {
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def getModelPrefix():\n");
			methodStmt.append("	worksheet = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getWorksheet(worksheetId)\n");
			methodStmt.append("	props = worksheet.getMetadataContainer().getWorksheetProperties()\n");
			methodStmt.append("	prefix = props.getPropertyValue(edu.isi.karma.rep.metadata.WorksheetProperties.Property.prefix)\n");
			methodStmt.append("	if prefix is not None:\n");
			methodStmt.append("		return prefix\n");
			methodStmt.append("	return ''\n");
			modelPrefixStatement = methodStmt.toString();
		}
		return modelPrefixStatement;
	}
	
	public static String getModelBaseUri() {
		if(modelBaseUriStatement == null) {
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def getModelBaseUri():\n");
			methodStmt.append("	worksheet = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getWorksheet(worksheetId)\n");
			methodStmt.append("	props = worksheet.getMetadataContainer().getWorksheetProperties()\n");
			methodStmt.append("	uri = props.getPropertyValue(edu.isi.karma.rep.metadata.WorksheetProperties.Property.baseURI)\n");
			methodStmt.append("	if uri is not None:\n");
			methodStmt.append("		return uri\n");
			methodStmt.append("	return ''\n");
			modelBaseUriStatement = methodStmt.toString();
		}
		return modelBaseUriStatement;
	}
	
	public static String getIsEmptyDefStatement() {

		if(isEmptyDefStatement == null)
		{
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def isEmpty(columnName):\n");
			methodStmt.append("	factory = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getFactory()\n");
			methodStmt.append("	node = factory.getNode(nodeid)\n");
			methodStmt.append("	targetNode = node.getNeighborByColumnName(columnName, factory)\n");
			methodStmt.append("	if targetNode is not None:\n");
			methodStmt.append("		hasNestedTable = targetNode.hasNestedTable()\n");
			methodStmt.append("		command.addInputColumns(targetNode.getHNodeId())\n");
			methodStmt.append("		if hasNestedTable: \n");
			methodStmt.append("			table = targetNode.getNestedTable(); \n");
			methodStmt.append("			if table.getNumRows() > 0:  \n");
			methodStmt.append("				return False  \n");
			methodStmt.append("		value = targetNode.getValue()\n");
			methodStmt.append("		if value is not None:\n");
			methodStmt.append("			valueAsString = value.asString()\n");
			methodStmt.append("			if ((not valueAsString) and len(valueAsString) > 0) :\n");
			methodStmt.append("				return False\n");
			methodStmt.append("	return True\n");
			isEmptyDefStatement = methodStmt.toString();
		}
		return isEmptyDefStatement;
	}

	public static String getVDefStatement()
	{
		return "def v(columnName):\n\treturn getValue(columnName)\n";

	}

	public static String getTransformStatement() {
		return  "transform(nodeid)";
	}

	public static String getSelectionStatement() {
		return  "selection(nodeid)";
	}
	public static boolean hasSelectedRows(Table nestedTable, RepFactory factory, String selectionName) {
		Worksheet worksheet = factory.getWorksheet(nestedTable.getWorksheetId());
		SuperSelection sel = worksheet.getSuperSelectionManager().getSuperSelection(selectionName);
		for (Row r : nestedTable.getRows(0, nestedTable.getNumRows(), SuperSelectionManager.DEFAULT_SELECTION)) {
			if (sel.isSelected(r))
				return true;
		}
		return false;
	}

}
