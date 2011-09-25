/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONWriter;

import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tableheadings.VTableHeadings;

/**
 * @author szekely
 * 
 */
public class VDTableData {

	// private final VTableHeadings vtHeadings;

	private final String rootTableId;

	private final List<VDRow> rows = new LinkedList<VDRow>();

	public VDTableData(VTableHeadings vtHeadings, VWorksheet vWorksheet,
			VWorkspace vWorkspace) {
		super();
		// this.vtHeadings = vtHeadings;
		this.rootTableId = vWorksheet.getWorksheet().getDataTable().getId();
		vtHeadings.getRootVHNode().populateVDRows(null, rows,
				vWorksheet.getTopTablePager(), vWorksheet);
		for (VDRow r : rows) {
			r.firstPassTopDown(vWorkspace);
		}
	}

	JSONWriter prettyPrintJson(JSONWriter jw) throws JSONException {
		jw.object()//
				.key("rootTableId").value(rootTableId)//
				.key("rows").array();
		for (VDRow r : rows) {
			r.prettyPrintJson(jw);
		}
		jw.endArray().endObject();
		return jw;
	}

}
