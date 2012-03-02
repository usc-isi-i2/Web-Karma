package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.rep.hierarchicalheadings.HHTable;
import edu.isi.karma.rep.hierarchicalheadings.HHTree;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.alignmentHeadings.AlignmentColorKeyTranslator;
import edu.isi.karma.view.alignmentHeadings.AlignmentForest;

public class AlignmentHeadersUpdate extends AbstractUpdate {

	private String vWorksheetId;
	private String alignmentId;
	AlignmentForest forest;

	public enum JsonKeys {
		worksheetId, rows, alignmentId
	}

	public AlignmentHeadersUpdate(AlignmentForest forest, String worksheetId, String alignmentId) {
		this.forest = forest;
		this.vWorksheetId = worksheetId;
		this.alignmentId = alignmentId;
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
				+ "\": \""+AlignmentHeadersUpdate.class.getSimpleName()+"\",");
		pw.println("\"" + JsonKeys.worksheetId.name() + "\": \"" + vWorksheetId
				+ "\",");
		pw.println("\"" + JsonKeys.alignmentId.name() + "\": \"" + alignmentId
				+ "\",");
		pw.println("\""+JsonKeys.rows.name()+ "\":");
		table.generateJson(pw, trans, true);
		pw.println(prefix + "}");
	}

}
