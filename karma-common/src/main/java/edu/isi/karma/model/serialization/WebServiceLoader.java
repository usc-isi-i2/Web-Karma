/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/

package edu.isi.karma.model.serialization;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;
import edu.isi.karma.rep.alignment.Label;
import edu.isi.karma.rep.model.Argument;
import edu.isi.karma.rep.model.ArgumentType;
import edu.isi.karma.rep.model.Atom;
import edu.isi.karma.rep.model.ClassAtom;
import edu.isi.karma.rep.model.IndividualPropertyAtom;
import edu.isi.karma.rep.sources.Attribute;
import edu.isi.karma.rep.sources.AttributeRequirement;
import edu.isi.karma.rep.sources.IOType;
import edu.isi.karma.rep.sources.Source;
import edu.isi.karma.rep.sources.WebService;
import edu.isi.karma.webserver.ContextParametersRegistry;
import edu.isi.karma.webserver.ServletContextParameterMap;
import edu.isi.karma.webserver.ServletContextParameterMap.ContextParameter;

public class WebServiceLoader extends SourceLoader
{

	private static Logger logger = LoggerFactory.getLogger(WebServiceLoader.class);
	private static WebServiceLoader instance = null;
	private final int DEFAULT_SERVICE_RESULTS_SIZE = 10;

	protected WebServiceLoader() {
		      // Exists only to defeat instantiation.
	}
		   
	//TODO make this not static!
	public static WebServiceLoader getInstance() {
		if (instance == null) {
			instance = new WebServiceLoader();
		}
		return instance;
	}
	
    @Override
	public WebService getSourceByUri(String uri) {
		
		Model m = Repository.Instance().getNamedModel(uri);
		if (m == null)
			return null;

		WebService service = importSourceFromJenaModel(m);
		return service;
	}
	
    @Override
	public void deleteSourceByUri(String uri) {
		Repository.Instance().clearNamedModel(uri);
		
		String service_id = uri.substring(uri.lastIndexOf("/") + 1, uri.length() - 1);
		//TODO this is not the right way to get the context parameters
		ServletContextParameterMap contextParameters = ContextParametersRegistry.getInstance().getDefault();
		String dir = contextParameters.getParameterValue(ContextParameter.USER_DIRECTORY_PATH) + Repository.Instance().SERVICE_REPOSITORY_REL_DIR;
		String fileName = service_id + Repository.Instance().getFileExtension(Repository.Instance().LANG);
		File f = new File(dir + fileName);
		
		try {
		if (f.exists()) {
			if (!f.delete())
				logger.debug("The file " + fileName + " cannot be deleted from " + dir);
			else
				logger.debug("The file " + fileName + " has been deleted from " + dir);
		} else
			logger.debug("The file " + fileName + " does not exist in " + dir);
		} catch (Throwable t) {
			logger.debug("cannot delete the file " + fileName + " from " + dir + " because " + t.getMessage());
		}
	}
	
