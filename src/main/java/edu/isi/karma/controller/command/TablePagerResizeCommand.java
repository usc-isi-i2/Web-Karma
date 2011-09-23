/**
 * 
 */
package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

public class TablePagerResizeCommand extends Command {

	private final String tableIdArg;
	private final String vWorksheetIdArg;
	private final int	newPageSize;

	protected TablePagerResizeCommand(String id, String vWorksheetIdArg,
			String tableIdArg, String newPageSize) {
		super(id);
		this.tableIdArg = tableIdArg;
		this.vWorksheetIdArg = vWorksheetIdArg;
		this.newPageSize = Integer.parseInt(newPageSize);
	}

	@Override
	public String getCommandName() {
		return "Page resize";
	}

	@Override
	public String getTitle() {
		return "Page Resize";
	}

	@Override
	public String getDescription() {
		return "Page Resize to" + this.newPageSize;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		VWorksheet vWorksheet = vWorkspace.getViewFactory().getVWorksheet(
				vWorksheetIdArg);
		TablePager pager = vWorksheet.getTablePager(tableIdArg);
		pager.setDesiredSize(newPageSize);
		vWorksheet.udateDataTable(vWorkspace.getViewFactory());
		return new UpdateContainer(new WorksheetDataUpdate(vWorksheet));
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// not undoable.
		return new UpdateContainer();
	}

}
