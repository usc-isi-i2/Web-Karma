// __COPYRIGHT_START__
//
// Copyright 2009 University of Southern California. All Rights Reserved.
//
// __COPYRIGHT_END__

package edu.isi.mediator.rdf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
