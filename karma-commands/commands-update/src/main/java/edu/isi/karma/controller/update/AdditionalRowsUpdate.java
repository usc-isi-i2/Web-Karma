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

package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.view.VHNode;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences.ViewPreference;

public class AdditionalRowsUpdate extends AbstractUpdate {

	private String worksheetId;
	private String tableId;
	private SuperSelection selection;
	private static Logger logger = LoggerFactory.getLogger(AdditionalRowsUpdate.class);
	

	private enum JsonKeys {
		worksheetId, rows, columnName, characterLength, hasNestedTable, columnClass,
		displayValue, expandedValue, nestedRows, additionalRowsCount, tableId
	}
	
	public AdditionalRowsUpdate(String worksheetId, String tableId, SuperSelection sel) {
		this.worksheetId = worksheetId;
		this.tableId = tableId;
		this.selection = sel;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		VWorksheet vWorksheet = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		final TablePager pager = vWorksheet.getTablePager(tableId);
		
		final List<Row> additionalRows = pager.loadAdditionalRows();
		
		WorksheetDataUpdate upd = new WorksheetDataUpdate(vWorksheet.getId(), selection);
		
		try {
			JSONArray rowsJson = new JSONArray();
			if(!additionalRows.isEmpty()) {
				Row row = additionalRows.get(0);
				List<VHNode> nodeList = getNestedNodeList(row, vWorksheet.getHeaderViewNodes());
				rowsJson = upd.getRowsJsonArray(additionalRows, vWorksheet, 
							nodeList,
							vWorkspace.getPreferences().getIntViewPreferenceValue(
							ViewPreference.maxCharactersInCell));
			}
			JSONObject responseObj = new JSONObject();
			responseObj.put(JsonKeys.tableId.name(), tableId);
			responseObj.put(JsonKeys.rows.name(), rowsJson);
			responseObj.put(JsonKeys.additionalRowsCount.name(), 
					pager.getAdditionalRowsLeftCount());
			responseObj.put(AbstractUpdate.GenericJsonKeys.updateType.name(), 
					AdditionalRowsUpdate.class.getSimpleName());
			
			pw.print(responseObj.toString());
		} catch (JSONException e) {
			logger.error("Error creating additional rows update!", e);
		}

	}

	private ArrayList<VHNode> getNestedNodeList(Row row, ArrayList<VHNode> nodeList) {
		ArrayList<VHNode> result = null;
		for (VHNode vNode : nodeList) {
			if(vNode.isVisible()) {
				Node rowNode = row.getNode(vNode.getId());
				if(rowNode != null)
					return nodeList;
				
				if (vNode.hasNestedTable()) {
					result =  getNestedNodeList(row, vNode.getNestedNodes());
					if(result != null)
						break;
				}
			}
		}
		return result;
	}
}
