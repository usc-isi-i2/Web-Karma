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

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.ImportServiceCommandPreferencesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.sources.InvocationManager;
import edu.isi.karma.util.HTTPUtil;

public class ImportServiceCommand extends ImportCommand {

    private static Logger logger = LoggerFactory.getLogger(ImportServiceCommand.class);
    private String serviceUrl;
    private String worksheetName;
    private boolean includeInputAttributes;
    private String encoding;

	protected ImportServiceCommand(String id, String model, String ServiceUrl, String worksheetName,
            boolean includeInputAttributes, String encoding) {
        super(id, model);
        this.serviceUrl = ServiceUrl;
        this.worksheetName = worksheetName;
        this.includeInputAttributes = includeInputAttributes;
        this.encoding = encoding;
    }

    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Import Service";
    }

    @Override
    public String getDescription() {
        if (serviceUrl.length() > 50) {
            return serviceUrl.substring(0, 50);
        } else {
            return serviceUrl;
        }
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        //save the preferences 

        UpdateContainer c = new UpdateContainer();

        try {
        	Object json = HTTPUtil.executeAndParseHTTPGetService(serviceUrl, includeInputAttributes);
			logger.debug(json.toString());
            Import imp = new JsonImport(json, worksheetName, workspace, encoding, -1);

            Worksheet wsht = imp.generateWorksheet();
            c.add(new ImportServiceCommandPreferencesUpdate(serviceUrl, worksheetName));

            c.add(new WorksheetListUpdate());
            c.append(WorksheetUpdateFactory.createWorksheetHierarchicalAndCleaningResultsUpdates(wsht.getId(), SuperSelectionManager.DEFAULT_SELECTION, workspace.getContextId()));
            return c;
        } catch (Exception e) {
            logger.error("Error occured while creating worksheet from web-service: " + serviceUrl);
            return new UpdateContainer(new ErrorUpdate("Error creating worksheet from web-service"));
        }
    }

    @Override
    protected Import createImport(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
