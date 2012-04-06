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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.hierarchicalheadings.ColspanMap;
import edu.isi.karma.rep.hierarchicalheadings.ColumnCoordinateSet;
import edu.isi.karma.rep.hierarchicalheadings.HHTree;
import edu.isi.karma.rep.hierarchicalheadings.LeafColumnIndexMap;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.ViewPreferences.ViewPreference;
import edu.isi.karma.view.tableheadings.VColumnHeader;

/**
 * @author szekely
 * 
 */
public class ViewFactory {

	private static final String ID_TYPE_VW = "VW";

	public ViewFactory() {
	}

	private int nextId = 1;

	private Map<String, VWorksheet> vWorksheets = new HashMap<String, VWorksheet>();

	/**
	 * Maps table Ids to CSS tags. By putting it here the same table type will
	 * have the same color in all worksheets.
	 */
	private final VTableCssTags tableCssTags = new VTableCssTags();

	private String getId(String prefix) {
		return prefix + (nextId++);
	}

	public VTableCssTags getTableCssTags() {
		return tableCssTags;
	}

	VColumnHeader createVColumnHeader(HNodePath path,
			ViewPreferences preferences) {
		HNode hn = path.getLeaf();
		String columnNameFull = hn.getColumnName();
		String columnNameShort = columnNameFull;
		if (columnNameFull.length() > preferences
				.getIntViewPreferenceValue(ViewPreference.maxCharactersInHeader)) {
			columnNameShort = JSONUtil
					.truncateForHeader(
							columnNameFull,
							preferences
									.getIntViewPreferenceValue(ViewPreference.maxCharactersInHeader));
		}
		tableCssTags.registerTablesInPath(path);
		return new VColumnHeader(path.toString(), columnNameFull,
				columnNameShort);
	}

	public VWorksheet createVWorksheet(Worksheet worksheet,
			List<HNodePath> columns, List<Row> rows, VWorkspace vWorkspace) {
		String id = getId(ID_TYPE_VW);
		VWorksheet vw = new VWorksheet(id, worksheet, columns, vWorkspace);
		vWorksheets.put(id, vw);
		return vw;
	}

	public void updateWorksheet(String vWorksheetId, Worksheet worksheet, List<HNodePath> columns,
			VWorkspace vWorkspace) {
		VWorksheet vw = new VWorksheet(vWorksheetId, worksheet, columns, vWorkspace);
		vWorksheets.put(vWorksheetId, vw);
		
		// Update the coordinate set
		HHTree hHtree = new HHTree();
		hHtree.constructHHTree(vw.getvHeaderForest());
		vw.setColumnCoordinatesSet(new ColumnCoordinateSet(hHtree, new ColspanMap(hHtree)));
		vw.setLeafColIndexMap(new LeafColumnIndexMap(hHtree));
	}

	public VWorksheet getVWorksheet(String vWorksheetId) {
		return vWorksheets.get(vWorksheetId);
	}
}
