package edu.isi.karma.linkedapi.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.model.serialization.MimeType;
import edu.isi.karma.model.serialization.SerializationLang;
import edu.isi.karma.model.serialization.WebServiceLoader;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.rep.model.Atom;
import edu.isi.karma.rep.model.ClassAtom;
import edu.isi.karma.rep.model.IndividualPropertyAtom;
import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.InvocationManager;
import edu.isi.karma.rep.sources.Table;
import edu.isi.karma.rep.sources.WebService;
import edu.isi.karma.webserver.KarmaException;


public class PostRequestManager extends LinkedApiRequestManager {

	static Logger logger = LoggerFactory.getLogger(PostRequestManager.class);

	private InputStream inputStream;
	private String inputLang;
	private Model inputJenaModel;
	private Model outputJenaModel;
	private WebService service;
	private List<Map<String, String>> listOfInputAttValues;
	
	public PostRequestManager(String serviceId, 
			InputStream inputStream,
			String inputLang,
			String returnType,
			HttpServletResponse response,
			String contextId
			) throws IOException {
		super(serviceId, null, returnType, response, contextId);
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
		service = WebServiceLoader.getInstance().getSourceByUri(getServiceUri());
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

		edu.isi.karma.rep.model.Model serviceInputModel = service.getInputModel();
		if (serviceInputModel == null) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("The service input model is null.");
			return false;
		}
		
		listOfInputAttValues = serviceInputModel.findModelDataInJenaData(this.inputJenaModel, null);
		
		if (listOfInputAttValues == null)
			return false;
		
		for (Map<String, String> m : listOfInputAttValues)
			for (Map.Entry<String, String> stringStringEntry : m.entrySet())
				logger.debug(stringStringEntry.getKey() + "-->" + stringStringEntry.getValue());
		
