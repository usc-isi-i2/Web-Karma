package edu.isi.karma.controller.command.transformation;

import org.json.JSONArray;
import org.json.JSONException;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommand;
import edu.isi.karma.controller.command.worksheet.MultipleValueEditColumnCommandFactory;
import edu.isi.karma.controller.history.HistoryJsonUtil.ParameterType;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class RepeatPythonTransformationCommand extends PythonTransformationCommand {
	public RepeatPythonTransformationCommand(String id, String model, String worksheetId, 
			String hNodeId, String transformCode, String selectionId) {
		super(id, model, transformCode, worksheetId, hNodeId, "error", selectionId);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Repeat Python Transformation";
	}

	@Override
	public String getDescription() {
		return hNodeId;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		final ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getContextParameters(workspace.getContextId());
		Worksheet worksheet = workspace.getWorksheet(worksheetId);
		JSONArray transformedRows = new JSONArray();
		JSONArray errorValues = new JSONArray();
		PythonRepository repo = PythonRepositoryRegistry.getInstance().getPythonRepository(contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		repo.resetLibrary();
		boolean isError = false;
		try {
			generateTransformedValues(workspace, 
					worksheet, workspace.getFactory(), workspace.getFactory().getHNode(hNodeId), transformedRows, errorValues, null);

			if (errorValues.length() > 0) {
				isError = true;
			}
			JSONArray multiCellEditInput = getMultiCellValueEditInputJSON(transformedRows, hNodeId);
			MultipleValueEditColumnCommandFactory mfc = new MultipleValueEditColumnCommandFactory();
			MultipleValueEditColumnCommand mvecc =  (MultipleValueEditColumnCommand) mfc.createCommand(multiCellEditInput, model, workspace);
			mvecc.doIt(workspace);
		}catch(Exception e) {
			isError = true;
		}
		worksheet.getMetadataContainer().getColumnMetadata().addColumnOnError(hNodeId, isError);
		UpdateContainer c = new UpdateContainer();
		c.append(WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, getSuperSelection(worksheet), workspace.getContextId()));		
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

	private JSONArray getMultiCellValueEditInputJSON(JSONArray rowsArray, String newHNodeId) throws JSONException {
		JSONArray arr = new JSONArray();
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.worksheetId.name(), 
				worksheetId, ParameterType.worksheetId));
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.hNodeID.name(), 
				newHNodeId, ParameterType.worksheetId));
		arr.put(CommandInputJSONUtil.createJsonObject(MultipleValueEditColumnCommandFactory.Arguments.rows.name(), 
				rowsArray, ParameterType.other));
		return arr;
	}
}
