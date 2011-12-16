/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.rep.hierarchicalheadings.HHTable;
import edu.isi.karma.rep.hierarchicalheadings.HHTree;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.tableheadings.HeadersColorKeyTranslator;

/**
 * @author szekely
 * 
 */
public class WorksheetHierarchicalHeadersUpdate extends AbstractUpdate {

	private final VWorksheet vWorksheet;

	public enum JsonKeys {
		worksheetId, rows, hTableId,
		//
		hNodeId, columnNameFull, columnNameShort, path,
		//
		cells,
		//
		cellType, fillId, topBorder, leftBorder, rightBorder, colSpan,
		//
		border, heading, headingPadding
	}

	public WorksheetHierarchicalHeadersUpdate(VWorksheet vWorksheet) {
		super();
		this.vWorksheet = vWorksheet;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		//vWorksheet.generateWorksheetHierarchicalHeadersJson(pw, vWorkspace);
		
		HHTree hHtree = new HHTree();
		hHtree.constructHHTree(vWorksheet.getvHeaderForest());

		HHTable table = new HHTable();
		table.constructCells(hHtree);

		HeadersColorKeyTranslator trans = new HeadersColorKeyTranslator();
		pw.println(prefix + "{");
		pw.println("\"" + GenericJsonKeys.updateType.name()
				+ "\": \"WorksheetHierarchicalHeadersUpdate\",");
		pw.println("\"" + JsonKeys.worksheetId.name() + "\": \"" + vWorksheet.getId()
				+ "\",");
		pw.println("\""+JsonKeys.rows.name()+ "\":");
		table.generateJson(pw, trans, false);
		pw.println(prefix + "}");
	}

}
