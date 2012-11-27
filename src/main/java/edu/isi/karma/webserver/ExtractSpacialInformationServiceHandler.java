package edu.isi.karma.webserver;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

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

import edu.isi.karma.er.helper.ConnectPostgis;
import edu.isi.karma.webserver.helper.CreateGeoBuildingForTable;
import edu.isi.karma.webserver.helper.CreateGeoStreetForTable;

public class ExtractSpacialInformationServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger
			.getLogger(LinkedApiServiceHandler.class);
	private String url;
	private Connection connection = null;
	private String osmFile_path = "/tmp/GET_OPENSTREETMAP.xml";

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		logger.debug("Request URL: " + request.getRequestURI());
		logger.debug("Request Path Info: " + request.getPathInfo());
		logger.debug("Request Param: " + request.getQueryString());
		
		String bbox = request.getParameter("bbox");
		String url = "bbox=" + bbox;
		String type = request.getParameter("type");

		try {
			System.out
					.println("Please Wait for extracting information from Web Site...");
			outputToOSM(url);
			System.out.println("You have got the OSM File at location: /tmp/GET_OPENSTREETMAP.xml ...");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (type.equalsIgnoreCase("street") || type.equalsIgnoreCase("streets")) {
			System.out.println("Opening PostGis Connection...");
			openConnection();
			System.out.println("Creating the CSV file from OSM file...");
			CreateGeoStreetForTable cgs = new CreateGeoStreetForTable(
					this.connection, this.osmFile_path);
			System.out.println("Extracting the Street Information from OSM file...");
			cgs.createGeoStreet();
			System.out.println("You have created the CSV file for STREETS at location:/tmp/buildings_geo.csv");
		}else if(type.equalsIgnoreCase("building") || type.equalsIgnoreCase("buildings")){
			System.out.println("Opening PostGis Connection...");
			openConnection();
			System.out.println("Creating the CSV file from OSM file...");
			CreateGeoBuildingForTable cgb = new CreateGeoBuildingForTable(
					this.connection, this.osmFile_path);
			System.out.println("Extracting the Building Information from OSM file...");
			cgb.createOpenStreetMapBuildings();
			System.out.println("SUCCEED! You have created the CSV file for BUILDINGS at location:/tmp/buildings_geo.csv");
		}else{
			System.out.println("ERROR: You have input the wrong URL");
		}

		this.closeConnection(this.connection);// close connection

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
		this.connection = conPostgis.ConnectingPostgis();	
	}
	
	private void closeConnection(Connection connection) {
		try {
			connection.close();
		} catch (Exception ex) {
			ex.getMessage();
		}
	}

}
