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

package edu.isi.karma.transformation;

import org.python.core.PyObject;
import org.python.core.PyType;

import edu.isi.karma.rep.Worksheet;

public class PythonTransformationHelper {
	public String getPyObjectValueAsString(PyObject obj) {
		if (obj == null)
			return "";
		PyType type = obj.getType();
		if (type.getName().equals("long"))
			return Long.toString(obj.asLong());
		else if (type.getName().equals("int"))
			return Integer.toString(obj.asInt());
		
		return obj.asString();
	}
	
	public String getImportStatements() {
		StringBuilder importStmt = new StringBuilder();
		importStmt.append("import re\n");
		importStmt.append("import datetime\n");
		importStmt.append("import edu.isi.karma.rep.WorkspaceManager\n");
		importStmt.append("import edu.isi.karma.rep.Workspace\n");
		importStmt.append("import edu.isi.karma.rep.Node\n");
		importStmt.append("import edu.isi.karma.rep.RepFactory\n");
		return importStmt.toString();
	}
	
	public String normalizeString(String string) {
		return string.replaceAll(" ", "").replaceAll("[^\\p{L}\\p{N}]","");
	}
	
	public String getPythonTransformMethodDefinitionState(Worksheet worksheet, String transformationCode) {
		StringBuilder methodStmt = new StringBuilder();
		methodStmt.append("def transform(r):\n");
		String lines[] = transformationCode.split("\\r?\\n");
		for (String line:lines) {
			methodStmt.append("\t" + line + "\n");
		}
		return methodStmt.toString();
	}


	public String getGetValueDefStatement() {
		StringBuilder methodStmt = new StringBuilder();
		methodStmt.append("def getValue(columnName):\n");
		methodStmt.append("	factory = edu.isi.karma.rep.WorkspaceManager.getInstance().getWorkspace(workspaceid).getFactory()\n");
		methodStmt.append("	node = factory.getNode(nodeid)\n");
		methodStmt.append("	return node.getNeighborByColumnName(columnName, factory).getValue().asString()");

		return methodStmt.toString();
	}
	
	public String getVDefStatement()
	{
		StringBuilder methodStmt = new StringBuilder();
		methodStmt.append("def v(columnName):\n\treturn getValue(columnName)\n");
		return methodStmt.toString();
	}
}
