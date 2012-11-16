package edu.isi.karma.er.helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RefineResult {

	private Connection connection;
	private ResultSet rs2;
	private ResultSet rs3;
	private ResultSet rs1;

	/*
	 * 
	 * UPDATE buildingmatchedresult a set OpenCenter= (select ST_AStext(ST_Centroid(ST_GeomFromText(ST_AsText(a.source_polygon)))) ) where a.source_polygon!='null';
     * UPDATE postgis.public.buildingmatchedresult a set distanceC2C=(select ST_Distance(ST_GeographyFromText(\'"+Ocenter+"\'),ST_GeographyFromText(\'"+Wcenter+"\'))) where a.opencenter=Ocenter;
	 * 
	 * 
	 * 
	 */
	
	public void refiningResult(Connection connection, String csvFileAddress) {
		this.connection = connection;
		Statement stmt1 = null;
		Statement stmt2= null;


		try {
			stmt1 = this.connection.createStatement();

		} catch (Exception ex) {
			ex.getStackTrace();
		}
		try {
			stmt2 = this.connection.createStatement();

		} catch (Exception ex) {
			ex.getStackTrace();
		}

		String sql = "DROP TABLE postgis.public.refinematchedresult;";
		try {

			rs1 = stmt1.executeQuery(sql);// create a table for the result file
										// with csv format;

		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		sql = "CREATE TABLE postgis.public.refinematchedresult (Source character varying, Source_NAME character varying, "
				+ "Source_X double precision, Source_Y double precision, Source_POLYGON character varying, Matched character varying,"
				+ " Matched_Building_Name character varying, Matched_Building_X double precision,Matched_Building_Y double precision,"
				+ "Matched_Building_polygon character varying,Distance double precision, Similarity double precision,IsOverlaps boolean);";
		try {

			rs1 = stmt1.executeQuery(sql);// create a table for the result file
										// with csv format;

		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		
		
		sql = "COPY postgis.public.refinematchedresult FROM '" + csvFileAddress
				+ "' WITH DELIMITER ',' NULL AS '' CSV HEADER QUOTE AS '|';";
		try {

			rs2 = stmt2.executeQuery(sql);// Importing data into talbe;

		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		
		isOverlaps();
		


	}
	
	
	
	
	private void isOverlaps(){
	    Statement stmt=null;
	    Statement stmt2=null;
		ResultSet rs =null;
		String sql = "select * from postgis.public.refinematchedresult where source_polygon!='null'";
		try {
			stmt = this.connection.createStatement();

		} catch (Exception ex) {
			ex.getStackTrace();
		}
		try {
			stmt2 = this.connection.createStatement();

		} catch (Exception ex) {
			ex.getStackTrace();
		}
		//String sql = "select source_polygon,matched_building_polygon from postgis.public.refinematchedresult where source_polygon!='null';";
		try {

			 rs = stmt.executeQuery(sql);// Importing data into talbe;

		} catch (SQLException ee) {
			ee.getStackTrace();
		}

		try {
			while (rs.next()) {
				String source_polygon = rs.getString("source_polygon");
				String matched_polygon = rs.getString("matched_building_polygon");

				
				String sql1 = "UPDATE postgis.public.refinematchedresult a set isoverlaps=(select ST_Overlaps " +
						"(ST_GeomFromText(ST_AsText(\'"+source_polygon+"\')),ST_GeomFromText(ST_AsText(\'"+matched_polygon+"\')))) " +
								"where a.source_polygon=\'"+source_polygon+"\'";
				
				
				/*
				String sql1 = "UPDATE postgis.public.refinematchedresult a set isoverlaps=(select ST_Overlaps " +
						"(ST_GeomFromText(ST_AsText(\'"+source_polygon+"\')),ST_GeomFromText(ST_AsText(\'"+matched_polygon+"\')))) " ;
*/
				System.out.println(sql1);

				try {
					rs2 = stmt2.executeQuery(sql1);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		String sql2= "Copy (select * from refinematchedresult order by similarity DESC) To '/Users/yzhang/Downloads/BUILDING_1.csv' CSV HEADER;";
		try {
			rs3 = stmt2.executeQuery(sql2);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	

}
