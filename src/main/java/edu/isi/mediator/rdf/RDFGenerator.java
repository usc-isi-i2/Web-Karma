/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *  
 *    This code was developed by the Information Integration Group as part 
 *    of the Karma project at the Information Sciences Institute of the 
 *    University of Southern California.  For more information, publications, 
 *    and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.mediator.rdf;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import edu.isi.mediator.domain.parser.DomainParser;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.rule.Rule;
import edu.isi.mediator.gav.util.MediatorUtil;


/**
 * Class that provides methods for generating RDF.
 * Takes a domain file containing source descriptions (rules), 
 * a connect string to a database or a CSV file and an output file 
 * and generates triples. Output is in N3 notation.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class RDFGenerator {

	/**
	 * List of RDFGenerators, one for each of the rules in the domain file.
	 */
	private ArrayList<RuleRDFGenerator> rdfGenerators = new ArrayList<RuleRDFGenerator>();
	/**
	 * Output writer. Either to a file or to System.out
	 */
	private PrintWriter outWriter;
	
	/**
	 * Database connection string to the DB that provides values for the attributes in the rules.
	 */
	private Connection conn;
	
	/**
	 * Unique id, used for generating value for attribs thathave no value in the DB.
	 */
	private long uniqueId = Calendar.getInstance().getTimeInMillis();
	/**
	 * Contains mapping of prefix name to source namespace.
	 * If prefix is not used in the source desc we have only one source namespace.
	 * key=prefix; value=namespace;
	 */
	Map<String,String> sourceNamespaces;
	/**
	 * Contains mapping of prefix name to ontology namespace.
	 * If prefix is not used in the source desc we have only one ontology namespace.
	 * key=prefix; value=namespace;
	 */
	Map<String,String> ontologyNamespaces;
	
	/**
	 * Constructs a RDFGenerator.
	 * @param domainFile
	 * 		The domain file name. Should contain "NAMESPACES" and "LAV_RULES" sections.
	 * @param outputFile
	 * 		location of output file OR null if output to Stdout
	 * @throws MediatorException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public RDFGenerator(String domainFile, String outputFile) 
				throws MediatorException, ClassNotFoundException, IOException{
		String ruleFileStr = MediatorUtil.getFileAsString(domainFile);
		initParams(ruleFileStr,outputFile);
	}

	/**
	 * Constructs a RDFGenerator.
	 * @param domainStr
	 * 		The domain file as string. Should contain "NAMESPACES" and "LAV_RULES" sections.
	 * @param outputFile
	 * 		location of output file OR null if output to Stdout
	 * @param b
	 * 		not used, can be any value (to distinguish between constructors)
	 * 		<br> I did not want to change the previous constructor for backward compatibility
	 * @throws MediatorException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public RDFGenerator(String domainStr, String outputFile, boolean b) 
				throws MediatorException, ClassNotFoundException, IOException{
		initParams(domainStr,outputFile);
	}

	/**
	 * Initialize class members.
	 * @param domainStr
	 * 		The domain file as string. Should contain "NAMESPACES" and "LAV_RULES" sections.
	 * @param outputFile
	 * 		location of output file OR null if output to Stdout
	 * @throws MediatorException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void initParams(String domainStr, String outputFile)
							throws MediatorException, ClassNotFoundException, IOException{
		DomainParser sp = new DomainParser();
		RDFDomainModel dm = (RDFDomainModel)sp.parseDomain(domainStr);

		sourceNamespaces = dm.getSourceNamespaces();
		//System.out.println("SN="+sourceNamespaces);
		ontologyNamespaces = dm.getOntologyNamespaces();
		//output file
		if(outputFile!=null){
			OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8");
			//FileWriter fw = new FileWriter (outputFile);
			BufferedWriter bw = new BufferedWriter (fw);
			outWriter = new PrintWriter (bw);
		}else{
			outWriter = new PrintWriter (System.out);			
		}
		
		//print version
		//outWriter.println("# RDFGenerator VERSION: v0.5");
		//outWriter.println();
		
		//write namespaces to out file
		RDFUtil.setNamespace(sourceNamespaces,ontologyNamespaces,outWriter);

        //create rdf generators for each rule
		//for LAV  and GLAV rules
        for(int i=0; i<dm.getAllRules().size(); i++){
        	Rule rule = dm.getAllRules().get(i);
        	RuleRDFGenerator rgen = new RuleRDFGenerator(rule, sourceNamespaces,
        			ontologyNamespaces, outWriter, uniqueId+ "t" + (i+1));
        	rdfGenerators.add(rgen);
        }
	}
	
	/**
	 * Returns a database connection.
	 * @param connectStr
	 * 		database connect string
	 * @param dbDriver
	 * 		database driver
	 * @return
	 * 		a database connection.
	 * @throws MediatorException
	 */
	private Connection getDbConnection(String connectStr, String dbDriver) throws MediatorException {
		Connection localConn = null;
		try {
			Class.forName(dbDriver);
			localConn = DriverManager.getConnection(connectStr);
		} catch (Exception e) {
			throw new MediatorException("Error occured while connecting to:" + connectStr + " " + e.getMessage());
		}
		return localConn;
	}

	/**
	 * Close Database Connection.
	 * @throws SQLException 
	 */
	private void closeDBConnection() throws SQLException {
		conn.close();
	}
	
	/**
	 * Close Output Writer.
	 */
	private void closeWriter(){
		outWriter.flush();
		outWriter.close();
	}

	/**
	 * Determines which generateTriples() method to use depending on the inputs.
	 * @param input
	 * 		csv file, or access .mdb file, or connect string
	 * @param sourceType
	 * 		"CSV", or "ACCESS" or DB_DRIVER
	 * @throws MediatorException
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 */
	public void generateTriples(String input, String sourceType) throws MediatorException, IOException, SQLException, ClassNotFoundException{
		
		if(sourceType == RDFGeneratorMain.CSV)
			generateTriplesCSV(input);
		else if(sourceType == RDFGeneratorMain.ACCESS)
			generateTriplesACCESS(input);
		else{
			//sourceType is the DBDriver
			generateTriplesDB(input,sourceType);
		}

	}
	
	/**
	 * Generate all triples for the rules in the domain file.
	 * @param connectStr
	 * 		database connect string
	 * @param dbDriver
	 * 		database driver
	 * @throws MediatorException
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * @throws UnsupportedEncodingException 
	 */
	private void generateTriplesDB(String connectStr, String dbDriver) throws MediatorException, SQLException, ClassNotFoundException, UnsupportedEncodingException{
        //get a connection to the DB
        conn = getDbConnection(connectStr, dbDriver);

        //System.out.println("Database connection made.");

		for(int i=0; i<rdfGenerators.size(); i++){
			RuleRDFGeneratorDB gen = new RuleRDFGeneratorDB(rdfGenerators.get(i));
			gen.generateTriplesDB(conn);
		}
		closeDBConnection();
		closeWriter();
	}

	/**
	 * Generate all triples for the rules in the domain file.
	 * @param inputFile
	 * 		input CSV file; first line should contain column names
	 * @throws MediatorException
	 * @throws IOException
	 */
	private void generateTriplesCSV(String inputFile) throws MediatorException, IOException{
		//only one rule should be present in the rule file (one input file/rule)
		//we could have several rules for the same concept
		//if(rdfGenerators.size()!=1){
		//throw new MediatorException("Source Descriptions file should contain ONE rule.");
		//}
		for(int i=0; i<rdfGenerators.size(); i++){
			RuleRDFGeneratorCSV gen =  new RuleRDFGeneratorCSV(rdfGenerators.get(i));
			gen.generateTriplesCSV(inputFile);
		}
		closeWriter();
	}

	/**
	 * Generate all triples for the rules in the domain file.
	 * @param accessDb
	 * 		path to a ACCESS DB file (.mdb; .accdb)
	 * @throws MediatorException
	 * @throws IOException
	 */
	private void generateTriplesACCESS(String accessDb) throws MediatorException, IOException{
		for(int i=0; i<rdfGenerators.size(); i++){
			RuleRDFGeneratorAccess gen = new RuleRDFGeneratorAccess(rdfGenerators.get(i));
			gen.generateTriplesACCESS(accessDb);
		}
		closeWriter();
	}

}
