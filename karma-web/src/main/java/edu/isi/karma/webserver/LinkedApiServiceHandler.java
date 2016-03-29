package edu.isi.karma.webserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.common.HttpMethods;
import edu.isi.karma.linkedapi.server.GetRequestManager;
import edu.isi.karma.linkedapi.server.PostRequestManager;
import edu.isi.karma.linkedapi.server.ResourceType;
import edu.isi.karma.model.serialization.MimeType;
import edu.isi.karma.model.serialization.SerializationLang;

public class LinkedApiServiceHandler extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(LinkedApiServiceHandler.class);
	private static String DEFAULT_FORMAT = SerializationLang.N3;

	private class RestRequest {
		
		private String guidRegex = "([A-Za-z0-9]{8}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{4}-[A-Za-z0-9]{12})"; 
		private String serviceIdRegex = guidRegex + "#?";
		private String formatRegex = "\\?format=(RDF/XML|RDF/XML-ABBREV|N-TRIPLE|N3|TTL|TURTLE)";
		private String ioFormatRegex = "\\?format=(RDF/XML|RDF/XML-ABBREV|N-TRIPLE|N3|TTL|TURTLE|SPARQL)";
		
		private String simpleUrlRegex = "/" + serviceIdRegex + "/?";
		private String urlWithFormatRegex = "/" + serviceIdRegex + formatRegex + "/?";
		private String getInputOrOutputRegex = "/" + serviceIdRegex + "/" + "(input|output)" + "/?";
		private String getInputOrOutputWithFormatRegex = "/" + serviceIdRegex + "/" + "(input|output)" + ioFormatRegex + "/?";
		
		Pattern simpleUrlPattern = Pattern.compile(simpleUrlRegex, Pattern.CASE_INSENSITIVE);
		Pattern urlWithFormatPattern = Pattern.compile(urlWithFormatRegex, Pattern.CASE_INSENSITIVE);
		Pattern getInputOrOutputPattern = Pattern.compile(getInputOrOutputRegex, Pattern.CASE_INSENSITIVE);
		Pattern getInputOrOutputWithFormatPattern = Pattern.compile(getInputOrOutputWithFormatRegex, Pattern.CASE_INSENSITIVE);
		 
		private String id;
		private String format;
		private String resource;
 
		public RestRequest(String pathInfo, String method) throws ServletException {
			// regex parse pathInfo
			
			Matcher matcher;
			if (pathInfo == null || pathInfo.trim().length() == 0)
				return;
			
			if (method == HttpMethods.GET.name()) {
				matcher = getInputOrOutputWithFormatPattern.matcher(pathInfo);
				if (matcher.find()) {
					logger.debug(getInputOrOutputWithFormatRegex);
					id = matcher.group(1);
					resource = matcher.group(2);
					format = matcher.group(3);
					return;
				}
		 
				matcher = getInputOrOutputPattern.matcher(pathInfo);
				if (matcher.find()) {
					logger.debug(getInputOrOutputRegex);
					id = matcher.group(1);
					resource = matcher.group(2);
					format = DEFAULT_FORMAT;
					return;
				}
			}

			matcher = urlWithFormatPattern.matcher(pathInfo);
			if (matcher.find()) {
				logger.debug(urlWithFormatRegex);
				id = matcher.group(1);
				format = matcher.group(2);
				resource = null;
				return;
			}
			
			matcher = simpleUrlPattern.matcher(pathInfo);
			if (matcher.find()) {
				logger.debug(simpleUrlRegex);
				id = matcher.group(1);
				format = DEFAULT_FORMAT;
				resource = null;
				return;
			}
			
			throw new ServletException("Invalid URI");
		}

		
		public String getId() {
			return id;
		}
		public String getFormat() {
			// I don't know why Jena gives error for turtle.
			if (format.equalsIgnoreCase("TTL") || format.equalsIgnoreCase("TURTLE"))
				format = DEFAULT_FORMAT;
			return format.toUpperCase();
		}
		public String getResource() {
			return resource;
		}
 
  }
	  
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		logger.debug("Request URL: " + request.getRequestURI());
		logger.debug("Request Path Info: " + request.getPathInfo());
		logger.debug("Request Param: " + request.getQueryString());

		RestRequest restRequest = null;
		try {
			String url = request.getPathInfo();
			if (request.getQueryString() != null) url += "?" + request.getQueryString();
			restRequest = new RestRequest(url , HttpMethods.GET.name());
		} catch (ServletException e) {
			response.setContentType(MimeType.TEXT_PLAIN);
			response.getWriter().write("Invalid URL!");
			return;
		}
		
		String serviceId = restRequest.getId();
		String format = restRequest.getFormat();
		String resource = restRequest.getResource();

		logger.debug("Id: " + serviceId);
		logger.debug("Format: " + format);
		logger.debug("Resource: " + resource);

		ResourceType resourceType = ResourceType.Service;
		if (resource != null && resource.trim().equalsIgnoreCase("input"))
			resourceType = ResourceType.Input;
		if (resource != null && resource.trim().equalsIgnoreCase("output"))
			resourceType = ResourceType.Output;

		String contextId = ContextParametersRegistry.getInstance().getDefault().getId();
		new GetRequestManager(serviceId, resourceType, format, response, contextId).HandleRequest();
		
		response.flushBuffer(); 
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		logger.debug("Request URL: " + request.getRequestURI());
		logger.debug("Request Path Info: " + request.getPathInfo());
		logger.debug("Request Param: " + request.getQueryString());

		RestRequest restRequest = null;
		try {
			String url = request.getPathInfo();
			if (request.getQueryString() != null) url += "?" + request.getQueryString();
			restRequest = new RestRequest(url, HttpMethods.POST.name());
		} catch (ServletException e) {
			response.setContentType(MimeType.TEXT_PLAIN);
			response.getWriter().write("Invalid URL!");
			return;
		}
		
		String serviceId = restRequest.getId();
		String format = restRequest.getFormat();

		for (Object s : request.getParameterMap().keySet())
			logger.debug(s + " --- " + request.getParameterMap().get(s).toString());
		logger.debug("Id: " + serviceId);
		logger.debug("Format: " + format);
		
		//request.setCharacterEncoding(CharEncoding.ISO_8859_1);
		logger.info("Content-Type: " + request.getContentType());
		
		String formData = null;
		String inputLang = "";
		if (request.getContentType().startsWith(MimeType.APPLICATION_RDF_XML))
			inputLang = SerializationLang.XML;
		if (request.getContentType().startsWith(MimeType.TEXT_XML))
			inputLang = SerializationLang.XML;
		else if (request.getContentType().startsWith(MimeType.APPLICATION_XML))
			inputLang = SerializationLang.XML;
		else if (request.getContentType().startsWith(MimeType.APPLICATION_RDF_N3))
			inputLang = SerializationLang.N3;
		if (request.getContentType().startsWith(MimeType.APPLICATION_FORM_URLENCODED)) {
			inputLang = SerializationLang.N3; // default for html forms
			formData = request.getParameter("rdf");
			logger.debug(formData);
		} else {
			response.setContentType(MimeType.TEXT_PLAIN);
			response.getWriter().write("The content type is neither rdf+xml nor rdf+n3");
			return;
		}
		
		InputStream in = request.getInputStream();
		
		if (formData != null) {
			in = new ByteArrayInputStream(formData.getBytes());
		} 
		String contextId = ContextParametersRegistry.getInstance().getDefault().getId();
		new PostRequestManager(serviceId, in, inputLang, format, response, contextId).HandleRequest();
		
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
