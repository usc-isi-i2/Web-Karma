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
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VHNode;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.ViewPreferences.ViewPreference;

public class WorksheetDataUpdate extends AbstractUpdate {

	private final String worksheetId;
	private static Logger logger = LoggerFactory.getLogger(WorksheetDataUpdate.class);
	private final SuperSelection selection;
	private enum JsonKeys {
		worksheetId, rows, columnName, characterLength, hasNestedTable, columnClass,
		displayValue, expandedValue, nestedRows, additionalRowsCount, tableId, 
		nodeId, rowId, isSelected, rowValueArray
	}

	public WorksheetDataUpdate(String worksheetId, SuperSelection selection) {
		super();
		this.worksheetId = worksheetId;
		this.selection = selection;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
		VWorksheet vWorksheet =  vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);

		try {
			JSONObject response = new JSONObject();
			response.put(JsonKeys.worksheetId.name(), worksheetId);
			response.put(AbstractUpdate.GenericJsonKeys.updateType.name(), 
					this.getClass().getSimpleName());

			Worksheet wk = vWorksheet.getWorksheet();
			Table dataTable = wk.getDataTable();
			TablePager pager = vWorksheet.getTopTablePager();

			JSONArray rows = getRowsUsingPager(
					pager, vWorksheet,
					vWorksheet.getHeaderViewNodes(), vWorkspace.getPreferences().getIntViewPreferenceValue(
							ViewPreference.maxCharactersInCell));
			int rowsLeft = dataTable.getNumRows() - rows.length();
			rowsLeft = rowsLeft < 0 ? 0 : rowsLeft;
			response.put(JsonKeys.additionalRowsCount.name(), rowsLeft);
			response.put(JsonKeys.tableId.name(), dataTable.getId());
			response.put(JsonKeys.rows.name(), rows);

			pw.println(response.toString());
		} catch (JSONException e) {
			logger.error("JSONException", e);
		}
	}

	private JSONArray getRowsUsingPager(TablePager pager, VWorksheet vWorksheet, List<VHNode> orderedHnodeIds, int maxDataDisplayLength) throws JSONException {
		return getRowsJsonArray(pager.getRows(),vWorksheet,  orderedHnodeIds, maxDataDisplayLength);
	}

	public JSONArray getRowsJsonArray(List<Row> rows, VWorksheet vWorksheet, List<VHNode> orderedHnodeIds, 
			int maxDataDisplayLength) throws JSONException {
		JSONArray rowsArr = new JSONArray();

		for (Row row:rows) {
			JSONObject rowObj = new JSONObject();
			JSONArray rowValueArray = new JSONArray();
			rowObj.put(JsonKeys.rowId.name(), row.getId());
			rowObj.put(JsonKeys.isSelected.name(), selection.isSelected(row));
			rowObj.put(JsonKeys.rowValueArray.name(), rowValueArray);
			for (VHNode vNode : orderedHnodeIds) {
				if(vNode.isVisible()) {
					Node rowNode = row.getNode(vNode.getId());
					JSONObject nodeObj = new JSONObject();
					nodeObj.put(JsonKeys.columnClass.name(), 
							WorksheetHeadersUpdate.getColumnClass(vNode.getId()));
					nodeObj.put(JsonKeys.nodeId.name(), rowNode.getId());

					if (vNode.hasNestedTable()) {
						nodeObj.put(JsonKeys.hasNestedTable.name(), true);
						Table nestedTable = rowNode.getNestedTable();
						JSONArray nestedTableRows = getRowsUsingPager( 
								vWorksheet.getNestedTablePager(nestedTable), 
								vWorksheet,
								vNode.getNestedNodes(), 
								maxDataDisplayLength);
						nodeObj.put(JsonKeys.nestedRows.name(), nestedTableRows);
						nodeObj.put(JsonKeys.tableId.name(), rowNode.getNestedTable().getId());

						int rowsLeft = nestedTable.getNumRows() - nestedTableRows.length();
						rowsLeft = rowsLeft < 0 ? 0 : rowsLeft;
						nodeObj.put(JsonKeys.additionalRowsCount.name(), rowsLeft);

					} else {
						String nodeVal = rowNode.getValue().asString();
						nodeVal = (nodeVal == null) ? "" : nodeVal;
						String displayVal = (nodeVal.length() > maxDataDisplayLength) 
								? nodeVal.substring(0, maxDataDisplayLength) + "..." : nodeVal;
						nodeObj.put(JsonKeys.displayValue.name(), displayVal);
						nodeObj.put(JsonKeys.expandedValue.name(), nodeVal);
						nodeObj.put(JsonKeys.hasNestedTable.name(), false);
					}
					rowValueArray.put(nodeObj);
				}
			}
			rowsArr.put(rowObj);
		}
		return rowsArr;
	}

	public boolean equals(Object o) {
		if (o instanceof WorksheetDataUpdate) {
			WorksheetDataUpdate t = (WorksheetDataUpdate)o;
			return t.worksheetId.equals(worksheetId);
		}
		return false;
	}
}