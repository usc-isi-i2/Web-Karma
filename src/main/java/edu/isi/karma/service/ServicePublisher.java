/*******************************************************************************
 * Copyright 2012 University of Southern California
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this._service file except in compliance with the License.
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

import edu.isi.karma.modeling.ModelingParams;
import edu.isi.karma.modeling.Namespaces;
import edu.isi.karma.modeling.Prefixes;

public class ServicePublisher {

	static Logger logger = Logger.getLogger(ServicePublisher.class);
	
	private static Service _service = null;

	public static Model generateModel(Service service) {
		
		_service = service;
		
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = _service.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.HRESTS, Namespaces.HRESTS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

		addInvocationPart(model);
		
		return model;
		
	}
	
	public static Model generateInputPart(Service service) {
		
		_service = service;
		
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = _service.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.HRESTS, Namespaces.HRESTS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

		addInput(model, false);
		
		return model;
		
	}

	public static Model generateOutputPart(Service service) {
		
		_service = service;
		
		Model model = ModelFactory.createDefaultModel();
		
		String baseNS = _service.getUri();
		model.setNsPrefix("", baseNS);
		model.setNsPrefix(Prefixes.KARMA, Namespaces.KARMA);
		model.setNsPrefix(Prefixes.RDF, Namespaces.RDF);
		model.setNsPrefix(Prefixes.RDFS, Namespaces.RDFS);
		model.setNsPrefix(Prefixes.SWRL, Namespaces.SWRL);

		addOutput(model, false);
		
		return model;
		
	}
	
	/**
	 * 
	 * @param lang The language in which to write the model is specified by the lang argument. 
	 * Predefined values are "RDF/XML", "RDF/XML-ABBREV", "N-TRIPLE", "TURTLE", (and "TTL") and "N3". 
	 * The default value, represented by null is "RDF/XML".
	 * @throws FileNotFoundException
	 */
	public static void publish(Service service, String lang, boolean writeToFile) throws FileNotFoundException {
		
		_service = service;

		Service existingService = ServiceLoader.getServiceByAddress(_service.getAddress());
		if (existingService != null) 
			ServiceLoader.deleteServiceByUri(existingService.getUri());
		
		Model model = generateModel(_service);
		
		// update the repository active model
		Repository.Instance().addModel(model, _service.getUri());

		// write the model to the file
		if (writeToFile) {
			String service_desc_file = Repository.Instance().SERVICE_REPOSITORY_DIR + 
										_service.getId() + 
										Repository.Instance().getFileExtension(lang);
			OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(service_desc_file));
			model.write(output,lang);	
		}
	}
	
	public static void addInvocationPart(Model model) {
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource service_resource = model.createResource(Namespaces.KARMA + "Service");
		Resource variavle_resource = model.createResource(Namespaces.SWRL + "Variable");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_address = model.createProperty(Namespaces.HRESTS, "hasAddress");
		Property has_method = model.createProperty(Namespaces.HRESTS, "hasMethod");
		Property has_input = model.createProperty(Namespaces.KARMA, "hasInput");
		Property has_output = model.createProperty(Namespaces.KARMA, "hasOutput");
		Property has_name = model.createProperty(Namespaces.KARMA, "hasName");
		Property has_variable = model.createProperty(Namespaces.KARMA, "hasVariable");
		
		// rdf datatypes
		String uri_template = Namespaces.HRESTS + "URITemplate";

		Resource my_service = model.createResource(baseNS + "");
		Literal service_address = model.createTypedLiteral(_service.getAddress(), uri_template);
		my_service.addProperty(rdf_type, service_resource);
		my_service.addProperty(has_address, service_address);
		if (_service.getName().length() > 0)
			my_service.addProperty(has_name, _service.getName());
		
		if (_service.getMethod().length() > 0)
			my_service.addProperty(has_method, _service.getMethod());
		if (_service.getAddress().length() > 0) {
			Literal operation_address_literal = model.createTypedLiteral(_service.getAddress(), uri_template);
			my_service.addLiteral(has_address, operation_address_literal);
		}
		
		if (_service.getVariables() != null)
		for (int i = 0; i < _service.getVariables().size(); i++) {
			Resource my_variable = model.createResource(baseNS + _service.getVariables().get(i).toString());
			my_variable.addProperty(rdf_type, variavle_resource);
			my_service.addProperty(has_variable, my_variable);
		}
		
		//for source description
		Property hasSourceDesc = model.createProperty(Namespaces.KARMA, "hasSourceDescription");
		String sourceDescription = _service.getSourceDescription();
		sourceDescription = sourceDescription.replaceAll("\n", " ").replaceAll("\r", " ");
		my_service.addProperty(hasSourceDesc, sourceDescription);
			
		Resource my_input = addInput(model, true);
		if (my_input != null) my_service.addProperty(has_input, my_input);

		Resource my_output = addOutput(model, true);
		if (my_output != null) my_service.addProperty(has_output, my_output);

	}
	
	private static Resource addInput(Model model, boolean includeAttributeDetails) {
		
		Resource my_input = null;
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource input_resource = model.createResource(Namespaces.KARMA + "Input");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_attribute = model.createProperty(Namespaces.KARMA, "hasAttribute");
		Property has_mandatory_attribute = model.createProperty(Namespaces.KARMA, "hasMandatoryAttribute");
		Property has_optional_attribute = model.createProperty(Namespaces.KARMA, "hasOptionalAttribute");

		if (_service.getInputAttributes() != null) {
			
			my_input = model.createResource(baseNS + "input");  
			if (_service.getInputAttributes().size() > 0) {
				my_input.addProperty(rdf_type, input_resource);
			}
			for (int i = 0; i < _service.getInputAttributes().size(); i++) {
				
				Attribute att = _service.getInputAttributes().get(i);
				Resource my_attribute = model.createResource(baseNS + att.getId());
				
				if (includeAttributeDetails)
					addAttributeDetails(model, my_attribute, att);
				
				if (att.getRequirement() == AttributeRequirement.NONE)
					my_input.addProperty(has_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.MANDATORY)
					my_input.addProperty(has_mandatory_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.OPTIONAL)
					my_input.addProperty(has_optional_attribute, my_attribute);

			}

			addModelPart(model, my_input, _service.getInputModel());
		}
		
		return my_input;
	}

	private static Resource addOutput(Model model, boolean includeAttributeDetails) {
		
		Resource my_output = null;
		
		String baseNS = model.getNsPrefixURI("");
		// resources
		Resource output_resource = model.createResource(Namespaces.KARMA + "Output");

		// properties
		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_attribute = model.createProperty(Namespaces.KARMA, "hasAttribute");
		Property has_mandatory_attribute = model.createProperty(Namespaces.KARMA, "hasMandatoryAttribute");
		Property has_optional_attribute = model.createProperty(Namespaces.KARMA, "hasOptionalAttribute");

		if (_service.getOutputAttributes() != null) {
			my_output = model.createResource(baseNS + "output");  
			if (_service.getOutputAttributes().size() > 0) {
				my_output.addProperty(rdf_type, output_resource);
			}
			for (int i = 0; i < _service.getOutputAttributes().size(); i++) {
				
				Attribute att = _service.getOutputAttributes().get(i);
				Resource my_attribute = model.createResource(baseNS + att.getId());
				
				if (includeAttributeDetails)
					addAttributeDetails(model, my_attribute, att);

				if (att.getRequirement() == AttributeRequirement.NONE)
					my_output.addProperty(has_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.MANDATORY)
					my_output.addProperty(has_mandatory_attribute, my_attribute);
				else if (att.getRequirement() == AttributeRequirement.OPTIONAL)
					my_output.addProperty(has_optional_attribute, my_attribute);

			}
			addModelPart(model, my_output, _service.getOutputModel());
		}
		return my_output;
	}

	private static void addAttributeDetails(Model model, Resource my_attribute, Attribute att) {

		Resource attribute_resource = model.createResource(Namespaces.KARMA + "Attribute");

		Property rdf_type = model.createProperty(Namespaces.RDF , "type");
		Property has_name = model.createProperty(Namespaces.KARMA, "hasName");
		Property is_grounded_in = model.createProperty(Namespaces.HRESTS, "isGroundedIn");

		my_attribute.addProperty(rdf_type, attribute_resource);
		my_attribute.addProperty(has_name, att.getName());
//			my_part.addProperty(model_reference, XSDDatatype.XSDstring.getURI());
		
		if (att.getIOType() == IOType.INPUT) {
			String rdf_plain_literal = Namespaces.RDF + "PlainLiteral";
			Literal ground_literal = model.createTypedLiteral(att.getGroundedIn(), rdf_plain_literal);
			my_attribute.addLiteral(is_grounded_in, ground_literal);
		}
		
	}

	private static void addModelPart(Model model, Resource resource, edu.isi.karma.service.Model semanticModel) {

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
					Resource className = model.createResource(classAtom.getClassPredicate().getUriString());
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
					Resource propertyName = model.createResource(propertyAtom.getPropertyPredicate().getUriString());
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
	
	public static void main(String[] args) throws FileNotFoundException {
		String serviceUri = ModelingParams.KARMA_SERVICE_PREFIX + "CDA81BE4-DD77-E0D3-D033-FC771B2F4800" + "#";
		Service service = ServiceLoader.getServiceByUri(serviceUri);
		
		String service_file = Repository.Instance().SERVICE_REPOSITORY_DIR + 
									"service" + 
									Repository.Instance().getFileExtension(SerializationLang.N3);
		
		String service_input_file = Repository.Instance().SERVICE_REPOSITORY_DIR + 
									"input" + 
									Repository.Instance().getFileExtension(SerializationLang.N3);

		String service_output_file = Repository.Instance().SERVICE_REPOSITORY_DIR + 
									"output" + 
									Repository.Instance().getFileExtension(SerializationLang.N3);

		OutputStreamWriter outputService = new OutputStreamWriter(new FileOutputStream(service_file));
		OutputStreamWriter outputServiceInput = new OutputStreamWriter(new FileOutputStream(service_input_file));
		OutputStreamWriter outputServiceOutput = new OutputStreamWriter(new FileOutputStream(service_output_file));

		com.hp.hpl.jena.rdf.model.Model model = ServicePublisher.generateModel(service);
		com.hp.hpl.jena.rdf.model.Model inputModel = ServicePublisher.generateInputPart(service);
		com.hp.hpl.jena.rdf.model.Model outputModel = ServicePublisher.generateOutputPart(service);
		
		model.write(outputService,SerializationLang.N3);		
		inputModel.write(outputServiceInput,SerializationLang.N3);		
		outputModel.write(outputServiceOutput,SerializationLang.N3);		
	}
}
