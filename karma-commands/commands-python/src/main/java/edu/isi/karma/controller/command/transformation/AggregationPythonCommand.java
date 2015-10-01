package edu.isi.karma.controller.command.transformation;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetSelectionCommand;
import edu.isi.karma.controller.command.selection.SuperSelection;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.CloneTableUtils;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.er.helper.PythonTransformationHelper;
import edu.isi.karma.rep.*;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengyey on 9/15/15.
 */
public class AggregationPythonCommand extends WorksheetSelectionCommand {
    private String hNodeId;
    private String newHNodeId;
    private String pythonCode;
    private String constructor;
    private String newColumnName;
    private String newColumnAbsoluteName;
    
    public AggregationPythonCommand(String id, String model, String worksheetId,
                                    String selectionId, String hNodeId,
                                    String pythonCode, String constructor,
                                    String newColumnName) {
        super(id, model, worksheetId, selectionId);
        this.hNodeId = hNodeId;
        this.pythonCode = pythonCode;
        this.constructor = constructor;
        this.newColumnName = newColumnName;
        addTag(CommandTag.Transformation);
    }

    @Override
    public String getCommandName() {
        return AggregationPythonCommand.class.getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Aggregation";
    }

    @Override
    public String getDescription() {
        return newColumnAbsoluteName + ": " + constructor;
    }

    @Override
    public CommandType getCommandType() {
        return CommandType.undoable;
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
        PythonRepository repo = PythonRepositoryRegistry.getInstance().getPythonRepository(contextParameters.getParameterValue(ServletContextParameterMap.ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
        PythonInterpreter interpreter = repo.getInterpreter();
        repo.initializeInterpreter(interpreter);
        interpreter.getLocals().__setitem__("workspaceid", new PyString(workspace.getId()));
        interpreter.getLocals().__setitem__("selectionName", new PyString(selectionId));
        interpreter.getLocals().__setitem__("command", Py.java2py(this));
        interpreter.getLocals().__setitem__("worksheetId", new PyString(worksheetId));
        if (!pythonCode.trim().isEmpty()) {
            interpreter.exec(pythonCode);
        }
        RepFactory factory = workspace.getFactory();
        Worksheet worksheet = workspace.getWorksheet(worksheetId);
        SuperSelection selection = getSuperSelection(worksheet);
        HNode hNode = factory.getHNode(hNodeId);
        HTable hTable = hNode.getHTable(factory);
        HTable parentHTable = hTable.getParentHNode().getHTable(factory);
        newHNodeId = parentHTable.addHNode(newColumnName, HNode.HNodeType.Transformation, worksheet, factory).getId();
        HNode newHNode = factory.getHNode(newHNodeId);
        newColumnAbsoluteName = newHNode.getAbsoluteColumnName(factory);
        
        List<Table> parentTables = new ArrayList<>();
        CloneTableUtils.getDatatable(worksheet.getDataTable(), parentHTable, parentTables, selection);
        for (Table parentTable : parentTables) {
            for (Row parentRow : parentTable.getRows(0, parentTable.getNumRows(), selection)) {
                interpreter.getLocals().__setitem__("nodeid", new PyString(parentRow.getNode(hTable.getParentHNode().getId()).getId()));
                Table nestedTable = parentRow.getNode(hTable.getParentHNode().getId()).getNestedTable();
                String instanceName = "aggregation" + System.currentTimeMillis();
                interpreter.exec(instanceName + " = " + constructor);
                if (nestedTable != null) {
                    for (Row nestedRow : nestedTable.getRows(0, nestedTable.getNumRows(), selection)) {
                        interpreter.getLocals().__setitem__("nodeid", new PyString(nestedRow.getNode(hNodeId).getId()));
                        interpreter.exec(String.format("%s.accumulate(%s.transform())", instanceName, instanceName));
                    }
                }
                interpreter.getLocals().__setitem__("nodeid", new PyString(parentRow.getNode(hTable.getParentHNode().getId()).getId()));
                PyObject returnVal = interpreter.eval(String.format("%s.getResult()", instanceName));
                parentRow.getNode(newHNodeId).setValue(PythonTransformationHelper.getPyObjectValueAsString(returnVal), Node.NodeStatus.original, factory);
            }
        }
        UpdateContainer c =  new UpdateContainer();
        c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, selection, workspace.getContextId()));
        c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
        WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
        return c;
    }

    @Override
    public UpdateContainer undoIt(Workspace workspace) {
        RepFactory factory = workspace.getFactory();
        HTable hTable = factory.getHNode(newHNodeId).getHTable(factory);
        hTable.removeHNode(newHNodeId, factory.getWorksheet(worksheetId));
        UpdateContainer c =  new UpdateContainer();
        c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(workspace), workspace.getContextId()));
        c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
        WorksheetUpdateFactory.detectSelectionStatusChange(worksheetId, workspace, this);
        return c;
    }

    public void addInputColumns(String hNodeId) {
        inputColumns.add(hNodeId);
    }
}
