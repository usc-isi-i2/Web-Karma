package edu.isi.karma.webserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.isi.karma.linkedapi.server.GetRequestManager;
import edu.isi.karma.linkedapi.server.PostRequestManager;
import edu.isi.karma.linkedapi.server.ResourceType;
import edu.isi.karma.service.MimeType;
import edu.isi.karma.service.SerializationLang;

public class LinkedApiServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(LinkedApiServiceHandler.class);

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
			response.setContentType(MimeType.TEXT_PLAIN);
			response.getWriter().write("No Service Request ID Found!");
		}
		
		response.flushBuffer(); 
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String serviceId = request.getParameter("id");
		String format = request.getParameter("format");
		
		//request.setCharacterEncoding(CharEncoding.ISO_8859_1);
		logger.info("Content-Type: " + request.getContentType());
		
		String inputLang = "";
		if (request.getContentType().startsWith(MimeType.APPLICATION_RDF_XML))
			inputLang = SerializationLang.XML;
		if (request.getContentType().startsWith(MimeType.TEXT_XML))
			inputLang = SerializationLang.XML;
		else if (request.getContentType().startsWith(MimeType.APPLICATION_XML))
			inputLang = SerializationLang.XML;
		else if (request.getContentType().startsWith(MimeType.APPLICATION_RDF_N3))
			inputLang = SerializationLang.N3;
		if (request.getContentType().startsWith(MimeType.APPLICATION_FORM_URLENCODED))
			;
		else {
			response.setContentType(MimeType.TEXT_PLAIN);
			response.getWriter().write("The content type is neither rdf+xml nor rdf+n3");
			return;
		}
		
		if (format == null || (!format.equalsIgnoreCase(SerializationLang.N3) && 
				!format.equalsIgnoreCase(SerializationLang.N_TRIPLE) &&
				!format.equalsIgnoreCase(SerializationLang.XML_ABBREV) ))
			format = SerializationLang.XML;

		format = format.toUpperCase();
		
		InputStream in = request.getInputStream();
		
		if (request.getContentType().startsWith(MimeType.APPLICATION_FORM_URLENCODED)) {
			inputLang = SerializationLang.N3; // default for html forms
			String formData = request.getParameter("rdf");
			logger.debug(formData);
			if (formData != null) {
				in = new ByteArrayInputStream(formData.getBytes());
			}
		} 
		
		if(serviceId != null) {
			new PostRequestManager(serviceId, in, inputLang, format, response).HandleRequest();
		} else {
			response.getWriter().write("No Service Request ID Found!");
		}
		
		response.flushBuffer();
	}
	
//	 public static Document loadXMLFromString(String xml) throws Exception
//	 {
//        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        InputSource is = new InputSource(new StringReader(xml));
//        return builder.parse(is);
//	 }
}
