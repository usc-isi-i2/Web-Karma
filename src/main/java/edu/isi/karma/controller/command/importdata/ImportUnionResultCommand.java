/**
 * *****************************************************************************
 * Copyright 2012 University of Southern California
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * This code was developed by the Information Integration Group as part of the
 * Karma project at the Information Sciences Institute of the University of
 * Southern California. For more information, publications, and related
 * projects, please see: http://www.isi.edu/integration
 *****************************************************************************
 */
package edu.isi.karma.controller.command.importdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.rdf.UnionImport;
import edu.isi.karma.imp.Import;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;

public class ImportUnionResultCommand extends ImportCommand {

    private static Logger logger = LoggerFactory.getLogger(ImportJSONFileCommand.class);

    protected ImportUnionResultCommand(String id) {
        super(id);
    }

    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Merge Datasets";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {

        UpdateContainer c = new UpdateContainer();
        try {
            Import uim = new UnionImport("union", workspace);
            Worksheet wsht = uim.generateWorksheet();
            
            c.add(new WorksheetListUpdate());
            c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId()));
        } catch (Exception e) {
            logger.error("Error occured while generating worksheet from JSON!", e);
            return new UpdateContainer(new ErrorUpdate(
                    "Error occured while importing JSON File."));
        }
        return c;
    }
}