	/**
	 * returns the service id, name, address of all services in the repository
	 * @param serviceLimit: maximum number of results, null value means all the services
	 * @return
	 */
    @Override
	public List<Source> getSourcesAbstractInfo(Integer serviceLimit) {
		
		List<Source> serviceList = new ArrayList<>();
		
		Model model = Repository.Instance().getModel();
		
		String service_id;
		String service_name = "";
		String service_address = "";
		
		// Create a new query
		String queryString =
			"PREFIX " + Prefixes.KARMA + ": <" + Namespaces.KARMA + "> \n" +
			"PREFIX " + Prefixes.HRESTS + ": <" + Namespaces.HRESTS + "> \n" +
			"SELECT ?s ?name ?address \n" +
			"WHERE { \n" +
			"      ?s a " + Prefixes.KARMA + ":Service . \n" +
			"      OPTIONAL {?s " + Prefixes.HRESTS + ":hasAddress ?address .} \n" +
			"      OPTIONAL {?s " + Prefixes.KARMA + ":hasName ?name .} \n" +
			"      } \n";
		
		if (serviceLimit != null) {
			if (serviceLimit.intValue() < 0) serviceLimit = DEFAULT_SERVICE_RESULTS_SIZE;
			queryString += "LIMIT " + String.valueOf(serviceLimit.intValue() + "\n");
		}
		
		logger.debug("query= \n" + queryString);
		
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			ResultSet results = qexec.execSelect() ;
			
			if (!results.hasNext())
				logger.info("query does not return any answer.");

//			ResultSetFormatter.out(System.out, results, query) ;
			 
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				
				RDFNode s = soln.get("s") ;       // Get a result variable by name.
				RDFNode name = soln.get("name") ;       // Get a result variable by name.
				RDFNode address = soln.get("address") ;       // Get a result variable by name.

				if (s == null) {
					logger.info("service id is null.");
					continue;
				}

				String service_uri = s.toString();
				service_id = service_uri.substring(service_uri.lastIndexOf("/") + 1, service_uri.length() - 1);

				logger.debug("service uri: " + service_uri);
				logger.debug("service id: " + service_id);
				if (name != null && name.isLiteral()) service_name = name.asLiteral().getString();
				logger.debug("service name: " + service_name);
				if (address != null && address.isLiteral()) service_address = address.asLiteral().getString();
				logger.debug("service address: " + service_address);
				
				if (service_id.trim().length() > 0)
					serviceList.add(new WebService(service_id, service_name, service_address));
				else
					logger.info("length of service id is zero.");
			}
			
