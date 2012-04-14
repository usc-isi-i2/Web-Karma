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
import java.util.List;

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

	static Logger logger = Logger.getLogger(ServiceLoader.class);

	public static Service getServiceByAddress(String address) {
		
		Model m = getJenaModelByServiceAddress(address);
		if (m == null)
			return null;

		Service service = getServiceFromJenaModel(m);
		return service;
	}
	
	public static Service getServiceByUri(String uri) {
		
		Model m = ServiceRepository.Instance().getNamedModel(uri);
		if (m == null)
			return null;

		Service service = getServiceFromJenaModel(m);
		return service;
	}
	
	/**
	 * returns the service id, name, address of all services in the repository
	 * @return
	 */
	public static List<Service> getAllServices() {
		
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

				service_id = s.toString();
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
	public static List<Service> getAllServicesComplete() {
		List<Service> serviceList = getAllServices();
		for (Service s : serviceList) {
			s = getServiceByUri(s.getId());
		}
		return serviceList;
	}
	
	private static Model getJenaModelByServiceAddress(String address) {

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
			
			// get the model specific to the found service
			if (serviceURI.length() > 0)
				return ServiceRepository.Instance().getNamedModel(serviceURI);
			else {
				logger.info("could not find a service with the address " + address + " in service repository.");
				return null;
			}
		} catch (Exception e) {
			logger.info("Exception in finding a service with the address " + address + " in service repository.");
			return null;
		} finally { 
			qexec.close() ; 
		}

	}
	
	public static Service getServiceFromJenaModel(Model model) {
		logger.debug("model size: " + model.getGraph().size());
		
		String service_name = "";
		String service_id = "";
		String service_localId = "";
		String service_address = "";
		
		
		
		Service service = new Service();
		List<Operation> operations = new ArrayList<Operation>();
		
		String serviceUri = model.getNsPrefixURI("");
		
		// service id
		service_id = serviceUri;
		logger.debug("service id: " + service_id);
		
		// service local id
		service_localId = service_id.substring(service_id.lastIndexOf("/") + 1, service_id.length() - 1);
		logger.debug("service local id: " + service_localId);
		
		Property has_address_property = model.getProperty(Namespaces.HRESTS + "hasAddress");
		Property has_name_property = model.getProperty(Namespaces.KARMA + "hasName");
		Property has_operation_property = model.getProperty(Namespaces.WSMO_LITE + "hasOperation");
		
		Resource service_resource = model.getResource(serviceUri);
		
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
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.debug("object of the hasOperation property is not a resource.");
				continue;
			}
			
			operations.add(getOperation(model, node.asResource()));
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
		
		Operation op = new Operation();
		List<Attribute> inputAttributes = null;;
		List<Attribute> outputAttributes = null;
		edu.isi.karma.service.Model inputModel = null;
		edu.isi.karma.service.Model outputModel = null;
		
		// operation id
		op_id = op_resource.getLocalName();
		logger.debug("operation id: " + op_id);

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
			logger.debug("operation does not have an output.");
		
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
				logger.debug("object of the hasAttribute property is not a resource.");
				continue;
			}
			
			attList.add(getAttribute(model, node.asResource(), ioType, AttributeRequirement.NONE));
		}

		// hasMandatoryAttribute
		nodeIterator = model.listObjectsOfProperty(io_resource, has_mandatory_attribute_property);
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.debug("object of the hasMandatoryAttribute property is not a resource.");
				continue;
			}
			
			attList.add(getAttribute(model, node.asResource(), ioType, AttributeRequirement.MANDATORY));
		}
		
		// hasOptionalAttribute
		nodeIterator = model.listObjectsOfProperty(io_resource, has_optional_attribute_property);
		while ( nodeIterator.hasNext()) {
			node = nodeIterator.next();
			
			if (!node.isResource()) {
				logger.debug("object of the hasOptionalAttribute property is not a resource.");
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
			att = new Attribute(att_id, att_name, ioType, requirement, att_groundedIn);
		else
			att = new Attribute(att_id, att_name, ioType, requirement);
		
		return att;

	}
	
	private static edu.isi.karma.service.Model getSemanticModel(Model model, Resource io_resource) {
		
		Property has_model_property = model.getProperty(Namespaces.KARMA + "hasModel");
		Property has_atom_property = model.getProperty(Namespaces.KARMA + "hasAtom");
		
		edu.isi.karma.service.Model semanticModel = new edu.isi.karma.service.Model();
		List<Atom> atoms = new ArrayList<Atom>();
		
		NodeIterator nodeIterator = null;
		RDFNode modelNode = null;
		RDFNode atomNode = null;

		// hasModel
		nodeIterator = model.listObjectsOfProperty(io_resource, has_model_property);
		if (!nodeIterator.hasNext() || !(modelNode = nodeIterator.next()).isResource()) {
			logger.debug("There is no model resource.");
			return semanticModel;
		}
		
		// hasAtom
		nodeIterator = model.listObjectsOfProperty(modelNode.asResource(), has_atom_property);
		while ( nodeIterator.hasNext()) {
			atomNode = nodeIterator.next();
			
			if (!atomNode.isResource()) {
				logger.debug("object of the hasAtom property is not a resource.");
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
			logger.debug("The atom type is not specified.");
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
			logger.debug("The class predicate resource is not specified.");
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
			logger.debug("atom does not have an argument1.");
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
			logger.debug("The property predicate resource is not specified.");
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
			logger.debug("atom does not have an argument1.");
			return null;
		}

		// atom argument2 
		nodeIterator = model.listObjectsOfProperty(atom_resource, argument2_property);
		if (nodeIterator.hasNext() && (node = nodeIterator.next()).isResource()) {
			argument2 = node.asResource().getLocalName();
			logger.debug("The atom argument2 is: " + argument2);
		} else {
			logger.debug("atom does not have an argument2.");
			return null;
		}
		
		Name predicateName = new Name(predicateUri, predicateNs, predicatePrefix);
		Name argument1Name = new Name(argument1, "", "");
		Name argument2Name = new Name(argument2, "", "");
		
		PropertyAtom propertyAtom = new PropertyAtom(predicateName, argument1Name, argument2Name);

		return propertyAtom;	
	}
	
	public static void main(String[] args) {

//		ServiceBuilder.main(new String[0]);
		
		String address = "http://api.geonames.org/";
		Service service = ServiceLoader.getServiceByAddress(address);
		if (service != null) service.print();

//		String uri = "http://isi.edu/integration/karma/services/E9C3F8D3-F778-5C4B-E089-C1749D50AE1F#";
//		Service service = ServiceLoader.getServiceByUri(uri);
//		if (service != null) service.print();
		
		List<Service> serviceList = getAllServices();
		for (Service s : serviceList) {
			if (s != null) s.print();
		}

	}

}
