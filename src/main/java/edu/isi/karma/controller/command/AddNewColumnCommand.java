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

import org.json.JSONObject;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.HNode;
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

	protected AddNewColumnCommand(String id, String vWorksheetId, String worksheetId, String hNodeId, String result) {
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
		//pedro 2012-09-15: unused.
		//		Worksheet wk = vWorkspace.getRepFactory().getWorksheet(worksheetId);
		UpdateContainer c = new UpdateContainer();
		Worksheet worksheet = vWorkspace.getWorkspace().getWorksheet(worksheetId);
		System.out.println("Old Size" + worksheet.getHeaders().getAllPaths().size());
		HTable headers = worksheet.getHeaders();
		String existingColumnName = headers.getHNode(hNodeId).getColumnName();
		ArrayList<Row> rows = worksheet.getDataTable().getRows(0, worksheet.getDataTable().getNumRows());
		worksheet.getHeaders().addNewHNodeAfter(hNodeId, vWorkspace.getRepFactory(), existingColumnName+"_copy", worksheet);
		HNode ndid = worksheet.getHeaders().getHNodeFromColumnName(existingColumnName+"_copy");
		System.out.println(""+ndid.getColumnName());
		JSONObject jObject = null;
		try 
		{
			jObject  = new JSONObject(result);
			for(Row r:rows)
			{
				Node node = r.getNode(hNodeId);
				String t = jObject.getString(node.getId());
				System.out.println(""+t+""+ndid.getId()+","+hNodeId);
				r.setValue(ndid.getId(), t, vWorkspace.getRepFactory());
			}
			System.out.println("Old VW ID: " + vWorksheetId);
			vWorkspace.getViewFactory().updateWorksheet(vWorksheetId, worksheet, worksheet.getHeaders().getAllPaths(), vWorkspace);
			VWorksheet vw = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId);
			System.out.println("New VW ID: " + vw.getId());
			vw.update(c);
		} catch (Exception e) {
			System.out.println(""+e.toString());
		}
		return c;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
