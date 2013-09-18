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

import edu.isi.karma.rep.hierarchicalheadings.HHTable;
import edu.isi.karma.rep.hierarchicalheadings.HHTree;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.alignmentHeadings.AlignmentColorKeyTranslator;
import edu.isi.karma.view.alignmentHeadings.AlignmentForest;

public class AlignmentHeadersUpdate extends AbstractUpdate {

	private String worksheetId;
	private String alignmentId;
	AlignmentForest forest;

	public enum JsonKeys {
		worksheetId, rows, alignmentId
	}

	public AlignmentHeadersUpdate(AlignmentForest forest, String worksheetId, String alignmentId) {
		this.forest = forest;
		this.worksheetId = worksheetId;
		this.alignmentId = alignmentId;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		HHTree hHtree = new HHTree();
		hHtree.constructHHTree(forest);
		
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
		hHtree.computeHTMLColSpanUsingLeafColumnIndices(vw.getColumnCoordinatesSet(), vw.getLeafColIndexMap());

		HHTable table = new HHTable();
		table.constructCells(hHtree);

		AlignmentColorKeyTranslator trans = new AlignmentColorKeyTranslator();
		pw.println(prefix + "{");
		pw.println("\"" + GenericJsonKeys.updateType.name()
				+ "\": \""+AlignmentHeadersUpdate.class.getSimpleName()+"\",");
		pw.println("\"" + JsonKeys.worksheetId.name() + "\": \"" + worksheetId
				+ "\",");
		pw.println("\"" + JsonKeys.alignmentId.name() + "\": \"" + alignmentId
				+ "\",");
		pw.println("\""+JsonKeys.rows.name()+ "\":");
		table.generateJson(pw, trans, true);
		pw.println(prefix + "}");
	}

}
