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

package edu.isi.karma.controller.command;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public abstract class ModelingHistoryCheckingCommandFactory extends CommandFactory {

	private static Logger logger = LoggerFactory.getLogger(ModelingHistoryCheckingCommandFactory.class);

	protected void executeModelingCommands(Workspace workspace,
			String worksheetId, Worksheet worksheet) {
		// Check if any command history exists for the worksheet
		if (HistoryJsonUtil.historyExists(worksheet.getTitle(),
				workspace.getCommandPreferencesId())) {
			WorksheetCommandHistoryReader commReader = new WorksheetCommandHistoryReader(
					worksheetId, workspace);
			WorksheetCommandHistoryExecutor commExecutor = new WorksheetCommandHistoryExecutor(
					worksheetId, workspace);
			try {
				List<CommandTag> tags = new ArrayList<CommandTag>();
				tags.add(CommandTag.Modeling);
				commExecutor.executeAllCommands(commReader.readCommandsByTag(tags));
			} catch (Exception e) {
				logger.error(
						"Error occured while reading model commands from history!",
						e);
			}
		}
	}
}
