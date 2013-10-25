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
package edu.isi.karma.rdf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class OfflineRdfGenerator {

    private static Logger logger = LoggerFactory.getLogger(OfflineRdfGenerator.class);

    public static void main(String[] args) {
    	
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
            /**
             * PARSE THE COMMAND LINE ARGUMENTS *
             */
            CommandLine cl = parser.parseAndHelp(args);
            if (cl == null || cl.getOptions().size() == 0 || cl.hasOption("--help")) {
                hf.setGroup(options);
                hf.print();
                return;
            }

            String inputType = (String) cl.getValue("--sourcetype");
            String modelFilePath = (String) cl.getValue("--modelfilepath");
            String outputFilePath = (String) cl.getValue("--outputfile");
            if (modelFilePath == null || outputFilePath == null || inputType == null) {
                logger.error("Mandatory value missing. Please provide argument value "
                        + "for sourcetype, modelfilepath and outputfile.");
                return;
            }

            /**
             * VALIDATE THE OPTIONS *
             */
            File modelFile = new File(modelFilePath);
            if (!modelFile.exists()) {
                logger.error("File not found: " + modelFile.getAbsolutePath());
                return;
            }
            if (!inputType.equalsIgnoreCase("DB")
                    && !inputType.equalsIgnoreCase("CSV")
                    && !inputType.equalsIgnoreCase("XML")
                    && !inputType.equalsIgnoreCase("JSON")) {
                logger.error("Invalid source type: " + inputType
                        + ". Please choose from: DB, CSV, XML, JSON.");
                return;
            }

            /**
             * CREATE THE REQUIRED KARMA OBJECTS *
             */
            ServletContextParameterMap.setParameterValue(
                    ContextParameter.USER_DIRECTORY_PATH, "src/main/webapp/");
            ServletContextParameterMap.setParameterValue(
                    ContextParameter.TRAINING_EXAMPLE_MAX_COUNT, "200");
       	
            /**
             * LOAD THE R2RML MODEL FILE INTO A JENA MODEL *
             */
           logger.info("Loading the R2RML model file...");
            Model model = loadSourceModelIntoJenaModel(modelFile);
            String worksheetName = getWorksheetTitleFromJenaModel(model);
            if (worksheetName == null) {
                throw new KarmaException("No source name found in the model.");
            }
            logger.info("done");

            /**
             * PREPATRE THE OUTPUT OBJECTS *
             */
            OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8");
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            /**
             * Generate RDF on the source type *
             */
            SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
            // Database table
            if (inputType.equals("DB")) {
                generateRdfFromDatabaseTable(cl, model, pw);
            } // File based worksheets such as JSON, XML, CSV
            else {
                generateRdfFromFile(cl, inputType, model, worksheetName, pw);
            }
            pw.close();
            logger.info("done");

            logger.info("RDF published at: " + outputFilePath);
        } catch (Exception e) {
            logger.error("Error occured while generating RDF!", e);
        }
    }

	private static void generateRdfFromDatabaseTable(CommandLine cl, Model model,
			PrintWriter pw) throws IOException, JSONException, KarmaException,
			SQLException, ClassNotFoundException {
		String dbtypeStr = (String) cl.getValue("--dbtype");
		String hostname = (String) cl.getValue("--hostname");
		String username = (String) cl.getValue("--username");
		String password = (String) cl.getValue("--password");
		int portnumber = 0;
		try {
		    portnumber = Integer.parseInt(cl.getValue("--portnumber").toString());
		} catch (Throwable t) {
		    logger.error("Error occured while parsing value for portnumber."
		            + " Provided value: " + cl.getValue("--portnumber"));
		    pw.close();
		    return;
		}
		String dBorSIDName = (String) cl.getValue("--dbname");
		String tablename = (String) cl.getValue("--tablename");

		// Validate the arguments
		if (dbtypeStr == null || dbtypeStr.equals("") || hostname == null
		        || hostname.equals("") || username == null || username.equals("")
		        || password == null || password.equals("") || dBorSIDName == null
		        || dBorSIDName.equals("") || tablename == null || tablename.equals("")
		        || tablename == null || tablename.equals("")) {
		    logger.error("A mandatory value is missing for fetching data from "
		            + "a database. Please provide argument values for dbtype, hostname, "
		            + "username, password, portnumber, dbname and tablename.");
		    pw.close();
		    return;
		}

		DBType dbType = DBType.valueOf(dbtypeStr);
		if (dbType == null) {
		    logger.error("Unidentified database type. Valid values: "
		            + "Oracle, MySQL, SQLServer, PostGIS");
		    pw.close();
		    return;
		}
		DatabaseTableRDFGenerator dbRdfGen = new DatabaseTableRDFGenerator(dbType,
		        hostname, portnumber, username, password, dBorSIDName, tablename);
		
		dbRdfGen.generateRDF(pw, model);
        pw.flush();
	}

	private static void generateRdfFromFile(CommandLine cl, String inputType,
			Model model, String worksheetName, PrintWriter pw)
			throws JSONException, IOException, KarmaException,
			ClassNotFoundException, SQLException {
		String sourceFilePath = (String) cl.getValue("--filepath");
		File inputFile = new File(sourceFilePath);
		if (!inputFile.exists()) {
		    logger.error("File not found: " + inputFile.getAbsolutePath());
		    pw.close();
		    return;
		}
		FileRdfGenerator.generateRdf(inputType, model, worksheetName, pw, inputFile);
        pw.flush();
	}


    private static Model loadSourceModelIntoJenaModel(File modelFile) throws FileNotFoundException {
        // Create an empty Model
        Model model = ModelFactory.createDefaultModel();
        InputStream s = new FileInputStream(modelFile);
        model.read(s, null, "TURTLE");
        return model;
    }

    private static String getWorksheetTitleFromJenaModel(Model model) throws KarmaException {
        Property sourceNameProp = model.getProperty(Uris.KM_SOURCE_NAME_URI);
        NodeIterator itr = model.listObjectsOfProperty(sourceNameProp);
        List<RDFNode> list = itr.toList();

        if (list.size() > 1) {
            throw new KarmaException("More than one resource exists with source name.");
        } else if (list.size() == 1) {
            return list.get(0).toString();
        } else {
            return null;
        }
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
