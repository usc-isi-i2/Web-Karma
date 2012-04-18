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
//		Model model = Repository.Instance().getNamedModel(baseNS);
//		ModelMaker maker = ModelFactory.createMemModelMaker();
//		Model model = maker.createModel(baseNS);
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = service.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
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
		
		Service existingService = ServiceLoader.getServiceByAddress(this.service.getAddress());
		if (existingService != null) 
			ServiceLoader.deleteServiceByUri(existingService.getUri());
		
		if (this.model == null)
			model = generateModel();
		
		// update the repository active model
		Repository.Instance().addModel(this.model, service.getUri());

		// write the model to the file
		if (writeToFile)
			writeToFile(lang);
	}
	
	public void writeToFile(String lang) throws FileNotFoundException {
		if (this.model == null)
			model = generateModel();
		
		String service_desc_file = Repository.Instance().SERVICE_REPOSITORY_DIR + 
									this.service.getId() + 
									Repository.Instance().getFileExtension(lang);
		
		OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(service_desc_file));
		model.write(output,lang);		
//		model.write(output,"RDF/XML-ABBREV");
//		model.write(output,"N-TRIPLE");
//		model.write(output,"RDF/XML");
		
	}
	
	public void addInvocationPart(Model model) {
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource service_resource = model.createResource(Namespaces.KARMA + "Service");
		Resource input_resource = model.createResource(Namespaces.KARMA + "Input");
		Resource output_resource = model.createResource(Namespaces.KARMA + "Output");
		Resource attribute_resource = model.createResource(Namespaces.KARMA + "Attribute");
		Resource variavle_resource = model.createResource(Namespaces.SWRL + "Variable");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
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
		
		if (this.service.getMethod().length() > 0)
			my_service.addProperty(has_method, this.service.getMethod());
		if (this.service.getAddress().length() > 0) {
			Literal operation_address_literal = model.createTypedLiteral(this.service.getAddress(), uri_template);
			my_service.addLiteral(has_address, operation_address_literal);
		}
			
		if (this.service.getInputAttributes() != null) {
			Resource my_input = model.createResource(baseNS + "input");  
			if (this.service.getInputAttributes().size() > 0) {
				my_service.addProperty(has_input, my_input);
				my_input.addProperty(rdf_type, input_resource);
			}
			for (int i = 0; i < this.service.getVariables().size(); i++) {
				Resource my_variable = model.createResource(baseNS + this.service.getVariables().get(i).toString());
				my_variable.addProperty(rdf_type, variavle_resource);
			}
			for (int i = 0; i < this.service.getInputAttributes().size(); i++) {
				
				Attribute att = this.service.getInputAttributes().get(i);
				
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
						this.service.getInputAttributes().get(i).getGroundedIn(), rdf_plain_literal);
				my_attribute.addLiteral(is_grounded_in, ground_literal);
			}

			addModelPart(model, my_input, this.service.getInputModel());
		}

		if (this.service.getOutputAttributes() != null) {
			Resource my_output = model.createResource(baseNS + "output");  
			if (this.service.getOutputAttributes().size() > 0) {
				my_service.addProperty(has_output, my_output);
				my_output.addProperty(rdf_type, output_resource);
			}
			for (int i = 0; i < this.service.getOutputAttributes().size(); i++) {
				
				Attribute att = this.service.getOutputAttributes().get(i);
				
				Resource my_attribute = model.createResource(baseNS + att.getId());

				if (att.getRequirement() == AttributeRequirement.NONE)
					my_output.addProperty(has_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.MANDATORY)
					my_output.addProperty(has_mandatory_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.OPTIONAL)
					my_output.addProperty(has_optional_attribute, my_attribute);

				my_attribute.addProperty(rdf_type, attribute_resource);
				my_attribute.addProperty(has_name, this.service.getOutputAttributes().get(i).getName());
//					my_part.addProperty(model_reference, XSDDatatype.XSDstring.getURI());
			}
			addModelPart(model, my_output, this.service.getOutputModel());
		}

	}
	
	public void addModelPart(Model model, Resource resource, edu.isi.karma.service.Model semanticModel) {

		if (semanticModel == null) {
			logger.info("The semantic model is null");
			return;
		}
		
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
					
					Resource arg1 = model.getResource(baseNS + classAtom.getArgument1().getAttOrVarId());
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
					
					Resource arg1 = model.getResource(baseNS + propertyAtom.getArgument1().getAttOrVarId());
					r.addProperty(has_argument1, arg1);
					
					Resource arg2 = model.getResource(baseNS + propertyAtom.getArgument2().getAttOrVarId());
					r.addProperty(has_argument2, arg2);
					
					my_model.addProperty(has_atom, r);
				}
			}
		}

	}
	
	public static void main(String[] args) {
		ServiceBuilder.main(new String[0]);
	}
}
