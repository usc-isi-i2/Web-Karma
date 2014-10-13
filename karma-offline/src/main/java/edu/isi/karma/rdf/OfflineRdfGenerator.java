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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.kr2rml.writer.BloomFilterKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.JSONKR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.KR2RMLRDFWriter;
import edu.isi.karma.kr2rml.writer.N3KR2RMLRDFWriter;
import edu.isi.karma.metadata.KarmaMetadataManager;
import edu.isi.karma.metadata.PythonTransformationMetadata;
import edu.isi.karma.metadata.UserConfigMetadata;
import edu.isi.karma.metadata.UserPreferencesMetadata;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Uris;
import edu.isi.karma.modeling.semantictypes.SemanticTypeUtil;
import edu.isi.karma.rdf.GenericRDFGenerator.InputType;
import edu.isi.karma.util.DBType;
import edu.isi.karma.util.EncodingDetector;
import edu.isi.karma.webserver.KarmaException;

public class OfflineRdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(OfflineRdfGenerator.class);
	private String inputType;
	private String modelFilePath;
	private String modelURLString;
	private String baseURI;
	private String outputFilePath;
	private String outputFileJSONPath;
	private String bloomFiltersFilePath;
	private List<KR2RMLRDFWriter> writers;
	private URL modelURL;
	private String dbtypeStr;
	private String username;
	private String password;
	private String hostname;
	private String encoding;
	private String sourceFilePath;
	private String dBorSIDName;
	private String tablename;
	private String queryFile;
	private String portnumber;
	private String sMaxNumLines;
	private String sourceName;
	private String selectionName;
	private int port;
	private DBType dbType;
	private File inputFile;
	private int maxNumLines;
	private String rootTripleMap;
	private List<String> killTripleMap;
	private List<String> stopTripleMap;
	private List<String> POMToKill;
	private String contextFile;
	public OfflineRdfGenerator(CommandLine cl)
	{

		this.writers = new LinkedList<KR2RMLRDFWriter>();
		parseCommandLineOptions(cl);	
	}

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

			OfflineRdfGenerator generator = new OfflineRdfGenerator(cl);

			generator.generate();


		} catch (Exception e) {
			logger.error("Error occured while generating RDF!", e);
		}
	}

	private void generate() throws Exception {
		if (validateCommandLineOptions()) {
			createModelURL();
			setupKarmaMetadata();
			generateRDF();
			closeWriters();
		}
	}

	private void generateRDF() throws Exception
	{
		/**
		 * Generate RDF on the source type *
		 */
		long l = System.currentTimeMillis();

		// Database table
		if (inputType.equals("DB") || inputType.equals("SQL")) {
			generateRdfFromDatabaseTable();
		} // File based worksheets such as JSON, XML, CSV
		else {
			generateRdfFromFile();
		}

		logger.info("done after {}", (System.currentTimeMillis() - l));
		logger.info("RDF published at: " + outputFilePath);
	}

	private void setupKarmaMetadata() throws KarmaException {
		UpdateContainer uc = new UpdateContainer();
		KarmaMetadataManager userMetadataManager = new KarmaMetadataManager();
		userMetadataManager.register(new UserPreferencesMetadata(), uc);
		userMetadataManager.register(new UserConfigMetadata(), uc);
		userMetadataManager.register(new PythonTransformationMetadata(), uc);
		PythonRepository.disableReloadingLibrary();

		SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
		ModelingConfiguration.setLearnerEnabled(false); // disable automatic learning

	}


	protected void parseCommandLineOptions(CommandLine cl) {
		inputType = (String) cl.getValue("--sourcetype");
		modelFilePath = (String) cl.getValue("--modelfilepath");
		modelURLString = (String) cl.getValue("--modelurl");
		outputFilePath = (String) cl.getValue("--outputfile");
		outputFileJSONPath = (String) cl.getValue("--jsonoutputfile");
		baseURI = (String) cl.getValue("--baseuri");
		bloomFiltersFilePath = (String) cl.getValue("--outputbloomfilter");
		selectionName = (String) cl.getValue("--selection");
		rootTripleMap = (String) cl.getValue("--root");
		String killTripleMap = (String) cl.getValue("--killtriplemap");
		String stopTripleMap = (String) cl.getValue("--stoptriplemap");
		String POMToKill = (String) cl.getValue("--pomtokill");
		contextFile = (String)cl.getValue("--contextfile");
		if (rootTripleMap == null) {
			rootTripleMap = "";
		}
		else {
			rootTripleMap = Namespaces.KARMA_DEV + rootTripleMap;
		}
		if (killTripleMap == null) {
			this.killTripleMap = new ArrayList<String>();
		}
		else {
			this.killTripleMap = new ArrayList<String>(Arrays.asList(killTripleMap.split(",")));
			int size = this.killTripleMap.size();
			for (int i = 0; i < size; i++) {
				String t = this.killTripleMap.remove(0);
				this.killTripleMap.add(Namespaces.KARMA_DEV + t);
			}
		}
		if (stopTripleMap == null) {
			this.stopTripleMap = new ArrayList<String>();
		}
		else {
			this.stopTripleMap = new ArrayList<String>(Arrays.asList(stopTripleMap.split(",")));
			int size = this.stopTripleMap.size();
			for (int i = 0; i < size; i++) {
				String t = this.stopTripleMap.remove(0);
				this.stopTripleMap.add(Namespaces.KARMA_DEV + t);
			}
		}
		if (POMToKill == null) {
			this.POMToKill = new ArrayList<String>();
		}
		else {
			this.POMToKill = new ArrayList<String>(Arrays.asList(POMToKill.split(",")));
			int size = this.POMToKill.size();
			for (int i = 0; i < size; i++) {
				String t = this.POMToKill.remove(0);
				this.POMToKill.add(Namespaces.KARMA_DEV + t);
			}
		}
		parseDatabaseCommandLineOptions(cl);
		parseFileCommandLineOptions(cl);

	}

	protected void parseDatabaseCommandLineOptions(CommandLine cl)
	{

		dbtypeStr = (String) cl.getValue("--dbtype");
		hostname = (String) cl.getValue("--hostname");
		username = (String) cl.getValue("--username");
		password = (String) cl.getValue("--password");
		encoding = (String) cl.getValue("--encoding");
		dBorSIDName = (String) cl.getValue("--dbname");
		tablename = (String) cl.getValue("--tablename");
		queryFile = (String) cl.getValue("--queryfile");
		portnumber = (String) cl.getValue("--portnumber");
	}

	protected void parseFileCommandLineOptions(CommandLine cl)
	{

		sourceFilePath = (String) cl.getValue("--filepath");
		sMaxNumLines = (String) cl.getValue("--maxNumLines");
		sourceName = (String) cl.getValue("--sourcename");
	}
	protected boolean validateCommandLineOptions() throws IOException
	{

		if ((modelURLString == null && modelFilePath == null) || outputFilePath == null || inputType == null) {
			logger.error("Mandatory value missing. Please provide argument value "
					+ "for sourcetype, modelfilepath and outputfile.");
			return false;
		}

		if (!inputType.equalsIgnoreCase("DB")
				&& !inputType.equalsIgnoreCase("CSV")
				&& !inputType.equalsIgnoreCase("XML")
				&& !inputType.equalsIgnoreCase("JSON")
				&& !inputType.equalsIgnoreCase("SQL")
				) {
			logger.error("Invalid source type: " + inputType
					+ ". Please choose from: DB, SQL, CSV, XML, JSON.");
			return false;
		}
		return true;
	}


	private boolean validateFileCommandLineOptions() {
		inputFile = new File(sourceFilePath);
		if (!inputFile.exists()) {
			logger.error("File not found: " + inputFile.getAbsolutePath());
			return false;
		}
		if(encoding == null) {
			encoding = EncodingDetector.detect(inputFile);
		}

		maxNumLines = -1;
		if(sMaxNumLines != null) {
			maxNumLines = Integer.parseInt(sMaxNumLines);
		}

		if(sourceName == null)
		{
			logger.error("You need to supply a value for '--sourcename'");
			return false;
		}
		return true;
	}

	private void createModelURL() throws IOException {
		/**
		 * VALIDATE THE OPTIONS *
		 */
		if(modelFilePath != null)
		{
			File modelFile = new File(modelFilePath);
			if (!modelFile.exists()) {
				throw new IOException("File not found: " + modelFile.getAbsolutePath());
			}
			modelURL = modelFile.toURI().toURL();
		}
		else
		{
			modelURL = new URL(modelURLString);
		}
		if (contextFile != null) {
			File tmp = new File(contextFile);
			if (!tmp.exists()) {
				throw new IOException("File not found: " + tmp.getAbsolutePath());
			}
		}
		if (baseURI != null && !baseURI.trim().isEmpty())
			return;
		try {
			Model model = WorksheetR2RMLJenaModelParser.loadSourceModelIntoJenaModel(modelURL);
			Property rdfTypeProp = model.getProperty(Uris.RDF_TYPE_URI);
			Property baseURIProp = model.getProperty(Uris.KM_HAS_BASEURI);
			RDFNode node = model.getResource(Uris.KM_R2RML_MAPPING_URI);
			ResIterator res = model.listResourcesWithProperty(rdfTypeProp, node);
			List<Resource> resList = res.toList();
			for(Resource r: resList)
			{
				if (r.hasProperty(baseURIProp)) {
					baseURI = r.getProperty(baseURIProp).asTriple().getObject().toString();
					baseURI = baseURI.replace("\"", "");
				}
			}
		} catch (IOException e) {

		}
	}

	private void generateRdfFromDatabaseTable() throws IOException, JSONException, KarmaException,
	SQLException, ClassNotFoundException {
		if(!validateDatabaseCommandLineOptions())
		{
			logger.error("Unable to generate RDF from database table!");
			return;
		}

		DatabaseTableRDFGenerator dbRdfGen = new DatabaseTableRDFGenerator(dbType,
				hostname, port, username, password, dBorSIDName, encoding, selectionName);
		ContextIdentifier contextId = null;
		if (contextFile != null) {
			File tmp = new File(contextFile);
			contextId = new ContextIdentifier(tmp.getName(), tmp.toURI().toURL());
		}
		if(inputType.equals("DB")) {
			R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(tablename, modelURL);
			createWriters();
			dbRdfGen.generateRDFFromTable(tablename, writers, id, contextId, baseURI);
		} else {
			String query = loadQueryFromFile();
			R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(modelURL.toString(), modelURL);
			createWriters();
			dbRdfGen.generateRDFFromSQL(query, writers, id, contextId, baseURI);
		}


	}

	private boolean validateDatabaseCommandLineOptions() {
		if(encoding == null)
			encoding = "UTF-8";
		port = 0;
		try {
			port = Integer.parseInt(portnumber);
		} catch (Throwable t) {
			logger.error("Error occured while parsing value for portnumber."
					+ " Provided value: " + portnumber);
			return false;
		}

		// Validate the arguments
		if (dbtypeStr == null || dbtypeStr.equals("") || hostname == null
				|| hostname.equals("") || username == null || username.equals("")
				|| password == null || password.equals("") || dBorSIDName == null
				|| dBorSIDName.equals("") 
				|| (inputType.equals("DB") && (tablename == null || tablename.equals("")))
				|| (inputType.equals("SQL") && (queryFile == null || queryFile.equals("")))
				) {
			if(inputType.equals("DB"))
				logger.error("A mandatory value is missing for fetching data from "
						+ "a database. Please provide argument values for dbtype, hostname, "
						+ "username, password, portnumber, dbname and tablename.");
			else
				logger.error("A mandatory value is missing for fetching data from "
						+ "a database. Please provide argument values for dbtype, hostname, "
						+ "username, password, portnumber, dbname and queryfile.");
			return false;
		}

		dbType = DBType.valueOf(dbtypeStr);
		if (dbType == null) {
			logger.error("Unidentified database type. Valid values: "
					+ "Oracle, MySQL, SQLServer, PostGIS");
			return false;
		}
		return true;
	}

	private String loadQueryFromFile() throws IOException {
		File file = new File(queryFile);
		String queryFileEncoding = EncodingDetector.detect(file);
		String query = EncodingDetector.getString(file, queryFileEncoding);
		return query;
	}

	protected void closeWriters() {
		for(KR2RMLRDFWriter writer : writers)
		{
			writer.flush();
			writer.close();
		}
	}

	protected void createWriters() throws IOException
	{
		createN3Writer();
		createBloomFilterWriter();
	}
	protected void createN3Writer()
			throws UnsupportedEncodingException, FileNotFoundException {

		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8");
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter pw = new PrintWriter(bw);
		N3KR2RMLRDFWriter n3Writer = new N3KR2RMLRDFWriter(new URIFormatter(), pw);
		if (outputFileJSONPath != null) {
			JSONKR2RMLRDFWriter jsonWriter = new JSONKR2RMLRDFWriter(new PrintWriter(outputFileJSONPath), baseURI);
			writers.add(jsonWriter);
		}
		if(baseURI != null)
		{
			n3Writer.setBaseURI(baseURI);
		}
		writers.add(n3Writer);
	}

	protected void createBloomFilterWriter() throws FileNotFoundException {
		if (bloomFiltersFilePath != null && !bloomFiltersFilePath.trim().isEmpty()) {
			PrintWriter bloomfilterpw = new PrintWriter(new File(bloomFiltersFilePath));
			logger.info(bloomFiltersFilePath);
			BloomFilterKR2RMLRDFWriter bloomfilter = null;
			if (bloomfilterpw != null)
			{
				bloomfilter = new BloomFilterKR2RMLRDFWriter(bloomfilterpw, true, baseURI);
				writers.add(bloomfilter);
			}
		}

	}

	private void generateRdfFromFile()
			throws JSONException, IOException, KarmaException,
			ClassNotFoundException, SQLException {
		if(!validateFileCommandLineOptions())
		{
			logger.error("Unable to generate RDF from file because of invalid configuration");
			return;
		}
		R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(sourceName, modelURL);

		createWriters();
		GenericRDFGenerator rdfGenerator = new GenericRDFGenerator(selectionName, killTripleMap, stopTripleMap, POMToKill, rootTripleMap);
		rdfGenerator.addModel(id);

		InputType inputType = null;
		if(this.inputType.equalsIgnoreCase("CSV"))
			inputType = InputType.CSV;
		else if(this.inputType.equalsIgnoreCase("JSON"))
			inputType = InputType.JSON;
		else if(this.inputType.equalsIgnoreCase("XML"))
			inputType = InputType.XML;
		RDFGeneratorRequest request = new RDFGeneratorRequest(sourceName, inputFile.getName());
		request.setInputFile(inputFile);
		request.setDataType(inputType);
		request.setMaxNumLines(maxNumLines);
		request.setAddProvenance(false);
		request.addWriters(writers);
		if (contextFile != null) {
			File tmp = new File(contextFile);
			ContextIdentifier contextId = new ContextIdentifier(tmp.getName(), tmp.toURI().toURL());
			rdfGenerator.addContext(contextId);
			request.setContextName(tmp.getName());
		}
		rdfGenerator.generateRDF(request);
	}


	private static Group createCommandLineOptions() {
		DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
		ArgumentBuilder abuilder = new ArgumentBuilder();
		GroupBuilder gbuilder = new GroupBuilder();

		Group options =
				gbuilder
				.withName("options")
				.withOption(buildOption("sourcetype", "type of source. Valid values: DB, SQL, CSV, JSON, XML", "sourcetype", obuilder, abuilder))
				.withOption(buildOption("filepath", "location of the input file", "filepath", obuilder, abuilder))
				.withOption(buildOption("modelfilepath", "location of the model file", "modelfilepath", obuilder, abuilder))
				.withOption(buildOption("modelurl", "location of the model", "modelurl", obuilder, abuilder))
				.withOption(buildOption("sourcename", "name of the source in the model to use", "sourcename", obuilder, abuilder))
				.withOption(buildOption("outputfile", "location of the output file", "outputfile", obuilder, abuilder))
				.withOption(buildOption("dbtype", "database type. Valid values: Oracle, MySQL, SQLServer, PostGIS", "dbtype", obuilder, abuilder))
				.withOption(buildOption("hostname", "hostname for database connection", "hostname", obuilder, abuilder))
				.withOption(buildOption("username", "username for database connection", "username", obuilder, abuilder))
				.withOption(buildOption("password", "password for database connection", "password", obuilder, abuilder))
				.withOption(buildOption("portnumber", "portnumber for database connection", "portnumber", obuilder, abuilder))
				.withOption(buildOption("dbname", "database or SID name for database connection", "dbname", obuilder, abuilder))
				.withOption(buildOption("tablename", "hostname for database connection", "tablename", obuilder, abuilder))
				.withOption(buildOption("queryfile", "query file for loading data", "queryfile", obuilder, abuilder))
				.withOption(buildOption("outputbloomfilter", "generate bloom filters", "bloomfiltersfile", obuilder, abuilder))
				.withOption(buildOption("baseuri", "specifies base uri", "base URI", obuilder, abuilder))
				.withOption(buildOption("selection", "specifies selection name", "selection", obuilder, abuilder))
				.withOption(buildOption("root", "specifies root", "root", obuilder, abuilder))
				.withOption(buildOption("killtriplemap", "specifies TripleMap to kill", "killtriplemap", obuilder, abuilder))
				.withOption(buildOption("stoptriplemap", "specifies TripleMap to stop", "stoptriplemap", obuilder, abuilder))
				.withOption(buildOption("pomtokill", "specifies POM to kill", "pomtokill", obuilder, abuilder))
				.withOption(buildOption("jsonoutputfile", "specifies JSONOutputFile", "jsonoutputfile", obuilder, abuilder))
				.withOption(buildOption("contextfile", "specifies global context file", "contextile", obuilder, abuilder))
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
