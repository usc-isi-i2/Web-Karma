package edu.isi.karma.webserver;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.isi.karma.er.helper.ConnectPostgis;
import edu.isi.karma.service.MimeType;

public class SpatialReferenceSystemServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger
			.getLogger(LinkedApiServiceHandler.class);
	private String url;
	private Connection connection = null;
	private String osmFile_path = "/tmp/GET_OPENSTREETMAP.xml";


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
		
		openConnection();


		String statement = "SELECT ST_AsText(ST_Transform(ST_GeomFromEWKT('SRID="
		+srid+";"+geometry+"'),4326))";
		
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
		
		jsonOutput=arr.toString();
		/*Close connection*/
		this.closeConnection(this.connection);
		
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

	protected void outputToOSM(String url) throws SQLException,
			ClientProtocolException, IOException {

		this.url = "http://www.openstreetmap.org/api/0.6/map?" + url;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(this.url);
		HttpResponse response = client.execute(get);
		HttpEntity entity = response.getEntity();
		String result = EntityUtils.toString(entity);
		FileOutputStream fout = new FileOutputStream(osmFile_path);
		OutputStream bout = new BufferedOutputStream(fout);
		OutputStreamWriter out = new OutputStreamWriter(bout, "UTF8");
		out.write(result);
		out.close();
	}

	private void openConnection(){
		ConnectPostgis conPostgis = new ConnectPostgis();
		this.connection = conPostgis.ConnectingPostgis("jdbc:postgresql://fusion.isi.edu:54322/testGIS","karma","2xpd516");	
	}
	
	private void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (Exception ex) {
			ex.getMessage();
		}
	}

}
