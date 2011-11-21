package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.rep.hierarchicalheadings.HHTable;
import edu.isi.karma.rep.hierarchicalheadings.HHTree;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.alignmentHeadings.AlignmentColorKeyTranslator;
import edu.isi.karma.view.alignmentHeadings.AlignmentForest;

public class AlignmentHeadersUpdate extends AbstractUpdate {

	private String vWorksheetId;
	AlignmentForest forest;

	private enum JsonKeys {
		worksheetId, rows
	}

	public AlignmentHeadersUpdate(AlignmentForest forest, String worksheetId) {
		this.forest = forest;
		this.vWorksheetId = worksheetId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		HHTree hHtree = new HHTree();
		hHtree.constructHHTree(forest);

		HHTable table = new HHTable();
		table.constructCells(hHtree);

		AlignmentColorKeyTranslator trans = new AlignmentColorKeyTranslator();
		pw.println(prefix + "{");
		pw.println("\"" + GenericJsonKeys.updateType.name()
				+ "\": \"AlignmentHeadersUpdate\",");
		pw.println("\"" + JsonKeys.worksheetId.name() + "\": \"" + vWorksheetId
				+ "\",");
		pw.println("\""+JsonKeys.rows.name()+ "\":");
		table.generateJson(pw, trans);
		pw.println(prefix + "}");
	}

}
