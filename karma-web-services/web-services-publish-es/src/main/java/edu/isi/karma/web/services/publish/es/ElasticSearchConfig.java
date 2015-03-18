package edu.isi.karma.web.services.publish.es;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MultivaluedMap;

public class ElasticSearchConfig {
	private String hostname;
	private String port;
	private String protocol;
	private String index;
	private String type;
	
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public String getPort() {
		return port;
	}
	public void setPort(String port) {
		this.port = port;
	}
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {
		return protocol + "://" + hostname  +":" + port + "/" + index + ", " + type;
	}
	
	public static ElasticSearchConfig parse(ServletContext context, MultivaluedMap<String, String> formParams) {
		ElasticSearchConfig config = new ElasticSearchConfig();
		if (formParams != null && formParams.containsKey(FormParameters.ES_HOSTNAME)) {
			config.setHostname(formParams.getFirst(FormParameters.ES_HOSTNAME));
		} else {
			config.setHostname(context.getInitParameter(FormParameters.ES_HOSTNAME));
		}
		
		if(formParams != null && formParams.containsKey(FormParameters.ES_PROTOCOL)) {
			config.setProtocol(formParams.getFirst(FormParameters.ES_PROTOCOL));
		} else {
			config.setProtocol(context.getInitParameter(FormParameters.ES_PROTOCOL));
		}
		
		if(formParams != null && formParams.containsKey(FormParameters.ES_INDEX)) {
			config.setIndex(formParams.getFirst(FormParameters.ES_INDEX));
		} else {
			config.setIndex(context.getInitParameter(FormParameters.ES_INDEX));
		}
		
		if(formParams != null && formParams.containsKey(FormParameters.ES_TYPE)) {
			config.setType(formParams.getFirst(FormParameters.ES_TYPE));
		} else {
			config.setType(context.getInitParameter(FormParameters.ES_TYPE));
		}
		
		if(formParams != null && formParams.containsKey(FormParameters.ES_PORT)) {
			config.setPort(formParams.getFirst(FormParameters.ES_PORT));
		} else {
			config.setPort(context.getInitParameter(FormParameters.ES_PORT));
		}
		
		System.out.println("ES Config:" + config);
		return config;
	}
}
