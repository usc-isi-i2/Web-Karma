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
/**
 * 
 */
package edu.isi.karma.view;

import java.io.PrintWriter;

/**
 * 
 * @author szekely
 * 
 */
public class VColumnHeader {

	/**
	 * The name of the column I represent, possibly shortened so the table looks
	 * nice.
	 */
	private final String columnNameShort;

	/**
	 * The full column name as it appears in the HNode.
	 */
	private final String columnNameFull;

	/**
	 * The path to the data shown in this column
	 */
	private final String hNodePathId;

	VColumnHeader(String hNodeId, String columnNameFull, String columnNameShort) {
		this.hNodePathId = hNodeId;
		this.columnNameFull = columnNameFull;
		this.columnNameShort = columnNameShort;
	}

	public String getColumnNameShort() {
		return columnNameShort;
	}

	public String getColumnNameFull() {
		return columnNameFull;
	}

	public String getHNodePathId() {
		return hNodePathId;
	}

	public void generateJson(String prefix, PrintWriter pw,
			ViewFactory factory, boolean generateComma) {
//		pw.print(prefix + "{ ");
//		pw.print(JSONUtil.json(WorksheetHeadersUpdate.JsonKeys.path, hNodePathId));
//		pw.print(JSONUtil.json(WorksheetHeadersUpdate.JsonKeys.columnNameFull,
//				columnNameFull));
//		pw.print(JSONUtil.jsonLast(WorksheetHeadersUpdate.JsonKeys.columnNameShort,
//				columnNameShort));
//		pw.print(" }");
//		if (generateComma) {
//			pw.println(" ,");
//		} else {
//			pw.println();
//		}

	}
}
