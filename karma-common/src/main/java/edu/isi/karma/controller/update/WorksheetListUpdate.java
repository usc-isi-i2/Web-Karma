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
package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

/**
 * Contains the list of worksheets to be shown in the window. The list may
 * include new worksheets that were not previously displayed, and may not
 * inlcude worksheets that were previously displayed. Those should be removed.
 * 
 * @author szekely
 * 
 */
public class WorksheetListUpdate extends AbstractUpdate {

	public enum JsonKeys {
		worksheets, worksheetId, title, isUpToDate, isCollapsed, encoding
	}

	public WorksheetListUpdate() {
		super();
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {

		Collection<VWorksheet>	vWorksheetList = vWorkspace.getViewFactory().getVWorksheets();

		pw.println(prefix + "{");
		String prefix1 = prefix + "  ";
		pw.println(prefix1
				+ JSONUtil.json(GenericJsonKeys.updateType, getUpdateType()));
		generateWorksheetListJson(prefix, pw, vWorksheetList, prefix1);
		pw.println(prefix + "}");

	}

	private void generateWorksheetListJson(String prefix, PrintWriter pw,
			Collection<VWorksheet> vWorksheetList, String prefix1) {
		pw.println(prefix1 + JSONUtil.jsonStartList(JsonKeys.worksheets));
		Iterator<VWorksheet> it = vWorksheetList.iterator();
		while (it.hasNext()) {
			it.next().generateWorksheetListJson(prefix + "  ", pw);
			if (it.hasNext()) {
				pw.println(prefix + "  , ");
			}
		}
		pw.println(prefix1 + "]");
	}

	@Override
	public void applyUpdate(VWorkspace vWorkspace)
	{
		vWorkspace.createVWorksheetsForAllWorksheets();
		Workspace workspace = vWorkspace.getWorkspace();
		for(VWorksheet vworksheet : vWorkspace.getViewFactory().getVWorksheets())
		{
			if(vworksheet.getWorksheet() != null && null == AlignmentManager.Instance().getAlignment(workspace.getId(), vworksheet.getWorksheetId()))
			{
				AlignmentManager.Instance().createAlignment(workspace.getId(), vworksheet.getWorksheetId(), workspace.getOntologyManager());
			}
		}
	}
	
	public boolean equals(Object o) {
		if (o instanceof WorksheetListUpdate)
			return true;
		return false;
	}
}