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

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.json.JSONException;

import com.hp.hpl.jena.rdf.model.Model;

import edu.isi.karma.kr2rml.ErrorReport;
import edu.isi.karma.kr2rml.KR2RMLWorksheetRDFGenerator;
import edu.isi.karma.kr2rml.WorksheetR2RMLJenaModelParser;
import edu.isi.karma.rep.HNode;
import edu.isi.karma.rep.HTable;
import edu.isi.karma.rep.RepFactory;
import edu.isi.karma.rep.Row;
import edu.isi.karma.rep.Table;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.util.AbstractJDBCUtil;
import edu.isi.karma.util.AbstractJDBCUtil.DBType;
import edu.isi.karma.util.JDBCUtilFactory;
import edu.isi.karma.webserver.KarmaException;

public class DatabaseTableRDFGenerator {
	private DBType dbType;
	private String hostname;
	private int portnumber;
	private String username;
	private String password;
	private String dBorSIDName;
	private String tablename;
	
	private static int DATABASE_TABLE_FETCH_SIZE = 1000;
	
	public DatabaseTableRDFGenerator(DBType dbType, String hostname,
			int portnumber, String username, String password,
			String dBorSIDName, String tablename) {
		super();
		this.dbType = dbType;
		this.hostname = hostname;
		this.portnumber = portnumber;
		this.username = username;
		this.password = password;
		this.dBorSIDName = dBorSIDName;
		this.tablename = tablename;
	}
	
	public void generateRDF(Workspace workspace, PrintWriter pw, Model model) 
			throws IOException, JSONException, KarmaException, SQLException, ClassNotFoundException {
		System.out.println("Generating RDF...");
		
		RepFactory factory = workspace.getFactory();
		
		AbstractJDBCUtil dbUtil = JDBCUtilFactory.getInstance(dbType);
		Connection conn = dbUtil.getConnection(hostname, portnumber, username, password, dBorSIDName);
		
		String query = "Select * FROM " + tablename;
		java.sql.Statement stmt = conn.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
				java.sql.ResultSet.CONCUR_READ_ONLY);
		stmt.setFetchSize(DATABASE_TABLE_FETCH_SIZE);
		
		ResultSet r = stmt.executeQuery(query);
		ResultSetMetaData meta = r.getMetaData();;
		
		// Get the column names
		ArrayList<String> columnNames = dbUtil.getColumnNames(tablename, conn);
		
		// Prepare required Karma objects
		Worksheet wk = factory.createWorksheet(tablename, workspace);
		ArrayList<String> headersList = addHeaders(wk, columnNames, factory);
		
		int counter = 0;
		
		while (r.next()) {
			// Generate RDF and create a new worksheet for every DATABASE_TABLE_FETCH_SIZE rows
			if(counter%DATABASE_TABLE_FETCH_SIZE == 0 && counter != 0) {
				generateRDFFromWorksheet(wk, workspace, model, pw);
				System.out.println("Done for " + counter + " rows ..." );
				
				wk = factory.createWorksheet(tablename, workspace);
				headersList = addHeaders(wk, columnNames, factory);
				
			}
			
			/** Add the data **/
	        Table dataTable = wk.getDataTable();
	        Row row = dataTable.addRow(factory);
			for (int i = 1; i <= meta.getColumnCount(); i++) {
				String val = r.getString(i);
				row.setValue(headersList.get(i-1), val, factory);
			}
			counter++;
		}
		
		generateRDFFromWorksheet(wk, workspace, model, pw);
		
		// Releasing all the resources
		r.close();
		conn.close();
		stmt.close();
		System.out.println("done");
	}
	
	private void generateRDFFromWorksheet(Worksheet wk, 
			Workspace workspace, Model model, PrintWriter pw) 
					throws IOException, JSONException, KarmaException {
		// Generate RDF for the remaining rows
		WorksheetR2RMLJenaModelParser parserTest = new WorksheetR2RMLJenaModelParser(
				wk, workspace, model, tablename);
		
		// Gets all the errors generated during the RDF generation
		ErrorReport errorReport = new ErrorReport();
		
		// RDF generation object initialization
		KR2RMLWorksheetRDFGenerator rdfGen = new KR2RMLWorksheetRDFGenerator(wk, 
				workspace.getFactory(), workspace.getOntologyManager(), pw, 
				parserTest.getAuxInfo(), errorReport, false);

		// Generate the rdf
		rdfGen.generateRDF(false);
	}
	
	private ArrayList<String> addHeaders (Worksheet wk, ArrayList<String> columnNames,
			RepFactory factory) {
		HTable headers = wk.getHeaders();
		ArrayList<String> headersList = new ArrayList<String>();
        for(int i=0; i< columnNames.size(); i++){
        	HNode hNode = null;
        	hNode = headers.addHNode(columnNames.get(i), wk, factory);
        	headersList.add(hNode.getId());
        }
        return headersList;
	}
}
