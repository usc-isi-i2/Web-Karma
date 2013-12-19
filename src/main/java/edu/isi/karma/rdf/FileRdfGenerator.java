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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.imp.Import;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLMapping;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.ExecutionController;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class FileRdfGenerator extends RdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(FileRdfGenerator.class);
	
    private static Worksheet generateWorksheetFromFile(File inputFile, String inputType,
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

	public static void generateRdf(String inputType, R2RMLMappingIdentifier id,
			 PrintWriter pw, File inputFile, String encoding, int maxNumLines)
			throws IOException, JSONException, KarmaException {
		logger.info("Generating worksheet from the data source ...");
		Workspace workspace = WorkspaceManager.getInstance().createWorkspace();
		WorkspaceRegistry.getInstance().register(new ExecutionController(workspace));
    		
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

		WorksheetCommandHistoryExecutor wchr = new WorksheetCommandHistoryExecutor(worksheet.getId(), workspace);
		try
		{
			List<CommandTag> tags = new ArrayList<CommandTag>();
			tags.add(CommandTag.Transformation);
			wchr.executeCommandsByTags(tags, mapping.getWorksheetHistory());
		}
		catch (CommandException | KarmaException e)
		{
			logger.error("Unable to execute column transformations", e);
		}
		// RDF generation object initialization
		KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(worksheet,
		        workspace.getFactory(), workspace.getOntologyManager(), pw,
		        mapping, errorReport, false);

		// Generate the rdf
		rdfGen.generateRDF(false);
		WorkspaceManager.getInstance().removeWorkspace(workspace.getId());
		WorkspaceRegistry.getInstance().deregister(workspace.getId());
		workspace = null;
	}
}
