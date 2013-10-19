/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
/**
 * 
 */
package edu.isi.karma.controller.command.worksheet;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
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
