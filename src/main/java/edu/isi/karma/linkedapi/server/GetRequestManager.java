package edu.isi.karma.linkedapi.server;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

import edu.isi.karma.service.MimeType;
import edu.isi.karma.service.SerializationLang;
import edu.isi.karma.service.Service;
import edu.isi.karma.service.ServiceLoader;
import edu.isi.karma.service.ServicePublisher;


public class GetRequestManager extends LinkedApiRequestManager {

	static Logger logger = Logger.getLogger(GetRequestManager.class);

	public GetRequestManager(String serviceId, 
			ResourceType resourceType, 
			String returnType,
			HttpServletResponse response) {
		super(serviceId, resourceType, returnType, response);
	}
	
	public void HandleRequest() throws IOException {
		
		PrintWriter pw = getResponse().getWriter();
		Model m = null;
		
		if (getResourceType() == ResourceType.Service) {
			m = ServiceLoader.getServiceJenaModelByUri(getServiceUri());
			if (m == null) {
				getResponse().setContentType(MimeType.TEXT_PLAIN);
				pw.write("Could not find the service " + getServiceId() + " in service repository");
				return;
			}
		}
		
		if (getResourceType() == ResourceType.Input || getResourceType() == ResourceType.Output) {
			Service s = ServiceLoader.getServiceByUri(getServiceUri());
			if (s == null) {
				getResponse().setContentType(MimeType.TEXT_PLAIN);
				pw.write("Could not find the service " + getServiceId() + " in service repository");
				return;
			}
			
			edu.isi.karma.service.Model inputModel = s.getInputModel();
			edu.isi.karma.service.Model outputModel = s.getOutputModel();
			String sparql;
			
			if (getResourceType() == ResourceType.Input) {
				if (getFormat().equalsIgnoreCase(SerializationLang.SPARQL)) {
					sparql = inputModel.getSparqlConstructQuery(null);
					getResponse().setContentType(MimeType.TEXT_PLAIN);
					pw.write(sparql);
					return;
				} else
					m = ServicePublisher.generateInputPart(s);
			}

			if (getResourceType() == ResourceType.Output) {
				if (getFormat().equalsIgnoreCase(SerializationLang.SPARQL)) {
					sparql = outputModel.getSparqlConstructQuery(null);
					getResponse().setContentType(MimeType.TEXT_PLAIN);
					pw.write(sparql);
					return;
				} else
					m = ServicePublisher.generateOutputPart(s);;
			}
		}
		
		if (getFormat().equalsIgnoreCase(SerializationLang.XML))
			getResponse().setContentType(MimeType.APPLICATION_RDF_XML); 
		else if (getFormat().equalsIgnoreCase(SerializationLang.XML_ABBREV))
			getResponse().setContentType(MimeType.APPLICATION_RDF_XML); 
		else
			getResponse().setContentType(MimeType.TEXT_PLAIN);

		m.write(pw, getFormat());
	}
}
