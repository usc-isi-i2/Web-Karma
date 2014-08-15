package edu.isi.karma.controller.command.worksheet;

import java.util.ArrayList;

import org.json.JSONArray;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VHNode;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.view.VWorkspaceRegistry;


public class OrganizeColumnsCommand extends WorksheetCommand {
	private String workspaceId;
	private ArrayList<VHNode> prevOrderedColumns;
	private JSONArray orderedColumns;

	protected OrganizeColumnsCommand(String id, String workspaceId, String worksheetId, org.json.JSONArray orderedColumns) {
		super(id, worksheetId);
		this.workspaceId = workspaceId;
		this.orderedColumns = orderedColumns;

		addTag(CommandTag.Transformation);
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
		orderColumns(orderedColumns);

		UpdateContainer c =  new UpdateContainer();
		c.append(WorksheetUpdateFactory.createWorksheetHierarchicalUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));

		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		if(prevOrderedColumns != null) {
			orderColumns(prevOrderedColumns);
		}
		UpdateContainer c =  new UpdateContainer();
		c.append(WorksheetUpdateFactory.createWorksheetHierarchicalUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION));
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		return c;
	}

	private void orderColumns(JSONArray columns) {
		VWorkspace vWorkspace = VWorkspaceRegistry.getInstance().getVWorkspace(workspaceId);
		if (vWorkspace != null) {
			VWorksheet viewWorksheet = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
			prevOrderedColumns = viewWorksheet.getHeaderViewNodes();

			viewWorksheet.organizeColumns(columns);
		}
	}

	private void orderColumns(ArrayList<VHNode> columns) {
		VWorkspace vWorkspace = VWorkspaceRegistry.getInstance().getVWorkspace(workspaceId);
		if (vWorkspace != null) {
			VWorksheet viewWorksheet = vWorkspace.getViewFactory().getVWorksheetByWorksheetId(worksheetId);
			prevOrderedColumns = viewWorksheet.getHeaderViewNodes();

			viewWorksheet.organizeColumns(columns);
		}
	}
}
