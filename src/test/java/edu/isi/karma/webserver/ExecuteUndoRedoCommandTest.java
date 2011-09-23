package edu.isi.karma.webserver;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.EditCellCommand;
import edu.isi.karma.controller.command.EditCellCommandFactory;
import edu.isi.karma.controller.command.UndoRedoCommand;
import edu.isi.karma.controller.command.UndoRedoCommandFactory;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.view.VWorksheet;

public class ExecuteUndoRedoCommandTest extends AbstractExecuteCommandTester {

	public ExecuteUndoRedoCommandTest(String name) {
		super(name);
	}

	public void testInvokeEditCellCommand() throws JSONException {
		Worksheet w1 = SampleDataFactory.createFlatWorksheet(
				vwsp.getWorkspace(), 1, 2);
		Worksheet w2 = SampleDataFactory.createFlatWorksheet(
				vwsp.getWorkspace(), 1, 2);
		vwsp.addAllWorksheets();
		VWorksheet vw1 = vwsp.getVWorksheetList().getVWorksheets().get(0);
		//VWorksheet vw2 = vwsp.getVWorksheetList().getVWorksheets().get(0);

		Row w1r = w1.getDataTable().getRows(0, 1).get(0);
		Row w2r = w2.getDataTable().getRows(0, 1).get(0);

		Node[] w1Nodes = new Node[w1r.getNodes().size()];
		w1r.getNodes().toArray(w1Nodes);
		Node[] w2Nodes = new Node[w2r.getNodes().size()];
		w2r.getNodes().toArray(w2Nodes);

		String w1n0Value = w1Nodes[0].getValue().asString();

		// Edit 1
		Command c1 = invokeEditCellCommand(vw1, w1Nodes[0], "1");
		assertNodeValue(w1Nodes[0], "1");

		// Edit 2
		Command c2 = invokeEditCellCommand(vw1, w1Nodes[0], "2");
		assertNodeValue(w1Nodes[0], "2");

		// Undo Edit 2
		String u2 = invokeUndoRedo(c2);
		System.err.println(u2);
		new JSONObject(u2);
		assertNodeValue(w1Nodes[0], "1");
		assertEquals(1, getRedoStack().size());

		// Undo Edit 1
		String u1 = invokeUndoRedo(c1);
		System.err.println(u1);
		new JSONObject(u1);
		assertNodeValue(w1Nodes[0], w1n0Value);
		assertEquals(2, getRedoStack().size());
		assertEquals(c2.getId(), getRedoStack().get(0).getId());
		assertEquals(c1.getId(), getRedoStack().get(1).getId());

		// Redo Edit 1
		String r1 = invokeUndoRedo(c1);
		System.err.println("After Redo Edit 1: " + r1);
		new JSONObject(r1);
		assertNodeValue(w1Nodes[0], "1");
		assertEquals(1, getRedoStack().size());
		assertEquals(c2.getId(), getRedoStack().get(0).getId());
		
		// Undo Edit 1 again
		String u1a = invokeUndoRedo(c1);
		System.err.println("After Undo Edit 1 again: " + u1a);
		new JSONObject(u1a);
		assertNodeValue(w1Nodes[0], w1n0Value);
		assertEquals(2, getRedoStack().size());
		assertEquals(c2.getId(), getRedoStack().get(0).getId());
		assertEquals(c1.getId(), getRedoStack().get(1).getId());
		
		// Redo Edit 2
		String r2 = invokeUndoRedo(c2);
		System.err.println("After Redo Edit 2: " + r2);
		new JSONObject(r2);
		assertNodeValue(w1Nodes[0], "2");
		assertEquals(0, getRedoStack().size());
	}

	private void assertNodeValue(Node node, String value) {
		assertEquals(value, node.getValue().asString());
	}

	private ArrayList<Command> getRedoStack() {
		return vwsp.getWorkspace().getCommandHistory()._getRedoStack();
	}

	@SuppressWarnings("unused")
	private ArrayList<Command> getHistory() {
		return vwsp.getWorkspace().getCommandHistory()._getHistory();
	}

	private Command invokeEditCellCommand(VWorksheet vWorksheet, Node node,
			String value) {
		NullHttpServletRequest request = new NullHttpServletRequest();
		request.setParameter("command", EditCellCommand.class.getSimpleName());
		request.setParameter(
				EditCellCommandFactory.Arguments.vWorksheetId.name(),
				vWorksheet.getId());
		request.setParameter(EditCellCommandFactory.Arguments.nodeId.name(),
				node.getId());
		request.setParameter(EditCellCommandFactory.Arguments.value.name(),
				value);

		Command c = em.getCommand(request);
		em.invokeCommand(c);
		return c;
	}

	private String invokeUndoRedo(Command commandToUndo) {
		NullHttpServletRequest request = new NullHttpServletRequest();
		request.setParameter("command", UndoRedoCommand.class.getSimpleName());
		request.setParameter(UndoRedoCommandFactory.Arguments.commandId.name(),
				commandToUndo.getId());
		Command undoCommand = em.getCommand(request);
		assertNotNull(undoCommand);
		return em.invokeCommand(undoCommand);
	}

}
