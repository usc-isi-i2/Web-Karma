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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.python.core.PyObject;
import org.python.core.PyType;

import edu.isi.karma.rep.HNode;
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
		return importStmt.toString();
	}

	
	public String getPythonClassCreationStatement(Worksheet worksheet, List<String> hNodeIds, Map<String,String> normalizedColumnNamesMap) {
		StringBuilder pyClass = new StringBuilder();
		pyClass.append("class " + normalizeString(worksheet.getTitle()) + ":\n");
		pyClass.append("\tdef __init__(self");
		Collection<HNode> hNodes = worksheet.getHeaders().getHNodes();
		List<String> propertyNames = new ArrayList<String>();
		for (HNode hNode: hNodes) {
			if (hNode.hasNestedTable())
				continue;
			String propertyName = normalizeString(hNode.getColumnName());
			normalizedColumnNamesMap.put(hNode.getColumnName(), propertyName);
			pyClass.append("," + propertyName);
			propertyNames.add(propertyName);
			hNodeIds.add(hNode.getId());
		}
		pyClass.append("):\n");
		for (String propertyName:propertyNames) {
			pyClass.append("\t\tself."+propertyName + " = " +propertyName +"\n");
		}
		return pyClass.toString();
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


	public String getColumnNameDictionaryStatement(Map<String, String> columnNameMap) {
		StringBuilder dictStmt = new StringBuilder();
		dictStmt.append("columnNameMap = {");
		int counter = 0;
		for (String columnName:columnNameMap.keySet()) {
			if (counter != 0)
				dictStmt.append(",");
			dictStmt.append("\"" + columnName + "\":\"" + columnNameMap.get(columnName) + "\"");
			counter++;
		}
		dictStmt.append("}");
		return dictStmt.toString();
	}


	public String getGetValueDefStatement(Map<String, String> columnNameMap) {
		StringBuilder methodStmt = new StringBuilder();
		methodStmt.append("def getValue(columnName):\n");
		methodStmt.append("\treturn getattr(r,columnNameMap[columnName])\n");
		return methodStmt.toString();
	}
}
