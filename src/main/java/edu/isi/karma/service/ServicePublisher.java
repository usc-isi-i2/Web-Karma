/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this.service file except in compliance with the License.
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class ServicePublisher {

	static Logger logger = Logger.getLogger(ServicePublisher.class);

	private Service service;
	private Model model = null;
	
	public ServicePublisher(Service service) {
		this.service = service;
	}
	
	private Model generateModel() {
		
//		OntModel model = ModelFactory.createOntologyModel();
//		Model model = ServiceRepository.Instance().getNamedModel(baseNS);
//		ModelMaker maker = ModelFactory.createMemModelMaker();
//		Model model = maker.createModel(baseNS);
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = service.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.WSMO_LITE, Namespaces.WSMO_LITE);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
//		model.setNsPrefix(Prefixes.SAWSDL, Namespaces.SAWSDL);
//		model.setNsPrefix(Prefixes.MSM, Namespaces.MSM);
		model.setNsPrefix(Prefixes.HRESTS, Namespaces.HRESTS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);
//		model.setNsPrefix(Prefixes.RULEML, Namespaces.RULEML);

		addInvocationPart(model);
		
		return model;
		
	}
	
	/**
	 * 
	 * @param lang The language in which to write the model is specified by the lang argument. 
	 * Predefined values are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3". 
	 * The default value, represented by null is "RDF/XML".
	 * @throws FileNotFoundException
	 */
	public void publish(String lang, boolean writeToFile) throws FileNotFoundException {
		
		Service existingService = ServiceLoader.getServiceByAddress(this.service.getAddress(), null);
		
		String newServiceOpName = "";
		String existingServiceOpName = "";
		String existingServiceOpId = "";

		if (existingService != null) {
			
			if (this.service.getOperations() != null)
			for (Operation newServiceOp : this.service.getOperations()) {
				
				if (existingService.getOperations() == null)
					existingService.setOperations(new ArrayList<Operation>());
				
				newServiceOpName = newServiceOp.getName();
				logger.debug("new operation: " + newServiceOpName);
				
				boolean found = false;
				for (Operation existingServiceOp : existingService.getOperations()) {
					existingServiceOpId = existingServiceOp.getId();
					existingServiceOpName = existingServiceOp.getName();
					logger.debug("existing operation: " + existingServiceOpName);
					
					// existing service has a previous version of this operation
					// should be replaced with the new model of the operation
					if (newServiceOpName.equalsIgnoreCase(existingServiceOpName)) {
						logger.info("The repository has already a model of the operation " + newServiceOpName + " and it should be updated.");
						newServiceOp.updateId(existingServiceOpId);
						existingServiceOp = newServiceOp;
						found = true;
						break;
					}
				}
				
				if (!found) {
					logger.info("The operation " + newServiceOpName + " is a new operation and should be added to repository.");
					newServiceOp.updateId("op" + String.valueOf(existingService.getOperations().size() +1) );
					existingService.getOperations().add(newServiceOp);
				}
			}
			
			this.service = existingService;
		}
		
		if (this.model == null)
			model = generateModel();
		
		// update the repository active model
		ServiceRepository.Instance().addModel(this.model, service.getUri());

		// write the model to the file
		if (writeToFile)
			writeToFile(lang);
	}
	
	public void writeToFile(String lang) throws FileNotFoundException {
		if (this.model == null)
			model = generateModel();
		
		String service_desc_file = ServiceRepository.Instance().SERVICE_REPOSITORY_DIR + this.service.getId() + ".n3";
		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(service_desc_file));
		model.write(output,lang);		
