package edu.isi.karma.webserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.jetty.http.HttpMethods;
import org.json.JSONArray;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetHierarchicalDataUpdate;
import edu.isi.karma.controller.update.WorksheetHierarchicalHeadersUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;


import edu.isi.karma.modeling.ontology.OntologyManager;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.rep.metadata.Tag;
import edu.isi.karma.rep.metadata.TagsContainer.Color;
import edu.isi.karma.rep.metadata.TagsContainer.TagName;
import edu.isi.karma.view.VWorksheet;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

import edu.isi.karma.er.helper.ConnectPostgis;
import edu.isi.karma.imp.csv.CSVFileImport;
import edu.isi.karma.linkedapi.server.GetRequestManager;
import edu.isi.karma.linkedapi.server.ResourceType;

import edu.isi.karma.service.MimeType;
import edu.isi.karma.service.SerializationLang;

import edu.isi.karma.webserver.helper.CreateGeoBuildingForTable;
import edu.isi.karma.webserver.helper.CreateGeoStreetForTable;

public class ExtractSpatialInformationServiceHandler extends HttpServlet {

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
		
		String jsonOutput=null;
		
		String minLon = request.getParameter("minLongitude");
		String minLat = request.getParameter("minLatitude");
		String maxLon = request.getParameter("maxLongitude");
		String maxLat = request.getParameter("maxLatitude");
		String type = request.getParameter("type");

		String url = "bbox=" + minLon+","+minLat+","+maxLon+","+maxLat;
		
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
			jsonOutput=cgs.createGeoStreet();
			System.out.println("You have created the CSV file for STREETS at location:/tmp/buildings_geo.csv");	
		}else if(type.equalsIgnoreCase("building") || type.equalsIgnoreCase("buildings")){
			System.out.println("Opening PostGis Connection...");
			openConnection();
			System.out.println("Creating the CSV file from OSM file...");
			CreateGeoBuildingForTable cgb = new CreateGeoBuildingForTable(
					this.connection, this.osmFile_path);
			System.out.println("Extracting the Building Information from OSM file...");
			jsonOutput=cgb.createOpenStreetMapBuildings();
			System.out.println("SUCCEED! You have created the CSV file for BUILDINGS at location:/tmp/buildings_geo.csv");
		}
		
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