			return serviceList;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		} finally { 
			qexec.close() ; 
		}
	}
	
	/**
	 * returns all the services in the repository along with their complete information
	 * probably this method is not useful since fetching all the services with their complete
	 * information takes long time.
	 * @return
	 */
    @Override
	public List<Source> getSourcesDetailedInfo(Integer serviceLimit) {

		List<Source> serviceList = getSourcesAbstractInfo(serviceLimit);
		List<Source> serviceListCompleteInfo = new ArrayList<>();
		for (Source s : serviceList) {
			serviceListCompleteInfo.add(getSourceByUri(s.getUri()));
		}
		return serviceListCompleteInfo;
	}
    
	public WebService getServiceByAddress(String address) {
		
		String uri = getServiceUriByServiceAddress(address);
		Model m = Repository.Instance().getNamedModel(uri);
		if (m == null)
			return null;

		WebService service = importSourceFromJenaModel(m);
		return service;
	}
	
	public void deleteServiceByAddress(String address) {
		
		String uri = getServiceUriByServiceAddress(address);
		Repository.Instance().clearNamedModel(uri);
	}
	
	private String getServiceUriByServiceAddress(String address) {

		Model model = Repository.Instance().getModel();
		
		// Create a new query
		String queryString =
			"PREFIX " + Prefixes.KARMA + ": <" + Namespaces.KARMA + "> \n" +
			"PREFIX " + Prefixes.HRESTS + ": <" + Namespaces.HRESTS + "> \n" +
			"SELECT ?s \n" +
			"WHERE { \n" +
			"      ?s a " + Prefixes.KARMA + ":Service . \n" +
			"      ?s " + Prefixes.HRESTS + ":hasAddress \"" + address + "\"^^hrests:URITemplate . \n" +
			"      } \n";
		
		logger.debug(queryString);
		
		Query query = QueryFactory.create(queryString);
		// Execute the query and obtain results
		QueryExecution qexec = QueryExecutionFactory.create(query, model);

		try {
			ResultSet results = qexec.execSelect() ;
			
			if (!results.hasNext())
				logger.info("query does not return any answer.");

//			ResultSetFormatter.out(System.out, results, query) ;
			 
			String serviceURI = "";
			for ( ; results.hasNext() ; )
			{
				QuerySolution soln = results.nextSolution() ;
				RDFNode x = soln.get("s") ;       // Get a result variable by name.
				serviceURI = x.toString();
				logger.info("Service with id " + x.toString() + " has been found with the address " + address);
				break;
			}
			
			return serviceURI;
		} catch (Exception e) {
			logger.info("Exception in finding a service with the address " + address + " in service repository.");
			return null;
		} finally { 
			qexec.close() ; 
		}

	}
	
	/**
	 * Searches the repository to find the services that the semantic model parameter is contained in 
	 * their input model.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @return a hashmap of all found services and a mapping from the found service parameters to the model parameters.
	 * This help us later to how to join the model's corresponding source and the matched service 
	 */
	public Map<WebService, Map<String, String>> getServicesByInputPattern(edu.isi.karma.rep.model.Model semanticModel, 
			Integer serviceLimit) {
		
		if (semanticModel == null || semanticModel.getAtoms() == null 
				|| semanticModel.getAtoms().isEmpty()) {
			logger.info("The input model is nul or it does not have any atom");
			return null;
		}
		
		Map<WebService, Map<String, String>> servicesAndMappings =
				new HashMap<>();
		
		Map<String, Map<String, String>> serviceIdsAndMappings = 
			semanticModel.findInServiceInputs(Repository.Instance().getModel(), serviceLimit);

		if (serviceIdsAndMappings == null)
			return null;
		
		for (Map.Entry<String, Map<String, String>> stringMapEntry : serviceIdsAndMappings.entrySet()) {
			Model m = Repository.Instance().getNamedModel(stringMapEntry.getKey());
			if (m != null)
				servicesAndMappings.put(importSourceFromJenaModel(m), stringMapEntry.getValue());
		}
		
		return servicesAndMappings;
	}
	
	/**
	 * Searches the repository to find the services that the semantic model parameter is contained in 
	 * their output model.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @return a hashmap of all found services and a mapping from the found service parameters to the model parameters.
	 * This help us later to how to join the model's corresponding source and the matched service 
	 */
	public Map<WebService, Map<String, String>> getServicesByOutputPattern(edu.isi.karma.rep.model.Model semanticModel, 
			Integer serviceLimit) {
		
		if (semanticModel == null || semanticModel.getAtoms() == null 
				|| semanticModel.getAtoms().isEmpty()) {
			logger.info("The input model is nul or it does not have any atom");
			return null;
		}
		
		Map<WebService, Map<String, String>> servicesAndMappings =
				new HashMap<>();
		
		Map<String, Map<String, String>> serviceIdsAndMappings = 
			semanticModel.findInServiceOutputs(Repository.Instance().getModel(), serviceLimit);
		
		if (serviceIdsAndMappings == null)
			return null;
		
		for (Map.Entry<String, Map<String, String>> stringMapEntry : serviceIdsAndMappings.entrySet()) {
			Model m = Repository.Instance().getNamedModel(stringMapEntry.getKey());
			if (m != null)
				servicesAndMappings.put(importSourceFromJenaModel(m), stringMapEntry.getValue());
		}
		
		return servicesAndMappings;
	}
	
	/**
	 * Searches the repository to find the services whose input model is contained in the semantic model parameter.
	 * Note that the services in the return list only include the operations that match the model parameter.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @return a hashmap of all found services and a mapping from the found service parameters to the model parameters.
	 * This help us later to how to join the model's corresponding source and the matched service 
	 */
	public Map<WebService, Map<String, String>> getServicesWithInputContainedInModel(
			edu.isi.karma.rep.model.Model semanticModel, 
			Integer serviceLimit) {
		
		List<Source> serviceList = getSourcesDetailedInfo(serviceLimit);
		
		Map<WebService, Map<String, String>> servicesAndMappings =
				new HashMap<>();

		Model jenaModel = semanticModel.getJenaModel();
		for (Source service : serviceList) {
			
			if (!(service instanceof WebService))
				continue;
			
			edu.isi.karma.rep.model.Model m = ((WebService)service).getInputModel();
			
			if (m == null)
				continue;
			
			Map<String, Map<String, String>> serviceIdsAndMappings =
				m.findInJenaModel(jenaModel, null);
			
			if (serviceIdsAndMappings == null)
				continue;
			
			Iterator<String> itr = serviceIdsAndMappings.keySet().iterator();
			if (itr.hasNext()) {
				String key = itr.next();
				servicesAndMappings.put((WebService)service, serviceIdsAndMappings.get(key));
			}
		}
		
		return servicesAndMappings;
	}
	
	/**
	 * Searches the repository to find the services whose output model is contained in the semantic model parameter.
	 * Note that the services in the return list only include the operations that match the model parameter.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @return a hashmap of all found services and a mapping from the found service parameters to the model parameters.
	 * This help us later to how to join the model's corresponding source and the matched service 
	 */
	public Map<WebService, Map<String, String>> getServicesWithOutputContainedInModel(
			edu.isi.karma.rep.model.Model semanticModel, 
			Integer serviceLimit) {
		
		List<Source> serviceList = getSourcesDetailedInfo(serviceLimit);
		
		Map<WebService, Map<String, String>> servicesAndMappings =
				new HashMap<>();

		Model jenaModel = semanticModel.getJenaModel();
		for (Source service : serviceList) {
			
			if (!(service instanceof WebService))
				continue;

			edu.isi.karma.rep.model.Model m = ((WebService)service).getOutputModel();
			
			if (m == null)
				continue;
			
			Map<String, Map<String, String>> serviceIdsAndMappings =
				m.findInJenaModel(jenaModel, null);
			
			if (serviceIdsAndMappings == null)
				continue;
			
			Iterator<String> itr = serviceIdsAndMappings.keySet().iterator();
			if (itr.hasNext()) {
				String key = itr.next();
				servicesAndMappings.put((WebService)service, serviceIdsAndMappings.get(key));
			}
		}
		
		return servicesAndMappings;
	}
	
	/**
	 * From the service model, returns the service object
	 * @param model
	 * @return
	 */
	@Override
	public WebService importSourceFromJenaModel(Model model) {
		logger.debug("model size: " + model.getGraph().size());

		String service_name = "";
		String service_uri;
		String service_id;
		String service_address = "";
		String service_method = "";

		// service id
		service_uri = model.getNsPrefixURI("");
		logger.debug("service uri: " + service_uri);
		
		// service local id
		service_id = service_uri.substring(service_uri.lastIndexOf("/") + 1, service_uri.length() - 1);
		logger.debug("service id: " + service_id);
		
		Property has_address_property = model.getProperty(Namespaces.HRESTS + "hasAddress");
		Property has_name_property = model.getProperty(Namespaces.KARMA + "hasName");
		Property has_method_property = model.getProperty(Namespaces.HRESTS + "hasMethod");
		Property has_input_property = model.getProperty(Namespaces.KARMA + "hasInput");
		Property has_output_property = model.getProperty(Namespaces.KARMA + "hasOutput");

		Resource service_resource = model.getResource(service_uri);
		
		NodeIterator nodeIterator;
		RDFNode node;

		// service name
		nodeIterator = model.listObjectsOfProperty(service_resource, has_name_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			service_name = node.asLiteral().getString();
			logger.debug("service name: " + service_name);
		} else
			logger.debug("service does not have a name.");
		
		// service address
		nodeIterator = model.listObjectsOfProperty(service_resource, has_address_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			service_address = node.asLiteral().getString();
			logger.debug("service address: " + service_address);
		} else
			logger.debug("service does not have an address.");

		// service method
		nodeIterator = model.listObjectsOfProperty(service_resource, has_method_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			service_method = node.asLiteral().getString();
			logger.debug("service method: " + service_method);
		} else
			logger.debug("service does not have a method.");

		List<String> variables;
		List<Attribute> inputAttributes = null;
		List<Attribute> outputAttributes = null;
		edu.isi.karma.rep.model.Model inputModel = null;
		edu.isi.karma.rep.model.Model outputModel = null;

		// service variables
		variables = getVariables(model, service_resource);
		
		// service input
		nodeIterator = model.listObjectsOfProperty(service_resource, has_input_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			inputAttributes = getAttributes(model, node.asResource(), IOType.INPUT);
			inputModel = getSemanticModel(model, node.asResource());
		} else
			logger.debug("service does not have an input.");
		
		// service output
		nodeIterator = model.listObjectsOfProperty(service_resource, has_output_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			outputAttributes = getAttributes(model, node.asResource(), IOType.OUTPUT );
			outputModel = getSemanticModel(model, node.asResource());
		} else
			logger.info("service does not have an output.");
		
		WebService service = new WebService(service_id, service_name, service_address);
		service.setMethod(service_method);
		service.setVariables(variables);
		service.setInputAttributes(inputAttributes);
		service.setOutputAttributes(outputAttributes);
		service.setInputModel(inputModel);
		service.setOutputModel(outputModel);

		
		return service;
	}
	
	private List<String> getVariables(Model model, Resource service_resource) {
		
		Property has_variable_property = model.getProperty(Namespaces.KARMA + "hasVariable");
		List<String> variables = new ArrayList<>();
		NodeIterator nodeIterator;
		RDFNode node;

		// hasAttribute
		nodeIterator = model.listObjectsOfProperty(service_resource, has_variable_property);
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.info("object of the hasAttribute property is not a resource.");
				continue;
			}
			
			variables.add(node.asResource().getLocalName());
		}
		
		return variables;

	}
	
	private List<Attribute> getAttributes(Model model, Resource io_resource, String ioType) {
		
		Property has_attribute_property = model.getProperty(Namespaces.KARMA + "hasAttribute");
		Property has_mandatory_attribute_property = model.getProperty(Namespaces.KARMA + "hasMandatoryAttribute");
		Property has_optional_attribute_property = model.getProperty(Namespaces.KARMA + "hasOptionalAttribute");

		List<Attribute> attList = new ArrayList<>();
		
		NodeIterator nodeIterator;
		RDFNode node;

		// hasAttribute
		nodeIterator = model.listObjectsOfProperty(io_resource, has_attribute_property);
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.info("object of the hasAttribute property is not a resource.");
				continue;
			}
			
			attList.add(getAttribute(model, node.asResource(), ioType, AttributeRequirement.NONE));
		}

		// hasMandatoryAttribute
		nodeIterator = model.listObjectsOfProperty(io_resource, has_mandatory_attribute_property);
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.info("object of the hasMandatoryAttribute property is not a resource.");
				continue;
			}
			
			attList.add(getAttribute(model, node.asResource(), ioType, AttributeRequirement.MANDATORY));
		}
		
		// hasOptionalAttribute
		nodeIterator = model.listObjectsOfProperty(io_resource, has_optional_attribute_property);
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.info("object of the hasOptionalAttribute property is not a resource.");
				continue;
			}
			
			attList.add(getAttribute(model, node.asResource(), ioType, AttributeRequirement.OPTIONAL));
		}
		
		return attList;

	}
	
	private Attribute getAttribute(Model model, Resource att_resource, String ioType, AttributeRequirement requirement) {
		
		String att_id;
		String att_name = "";
		String att_groundedIn = "";

		Property has_name_property = model.getProperty(Namespaces.KARMA + "hasName");
		Property is_gounded_in_property = model.getProperty(Namespaces.HRESTS + "isGroundedIn");
		
		// attribute id
		att_id = att_resource.getLocalName();
		logger.debug("attribute id: " + att_id);

		NodeIterator nodeIterator;
		RDFNode node;
		

		// attribute name
		nodeIterator = model.listObjectsOfProperty(att_resource, has_name_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			att_name = node.asLiteral().getString();
			logger.debug("attribute name: " + att_name);
		} else
			logger.debug("attribute does not have a name.");
		
		// attribute grounded In
		nodeIterator = model.listObjectsOfProperty(att_resource, is_gounded_in_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			att_groundedIn = node.asLiteral().getString();
			logger.debug("attribute grounded in: " + att_groundedIn);
		} else
			logger.debug("attribute does not have agroundedIn value.");

		Attribute att;
		if (att_groundedIn.length() > 0)
			att = new Attribute(att_id, att_resource.getNameSpace(), att_name, ioType, requirement, att_groundedIn );
		else
			att = new Attribute(att_id, att_resource.getNameSpace(), att_name, ioType, requirement);
		
		return att;

	}
	
	private edu.isi.karma.rep.model.Model getSemanticModel(Model model, Resource io_resource) {
		
		Property has_model_property = model.getProperty(Namespaces.KARMA + "hasModel");
		Property has_atom_property = model.getProperty(Namespaces.KARMA + "hasAtom");
		
		NodeIterator nodeIterator;
		RDFNode modelNode;
		RDFNode atomNode;

		// hasModel
		nodeIterator = model.listObjectsOfProperty(io_resource, has_model_property);
		if (!nodeIterator.hasNext() || !(modelNode = nodeIterator.next()).isResource()) {
			logger.info("There is no model resource.");
			return null;
		}

		edu.isi.karma.rep.model.Model semanticModel = 
			new edu.isi.karma.rep.model.Model(modelNode.asResource().getLocalName());
		List<Atom> atoms = new ArrayList<>();
		

		// hasAtom
		nodeIterator = model.listObjectsOfProperty(modelNode.asResource(), has_atom_property);
		while ( nodeIterator.hasNext()) {
			atomNode = nodeIterator.next();
			
			if (!atomNode.isResource()) {
				logger.info("object of the hasAtom property is not a resource.");
				continue;
			}
			
			atoms.add(getAtom(model, atomNode.asResource()));
		}

		semanticModel.setAtoms(atoms);
		return semanticModel;

	}
	
	private Atom getAtom(Model model, Resource atom_resource) {
		
		Property rdf_type = model.getProperty(Namespaces.RDF + "type");

		NodeIterator nodeIterator;
		RDFNode node;

		String classAtomUri = Namespaces.SWRL + "ClassAtom";
		String propertyAtomUri = Namespaces.SWRL + "IndividualPropertyAtom";
		
		// atom type
		nodeIterator = model.listObjectsOfProperty(atom_resource, rdf_type);
		if (!nodeIterator.hasNext() || !(node = nodeIterator.next()).isResource()) {
			logger.info("The atom type is not specified.");
			return null;
		}
		
		if (node.asResource().getURI().equalsIgnoreCase(classAtomUri)) {
			logger.debug("The atom is a ClassAtom");
			return getClassAtom(model, atom_resource);
		}
		else if (node.asResource().getURI().equalsIgnoreCase(propertyAtomUri)) {
			logger.debug("The atom is an IndividualPropertyAtom");
			return getPropertyAtom(model, atom_resource);
		}
		
		return null;

	}
	
	private ClassAtom getClassAtom(Model model, Resource atom_resource) {
		
		String predicateUri;
		String predicatePrefix;
		String predicateNs;
		
		String argument1Id;
		String argument1Type = null;
		
		Resource attribute = ResourceFactory.createResource(Namespaces.KARMA + "Attribute");
		Resource variable = ResourceFactory.createResource(Namespaces.SWRL + "Variable");

		Property class_predicate_property = model.getProperty(Namespaces.SWRL + "classPredicate");
		Property argument1_property = model.getProperty(Namespaces.SWRL + "argument1");


		NodeIterator nodeIterator;
		RDFNode node;

		// atom class predicate
		nodeIterator = model.listObjectsOfProperty(atom_resource, class_predicate_property);
		if (!nodeIterator.hasNext() || !(node = nodeIterator.next()).isResource()) {
			logger.info("The class predicate resource is not specified.");
			return null;
		}
		
		predicateUri = node.asResource().getURI();
		logger.debug("The atom predicate is: " + predicateUri);
		predicateNs = node.asResource().getNameSpace();
		predicatePrefix = model.getNsURIPrefix(predicateNs);
		
		// atom argument1 
		nodeIterator = model.listObjectsOfProperty(atom_resource, argument1_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			argument1Id = node.asResource().getLocalName();
			logger.debug("The atom argument1 is: " + argument1Id);
			
			if (isInstanceOfTheClass(node.asResource(), attribute))
				argument1Type = ArgumentType.ATTRIBUTE;
			else if (isInstanceOfTheClass(node.asResource(), variable))
				argument1Type = ArgumentType.VARIABLE;
			
		} else {
			logger.info("atom does not have an argument1.");
			return null;
		}
		
		Label predicateName = new Label(predicateUri, predicateNs, predicatePrefix);
		Argument arg1 = new Argument(argument1Id, argument1Id, argument1Type);
		
		ClassAtom classAtom = new ClassAtom(predicateName, arg1);

		return classAtom;

	}
	
	private IndividualPropertyAtom getPropertyAtom(Model model, Resource atom_resource) {
		
		String predicateUri;
		String predicatePrefix;
		String predicateNs;
		
		String argument1Id;
		String argument2Id; 

		String argument1Type = null;
		String argument2Type = null; 
		
		Resource attribute = ResourceFactory.createResource(Namespaces.KARMA + "Attribute");
		Resource variable = ResourceFactory.createResource(Namespaces.SWRL + "Variable");

		Property property_predicate_property = model.getProperty(Namespaces.SWRL + "propertyPredicate");
		Property argument1_property = model.getProperty(Namespaces.SWRL + "argument1");
		Property argument2_property = model.getProperty(Namespaces.SWRL + "argument2");

		NodeIterator nodeIterator;
		RDFNode node;

		// atom class predicate
		nodeIterator = model.listObjectsOfProperty(atom_resource, property_predicate_property);
		if (!nodeIterator.hasNext() || !(node = nodeIterator.next()).isResource()) {
			logger.info("The property predicate resource is not specified.");
			return null;
		}
		
		predicateUri = node.asResource().getURI();
		logger.debug("The atom predicate is: " + predicateUri);
		predicateNs = node.asResource().getNameSpace();
		predicatePrefix = model.getNsURIPrefix(predicateNs);
		
		// atom argument1 
		nodeIterator = model.listObjectsOfProperty(atom_resource, argument1_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			argument1Id = node.asResource().getLocalName();
			logger.debug("The atom argument1 is: " + argument1Id);
			
			if (isInstanceOfTheClass(node.asResource(), attribute))
				argument1Type = ArgumentType.ATTRIBUTE;
			else if (isInstanceOfTheClass(node.asResource(), variable))
				argument1Type = ArgumentType.VARIABLE;
			
		} else {
			logger.info("atom does not have an argument1.");
			return null;
		}

		// atom argument2 
		nodeIterator = model.listObjectsOfProperty(atom_resource, argument2_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			argument2Id = node.asResource().getLocalName();
			logger.debug("The atom argument2 is: " + argument2Id);
			
			if (isInstanceOfTheClass(node.asResource(), attribute))
				argument2Type = ArgumentType.ATTRIBUTE;
			else if (isInstanceOfTheClass(node.asResource(), variable))
				argument2Type = ArgumentType.VARIABLE;
			
		} else {
			logger.info("atom does not have an argument2.");
			return null;
		}
		
		Label predicateName = new Label(predicateUri, predicateNs, predicatePrefix);
		Argument arg1 = new Argument(argument1Id, argument1Id, argument1Type);
		Argument arg2 = new Argument(argument2Id, argument2Id, argument2Type);
		
		IndividualPropertyAtom propertyAtom = new IndividualPropertyAtom(predicateName, arg1, arg2);

		return propertyAtom;	
	}
	
	private boolean isInstanceOfTheClass(Resource resource, Resource class_resource) {
		Property type_property = ResourceFactory.createProperty(Namespaces.RDF + "type");
		
		if (resource == null || !resource.isResource())
			return true;
		
		if (resource.hasProperty(type_property, class_resource))
			return true;
		else
			return false;
	}
	
	private static void testGetServiceByUri() {
		String uri = "http://isi.edu/integration/karma/services/CDA81BE4-DD77-E0D3-D033-FC771B2F4800#";
		WebService service = WebServiceLoader.getInstance().getSourceByUri(uri);
		
		if (service != null) {
//			System.out.println(service.getInputModel().getSPARQLConstructQuery());
//			System.out.println(service.getOutputModel().getSPARQLConstructQuery());
			service.print();
		}
	}
	private static void testGetServiceByAddress() {
		String address = "http://api.geonames.org/";
		WebService service = WebServiceLoader.getInstance().getServiceByAddress(address);
		if (service != null) service.print();
	}
	private static void testGetAllServices() {
		List<Source> serviceList = WebServiceLoader.getInstance().getSourcesDetailedInfo(null);
		for (Source s : serviceList) {
			if (s != null) s.print();
		}
	}
	private static void testGetServicesByIOPattern() {
		edu.isi.karma.rep.model.Model semanticModel = new edu.isi.karma.rep.model.Model(null);

//		String geonamesOntology = "http://www.geonames.org/ontology#";
//		String wgs84Ontology = "http://www.w3.org/2003/01/geo/wgs84_pos#";
		String geoOntology = "http://isi.edu/ontologies/geo/current#";
		
		Label featurePredicatName = new Label(geoOntology + "Feature", geoOntology, "geo");
		Label latPredicatName = new Label(geoOntology + "lat", geoOntology, "geo");
		Label lngPredicatName = new Label(geoOntology + "long", geoOntology, "geo");
		
		ClassAtom c1 = new ClassAtom(featurePredicatName, new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE));
		IndividualPropertyAtom p1 = new IndividualPropertyAtom(latPredicatName,
				new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE), 
				new Argument("arg2", "arg2", ArgumentType.ATTRIBUTE));
		IndividualPropertyAtom p2 = new IndividualPropertyAtom(lngPredicatName,
				new Argument("arg1", "arg1", ArgumentType.ATTRIBUTE), 
				new Argument("arg3", "arg3", ArgumentType.ATTRIBUTE));
