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
package edu.isi.karma.controller.command;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.SVGAlignmentUpdate_ForceKarmaLayout;
import edu.isi.karma.controller.update.SemanticTypesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.Alignment;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Node.NodeStatus;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

/**
 * Adds a new column to the table with hTableId.
 * hTableId may be empty if a hNodeId is provided.
 * If hNodeId is provided, adds the new column after hNodeId.
 * If no hNodeId is provided adds the new column as the first column in the table hTableId.
 * Returns the hNodeId of the newly created column.
 */
public class AddColumnCommand extends WorksheetCommand {
	//if null add column at beginning of table
	private final String hNodeId;
	//add column to this table
	private String hTableId;
	private final String newColumnName;
	private final String defaultValue;
	private final String vWorksheetId;

	//the id of the new column that was created
	//needed for undo
	private String newHNodeId;
	
	private static Logger logger = LoggerFactory
	.getLogger(AddColumnCommand.class);
	
	public enum JsonKeys {
		updateType, hNodeId, vWorksheetId
	}
	
	protected AddColumnCommand(String id, String vWorksheetId, String worksheetId, 
			String hTableId, String hNodeId, String newColumnName, String defaultValue) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.hTableId = hTableId;
		this.newColumnName=newColumnName;
		this.vWorksheetId = vWorksheetId;
		this.defaultValue = defaultValue;
	}

	@Override
	public String getCommandName() {
		return AddColumnCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Add New Column";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getWorkspace().getWorksheet(
				worksheetId);
		
		try{
			if(hTableId==null || hTableId.isEmpty()){
				//get table id based on the hNodeId
				if(hNodeId==null)
					throw new KarmaException("TableId and NodeId are empty. Can't add column.");
				hTableId = vWorkspace.getRepFactory().getHNode(hNodeId).getHTableId();
			}
						
			//get the HTable
			HTable currentTable = vWorkspace.getRepFactory().getHTable(hTableId);
			//add new column to this table
			//add column after the column with hNodeId
			HNode ndid = currentTable.addNewHNodeAfter(hNodeId, vWorkspace.getRepFactory(), newColumnName, worksheet,true);
			//add as first column in the table if hNodeId is null
			//HNode ndid = currentTable.addNewHNodeAfter(null, vWorkspace.getRepFactory(), newColumnName, worksheet,true);

			//save the new hNodeId for undo
			newHNodeId = ndid.getId();
			
			// Populate the column with default value if default value is present
			if (this.defaultValue != null && !this.defaultValue.equals("")) {
				populateRowsWithDefaultValues(worksheet, vWorkspace.getRepFactory());
			}
			
			//create container and return hNodeId of newly created column
			UpdateContainer c =  new UpdateContainer(new AbstractUpdate() {
				@Override
				public void generateJson(String prefix, PrintWriter pw,
						VWorkspace vWorkspace) {
					JSONObject outputObject = new JSONObject();
					try {
						outputObject.put(JsonKeys.updateType.name(),
								"AddColumnUpdate");
						outputObject.put(JsonKeys.hNodeId.name(),newHNodeId);
						outputObject.put(JsonKeys.vWorksheetId.name(),
								vWorksheetId);
						pw.println(outputObject.toString(4));
					} catch (JSONException e) {
						logger.error("Error occured while generating JSON!");
					}
				}
			});
			
			vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet,
					worksheet.getHeaders().getAllPaths(), vWorkspace);
			VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
			vw.update(c);
			
			//THIS HAS A PROBLEM.
			//The alignment isn't generated
			// Get the alignment update if any
			// Shubham 2012/10/28 to move the red dots in case the the model is
			// being shown
			Alignment alignment = AlignmentManager.Instance().getAlignment(vWorkspace.getWorkspace().getId(), vWorksheetId);
			if (!worksheet.getSemanticTypes().getListOfTypes().isEmpty() && alignment!= null && alignment.isEmpty()) {
				try {
					// Compute suggestions for the new column 
					OntologyManager ontMgr = vWorkspace.getWorkspace().getOntologyManager();
					SemanticTypeUtil.computeSemanticTypesSuggestion(worksheet, vWorkspace.getWorkspace().getCrfModelHandler(), ontMgr, alignment);

					// Get the updated alignment
					c.add(new SemanticTypesUpdate(worksheet, vWorksheetId, alignment));
					c.add(new SVGAlignmentUpdate_ForceKarmaLayout(vw, alignment));
				} catch (Exception e) {
					return new UpdateContainer(
							new ErrorUpdate("Error occured while generating the model for the source."));
				}
			}
			return c;
		} catch (Exception e) {
			System.out.println("Error in AddColumnCommand" + e.toString());
			return new UpdateContainer(new ErrorUpdate(e.getMessage()));
		}
	}

	private void populateRowsWithDefaultValues(Worksheet worksheet, RepFactory factory) {
		HNodePath selectedPath = null;
		List<HNodePath> columnPaths = worksheet.getHeaders().getAllPaths();
		for (HNodePath path : columnPaths) {
			if (path.getLeaf().getId().equals(newHNodeId)) {
				selectedPath = path;
			}
		}
		Collection<Node> nodes = new ArrayList<Node>();
		worksheet.getDataTable().collectNodes(selectedPath, nodes);	
		for (Node node : nodes) {
			node.setValue(this.defaultValue, NodeStatus.original, factory);
		}
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		Worksheet worksheet = vWorkspace.getViewFactory()
		.getVWorksheet(vWorksheetId).getWorksheet();

		HTable currentTable = vWorkspace.getRepFactory().getHTable(hTableId);
		//remove the new column
		currentTable.removeHNode(newHNodeId, worksheet);

		UpdateContainer c = new UpdateContainer();
		vWorkspace.getViewFactory()
		.updateWorksheet(vWorksheetId, worksheet,
				worksheet.getHeaders().getAllPaths(), vWorkspace);
		VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(
				vWorksheetId);
		vw.update(c);

		return c;
	}

	public String getNewHNodeId() {
		return newHNodeId;
	}
}
