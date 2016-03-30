package edu.isi.karma.controller.command.selection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.python.core.Py;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.er.helper.PythonTransformationHelper;
import edu.isi.karma.rep.Node;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

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

	public void updateSelection() {
		if (this.status == SelectionStatus.UP_TO_DATE)
			return;
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		PythonRepository repo = PythonRepositoryRegistry.getInstance().getPythonRepository(contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonInterpreter interpreter = repo.getInterpreter();
		repo.initializeInterpreter(interpreter);
		evalColumns.clear();
		String transformId = String.format("%d_%d_%s", System.currentTimeMillis(), Thread.currentThread().getId(), this.superSelectionName);
		PyCode code = null;
		try {
			code = getCompiledCode(pythonCode, interpreter, transformId);
		}catch(IOException e) {
			logger.error("Code error", e);
		}
		for (Entry<Row, Boolean> entry : this.selectedRowsCache.entrySet()) {
			Row key = entry.getKey();
				entry.setValue(evaluatePythonExpression(key, code, interpreter));
		}
		this.status = SelectionStatus.UP_TO_DATE;
	}

	public void addInputColumns(String hNodeId) {
		evalColumns.add(hNodeId);
	}

	private void populateSelection() {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		List<Table> tables = new ArrayList<>();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		CloneTableUtils.getDatatable(worksheet.getDataTable(), workspace.getFactory().getHTable(hTableId), tables, SuperSelectionManager.DEFAULT_SELECTION);
		String selectionId = String.format("%d_%d_%s", System.currentTimeMillis(), Thread.currentThread().getId(), this.superSelectionName);//Thread.currentThread().getId() + this.superSelectionName;
		PythonRepository repo = PythonRepositoryRegistry.getInstance().getPythonRepository(contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonInterpreter interpreter = repo.getInterpreter();
		repo.initializeInterpreter(interpreter);
		PyCode code;
		try {
			code = getCompiledCode(pythonCode, interpreter, selectionId);
			interpreter.getLocals().__setitem__("selection", interpreter.get("selection"+selectionId));
			
		
			for (Table t : tables) {
				for (Row r : t.getRows(0, t.getNumRows(), SuperSelectionManager.DEFAULT_SELECTION)) {
					if (code == null)
						selectedRowsCache.put(r, onError);
					else
						selectedRowsCache.put(r, evaluatePythonExpression(r, code, interpreter));
				}
			}
		} catch(Exception e) {
			logger.error("Unable to populate selection");
		}
	}

	private boolean evaluatePythonExpression(Row r, PyCode code, PythonInterpreter interpreter) {
		evalColumns.clear();
		try {
			ArrayList<Node> nodes = new ArrayList<>(r.getNodes());
			Node node = nodes.get(0);
			interpreter.getLocals().__setitem__("nodeid", new PyString(node.getId()));
			PyObject output = interpreter.eval(code);
			return PythonTransformationHelper.getPyObjectValueAsBoolean(output);
		}catch(Exception e) {
			return onError;
		}

	}

	private PyCode getCompiledCode(String pythonCode, PythonInterpreter interpreter, String selectionId) throws IOException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		String trimmedSelectionCode = pythonCode.trim();
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		if (trimmedSelectionCode.isEmpty()) {
			trimmedSelectionCode = "return False";
		}
		String selectionMethodStmt = PythonTransformationHelper
				.getPythonSelectionMethodDefinitionState(worksheet,
						trimmedSelectionCode, selectionId);


		logger.debug("Executing PySelection\n" + selectionMethodStmt);

		// Prepare the Python interpreter
		PythonRepository repo = PythonRepositoryRegistry.getInstance().getPythonRepository(contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		
		repo.compileAndAddToRepositoryAndExec(interpreter, selectionMethodStmt);
		PyObject locals = interpreter.getLocals();
		locals.__setitem__("workspaceid", new PyString(workspace.getId()));
		locals.__setitem__("selectionName", new PyString(superSelectionName));
		locals.__setitem__("command", Py.java2py(this));
		locals.__setitem__("worksheetId", new PyString(worksheetId));
		return repo.getSelectionCode();
	}	

}
