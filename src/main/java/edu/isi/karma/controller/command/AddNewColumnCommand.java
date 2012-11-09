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

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONObject;

import edu.isi.karma.cleaning.MyLogger;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.modeling.alignment.AlignToOntology;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HNodePath;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class AddNewColumnCommand extends WorksheetCommand {
	private final String hNodeId;
	private final String vWorksheetId;
	private String result;

	protected AddNewColumnCommand(String id, String vWorksheetId,
			String worksheetId, String hNodeId, String result) {
		super(id, worksheetId);
		this.hNodeId = hNodeId;
		this.vWorksheetId = vWorksheetId;
		this.result = result;
	}

	@Override
	public String getCommandName() {
		return AddNewColumnCommand.class.getSimpleName();
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
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		// pedro 2012-09-15: unused.
		// Worksheet wk = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = vWorkspace.getWorkspace().getWorksheet(
				worksheetId);
		System.out.println("Old Size"
				+ worksheet.getHeaders().getAllPaths().size());

		// pedro 2012-10-28: there needs to be a better way to do this. A more
		// efficient implementation should be done in HTable.
		HTable headers = worksheet.getHeaders();
		java.util.List<HNodePath> nodesList = headers.getAllPaths();
		HNodePath index = null;
		for (HNodePath hp : nodesList) {
			if (hp.toString().contains(hNodeId)) {
				index = hp;
			}
		}
		HNode xHNode = index.getLeaf();
		String existingColumnName = xHNode
				.getHTable(vWorkspace.getRepFactory()).getHNode(hNodeId)
				.getColumnName();
		xHNode.getHTable(vWorkspace.getRepFactory()).addNewHNodeAfter(hNodeId,
				vWorkspace.getRepFactory(), existingColumnName + "_copy",
				worksheet);
		// worksheet.getHeaders().addNewHNodeAfter(hNodeId,
		// vWorkspace.getRepFactory(), existingColumnName+"_copy", worksheet);
		HNode ndid = xHNode.getHTable(vWorkspace.getRepFactory())
				.getHNodeFromColumnName(existingColumnName + "_copy");
		System.out.println("" + ndid.getColumnName());
		Collection<Node> nodes = new ArrayList<Node>();
		worksheet.getDataTable().collectNodes(index, nodes);
		try {
			JSONObject jObject = new JSONObject(this.result);
			for (Node xnode : nodes) {
				Row r = xnode.getBelongsToRow();
				Node node = r.getNode(hNodeId);
				String t = jObject.getString(node.getId());
				// System.out.println(""+t+""+ndid.getId()+","+hNodeId);
				// resultString += t+"\n";
				r.setValue(ndid.getId(), t, vWorkspace.getRepFactory());

			}
			System.out.println("Old VW ID: " + vWorksheetId);
			vWorkspace.getViewFactory()
					.updateWorksheet(vWorksheetId, worksheet,
							worksheet.getHeaders().getAllPaths(), vWorkspace);
			VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(
					vWorksheetId);
			System.out.println("New VW ID: " + vw.getId());
			vw.update(c);
			String resultString = "";

			/************ collect info ************/
			String id = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId)
					.getWorksheetId();
			MyLogger.logsth(id + " results: " + resultString);
			MyLogger.logsth(id + " time span: " + MyLogger.getDuration(id));
			MyLogger.logsth("Finish Submitting: " + id);
			/*************************************/
		} catch (Exception e) {
			System.out.println("" + e.toString());
		}

		// Get the alignment update if any
		// Shubham 2012/10/28 to move the red dots in case the the model is
		// being shown
		if (!worksheet.getSemanticTypes().getListOfTypes().isEmpty()) {
			OntologyManager ontMgr = vWorkspace.getWorkspace()
					.getOntologyManager();
			SemanticTypeUtil.computeSemanticTypesSuggestion(worksheet,
					vWorkspace.getWorkspace().getCrfModelHandler(), ontMgr);

			AlignToOntology align = new AlignToOntology(worksheet, vWorkspace,
					vWorksheetId);
			try {
				align.alignAndUpdate(c, true);
			} catch (Exception e) {
				return new UpdateContainer(
						new ErrorUpdate(
								"Error occured while generating the model for the source."));
			}
		}
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
