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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorLogger;
import edu.isi.mediator.gav.util.MediatorUtil;
import edu.isi.mediator.rule.LAVRule;


/**
 * Class that provides methods for generating RDF from an Access database.
 * Takes a source description (rule) and an output file 
 * and generates triples. Outputs the RDF triples in N3 notation
 * in the output file or Stdout.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class RuleRDFGeneratorAccess extends RuleRDFGenerator{

	LAVRule lavRule;
	
	private static final MediatorLogger logger = MediatorLogger.getLogger(RuleRDFGeneratorAccess.class.getName());

	/**
	 * Copy Constructor.
	 * @param r
	 * @throws MediatorException
	 */
	public RuleRDFGeneratorAccess(RuleRDFGenerator r) throws MediatorException{
		super(r.rule, r.sourceNamespaces, r.ontologyNamespaces, r.outWriter, r.uniqueId);	
		lavRule = (LAVRule)rule;
	}

	/**
	 * Generates triples and writes them to output.
	 * <p>Gets data from the input access DB. constructs triples for each row of the table represented by this rule
	 * <br> Uses jackcess API to connect to the ACCESS DB.
	 * <br>For each row calls generateTriples(Map<String,String> values).
	 * @param accessDB
	 * 		the ACCESS DB (.mdb; .accdb)
	 * @throws MediatorException
	 * @throws IOException
	 */
	public void generateTriplesACCESS(String accessDB) throws MediatorException, IOException{

		outWriter.println();
		outWriter.println("# Table: " + lavRule.getHead().toString());

		String tableName = MediatorUtil.removeBacktick(lavRule.getHead().getName());
		
		Table table = Database.open(new File(accessDB)).getTable(tableName);
		if(table==null)
			throw new MediatorException("Table " + lavRule.getHead().getName() + " not found in " + accessDB);
		///////////////////////////
		/*
		List<Column> columns = table.getColumns();
		
		System.out.print("Parse File:[");
		for (int j = 0; j < columns.size(); j++){
			if(j>0)
				System.out.print(",");
			System.out.print(columns.get(j).getName());
		}
		System.out.println("]");
		*/
		//////////////////////
		
		//column names used in the rule
		ArrayList<String> colNamesInHead = lavRule.getHead().getVars();

		//check that all columns in the rule are in the result
		for (int i = 0; i < colNamesInHead.size(); i++) {
			String colName = colNamesInHead.get(i).trim();
			try{
				table.getColumn(colName);
			}
			catch(Exception e){
				throw new MediatorException("Column " + colName + " not found in table " + lavRule.getHead().getName() + "." + 
							e.getMessage());
			}
		}

		int l=0;
		for(Map<String, Object> row : table) {
			l++;
			//System.out.println("One row=" + row);
			//represents one row
			Map<String,String> values = new HashMap<String,String>();
			for (int i = 0; i < colNamesInHead.size(); i++) {
				String colName = colNamesInHead.get(i).trim();
				Object valO = row.get(colName);
				String val = "";
				if(valO==null){
					//it is possible that the value is NULL
					val="NULL";
				}
				else
					val = valO.toString();
				//I need only values used in the rule
				values.put(colName, val);
			}
			if(l%10000==0)
				logger.info("Processed " + l + " rows");
			//for one row
			generateTriples(values);
			//if(row==3) break;
		}
	}
}
