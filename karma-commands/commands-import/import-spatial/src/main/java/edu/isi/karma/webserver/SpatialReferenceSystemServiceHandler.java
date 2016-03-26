package edu.isi.karma.webserver;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.geospatial.SpatialReferenceSystemTransformationUtil;
import edu.isi.karma.model.serialization.MimeType;

public class SpatialReferenceSystemServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory
			.getLogger(SpatialReferenceSystemServiceHandler.class);

	public void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		logger.debug("Request URL: " + request.getRequestURI());
		logger.debug("Request Path Info: " + request.getPathInfo());
		logger.debug("Request Param: " + request.getQueryString());
		
		String jsonOutput;

		String inGeomWKT = request.getParameter("geometry");
		String fromSRID = request.getParameter("srid");
		JSONObject obj=new JSONObject();
		JSONArray arr=new JSONArray();
		try {
			if(fromSRID=="4326" || fromSRID=="EPSG:4326") {
				obj.put("Geometry", inGeomWKT);
				obj.put("SRID", fromSRID);
				arr.put(obj);
			}else {
				String outGeomWKT = SpatialReferenceSystemTransformationUtil.Transform(inGeomWKT, fromSRID, "4326");
				obj.put("Geometry", outGeomWKT);
				obj.put("SRID", 4326);
				arr.put(obj);
			}
		} catch (JSONException e) {
			logger.error("Cannot write JSON!",e);
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
