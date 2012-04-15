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

package edu.isi.karma.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

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

import edu.isi.karma.modeling.alignment.Name;

public class ServiceLoader {

	private static final int DEFAULT_SERVICE_RESULTS_SIZE = 10;
	private static final int DEFAULT_OPERATION_RESULTS_SIZE = 10;

	static Logger logger = Logger.getLogger(ServiceLoader.class);

	public static void deleteServiceByAddress(String address, Integer operationLimit) {
		
		String uri = getServiceUriByServiceAddress(address);
		ServiceRepository.Instance().clearNamedModel(uri);
	}
	
	public static Service getServiceByAddress(String address, Integer operationLimit) {
		
		String uri = getServiceUriByServiceAddress(address);
		Model m = ServiceRepository.Instance().getNamedModel(uri);
		if (m == null)
			return null;

		Service service = getServiceFromJenaModel(m, null, operationLimit);
		return service;
	}
	
	public static void deleteServiceByUri(String uri) {
		ServiceRepository.Instance().clearNamedModel(uri);
	}
	
	public static Service getServiceByUri(String uri, Integer operationLimit) {
		
		Model m = ServiceRepository.Instance().getNamedModel(uri);
		if (m == null)
			return null;

		Service service = getServiceFromJenaModel(m, null, operationLimit);
		return service;
	}
	
