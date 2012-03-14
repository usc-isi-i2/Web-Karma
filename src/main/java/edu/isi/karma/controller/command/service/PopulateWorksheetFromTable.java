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


package edu.isi.karma.controller.command.service;

import java.util.ArrayList;
import java.util.List;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.service.Table;

public class PopulateWorksheetFromTable {

	private Workspace workspace;
	private Worksheet worksheet;
	private Table table;

	public PopulateWorksheetFromTable(Workspace workspace, Worksheet worksheet, Table table) {
		this.workspace = workspace;
		this.worksheet = worksheet;
		this.table = table;
	}
	public void populate() {
		
		if (table == null)
			return;
		
		if (worksheet == null)
			return;
		
		List<String> addedHNodeIds = addHeaders();
		addRows(addedHNodeIds);
	}
	
	private List<String> addHeaders() {
		HTable headers = worksheet.getHeaders();
		List<String> addedHNodeIds = new ArrayList<String>();

		for (int i = 0; i < table.getColumns().size(); i++) {
			HNode hNode = headers.addHNode(table.getColumns().get(i), worksheet, workspace.getFactory());
			addedHNodeIds.add(hNode.getId());
		}
		
		return addedHNodeIds;
	}
	
	private void addRows(List<String> hNodeIds) {

		for (int i = 0; i < table.getValues().size(); i++) {
			List<Row> rows = worksheet.getDataTable().getRows(i, 1);
			if (rows == null || rows.size() == 0)
				continue;
			
			Row row = rows.get(0);
			for (int j = 0; j < hNodeIds.size(); j++) {
				row.setValue(hNodeIds.get(j), table.getValues().get(i).get(j));
			}

		}
		
	}
}
