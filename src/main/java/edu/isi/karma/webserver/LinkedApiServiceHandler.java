package edu.isi.karma.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.isi.karma.linkedapi.server.GetRequestManager;
import edu.isi.karma.linkedapi.server.ResourceType;
import edu.isi.karma.service.SerializationLang;

public class LinkedApiServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String serviceId = request.getParameter("id");
		String format = request.getParameter("format");
		String resource = request.getParameter("resource");
		
		ResourceType resourceType = ResourceType.Service;
		if (resource != null && resource.trim().toString().equalsIgnoreCase("input"))
			resourceType = ResourceType.Input;
		if (resource != null && resource.trim().toString().equalsIgnoreCase("output"))
			resourceType = ResourceType.Output;

		if (format == null || (!format.equalsIgnoreCase(SerializationLang.N3) && 
				!format.equalsIgnoreCase(SerializationLang.N_TRIPLE) &&
				!format.equalsIgnoreCase(SerializationLang.XML_ABBREV) &&
				!format.equalsIgnoreCase(SerializationLang.SPARQL) ))
			format = SerializationLang.XML;

		if (resourceType == ResourceType.Service && 
				format.equalsIgnoreCase(SerializationLang.SPARQL))
			format = SerializationLang.XML;
		
		format = format.toUpperCase();
		
		if(serviceId != null) {
			new GetRequestManager(serviceId, resourceType, format, response).HandleRequest();
		} else {
			response.getWriter().write("No Service Request ID Found!");
		}
		
		response.flushBuffer(); 
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");
		if(id != null)
			response.getWriter().write("Request ID: " + id);
		else
			response.getWriter().write("No Service Request ID Found!");
		response.flushBuffer(); 
	}
}
