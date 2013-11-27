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
 * ****************************************************************************
 */
package edu.isi.karma.controller.command.importdata;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.controller.command.importdata.ImportCSVFileCommand.InteractionType;
import edu.isi.karma.controller.update.ImportPropertiesUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.json.XMLImport;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.EncodingDetector;

public class ImportXMLFileCommand extends ImportFileCommand implements IPreviewable {
	private String encoding;
	private int maxNumLines = 100;
	
	private static Logger logger = LoggerFactory
            .getLogger(ImportXMLFileCommand.class.getSimpleName());
	
    protected ImportXMLFileCommand(String id, File uploadedFile) {
        super(id, uploadedFile);
        this.encoding = EncodingDetector.detect(uploadedFile);
    }

    protected ImportXMLFileCommand(String id, String revisedId, File uploadedFile) {
        super(id, revisedId, uploadedFile);
        this.encoding = EncodingDetector.detect(uploadedFile);
    }

    @Override
    public String getCommandName() {
        return "ImportXMLFileCommand";
    }

    @Override
    public String getTitle() {
        return "Import XML File";
    }

    @Override
    public String getDescription() {
        if (isExecuted()) {
            return getFile().getName() + " imported";
        }
        return "";
    }

    @Override
    protected Import createImport(Workspace workspace) {
        return new XMLImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines);
    }
    
    public void setEncoding(String encoding) {
    	this.encoding = encoding;
    }
    
    public void setMaxNumLines(int lines) {
    	this.maxNumLines = lines;
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
        
        String maxNumLines = request.getParameter("maxNumLines");
        if(maxNumLines != null && maxNumLines != "") {
        	try {
                int num = Integer.parseInt(maxNumLines);
                setMaxNumLines(num);
            } catch (Throwable t) {
                logger.error("Wrong user input for Data Number of Lines to import");
                return null;
            }
        }
        /**
         * Send response based on the interaction type *
         */
        UpdateContainer c = null;
        InteractionType type = InteractionType.valueOf(request
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
        c.add(new ImportPropertiesUpdate(getFile(), encoding, maxNumLines, id));
        return c;
	   
	}
}
