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

import edu.isi.karma.rep.hierarchicalheadings.ColspanMap;
import edu.isi.karma.rep.hierarchicalheadings.ColumnCoordinateSet;
import edu.isi.karma.rep.hierarchicalheadings.HHTable;
import edu.isi.karma.rep.hierarchicalheadings.HHTree;
import edu.isi.karma.rep.hierarchicalheadings.LeafColumnIndexMap;
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
		
		ColspanMap cspanmap = new ColspanMap(hHtree);
		ColumnCoordinateSet ccSet = new ColumnCoordinateSet(hHtree, cspanmap);
		vWorksheet.setColumnCoordinatesSet(ccSet);
		LeafColumnIndexMap lfMap = new LeafColumnIndexMap(hHtree);
		vWorksheet.setLeafColIndexMap(lfMap);
		
		hHtree.computeHTMLColSpanUsingColCoordinates(ccSet, cspanmap);
		
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
