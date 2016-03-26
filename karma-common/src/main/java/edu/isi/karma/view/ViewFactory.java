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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.ViewPreferences.ViewPreference;

/**
 * @author szekely
 * 
 */
public class ViewFactory {

	private static final String ID_TYPE_VW = "VW";

	public ViewFactory() {
	}

	private int nextId = 1;

	private Map<String, VWorksheet> vWorksheets = new HashMap<>();

	private String getId(String prefix) {
		return prefix + (nextId++);
	}

//	VColumnHeader createVColumnHeader(HNodePath path,
//			ViewPreferences preferences) {
//		HNode hn = path.getLeaf();
//		String columnNameFull = hn.getColumnName();
//		String columnNameShort = columnNameFull;
//		if (columnNameFull.length() > preferences
//				.getIntViewPreferenceValue(ViewPreference.maxCharactersInHeader)) {
//			columnNameShort = JSONUtil
//					.truncateForHeader(
//							columnNameFull,
//							preferences
//									.getIntViewPreferenceValue(ViewPreference.maxCharactersInHeader));
//		}
////		tableCssTags.registerTablesInPath(path);
//		return new VColumnHeader(path.toString(), columnNameFull,
//				columnNameShort);
//	}

	public VWorksheet createVWorksheet(Worksheet worksheet,
			List<HNodePath> columns, List<Row> rows, VWorkspace vWorkspace) {
		String id = getId(ID_TYPE_VW);
		VWorksheet vw = new VWorksheet(id, worksheet, columns, vWorkspace);
		vWorksheets.put(id, vw);
		return vw;
	}

	public void updateWorksheet(String vWorksheetId, Worksheet worksheet, List<HNodePath> columns,
			VWorkspace vWorkspace) {
		// Grab reference to the pager in the old worksheet
		VWorksheet oldVWorksheet = getVWorksheet(vWorksheetId);
		Map<String, TablePager> vwPager = oldVWorksheet.getTableId2TablePager();
		ArrayList<VHNode> orderedViewNodes = oldVWorksheet.getHeaderViewNodes();
		
		VWorksheet vw = new VWorksheet(vWorksheetId, worksheet, columns, vWorkspace);
		vw.updateHeaderViewNodes(orderedViewNodes);
		
		vWorksheets.put(vWorksheetId, vw);
		vw.setTableId2TablePager(vwPager);
	}

	public void removeWorksheet(String vWorksheetId) {
		vWorksheets.remove(vWorksheetId);
	}
	
	public Collection<VWorksheet> getVWorksheets()
	{
		return vWorksheets.values();
	}
	
	public VWorksheet getVWorksheet(String vWorksheetId) {
		return vWorksheets.get(vWorksheetId);
	}
	
	public VWorksheet getVWorksheetByWorksheetId(String worksheetId)
	{
		for(VWorksheet vw : this.vWorksheets.values())
		{
			if(vw.getWorksheetId().compareTo(worksheetId) == 0)
			{
				return vw;
			}
		}
		return null;
	}
	
	public void addWorksheets(Collection<Worksheet> worksheets, VWorkspace vWorkspace) {
		List<VWorksheet> newWorksheets = new LinkedList<>();
		for (Worksheet w : worksheets) {
			if (null == getVWorksheetByWorksheetId(w.getId())) {
				newWorksheets
						.add(createVWorksheetWithDefaultPreferences(vWorkspace, w));
			}
		}
	}

	public VWorksheet createVWorksheetWithDefaultPreferences(
			VWorkspace vWorkspace, Worksheet w) {

		ViewPreferences pref = vWorkspace.getPreferences();
		return vWorkspace
				.getViewFactory()
				.createVWorksheet(
						w,
						w.getHeaders().getAllPaths(),
						w.getDataTable()
								.getRows(
										0,
										pref.getIntViewPreferenceValue(ViewPreference.defaultRowsToShowInTopTables),
										SuperSelectionManager.DEFAULT_SELECTION),
						vWorkspace);
	}
}
