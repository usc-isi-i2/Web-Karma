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

package edu.isi.karma.rdf;

import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.kr2rml.*;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.KarmaException;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class FileRdfGenerator extends RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(FileRdfGenerator.class);
	
    private Worksheet generateWorksheetFromFile(File inputFile, String inputType,
            Workspace workspace, String encoding, int maxNumLines) throws JSONException, IOException, KarmaException, ClassNotFoundException {
        Worksheet worksheet = null;

        if (inputType.equalsIgnoreCase("JSON")) {
            FileReader reader = new FileReader(inputFile);
            Object json = JSONUtil.createJson(reader);
            JsonImport imp = new JsonImport(json, inputFile.getName(), workspace, encoding, maxNumLines);
            worksheet = imp.generateWorksheet();
        } else if (inputType.equalsIgnoreCase("XML")) {
            String fileContents = FileUtil.readFileContentsToString(inputFile, encoding);
            JSONObject json = XML.toJSONObject(fileContents);
            JsonImport imp = new JsonImport(json, inputFile.getName(), workspace, encoding, maxNumLines);
            worksheet = imp.generateWorksheet();
        } else if (inputType.equalsIgnoreCase("CSV")) {
            Import fileImport = new CSVFileImport(1, 2, ',', '\"', encoding, -1, inputFile, workspace);

            worksheet = fileImport.generateWorksheet();
        }

        return worksheet;
    }

	public void generateRdf(String inputType, R2RMLMappingIdentifier id,
			 PrintWriter pw, File inputFile, String encoding, int maxNumLines)
			throws IOException, JSONException, KarmaException {
		logger.info("Generating worksheet from the data source ...");
		Workspace workspace = initializeWorkspace();
    		
		Worksheet worksheet;
		try {
			worksheet = generateWorksheetFromFile(inputFile, inputType, workspace, encoding, maxNumLines);
		} catch (ClassNotFoundException e) {
			throw new KarmaException("Unable to generate worksheet from file : " + e.getMessage());
		}
		logger.info("done");
		/**
		 * GENERATE RDF FROM WORKSHEET OBJECT *
		 */
		logger.info("Generating RDF...");
		WorksheetR2RMLJenaModelParser parserTest = new WorksheetR2RMLJenaModelParser(id);
		KR2RMLMapping mapping = parserTest.parse();
		

		// Gets all the errors generated during the RDF generation
		ErrorReport errorReport = new ErrorReport();

		this.applyHistoryToWorksheet(workspace, worksheet, mapping);
		
		// RDF generation object initialization
		KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet,
		        workspace.getFactory(), workspace.getOntologyManager(), pw,
		        mapping, errorReport, false);

		// Generate the rdf
		rdfGen.generateRDF(false);
		this.removeWorkspace(workspace);
		workspace = null;
	}
}
