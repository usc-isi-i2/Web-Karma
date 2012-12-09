package edu.isi.karma.webserver;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.er.helper.ConnectPostgis;
import edu.isi.karma.service.MimeType;

public class SpatialReferenceSystemServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger
			.getLogger(LinkedApiServiceHandler.class);
	//private String url;
	private Connection connection = null;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		logger.debug("Request URL: " + request.getRequestURI());
		logger.debug("Request Path Info: " + request.getPathInfo());
		logger.debug("Request Param: " + request.getQueryString());
		Statement stmt = null;
		ResultSet rs = null;
		String jsonOutput=null;
		
		String geometry = request.getParameter("geometry");
		String srid = request.getParameter("srid");
		
		//openConnection();


		String statement = "SELECT ST_AsText(ST_Transform(ST_GeomFromText('"+geometry+"',"
		+srid+"),4326))";
		
		JSONObject obj=new JSONObject();
		JSONArray arr=new JSONArray();
		try {
			stmt = connection.createStatement();

		} catch (Exception ex) {
			ex.getStackTrace();
		}

		try {

			rs = stmt
					.executeQuery(statement);
			
			while (rs.next()) {
				try {
					
					obj.put("Geometry", rs.getString(1));
					obj.put("SRID", 4326);
					arr.put(obj);
					obj=new JSONObject();
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		} catch (SQLException ee) {
			ee.getStackTrace();
		}
		/*Close connection*/
		//this.closeConnection(this.connection);
		
		if(arr.length()==0) {
			try {
				obj.put("Geometry", geometry);
				obj.put("SRID", srid);
				arr.put(obj);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		jsonOutput=arr.toString();
		
		/*Output the JSON content to Web Page*/
		PrintWriter pw = response.getWriter();
		response.setContentType(MimeType.APPLICATION_JSON);
		pw.write(jsonOutput);
		return;

	}

	
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
          // doPost;
	
	}
/*
	private void openConnection(){
		ConnectPostgis conPostgis = new ConnectPostgis();
		//this.connection = conPostgis.ConnectingPostgis("jdbc:postgresql://fusion.isi.edu:54322/testGIS","karma","2xpd516");	
		this.connection = conPostgis.ConnectingPostgis("jdbc:postgresql://localhost:54321/testGIS","karma","2xpd516");	
	}
	
	private void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (Exception ex) {
			ex.getMessage();
		}
	}*/

}