		//for (String s : serviceIdsAndMappings.)
		return true;
	}
	
	private String getUrlString(WebService service, Map<String, String> inputAttValues) {
		
		List<Attribute> missingAttributes= null;
		
		missingAttributes = new ArrayList<>();
		String url = service.getPopulatedAddress(inputAttValues, missingAttributes);
		
		//FIXME: Authentication Data
		url = url.replaceAll("\\{p3\\}", "karma");
		
		logger.debug(url);
		
		for (Attribute att : missingAttributes)
			logger.debug("missing: " + att.getName() + ", grounded in:" + att.getGroundedIn());

		return url;
	}
	
	private Table invokeWebAPI(String requestURLString) {
		
		InvocationManager invocatioManager;
		try {
			invocatioManager = new InvocationManager(null, requestURLString);
			logger.info("Requesting data with includeURL=" + true + ",includeInput=" + true + ",includeOutput=" + true);
			Table serviceTable = invocatioManager.getServiceData(false, false, true);
			logger.debug(serviceTable.getPrintInfo());
			logger.info("The service " + service.getUri() + " has been invoked successfully.");
			return serviceTable;

		} catch (MalformedURLException e) {
			logger.error("Malformed service request URL.");
			return null;
		} catch (KarmaException e) {
			logger.error(e.getMessage());
			return null;
		}

	}
	
	public void addStatementsToJenaModel(WebService service, Model model,  
			Map<String, String> inputAttValues, Map<String, String> outputAttValues) {
		edu.isi.karma.rep.model.Model outputModel = service.getOutputModel();
		if (outputModel == null) { 
			logger.info("The service output model is null.");
			return;
		}
		
		Resource r;
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");

		Map<String, Resource> outputVariablesToResource = new HashMap<>();
		String predicateUri = "";
		String argument1 = "";
		String argument2 = "";

		for (Atom atom : outputModel.getAtoms()) {
			if (atom instanceof ClassAtom) {
				ClassAtom classAtom = (ClassAtom)atom;
				
				// creating a blank node
				r = model.createResource();
				if (classAtom.getClassPredicate().getPrefix() != null && classAtom.getClassPredicate().getNs() != null)
					model.setNsPrefix(classAtom.getClassPredicate().getPrefix(), classAtom.getClassPredicate().getNs());
				predicateUri = classAtom.getClassPredicate().getUri();
				
				// creating the class resource
				Resource classResource = model.getResource(predicateUri);
				if (classResource == null)
					classResource = model.createResource(predicateUri);

				argument1 = classAtom.getArgument1().getId();
				outputVariablesToResource.put(argument1, r);

				r.addProperty(rdf_type, classResource);
			}
		}
		
		for (Atom atom : outputModel.getAtoms()) {
			if (atom instanceof IndividualPropertyAtom) {
				IndividualPropertyAtom propertyAtom = (IndividualPropertyAtom)atom;
				
				if (propertyAtom.getPropertyPredicate().getPrefix() != null && propertyAtom.getPropertyPredicate().getNs() != null)
					model.setNsPrefix(propertyAtom.getPropertyPredicate().getPrefix(), propertyAtom.getPropertyPredicate().getNs());
				predicateUri = propertyAtom.getPropertyPredicate().getUri();

				// creating the property resource
				Property propertyResource = model.getProperty(predicateUri);
				if (propertyResource == null)
					propertyResource = model.createProperty(predicateUri);

				argument1 = propertyAtom.getArgument1().getId();
				argument2 = propertyAtom.getArgument2().getId();
				
				Resource subjectResource = outputVariablesToResource.get(argument1);
				// maybe this variable is defined in input rdf
				if (subjectResource == null) {
					String subjectUri = inputAttValues.get(argument1);
					if (subjectUri != null) {
						subjectResource = model.getResource(subjectUri);
					}
				}

				if (subjectResource == null) {
					logger.error("Could not find the corresponding resource of " + argument1 + " variable.");
					continue;
				}
				
				String attValue = outputAttValues.get(argument2);
				// the object of this predicate is a literal
				if (attValue != null) {
					subjectResource.addProperty(propertyResource, attValue);
				} else { // object is a resource
					Resource objectResource = outputVariablesToResource.get(argument2);
					if (objectResource == null) { // this is not a variable created by output model, but it might exist in input model
						String objectUri = inputAttValues.get(argument2);
						if (objectUri != null) {
							objectResource = model.getResource(objectUri);
						}
					}

					if (objectResource == null) {
						logger.error("Could not find the corresponding resource of " + argument2 + " variable.");
						continue;
					}
					
					subjectResource.addProperty(propertyResource, objectResource);
				}
					
			}
		}
		
	}
	
	public void HandleRequest() throws IOException {
		
		// printing the input data (just fo debug)
//		InputStreamReader is = new InputStreamReader(inputStream);
//		BufferedReader br = new BufferedReader(is);
//		String read = br.readLine();
//		System.out.println("START");
//		while(read != null) {
//		    System.out.println(read);
//		    read = br.readLine();
//		}
//		System.out.println("END");
		
		boolean blankInput = false;
		
		PrintWriter pw = getResponse().getWriter();
		
		if (!loadService()) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("Could not find the service " + getServiceId() + " in service repository");
			return;
		}

		if (this.service.getInputAttributes() == null ||
				this.service.getInputAttributes().isEmpty()) {
			blankInput = true;
		} else {
		
			if (!validateInputSyntax()) {
				getResponse().setContentType(MimeType.TEXT_PLAIN);
				pw.write("Could not validate the syntax of input RDF.");
				return;
			}
			
			if (!blankInput && !validateInputSemantic()) {
				getResponse().setContentType(MimeType.TEXT_PLAIN);
				pw.write("The input RDF does not have a matching pattern for service input model. ");
				return;
			}
		}

		// including the statements of the input model in the output model
		this.outputJenaModel = ModelFactory.createDefaultModel();
		if (this.inputJenaModel != null) {
			this.outputJenaModel.add(this.inputJenaModel);
			this.outputJenaModel.setNsPrefixes(this.inputJenaModel.getNsPrefixMap());
		}
		
		Map<String, String> outputAttNameToAttIds = new HashMap<>();
		Map<String, String> outputAttValues = new HashMap<>();
		
		if (blankInput) { // service without input parameter
			String invocationURL = getUrlString(service, new HashMap<String, String>());
			Table table = invokeWebAPI(invocationURL);

			if (table == null || table.getHeaders() == null) {
				logger.info("Error in invoking " + invocationURL);
			} else {
			
				// creating a mapping from attribute names (table headers) to attribute Ids
				outputAttNameToAttIds.clear();
				for (Attribute header : table.getHeaders()) {
					String name = header.getName();
					Attribute serviceAtt = service.getOutputAttributeByName(name);
					if (serviceAtt == null) {
						logger.info("Could not find the attribute " + name + " in service output attributes.");
						continue;
					}
					outputAttNameToAttIds.put(name, serviceAtt.getId());
				}
				
				// iterating over the rows to create the output RDF
				outputAttValues.clear();
				for (List<String> values : table.getValues()) {
					for (int i = 0; i < table.getColumnsCount(); i++) {
						String attId = outputAttNameToAttIds.get(table.getHeaders().get(i).getName());
						if (attId == null) continue;
						String value = values.get(i);
						outputAttValues.put(attId, value);
					}
					addStatementsToJenaModel(this.service, this.outputJenaModel, new HashMap<String, String>(), outputAttValues);
				}
			}
		} else if (listOfInputAttValues == null) {
			getResponse().setContentType(MimeType.TEXT_PLAIN);
			pw.write("Cannot extract the input values from the service input model. ");
			return;
		} else {
			for (Map<String,String> inputAttValues : listOfInputAttValues) {
				
				// invoking the Web API and load the response in a table
				String invocationURL = getUrlString(service, inputAttValues);
				Table table = invokeWebAPI(invocationURL);
	
				if (table == null || table.getHeaders() == null) {
					logger.info("Error in invoking " + invocationURL);
					continue;
				}
				
				// creating a mapping from attribute names (table headers) to attribute Ids
				outputAttNameToAttIds.clear();
				for (Attribute header : table.getHeaders()) {
					String name = header.getName();
					Attribute serviceAtt = service.getOutputAttributeByName(name);
					if (serviceAtt == null) {
						logger.info("Could not find the attribute " + name + " in service output attributes.");
						continue;
					}
					outputAttNameToAttIds.put(name, serviceAtt.getId());
				}
				
				// iterating over the rows to create the output RDF
				outputAttValues.clear();
				for (List<String> values : table.getValues()) {
					for (int i = 0; i < table.getColumnsCount(); i++) {
						String attId = outputAttNameToAttIds.get(table.getHeaders().get(i).getName());
						if (attId == null) continue;
						String value = values.get(i);
						outputAttValues.put(attId, value);
					}
					addStatementsToJenaModel(this.service, this.outputJenaModel, inputAttValues, outputAttValues);
				}
			}
		}
		
		if (getFormat().equalsIgnoreCase(SerializationLang.XML))
			getResponse().setContentType(MimeType.APPLICATION_XML); 
		else if (getFormat().equalsIgnoreCase(SerializationLang.XML_ABBREV))
			getResponse().setContentType(MimeType.APPLICATION_XML); 
		else
			getResponse().setContentType(MimeType.TEXT_PLAIN);

//		getResponse().setContentType(MimeType.TEXT_PLAIN);
//		pw.write("Success.");

		getResponse().setContentType(MimeType.TEXT_PLAIN);
		this.outputJenaModel.write(pw, getFormat());
	}
	
	
}
