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



package edu.isi.karma.webserver;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.sql.Connection;
import java.sql.SQLException;

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
import edu.isi.karma.service.MimeType;
import edu.isi.karma.webserver.helper.CreateWikimapiaInformation;

public class ExtractSpatialInformationFromWikimapiaServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger
			.getLogger(LinkedApiServiceHandler.class);
	private String url;
	private Connection connection = null;
	private String osmFile_path ="/tmp/GET_WIKIMAPIA.xml";


	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		logger.debug("Request URL: " + request.getRequestURI());
		logger.debug("Request Path Info: " + request.getPathInfo());
		logger.debug("Request Param: " + request.getQueryString());
		
		String jsonOutput=null;
		
		String lon_min = request.getParameter("lon_min");
		String lat_min = request.getParameter("lat_min");
		String lon_max = request.getParameter("lon_max");
		String lat_max = request.getParameter("lat_max");
		//String type = request.getParameter("type");

		String url = "&lon_min="+lon_min+"&lat_min="+lat_min+"&lon_max="+lon_max+"&lat_max="+lat_max;
		
		try {
			System.out
					.println("Please Wait for extracting information from Web Site...");
			outputToOSM(url);
			System.out.println("You have got the OSM File at location: /tmp/GET_WIKIMAPIA.xml ...");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
			System.out.println("Opening PostGis Connection...");
			openConnection();
			System.out.println("Creating the CSV file from OSM file...");
			
			CreateWikimapiaInformation cwi = new CreateWikimapiaInformation(
					this.connection, this.osmFile_path);
			System.out.println("Extracting the Street Information from OSM file...");
			
			jsonOutput=cwi.createWikiMapiaTable();
			System.out.println("You have created the CSV file for STREETS at location:/tmp/buildings_geo.csv");	
		
		/*Close connection*/
		this.closeConnection(this.connection);
		
		/*Output the JSON content to Web Page*/
		response.setCharacterEncoding("UTF8");
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
		//String url = "http://api.wikimapia.org/?function=box&count=100&lon_min=-118.29244&lat_min=34.01794&lon_max=-118.28&lat_max=34.02587&key=156E90A4-57B03618-05BDBEA0-ED9C6E97-DD2116A6-A5229FEC-091E312A-2856C8BA";		
		this.url = "http://api.wikimapia.org/?function=box&count=100" + url+"&key=156E90A4-57B03618-05BDBEA0-ED9C6E97-DD2116A6-A5229FEC-091E312A-2856C8BA";
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