	/**
	 * returns the service id, name, address of all services in the repository
	 * @param limit : maximum number of results, null value means all the services
	 * @return
	 */
	public static List<Service> getAllServices(Integer serviceLimit) {
		
		List<Service> serviceList = new ArrayList<Service>();
		
		Model model = ServiceRepository.Instance().getModel();
		
		String service_id = "";
		String service_name = "";
		String service_address = "";
		
		// Create a new query
		String queryString =
			"PREFIX " + Prefixes.KARMA + ": <" + Namespaces.KARMA + "> \n" +
			"PREFIX " + Prefixes.WSMO_LITE + ": <" + Namespaces.WSMO_LITE + "> \n" +
			"PREFIX " + Prefixes.HRESTS + ": <" + Namespaces.HRESTS + "> \n" +
			"SELECT ?s ?name ?address \n" +
			"WHERE { \n" +
			"      ?s a " + Prefixes.WSMO_LITE + ":Service . \n" +
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
					serviceList.add(new Service(service_id, service_name, service_address));
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
	public static List<Service> getAllServicesComplete(Integer serviceLimit, Integer operationLimit) {

		List<Service> serviceList = getAllServices(serviceLimit);
		List<Service> serviceListCompleteInfo = new ArrayList<Service>();
		for (Service s : serviceList) {
			serviceListCompleteInfo.add(getServiceByUri(s.getUri(), operationLimit));
		}
		return serviceListCompleteInfo;
	}
	
	/**
	 * Returns all the operations in the repository whose input/output model matches the semantic model parameter.
	 * Note that the services in the return list only include the operations that match the model parameter.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @param ioType declares which one of the service input or service output will be tested for matching
	 * @param operationsLimit maximum number of operations that will be fetched
	 * @return
	 */
	public static List<Service> getServicesByIOPattern(edu.isi.karma.service.Model semanticModel, 
			String ioType, Integer operationsLimit) {
		
		Map<String, List<String>> serviceAndOperations = getServicesIdsByIOPattern(semanticModel, ioType, operationsLimit);
		if (serviceAndOperations == null)
			return null;
		
		List<Service> serviceList = new ArrayList<Service>();
		
		for (String uri : serviceAndOperations.keySet()) {
			Model m = ServiceRepository.Instance().getNamedModel(uri);
			if (m != null)
				serviceList.add(getServiceFromJenaModel(m, serviceAndOperations.get(uri), null));
		}
		return serviceList;
	}
	
	/**
	 * Returns a hash map of all services and their operations whose input/output model matches the semantic model parameter.
	 * Note that the services in the return list only include the operations that match the model parameter.
	 * @param semanticModel The input model whose pattern will be searched in the repository
	 * @param ioType declares which one of the service input or service output will be tested for matching
	 * @param operationsLimit maximum number of operations that will be fetched
	 * @return
	 */
	private static Map<String, List<String>> getServicesIdsByIOPattern(edu.isi.karma.service.Model semanticModel, 
			String ioType, Integer operationsLimit) {

		Map<String, List<String>> serviceAndOperations = new HashMap<String, List<String>>();

		if (semanticModel == null || semanticModel.getAtoms() == null 
				|| semanticModel.getAtoms().size() == 0) {
			logger.info("The input model is nul or it does not have any atom");
			return null;
		}
		
		String io_class = "";
		String io_property = "";
		if (ioType.equalsIgnoreCase(IOType.OUTPUT))
		{
			io_class = "Output";
			io_property = "hasOutput";
		} else {
			io_class =  "Input";
			io_property = "hasInput";
		}
	
		// map of NS --> Prefix
		Map<String, String> nsToPrefixMapping = new HashMap<String, String>();
		nsToPrefixMapping.put(Namespaces.KARMA, Prefixes.KARMA);
		nsToPrefixMapping.put(Namespaces.WSMO_LITE, Prefixes.WSMO_LITE);
		nsToPrefixMapping.put(Namespaces.SWRL, Prefixes.SWRL);
		nsToPrefixMapping.put(Namespaces.HRESTS, Prefixes.HRESTS);
		
		String select =
			"SELECT ?s ?op \n" +
			"WHERE { \n" +
			"      ?s a " + Prefixes.WSMO_LITE + ":Service . \n" +
			"      ?s " + Prefixes.WSMO_LITE + ":hasOperation ?op . \n" +
			"      ?op a " + Prefixes.WSMO_LITE + ":Operation . \n" +
			"      ?op " + Prefixes.KARMA + ":" + io_property + " ?io . \n" +
			"      ?io a " + Prefixes.KARMA + ":" + io_class + " . \n" +
			"      ?io " + Prefixes.KARMA + ":hasModel ?model . \n" + 
			"      ?model a " + Prefixes.KARMA + ":Model . \n";
		
		String atomVar = "";
		String predicateUri = "";
		String argument1 = "";
		String argument2 = "";

		for (int i = 0; i < semanticModel.getAtoms().size(); i++) {
			Atom atom = semanticModel.getAtoms().get(i);
			if (atom != null) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = ((ClassAtom)atom);
					atomVar = "?atom" + String.valueOf(i+1);
					predicateUri = classAtom.getClassPredicate().getUri();
					argument1 = "?" + classAtom.getArgument1().getLocalName();
					
					select += 
						"      ?model " + Prefixes.KARMA + ":hasAtom " + atomVar + " . \n" +
						"      " + atomVar + " a " + Prefixes.SWRL + ":ClassAtom . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":classPredicate <" + predicateUri + "> . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":argument1 " + argument1 + " . \n";
				}
				else if (atom instanceof PropertyAtom) {
					PropertyAtom propertyAtom = ((PropertyAtom)atom);
					atomVar = "?atom" + String.valueOf(i+1);
					predicateUri = propertyAtom.getPropertyPredicate().getUri();
					argument1 = "?" + propertyAtom.getArgument1().getLocalName();
					argument2 = "?" + propertyAtom.getArgument2().getLocalName();

					
					select += 
						"      ?model " + Prefixes.KARMA + ":hasAtom " + atomVar + " . \n" +
						"      " + atomVar + " a " + Prefixes.SWRL + ":IndividualPropertyAtom . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":propertyPredicate <" + predicateUri + "> . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":argument1 " + argument1 + " . \n" +
						"      " + atomVar + " " + Prefixes.SWRL + ":argument2 " + argument2 + " . \n";
				}			
			}
		}		
		select += 	"      } \n";
 
		String prefix = getSPARQLHeader(nsToPrefixMapping); 
		String queryString = prefix + select;
		
		if (operationsLimit != null) {
			if (operationsLimit.intValue() < 0) operationsLimit = DEFAULT_OPERATION_RESULTS_SIZE;
			queryString += "LIMIT " + String.valueOf(operationsLimit.intValue() + "\n");
		}
		
		logger.debug("query= \n" + queryString);
		
		Model model = ServiceRepository.Instance().getModel();
		Query query = QueryFactory.create(queryString);
//		// Execute the query and obtain results
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
				RDFNode op = soln.get("op") ;       // Get a result variable by name.

				if (s == null) {
					logger.info("service uri is null.");
					continue;
				}

				String service_uri = s.toString();
				logger.debug("service uri: " + service_uri);
				if (serviceAndOperations.get(service_uri) == null)
					serviceAndOperations.put(service_uri, new ArrayList<String>());
				