//		ClassAtom c2 = new ClassAtom(featurePredicatName, new Argument("arg2", "arg2", ArgumentType.ATTRIBUTE));

		semanticModel.getAtoms().add(c1);
//		semanticModel.getAtoms().add(c2);
		semanticModel.getAtoms().add(p1);
		semanticModel.getAtoms().add(p2);
		
		Map<WebService, Map<String, String>> servicesAndMappings = 
				WebServiceLoader.getInstance().getServicesWithInputContainedInModel(semanticModel, null);
//			getServicesByInputPattern(semanticModel, null);

//		Map<Service, Map<String, String>> servicesAndMappings = 
//			getServicesByIOPattern(semanticModel, IOType.INPUT, null);

		if (servicesAndMappings == null)
			return;
		
		for (WebService s : servicesAndMappings.keySet()) {
			if (s != null) System.out.println((s.getUri())); //s.print();
		}
		
		System.out.println("Mappings from matched source to model arguments:");
		for (Map.Entry<WebService, Map<String, String>> webServiceMapEntry : servicesAndMappings.entrySet()) {
			System.out.println("Service: " + webServiceMapEntry.getKey().getId());
			if (webServiceMapEntry.getValue() == null)
				continue;
			for (String str : webServiceMapEntry.getValue().keySet())
				System.out.println(str + "-------" + webServiceMapEntry.getValue().get(str));
		}

	}
	private static void testDeleteServiceByUri() {
		String uri = "http://isi.edu/integration/karma/services/3D579101-2596-2331-53A8-63F949D71C8F#";
		WebServiceLoader.getInstance().deleteSourceByUri(uri);
	}	
	public static void main(String[] args) {

//		ServiceBuilder.main(new String[0]);

		boolean test1 = true, test2 = false, test3 = false, test4 = false, test5 = false;
		if (test1) testGetServiceByUri();
		if (test2) testGetServiceByAddress();
		if (test3) testGetServicesByIOPattern();
		if (test4) testGetAllServices();
		if (test5) testDeleteServiceByUri();

	}

}
