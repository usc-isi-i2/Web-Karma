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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.ViewPreferences.ViewPreference;

/**
 * @author szekely
 * 
 */
public class VWorksheetList {

	class VWorksheetTitleComparator implements Comparator<VWorksheet> {

		private RepFactory repFactory;

		VWorksheetTitleComparator(RepFactory repFactory) {
			super();
			this.repFactory = repFactory;
		}

		@Override
		public int compare(VWorksheet arg0, VWorksheet arg1) {
			String title0 = repFactory.getWorksheet(arg0.getWorksheetId())
					.getTitle();
			String title1 = repFactory.getWorksheet(arg1.getWorksheetId())
					.getTitle();
			return title0.compareTo(title1);
		}

	}

	private List<VWorksheet> sortedWorksheets = new LinkedList<VWorksheet>();

	VWorksheetList() {
		super();
	}

	int getNumWorksheets() {
		return sortedWorksheets.size();
	}

	public VWorksheet getVWorksheet(String worksheetId) {
		for (VWorksheet vw : sortedWorksheets) {
			if (vw.getWorksheetId().equals(worksheetId)) {
				return vw;
			}
		}
		return null;
	}

	/**
	 * Add the given worksheets to the view if they are not already viewed.
	 * Newly added worksheets are added at the end.
	 * 
	 * @param worksheets
	 * @param vWorkspace
	 */
	void addWorksheets(Collection<Worksheet> worksheets, VWorkspace vWorkspace) {
		List<VWorksheet> newWorksheets = new LinkedList<VWorksheet>();
		for (Worksheet w : worksheets) {
			if (!contains(w)) {
				ViewPreferences pref = vWorkspace.getPreferences();
				newWorksheets
						.add(vWorkspace
								.getViewFactory()
								.createVWorksheet(
										w,
										w.getHeaders().getAllPaths(),
										w.getDataTable()
												.getRows(
														0,
														pref.getIntViewPreferenceValue(ViewPreference.defaultRowsToShowInTopTables)),
										vWorkspace));
			}
		}
		Collections.sort(newWorksheets, new VWorksheetTitleComparator(
				vWorkspace.getWorkspace().getFactory()));
		sortedWorksheets.addAll(newWorksheets);
	}

	public void generateJson(String prefix, PrintWriter pw, ViewFactory vFactory) {
		Iterator<VWorksheet> it = sortedWorksheets.iterator();
		while (it.hasNext()) {
			it.next().generateWorksheetListJson(prefix + "  ", pw, vFactory);
			if (it.hasNext()) {
				pw.println(prefix + "  , ");
			}
		}
	}

	boolean contains(Worksheet worksheet) {
		for (VWorksheet vw : sortedWorksheets) {
			if (vw.getWorksheetId().equals(worksheet.getId())) {
				return true;
			}
		}
		return false;
	}

	public List<VWorksheet> getVWorksheets() {
		return sortedWorksheets;
	}
}
