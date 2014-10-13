package edu.isi.karma.linkedapi.server;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.model.serialization.SerializationLang;

import javax.servlet.http.HttpServletResponse;

import java.io.PrintWriter;

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
		this.serviceUri = ModelingConfiguration.getKarmaServicePrefix() + serviceId + (!serviceId.endsWith("#")?"#":""); 
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
