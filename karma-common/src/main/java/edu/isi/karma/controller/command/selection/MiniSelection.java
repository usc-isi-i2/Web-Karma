package edu.isi.karma.controller.command.selection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonTransformationHelper;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class MiniSelection extends Selection {

	private String pythonCode;
	private boolean onError;
	private static Logger logger = LoggerFactory
			.getLogger(MiniSelection.class);

	public MiniSelection(Workspace workspace, String worksheetId, String hTableId,
			String name, String superSelectionName, String pythonCode, boolean onError) {
		super(workspace, worksheetId, hTableId, name, superSelectionName);
		this.pythonCode = pythonCode;
		this.onError = onError;
		populateSelection();
	}

	public void updateSelection(){
		if (this.status == SelectionStatus.UP_TO_DATE)
			return;
		evalColumns.clear();
		String transformId = Thread.currentThread().getName() + this.superSelectionName;
		for (Entry<Row, Boolean> entry : this.selectedRowsCache.entrySet()) {
			Row key = entry.getKey();
			PythonInterpreter interpreter = new PythonInterpreter();
			try {
				entry.setValue(evaluatePythonExpression(key, getCompiledCode(pythonCode, interpreter, transformId), interpreter));
			}catch(IOException e) {
				entry.setValue(false);
			}
		}
		this.status = SelectionStatus.UP_TO_DATE;
	}

	public void addInputColumns(String hNodeId) {
		evalColumns.add(hNodeId);
	}

	private void populateSelection() {
		List<Table> tables = new ArrayList<Table>();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		CloneTableUtils.getDatatable(worksheet.getDataTable(), workspace.getFactory().getHTable(hTableId), tables, SuperSelectionManager.DEFAULT_SELECTION);
		String transformId = Thread.currentThread().getName() + this.superSelectionName;
		PythonInterpreter interpreter = PythonRepository.getInstance().interpreter;
		PyCode code = null;
		try {
			code = getCompiledCode(pythonCode, interpreter, transformId);
			interpreter.set("transform", interpreter.get("transform"+transformId));
			
		} catch(Exception e) {
			
		}
		for (Table t : tables) {
			for (Row r : t.getRows(0, t.getNumRows(), SuperSelectionManager.DEFAULT_SELECTION)) {
				if (code == null)
					selectedRowsCache.put(r, onError);
				else
					selectedRowsCache.put(r, evaluatePythonExpression(r, code, interpreter));
			}
		}
	}

	private boolean evaluatePythonExpression(Row r, PyCode code, PythonInterpreter interpreter) {
		evalColumns.clear();
		try {
			ArrayList<Node> nodes = new ArrayList<Node>(r.getNodes());
			Node node = nodes.get(0);
			interpreter.set("nodeid", node.getId());
			PyObject output = interpreter.eval(code);
			return PythonTransformationHelper.getPyObjectValueAsBoolean(output);
		}catch(Exception e) {
			return onError;
		}

	}

	private PyCode getCompiledCode(String pythonCode, PythonInterpreter interpreter, String transformId) throws IOException {

		String trimmedTransformationCode = pythonCode.trim();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		if (trimmedTransformationCode.isEmpty()) {
			trimmedTransformationCode = "return False";
		}
		String transformMethodStmt = PythonTransformationHelper
				.getPythonTransformMethodDefinitionState(worksheet,
						trimmedTransformationCode,transformId);


		logger.debug("Executing PySelection\n" + transformMethodStmt);

		// Prepare the Python interpreter
		PythonRepository repo = PythonRepository.getInstance();

		repo.compileAndAddToRepositoryAndExec(interpreter, transformMethodStmt);

		interpreter.set("workspaceid", workspace.getId());
		interpreter.set("selectionName", superSelectionName);
		interpreter.set("command", this);
		return repo.getTransformCode();
	}	

}
