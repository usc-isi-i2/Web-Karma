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
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.er.helper.PythonRepository;
import edu.isi.karma.er.helper.PythonRepositoryRegistry;
import edu.isi.karma.kr2rml.ContextIdentifier;
import edu.isi.karma.kr2rml.URIFormatter;
import edu.isi.karma.kr2rml.mapping.R2RMLMappingIdentifier;
import edu.isi.karma.kr2rml.mapping.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.kr2rml.planning.UserSpecifiedRootStrategy;
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
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.KarmaException;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class OfflineRdfGenerator {

	private static Logger logger = LoggerFactory.getLogger(OfflineRdfGenerator.class);
	
	private String inputType;
	private String inputEncoding;
	private String inputDelimiter;
	private String inputTextQualifier;
	private String inputHeaderStartIndex;
	private String inputDataStartIndex;
	
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
	private String topkrows;
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
	private String contextURLString;
	private URL contextURL;
	private ServletContextParameterMap contextParameters;
	public OfflineRdfGenerator(CommandLine cl)
	{

		this.writers = new LinkedList<>();
		parseCommandLineOptions(cl);	
	}

	public static void main(String[] args) {

		Options options = createCommandLineOptions();
		CommandLine cl = CommandLineArgumentParser.parse(args, options, OfflineRdfGenerator.class.getSimpleName());
		if(cl == null)
		{
			return;
		}

		try {
			OfflineRdfGenerator generator = new OfflineRdfGenerator(cl);

			long start = System.currentTimeMillis();
			generator.generate();
			long end = System.currentTimeMillis();
			
			logger.info("Time to generate RDF:" + (float) (end-start) /(1000*60) + " mins");

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

		logger.info("done after {}", System.currentTimeMillis() - l);
		if(outputFilePath != null)
		{
			logger.info("RDF published at: " + outputFilePath);
		}
		if(outputFileJSONPath != null)
		{
			logger.info("JSON-LD published at: " + outputFileJSONPath);
		}
	}

	private void setupKarmaMetadata() throws KarmaException {
		
		ContextParametersRegistry contextParametersRegistry = ContextParametersRegistry.getInstance();
		contextParameters = contextParametersRegistry.registerByKarmaHome(null);
		
		UpdateContainer uc = new UpdateContainer();
		KarmaMetadataManager userMetadataManager = new KarmaMetadataManager(contextParameters);
		userMetadataManager.register(new UserPreferencesMetadata(contextParameters), uc);
		userMetadataManager.register(new UserConfigMetadata(contextParameters), uc);
		userMetadataManager.register(new PythonTransformationMetadata(contextParameters), uc);
		PythonRepository pythonRepository = new PythonRepository(false, contextParameters.getParameterValue(ContextParameter.USER_PYTHON_SCRIPTS_DIRECTORY));
		PythonRepositoryRegistry.getInstance().register(pythonRepository);

		SemanticTypeUtil.setSemanticTypeTrainingStatus(false);
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().register(contextParameters.getId());
		modelingConfiguration.setLearnerEnabled(false); // disable automatic learning

	}


	protected void parseCommandLineOptions(CommandLine cl) {
		inputType = (String) cl.getOptionValue("sourcetype");
		inputEncoding = (String) cl.getOptionValue("encoding");
		inputDelimiter  = (String) cl.getOptionValue("delimiter");
		if(inputDelimiter != null) {
			if(inputDelimiter.equalsIgnoreCase("tab"))
				inputDelimiter = "\t";
			else if(inputDelimiter.equalsIgnoreCase("space"))
				inputDelimiter = " ";
		}
		inputTextQualifier = (String) cl.getOptionValue("textqualifier");
		inputHeaderStartIndex = (String) cl.getOptionValue("headerindex");
		inputDataStartIndex = (String) cl.getOptionValue("dataindex");
		
		modelFilePath = (String) cl.getOptionValue("modelfilepath");
		modelURLString = (String) cl.getOptionValue("modelurl");
		outputFilePath = (String) cl.getOptionValue("outputfile");
		outputFileJSONPath = (String) cl.getOptionValue("jsonoutputfile");
		baseURI = (String) cl.getOptionValue("baseuri");
		bloomFiltersFilePath = (String) cl.getOptionValue("outputbloomfilter");
		selectionName = (String) cl.getOptionValue("selection");
		rootTripleMap = (String) cl.getOptionValue("root");
		String killTripleMap = (String) cl.getOptionValue("killtriplemap");
		String stopTripleMap = (String) cl.getOptionValue("stoptriplemap");
		String POMToKill = (String) cl.getOptionValue("pomtokill");
		contextFile = (String)cl.getOptionValue("contextfile");
		contextURLString = (String)cl.getOptionValue("contexturl");
		if (rootTripleMap == null) {
			rootTripleMap = "";
		}
		if (killTripleMap == null) {
			this.killTripleMap = new ArrayList<>();
		}
		else {
			this.killTripleMap = new ArrayList<>(Arrays.asList(killTripleMap.split(",")));
			int size = this.killTripleMap.size();
			for (int i = 0; i < size; i++) {
				String t = this.killTripleMap.remove(0);
				this.killTripleMap.add(Namespaces.KARMA_DEV + t);
			}
		}
		if (stopTripleMap == null) {
			this.stopTripleMap = new ArrayList<>();
		}
		else {
			this.stopTripleMap = new ArrayList<>(Arrays.asList(stopTripleMap.split(",")));
			int size = this.stopTripleMap.size();
			for (int i = 0; i < size; i++) {
				String t = this.stopTripleMap.remove(0);
				this.stopTripleMap.add(Namespaces.KARMA_DEV + t);
			}
		}
		if (POMToKill == null) {
			this.POMToKill = new ArrayList<>();
		}
		else {
			this.POMToKill = new ArrayList<>(Arrays.asList(POMToKill.split(",")));
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

		dbtypeStr = (String) cl.getOptionValue("dbtype");
		hostname = (String) cl.getOptionValue("hostname");
		username = (String) cl.getOptionValue("username");
		password = (String) cl.getOptionValue("password");
		encoding = (String) cl.getOptionValue("encoding");
		dBorSIDName = (String) cl.getOptionValue("dbname");
		tablename = (String) cl.getOptionValue("tablename");
		topkrows = (String) cl.getOptionValue("topkrows");
		queryFile = (String) cl.getOptionValue("queryfile");
		portnumber = (String) cl.getOptionValue("portnumber");
	}

	protected void parseFileCommandLineOptions(CommandLine cl)
	{

		sourceFilePath = (String) cl.getOptionValue("filepath");
		sMaxNumLines = (String) cl.getOptionValue("maxNumLines");
		sourceName = (String) cl.getOptionValue("sourcename");
	}
	protected boolean validateCommandLineOptions() throws IOException
	{

		if ((modelURLString == null && modelFilePath == null) || (outputFilePath == null && outputFileJSONPath == null) || inputType == null) {
			logger.error("Mandatory value missing. Please provide argument value "
					+ "for sourcetype, (modelfilepath or modelurl) and (outputfile or jsonoutputfile).");
			return false;
		}

		if (!inputType.equalsIgnoreCase("DB")
				&& !inputType.equalsIgnoreCase("CSV")
				&& !inputType.equalsIgnoreCase("XML")
				&& !inputType.equalsIgnoreCase("JSON")
				&& !inputType.equalsIgnoreCase("SQL")
				&& !inputType.equalsIgnoreCase("AVRO")
				&& !inputType.equalsIgnoreCase("JL")
				) {
			logger.error("Invalid source type: " + inputType
					+ ". Please choose from: DB, SQL, CSV, XML, JSON, AVRO, JL.");
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
			contextURL = tmp.toURI().toURL();
		}
		else if(contextURLString != null)
		{
			contextURL = new URL(contextURLString);
		}
		if (baseURI != null && !baseURI.trim().isEmpty())
			return;
		try {
			R2RMLMappingIdentifier modelIdentifier = new R2RMLMappingIdentifier(modelURL.toString(), modelURL, null);
			Model model = WorksheetR2RMLJenaModelParser.loadSourceModelIntoJenaModel(modelIdentifier);
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

	private void generateRdfFromDatabaseTable() throws Exception {
		if(!validateDatabaseCommandLineOptions())
		{
			logger.error("Unable to generate RDF from database table!");
			return;
		}

		DatabaseTableRDFGenerator dbRdfGen = new DatabaseTableRDFGenerator(dbType,
				hostname, port, username, password, dBorSIDName, encoding, selectionName, contextParameters);
		ContextIdentifier contextId = null;
		if (contextURL != null) {
			
			contextId = new ContextIdentifier(contextURL.getQuery(), contextURL, null);
		}
		if(inputType.equals("DB")) {
			R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(tablename, modelURL, null);
			createWriters();
			dbRdfGen.generateRDFFromTable(tablename, topkrows, writers, id, contextId, baseURI);
		} else {
			String query = loadQueryFromFile();
			R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(modelURL.toString(), modelURL, null);
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

	protected void createWriters() throws Exception
	{
		createN3Writer();
		createBloomFilterWriter();
	}
	protected void createN3Writer()
			throws UnsupportedEncodingException, FileNotFoundException {

		if(outputFilePath != null)
		{
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFilePath), "UTF-8");
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter pw = new PrintWriter(bw);
			N3KR2RMLRDFWriter n3Writer = new N3KR2RMLRDFWriter(new URIFormatter(), pw);
			
			if(baseURI != null)
			{
				n3Writer.setBaseURI(baseURI);
			}
			writers.add(n3Writer);
		}
		if (outputFileJSONPath != null) {
			JSONKR2RMLRDFWriter jsonWriter = new JSONKR2RMLRDFWriter(new PrintWriter(outputFileJSONPath), baseURI);
			writers.add(jsonWriter);
		}
	}

	protected void createBloomFilterWriter() throws Exception {
		if (bloomFiltersFilePath != null && !bloomFiltersFilePath.trim().isEmpty()) {
			PrintWriter bloomfilterpw = new PrintWriter(new File(bloomFiltersFilePath));
			logger.info(bloomFiltersFilePath);
			writers.add(createBloomFilterWriter(bloomfilterpw, true, baseURI));
		}

	}

	private KR2RMLRDFWriter createBloomFilterWriter(PrintWriter bloomfilterpw, Boolean isRDF, String baseURI)
			throws Exception {
		
		Reflections reflections = new Reflections("edu.isi.karma.kr2rml.writer");

		Set<Class<? extends KR2RMLRDFWriter>> subTypes =
				reflections.getSubTypesOf(KR2RMLRDFWriter.class);
		
		for (Class<? extends KR2RMLRDFWriter> subType : subTypes)
		{
			if(!Modifier.isAbstract(subType.getModifiers()) && !subType.isInterface() && subType.getName().equals("BloomFilterKR2RMLRDFWriter"))
				try
			{
					KR2RMLRDFWriter writer = subType.newInstance();
					writer.setWriter(bloomfilterpw);
					Properties p = new Properties();
					p.setProperty("is.rdf", isRDF.toString());
					p.setProperty("base.uri", baseURI);
					writer.initialize(p);
					return writer;
			}
			catch (Exception e)
			{
				bloomfilterpw.close();
				throw new Exception("Unable to instantiate bloom filter writer", e);
			}
		}

		bloomfilterpw.close();
		throw new Exception("Bloom filter writing support not enabled.  Please recompile with -Pbloom");
	}

	private void generateRdfFromFile()
			throws Exception {
		if(!validateFileCommandLineOptions())
		{
			logger.error("Unable to generate RDF from file because of invalid configuration");
			return;
		}
		R2RMLMappingIdentifier id = new R2RMLMappingIdentifier(sourceName, modelURL, null);

		createWriters();
		GenericRDFGenerator rdfGenerator = new GenericRDFGenerator(selectionName);
		rdfGenerator.addModel(id);
		
		InputType inputType = null;
		if(this.inputType.equalsIgnoreCase("CSV"))
			inputType = InputType.CSV;
		else if(this.inputType.equalsIgnoreCase("JSON"))
			inputType = InputType.JSON;
		else if(this.inputType.equalsIgnoreCase("XML"))
			inputType = InputType.XML;
		else if(this.inputType.equalsIgnoreCase("AVRO"))
			inputType = InputType.AVRO;
		else if(this.inputType.equalsIgnoreCase("JL"))
			inputType = InputType.JL;
		Model model = rdfGenerator.getModelParser(sourceName).getModel();
		if (rootTripleMap != null && !rootTripleMap.isEmpty()) {
			StmtIterator itr = model.listStatements(null, model.getProperty(Uris.KM_NODE_ID_URI), rootTripleMap);
			Resource subject = null;
			while (itr.hasNext()) {
				subject = itr.next().getSubject();
			}
			if (subject != null) {
				itr = model.listStatements(null, model.getProperty(Uris.RR_SUBJECTMAP_URI), subject);
				while (itr.hasNext()) {
					rootTripleMap = itr.next().getSubject().toString();
				}
			}
		}
		RDFGeneratorRequest request = new RDFGeneratorRequest(sourceName, inputFile.getName());
		request.setInputFile(inputFile);
		request.setDataType(inputType);
		if(inputEncoding != null) request.setEncoding(inputEncoding);
		if(inputDelimiter != null) request.setDelimiter(this.inputDelimiter);
		if(inputTextQualifier != null) request.setTextQualifier(inputTextQualifier);
		if(inputHeaderStartIndex != null) request.setHeaderStartIndex(Integer.parseInt(inputHeaderStartIndex));
		if(inputDataStartIndex != null) request.setDataStartIndex(Integer.parseInt(inputDataStartIndex));
		
		request.setMaxNumLines(maxNumLines);
		request.setAddProvenance(false);
		request.addWriters(writers);
		request.setPOMToKill(POMToKill);
		request.setTripleMapToKill(killTripleMap);
		request.setTripleMapToStop(stopTripleMap);
		request.setStrategy(new UserSpecifiedRootStrategy(rootTripleMap));
		request.setContextParameters(contextParameters);
		if (contextURL != null) {
			ContextIdentifier contextId = new ContextIdentifier(contextURL.getQuery(), contextURL, null);
			rdfGenerator.addContext(contextId);
			request.setContextName(contextURL.getQuery());
		}
		rdfGenerator.generateRDF(request);
	}


	private static Options createCommandLineOptions() {

		Options options = new Options();
				
		options.addOption(new Option("sourcetype", "sourcetype", true, "type of source. Valid values: DB, SQL, CSV, JSON, XML"));
		options.addOption(new Option("delimiter","delimiter", true, "column delimter for CSV file"));
		options.addOption(new Option("encoding","encoding",true, "source encoding"));
		options.addOption(new Option("textqualifier","textQualifier", true, "text qualifier for CSV file"));
		options.addOption(new Option("headerindex", "headerindex", true, "header index for CSV file"));
		options.addOption(new Option("dataindex", "dataindex", true, "data start index for CSV file"));
		options.addOption(new Option("filepath", "filepath", true, "location of the input file"));
		options.addOption(new Option("modelfilepath", "modelfilepath", true, "location of the model file"));
		options.addOption(new Option("modelurl", "modelurl", true, "location of the model"));
		options.addOption(new Option("sourcename", "sourcename", true, "name of the source in the model to use"));
		options.addOption(new Option("outputfile", "outputfile", true, "location of the output file"));
		options.addOption(new Option("dbtype", "dbtype", true, "database type. Valid values: Oracle, MySQL, SQLServer, PostGIS"));
		options.addOption(new Option("hostname", "hostname", true, "hostname for database connection"));
		options.addOption(new Option("username", "username", true, "username for database connection"));
		options.addOption(new Option("password", "password", true, "password for database connection"));
		options.addOption(new Option("portnumber", "portnumber", true, "portnumber for database connection"));
		options.addOption(new Option("dbname", "dbname", true, "database or SID name for database connection"));
		options.addOption(new Option("tablename", "tablename", true, "hostname for database connection"));
		options.addOption(new Option("topkrows", "topkrows", true, "number of top k rows to select from the table"));
		options.addOption(new Option("queryfile", "queryfile", true, "query file for loading data"));
		options.addOption(new Option("outputbloomfilter", "bloomfiltersfile", true, "generate bloom filters"));
		options.addOption(new Option("baseuri", "base URI", true, "specifies base uri"));
		options.addOption(new Option("selection", "selection", true, "specifies selection name"));
		options.addOption(new Option("root", "root", true, "specifies root"));
		options.addOption(new Option("killtriplemap", "killtriplemap", true, "specifies TripleMap to kill"));
		options.addOption(new Option("stoptriplemap", "stoptriplemap", true, "specifies TripleMap to stop"));
		options.addOption(new Option("pomtokill", "pomtokill", true, "specifies POM to kill"));
		options.addOption(new Option("jsonoutputfile", "jsonoutputfile", true, "specifies JSONOutputFile"));
		options.addOption(new Option("contextfile", "contextile", true, "specifies global context file"));
		options.addOption(new Option("contexturl", "contexturl", true, "specifies global context url"));
		options.addOption(new Option("help", "help", false, "print this message"));

		return options;
	}
}
