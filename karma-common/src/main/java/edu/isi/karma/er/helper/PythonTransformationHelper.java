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

import java.io.File;
import java.io.FilenameFilter;

import org.python.core.PyObject;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;

import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class PythonTransformationHelper {
	
	private static String valueDefStatement = null;
	private static String isEmptyDefStatement = null;
	private static String importStatement = null;
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
	
	public static String getPythonTransformMethodDefinitionState(Worksheet worksheet, String transformationCode) {
		StringBuilder methodStmt = new StringBuilder();
		methodStmt.append("def transform(r):\n");
		String lines[] = transformationCode.split("\\r?\\n");
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
			importStmt.append("import edu.isi.karma.rep.WorkspaceManager\n");
			importStmt.append("import edu.isi.karma.rep.Workspace\n");
			importStmt.append("import edu.isi.karma.rep.Node\n");
			importStmt.append("import edu.isi.karma.rep.RepFactory\n");
			importStmt.append("import edu.isi.karma.controller.command.transformation.PythonTransformationCommand\n");
			importStatement = importStmt.toString();
		}
		return importStatement;
	}

	public static String getGetValueDefStatement() {
	
		if(valueDefStatement == null)
		{
			StringBuilder methodStmt = new StringBuilder();
			methodStmt.append("def getValue(columnName):\n");
			methodStmt.append("	factory = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getFactory()\n");
			methodStmt.append("	node = factory.getNode(nodeid)\n");
			methodStmt.append("	targetNode = node.getNeighborByColumnName(columnName, factory)\n");
			methodStmt.append("	if targetNode is not None:\n");
			methodStmt.append("		command.addInputColumns(targetNode.getHNodeId())\n");
			methodStmt.append("		value = targetNode.getValue()\n");
			methodStmt.append("		if value is not None:\n");
			methodStmt.append("			valueAsString = value.asString()\n");
			methodStmt.append("			if valueAsString is not None:\n");
			methodStmt.append("				return valueAsString\n");
			methodStmt.append("	return ''\n");
			valueDefStatement = methodStmt.toString();
		}
		return valueDefStatement;
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
			methodStmt.append("				return True  \n");
			methodStmt.append("		value = targetNode.getValue()\n");
			methodStmt.append("		if value is not None:\n");
			methodStmt.append("			valueAsString = value.asString()\n");
			methodStmt.append("			if ((not valueAsString) and len(valueAsString) > 0) :\n");
			methodStmt.append("				return True\n");
			methodStmt.append("	return False\n");
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
	
	public void importUserScripts(PythonInterpreter interpreter) {
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
}
