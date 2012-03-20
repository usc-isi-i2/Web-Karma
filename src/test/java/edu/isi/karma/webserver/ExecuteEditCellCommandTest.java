package edu.isi.karma.webserver;

import static edu.isi.karma.controller.update.AbstractUpdate.GenericJsonKeys.updateType;
import static edu.isi.karma.controller.update.UpdateContainer.JsonKeys.elements;
import static edu.isi.karma.controller.update.NodeChangedUpdate.JsonKeys.fullValue;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.EditCellCommand;
import edu.isi.karma.controller.command.EditCellCommandFactory;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.update.NodeChangedUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.CellValue;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.view.VWorksheet;

public class ExecuteEditCellCommandTest extends AbstractExecuteCommandTester {

	public ExecuteEditCellCommandTest(String name) {
		super(name);
	}

	public void testInvokeEditCellCommand() throws JSONException, CommandException {
		SampleDataFactory.createFlatWorksheet(vwsp.getWorkspace(), 2, 3);
		vwsp.addAllWorksheets();
		VWorksheet vw = vwsp.getVWorksheetList().getVWorksheets().get(0);

		ArrayList<Row> rows = vw.getWorksheet().getDataTable().getRows(0, 1);
		Node n = rows.get(0).getNodes().iterator().next();
		CellValue originalValue = n.getValue();

		r.setParameter("command", EditCellCommand.class.getSimpleName());
		r.setParameter(EditCellCommandFactory.Arguments.vWorksheetId.name(),
				vw.getId());
		r.setParameter(EditCellCommandFactory.Arguments.nodeId.name(),
				n.getId());
		r.setParameter(EditCellCommandFactory.Arguments.value.name(), "x");

		Command c = em.getCommand(r);
		String response = em.invokeCommand(c);
		System.err.println(response);

		JSONObject jo = new JSONObject(new JSONTokener(response));
		JSONArray ja = jo.getJSONArray(elements.name());
		assertEquals(2, ja.length());

		JSONObject nodeChangeUpdate = ja.getJSONObject(1);
		assertEquals(NodeChangedUpdate.class.getSimpleName(),
				nodeChangeUpdate.getString(updateType.name()));
		assertEquals("x", nodeChangeUpdate.getString(fullValue.name()));

		CommandHistory ch = vwsp.getWorkspace().getCommandHistory();
		assertTrue(ch.isUndoEnabled());
		assertFalse(ch.isRedoEnabled());

		UpdateContainer uc = ch.undoOrRedoCommandsUntil(vwsp, c.getId());
		String undoResponse = uc.generateJson(vwsp);
		//System.err.println(undoResponse);
		assertEquals(originalValue, n.getValue());
		
		JSONObject jo2 = new JSONObject(new JSONTokener(undoResponse));
		JSONArray ja2 = jo2.getJSONArray(elements.name());
		assertEquals(1, ja2.length());

		JSONObject nodeChangeUpdate2 = ja2.getJSONObject(0);
		assertEquals("Value 2", nodeChangeUpdate2.getString(fullValue.name()));
	}
}
