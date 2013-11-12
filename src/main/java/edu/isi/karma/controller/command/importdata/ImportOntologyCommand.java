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

import java.io.File;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class ImportOntologyCommand extends ImportFileCommand {

    private static Logger logger = LoggerFactory.getLogger(ImportOntologyCommand.class);

    @Override
    protected Import createImport(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private enum JsonKeys {
        Import
    }

    public ImportOntologyCommand(String id, File file) {
        super(id, file);
    }

    @Override
    public String getCommandName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getTitle() {
        return "Import Ontology";
    }

    @Override
    public String getDescription() {
        return getFile().getName();
    }

    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        OntologyManager ontManager = workspace.getOntologyManager();


        logger.debug("Loading ontology: " + getFile().getAbsolutePath());
        final boolean success = ontManager.doImportAndUpdateCache(getFile());
        logger.debug("Done loading ontology: " + getFile().getAbsolutePath());
        return new UpdateContainer(new AbstractUpdate() {
            @Override
            public void generateJson(String prefix, PrintWriter pw,
                    VWorkspace vWorkspace) {
                pw.println("{");
                pw.println("	\"" + GenericJsonKeys.updateType.name() + "\": \"" + ImportOntologyCommand.class.getSimpleName() + "\",");
                pw.println("	\"" + JsonKeys.Import.name() + "\":" + success);
                pw.println("}");
            }
        });
    }
}
