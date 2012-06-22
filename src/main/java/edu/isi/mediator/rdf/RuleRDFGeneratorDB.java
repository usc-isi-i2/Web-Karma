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


import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.isi.mediator.gav.main.MediatorException;
import edu.isi.mediator.gav.util.MediatorLogger;
import edu.isi.mediator.simplegraph.SimpleGraph;


/**
 * Class that provides methods for generating RDF from a database.
 * Takes a source description (rule) and an output file 
 * and generates triples. Outputs the RDF triples in N3 notation
 * in the output file or Stdout.
 * 
 * @author Maria Muslea(USC/ISI)
 *
 */
public class RuleRDFGeneratorDB extends RuleRDFGenerator{

	private static final MediatorLogger logger = MediatorLogger.getLogger(RuleRDFGeneratorDB.class.getName());

	/**
	 * Copy Constructor.
	 * @param r
	 * @throws MediatorException
	 */
	public RuleRDFGeneratorDB(RuleRDFGenerator r) throws MediatorException{
		super(r.rule, r.sourceNamespaces, r.ontologyNamespaces, r.outWriter, r.uniqueId);		
	}
	
	/**
	 * Generates triples and writes them to output.
	 * <p>Uses the database defined by the connection string and retrieves tuples
	 * <br>from the table defined by the rule name. Each tuple contains values 
	 * <br>for the attributes used in the rule.
	 * <br>For each row in the table calls generateTriples(Map<String,String> values).
	 * @param conn
	 * 		database connection
	 * @throws MediatorException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws UnsupportedEncodingException 
	 */
	public void generateTriplesDB(Connection conn) throws MediatorException, ClassNotFoundException, SQLException, UnsupportedEncodingException{

		if(conn==null){
			throw new MediatorException("Connection to database was not established");
		}
		//get the rule head
		//outWriter.println();
		//outWriter.println("# Table: " + rule.antecedentToString());

		java.sql.Statement s = conn.createStatement();
		//if table has a backtick in the domain file leave it like that in the query
		//ResultSet r = s.executeQuery("select * from " + table);
		//either a simple query or a join query
		String query = buildQuery();
		ResultSet r = s.executeQuery(query);

		
		ResultSetMetaData meta = null;

		if (r == null) {
			s.close();
			throw new MediatorException("Could not execute query:" + "select * from " + query);
		}

		//column names used in the rule
		ArrayList<String> colNamesInHead = rule.getAllAntecedentVars();
		
		int row=0;
		meta = r.getMetaData();
		while (r.next()) {
			row++;
			//represents one row
			Map<String,String> values = new HashMap<String,String>();
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				String colName = meta.getColumnName(i);
				String val = r.getString(i);

				//System.out.println("VAL=" + val);
				if(val==null){
					val="NULL";
				}
				//I need only values used in the rule
				if(colNamesInHead.contains(colName))
					values.put(colName, val);
			}
			if(row%10000==0)
				logger.info("Processed " + row + " rows");
			//for one row
			generateTriples(values);
			//if(row==3) break;
		}
		r.close();
		s.close();
	}	
	
	/**
	 * Builds a SQL query from the given rule.
	 * @return
	 * 		a SQL query from the given rule.
	 * <br> If the rule contains one predicate, returns a query of the form select * from predName.
	 * <br> If the rule contains a conjunction, the SQL query is a join between the predicates.
	 * @throws MediatorException
	 */
	String buildQuery() throws MediatorException{
		
		//check if I have a simple query or a join
		if(rule.getAntecedent().size()==1){
			//I have one predicate
			//get the table name
			String tableName = rule.getAntecedent().get(0).getName();
			return "select * from " + tableName;
		}
		else{
			//I have a join
			//construct a graph
			SimpleGraph g = new SimpleGraph();
			g.generateGraph(rule.getAntecedent());
			String sql = g.getSQL();
			System.out.println("SQl="+sql);
			return sql;
		}		
	}
}
