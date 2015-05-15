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

import edu.isi.karma.controller.command.IPreviewable;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.avro.AvroImport;
import edu.isi.karma.rep.Workspace;

public class ImportAvroFileCommand extends ImportFileCommand implements IPreviewable {
	
    public ImportAvroFileCommand(String id, String model, File file) {
        super(id, model, file);
    }

    public ImportAvroFileCommand(String id, String model, String revisedId, File file) {
        super(id, model, revisedId, file);
    }

    @Override
    public String getTitle() {
        return "Import JSON File";
    }
    
    @Override
    protected Import createImport(Workspace workspace) {
        return new AvroImport(getFile(), getFile().getName(), workspace, encoding, maxNumLines);
    }
    
    @Override
    protected Import createImport(Workspace workspace, int sampleSize) {
        return new AvroImport(getFile(), getFile().getName(), workspace, encoding, sampleSize);
    }
    
}
