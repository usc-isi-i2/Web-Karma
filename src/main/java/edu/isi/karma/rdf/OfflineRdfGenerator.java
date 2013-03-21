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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.commons.cli2.util.HelpFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.imp.database.DatabaseTableImport;
import edu.isi.karma.imp.json.JsonImport;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.webserver.KarmaException;

public class OfflineRdfGenerator {
	private static Logger logger = LoggerFactory.getLogger(OfflineRdfGenerator.class);
	
	public static void main(String []args) {
		Group options = createCommandLineOptions();
		Parser parser = new Parser();
		parser.setGroup(options);
		
		// configure a HelpFormatter
		HelpFormatter hf = new HelpFormatter();

		// configure a parser
		Parser p = new Parser();
		p.setGroup(options);
		p.setHelpFormatter(hf);
		p.setHelpTrigger("--help");
		
		try {
			/** PARSE THE COMMAND LINE ARGUMENTS **/
			CommandLine cl = parser.parseAndHelp(args);
			if (cl == null || cl.getOptions().size() == 0 || cl.hasOption("--help")) {
				hf.setGroup(options);
				hf.print();
				return;
			}
				
			String inputType = (String) cl.getValue("--sourcetype");
			String modelFilePath = (String) cl.getValue("--modelfilepath");
			String outputFilePath = (String) cl.getValue("--outputfile");
			if (modelFilePath == null || outputFilePath == null || inputType==null) {
				System.out.println("Mandatory value missing. Please provide argument value for sourcetype, modelfilepath and outputfile.");
				return;
			}
			
			/** VALIDATE THE OPTIONS **/
			File modelFile = new File(modelFilePath);
			if (!modelFile.exists()) {
				System.out.println("File not found: " + modelFile.getAbsolutePath());
				return;
			}
			if (!inputType.equalsIgnoreCase("DB") && !inputType.equalsIgnoreCase("CSV") && !inputType.equalsIgnoreCase("XML") && !inputType.equalsIgnoreCase("JSON")) {
				System.out.println("Invalid source type: " + inputType + ". Please choose from: DB, CSV, XML, JSON.");
				return;
			}
			
			/** CREATE THE REQUIRED KARMA OBJECTS **/
		    Workspace workspace = WorkspaceManager._getNewInstance().createWorkspace();
		    RepFactory factory = workspace.getFactory();
		    Worksheet worksheet = null;
		    
			// Create the Worksheet depending on the source type
			if (inputType.equals("DB")) {
				String dbtypeStr = (String) cl.getValue("--dbtype");
				String hostname = (String) cl.getValue("--hostname");
				String username = (String) cl.getValue("--username");
				String password = (String) cl.getValue("--password");
				int portnumber = 0;
				try {
					portnumber = Integer.parseInt(cl.getValue("--portnumber").toString());
				} catch (Throwable t) {
					System.out.println("Error occured while parsing value for portnumber. Provided value: " + cl.getValue("--portnumber"));
					return;
				}
				String dBorSIDName = (String) cl.getValue("--dbname");
				String tablename = (String) cl.getValue("--tablename");
				
				// Validate the arguments
				if (dbtypeStr == null || dbtypeStr.equals("") || hostname == null || hostname.equals("") ||
						username == null || username.equals("") || password == null || password.equals("") ||
						dBorSIDName == null || dBorSIDName.equals("") || tablename == null || tablename.equals("") ||
						tablename == null || tablename.equals("")) {
					System.out.println("A mandatory value is missing for fetching data from a database. Please provide" +
							" argument values for dbtype, hostname, username, password, portnumber, dbname and tablename.");
					return;
				}
				
				DBType dbType = DBType.valueOf(dbtypeStr);
				if (dbType == null) {
					System.out.println("Unidentified database type. Valid values: Oracle, MySQL, SQLServer, PostGIS");
					return;
				}
				System.out.print("Generating worksheet from the data source ...");
				worksheet = generateWorksheetFromDBTable(dbType, hostname, portnumber, username, password, dBorSIDName, tablename, factory, workspace);
			} else {
				String sourceFilePath = (String) cl.getValue("--filepath");
				File inputFile = new File(sourceFilePath);
				if (!inputFile.exists()) {
					System.out.println("File not found: " + inputFile.getAbsolutePath());
					return;
				}
				System.out.print("Generating worksheet from the data source ...");
				worksheet = generateWorksheetFromFile(inputFile, inputType, factory, workspace);
			}
			// SANITY CHECK
			if (worksheet == null) {
				System.out.println("Error occured while creating the worksheet object.");
				return;
			}
			System.out.println("done");
			
			/** GET THE SOURCE DESCRIPTION FROM THE MODEL FILE **/
			System.out.print("Extracting source description from the model file...");
		    String sourceDescription = getSourceDescription(modelFile);
		    System.out.println("done");
			
			/** GENERATE RDF FROM WORKSHEET OBJECT **/
		    System.out.print("Generating RDF...");
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			WorksheetRDFGenerator wrg = new WorksheetRDFGenerator(factory, sourceDescription, pw);
			if (worksheet.getHeaders().hasNestedTables()) {
				wrg.generateTriplesCell(worksheet,true);
			} else {
				wrg.generateTriplesRow(worksheet, true);
			}
			
			pw.close();
			System.out.println("done");
			
			System.out.println("RDF published at: " + outputFilePath);
		} catch (Exception e) {
			logger.error("Error occured while generating RDF!", e);
		}
	}

