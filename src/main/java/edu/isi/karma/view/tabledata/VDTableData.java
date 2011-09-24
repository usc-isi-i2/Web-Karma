/**
 * 
 */
package edu.isi.karma.view.tabledata;

import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.rep.Table;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.tableheadings.VTableHeadings;

/**
 * @author szekely
 *
 */
public class VDTableData {

	private final VTableHeadings vtHeadings;
	
	private final String rootTableId;
	
	private final List<VDRow> nestedTableRows = new LinkedList<VDRow>();

	public VDTableData(VTableHeadings vtHeadings, VWorksheet vWorksheet) {
		super();
		this.vtHeadings = vtHeadings;
		this.rootTableId = vWorksheet.getWorksheet().getDataTable().getId();
		constructRows(vtHeadings, vWorksheet);
	}

	private void constructRows(VTableHeadings vtHeadings2, VWorksheet vWorksheet) {
		// TODO Auto-generated method stub
		
	}
	
}
