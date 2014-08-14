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

import java.util.HashSet;
import java.util.Set;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class WorksheetUpdateFactory {

	public static UpdateContainer createWorksheetHierarchicalAndCleaningResultsUpdates(String worksheetId, SuperSelection sel) {
		UpdateContainer c = new UpdateContainer();
		createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, c, sel);
		return c;
	}
	private static void createWorksheetHierarchicalAndCleaningResultsUpdates(
			String worksheetId, UpdateContainer c, SuperSelection sel) {
		c.add(new WorksheetCleaningUpdate(worksheetId, true, sel));
		createWorksheetHierarchicalUpdates(worksheetId, c, sel);
	}
	
	public static UpdateContainer createWorksheetHierarchicalUpdates(String worksheetId, SuperSelection sel) {
		UpdateContainer c = new UpdateContainer();
		c.add(new WorksheetCleaningUpdate(worksheetId, false, sel));
		createWorksheetHierarchicalUpdates(worksheetId, c, sel);
		return c;
	}
	
	private static void createWorksheetHierarchicalUpdates(String worksheetId,
			UpdateContainer c, SuperSelection sel) {
		c.add(new WorksheetHeadersUpdate(worksheetId));
		c.add(new WorksheetDataUpdate(worksheetId, sel));
		c.add(new WorksheetSuperSelectionListUpdate(worksheetId));
	}
	public static UpdateContainer createRegenerateWorksheetUpdates(String worksheetId, SuperSelection sel) {
		UpdateContainer c = new UpdateContainer();
		c.add(new RegenerateWorksheetUpdate(worksheetId));
		createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, c, sel);
		return c;
	}
	public static UpdateContainer createSemanticTypesAndSVGAlignmentUpdates(String worksheetId, Workspace workspace, Alignment alignment)
	{
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, worksheetId, alignment));
		c.add(new AlignmentSVGVisualizationUpdate(worksheetId, alignment));
		return c;
	}
	
	public static void detectSelectionStatusChange(String worksheetId, Workspace workspace, Command command) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		for (Selection sel : worksheet.getSelectionManager().getDefinedSelection()) {
			Set<String> inputColumns = new HashSet<String>(sel.getInputColumns());
			inputColumns.retainAll(command.getOutputColumns());
			if (inputColumns.size() > 0)
				sel.invalidateSelection();
		}
	}

}
