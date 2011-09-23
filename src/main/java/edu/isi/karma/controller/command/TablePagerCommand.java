/**
 * 
 */
package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDataUpdate;
import edu.isi.karma.rep.TablePager;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;

/**
 * @author szekely
 * 
 */
public class TablePagerCommand extends Command {

	private final String tableIdArg;

	enum Direction {
		showPrevious("up"), showNext("down");

		String userLabel;

		private Direction(String userLabel) {
			this.userLabel = userLabel;
		}

		String getUserLabel() {
			return userLabel;
		}
	}

	private final Direction directionArg;
	private final String vWorksheetIdArg;

	protected TablePagerCommand(String id, String vWorksheetIdArg,
			String tableIdArg, String direction) {
		super(id);
		this.tableIdArg = tableIdArg;
		this.vWorksheetIdArg = vWorksheetIdArg;
		this.directionArg = Direction.valueOf(direction);
	}

	@Override
	public String getCommandName() {
		return "Page up/down";
	}

	@Override
	public String getTitle() {
		return "Page up/down";
	}

	@Override
	public String getDescription() {
		return "Page " + directionArg.getUserLabel();
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
		switch (directionArg) {
		case showPrevious:
			pager.moveToPreviousPage();
			break;
		case showNext:
			pager.moveToNextPage();
		}
		vWorksheet.udateDataTable(vWorkspace.getViewFactory());
		return new UpdateContainer(new WorksheetDataUpdate(vWorksheet));
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// not undoable.
		return new UpdateContainer();
	}

}