//		model.write(output,"RDF/XML-ABBREV");
//		model.write(output,"N-TRIPLE");
//		model.write(output,"RDF/XML");
		
	}
	
	public void addInvocationPart(Model model) {
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource service_resource = model.createResource(Namespaces.WSMO_LITE + "Service");
		Resource operation_resource = model.createResource(Namespaces.WSMO_LITE + "Operation");
		Resource input_resource = model.createResource(Namespaces.KARMA + "Input");
		Resource output_resource = model.createResource(Namespaces.KARMA + "Output");
		Resource attribute_resource = model.createResource(Namespaces.KARMA + "Attribute");
		Resource variavle_resource = model.createResource(Namespaces.SWRL + "Variable");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_operation = model.createProperty(Namespaces.WSMO_LITE, "hasOperation");
		Property has_address = model.createProperty(Namespaces.HRESTS, "hasAddress");
		Property has_method = model.createProperty(Namespaces.HRESTS, "hasMethod");
		Property has_input = model.createProperty(Namespaces.KARMA, "hasInput");
		Property has_output = model.createProperty(Namespaces.KARMA, "hasOutput");
		Property has_attribute = model.createProperty(Namespaces.KARMA, "hasAttribute");
		Property has_mandatory_attribute = model.createProperty(Namespaces.KARMA, "hasMandatoryAttribute");
		Property has_optional_attribute = model.createProperty(Namespaces.KARMA, "hasOptionalAttribute");
		Property has_name = model.createProperty(Namespaces.KARMA, "hasName");
//		Property model_reference = model.createProperty(Namespaces.SAWSDL, "modelReference");
		Property is_grounded_in = model.createProperty(Namespaces.HRESTS, "isGroundedIn");
		
		// rdf datatypes
		String uri_template = Namespaces.HRESTS + "URITemplate";
		String rdf_plain_literal = Namespaces.RDF + "PlainLiteral";

		Resource my_service = model.createResource(baseNS + "");
		Literal service_address = model.createTypedLiteral(this.service.getAddress(), uri_template);
		my_service.addProperty(rdf_type, service_resource);
		my_service.addProperty(has_address, service_address);
		if (this.service.getName().length() > 0)
			my_service.addProperty(has_name, this.service.getName());
		
		if (this.service.getOperations() != null)
		for (int k = 0; k < this.service.getOperations().size(); k++) {
			Operation op = this.service.getOperations().get(k);
			Resource my_operation = model.createResource(baseNS + op.getId());
			my_service.addProperty(has_operation, my_operation);
			my_operation.addProperty(rdf_type, operation_resource);

			// operation name, address, and method
			if (op.getName().length() > 0)
				my_operation.addProperty(has_name, op.getName());
			if (op.getMethod().length() > 0)
				my_operation.addProperty(has_method, op.getMethod());
			if (op.getAddressTemplate().length() > 0) {
				Literal operation_address_literal = model.createTypedLiteral(op.getAddressTemplate(), uri_template);
				my_operation.addLiteral(has_address, operation_address_literal);
			}
			
			
			if (op.getInputAttributes() != null) {
				Resource my_input = model.createResource(baseNS + op.getId() + "_input");  
				if (op.getInputAttributes().size() > 0) {
					my_operation.addProperty(has_input, my_input);
					my_input.addProperty(rdf_type, input_resource);
				}
				for (int i = 0; i < op.getVariables().size(); i++) {
					Resource my_variable = model.createResource(baseNS + op.getVariables().get(i).toString());
					my_variable.addProperty(rdf_type, variavle_resource);
				}
				for (int i = 0; i < op.getInputAttributes().size(); i++) {
					
					Attribute att = op.getInputAttributes().get(i);
					
					Resource my_attribute = model.createResource(baseNS + att.getId());
					
					if (att.getRequirement() == AttributeRequirement.NONE)
						my_input.addProperty(has_attribute, my_attribute);
					else if (att.getRequirement() == AttributeRequirement.MANDATORY)
						my_input.addProperty(has_mandatory_attribute, my_attribute);
					else if (att.getRequirement() == AttributeRequirement.OPTIONAL)
						my_input.addProperty(has_optional_attribute, my_attribute);
					
					my_attribute.addProperty(rdf_type, attribute_resource);
					my_attribute.addProperty(has_name, att.getName());
//					my_part.addProperty(model_reference, XSDDatatype.XSDstring.getURI());
					
					Literal ground_literal = model.createTypedLiteral(
							op.getInputAttributes().get(i).getGroundedIn(), rdf_plain_literal);
					my_attribute.addLiteral(is_grounded_in, ground_literal);
				}

				addModelPart(model, my_input, op.getInputModel());
			}

			if (op.getOutputAttributes() != null) {
				Resource my_output = model.createResource(baseNS + op.getId() + "_output");  
				if (op.getOutputAttributes().size() > 0) {
					my_operation.addProperty(has_output, my_output);
					my_output.addProperty(rdf_type, output_resource);
				}
				for (int i = 0; i < op.getOutputAttributes().size(); i++) {
					
					Attribute att = op.getOutputAttributes().get(i);
					
					Resource my_attribute = model.createResource(baseNS + att.getId());

					if (att.getRequirement() == AttributeRequirement.NONE)
						my_output.addProperty(has_attribute, my_attribute);
					else if (att.getRequirement() == AttributeRequirement.MANDATORY)
						my_output.addProperty(has_mandatory_attribute, my_attribute);
					else if (att.getRequirement() == AttributeRequirement.OPTIONAL)
						my_output.addProperty(has_optional_attribute, my_attribute);

					my_attribute.addProperty(rdf_type, attribute_resource);
					my_attribute.addProperty(has_name, op.getOutputAttributes().get(i).getName());
//					my_part.addProperty(model_reference, XSDDatatype.XSDstring.getURI());
				}
				addModelPart(model, my_output, op.getOutputModel());
			}

		}
	}
	
	public void addModelPart(Model model, Resource resource, edu.isi.karma.service.Model semanticModel) {

		if (semanticModel == null)
			return;
		
		String baseNS = model.getNsPrefixURI("");
		
		Resource model_resource = model.createResource(Namespaces.KARMA + "Model");
		Resource class_atom_resource = model.createResource(Namespaces.SWRL + "ClassAtom");
		Resource individual_property_atom_resource = model.createResource(Namespaces.SWRL + "IndividualPropertyAtom");

		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_model = model.createProperty(Namespaces.KARMA, "hasModel");
		Property has_atom = model.createProperty(Namespaces.KARMA, "hasAtom");

		Property class_predicate = model.createProperty(Namespaces.SWRL, "classPredicate");
		Property property_predicate = model.createProperty(Namespaces.SWRL, "propertyPredicate");
		Property has_argument1 = model.createProperty(Namespaces.SWRL, "argument1");
		Property has_argument2 = model.createProperty(Namespaces.SWRL, "argument2");

		Resource my_model = model.createResource(baseNS + semanticModel.getId());
		my_model.addProperty(rdf_type, model_resource);
		resource.addProperty(has_model, my_model);
		
		if (semanticModel != null) {
			for (Atom atom : semanticModel.getAtoms()) {
				if (atom instanceof ClassAtom) {
					ClassAtom classAtom = (ClassAtom)atom;
					
					Resource r = model.createResource();
					r.addProperty(rdf_type, class_atom_resource);
					
					if (classAtom.getClassPredicate().getPrefix() != null && classAtom.getClassPredicate().getNs() != null)
						model.setNsPrefix(classAtom.getClassPredicate().getPrefix(), classAtom.getClassPredicate().getNs());
					Resource className = model.createResource(classAtom.getClassPredicate().getUri());
					r.addProperty(class_predicate, className);
					
					Resource arg1 = model.getResource(baseNS + classAtom.getArgument1().getUri());
					r.addProperty(has_argument1, arg1);
					
					my_model.addProperty(has_atom, r);
				}
				else if (atom instanceof PropertyAtom) {
					PropertyAtom propertyAtom = (PropertyAtom)atom;
					
					Resource r = model.createResource();
					r.addProperty(rdf_type, individual_property_atom_resource);
					
					if (propertyAtom.getPropertyPredicate().getPrefix() != null && propertyAtom.getPropertyPredicate().getNs() != null)
						model.setNsPrefix(propertyAtom.getPropertyPredicate().getPrefix(), propertyAtom.getPropertyPredicate().getNs());
					Resource propertyName = model.createResource(propertyAtom.getPropertyPredicate().getUri());
					r.addProperty(property_predicate, propertyName);
					
					Resource arg1 = model.getResource(baseNS + propertyAtom.getArgument1().getUri());
					r.addProperty(has_argument1, arg1);
					
					Resource arg2 = model.getResource(baseNS + propertyAtom.getArgument2().getUri());
					r.addProperty(has_argument2, arg2);
					
					my_model.addProperty(has_atom, r);
				}
			}
		}

	}
	
}
