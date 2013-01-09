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

package edu.isi.karma.er.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import edu.isi.karma.er.helper.ConnectPostgis;

public class ExtractUSGSInformation {

	/**
	 * @param args
	 */
	private static Connection connection = null;
	private static Statement stmt = null;

	private static void openConnection() {
		ConnectPostgis conPostgis = new ConnectPostgis();
		connection = conPostgis.ConnectingPostgis();
	}

	private static void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (Exception ex) {
			ex.getMessage();
		}
	}

	private static void extractUSGS(String path) {
		try {
			stmt.executeQuery("DROP TABLE postgis.public.USGS_LosAngeles");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		
		try {
			stmt.executeQuery("CREATE TABLE postgis.public.USGS_LosAngeles (feature_name character varying,id character varying,class character varying,county character varying,state character varying,latitude character varying,longitude character varying,ele character varying,map character varying,bgn_date character varying,entry_date character varying)");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		
		try {// got the file "/tmp/USGS_Building.csv" from USGS web site;
			stmt.executeQuery("COPY postgis.public.USGS_LosAngeles FROM '/tmp/USGS_Building.csv' WITH DELIMITER '|' NULL AS '' CSV HEADER QUOTE AS '\"';");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}	
		
		try {// insert data into srid column with default value;
			stmt.executeQuery("ALTER TABLE postgis.public.USGS_LosAngeles ADD srid character varying default ('4326') ;");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		try {// insert data into srid column with default value;
			stmt.executeQuery("DELETE FROM usgs_losangeles WHERE latitude='0.0000000' and longitude='0.0000000';");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		
		
		
		
		try {// output table USGS_LosAngeles as a csv file;
			stmt.executeQuery("Copy (Select * From postgis.public.USGS_LosAngeles) To '/tmp/usgs_losangeles.csv' CSV HEADER;");
		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		
		
	}

	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		openConnection();
		try {
			stmt = connection.createStatement();
		} catch (Exception ex) {
			ex.getStackTrace();
		}

		extractUSGS("hello");
		
		
		closeConnection(connection);
	}

}
