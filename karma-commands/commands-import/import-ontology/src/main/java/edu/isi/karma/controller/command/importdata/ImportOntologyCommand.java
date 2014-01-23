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

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ImportPropertiesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.view.VWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.PrintWriter;

public class ImportOntologyCommand extends ImportFileCommand implements IPreviewable {

    private static Logger logger = LoggerFactory.getLogger(ImportOntologyCommand.class);
    private String encoding = null;
    
    @Override
    protected Import createImport(Workspace workspace) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private enum JsonKeys {
        Import
    }

    public ImportOntologyCommand(String id, File file) {
        super(id, file);
        this.encoding = EncodingDetector.detect(file);
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

    public void setEncoding(String encoding) {
    	this.encoding = encoding;
    }
    
    @Override
    public UpdateContainer doIt(Workspace workspace) throws CommandException {
        OntologyManager ontManager = workspace.getOntologyManager();


        logger.debug("Loading ontology: " + getFile().getAbsolutePath());
        final boolean success = ontManager.doImportAndUpdateCache(getFile(), encoding);
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
    
    @Override
    public UpdateContainer handleUserActions(HttpServletRequest request) {
       
        String strEncoding = request.getParameter("encoding");
        if(strEncoding == null || strEncoding == "") {
        	try {
        		strEncoding = EncodingDetector.detect(getFile());
        	} catch(Exception e) {
        		strEncoding = EncodingDetector.DEFAULT_ENCODING;
        	}
        }
        setEncoding(strEncoding);
       
        /**
         * Send response based on the interaction type *
         */
        UpdateContainer c = null;
        ImportFileInteractionType type = ImportFileInteractionType.valueOf(request
                .getParameter("interactionType"));
        switch (type) {
            case generatePreview: {
                try {

                    c = showPreview();
                } catch (CommandException e) {
                    logger.error(
                            "Error occured while creating utput JSON for JSON Import",
                            e);
                }
                return c;
            }
            case importTable:
                return c;
        }
        return c;
    }

	@Override
	public UpdateContainer showPreview() throws CommandException {
		
        UpdateContainer c = new UpdateContainer();
        c.add(new ImportPropertiesUpdate(getFile(), encoding, -1, id));
        return c;
	   
	}
}
