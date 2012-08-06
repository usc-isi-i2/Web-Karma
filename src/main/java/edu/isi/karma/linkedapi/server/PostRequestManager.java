package edu.isi.karma.linkedapi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.isi.karma.service.Attribute;
import edu.isi.karma.service.MimeType;
import edu.isi.karma.service.Service;
import edu.isi.karma.service.ServiceLoader;


public class PostRequestManager extends LinkedApiRequestManager {

	static Logger logger = Logger.getLogger(PostRequestManager.class);

	private InputStream inputStream;
	private String inputLang;
	private Model inputJenaModel;
	private Service service;
	private List<Map<String, String>> listOfAttValues;
	
	public PostRequestManager(String serviceId, 
			InputStream inputStream,
			String inputLang,
			String returnType,
			HttpServletResponse response) throws IOException {
		super(serviceId, null, returnType, response);
		this.inputStream = inputStream;
		this.inputLang = inputLang;
		this.inputJenaModel = ModelFactory.createDefaultModel();
	}
	
	/**
	 * checks whether the input has correct RDf syntax or not
	 * @return
	 */
	private boolean validateInputSyntax() {
		try {
			this.inputJenaModel.read(this.inputStream, null, this.inputLang);
		} catch (Exception e) {
			logger.error("Exception in creating the jena model from the input data.");
			return false;
		}
		if (this.inputJenaModel == null) {
			logger.error("Could not create a jena model from the input data.");
			return false;
		}
		return true;
	}
	
	private boolean loadService() {
		service = ServiceLoader.getServiceByUri(getServiceUri());
		if (service == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * checks if the input data satisfies the service input graph
	 * (if the service input contained in the input data or not)
	 * @return
	 * @throws IOException 
	 */
	private boolean validateInputSemantic() throws IOException {
		
		PrintWriter pw = getResponse().getWriter();

		edu.isi.karma.service.Model serviceInputModel = service.getInputModel();
		if (serviceInputModel == null) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("The service input model is null.");
			return false;
		}
		
		listOfAttValues = serviceInputModel.findModelDataInJenaData(this.inputJenaModel, null);
		
		if (listOfAttValues == null)
			return false;
		
		for (Map<String, String> m : listOfAttValues)
			for (String s : m.keySet())
				System.out.println(s + "-->" + m.get(s));
		
		//for (String s : serviceIdsAndMappings.)
		return true;
	}
	
	private List<String> getUrlStrings(Service service, List<Map<String, String>> attValueList) {
		
		List<String> urls = new ArrayList<String>();
		
		List<Attribute> missingAttributes= null;
		
		for (Map<String, String> attValues : attValueList) {
			
			missingAttributes = new ArrayList<Attribute>();
			String url = service.getPopulatedAddress(attValues, missingAttributes);
			urls.add(url);
			
			System.out.println(url);
			
			for (Attribute att : missingAttributes)
				System.out.println("missing: " + att.getName() + ", grounded in:" + att.getGroundedIn());
		}
		
		return urls;
	}
	
	private void invokeWebAPI(List<String> invocationUrls) {
		return;
	}
	
	public void HandleRequest() throws IOException {
		
		// printing the input data (just fo debug)
//		InputStreamReader is = new InputStreamReader(inputStream);
//		BufferedReader br = new BufferedReader(is);
//		String read = br.readLine();
//		while(read != null) {
//		    System.out.println(read);
//		    read = br.readLine();
//		}
		
		PrintWriter pw = getResponse().getWriter();
		
		if (!validateInputSyntax()) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("Could not validate the syntax of input RDF.");
			return;
		}
		
		if (!loadService()) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("Could not find the service " + getServiceId() + " in service repository");
			return;
		}
		
		if (!validateInputSemantic()) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("The input RDF does not have a matching pattern for service input model. ");
			return;
		}

		List<String> invocationURLs = getUrlStrings(service, listOfAttValues);
		invokeWebAPI(invocationURLs);
		
		getResponse().setContentType(MimeType.TEXT_PLAIN);
		pw.write("Success.");
		return;
	}
	
	
}
