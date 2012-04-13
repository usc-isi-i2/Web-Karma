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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.util.RandomGUID;

public class Service {
	
	private String id;
	private String name;
	private String address;
	private String description;

	private List<Operation> operations;

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Operation> getOperations() {
		return operations;
	}

	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

	public Model publish(String path) throws FileNotFoundException {
		
		OntModel model = ModelFactory.createOntologyModel();
		
		String serviceGUID = new RandomGUID().toString();
		String defaultNS = Namespaces.KARMA_SERVICE_NS + serviceGUID + "/";
		model.setNsPrefix("", defaultNS);
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
		
		String service_desc_file = ServiceRepository.Instance().SERVICE_REPOSITORY_DIR + serviceGUID + ".rdf";
		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(service_desc_file));

//		model.write(output,"RDF/XML-ABBREV");
//		model.write(output,"N-TRIPLE");
//		model.write(output,"RDF/XML");
		model.write(output,"N3");
		return model;
		
	}
	
	public void addInvocationPart(Model model) {
		
		String defaultNS = model.getNsPrefixURI("");
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

		Resource my_service = model.createResource(defaultNS + this.getId());
		Literal service_address = model.createTypedLiteral(this.getAddress(), uri_template);
		my_service.addProperty(rdf_type, service_resource);
		my_service.addProperty(has_address, service_address);
		if (this.getName().length() > 0)
			my_service.addProperty(has_name, this.getName());
		
		if (this.getOperations() != null)
		for (int k = 0; k < this.getOperations().size(); k++) {
			Operation op = this.getOperations().get(k);
			Resource my_operation = model.createResource(defaultNS + op.getId());
			if (op.getName().length() > 0)
				my_operation.addProperty(has_name, op.getName());
			my_service.addProperty(has_operation, my_operation);
			my_operation.addProperty(rdf_type, operation_resource);

			// operation name, address, and method
			my_operation.addProperty(has_name, op.getName());
			my_operation.addProperty(has_method, op.getMethod());
			if (op.getAddressTemplate().length() > 0) {
				Literal operation_address_literal = model.createTypedLiteral(op.getAddressTemplate(), uri_template);
				my_operation.addLiteral(has_address, operation_address_literal);
			}
			
			
			if (op.getInputAttributes() != null) {
				Resource my_input = model.createResource(defaultNS + op.getId() + "_input");  
				if (op.getInputAttributes().size() > 0) {
					my_operation.addProperty(has_input, my_input);
					my_input.addProperty(rdf_type, input_resource);
				}
				for (int i = 0; i < op.getVariables().size(); i++) {
					Resource my_variable = model.createResource(defaultNS + op.getVariables().get(i).toString());
					my_variable.addProperty(rdf_type, variavle_resource);
				}
				for (int i = 0; i < op.getInputAttributes().size(); i++) {
					
					Attribute att = op.getInputAttributes().get(i);
					
					Resource my_attribute = model.createResource(defaultNS + att.getId());
					
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

				addModelPart(model, my_input, op.getInputModel(), op.getId() + "_inputModel");
			}

			if (op.getOutputAttributes() != null) {
				Resource my_output = model.createResource(defaultNS + op.getId() + "_output");  
				if (op.getOutputAttributes().size() > 0) {
					my_operation.addProperty(has_output, my_output);
					my_output.addProperty(rdf_type, output_resource);
				}
				for (int i = 0; i < op.getOutputAttributes().size(); i++) {
					
					Attribute att = op.getOutputAttributes().get(i);
					
					Resource my_attribute = model.createResource(defaultNS + att.getId());

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
				addModelPart(model, my_output, op.getOutputModel(), op.getId() + "_outputModel");
			}

		}
	}
	
	public void addModelPart(Model model, Resource resource, edu.isi.karma.service.Model semanticModel, String modelName) {

		if (semanticModel == null)
			return;
		
		String defaultNS = model.getNsPrefixURI("");
		
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

		Resource my_model = model.createResource(defaultNS + modelName);
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
					
					Resource arg1 = model.getResource(defaultNS + classAtom.getArgument1().getUri());
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
					
					Resource arg1 = model.getResource(defaultNS + propertyAtom.getArgument1().getUri());
					r.addProperty(has_argument1, arg1);
					
					Resource arg2 = model.getResource(defaultNS + propertyAtom.getArgument2().getUri());
					r.addProperty(has_argument2, arg2);
					
					my_model.addProperty(has_atom, r);
				}
			}
		}

	}


	public void print() {
		System.out.println("address: " + this.getAddress());
		System.out.println("name: " + this.getName());
		System.out.println("description: " + this.getDescription());
		System.out.println("----------------------");
		System.out.println("operations: ");
		for (Operation op: getOperations())
			op.print();
	}
	
	public static void main(String[] args) {
		
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
//	//Example
//	@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>.
//		@prefix swrl: <http://www.w3.org/2003/11/swrl#>.
//		@prefix owl: <http://www.w3.org/2002/07/owl>.
//		@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.
//		@prefix eg: <http://example.org/example-ont#>.
//		_:bnode1164532928 a swrl:IndividualPropertyAtom;
//			swrl:argument1 <:x1>;
//			swrl:argument2 <:x2>;
//			swrl:propertyPredicate <eg:hasParent>.
//		_:bnode127092800 a swrl:IndividualPropertyAtom;
//			swrl:argument1 <:x2>;
//			swrl:argument2 <:x3>;
//			swrl:propertyPredicate <eg:hasSibling>.
//		_:bnode1947787456 a swrl:IndividualPropertyAtom;
//			swrl:argument1 <:x1>;
//			swrl:argument2 <:x3>;
//			swrl:propertyPredicate <eg:hasUncle>.
//		_:bnode2092994240 a swrl:Imp;
//			swrl:body (_:bnode1164532928 _:bnode127092800 _:bnode910347328);
//			swrl:head (_:bnode1947787456).
//		_:bnode910347328 a swrl:IndividualPropertyAtom;
//			swrl:argument1 <:x3>;
//			swrl:argument2 <:male>;
//			swrl:propertyPredicate <eg:hasSex>.
}
