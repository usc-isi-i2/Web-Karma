package edu.isi.karma.linkedapi.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import edu.isi.karma.service.MimeType;


public class PostRequestManager extends LinkedApiRequestManager {

	static Logger logger = Logger.getLogger(PostRequestManager.class);

	private InputStream inputStream;
	private String inputLang;
	private Model inputJenaModel;
	
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
			this.inputJenaModel.read(this.inputStream, null, inputLang);
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
	
	/**
	 * checks if the input data matches with the service input graph
	 * @return
	 */
	private boolean validateInputSemantic() {
		// TODO
		return true;
	}
	
	public void HandleRequest() throws IOException {
		
		// printing the input data (just fo debug)
		InputStreamReader is = new InputStreamReader(inputStream);
		BufferedReader br = new BufferedReader(is);
		String read = br.readLine();
		while(read != null) {
		    System.out.println(read);
		    read = br.readLine();
		}
		
		PrintWriter pw = getResponse().getWriter();
		
		if (!validateInputSyntax()) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("Could not validate the syntax of input RDF.");
			return;
		}
		
		if (!validateInputSemantic()) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("The input RDF does not have a matching pattern for service input model. ");
			return;
		}

		getResponse().setContentType(MimeType.TEXT_PLAIN);
		pw.write("Success.");
		return;
	}
	
	
}
