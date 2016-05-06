package edu.isi.karma.controller.command.worksheet;

import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.VWorkspaceRegistry;


public class OrganizeColumnsCommand extends WorksheetCommand {
	private JSONArray prevOrderedColumns;
	private JSONArray orderedColumns;

	protected OrganizeColumnsCommand(String id, String model, String workspaceId, String worksheetId, org.json.JSONArray orderedColumns) {
		super(id, model, worksheetId);
		this.orderedColumns = orderedColumns;

		addTag(CommandTag.Transformation);
		addTag(CommandTag.IgnoreInBatch);
	}

	@Override
	public String getCommandName() {
		return OrganizeColumnsCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Organize Columns";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.undoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		orderColumns(workspace.getId(), orderedColumns);
		UpdateContainer c =  new UpdateContainer();
		c.append(WorksheetUpdateFactory.createWorksheetHierarchicalUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));

		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		
		if(prevOrderedColumns != null)
			orderColumns(workspace.getId(), prevOrderedColumns);
		
		UpdateContainer c =  new UpdateContainer();
		c.append(WorksheetUpdateFactory.createWorksheetHierarchicalUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	private void orderColumns(String workspaceId, JSONArray columns) {
		VWorkspace vWorkspace = VWorkspaceRegistry.getInstance().getVWorkspace(workspaceId);
		if (vWorkspace != null) {
			vWorkspace.createVWorksheetsForAllWorksheets();
			VWorksheet viewWorksheet = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
			prevOrderedColumns = viewWorksheet.getHeaderViewNodesJSON();

			viewWorksheet.organizeColumns(columns);
			updateOrderedColumns(viewWorksheet);
		}
	}

	
	
	private void updateOrderedColumns(VWorksheet viewWorksheet) {
		orderedColumns = viewWorksheet.getHeaderViewNodesJSON();
		JSONArray inputParams = new JSONArray(getInputParameterJson());
		for(int i=0; i<inputParams.length(); i++) {
			JSONObject param = inputParams.getJSONObject(i);
			if(param.getString("name").equals("orderedColumns")) {
				param.put("value", orderedColumns);
				inputParams.put(i, param);
				break;
			}
		}
		setInputParameterJson(inputParams.toString());
	}
}