	private static Worksheet generateWorksheetFromDBTable(DBType dbType, String hostname,
			int portnumber, String username, String password, String dBorSIDName, String tableName, 
			RepFactory factory, Workspace workspace) throws SQLException, ClassNotFoundException {
		DatabaseTableImport  dbTableImport = new DatabaseTableImport(dbType, hostname, portnumber, username, 
				password, dBorSIDName, tableName, workspace);
		return dbTableImport.generateWorksheetForAllRows();
	}

	private static Worksheet generateWorksheetFromFile(File inputFile, String inputType,
			RepFactory factory, Workspace workspace) throws JSONException, IOException, KarmaException {
		Worksheet worksheet = null;
			
		if (inputType.equalsIgnoreCase("JSON")) {
			FileReader reader = new FileReader(inputFile);
			Object json = JSONUtil.createJson(reader);
			JsonImport imp = new JsonImport(json, inputFile.getName(), workspace);
			worksheet = imp.generateWorksheet();
		} 
		
		else if (inputType.equalsIgnoreCase("XML")) {
			String fileContents = FileUtil.readFileContentsToString(inputFile);
			JSONObject json = XML.toJSONObject(fileContents);
			JsonImport imp = new JsonImport(json, inputFile.getName(), workspace);
			worksheet = imp.generateWorksheet();
		} 
		
		else if (inputType.equalsIgnoreCase("CSV")) {
			CSVFileImport fileImport = new CSVFileImport(1, 2, ',', '\"', inputFile, factory, workspace);
			worksheet = fileImport.generateWorksheet();
		}
			
		return worksheet;
	}

	private static String getSourceDescription(File modelFile) throws IOException, KarmaException {
		Model model = ModelFactory.createDefaultModel();
		InputStream s = new FileInputStream(modelFile);
		model.read(s, null, "N3");
		s.close();

		// Get the alignment rules
		Property hasSourceDesc = model.createProperty(Namespaces.KARMA, "hasSourceDescription");
		ResIterator itr = model.listResourcesWithProperty(hasSourceDesc);
		List<Resource> sourceList = itr.toList();
		if (sourceList.size() == 0)
		    throw new KarmaException("No source found in the model file.");
		Resource resource = sourceList.get(0);
		Statement stmt = model.getProperty(resource, hasSourceDesc);
		return stmt.getObject().toString();
	}

	private static Group createCommandLineOptions() {
		DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		ArgumentBuilder abuilder = new ArgumentBuilder();
		GroupBuilder gbuilder = new GroupBuilder();
		
		Group options =
			    gbuilder
			        .withName("options")
			        .withOption(buildOption("sourcetype", "type of source. Valid values: DB, CSV, JSON, XML", "sourcetype", obuilder, abuilder))
			        .withOption(buildOption("filepath", "location of the input file", "filepath", obuilder, abuilder))
			        .withOption(buildOption("modelfilepath", "location of the model file", "modelfilepath", obuilder, abuilder))
			        .withOption(buildOption("outputfile", "location of the output file", "outputfile", obuilder, abuilder))
			        .withOption(buildOption("dbtype", "database type. Valid values: Oracle, MySQL, SQLServer, PostGIS", "dbtype", obuilder, abuilder))
			        .withOption(buildOption("hostname", "hostname for database connection", "hostname", obuilder, abuilder))
			        .withOption(buildOption("username", "username for database connection", "username", obuilder, abuilder))
			        .withOption(buildOption("password", "password for database connection", "password", obuilder, abuilder))
			        .withOption(buildOption("portnumber", "portnumber for database connection", "portnumber", obuilder, abuilder))
			        .withOption(buildOption("dbname", "database or SID name for database connection", "dbname", obuilder, abuilder))
			        .withOption(buildOption("tablename", "hostname for database connection", "tablename", obuilder, abuilder))
			        .withOption(obuilder
				        .withLongName("help")
				        .withDescription("print this message")
				        .create())
			        .create();
		
		return options;
	}
	
	public static Option buildOption(String shortName, String description, String argumentName,
			DefaultOptionBuilder obuilder, ArgumentBuilder abuilder) {
		return obuilder
			        .withLongName(shortName)
			        .withDescription(description)
			        .withArgument(
			            abuilder
			                .withName(argumentName)
			                .withMinimum(1)
			                .withMaximum(1)
			                .create())
			        .create();
	}
}
