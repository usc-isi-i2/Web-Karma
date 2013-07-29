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

package edu.isi.karma.controller.command.worksheet;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class RenameColumnCommand extends Command {
	final private String newColumnName;
	final private String hNodeId;
	final private String vWorksheetId;
	final private boolean getAlignmentUpdate;
	private String oldColumnName;

	public RenameColumnCommand(String id, String newColumnName, String hNodeId, String vWorksheetId, boolean getAlignmentUpdate) {
		super(id);
		this.newColumnName = newColumnName;
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
		this.getAlignmentUpdate = getAlignmentUpdate;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Rename Column";
	}

	
	@Override
	public String getDescription() {
		if (newColumnName.length() > 20)
			return newColumnName.substring(0, 19) + "...";
		return newColumnName;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		HNode columnNode = vWorkspace.getWorkspace().getFactory().getHNode(hNodeId);
		oldColumnName = columnNode.getColumnName();
		
		// Change the column name
		columnNode.setColumnName(newColumnName);
		
		// Prepare the output to be sent
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		Worksheet wk = vw.getWorksheet();
		
		UpdateContainer c =  new UpdateContainer();
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, wk, wk.getHeaders().getAllPaths(), vWorkspace);
		vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		vw.updateHeaders(c);
		if (getAlignmentUpdate) {
			addAlignmentUpdate(vWorkspace, wk, vw, c);
		}
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		HNode columnNode = vWorkspace.getWorkspace().getFactory().getHNode(hNodeId);
		// Change the column name
		columnNode.setColumnName(oldColumnName);
		
		// Prepare the output to be sent
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		Worksheet wk = vw.getWorksheet();
		
		UpdateContainer c =  new UpdateContainer();
		vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, wk, wk.getHeaders().getAllPaths(), vWorkspace);
		vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
		vw.updateHeaders(c);
		
		if (getAlignmentUpdate) {
			addAlignmentUpdate(vWorkspace, wk, vw, c);
		}
		
		return c;
	}
	

	private void addAlignmentUpdate(VWorkspace vWorkspace, Worksheet wk,
			VWorksheet vw, UpdateContainer c) {
		Alignment alignment = AlignmentManager.Instance().getAlignment(vWorkspace.getWorkspace().getId(), vWorksheetId);
		OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
		SemanticTypeUtil.computeSemanticTypesSuggestion(wk, vWorkspace.getWorkspace().getCrfModelHandler(), ontMgr, alignment);

		// Get the updated alignment
		c.add(new SemanticTypesUpdate(wk, vWorksheetId, alignment));
		c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vw, alignment));
	}

}
