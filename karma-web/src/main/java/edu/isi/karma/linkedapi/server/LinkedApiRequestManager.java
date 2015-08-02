package edu.isi.karma.linkedapi.server;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import edu.isi.karma.config.ModelingConfiguration;
import edu.isi.karma.config.ModelingConfigurationRegistry;
import edu.isi.karma.model.serialization.SerializationLang;

public class LinkedApiRequestManager {

	private ResourceType resourceType = ResourceType.Service;
	private String format = SerializationLang.XML;
	private String serviceId;
	private String serviceUri;
	private HttpServletResponse response;
	protected String contextId;
	public LinkedApiRequestManager(String serviceId, 
			ResourceType resourceType, 
			String returnType,
			HttpServletResponse response,
			String contextId) {
		this.serviceId = serviceId;
		this.contextId = contextId;
		ModelingConfiguration modelingConfiguration = ModelingConfigurationRegistry.getInstance().getModelingConfiguration(contextId);
		this.serviceUri = modelingConfiguration.getKarmaServicePrefix() + serviceId + (!serviceId.endsWith("#")?"#":""); 
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
