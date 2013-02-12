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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import au.com.bytecode.opencsv.CSVReader;
import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorLogger;
import edu.isi.mediator.rule.LAVRule;


/**
 * Class that provides methods for generating RDF from CSV file.
 * Takes a source description (rule) and an output file 
 * and generates triples. Outputs the RDF triples in N3 notation
 * in the output file or Stdout.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class RuleRDFGeneratorCSV extends RuleRDFGenerator {

	LAVRule lavRule;
	
	private static final MediatorLogger logger = MediatorLogger.getLogger(RuleRDFGeneratorCSV.class.getName());

	/**
	 * Copy Constructor.
	 * @param r
	 * @throws MediatorException
	 */
	public RuleRDFGeneratorCSV(RuleRDFGenerator r) throws MediatorException{
		super(r.rule, r.sourceNamespaces, r.ontologyNamespaces, r.outWriter, r.uniqueId);		
		lavRule = (LAVRule)rule;
	}

	/**
	 * Generates triples and writes them to output.
	 * <p>Gets data from the input CSV file. Constructs triples for each row in the file.
	 * <br>The first row represents the column name, and it should contain names of attributes used in the rule.
	 * <br>For each row calls generateTriples(Map<String,String> values).
	 * @param inputFile
	 * 		input CSV file
	 * @throws MediatorException
	 * @throws SQLException
	 */
	@SuppressWarnings("all")
	public void generateTriplesCSV(String inputFile) throws MediatorException, IOException{

		//outWriter.println();
		//outWriter.println("# Table: " + rule.getHead().toString());

		CSVReader reader = new CSVReader(new BufferedReader(new FileReader(inputFile)));
		List<String []> lines = reader.readAll();
		if(lines.isEmpty()){
			throw new MediatorException("Input file is EMPTY!");
		}
		String[] firstLine = lines.get(0);

		
		///////////////////////////
		/*
		System.out.print("Parse File:[");
		for (int j = 0; j < firstLine.length; j++){
			if(j>0)
				System.out.print(",");
			System.out.print(firstLine[j]);
		}
		System.out.println("]");
		*/
		//////////////////////
		
		//column names used in the rule
		ArrayList<String> colNamesInHead = lavRule.getHead().getVars();

		for(int l=1; l<lines.size(); l++){
			String[] line = lines.get(l);
			///////////////
			/*
			for (int j = 0; j < line.length; j++){
				if(j>0)
					System.out.print(",");
				System.out.print("val=" + line[j]);
			}
			*/
			//////////////
			//represents one row
			Map<String,String> values = new HashMap<String,String>();
			for (int i = 0; i < line.length; i++) {
				String colName = firstLine[i].trim();
				String val = line[i].trim();

				//System.out.println("name=" + colName + " val=" + val);

				//12/7/2012 MariaM
				if(val.trim().equals("null")){
					val="NULL";
				}

				//I need only values used in the rule
				if(colNamesInHead.contains(colName))
					values.put(colName, val);
			}
			if(l%10000==0)
				logger.info("Processed " + l + " rows");
			//for one row
			generateTriples(values);
			//if(row==3) break;
		}
		reader.close();
	}
	
}
