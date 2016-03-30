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

import java.util.Set;

import edu.isi.karma.config.UIConfiguration;
import edu.isi.karma.config.UIConfigurationRegistry;
import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.selection.Selection;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class WorksheetUpdateFactory {

	private WorksheetUpdateFactory() {
	}

	public static UpdateContainer createWorksheetHierarchicalAndCleaningResultsUpdates(String worksheetId, SuperSelection sel, String contextId) {
		UpdateContainer c = new UpdateContainer();
		createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, c, sel, contextId);
		return c;
	}
	private static void createWorksheetHierarchicalAndCleaningResultsUpdates(
			String worksheetId, UpdateContainer c, SuperSelection sel, String contextId) {
		UIConfiguration uiConfiguration = UIConfigurationRegistry.getInstance().getUIConfiguration(contextId);
		boolean showCleaningCharts = uiConfiguration.isD3ChartsEnabled();
		if (showCleaningCharts)
			c.add(new WorksheetCleaningUpdate(worksheetId, true, sel));
		createWorksheetHierarchicalUpdates(worksheetId, c, sel);
	}

	public static UpdateContainer createWorksheetHierarchicalUpdates(String worksheetId, SuperSelection sel, String contextId) {
		UpdateContainer c = new UpdateContainer();
		UIConfiguration uiConfiguration = UIConfigurationRegistry.getInstance().getUIConfiguration(contextId);
		boolean showCleaningCharts = uiConfiguration.isD3ChartsEnabled();
		if (showCleaningCharts)
			c.add(new WorksheetCleaningUpdate(worksheetId, false, sel));
		createWorksheetHierarchicalUpdates(worksheetId, c, sel);
		return c;
	}

	private static void createWorksheetHierarchicalUpdates(String worksheetId,
			UpdateContainer c, SuperSelection sel) {
		c.add(new WorksheetHeadersUpdate(worksheetId, sel));
		c.add(new WorksheetDataUpdate(worksheetId, sel));
		c.add(new WorksheetSuperSelectionListUpdate(worksheetId));
	}
	public static UpdateContainer createRegenerateWorksheetUpdates(String worksheetId, SuperSelection sel, String contextId) {
		UpdateContainer c = new UpdateContainer();
		c.add(new RegenerateWorksheetUpdate(worksheetId));
		createWorksheetHierarchicalAndCleaningResultsUpdates(worksheetId, c, sel, contextId);
		return c;
	}
	public static UpdateContainer createSemanticTypesAndSVGAlignmentUpdates(String worksheetId, Workspace workspace)
	{
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		UpdateContainer c = new UpdateContainer();
		c.add(new SemanticTypesUpdate(worksheet, worksheetId));
		c.add(new AlignmentSVGVisualizationUpdate(worksheetId));
		return c;
	}

	public static void detectSelectionStatusChange(String worksheetId, Workspace workspace, Command command) {
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		for (Selection sel : worksheet.getSelectionManager().getAllDefinedSelection()) {
			Set<String> inputColumns = sel.getInputColumns();
			inputColumns.retainAll(command.getOutputColumns());
			if (!inputColumns.isEmpty() && !command.getCommandName().equals("OperateSelectionCommand") && !command.getCommandName().equals("ClearSelectionCommand"))
				sel.invalidateSelection();
			if (sel.isSelectedRowsMethod() && checkSelection(sel, command, workspace.getFactory())) {
				sel.invalidateSelection();
			}
		}
	}
	
	private static boolean checkSelection(Selection sel, Command command, RepFactory factory) {
		Set<String> selectedRowsColumns = sel.getSelectedRowsColumns();
		Set<String> outputColumns = command.getOutputColumns();
		for (String parent : selectedRowsColumns) {
			HTable parentHT = factory.getHTable(factory.getHNode(parent).getHTableId());
			for (String child : outputColumns) {
				HTable childHT = factory.getHTable(factory.getHNode(child).getHTableId());
				if (isChildHTable(parentHT, childHT, factory))
					return true;
			}
		}
		return false;
	}
	
	private static boolean isChildHTable(HTable parent, HTable child, RepFactory factory) {
		while (child != null) {
			HNode parentHN = child.getParentHNode();
			child = null;
			if (parentHN != null)
				child = parentHN.getHTable(factory);
			if (parent == child)
				return true;
		}
		return false;
	}
	
}
