package edu.isi.karma.linkedapi.server;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.service.SerializationLang;

public class LinkedApiRequestManager {

	private ResourceType resourceType = ResourceType.Service;
	private String format = SerializationLang.XML;
	private String serviceId;
	private String serviceUri;
	private HttpServletResponse response;
	
	public LinkedApiRequestManager(String serviceId, 
			ResourceType resourceType, 
			String returnType,
			HttpServletResponse response) {
		this.serviceId = serviceId;
		this.serviceUri = ModelingParams.KARMA_SERVICE_PREFIX + serviceId + (!serviceId.endsWith("#")?"#":""); 
		this.resourceType = resourceType;
		this.format = returnType;
		this.response = response;
	}

	protected ResourceType getResourceType() {
		return resourceType;
	}


	protected String getFormat() {
		return format;
	}


	protected String getServiceId() {
		return serviceId;
	}

	protected String getServiceUri() {
		return serviceUri;
	}

	protected HttpServletResponse getResponse() {
		return response;
	}

	public static void getServiceDescripton(String serviceID, String responseLang, PrintWriter pw) {
		

	}
}
