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
package edu.isi.karma.imp.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.json.JSONArray;

import edu.isi.karma.rep.Workspace;

public class CSVFileImport extends CSVImport {

	private String filename;
	
    public CSVFileImport(int headerRowIndex, int dataStartRowIndex,
            char delimiter, char quoteCharacter, String encoding,
            int maxNumLines,
            File csvFile,
            Workspace workspace, 
            JSONArray columnsJson) throws IOException {

    	super(headerRowIndex, dataStartRowIndex,delimiter, 
    			quoteCharacter,encoding, maxNumLines, 
    			csvFile.getName(), new FileInputStream(csvFile), 
    			workspace, columnsJson);
       filename = csvFile.getAbsolutePath();
    }

    public CSVFileImport duplicate() throws IOException {
    	return new CSVFileImport(headerRowIndex, dataStartRowIndex, delimiter, quoteCharacter, encoding, maxNumLines, 
    			new File(filename), workspace, columnsJson);
    }
}