				if (op != null && op.isResource()) {
					String op_id = op.asResource().getLocalName();
					logger.debug("op id: " + op_id);
					serviceAndOperations.get(service_uri).add(op.asResource().getLocalName());
				} else logger.debug("op is null");
					
			}
			
			return serviceAndOperations;
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		} finally { 
			qexec.close() ; 
		}
	}

	private static String getSPARQLHeader(Map<String, String> nsToPrefixMapping) {
		String prefixHeader = "";
		
		for (String ns : nsToPrefixMapping.keySet()) {
			String prefix = nsToPrefixMapping.get(ns);
			if (prefix != null)
				prefixHeader += "PREFIX " + prefix + ": <" + ns + "> \n";
		}

		return prefixHeader;
	}
	
	private static String getServiceUriByServiceAddress(String address) {

		Model model = ServiceRepository.Instance().getModel();
		
		// Create a new query
		String queryString =
			"PREFIX " + Prefixes.KARMA + ": <" + Namespaces.KARMA + "> \n" +
			"PREFIX " + Prefixes.WSMO_LITE + ": <" + Namespaces.WSMO_LITE + "> \n" +
			"PREFIX " + Prefixes.HRESTS + ": <" + Namespaces.HRESTS + "> \n" +
			"SELECT ?s \n" +
			"WHERE { \n" +
			"      ?s a " + Prefixes.WSMO_LITE + ":Service . \n" +
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
	 * From the service model, returns the service object
	 * If the list of operation ids is null, it includes all the operations, otherwise it only 
	 * fetches the mentioned operations
	 * If operationLimit=null and operationIds=null returns all operations
	 * If operationLimit=null, it returns the all specified operations
	 * If operationIds=null, it returns the first (opLimit) number of operations
	 * If both of them are not null, it returns the first (opLimit) number of specified operations 
	 * @param model
	 * @param operationIds
	 * @return
	 */
	public static Service getServiceFromJenaModel(Model model, List<String> operationIds, Integer operationLimit) {
		logger.debug("model size: " + model.getGraph().size());
		
		if (operationLimit != null && operationLimit.intValue() < 0)
			operationLimit = DEFAULT_OPERATION_RESULTS_SIZE;
		
		String service_name = "";
		String service_uri = "";
		String service_id = "";
		String service_address = "";
		
		Service service = new Service();
		List<Operation> operations = new ArrayList<Operation>();
		
		// service id
		service_uri = model.getNsPrefixURI("");
		logger.debug("service uri: " + service_uri);
		
		// service local id
		service_id = service_uri.substring(service_uri.lastIndexOf("/") + 1, service_uri.length() - 1);
		logger.debug("service id: " + service_id);
		
		Property has_address_property = model.getProperty(Namespaces.HRESTS + "hasAddress");
		Property has_name_property = model.getProperty(Namespaces.KARMA + "hasName");
		Property has_operation_property = model.getProperty(Namespaces.WSMO_LITE + "hasOperation");
		
		Resource service_resource = model.getResource(service_uri);
		
		NodeIterator nodeIterator = null;
		RDFNode node = null;

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

		// operations
		nodeIterator = model.listObjectsOfProperty(service_resource, has_operation_property);
		int opCounter = 0;
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.info("object of the hasOperation property is not a resource.");
				continue;
			}
			
			if (operationLimit != null && opCounter == operationLimit)
				break;
			
			String op_id = node.asResource().getLocalName();
			if (operationIds == null || operationIds.indexOf(op_id) != -1) {
				operations.add(getOperation(model, node.asResource()));
				opCounter ++;
			}
		}
		
		service.setId(service_id);
		service.setName(service_name);
		service.setAddress(service_address);
		service.setOperations(operations);
		
		return service;
	}
	
	private static Operation getOperation(Model model, Resource op_resource) {
		
		String op_name = "";
		String op_id = "";
		String op_method = "";
		String op_address = "";
		
		Property has_address_property = model.getProperty(Namespaces.HRESTS + "hasAddress");
		Property has_name_property = model.getProperty(Namespaces.KARMA + "hasName");
		Property has_method_property = model.getProperty(Namespaces.HRESTS + "hasMethod");
		Property has_input_property = model.getProperty(Namespaces.KARMA + "hasInput");
		Property has_output_property = model.getProperty(Namespaces.KARMA + "hasOutput");
		

		// operation id
		op_id = op_resource.getLocalName();
		logger.debug("operation id: " + op_id);

		Operation op = new Operation(op_id);
		List<Attribute> inputAttributes = null;;
		List<Attribute> outputAttributes = null;
		edu.isi.karma.service.Model inputModel = null;
		edu.isi.karma.service.Model outputModel = null;

		NodeIterator nodeIterator = null;
		RDFNode node = null;
		

		// operation name
		nodeIterator = model.listObjectsOfProperty(op_resource, has_name_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			op_name = node.asLiteral().getString();
			logger.debug("operation name: " + op_name);
		} else
			logger.debug("operation does not have a name.");
		
		// operation address
		nodeIterator = model.listObjectsOfProperty(op_resource, has_address_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			op_address = node.asLiteral().getString();
			logger.debug("operation address: " + op_address);
		} else
			logger.debug("operation does not have an address.");

		// operation method
		nodeIterator = model.listObjectsOfProperty(op_resource, has_method_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isLiteral()) {
			op_method = node.asLiteral().getString();
			logger.debug("operation method: " + op_address);
		} else
			logger.debug("operation does not have a method.");

		// operation input
		nodeIterator = model.listObjectsOfProperty(op_resource, has_input_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			inputAttributes = getAttributes(model, node.asResource(), IOType.INPUT);
			inputModel = getSemanticModel(model, node.asResource());
		} else
			logger.debug("operation does not have an input.");
		
		// operation output
		nodeIterator = model.listObjectsOfProperty(op_resource, has_output_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			outputAttributes = getAttributes(model, node.asResource(), IOType.OUTPUT );
			outputModel = getSemanticModel(model, node.asResource());
		} else
			logger.info("operation does not have an output.");
		
		op.setId(op_id);
		op.setName(op_name);
		op.setAddressTemplate(op_address);
		op.setMethod(op_method);
		op.setInputAttributes(inputAttributes);
		op.setOutputAttributes(outputAttributes);
		op.setInputModel(inputModel);
		op.setOutputModel(outputModel);
		
		return op;

	}
	
	private static List<Attribute> getAttributes(Model model, Resource io_resource, String ioType) {
		
		Property has_attribute_property = model.getProperty(Namespaces.KARMA + "hasAttribute");
		Property has_mandatory_attribute_property = model.getProperty(Namespaces.KARMA + "hasMandatoryAttribute");
		Property has_optional_attribute_property = model.getProperty(Namespaces.KARMA + "hasOptionalAttribute");

		List<Attribute> attList = new ArrayList<Attribute>();
		
		NodeIterator nodeIterator = null;
		RDFNode node = null;

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
	
	private static Attribute getAttribute(Model model, Resource att_resource, String ioType, AttributeRequirement requirement) {
		
		String att_id = "";
		String att_name = "";
		String att_groundedIn = "";

		Property has_name_property = model.getProperty(Namespaces.KARMA + "hasName");
		Property is_gounded_in_property = model.getProperty(Namespaces.HRESTS + "isGroundedIn");
		
		// attribute id
		att_id = att_resource.getLocalName();
		logger.debug("attribute id: " + att_id);

		NodeIterator nodeIterator = null;
		RDFNode node = null;
		

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

		Attribute att = null;
		if (att_groundedIn.length() > 0)
			att = new Attribute(att_id, att_resource.getNameSpace(), att_name, ioType, requirement, att_groundedIn );
		else
			att = new Attribute(att_id, att_resource.getNameSpace(), att_name, ioType, requirement);
		
		return att;

	}
	
	private static edu.isi.karma.service.Model getSemanticModel(Model model, Resource io_resource) {
		
		Property has_model_property = model.getProperty(Namespaces.KARMA + "hasModel");
		Property has_atom_property = model.getProperty(Namespaces.KARMA + "hasAtom");
		
		NodeIterator nodeIterator = null;
		RDFNode modelNode = null;
		RDFNode atomNode = null;

		// hasModel
		nodeIterator = model.listObjectsOfProperty(io_resource, has_model_property);
		if (!nodeIterator.hasNext() || !(modelNode = nodeIterator.next()).isResource()) {
			logger.info("There is no model resource.");
			return null;
		}

		edu.isi.karma.service.Model semanticModel = 
			new edu.isi.karma.service.Model(modelNode.asResource().getLocalName());
		List<Atom> atoms = new ArrayList<Atom>();
		

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
	
	private static Atom getAtom(Model model, Resource atom_resource) {
		
		Property rdf_type = model.getProperty(Namespaces.RDF + "type");

		NodeIterator nodeIterator = null;
		RDFNode node = null;

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
	
	private static ClassAtom getClassAtom(Model model, Resource atom_resource) {
		
		String predicateUri = "";
		String predicatePrefix = "";
		String predicateNs = "";
		
		String argument1 = "";
		
		Property class_predicate_property = model.getProperty(Namespaces.SWRL + "classPredicate");
		Property argument1_property = model.getProperty(Namespaces.SWRL + "argument1");


		NodeIterator nodeIterator = null;
		RDFNode node = null;

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
			argument1 = node.asResource().getLocalName();
			logger.debug("The atom argument1 is: " + argument1);
		} else {
			logger.info("atom does not have an argument1.");
			return null;
		}
		
		Name predicateName = new Name(predicateUri, predicateNs, predicatePrefix);
		Name argument1Name = new Name(argument1, "", "");
		
		ClassAtom classAtom = new ClassAtom(predicateName, argument1Name);

		return classAtom;

	}
	
	private static PropertyAtom getPropertyAtom(Model model, Resource atom_resource) {
		
		String predicateUri = "";
		String predicatePrefix = "";
		String predicateNs = "";
		
		String argument1 = "";
		String argument2 = ""; 
		
		Property property_predicate_property = model.getProperty(Namespaces.SWRL + "propertyPredicate");
		Property argument1_property = model.getProperty(Namespaces.SWRL + "argument1");
		Property argument2_property = model.getProperty(Namespaces.SWRL + "argument2");

		NodeIterator nodeIterator = null;
		RDFNode node = null;

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
			argument1 = node.asResource().getLocalName();
			logger.debug("The atom argument1 is: " + argument1);
		} else {
			logger.info("atom does not have an argument1.");
			return null;
		}

		// atom argument2 
		nodeIterator = model.listObjectsOfProperty(atom_resource, argument2_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			argument2 = node.asResource().getLocalName();
			logger.debug("The atom argument2 is: " + argument2);
		} else {
			logger.info("atom does not have an argument2.");
			return null;
		}
		
		Name predicateName = new Name(predicateUri, predicateNs, predicatePrefix);
		Name argument1Name = new Name(argument1, "", "");
		Name argument2Name = new Name(argument2, "", "");
		
		PropertyAtom propertyAtom = new PropertyAtom(predicateName, argument1Name, argument2Name);

		return propertyAtom;	
	}
	
	private static void testGetServiceByUri() {
		String uri = "http://isi.edu/integration/karma/services/E9C3F8D3-F778-5C4B-E089-C1749D50AE1F#";
		Service service = ServiceLoader.getServiceByUri(uri, null);
		if (service != null) service.print();
	}
	private static void testGetServiceByAddress() {
		String address = "http://api.geonames.org/";
		Service service = ServiceLoader.getServiceByAddress(address, null);
		if (service != null) service.print();
	}
	private static void testGetAllServices() {
		List<Service> serviceList = getAllServicesComplete(null, null);
		for (Service s : serviceList) {
			if (s != null) s.print();
		}
	}
	private static void testGetServicesByIOPattern() {
		edu.isi.karma.service.Model semanticModel = new edu.isi.karma.service.Model(null);

		String geonamesOntology = "http://www.geonames.org/ontology#";
		String wgs84Ontology = "http://www.w3.org/2003/01/geo/wgs84_pos#";
		
		Name featurePredicatName = new Name(geonamesOntology + "Feature", geonamesOntology, "gn");
		Name latPredicatName = new Name(wgs84Ontology + "lat", wgs84Ontology, "wgs84");
		Name lngPredicatName = new Name(wgs84Ontology + "long", wgs84Ontology, "wgs84");
		
		ClassAtom c1 = new ClassAtom(featurePredicatName, new Name("arg1", null, null));
		PropertyAtom p1 = new PropertyAtom(latPredicatName, new Name("arg1", null, null), new Name("arg2", null, null));
		PropertyAtom p2 = new PropertyAtom(lngPredicatName, new Name("arg1", null, null), new Name("arg3", null, null));

		semanticModel.getAtoms().add(c1);
		semanticModel.getAtoms().add(p1);
		semanticModel.getAtoms().add(p2);
		List<Service> serviceList = getServicesByIOPattern(semanticModel, IOType.INPUT, null);
		for (Service s : serviceList) {
			if (s != null) s.print();
		}
	}
	public static void main(String[] args) {

//		ServiceBuilder.main(new String[0]);

		boolean test1 = false, test2 = false, test3 = false, test4 = true;
		if (test1) testGetServiceByUri();
		if (test2) testGetServiceByAddress();
		if (test3) testGetServicesByIOPattern();
		if (test4) testGetAllServices();


	}

}
