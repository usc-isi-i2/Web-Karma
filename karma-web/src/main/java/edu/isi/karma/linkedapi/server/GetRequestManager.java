package edu.isi.karma.linkedapi.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.isi.karma.model.serialization.MimeType;
import edu.isi.karma.model.serialization.SerializationLang;
import edu.isi.karma.model.serialization.WebServiceLoader;
import edu.isi.karma.model.serialization.WebServicePublisher;
import edu.isi.karma.rep.sources.WebService;


public class GetRequestManager extends LinkedApiRequestManager {

	static Logger logger = LoggerFactory.getLogger(GetRequestManager.class);

	public GetRequestManager(String serviceId, 
			ResourceType resourceType, 
			String returnType,
			HttpServletResponse response,
			String contextId) {
		super(serviceId, resourceType, returnType, response,contextId);
	}
	
	public void HandleRequest() throws IOException {
		
		PrintWriter pw = getResponse().getWriter();
		Model m = null;
		
		if (getResourceType() == ResourceType.Service) {
			m = WebServiceLoader.getInstance().getSourceJenaModel(getServiceUri());
			if (m == null) {
				getResponse().setContentType(MimeType.TEXT_PLAIN);
				pw.write("Could not find the service " + getServiceId() + " in service repository");
				return;
			}
		}
		
		if (getResourceType() == ResourceType.Input || getResourceType() == ResourceType.Output) {
			WebService s = WebServiceLoader.getInstance().getSourceByUri(getServiceUri());
			if (s == null) {
				getResponse().setContentType(MimeType.TEXT_PLAIN);
				pw.write("Could not find the service " + getServiceId() + " in service repository");
				return;
			}
			
			edu.isi.karma.rep.model.Model inputModel = s.getInputModel();
			edu.isi.karma.rep.model.Model outputModel = s.getOutputModel();
			String sparql;
			
			WebServicePublisher servicePublisher = new WebServicePublisher(s, contextId);
			if (getResourceType() == ResourceType.Input) {
				if (getFormat().equalsIgnoreCase(SerializationLang.SPARQL)) {
					sparql = inputModel.getSparqlConstructQuery(null);
					getResponse().setContentType(MimeType.TEXT_PLAIN);
					pw.write(sparql);
					return;
				} else
					m = servicePublisher.generateInputPart();
			}

			if (getResourceType() == ResourceType.Output) {
				if (getFormat().equalsIgnoreCase(SerializationLang.SPARQL)) {
					sparql = outputModel.getSparqlConstructQuery(null);
					getResponse().setContentType(MimeType.TEXT_PLAIN);
					pw.write(sparql);
					return;
				} else
					m = servicePublisher.generateOutputPart();;
			}
		}
		
		if (getFormat().equalsIgnoreCase(SerializationLang.XML))
			getResponse().setContentType(MimeType.APPLICATION_XML); 
		else if (getFormat().equalsIgnoreCase(SerializationLang.XML_ABBREV))
			getResponse().setContentType(MimeType.APPLICATION_XML); 
		else
			getResponse().setContentType(MimeType.TEXT_PLAIN);

		m.write(pw, getFormat());
	}
}
